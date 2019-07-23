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
package org.olat.portfolio.ui.structel.edit;

import java.util.HashMap;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeDropEvent;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.tree.TreeHelper;
import org.olat.portfolio.EPSecurityCallback;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.manager.EPStructureManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.model.structel.EPAbstractMap;
import org.olat.portfolio.model.structel.EPPage;
import org.olat.portfolio.model.structel.EPStructureElement;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.model.structel.PortfolioStructureMap;
import org.olat.portfolio.ui.structel.EPAddElementsController;
import org.olat.portfolio.ui.structel.EPArtefactClicked;
import org.olat.portfolio.ui.structel.EPStructureChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Controller shows a TOC (table of content) of the given PortfolioStructure
 * elements can be moved around by d&d
 * 
 * <P>
 * Initial Date:  13.09.2010 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPTOCController extends BasicController {

	protected static final String ARTEFACT_NODE_CLICKED = "artefactNodeClicked";
	private static final String DELETE_LINK_CMD = "delete";
	private static final String ROOT_NODE_IDENTIFIER = "rootStruct";
	@Autowired
	private EPFrontendManager ePFMgr;
	@Autowired
	private EPStructureManager eSTMgr;
	protected PortfolioStructureMap rootNode;
	protected final EPSecurityCallback secCallback;
	private MenuTree treeCtr;
	private VelocityContainer tocV;
	private PortfolioStructure structureClicked;
	private AbstractArtefact artefactClicked;
	
	protected final Map<Long,String> idToPath = new HashMap<>();
	protected final Map<String,PortfolioStructure> pathToStructure = new HashMap<>();
	private EPAddElementsController addElCtrl;
	private Link delButton;

	public EPTOCController(UserRequest ureq, WindowControl wControl, PortfolioStructure selectedEl, 
			PortfolioStructureMap rootNode, EPSecurityCallback secCallback) {
		super(ureq, wControl);
		this.secCallback = secCallback;
		tocV = createVelocityContainer("toc");
		this.rootNode = rootNode;
		TreeModel treeModel = buildTreeModel();
		treeCtr = new MenuTree("toc");
		treeCtr.setTreeModel(treeModel);
		treeCtr.setSelectedNode(treeModel.getRootNode());
		treeCtr.setDragEnabled(true);
		treeCtr.setDropEnabled(true);
		treeCtr.setDropSiblingEnabled(true);
		treeCtr.setDndAcceptJSMethod("treeAcceptDrop_portfolio");
		treeCtr.addListener(this);
		treeCtr.setRootVisible(true);

		tocV.put("tocTree", treeCtr);		
		delButton = LinkFactory.createCustomLink("deleteButton", DELETE_LINK_CMD, translate("delete"), Link.NONTRANSLATED, tocV, this);
		delButton.setTooltip(translate("deleteButton"));
		delButton.setIconLeftCSS("o_icon o_icon_delete");
		tocV.put("deleteButton", delButton);		

		if(selectedEl == null) {
			refreshAddElements(ureq, rootNode);
		} else {
			TreeNode selectedNode = TreeHelper.findNodeByUserObject(selectedEl, treeModel.getRootNode());
			if(selectedNode != null) {
				structureClicked = selectedEl;
				treeCtr.setSelectedNode(selectedNode);
				refreshAddElements(ureq, selectedEl);
			}
		}
		
		putInitialPanel(tocV);
	}
	
	
	public void update(UserRequest ureq, PortfolioStructure structure) {
		reloadTreeModel(structure);
		refreshAddElements(ureq, structure);
	}
	
	protected void refreshTree(PortfolioStructureMap root) {
		this.rootNode = root;
		reloadTreeModel(root);
	}
	
	/**
	 * refreshing the add elements link to actual structure
	 * @param ureq
	 * @param struct maybe null -> hiding the add-button
	 */
	private void refreshAddElements(UserRequest ureq, PortfolioStructure struct){
		tocV.remove(tocV.getComponent("addElement"));
		removeAsListenerAndDispose(addElCtrl);
		if (struct != null){
			addElCtrl = new EPAddElementsController(ureq, getWindowControl(), struct);
			if (struct instanceof EPPage) {
				if(secCallback.canAddStructure()) {
					addElCtrl.setShowLink(EPAddElementsController.ADD_STRUCTUREELEMENT);
				}
				if(secCallback.canAddArtefact()) {
					addElCtrl.setShowLink(EPAddElementsController.ADD_ARTEFACT);
				}
			} else if (struct instanceof EPAbstractMap) {
				if(secCallback.canAddPage()) {
					addElCtrl.setShowLink(EPAddElementsController.ADD_PAGE);
				}
			} else { // its a structure element
				if(secCallback.canAddArtefact()) {
					addElCtrl.setShowLink(EPAddElementsController.ADD_ARTEFACT);
				}
			}
			listenTo(addElCtrl);
			tocV.put("addElement", addElCtrl.getInitialComponent());
		}		
	}
	
	private void reloadTreeModel(PortfolioStructure oldStruct, PortfolioStructure newStruct) {
		if(oldStruct != null && newStruct != null && oldStruct.equals(newStruct)) {
			newStruct = null;//only 1 reload
		}
		if(oldStruct != null ) {
			reloadTreeModel(oldStruct);
		}
		if(newStruct != null) {
			reloadTreeModel(newStruct);
		}
	}
	
	private void reloadTreeModel(PortfolioStructure struct) {
		EPTOCTreeModel model = (EPTOCTreeModel)treeCtr.getTreeModel();
		if(struct != null) {
			GenericTreeNode node = (GenericTreeNode)TreeHelper.findNodeByUserObject(struct, model.getRootNode());
			if(node != null) {
				node.setTitle(struct.getTitle());
				node.setUserObject(struct);
				model.loadChildNode(struct, node);
			}
		}
	}
	
	private TreeModel buildTreeModel() {
		idToPath.put(rootNode.getKey(), "/" + ROOT_NODE_IDENTIFIER);
		return new EPTOCTreeModel(rootNode, translate("toc.root"));
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link) {
			Link link = (Link) source;
			if (link.getCommand().equals(DELETE_LINK_CMD)) {
				if (artefactClicked != null) {
					AbstractArtefact artefact = artefactClicked;
					PortfolioStructure parentStruct = getArtefactParentStruct(artefactClicked);
					ePFMgr.removeArtefactFromStructure(artefact, parentStruct);
					// refresh the view
					fireEvent(ureq, Event.CHANGED_EVENT);
				} else if (structureClicked != null) {
					if ((structureClicked instanceof EPPage)
							&& !(structureClicked instanceof EPAbstractMap)) {
						PortfolioStructure ps = structureClicked;
						while (ePFMgr.loadStructureParent(ps) != null) {
							ps = ePFMgr.loadStructureParent(ps);
						}
						int childPages = ePFMgr.countStructureChildren(ps);
						if (childPages > 1) {
							eSTMgr.removeStructureRecursively(structureClicked);
							// refresh the view
							fireEvent(ureq, Event.CHANGED_EVENT);
						} else {
							showError("last.page.not.deletable");
						}
					} else if(structureClicked instanceof EPStructureElement 
							&& !(structureClicked instanceof EPAbstractMap)) {
						//structures should always be deletable
						eSTMgr.removeStructureRecursively(structureClicked);
						// refresh the view
						fireEvent(ureq, Event.CHANGED_EVENT);
					} else {
						showInfo("element.not.deletable");
					}
				}
			}
		} else if (source == treeCtr) {
			if(event instanceof TreeEvent) {
				TreeEvent te = (TreeEvent)event;
				if(MenuTree.COMMAND_TREENODE_CLICKED.equals(te.getCommand())) {
					doSelectTreeElement(ureq, te);
				}
			} else if(event instanceof TreeDropEvent) {
				TreeDropEvent te = (TreeDropEvent)event;
				doDrop(ureq, te.getDroppedNodeId(), te.getTargetNodeId(), te.isAsChild());
			}
		}
	}
	
	private void doSelectTreeElement(UserRequest ureq, TreeEvent te) {
		TreeNode selectedNode = treeCtr.getTreeModel().getNodeById(te.getNodeId());
		Object userObj = selectedNode.getUserObject();
		if (userObj instanceof PortfolioStructure){
			//structure clicked
			structureClicked = (PortfolioStructure)userObj;
			refreshAddElements(ureq, structureClicked);
			delButton.setVisible(true);
			//send event to load this page
			fireEvent(ureq, new EPStructureChangeEvent(EPStructureChangeEvent.SELECTED, structureClicked));
		} else if (userObj instanceof AbstractArtefact) {
			//artefact clicked
			Object parentObj = ((TreeNode)selectedNode.getParent()).getUserObject();
			if(parentObj instanceof PortfolioStructure) {
				artefactClicked = (AbstractArtefact)userObj;
				PortfolioStructure structure = (PortfolioStructure)parentObj;
				refreshAddElements(ureq, null);
				delButton.setVisible(true);
				fireEvent(ureq, new EPArtefactClicked(ARTEFACT_NODE_CLICKED, structure));
			}
		} else {
			// root tree node clicked, no add/delete link
			delButton.setVisible(false);
			refreshAddElements(ureq, null);
			fireEvent(ureq, new Event(ARTEFACT_NODE_CLICKED));
		}
	}
	
	private void doDrop(UserRequest ureq, String droppedNodeId, String targetNodeId, boolean asChild) {
		TreeNode droppedNode = treeCtr.getTreeModel().getNodeById(droppedNodeId);
		TreeNode targetNode = treeCtr.getTreeModel().getNodeById(targetNodeId);
		if(droppedNode == null || targetNode == null) return;
		
		Object droppedObj = droppedNode.getUserObject();
		Object droppedParentObj = null;
		if(droppedNode.getParent() != null) {
			droppedParentObj = ((TreeNode)droppedNode.getParent()).getUserObject();
		}
		Object targetObj = targetNode.getUserObject();
		Object targetParentObj = null;
		if(targetNode.getParent() != null) {
			targetParentObj = ((TreeNode)targetNode.getParent()).getUserObject();
		}

		if (droppedObj instanceof AbstractArtefact) {
			AbstractArtefact artefact = (AbstractArtefact)droppedObj;
			if (checkArtefactTarget(artefact, targetObj)){
				moveArtefactToNewParent(ureq, artefact, droppedParentObj, targetObj);
			} else if(targetParentObj != null && targetParentObj.equals(droppedParentObj)) {
				reorder(ureq, artefact, (TreeNode)targetNode.getParent(), targetObj);
			}
		} else if (droppedObj instanceof PortfolioStructure) {
			PortfolioStructure droppedStruct = (PortfolioStructure)droppedObj;
			if (checkStructureTarget(droppedStruct, droppedParentObj, targetObj, targetParentObj, asChild)) {
				if(asChild) {
					int newPos = TreeHelper.indexOfByUserObject(targetObj, (TreeNode)targetNode.getParent());
					moveStructureToNewParent(ureq, droppedStruct, droppedParentObj, targetObj, newPos);
				} else if(droppedParentObj != null && targetParentObj != null && droppedParentObj.equals(targetParentObj)) {
					int newPos = TreeHelper.indexOfByUserObject(targetObj, (TreeNode)targetNode.getParent());
					moveStructureToNewParent(ureq, droppedStruct, droppedParentObj, targetParentObj, newPos);
				} else {
					int newPos = TreeHelper.indexOfByUserObject(targetObj, (TreeNode)targetNode.getParent());
					moveStructureToNewParent(ureq, droppedStruct, droppedParentObj, targetParentObj, newPos);
				}
			}
		}
	}
	
	private boolean checkArtefactTarget(AbstractArtefact artefact, Object  targetObj) {
		PortfolioStructure newParStruct;
		if (targetObj instanceof EPAbstractMap ) {
			return false;
		} else if(targetObj instanceof PortfolioStructure) {
			newParStruct = (PortfolioStructure)targetObj;
		} else {
			return false;
		}

		boolean sameTarget = ePFMgr.isArtefactInStructure(artefact, newParStruct);
		if (sameTarget) {
			return false;
		}
		return true;
	}
	
	// really do the move!
	private boolean moveArtefactToNewParent(UserRequest ureq, AbstractArtefact artefact, Object oldParent, Object newParent){
		if(!(oldParent instanceof PortfolioStructure) || !(newParent instanceof PortfolioStructure)) {
			return false;
		}

		try {
			PortfolioStructure oldParStruct = (PortfolioStructure)oldParent;
			PortfolioStructure newParStruct = (PortfolioStructure)newParent;
			
			if(ePFMgr.moveArtefactFromStructToStruct(artefact, oldParStruct, newParStruct)) {
				reloadTreeModel(oldParStruct, newParStruct);
				fireEvent(ureq, new EPMoveEvent());
				return true;
			}
		} catch (Exception e) {
			logError("could not load artefact, old and new parent", e);
		}
		return false;
	}
	
	private boolean reorder(UserRequest ureq, AbstractArtefact artefact, TreeNode parentNode, Object target){
		Object parentObj = parentNode.getUserObject();
		if(!(parentObj instanceof PortfolioStructure)) {
			return false;
		}
		
		int position = TreeHelper.indexOfByUserObject(target, parentNode);
		int current = TreeHelper.indexOfByUserObject(artefact, parentNode);
		if(current == position) {
			return false;//nothing to do
		} else {
			position++;//drop after
		}

		try {
			PortfolioStructure parStruct = (PortfolioStructure)parentObj;
			//translate in the position in the list of artefacts
			int numOfChildren = ePFMgr.countStructureChildren(parStruct);
			position = position - numOfChildren;
			if(position < 0) {
				position = 0;
			}
			
			if(ePFMgr.moveArtefactInStruct(artefact, parStruct, position)) {
				reloadTreeModel(parStruct, null);
				fireEvent(ureq, new EPMoveEvent());
				return true;
			}
		} catch (Exception e) {
			logError("could not load artefact, old and new parent", e);
		}
		return false;
	}
	
	private boolean checkStructureTarget(PortfolioStructure droppedObj, Object droppedParentObj,
			Object targetObj, Object targetParentObj, boolean asChild) {
		
		if(targetObj == null || droppedParentObj == null) {
			return false;
		}
		if (droppedParentObj != null && droppedParentObj.equals(targetParentObj)) {
			return true; // seems only to be a move in order
		}
		
		if(asChild) {
			if (droppedParentObj != null && droppedParentObj.equals(targetParentObj)) {
				return true; // seems only to be a move in order
			}
			if (droppedObj instanceof EPPage && targetObj instanceof EPPage) {
				return false;
			}
			if (droppedObj instanceof EPStructureElement && !(targetObj instanceof EPPage)) {
				return false;
			}
		} else {
			
			if (droppedObj instanceof EPPage && targetParentObj instanceof EPPage) {
				return false;
			}
			if (droppedObj instanceof EPStructureElement && !(targetParentObj instanceof EPPage)) {
				return false;
			}
		}
		return true;
	}
	
	private boolean moveStructureToNewParent(UserRequest ureq, PortfolioStructure structToBeMvd,
			Object oldParent, Object newParent, int newPos) {
		
		if(oldParent instanceof PortfolioStructure && newParent instanceof PortfolioStructure) {
			PortfolioStructure oldParStruct = (PortfolioStructure)oldParent;
			PortfolioStructure newParStruct = (PortfolioStructure)newParent;
			if (oldParStruct.equals(newParStruct)) {
				// this is only a position move
				if(ePFMgr.moveStructureToPosition(structToBeMvd, newPos)) {
					reloadTreeModel(structToBeMvd, null);
					fireEvent(ureq, new EPMoveEvent());
					return true;
				}
			} else if(ePFMgr.moveStructureToNewParentStructure(structToBeMvd, oldParStruct, newParStruct, newPos)) {
				reloadTreeModel(oldParStruct, newParStruct);
				fireEvent(ureq, new EPMoveEvent());
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == addElCtrl){
			// refresh the view, this is a EPStructureChangeEvent
			fireEvent(ureq, event);	
		}
	}
	
	private PortfolioStructure getArtefactParentStruct(AbstractArtefact artefact) {
		TreeNode artefactNode = TreeHelper.findNodeByUserObject(artefact, treeCtr.getTreeModel().getRootNode());
		if(artefactNode != null && artefactNode.getParent() != null) {
			Object parentObj = ((TreeNode)artefactNode.getParent()).getUserObject();
			if(parentObj instanceof PortfolioStructure) {
				return (PortfolioStructure)parentObj;
			}
		}

		return null;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		//
	}
}