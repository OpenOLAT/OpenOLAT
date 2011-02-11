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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.modules.cp;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.search.ui.SearchServiceUIFactory;
import org.olat.core.commons.services.search.ui.SearchServiceUIFactory.DisplayOption;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlsite.HtmlStaticPageComponent;
import org.olat.core.gui.components.htmlsite.NewInlineUriEvent;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.iframe.IFrameDisplayController;
import org.olat.core.gui.control.generic.iframe.NewIframeUriEvent;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.activity.CourseLoggingAction;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.course.ICourse;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * Description:<br>
 * shows the actual content package with or without a menu
 * 
 * @author Felix Jost
 */
public class CPDisplayController extends BasicController {

	private static final String FILE_SUFFIX_HTM = "htm";
	private static final String FILE_SUFFIX_XML = "xml";

	private VelocityContainer myContent;
	private MenuTree cpTree;
	private CPManifestTreeModel ctm;
	private VFSContainer rootContainer;
	private String selNodeId;
	private HtmlStaticPageComponent cpComponent;
	private IFrameDisplayController cpContentCtr;
	private Controller searchCtrl;

	/**
	 * @param ureq
	 * @param cpRoot
	 * @param showMenu
	 * @param activateFirstPage
	 */
	CPDisplayController(UserRequest ureq, WindowControl wControl, VFSContainer rootContainer, boolean showMenu, boolean activateFirstPage,
			String initialUri, OLATResourceable ores) {
		super(ureq, wControl);
		this.rootContainer = rootContainer;

		// wrapper velocity container for page content
		this.myContent = createVelocityContainer("cpcontent");
		// the cp component, added to the velocity
		
		if(!ureq.getUserSession().getRoles().isGuestOnly()) {
		  SearchServiceUIFactory searchServiceUIFactory = (SearchServiceUIFactory)CoreSpringFactory.getBean(SearchServiceUIFactory.class);
		  searchCtrl = searchServiceUIFactory.createInputController(ureq, wControl, DisplayOption.BUTTON, null);
		  myContent.put("search_input", searchCtrl.getInitialComponent());
		}
		
		//TODO:gs:a
		//may add an additional config for disabling, enabling IFrame style or not in CP mode
		//but always disable IFrame display when in screenreader mode (no matter whether style gets ugly)
		if (getWindowControl().getWindowBackOffice().getWindowManager().isForScreenReader()) {
			cpComponent = new HtmlStaticPageComponent("", rootContainer);
			cpComponent.addListener(this);
			myContent.put("cpContent", cpComponent);
		} else {
			cpContentCtr = new IFrameDisplayController(ureq, getWindowControl(),rootContainer, null, ores);
			cpContentCtr.setAllowDownload(true);
			listenTo(cpContentCtr);
			myContent.put("cpContent", cpContentCtr.getInitialComponent());
		}

		// even if we do not show the menu, we need to build parse the manifest and
		// find the first node to display at startup
		VFSItem mani = rootContainer.resolve("imsmanifest.xml");
		if (mani == null || !(mani instanceof VFSLeaf)) { throw new OLATRuntimeException("error.manifest.missing", null, this.getClass()
				.getPackage().getName(), "CP " + rootContainer + " has no imsmanifest", null); }
		// initialize tree model in any case
		ctm = new CPManifestTreeModel((VFSLeaf) mani);

		if (showMenu) {
			// the menu is only initialized when needed.
			cpTree = new MenuTree("cpDisplayTree");
			cpTree.setTreeModel(ctm);
			cpTree.addListener(this);
		}

		LoggingResourceable nodeInfo = null;
		if (activateFirstPage) {
			// set content to first accessible child or root node if no children
			// available
			TreeNode node = ctm.getRootNode();
			if (node == null) throw new OLATRuntimeException(CPDisplayController.class, "root node of content packaging was null, file:"
					+ rootContainer, null);
			while (node != null && !node.isAccessible()) {
				if (node.getChildCount() > 0) {
					node = (TreeNode) node.getChildAt(0);
				} else node = null;
			}
			if (node != null) { // node.isAccessible
				String nodeUri = (String) node.getUserObject();
				if (cpContentCtr != null) cpContentCtr.setCurrentURI(nodeUri);
				if (cpComponent != null) cpComponent.setCurrentURI(nodeUri);
				if (showMenu) cpTree.setSelectedNodeId(node.getIdent());
				// activate the selected node in the menu (skips the root node that is
				// empty anyway and saves one user click)
				selNodeId = node.getIdent();

				nodeInfo = LoggingResourceable.wrapCpNode(nodeUri);
			}
		} else if (initialUri != null) {
			// set page
			if (cpContentCtr != null) cpContentCtr.setCurrentURI(initialUri);
			if (cpComponent != null) cpComponent.setCurrentURI(initialUri);
			// update menu
			TreeNode newNode = ctm.lookupTreeNodeByHref(initialUri);
			if (newNode != null) { // user clicked on a link which is listed in the
															// toc
				if (cpTree != null) {
					cpTree.setSelectedNodeId(newNode.getIdent());
				} else {
					selNodeId = newNode.getIdent();
				}
			}
			nodeInfo = LoggingResourceable.wrapCpNode(initialUri);
		}
		// Note: the ores has a typename of ICourse - see
		// CPCourseNode.createNodeRunConstructorResult
		// which has the following line:
		//   OresHelper.createOLATResourceableInstance(ICourse.class, userCourseEnv.getCourseEnvironment().getCourseResourceableId());
		// therefore we use OresHelper.calculateTypeName(ICourse.class) here
		if (ores!=null && nodeInfo!= null && !OresHelper.calculateTypeName(ICourse.class).equals(ores.getResourceableTypeName())) {
			addLoggingResourceable(LoggingResourceable.wrap(ores, OlatResourceableType.cp));
			ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_OPEN, getClass(), nodeInfo);
		}

