/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.taxonomy.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.manager.TaxonomyAllTreesBuilder;
import org.olat.modules.taxonomy.model.TaxonomyModel;
import org.olat.modules.taxonomy.ui.component.TaxonomyLevelDepthComparator;
import org.olat.modules.taxonomy.ui.events.DeleteTaxonomyLevelEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 déc. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DeleteTaxonomyLevelController extends FormBasicController {
	
	private List<TaxonomyLevel> levels;
	private final TaxonomyModel treeModel;
	private final Set<String> selectedNodeIds = new HashSet<>();
	
	private SingleSelection mergeToEl;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TaxonomyService taxonomyService;
	
	public DeleteTaxonomyLevelController(UserRequest ureq, WindowControl wControl,
			List<TaxonomyLevel> levels, Taxonomy taxonomy) {
		super(ureq, wControl, "confirm_delete_levels");
		this.levels = levels;
		
		treeModel = new TaxonomyAllTreesBuilder(getLocale()).buildTreeModel(taxonomy);
		for(TaxonomyLevel level:levels) {
			selectedNodeIds.add(TaxonomyAllTreesBuilder.nodeKey(level));
		}
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(treeModel.validateContinuousSelection(levels)) {
			initForm(formLayout, ureq);
		} else {
			initErrorMessage(formLayout, ureq);
		}
	}
	
	private void initForm(FormItemContainer formLayout, UserRequest ureq) {
		StringBuilder sb = new StringBuilder();
		for(TaxonomyLevel level:levels) {
			if(sb.length() > 0) sb.append(", ");
			sb.append(StringHelper.escapeHtml(TaxonomyUIFactory.translateDisplayName(getTranslator(), level)));
		}
		
		boolean canDelete = true;
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			String text = translate("confirmation.delete.level", new String[] { sb.toString() });
			layoutCont.contextPut("msg", text);
			layoutCont.contextPut("msgForList", translate("error.delete.num"));
			canDelete &= buildDangerMessage(layoutCont);
		}
		
		List<String> keyList = new ArrayList<>();
		List<String> valueList = new ArrayList<>();
		keyList.add("-");
		valueList.add("-");
		buildMergeToSelection(treeModel.getRootNode(), "", keyList, valueList);
		mergeToEl = uifactory.addDropdownSingleselect("mergeto", "merge.to", formLayout,
				keyList.toArray(new String[keyList.size()]), valueList.toArray(new String[valueList.size()]), null);
		mergeToEl.setEscapeHtml(false);

		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		if(canDelete) {
			uifactory.addFormSubmitButton("delete", formLayout);
		}
	}
	
	private void initErrorMessage(FormItemContainer formLayout, UserRequest ureq) {
		String errorMsg = translate("error.discontinuous.selection");
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("errorMsg", errorMsg);
		}
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}
	
	private void buildMergeToSelection(TreeNode node, String identation, List<String> keys, List<String> values) {
		String childIndentation = identation + "&nbsp;&nbsp;";
		int numOfChildren = node.getChildCount();
		for(int i=0; i<numOfChildren; i++) {
			TreeNode child = (TreeNode)node.getChildAt(i);
			if(!selectedNodeIds.contains(child.getIdent())) {
				keys.add(child.getIdent());
				values.add(identation + StringHelper.escapeHtml(child.getTitle()));
				buildMergeToSelection(child, childIndentation, keys, values);
			}
		}
	}
	
	/**
	 * 
	 * @param layoutCont The layout container
	 * @return false if the taxonomy level cannot be deleted
	 */
	private boolean buildDangerMessage(FormLayoutContainer layoutCont) {
		List<String> messages = new ArrayList<>();
		
		// child (no partially selection allowed for deletion)
		Set<TreeNode> childrenToDelete = new HashSet<>();
		for(TaxonomyLevel level:levels) {
			String nodeId = treeModel.nodeKey(level);
			TreeNode node = treeModel.getNodeById(nodeId);
			childrenToDelete.addAll(treeModel.getDescendants(node));
		}
		if(!childrenToDelete.isEmpty()) {
			messages.add(translate("error.delete.num.child.levels", new String[]{ Integer.toString(childrenToDelete.size()) }));
		}
		
		List<TaxonomyLevelRef> refs = new ArrayList<>(levels);
		childrenToDelete
			.stream()
			.forEach(c -> refs.add((TaxonomyLevelRef)c.getUserObject()));
		
		// questions / relations / references
		int relations = taxonomyService.countRelations(refs);
		if(relations > 0) {
			messages.add(translate("error.delete.num.relations", new String[]{ Integer.toString(relations) }));
		}
		
		int surveys = taxonomyService.countQualityManagementsRelations(refs);
		if(surveys > 0) {
			messages.add(translate("error.delete.num.surveys", new String[]{ Integer.toString(surveys) }));
		}
		
		// competences
		int competences = taxonomyService.countTaxonomyCompetences(refs);
		if(competences > 0) {
			messages.add(translate("error.delete.num.competences", new String[]{ Integer.toString(competences) }));
		}

		layoutCont.contextPut("messages", messages);
		layoutCont.contextPut("mergeToMessage", translate("info.delete.merge.to"));	
		
		return surveys == 0;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		StringBuilder deletedLevels = new StringBuilder();
		StringBuilder notDeletedLevels = new StringBuilder();
		
		if(levels.size() > 1) {
			Collections.sort(levels, new TaxonomyLevelDepthComparator());
		}
		
		TaxonomyLevel mergeTo = null;
		if(mergeToEl.isOneSelected() && !"-".equals(mergeToEl.getSelectedKey())) {
			String selectedNodeKey = mergeToEl.getSelectedKey();
			TreeNode selectedNode = treeModel.getNodeById(selectedNodeKey);
			mergeTo = (TaxonomyLevel)selectedNode.getUserObject();
		}

		for(TaxonomyLevel level:levels) {
			TaxonomyLevel taxonomyLevel = taxonomyService.getTaxonomyLevel(level);
			if(taxonomyService.deleteTaxonomyLevel(taxonomyLevel, mergeTo)) {
				if(deletedLevels.length() > 0) deletedLevels.append(", ");
				deletedLevels.append(StringHelper.escapeHtml(TaxonomyUIFactory.translateDisplayName(getTranslator(), taxonomyLevel)));
			} else {
				if(notDeletedLevels.length() > 0) notDeletedLevels.append(", ");
				notDeletedLevels.append(StringHelper.escapeHtml(TaxonomyUIFactory.translateDisplayName(getTranslator(), taxonomyLevel)));
			}
		}
		dbInstance.commit();//commit before sending event
		fireEvent(ureq, new DeleteTaxonomyLevelEvent());
	
		if(notDeletedLevels.length() == 0) {
			showInfo("confirm.deleted.level", new String[] { deletedLevels.toString() });
		} else if(deletedLevels.length() == 0) {
			if(levels.size() == 1) {
				showWarning("warning.delete.level");
			} else {
				showWarning("warning.deleted.level.nothing");
			}
		} else {
			showWarning("warning.deleted.level.partial", new String[] {
					deletedLevels.toString(), notDeletedLevels.toString() });
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
