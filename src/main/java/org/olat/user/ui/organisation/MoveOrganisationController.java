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
package org.olat.user.ui.organisation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.OrganisationType;
import org.olat.basesecurity.OrganisationTypeToType;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTreeItem;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.util.nodes.INode;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 8 déc. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MoveOrganisationController extends FormBasicController {
	
	private MenuTreeItem organisationTreeEl;
	private OrganisationTreeModel organisationModel;
	
	private Set<OrganisationType> allowedTypes;
	private List<Organisation> organisationsToMove;
	private Set<TreeNode> targetableNodes = new HashSet<>();
	
	@Autowired
	private OrganisationService organisationService;
	
	public MoveOrganisationController(UserRequest ureq, WindowControl wControl, List<Organisation> organisationsToMove) {
		super(ureq, wControl, "move_organisation");
		this.organisationsToMove = new ArrayList<>(organisationsToMove);
		allowedTypes = getAllowedTypes();
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		organisationModel = new OrganisationTreeModel();
		organisationTreeEl = uifactory.addTreeMultiselect("organisations", null, formLayout, organisationModel, this);
		organisationTreeEl.setMultiSelect(false);
		organisationTreeEl.setRootVisible(false);

		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("move.organisation", formLayout);
	}
	
	private void loadModel() {
		List<Organisation> allOrganisations = organisationService.getOrganisations();
		organisationModel.loadTreeModel(allOrganisations);
		
		//remove children of the organizations to move
		for(Organisation organisationMove:organisationsToMove) {
			TreeNode nodeToMove = organisationModel
					.getNodeById(OrganisationTreeModel.nodeKey(organisationMove));
			nodeToMove.removeAllChildren();
		}
		
		// remove the organizations with incompatible types
		List<TreeNode> openedNodes = new ArrayList<>();
		filterByAllowedTypes(organisationModel.getRootNode(), openedNodes);

		List<String> nodeIds = openedNodes
				.stream().map(TreeNode::getIdent)
				.collect(Collectors.toList());
		organisationTreeEl.setOpenNodeIds(nodeIds);
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
		if(uobject instanceof Organisation) {
			Organisation level = (Organisation)uobject;
			OrganisationType type = level.getType();
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
	
	private Set<OrganisationType> getAllowedTypes() {
		List<OrganisationType> allTypes = new ArrayList<>(organisationService.getOrganisationTypes());
		Map<OrganisationType, Set<OrganisationType>> subToParentTypes = new HashMap<>();
		for(OrganisationType type:allTypes) {
			Set<OrganisationTypeToType> typesToTypes = type.getAllowedSubTypes();
			for(OrganisationTypeToType typeToType:typesToTypes) {
				OrganisationType subTyp = typeToType.getAllowedSubOrganisationType();
				subToParentTypes
					.computeIfAbsent(subTyp, t -> new HashSet<>())
					.add(type);
			}
		}
		
		Set<OrganisationType> analyzedTypes = new HashSet<>();
		for(Organisation organisation:organisationsToMove) {
			OrganisationType levelType = organisation.getType();
			if(levelType != null && !analyzedTypes.contains(levelType)) {
				analyzedTypes.add(levelType);
				
				Set<OrganisationType> allowed = subToParentTypes.get(levelType);
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
		
		organisationTreeEl.clearError();
		if(organisationTreeEl.getSelectedNode() == null) {
			organisationTreeEl.setErrorKey("error.select.target.level", null);
			allOk &= false;
		} else if(isParent()) {
			organisationTreeEl.setErrorKey("error.target.no.parent", null);
			allOk &= false;
		} else if(!targetableNodes.contains(organisationTreeEl.getSelectedNode())) {
			organisationTreeEl.setErrorKey("error.target.not.allowed", null);
			allOk &= false;
		}

		return allOk;
	}
	
	private boolean isParent() {
		boolean parent = false;
		for(Organisation organisation:organisationsToMove) {
			parent |= isParent(organisation);
		}
		return parent;
	}
	
	private boolean isParent(Organisation organisation) {
		TreeNode nodeToMove = organisationModel
				.getNodeById(OrganisationTreeModel.nodeKey(organisation));
		TreeNode selectedNode = organisationTreeEl.getSelectedNode();
		if(selectedNode == organisationModel.getRootNode()) {
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
			TreeNode selectedNode = organisationTreeEl.getSelectedNode();
			if(selectedNode == organisationModel.getRootNode()) {
				for(Organisation organisationToMove:organisationsToMove) {
					organisationService.moveOrganisation(organisationToMove, null);
				}
			} else {
				Organisation newParent = (Organisation)selectedNode.getUserObject();
				for(Organisation organisationToMove:organisationsToMove) {
					organisationService.moveOrganisation(organisationToMove, newParent);
				}
			}
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
