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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsBackController;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
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
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.helpers.Settings;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.ScormCourseNode;
import org.olat.modules.scorm.events.FinishEvent;
import org.olat.modules.scorm.events.UnloadSCOCommand;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Controller that handles the display of a single Scorm sco item and delegates
 * the sco api calls to the scorm RTE backend. It provides also an navigation to
 * navigate in the tree with "pre" "next" buttons.
 */
public class ScormAPIandDisplayController extends MainLayoutBasicController implements ConfigurationChangedListener {

	private static final int EXPIRATION_TIME = 10 * 60;// 10 minutes
	private static final String SCORM_CONTENT_FRAME = "scormContentFrame";
	private static final String BACK_PSEUDO_SCO = "oo-back";
	
	private VelocityContainer displayContent;
	private MenuTree menuTree;
	private Controller columnLayoutCtr;
	private ScormCPManifestTreeModel treeModel;
	private IFrameDisplayController iframeCtr;

	private Link nextScoTop;
	private Link nextScoBottom;
	private Link previousScoTop;
	private Link previousScoBottom;
	private ListPanel scoTopButtons;
	private ListPanel scoBottomButtons;
	
	private String username;
	private String scorm_lesson_mode;
	private String scormAgaingCallbackUri;

	private String requestScoId;
	private ScormSessionController sessionController;
	
	@Autowired
	private ScormMainManager scormMainManager;

	/**
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param showMenu if true, the ims cp menu is shown
	 * @param cpRoot
	 * @param scormResourceId The SCORM learn resource
	 * @param courseIdNodeId The course ID and optional the course node ID combined with "-". Example: 77554952047098-77554952047107
	 * @param lesson_mode add null for the default value or "normal", "browse" or "review"
	 * @param credit_mode add null for the default value or "credit", "no-credit"
	 * @param attemptsIncremented Is the attempts counter already incremented
	 * @param assessableType
	 * @param activate Open the layout
	 * @param fullWindow Open in full window
	 * @param radomizeDelivery Randomize delivery for development
	 * @param deliveryOptions This delivery options can override the default from the SCORM module
	 */
	ScormAPIandDisplayController(UserRequest ureq, WindowControl wControl, boolean showMenu,
			File cpRoot, Long scormResourceId, String courseIdNodeId, String lesson_mode, String credit_mode,
			String assessableType, boolean activate, boolean fullWindow, boolean attemptsIncremented,
			boolean radomizeDelivery, DeliveryOptions deliveryOptions) {
		super(ureq, wControl);
		
		// logging-note: the callers of createScormAPIandDisplayController make sure they have the scorm resource added to the ThreadLocalUserActivityLogger
		ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_OPEN, getClass());
		this.username = ureq.getIdentity().getName();
		if (!lesson_mode.equals(ScormConstants.SCORM_MODE_NORMAL) && !lesson_mode.equals(ScormConstants.SCORM_MODE_REVIEW) && !lesson_mode.equals(ScormConstants.SCORM_MODE_BROWSE)) {
			//throw new AssertException("Wrong parameter for constructor, only 'normal', 'browse' or 'review' are allowed for lesson_mode");
		}
		if (!credit_mode.equals("credit") && !credit_mode.equals("no-credit")) {
			//throw new AssertException("Wrong parameter for constructor, only 'credit' or 'no-credit' are allowed for credit_mode");
		}
		
		scorm_lesson_mode = lesson_mode;
		
		// id -> static component without timestamp
		displayContent = createVelocityContainer("scorm_display", "display");
		JSAndCSSComponent scormAgain = new JSAndCSSComponent("apiadapter", new String[] { "js/scorm/scorm-again.min.js" }, null);
		displayContent.put("apiadapter", scormAgain);
		
