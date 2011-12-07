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
* <p>
*/ 

package org.olat.modules.scorm;

import java.io.File;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsBackController;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsPreviewController;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.dispatcher.mapper.MapperRegistry;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
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
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.iframe.IFrameDisplayController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.StringMediaResource;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.StringResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.course.CourseModule;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * Description:<br>
 * Controller that handles the display of a single Scorm sco item and delegates
 * the sco api calls to the scorm RTE backend. It provides also an navigation to
 * navigate in the tree with "pre" "next" buttons.
 */
public class ScormAPIandDisplayController extends MainLayoutBasicController {
	//private static final String PACKAGE = Util.getPackageName(ScormAPIandDisplayController.class);

	// private static final String ACTIVITY_CONTENTPACKING_GET_FILE =
	// "CONTENTPACKING_GET_FILE";
	private static final String LMS_INITIALIZE = "LMSInitialize";
	private static final String LMS_GETVALUE = "LMSGetValue";
	private static final String LMS_SETVALUE = "LMSSetValue";
	private static final String LMS_FINISH = "LMSFinish";
	private static final String LMS_GETLASTERROR = "LMSGetLastError";
	private static final String LMS_GETERRORSTRING = "LMSGetErrorString";
	private static final String LMS_GETDIAGNOSTIC = "LMSGetDiagnostic";
	private static final String LMS_COMMIT = "LMSCommit";
	private static final String SCORM_CONTENT_FRAME = "scormContentFrame";
	private String scorm_lesson_mode;
	private VelocityContainer myContent;
	private MenuTree menuTree;
	private Controller columnLayoutCtr;
	private ScormCPManifestTreeModel treeModel;
	private IFrameDisplayController iframectr;
	private OLATApiAdapter scormAdapter;
	private String username;
	private Link nextScoTop, nextScoBottom, previousScoTop, previousScoBottom;
	private MapperRegistry mapreg;
	private Mapper mapper;

	/**
	 * @param ureq
	 * @param wControl
	 * @param showMenu if true, the ims cp menu is shown
	 * @param apiCallback the callback to where lmssetvalue data is mirrored, or null if no callback is desired
	 * @param cpRoot
	 * @param resourceId
	 * @param courseIdNodeId The course ID and optional the course node ID combined with "-". Example: 77554952047098-77554952047107
	 * @param lesson_mode add null for the default value or "normal", "browse" or
	 *          "review"
	 * @param credit_mode add null for the default value or "credit", "no-credit"
	 */
	ScormAPIandDisplayController(UserRequest ureq, WindowControl wControl, boolean showMenu, ScormAPICallback apiCallback, File cpRoot, String resourceId, String courseIdNodeId, String lesson_mode,
			String credit_mode, boolean previewMode, boolean activate) {
		super(ureq, wControl);
		
		// logging-note: the callers of createScormAPIandDisplayController make sure they have the scorm resource added to the ThreadLocalUserActivityLogger
		ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_OPEN, getClass());
		this.username = ureq.getIdentity().getName();
		if (!lesson_mode.equals(ScormConstants.SCORM_MODE_NORMAL) && !lesson_mode.equals("review") && !lesson_mode.equals(ScormConstants.SCORM_MODE_BROWSE)) throw new AssertException(
				"Wrong parameter for constructor, only 'normal', 'browse' or 'review' are allowed for lesson_mode");
		if (!credit_mode.equals("credit") && !credit_mode.equals("no-credit")) throw new AssertException(
				"Wrong parameter for constructor, only 'credit' or 'no-credit' are allowed for credit_mode");
		//if (lesson_mode == null) scorm_lesson_mode = ScormConstants.SCORM_MODE_BROWSE;
		//if (credit_mode == null) scorm_lesson_mode = ScormConstants.SCORM_MODE_NOCREDIT;
		scorm_lesson_mode = lesson_mode;
		
		myContent = createVelocityContainer("display");
		Locale loc = ureq.getLocale();
		JSAndCSSComponent jsAdapter = new JSAndCSSComponent("apiadapter", this.getClass(), new String[] {"scormApiAdapter.js"}, null, true);
		myContent.put("apiadapter", jsAdapter);
		
		// init SCORM adapter
		scormAdapter = new OLATApiAdapter(apiCallback);	

