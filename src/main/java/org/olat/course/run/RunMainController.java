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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.NewControllerFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.controllers.linkchooser.CustomLinkTreeModel;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.commons.modules.bc.FolderRunController;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.Dropdown.Spacer;
import org.olat.core.gui.components.htmlheader.jscss.CustomCSS;
import org.olat.core.gui.components.htmlsite.OlatCmdEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.link.LinkPopupSettings;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.ConfigurationChangedListener;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.dtabs.DTab;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.gui.control.generic.messages.MessageController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.gui.control.generic.textmarker.GlossaryMarkupItemController;
import org.olat.core.gui.control.generic.title.TitledWrapperController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.HistoryPoint;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.OLATSecurityException;
import org.olat.core.logging.activity.CourseLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.tree.TreeHelper;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.tree.Visitor;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.archiver.ArchiverMainController;
import org.olat.course.archiver.FullAccessArchiverCallback;
import org.olat.course.area.CourseAreasController;
import org.olat.course.assessment.AssessmentChangedEvent;
import org.olat.course.assessment.AssessmentMainController;
import org.olat.course.assessment.CoachingGroupAccessAssessmentCallback;
import org.olat.course.assessment.EfficiencyStatementController;
import org.olat.course.assessment.EfficiencyStatementManager;
import org.olat.course.assessment.FullAccessAssessmentCallback;
import org.olat.course.assessment.UserEfficiencyStatement;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.course.config.CourseConfig;
import org.olat.course.config.CourseConfigEvent;
import org.olat.course.config.ui.CourseOptionsController;
import org.olat.course.config.ui.courselayout.CourseLayoutGeneratorController;
import org.olat.course.db.CourseDBManager;
import org.olat.course.db.CustomDBMainController;
import org.olat.course.editor.EditorMainController;
import org.olat.course.editor.PublishEvent;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.groupsandrights.CourseRights;
import org.olat.course.member.MembersManagementMainController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.calendar.CourseCalendarController;
import org.olat.course.run.glossary.CourseGlossaryFactory;
import org.olat.course.run.glossary.CourseGlossaryToolLinkController;
import org.olat.course.run.navigation.NavigationHandler;
import org.olat.course.run.navigation.NodeClickedRef;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.course.statistic.StatisticCourseNodesController;
import org.olat.course.statistic.StatisticMainController;
import org.olat.course.tree.CourseInternalLinkTreeModel;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.ui.edit.BusinessGroupModifiedEvent;
import org.olat.ims.qti.statistics.QTIType;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.OpenInstantMessageEvent;
import org.olat.modules.cp.TreeNodeEvent;
import org.olat.note.NoteController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryEntryStatus;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.ui.author.AuthoringEditAccessController;
import org.olat.repository.ui.author.AuthoringEditEntrySettingsController;
import org.olat.repository.ui.author.CatalogSettingsController;
import org.olat.repository.ui.author.RepositoryEditDescriptionController;
import org.olat.repository.ui.list.RepositoryEntryDetailsController;
import org.olat.resource.OLATResource;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class RunMainController extends MainLayoutBasicController implements GenericEventListener, Activateable2 {

	private static final String JOINED = "joined";
	private static final String LEFT   = "left";

	private static final String CMD_START_GROUP_PREFIX = "cmd.group.start.ident.";
	
	public static final String REBUILD = "rebuild";
	public static final String ORES_TYPE_COURSE_RUN = OresHelper.calculateTypeName(RunMainController.class, CourseModule.ORES_TYPE_COURSE);
	private final OLATResourceable courseRunOres; //course run ores for course run channel 

	private ICourse course;//o_clusterOK: this controller listen to course change events
	private RepositoryEntry courseRepositoryEntry;
	
	
	private MenuTree luTree;
	//tools
	private Link previousLink, nextLink,
			//tools
			editLink, folderLink,
			userMgmtLink, assessmentLink, archiverLink,
			courseStatisticLink, surveyStatisticLink, testStatisticLink,
			areaLink, dbLink, bookingLink,
			//settings
			editDescriptionLink, accessLink, catalogLink,
			layoutLink, optionsLink,
			//my course
			efficiencyStatementsLink, bookmarkLink, calendarLink, detailsLink, noteLink, chatLink;
	private Dropdown settings, tools, myCourse;
	
	private Panel contentP;

	private NavigationHandler navHandler;
	private UserCourseEnvironmentImpl uce;
	private LayoutMain3ColsController columnLayoutCtr;

	private Controller currentToolCtr;
	private CourseOptionsController optionsToolCtr;
	private AuthoringEditAccessController accessToolCtr;
	
	
	private Controller currentNodeController; // the currently open node config
	private TooledStackedPanel toolbarPanel;
	private String launchdFromBusinessPath;

	private boolean isInEditor = false;

	private Map<String, Boolean> courseRightsCache = new HashMap<String, Boolean>();
	private boolean isCourseAdmin = false;
	private boolean isCourseCoach = false;
	private final Roles roles;

	private CourseNode currentCourseNode;
	private TreeModel treeModel;
	private boolean needsRebuildAfter = false;
	private boolean needsRebuildAfterPublish = false;
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
	
	@Autowired
	private MarkManager markManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private EfficiencyStatementManager efficiencyStatementManager;
	
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
	public RunMainController(final UserRequest ureq, final WindowControl wControl, final ICourse course,
			final RepositoryEntry re, final boolean offerBookmark, final boolean showCourseConfigLink) {

		// Use repository package as fallback translator
		super(ureq, wControl, Util.createPackageTranslator(RepositoryEntry.class, ureq.getLocale()));
		
		roles = ureq.getUserSession().getRoles();

		this.course = course;
		addLoggingResourceable(LoggingResourceable.wrap(course));
		this.courseTitle = course.getCourseTitle();
		this.courseRepositoryEntry = re;
		this.offerBookmark = offerBookmark;
		this.showCourseConfigLink = showCourseConfigLink;
		this.courseRunOres = OresHelper.createOLATResourceableInstance(ORES_TYPE_COURSE_RUN, course.getResourceableId());
		
		Identity identity = ureq.getIdentity();
		// course and system logging
		ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_ENTERING, getClass());
		
		// log shows who entered which course, this can then be further used to jump
		// to the courselog
		logAudit("Entering course: [[["+courseTitle+"]]]", course.getResourceableId().toString());
		
		// set up the components
		toolbarPanel = new TooledStackedPanel("courseStackPanel", getTranslator(), this);
		toolbarPanel.setInvisibleCrumb(0); // show root (course) level
		toolbarPanel.setShowCloseLink(true, true);
		// set last location as path from where the controller was launched
		UserSession session = ureq.getUserSession();
		if(session != null &&  session.getHistoryStack() != null && session.getHistoryStack().size() >= 2) {
			// Set previous business path as back link for this course - brings user back to place from which he launched the course
			List<HistoryPoint> stack = session.getHistoryStack();
			HistoryPoint point = stack.get(stack.size() - 2);
			launchdFromBusinessPath = point.getBusinessPath();
		}
				
		//StackedControllerImpl(getWindowControl(), getTranslator(), "o_course_breadcumbs");
		luTree = new MenuTree(null, "luTreeRun", this);
		luTree.setExpandSelectedNode(false);
		luTree.setElementCssClass("o_course_menu");
		contentP = new Panel("building_block_content");

		// Initialize the the users roles and right for this course
		// 1) is guest flag
		isGuest = ureq.getUserSession().getRoles().isGuestOnly();
		// preload user assessment data in assessmnt properties cache to speed up
		// course loading
		course.getCourseEnvironment().getAssessmentManager().preloadCache(identity);

		// build up the running structure for this user;
		// get all group memberships for this course
		uce = loadUserCourseEnvironment(ureq);
		// 2) all course internal rights
		reloadUserRolesAndRights(identity);

		// build score now
		uce.getScoreAccounting().evaluateAll();
		navHandler = new NavigationHandler(uce, false);

		updateTreeAndContent(ureq, currentCourseNode, null);
		
		//set the launch date after the evaluation
		setLaunchDates();

		if (courseRepositoryEntry != null && RepositoryManager.getInstance().createRepositoryEntryStatus(courseRepositoryEntry.getStatusCode()).isClosed()) {
			wControl.setWarning(translate("course.closed"));
		}

		// add text marker wrapper controller to implement course glossary
		// textMarkerCtr must be created before the toolC!
		CourseConfig cc = uce.getCourseEnvironment().getCourseConfig();
		glossaryMarkerCtr = CourseGlossaryFactory.createGlossaryMarkupWrapper(ureq, wControl, contentP, cc);
	
		boolean hasGlossaryRights = hasCourseRight(CourseRights.RIGHT_GLOSSARY) || isCourseAdmin;
		glossaryToolCtr = new CourseGlossaryToolLinkController(getWindowControl(), ureq, course, getTranslator(), hasGlossaryRights, 
				uce.getCourseEnvironment(), glossaryMarkerCtr);
		listenTo(glossaryToolCtr);
		
				
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
			columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), luTree, glossaryMarkerCtr.getInitialComponent(), "course" + course.getResourceableId());				
		} else {
			columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), luTree, contentP, "courseRun" + course.getResourceableId());							
		}
		listenTo(columnLayoutCtr);

		// activate the custom course css if any
		setCustomCSS(CourseFactory.getCustomCourseCss(ureq.getUserSession(), uce.getCourseEnvironment()));
		toolbarPanel.pushController(courseTitle, columnLayoutCtr);
		
		// init the menu and tool controller
		initToolbar();

		coursemain = createVelocityContainer("index");
		coursemain.setDomReplaceable(false);
		// see function gotonode in functions.js to see why we need the repositoryentry-key here:
		// it is to correctly apply external links using course-internal links via javascript
		coursemain.contextPut("courserepokey", courseRepositoryEntry.getKey());
		coursemain.put("coursemain", toolbarPanel);
		putInitialPanel(coursemain);

		// disposed message controller must be created beforehand
		Controller courseCloser = CourseFactory.createDisposedCourseRestartController(ureq, wControl, courseRepositoryEntry);
		Controller disposedRestartController = new LayoutMain3ColsController(ureq, wControl, courseCloser);
		setDisposedMsgController(disposedRestartController);

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
	
	private UserCourseEnvironmentImpl loadUserCourseEnvironment(UserRequest ureq) {
		CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
		List<BusinessGroup> coachedGroups = cgm.getOwnedBusinessGroups(ureq.getIdentity());
		List<BusinessGroup> participatedGroups = cgm.getParticipatingBusinessGroups(ureq.getIdentity());
		List<BusinessGroup> waitingLists = cgm.getWaitingListGroups(ureq.getIdentity());
		return new UserCourseEnvironmentImpl(ureq.getUserSession().getIdentityEnvironment(), course.getCourseEnvironment(),
				coachedGroups, participatedGroups, waitingLists, null, null, null);
		//TODO fire event???
	}
	
	private void setLaunchDates() {
		UserCourseInformationsManager userCourseInfoMgr = CoreSpringFactory.getImpl(UserCourseInformationsManager.class);
		userCourseInfoMgr.updateUserCourseInformations(uce.getCourseEnvironment().getCourseResourceableId(), getIdentity(), false);
	}
	
	/**
	 * @param locale
	 * @return
	 */
	private String getExtendedCourseTitle(Locale locale) {
		String extendedCourseTitle = this.courseTitle;
		RepositoryEntry repositoryEntry = RepositoryManager.getInstance().lookupRepositoryEntry(course, false);
		if (repositoryEntry != null && RepositoryManager.getInstance().createRepositoryEntryStatus(repositoryEntry.getStatusCode()).isClosed()) {
			Translator pT = Util.createPackageTranslator(RepositoryEntryStatus.class, locale);
			extendedCourseTitle = "[" + pT.translate("title.prefix.closed") + "] ".concat(extendedCourseTitle);
		}
		return extendedCourseTitle;
	}

	private void reloadUserRolesAndRights(final Identity identity) {
		CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
		// 1) course admins: users who are in repository entry owner group
		// if user has the role InstitutionalResourceManager and has the same institution like author
		// then set isCourseAdmin true
		isCourseAdmin = roles.isOLATAdmin()
				|| cgm.isIdentityCourseAdministrator(identity)
				|| RepositoryManager.getInstance().isInstitutionalRessourceManagerFor(identity, roles, courseRepositoryEntry);
		// 2) course coaches: users who are in the owner group of any group of this
		// course
		isCourseCoach = cgm.isIdentityCourseCoach(identity);
		// 3) all other rights are defined in the groupmanagement using the learning
		// group rights
		uce.setUserRoles(isCourseAdmin, isCourseCoach);
		
		List<String> rights = cgm.getRights(identity);
		courseRightsCache.put(CourseRights.RIGHT_GROUPMANAGEMENT, new Boolean(rights.contains(CourseRights.RIGHT_GROUPMANAGEMENT)));
		courseRightsCache.put(CourseRights.RIGHT_COURSEEDITOR, new Boolean(rights.contains(CourseRights.RIGHT_COURSEEDITOR)));
		courseRightsCache.put(CourseRights.RIGHT_ARCHIVING, new Boolean(rights.contains(CourseRights.RIGHT_ARCHIVING)));
		courseRightsCache.put(CourseRights.RIGHT_ASSESSMENT, new Boolean(rights.contains(CourseRights.RIGHT_ASSESSMENT)));
		courseRightsCache.put(CourseRights.RIGHT_GLOSSARY, new Boolean(rights.contains(CourseRights.RIGHT_GLOSSARY)));
		courseRightsCache.put(CourseRights.RIGHT_STATISTICS, new Boolean(rights.contains(CourseRights.RIGHT_STATISTICS)));
		courseRightsCache.put(CourseRights.RIGHT_DB, new Boolean(rights.contains(CourseRights.RIGHT_DB)));
	}
	
	private CourseNode updateAfterChanges(CourseNode courseNode) {
		if(currentCourseNode == null) return null;
		
		CourseNode newCurrentCourseNode;
		NodeClickedRef nclr = navHandler.reloadTreeAfterChanges(courseNode);
		if(nclr == null) {
			doDisposeAfterEvent();
			newCurrentCourseNode = null;
		} else {
			treeModel = nclr.getTreeModel();
			luTree.setTreeModel(treeModel);
			String selNodeId = nclr.getSelectedNodeId();
			luTree.setSelectedNodeId(selNodeId);
			luTree.setOpenNodeIds(nclr.getOpenNodeIds());
			newCurrentCourseNode = nclr.getCalledCourseNode();
		}
		return newCurrentCourseNode;
	}
	
	private void updateNextPrevious() {
		if(nextLink == null || previousLink == null || luTree == null) return;
		
		boolean hasPrevious;
		boolean hasNext;
		if(luTree.getSelectedNode() == null) {
			hasPrevious = true;
			hasNext = true;
		} else {
			List<TreeNode> flatTree = new ArrayList<>();
			TreeHelper.makeTreeFlat(luTree.getTreeModel().getRootNode(), flatTree);
			int index = flatTree.indexOf(luTree.getSelectedNode());
			hasPrevious = index > 0;
			hasNext = index  >= 0 && index+1 < flatTree.size();
		}
		previousLink.setEnabled(hasPrevious);
		nextLink.setEnabled(hasNext);
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
	
	private boolean updateTreeAndContent(UserRequest ureq, CourseNode calledCourseNode, String nodecmd, List<ContextEntry> entries, StateEntry state) {
		// build menu (treemodel)
		// dispose old node controller before creating the NodeClickedRef which creates 
		// the new node controller. It is important that the old node controller is 
		// disposed before the new one to not get conflicts with cacheable mappers that
		// might be used in both controllers with the same ID (e.g. the course folder)
		if (currentNodeController != null && !currentNodeController.isDisposed() && !navHandler.isListening(currentNodeController)) {
			currentNodeController.dispose();
		}
		NodeClickedRef nclr = navHandler.evaluateJumpToCourseNode(ureq, getWindowControl(), calledCourseNode, this, nodecmd);
		if (!nclr.isVisible()) {
			// if not root -> fallback to root. e.g. when a direct node jump fails
			if (calledCourseNode != null) {
				nclr = navHandler.evaluateJumpToCourseNode(ureq, getWindowControl(), null, null, null);
			}
			if (!nclr.isVisible()) {
				MessageController msgController = MessageUIFactory.createInfoMessage(ureq, getWindowControl(),	translate("course.noaccess.title"), translate("course.noaccess.text"));
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

		// get new run controller.
		currentNodeController = nclr.getRunController();
		addToHistory(ureq, currentNodeController);
		
		if (currentNodeController instanceof TitledWrapperController) {
			Controller contentcontroller = ((TitledWrapperController)currentNodeController).getContentController();
			addToHistory(ureq, contentcontroller);
			if(contentcontroller instanceof Activateable2) {
				((Activateable2)contentcontroller).activate(ureq, entries, state);
			}
		} else if(currentNodeController instanceof Activateable2) {
			((Activateable2)currentNodeController).activate(ureq, entries, state);
		}
		contentP.setContent(currentNodeController.getInitialComponent());
		
		updateNextPrevious();
		
		return true;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(needsRebuildAfter) {
			currentCourseNode = updateAfterChanges(currentCourseNode);
			needsRebuildAfter = false;
		}
		
		// Links in editTools dropdown
		if(editLink == source) {
			launchEdit(ureq);
			removeCustomCSS(ureq);
			tools.setActiveLink(editLink);
		} else if(userMgmtLink == source) {
			launchMembersManagement(ureq);
			removeCustomCSS(ureq);
			tools.setActiveLink(userMgmtLink);
		} else if(archiverLink == source) {
			launchArchive(ureq);
			removeCustomCSS(ureq);
			tools.setActiveLink(archiverLink);
		} else if(assessmentLink == source) {
			launchAssessmentTool(ureq);
			removeCustomCSS(ureq);
			tools.setActiveLink(assessmentLink);
		} else if(testStatisticLink == source) {
			launchAssessmentStatistics(ureq, "command.openteststatistic", "TestStatistics", QTIType.test, QTIType.onyx);
			removeCustomCSS(ureq);
			tools.setActiveLink(testStatisticLink);
		} else if (surveyStatisticLink == source) {
			launchAssessmentStatistics(ureq, "command.opensurveystatistic", "SurveyStatistics", QTIType.survey);
			removeCustomCSS(ureq);
			tools.setActiveLink(surveyStatisticLink);
		} else if(courseStatisticLink == source) {
			launchStatistics(ureq);
			removeCustomCSS(ureq);
			tools.setActiveLink(courseStatisticLink);
		} else if(dbLink == source) {
			launchDbs(ureq);
			removeCustomCSS(ureq);
			tools.setActiveLink(dbLink);
		} else if(folderLink == source) {
			launchCourseFolder(ureq);
			removeCustomCSS(ureq);
			tools.setActiveLink(folderLink);
		} else if(areaLink == source) {
			launchCourseAreas(ureq);
			removeCustomCSS(ureq);
			tools.setActiveLink(areaLink);
		//links as settings
		} else if(editDescriptionLink == source) {
			launchEditSettings(ureq);
			removeCustomCSS(ureq);
			settings.setActiveLink(editDescriptionLink);
		} else if(accessLink == source) {
			launchAccess(ureq);
			removeCustomCSS(ureq);
			settings.setActiveLink(accessLink);
		} else if(catalogLink == source) {
			launchCatalog(ureq);
			removeCustomCSS(ureq);
			settings.setActiveLink(catalogLink);
		} else if(layoutLink == source) {
			launchLayout(ureq);
			removeCustomCSS(ureq);
			settings.setActiveLink(layoutLink);
		} else if(optionsLink == source) {
			launchOptions(ureq);
			removeCustomCSS(ureq);
			settings.setActiveLink(optionsLink);
		// links as dedicated tools
		} else if(efficiencyStatementsLink == source) {
			launchEfficiencyStatements(ureq);
		} else if(bookmarkLink == source) {
			toogleBookmark();
		} else if(calendarLink == source) {
			launchCalendar(ureq);
		} else if(detailsLink == source) {
			launchDetails(ureq);
			removeCustomCSS(ureq);
		} else if(noteLink == source) {
			launchPersonalNotes(ureq);
		} else if(chatLink == source) {
			launchChat(ureq);
		} else if(source instanceof Link && ((Link)source).getCommand().startsWith(CMD_START_GROUP_PREFIX)) {
			String groupIdent = ((Link)source).getCommand().substring(CMD_START_GROUP_PREFIX.length());
			Long groupKey = new Long(Long.parseLong(groupIdent));
			launchGroup(ureq, groupKey);
		} else if(nextLink == source) {
			doNext(ureq);
		} else if(previousLink == source) {
			doPrevious(ureq);
		} else if (source == luTree) {
			if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
				TreeEvent tev = (TreeEvent) event;
				doNodeClick(ureq, tev);
			}
		} else if (source == coursemain) {
			if (event.getCommand().equals("activateCourseNode")) {
				// Events from the JS function o_activateCourseNode() - activate the given node id
				String nodeid = ureq.getModuleURI();
				if (nodeid != null) {
					CourseNode identNode = course.getRunStructure().getNode(nodeid);
					boolean success = updateTreeAndContent(ureq, identNode, null);
					if (success) {
						currentCourseNode = identNode;
					} else {
						getWindowControl().setWarning(translate("msg.nodenotavailableanymore"));
					}
				}
			}
		} else if(source == toolbarPanel) {
			if(event instanceof PopEvent) {
				PopEvent pop = (PopEvent)event;
				if(pop.getController() == currentToolCtr) {
					toolCtrDone(ureq);
				}
			} else if (event == Event.CLOSE_EVENT) {
				// Navigate beyond the stack, our own layout has been popped - close this tab
				DTabs tabs = getWindowControl().getWindowBackOffice().getWindow().getDTabs();
				if (tabs != null) {
					DTab tab = tabs.getDTab(course);
					if (tab != null) {
						tabs.removeDTab(ureq, tab);						
					}
				}
				// Now try to go back to place that is attacked to (optional) root back business path
				if (StringHelper.containsNonWhitespace(launchdFromBusinessPath)) {
					BusinessControl bc = BusinessControlFactory.getInstance().createFromString(launchdFromBusinessPath);
					WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
					try {
						//make the resume secure. If something fail, don't generate a red screen
						NewControllerFactory.getInstance().launch(ureq, bwControl);
					} catch (Exception e) {
						logError("Error while resuming with root leve back business path::" + launchdFromBusinessPath, e);
					}
				}
			}
		}
	}
	
	private void addCustomCSS(UserRequest ureq) {
		uce = loadUserCourseEnvironment(ureq);
		CustomCSS customCSS = CourseFactory.getCustomCourseCss(ureq.getUserSession(), uce.getCourseEnvironment());
		ChiefController cc = Windows.getWindows(ureq).getChiefController();
		if (cc != null) {
			if(customCSS == null) {
				cc.removeCurrentCustomCSSFromView();
			} else {
				cc.addCurrentCustomCSSToView(customCSS);
			}
		}
		setCustomCSS(customCSS);
	}
	
	private void removeCustomCSS(UserRequest ureq) {
		ChiefController cc = Windows.getWindows(ureq).getChiefController();
		if (cc != null) {
			cc.removeCurrentCustomCSSFromView();
		}
		setCustomCSS(null);
	}
	
	private void toolCtrDone(UserRequest ureq) {
		// release current node controllers resources and do cleanup
		removeAsListenerAndDispose(currentToolCtr);
		currentToolCtr = null;
		accessToolCtr = null;
		optionsToolCtr = null;
		
		addCustomCSS(ureq);
		
		if (isInEditor) {
			isInEditor = false; // for clarity
			if (needsRebuildAfterPublish) {
				needsRebuildAfterPublish = false;
				
			  // rebuild up the running structure for this user, after publish;
				course = CourseFactory.loadCourse(course.getResourceableId());
				uce = loadUserCourseEnvironment(ureq);
				// build score now
				uce.getScoreAccounting().evaluateAll();
				navHandler = new NavigationHandler(uce, false);
				
				// rebuild and jump to root node
				updateTreeAndContent(ureq, null, null);
				// and also tools (maybe new assessable nodes -> show efficiency
				// statment)
				initToolbar();
			}
		}
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(needsRebuildAfter) {
			currentCourseNode = updateAfterChanges(currentCourseNode);
			needsRebuildAfter = false;
		}
		
		// event from the current tool (editor, groupmanagement, archiver)	
		
		if(optionsToolCtr == source) {
			if(event == Event.CHANGED_EVENT) {
				initToolbar();
				toolCtrDone(ureq);
			}
		} else if(accessToolCtr == source) {
			if(event == Event.CHANGED_EVENT) {
				initToolbar();
				toolCtrDone(ureq);
			}
		} else if (currentToolCtr == source) {
			if (event == Event.DONE_EVENT) {
				// special check for editor
				toolCtrDone(ureq);
			}
		} else if (source == toolbarPanel) {
			if(event instanceof PopEvent) {
				PopEvent pop = (PopEvent)event;
				if(pop.getController() == currentToolCtr) {
					toolCtrDone(ureq);
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
					addLoggingResourceable(LoggingResourceable.wrap(identNode));
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
			} else if (REBUILD.equals(event.getCommand())) {
				needsRebuildAfterRunDone = false;
				updateTreeAndContent(ureq, currentCourseNode, null);
			} else if (event instanceof TreeNodeEvent) {
				TreeNodeEvent tne = (TreeNodeEvent) event;
				TreeNode newCpTreeNode = tne.getChosenTreeNode();
				luTree.setSelectedNodeId(newCpTreeNode.getIdent());
			} else if (event == Event.CHANGED_EVENT) {
				updateTreeAndContent(ureq, currentCourseNode, null);
			} else if (event instanceof BusinessGroupModifiedEvent) {
				processBusinessGroupModifiedEvent((BusinessGroupModifiedEvent)event);
				updateTreeAndContent(ureq, currentCourseNode, null);
			}
		} else if (source == glossaryToolCtr) {
			//fire info to IFrameDisplayController
			Long courseID = course.getResourceableId();
			// must work with SP and CP nodes, IFrameDisplayController listens to this event and expects "ICourse" resources.
			String oresName = ICourse.class.getSimpleName();
			ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(new MultiUserEvent(event.getCommand()), OresHelper.createOLATResourceableInstance(oresName, courseID));
		}
	}
	
	private void doNext(UserRequest ureq) {
		List<TreeNode> flatList = new ArrayList<>();
		TreeNode currentNode = luTree.getSelectedNode();
		TreeHelper.makeTreeFlat(luTree.getTreeModel().getRootNode(), flatList);
		int index = flatList.indexOf(currentNode);
		if(index >= 0 && index+1 <flatList.size()) {
			TreeNode nextNode = flatList.get(index + 1);
			TreeEvent tev = new TreeEvent(MenuTree.COMMAND_TREENODE_CLICKED, nextNode.getIdent());
			doNodeClick(ureq, tev);
		}
	}
	
	private void doPrevious(UserRequest ureq) {
		List<TreeNode> flatList = new ArrayList<>();
		TreeNode currentNode = luTree.getSelectedNode();
		TreeHelper.makeTreeFlat(luTree.getTreeModel().getRootNode(), flatList);
		int index = flatList.indexOf(currentNode);
		if(index-1 >= 0 && index-1 < flatList.size()) {
			TreeNode previousNode = flatList.get(index - 1);
			TreeEvent tev = new TreeEvent(MenuTree.COMMAND_TREENODE_CLICKED, previousNode.getIdent());
			doNodeClick(ureq, tev);
		}
	}
	
	private void doNodeClick(UserRequest ureq, TreeEvent tev) {
		if(assessmentChangedEventReceived) {
			uce.getScoreAccounting().evaluateAll();
			assessmentChangedEventReceived = false;
		}
		
		// goto node:
		// after a click in the tree, evaluate the model anew, and set the
		// selection of the tree again
		NodeClickedRef nclr = navHandler.evaluateJumpToTreeNode(ureq, getWindowControl(), treeModel, tev, this, null, currentNodeController);
		if (!nclr.isVisible()) {
			getWindowControl().setWarning(translate("msg.nodenotavailableanymore"));
			// go to root since the current node is no more visible 
			updateTreeAndContent(ureq, null, null);
			updateNextPrevious();					
			return;
		}
		// a click to a subtree's node
		if (nclr.isHandledBySubTreeModelListener() || nclr.getSelectedNodeId() == null) {
			if(nclr.getRunController() != null) {
				//there is an update to the currentNodeController, apply it
				if (currentNodeController != null && !currentNodeController.isDisposed() && !navHandler.isListening(currentNodeController)) {
					currentNodeController.dispose();
				}
				currentNodeController = nclr.getRunController();
				Component nodeComp = currentNodeController.getInitialComponent();
				contentP.setContent(nodeComp);
			}
			
			if(nclr.getSelectedNodeId() != null && nclr.getOpenNodeIds() != null) {
				luTree.setSelectedNodeId(nclr.getSelectedNodeId());
				luTree.setOpenNodeIds(nclr.getOpenNodeIds());
			}
			updateNextPrevious();
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

		// get new run controller. Dispose only if not already disposed in navHandler.evaluateJumpToTreeNode()
		if (currentNodeController != null && !currentNodeController.isDisposed() && !navHandler.isListening(currentNodeController)) {
			currentNodeController.dispose();
		}
		currentNodeController = nclr.getRunController();
		Component nodeComp = currentNodeController.getInitialComponent();
		contentP.setContent(nodeComp);
		addToHistory(ureq, currentNodeController);
		
		// set glossary wrapper dirty after menu click to make it reload the glossary
		// stuff properly when in AJAX mode
		if (glossaryMarkerCtr != null && glossaryMarkerCtr.isTextMarkingEnabled()) {
			glossaryMarkerCtr.getInitialComponent().setDirty(true);
		}
		
		//re set current user count but not every click
		if (currentUserCountLink != null) {
			OLATResourceable runOres = OresHelper.createOLATResourceableInstance(RunMainController.ORES_TYPE_COURSE_RUN, course.getResourceableId());
			int cUsers = CoordinatorManager.getInstance().getCoordinator().getEventBus().getListeningIdentityCntFor(runOres);
			if (cUsers == 0) {
				cUsers = 1;
			}
			currentUserCountLink.setCustomDisplayText(getTranslator().translate("participants.in.course", new String[]{ String.valueOf(cUsers) }));
			currentUserCountLink.setEnabled(false);
		}

		updateNextPrevious();
	}
	
	private void launchAccess(UserRequest ureq) {
		if (hasCourseRight(CourseRights.RIGHT_COURSEEDITOR) || isCourseAdmin) {
			toolbarPanel.popUpToRootController(ureq);
			currentToolCtr = accessToolCtr = new AuthoringEditAccessController(ureq, getWindowControl(), courseRepositoryEntry);
			toolbarPanel.pushController(translate("command.access"), currentToolCtr);
		}
	}
	
	private void launchCatalog(UserRequest ureq) {
		if (hasCourseRight(CourseRights.RIGHT_COURSEEDITOR) || isCourseAdmin) {
			toolbarPanel.popUpToRootController(ureq);
			currentToolCtr = new CatalogSettingsController(ureq, getWindowControl(), toolbarPanel, courseRepositoryEntry);
			listenTo(currentToolCtr);
		}
	}
	
	private void launchEdit(UserRequest ureq) {
		if (hasCourseRight(CourseRights.RIGHT_COURSEEDITOR) || isCourseAdmin) {
			if(!(currentToolCtr instanceof EditorMainController)) {
				//pop and push happens in this method
				EditorMainController ec = CourseFactory.createEditorController(ureq, getWindowControl(), toolbarPanel, course, currentCourseNode);
				//user activity logger which was initialized with course run
				if(ec != null){
					currentToolCtr = ec;
					listenTo(currentToolCtr);
					isInEditor = true;
				}
			}
		}
	}
	
	private void launchEditSettings(UserRequest ureq) {
		if (hasCourseRight(CourseRights.RIGHT_COURSEEDITOR) || isCourseAdmin) {
			if(!(currentToolCtr instanceof AuthoringEditEntrySettingsController)) {
				toolbarPanel.popUpToRootController(ureq);
				//push happen in the init method of the controller
				currentToolCtr = new RepositoryEditDescriptionController(ureq, getWindowControl(), courseRepositoryEntry, false);
				listenTo(currentToolCtr);
				toolbarPanel.pushController(translate("command.settings"), currentToolCtr);
			}
		}
	}
	
	private void launchLayout(UserRequest ureq) {
		if (hasCourseRight(CourseRights.RIGHT_COURSEEDITOR) || isCourseAdmin) {
			toolbarPanel.popUpToRootController(ureq);
			
			boolean managedLayout = RepositoryEntryManagedFlag.isManaged(courseRepositoryEntry, RepositoryEntryManagedFlag.layout);
			CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig().clone();
			currentToolCtr = new CourseLayoutGeneratorController(ureq, getWindowControl(), course, courseConfig,
			  		course.getCourseEnvironment(), !managedLayout);
			toolbarPanel.pushController(translate("command.layout"), currentToolCtr);
		}
	}
	
	private void launchOptions(UserRequest ureq) {
		if (hasCourseRight(CourseRights.RIGHT_COURSEEDITOR) || isCourseAdmin) {
			toolbarPanel.popUpToRootController(ureq);
			
			CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig().clone();
			boolean managedFolder = RepositoryEntryManagedFlag.isManaged(courseRepositoryEntry, RepositoryEntryManagedFlag.resourcefolder);
			currentToolCtr = optionsToolCtr = new CourseOptionsController(ureq, getWindowControl(), course, courseConfig, !managedFolder);
			toolbarPanel.pushController(translate("command.options"), currentToolCtr);
			
		}
	}
	
	private void launchArchive(UserRequest ureq) {
		if (hasCourseRight(CourseRights.RIGHT_ARCHIVING) || isCourseAdmin) {
			toolbarPanel.popUpToRootController(ureq);
			
			currentToolCtr = new ArchiverMainController(ureq, getWindowControl(), course, new FullAccessArchiverCallback());
			listenTo(currentToolCtr);
			toolbarPanel.pushController(translate("command.openarchiver"), currentToolCtr);
		} else {
			throw new OLATSecurityException("clicked archiver, but no according right");
		}
	}
	
	private void launchCalendar(UserRequest ureq) {
		ControllerCreator ctrlCreator = new ControllerCreator() {
			public Controller createController(UserRequest lureq, WindowControl lwControl) {
				ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(courseRepositoryEntry);
				WindowControl llwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, lwControl);
				CourseCalendarController calendarController = new CourseCalendarController(lureq, llwControl, course);					
				// use a one-column main layout
				LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(lureq, llwControl, calendarController);
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
	}
	
	private void launchChat(UserRequest ureq) {
		boolean vip = isCourseCoach || isCourseAdmin;
		OpenInstantMessageEvent event = new OpenInstantMessageEvent(ureq, course, courseTitle, vip);
		ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(event, InstantMessagingService.TOWER_EVENT_ORES);
	}
	
	private void launchDetails(UserRequest ureq) {
		currentToolCtr = new RepositoryEntryDetailsController(ureq, getWindowControl(), courseRepositoryEntry);
		toolbarPanel.pushController(translate("command.courseconfig"), currentToolCtr);
	}
	
	private void launchCourseFolder(UserRequest ureq) {
		toolbarPanel.popUpToRootController(ureq);
		
		// Folder for course with custom link model to jump to course nodes
		VFSContainer namedCourseFolder = new NamedContainerImpl(translate("command.coursefolder"), course.getCourseFolderContainer());
		CustomLinkTreeModel customLinkTreeModel = new CourseInternalLinkTreeModel(course.getEditorTreeModel());
		
		FolderRunController folderMainCtl = new FolderRunController(namedCourseFolder, true, true, true, ureq, getWindowControl(), null, customLinkTreeModel);
		folderMainCtl.addLoggingResourceable(LoggingResourceable.wrap(course));
		currentToolCtr = new LayoutMain3ColsController(ureq, getWindowControl(), folderMainCtl);
		toolbarPanel.pushController(translate("command.coursefolder"), currentToolCtr);
	}
	
	private void launchCourseAreas(UserRequest ureq) {
		toolbarPanel.popUpToRootController(ureq);
		
		OLATResource courseRes = course.getCourseEnvironment().getCourseGroupManager().getCourseResource();
		CourseAreasController areasMainCtl = new CourseAreasController(ureq, getWindowControl(), courseRes);
		areasMainCtl.addLoggingResourceable(LoggingResourceable.wrap(course));
		currentToolCtr = new LayoutMain3ColsController(ureq, getWindowControl(), areasMainCtl);
		toolbarPanel.pushController(translate("command.courseareas"), currentToolCtr);
	}
	
	private void launchDbs(UserRequest ureq) {
		if (hasCourseRight(CourseRights.RIGHT_DB) || isCourseAdmin) {
			toolbarPanel.popUpToRootController(ureq);
			currentToolCtr = new CustomDBMainController(ureq, getWindowControl(), course);
			listenTo(currentToolCtr);
			toolbarPanel.pushController(translate("command.opendb"), currentToolCtr);
		} else {
			throw new OLATSecurityException("clicked dbs, but no according right");
		}
	}
	
	private void launchEfficiencyStatements(UserRequest ureq) {
		// will not be disposed on course run dispose, popus up as new browserwindow
		ControllerCreator ctrlCreator = new ControllerCreator() {
			public Controller createController(UserRequest lureq, WindowControl lwControl) {
				EfficiencyStatementController efficiencyStatementController = new EfficiencyStatementController(lwControl, lureq, courseRepositoryEntry.getKey());
				LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(lureq, getWindowControl(), efficiencyStatementController);
				layoutCtr.setCustomCSS(CourseFactory.getCustomCourseCss(lureq.getUserSession(), uce.getCourseEnvironment()));
				return layoutCtr;
			}					
		};
		//wrap the content controller into a full header layout
		ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
		//open in new browser window
		openInNewBrowserWindow(ureq, layoutCtrlr);
	}
	
	private void launchGroup(UserRequest ureq, Long groupKey) {
		// launch the group in a new top nav tab
		BusinessGroup group = businessGroupService.loadBusinessGroup(groupKey);
		// check if the group still exists and the user is really in this group
		// (security, changed group)
		if (group != null && businessGroupService.isIdentityInBusinessGroup(getIdentity(), group)) {
			// create group without admin flag enabled even though the user might be
			// coach. the flag is not needed here
			// since the groups knows itself if the user is coach and the user sees
			// only his own groups.
			String bsuinessPath = "[BusinessGroup:" + group.getKey() + "]";
			NewControllerFactory.getInstance().launch(bsuinessPath, ureq, getWindowControl());
		} else {
			// display error and do logging
			getWindowControl().setError(translate("error.invalid.group"));
			logAudit("User tried to launch a group but user is not owner or participant "
					+ "of group or group doesn't exist. Hacker attack or group has been changed or deleted. group key :: " + groupKey, null);
			// refresh toolbox that contained wrong group
			reloadGroupMemberships(getIdentity());
		}
	}
	
	private void launchPersonalNotes(UserRequest ureq) {
		// will not be disposed on course run dispose, popus up as new browserwindow
		ControllerCreator ctrlCreator = new ControllerCreator() {
			public Controller createController(UserRequest lureq, WindowControl lwControl) {
				Controller notesCtr = new NoteController(lureq, course, getExtendedCourseTitle(lureq.getLocale()), lwControl);
				LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(lureq, lwControl, notesCtr);
				layoutCtr.setCustomCSS(CourseFactory.getCustomCourseCss(lureq.getUserSession(), uce.getCourseEnvironment()));
				layoutCtr.addDisposableChildController(notesCtr); // dispose glossary on layout dispose
				return layoutCtr;
			}					
		};
		//wrap the content controller into a full header layout
		ControllerCreator popupLayoutCtr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
		//open in new browser window
		openInNewBrowserWindow(ureq, popupLayoutCtr);
	}
	
	private void launchStatistics(UserRequest ureq) {
		if (hasCourseRight(CourseRights.RIGHT_STATISTICS) || isCourseAdmin) {
			toolbarPanel.popUpToRootController(ureq);
			
			currentToolCtr = new StatisticMainController(ureq, getWindowControl(), course);
			listenTo(currentToolCtr);
			toolbarPanel.pushController(translate("command.openstatistic"), currentToolCtr);
		} else throw new OLATSecurityException("clicked statistic, but no according right");
	}
	
	private MembersManagementMainController launchMembersManagement(UserRequest ureq) {
		if (hasCourseRight(CourseRights.RIGHT_GROUPMANAGEMENT) || isCourseAdmin) {
			if(!(currentToolCtr instanceof MembersManagementMainController)) {
				removeAsListenerAndDispose(currentToolCtr);
				
				OLATResourceable ores = OresHelper.createOLATResourceableInstance("MembersMgmt", 0l);
				ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
				WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
				currentToolCtr = new MembersManagementMainController(ureq, addToHistory(ureq, bwControl), courseRepositoryEntry);
				listenTo(currentToolCtr);
				
				toolbarPanel.popUpToRootController(ureq);
				toolbarPanel.pushController(translate("command.opensimplegroupmngt"), currentToolCtr);
			}
			return (MembersManagementMainController)currentToolCtr;
		} else throw new OLATSecurityException("clicked groupmanagement, but no according right");
	}

	private Activateable2 launchAssessmentStatistics(UserRequest ureq, String i18nCrumbKey, String typeName, QTIType... types) {
		OLATResourceable ores = OresHelper.createOLATResourceableType(typeName);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl swControl = addToHistory(ureq, ores, null);
		if (hasCourseRight(CourseRights.RIGHT_STATISTICS) || isCourseAdmin || isCourseCoach) {
			toolbarPanel.popUpToRootController(ureq);
			
			StatisticCourseNodesController statsToolCtr = new StatisticCourseNodesController(ureq, swControl, toolbarPanel, uce, types);
			currentToolCtr = statsToolCtr;
			listenTo(statsToolCtr);
			toolbarPanel.pushController(translate(i18nCrumbKey), statsToolCtr);
			return statsToolCtr;
		}
		return null;
	}

	private Activateable2 launchAssessmentTool(UserRequest ureq) {
		OLATResourceable ores = OresHelper.createOLATResourceableType("assessmentTool");
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl swControl = addToHistory(ureq, ores, null);
		
		// 1) course admins and users with tool right: full access
		if (hasCourseRight(CourseRights.RIGHT_ASSESSMENT) || isCourseAdmin) {
			toolbarPanel.popUpToRootController(ureq);
			
			AssessmentMainController assessmentToolCtr = new AssessmentMainController(ureq, swControl, toolbarPanel, course,
					new FullAccessAssessmentCallback(isCourseAdmin));
			assessmentToolCtr.activate(ureq, null, null);
			currentToolCtr = assessmentToolCtr;
			listenTo(currentToolCtr);
			toolbarPanel.pushController(translate("command.openassessment"), currentToolCtr);
			return assessmentToolCtr;
		}
		// 2) users with coach right: limited access to coached groups
		else if (isCourseCoach) {
			toolbarPanel.popUpToRootController(ureq);
			
			AssessmentMainController assessmentToolCtr =  new AssessmentMainController(ureq, swControl, toolbarPanel, course,
					new CoachingGroupAccessAssessmentCallback());
			assessmentToolCtr.activate(ureq, null, null);
			currentToolCtr = assessmentToolCtr;
			listenTo(currentToolCtr);
			toolbarPanel.pushController(translate("command.openassessment"), currentToolCtr);
			return assessmentToolCtr;
		} else throw new OLATSecurityException("clicked assessment tool in course::" + course.getResourceableId()
				+ ", but no right to launch it. Username::" + ureq.getIdentity().getName());
	}
	
	private void toogleBookmark() {
		String css;
		boolean marked = markManager.isMarked(courseRepositoryEntry, getIdentity(), null);
		if(marked) {
			markManager.removeMark(courseRepositoryEntry, getIdentity(), null);
			css = Mark.MARK_ADD_CSS_ICON;
		} else {
			String businessPath = "[RepositoryEntry:" + courseRepositoryEntry.getKey() + "]";
			markManager.setMark(courseRepositoryEntry, getIdentity(), null, businessPath);
			css = Mark.MARK_CSS_ICON;
		}
		bookmarkLink.setIconLeftCSS(css);
		bookmarkLink.setTitle(translate(marked ? "details.bookmark.remove" : "details.bookmark"));
		bookmarkLink.setDirty(true);
	}

	/**
	 * implementation of listener which listens to publish events
	 * 
	 * @see org.olat.core.util.event.GenericEventListener#event(org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(Event event) {	
		if (event instanceof PublishEvent) {
			PublishEvent pe = (PublishEvent)event;
			if(course.getResourceableId().equals(pe.getPublishedCourseResId())) {
				processPublishEvent(pe);
			}
		} else if (event instanceof OLATResourceableJustBeforeDeletedEvent) {
			OLATResourceableJustBeforeDeletedEvent ojde = (OLATResourceableJustBeforeDeletedEvent) event;
			// make sure it is our course (actually not needed till now, since we
			// registered only to one event, but good style.
			if (ojde.targetEquals(course, true)) {
				doDisposeAfterEvent();
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
					if(!isGuest) {
						UserEfficiencyStatement es = efficiencyStatementManager
							.getUserEfficiencyStatementLight(courseRepositoryEntry.getKey(), identity);
						efficiencyStatementsLink.setEnabled(es != null);
					}
				}
				// raise a flag to indicate refresh
				needsRebuildAfterRunDone = true;
			}
		} else if (event instanceof BusinessGroupModifiedEvent) {
			processBusinessGroupModifiedEvent((BusinessGroupModifiedEvent)event);
		} else if (event instanceof CourseConfigEvent) {				
			processCourseConfigEvent((CourseConfigEvent)event);
		} else if (event instanceof EntryChangedEvent ) {
			EntryChangedEvent repoEvent = (EntryChangedEvent) event;
			if (courseRepositoryEntry.getKey().equals(repoEvent.getChangedEntryKey())) {
				processEntryChangedEvent(repoEvent);
			}
		//All events are MultiUserEvent, check with command at the end
		} else if (event instanceof MultiUserEvent) {
			if (event.getCommand().equals(JOINED) || event.getCommand().equals(LEFT)) {
				updateCurrentUserCount();
			}
		} 
	}
	
	private void processPublishEvent(PublishEvent pe) {
		if (pe.getState() == PublishEvent.PRE_PUBLISH) {
			// so far not interested in PRE PUBLISH event, but one could add user
			// and the currently active BB information. This in turn could be used 
			// by the publish event issuer to decide whether or not to publish...
		} else if (pe.getState() == PublishEvent.PUBLISH) {
			// disable this controller and issue a information
			if (isInEditor) {
				needsRebuildAfterPublish = true;
				// author went in editor and published the course -> raise a flag to
				// later prepare the new
				// course to present him/her a nice view when
				// he/she closes the editor to return to the run main (this controller)
			} else if(getIdentity().getKey().equals(pe.getAuthorKey())) {
				//do nothing
			} else {
				if(currentCourseNode == null) {
					needsRebuildAfter = true;
				} else {
					try {
						String currentNodeIdent = currentCourseNode.getIdent();
						Set<String> deletedNodeIds = pe.getDeletedCourseNodeIds();
						Set<String> modifiedNodeIds = pe.getModifiedCourseNodeIds();
						
						if(deletedNodeIds != null && deletedNodeIds.contains(currentNodeIdent)) {
							doDisposeAfterEvent();
						} else if(modifiedNodeIds != null && modifiedNodeIds.contains(currentNodeIdent)) {
							doDisposeAfterEvent();
							//needsRebuildAfter = true;
						} else {
							needsRebuildAfter = true;
						}
					} catch (Exception e) {
						logError("", e);
						//beyond update, be paranoid
						doDisposeAfterEvent();
					}
				}
			}
		}
	}
	
	private void processEntryChangedEvent(EntryChangedEvent repoEvent) {
		switch(repoEvent.getChange()) {
			case modifiedAtPublish:
			case modifiedAccess:
				if(repoEvent.getAuthorKey() == null || !getIdentity().getKey().equals(repoEvent.getAuthorKey())) {
					doDisposeAfterEvent();
				} 
				break;
			case deleted:
				doDisposeAfterEvent();
				break;
			default: {}
		}
	}
	
	private void processCourseConfigEvent(CourseConfigEvent event) {
		switch(event.getType()) {
			case calendar: {
				if(calendarLink != null) {
					CourseConfig cc = uce.getCourseEnvironment().getCourseConfig();
					calendarLink.setVisible(cc.isCalendarEnabled());
					toolbarPanel.setDirty(true);
				}
				break;
			}
			case efficiencyStatement: {
				if(efficiencyStatementsLink != null) {
					CourseConfig cc = uce.getCourseEnvironment().getCourseConfig();
					efficiencyStatementsLink.setVisible(cc.isEfficencyStatementEnabled());
					if(cc.isEfficencyStatementEnabled()) {
						UserEfficiencyStatement es = efficiencyStatementManager
								.getUserEfficiencyStatementLight(courseRepositoryEntry.getKey(), getIdentity());
						efficiencyStatementsLink.setEnabled(es != null);
					}
					toolbarPanel.setDirty(true);
				}
				break;
			}
			case layout: {
				//don't restart for that
				break;
			}
		}
	}
	
	private void processBusinessGroupModifiedEvent(BusinessGroupModifiedEvent bgme) {
		Identity identity = uce.getIdentityEnvironment().getIdentity();
		// only do something if this identity is affected by change and the action
		// was adding or removing of the user
		if (bgme.wasMyselfAdded(identity) || bgme.wasMyselfRemoved(identity)) {
			// 1) reinitialize all group memberships
			reloadGroupMemberships(identity);
			// 2) reinitialize the users roles and rights
			reloadUserRolesAndRights(identity);
			// 3) rebuild toolboxes with link to groups and tools
			 initToolbar();
			needsRebuildAfterRunDone = true;
		} else if (bgme.getCommand().equals(BusinessGroupModifiedEvent.GROUPRIGHTS_MODIFIED_EVENT)) {
			// check if this affects a right group where the user does participate.
			// if so, we need
			// to rebuild the toolboxes
			if (PersistenceHelper.listContainsObjectByKey(uce.getParticipatingGroups(), bgme.getModifiedGroupKey()) ||
					PersistenceHelper.listContainsObjectByKey(uce.getCoachedGroups(), bgme.getModifiedGroupKey())) {
				// 1) reinitialize all group memberships
				reloadGroupMemberships(identity);
				// 2) reinitialize the users roles and rights
				reloadUserRolesAndRights(identity);
				// 3) rebuild toolboxes with link to groups and tools
				initToolbar();
			}
		}
	}
	
	private void doDisposeAfterEvent() {
		if(currentNodeController instanceof ConfigurationChangedListener) {
			//give to opportunity to close popups ...
			((ConfigurationChangedListener)currentNodeController).configurationChanged();
		}
		dispose();	
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
	private void initToolbar() {
		CourseConfig cc = uce.getCourseEnvironment().getCourseConfig();
		
		toolbarPanel.removeAllTools();
		
		initTools();
		initSettings();
		initToolsMyCourse(cc);
		initGeneralTools(cc);
	}
	
	private void initTools() {
		boolean managed = RepositoryEntryManagedFlag.isManaged(courseRepositoryEntry, RepositoryEntryManagedFlag.editcontent);
		// 1) administrative tools
		if (isCourseAdmin || isCourseCoach || hasCourseRight(CourseRights.RIGHT_COURSEEDITOR)
				|| hasCourseRight(CourseRights.RIGHT_GROUPMANAGEMENT) || hasCourseRight(CourseRights.RIGHT_ARCHIVING)
				|| hasCourseRight(CourseRights.RIGHT_STATISTICS) || hasCourseRight(CourseRights.RIGHT_DB)
				|| hasCourseRight(CourseRights.RIGHT_ASSESSMENT)) {

			tools = new Dropdown("editTools", "header.tools", false, getTranslator());
			tools.setElementCssClass("o_sel_course_tools");
			tools.setIconCSS("o_icon o_icon_tools");

			if (hasCourseRight(CourseRights.RIGHT_COURSEEDITOR) || isCourseAdmin) {
				editLink = LinkFactory.createToolLink("edit.cmd", translate("command.openeditor"), this, "o_icon_courseeditor");
				editLink.setElementCssClass("o_sel_course_editor");
				editLink.setEnabled(!managed);
				tools.addComponent(editLink);
				
				folderLink = LinkFactory.createToolLink("cfd", translate("command.coursefolder"), this, "o_icon_coursefolder");
				folderLink.setElementCssClass("o_sel_course_folder");
				folderLink.setEnabled(!managed);
				tools.addComponent(folderLink);
				tools.addComponent(new Spacer(""));
			}
			
			if (hasCourseRight(CourseRights.RIGHT_GROUPMANAGEMENT) || isCourseAdmin) {
				userMgmtLink = LinkFactory.createToolLink("unifiedusermngt", translate("command.opensimplegroupmngt"), this, "o_icon_membersmanagement");
				tools.addComponent(userMgmtLink);
			}
			if (hasCourseRight(CourseRights.RIGHT_ASSESSMENT) || isCourseCoach || isCourseAdmin) {
				assessmentLink = LinkFactory.createToolLink("assessment",translate("command.openassessment"), this, "o_icon_assessment_tool");
				tools.addComponent(assessmentLink);
			}
			if (hasCourseRight(CourseRights.RIGHT_ARCHIVING) || isCourseAdmin) {
				archiverLink = LinkFactory.createToolLink("archiver", translate("command.openarchiver"), this, "o_icon_archive_tool");
				tools.addComponent(archiverLink);
			}
			tools.addComponent(new Spacer(""));
			
			
			if (hasCourseRight(CourseRights.RIGHT_STATISTICS) || isCourseAdmin) {
				courseStatisticLink = LinkFactory.createToolLink("statistic",translate("command.openstatistic"), this, "o_icon_statistics_tool");
				tools.addComponent(courseStatisticLink);
			}
			if (hasCourseRight(CourseRights.RIGHT_STATISTICS) || isCourseAdmin || isCourseCoach) {
				final AtomicInteger testNodes = new AtomicInteger();
				final AtomicInteger surveyNodes = new AtomicInteger();
				new TreeVisitor(new Visitor() {
					@Override
					public void visit(INode node) {
						if(((CourseNode)node).isStatisticNodeResultAvailable(uce, QTIType.test, QTIType.onyx)) {
							testNodes.incrementAndGet();
						} else if(((CourseNode)node).isStatisticNodeResultAvailable(uce, QTIType.survey)) {
							surveyNodes.incrementAndGet();
						}
					}
				}, course.getRunStructure().getRootNode(), true).visitAll();
				if(testNodes.intValue() > 0) {
					testStatisticLink = LinkFactory.createToolLink("qtistatistic", translate("command.openteststatistic"), this, "o_icon_statistics_tool");
					tools.addComponent(testStatisticLink);
				}
				
				if(surveyNodes.intValue() > 0) {
					surveyStatisticLink = LinkFactory.createToolLink("qtistatistic", translate("command.opensurveystatistic"), this, "o_icon_statistics_tool");
					tools.addComponent(surveyStatisticLink);
				}
			}
			tools.addComponent(new Spacer(""));

			if (hasCourseRight(CourseRights.RIGHT_COURSEEDITOR) || isCourseAdmin) {
				areaLink = LinkFactory.createToolLink("careas", translate("command.courseareas"), this, "o_icon_courseareas");
				areaLink.setElementCssClass("o_sel_course_areas");
				areaLink.setEnabled(!managed);
				tools.addComponent(areaLink);
			}
			if (CourseDBManager.getInstance().isEnabled() && (hasCourseRight(CourseRights.RIGHT_DB) || isCourseAdmin)) {
				dbLink = LinkFactory.createToolLink("customDb",translate("command.opendb"), this, "o_icon_coursedb");
				tools.addComponent(dbLink);
			}
			
			toolbarPanel.addTool(tools, Align.left, true);
		}
	}
	
	private void initSettings() {
		if (isCourseAdmin || hasCourseRight(CourseRights.RIGHT_COURSEEDITOR)) {
			boolean managed = RepositoryEntryManagedFlag.isManaged(courseRepositoryEntry, RepositoryEntryManagedFlag.editcontent);
			
			settings = new Dropdown("settings", "header.settings", false, getTranslator());
			settings.setElementCssClass("o_sel_course_settings");
			settings.setIconCSS("o_icon o_icon_customize");
			
			editDescriptionLink = LinkFactory.createToolLink("settings.cmd", translate("command.settings"), this, "o_icon_settings");
			editDescriptionLink.setElementCssClass("o_sel_course_settings");
			editDescriptionLink.setEnabled(!managed);
			settings.addComponent(editDescriptionLink);
			
			accessLink = LinkFactory.createToolLink("access.cmd", translate("command.access"), this, "o_icon_password");
			accessLink.setElementCssClass("o_sel_course_access");
			accessLink.setEnabled(!managed);
			settings.addComponent(accessLink);
			
			catalogLink = LinkFactory.createToolLink("access.cmd", translate("command.catalog"), this, "o_icon_catalog");
			catalogLink.setElementCssClass("o_sel_course_catalog");
			catalogLink.setEnabled(!managed);
			settings.addComponent(catalogLink);
			
			settings.addComponent(new Spacer(""));
			
			layoutLink = LinkFactory.createToolLink("access.cmd", translate("command.layout"), this, "o_icon_layout");
			layoutLink.setElementCssClass("o_sel_course_layout");
			layoutLink.setEnabled(!managed);
			settings.addComponent(layoutLink);
			
			optionsLink = LinkFactory.createToolLink("access.cmd", translate("command.options"), this, "o_icon_options");
			optionsLink.setElementCssClass("o_sel_course_options");
			optionsLink.setEnabled(!managed);
			settings.addComponent(optionsLink);

			toolbarPanel.addTool(settings, Align.left, true);
		}
	}
	
	private void initToolsMyCourse(CourseConfig cc) {
		myCourse = new Dropdown("myCourse", "header.tools.participatedGroups", false, getTranslator());
		myCourse.setIconCSS("o_icon o_icon_user");

		// Personal tools on right side
		if (course.hasAssessableNodes() && !isGuest) {
			// link to efficiency statements should
			// - not appear when not configured in course configuration
			// - not appear when configured in course configuration but no assessable
			// node exist
			// - appear but dimmed when configured, assessable node exist but no
			// assessment data exists for user
			// - appear as link when configured, assessable node exist and assessment
			// data exists for user
			efficiencyStatementsLink = LinkFactory.createToolLink("efficiencystatement",translate("command.efficiencystatement"), this, "o_icon_certificate");
			efficiencyStatementsLink.setPopup(new LinkPopupSettings(750, 800, "eff"));
			efficiencyStatementsLink.setVisible(cc.isEfficencyStatementEnabled());
			myCourse.addComponent(efficiencyStatementsLink);
			if(cc.isEfficencyStatementEnabled()) {
				UserEfficiencyStatement es = efficiencyStatementManager
						.getUserEfficiencyStatementLight(courseRepositoryEntry.getKey(), getIdentity());
				efficiencyStatementsLink.setEnabled(es != null);
			}
		}
		
		if (!isGuest) {
			noteLink = LinkFactory.createToolLink("personalnote",translate("command.personalnote"), this, "o_icon_notes");
			noteLink.setPopup(new LinkPopupSettings(750, 550, "notes"));
			myCourse.addComponent(noteLink);
		}
		
		if (offerBookmark && !isGuest) {
			boolean marked = markManager.isMarked(courseRepositoryEntry, getIdentity(), null);
			String css = marked ? Mark.MARK_CSS_ICON : Mark.MARK_ADD_CSS_ICON;
			bookmarkLink = LinkFactory.createToolLink("bookmark",translate("command.bookmark"), this, css);
			bookmarkLink.setTitle(translate(marked ? "details.bookmark.remove" : "details.bookmark"));
			myCourse.addComponent(bookmarkLink);
		}
		
		if(myCourse.size() > 0 && (uce.getCoachedGroups().size() > 0 || uce.getParticipatingGroups().size() > 0 || uce.getWaitingLists().size() > 0)) {
			myCourse.addComponent(new Spacer(""));
		}

		// 2) add coached groups
		if (uce.getCoachedGroups().size() > 0) {
			for (BusinessGroup group:uce.getCoachedGroups()) {
				Link link = LinkFactory.createToolLink(CMD_START_GROUP_PREFIX + group.getKey(), StringHelper.escapeHtml(group.getName()), this);
				myCourse.addComponent(link);
			}
		}

		// 3) add participating groups
		if (uce.getParticipatingGroups().size() > 0) {
			for (BusinessGroup group: uce.getParticipatingGroups()) {
				Link link = LinkFactory.createToolLink(CMD_START_GROUP_PREFIX + group.getKey(), StringHelper.escapeHtml(group.getName()), this);
				myCourse.addComponent(link);
			}
		}

		// 5) add waiting-list groups
		if (uce.getWaitingLists().size() > 0) {
			for (BusinessGroup group:uce.getWaitingLists()) {
				int pos = businessGroupService.getPositionInWaitingListFor(getIdentity(), group);
				String name = StringHelper.escapeHtml(group.getName()) + " (" + pos + ")";
				Link link = LinkFactory.createToolLink(CMD_START_GROUP_PREFIX + group.getKey(), name, this);
				link.setEnabled(false);
				myCourse.addComponent(link);
			}
		}
		if(myCourse.size() > 0) {
			toolbarPanel.addTool(myCourse, Align.right);
		}
	}
	
	private void initGeneralTools(CourseConfig cc) {
		if (showCourseConfigLink) {
			detailsLink = LinkFactory.createToolLink("courseconfig",translate("command.courseconfig"), this, "o_icon_details");
			toolbarPanel.addTool(detailsLink);
		}		
		if (!isGuest) {
			calendarLink = LinkFactory.createToolLink("calendar",translate("command.calendar"), this, "o_icon_calendar");
			calendarLink.setPopup(new LinkPopupSettings(950, 750, "cal"));
			calendarLink.setVisible(cc.isCalendarEnabled());
			toolbarPanel.addTool(calendarLink);
		}
		if (cc.hasGlossary()) {
			toolbarPanel.addTool(glossaryToolCtr.getInitialComponent(), null, false, "o_tool_dropdown dropdown");
		}
		//add group chat to toolbox
		InstantMessagingModule imModule = CoreSpringFactory.getImpl(InstantMessagingModule.class);
		boolean chatIsEnabled = !isGuest && imModule.isEnabled() && imModule.isCourseEnabled()
				&& CourseModule.isCourseChatEnabled() && cc.isChatEnabled();
		if(chatIsEnabled) {
			chatLink = LinkFactory.createToolLink("chat",translate("command.coursechat"), this, "o_icon_chat");
			toolbarPanel.addTool(chatLink);
		}
		
		// new toolbox 'general'
		previousLink = LinkFactory.createToolLink("previouselement","", this, "o_icon_previous_toolbar");
		previousLink.setTitle(translate("command.previous"));
		toolbarPanel.addTool(previousLink, Align.rightEdge, false, "o_tool_previous");
		nextLink = LinkFactory.createToolLink("nextelement","", this, "o_icon_next_toolbar");
		nextLink.setTitle(translate("command.next"));
		toolbarPanel.addTool(nextLink, Align.rightEdge, false, "o_tool_next");
		updateNextPrevious();
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
			
		currentUserCountLink.setCustomDisplayText(translate("participants.in.course", new String[]{String.valueOf(currentUserCount)}));
	}

	/**
	 * Reads the users learning group rights from a local hash map. use
	 * initCourseRightCache() to initialize the cache.
	 * 
	 * @param right The name of a learning group right
	 * @return true if the user has this right, false otherwise.
	 */
	private boolean hasCourseRight(String right) {
		Boolean bool = courseRightsCache.get(right);
		return bool.booleanValue();
	}
	
	UserCourseEnvironmentImpl getUce() {
		return uce;
	}

	/**
	 * Initialize the users group memberships for groups used within this course
	 * 
	 * @param identity
	 */
	private void reloadGroupMemberships(Identity identity) {
		CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
		List<BusinessGroup> coachedGroups = cgm.getOwnedBusinessGroups(identity);
		List<BusinessGroup> participatedGroups = cgm.getParticipatingBusinessGroups(identity);
		List<BusinessGroup> waitingLists = cgm.getWaitingListGroups(identity);
		uce.setGroupMemberships(coachedGroups, participatedGroups, waitingLists);
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
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, course);
		//remove as listener from course run eventAgency
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, courseRunOres);

		if (CourseModule.displayParticipantsCount()) {
			CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new MultiUserEvent(LEFT), courseRunOres);
		}
				
		// currentNodeController must be disposed manually does not work with
		// general BasicController methods
		if (currentNodeController != null && !currentNodeController.isDisposed()) {
			currentNodeController.dispose();
		}
		currentNodeController = null;
		navHandler.dispose();

		// log to Statistical and User log
		ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_LEAVING, getClass());
		
		// log the fact that the user is leaving the course in the log file
		logAudit("Leaving course: [[["+courseTitle+"]]]", course.getResourceableId().toString());
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry firstEntry = entries.get(0);
		String type = firstEntry.getOLATResourceable().getResourceableTypeName();
		if("CourseNode".equalsIgnoreCase(type)) {
			CourseNode cn = course.getRunStructure().getNode(firstEntry.getOLATResourceable().getResourceableId().toString());

			getWindowControl().makeFlat();
			// add logging information for case course gets started via jump-in
			// link/search
			addLoggingResourceable(LoggingResourceable.wrap(course));
			if (cn != null) {
				addLoggingResourceable(LoggingResourceable.wrap(cn));
			}
			
			if(entries.size() > 1) {
				entries = entries.subList(1, entries.size());
			}
			updateTreeAndContent(ureq, cn, null, entries, firstEntry.getTransientState());
		} else if ("assessmentTool".equalsIgnoreCase(type)) {
			//check the security before, the link is perhaps in the wrong hands
			if(hasCourseRight(CourseRights.RIGHT_ASSESSMENT) || isCourseAdmin || isCourseCoach) {
				try {
					Activateable2 assessmentCtrl = launchAssessmentTool(ureq);
					
					List<ContextEntry> subEntries;
					if(entries.size() > 1 && entries.get(1).getOLATResourceable().getResourceableTypeName().equals(type)) {
						subEntries = entries.subList(2, entries.size());
					} else {
						subEntries = entries.subList(1, entries.size());
					}
					assessmentCtrl.activate(ureq, subEntries, firstEntry.getTransientState());
				} catch (OLATSecurityException e) {
					//the wrong link to the wrong person
				}
			}
		}  else if ("TestStatistics".equalsIgnoreCase(type) || "SurveyStatistics".equalsIgnoreCase(type)) {
			//check the security before, the link is perhaps in the wrong hands
			if(hasCourseRight(CourseRights.RIGHT_ASSESSMENT) || isCourseAdmin || isCourseCoach) {
				try {
					Activateable2 assessmentCtrl;
					if("TestStatistics".equalsIgnoreCase(type)) {
						assessmentCtrl = launchAssessmentStatistics(ureq, "command.openteststatistic", "TestStatistics", QTIType.test, QTIType.onyx);
					} else {
						assessmentCtrl = launchAssessmentStatistics(ureq, "command.opensurveystatistic", "SurveyStatistics", QTIType.survey);
					}
					
					List<ContextEntry> subEntries;
					if(entries.size() > 1 && entries.get(1).getOLATResourceable().getResourceableTypeName().equals(type)) {
						subEntries = entries.subList(2, entries.size());
					} else {
						subEntries = entries.subList(1, entries.size());
					}
					assessmentCtrl.activate(ureq, subEntries, firstEntry.getTransientState());
				} catch (OLATSecurityException e) {
					//the wrong link to the wrong person
				}
			}
		} else if("MembersMgmt".equalsIgnoreCase(type)) {
			try {
				List<ContextEntry> subEntries = entries.subList(1, entries.size());
				launchMembersManagement(ureq).activate(ureq, subEntries, firstEntry.getTransientState());
			} catch (OLATSecurityException e) {
				//the wrong link to the wrong person
			}
		} else if("Editor".equalsIgnoreCase(type) || "activateEditor".equalsIgnoreCase(type)) {
			// Nothing to do if already in editor. Can happen when editor is
			// triggered externally, e.g. from the details page while user has
			// the editor already open
			if (!isInEditor && !RepositoryEntryManagedFlag.isManaged(courseRepositoryEntry, RepositoryEntryManagedFlag.editcontent)) {
				launchEdit(ureq);
			}
		}
	}

	public void disableToolController(boolean disable) {
		toolbarPanel.setToolbarEnabled(!disable);
	}
	public void disableCourseClose(boolean disable) {
		toolbarPanel.setShowCloseLink(true, !disable);
	}

	@Override
	public void setDisposedMsgController(Controller disposeMsgController) {
		super.setDisposedMsgController(disposeMsgController);
	}
}