		ICourse course = null;
		OLATResourceable courseOres = null;
		CourseNode courseNode = null;
		// load course where this scorm package runs in
		if (courseIdNodeId != null) {
			String courseId = courseIdNodeId;
			String courseNodeId = null;
			int delimiterPos = courseId.indexOf("-");
			if (delimiterPos != -1) {
				// remove course node id from combined course id / node id value
				courseId = courseIdNodeId.substring(0, delimiterPos);
				if(courseIdNodeId.length() > delimiterPos + 1) {
					courseNodeId = courseIdNodeId.substring(delimiterPos + 1);
				}
			}
			Long courseOresId = Long.valueOf(courseId);
			courseOres = OresHelper.createOLATResourceableInstance(CourseModule.class, courseOresId);
			course = CourseFactory.loadCourse(courseOresId);
			if(courseNodeId != null) {
				courseNode = course.getRunStructure().getNode(courseNodeId);
			}
		}
		
		// init SCORM adapter
		try {
			sessionController = new ScormSessionController(getIdentity(), assessableType);
			String fullname = UserManager.getInstance().getUserDisplayName(getIdentity());
			String scormResourceIdStr = scormResourceId == null ? null : scormResourceId.toString();
			sessionController.init(cpRoot, scormResourceIdStr, courseIdNodeId, FolderConfig.getCanonicalRoot(), username, fullname, lesson_mode, credit_mode, hashCode());
			if(course != null && courseNode instanceof ScormCourseNode) {
				sessionController.initCurrentScore(course, (ScormCourseNode)courseNode);
			}
		} catch (IOException e) {
			showError("error.manifest.corrupted");
			LayoutMain3ColsController ctr = new LayoutMain3ColsController(ureq, getWindowControl(), null, new Panel("empty"), "scorm" + scormResourceId);
			columnLayoutCtr = ctr;
			putInitialPanel(columnLayoutCtr.getInitialComponent());
			return;
		}
		
		// even if we do not show the menu, we need to build parse the manifest
		// and find the first node to display at startup
		File mani = new File(cpRoot, "imsmanifest.xml");
		if (!mani.exists()) { 
			throw new OLATRuntimeException("error.manifest.missing", null, getClass().getName(), "CP " + cpRoot.getAbsolutePath() + " has no imsmanifest", null);
		}
		treeModel = new ScormCPManifestTreeModel(mani, sessionController.getScoItemsStatus());
		menuTree = new MenuTree("cpDisplayTree");
		menuTree.setTreeModel(treeModel);
		menuTree.addListener(this);
		
		ScormPackageConfig packageConfig = scormMainManager.getScormPackageConfig(cpRoot);
		if((deliveryOptions == null || (deliveryOptions.getInherit() != null && deliveryOptions.getInherit().booleanValue()))
				&& packageConfig != null) {
			deliveryOptions = packageConfig.getDeliveryOptions();
		}
		iframeCtr = new IFrameDisplayController(ureq, wControl, new LocalFolderImpl(cpRoot), SCORM_CONTENT_FRAME, courseOres, deliveryOptions, true, radomizeDelivery);
		listenTo(iframeCtr);
		displayContent.put("contentpackage", iframeCtr.getInitialComponent());
		displayContent.contextPut("frameId", SCORM_CONTENT_FRAME);
		
		initButtons();
		
		// bootId is the item the user left the sco last time or the first one
		String bootId = sessionController.getScormLastAccessedItemId();
		TreeNode bootNode = treeModel.getNodeByScormItemId(bootId);
		setSco(bootId, bootNode, false);

		if (activate) {
			LayoutMain3ColsBackController ctr = new LayoutMain3ColsBackController(ureq, getWindowControl(), (showMenu ? menuTree : null), displayContent, "scorm" + scormResourceId);
			ctr.setDeactivateOnBack(false);
			if(fullWindow) {
				ctr.setAsFullscreen();
			}
			columnLayoutCtr = ctr;
		} else {
			LayoutMain3ColsController ctr = new LayoutMain3ColsController(ureq, getWindowControl(), (showMenu ? menuTree : null), displayContent, "scorm" + scormResourceId);
			columnLayoutCtr = ctr;			
			putInitialPanel(columnLayoutCtr.getInitialComponent());
		}
		listenTo(columnLayoutCtr);
		
