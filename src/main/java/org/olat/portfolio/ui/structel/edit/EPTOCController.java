/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
* <p>
*/
package org.olat.portfolio.ui.structel.edit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.ajax.tree.AjaxTreeModel;
import org.olat.core.gui.control.generic.ajax.tree.AjaxTreeNode;
import org.olat.core.gui.control.generic.ajax.tree.MoveTreeNodeEvent;
import org.olat.core.gui.control.generic.ajax.tree.TreeController;
import org.olat.core.gui.control.generic.ajax.tree.TreeNodeClickedEvent;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.filter.FilterFactory;
import org.olat.portfolio.EPSecurityCallback;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.manager.EPStructureManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.model.structel.EPAbstractMap;
import org.olat.portfolio.model.structel.EPPage;
import org.olat.portfolio.model.structel.EPStructureElement;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.ui.structel.EPAddElementsController;
import org.olat.portfolio.ui.structel.EPArtefactClicked;
import org.olat.portfolio.ui.structel.EPStructureChangeEvent;

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
	private static final String ARTEFACT_NODE_IDENTIFIER = "art";
	private static final String ROOT_NODE_IDENTIFIER = "root";
	protected final EPFrontendManager ePFMgr;
	protected final EPStructureManager eSTMgr;
	protected PortfolioStructure rootNode;
	protected final EPSecurityCallback secCallback;
	private TreeController treeCtr;
	private VelocityContainer tocV;
	private PortfolioStructure structureClicked;
	private String artefactNodeClicked;
	
	protected final Map<Long,String> idToPath = new HashMap<Long,String>();
	protected final Map<String,PortfolioStructure> pathToStructure = new HashMap<String,PortfolioStructure>();
	private EPAddElementsController addElCtrl;
	private Link delButton;

	public EPTOCController(UserRequest ureq, WindowControl wControl, PortfolioStructure selectedEl, 
			PortfolioStructure rootNode, EPSecurityCallback secCallback) {
		super(ureq, wControl);
		this.secCallback = secCallback;
		tocV = createVelocityContainer("toc");
		ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");
		eSTMgr = (EPStructureManager) CoreSpringFactory.getBean("epStructureManager");
		this.rootNode = rootNode;
		AjaxTreeModel treeModel = buildTreeModel();
		treeCtr = new TreeController(ureq, getWindowControl(), translate("toc.root"), treeModel, "myjsCallback");
		treeCtr.setTreeSorting(false, false, false);
		listenTo(treeCtr);
		tocV.put("tocTree", treeCtr.getInitialComponent());		
		delButton = LinkFactory.createCustomLink("deleteButton", DELETE_LINK_CMD, "&nbsp;&nbsp;&nbsp;", Link.NONTRANSLATED, tocV, this);
		delButton.setTooltip(translate("deleteButton"), false);
		delButton.setCustomEnabledLinkCSS("b_delete_icon b_eportfolio_del_link ");
		tocV.put("deleteButton", delButton);		

		if(selectedEl == null) {
			treeCtr.selectPath("/" + ROOT_NODE_IDENTIFIER + "/" + rootNode.getKey()); // select map
			refreshAddElements(ureq, rootNode);
		} else {
			String pagePath = calculatePathByDeepestNode(selectedEl);
			treeCtr.selectPath("/" + ROOT_NODE_IDENTIFIER + "/" + rootNode.getKey() + pagePath);
			structureClicked = selectedEl;
			refreshAddElements(ureq, selectedEl);
		}
		
		putInitialPanel(tocV);
	}
	
	private String calculatePathByDeepestNode(PortfolioStructure pStruct) {
		StringBuffer path = new StringBuffer();
		PortfolioStructure ps = pStruct;
		while (ps.getRootMap() != null) {
			path.insert(0, "/" + ps.getKey().toString());
			ps = ps.getRoot();
		}
		return path.toString();
	}
	
	protected void refreshTree(PortfolioStructure root) {
		this.rootNode = root;
		treeCtr.reloadPath("/" + ROOT_NODE_IDENTIFIER + "/" + rootNode.getKey());
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
	
	private AjaxTreeModel buildTreeModel() {
		idToPath.put(rootNode.getKey(), "/" + ROOT_NODE_IDENTIFIER);
		
		AjaxTreeModel model = new AjaxTreeModel(ROOT_NODE_IDENTIFIER) {

			@Override
			public List<AjaxTreeNode> getChildrenFor(String nodeId) {
				List<AjaxTreeNode> children = new ArrayList<AjaxTreeNode>();
				AjaxTreeNode child;
				boolean isRoot = false;
				PortfolioStructure selStruct = null;
				try {
					List<PortfolioStructure> structs = new ArrayList<PortfolioStructure>();
					if (nodeId.equals(ROOT_NODE_IDENTIFIER)) {
						structs.add(rootNode);
						isRoot = true;
					} else if (!nodeId.startsWith(ARTEFACT_NODE_IDENTIFIER)){
						selStruct = ePFMgr.loadPortfolioStructureByKey(new Long(nodeId));
						structs = ePFMgr.loadStructureChildren(selStruct);
					} else {
						// its an artefact -> no childs anymore
						return null;
					}
					if (structs != null && structs.size() != 0) { 
						for (PortfolioStructure portfolioStructure : structs) {
							String childNodeId = String.valueOf(portfolioStructure.getKey());
							boolean hasStructureChild = eSTMgr.countStructureChildren(portfolioStructure) > 0;
							boolean hasArtefacts = eSTMgr.countArtefacts(portfolioStructure) > 0;
							boolean hasChilds = hasStructureChild || hasArtefacts;
							child = new AjaxTreeNode(childNodeId, portfolioStructure.getTitle());
							if (isLogDebugEnabled()){
								child = new AjaxTreeNode(childNodeId, portfolioStructure.getTitle() + "drop:" + !isRoot + "drag:" + !isRoot + "leaf:"+!hasChilds);
							}
							// seems to be a bug, nothing can be dropped on a leaf, therefore we need to tweak with expanded/expandable ourself!
//							child.put(AjaxTreeNode.CONF_LEAF, !hasChilds);
							child.put(AjaxTreeNode.CONF_IS_TYPE_LEAF, !hasChilds);
							child.put(AjaxTreeNode.CONF_ALLOWDRAG, !isRoot);
				
							child.put(AjaxTreeNode.CONF_EXPANDED, hasStructureChild);
							child.put(AjaxTreeNode.CONF_EXPANDABLE, hasChilds);
							child.put(AjaxTreeNode.CONF_ALLOWDROP, true);
							child.put(AjaxTreeNode.CONF_ISTARGET, !isRoot); 
							
							child.put(AjaxTreeNode.CONF_ICON_CSS_CLASS, portfolioStructure.getIcon());
							String description = FilterFactory.getHtmlTagAndDescapingFilter().filter(portfolioStructure.getDescription());
							child.put(AjaxTreeNode.CONF_QTIP, description);
							children.add(child);
							
							String path;
							if(isRoot) {
								path = "/" + ROOT_NODE_IDENTIFIER;
							} else {
								path = idToPath.get(selStruct.getKey()); 
							}

							idToPath.put(portfolioStructure.getKey(), path + "/" + childNodeId);
						}
					} 
					if (selStruct != null && ePFMgr.countArtefactsRecursively(selStruct) != 0){
						List<AbstractArtefact> artList = ePFMgr.getArtefacts(selStruct);
						for (AbstractArtefact abstractArtefact : artList) {
							//include struct also, to still be unique if an artefact is linked multiple times
							String childNodeId = ARTEFACT_NODE_IDENTIFIER + String.valueOf(selStruct.getKey()) + "_" + String.valueOf(abstractArtefact.getKey());
							child = new AjaxTreeNode(childNodeId, abstractArtefact.getTitle());
							child.put(AjaxTreeNode.CONF_LEAF, true);
							child.put(AjaxTreeNode.CONF_IS_TYPE_LEAF, true);
							child.put(AjaxTreeNode.CONF_ALLOWDRAG, true);
							child.put(AjaxTreeNode.CONF_EXPANDED, false);
							child.put(AjaxTreeNode.CONF_ALLOWDROP, false);
							child.put(AjaxTreeNode.CONF_ICON_CSS_CLASS, abstractArtefact.getIcon());
							String description = FilterFactory.getHtmlTagAndDescapingFilter().filter(abstractArtefact.getDescription());
							child.put(AjaxTreeNode.CONF_QTIP, description);
							children.add(child);
							
							String path = idToPath.get(selStruct.getKey());
							
							String artefactPath = path + "/" + childNodeId;
							idToPath.put(abstractArtefact.getKey(), artefactPath);
							pathToStructure.put(artefactPath, selStruct);
						}						
					} 
				} catch (JSONException e) {
					throw new OLATRuntimeException("Error while creating tree model for map/page/structure selection", e);
				}
				return children;
			}
		};
		model.setCustomRootIconCssClass("o_st_icon");
		return model;
	}
	
	public void update(UserRequest ureq, PortfolioStructure structure) {
		String path = idToPath.get(structure.getKey());
		if(path != null) {
			treeCtr.reloadPath(path);
			treeCtr.selectPath(path);
		}
		refreshAddElements(ureq, structure);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@SuppressWarnings("unused")
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link) {
			Link link = (Link) source;
			if (link.getCommand().equals(DELETE_LINK_CMD)) {
				if (artefactNodeClicked != null) {
					AbstractArtefact artefact = ePFMgr.loadArtefactByKey(new Long(getArtefactIdFromNodeId(artefactNodeClicked)));
					PortfolioStructure parentStruct = ePFMgr.loadPortfolioStructureByKey(new Long(
							getArtefactParentStructIdFromNodeId(artefactNodeClicked)));
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
		}
	}
	
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (event instanceof TreeNodeClickedEvent) {
			resetClickedNodes();
			TreeNodeClickedEvent treeEv = (TreeNodeClickedEvent) event;
			String nodeClicked = treeEv.getNodeId();
			boolean isArtefactNode = nodeClicked.startsWith(ARTEFACT_NODE_IDENTIFIER);
			if (!nodeClicked.equals(ROOT_NODE_IDENTIFIER) && !isArtefactNode){
				structureClicked = ePFMgr.loadPortfolioStructureByKey(new Long(nodeClicked));
				refreshAddElements(ureq, structureClicked);
				delButton.setVisible(true);
				//send event to load this page
				fireEvent(ureq, new EPStructureChangeEvent(EPStructureChangeEvent.SELECTED, structureClicked));
				// needed because refreshAddElements set flc dirty, therefore selected node gets lost
				String path = idToPath.get(structureClicked.getKey());
				treeCtr.selectPath(path);
			} else if (isArtefactNode) {
				artefactNodeClicked = nodeClicked;
				refreshAddElements(ureq, null);
				delButton.setVisible(true);
				String artIdent = getArtefactIdFromNodeId(nodeClicked);
				String path = idToPath.get(new Long(artIdent));
				PortfolioStructure structure = pathToStructure.get(path);
				fireEvent(ureq, new EPArtefactClicked(ARTEFACT_NODE_CLICKED, structure));
				// needed because refreshAddElements set flc dirty, therefore selected node gets lost
				treeCtr.selectPath(path); 
			} else {
				// root tree node clicked, no add/delete link
				delButton.setVisible(false);
				refreshAddElements(ureq, null);
				fireEvent(ureq, new Event(ARTEFACT_NODE_CLICKED));
			}
		} else if (event instanceof MoveTreeNodeEvent) {
			resetClickedNodes();
			MoveTreeNodeEvent moveEvent = (MoveTreeNodeEvent) event;
			String movedNode = moveEvent.getNodeId();
			String oldParent = moveEvent.getOldParentNodeId();
			String newParent = moveEvent.getNewParentNodeId();
			int newPos = moveEvent.getPosition();
			boolean isArtefactNode = movedNode.startsWith(ARTEFACT_NODE_IDENTIFIER);
			if (isArtefactNode) {
				String nodeId = getArtefactIdFromNodeId(movedNode);
				if (checkNewArtefactTarget(nodeId, newParent)){
					if (moveArtefactToNewParent(nodeId, oldParent, newParent)) {
						if (isLogDebugEnabled()) logInfo("moved artefact " + nodeId + " from structure " + oldParent + " to " + newParent, null);
						moveEvent.setResult(true, null, null);
						// refresh the view
						EPMoveEvent movedEvent = new EPMoveEvent(newParent, nodeId);
						fireEvent(ureq, movedEvent);						
					} else {
						moveEvent.setResult(false, translate("move.error.title"), translate("move.artefact.error.move"));	
					}						
				} else {
					moveEvent.setResult(false, translate("move.error.title"), translate("move.artefact.error.target"));
				}
			} else {
				if (checkNewStructureTarget(movedNode, oldParent, newParent)){
					if (moveStructureToNewParent(movedNode, oldParent, newParent, newPos)) {
						if (isLogDebugEnabled()) logInfo("moved structure " + movedNode + " from structure " + oldParent + " to " + newParent, null);
						moveEvent.setResult(true, null, null);
						// refresh the view
						EPMoveEvent movedEvent = new EPMoveEvent(newParent, movedNode);
						fireEvent(ureq, movedEvent);
					} else {
						moveEvent.setResult(false, translate("move.error.title"), translate("move.struct.error.move"));
					}
				} else {					
					moveEvent.setResult(false, translate("move.error.title"), translate("move.struct.error.target"));
				}
			
			}
		} else if (source == addElCtrl){
			// refresh the view, this is a EPStructureChangeEvent
			fireEvent(ureq, event);	
		}
	}
	
	// reset previously choosen nodes. reference were there to be able to delete a node.
	private void resetClickedNodes(){
		structureClicked = null;
		artefactNodeClicked = null;
	}
	
	private String getArtefactIdFromNodeId(String nodeId){
		String artId = nodeId.substring(ARTEFACT_NODE_IDENTIFIER.length());
		if (artId.contains("_")){
			artId = artId.substring(artId.indexOf("_")+1);
		}
		return artId;
	}
	
	private String getArtefactParentStructIdFromNodeId(String nodeId){
		String structId = nodeId.substring(ARTEFACT_NODE_IDENTIFIER.length());
		if (structId.contains("_")){
			structId = structId.substring(0, structId.indexOf("_"));
		}
		return structId;
	}
	
	/**
	 * check if an artefact might be moved to this new parent node
	 * artefact might be moved to pages or structureElements, but not on maps
	 * @param artefactId
	 * @param structureId
	 * @return
	 */
	private boolean checkNewArtefactTarget(String artefactId, String structureId){
		//artefact cannot be moved directly under root
		if(ROOT_NODE_IDENTIFIER.equals(structureId)) return false;
		
		PortfolioStructure newParStruct;
		AbstractArtefact artefact;
		try {
			artefact = ePFMgr.loadArtefactByKey(new Long(artefactId));
			newParStruct = ePFMgr.loadPortfolioStructureByKey(new Long(structureId));
		} catch (Exception e) {
			logWarn("could not check for valid artefact target", e);
			return false;
		}
		boolean sameTarget = ePFMgr.isArtefactInStructure(artefact, newParStruct);
		if (sameTarget) return false;
		if (newParStruct instanceof EPAbstractMap ) return false;
		return true;
	}
	
	// really do the move!
	private boolean moveArtefactToNewParent(String artefactId, String oldParentId, String newParentId){
		PortfolioStructure newParStruct;
		PortfolioStructure oldParStruct;
		AbstractArtefact artefact;
		try {
			artefact = ePFMgr.loadArtefactByKey(new Long(artefactId));
			oldParStruct = ePFMgr.loadPortfolioStructureByKey(new Long(oldParentId));
			newParStruct = ePFMgr.loadPortfolioStructureByKey(new Long(newParentId));
		} catch (Exception e) {
			logError("could not load artefact, old and new parent", e);
			return false;
		}
		return ePFMgr.moveArtefactFromStructToStruct(artefact, oldParStruct, newParStruct);
	}
	
	/**
	 * check if a structure (page/structEl/map may be dropped here!
	 * its only allowed to move:
	 * - StructureElement from page -> page
	 * - change the order of pages
	 * - change the order of structures 
	 * @param subjectStructId
	 * @param oldParStructId
	 * @param newParStructId
	 * @return
	 */	
	private boolean checkNewStructureTarget(String subjectStructId, String oldParStructId, String newParStructId){
		PortfolioStructure structToBeMvd;
		PortfolioStructure newParStruct;
		if (oldParStructId.equals(newParStructId)) return true; // seems only to be a move in order
		if (newParStructId.equals(ROOT_NODE_IDENTIFIER)) return false;
		try {
			structToBeMvd = ePFMgr.loadPortfolioStructureByKey(new Long(subjectStructId));
			newParStruct = ePFMgr.loadPortfolioStructureByKey(new Long(newParStructId));
		} catch (Exception e) {
			logError("could not check for valid structure target", e);
			return false;
		}
		if (structToBeMvd instanceof EPPage && newParStruct instanceof EPPage) return false;
		if (structToBeMvd instanceof EPStructureElement && !(newParStruct instanceof EPPage)) return false;

		return true;
	}
	
	// really do the move
	private boolean moveStructureToNewParent(String subjectStructId, String oldParStructId, String newParStructId, int newPos){
		PortfolioStructure structToBeMvd;
		PortfolioStructure oldParStruct;
		PortfolioStructure newParStruct;
		try {
			structToBeMvd = ePFMgr.loadPortfolioStructureByKey(new Long(subjectStructId));
			oldParStruct = ePFMgr.loadPortfolioStructureByKey(new Long(oldParStructId));
			newParStruct = ePFMgr.loadPortfolioStructureByKey(new Long(newParStructId));
		} catch (Exception e) {
			logError("could not load: structure to be moved, old or new structure while trying to move", e);
			return false;
		}
		
		if (oldParStructId.equals(newParStructId)) {
			// this is only a position move
			return ePFMgr.moveStructureToPosition(structToBeMvd, newPos);
		}
		
		return ePFMgr.moveStructureToNewParentStructure(structToBeMvd, oldParStruct, newParStruct, newPos);		
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		//
	}

}
