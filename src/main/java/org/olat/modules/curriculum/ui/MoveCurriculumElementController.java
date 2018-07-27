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
package org.olat.modules.curriculum.ui;

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
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumElementTypeToType;
import org.olat.modules.curriculum.CurriculumService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 8 déc. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MoveCurriculumElementController extends FormBasicController {
	
	private MenuTreeItem curriculumTreeEl;
	private final CurriculumTreeModel curriculumModel = new CurriculumTreeModel();
	
	private final Curriculum curriculum;
	private Set<CurriculumElementType> allowedTypes;
	private List<CurriculumElement> curriculumElementsToMove;
	private Set<TreeNode> targetableNodes = new HashSet<>();
	
	@Autowired
	private CurriculumService curriculumService;
	
	public MoveCurriculumElementController(UserRequest ureq, WindowControl wControl,
			List<CurriculumElement> curriculumElementsToMove, Curriculum curriculum) {
		super(ureq, wControl, "move_curriculum_element");
		this.curriculum = curriculum;
		this.curriculumElementsToMove = new ArrayList<>(curriculumElementsToMove);
		allowedTypes = getAllowedTypes();
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		curriculumTreeEl = uifactory.addTreeMultiselect("elements", null, formLayout, curriculumModel, this);
		curriculumTreeEl.setMultiSelect(false);
		curriculumTreeEl.setRootVisible(false);

		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("move.element", formLayout);
	}
	
	private void loadModel() {
		List<CurriculumElement> allElements = curriculumService.getCurriculumElements(curriculum, CurriculumElementStatus.notDeleted());
		curriculumModel.loadTreeModel(allElements);
		
		//remove children of the curriculum element to move
		for(CurriculumElement elementToMove:curriculumElementsToMove) {
			TreeNode nodeToMove = curriculumModel
					.getNodeById(CurriculumTreeModel.nodeKey(elementToMove));
			nodeToMove.removeAllChildren();
			if(nodeToMove.getParent() != null) {
				nodeToMove.getParent().remove(nodeToMove);
			}
		}
		
		// remove the elements with incompatible types
		List<TreeNode> openedNodes = new ArrayList<>();
		filterByAllowedTypes(curriculumModel.getRootNode(), openedNodes);

		List<String> nodeIds = openedNodes
				.stream().map(TreeNode::getIdent)
				.collect(Collectors.toList());
		curriculumTreeEl.setOpenNodeIds(nodeIds);
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
		if(uobject instanceof CurriculumElement) {
			CurriculumElement level = (CurriculumElement)uobject;
			CurriculumElementType type = level.getType();
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
	
	private Set<CurriculumElementType> getAllowedTypes() {
		List<CurriculumElementType> allTypes = new ArrayList<>(curriculumService.getCurriculumElementTypes());
		Map<CurriculumElementType, Set<CurriculumElementType>> subToParentTypes = new HashMap<>();
		for(CurriculumElementType type:allTypes) {
			Set<CurriculumElementTypeToType> typesToTypes = type.getAllowedSubTypes();
			for(CurriculumElementTypeToType typeToType:typesToTypes) {
				CurriculumElementType subTyp = typeToType.getAllowedSubType();
				subToParentTypes
					.computeIfAbsent(subTyp, t -> new HashSet<>())
					.add(type);
			}
		}
		
		Set<CurriculumElementType> analyzedTypes = new HashSet<>();
		for(CurriculumElement element:curriculumElementsToMove) {
			CurriculumElementType levelType = element.getType();
			if(levelType != null && !analyzedTypes.contains(levelType)) {
				analyzedTypes.add(levelType);
				
				Set<CurriculumElementType> allowed = subToParentTypes.get(levelType);
				if(allowed != null) {
					allTypes.retainAll(allowed);
				}
			}
		}

		return new HashSet<>(allTypes);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		curriculumTreeEl.clearError();
		if(curriculumTreeEl.getSelectedNode() == null) {
			curriculumTreeEl.setErrorKey("error.select.target.level", null);
			allOk &= false;
		} else if(isParent()) {
			curriculumTreeEl.setErrorKey("error.target.no.parent", null);
			allOk &= false;
		} else if(!targetableNodes.contains(curriculumTreeEl.getSelectedNode())) {
			curriculumTreeEl.setErrorKey("error.target.not.allowed", null);
			allOk &= false;
		}

		return allOk;
	}
	
	private boolean isParent() {
		boolean parent = false;
		for(CurriculumElement element:curriculumElementsToMove) {
			parent |= isParent(element);
		}
		return parent;
	}
	
	private boolean isParent(CurriculumElement element) {
		TreeNode nodeToMove = curriculumModel
				.getNodeById(CurriculumTreeModel.nodeKey(element));
		TreeNode selectedNode = curriculumTreeEl.getSelectedNode();
		if(selectedNode == curriculumModel.getRootNode()) {
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
			TreeNode selectedNode = curriculumTreeEl.getSelectedNode();
			if(selectedNode == curriculumModel.getRootNode()) {
				for(CurriculumElement elementToMove:curriculumElementsToMove) {
					curriculumService.moveCurriculumElement(elementToMove, null);
				}
			} else {
				CurriculumElement newParent = (CurriculumElement)selectedNode.getUserObject();
				for(CurriculumElement elementToMove:curriculumElementsToMove) {
					curriculumService.moveCurriculumElement(elementToMove, newParent);
				}
			}
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