		//SCORM API calls get handled by this mapper
		Mapper againMapper = new ScormAgainMapper(sessionController);
		String mapperId = courseIdNodeId;
		scormAgaingCallbackUri = registerCacheableMapper(ureq, mapperId, againMapper, EXPIRATION_TIME);
		displayContent.contextPut("scormAgainCallbackUri", scormAgaingCallbackUri);
	}
	
	private void initButtons() {
		//previous / next navigation links
		nextScoTop = LinkFactory.createCustomLink("nextScoTop", "nextsco", "", Link.NONTRANSLATED | Link.BUTTON, displayContent, this);
		nextScoTop.setIconLeftCSS("o_icon o_icon_next_page");
		
		previousScoTop = LinkFactory.createCustomLink("previousScoTop", "previoussco", "", Link.NONTRANSLATED | Link.BUTTON, displayContent, this);
		previousScoTop.setIconLeftCSS("o_icon o_icon_previous_page");
		
		nextScoBottom = LinkFactory.createCustomLink("nextScoBottom", "nextsco", "", Link.NONTRANSLATED | Link.BUTTON, displayContent, this);
		nextScoBottom.setIconLeftCSS("o_icon o_icon_next_page");
		
		previousScoBottom = LinkFactory.createCustomLink("previousScoBottom", "previoussco", "", Link.NONTRANSLATED | Link.BUTTON, displayContent, this);
		previousScoBottom.setIconLeftCSS("o_icon o_icon_previous_page");
		
		scoTopButtons = new ListPanel("scoTopButtons", "o_scorm_navigation");
		scoTopButtons.addContent(previousScoTop);
		scoTopButtons.addContent(nextScoTop);
		scoBottomButtons = new ListPanel("scoBottomButtons", "o_scorm_navigation");
		scoBottomButtons.addContent(previousScoBottom);
		scoBottomButtons.addContent(nextScoBottom);
		
		// show the buttons, default. use setter method to change default behaviour
		displayContent.contextPut("showNavButtons", Boolean.TRUE);
				
		displayContent.put("scoTopButtons", scoTopButtons);
		displayContent.put("scoBottomButtons", scoBottomButtons);
	}
	
	/**
	 * Configuration method to enable/disable the havigation buttons that appear
	 * on the right side above and below the content. Default is set to true.
	 * 
	 * @param showNavButtons
	 */
	public void showNavButtons(boolean showNavButtons) {
		displayContent.contextPut("showNavButtons", Boolean.valueOf(showNavButtons));
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
		iframeCtr.setHeightPX(height);
	}
	
	public void setRawContent(boolean rawContent) {
		iframeCtr.setRawContent(rawContent);
	}
	
	public DeliveryOptions getDeliveryOptions() {
		return iframeCtr.getDeliveryOptions();
	}
	
	public void setDeliveryOptions(DeliveryOptions config) {
		iframeCtr.setDeliveryOptions(config);
	}
	
	public void setContentEncoding(String encoding) {
		iframeCtr.setContentEncoding(encoding);
	}
	
	public void setJSEncoding(String encoding) {
		iframeCtr.setJSEncoding(encoding);
	}

	public void close() {
		if(columnLayoutCtr instanceof LayoutMain3ColsBackController) {
			((LayoutMain3ColsBackController)columnLayoutCtr).deactivate();
		}
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		getLogger().debug("SCORM Event: {} ", event.getCommand());
		if (source instanceof Link) {
			doNextOrPreviousSco((Link)source);
		} else if("LMSInitialize".equals(event.getCommand())) {
			doLmsInitialize();
		} else if("LMSFinish".equals(event.getCommand())) {
			doLmsFinish(ureq);
		} else if("LMSTimeout".equals(event.getCommand())) {
			doTimeout(ureq);
		} else if(source == menuTree) {
			doGoToSco((TreeEvent) event);
		}	 
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == columnLayoutCtr) {
			if(event == Event.BACK_EVENT) {
				doBack(ureq);
			}
		}
		super.event(ureq, source, event);
	}
	
	private void doNextOrPreviousSco(Link link) {
		String nextScoId = (String)link.getUserObject();
		TreeNode tn = treeModel.getNodeByScormItemId(nextScoId);
		if(tn != null) {
			requestSco(nextScoId);
		}
	}
	
	private void doLmsInitialize() {
		sessionController.lmsInitialize();
	}
	
	private void doLmsFinish(UserRequest ureq) {
		getLogger().debug("doLmsFinish: requested: {}, current SCO: {}", requestScoId, sessionController.getCurrenSCOId());
		
		Map<String,String> cmis = extractCmisFromRequest(ureq);
		String nextScoId = requestScoId;
		sessionController.lmsFinish(true, cmis);
		if(BACK_PSEUDO_SCO.equals(nextScoId)) {
			executeBack(ureq);
		} else if(nextScoId != null) {
			TreeNode tn = treeModel.getNodeByScormItemId(nextScoId);
			setSco(nextScoId, tn, false);
		} else if(sessionController.getNumOfSCOs() == 1 && sessionController.isCurrentSCOFinished()) {
			executeBack(ureq);
			fireEvent(ureq, new FinishEvent());
		} else {
			updateMenuTreeIconsAndMessages();
		}
	}

	/**
	 * @param te is an Event fired by clicking a node in a tree
	 */
	public void doGoToSco(TreeEvent te) {
		// switch to the new page
		String nodeId = te.getNodeId();
		TreeNode tn = treeModel.getNodeById(nodeId);
		if (te.getCommand().equals(MenuTree.COMMAND_TREENODE_EXPANDED)) {
			if(menuTree.isOpen(tn)) {
				menuTree.getOpenNodeIds().remove(tn.getIdent());
			} else {
				menuTree.open(tn);
			}
			menuTree.setDirty(true);
		} else {
			String scoId = String.valueOf(treeModel.lookupScormNodeId(tn));
			requestSco(scoId);
		}
	}
	
	/**
	 * Request back. If this is the second time, force closing the SCORM. If
	 * the SCO has it's exit status set to suspend or logout, go straight to
	 * execute the back, if not unload the SCO.
	 * 
	 * @param ureq The user request
	 */
	protected void doBack(UserRequest ureq) {
		if(BACK_PSEUDO_SCO.equals(requestScoId)) {
			// force close
			fireEvent(ureq, Event.CLOSE_EVENT);
		} else {
			requestBack();
		}
	}
	
	private void doTimeout(UserRequest ureq) {
		String scoId = ureq.getParameter("scoId");
		String command = ureq.getParameter("command");
		if(scoId != null && scoId.equals(sessionController.getCurrenSCOId())
				&& !sessionController.isCurrentSCOFinished()) {
			Map<String,String> cmis = extractCmisFromRequest(ureq);
			sessionController.lmsCommit(scoId, false, cmis);
		}
		if("Back".equalsIgnoreCase(command)) {
			executeBack(ureq);
		} else if(requestScoId != null) {
			TreeNode tn = treeModel.getNodeByScormItemId(requestScoId);
			if(tn != null) {
				setSco(requestScoId, tn, true);
			}
		}
	}
	
	private Map<String,String> extractCmisFromRequest(UserRequest ureq) {
		String data = ureq.getParameter("cmis");
		JSONObject cmi = new JSONObject(data);
		Map<String,String> cmis = new HashMap<>();
		for(Iterator<String> keyIterator = cmi.keys(); keyIterator.hasNext(); ) {
			String key = keyIterator.next();
			if(key.startsWith("cmi.")) {
				Object val = cmi.get(key);	
				cmis.put(key, val.toString());
			}
		}
		return cmis;
	}
	
	private void executeBack(UserRequest ureq) {
		if(columnLayoutCtr instanceof LayoutMain3ColsBackController) {
			((LayoutMain3ColsBackController)columnLayoutCtr).deactivate();
		}
		fireEvent(ureq, Event.BACK_EVENT);
	}

	private void requestBack() {
		requestScoId = BACK_PSEUDO_SCO;
		getWindowControl().getWindowBackOffice()
			.sendCommandTo(new UnloadSCOCommand("Back", sessionController.getCurrenSCOId(), requestScoId));
	}
	
	private void requestSco(String nextScoId) {
		requestScoId = nextScoId;
		getWindowControl().getWindowBackOffice()
			.sendCommandTo(new UnloadSCOCommand("Next", sessionController.getCurrenSCOId(), nextScoId));
	}
	
	private void setSco(String scoId, TreeNode tn, boolean force) {
		requestScoId = null;
		menuTree.setSelectedNodeId(tn.getIdent());
		iframeCtr.getInitialComponent().setVisible(true);
		String identifierRes = (String) tn.getUserObject();
		updateNextPreviousButtons(scoId);
		if (sessionController.hasItemPrerequisites(scoId)) {
			iframeCtr.getInitialComponent().setVisible(false);
			showInfo("scorm.item.has.preconditions");
			return;
		}
		
		sessionController.launchItem(scoId, force);
		if(StringHelper.containsNonWhitespace(identifierRes)) {
			iframeCtr.setCurrentURI(identifierRes);
		} else {
			String emptyUrl = emptyPageUrl();
			iframeCtr.setCurrentURI(emptyUrl);
		}
		loadCmisData(scoId);
		updateMenuTreeIconsAndMessages();
	}
	
	private String emptyPageUrl() {
		try(StringOutput sb=new StringOutput()) {
			sb.append(Settings.getServerContextPathURI());
			Renderer.renderStaticURI(sb, "empty.html");
			return sb.toString();
		} catch(IOException e) {
			return null;
		}
	}
	
	private void loadCmisData(String scoId) {
		Set<String> readOnlyKeys = Set.of("cmi.interactions._count", "cmi.objectives._count");
		String[][] cmis = sessionController.getScoCmis(scoId);
		JSONObject obj = new JSONObject();
		if(cmis != null) {
			for(String[] cmi:cmis) {
				String key = cmi[0];
				if(key.endsWith("._count") || readOnlyKeys.contains(key)) {
					continue;
				}
				if(key.startsWith("cmi.")) {
					key = key.substring(4);
				}
				obj.put(key, cmi[1]);
			}
		}
		String cmisJson = obj.toString();
		displayContent.contextPut("startCmis", cmisJson);
		displayContent.contextPut("scoId", scoId);
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

	@Override
	protected void doDispose() {
		cleanUpCollectedScoData();
	}

	/**
	 * in "browse" or "review" mode we dont collect sco data
	 */
	private void cleanUpCollectedScoData() {
		if(scorm_lesson_mode.equals(ScormConstants.SCORM_MODE_BROWSE) ||
			 scorm_lesson_mode.equals(ScormConstants.SCORM_MODE_REVIEW)) {
			StringBuilder path = new StringBuilder();
			path.append(WebappHelper.getTmpDir())
			  .append("/tmp").append(WebappHelper.getInstanceId()).append("scorm/")
			  .append(hashCode());
			FileUtils.deleteDirsAndFiles( new File(path.toString()),true, true);
		}
	}
	
	private void updateNextPreviousButtons(String nextScoId) {
		Integer nextInt = sessionController.getNextSco(nextScoId);
		Integer preInt = sessionController.getPreviousSco(nextScoId);
		
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
	
	public void activate() {
		if (columnLayoutCtr instanceof LayoutMain3ColsBackController){
			LayoutMain3ColsBackController ctrl =  (LayoutMain3ColsBackController)columnLayoutCtr;
			ctrl.activate();
		}
	}

	private void updateMenuTreeIconsAndMessages() {
		menuTree.setDirty(true);
		Map<String,String> itemsStat = sessionController.getScoItemsStatus();
		Map<String,GenericTreeNode> idToNode = treeModel.getScormIdToNodeRelation();
		
		for (Iterator<String> it = itemsStat.keySet().iterator(); it.hasNext();) {
			String itemId = it.next();
			GenericTreeNode tn = idToNode.get(itemId);
			// change icon decorator
			tn.setIconDecorator1CssClass("o_scorm_" + itemsStat.get(itemId));
		}
	}
}