		putInitialPanel(myContent);
	}
	
	public void setContentEncoding(String encoding) {
		if(cpContentCtr != null) {
			cpContentCtr.setContentEncoding(encoding);
		}
	}
	
	public void setJSEncoding(String encoding) {
		if(cpContentCtr != null) {
			cpContentCtr.setJSEncoding(encoding);
		}
	}

	/**
	 * @return The menu component for this content packaging. The Controller must
	 *         be initialized properly to use this method
	 */
	Component getMenuComponent() {
		return cpTree;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == cpTree) {
			// FIXME:fj: cleanup between MenuTree.COMMAND_TREENODE_CLICKED and
			// TreeEvent.dito...
			if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
				TreeEvent te = (TreeEvent) event;
				switchToPage(ureq, te);
			}
		} else if (source == cpComponent) {
			if (event instanceof NewInlineUriEvent) {
				NewInlineUriEvent nue = (NewInlineUriEvent) event;
				// adjust the tree selection to the current choice if found
				selectTreeNode(ureq, nue.getNewUri());
			}
		}
	}
	
		@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
			if (source == cpContentCtr) { // a .html click within the contentpackage
				if (event instanceof NewInlineUriEvent) {
					NewInlineUriEvent nue = (NewInlineUriEvent) event;
					// adjust the tree selection to the current choice if found
					selectTreeNode(ureq, nue.getNewUri());
				} else if (event instanceof NewIframeUriEvent) {
					NewIframeUriEvent nue =  (NewIframeUriEvent) event;
					selectTreeNode(ureq, nue.getNewUri());
				}// else ignore (e.g. misplaced olatcmd event (inner olat link found in a
					// contentpackaging file)
			}
	}

	/**
	 * adjust the cp menu tree with the page selected with a link clicked in the content
	 * @param ureq
	 * @param newUri
	 */
	public void selectTreeNode(UserRequest ureq, String newUri) {
		TreeNode newNode = ctm.lookupTreeNodeByHref(newUri);
		if (newNode != null) { // user clicked on a link which is listed in the
			// toc
			if (cpTree != null) {
				cpTree.setSelectedNodeId(newNode.getIdent());
			} else {
				// for the case the tree is outside this controller (e.g. in the
				// course), we fire an event with the chosen node)
				fireEvent(ureq, new TreeNodeEvent(newNode));
			}
		}
		ThreadLocalUserActivityLogger.log(CourseLoggingAction.CP_GET_FILE, getClass(), LoggingResourceable.wrapCpNode(newUri));
	}

	/**
	 * @param ureq
	 * @param te
	 */
	public void switchToPage(UserRequest ureq, TreeEvent te) {
		// all treeevents receiced here are event clicked only
		// if (!te.getCommand().equals(TreeEvent.COMMAND_TREENODE_CLICKED)) throw
		// new AssertException("error");

		// switch to the new page
		String nodeId = te.getNodeId();
		TreeNode tn = ctm.getNodeById(nodeId);
		String identifierRes = (String) tn.getUserObject();
		
		// security check
		if (identifierRes.indexOf("../") != -1) throw new AssertException("a non-normalized url encountered in a manifest item:"
				+ identifierRes);
		
		// Check if path ends with .html, .htm or .xhtml. We do this by searching for "htm" 
		// and accept positions of this string at length-3 or length-4
		// Check also for XML resources that use XSLT for rendering
		if (identifierRes.toLowerCase().lastIndexOf(FILE_SUFFIX_HTM) >= (identifierRes.length() - 4)
				|| identifierRes.toLowerCase().endsWith(FILE_SUFFIX_XML)) {
			// display html files inline or in an iframe
			if (cpContentCtr != null) cpContentCtr.setCurrentURI(identifierRes);
			if (cpComponent != null) cpComponent.setCurrentURI(identifierRes);

		} else {
			// Also display pdf and other files in the iframe if it has been
			// initialized. Delegates displaying to the browser (and its plugins).
			if (cpContentCtr != null) {
				cpContentCtr.setCurrentURI(identifierRes);
			}
			else {
				// if an entry in a manifest points e.g. to a pdf file and the iframe
				// controller has not been initialized display it non-inline
				VFSItem currentItem = rootContainer.resolve(identifierRes);
				MediaResource mr;
				if (currentItem == null || !(currentItem instanceof VFSLeaf)) mr = new NotFoundMediaResource(identifierRes);
				else mr = new VFSMediaResource((VFSLeaf) currentItem);
				ureq.getDispatchResult().setResultingMediaResource(mr);
				// Prevent 'don't reload' warning
				cpTree.setDirty(false);
			}
		}
		ThreadLocalUserActivityLogger.log(CourseLoggingAction.CP_GET_FILE, getClass(), LoggingResourceable.wrapCpNode(identifierRes));
	}

	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_CLOSE, getClass());
		cpTree = null;
		ctm = null;
		myContent = null;
		rootContainer = null;
		cpComponent = null;
	}

	/**
	 * @return the treemodel. (for read-only usage) Useful if you would like to
	 *         integrate the menu at some other place
	 */
	public CPManifestTreeModel getTreeModel() {
		return ctm;
	}

	/**
	 * @param ureq
	 * @param te
	 * @deprecated @TODO To be deleted - does logging and would have to go via an event() method
	 */
	public void externalNodeClicked(UserRequest ureq, TreeEvent te) {
		switchToPage(ureq, te);
	}

	/**
	 * to use with the option "external menu" only
	 * 
	 * @return
	 */
	public String getInitialSelectedNodeId() {
		return selNodeId;
	}
	
	public String getNodeByUri(String uri) {
		if(StringHelper.containsNonWhitespace(uri)) {
			TreeNode node = ctm.lookupTreeNodeByHref(uri);
			if(node != null) {
				return node.getIdent();
			}
		}
		return getInitialSelectedNodeId();
	}
}
