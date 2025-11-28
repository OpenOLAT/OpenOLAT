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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
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
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class MoveCurriculumElementController extends FormBasicController {
	
	private MenuTreeItem curriculumTreeEl;
	private final CurriculumTreeModel curriculumModel;
	
	private final Curriculum curriculum;
	private final CurriculumSecurityCallback secCallback;
	private final CurriculumElement implementationElement;
	private final CurriculumElement curriculumElementToMove;
	
	private Predicate<CurriculumElement> admin = c -> true;
	private Predicate<CurriculumElement> editionOnly = c -> isEditable(c);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumService curriculumService;
	
	public MoveCurriculumElementController(UserRequest ureq, WindowControl wControl,
			CurriculumElement curriculumElementToMove, Curriculum curriculum,
			CurriculumSecurityCallback secCallback) {
		super(ureq, wControl, "move_curriculum_element");
		this.curriculum = curriculum;
		this.secCallback = secCallback;
		this.curriculumElementToMove = curriculumElementToMove;
		implementationElement = curriculumService.getImplementationOf(curriculumElementToMove);
		
		Map<CurriculumElementType, List<CurriculumElementType>> mapTypes = getAllowedSubTypes();
		curriculumModel = new CurriculumTreeModel(implementationElement, curriculumElementToMove, mapTypes);
		
		initForm(ureq);
		loadModel();
	}
	
	private Map<CurriculumElementType, List<CurriculumElementType>> getAllowedSubTypes() {
		List<CurriculumElementType> allTypes = curriculumService.getCurriculumElementTypes();
		Map<CurriculumElementType, List<CurriculumElementType>> map = new HashMap<>();
		for(CurriculumElementType type:allTypes) {
			Set<CurriculumElementTypeToType> typeToTypes = type.getAllowedSubTypes();
			List<CurriculumElementType> allowedSubTypes = new ArrayList<>();
			for(CurriculumElementTypeToType typeToType:typeToTypes) {
				allowedSubTypes.add(typeToType.getAllowedSubType());
			}
			map.put(type, allowedSubTypes);
		}
		return map;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		curriculumTreeEl = uifactory.addTreeMultiselect("elements", null, formLayout, curriculumModel, this);
		curriculumTreeEl.setMultiSelect(false);
		curriculumTreeEl.setRootVisible(true);
		curriculumTreeEl.setInsertTool(true);

		uifactory.addFormSubmitButton("move.element", formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}
	
	private void loadModel() {
		List<CurriculumElement> allElements = curriculumService.getCurriculumElementsDescendants(implementationElement);
		List<CurriculumElement> activeElements = allElements.stream()
				.filter(el -> CurriculumElementStatus.isInArray(el.getElementStatus(), CurriculumElementStatus.notDeleted()))
				.toList();
		Predicate<CurriculumElement> filter = secCallback.canEditCurriculumTree() ? admin : editionOnly;
		curriculumModel.loadTreeModel(activeElements, filter);

		List<TreeNode> openedNodes = new ArrayList<>();
		
		List<String> nodeIds = openedNodes
				.stream().map(TreeNode::getIdent)
				.collect(Collectors.toList());
		curriculumTreeEl.setOpenNodeIds(nodeIds);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		curriculumTreeEl.clearError();
		if(curriculumTreeEl.getSelectedNode() == null) {
			curriculumTreeEl.setErrorKey("error.select.target.level");
			allOk &= false;
		} else if(isParent()) {
			curriculumTreeEl.setErrorKey("error.target.no.parent");
			allOk &= false;
		} else if(curriculumTreeEl.getInsertionPosition() == null) {
			curriculumTreeEl.setErrorKey("error.target.no.insertion.point");
			allOk &= false;
		}

		return allOk;
	}
	
	private boolean isParent() {
		boolean parent = isParent(curriculumElementToMove);
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
			fireEvent(ureq, Event.DONE_EVENT);
		} else if(curriculumTreeEl.getInsertionPosition() == null)  {
			showWarning("error.target.no.insertion.point");
			fireEvent(ureq, Event.DONE_EVENT);
		} else {
			ElementPosition position = getSelectedPosition();
			List<CurriculumElementType> elementAllowTypes = curriculumService.getAllowedCurriculumElementType(position.newParent(), curriculumElementToMove);
			if(curriculumElementToMove.getType() == null || !elementAllowTypes.contains(curriculumElementToMove.getType())) {
				// Show error
			} else {
				doMove(position);
				fireEvent(ureq, Event.DONE_EVENT);
			}
		}
	}
	
	private void doMove(ElementPosition position) {
		curriculumService.moveCurriculumElement(curriculumElementToMove, position.newParent(), position.siblingBefore(), curriculum);
		dbInstance.commitAndCloseSession();
	}
	
	private ElementPosition getSelectedPosition() {
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
		
		return new ElementPosition(newParent, siblingBefore);
	}
	
	public record ElementPosition(CurriculumElement newParent, CurriculumElement siblingBefore) {
		//
	}
}
