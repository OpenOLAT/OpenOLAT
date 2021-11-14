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
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.InsertionPoint.Position;
import org.olat.core.gui.components.tree.MenuTreeItem;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.tree.TreePosition;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.nodes.INode;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumElementTypeToType;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
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
	private final CurriculumTreeModel curriculumModel;
	
	private final Curriculum curriculum;
	private final CurriculumSecurityCallback secCallback;
	private Set<CurriculumElementType> allowedTypes = new HashSet<>();
	private Set<CurriculumElementType> allowedSiblingTypes = new HashSet<>();
	private List<CurriculumElement> curriculumElementsToMove;
	private Set<TreeNode> targetableNodes = new HashSet<>();
	
	private Predicate<CurriculumElement> admin = c -> true;
	private Predicate<CurriculumElement> editionOnly = c -> isEditable(c);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumService curriculumService;
	
	public MoveCurriculumElementController(UserRequest ureq, WindowControl wControl,
			List<CurriculumElement> curriculumElementsToMove, Curriculum curriculum,
			CurriculumSecurityCallback secCallback) {
		super(ureq, wControl, "move_curriculum_element");
		this.curriculum = curriculum;
		this.secCallback = secCallback;
		this.curriculumElementsToMove = new ArrayList<>(curriculumElementsToMove);
		curriculumModel = new CurriculumTreeModel(curriculum, curriculumElementsToMove);
		initAllowedTypes();
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		curriculumTreeEl = uifactory.addTreeMultiselect("elements", null, formLayout, curriculumModel, this);
		curriculumTreeEl.setMultiSelect(false);
		curriculumTreeEl.setRootVisible(true);
		curriculumTreeEl.setInsertTool(true);

		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("move.element", formLayout);
	}
	
	private void loadModel() {
		List<CurriculumElement> allElements = curriculumService.getCurriculumElements(curriculum, CurriculumElementStatus.notDeleted());
		Predicate<CurriculumElement> filter = secCallback.canEditCurriculumTree() ? admin : editionOnly;
		curriculumModel.loadTreeModel(allElements, filter);
		
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
			} else if(allowedSiblingTypes.contains(type)) {
				openedNodes.add(node);
				// CSS class used as marker for restrictions on insertion point in tree model
				((GenericTreeNode)node).setIconCssClass("o_icon_node_up_down");
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
	
	private void initAllowedTypes() {
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

		Set<CurriculumElementType> siblingTypes = new HashSet<>();
		Set<CurriculumElementType> analyzedTypes = new HashSet<>();
		for(CurriculumElement element:curriculumElementsToMove) {
			CurriculumElementType levelType = element.getType();
			if(levelType != null && !analyzedTypes.contains(levelType)) {
				analyzedTypes.add(levelType);
				siblingTypes.add(levelType);
				
				Set<CurriculumElementType> allowed = subToParentTypes.get(levelType);
				if(allowed != null) {
					allTypes.retainAll(allowed);
				}
			}
		}

		allowedTypes = new HashSet<>(allTypes);
		allowedSiblingTypes = new HashSet<>();
		if(siblingTypes.size() == 1) {
			allowedSiblingTypes.addAll(siblingTypes);
		}
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
		} else if(curriculumTreeEl.getInsertionPosition() == null) {
			curriculumTreeEl.setErrorKey("error.target.no.insertion.point", null);
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
	
	private boolean isEditable(CurriculumElement element) {
		return secCallback.canEditCurriculumElement(element);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(isParent()) {
			showWarning("error.target.no.parent");
		} else if(curriculumTreeEl.getInsertionPosition() == null)  {
			showWarning("error.target.no.insertion.point");
		} else {
			TreePosition tp = curriculumTreeEl.getInsertionPosition();
			TreeNode parentNode = tp.getParentTreeNode();
			CurriculumElement newParent = (CurriculumElement)parentNode.getUserObject();
			if(newParent == curriculumModel.getRootNode()) {
				newParent = null; // root element
			}
			
			CurriculumElement siblingBefore;
			if(tp.getNode() == null) {
				siblingBefore = null;
			} else if(tp.getPosition() == Position.down) {
				siblingBefore = (CurriculumElement)tp.getNode().getUserObject();
			} else if(tp.getPosition() == Position.up) {
				TreeNode selectedNode = tp.getNode();
				int index = -1;
				for(int i=tp.getParentTreeNode().getChildCount(); i-->0; ) {
					if(selectedNode.equals(tp.getParentTreeNode().getChildAt(i))) {
						index = i;
						break;
					}
				}
				
				if(index == 0) {
					siblingBefore = null;
				} else {
					INode nodeBefore = tp.getParentTreeNode().getChildAt(index -1);
					siblingBefore = (CurriculumElement)((TreeNode)nodeBefore).getUserObject();
				}
			} else {
				siblingBefore = null;
			}
	
			for(CurriculumElement elementToMove:curriculumElementsToMove) {
				curriculumService.moveCurriculumElement(elementToMove, newParent, siblingBefore, curriculum);
				dbInstance.commit();
			}
		}
		dbInstance.commitAndCloseSession();
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
