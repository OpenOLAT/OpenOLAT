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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/

package org.olat.ims.cp.ui;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeDropEvent;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.tree.TreeHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.ims.cp.CPManager;
import org.olat.ims.cp.CPTreeDataModel;
import org.olat.ims.cp.ContentPackage;
import org.olat.ims.cp.objects.CPItem;
import org.olat.ims.cp.objects.CPOrganization;
import org.olat.ims.cp.objects.CPResource;

/**
 * The left-hand side of the cp editor shows the document tree with some edit
 * icons on top.
 * <P>
 * Initial Date: May 5, 2009 <br>
 * 
 * @author gwassmann
 */
public class CPTreeController extends BasicController {

	private MenuTree treeCtr;
	private DialogBoxController dialogCtr;
	private CPFileImportController uploadCtr;
	private CPContentController contentCtr;
	private VelocityContainer contentVC;
	private ContentPackage cp;
	private CloseableModalController cmc;

	private Link importLink;
	private Link newLink;
	private Link copyLink;
	private Link deleteLink;
	private CPTreeDataModel treeModel;

	private CPPage currentPage;

	protected CPTreeController(UserRequest ureq, WindowControl control, ContentPackage cp) {
		super(ureq, control);
		contentVC = createVelocityContainer("treeView");

		this.cp = cp;
		
		CPManager cpMgm = CPManager.getInstance();
		treeModel = cpMgm.getTreeDataModel(cp);
		treeCtr = new MenuTree("cp");
		treeCtr.setTreeModel(treeModel);
		treeCtr.setDragEnabled(true);
		treeCtr.setDropEnabled(true);
		treeCtr.setDropSiblingEnabled(true);
		treeCtr.setDndAcceptJSMethod("treeAcceptDrop_notWithChildren");
		treeCtr.addListener(this);

		setLinks();
		contentVC.put("cptreecontroller.tree", treeCtr);
		contentVC.contextPut("treeId", treeCtr.getDispatchID());
		putInitialPanel(contentVC);
	}

	private void setLinks() {
		importLink = LinkFactory.createCustomLink("cptreecontroller.importlink", "cptreecontroller.importlink", null, Link.NONTRANSLATED,
				contentVC, this);
		importLink.setCustomEnabledLinkCSS("o_cpeditor_import");
		importLink.setTooltip(translate("cptreecontroller.importlink_title"));
		importLink.setTitle(translate("cptreecontroller.importlink_title"));

		newLink = LinkFactory.createCustomLink("cptreecontroller.newlink", "cptreecontroller.newlink", null, Link.NONTRANSLATED, contentVC,
				this);
		newLink.setCustomEnabledLinkCSS("o_cpeditor_new");
		newLink.setTooltip(translate("cptreecontroller.newlink_title"));
		newLink.setTitle(translate("cptreecontroller.newlink_title"));

		copyLink = LinkFactory.createCustomLink("cptreecontroller.copylink", "cptreecontroller.copylink", null, Link.NONTRANSLATED, contentVC,
				this);
		copyLink.setTooltip(translate("cptreecontroller.copylink_title"));
		copyLink.setTitle(translate("cptreecontroller.copylink_title"));
		copyLink.setCustomEnabledLinkCSS("o_cpeditor_copy");

		deleteLink = LinkFactory.createCustomLink("cptreecontroller.deletelink", "cptreecontroller.deletelink", null, Link.NONTRANSLATED,
				contentVC, this);
		deleteLink.setTooltip(translate("cptreecontroller.deletelink_title"));
		deleteLink.setTitle(translate("cptreecontroller.deletelink_title"));
		deleteLink.setCustomEnabledLinkCSS("o_cpeditor_delete");
	}

	/**
	 * page setter
	 * 
	 * @param page
	 */
	protected void setCurrentPage(CPPage page) {
		currentPage = page;
	}

