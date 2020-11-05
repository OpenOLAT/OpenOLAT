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

package org.olat.modules.scorm;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsBackController;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsPreviewController;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.ListPanel;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.ConfigurationChangedListener;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.iframe.DeliveryOptions;
import org.olat.core.gui.control.generic.iframe.IFrameDisplayController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.course.CourseModule;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Controller that handles the display of a single Scorm sco item and delegates
 * the sco api calls to the scorm RTE backend. It provides also an navigation to
 * navigate in the tree with "pre" "next" buttons.
 */
public class ScormAPIandDisplayController extends MainLayoutBasicController implements ConfigurationChangedListener {

	protected static final String LMS_INITIALIZE = "LMSInitialize";
	protected static final String LMS_GETVALUE = "LMSGetValue";
	protected static final String LMS_SETVALUE = "LMSSetValue";
	protected static final String LMS_FINISH = "LMSFinish";
	protected static final String LMS_GETLASTERROR = "LMSGetLastError";
	protected static final String LMS_GETERRORSTRING = "LMSGetErrorString";
	protected static final String LMS_GETDIAGNOSTIC = "LMSGetDiagnostic";
	protected static final String LMS_COMMIT = "LMSCommit";
	protected static final String SCORM_CONTENT_FRAME = "scormContentFrame";
	private String scorm_lesson_mode;
	private VelocityContainer myContent;
	private MenuTree menuTree;
	private Controller columnLayoutCtr;
	private ScormCPManifestTreeModel treeModel;
	private IFrameDisplayController iframectr;
	private OLATApiAdapter scormAdapter;
	private String username;
	private Link nextScoTop, nextScoBottom, previousScoTop, previousScoBottom;
	private ListPanel scoTopButtons, scoBottomButtons;
	
	@Autowired
	private ScormMainManager scormMainManager;

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
	 * @param attemptsIncremented Is the attempts counter already incremented 
	 * @param deliveryOptions This delivery options can override the default from the SCORM module
	 */
	ScormAPIandDisplayController(UserRequest ureq, WindowControl wControl, boolean showMenu, ScormAPICallback apiCallback,
			File cpRoot, Long scormResourceId, String courseIdNodeId, String lesson_mode, String credit_mode,
			boolean previewMode, String assessableType, boolean activate, boolean fullWindow, boolean attemptsIncremented,
			boolean radomizeDelivery, DeliveryOptions deliveryOptions) {
		super(ureq, wControl);
		
		// logging-note: the callers of createScormAPIandDisplayController make sure they have the scorm resource added to the ThreadLocalUserActivityLogger
		ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_OPEN, getClass());
		this.username = ureq.getIdentity().getName();
		if (!lesson_mode.equals(ScormConstants.SCORM_MODE_NORMAL) && !lesson_mode.equals(ScormConstants.SCORM_MODE_REVIEW) && !lesson_mode.equals(ScormConstants.SCORM_MODE_BROWSE)) throw new AssertException(
				"Wrong parameter for constructor, only 'normal', 'browse' or 'review' are allowed for lesson_mode");
		if (!credit_mode.equals("credit") && !credit_mode.equals("no-credit")) throw new AssertException(
				"Wrong parameter for constructor, only 'credit' or 'no-credit' are allowed for credit_mode");

		scorm_lesson_mode = lesson_mode;
		
		myContent = createVelocityContainer("display");
		JSAndCSSComponent jsAdapter = new JSAndCSSComponent("apiadapter", new String[] {"js/openolat/scormApiAdapter.js"}, null);
		myContent.put("apiadapter", jsAdapter);
		
