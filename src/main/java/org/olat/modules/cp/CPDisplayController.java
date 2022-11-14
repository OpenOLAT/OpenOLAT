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
*/

package org.olat.modules.cp;

import java.io.IOException;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.pdf.PdfModule;
import org.olat.core.commons.services.pdf.PdfService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.iframe.DeliveryOptions;
import org.olat.core.gui.control.generic.iframe.IFrameDisplayController;
import org.olat.core.gui.control.generic.iframe.NewIframeUriEvent;
import org.olat.core.gui.control.winmgr.JSCommand;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
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
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.cp.CPManifestTreeModel.UserObject;
import org.olat.search.SearchModule;
import org.olat.search.SearchServiceUIFactory;
import org.olat.search.SearchServiceUIFactory.DisplayOption;
import org.olat.search.ui.SearchInputController;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * shows the actual content package with or without a menu
 * 
 * @author Felix Jost
 */
public class CPDisplayController extends BasicController implements Activateable2 {

	private static final String FILE_SUFFIX_HTM = "htm";
	private static final String FILE_SUFFIX_XML = "xml";

	private VelocityContainer myContent;
	private MenuTree cpTree;
	private CPManifestTreeModel ctm;
	private VFSContainer rootContainer;
	private String selNodeId;
	private IFrameDisplayController cpContentCtr;
	private SearchInputController searchCtrl;
	private Link pdfLink;
	private Link printLink;
	private Link nextLink;
	private Link previousLink;
	private String mapperBaseURL;
	private CPPrintMapper printMapper;
	
	private CPSelectPrintPagesController printController;
	private CloseableModalController printPopup;

	private final CPAssessmentProvider cpAssessmentProvider;
	
	@Autowired
	private PdfModule pdfModule;
	@Autowired
	private PdfService pdfService;
	@Autowired
	private SearchModule searchModule;
	