	/**
	 * Make this controller aware of the content controller.
	 * 
	 * @param page
	 */
	protected void setContentController(CPContentController contentCtr) {
		this.contentCtr = contentCtr;
	}

	/**
	 * deletes a page from the manifest
	 * 
	 * @param nodeID
	 */
	private void deletePage(String identifier, boolean deleteResource) {
		if (identifier.equals("")) {
			// no page selected
		} else {
			CPManager cpMgm = CPManager.getInstance();
			treeModel.removePath(identifier);
			cpMgm.removeElement(cp, identifier, deleteResource);
			cpMgm.writeToFile(cp);
			updateTree();
		}
	}
	
	private void updateTree() {
		treeModel.update();
		treeCtr.setDirty(true);
	}

	/**
	 * copies the page with given nodeID
	 * 
	 * @param nodeID
	 */
	private String copyPage(CPPage page) {
		String newIdentifier = null;
		if (page != null) {
			CPManager cpMgm = CPManager.getInstance();
			newIdentifier = cpMgm.copyElement(cp, page.getIdentifier());
			cpMgm.writeToFile(cp);
		}
		return newIdentifier;
	}

	/**
	 * Adds a new page to the CP
	 * 
	 * @return
	 */
	protected String addNewHTMLPage() {
		String newId = CPManager.getInstance().addBlankPage(cp, translate("cptreecontroller.newpage.title"), currentPage.getIdentifier());
		CPPage newPage = new CPPage(newId, cp);
		// Create an html file
		VFSContainer root = cp.getRootDir();
		VFSLeaf htmlFile = root.createChildLeaf(newId + ".html");
		newPage.setFile(htmlFile);
		updatePage(newPage);
		updateTree();
		return newId;
	}

	/**
	 * Adds a page to the CP
	 * 
	 * @return
	 */
	protected String addPage(CPPage page) {
		CPManager cpMgm = CPManager.getInstance();
		String newNodeID = "";

		if (currentPage.getIdentifier().equals("")) {
			newNodeID = cpMgm.addBlankPage(cp, page.getTitle());
		} else {
			// adds new page as child of currentPage
			newNodeID = cpMgm.addBlankPage(cp, page.getTitle(), currentPage.getIdentifier());
		}
		setCurrentPage(new CPPage(newNodeID, cp));

		cpMgm.writeToFile(cp);
		updateTree();
		return newNodeID;
	}

	/**
	 * @param page
	 */
	protected void updatePage(CPPage page) {
		setCurrentPage(page);
		CPManager cpMgm = CPManager.getInstance();
		cpMgm.updatePage(cp, page);
		cpMgm.writeToFile(cp);
		if (page.isOrgaPage()) {
			updateTree();
		}
		selectTreeNodeByCPPage(page);
	}

	/**
	 * Updates a page by nodeId.
	 * 
	 * @param nodeId
	 */
	protected void updateNode(String nodeId, String title) {
		String nodeIdentifier = treeModel.getIdentifierForNodeID(nodeId);
		CPPage page = new CPPage(nodeIdentifier, cp);
		page.setTitle(title);
		if (page.isOrgaPage()) {
			updateTree();
		}
		updatePage(page);
	}

	/**
	 * selects a Tree node in the tree with given id (if found). Returns false, if
	 * node is not found, true otherwise info: todo: implement selection of node
	 * in js tree
	 * 
	 * @param id
	 * @return
	 */
	protected boolean selectTreeNodeById(String id) {
		currentPage = new CPPage(id, cp);
		return selectTreeNodeByCPPage(currentPage);
	}

	/**
	 * Selects the node in the tree with the given page (if found). Returns false,
	 * if node is not found, true otherwise
	 * 
	 * info: todo: implement selection of node in js tree
	 * 
	 * @param page
	 * @return
	 */
	protected boolean selectTreeNodeByCPPage(CPPage page) {
		currentPage = page;
		if(currentPage != null) {
			String identifier = currentPage.getIdentifier();
			String nodeId = treeModel.getNodeIDForIdentifier(identifier);
			treeCtr.setSelectedNodeId(nodeId);
		}
		return true;
	}