		// init SCORM adapter
		try {
			scormAdapter = new OLATApiAdapter();	
			scormAdapter.addAPIListener(apiCallback);
			String fullname = UserManager.getInstance().getUserDisplayName(getIdentity());
			String scormResourceIdStr = scormResourceId == null ? null : scormResourceId.toString();
			scormAdapter.init(cpRoot, scormResourceIdStr, courseIdNodeId, FolderConfig.getCanonicalRoot(), username, fullname, lesson_mode, credit_mode, hashCode());
		} catch (IOException e) {
			showError("error.manifest.corrupted");
			LayoutMain3ColsController ctr = new LayoutMain3ColsController(ureq, getWindowControl(), null, new Panel("empty"), "scorm" + scormResourceId);
			columnLayoutCtr = ctr;
			putInitialPanel(columnLayoutCtr.getInitialComponent());
			return;
		}

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
		ScormPackageConfig packageConfig = scormMainManager.getScormPackageConfig(cpRoot);
		if((deliveryOptions == null || (deliveryOptions.getInherit() != null && deliveryOptions.getInherit().booleanValue()))
				&& packageConfig != null) {
			deliveryOptions = packageConfig.getDeliveryOptions();
		}
		iframectr = new IFrameDisplayController(ureq, wControl, new LocalFolderImpl(cpRoot), SCORM_CONTENT_FRAME, courseOres, deliveryOptions, true, radomizeDelivery);
		listenTo(iframectr);
		myContent.contextPut("frameId", SCORM_CONTENT_FRAME);
		
		//pre next navigation links
		nextScoTop = LinkFactory.createCustomLink("nextScoTop", "nextsco", "", Link.NONTRANSLATED | Link.BUTTON, myContent, this);
		nextScoTop.setIconLeftCSS("o_icon o_icon_next_page");
		
		previousScoTop = LinkFactory.createCustomLink("previousScoTop", "previoussco", "", Link.NONTRANSLATED | Link.BUTTON, myContent, this);
		previousScoTop.setIconLeftCSS("o_icon o_icon_previous_page");
		
		nextScoBottom = LinkFactory.createCustomLink("nextScoBottom", "nextsco", "", Link.NONTRANSLATED | Link.BUTTON, myContent, this);
		nextScoBottom.setIconLeftCSS("o_icon o_icon_next_page");
		
		previousScoBottom = LinkFactory.createCustomLink("previousScoBottom", "previoussco", "", Link.NONTRANSLATED | Link.BUTTON, myContent, this);
		previousScoBottom.setIconLeftCSS("o_icon o_icon_previous_page");
		
		scoTopButtons = new ListPanel("scoTopButtons", "o_scorm_navigation");
		scoTopButtons.addContent(previousScoTop);
		scoTopButtons.addContent(nextScoTop);
		scoBottomButtons = new ListPanel("scoBottomButtons", "o_scorm_navigation");
		scoBottomButtons.addContent(previousScoBottom);
		scoBottomButtons.addContent(nextScoBottom);
		
		
		// show the buttons, default. use setter method to change default behaviour
		myContent.contextPut("showNavButtons", Boolean.TRUE);
		
		myContent.put("scoTopButtons", scoTopButtons);
		myContent.put("scoBottomButtons", scoBottomButtons);
		
		// bootId is the item the user left the sco last time or the first one
		String bootId = scormAdapter.getScormLastAccessedItemId();
		// if bootId is -1 all course sco's are completed, we show a message
		scormAdapter.launchItem(bootId);
		TreeNode bootnode = treeModel.getNodeByScormItemId(bootId);

		iframectr.setCurrentURI((String) bootnode.getUserObject());
		menuTree.setSelectedNodeId(bootnode.getIdent());

		updateNextPreviousButtons(bootId);
		
		myContent.put("contentpackage", iframectr.getInitialComponent());

		if (activate) {
			if (previewMode) {
				LayoutMain3ColsPreviewController ctr = new LayoutMain3ColsPreviewController(ureq, getWindowControl(), (showMenu ? menuTree : null), myContent, "scorm" + scormResourceId);
				if(fullWindow)
					ctr.setAsFullscreen();
				columnLayoutCtr = ctr;
			} else {
				LayoutMain3ColsBackController ctr = new LayoutMain3ColsBackController(ureq, getWindowControl(), (showMenu ? menuTree : null), myContent, "scorm" + scormResourceId);
				if(fullWindow)
					ctr.setAsFullscreen();
				columnLayoutCtr = ctr;
			}
		} else {
			LayoutMain3ColsController ctr = new LayoutMain3ColsController(ureq, getWindowControl(), (showMenu ? menuTree : null), myContent, "scorm" + scormResourceId);
			columnLayoutCtr = ctr;			
			putInitialPanel(columnLayoutCtr.getInitialComponent());
		}
		listenTo(columnLayoutCtr);
		