	/**
	 * @param ureq
	 * @param showMenu
	 * @param showNavigation Show the next/previous link
	 * @param activateFirstPage
	 * @param identPrefix In a course, set a unique prefix per node, if someone set 2x the same CPs in the course, the node identifiers
	 * of the CP elements must be different but predictable
	 * @param cpAssessmentProvider 
	 * @param cpRoot
	 */
	public CPDisplayController(UserRequest ureq, WindowControl wControl, VFSContainer rootContainer, boolean showMenu, boolean showNavigation,
			boolean activateFirstPage, boolean showPrint, DeliveryOptions deliveryOptions, String initialUri, OLATResourceable ores,
			String identPrefix, boolean randomizeMapper, CPAssessmentProvider cpAssessmentProvider) {
		super(ureq, wControl);
		this.rootContainer = rootContainer;
		this.cpAssessmentProvider = cpAssessmentProvider;

		// wrapper velocity container for page content
		myContent = createVelocityContainer("cpcontent");
		// the cp component, added to the velocity
		
		if(searchModule.isSearchAllowed(ureq.getUserSession().getRoles())) {
			SearchServiceUIFactory searchServiceUIFactory = (SearchServiceUIFactory)CoreSpringFactory.getBean(SearchServiceUIFactory.class);
			searchCtrl = searchServiceUIFactory.createInputController(ureq, wControl, DisplayOption.BUTTON, null);
			myContent.put("search_input", searchCtrl.getInitialComponent());
			listenTo(searchCtrl);
		}
		
		cpContentCtr = new IFrameDisplayController(ureq, getWindowControl(),rootContainer, null, ores, deliveryOptions, false, randomizeMapper);
		cpContentCtr.setAllowDownload(true);
		listenTo(cpContentCtr);
		myContent.put("cpContent", cpContentCtr.getInitialComponent());
		myContent.contextPut("isIframeDelivered", Boolean.TRUE);

		// even if we do not show the menu, we need to build parse the manifest and
		// find the first node to display at startup
		VFSItem mani = rootContainer.resolve("imsmanifest.xml");
		if (mani == null || !(mani instanceof VFSLeaf)) {
			showError("error.manifest.missing");
			return;
		}
		// initialize tree model in any case
		try {
			ctm = new CPManifestTreeModel((VFSLeaf) mani, identPrefix, cpAssessmentProvider);
		} catch (IOException e) {
			showError("error.manifest.corrupted");
			return;
		}

		if (showMenu) {
			// the menu is only initialized when needed.
			cpTree = new MenuTree("cpDisplayTree");
			cpTree.setScrollTopOnClick(true);
			cpTree.setTreeModel(ctm);
			cpTree.addListener(this);
		}
		
		if(showPrint) {
			printLink = LinkFactory.createCustomLink("print", "print", null, Link.LINK + Link.NONTRANSLATED, myContent, this);
			printLink.setCustomDisplayText("");
			printLink.setIconLeftCSS("o_icon o_icon-fw o_icon_print o_icon-lg");
			printLink.setCustomEnabledLinkCSS("o_print");
			printLink.setTitle(translate("print.node"));
			
			if(pdfModule.isEnabled()) {
				pdfLink = LinkFactory.createCustomLink("pdf", "pdf", null, Link.LINK + Link.NONTRANSLATED, myContent, this);
				pdfLink.setCustomDisplayText("");
				pdfLink.setIconLeftCSS("o_icon o_icon-fw o_icon_tool_pdf o_icon-lg");
				pdfLink.setCustomEnabledLinkCSS("o_pdf");
				pdfLink.setTitle(translate("pdf.node"));
			}
			
			String themeBaseUri = wControl.getWindowBackOffice().getWindow().getGuiTheme().getBaseURI();
			printMapper = new CPPrintMapper(ctm, rootContainer, themeBaseUri);
			mapperBaseURL = registerMapper(ureq, printMapper);
			printMapper.setBaseUri(mapperBaseURL);
		}
		
		if(showNavigation && ctm.getRootNode().getChildCount() > 0) {
			nextLink = LinkFactory.createCustomLink("next", "next", null, Link.LINK + Link.NONTRANSLATED, myContent, this);
			nextLink.setCustomDisplayText("");
			nextLink.setIconLeftCSS("o_icon o_icon-fw o_icon_next o_icon-lg");
			nextLink.setCustomEnabledLinkCSS("o_next");
			nextLink.setTitle(translate("next"));
			
			previousLink = LinkFactory.createCustomLink("previous", "previous", null, Link.LINK + Link.NONTRANSLATED, myContent, this);
			previousLink.setCustomDisplayText("");
			previousLink.setIconLeftCSS("o_icon o_icon-fw o_icon_previous o_icon-lg");
			previousLink.setCustomEnabledLinkCSS("o_previous");
			previousLink.setTitle(translate("previous"));
			
			myContent.put("next", nextLink);
			myContent.put("previous", previousLink);
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
				UserObject userObject = (UserObject)node.getUserObject();
				String nodeUri = userObject.getHref();
				if (cpContentCtr != null) cpContentCtr.setCurrentURI(nodeUri);
				if (showMenu) cpTree.setSelectedNodeId(node.getIdent());
				// activate the selected node in the menu (skips the root node that is
				// empty anyway and saves one user click)
				selNodeId = node.getIdent();

				onPageVisited(node);
				nodeInfo = LoggingResourceable.wrapCpNode(nodeUri);
				updateNextPreviousLink(node);
				OLATResourceable pOres = OresHelper.createOLATResourceableInstanceWithoutCheck("path=" + nodeUri, 0l);
				addToHistory(ureq, pOres, null);
			}
		} else if (initialUri != null) {
			// set page
			if (cpContentCtr != null) cpContentCtr.setCurrentURI(initialUri);
			// update menu
			TreeNode newNode = ctm.lookupTreeNodeByHref(initialUri);
			if (newNode != null) { // user clicked on a link which is listed in the
															// toc
				if (cpTree != null) {
					cpTree.setSelectedNodeId(newNode.getIdent());
				} else {
					selNodeId = newNode.getIdent();
				}
				updateNextPreviousLink(newNode);
				if(newNode.getUserObject() != null) {
					String identifierRes = ((UserObject)newNode.getUserObject()).getHref();
					Long id = Long.parseLong(newNode.getIdent());
					OLATResourceable pOres = OresHelper.createOLATResourceableInstanceWithoutCheck("path=" + identifierRes, id);
					addToHistory(ureq, pOres, null);
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
		if(printMapper != null) {
			printMapper.setContentEncoding(encoding);
		}
	}
	
	public void setJSEncoding(String encoding) {
		if(cpContentCtr != null) {
			cpContentCtr.setJSEncoding(encoding);
		}
		if(printMapper != null) {
			printMapper.setJSEncoding(encoding);
		}
	}

	/**
	 * @return The menu component for this content packaging. The Controller must
	 *         be initialized properly to use this method
	 */
	public Component getMenuComponent() {
		return cpTree;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == cpTree) {
			// FIXME:fj: cleanup between MenuTree.COMMAND_TREENODE_CLICKED and
			// TreeEvent.dito...
			if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
				TreeEvent te = (TreeEvent) event;
				switchToPage(ureq, te);
			}
		} else if (source == nextLink) {
			TreeNode nextUri = (TreeNode)nextLink.getUserObject();
			switchToPage(ureq, nextUri);
			if(cpTree != null) {
				cpTree.setSelectedNode(nextUri);
			}
			fireEvent(ureq, new TreeNodeEvent(nextUri));
		} else if (source == previousLink) {
			TreeNode previousUri = (TreeNode)previousLink.getUserObject();
			if(cpTree != null) {
				cpTree.setSelectedNode(previousUri);
			}
			switchToPage(ureq, previousUri);
			fireEvent(ureq, new TreeNodeEvent(previousUri));
		} else if (source == printLink) {
			selectPagesToPrint(ureq);
		} else if (source == pdfLink) {
			exportPagesToPdf(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == cpContentCtr) { // a .html click within the contentpackage
			if (event instanceof NewIframeUriEvent) {
				NewIframeUriEvent nue =  (NewIframeUriEvent) event;
				selectTreeNode(ureq, nue.getNewUri());
			}// else ignore (e.g. misplaced olatcmd event (inner olat link found in a
				// contentpackaging file)
		} else if (source == printPopup) {
			cleanUp();
		} else if (source == printController) {
			if(Event.DONE_EVENT == event) {
				List<String> nodeToPrint = printController.getSelectedNodeIdentifiers();
				printPages(nodeToPrint);
			}
			printPopup.deactivate();
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(printPopup);
		removeAsListenerAndDispose(printController);
		printController = null;
		printPopup = null;
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		Long id = entries.get(0).getOLATResourceable().getResourceableId();
		TreeNode newNode = null;
		if(id != null && id.longValue() > 0l) {
			newNode = ctm.getNodeById(id.toString());
		}
		if(newNode == null) {
			String path = BusinessControlFactory.getInstance().getPath(entries.get(0));
			newNode = ctm.lookupTreeNodeByHref(path);
		}
		if(newNode != null) {
			selectTreeNode(ureq, newNode);
			switchToPage(ureq, new TreeEvent(TreeEvent.COMMAND_TREENODES_SELECTED, newNode.getIdent()));
		}
	}

	private void printPages(final List<String> selectedNodeIds) {
		StringBuilder sb = new StringBuilder();
		sb.append("window.open('" + mapperBaseURL + "/print.html', '_print','height=800,left=100,top=100,width=800,toolbar=no,titlebar=0,status=0,menubar=yes,location= no,scrollbars=1');");
		printMapper.setSelectedNodeIds(selectedNodeIds);
		getWindowControl().getWindowBackOffice().sendCommandTo(new JSCommand(sb.toString()));
	}

	private void selectPagesToPrint(UserRequest ureq) {
		removeAsListenerAndDispose(printController);
		removeAsListenerAndDispose(printPopup);
		
		printController = new CPSelectPrintPagesController(ureq, getWindowControl(), ctm);
		listenTo(printController);
		
		printPopup = new CloseableModalController(getWindowControl(), "cancel", printController.getInitialComponent(), true, translate("print.node.list.title"));
		listenTo(printPopup);
		printPopup.activate();
	}
	
	private void exportPagesToPdf(UserRequest ureq) {
		ControllerCreator pdfControllerCreator = (lureq, lwcontrol) -> {
			return new CPPrintController(lureq, lwcontrol, ctm, rootContainer);
		};
		MediaResource resource = pdfService.convert("toPdf", getIdentity(), pdfControllerCreator, getWindowControl());
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}

	/**
	 * adjust the cp menu tree with the page selected with a link clicked in the content
	 * @param ureq
	 * @param newUri
	 */
	public void selectTreeNode(UserRequest ureq, String newUri) {
		TreeNode newNode = ctm.lookupTreeNodeByHref(newUri);
		if (newNode == null && newUri.contains("?")) {
			// remove any url paramters in case it is not an html5 app. E.g. some ELML contents
			newUri = newUri.substring(0, newUri.indexOf("?"));
			newNode = ctm.lookupTreeNodeByHref(newUri);
		}
		selectTreeNode(ureq, newNode);
		ThreadLocalUserActivityLogger.log(CourseLoggingAction.CP_GET_FILE, getClass(), LoggingResourceable.wrapCpNode(newUri));
	}
	
	public void selectTreeNode(UserRequest ureq, TreeNode newNode) {
		if (newNode != null) { // user clicked on a link which is listed in the
			// toc
			if (cpTree != null) {
				cpTree.setSelectedNodeId(newNode.getIdent());
			} else {
				// for the case the tree is outside this controller (e.g. in the
				// course), we fire an event with the chosen node)
				fireEvent(ureq, new TreeNodeEvent(newNode));
			}
			updateNextPreviousLink(newNode);
		}
	}
	
	private void updateNextPreviousLink(TreeNode currentNode) {
		if(nextLink != null) {
			TreeNode nextNode = ctm.getNextNodeWithContent(currentNode);
			nextLink.setEnabled(nextNode != null);
			nextLink.setUserObject(nextNode);
		}
		if(previousLink != null) {
			TreeNode previousNode = ctm.getPreviousNodeWithContent(currentNode);
			previousLink.setEnabled(previousNode != null);
			previousLink.setUserObject(previousNode);
		}
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
		if(tn != null) {
			switchToPage(ureq, tn);
		}
	}
	
	public void switchToPage(UserRequest ureq, TreeNode tn) {
		UserObject userObject = (UserObject)tn.getUserObject();
		String identifierRes = userObject.getHref();
		OLATResourceable ores = OresHelper.createOLATResourceableInstanceWithoutCheck("path=" + identifierRes, 0l);
		addToHistory(ureq, ores, null);
		
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
				if (currentItem == null || !(currentItem instanceof VFSLeaf)) mr = new NotFoundMediaResource();
				else mr = new VFSMediaResource((VFSLeaf) currentItem);
				ureq.getDispatchResult().setResultingMediaResource(mr);
				// Prevent 'don't reload' warning
				cpTree.setDirty(false);
			}
		}
		
		updateNextPreviousLink(tn);
		onPageVisited(tn);
		
		ThreadLocalUserActivityLogger.log(CourseLoggingAction.CP_GET_FILE, getClass(), LoggingResourceable.wrapCpNode(identifierRes));
	}

	private void onPageVisited(TreeNode treeNode) {
		UserObject userObject = (UserObject)treeNode.getUserObject();
		String identifier = userObject.getIdentifier();
		AssessmentEntryStatus status = cpAssessmentProvider.onPageVisited(identifier);
		if (cpAssessmentProvider.isLearningPathCSS()) {
			String cssClass = CPManifestTreeModel.getItemCssClass(cpAssessmentProvider.isLearningPathStatus(), status);
			((GenericTreeNode)treeNode).setCssClass(cssClass);
		}
	}

	@Override
	protected void doDispose() {
		ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_CLOSE, getClass());
		cpTree = null;
		ctm = null;
		myContent = null;
		rootContainer = null;
        super.doDispose();
	}

	/**
	 * @return the treemodel. (for read-only usage) Useful if you would like to
	 *         integrate the menu at some other place
	 */
	public CPManifestTreeModel getTreeModel() {
		return ctm;
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
