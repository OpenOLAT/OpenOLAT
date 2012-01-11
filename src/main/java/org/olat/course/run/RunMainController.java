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

package org.olat.course.run;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.bookmark.AddAndEditBookmarkController;
import org.olat.bookmark.BookmarkManager;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlsite.OlatCmdEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.dtabs.Activateable;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.gui.control.generic.messages.MessageController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.gui.control.generic.textmarker.GlossaryMarkupItemController;
import org.olat.core.gui.control.generic.title.TitledWrapperController;
import org.olat.core.gui.control.generic.tool.ToolController;
import org.olat.core.gui.control.generic.tool.ToolFactory;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLATSecurityException;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.CourseLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerExecutor;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.archiver.ArchiverMainController;
import org.olat.course.archiver.IArchiverCallback;
import org.olat.course.assessment.AssessmentChangedEvent;
import org.olat.course.assessment.AssessmentUIFactory;
import org.olat.course.assessment.CoachingGroupAccessAssessmentCallback;
import org.olat.course.assessment.EfficiencyStatement;
import org.olat.course.assessment.EfficiencyStatementController;
import org.olat.course.assessment.EfficiencyStatementManager;
import org.olat.course.assessment.FullAccessAssessmentCallback;
import org.olat.course.config.CourseConfig;
import org.olat.course.config.CourseConfigEvent;
import org.olat.course.editor.PublishEvent;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.groupsandrights.CourseRights;
import org.olat.course.groupsandrights.ui.CourseGroupManagementMainController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.calendar.CourseCalendarController;
import org.olat.course.run.glossary.CourseGlossaryFactory;
import org.olat.course.run.glossary.CourseGlossaryToolLinkController;
import org.olat.course.run.navigation.NavigationHandler;
import org.olat.course.run.navigation.NodeClickedRef;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.course.statistic.StatisticMainController;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManager;
import org.olat.group.BusinessGroupManagerImpl;
import org.olat.group.ui.BGControllerFactory;
import org.olat.group.ui.edit.BusinessGroupModifiedEvent;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.instantMessaging.groupchat.GroupChatManagerController;
import org.olat.modules.cp.TreeNodeEvent;
import org.olat.note.NoteController;
import org.olat.properties.Property;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatus;
import org.olat.repository.RepositoryManager;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.RepositoryDetailsController;
import org.olat.repository.controllers.RepositoryMainController;
import org.olat.repository.site.RepositorySite;
import org.olat.resource.accesscontrol.ui.SecurityGroupsRepositoryMainController;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class RunMainController extends MainLayoutBasicController implements GenericEventListener, Activateable, Activateable2 {
	private static final String COMMAND_EDIT = "gotoeditor";
	private static final String TOOLBOX_LINK_COURSECONFIG = "courseconfig";

	private static final String JOINED = "joined";
	private static final String LEFT   = "left";

	private static final String CMD_START_GROUP_PREFIX = "cmd.group.start.ident.";

	private static final String ACTION_CALENDAR = "cal";
	private static final String ACTION_BOOKMARK = "bm";
	private static final String TOOL_BOOKMARK = "b";
	
	public static final String ORES_TYPE_COURSE_RUN = OresHelper.calculateTypeName(RunMainController.class, CourseModule.ORES_TYPE_COURSE);
	private final OLATResourceable courseRunOres; //course run ores for course run channel 

	private ICourse course;//o_clusterOK: this controller listen to course change events
	private RepositoryEntry courseRepositoryEntry;
	private MenuTree luTree;
	private Panel contentP;
	private Panel all;

	private NavigationHandler navHandler;
	private UserCourseEnvironment uce;
	private LayoutMain3ColsController columnLayoutCtr;
	private GroupChatManagerController courseChatManagerCtr;

	private Controller currentToolCtr;
	private ToolController toolC;
	private Controller currentNodeController; // the currently open node config
	private AddAndEditBookmarkController bookmarkController;

	private boolean isInEditor = false;

	private Map courseRightsCache = new HashMap();
	private boolean isCourseAdmin = false;
	private boolean isCourseCoach = false;
	private List ownedGroups;
	private List participatedGroups;
	private List waitingListGroups;
	private List rightGroups;

	private CourseNode currentCourseNode;
	private TreeModel treeModel;
	private boolean needsRebuildAfterPublish;
	private boolean needsRebuildAfterRunDone = false;
	
	private String courseTitle;
	private boolean isGuest = true;
	private boolean offerBookmark = true;
	
	private GlossaryMarkupItemController glossaryMarkerCtr;
	private CourseGlossaryToolLinkController glossaryToolCtr;
	private boolean showCourseConfigLink;
	private VelocityContainer coursemain;
	
	private boolean assessmentChangedEventReceived = false;
	private Link currentUserCountLink;
	private int currentUserCount;
	
	/**
	 * Constructor for the run main controller
	 * 
	 * @param ureq The user request
	 * @param wControl The current window controller
	 * @param course The current course
	 * @param initialViewIdentifier if null the default view will be started,
	 *          otherwise a controllerfactory type dependant view will be
	 *          activated (subscription subtype) number: node with nodenumber will
	 *          be activated "assessmentTool": assessment tool will be activated
	 * @param offerBookmark - whether to offer bookmarks or not
	 * @param showCourseConfigLink  Flag to enable/disable link to detail-page in tool menu. 
	 */
	public RunMainController(final UserRequest ureq, final WindowControl wControl, final ICourse course, final String initialViewIdentifier,
			final boolean offerBookmark, final boolean showCourseConfigLink) {
		this(ureq, wControl, course, initialViewIdentifier, offerBookmark, showCourseConfigLink, false);
	}

	/**
	 * fxdiff: change these two constructors to allow setting of launchFromSite
	 * for CourseSite
	 * 
	 * @param ureq
	 * @param wControl
	 * @param course
	 * @param initialViewIdentifier
	 * @param offerBookmark
	 * @param showCourseConfigLink
	 * @param launchFromSite allows to set disposed controller after init!
	 */
	public RunMainController(final UserRequest ureq, final WindowControl wControl, final ICourse course, final String initialViewIdentifier,
			final boolean offerBookmark, final boolean showCourseConfigLink, final boolean launchFromSite) {

		super(ureq, wControl);

		this.course = course;
		addLoggingResourceable(LoggingResourceable.wrap(course));
		this.courseTitle = course.getCourseTitle();
		this.courseRepositoryEntry = RepositoryManager.getInstance().lookupRepositoryEntry(course, true);
		this.offerBookmark = offerBookmark;
		this.showCourseConfigLink = showCourseConfigLink;
		this.courseRunOres = OresHelper.createOLATResourceableInstance(ORES_TYPE_COURSE_RUN, course.getResourceableId());
		
		Identity identity = ureq.getIdentity();
		// course and system logging
		ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_ENTERING, getClass());
		
		// log shows who entered which course, this can then be further used to jump
		// to the courselog
		Tracing.logAudit("Entering course: [[["+courseTitle+"]]]", course.getResourceableId().toString(), RunMainController.class);
		
		// set up the components
		all = new Panel("allofcourse");
		luTree = new MenuTree("luTreeRun", this);
		luTree.setExpandSelectedNode(false);
		contentP = new Panel("building_block_content");

		// get all group memberships for this course
		initGroupMemberships(identity);
		// Initialize the the users roles and right for this course
		// 1) is guest flag
		isGuest = ureq.getUserSession().getRoles().isGuestOnly();
		// 2) all course internal rights
		initUserRolesAndRights(identity);

		// preload user assessment data in assessmnt properties cache to speed up
		// course loading
		course.getCourseEnvironment().getAssessmentManager().preloadCache(identity);

		// build up the running structure for this user;
		uce = new UserCourseEnvironmentImpl(ureq.getUserSession().getIdentityEnvironment(), course.getCourseEnvironment());
		// build score now
		uce.getScoreAccounting().evaluateAll();
		navHandler = new NavigationHandler(uce, false);

		// jump to either the forum or the folder if the business-launch-path says
		// so.
		boolean showAssessmentTool = false;
		
		BusinessControl bc = getWindowControl().getBusinessControl();
		ContextEntry ce = bc.popLauncherContextEntry();
		if (ce != null) {
			logDebug("businesscontrol (for further jumps) would be:" + bc, null);
			OLATResourceable ores = ce.getOLATResourceable();
			logDebug("OLATResourceable=" + ores, null);
			if (OresHelper.isOfType(ores, CourseNode.class)) {
				// TODO, don't use CourseNode.class, but a lookup using the ClassToId
				// class
				// jump to the coursenode with id
				Long nodeId = ores.getResourceableId();
				String nodeIdS = nodeId.toString();
				currentCourseNode = course.getRunStructure().getNode(nodeIdS);
				if (currentCourseNode == null) {
					// (e.g. a coursenode that no longer is accessible or existing)
					// -> fallback to root node (automatically done later), and drop wrong
					// remaining contexts
					logDebug("currentCourseNode=null => dropLauncherEntries for nodeIdS=" + nodeIdS, null);
					bc.dropLauncherEntries();
				}
			} else if ("assessmentTool".equals(ores.getResourceableTypeName())) {
				showAssessmentTool = true;
			}
		}
		/*
		 * ContextEntry curCe = bc.getCurrentEntryAndAdvance(); if (curCe != null) { //
		 * e.g something like FOCourseNode:3243 or AssementTool:0 or AssmentTool:1
		 * (0,1 must be dokumented) OLATResourceable ores =
		 * curCe.getOLATResourceable(); if (OresHelper.isOfType(ores,
		 * CourseNode.class)) { // TODO, don't use CourseNode.class, but a lookup
		 * using the ClassToId class // jump to the coursenode with id Long nodeId =
		 * ores.getResourceableId(); String nodeIdS = nodeId.toString();
		 * currentCourseNode = course.getRunStructure().getNode(nodeIdS); } else {
		 * //TODO assessment tool } }
		 */
		
		
		String subsubId = null;
		String assessmentViewIdentifier = null;
		// activate assessent tool or node as the first screen
		if (initialViewIdentifier != null) {
			if (initialViewIdentifier.equals("assessmentTool")) {
				showAssessmentTool = true;
				// assessmentViewIdentifier must be null
			} else if (initialViewIdentifier.equals("assessmentTool:nodeChoose")) {
				showAssessmentTool = true;
				assessmentViewIdentifier = "node-choose";
			} else {
				// check for subsubIdent
				if (initialViewIdentifier.indexOf(":") != -1) {
					String[] viewIdentifier = initialViewIdentifier.split(":");
					currentCourseNode = course.getRunStructure().getNode(viewIdentifier[0]);
					subsubId = viewIdentifier[1];
				} else {
					currentCourseNode = course.getRunStructure().getNode(initialViewIdentifier);
				}
			}
		}

		updateTreeAndContent(ureq, currentCourseNode, subsubId);
		
		//set the launch date after the evaluation
		setLaunchDates(identity);

		if (courseRepositoryEntry != null && RepositoryManager.getInstance().createRepositoryEntryStatus(courseRepositoryEntry.getStatusCode()).isClosed()) {
			wControl.setWarning(translate("course.closed"));
		}
		
		// instant messaging: send presence like "reading course: Biology I" as
		// awareness info. Important: do this before initializing the toolbox
		// controller!
		if (InstantMessagingModule.isEnabled() && CourseModule.isCourseChatEnabled() && !isGuest) {
			String intro = translate("course.presence.message.enter") + " " + getExtendedCourseTitle(ureq.getLocale());
			InstantMessagingModule.getAdapter().sendStatus(identity.getName(), intro);
			if (course.getCourseEnvironment().getCourseConfig().isChatEnabled()) {
				// create groupchat room link only if course chat is enabled
				createCourseGroupChatLink(ureq);
			}
		}

		// add text marker wrapper controller to implement course glossary
		// textMarkerCtr must be created before the toolC!
		CourseConfig cc = uce.getCourseEnvironment().getCourseConfig();
		glossaryMarkerCtr = CourseGlossaryFactory.createGlossaryMarkupWrapper(ureq, wControl, contentP, cc);
	
		boolean hasGlossaryRights = hasCourseRight(CourseRights.RIGHT_GLOSSARY) || isCourseAdmin;
		glossaryToolCtr = new CourseGlossaryToolLinkController(getWindowControl(), ureq, course, getTranslator(), hasGlossaryRights, 
				uce.getCourseEnvironment(), glossaryMarkerCtr);
		listenTo(glossaryToolCtr);
		// init the menu and tool controller
		toolC = initToolController(identity, ureq);
		listenTo (toolC);
		
		Component toolComp = (toolC == null ? null : toolC.getInitialComponent());

		if (glossaryMarkerCtr != null) {
			listenTo(glossaryMarkerCtr);
			// enable / disable glossary highlighting according to user prefs
			Preferences prefs = ureq.getUserSession().getGuiPreferences();
			Boolean state = (Boolean) prefs.get(CourseGlossaryToolLinkController.class, CourseGlossaryFactory.createGuiPrefsKey(course));

			//Glossary always on for guests. OLAT-4241
			if(ureq.getUserSession().getRoles().isGuestOnly()){
				state = Boolean.TRUE;
			}
			
			if (state == null) {
				glossaryMarkerCtr.setTextMarkingEnabled(false);
			} else {
				glossaryMarkerCtr.setTextMarkingEnabled(state.booleanValue());
			}
			columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), luTree, toolComp, glossaryMarkerCtr.getInitialComponent(), "course" + this.course.getResourceableId());				
		} else {
			columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), luTree, toolComp, contentP, "courseRun" + this.course.getResourceableId());							
		}
		listenTo(columnLayoutCtr);

		// activate the custom course css if any
		setCustomCSS(CourseFactory.getCustomCourseCss(ureq.getUserSession(), uce.getCourseEnvironment()));

		// decide what is show fist - a tool since activaded this way or the regular
		// course view
		if (showAssessmentTool) {
			launchAssessmentTool(ureq, assessmentViewIdentifier);
			coursemain = createVelocityContainer("index");
			coursemain.put("coursemain", all);
		} else {
			all.setContent(columnLayoutCtr.getInitialComponent());
			coursemain = createVelocityContainer("index");
			// see function gotonode in functions.js to see why we need the
			// repositoryentry-key here:
			// it is to correctly apply external links using course-internal links via
			// javascript
			coursemain.contextPut("courserepokey", courseRepositoryEntry.getKey());
			coursemain.put("coursemain", all);
		}
		
		putInitialPanel(coursemain);

		if (!launchFromSite) {
			// disposed message controller
			// must be created beforehand
			Panel empty = new Panel("empty");// empty panel set as "menu" and "tool"
			Controller courseCloser = CourseFactory.createDisposedCourseRestartController(ureq, wControl,
					courseRepositoryEntry.getResourceableId());
			Controller disposedRestartController = new LayoutMain3ColsController(ureq, wControl, empty, empty,
					courseCloser.getInitialComponent(), "disposed course" + this.course.getResourceableId());
			setDisposedMsgController(disposedRestartController);
		}

		// add as listener to course so we are being notified about course events:
		// - publish changes
		// - assessment events
		// - listen for CourseConfig events
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, identity, course);
		// - group modification events
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, identity, courseRepositoryEntry);
		
		if (CourseModule.displayParticipantsCount()) {
			//eventAgency for identity per course counting purposes				
			CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, identity, courseRunOres);		
			CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new MultiUserEvent(JOINED), courseRunOres);
			updateCurrentUserCount();
		}
	}
	
	private void setLaunchDates(final Identity identity) {
		CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(createOLATResourceableForLocking(identity), new SyncerExecutor(){
			public void execute() {
				CourseNode rootNode = course.getRunStructure().getRootNode();
				//log launch date
				String nowString = Long.toString(course.getCourseEnvironment().getCurrentTimeMillis());
				CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
				Property initialLaunchProperty = cpm.findCourseNodeProperty(rootNode, identity, null, ICourse.PROPERTY_INITIAL_LAUNCH_DATE);
				if(initialLaunchProperty == null) {
					initialLaunchProperty = cpm.createCourseNodePropertyInstance(rootNode, identity, null, ICourse.PROPERTY_INITIAL_LAUNCH_DATE, null, null, nowString, null);
					cpm.saveProperty(initialLaunchProperty);
				}
				
				Property recentLaunchProperty = cpm.findCourseNodeProperty(rootNode, identity, null, ICourse.PROPERTY_RECENT_LAUNCH_DATE);
				if (recentLaunchProperty == null) {
					recentLaunchProperty = cpm.createCourseNodePropertyInstance(rootNode, identity, null, ICourse.PROPERTY_RECENT_LAUNCH_DATE, null, null, nowString, null);
					cpm.saveProperty(recentLaunchProperty);
				} else {
					recentLaunchProperty.setStringValue(nowString);
					cpm.updateProperty(recentLaunchProperty);
				}
			}
		});
	}
	
	private OLATResourceable createOLATResourceableForLocking(Identity identity) {				
		String type = "CourseLaunchDate::Identity";
		OLATResourceable oLATResourceable = OresHelper.createOLATResourceableInstance(type,identity.getKey());
		return oLATResourceable;
	}

	
	/**
	 * @param ureq
	 */
	private void createCourseGroupChatLink(UserRequest ureq) {
		
		courseChatManagerCtr = InstantMessagingModule.getAdapter().getGroupChatManagerController(ureq);
		if (courseChatManagerCtr != null) {
			courseChatManagerCtr.createGroupChat(ureq, getWindowControl(), course, getExtendedCourseTitle(ureq.getLocale()), true, true);
			listenTo(courseChatManagerCtr);
		}
	}

	/**
	 * @param locale
	 * @return
	 */
	private String getExtendedCourseTitle(Locale locale) {
		String extendedCourseTitle = this.courseTitle;
		RepositoryEntry repositoryEntry = RepositoryManager.getInstance().lookupRepositoryEntry(course, false);
		if (repositoryEntry != null && RepositoryManager.getInstance().createRepositoryEntryStatus(repositoryEntry.getStatusCode()).isClosed()) {
			PackageTranslator pT = new PackageTranslator(RepositoryEntryStatus.class.getPackage().getName(), locale);
			extendedCourseTitle = "[" + pT.translate("title.prefix.closed") + "] ".concat(extendedCourseTitle);
		}
		return extendedCourseTitle;
	}

	private void initUserRolesAndRights(final Identity identity) {
		CourseGroupManager cgm = this.course.getCourseEnvironment().getCourseGroupManager();
		// 1) course admins: users who are in repository entry owner group
		// if user has the role InstitutionalResourceManager and has the same institution like author
		// then set isCourseAdmin true
		isCourseAdmin = cgm.isIdentityCourseAdministrator(identity) | RepositoryManager.getInstance().isInstitutionalRessourceManagerFor(courseRepositoryEntry, identity);
		// 2) course coaches: users who are in the owner group of any group of this
		// course
		isCourseCoach = cgm.isIdentityCourseCoach(identity);
		// 3) all other rights are defined in the groupmanagement using the learning
		// group rights
		courseRightsCache.put(CourseRights.RIGHT_GROUPMANAGEMENT, new Boolean(cgm.hasRight(identity, CourseRights.RIGHT_GROUPMANAGEMENT)));
		courseRightsCache.put(CourseRights.RIGHT_COURSEEDITOR, new Boolean(cgm.hasRight(identity, CourseRights.RIGHT_COURSEEDITOR)));
		courseRightsCache.put(CourseRights.RIGHT_ARCHIVING, new Boolean(cgm.hasRight(identity, CourseRights.RIGHT_ARCHIVING)));
		courseRightsCache.put(CourseRights.RIGHT_ASSESSMENT, new Boolean(cgm.hasRight(identity, CourseRights.RIGHT_ASSESSMENT)));
		courseRightsCache.put(CourseRights.RIGHT_GLOSSARY, new Boolean(cgm.hasRight(identity, CourseRights.RIGHT_GLOSSARY)));
		courseRightsCache.put(CourseRights.RIGHT_STATISTICS, new Boolean(cgm.hasRight(identity, CourseRights.RIGHT_STATISTICS)));
	}

	/**
	 * side-effecty to content and luTree
	 * 
	 * @param ureq
	 * @param calledCourseNode the node to jump to, if null = jump to root node
	 * @param nodecmd An optional command used to activate the run view or NULL if not available
	 * @return true if the node jumped to is visible
	 */
	private boolean updateTreeAndContent(UserRequest ureq, CourseNode calledCourseNode, String nodecmd) {
		return updateTreeAndContent(ureq, calledCourseNode, nodecmd, null, null);
	}
	
	//fxdiff BAKS-7 Resume function
	private boolean updateTreeAndContent(UserRequest ureq, CourseNode calledCourseNode, String nodecmd, List<ContextEntry> entries, StateEntry state) {
		// build menu (treemodel)
		// dispose old node controller before creating the NodeClickedRef which creates 
		// the new node controller. It is important that the old node controller is 
		// disposed before the new one to not get conflicts with cacheable mappers that
		// might be used in both controllers with the same ID (e.g. the course folder)
		if (currentNodeController != null) {
			currentNodeController.dispose();
		}
		NodeClickedRef nclr = navHandler.evaluateJumpToCourseNode(ureq, getWindowControl(), calledCourseNode, this, nodecmd);
		if (!nclr.isVisible()) {
			// if not root -> fallback to root. e.g. when a direct node jump fails
			if (calledCourseNode != null) {
				nclr = navHandler.evaluateJumpToCourseNode(ureq, getWindowControl(), null, null, null);
			}
			if (!nclr.isVisible()) {
				MessageController msgController = MessageUIFactory.createInfoMessage(ureq, this.getWindowControl(),	translate("course.noaccess.title"), translate("course.noaccess.text"));
				contentP.setContent(msgController.getInitialComponent());					
				luTree.setTreeModel(new GenericTreeModel());
				return false;
			}
		}

		treeModel = nclr.getTreeModel();
		luTree.setTreeModel(treeModel);
		String selNodeId = nclr.getSelectedNodeId();
		luTree.setSelectedNodeId(selNodeId);
		luTree.setOpenNodeIds(nclr.getOpenNodeIds());
		CourseNode courseNode = nclr.getCalledCourseNode();
		updateState(courseNode);

		// get new run controller.
		currentNodeController = nclr.getRunController();
		//fxdiff BAKS-7 Resume function
		if(currentNodeController instanceof Activateable2) {
			((Activateable2)currentNodeController).activate(ureq, entries, state);
		} else if (currentNodeController instanceof TitledWrapperController) {
			Controller contentcontroller = ((TitledWrapperController)currentNodeController).getContentController();
			if(contentcontroller instanceof Activateable2) {
				((Activateable2)contentcontroller).activate(ureq, entries, state);
			} else {
				addToHistory(ureq, currentNodeController);
			}
		} else {
			addToHistory(ureq, currentNodeController);
		}
		contentP.setContent(currentNodeController.getInitialComponent());
		// enableCustomCourseCSS(ureq);
		
		return true;
	}

	private void updateState(CourseNode courseNode) {
		// TODO: fj : describe when the course node can be null
		if (courseNode == null) return;
		
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {	
		
		if (source == luTree) {
			if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
				TreeEvent tev = (TreeEvent) event;
				if(this.assessmentChangedEventReceived) {
					uce.getScoreAccounting().evaluateAll();
					assessmentChangedEventReceived = false;
				}
				
				//getWindowControl().setInfo("time: "+System.currentTimeMillis());
				// goto node:
				// after a click in the tree, evaluate the model anew, and set the
				// selection of the tree again
				NodeClickedRef nclr = navHandler.evaluateJumpToTreeNode(ureq, getWindowControl(), treeModel, tev, this, null, currentNodeController);
				if (!nclr.isVisible()) {
					getWindowControl().setWarning(translate("msg.nodenotavailableanymore"));
					// go to root since the current node is no more visible 
					this.updateTreeAndContent(ureq, null, null);					
					return;
				}
				// a click to a subtree's node
				if (nclr.isHandledBySubTreeModelListener() || nclr.getSelectedNodeId() == null) {
					if(nclr.getRunController() != null) {
						//there is an update to the currentNodeController, apply it
						if (currentNodeController != null && !currentNodeController.isDisposed()) {
							currentNodeController.dispose();
						}
						currentNodeController = nclr.getRunController();
						Component nodeComp = currentNodeController.getInitialComponent();
						contentP.setContent(nodeComp);
					}
					return;
				}

				// set the new treemodel
				treeModel = nclr.getTreeModel();
				luTree.setTreeModel(treeModel);

				// set the new tree selection
				String nodeId = nclr.getSelectedNodeId();
				luTree.setSelectedNodeId(nodeId);
				luTree.setOpenNodeIds(nclr.getOpenNodeIds());
				currentCourseNode = nclr.getCalledCourseNode();
				updateState(currentCourseNode);

				// get new run controller. Dispose only if not already disposed in navHandler.evaluateJumpToTreeNode()
				if (currentNodeController != null && !currentNodeController.isDisposed()) currentNodeController.dispose();
				currentNodeController = nclr.getRunController();
				Component nodeComp = currentNodeController.getInitialComponent();
				contentP.setContent(nodeComp);
				//fxdiff BAKS-7 Resume function
				addToHistory(ureq, currentNodeController);
				
				// set glossary wrapper dirty after menu click to make it reload the glossary
				// stuff properly when in AJAX mode
				if (glossaryMarkerCtr != null && glossaryMarkerCtr.isTextMarkingEnabled()) {
					glossaryMarkerCtr.getInitialComponent().setDirty(true);
				}
				
				//re set current user count but not every click
				if (currentUserCountLink != null) {
					OLATResourceable courseRunOres = OresHelper.createOLATResourceableInstance(RunMainController.ORES_TYPE_COURSE_RUN, course.getResourceableId());
					int cUsers = CoordinatorManager.getInstance().getCoordinator().getEventBus().getListeningIdentityCntFor(courseRunOres);
					if (cUsers == 0) {
						cUsers = 1;
					}
					currentUserCountLink.setCustomDisplayText(getTranslator().translate("participants.in.course", new String[]{ String.valueOf(cUsers) }));
					currentUserCountLink.setEnabled(false);
				}
			}
		} else if (source == coursemain) {
			if (event.getCommand().equals("activateCourseNode")) {
				// Events from the JS function o_activateCourseNode() - activate the given node id
				String nodeid = ureq.getModuleURI();
				if (nodeid != null) {
					CourseNode identNode = course.getRunStructure().getNode(nodeid);
					boolean success = updateTreeAndContent(ureq, identNode, null);
					if (success) currentCourseNode = identNode;
					else getWindowControl().setWarning(translate("msg.nodenotavailableanymore"));
				}
			}
		}
	}
		
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		
		// event from the current tool (editor, groupmanagement, archiver)		
		if (source == currentToolCtr) {
			if (event == Event.DONE_EVENT) {
				// for all other tools just pop
				all.setContent(columnLayoutCtr.getInitialComponent());
				// special check for editor
				if (isInEditor) {
					isInEditor = false; // for clarity
					removeAsListenerAndDispose(currentToolCtr);
					currentToolCtr = null;
					if (needsRebuildAfterPublish) {
						needsRebuildAfterPublish = false;
						
					  // rebuild up the running structure for this user, after publish;
						uce = new UserCourseEnvironmentImpl(ureq.getUserSession().getIdentityEnvironment(), CourseFactory.loadCourse(course.getResourceableId()).getCourseEnvironment());
						// build score now
						uce.getScoreAccounting().evaluateAll();
						navHandler = new NavigationHandler(uce, false);
						
						// rebuild and jump to root node
						updateTreeAndContent(ureq, null, null);
						// and also tools (maybe new assessable nodes -> show efficiency
						// statment)
						removeAsListenerAndDispose(toolC);
						toolC = initToolController(ureq.getIdentity(), ureq);
						listenTo(toolC);
						
						Component toolComp = (toolC == null ? null : toolC.getInitialComponent());
						columnLayoutCtr.setCol2(toolComp);
					}
				} else {
					// release current node controllers resources and do cleanup if it was not the editor!
					removeAsListenerAndDispose(currentToolCtr);
					currentToolCtr = null;
				}
			}
		} else if (source == currentNodeController) {
			if (event instanceof OlatCmdEvent) {
				OlatCmdEvent oe = (OlatCmdEvent) event;
				String cmd = oe.getCommand();
				if (cmd.equals(OlatCmdEvent.GOTONODE_CMD)) {
					String subcmd = oe.getSubcommand(); // "69680861018558/node-specific-data";
					CourseNode identNode;
					String nodecmd = null;
					int slPos = subcmd.indexOf('/');
					if (slPos != -1) {
						nodecmd = subcmd.substring(slPos + 1);
						identNode = course.getRunStructure().getNode(subcmd.substring(0, slPos));
					} else {
						identNode = course.getRunStructure().getNode(subcmd);
					}
					currentCourseNode = identNode;
					updateTreeAndContent(ureq, identNode, nodecmd);
					oe.accept();
				}
			} else if (event == Event.DONE_EVENT) {
				// the controller is done.
				// we have a chance here to test if we need to refresh the evalution.
				// this is the case when a test was submitted and scoring has changed ->
				// preconditions may have changed
				if (needsRebuildAfterRunDone) {
					needsRebuildAfterRunDone = false;
					updateTreeAndContent(ureq, currentCourseNode, null);
				}
			} else if (event instanceof TreeNodeEvent) {
				TreeNodeEvent tne = (TreeNodeEvent) event;
				TreeNode newCpTreeNode = tne.getChosenTreeNode();
				luTree.setSelectedNodeId(newCpTreeNode.getIdent());
			} else if (event == Event.CHANGED_EVENT) {
				updateTreeAndContent(ureq, currentCourseNode, null);
			}

		} else if (source == toolC) {
			String cmd = event.getCommand();
			doHandleToolEvents(ureq, cmd);

		} else if (source == bookmarkController) {
			if (event.equals(Event.DONE_EVENT)) {
				toolC.setEnabled(TOOL_BOOKMARK, false);
			} else if (event.equals(Event.CANCELLED_EVENT)) {
				toolC.setEnabled(TOOL_BOOKMARK, true);
			}
			updateTreeAndContent(ureq, currentCourseNode, null);
		} else if (source == glossaryToolCtr) {
			//fire info to IFrameDisplayController
			Long courseID = course.getResourceableId();
			// must work with SP and CP nodes, IFrameDisplayController listens to this event and expects "ICourse" resources.
			String oresName = ICourse.class.getSimpleName();
			ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(new MultiUserEvent(event.getCommand()), OresHelper.createOLATResourceableInstance(oresName, courseID));
		}
	}

	private void doHandleToolEvents(UserRequest ureq, String cmd) {
		if (cmd.indexOf(CMD_START_GROUP_PREFIX) == 0) {
			// launch the group in a new top nav tab
			String groupIdent = cmd.substring(CMD_START_GROUP_PREFIX.length());
			Long groupKey = new Long(Long.parseLong(groupIdent));
			BusinessGroupManager groupManager = BusinessGroupManagerImpl.getInstance();
			BusinessGroup group = groupManager.loadBusinessGroup(groupKey, false);
			// check if the group still exists and the user is really in this group
			// (security, changed group)
			if (group != null && groupManager.isIdentityInBusinessGroup(ureq.getIdentity(), group)) {
				BaseSecurity securityManager = BaseSecurityManager.getInstance();
				boolean isCoach = securityManager.isIdentityInSecurityGroup(ureq.getIdentity(), group.getOwnerGroup());
				// create group without admin flag enabled eventhough the user might be
				// coach. the flag is not needed here
				// since the groups knows itself if the user is coach and the user sees
				// only his own groups.
				BGControllerFactory.getInstance().createRunControllerAsTopNavTab(group, ureq, getWindowControl(), /*ual, */false, null);
			} else {
				// display error and do logging
				getWindowControl().setError(translate("error.invalid.group"));
				logAudit("User tried to launch a group but user is not owner or participant "
						+ "of group or group doesn't exist. Hacker attack or group has been changed or deleted. group key :: " + groupKey, null);
				// refresh toolbox that contained wrong group
				initGroupMemberships(ureq.getIdentity());
				removeAsListenerAndDispose(toolC);
				toolC = initToolController(ureq.getIdentity(), ureq);
				listenTo(toolC);
				Component toolComp = (toolC == null ? null : toolC.getInitialComponent());
				columnLayoutCtr.setCol2(toolComp);

			}
		} else if (cmd.equals(COMMAND_EDIT)) {
			if (hasCourseRight(CourseRights.RIGHT_COURSEEDITOR) || isCourseAdmin) {
				Controller ec = CourseFactory.createEditorController(ureq, getWindowControl(), course);
				//user activity logger which was initialised with course run
				//
				if(ec != null){
					//we are in editing mode
					currentToolCtr = ec;
					listenTo(currentToolCtr);
					//currentToolCtr.addControllerListener(this); //deprecated, replaced by listenTo from BasicController
					isInEditor = true;
					all.setContent(currentToolCtr.getInitialComponent());
				}else{
					//editor could not be created
					//message already pushed as Error Box 
				}
				
			} else throw new OLATSecurityException("wanted to activate editor, but no according right");

		} else if (cmd.equals("groupmngt")) {
			if (hasCourseRight(CourseRights.RIGHT_GROUPMANAGEMENT) || isCourseAdmin) {
				currentToolCtr = new CourseGroupManagementMainController(ureq, getWindowControl(), course, BusinessGroup.TYPE_LEARNINGROUP);
				listenTo(currentToolCtr);
				all.setContent(currentToolCtr.getInitialComponent());
			} else throw new OLATSecurityException("clicked groupmanagement, but no according right");
		//fxdiff VCRP-1,2: access control of resources
		} else if (cmd.equals("simplegroupmngt")) {
			if (hasCourseRight(CourseRights.RIGHT_GROUPMANAGEMENT) || isCourseAdmin) {
				boolean mayModifyMembers = true;
				currentToolCtr = new SecurityGroupsRepositoryMainController(ureq, getWindowControl(), course, courseRepositoryEntry, mayModifyMembers);
				listenTo(currentToolCtr);
				all.setContent(currentToolCtr.getInitialComponent());
			} else throw new OLATSecurityException("clicked groupmanagement, but no according right");
			
		} else if (cmd.equals("rightmngt")) {
			if (isCourseAdmin) {
				currentToolCtr = new CourseGroupManagementMainController(ureq, getWindowControl(), course, BusinessGroup.TYPE_RIGHTGROUP);
				listenTo(currentToolCtr);
				all.setContent(currentToolCtr.getInitialComponent());
			} else throw new OLATSecurityException("clicked rightmanagement, but no according right");

		} else if (cmd.equals("statistic")) {
			if (hasCourseRight(CourseRights.RIGHT_STATISTICS) || isCourseAdmin) {
				currentToolCtr = new StatisticMainController(ureq, getWindowControl(), course);
				listenTo(currentToolCtr);
				all.setContent(currentToolCtr.getInitialComponent());
			} else throw new OLATSecurityException("clicked statistic, but no according right");
		} else if (cmd.equals("archiver")) {
			if (hasCourseRight(CourseRights.RIGHT_ARCHIVING) || isCourseAdmin) {
				currentToolCtr = new ArchiverMainController(ureq, getWindowControl(), course, new IArchiverCallback() {
					public boolean mayArchiveQtiResults() {
						return true;
					}

					public boolean mayArchiveLogfiles() {
						return true;
					}

					public boolean mayArchiveCoursestructure() {
						return true;
					}

					public boolean mayArchiveProperties() {
						return true;
					}

					public boolean mayArchiveHandedInTasks() {
						return true;
					}

					public boolean mayArchiveForums() {
						return true;
					}

					public boolean mayArchiveDialogs() {
						return true;
					}

					public boolean mayArchiveWikis() {
						return true;
					}

					public boolean mayArchiveProjectBroker() {
						return true;
					}

				});
				listenTo(currentToolCtr);
				all.setContent(currentToolCtr.getInitialComponent());
			} else throw new OLATSecurityException("clicked archiver, but no according right");

		} else if (cmd.equals("assessment")) {
			launchAssessmentTool(ureq, null);

		} else if (cmd.equals("efficiencystatement")) {
			// will not be disposed on course run dispose, popus up as new
			// browserwindow
			ControllerCreator ctrlCreator = new ControllerCreator() {
				public Controller createController(UserRequest lureq, WindowControl lwControl) {
					EfficiencyStatementController efficiencyStatementController = new EfficiencyStatementController(lwControl, lureq, courseRepositoryEntry.getKey());
					efficiencyStatementController.setCustomCSS(CourseFactory.getCustomCourseCss(lureq.getUserSession(), uce.getCourseEnvironment()));
					return efficiencyStatementController;
				}					
			};
			//wrap the content controller into a full header layout
			ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
			//open in new browser window
			openInNewBrowserWindow(ureq, layoutCtrlr);
			//
		} else if (cmd.equals("personalnote")) {
			// will not be disposed on course run dispose, popus up as new
			// browserwindow
			ControllerCreator ctrlCreator = new ControllerCreator() {
				public Controller createController(UserRequest lureq, WindowControl lwControl) {
					Controller notesCtr = new NoteController(lureq, course, getExtendedCourseTitle(lureq.getLocale()), lwControl);
					LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(lureq, lwControl, null, null, notesCtr.getInitialComponent(), null);
					layoutCtr.setCustomCSS(CourseFactory.getCustomCourseCss(lureq.getUserSession(), uce.getCourseEnvironment()));
					layoutCtr.addDisposableChildController(notesCtr); // dispose glossary on layout dispose
					return layoutCtr;
				}					
			};
			//wrap the content controller into a full header layout
			ControllerCreator popupLayoutCtr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
			//open in new browser window
			openInNewBrowserWindow(ureq, popupLayoutCtr);
			//
		} else if (cmd.equals(TOOLBOX_LINK_COURSECONFIG)) {
			//FIXME:pb:a better workflows to link a course detail page from course run
			//was brasato getWindowControl().getDTabs().activateStatic(ureq, RepositorySite.class.getName(),RepositoryMainController.JUMPFROMEXTERN + RepositoryMainController.JUMPFROMCOURSE + courseRepositoryEntry.getKey().toString());
			DTabs dts = (DTabs)Windows.getWindows(ureq).getWindow(ureq).getAttribute("DTabs");
			if(dts != null){
				dts.activateStatic(ureq, RepositorySite.class.getName(),RepositoryMainController.JUMPFROMEXTERN + RepositoryMainController.JUMPFROMCOURSE + courseRepositoryEntry.getKey().toString());
			}else{
				//help course in popup window can not display detail page
				getWindowControl().setInfo("detail can not be displayed here");
			}
			return;
		} else if (cmd.equals(ACTION_BOOKMARK)) { // add bookmark
			RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(course, true);
			bookmarkController = new AddAndEditBookmarkController(ureq, getWindowControl(), re.getDisplayname(), "", re, re.getOlatResource()
					.getResourceableTypeName());
			listenTo(bookmarkController);
			contentP.pushContent(bookmarkController.getInitialComponent());
		} else if (cmd.equals(ACTION_CALENDAR)) { // popup calendar
			ControllerCreator ctrlCreator = new ControllerCreator() {
				public Controller createController(UserRequest lureq, WindowControl lwControl) {
					ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(courseRepositoryEntry);
					WindowControl llwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, lwControl);
					CourseCalendarController calendarController = new CourseCalendarController(lureq, llwControl, course);					
					// use a one-column main layout
					LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(lureq, llwControl, null, null, calendarController.getInitialComponent(), null);
					layoutCtr.setCustomCSS(CourseFactory.getCustomCourseCss(lureq.getUserSession(), uce.getCourseEnvironment()));
					layoutCtr.addDisposableChildController(calendarController); // dispose calendar on layout dispose
					return layoutCtr;					
				}					
			};
			//wrap the content controller into a full header layout
			ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
			//open in new browser window
			PopupBrowserWindow pbw = getWindowControl().getWindowBackOffice().getWindowManager().createNewPopupBrowserWindowFor(ureq, layoutCtrlr);
			pbw.open(ureq);
			//
		} 
	}

	private Activateable launchAssessmentTool(UserRequest ureq, String viewIdentifier) {
		// 1) course admins and users with tool right: full access
		if (hasCourseRight(CourseRights.RIGHT_ASSESSMENT) || isCourseAdmin) {

			Activateable assessmentToolCtr = 
				AssessmentUIFactory.createAssessmentMainController(ureq, getWindowControl(), course, new FullAccessAssessmentCallback());
			assessmentToolCtr.activate(ureq, viewIdentifier);
			currentToolCtr = assessmentToolCtr;
			listenTo(currentToolCtr);
			all.setContent(currentToolCtr.getInitialComponent());
			return assessmentToolCtr;
		}
		// 2) users with coach right: limited access to coached groups
		else if (isCourseCoach) {
			Activateable assessmentToolCtr = AssessmentUIFactory.createAssessmentMainController(ureq, getWindowControl(), course,
					new CoachingGroupAccessAssessmentCallback());
			assessmentToolCtr.activate(ureq, viewIdentifier);
			currentToolCtr = assessmentToolCtr;
			listenTo(currentToolCtr);
			all.setContent(currentToolCtr.getInitialComponent());
			return assessmentToolCtr;
		} else throw new OLATSecurityException("clicked assessment tool in course::" + course.getResourceableId()
				+ ", but no right to launch it. Username::" + ureq.getIdentity().getName());
	}

	/**
	 * implementation of listener which listens to publish events
	 * 
	 * @see org.olat.core.util.event.GenericEventListener#event(org.olat.core.gui.control.Event)
	 */
	public void event(Event event) {	
		
		if (event instanceof MultiUserEvent) {
			
			if (event.getCommand().equals(JOINED) || event.getCommand().equals(LEFT)) {
				updateCurrentUserCount();
				return;
			}
		}
		
		if (event instanceof PublishEvent) {
			PublishEvent pe = (PublishEvent) event;
			if (pe.getState() == PublishEvent.PRE_PUBLISH) {
				// so far not interested in PRE PUBLISH event, but one could add user
				// and the currently active BB information. This in turn could be used 
				// by the publish event issuer to decide whether or not to publish...
				return;
			}
			if (!course.getResourceableId().equals(pe.getPublishedCourseResId())) throw new AssertException("not the same course");
			// long pts = pe.getLatestPublishTimestamp();
			// disable this controller and issue a information
			// FIXME:fj:b - implement the 'there are 5 users using this course at the
			// moment' warning (in the publishcontroller)
			if (isInEditor) {
				needsRebuildAfterPublish = true;				
				
				// author went in editor and published the course -> raise a flag to
				// later prepare the new
				// course to present him/her a nice view when
				// he/she closes the editor to return to the run main (this controller)
			} else {
				dispose();
			}
		} else if (event instanceof OLATResourceableJustBeforeDeletedEvent) {
			OLATResourceableJustBeforeDeletedEvent ojde = (OLATResourceableJustBeforeDeletedEvent) event;
			// make sure it is our course (actually not needed till now, since we
			// registered only to one event, but good style.
			if (ojde.targetEquals(course, true)) {
				dispose();
			}
		} else if (event instanceof AssessmentChangedEvent) {
			AssessmentChangedEvent ace = (AssessmentChangedEvent) event;
			Identity identity = uce.getIdentityEnvironment().getIdentity();
			// reevaluate the changed node if the event changed the current user
			if (ace.getIdentityKey().equals(identity.getKey())) {
				String assessmentChangeType = ace.getCommand();
				// do not re-evaluate things if only comment has been changed
				if (assessmentChangeType.equals(AssessmentChangedEvent.TYPE_SCORE_EVAL_CHANGED)
						|| assessmentChangeType.equals(AssessmentChangedEvent.TYPE_ATTEMPTS_CHANGED)) {
					//LD: do not recalculate the score now, but at the next click, since the event comes before DB commit
          //uce.getScoreAccounting().evaluateAll(); 
					assessmentChangedEventReceived = true;										
				} else if (assessmentChangeType.equals(AssessmentChangedEvent.TYPE_EFFICIENCY_STATEMENT_CHANGED)) {
					// update tools, maybe efficiency statement link has changed
					removeAsListenerAndDispose(toolC);
					toolC = initToolController(identity, null);
					listenTo(toolC);
					
					Component toolComp = (toolC == null ? null : toolC.getInitialComponent());
					columnLayoutCtr.setCol2(toolComp);
				}
				// raise a flag to indicate refresh
				needsRebuildAfterRunDone = true;
			}
		} else if (event instanceof BusinessGroupModifiedEvent) {
			BusinessGroupModifiedEvent bgme = (BusinessGroupModifiedEvent) event;
			Identity identity = uce.getIdentityEnvironment().getIdentity();
			// only do something if this identity is affected by change and the action
			// was adding or removing of the user
			if (bgme.wasMyselfAdded(identity) || bgme.wasMyselfRemoved(identity)) {
				// 1) reinitialize all group memberships
				initGroupMemberships(identity);
				// 2) reinitialize the users roles and rights
				initUserRolesAndRights(identity);
				// 3) rebuild toolboxes with link to groups and tools
				removeAsListenerAndDispose(toolC);
				toolC = initToolController(identity, null);
				listenTo(toolC);
				Component toolComp = (toolC == null ? null : toolC.getInitialComponent());
				columnLayoutCtr.setCol2(toolComp);
				needsRebuildAfterRunDone = true;
			} else if (bgme.getCommand().equals(BusinessGroupModifiedEvent.GROUPRIGHTS_MODIFIED_EVENT)) {
				// check if this affects a right group where the user does participate.
				// if so, we need
				// to rebuild the toolboxes
				if (PersistenceHelper.listContainsObjectByKey(rightGroups, bgme.getModifiedGroupKey())) {
					// 1) reinitialize all group memberships
					initGroupMemberships(identity);
					// 2) reinitialize the users roles and rights
					initUserRolesAndRights(identity);
					// 3) rebuild toolboxes with link to groups and tools
					removeAsListenerAndDispose(toolC);
					toolC = initToolController(identity, null);
					listenTo(toolC);
					Component toolComp = (toolC == null ? null : toolC.getInitialComponent());
					columnLayoutCtr.setCol2(toolComp);
				}
			}

		} else if (event instanceof CourseConfigEvent) {				
			dispose();
			
		} else if (event instanceof EntryChangedEvent && ((EntryChangedEvent)event).getChange()!=EntryChangedEvent.MODIFIED_AT_PUBLISH) {
			//courseRepositoryEntry changed (e.g. fired at course access rule change)
			EntryChangedEvent repoEvent = (EntryChangedEvent) event;			
			if (courseRepositoryEntry.getKey().equals(repoEvent.getChangedEntryKey()) && repoEvent.getChange() == EntryChangedEvent.MODIFIED) {				
				dispose();				
			}
		}
	}
		

	/**
	 * Initializes the course tools according to the users rights (repository
	 * author group and course rights in course groupmanagement)
	 * 
	 * @param identity
	 * @param ureq
	 * @param ureq
	 * @return ToolController
	 */
	private ToolController initToolController(Identity identity, UserRequest ureq) {
		
		ToolController myTool = ToolFactory.createToolController(getWindowControl());
		CourseConfig cc = uce.getCourseEnvironment().getCourseConfig();

		// 1) administrative tools
		if (isCourseAdmin || isCourseCoach || hasCourseRight(CourseRights.RIGHT_COURSEEDITOR)
				|| hasCourseRight(CourseRights.RIGHT_GROUPMANAGEMENT) || hasCourseRight(CourseRights.RIGHT_ARCHIVING)
				|| hasCourseRight(CourseRights.RIGHT_ASSESSMENT)) {
			myTool.addHeader(translate("header.tools"));
			if (hasCourseRight(CourseRights.RIGHT_COURSEEDITOR) || isCourseAdmin) {
				myTool.addLink(COMMAND_EDIT, translate("command.openeditor"));
			}
			if (hasCourseRight(CourseRights.RIGHT_GROUPMANAGEMENT) || isCourseAdmin) {
				//fxdiff VCRP-1,2: access control of resources
				myTool.addLink("simplegroupmngt", translate("command.opensimplegroupmngt"));
				//
				myTool.addLink("groupmngt", translate("command.opengroupmngt"));
			}
			if (isCourseAdmin) {
				myTool.addLink("rightmngt", translate("command.openrightmngt"));
			}
			if (hasCourseRight(CourseRights.RIGHT_ARCHIVING) || isCourseAdmin) {
				myTool.addLink("archiver", translate("command.openarchiver"));
			}
			if (hasCourseRight(CourseRights.RIGHT_ASSESSMENT) || isCourseCoach || isCourseAdmin) {
				myTool.addLink("assessment", translate("command.openassessment"));
			}
			/*
			 * http://bugs.olat.org/jira/browse/OLAT-4928
			 */
			if (hasCourseRight(CourseRights.RIGHT_STATISTICS) || isCourseAdmin) {
				myTool.addLink("statistic", translate("command.openstatistic"));
			}

			//
			/*
			 * if (isCourseAdmin) { myTool.addLink(TOOLBOX_LINK_COURSECONFIG,
			 * translate("command.courseconfig")); }
			 */
			//
		}

		// 2) add coached groups
		if (ownedGroups.size() > 0) {
			myTool.addHeader(translate("header.tools.ownerGroups"));
			Iterator iter = ownedGroups.iterator();
			while (iter.hasNext()) {
				BusinessGroup group = (BusinessGroup) iter.next();
				myTool.addLink(CMD_START_GROUP_PREFIX + group.getKey().toString(), group.getName());
			}
		}

		// 3) add participating groups
		if (participatedGroups.size() > 0) {
			myTool.addHeader(translate("header.tools.participatedGroups"));
			Iterator iter = participatedGroups.iterator();
			while (iter.hasNext()) {
				BusinessGroup group = (BusinessGroup) iter.next();
				myTool.addLink(CMD_START_GROUP_PREFIX + group.getKey().toString(), group.getName());
			}
		}

		// 4) add right groups
		if (rightGroups.size() > 0) {
			myTool.addHeader(translate("header.tools.rightGroups"));
			Iterator iter = rightGroups.iterator();
			while (iter.hasNext()) {
				BusinessGroup group = (BusinessGroup) iter.next();
				myTool.addLink(CMD_START_GROUP_PREFIX + group.getKey().toString(), group.getName());
			}
		}

		// 5) add waiting-list groups
		if (waitingListGroups.size() > 0) {
			myTool.addHeader(translate("header.tools.waitingListGroups"));
			Iterator iter = waitingListGroups.iterator();
			while (iter.hasNext()) {
				BusinessGroup group = (BusinessGroup) iter.next();
				BusinessGroupManager businessGroupManager = BusinessGroupManagerImpl.getInstance();
				int pos = businessGroupManager.getPositionInWaitingListFor(identity, group);
				myTool.addLink(CMD_START_GROUP_PREFIX + group.getKey().toString(), group.getName() + "(" + pos + ")", group
						.getKey().toString(), null);
				myTool.setEnabled(group.getKey().toString(), false);
			}
		}

		// new toolbox 'general'
		myTool.addHeader(translate("header.tools.general"));
		if (cc.isCalendarEnabled() && !isGuest) {
			myTool.addPopUpLink(ACTION_CALENDAR, translate("command.calendar"), null, null, "950", "750", false);
		}
		if (cc.hasGlossary()) {
			myTool.addComponent(glossaryToolCtr.getInitialComponent());
		}
		if (showCourseConfigLink) {
		  myTool.addLink(TOOLBOX_LINK_COURSECONFIG, translate("command.courseconfig"));
		}
		if (!isGuest) {
			myTool.addPopUpLink("personalnote", translate("command.personalnote"), null, null, "750", "550", false);
		}
		if (offerBookmark && !isGuest) {
			myTool.addLink(ACTION_BOOKMARK, translate("command.bookmark"), TOOL_BOOKMARK, null);
			BookmarkManager bm = BookmarkManager.getInstance();
			if (bm.isResourceableBookmarked(identity, courseRepositoryEntry)) myTool.setEnabled(TOOL_BOOKMARK, false);

		}
		if (cc.isEfficencyStatementEnabled() && course.hasAssessableNodes() && !isGuest) {
			// link to efficiency statements should
			// - not appear when not configured in course configuration
			// - not appear when configured in course configuration but no assessable
			// node exist
			// - appear but dimmed when configured, assessable node exist but no
			// assessment data exists for user
			// - appear as link when configured, assessable node exist and assessment
			// data exists for user
			myTool.addPopUpLink("efficiencystatement", translate("command.efficiencystatement"), "command.efficiencystatement", null,
					"750", "800", false);
			EfficiencyStatementManager esm = EfficiencyStatementManager.getInstance();
			EfficiencyStatement es = esm.getUserEfficiencyStatement(courseRepositoryEntry.getKey(), identity);
			if (es == null) {
				myTool.setEnabled("command.efficiencystatement", false);
			}
		}
		
		//add group chat to toolbox
		boolean instantMsgYes = InstantMessagingModule.isEnabled();
		boolean chatIsEnabled = CourseModule.isCourseChatEnabled() &&  cc.isChatEnabled();
		if (instantMsgYes && chatIsEnabled && !isGuest) {
			// we add the course chat link controller to the toolbox
			if (courseChatManagerCtr == null && ureq != null) createCourseGroupChatLink(ureq);
			if (courseChatManagerCtr != null){
				Controller groupChatController = courseChatManagerCtr.getGroupChatController(course);
				if(groupChatController !=null){
					myTool.addComponent(groupChatController.getInitialComponent());
				}
			}
		}
		
		if (CourseModule.displayParticipantsCount() && !isGuest) {
			addCurrentUserCount(myTool);
		}
	
		return myTool;
	}

	private void updateCurrentUserCount() {
			if (currentUserCountLink == null) {
				// either called from event handler, not initialized yet
				// or display of user count is not configured
				return;
			}
			
			currentUserCount = CoordinatorManager.getInstance().getCoordinator().getEventBus().getListeningIdentityCntFor(courseRunOres);
			if(currentUserCount == 0) {
				currentUserCount = 1;
			}
			
			currentUserCountLink.setCustomDisplayText(
					translate(
							"participants.in.course", 
							new String[]{String.valueOf(currentUserCount)}
					)
			);
	}
	
	private void addCurrentUserCount(ToolController myTool) {
			VelocityContainer currentUsers = createVelocityContainer("currentUsers");
			currentUserCountLink = LinkFactory.createCustomLink("currentUsers", "cUsers", "", Link.NONTRANSLATED, currentUsers, this);
			updateCurrentUserCount();
			currentUserCountLink.setCustomEnabledLinkCSS("b_toolbox_link");
			currentUserCountLink.setTooltip(getTranslator().translate("participants.in.course.desc"), false);
			currentUserCountLink.setEnabled(false);
			myTool.addComponent(currentUserCountLink);
	}

	/**
	 * Reads the users learning group rights from a local hash map. use
	 * initCourseRightCache() to initialize the cache.
	 * 
	 * @param right The name of a learning group right
	 * @return true if the user has this right, false otherwise.
	 */
	private boolean hasCourseRight(String right) {
		Boolean bool = (Boolean) courseRightsCache.get(right);
		return bool.booleanValue();
	}

	/**
	 * Initialize the users group memberships for groups used within this course
	 * 
	 * @param identity
	 */
	private void initGroupMemberships(Identity identity) {
		CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
		ownedGroups = cgm.getOwnedLearningGroupsFromAllContexts(identity);
		participatedGroups = cgm.getParticipatingLearningGroupsFromAllContexts(identity);
		waitingListGroups = cgm.getWaitingListGroupsFromAllContexts(identity);
		rightGroups = cgm.getParticipatingRightGroupsFromAllContexts(identity);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		// remove as listener from this course events:
		// - group modification events
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, courseRepositoryEntry);
		// - publish changes
		// - assessment events
		// TODO:pb: also here - CourseConfig events are removed
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, course);
		//remove as listener from course run eventAgency
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, courseRunOres);

		if (CourseModule.displayParticipantsCount()) {
			CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new MultiUserEvent(LEFT), courseRunOres);
		}
				
		// currentNodeController must be disposed manually does not work with
		// general BasicController methods
		if (currentNodeController != null) {
			currentNodeController.dispose();
			currentNodeController = null;
		}

		// instant messaging: send presence like "leaving course: Biology I" as
		// awareness info.
		if (InstantMessagingModule.isEnabled()&& CourseModule.isCourseChatEnabled() && !isGuest) {
			if (courseChatManagerCtr != null) {
				courseChatManagerCtr.removeChat(course);
			}
			String intro = translate("course.presence.message.leave") + " " + getExtendedCourseTitle(getLocale());
			InstantMessagingModule.getAdapter().sendStatus(getIdentity().getName(), intro);
		}
		// log to Statistical and User log
		ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_LEAVING, getClass());
		
		// log the fact that the user is leaving the course in the log file
		logAudit("Leaving course: [[["+courseTitle+"]]]", course.getResourceableId().toString());
	}

	/**
	 * @see org.olat.core.gui.control.generic.dtabs.Activateable#activate(org.olat.core.gui.UserRequest,
	 *      java.lang.String)
	 */
	public void activate(UserRequest ureq, String viewIdentifier) {
		if(isDisposed()) {
			return;
		}
		// transform ACTIVATE_RUN to activate course root node
		if (viewIdentifier.equals(RepositoryDetailsController.ACTIVATE_RUN)) {
			// activating the run, means so far activating the root node
			// viewIdentifier = course.getRunStructure().getRootNode().getIdent();
			return;
		}
		//

		if (viewIdentifier.equals(RepositoryDetailsController.ACTIVATE_EDITOR)) {
			// activate editor
			if (!isInEditor) {
				if (currentToolCtr == null) {
					// not already in the editor and also no other course tool running
					isInEditor = true;
					doHandleToolEvents(ureq, COMMAND_EDIT);
				} else if (currentToolCtr != null) {
					// not activateable as another tool is running, i.e. assessment tool
					getWindowControl().setWarning(translate("warn.cannotactivatesinceintool"));
				}
			}// else it is already active
		} else if (isInEditor || currentToolCtr != null) {
			// do not activate anything if a course tool or the editor is active!
			getWindowControl().setWarning(translate("warn.cannotactivatesinceintool"));
		} else {
			if (currentNodeController != null) {
				currentNodeController.dispose();
			}
			//fxdiff BAKS-7 Resume function
			if (viewIdentifier.startsWith("CourseNode:")) {
				viewIdentifier = viewIdentifier.substring("CourseNode:".length());
			} 

			CourseNode cn = null;
			cn = (viewIdentifier == null ? null : course.getRunStructure().getNode(viewIdentifier));
			String subsubId = null;

			// check for subsubIdent (jump into the third level)
			if (viewIdentifier != null) {
				if (viewIdentifier.indexOf(":") != -1) {
					String[] vis = viewIdentifier.split(":");
					cn = course.getRunStructure().getNode(vis[0]);
					subsubId = vis[1];
				}
			}
			// FIXME:fj:b is this needed in some cases?: currentCourseNode = cn;
			getWindowControl().makeFlat();
			
			//add loggin information for case course gets started via jumpin link/search
			addLoggingResourceable(LoggingResourceable.wrap(course));
			if (cn!=null) {
				addLoggingResourceable(LoggingResourceable.wrap(cn));
			}
			updateTreeAndContent(ureq, cn, subsubId);
		}
		
		// reset gloss toolC to get newly tabs
		removeAsListenerAndDispose(glossaryToolCtr);
		boolean hasGlossaryRights = hasCourseRight(CourseRights.RIGHT_GLOSSARY) || isCourseAdmin;
		glossaryToolCtr = new CourseGlossaryToolLinkController(getWindowControl(), ureq, course, getTranslator(), hasGlossaryRights, 
				uce.getCourseEnvironment(), glossaryMarkerCtr);
		listenTo(glossaryToolCtr);
	}

	@Override
	//fxdiff BAKS-7 Resume function
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry firstEntry = entries.get(0);
		String type = firstEntry.getOLATResourceable().getResourceableTypeName();
		if("CourseNode".equals(type)) {
			CourseNode cn = course.getRunStructure().getNode(firstEntry.getOLATResourceable().getResourceableId().toString());
			
			// FIXME:fj:b is this needed in some cases?: currentCourseNode = cn;
			getWindowControl().makeFlat();

			// add loggin information for case course gets started via jumpin
			// link/search
			addLoggingResourceable(LoggingResourceable.wrap(course));
			if (cn != null) {
				addLoggingResourceable(LoggingResourceable.wrap(cn));
			}
			
			if(entries.size() > 1) {
				entries = entries.subList(1, entries.size());
			}
			updateTreeAndContent(ureq, cn, null, entries, firstEntry.getTransientState());
		} else if ("assessmentTool".equals(type)) {
			//check the security before, the link is perhaps in the wrong hands
			if(hasCourseRight(CourseRights.RIGHT_ASSESSMENT) || isCourseAdmin || isCourseCoach) {
				try {
					Activateable assessmentCtrl = launchAssessmentTool(ureq, null);
					if(assessmentCtrl instanceof Activateable2) {
						List<ContextEntry> subEntries = entries.subList(1, entries.size());
						((Activateable2)assessmentCtrl).activate(ureq, subEntries, firstEntry.getTransientState());
					}
				} catch (OLATSecurityException e) {
					//the wrong link to the wrong person
				}
			}
		}
	}

	// fxdiff: allow disabling after instantiation
	public void disableToolController(boolean disable) {
		columnLayoutCtr.hideCol2(disable);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#setDisposedMsgController(org.olat.core.gui.control.Controller)
	 */
	@Override
	// fxdiff: exchange dispose controller
	public void setDisposedMsgController(Controller disposeMsgController) {
		super.setDisposedMsgController(disposeMsgController);
	}
}