		//scrom API calls get handled by this mapper
		String scormResourceIdStr = (scormResourceId == null ? null : scormResourceId.toString());
		Mapper mapper = new ScormAPIMapper(ureq.getIdentity(), scormResourceIdStr, courseIdNodeId, assessableType, cpRoot, scormAdapter, attemptsIncremented);
		String scormCallbackUri = registerMapper(ureq, mapper);
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
	
	public void setRawContent(boolean rawContent) {
		iframectr.setRawContent(rawContent);
	}
	
	public DeliveryOptions getDeliveryOptions() {
		return iframectr.getDeliveryOptions();
	}
	
	public void setDeliveryOptions(DeliveryOptions config) {
		iframectr.setDeliveryOptions(config);
	}
	
	public void setContentEncoding(String encoding) {
		iframectr.setContentEncoding(encoding);
	}
	
	public void setJSEncoding(String encoding) {
		iframectr.setJSEncoding(encoding);
	}

	//fxdiff FXOLAT-116: SCORM improvements
	public void close() {
		if(columnLayoutCtr instanceof LayoutMain3ColsBackController) {
			((LayoutMain3ColsBackController)columnLayoutCtr).deactivate();
		} else if(columnLayoutCtr instanceof LayoutMain3ColsPreviewController) {
			((LayoutMain3ColsPreviewController)columnLayoutCtr).deactivate();
		}
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link) {
			switchToNextOrPreviousSco((Link)source);
		} else if (source == menuTree) {
			// user clicked a node in the tree navigation
			TreeEvent te = (TreeEvent) event;
			switchToPage(te);
		} else if (source == myContent && "abort".equals(event.getCommand())) {
			// user has wrong browser - abort
			fireEvent(ureq, Event.FAILED_EVENT);
		} else if (source == myContent && "ping".equals(event.getCommand())) {
			// Nothing to do, just let the framework redraw itself if necessary
			// This is used when LMS-Finished has been called and it is configured to 
			// close the module automatically
			myContent.setDirty(false);
		}		 
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == columnLayoutCtr) {
			if(event == Event.BACK_EVENT) {
				fireEvent(ureq, Event.BACK_EVENT);
			}
		}
		super.event(ureq, source, event);
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
			myContent.setDirty(true);//update the view
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
	
	@Override
	public void configurationChanged() {
		if(columnLayoutCtr instanceof LayoutMain3ColsBackController) {
			LayoutMain3ColsBackController layoutCtr = (LayoutMain3ColsBackController)columnLayoutCtr;
			layoutCtr.deactivate();
		} else if(columnLayoutCtr instanceof LayoutMain3ColsController) {
			LayoutMain3ColsController layoutCtr = (LayoutMain3ColsController)columnLayoutCtr;
			layoutCtr.deactivate(null);
		}
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
			StringBuilder path = new StringBuilder();
			path.append(WebappHelper.getTmpDir())
			  .append("/tmp").append(WebappHelper.getInstanceId()).append("scorm/")
			  .append(hashCode());
			FileUtils.deleteDirsAndFiles( new File(path.toString()),true, true);
		}
	}
	/**
	 * @return the treemodel. (for read-only usage) Useful if you would like to
	 *         integrate the menu at some other place
	 */
	public ScormCPManifestTreeModel getTreeModel() {
		return treeModel;
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
	
	public void activate(){
		if (columnLayoutCtr instanceof LayoutMain3ColsPreviewController) {
			LayoutMain3ColsPreviewController ctrl = (LayoutMain3ColsPreviewController) columnLayoutCtr;
			ctrl.activate();
		} else if (columnLayoutCtr instanceof LayoutMain3ColsBackController){
			LayoutMain3ColsBackController ctrl =  (LayoutMain3ColsBackController)columnLayoutCtr;
			ctrl.activate();
		}
	}

	private void updateMenuTreeIconsAndMessages() {
		menuTree.setDirty(true);
		Map<String,String> itemsStat = scormAdapter.getScoItemsStatus();
		Map<String,GenericTreeNode> idToNode = treeModel.getScormIdToNodeRelation();
		
		for (Iterator<String> it = itemsStat.keySet().iterator(); it.hasNext();) {
			String itemId = it.next();
			GenericTreeNode tn = idToNode.get(itemId);
			// change icon decorator
			tn.setIconDecorator1CssClass("o_scorm_" + itemsStat.get(itemId));
		}
	}

}