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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTreeItem;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.nodes.INode;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelType;
import org.olat.modules.taxonomy.TaxonomyLevelTypeToType;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.manager.TaxonomyAllTreesBuilder;
import org.olat.modules.taxonomy.model.TaxonomyModel;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 8 déc. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MoveTaxonomyLevelController extends FormBasicController {
	
	private MenuTreeItem taxonomyEl;
	private TaxonomyModel taxonomyModel;
	
	private final Taxonomy taxonomy;
	private TaxonomyLevel movedLevel;
	private List<TaxonomyLevel> levelsToMove;
	private Set<TaxonomyLevelType> allowedTypes;
	private Set<TreeNode> targetableNodes = new HashSet<>();
	
	@Autowired
	private TaxonomyService taxonomyService;
	
	public MoveTaxonomyLevelController(UserRequest ureq, WindowControl wControl,
			List<TaxonomyLevel> levelsToMove, Taxonomy taxonomy) {
		super(ureq, wControl, "move_taxonomy_level");
		this.taxonomy = taxonomy;
		this.levelsToMove = levelsToMove;
		allowedTypes = getAllowedTypes();
		
		initForm(ureq);
		loadModel();
	}
	
	public TaxonomyLevel getMovedTaxonomyLevel() {
		return movedLevel;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		taxonomyModel = new TaxonomyModel();
		taxonomyEl = uifactory.addTreeMultiselect("taxonomy", null, formLayout, taxonomyModel, this);
		taxonomyEl.setMultiSelect(false);
		taxonomyEl.setRootVisible(true);

		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("move.taxonomy.level", formLayout);
	}
	
	private void loadModel() {
		new TaxonomyAllTreesBuilder().loadTreeModel(taxonomyModel, taxonomy);
		//remove children of the level to move
		for(TaxonomyLevel levelToMove:levelsToMove) {
			TreeNode nodeToMove = taxonomyModel
					.getNodeById(TaxonomyAllTreesBuilder.nodeKey(levelToMove));
			if(nodeToMove != null) {
				nodeToMove.removeAllChildren();
			}
		}
		
		// remove the level with
		
		List<TreeNode> openedNodes = new ArrayList<>();
		filterByAllowedTypes(taxonomyModel.getRootNode(), openedNodes);
		taxonomyModel.sort(taxonomyModel.getRootNode());

		List<String> nodeIds = openedNodes
				.stream().map(node -> node.getIdent())
				.collect(Collectors.toList());
		taxonomyEl.setOpenNodeIds(nodeIds);
	}
	
	private boolean filterByAllowedTypes(TreeNode node, List<TreeNode> openedNodes) {
		((GenericTreeNode)node).setIconCssClass(null);
		
		for(int i=node.getChildCount(); i-->0; ) {
			boolean ok = filterByAllowedTypes((TreeNode)node.getChildAt(i), openedNodes);
			if(!ok) {
				node.remove(node.getChildAt(i));
			}
		}
		
		boolean ok = false;
		Object uobject = node.getUserObject();
		if(uobject instanceof TaxonomyLevel) {
			TaxonomyLevel level = (TaxonomyLevel)uobject;
			TaxonomyLevelType type = level.getType();
			if(type == null || allowedTypes.contains(type)) {
				openedNodes.add(node);
				((GenericTreeNode)node).setIconCssClass("o_icon_node_under o_icon-rotate-180");
				targetableNodes.add(node);
				ok = true;
			} else if(node.getChildCount() > 0) {
				openedNodes.add(node);
				ok = true;
			}
		} else {
			targetableNodes.add(node);
			openedNodes.add(node);
			ok = true;
		}

		return ok;
	}
	
	private Set<TaxonomyLevelType> getAllowedTypes() {
		List<TaxonomyLevelType> allTypes = new ArrayList<>(taxonomyService.getTaxonomyLevelTypes(taxonomy));
		Map<TaxonomyLevelType, Set<TaxonomyLevelType>> subToParentTypes = new HashMap<>();
		for(TaxonomyLevelType type:allTypes) {
			Set<TaxonomyLevelTypeToType> typesToTypes = type.getAllowedTaxonomyLevelSubTypes();
			for(TaxonomyLevelTypeToType typeToType:typesToTypes) {
				TaxonomyLevelType subTyp = typeToType.getAllowedSubTaxonomyLevelType();
				subToParentTypes
					.computeIfAbsent(subTyp, t -> new HashSet<>())
					.add(type);
			}
		}
		
		Set<TaxonomyLevelType> analyzedTypes = new HashSet<>();
		for(TaxonomyLevel level:levelsToMove) {
			TaxonomyLevelType levelType = level.getType();
			if(levelType != null && !analyzedTypes.contains(levelType)) {
				analyzedTypes.add(levelType);
				
				Set<TaxonomyLevelType> allowed = subToParentTypes.get(levelType);
				if(allowed != null) {
					allTypes.retainAll(allowed);
				}
			}
		}

		return new HashSet<>(allTypes);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		taxonomyEl.clearError();
		if(taxonomyEl.getSelectedNode() == null) {
			taxonomyEl.setErrorKey("error.select.target.level", null);
			allOk &= false;
		} else if(isParent()) {
			taxonomyEl.setErrorKey("error.target.no.parent", null);
			allOk &= false;
		} else if(!targetableNodes.contains(taxonomyEl.getSelectedNode())) {
			taxonomyEl.setErrorKey("error.target.not.allowed", null);
			allOk &= false;
		}

		return allOk;
	}
	
	private boolean isParent() {
		boolean parent = false;
		for(TaxonomyLevel levelToMove:levelsToMove) {
			parent |= isParent(levelToMove);
		}
		return parent;
	}
	
	private boolean isParent(TaxonomyLevel levelToMove) {
		TreeNode nodeToMove = taxonomyModel
				.getNodeById(TaxonomyAllTreesBuilder.nodeKey(levelToMove));
		TreeNode selectedNode = taxonomyEl.getSelectedNode();
		if(selectedNode == taxonomyModel.getRootNode()) {
			return false;//can move to root
		}
		for(INode node=nodeToMove; node != null; node = node.getParent()) {
			if(selectedNode == node) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(isParent()) {
			showWarning("error.target.no.parent");
		} else {
			TreeNode selectedNode = taxonomyEl.getSelectedNode();
			if(selectedNode == taxonomyModel.getRootNode()) {
				for(TaxonomyLevel levelToMove:levelsToMove) {
					movedLevel = taxonomyService.moveTaxonomyLevel(levelToMove, null);
				}
			} else {
				TaxonomyLevel newParentLevel = (TaxonomyLevel)selectedNode.getUserObject();
				for(TaxonomyLevel levelToMove:levelsToMove) {
					movedLevel = taxonomyService.moveTaxonomyLevel(levelToMove, newParentLevel);
				}
			}
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