	/**
	 * Builds an html-info string about the current page and its linked resources
	 * 
	 * @return HTML-String
	 */
	private String getCurrentPageInfoStringHTML() {
		// test if currentPage links to resource, which is used (linked) somewhere
		// else in the manifest
		CPManager cpMgm = CPManager.getInstance();
		DefaultElement ele = cpMgm.getElementByIdentifier(cp, currentPage.getIdRef());
		boolean single = false;
		if (ele instanceof CPResource) {
			CPResource res = (CPResource) ele;
			single = cpMgm.isSingleUsedResource(res, cp);
		}

		StringBuilder b = new StringBuilder();
		b.append("<br /><ul>");
		b.append("<li><b>" + translate("cptreecontroller.pagetitle") + "</b> " + currentPage.getTitle() + "</li>");
		if (single) {
			b.append("<li><b>" + translate("cptreecontroller.file") + "</b> " + currentPage.getFileName() + "</li>");
		}
		b.append("</ul>");
		return b.toString();

	}

	/**
	 * Event-handling from components
	 * 
	 */
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == importLink) {
			uploadCtr = new CPFileImportController(ureq, getWindowControl(), cp, currentPage);
			activateModalDialog(uploadCtr);

		} else if (source == newLink) {
			fireEvent(ureq, new Event("New Page"));

		} else if (source == copyLink) {
			if (currentPage.isOrgaPage()) {
				showInfo("cptreecontroller.orga.cannot.be.copied");
			} else {
				String newIdentifier = copyPage(currentPage);
				// this.getInitialComponent().setDirty(true);
				contentCtr.displayPage(ureq, newIdentifier);
			}
		} else if (source == deleteLink) {
			if (currentPage.isOrgaPage()) {
				showInfo("cptreecontroller.orga.cannot.be.deleted");
			} else {
				List<String> buttonLables = new ArrayList<String>();
				buttonLables.add(translate("cptreecontrolller.delete.items.and.files"));
				buttonLables.add(translate("cptreecontrolller.delete.items.only"));
				buttonLables.add(translate("cancel"));

				dialogCtr = DialogBoxUIFactory.createGenericDialog(ureq, getWindowControl(), translate("cptreecontroller.q_delete_title"),
						translate("cptreecontroller.q_delete_text", getCurrentPageInfoStringHTML()), buttonLables);
				listenTo(dialogCtr);
				dialogCtr.activate();
			}
		} else if (source == treeCtr) {
			if(event instanceof TreeEvent) {
				TreeEvent te = (TreeEvent)event;
				String selectedNodeID = treeModel.getIdentifierForNodeID(te.getNodeId());
				currentPage = new CPPage(selectedNodeID, cp);
				fireEvent(ureq, new TreeEvent(TreeEvent.COMMAND_TREENODE_CLICKED, selectedNodeID));
			} else if(event instanceof TreeDropEvent) {
				TreeDropEvent te = (TreeDropEvent)event;
				doDrop(ureq, te.getDroppedNodeId(), te.getTargetNodeId(), te.isAsChild(), te.isAtTheEnd());
			}
		}
	}

	/**
	 * Event-handling from controllers
	 * 
	 */
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == cmc) {
			if (event.equals(CloseableModalController.CLOSE_MODAL_EVENT)) {
				removeAsListenerAndDispose(cmc);
				cmc = null;
				removeAsListenerAndDispose(uploadCtr);
				uploadCtr = null;
			}
		} else if (source == uploadCtr) {
			if (event instanceof NewCPPageEvent) {
				// TODO:GW Is it necessary to set component dirty?
				// getInitialComponent().setDirty(true);
				fireEvent(ureq, event);
			}
			// Dispose the cmc and the podcastFormCtr.
			cmc.deactivate();
			removeAsListenerAndDispose(cmc);
			cmc = null;
			removeAsListenerAndDispose(uploadCtr);
			uploadCtr = null;
		} else if (source == dialogCtr) {
			// event from dialog (really-delete-dialog)
			if (event != Event.CANCELLED_EVENT) {
				int position = DialogBoxUIFactory.getButtonPos(event);

				// 0 = delete with resource
				// 1 = delete without resource
				// 2 = cancel
				if (position == 0 || position == 1) {
					boolean deleteResource = false;
					if (position == 0) {
						// Delete element including files
						deleteResource = true;
					}
					String parentIdentifier = getParentIdentifier();

					// finally delete the page
					deletePage(currentPage.getIdentifier(), deleteResource);

					if (parentIdentifier != null) {
						contentCtr.displayPage(ureq, parentIdentifier);
					}
				} else {
					// Cancel dialog and close window.
				}
			}
		}
	}
	
	private void doDrop(UserRequest ureq, String droppedNodeId, String targetNodeId, boolean asChild, boolean atTheEnd) {
		TreeNode droppedNode = treeModel.getNodeById(droppedNodeId);
		TreeNode targetNode = treeModel.getNodeById(targetNodeId);

		String droppedNodeIdent = treeModel.getIdentifierForNodeID(droppedNodeId);
		String targetNodeIdent = treeModel.getIdentifierForNodeID(targetNodeId);

		CPManager cpMgm = CPManager.getInstance();
		if(asChild) {
			cpMgm.moveElement(cp, droppedNodeIdent, targetNodeIdent, 0);
			cpMgm.writeToFile(cp);
		} else if(targetNode.getParent() == null) {
			//root -> do nothing
		} else {
			TreeNode parentTargetNode = (TreeNode)targetNode.getParent();
			int index = TreeHelper.indexOf(targetNode, parentTargetNode);
			boolean sibling = droppedNode.getParent().equals(parentTargetNode);
			if(sibling) {
				int droppedCurentIndex = TreeHelper.indexOf(targetNode, parentTargetNode);
				if(droppedCurentIndex < index) {
					index = index -2;
				}
			}
			
			if(index < 0) {
				index = 0;
			}
			
			String parentTargetNodeIdent = treeModel.getIdentifierForNodeID(parentTargetNode.getIdent());
			cpMgm.moveElement(cp, droppedNodeIdent, parentTargetNodeIdent, index + 1);
			cpMgm.writeToFile(cp);	
		}
		selectTreeNodeById(droppedNodeIdent);
		
		updateTree();
		
		fireEvent(ureq, new TreeEvent(TreeEvent.COMMAND_TREENODE_CLICKED, droppedNodeIdent));
	}

	/**
	 * Retrieves the parent identifier of the current page
	 * 
	 * @return The identifier of the current page's parent
	 */
	private String getParentIdentifier() {
		DefaultElement currentElem = CPManager.getInstance().getElementByIdentifier(cp, currentPage.getIdentifier());

		// Get the parent node to be displayed after deletion.
		String parentIdentifier = null;
		if (currentElem instanceof CPItem) {
			Element parent = ((CPItem) currentElem).getParentElement();
			if (parent instanceof CPItem) {
				CPItem parentItem = (CPItem) parent;
				parentIdentifier = parentItem.getIdentifier();
			} else if (parent instanceof CPOrganization) {
				CPOrganization parentItem = (CPOrganization) parent;
				parentIdentifier = parentItem.getIdentifier();
			}
		}
		return parentIdentifier;
	}

	@Override
	protected void doDispose() {
		contentVC = null;
	}

	/**
	 * @param controller The <code>FormBasicController</code> to be displayed in
	 *          the modal dialog.
	 */
	private void activateModalDialog(FormBasicController controller) {
		listenTo(controller);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), controller.getInitialComponent());
		listenTo(cmc);
		cmc.activate();
	}
}
