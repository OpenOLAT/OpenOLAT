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
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
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
public class MergeTaxonomyLevelController extends FormBasicController {

	private SingleSelection mergeToEl;
	
	private List<TaxonomyLevel> levels;
	private final TaxonomyModel treeModel;
	private final Set<String> selectedNodeIds = new HashSet<>();
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TaxonomyService taxonomyService;
	
	public MergeTaxonomyLevelController(UserRequest ureq, WindowControl wControl,
			List<TaxonomyLevel> levels, Taxonomy taxonomy) {
		super(ureq, wControl, "merge_taxonomy_levels");
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
		List<String> keyList = new ArrayList<>();
		List<String> valueList = new ArrayList<>();
		buildMergeToSelection(treeModel.getRootNode(), "", keyList, valueList);
		mergeToEl = uifactory.addDropdownSingleselect("mergeto", "merge.to", formLayout,
				keyList.toArray(new String[keyList.size()]), valueList.toArray(new String[valueList.size()]), null);
		mergeToEl.setEscapeHtml(false);
		
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("merge.taxonomy.level", formLayout);
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
	
	private void initErrorMessage(FormItemContainer formLayout, UserRequest ureq) {
		String errorMsg = translate("error.discontinuous.selection");
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("errorMsg", errorMsg);
		}
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		mergeToEl.clearError();
		if(!mergeToEl.isOneSelected()) {
			mergeToEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(levels.size() > 1) {
			Collections.sort(levels, new TaxonomyLevelDepthComparator());
		}

		String selectedNodeKey = mergeToEl.getSelectedKey();
		TreeNode selectedNode = treeModel.getNodeById(selectedNodeKey);
		TaxonomyLevel mergeTo = (TaxonomyLevel)selectedNode.getUserObject();

		Translator taxonomyTranslator = Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale());
		StringBuilder sb = new StringBuilder();
		for(TaxonomyLevel level:levels) {
			TaxonomyLevel taxonomyLevel = taxonomyService.getTaxonomyLevel(level);
			if(taxonomyService.deleteTaxonomyLevel(taxonomyLevel, mergeTo)) {
				if(sb.length() > 0) sb.append(", ");
				sb.append(StringHelper.escapeHtml(TaxonomyUIFactory.translateDisplayName(taxonomyTranslator, taxonomyLevel)));
			}
		}
		
		dbInstance.commit();//commit before sending event
		fireEvent(ureq, new DeleteTaxonomyLevelEvent());
		showInfo("confirm.merge.level", new String[] { sb.toString() });
	}
}
