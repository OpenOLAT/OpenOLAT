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
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.htmlsite.OlatCmdEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.ConfigurationChangedListener;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.messages.MessageController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.gui.control.generic.textmarker.GlossaryMarkupItemController;
import org.olat.core.gui.control.generic.title.TitledWrapperController;
import org.olat.core.gui.control.generic.tool.ToolController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLATSecurityException;
import org.olat.core.logging.activity.CourseLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.core.util.resource.OresHelper;
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
import org.olat.course.db.CourseDBManager;
import org.olat.course.db.CustomDBMainController;
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
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.RepositoryEntryStatus;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.ui.author.AuthoringEditEntrySettingsController;
import org.olat.repository.ui.list.RepositoryEntryDetailsController;
import org.olat.repository.ui.list.RepositoryEntryRow;
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
	private Link editLink, editSettingsLink, areaLink, folderLink,
			surveyStatisticLink, testStatisticLink, courseStatisticLink,
			userMgmtLink, archiverLink, assessmentLink, dbLink,
			efficiencyStatementsLink, bookmarkLink, calendarLink, detailsLink, noteLink, chatLink;
	
	private Panel contentP;

	private NavigationHandler navHandler;
	private UserCourseEnvironmentImpl uce;
	private LayoutMain3ColsController columnLayoutCtr;

	private Controller currentToolCtr;
	private Controller currentNodeController; // the currently open node config
	private TooledStackedPanel all;

	private boolean isInEditor = false;

	private Map<String, Boolean> courseRightsCache = new HashMap<String, Boolean>();
	private boolean isCourseAdmin = false;
	private boolean isCourseCoach = false;
	private final Roles roles;

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

		super(ureq, wControl);
		
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
		all = new TooledStackedPanel("courseStackPanel", getTranslator(), this);
				
		//StackedControllerImpl(getWindowControl(), getTranslator(), "o_course_breadcumbs");
		luTree = new MenuTree(null, "luTreeRun", this);
		luTree.setExpandSelectedNode(false);
		contentP = new Panel("building_block_content");

		// Initialize the the users roles and right for this course
		// 1) is guest flag
		isGuest = ureq.getUserSession().getRoles().isGuestOnly();
		// preload user assessment data in assessmnt properties cache to speed up
		// course loading
		course.getCourseEnvironment().getAssessmentManager().preloadCache(identity);

		// build up the running structure for this user;
		// get all group memberships for this course
		uce = loadUserCourseEnvironment(ureq, course);
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
		all.pushController(courseTitle, columnLayoutCtr);
		
		// init the menu and tool controller
		initToolController();

		coursemain = createVelocityContainer("index");
		coursemain.setDomReplaceable(false);
		// see function gotonode in functions.js to see why we need the repositoryentry-key here:
		// it is to correctly apply external links using course-internal links via javascript
		coursemain.contextPut("courserepokey", courseRepositoryEntry.getKey());
		//coursemain.put("coursemain", all.getInitialComponent());

		putInitialPanel(all);

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
	
	private UserCourseEnvironmentImpl loadUserCourseEnvironment(UserRequest ureq, ICourse course) {
		CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
		List<BusinessGroup> coachedGroups = cgm.getOwnedBusinessGroups(ureq.getIdentity());
		List<BusinessGroup> participatedGroups = cgm.getParticipatingBusinessGroups(ureq.getIdentity());
		List<BusinessGroup> waitingLists = cgm.getWaitingListGroups(ureq.getIdentity());
		return new UserCourseEnvironmentImpl(ureq.getUserSession().getIdentityEnvironment(), course.getCourseEnvironment(),
				coachedGroups, participatedGroups, waitingLists, null, null, null);
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
		
		//fxdiff BAKS-7 Resume function
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
		// enableCustomCourseCSS(ureq);
		
		return true;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(editLink == source) {
			all.popUpToRootController(ureq);
			doEdit(ureq);
		} else if(editSettingsLink == source) {
			all.popUpToRootController(ureq);
			doEditSettings(ureq);
		} else if(userMgmtLink == source) {
			all.popUpToRootController(ureq);
			launchMembersManagement(ureq);
		} else if(archiverLink == source) {
			all.popUpToRootController(ureq);
			launchArchive(ureq);
		} else if(assessmentLink == source) {
			all.popUpToRootController(ureq);
			launchAssessmentTool(ureq);
		} else if(testStatisticLink == source) {
			all.popUpToRootController(ureq);
			launchAssessmentStatistics(ureq, "command.openteststatistic", "TestStatistics", QTIType.test, QTIType.onyx);
		} else if (surveyStatisticLink == source) {
			all.popUpToRootController(ureq);
			launchAssessmentStatistics(ureq, "command.opensurveystatistic", "SurveyStatistics", QTIType.survey);
		} else if(courseStatisticLink == source) {
			all.popUpToRootController(ureq);
			launchStatistics(ureq);
		} else if(dbLink == source) {
			all.popUpToRootController(ureq);
			launchDbs(ureq);
		} else if(folderLink == source) {
			all.popUpToRootController(ureq);
			launchCourseFolder(ureq);
		} else if(areaLink == source) {
			all.popUpToRootController(ureq);
			launchCourseAreas(ureq);
		} else if(efficiencyStatementsLink == source) {
			launchEfficiencyStatements(ureq);
		} else if(bookmarkLink == source) {
			toogleBookmark();
		} else if(calendarLink == source) {
			launchCalendar(ureq);
		} else if(detailsLink == source) {
			launchDetails(ureq);
		} else if(noteLink == source) {
			launchPersonalNotes(ureq);
		} else if(chatLink == source) {
			launchChat(ureq);
		} else if(source instanceof Link && ((Link)source).getCommand().startsWith(CMD_START_GROUP_PREFIX)) {
			String groupIdent = ((Link)source).getCommand().substring(CMD_START_GROUP_PREFIX.length());
			Long groupKey = new Long(Long.parseLong(groupIdent));
			launchGroup(ureq, groupKey);
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
					if (success) currentCourseNode = identNode;
					else getWindowControl().setWarning(translate("msg.nodenotavailableanymore"));
				}
			}
		}
	}
	
	private void eventDone(UserRequest ureq) {
		if (isInEditor) {
			isInEditor = false; // for clarity
			removeAsListenerAndDispose(currentToolCtr);
			currentToolCtr = null;
			if (needsRebuildAfterPublish) {
				needsRebuildAfterPublish = false;
				
			  // rebuild up the running structure for this user, after publish;
				course = CourseFactory.loadCourse(course.getResourceableId());
				uce = loadUserCourseEnvironment(ureq, course);
				// build score now
				uce.getScoreAccounting().evaluateAll();
				navHandler = new NavigationHandler(uce, false);
				
				// rebuild and jump to root node
				updateTreeAndContent(ureq, null, null);
				// and also tools (maybe new assessable nodes -> show efficiency
				// statment)
				initToolController();
			}
		} else {
			// release current node controllers resources and do cleanup if it was not the editor!
			removeAsListenerAndDispose(currentToolCtr);
			currentToolCtr = null;
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
				// special check for editor
				eventDone(ureq);
			}
		} else if (source == all) {
			if(event instanceof PopEvent) {
				PopEvent pop = (PopEvent)event;
				if(pop.getController() == currentToolCtr) {
					eventDone(ureq);
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
	
	private void doEdit(UserRequest ureq) {
		if (hasCourseRight(CourseRights.RIGHT_COURSEEDITOR) || isCourseAdmin) {
			Controller ec = CourseFactory.createEditorController(ureq, getWindowControl(), all, course, currentCourseNode);
			//user activity logger which was initialized with course run
			if(ec != null){
				//we are in editing mode
				currentToolCtr = ec;
				listenTo(currentToolCtr);
				isInEditor = true;
			}
		} else throw new OLATSecurityException("wanted to activate editor, but no according right");
	}
	
	private void doEditSettings(UserRequest ureq) {
		if (hasCourseRight(CourseRights.RIGHT_COURSEEDITOR) || isCourseAdmin) {
			currentToolCtr = new AuthoringEditEntrySettingsController(ureq, getWindowControl(), all, courseRepositoryEntry);
			listenTo(currentToolCtr);
		} else throw new OLATSecurityException("wanted to activate editor, but no according right");
	}
	
	private void doNodeClick(UserRequest ureq, TreeEvent tev) {
		if(assessmentChangedEventReceived) {
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
	
	private void launchArchive(UserRequest ureq) {
		if (hasCourseRight(CourseRights.RIGHT_ARCHIVING) || isCourseAdmin) {
			currentToolCtr = new ArchiverMainController(ureq, getWindowControl(), course, new FullAccessArchiverCallback());
			listenTo(currentToolCtr);
			all.pushController(translate("command.openarchiver"), currentToolCtr);
		} else throw new OLATSecurityException("clicked archiver, but no according right");
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
		RepositoryEntryMyView view = repositoryService.loadMyView(getIdentity(), courseRepositoryEntry);
		RepositoryEntryRow row = new RepositoryEntryRow(view);
		currentToolCtr = new RepositoryEntryDetailsController(ureq, getWindowControl(), row);
		all.pushController(translate("command.courseconfig"), currentToolCtr);
	}
	
	private void launchCourseFolder(UserRequest ureq) {
		// Folder for course with custom link model to jump to course nodes
		VFSContainer namedCourseFolder = new NamedContainerImpl(translate("command.coursefolder"), course.getCourseFolderContainer());
		CustomLinkTreeModel customLinkTreeModel = new CourseInternalLinkTreeModel(course.getEditorTreeModel());
		
		FolderRunController folderMainCtl = new FolderRunController(namedCourseFolder, true, true, true, ureq, getWindowControl(), null, customLinkTreeModel);
		folderMainCtl.addLoggingResourceable(LoggingResourceable.wrap(course));
		currentToolCtr = new LayoutMain3ColsController(ureq, getWindowControl(), folderMainCtl);
		all.pushController(translate("command.coursefolder"), currentToolCtr);
	}
	
	private void launchCourseAreas(UserRequest ureq) {
		OLATResource courseRes = course.getCourseEnvironment().getCourseGroupManager().getCourseResource();
		CourseAreasController areasMainCtl = new CourseAreasController(ureq, getWindowControl(), courseRes);
		areasMainCtl.addLoggingResourceable(LoggingResourceable.wrap(course));
		currentToolCtr = new LayoutMain3ColsController(ureq, getWindowControl(), areasMainCtl);
		all.pushController(translate("command.courseareas"), currentToolCtr);
	}
	
	private void launchDbs(UserRequest ureq) {
		if (hasCourseRight(CourseRights.RIGHT_DB) || isCourseAdmin) {
			currentToolCtr = new CustomDBMainController(ureq, getWindowControl(), course);
			listenTo(currentToolCtr);
			all.pushController(translate("command.opendb"), currentToolCtr);
		} else throw new OLATSecurityException("clicked dbs, but no according right");
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
			currentToolCtr = new StatisticMainController(ureq, getWindowControl(), course);
			listenTo(currentToolCtr);
			all.pushController(translate("command.openstatistic"), currentToolCtr);
		} else throw new OLATSecurityException("clicked statistic, but no according right");
	}
	
	private MembersManagementMainController launchMembersManagement(UserRequest ureq) {
		if (hasCourseRight(CourseRights.RIGHT_GROUPMANAGEMENT) || isCourseAdmin) {
			if(!(currentToolCtr instanceof MembersManagementMainController)) {
				OLATResourceable ores = OresHelper.createOLATResourceableInstance("MembersMgmt", 0l);
				ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
				WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
				currentToolCtr = new MembersManagementMainController(ureq, addToHistory(ureq, bwControl), courseRepositoryEntry);
				listenTo(currentToolCtr);
				
				all.popUpToRootController(ureq);
				all.pushController(translate("command.opensimplegroupmngt"), currentToolCtr);
			}
			return (MembersManagementMainController)currentToolCtr;
		} else throw new OLATSecurityException("clicked groupmanagement, but no according right");
	}

	private Activateable2 launchAssessmentStatistics(UserRequest ureq, String i18nCrumbKey, String typeName, QTIType... types) {
		OLATResourceable ores = OresHelper.createOLATResourceableType(typeName);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl swControl = addToHistory(ureq, ores, null);
		if (hasCourseRight(CourseRights.RIGHT_STATISTICS) || isCourseAdmin || isCourseCoach) {
			StatisticCourseNodesController statsToolCtr = new StatisticCourseNodesController(ureq, swControl, uce, types);
			currentToolCtr = statsToolCtr;
			listenTo(statsToolCtr);
			all.pushController(translate(i18nCrumbKey), statsToolCtr);
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
			AssessmentMainController assessmentToolCtr = new AssessmentMainController(ureq, swControl, all, course,
					new FullAccessAssessmentCallback(isCourseAdmin));
			assessmentToolCtr.activate(ureq, null, null);
			currentToolCtr = assessmentToolCtr;
			listenTo(currentToolCtr);
			all.pushController(translate("command.openassessment"), currentToolCtr);
			return assessmentToolCtr;
		}
		// 2) users with coach right: limited access to coached groups
		else if (isCourseCoach) {
			AssessmentMainController assessmentToolCtr =  new AssessmentMainController(ureq, swControl, all, course,
					new CoachingGroupAccessAssessmentCallback());
			assessmentToolCtr.activate(ureq, null, null);
			currentToolCtr = assessmentToolCtr;
			listenTo(currentToolCtr);
			all.pushController(translate("command.openassessment"), currentToolCtr);
			return assessmentToolCtr;
		} else throw new OLATSecurityException("clicked assessment tool in course::" + course.getResourceableId()
				+ ", but no right to launch it. Username::" + ureq.getIdentity().getName());
	}
	
	private void toogleBookmark() {
		boolean marked = markManager.isMarked(courseRepositoryEntry, getIdentity(), null);
		if(marked) {
			markManager.removeMark(courseRepositoryEntry, getIdentity(), null);
		} else {
			String businessPath = "[RepositoryEntry:" + courseRepositoryEntry.getKey() + "]";
			markManager.setMark(courseRepositoryEntry, getIdentity(), null, businessPath);
		}
		String css = marked ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE;
		bookmarkLink.setIconLeftCSS(css);
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
				doDisposeAfterEvent();
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
					UserEfficiencyStatement es = efficiencyStatementManager
							.getUserEfficiencyStatementLight(courseRepositoryEntry.getKey(), identity);
					efficiencyStatementsLink.setEnabled(es != null);
				}
				// raise a flag to indicate refresh
				needsRebuildAfterRunDone = true;
			}
		} else if (event instanceof BusinessGroupModifiedEvent) {
			processBusinessGroupModifiedEvent((BusinessGroupModifiedEvent)event);
		} else if (event instanceof CourseConfigEvent) {				
			processCourseConfigEvent((CourseConfigEvent)event);
		} else if (event instanceof EntryChangedEvent && ((EntryChangedEvent)event).getChange()!=EntryChangedEvent.MODIFIED_AT_PUBLISH) {
			//courseRepositoryEntry changed (e.g. fired at course access rule change)
			EntryChangedEvent repoEvent = (EntryChangedEvent) event;			
			if (courseRepositoryEntry.getKey().equals(repoEvent.getChangedEntryKey()) && repoEvent.getChange() == EntryChangedEvent.MODIFIED) {				
				doDisposeAfterEvent();
			}
		}
	}
	
	private void processCourseConfigEvent(CourseConfigEvent event) {
		String cmd = event.getCommand();
		System.out.println(cmd);
		
		doDisposeAfterEvent();
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
			 initToolController();
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
				initToolController();
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
	private void initToolController() {
		CourseConfig cc = uce.getCourseEnvironment().getCourseConfig();
		
		initEditionTools();
		initGroupTools();
		initGeneralTools(cc);
	}
	
	private void initEditionTools() {
		// 1) administrative tools
		if (isCourseAdmin || isCourseCoach || hasCourseRight(CourseRights.RIGHT_COURSEEDITOR)
				|| hasCourseRight(CourseRights.RIGHT_GROUPMANAGEMENT) || hasCourseRight(CourseRights.RIGHT_ARCHIVING)
				|| hasCourseRight(CourseRights.RIGHT_STATISTICS) || hasCourseRight(CourseRights.RIGHT_DB)
				|| hasCourseRight(CourseRights.RIGHT_ASSESSMENT)) {

			Dropdown editTools = new Dropdown("editTools", "header.tools", false, getTranslator());
			if (hasCourseRight(CourseRights.RIGHT_COURSEEDITOR) || isCourseAdmin) {
				boolean managed = RepositoryEntryManagedFlag.isManaged(courseRepositoryEntry, RepositoryEntryManagedFlag.editcontent);
				editLink = LinkFactory.createToolLink("edit.cmd", translate("command.openeditor"), this, "o_sel_course_open_editor");
				editLink.setEnabled(!managed);
				editTools.addComponent(editLink);
				editSettingsLink = LinkFactory.createToolLink("settings.cmd", translate("command.settings"), this, "o_sel_course_settings");
				editSettingsLink.setEnabled(!managed);
				editTools.addComponent(editSettingsLink);
				folderLink = LinkFactory.createToolLink("cfd", translate("command.coursefolder"), this);
				folderLink.setEnabled(!managed);
				editTools.addComponent(folderLink);
				areaLink = LinkFactory.createToolLink("careas", translate("command.courseareas"), this, "o_toolbox_courseareas");
				areaLink.setEnabled(!managed);
				editTools.addComponent(areaLink);
			}
			if (hasCourseRight(CourseRights.RIGHT_GROUPMANAGEMENT) || isCourseAdmin) {
				userMgmtLink = LinkFactory.createToolLink("unifiedusermngt", translate("command.opensimplegroupmngt"), this, "o_sel_course_open_membersmgmt");
				editTools.addComponent(userMgmtLink);
			}
			if (hasCourseRight(CourseRights.RIGHT_ARCHIVING) || isCourseAdmin) {
				archiverLink = LinkFactory.createToolLink("archiver", translate("command.openarchiver"), this);
				editTools.addComponent(archiverLink);
			}
			if (hasCourseRight(CourseRights.RIGHT_ASSESSMENT) || isCourseCoach || isCourseAdmin) {
				assessmentLink = LinkFactory.createToolLink("assessment",translate("command.openassessment"), this);
				editTools.addComponent(assessmentLink);
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
					testStatisticLink = LinkFactory.createToolLink("qtistatistic", translate("command.openteststatistic"), this);
					editTools.addComponent(testStatisticLink);
				}
				
				if(surveyNodes.intValue() > 0) {
					surveyStatisticLink = LinkFactory.createToolLink("qtistatistic", translate("command.opensurveystatistic"), this);
					editTools.addComponent(surveyStatisticLink);
				}
			}
			if (hasCourseRight(CourseRights.RIGHT_STATISTICS) || isCourseAdmin) {
				courseStatisticLink = LinkFactory.createToolLink("statistic",translate("command.openstatistic"), this);
				editTools.addComponent(courseStatisticLink);
			}
			if (CourseDBManager.getInstance().isEnabled() && (hasCourseRight(CourseRights.RIGHT_DB) || isCourseAdmin)) {
				dbLink = LinkFactory.createToolLink("customDb",translate("command.opendb"), this);
				editTools.addComponent(dbLink);
			}
			
			all.addTool(editTools);
		}
	}
	
	private void initGroupTools() {
		Dropdown groupTools = new Dropdown("editTools", "header.tools.participatedGroups", false, getTranslator());

		// 2) add coached groups
		if (uce.getCoachedGroups().size() > 0) {
			for (BusinessGroup group:uce.getCoachedGroups()) {
				Link link = LinkFactory.createToolLink(CMD_START_GROUP_PREFIX + group.getKey(), StringHelper.escapeHtml(group.getName()), this);
				groupTools.addComponent(link);
			}
		}

		// 3) add participating groups
		if (uce.getParticipatingGroups().size() > 0) {
			for (BusinessGroup group: uce.getParticipatingGroups()) {
				Link link = LinkFactory.createToolLink(CMD_START_GROUP_PREFIX + group.getKey(), StringHelper.escapeHtml(group.getName()), this);
				groupTools.addComponent(link);
			}
		}

		// 5) add waiting-list groups
		if (uce.getWaitingLists().size() > 0) {
			for (BusinessGroup group:uce.getWaitingLists()) {
				int pos = businessGroupService.getPositionInWaitingListFor(getIdentity(), group);
				String name = StringHelper.escapeHtml(group.getName()) + " (" + pos + ")";
				Link link = LinkFactory.createToolLink(CMD_START_GROUP_PREFIX + group.getKey(), name, this);
				link.setEnabled(false);
				groupTools.addComponent(link);
			}
		}
		
		if(groupTools.size() > 0) {
			all.addTool(groupTools);
		}
	}
	
	private void initGeneralTools(CourseConfig cc) {
		// new toolbox 'general'
		if (cc.isCalendarEnabled() && !isGuest) {
			calendarLink = LinkFactory.createToolLink("calendar",translate("command.calendar"), this);
			calendarLink.setPopup(true);//"950", "750"
			calendarLink.setIconLeftCSS("o_icon o_icon_calendar");
			all.addTool(calendarLink);
		}
		if (cc.hasGlossary()) {
			all.addTool(glossaryToolCtr.getInitialComponent());
		}
		if (showCourseConfigLink) {
			detailsLink = LinkFactory.createToolLink("courseconfig",translate("command.courseconfig"), this);
			all.addTool(detailsLink);
		}
		if (!isGuest) {
			noteLink = LinkFactory.createToolLink("personalnote",translate("command.personalnote"), this);
			noteLink.setPopup(true);//"750", "550"
			all.addTool(noteLink);
		}
		if (offerBookmark && !isGuest) {
			boolean marked = markManager.isMarked(courseRepositoryEntry, getIdentity(), null);
			String css = marked ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE;
			bookmarkLink = LinkFactory.createToolLink("bookmark",translate("command.bookmark"), this);
			bookmarkLink.setIconLeftCSS(css);
			all.addTool(bookmarkLink);
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
			efficiencyStatementsLink = LinkFactory.createToolLink("efficiencystatement",translate("command.efficiencystatement"), this);
			efficiencyStatementsLink.setPopup(true);//"750", "800"
			all.addTool(efficiencyStatementsLink);
			
			UserEfficiencyStatement es = efficiencyStatementManager
					.getUserEfficiencyStatementLight(courseRepositoryEntry.getKey(), getIdentity());
			if (es == null) {
				efficiencyStatementsLink.setEnabled(false);
			}
		}
		
		//add group chat to toolbox
		InstantMessagingModule imModule = CoreSpringFactory.getImpl(InstantMessagingModule.class);
		boolean chatIsEnabled = !isGuest && imModule.isEnabled() && imModule.isCourseEnabled()
				&& CourseModule.isCourseChatEnabled() && cc.isChatEnabled();
		if(chatIsEnabled) {
			chatLink = LinkFactory.createToolLink("chat",translate("command.coursechat"), this);
			all.addTool(chatLink);
		}
		
		if (CourseModule.displayParticipantsCount() && !isGuest) {
			//TODO toolbox
			//addCurrentUserCount(myTool);
		}
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
			currentUserCountLink.setTooltip(getTranslator().translate("participants.in.course.desc"));
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
		Boolean bool = courseRightsCache.get(right);
		return bool.booleanValue();
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
		uce.setGroupMemberships(courseRepositoryEntry, coachedGroups, participatedGroups, waitingLists);
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
			
			// FIXME:fj:b is this needed in some cases?: currentCourseNode = cn;
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
			if (!isInEditor) {
				boolean managed = RepositoryEntryManagedFlag.isManaged(courseRepositoryEntry, RepositoryEntryManagedFlag.editcontent);
				if(!managed) {
					doEdit(ureq);
				}
			} else {
				logDebug("Activate called for editor but editor for course::" + courseRepositoryEntry.getResourceableId() + " is already opened. Reuse current editor instance.",  null);
			}
		}
	}

	public void disableToolController(boolean disable) {
		columnLayoutCtr.hideCol2(disable);
	}

	@Override
	public void setDisposedMsgController(Controller disposeMsgController) {
		super.setDisposedMsgController(disposeMsgController);
	}
}