		scormAdapter.init(cpRoot, resourceId, courseIdNodeId, FolderConfig.getCanonicalRoot(), this.username, ureq.getIdentity().getUser().getProperty(UserConstants.LASTNAME, loc)+", "+ureq.getIdentity().getUser().getProperty(UserConstants.FIRSTNAME, loc), lesson_mode, credit_mode, this.hashCode());

		// at this point we know the filelocation for our xstream-sco-score file (FIXME:fj: do better
		
		// even if we do not show the menu, we need to build parse the manifest
		// and find the first node to display at startup
		File mani = new File(cpRoot, "imsmanifest.xml");
		if (!mani.exists()) { 
			throw new OLATRuntimeException(
					"error.manifest.missing", null, getClass().getName(), "CP " + cpRoot.getAbsolutePath()
					+ " has no imsmanifest", null
			);
		}
		treeModel = new ScormCPManifestTreeModel(mani, scormAdapter.getScoItemsStatus());

		menuTree = new MenuTree("cpDisplayTree");
		menuTree.setTreeModel(treeModel);
		menuTree.addListener(this);
		
		OLATResourceable courseOres = null;
		// load course where this scorm package runs in
		if (courseIdNodeId != null) {
			String courseId = courseIdNodeId;
			int delimiterPos = courseId.indexOf("-");
			if (delimiterPos != -1) {
				// remove course node id from combined course id / node id value
				courseId = courseId.substring(0, delimiterPos);
			}
			courseOres = OresHelper.createOLATResourceableInstance(CourseModule.class, Long.valueOf(courseId));
		}
		iframectr = new IFrameDisplayController(ureq, wControl, new LocalFolderImpl(cpRoot), SCORM_CONTENT_FRAME,  courseOres);
		listenTo(iframectr);
		myContent.contextPut("frameId", SCORM_CONTENT_FRAME);
		
		//pre next navigation links
		nextScoTop = LinkFactory.createCustomLink("nextScoTop", "nextsco", "", Link.NONTRANSLATED, myContent, this);
		nextScoTop.setCustomEnabledLinkCSS("b_small_icon o_scorm_next_icon");
		
		previousScoTop = LinkFactory.createCustomLink("previousScoTop", "previoussco", "", Link.NONTRANSLATED, myContent, this);
		previousScoTop.setCustomEnabledLinkCSS("b_small_icon o_scorm_previous_icon");
		
		nextScoBottom = LinkFactory.createCustomLink("nextScoBottom", "nextsco", "", Link.NONTRANSLATED, myContent, this);
		nextScoBottom.setCustomEnabledLinkCSS("b_small_icon o_scorm_next_icon");
		
		previousScoBottom = LinkFactory.createCustomLink("previousScoBottom", "previoussco", "", Link.NONTRANSLATED, myContent, this);
		previousScoBottom.setCustomEnabledLinkCSS("b_small_icon o_scorm_previous_icon");
		
		// show the buttons, default. use setter method to change default behaviour
		myContent.contextPut("showNavButtons", Boolean.TRUE);
		
		// bootId is the item the user left the sco last time or the first one
		String bootId = scormAdapter.getScormLastAccessedItemId();
		// if bootId is -1 all course sco's are completed, we show a message
		if (bootId.equals("-1")) {
			iframectr.getInitialComponent().setVisible(false);
			showInfo("scorm.course.completed");
			
		} else {
			scormAdapter.launchItem(bootId);
			TreeNode bootnode = treeModel.getNodeByScormItemId(bootId);

			iframectr.setCurrentURI((String) bootnode.getUserObject());
			menuTree.setSelectedNodeId(bootnode.getIdent());
			
		}
		updateNextPreviousButtons(bootId);
		
		myContent.put("contentpackage", iframectr.getInitialComponent());

		if (activate) {
			if (previewMode) {
				LayoutMain3ColsPreviewController ctr = new LayoutMain3ColsPreviewController(ureq, getWindowControl(), (showMenu ? menuTree : null), null, myContent, "scorm" + resourceId);
				ctr.activate();
				columnLayoutCtr = ctr;
			} else {
				LayoutMain3ColsBackController ctr = new LayoutMain3ColsBackController(ureq, getWindowControl(), (showMenu ? menuTree : null), null, myContent, "scorm" + resourceId);
				ctr.activate();
				columnLayoutCtr = ctr;
			}
		} else {
			LayoutMain3ColsController ctr = new LayoutMain3ColsController(ureq, getWindowControl(), (showMenu ? menuTree : null), null, myContent, "scorm" + resourceId);
			columnLayoutCtr = ctr;			
			putInitialPanel(columnLayoutCtr.getInitialComponent());
		}
		listenTo(columnLayoutCtr);
		
		//scrom API calls get handled by this mapper
		mapreg = MapperRegistry.getInstanceFor(ureq.getUserSession());
		mapper = new Mapper() {

			public MediaResource handle(String relPath, HttpServletRequest request) {
				String apiCall = request.getParameter("apiCall");
				String apiCallParamOne = request.getParameter("apiCallParamOne");
				String apiCallParamTwo = request.getParameter("apiCallParamTwo");
				
				logDebug("scorm api request by user:"+ username +": " + apiCall + "('" + apiCallParamOne + "' , '" + apiCallParamTwo + "')", null);
				

				String returnValue = "";
				StringMediaResource smr = new StringMediaResource();
				smr.setContentType("text/html");
				smr.setEncoding("utf-8");
				
				if (apiCall != null && apiCall.equals("initcall")) {
					//used for Mozilla / firefox only to get more time for fireing the onunload stuff triggered by overwriting the content.
					smr.setData("<html><body></body></html>");
					return smr;
				}
				

				if (apiCall != null) {
					if (apiCall.equals(LMS_INITIALIZE)) {
						returnValue = scormAdapter.LMSInitialize(apiCallParamOne);
					} else if (apiCall.equals(LMS_GETVALUE)) {
						returnValue = scormAdapter.LMSGetValue(apiCallParamOne);
					} else if (apiCall.equals(LMS_SETVALUE)) {
						returnValue = scormAdapter.LMSSetValue(apiCallParamOne, apiCallParamTwo);
					} else if (apiCall.equals(LMS_COMMIT)) {
						returnValue = scormAdapter.LMSCommit(apiCallParamOne);
					} else if (apiCall.equals(LMS_FINISH)) {
						returnValue = scormAdapter.LMSFinish(apiCallParamOne);
					} else if (apiCall.equals(LMS_GETLASTERROR)) {
						returnValue = scormAdapter.LMSGetLastError();
					} else if (apiCall.equals(LMS_GETDIAGNOSTIC)) {
						returnValue = scormAdapter.LMSGetDiagnostic(apiCallParamOne);
					} else if (apiCall.equals(LMS_GETERRORSTRING)) {
						returnValue = scormAdapter.LMSGetErrorString(apiCallParamOne);
					}
					smr.setData("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"></head><body><p>"
							+ returnValue + "</p></body></html>");
					return smr;
					
				}
				smr.setData("");
				return smr;
			}};
			
		String scormCallbackUri = mapreg.register(mapper);
		myContent.contextPut("scormCallbackUri", scormCallbackUri+"/");

	}
	
	/**
	 * Configuration method to enable/disable the havigation buttons that appear
	 * on the right side above and below the content. Default is set to true.
	 * 
	 * @param showNavButtons
	 */
	public void showNavButtons(boolean showNavButtons) {
		myContent.contextPut("showNavButtons", Boolean.valueOf(showNavButtons));
	}

	/**
	 * Configuration method to use an explicit height for the iframe instead of
	 * the default automatic sizeing code. If you don't call this method, OLAT
	 * will try to size the iframe so that no scrollbars appear. In most cases
	 * this works. If it does not work, use this method to set an explicit height.
	 * <br />
	 * Set 0 to reset to automatic behaviour.
	 * 
	 * @param height
	 */
	public void setHeightPX(int height) {
		iframectr.setHeightPX(height);
	}
	
	public void setContentEncoding(String encoding) {
		iframectr.setContentEncoding(encoding);
	}
	
	public void setJSEncoding(String encoding) {
		iframectr.setJSEncoding(encoding);
	}

	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link) {
			switchToNextOrPreviousSco((Link)source);
		} else if (source == menuTree) {
			// user clicked a node in the tree navigation
			TreeEvent te = (TreeEvent) event;
			switchToPage(te);
		} else if (source == myContent && event.getCommand().equals("abort")) {
			// user has wrong browser - abort
			fireEvent(ureq, Event.FAILED_EVENT);
		}
		 
	}
	
	private void  switchToNextOrPreviousSco(Link link) {
		String nextScoId = (String)link.getUserObject();
		GenericTreeNode tn = (GenericTreeNode) treeModel.getNodeByScormItemId(nextScoId);
		menuTree.setSelectedNodeId(tn.getIdent());
		iframectr.getInitialComponent().setVisible(true);
		String identifierRes = (String) tn.getUserObject();
		updateNextPreviousButtons(nextScoId);
		displayMessages(nextScoId, identifierRes);
		updateMenuTreeIconsAndMessages();
	}

	/**
	 * @param te is an Event fired by clicking a node in a tree
	 */
	public void switchToPage(TreeEvent te) {

		// switch to the new page
		String nodeId = te.getNodeId();
		GenericTreeNode tn = (GenericTreeNode) treeModel.getNodeById(nodeId);

		if (te.getCommand().equals(MenuTree.COMMAND_TREENODE_EXPANDED)) {
			iframectr.getInitialComponent().setVisible(false);
		} else {
			iframectr.getInitialComponent().setVisible(true);
			String scormId = String.valueOf(treeModel.lookupScormNodeId(tn));
			updateNextPreviousButtons(scormId);
			displayMessages(scormId, (String) tn.getUserObject());
		}
		updateMenuTreeIconsAndMessages();
	}

	private void displayMessages(String scormId, String identifierRes) {
			
		if (scormAdapter.hasItemPrerequisites(scormId)) {
			iframectr.getInitialComponent().setVisible(false);
			showInfo("scorm.item.has.preconditions");
			return;
		}
		
		scormAdapter.launchItem(scormId);
		iframectr.setCurrentURI(identifierRes);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		cleanUpCollectedScoData();
	}

	/**
	 * in "browse" or "review" mode we dont collect sco data
	 */
	private void cleanUpCollectedScoData() {
		if(scorm_lesson_mode.equals(ScormConstants.SCORM_MODE_BROWSE) ||
			 scorm_lesson_mode.equals(ScormConstants.SCORM_MODE_REVIEW)){
			String path = FolderConfig.getCanonicalRoot()+"/tmp/"+this.hashCode();
			FileUtils.deleteDirsAndFiles( new File(path),true, true);
		}
	}
	/**
	 * @return the treemodel. (for read-only usage) Useful if you would like to
	 *         integrate the menu at some other place
	 */
	public ScormCPManifestTreeModel getTreeModel() {
		return treeModel;
	}

	/**
	 * @param te
	 * @deprecated @TODO To be deleted - does logging and would have to go via an event() method
	 */
	public void externalNodeClicked(TreeEvent te) {
		switchToPage(te);
	}
	
	private void updateNextPreviousButtons(String nextScoId) {
		Integer nextInt = scormAdapter.getNextSco(nextScoId);
		Integer preInt = scormAdapter.getPreviousSco(nextScoId);
		
		nextScoTop.setUserObject(nextInt.toString());
		nextScoBottom.setUserObject(nextInt.toString());
		if(nextInt.intValue() != -1 ) {
			nextScoTop.setVisible(true);
			nextScoBottom.setVisible(true);
		} else {
			nextScoTop.setVisible(false);
			nextScoBottom.setVisible(false);
		}
		
		previousScoTop.setUserObject(preInt.toString());
		previousScoBottom.setUserObject(preInt.toString());
		if(preInt.intValue() != -1 ) {
			previousScoTop.setVisible(true);
			previousScoBottom.setVisible(true);
		} else {
			previousScoTop.setVisible(false);
			previousScoBottom.setVisible(false);
		}
	}

	private void updateMenuTreeIconsAndMessages() {
		menuTree.setDirty(true);
		Map itemsStat = scormAdapter.getScoItemsStatus();
		Map idToNode = treeModel.getScormIdToNodeRelation();
		
		for (Iterator it = itemsStat.keySet().iterator(); it.hasNext();) {
			String itemId = (String) it.next();
			GenericTreeNode tn = (GenericTreeNode) idToNode.get(itemId);
			// change icon decorator
			tn.setIconDecorator1CssClass("o_scorm_" + (String) itemsStat.get(itemId));
		}
	}

}