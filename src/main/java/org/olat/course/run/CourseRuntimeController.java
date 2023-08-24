/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.run;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.commons.calendar.CalendarModule;
import org.olat.commons.info.ui.InfoSecurityCallback;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.controllers.linkchooser.CustomLinkTreeModel;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.commons.modules.bc.FolderRunController;
import org.olat.core.commons.modules.bc.FolderRunController.Mail;
import org.olat.core.commons.modules.glossary.GlossaryMainController;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.Dropdown.Spacer;
import org.olat.core.gui.components.htmlheader.jscss.CustomCSS;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.link.LinkPopupSettings;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel.BreadCrumb;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.stack.VetoPopEvent;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.VetoableCloseController;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.dtabs.DTab;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.gui.util.SyntheticUserRequest;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.OLATSecurityException;
import org.olat.core.logging.activity.CourseLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.PersistingCourseImpl;
import org.olat.course.archiver.ArchiverMainController;
import org.olat.course.archiver.FullAccessArchiverCallback;
import org.olat.course.area.CourseAreasController;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.Status;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.AssessmentModeNotificationEvent;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.ui.mode.AssessmentModeListController;
import org.olat.course.assessment.ui.mode.AssessmentModeSecurityCallback;
import org.olat.course.assessment.ui.mode.AssessmentModeSecurityCallbackFactory;
import org.olat.course.assessment.ui.mode.ChangeAssessmentModeEvent;
import org.olat.course.assessment.ui.tool.AssessmentToolController;
import org.olat.course.assessment.ui.tool.StopAssessmentWarningController;
import org.olat.course.assessment.ui.tool.event.AssessmentModeStatusEvent;
import org.olat.course.assessment.ui.tool.event.CourseNodeEvent;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.ui.CertificateAndEfficiencyStatementController;
import org.olat.course.config.CourseConfig;
import org.olat.course.config.CourseConfigEvent;
import org.olat.course.config.ui.CourseSettingsController;
import org.olat.course.db.CourseDBManager;
import org.olat.course.db.CustomDBMainController;
import org.olat.course.disclaimer.CourseDisclaimerManager;
import org.olat.course.disclaimer.event.CourseDisclaimerEvent;
import org.olat.course.disclaimer.ui.CourseDisclaimerReviewController;
import org.olat.course.editor.EditorMainController;
import org.olat.course.editor.overview.OverviewController;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.groupsandrights.CourseRights;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.learningpath.ui.LearningPathIdentityListController;
import org.olat.course.learningpath.ui.MyLearningPathController;
import org.olat.course.member.MembersManagementMainController;
import org.olat.course.nodeaccess.ui.UnsupportedCourseNodesController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.ENCourseNode;
import org.olat.course.nodes.bc.CoachFolderController;
import org.olat.course.nodes.bc.CoachFolderFactory;
import org.olat.course.nodes.bc.CourseDocumentsController;
import org.olat.course.nodes.bc.CourseDocumentsFactory;
import org.olat.course.nodes.co.COToolController;
import org.olat.course.nodes.feed.blog.BlogToolController;
import org.olat.course.nodes.fo.FOToolController;
import org.olat.course.nodes.info.InfoCourseSecurityCallback;
import org.olat.course.nodes.info.InfoRunController;
import org.olat.course.nodes.members.MembersToolRunController;
import org.olat.course.nodes.wiki.WikiToolController;
import org.olat.course.quota.ui.CourseQuotaUsageController;
import org.olat.course.reminder.CourseProvider;
import org.olat.course.reminder.ui.CourseReminderListController;
import org.olat.course.run.calendar.CourseCalendarController;
import org.olat.course.run.glossary.CourseGlossaryFactory;
import org.olat.course.run.glossary.CourseGlossaryToolLinkController;
import org.olat.course.run.preview.PreviewConfigController;
import org.olat.course.run.tools.CourseTool;
import org.olat.course.run.tools.OpenCourseToolEvent;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.course.statistic.StatisticCourseNodesController;
import org.olat.course.statistic.StatisticMainController;
import org.olat.course.statistic.StatisticType;
import org.olat.course.tree.CourseInternalLinkTreeModel;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupRef;
import org.olat.group.BusinessGroupService;
import org.olat.group.ui.edit.BusinessGroupModifiedEvent;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.OpenInstantMessageEvent;
import org.olat.instantMessaging.ui.ChatViewConfig;
import org.olat.instantMessaging.ui.RosterFormDisplay;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonMeetingDefaultConfiguration;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonRunController;
import org.olat.modules.invitation.InvitationConfigurationPermission;
import org.olat.modules.invitation.InvitationModule;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.modules.lecture.ui.LecturesSecurityCallbackFactory;
import org.olat.modules.lecture.ui.TeacherOverviewController;
import org.olat.modules.openbadges.BadgeEntryConfiguration;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.modules.openbadges.ui.BadgeClassesController;
import org.olat.modules.openbadges.ui.IssuedBadgesController;
import org.olat.modules.reminder.ReminderModule;
import org.olat.modules.teams.ui.TeamsMeetingsRunController;
import org.olat.modules.zoom.ZoomManager;
import org.olat.modules.zoom.ZoomModule;
import org.olat.modules.zoom.ui.ZoomRunController;
import org.olat.note.NoteController;
import org.olat.repository.LeavingStatusList;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.model.SingleRoleRepositoryEntrySecurity.Role;
import org.olat.repository.ui.FakeParticipantStopController;
import org.olat.repository.ui.RepositoryEntryLifeCycleChangeController;
import org.olat.repository.ui.RepositoryEntryRuntimeController;
import org.olat.repository.ui.author.copy.CopyRepositoryEntryWrapperController;
import org.olat.resource.OLATResource;
import org.olat.search.SearchModule;
import org.olat.search.SearchServiceUIFactory;
import org.olat.search.SearchServiceUIFactory.DisplayOption;
import org.olat.search.service.QuickSearchEvent;
import org.olat.search.ui.SearchInputController;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.08.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseRuntimeController extends RepositoryEntryRuntimeController implements VetoableCloseController  {
	
	private static final String JOINED = "joined";
	private static final String LEFT   = "left";
	private static final String CMD_START_GROUP_PREFIX = "cmd.group.start.ident.";
	
	private Delayed delayedClose;
	private boolean listeningAssessmentMode;

	//tools
	private Link folderLink, coachFolderLink,
		assessmentLink, badgesLink, archiverLink,
		courseStatisticLink, surveyStatisticLink, testStatisticLink,
		areaLink, dbLink, convertLearningPathLink,
		//settings
		lecturesAdminLink, reminderLink,
		assessmentModeLink, lifeCycleChangeLink,
		//my course
		efficiencyStatementsLink, issuedBadgesLink, myBadgesLink, noteLink, leaveLink, disclaimerLink,
		// course tools
		learningPathLink, learningPathsLink, calendarLink, chatLink, participantListLink, participantInfoLink,
		blogLink, wikiLink, forumLink, documentsLink, emailLink, searchLink, teamsLink, bigBlueButtonLink, zoomLink,
		//glossary
		openGlossaryLink, enableGlossaryLink, lecturesLink;
	private Link copyWithWizardLink;
	private Link currentUserCountLink;
	private Dropdown myCourse, glossary;

	private CloseableModalController cmc;
	private COToolController emailCtrl;
	private BlogToolController blogCtrl;
	private WikiToolController wikiCtrl;
	private FOToolController forumCtrl;
	private CourseDocumentsController documentsCtrl;
	private CoachFolderController coachFolderCtrl;
	private CourseAreasController areasCtrl;
	private ConfirmLeaveController leaveDialogBox;
	private ArchiverMainController archiverCtrl;
	private CustomDBMainController databasesCtrl;
	private FolderRunController courseFolderCtrl;
	private InfoRunController participatInfoCtrl;
	private TeamsMeetingsRunController teamsCtrl;
	private SearchInputController searchController;
	private StatisticMainController statisticsCtrl;
	private CourseReminderListController remindersCtrl;
	private TeacherOverviewController lecturesCtrl;
	private AssessmentToolController assessmentToolCtr;
	private BadgeClassesController badgeClassesCtrl;
	private IssuedBadgesController myBadgesCtrl;
	private IssuedBadgesController issuedBadgesCtrl;
	private MembersToolRunController participatListCtrl;
	private MembersManagementMainController membersCtrl;
	private StatisticCourseNodesController statsToolCtr;
	private BigBlueButtonRunController bigBlueButtonCtrl;
	private ZoomRunController zoomCtrl;
	private AssessmentModeListController assessmentModeCtrl;
	private StopAssessmentWarningController stopAssessmentCtrl;
	private FakeParticipantStopController fakeParticipantStopCtrl;
	private LectureRepositoryAdminController lecturesAdminCtrl;
	private UnsupportedCourseNodesController unsupportedCourseNodesCtrl;
	private CloseableCalloutWindowController courseSearchCalloutCtr;
	protected RepositoryEntryLifeCycleChangeController lifeCycleChangeCtr;
	private CourseDisclaimerReviewController disclaimeReviewController;
	private CourseQuotaUsageController courseQuotaUsageController;

	private Map<String, Boolean> courseRightsCache;

	@Autowired
	private CourseModule courseModule;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private ReminderModule reminderModule;
	@Autowired
	private CalendarModule calendarModule;
	@Autowired
	private CoordinatorManager coordinatorManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private AssessmentModule assessmentModule;
	@Autowired
	private OpenBadgesManager openBadgesManager;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private AssessmentToolManager assessmentToolManager;
	@Autowired
	private AssessmentModeManager assessmentModeManager;
	@Autowired
	private InstantMessagingModule imModule;
	@Autowired
	private CourseDBManager courseDBManager;
	@Autowired
	private SearchModule searchModule;
	@Autowired
	private InvitationModule invitationModule;
	@Autowired
	private LearningPathService learningPathService;
	@Autowired 
	private CourseDisclaimerManager disclaimerManager;
	@Autowired
	private ZoomModule zoomModule;

	
	public CourseRuntimeController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry re, RepositoryEntrySecurity reSecurity, RuntimeControllerCreator runtimeControllerCreator,
			boolean offerBookmark, boolean showCourseConfigLink) {
		super(ureq, wControl, re, reSecurity, runtimeControllerCreator, offerBookmark, showCourseConfigLink);
		
		if(!corrupted) {
			ICourse course = CourseFactory.loadCourse(getRepositoryEntry());
			addLoggingResourceable(LoggingResourceable.wrap(course));

			coordinatorManager.getCoordinator().getEventBus().registerFor(this, getIdentity(), getOlatResourceable());
			// - group modification events
			coordinatorManager.getCoordinator().getEventBus().registerFor(this, getIdentity(), getRepositoryEntry());
			
			if (courseModule.displayParticipantsCount()) {
				coordinatorManager.getCoordinator().getEventBus().fireEventToListenersOf(new MultiUserEvent(JOINED), getOlatResourceable());
				updateCurrentUserCount();
			}
			
			if(enableGlossaryLink != null) {
				Preferences prefs = ureq.getUserSession().getGuiPreferences();
				String guiPrefsKey = CourseGlossaryFactory.createGuiPrefsKey(course);
				Boolean state = (Boolean) prefs.get(CourseGlossaryToolLinkController.class, guiPrefsKey);
				setGlossaryLinkTitle(ureq, state);
			}
		}
		
		setAssessmentModeMessage(ureq);
	}

	@Override
	protected boolean isCorrupted(RepositoryEntry entry) {
		try {
			CourseFactory.loadCourse(entry);
			return false;
		} catch (Exception e) {
			return true;
		}
	}

	@Override
	protected void onSecurityReloaded(UserRequest ureq) {
		if(corrupted) return;
		
		loadRights();
		
		RunMainController runMainController = getRunMainController();
		if(runMainController != null) {
			runMainController.reloadGroupMemberships(reSecurity);
			runMainController.updateCurrentCourseNode(ureq);
		}
		
		setAssessmentModeMessage(ureq);
		updateFakeParticipantMessage(ureq);
	}

	private void loadRights() {
		ICourse course = CourseFactory.loadCourse(getRepositoryEntry());
		CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
		// 3) all other rights are defined in the groupmanagement using the learning
		// group rights
		UserCourseEnvironmentImpl uce = getUserCourseEnvironment();
		if(uce != null) {
			uce.setUserRoles(reSecurity.isEntryAdmin() || reSecurity.isPrincipal() || reSecurity.isMasterCoach(), reSecurity.isCoach(), reSecurity.isParticipant());
			if(reSecurity.isPrincipal() || reSecurity.isMasterCoach()) {
				uce.setCourseReadOnlyByRole(Boolean.TRUE);
				uce.setCourseReadOnlyByStatus(reSecurity.isReadOnly());
			} else if(reSecurity.isReadOnly()) {
				uce.setCourseReadOnlyByRole(Boolean.FALSE);
				if(overrideReadOnly) {
					uce.setCourseReadOnlyByStatus(Boolean.FALSE);
				} else {
					uce.setCourseReadOnlyByStatus(Boolean.TRUE);
				}
			} else {
				uce.setCourseReadOnlyByRole(Boolean.FALSE);
				uce.setCourseReadOnlyByStatus(Boolean.FALSE);
			}
			uce.getScoreAccounting().evaluateAll(true);
		}
		
		Map<String,Boolean> rightsCache = new HashMap<>();
		Role currentRole = reSecurity.getCurrentRole();
		if((Role.participant == currentRole || Role.coach == currentRole) && !isGuestOnly) {
			GroupRoles role = GroupRoles.valueOf(currentRole.name());
			List<String> rights = cgm.getRights(getIdentity(), role);
			rightsCache.put(CourseRights.RIGHT_GROUPMANAGEMENT, Boolean.valueOf(rights.contains(CourseRights.RIGHT_GROUPMANAGEMENT)));
			rightsCache.put(CourseRights.RIGHT_MEMBERMANAGEMENT, Boolean.valueOf(rights.contains(CourseRights.RIGHT_MEMBERMANAGEMENT)));
			rightsCache.put(CourseRights.RIGHT_COURSEEDITOR, Boolean.valueOf(rights.contains(CourseRights.RIGHT_COURSEEDITOR)));
			rightsCache.put(CourseRights.RIGHT_ARCHIVING, Boolean.valueOf(rights.contains(CourseRights.RIGHT_ARCHIVING)));
			rightsCache.put(CourseRights.RIGHT_ASSESSMENT, Boolean.valueOf(rights.contains(CourseRights.RIGHT_ASSESSMENT)));
			rightsCache.put(CourseRights.RIGHT_ASSESSMENT_MODE, Boolean.valueOf(rights.contains(CourseRights.RIGHT_ASSESSMENT_MODE)));
			rightsCache.put(CourseRights.RIGHT_GLOSSARY, Boolean.valueOf(rights.contains(CourseRights.RIGHT_GLOSSARY)));
			rightsCache.put(CourseRights.RIGHT_STATISTICS, Boolean.valueOf(rights.contains(CourseRights.RIGHT_STATISTICS)));
			rightsCache.put(CourseRights.RIGHT_DB, Boolean.valueOf(rights.contains(CourseRights.RIGHT_DB)));
		}
		courseRightsCache = Map.copyOf(rightsCache);
	}

	private boolean hasCourseRight(String right) {
		Boolean bool = courseRightsCache.get(right);
		return bool != null && bool.booleanValue();
	}
	
	private UserCourseEnvironmentImpl getUserCourseEnvironment() {
		RunMainController run = getRunMainController();
		UserCourseEnvironmentImpl uce = run == null ? null : run.getUce();
		if(uce != null && uce.isCourseReadOnly() && overrideReadOnly) {
			uce.setCourseReadOnlyByStatus(Boolean.FALSE);
		}
		return uce;
	}
	
	/**
	 * Refresh the cached repository entry of the course
	 * @return
	 */
	@Override
	protected RepositoryEntry loadRepositoryEntry() {
		RepositoryEntry refreshedEntry = super.loadRepositoryEntry();
		ICourse course = CourseFactory.loadCourse(getRepositoryEntry());
		course.getCourseEnvironment().updateCourseEntry(refreshedEntry);
		return refreshedEntry;
	}
	
	/**
	 * Refresh the cached repository entry of the course
	 * @return
	 */
	@Override
	protected RepositoryEntry refreshRepositoryEntry(RepositoryEntry refreshedEntry) {
		ICourse course = CourseFactory.loadCourse(refreshedEntry);
		course.getCourseEnvironment().updateCourseEntry(refreshedEntry);
		return super.refreshRepositoryEntry(refreshedEntry);
	}

	private RunMainController getRunMainController() {
		return getRuntimeController() instanceof RunMainController ?
			(RunMainController)getRuntimeController() : null;
	}
	
	private CourseNode getCurrentCourseNode() {
		RunMainController run = getRunMainController();
		return run == null ? null : run.getCurrentCourseNode();
	}
	
	private boolean isInEditor() {
		RunMainController run = getRunMainController();
		return run != null && run.isInEditor();
	}
	
	private void setIsInEditor(boolean editor) {
		RunMainController run = getRunMainController();
		if(run != null) run.setInEditor(editor);
	}
	
	private void reloadGroupMemberships() {
		RunMainController run = getRunMainController();
		if(run != null) {
			run.reloadGroupMemberships(reSecurity);
		}
	}
	
	private void toolControllerDone(UserRequest ureq) {
		RunMainController run = getRunMainController();
		if(run != null) {
			addCustomCSS(ureq);
			run.toolCtrDone(ureq, reSecurity);
			currentToolCtr = null;
		}
	}
	
	private void setTextMarkingEnabled(boolean enable) {
		RunMainController run = getRunMainController();
		if(run != null) run.setTextMarkingEnabled(enable);
	}
	
	public void setCourseCloseEnabled(boolean enabled) {
		toolbarPanel.setShowCloseLink(true, enabled);
	}

	public void setToolControllerEnabled(boolean enabled) {
		toolbarPanel.setToolbarEnabled(enabled);
	}
	
	private void addCustomCSS(UserRequest ureq) {
		ICourse course = CourseFactory.loadCourse(getRepositoryEntry());
		CustomCSS customCSS = CourseFactory.getCustomCourseCss(ureq.getUserSession(), course.getCourseEnvironment());
		ChiefController cc = getWindowControl().getWindowBackOffice().getChiefController();
		if (cc != null) {
			if(customCSS == null) {
				cc.removeCurrentCustomCSSFromView();
			} else {
				cc.addCurrentCustomCSSToView(customCSS);
			}
		}
		setCustomCSS(customCSS);
	}
	
	private void removeCustomCSS() {
		ChiefController cc = getWindowControl().getWindowBackOffice().getChiefController();
		if (cc != null) {
			cc.removeCurrentCustomCSSFromView();
		}
		setCustomCSS(null);
		updateCourseWarningMessage();
	}

	@Override
	protected void initToolbar(Dropdown toolsDropdown) {
		toolsDropdown.removeAllComponents();
		if(corrupted) return;
		
		ICourse course = CourseFactory.loadCourse(getRepositoryEntry());
		if(!course.getCourseConfig().isToolbarEnabled() && !reSecurity.isEntryAdmin() && !reSecurity.isCoach()
				&& !hasCourseRight(CourseRights.RIGHT_COURSEEDITOR) && !hasCourseRight(CourseRights.RIGHT_MEMBERMANAGEMENT)
				&& !hasCourseRight(CourseRights.RIGHT_GROUPMANAGEMENT) && !hasCourseRight(CourseRights.RIGHT_ARCHIVING)
					&& !hasCourseRight(CourseRights.RIGHT_STATISTICS) && !hasCourseRight(CourseRights.RIGHT_DB)
					&& !hasCourseRight(CourseRights.RIGHT_ASSESSMENT) && !hasCourseRight(CourseRights.RIGHT_ASSESSMENT_MODE)) {
			toolbarPanel.setToolbarEnabled(false);
		} else {
			toolbarPanel.setToolbarEnabled(true);
		}
		// make bread crumb disappear if not enabled
		if (!course.getCourseConfig().isBreadCrumbEnabled() && !toolbarPanel.isToolbarEnabled()) {
			// disable if toolbar also not visible
			toolbarPanel.setBreadcrumbEnabled(false);
		}
		
		RunMainController rmc = getRunMainController();
		boolean disclaimerAccepted = rmc != null && rmc.isDisclaimerAccepted();
		
		initToolsMenu(toolsDropdown);
		initToolsMyCourse(course);
		initGeneralTools(course, disclaimerAccepted);
		
		if(rmc != null) {
			rmc.initToolbarAndProgressbar();
		}
		updateCourseWarningMessage();
	}
	
	private void updateCourseWarningMessage() {
		UserCourseEnvironment userCourseEnv = getUserCourseEnvironment();
		if(userCourseEnv != null) {
			if (isFakeParticipantMessageSet()) {
				// Highest priority
			} else if(getRepositoryEntry().getEntryStatus() == RepositoryEntryStatusEnum.closed) {
				toolbarPanel.setMessage(translate("course.closed"));
				toolbarPanel.setMessageCssClass("o_warning");
			} else if(getRepositoryEntry().getEntryStatus() == RepositoryEntryStatusEnum.deleted
					|| getRepositoryEntry().getEntryStatus() == RepositoryEntryStatusEnum.trash) {
				toolbarPanel.setMessage(translate("course.deleted"));
				toolbarPanel.setMessageCssClass("o_warning");
			} else if(!isAssessmentModeMessageSet()) {
				toolbarPanel.setMessage(null);
				toolbarPanel.setMessageComponent(null);
			}
		} else if(!isAssessmentModeMessageSet()) {
			toolbarPanel.setMessage(null);
			toolbarPanel.setMessageComponent(null);
		}
	}
	
	private boolean isFakeParticipantMessageSet() {
		return fakeParticipantStopCtrl != null && fakeParticipantStopCtrl.getInitialComponent() == toolbarPanel.getMessageComponent();
	}
	
	private void updateFakeParticipantMessage(UserRequest ureq) {
		removeAsListenerAndDispose(fakeParticipantStopCtrl);
		fakeParticipantStopCtrl = null;
		
		if (Role.fakeParticipant == reSecurity.getCurrentRole()) {
			fakeParticipantStopCtrl = new FakeParticipantStopController(ureq, getWindowControl(), getRepositoryEntry());
			listenTo(fakeParticipantStopCtrl);
			toolbarPanel.setMessageComponent(fakeParticipantStopCtrl.getInitialComponent());
		} else {
			toolbarPanel.removeMessageComponent();
		}
	}
	
	/**
	 * Is the message in toolbar set with the assessment mode controller?
	 * 
	 * @return true if the message in toolbar is about assessment mode.
	 */
	private boolean isAssessmentModeMessageSet() {
		return stopAssessmentCtrl != null && stopAssessmentCtrl.getInitialComponent() == toolbarPanel.getMessageComponent();
	}
	
	private void setAssessmentModeMessage(UserRequest ureq) {
		UserCourseEnvironment userCourseEnv = getUserCourseEnvironment();
		if(userCourseEnv != null && assessmentLink != null) {
			RepositoryEntry courseEntry = getRepositoryEntry();
			List<AssessmentMode> modes = assessmentModeManager.getCurrentAssessmentMode(courseEntry, new Date());
			if(!modes.isEmpty()) {
				if(stopAssessmentCtrl != null) {
					stopAssessmentCtrl.removeControllerListener(this);
				}
				stopAssessmentCtrl = new StopAssessmentWarningController(ureq, getWindowControl(), toolbarPanel,
						courseEntry, modes, createAssessmentToolSecurityCallback());
				listenTo(stopAssessmentCtrl);
				toolbarPanel.setMessageComponent(stopAssessmentCtrl.getInitialComponent());
			} else {
				toolbarPanel.removeMessageComponent();	
			}
			
			if (assessmentToolCtr != null) {
				assessmentToolCtr.reloadAssessmentModes();
			}
		} else {
			toolbarPanel.removeMessageComponent();
		}
	}
	
	@Override
	protected void initToolsMenu(Dropdown toolsDropdown) {
		toolsDropdown.removeAllComponents();
		toolsDropdown.setDirty(true);
		
		if(!isAssessmentLock()) {
			ICourse course = CourseFactory.loadCourse(getRepositoryEntry());
			UserCourseEnvironmentImpl uce = getUserCourseEnvironment();
			
			initToolsMenuSettings(toolsDropdown);
			initToolsMenuEditor(toolsDropdown, uce);
			initToolsMenuRuntime(toolsDropdown, course, uce);
			initToolsMenuStatistics(toolsDropdown, course, uce);
			initToolsMenuEdition(toolsDropdown);
			initToolsBeta(toolsDropdown, course);
			initToolsMenuDelete(toolsDropdown);
		}
	}
	
	@Override
	protected void initToolsMenuSettings(Dropdown tools) {
		// 1) administrative tools
		if (reSecurity.isEntryAdmin() || reSecurity.isPrincipal() || reSecurity.isMasterCoach() || reSecurity.isCoach()
				|| hasCourseRight(CourseRights.RIGHT_COURSEEDITOR) || hasCourseRight(CourseRights.RIGHT_MEMBERMANAGEMENT)
				|| hasCourseRight(CourseRights.RIGHT_GROUPMANAGEMENT)) {

			tools.setI18nKey("header.tools");
			tools.setElementCssClass("o_sel_course_tools");
			
			if(reSecurity.isEntryAdmin() || hasCourseRight(CourseRights.RIGHT_COURSEEDITOR)) {
				settingsLink = LinkFactory.createToolLink("settings", translate("details.settings"), this, "o_sel_repo_settings");
				settingsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_settings");
				settingsLink.setElementCssClass("o_sel_course_settings");
				settingsLink.setUrl(BusinessControlFactory.getInstance()
						.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[Settings:0]"));
				tools.addComponent(settingsLink);
			}
			
			if (reSecurity.isEntryAdmin() || reSecurity.isPrincipal() || reSecurity.isMasterCoach() || hasCourseRight(CourseRights.RIGHT_GROUPMANAGEMENT) || hasCourseRight(CourseRights.RIGHT_MEMBERMANAGEMENT)) {
				membersLink = LinkFactory.createToolLink("unifiedusermngt", translate("command.opensimplegroupmngt"), this, "o_icon_membersmanagement");
				membersLink.setUrl(BusinessControlFactory.getInstance()
						.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[MembersMgmt:0][Members:0][All:0]"));
				membersLink.setElementCssClass("o_sel_course_members");
				tools.addComponent(membersLink);
			}
			tools.addComponent(new Spacer("first"));
		}
	}
	
	private void initToolsMenuEditor(Dropdown tools, final UserCourseEnvironmentImpl uce) {
		if(uce == null) return;
		
		if (reSecurity.isEntryAdmin() || hasCourseRight(CourseRights.RIGHT_COURSEEDITOR)) {
			boolean managed = RepositoryEntryManagedFlag.isManaged(getRepositoryEntry(), RepositoryEntryManagedFlag.editcontent);
			boolean readOnly = uce.isCourseReadOnly();
			editLink = LinkFactory.createToolLink("edit.cmd", translate("command.openeditor"), this, "o_icon_courseeditor");
			editLink.setUrl(BusinessControlFactory.getInstance()
					.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[Editor:0]]"));
			editLink.setElementCssClass("o_sel_course_editor");
			editLink.setEnabled(!corrupted && !managed);
			editLink.setVisible(!readOnly);
			tools.addComponent(editLink);
		}
			
		if (reSecurity.isEntryAdmin() || reSecurity.isPrincipal() || reSecurity.isMasterCoach() || hasCourseRight(CourseRights.RIGHT_COURSEEDITOR)) {
			folderLink = LinkFactory.createToolLink("cfd", translate("command.coursefolder"), this, "o_icon_coursefolder");
			folderLink.setUrl(BusinessControlFactory.getInstance()
					.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[Coursefolder:0]]"));
			folderLink.setElementCssClass("o_sel_course_folder");
			tools.addComponent(folderLink);
			tools.addComponent(new Spacer(""));
		}
	}
	
	private void initToolsMenuRuntime(Dropdown tools, ICourse course, final UserCourseEnvironmentImpl uce) {
		CourseConfig cc = course.getCourseConfig();
		
		boolean courseAuthorRight = reSecurity.isEntryAdmin() || hasCourseRight(CourseRights.RIGHT_COURSEEDITOR);
		if (courseAuthorRight || reSecurity.isPrincipal() || reSecurity.isMasterCoach() || reSecurity.isCoach()
				|| hasCourseRight(CourseRights.RIGHT_DB)
				|| hasCourseRight(CourseRights.RIGHT_ASSESSMENT) || hasCourseRight(CourseRights.RIGHT_ASSESSMENT_MODE)) {	

			tools.addComponent(new Spacer(""));
			
			if (reSecurity.isEntryAdmin() || reSecurity.isPrincipal() || reSecurity.isMasterCoach() || reSecurity.isCoach() || hasCourseRight(CourseRights.RIGHT_ASSESSMENT)) {
				assessmentLink = LinkFactory.createToolLink("assessment", translate("command.openassessment"), this, "o_icon_assessment_tool");
				assessmentLink.setUrl(BusinessControlFactory.getInstance()
						.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[assessmentToolv2:0]"));
				assessmentLink.setElementCssClass("o_sel_course_assessment_tool");
				tools.addComponent(assessmentLink);
				
				if(!listeningAssessmentMode) {
					coordinatorManager.getCoordinator().getEventBus()
						.registerFor(this, getIdentity(), ChangeAssessmentModeEvent.ASSESSMENT_MODE_ORES);
					coordinatorManager.getCoordinator().getEventBus()
						.registerFor(this, getIdentity(), AssessmentModeNotificationEvent.ASSESSMENT_MODE_NOTIFICATION);
					listeningAssessmentMode = true;
				}
			}

			if (reSecurity.isCoach() || reSecurity.isOwner() || reSecurity.isEntryAdmin()) {
				badgesLink = LinkFactory.createToolLink("badges", translate("command.openbadges"),
						this, "o_icon_badge");
				badgesLink.setUrl(BusinessControlFactory.getInstance()
						.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[Badges:0]"));
				badgesLink.setElementCssClass("o_sel_course_badges");
				tools.addComponent(badgesLink);

				BadgeEntryConfiguration badgeEntryConfiguration = openBadgesManager.getConfiguration(getRepositoryEntry());
				boolean enabled = openBadgesManager.isEnabled() && badgeEntryConfiguration.isAwardEnabled();
				badgesLink.setVisible(false);
				if (enabled && reSecurity.isCoach()) {
					if (badgeEntryConfiguration.isCoachCanAward()) {
						badgesLink.setVisible(true);
					}
				}
				if (enabled && (reSecurity.isOwner() || reSecurity.isEntryAdmin())) {
					badgesLink.setVisible(true);
				}
			}

			if (reSecurity.isEntryAdmin() || reSecurity.isPrincipal() || reSecurity.isMasterCoach() || reSecurity.isCoach() || hasCourseRight(CourseRights.RIGHT_COURSEEDITOR)) {
				coachFolderLink = LinkFactory.createToolLink("coachfolder", translate("command.coachfolder"), this, "o_icon_coursefolder");
				coachFolderLink.setUrl(BusinessControlFactory.getInstance()
						.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[Coachfolder:0]]"));
				coachFolderLink.setElementCssClass("o_sel_course_folder");
				coachFolderLink.setVisible(cc.isCoachFolderEnabled());
				tools.addComponent(coachFolderLink);
			}
			
			if(lectureModule.isEnabled() && (courseAuthorRight || reSecurity.isPrincipal() || reSecurity.isMasterCoach()) && isLectureEnabled()) {
				lecturesAdminLink = LinkFactory.createToolLink("lectures.admin.cmd", translate("command.options.lectures.admin"), this, "o_icon_lecture");
				lecturesAdminLink.setUrl(BusinessControlFactory.getInstance()
						.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[LecturesAdmin:0][LectureBlocks:0]"));
				lecturesAdminLink.setElementCssClass("o_sel_course_lectures_admin");
				tools.addComponent(lecturesAdminLink);
			}
			
			if(reminderModule.isEnabled() && courseAuthorRight) {
				reminderLink = LinkFactory.createToolLink("reminders.cmd", translate("command.options.reminders"), this, "o_icon_reminder");
				reminderLink.setUrl(BusinessControlFactory.getInstance()
						.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[Reminders:0]"));
				reminderLink.setElementCssClass("o_sel_course_reminders");
				reminderLink.setVisible(uce != null && !uce.isCourseReadOnly());
				tools.addComponent(reminderLink);
			}
			
			if (courseAuthorRight || reSecurity.isPrincipal() || reSecurity.isMasterCoach() || hasCourseRight(CourseRights.RIGHT_ASSESSMENT_MODE)) {
				//course author right or the assessment mode access right 
				boolean managed = RepositoryEntryManagedFlag.isManaged(getRepositoryEntry(), RepositoryEntryManagedFlag.editcontent);
				assessmentModeLink = LinkFactory.createToolLink("assessment.mode.cmd", translate("command.assessment.mode"), this, "o_icon_assessment_mode");
				assessmentModeLink.setUrl(BusinessControlFactory.getInstance()
						.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[AssessmentMode:0]"));
				assessmentModeLink.setElementCssClass("o_sel_course_assessment_mode");
				assessmentModeLink.setEnabled(!managed);
				assessmentModeLink.setVisible(assessmentModule.isAssessmentModeEnabled() && uce != null && !uce.isCourseReadOnly());
				tools.addComponent(assessmentModeLink);
			}

			if (reSecurity.isEntryAdmin() || hasCourseRight(CourseRights.RIGHT_COURSEEDITOR)) {
				areaLink = LinkFactory.createToolLink("careas", translate("command.courseareas"), this, "o_icon_courseareas");
				areaLink.setUrl(BusinessControlFactory.getInstance()
						.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[CourseAreas:0]"));
				areaLink.setElementCssClass("o_sel_course_areas");
				tools.addComponent(areaLink);
			}
			
			if (courseDBManager.isEnabled() && (reSecurity.isEntryAdmin() || hasCourseRight(CourseRights.RIGHT_DB))) {
				dbLink = LinkFactory.createToolLink("customDb",translate("command.opendb"), this, "o_icon_coursedb");
				tools.addComponent(dbLink);
			}
		}
	}
	
	private boolean isLectureEnabled() {
		return lectureService.isRepositoryEntryLectureEnabled(getRepositoryEntry());
	}
	
	private void initToolsMenuStatistics(Dropdown tools, ICourse course, final UserCourseEnvironmentImpl uce) {
		if (reSecurity.isEntryAdmin() || reSecurity.isPrincipal() || reSecurity.isMasterCoach() || reSecurity.isCoach()
				|| hasCourseRight(CourseRights.RIGHT_ARCHIVING) || hasCourseRight(CourseRights.RIGHT_STATISTICS)) {	

			tools.addComponent(new Spacer(""));
			
			if (reSecurity.isEntryAdmin() || reSecurity.isPrincipal() || reSecurity.isMasterCoach() || hasCourseRight(CourseRights.RIGHT_STATISTICS)) {
				courseStatisticLink = LinkFactory.createToolLink("statistic",translate("command.openstatistic"), this, "o_icon_statistics_tool");
				courseStatisticLink.setUrl(BusinessControlFactory.getInstance()
						.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[CourseStatistics:0]"));
				tools.addComponent(courseStatisticLink);
			}
			
			if (uce != null && (reSecurity.isEntryAdmin() || reSecurity.isPrincipal() || reSecurity.isMasterCoach() || reSecurity.isCoach() || hasCourseRight(CourseRights.RIGHT_STATISTICS))) {
				final AtomicInteger testNodes = new AtomicInteger();
				final AtomicInteger surveyNodes = new AtomicInteger();
				new TreeVisitor(node -> {
					if(((CourseNode)node).isStatisticNodeResultAvailable(uce, StatisticType.TEST)) {
						testNodes.incrementAndGet();
					} else if(((CourseNode)node).isStatisticNodeResultAvailable(uce, StatisticType.SURVEY)) {
						surveyNodes.incrementAndGet();
					}
				}, course.getRunStructure().getRootNode(), true).visitAll();
				if(testNodes.intValue() > 0) {
					testStatisticLink = LinkFactory.createToolLink("qtistatistic", translate("command.openteststatistic"), this, "o_icon_statistics_tool");
					testStatisticLink.setUrl(BusinessControlFactory.getInstance()
							.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[TestStatistics:0]"));
					tools.addComponent(testStatisticLink);
				}
				
				if(surveyNodes.intValue() > 0) {
					surveyStatisticLink = LinkFactory.createToolLink("qtistatistic", translate("command.opensurveystatistic"), this, "o_icon_statistics_tool");
					surveyStatisticLink.setUrl(BusinessControlFactory.getInstance()
							.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[SurveyStatistics:0]"));
					tools.addComponent(surveyStatisticLink);
				}
			}

			if (reSecurity.isEntryAdmin() || hasCourseRight(CourseRights.RIGHT_ARCHIVING)) {
				archiverLink = LinkFactory.createToolLink("archiver", translate("command.openarchiver"), this, "o_icon_archive_tool");
				archiverLink.setUrl(BusinessControlFactory.getInstance()
						.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[Archives:0]"));
				tools.addComponent(archiverLink);
			}
			
			if (reSecurity.isEntryAdmin() || reSecurity.isPrincipal()) {
				ordersLink = LinkFactory.createToolLink("bookings", translate("details.orders"), this, "o_sel_repo_booking");
				ordersLink.setIconLeftCSS("o_icon o_icon-fw o_icon_booking");
				ordersLink.setElementCssClass("o_sel_course_ac_tool");
				boolean booking = re.isPublicVisible() && acService.isResourceAccessControled(getRepositoryEntry().getOlatResource(), null);
				ordersLink.setVisible(!corrupted && booking);
				tools.addComponent(ordersLink);
			}
		}
	}
	
	@Override
	protected void initToolsMenuEdition(Dropdown toolsDropdown) {
		super.initToolsMenuEdition(toolsDropdown);
		if (copyLink != null) {
			ICourse course = CourseFactory.loadCourse(getRepositoryEntry());
			if (course != null && !LearningPathNodeAccessProvider.TYPE.equals(course.getCourseConfig().getNodeAccessType().getType())) {
				Integer index = toolsDropdown.getComponentIndex(copyLink);
				if(index != null) {
					convertLearningPathLink = LinkFactory.createToolLink("convert.course.learning.path",
							translate("tools.convert.course.learning.path"), this, "o_icon o_icon-fw  o_icon_learning_path");
					toolsDropdown.addComponent(index.intValue() + 1, convertLearningPathLink);
				}
			}
		}
	}
	
	protected void initToolsBeta(Dropdown toolsDropdown, ICourse course) {
		boolean copyManaged = RepositoryEntryManagedFlag.isManaged(re, RepositoryEntryManagedFlag.copy);
		boolean canCopy = (isAuthor || reSecurity.isEntryAdmin()) && (re.getCanCopy() || reSecurity.isEntryAdmin()) && !copyManaged;
		
		if (canCopy && course != null && LearningPathNodeAccessProvider.TYPE.equals(course.getCourseConfig().getNodeAccessType().getType())) {
			Integer index = toolsDropdown.getComponentIndex(copyLink);
			if(index != null) {
				copyWithWizardLink = LinkFactory.createToolLink("copy.course.with.wizard", translate("tools.copy.course.with.wizard"), this, "o_icon o_icon-fw  o_icon_copy");
				toolsDropdown.addComponent(index.intValue() + 1, copyWithWizardLink);
			}
		}
	}

	@Override
	protected void initToolsMenuDelete(Dropdown settingsDropdown) {
		RepositoryEntry re = getRepositoryEntry();
		boolean closeManged = RepositoryEntryManagedFlag.isManaged(re, RepositoryEntryManagedFlag.close);
		
		if(reSecurity.isEntryAdmin()) {
			boolean deleteManaged = RepositoryEntryManagedFlag.isManaged(re, RepositoryEntryManagedFlag.delete);
			if(settingsDropdown.size() > 0 && !deleteManaged) {
				settingsDropdown.addComponent(new Spacer("close-delete"));
			}

			if(!closeManged || !deleteManaged) {
				if(re.getEntryStatus() == RepositoryEntryStatusEnum.deleted || re.getEntryStatus() == RepositoryEntryStatusEnum.trash) {
					restoreLink = LinkFactory.createToolLink("restore", translate("details.restore"), this, "o_icon o_icon-fw o_icon_restore");
					restoreLink.setElementCssClass("o_sel_repo_restore");
					settingsDropdown.addComponent(restoreLink);
				} else {
					// If a resource is closable (currently only course) and
					// deletable (currently all resources) we offer those two
					// actions in a separate page, unless both are managed
					// operations. In that case we don't show anything at all.				
					// If only one of the two actions are managed, we go to the
					// separate page as well and show only the relevant action
					// there.
					lifeCycleChangeLink = LinkFactory.createToolLink("lifeCycleChange", translate("details.lifecycle.change"), this, "o_icon o_icon-fw o_icon_delete_item");
					settingsDropdown.addComponent(lifeCycleChangeLink);
				}
			} else if(!deleteManaged) {
				if(re.getEntryStatus() == RepositoryEntryStatusEnum.deleted || re.getEntryStatus() == RepositoryEntryStatusEnum.trash) {
					restoreLink = LinkFactory.createToolLink("restore", translate("details.restore"), this, "o_icon o_icon-fw o_icon_restore");
					restoreLink.setElementCssClass("o_sel_repo_restore");
					settingsDropdown.addComponent(restoreLink);
				} else {
					String type = translate(handler.getSupportedType());
					String deleteTitle = translate("details.delete.alt", new String[]{ type });
					deleteLink = LinkFactory.createToolLink("delete", deleteTitle, this, "o_icon o_icon-fw o_icon_delete_item");
					deleteLink.setElementCssClass("o_sel_repo_close");
					settingsDropdown.addComponent(deleteLink);
				}
			}
		}
	}

	private void initToolsMyCourse(ICourse course) {
		boolean assessmentLock = isAssessmentLock();
		UserCourseEnvironment userCourseEnv = getUserCourseEnvironment();

		myCourse = new Dropdown("myCourse", "header.tools.mycourse", false, getTranslator());
		myCourse.setElementCssClass("dropdown-menu-right");
		myCourse.setIconCSS("o_icon o_icon_user");

		// Personal tools on right side
		CourseConfig cc = course.getCourseConfig();
		if ((cc.isEfficencyStatementEnabled() || certificatesManager.isCertificateEnabled(getRepositoryEntry())) && !isGuestOnly && !assessmentLock
				&& userCourseEnv != null && userCourseEnv.isParticipant()) {
			efficiencyStatementsLink = LinkFactory.createToolLink("efficiencystatement",
					translate(CourseTool.efficiencystatement.getI18nKey()), this,
					CourseTool.efficiencystatement.getIconCss());
			myCourse.addComponent(efficiencyStatementsLink);
		}

		boolean badgesEnabled = openBadgesManager.isEnabled();
		BadgeEntryConfiguration badgeEntryConfiguration = openBadgesManager.getConfiguration(getRepositoryEntry());

		if (badgesEnabled && badgeEntryConfiguration.isAwardEnabled() && !isGuestOnly && !assessmentLock) {
			if (reSecurity.isEntryAdmin() || reSecurity.isOwner() || (reSecurity.isCoach() && badgeEntryConfiguration.isCoachCanAward())) {
				issuedBadgesLink = LinkFactory.createToolLink("badges", translate(CourseTool.issuedbadges.getI18nKey()),
						this, CourseTool.issuedbadges.getIconCss());
				myCourse.addComponent(issuedBadgesLink);
			}

			if (userCourseEnv.isParticipant()) {
				myBadgesLink = LinkFactory.createToolLink("myBadges", translate(CourseTool.mybadges.getI18nKey()),
						this, CourseTool.mybadges.getIconCss());
				myCourse.addComponent(myBadgesLink);
			}
		}

		if (!isGuestOnly && !assessmentLock) {
			noteLink = LinkFactory.createToolLink("personalnote",translate("command.personalnote"), this, "o_icon_notes");
			noteLink.setPopup(new LinkPopupSettings(750, 550, "notes"));
			myCourse.addComponent(noteLink);
		}
		
		if (allowBookmark && !isGuestOnly) {
			boolean marked = markManager.isMarked(getRepositoryEntry(), getIdentity(), null);
			String css = marked ? Mark.MARK_CSS_ICON : Mark.MARK_ADD_CSS_ICON;
			bookmarkLink = LinkFactory.createToolLink("bookmark",translate("command.bookmark"), this, css);
			bookmarkLink.setTitle(translate(marked ? "details.bookmark.remove" : "details.bookmark"));
			myCourse.addComponent(bookmarkLink);
		}
		
		boolean hasDisclaimer = courseModule.isDisclaimerEnabled() 
				&& cc.isDisclaimerEnabled()
				&& userCourseEnv != null
				&& disclaimerManager.isAccessGranted(getRepositoryEntry(), getIdentity(), userCourseEnv.getIdentityEnvironment().getRoles());
		if (hasDisclaimer) {
			disclaimerLink = LinkFactory.createToolLink("disclaimer",translate("command.disclaimer"), this, "o_icon_disclaimer");
			myCourse.addComponent(disclaimerLink);
		}
		

		if (userCourseEnv != null) {
			if(myCourse.size() > 0 && (!userCourseEnv.getCoachedGroups().isEmpty() || !userCourseEnv.getParticipatingGroups().isEmpty() || !userCourseEnv.getWaitingLists().isEmpty())) {
				myCourse.addComponent(new Spacer(""));
			}
			
			// 2) add coached groups
			if (!userCourseEnv.getCoachedGroups().isEmpty()) {
				for (BusinessGroup group: userCourseEnv.getCoachedGroups()) {
					if(BusinessGroup.BUSINESS_TYPE.equals(group.getTechnicalType())) {
						Link link = LinkFactory.createToolLink(CMD_START_GROUP_PREFIX + group.getKey(), "group", StringHelper.escapeHtml(group.getName()), this);
						link.setIconLeftCSS("o_icon o_icon-fw o_icon_group");
						link.setUserObject(group);
						link.setEnabled(!assessmentLock);
						myCourse.addComponent(link);
					}
					
				}
			}

			// 3) add participating groups
			if (!userCourseEnv.getParticipatingGroups().isEmpty()) {
				for (BusinessGroup group: userCourseEnv.getParticipatingGroups()) {
					if(BusinessGroup.BUSINESS_TYPE.equals(group.getTechnicalType())) {
						Link link = LinkFactory.createToolLink(CMD_START_GROUP_PREFIX + group.getKey(), "group", StringHelper.escapeHtml(group.getName()), this);
						link.setIconLeftCSS("o_icon o_icon-fw o_icon_group");
						link.setUserObject(group);
						link.setEnabled(!assessmentLock);
						myCourse.addComponent(link);
					}
				}
			}

			// 5) add waiting-list groups
			if (!userCourseEnv.getWaitingLists().isEmpty()) {
				for (BusinessGroup group: userCourseEnv.getWaitingLists()) {
					int pos = businessGroupService.getPositionInWaitingListFor(getIdentity(), group);
					String name = StringHelper.escapeHtml(group.getName()) + " (" + pos + ")";
					Link link = LinkFactory.createToolLink(CMD_START_GROUP_PREFIX + group.getKey(), "group", name, this);
					link.setIconLeftCSS("o_icon o_icon-fw o_icon_group");
					link.setUserObject(group);
					link.setEnabled(false);
					myCourse.addComponent(link);
				}
			}
			
			if(repositoryService.isParticipantAllowedToLeave(getRepositoryEntry())
					&& !assessmentLock && !roles.isGuestOnly() && !userCourseEnv.isCourseReadOnly()
					&& isAllowedToLeave(userCourseEnv)) {
				leaveLink = LinkFactory.createToolLink("sign.out", "leave", translate("sign.out"), this);
				leaveLink.setIconLeftCSS("o_icon o_icon-fw o_icon_sign_out");
				myCourse.addComponent(new Spacer("leaving-space"));
				myCourse.addComponent(leaveLink);
			}
		}
			
		if(myCourse.size() > 0) {
			toolbarPanel.addTool(myCourse, Align.right);
		}
	}
	
	private boolean isAllowedToLeave(UserCourseEnvironment userCourseEnv) {
		if(!userCourseEnv.getParticipatingGroups().isEmpty()) {
			CourseNode rootNode = userCourseEnv.getCourseEnvironment().getRunStructure().getRootNode();
			OLATResource courseResource = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseResource();
			
			AtomicBoolean bool = new AtomicBoolean(false);
			new TreeVisitor(node -> {
				if(!bool.get() && node instanceof ENCourseNode) {
					try {
						ENCourseNode enNode = (ENCourseNode)node;
						boolean cancelEnrollEnabled = enNode.getModuleConfiguration().getBooleanSafe(ENCourseNode.CONF_CANCEL_ENROLL_ENABLED);
						if(!cancelEnrollEnabled && enNode.isUsedForEnrollment(userCourseEnv.getParticipatingGroups(), courseResource)) {
							bool.set(true);
						}
					} catch (Exception e) {
						logError("", e);
					}
				}
			}, rootNode, true).visitAll();

			if(bool.get()) {
				return false;// is in a enrollment group
			}
		}
		return (userCourseEnv.isParticipant() || !userCourseEnv.getParticipatingGroups().isEmpty());
	}
	
	private void initGeneralTools(ICourse course, boolean disclaimerAccepted) {
		boolean assessmentLock = isAssessmentLock();
		UserCourseEnvironment userCourseEnv = getUserCourseEnvironment();

		CourseConfig cc = course.getCourseConfig();
		if (!assessmentLock && showDetails) {
			detailsLink = LinkFactory.createToolLink("courseconfig",translate("command.courseconfig"), this, "o_icon_details");
			detailsLink.setUrl(BusinessControlFactory.getInstance()
					.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[Infos:0]"));
			toolbarPanel.addTool(detailsLink);
		}
		
		if (!assessmentLock && !isGuestOnly && disclaimerAccepted
				&& LearningPathNodeAccessProvider.TYPE.equals(cc.getNodeAccessType().getType())) {
			learningPathLink = LinkFactory.createToolLink("learningPath", translate("command.learning.path"), this, CourseTool.learningpath.getIconCss());
			learningPathLink.setUrl(BusinessControlFactory.getInstance()
					.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[LearningPath:0]"));
			toolbarPanel.addTool(learningPathLink);
		}
		if (learningPathLink != null) {
			learningPathLink.setVisible(userCourseEnv != null && userCourseEnv.isParticipant());
		}
		
		if (!assessmentLock && !isGuestOnly && disclaimerAccepted
				&& LearningPathNodeAccessProvider.TYPE.equals(cc.getNodeAccessType().getType())) {
			learningPathsLink = LinkFactory.createToolLink("learningPaths", translate("command.learning.paths"), this, CourseTool.learningpath.getIconCss());
			learningPathsLink.setUrl(BusinessControlFactory.getInstance()
					.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[LearningPaths:0]"));
			toolbarPanel.addTool(learningPathsLink);
		}
		if (learningPathsLink != null) {
			learningPathsLink.setVisible(userCourseEnv != null && (userCourseEnv.isCoach() || userCourseEnv.isAdmin()));
		}
		
		boolean calendarIsEnabled = !assessmentLock && !isGuestOnly && disclaimerAccepted && calendarModule.isEnabled()
				&& calendarModule.isEnableCourseToolCalendar() && reSecurity.canLaunch();
		if (calendarIsEnabled && userCourseEnv != null) {
			calendarLink = LinkFactory.createToolLink("calendar",translate("command.calendar"), this, "o_icon_calendar");
			calendarLink.setPopup(new LinkPopupSettings(950, 750, "cal"));
			calendarLink.setUrl(BusinessControlFactory.getInstance()
					.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[Calendar:0]"));
			calendarLink.setVisible(cc.isCalendarEnabled());
			toolbarPanel.addTool(calendarLink);
		}
		
		if(!assessmentLock && disclaimerAccepted && isLecturesLinkEnabled()) {
			lecturesLink = LinkFactory.createToolLink("command.lectures", translate("command.lectures"), this, "o_icon_lecture");
			lecturesLink.setUrl(BusinessControlFactory.getInstance()
					.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[Lectures:0]"));
			toolbarPanel.addTool(lecturesLink);
		}
		
		if(!assessmentLock && !isGuestOnly && disclaimerAccepted && userCourseEnv != null) {
			participantListLink = LinkFactory.createToolLink("participantlist",
					translate(CourseTool.participantlist.getI18nKey()), this,
					CourseTool.participantlist.getIconCss());
			participantListLink.setUrl(BusinessControlFactory.getInstance()
					.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[ParticipantList:0]"));
			participantListLink.setVisible(cc.isParticipantListEnabled());
			toolbarPanel.addTool(participantListLink);
		}
		
		if(!assessmentLock && disclaimerAccepted && userCourseEnv != null) {
			participantInfoLink = LinkFactory.createToolLink("participantinfo",
					translate(CourseTool.participantinfos.getI18nKey()), this,
					CourseTool.participantinfos.getIconCss());
			participantInfoLink.setUrl(BusinessControlFactory.getInstance()
					.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[ParticipantInfos:0]"));
			participantInfoLink.setVisible(cc.isParticipantInfoEnabled());
			toolbarPanel.addTool(participantInfoLink);
		}
		
		if(!assessmentLock && !isGuestOnly && disclaimerAccepted && userCourseEnv != null && !userCourseEnv.isCourseReadOnly()) {
			emailLink = LinkFactory.createToolLink("email", translate(CourseTool.email.getI18nKey()), this, CourseTool.email.getIconCss());
			emailLink.setUrl(BusinessControlFactory.getInstance()
					.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[email:0]"));
			emailLink.setVisible(cc.isEmailEnabled());
			toolbarPanel.addTool(emailLink);
		}
		
		if(!assessmentLock && disclaimerAccepted && userCourseEnv != null) {
			teamsLink = LinkFactory.createToolLink("teams", translate(CourseTool.teams.getI18nKey()), this, CourseTool.teams.getIconCss());
			teamsLink.setUrl(BusinessControlFactory.getInstance()
					.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[teams:0]"));
			teamsLink.setVisible(cc.isTeamsEnabled());
			toolbarPanel.addTool(teamsLink);
		}
		
		if(!assessmentLock && disclaimerAccepted && userCourseEnv != null) {
			bigBlueButtonLink = LinkFactory.createToolLink("bigbluebutton", translate(CourseTool.bigbluebutton.getI18nKey()), this, CourseTool.bigbluebutton.getIconCss());
			bigBlueButtonLink.setUrl(BusinessControlFactory.getInstance()
					.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[bigbluebutton:0]"));
			bigBlueButtonLink.setVisible(cc.isBigBlueButtonEnabled());
			toolbarPanel.addTool(bigBlueButtonLink);
		}

		if (!assessmentLock && disclaimerAccepted && userCourseEnv != null) {
			zoomLink = LinkFactory.createToolLink("zoom", translate(CourseTool.zoom.getI18nKey()), this, CourseTool.zoom.getIconCss());
			zoomLink.setUrl(BusinessControlFactory.getInstance()
					.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[zoom:0]"));
			zoomLink.setVisible(cc.isZoomEnabled() && zoomModule.isEnabled() && zoomModule.isEnabledForCourseTool());
			toolbarPanel.addTool(zoomLink);
		}

		if(!assessmentLock && disclaimerAccepted && userCourseEnv != null) {
			blogLink = LinkFactory.createToolLink("blog", translate(CourseTool.blog.getI18nKey()), this, CourseTool.blog.getIconCss());
			blogLink.setUrl(BusinessControlFactory.getInstance()
					.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[blog:0]"));
			blogLink.setVisible(cc.isBlogEnabled());
			toolbarPanel.addTool(blogLink);
		}
		
		if(!assessmentLock && disclaimerAccepted && userCourseEnv != null) {
			wikiLink = LinkFactory.createToolLink("wiki", translate(CourseTool.wiki.getI18nKey()), this, CourseTool.wiki.getIconCss());
			wikiLink.setUrl(BusinessControlFactory.getInstance()
					.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[wiki:0]"));
			wikiLink.setVisible(cc.isWikiEnabled());
			toolbarPanel.addTool(wikiLink);
		}
		
		if(!assessmentLock && disclaimerAccepted && userCourseEnv != null) {
			forumLink = LinkFactory.createToolLink("forum", translate(CourseTool.forum.getI18nKey()), this, CourseTool.forum.getIconCss());
			forumLink.setUrl(BusinessControlFactory.getInstance()
					.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[forum:0]"));
			forumLink.setVisible(cc.isForumEnabled());
			toolbarPanel.addTool(forumLink);
		}
		
		if(!assessmentLock && disclaimerAccepted && userCourseEnv != null) {
			documentsLink = LinkFactory.createToolLink("documents", translate(CourseTool.documents.getI18nKey()), this, CourseTool.documents.getIconCss());
			documentsLink.setUrl(BusinessControlFactory.getInstance()
					.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[documents:0]"));
			documentsLink.setVisible(cc.isDocumentsEnabled());
			toolbarPanel.addTool(documentsLink);
		}
		
		if(!assessmentLock && disclaimerAccepted) {
			glossary = new Dropdown("glossary", "command.glossary", false, getTranslator());
			glossary.setIconCSS("o_icon o_FileResource-GLOSSARY_icon");
			glossary.setVisible(cc.hasGlossary() && cc.isGlossaryEnabled());
	
			openGlossaryLink = LinkFactory.createToolLink("command.glossary.open", translate("command.glossary.open"), this);
			openGlossaryLink.setPopup(new LinkPopupSettings(950, 750, "gloss"));
			glossary.addComponent(openGlossaryLink);
	
			enableGlossaryLink = LinkFactory.createToolLink("command.glossary.on.off", translate("command.glossary.on.alt"), this);
			glossary.addComponent(enableGlossaryLink);
			toolbarPanel.addTool(glossary);
		}
		
		//add group chat to toolbox
		boolean chatIsEnabled = !assessmentLock && !isGuestOnly && disclaimerAccepted && imModule.isEnabled()
				&& imModule.isCourseEnabled() && reSecurity.canLaunch();
		if(chatIsEnabled && userCourseEnv != null && !userCourseEnv.isCourseReadOnly()) {
			chatLink = LinkFactory.createToolLink("chat",translate("command.coursechat"), this, "o_icon_chat");
			chatLink.setVisible(imModule.isCourseEnabled() && cc.isChatEnabled());
			toolbarPanel.addTool(chatLink);
		}
		
		// add course search to toolbox 
		boolean isSearchEnabled = !assessmentLock && disclaimerAccepted && searchModule.isSearchAllowed(roles);
		if (isSearchEnabled) {
			searchLink = LinkFactory.createToolLink("coursesearch", translate("command.coursesearch"), this, "o_icon_search");
			searchLink.setVisible(cc.isCourseSearchEnabled());
			toolbarPanel.addTool(searchLink);
		}
	}

	//check the configuration enable the lectures and the user is a teacher 
	private boolean isLecturesLinkEnabled() {
		if(lectureModule.isEnabled()) {
			if(reSecurity.isEntryAdmin()) {
				return lectureService.isRepositoryEntryLectureEnabled(getRepositoryEntry());
			} else {
				//check the configuration enable the lectures and the user is a teacher 
				return lectureService.hasLecturesAsTeacher(getRepositoryEntry(), getIdentity());
			}
		}
		return false;
	}

	@Override
	public void setActiveTool(Link tool) {
		if(myCourse != null) {
			myCourse.setActiveLink(tool);
		}
		super.setActiveTool(tool);
	}

	@Override
	protected void doDispose() {
		super.doDispose();
		
		if (courseModule.displayParticipantsCount()) {
			coordinatorManager.getCoordinator().getEventBus().fireEventToListenersOf(new MultiUserEvent(LEFT), getOlatResourceable());
		}
		
		if(listeningAssessmentMode) {
			coordinatorManager.getCoordinator().getEventBus().deregisterFor(this, ChangeAssessmentModeEvent.ASSESSMENT_MODE_ORES);
			coordinatorManager.getCoordinator().getEventBus().deregisterFor(this, AssessmentModeNotificationEvent.ASSESSMENT_MODE_NOTIFICATION);
		}
	}
	
	private void doDisclaimerAccept() {
		initToolbar();
		ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURES_DISCLAIMER_ACCEPTED, getClass());
	}
	private void doDisclaimerReject(UserRequest ureq) {
		doClose(ureq);
		cleanUp();
		showInfo("msg.disclaimer.denied");			
		ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURES_DISCLAIMER_REJECTED, getClass());
	}

	@Override
	public boolean requestForClose(UserRequest ureq) {
		if(editorCtrl instanceof VetoableCloseController) {
			return ((VetoableCloseController) editorCtrl).requestForClose(ureq);
		}
		return true;
	}

	@Override
	public void event(Event event) {
		if(event instanceof CourseConfigEvent) {				
			processCourseConfigEvent((CourseConfigEvent)event);
		} else if (event instanceof EntryChangedEvent ) {
			EntryChangedEvent repoEvent = (EntryChangedEvent) event;
			if (repoEvent.isMe(getRepositoryEntry())) {
				processEntryChangedEvent(repoEvent);
			}
		//All events are MultiUserEvent, check with command at the end
		} else if(event instanceof ChangeAssessmentModeEvent) {
			processChangeAssessmentModeEvents((ChangeAssessmentModeEvent)event);
		} else if(event instanceof AssessmentModeNotificationEvent) {
			processChangeAssessmentModeEvents((AssessmentModeNotificationEvent)event);
		} else if (event instanceof MultiUserEvent) {
			if (event.getCommand().equals(JOINED) || event.getCommand().equals(LEFT)) {
				updateCurrentUserCount();
			}
		}
		super.event(event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(assessmentModeLink == source) {
			doAssessmentMode(ureq);
		} else if (lifeCycleChangeLink == source) {
			doLifeCycleChange(ureq);
		} else if(reminderLink == source) {
			doReminders(ureq);
		} else if(lecturesAdminLink == source) {
			doLecturesAdmin(ureq);
		} else if(lecturesLink == source) {
			doLectures(ureq);
		} else if(archiverLink == source) {
			doArchive(ureq);
		} else if(folderLink == source) {
			doCourseFolder(ureq);
		} else if(coachFolderLink == source) {
			doCoachFolder(ureq);
		} else if(areaLink == source) {
			doCourseAreas(ureq);
		} else if(dbLink == source) {
			doDatabases(ureq);
		} else if(courseStatisticLink == source) {
			doCourseStatistics(ureq);
		} else if(testStatisticLink == source) {
			doAssessmentTestStatistics(ureq);
		} else if(surveyStatisticLink == source) {
			doAssessmentSurveyStatistics(ureq);
		} else if(assessmentLink == source) {
			doAssessmentTool(ureq);
		} else if (badgesLink == source) {
			doBadges(ureq);
		} else if (convertLearningPathLink == source) {
			doConvertToLearningPath(ureq);
		} else if(participantListLink == source) {
			doParticipantList(ureq);
		} else if(participantInfoLink == source) {
			doParticipantInfo(ureq);
		} else if(emailLink == source) {
			doEmail(ureq);
		} else if(teamsLink == source) {
			doTeams(ureq);
		} else if(bigBlueButtonLink == source) {
			doBigBlueButton(ureq);
		} else if (zoomLink == source) {
			doZoom(ureq);
		} else if(blogLink == source) {
			doBlog(ureq);
		} else if(wikiLink == source) {
			doWiki(ureq);
		} else if(forumLink == source) {
			doForum(ureq);
		} else if(documentsLink == source) {
			doDocuments(ureq);
		} else if(learningPathLink == source) {
			doLearningPath(ureq);
		} else if(learningPathsLink == source) {
			doLearningPaths(ureq);
		} else if(calendarLink == source) {
			launchCalendar(ureq);
		} else if(chatLink == source) {
			launchChat(ureq);
		} else if(searchLink == source) {
			launchCourseSearch(ureq);
		} else if(efficiencyStatementsLink == source) {
			doEfficiencyStatements(ureq);
		} else if(myBadgesLink == source) {
			doMyBadges(ureq);
		} else if(issuedBadgesLink == source) {
			doIssuedBadges(ureq);
		} else if(noteLink == source) {
			launchPersonalNotes(ureq);
		} else if(disclaimerLink == source) {
			showDisclaimer(ureq);
		} else if(openGlossaryLink == source) {
			launchGlossary(ureq);
		} else if(leaveLink == source) {
			doConfirmLeave(ureq);
		} else if(copyWithWizardLink == source) {
			doCopyWithWizard(ureq);
		} else if(source instanceof Link && "group".equals(((Link)source).getCommand())) {
			BusinessGroupRef ref = (BusinessGroupRef)((Link)source).getUserObject();
			launchGroup(ureq, ref.getKey());
		} else if(source == toolbarPanel) {
			if(event instanceof VetoPopEvent) {
				delayedClose = Delayed.pop;
			} else if(event instanceof PopEvent) {
				processPopEvent(ureq, (PopEvent)event);
			}
		} else if(enableGlossaryLink == source) {
			toggleGlossary(ureq);
		}
		
		// Update window title
		if (source instanceof Link) {
			Link link = (Link) source;
			ICourse course = CourseFactory.loadCourse(getRepositoryEntry());
			String newTitle = course.getCourseTitle() + " - " + link.getI18n();
			getWindowControl().getWindowBackOffice().getWindow().setTitle(getTranslator(), newTitle);						
		}
		
		super.event(ureq, source, event);
	}
	
	@Override
	protected void processPopEvent(UserRequest ureq, PopEvent pop) {
		super.processPopEvent(ureq, pop);
		
		Controller popedController = pop.getController();
		if(popedController != null && popedController == membersCtrl) {
			// The user maybe has changed his own membership.
			// Reload the security to ensure the right user switch roles etc.
			reloadSecurity(ureq);
		}
		if(popedController != null && popedController == assessmentToolCtr) {
			updateCourseWarningMessage();
		}
		if(popedController != null && (popedController == editorCtrl || popedController == settingsCtrl)) {
			loadRepositoryEntry();
			reloadStatus();
		}
		
		if(popedController != getRunMainController()
				&& !(popedController instanceof PreviewConfigController)
				&& !(popedController instanceof OverviewController)) {
			toolControllerDone(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(getRunMainController() == source) {
			if(event instanceof BusinessGroupModifiedEvent) {
				processBusinessGroupModifiedEvent(ureq, (BusinessGroupModifiedEvent)event);
			} else if (event instanceof OpenCourseToolEvent) {
				CourseTool tool = ((OpenCourseToolEvent)event).getTool();
				doOpenTool(ureq, tool);
			}
		} else if (lifeCycleChangeCtr == source) {
			if (event == RepositoryEntryLifeCycleChangeController.deletedEvent) {
				doClose(ureq);
				cleanUp();	
			}
		} else if (courseFolderCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doQuotaUsageView(ureq);
			}
		} else if (currentToolCtr == source) {
			if (event == Event.DONE_EVENT) {
				// special check for editor
				toolControllerDone(ureq);
			}
		}  else if(source == leaveDialogBox) {
			if (event.equals(Event.DONE_EVENT)) {
				doLeave(ureq);
			} else{
				cmc.deactivate();
			}
		} else if (source == searchController) {
			if (QuickSearchEvent.QUICKSEARCH.equals(event.getCommand())) {
				doDeactivateQuickSearch();
			}
		} else if (source == unsupportedCourseNodesCtrl) {
			cmc.deactivate();
			cleanUp();
		} else if (source == fakeParticipantStopCtrl) {
			if (event == FakeParticipantStopController.STOP_EVENT) {
				doSwitchRole(ureq, reSecurity.getPreviousRole());
			} else if (event == FakeParticipantStopController.RESET_EVENT) {
				doReloadCurrentCourseNode(ureq);
			}
		} else if (event instanceof CourseNodeEvent cne) {
			if (cne.getIdent().contains(PersistingCourseImpl.COURSEFOLDER)) {
				doCourseFolder(ureq);
			} else if (cne.getIdent().contains(CoachFolderFactory.FOLDER_NAME)) {
				doCoachFolder(ureq);
			} else if (cne.getIdent().contains(CourseDocumentsFactory.FOLDER_NAME)) {
				doDocuments(ureq);
			}
		}
		
		if (event.equals(CourseDisclaimerEvent.ACCEPTED)) {
			doDisclaimerAccept();
		} else if (event.equals(CourseDisclaimerEvent.REJECTED)) {
			doDisclaimerReject(ureq);
		}
		
		if (event instanceof AssessmentModeStatusEvent) {
			setAssessmentModeMessage(ureq);
		} else if (event == RunMainController.RELOAD_COURSE_NODE) {
			doReloadCurrentCourseNode(ureq);
		} else if (event instanceof GoToEvent gte) {
			doGoTo(ureq, gte);
		}
		
		if(editorCtrl == source && source instanceof VetoableCloseController) {
			if(event == Event.DONE_EVENT) {
				if(delayedClose != null) {
					switch(delayedClose) {
						case archive: doArchive(ureq); break;
						case assessmentMode: doAssessmentMode(ureq); break;
						case assessmentSurveyStatistics: doAssessmentSurveyStatistics(ureq); break;
						case assessmentTestStatistics: doAssessmentTestStatistics(ureq); break;
						case assessmentTool: doAssessmentTool(ureq); break;
						case badges: doBadges(ureq); break;
						case reminders: doReminders(ureq); break;
						case learningPath: doLearningPath(ureq); break;
						case learningPaths: doLearningPaths(ureq); break;
						case lecturesAdmin: doLecturesAdmin(ureq); break;
						case lectures: doLectures(ureq); break;
						case courseAreas: doCourseAreas(ureq); break;
						case courseFolder: doCourseFolder(ureq); break;
						case coachFolder: doCoachFolder(ureq); break;
						case courseStatistics: doCourseStatistics(ureq); break;
						case databases: doDatabases(ureq); break;
						case details: doDetails(ureq); break;
						case settings: doSettings(ureq, null); break;
						case efficiencyStatements: doEfficiencyStatements(ureq); break;
						case members: doMembers(ureq); break;
						case orders: doOrders(ureq); break;
						case close: doClose(ureq); break;
						case pop: popToRoot(ureq); cleanUp(); break;
						case participantList: doParticipantList(ureq); break;
						case participantInfo: doParticipantInfo(ureq); break;
						case email: doEmail(ureq); break;
						case blog: doBlog(ureq); break;
						case wiki: doWiki(ureq); break;
						case forum: doForum(ureq); break;
						case documents: doDocuments(ureq); break;
						case bigbluebutton: doBigBlueButton(ureq); break;
						case teams: doTeams(ureq); break;
						case zoom: doZoom(ureq); break;
						case myBadges: doMyBadges(ureq); break;
						case issuedBadges: doIssuedBadges(ureq); break;
					}
					delayedClose = null;
				} else {
					fireEvent(ureq, Event.DONE_EVENT);
				}
			}
		} 
		
		super.event(ureq, source, event);
	}

	@Override
	protected void cleanUp() {
		removeAsListenerAndDispose(unsupportedCourseNodesCtrl);
		removeAsListenerAndDispose(lifeCycleChangeCtr);
		removeAsListenerAndDispose(assessmentModeCtrl);
		removeAsListenerAndDispose(lecturesAdminCtrl);
		removeAsListenerAndDispose(assessmentToolCtr);
		removeAsListenerAndDispose(badgeClassesCtrl);
		removeAsListenerAndDispose(courseFolderCtrl);
		removeAsListenerAndDispose(statisticsCtrl);
		removeAsListenerAndDispose(remindersCtrl);
		removeAsListenerAndDispose(databasesCtrl);
		removeAsListenerAndDispose(lecturesCtrl);
		removeAsListenerAndDispose(archiverCtrl);
		removeAsListenerAndDispose(statsToolCtr);
		removeAsListenerAndDispose(membersCtrl);
		removeAsListenerAndDispose(areasCtrl);
		removeAsListenerAndDispose(leaveDialogBox);
		unsupportedCourseNodesCtrl = null;
		lifeCycleChangeCtr = null;
		assessmentModeCtrl = null;
		lecturesAdminCtrl = null;
		assessmentToolCtr = null;
		badgeClassesCtrl = null;
		courseFolderCtrl = null;
		statisticsCtrl = null;
		remindersCtrl = null;
		databasesCtrl = null;
		lecturesCtrl = null;
		archiverCtrl = null;
		statsToolCtr = null;
		membersCtrl = null;
		areasCtrl = null;

		super.cleanUp();
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			if(currentToolCtr != null) {
				addToHistory(ureq, currentToolCtr);
			} else {
				Controller runtimeCtrl = getRuntimeController();
				if(runtimeCtrl instanceof Activateable2) {
					((Activateable2)runtimeCtrl).activate(ureq, entries, state);
				} else {
					addToHistory(ureq, runtimeCtrl);
				}
			}
			return;
		}

		entries = removeRepositoryEntry(entries);
		if(entries != null && !entries.isEmpty()) {
			String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if("Payment".equalsIgnoreCase(type)) {
				doPostSuccessfullAccess(ureq);
			} else if("Editor".equalsIgnoreCase(type)) {
				if (!isInEditor() && !RepositoryEntryManagedFlag.isManaged(getRepositoryEntry(), RepositoryEntryManagedFlag.editcontent)) {
					doEdit(ureq);
				}
			} else if("Infos".equalsIgnoreCase(type)) {
				doDetails(ureq);
			} else if("LearningPath".equalsIgnoreCase(type)) {
				if (learningPathLink != null && learningPathLink.isVisible()) {
					doLearningPath(ureq);
				}
			} else if("LearningPaths".equalsIgnoreCase(type)) {
				if (learningPathsLink != null && learningPathsLink.isVisible()) {
					activateSubEntries(ureq, doLearningPaths(ureq), entries);
				}
			} else if("Settings".equalsIgnoreCase(type) || "EditDescription".equalsIgnoreCase(type)) {
				List<ContextEntry> subEntries = entries.subList(1, entries.size());
				doSettings(ureq, subEntries);
			} else if("ParticipantList".equalsIgnoreCase(type)) {
				if (participantListLink != null && participantListLink.isVisible()) {
					doParticipantList(ureq);
				}
			} else if("ParticipantInfos".equalsIgnoreCase(type)) {
				if (participantInfoLink != null && participantInfoLink.isVisible()) {
					doParticipantInfo(ureq);
				}
			} else if("Email".equalsIgnoreCase(type)) {
				if (emailLink != null && emailLink.isVisible()) {
					doEmail(ureq);
				}
			} else if("Blog".equalsIgnoreCase(type)) {
				if (blogLink != null && blogLink.isVisible()) {
					activateSubEntries(ureq, doBlog(ureq), entries);
				}
			} else if("Teams".equalsIgnoreCase(type)) {
				if (teamsLink != null && teamsLink.isVisible()) {
					activateSubEntries(ureq, doTeams(ureq), entries);
				}
			} else if ("Zoom".equalsIgnoreCase(type)) {
				if (zoomLink != null && zoomLink.isVisible()) {
					activateSubEntries(ureq, doZoom(ureq), entries);
				}
			} else if("BigBlueButton".equalsIgnoreCase(type)) {
				if (bigBlueButtonLink != null && bigBlueButtonLink.isVisible()) {
					activateSubEntries(ureq, doBigBlueButton(ureq), entries);
				}
			} else if("Meeting".equalsIgnoreCase(type)) {
				doActivateMeeting(ureq, entries);
			} else if("Wiki".equalsIgnoreCase(type)) {
				if (wikiLink != null && wikiLink.isVisible()) {
					activateSubEntries(ureq, doWiki(ureq), entries);
				}
			} else if("Forum".equalsIgnoreCase(type)) {
				if (forumLink != null && forumLink.isVisible()) {
					doForum(ureq);
				}
			} else if("Calendar".equalsIgnoreCase(type)) {
				if (calendarLink != null && calendarLink.isVisible()) {
					doCalendar(ureq);
				}
			} else if("Documents".equalsIgnoreCase(type)) {
				if (documentsLink != null && documentsLink.isVisible()) {
					activateSubEntries(ureq, doDocuments(ureq), entries);
				}
			} else if("Certification".equalsIgnoreCase(type)) {
				if (efficiencyStatementsLink != null && efficiencyStatementsLink.isVisible()) {
					doEfficiencyStatements(ureq);
				}
			} else if("Reminders".equalsIgnoreCase(type)) {
				if (reminderLink != null && reminderLink.isVisible()) {
					activateSubEntries(ureq, doReminders(ureq), entries);
				}
			} else if("Lectures".equalsIgnoreCase(type)) {
				activateSubEntries(ureq, doLectures(ureq), entries);
			} else if("LectureBlock".equalsIgnoreCase(type)) {
				Activateable2 lectures = doLectures(ureq);
				if(lectures != null) {
					lectures.activate(ureq, entries, state);
				}
			} else if("LecturesAdmin".equalsIgnoreCase(type)) {
				activateSubEntries(ureq, doLecturesAdmin(ureq), entries);
			} else if("AssessmentMode".equalsIgnoreCase(type)) {
				doAssessmentMode(ureq);
			} else if("CourseAreas".equalsIgnoreCase(type)) {
				doCourseAreas(ureq);
			} else if("CourseStatistics".equalsIgnoreCase(type)) {
				activateSubEntries(ureq, doCourseStatistics(ureq), entries);
			} else if("Archives".equalsIgnoreCase(type)) {
				activateSubEntries(ureq, doArchive(ureq), entries);
			} else if("MembersMgmt".equalsIgnoreCase(type)) {
				activateSubEntries(ureq, doMembers(ureq), entries);	
			} else if ("assessmentTool".equalsIgnoreCase(type) || "assessmentToolv2".equalsIgnoreCase(type)) {
				//check the security before, the link is perhaps in the wrong hands
				if (reSecurity.isEntryAdmin() || reSecurity.isPrincipal() || reSecurity.isMasterCoach() || reSecurity.isCoach() || hasCourseRight(CourseRights.RIGHT_ASSESSMENT)) {
					try {
						Activateable2 assessmentCtrl = doAssessmentTool(ureq);
						if (assessmentCtrl != null) {
							List<ContextEntry> subEntries;
							if (entries.size() > 1 && entries.get(1).getOLATResourceable().getResourceableTypeName().equals(type)) {
								subEntries = entries.subList(2, entries.size());
							} else {
								subEntries = entries.subList(1, entries.size());
							}
							assessmentCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
						}
					} catch (OLATSecurityException e) {
						//the wrong link to the wrong person
					}
				}
			} else if ("Badges".equalsIgnoreCase(type)) {
				if (badgesLink != null && badgesLink.isVisible()) {
					activateSubEntries(ureq, doBadges(ureq), entries);
				}
			} else if ("MyBadges".equalsIgnoreCase(type)) {
				doMyBadges(ureq);
			} else if ("IssuedBadges".equalsIgnoreCase(type)) {
				doIssuedBadges(ureq);
			} else if ("TestStatistics".equalsIgnoreCase(type) || "SurveyStatistics".equalsIgnoreCase(type)) {
				//check the security before, the link is perhaps in the wrong hands
				if(reSecurity.isEntryAdmin() || reSecurity.isPrincipal() || reSecurity.isMasterCoach() || reSecurity.isCoach() || hasCourseRight(CourseRights.RIGHT_ASSESSMENT)) {
					try {
						Activateable2 assessmentCtrl = null;
						if("TestStatistics".equalsIgnoreCase(type)) {
							assessmentCtrl = doAssessmentTestStatistics(ureq);
						} else {
							assessmentCtrl = doAssessmentSurveyStatistics(ureq);
						}
						
						List<ContextEntry> subEntries;
						if(entries.size() > 1 && entries.get(1).getOLATResourceable().getResourceableTypeName().equals(type)) {
							subEntries = entries.subList(2, entries.size());
						} else {
							subEntries = entries.subList(1, entries.size());
						}
						if(assessmentCtrl != null) {
							assessmentCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
						}
					} catch (OLATSecurityException e) {
						//the wrong link to the wrong person
					}
				}
			} else if("CourseFolder".equalsIgnoreCase(type)) {
				FolderRunController folderCtrl = doCourseFolder(ureq);
				if(folderCtrl != null && entries.size() > 1) {
					folderCtrl.activatePath(ureq, BusinessControlFactory.getInstance().getPath(entries.get(1)));
				}
			} else if("CoachFolder".equalsIgnoreCase(type)) {
				if (coachFolderLink != null && coachFolderLink.isVisible()) {
					activateSubEntries(ureq, doCoachFolder(ureq), entries);
				}
			} else if(type != null && type.startsWith("path=")) {
				FolderRunController folderCtrl = doCourseFolder(ureq);
				if(folderCtrl != null) {
					folderCtrl.activatePath(ureq, BusinessControlFactory.getInstance().getPath(entries.get(0)));
				}
			} else if("CourseNode".equals(type)) {
				// free to stack for the run main controller
				if(currentToolCtr != null) {
					toolbarPanel.popController(currentToolCtr);
					toolControllerDone(ureq);
				}
			}
		}

		RunMainController rmc = getRunMainController();
		if(rmc != null) {
			rmc.activate(ureq, entries, state);
		}
	}
	
	private void doActivateMeeting(UserRequest ureq, List<ContextEntry> entries) {
		boolean selected = false;
		if(bigBlueButtonLink != null && bigBlueButtonLink.isVisible()) {
			BigBlueButtonRunController bigBlueButtonRunCtrl = doBigBlueButton(ureq);
			if(bigBlueButtonRunCtrl != null) {
				bigBlueButtonRunCtrl.activate(ureq, entries, null);
				selected = bigBlueButtonRunCtrl.isMeetingSelected();
			}	
		}
		if(!selected && teamsLink != null && teamsLink.isVisible()) {
			TeamsMeetingsRunController teamsRunCtrl = doTeams(ureq);
			if(teamsRunCtrl != null) {
				teamsRunCtrl.activate(ureq, entries, null);
			}
		}
	}
	
	private void doGoTo(UserRequest ureq, GoToEvent e) {
		if(e.getTargetId() == null) return;
		
		if(GoToEvent.GOTO_TOOL.equals(e.getCommand())) {
			toolControllerDone(ureq);
			String tool = e.getTargetId().toLowerCase();
			for(CourseTool cTool:CourseTool.values()) {
				if(cTool.name().equals(tool)) {
					doOpenTool(ureq, cTool);
					break;
				}
			}
		} else if(GoToEvent.GOTO_NODE.equals(e.getCommand())) {
			if(currentToolCtr != null) {
				toolbarPanel.popController(currentToolCtr);
				toolControllerDone(ureq);
			}
			getRunMainController().updateTreeAndContent(ureq, e.getTargetId());
		}
	}
	
	private void doOpenTool(UserRequest ureq, CourseTool tool) {
		switch (tool) {
		case bigbluebutton: {
			if (bigBlueButtonLink != null && bigBlueButtonLink.isVisible()) {
				doBigBlueButton(ureq);
			} else {
				doShowToolNotAvailable(CourseTool.bigbluebutton);
			}
		}
		break;
		case blog: {
			if (blogLink != null && blogLink.isVisible()) {
				doBlog(ureq);
			} else {
				doShowToolNotAvailable(CourseTool.blog);
			}
		}
		break;
		case documents: {
			if (documentsLink != null && documentsLink.isVisible()) {
				doDocuments(ureq);
			} else {
				doShowToolNotAvailable(CourseTool.documents);
			}
		}
		break;
		case efficiencystatement: {
			if (efficiencyStatementsLink != null && efficiencyStatementsLink.isVisible()) {
				doEfficiencyStatements(ureq);
			} else {
				doShowToolNotAvailable(CourseTool.efficiencystatement);
			}
		}
		break;
		case email: {
			if (emailLink != null && emailLink.isVisible()) {
				doEmail(ureq);
			} else {
				doShowToolNotAvailable(CourseTool.email);
			}
		}
		break;
		case forum: {
			if (forumLink != null && forumLink.isVisible()) {
				doForum(ureq);
			} else {
				doShowToolNotAvailable(CourseTool.forum);
			}
		}
		break;
		case learningpath: {
			if (learningPathLink != null && learningPathLink.isVisible()) {
				doLearningPath(ureq);
			} else if (learningPathsLink != null && learningPathsLink.isVisible()) {
				doLearningPaths(ureq);
			} else {
				doShowToolNotAvailable(CourseTool.learningpath);
			}
		}
		break;
		case participantlist: {
			if (participantListLink != null && participantListLink.isVisible()) {
				doParticipantList(ureq);
			} else {
				doShowToolNotAvailable(CourseTool.participantlist);
			}
		}
		break;
		case participantinfos: {
			if (participantInfoLink != null && participantInfoLink.isVisible()) {
				doParticipantInfo(ureq);
			} else {
				doShowToolNotAvailable(CourseTool.participantinfos);
			}
		}
		break;
		case teams: {
			if (teamsLink != null && teamsLink.isVisible()) {
				doTeams(ureq);
			} else {
				doShowToolNotAvailable(CourseTool.teams);
			}
		}
		break;
		case wiki: {
			if (wikiLink != null && wikiLink.isVisible()) {
				doWiki(ureq);
			} else {
				doShowToolNotAvailable(CourseTool.wiki);
			}
		}
		break;
		case zoom: {
			if (zoomLink != null && zoomLink.isVisible()) {
				doZoom(ureq);
			} else {
				doShowToolNotAvailable(CourseTool.zoom);
			}
		}
		break;
		default:
			getWindowControl().setWarning(translate("msg.tool.not.available", new String[] { tool.name() } ));
		}
		
	}
	
	private void doShowToolNotAvailable(CourseTool tool) {
		getWindowControl().setWarning(translate("msg.tool.not.available", new String[] { translate(tool.getI18nKey()) } ));

	}

	@Override
	protected void doEdit(UserRequest ureq) {
		if ((reSecurity.isEntryAdmin()
				|| hasCourseRight(CourseRights.RIGHT_COURSEEDITOR))) {
			removeCustomCSS();
			popToRoot(ureq);
			cleanUp();

			CourseNode currentCourseNode = getCurrentCourseNode();
			WindowControl bwControl = getSubWindowControl("Editor");
			EditorMainController ctrl = CourseFactory.createEditorController(ureq, addToHistory(ureq, bwControl),
					toolbarPanel, getRepositoryEntry(), currentCourseNode);
			//user activity logger which was initialized with course run
			if(ctrl != null){
				editorCtrl = pushController(ureq, "Editor", ctrl);
				listenTo(editorCtrl);
				setIsInEditor(true);
				currentToolCtr = editorCtrl;
				setActiveTool(editLink);
			}
		}
	}
	
	@Override
	protected Activateable2 doSettings(UserRequest ureq, List<ContextEntry> entries) {
		if(delayedClose == Delayed.settings || requestForClose(ureq)) {
			if (reSecurity.isEntryAdmin() || hasCourseRight(CourseRights.RIGHT_COURSEEDITOR)) {
				removeCustomCSS();
				
				WindowControl bwControl = getSubWindowControl("Settings");
				RepositoryEntry refreshedEntry = loadRepositoryEntry();
				RepositoryEntrySecurity security = reSecurity.getWrappedSecurity();
				CourseSettingsController ctrl
					= new CourseSettingsController(ureq, addToHistory(ureq, bwControl), toolbarPanel, refreshedEntry, security);
				listenTo(ctrl);
				settingsCtrl = pushController(ureq, translate("details.settings"), ctrl);
				currentToolCtr = settingsCtrl;
				setActiveTool(settingsLink);
				settingsCtrl.activate(ureq, entries, null);
				return settingsCtrl;
			}
		} else {
			delayedClose = Delayed.settings;
		}
		return null;
	}

	@Override
	protected Activateable2 doMembers(UserRequest ureq) {
		if(delayedClose == Delayed.members || requestForClose(ureq)) {
			if (reSecurity.isEntryAdmin() || reSecurity.isPrincipal() || reSecurity.isMasterCoach() || hasCourseRight(CourseRights.RIGHT_GROUPMANAGEMENT) || hasCourseRight(CourseRights.RIGHT_MEMBERMANAGEMENT)) {
				removeCustomCSS();
				if(currentToolCtr instanceof MembersManagementMainController) {
					((MembersManagementMainController)currentToolCtr).activate(ureq, null, null);
				} else {
					boolean canInvite = isAllowedToInvite();
					WindowControl bwControl = getSubWindowControl("MembersMgmt");
					MembersManagementMainController ctrl = new MembersManagementMainController(ureq, addToHistory(ureq, bwControl), toolbarPanel,
							getRepositoryEntry(), getUserCourseEnvironment(), reSecurity.isEntryAdmin(), reSecurity.isPrincipal() || reSecurity.isMasterCoach(),
							hasCourseRight(CourseRights.RIGHT_GROUPMANAGEMENT), hasCourseRight(CourseRights.RIGHT_MEMBERMANAGEMENT), canInvite);
					listenTo(ctrl);
					membersCtrl = pushController(ureq, translate("command.opensimplegroupmngt"), ctrl);
					setActiveTool(membersLink);
					currentToolCtr = membersCtrl;
				}
			}
		} else {
			delayedClose = Delayed.members;
		}
		return membersCtrl;
	}
	
	private boolean isAllowedToInvite() {
		if(!invitationModule.isCourseInvitationEnabled()) {
			return false;
		}
		
		final RepositoryEntry entry = getRepositoryEntry();
		final RepositoryEntrySecurity wreSecurity = reSecurity.getWrappedSecurity();
		return wreSecurity.isAdministrator() || wreSecurity.isLearnResourceManager()
				|| (wreSecurity.isAuthor() && wreSecurity.isEntryAdmin()
						&& invitationModule.getCourseOwnerPermission() == InvitationConfigurationPermission.allResources)
				|| (wreSecurity.isAuthor() && wreSecurity.isEntryAdmin()
						&& invitationModule.getCourseOwnerPermission() == InvitationConfigurationPermission.perResource
						&& entry.isInvitationByOwnerWithAuthorRightsEnabled());
	}
	
	private void doConfirmLeave(UserRequest ureq) {
		String title = translate("sign.out");

		leaveDialogBox = new ConfirmLeaveController(ureq, getWindowControl(), getRepositoryEntry());
		listenTo(leaveDialogBox);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), leaveDialogBox.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doLeave(UserRequest ureq) {
		if(roles.isGuestOnly()) return;
		
		MailerResult result = new MailerResult();
		MailPackage reMailing = new MailPackage(result, getWindowControl().getBusinessControl().getAsString(), true);
		//leave course
		LeavingStatusList status = new LeavingStatusList();
		repositoryManager.leave(getIdentity(), getRepositoryEntry(), status, reMailing);
		//leave groups
		businessGroupService.leave(getIdentity(), getRepositoryEntry(), status, reMailing);
		
		if(status.isWarningManagedGroup() || status.isWarningManagedCourse()) {
			showWarning("sign.out.warning.managed");
		} else if(status.isWarningGroupWithMultipleResources()) {
			showWarning("sign.out.warning.mutiple.resources");
		} else {
			showInfo("sign.out.success", new String[]{ getRepositoryEntry().getDisplayname() });
		}

		doClose(ureq);
	}
	
	private void doDeactivateQuickSearch() {
		courseSearchCalloutCtr.deactivate();
	}
	
	private void doLifeCycleChange(UserRequest ureq) {
		List<Link> breadCrumbs = toolbarPanel.getBreadCrumbs();
		BreadCrumb lastCrumb = null;
		if (!breadCrumbs.isEmpty()) {
			lastCrumb = (BreadCrumb) breadCrumbs.get(breadCrumbs.size()-1).getUserObject();
		}
		if (lastCrumb == null || lastCrumb.getController() != lifeCycleChangeCtr) {
			// only create and add to stack if not already there
			lifeCycleChangeCtr = new RepositoryEntryLifeCycleChangeController(ureq, getWindowControl(),
					getRepositoryEntry(), reSecurity, handler);
			listenTo(lifeCycleChangeCtr);
			currentToolCtr = lifeCycleChangeCtr;
			toolbarPanel.pushController(translate("details.lifecycle.change"), lifeCycleChangeCtr);
		}
	}
	
	private void doAssessmentMode(UserRequest ureq) {
		if(delayedClose == Delayed.assessmentMode || requestForClose(ureq)) {
			if (reSecurity.isEntryAdmin() || reSecurity.isPrincipal() || reSecurity.isMasterCoach()
					|| hasCourseRight(CourseRights.RIGHT_COURSEEDITOR) || hasCourseRight(CourseRights.RIGHT_ASSESSMENT_MODE)) {
				removeCustomCSS();
				
				boolean canManage = reSecurity.isEntryAdmin() || hasCourseRight(CourseRights.RIGHT_COURSEEDITOR) || hasCourseRight(CourseRights.RIGHT_ASSESSMENT_MODE);
				AssessmentModeSecurityCallback secCallback = AssessmentModeSecurityCallbackFactory.getSecurityCallback(canManage);

				WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("AssessmentMode", 0l), null);
				AssessmentModeListController ctrl = new AssessmentModeListController(ureq, swControl,
						toolbarPanel, getRepositoryEntry(), secCallback);
				assessmentModeCtrl = pushController(ureq, translate("command.assessment.mode"), ctrl);
				listenTo(assessmentModeCtrl);
				setActiveTool(assessmentModeLink);
				currentToolCtr = assessmentModeCtrl;
			}
		} else {
			delayedClose = Delayed.assessmentMode;
		}
	}
	
	private Activateable2 doReminders(UserRequest ureq) {
		if(delayedClose == Delayed.reminders || requestForClose(ureq)) {
			if (reSecurity.isEntryAdmin() || hasCourseRight(CourseRights.RIGHT_COURSEEDITOR)) {
				removeCustomCSS();
				
				WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("Reminders", 0l), null);
				CourseReminderListController ctrl = new CourseReminderListController(ureq, swControl, toolbarPanel, getRepositoryEntry(),
						CourseProvider.create(), null, true);
				remindersCtrl = pushController(ureq, translate("command.reminders"), ctrl);
				setActiveTool(reminderLink);
				currentToolCtr = remindersCtrl;
			}
		} else {
			delayedClose = Delayed.reminders;
		}
		return remindersCtrl;
	}
	
	private LectureRepositoryAdminController doLecturesAdmin(UserRequest ureq) {
		if(delayedClose == Delayed.lecturesAdmin || requestForClose(ureq)) {
			if (reSecurity.isEntryAdmin() || reSecurity.isPrincipal() || reSecurity.isMasterCoach() || hasCourseRight(CourseRights.RIGHT_COURSEEDITOR)) {
				removeCustomCSS();

				OLATResourceable ores = OresHelper.createOLATResourceableType("LecturesAdmin");
				WindowControl swControl = addToHistory(ureq, ores, null);
				LecturesSecurityCallback secCallback = LecturesSecurityCallbackFactory
						.getSecurityCallback(reSecurity.isEntryAdmin() || hasCourseRight(CourseRights.RIGHT_COURSEEDITOR), reSecurity.isMasterCoach(), false,
								getUserCourseEnvironment().getCourseReadOnlyDetails());
				LectureRepositoryAdminController ctrl = new LectureRepositoryAdminController(ureq, swControl, toolbarPanel, getRepositoryEntry(), secCallback);
				listenTo(ctrl);
				lecturesAdminCtrl = pushController(ureq, translate("command.options.lectures.admin"), ctrl);
				setActiveTool(lecturesAdminLink);
				currentToolCtr = lecturesAdminCtrl;
				return lecturesAdminCtrl;
			} else {
				return null;
			}
		} else {
			delayedClose = Delayed.lecturesAdmin;
			return null;
		}
	}
	
	private TeacherOverviewController doLectures(UserRequest ureq) {
		if(delayedClose == Delayed.lectures || requestForClose(ureq)) {
			removeCustomCSS();
			
			OLATResourceable ores = OresHelper.createOLATResourceableType("lectures");
			WindowControl swControl = addToHistory(ureq, ores, null);
			TeacherOverviewController ctrl = new TeacherOverviewController(ureq, swControl, toolbarPanel, getRepositoryEntry(),
					reSecurity.isEntryAdmin(), lectureModule.isShowLectureBlocksAllTeachersDefault());
			lecturesCtrl = pushController(ureq, translate("command.lectures"), ctrl);

			setActiveTool(lecturesLink);
			currentToolCtr = lecturesCtrl;
			return lecturesCtrl;
		} else {
			delayedClose = Delayed.lectures;
			return null;
		}
	}
	
	private ArchiverMainController doArchive(UserRequest ureq) {
		if(delayedClose == Delayed.archive || requestForClose(ureq)) {
			if (reSecurity.isEntryAdmin() || hasCourseRight(CourseRights.RIGHT_ARCHIVING)) {
				removeCustomCSS();
				ICourse course = CourseFactory.loadCourse(getRepositoryEntry());
				WindowControl swControl = this.addToHistory(ureq, OresHelper.createOLATResourceableInstance("Archives", 0l), null);
				ArchiverMainController ctrl = new ArchiverMainController(ureq, swControl, course, new FullAccessArchiverCallback());
				listenTo(ctrl);
				archiverCtrl = pushController(ureq, translate("command.openarchiver"), ctrl);
				currentToolCtr = archiverCtrl;
				setActiveTool(archiverLink);
				return ctrl;
			}
		} else {
			delayedClose = Delayed.archive;
		}
		return null;
	}
	
	private FolderRunController doCourseFolder(UserRequest ureq) {
		if(delayedClose == Delayed.courseFolder || requestForClose(ureq)) {
			if (reSecurity.isEntryAdmin() || hasCourseRight(CourseRights.RIGHT_COURSEEDITOR)) {
				removeCustomCSS();
				// Folder for course with custom link model to jump to course nodes
				ICourse course = CourseFactory.loadCourse(getRepositoryEntry());
				VFSContainer courseContainer;
				if(overrideReadOnly) {
					courseContainer = course.getCourseFolderContainer(overrideReadOnly);
				} else {
					courseContainer = course.getCourseFolderContainer();
				}
				VFSContainer namedCourseFolder = new NamedContainerImpl(translate("command.coursefolder"), courseContainer);
				CustomLinkTreeModel customLinkTreeModel = new CourseInternalLinkTreeModel(course.getEditorTreeModel());
	
				WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("CourseFolder", 0l), null);
				FolderRunController ctrl = new FolderRunController(namedCourseFolder, true, true, Mail.always, true, ureq, swControl, null, customLinkTreeModel, null);
				ctrl.addLoggingResourceable(LoggingResourceable.wrap(course));
				courseFolderCtrl = pushController(ureq, translate("command.coursefolder"), ctrl);
				setActiveTool(folderLink);
				ctrl.initToolbar(toolbarPanel);
				listenTo(ctrl);
				currentToolCtr = courseFolderCtrl;
			}
		} else {
			delayedClose = Delayed.courseFolder;
		}
		return courseFolderCtrl;
	}

	private void doQuotaUsageView(UserRequest ureq) {
		toolbarPanel.popUpToController(this);
		courseQuotaUsageController = new CourseQuotaUsageController(ureq, getWindowControl(), loadRepositoryEntry());
		listenTo(courseQuotaUsageController);
		toolbarPanel.pushController(translate("menu.quota.usage"), courseQuotaUsageController);
	}
	
	private CoachFolderController doCoachFolder(UserRequest ureq) {
		if(delayedClose == Delayed.coachFolder || requestForClose(ureq)) {
			removeCustomCSS();
			
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("CoachFolder", 0l);
			WindowControl swControl = addToHistory(ureq, ores, null);
			coachFolderCtrl = new CoachFolderController(ureq, swControl, getUserCourseEnvironment());

			pushController(ureq, translate("command.coachfolder"), coachFolderCtrl);
			setActiveTool(coachFolderLink);
			currentToolCtr = coachFolderCtrl;
			return coachFolderCtrl;
		}
		delayedClose = Delayed.coachFolder;
		return null;
	}
	
	private void doCourseAreas(UserRequest ureq) {
		if(delayedClose == Delayed.courseAreas || requestForClose(ureq)) {
			if (reSecurity.isEntryAdmin() || hasCourseRight(CourseRights.RIGHT_COURSEEDITOR)) {
				removeCustomCSS();
				ICourse course = CourseFactory.loadCourse(getRepositoryEntry());
				WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("CourseAreas", 0l), null);
				CourseAreasController ctrl = new CourseAreasController(ureq, swControl,
						getRepositoryEntry().getOlatResource(), getUserCourseEnvironment().isCourseReadOnly());
				ctrl.addLoggingResourceable(LoggingResourceable.wrap(course));
				areasCtrl = pushController(ureq, translate("command.courseareas"), ctrl);
				setActiveTool(areaLink);
				currentToolCtr = areasCtrl;
			}
		} else {
			delayedClose = Delayed.courseAreas;
		}
	}
	
	private void doDatabases(UserRequest ureq) {
		if(delayedClose == Delayed.databases || requestForClose(ureq)) {
			if (reSecurity.isEntryAdmin() || hasCourseRight(CourseRights.RIGHT_DB)) {
				removeCustomCSS();
				ICourse course = CourseFactory.loadCourse(getRepositoryEntry());
				CustomDBMainController ctrl = new CustomDBMainController(ureq, getWindowControl(), course,
						getUserCourseEnvironment().isCourseReadOnly());
				listenTo(ctrl);
				databasesCtrl = pushController(ureq, translate("command.opendb"), ctrl);
				setActiveTool(dbLink);
				currentToolCtr = databasesCtrl;
			}
		} else {
			delayedClose = Delayed.databases;
		}
	}
	
	private StatisticMainController doCourseStatistics(UserRequest ureq) {
		if(delayedClose == Delayed.courseStatistics || requestForClose(ureq)) {
			if (reSecurity.isEntryAdmin() || reSecurity.isPrincipal() || reSecurity.isMasterCoach()
					|| hasCourseRight(CourseRights.RIGHT_STATISTICS)) {
				removeCustomCSS();
				ICourse course = CourseFactory.loadCourse(getRepositoryEntry());
				WindowControl swControl = this.addToHistory(ureq, OresHelper.createOLATResourceableInstance("CourseStatistics", 0l), null);
				StatisticMainController ctrl = new StatisticMainController(ureq, swControl, course);
				listenTo(ctrl);
				statisticsCtrl = pushController(ureq, translate("command.openstatistic"), ctrl);
				setActiveTool(courseStatisticLink);
				currentToolCtr = statisticsCtrl;
			}
		} else {
			delayedClose = Delayed.courseStatistics;
		}
		return statisticsCtrl;
	}
	
	private Activateable2 doAssessmentTestStatistics(UserRequest ureq) {
		Activateable2 controller = null;
		if(delayedClose == Delayed.assessmentTestStatistics || requestForClose(ureq)) {
			controller = doAssessmentStatistics(ureq, "command.openteststatistic", "TestStatistics", testStatisticLink, StatisticType.TEST);
		} else {
			delayedClose = Delayed.assessmentTestStatistics;
		}
		return controller;
	}
	
	private Activateable2 doAssessmentSurveyStatistics(UserRequest ureq) {
		Activateable2 controller = null;
		if(delayedClose == Delayed.assessmentSurveyStatistics || requestForClose(ureq)) {
			controller = doAssessmentStatistics(ureq, "command.opensurveystatistic", "SurveyStatistics",
					surveyStatisticLink, StatisticType.SURVEY);
		} else {
			delayedClose = Delayed.assessmentSurveyStatistics;
		}
		return controller;
	}
	
	/**
	 * Only an helper method for the 2 methods above. Don't call it directly, there is no request on close guard.
	 * @param ureq
	 * @param i18nCrumbKey
	 * @param typeName
	 * @param tool
	 * @param types
	 * @return
	 */
	private Activateable2 doAssessmentStatistics(UserRequest ureq, String i18nCrumbKey, String typeName, Link tool, StatisticType type) {
		OLATResourceable ores = OresHelper.createOLATResourceableType(typeName);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl swControl = addToHistory(ureq, ores, null);
		if (reSecurity.isEntryAdmin() || reSecurity.isPrincipal() || reSecurity.isMasterCoach() || reSecurity.isCoach() || hasCourseRight(CourseRights.RIGHT_STATISTICS)) {
			removeCustomCSS();
			UserCourseEnvironmentImpl uce = getUserCourseEnvironment();
			StatisticCourseNodesController ctrl = new StatisticCourseNodesController(ureq, swControl, toolbarPanel,  reSecurity, uce, type);
			listenTo(ctrl);
			statsToolCtr = pushController(ureq, translate(i18nCrumbKey), ctrl);
			currentToolCtr = statsToolCtr;
			setActiveTool(tool);
			return statsToolCtr;
		}
		return null;
	}
	
	private Activateable2 doAssessmentTool(UserRequest ureq) {
		if(delayedClose == Delayed.assessmentTool || requestForClose(ureq)) {
			OLATResourceable ores = OresHelper.createOLATResourceableType("assessmentToolv2");
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl swControl = addToHistory(ureq, ores, null);
			
			removeCustomCSS();
			AssessmentToolController ctrl = new AssessmentToolController(ureq, swControl, toolbarPanel, getRepositoryEntry(), getUserCourseEnvironment(), createAssessmentToolSecurityCallback());
			assessmentToolCtr = pushController(ureq, translate("command.openassessment"), ctrl);
			listenTo(assessmentToolCtr);
			OLATResourceable overviewRes = OresHelper.createOLATResourceableType("Overview");
			List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromString(overviewRes);
			ctrl.activate(ureq, entries, null);
			currentToolCtr = assessmentToolCtr;
			setActiveTool(assessmentLink);
			ctrl.initToolbar();
			return assessmentToolCtr;

		} else {
			delayedClose = Delayed.assessmentTool;
		}
		return null;
	}

	private Activateable2 doBadges(UserRequest ureq) {
		if (delayedClose == Delayed.badges || requestForClose(ureq)) {
			OLATResourceable ores = OresHelper.createOLATResourceableType("Badges");
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl swControl = addToHistory(ureq, ores, null);

			BadgeClassesController badgeClassesController = new BadgeClassesController(ureq, swControl, getRepositoryEntry(),
					toolbarPanel, null, "form.add.new.badge", "form.edit.badge");
			badgeClassesCtrl = pushController(ureq, translate("command.openbadges"), badgeClassesController);
			listenTo(badgeClassesCtrl);

			currentToolCtr = badgeClassesCtrl;
			setActiveTool(badgesLink);
			return badgeClassesCtrl;
		}
		delayedClose = Delayed.badges;
		return null;
	}

	private void doMyBadges(UserRequest ureq) {
		if (delayedClose == Delayed.myBadges || requestForClose(ureq)) {
			UserCourseEnvironment userCourseEnv = getUserCourseEnvironment();
			if (userCourseEnv.isParticipant()) {
				OLATResourceable ores = OresHelper.createOLATResourceableType("MyBadges");
				ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
				WindowControl swControl = addToHistory(ureq, ores, null);

				IssuedBadgesController ctrl = new IssuedBadgesController(ureq, swControl, "badges.mine.title",
						getRepositoryEntry(), false, getIdentity(), null);
				myBadgesCtrl = pushController(ureq, translate("command.mybadges"), ctrl);
				listenTo(myBadgesCtrl);

				currentToolCtr = myBadgesCtrl;
				setActiveTool(myBadgesLink);
			}
		} else {
			delayedClose = Delayed.myBadges;
		}
	}

	private void doIssuedBadges(UserRequest ureq) {
		if (delayedClose == Delayed.issuedBadges || requestForClose(ureq)) {
			if (reSecurity.isCoach() || reSecurity.isOwner() || reSecurity.isEntryAdmin()) {
				OLATResourceable ores = OresHelper.createOLATResourceableType("IssuedBadges");
				ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
				WindowControl swControl = addToHistory(ureq, ores, null);

				IssuedBadgesController ctrl = new IssuedBadgesController(ureq, swControl, "issuedBadges",
						getRepositoryEntry(), false, null, null);
				issuedBadgesCtrl = pushController(ureq, translate("command.issuedbadges"), ctrl);
				listenTo(issuedBadgesCtrl);

				currentToolCtr = issuedBadgesCtrl;
				setActiveTool(issuedBadgesLink);
			}
		} else {
			delayedClose = Delayed.issuedBadges;
		}
	}

	private AssessmentToolSecurityCallback createAssessmentToolSecurityCallback() {
		boolean admin = reSecurity.isEntryAdmin() || reSecurity.isPrincipal() || reSecurity.isMasterCoach() || hasCourseRight(CourseRights.RIGHT_ASSESSMENT);
		boolean nonMembers = reSecurity.isEntryAdmin();
		List<BusinessGroup> coachedGroups = null;
		UserCourseEnvironment userCourseEnv = getUserCourseEnvironment();
		if(reSecurity.isGroupCoach()) {
			coachedGroups = userCourseEnv.getCoachedGroups();
		}
		Set<IdentityRef> fakeParticipants = assessmentToolManager.getFakeParticipants(getRepositoryEntry(),
				userCourseEnv.getIdentityEnvironment().getIdentity(), nonMembers, !nonMembers);
		return new AssessmentToolSecurityCallback(admin, reSecurity.isOnlyPrincipal(), nonMembers, reSecurity.isCourseCoach(),
				reSecurity.isGroupCoach(), reSecurity.isCurriculumCoach(), coachedGroups, fakeParticipants);
	}
	
	private void doEfficiencyStatements(UserRequest ureq) {
		if(delayedClose == Delayed.efficiencyStatements || requestForClose(ureq)) {
			// will not be disposed on course run dispose, popus up as new browserwindow
			WindowControl bwControl = getSubWindowControl("Certification");
			CertificateAndEfficiencyStatementController efficiencyStatementController
				= new CertificateAndEfficiencyStatementController(addToHistory(ureq, bwControl), ureq, getRepositoryEntry());
			listenTo(efficiencyStatementController);
			efficiencyStatementController = pushController(ureq, translate("command.efficiencystatement"), efficiencyStatementController);
			currentToolCtr = efficiencyStatementController;
			setActiveTool(efficiencyStatementsLink);
		} else {
			delayedClose = Delayed.efficiencyStatements;
		}
	}

	private void toggleGlossary(UserRequest ureq) {
		// enable / disable glossary highlighting according to user prefs
		Preferences prefs = ureq.getUserSession().getGuiPreferences();
		String guiPrefsKey = CourseGlossaryFactory.createGuiPrefsKey(getOlatResourceable());
		Boolean state = (Boolean) prefs.get(CourseGlossaryToolLinkController.class, guiPrefsKey);
		Boolean newState = state == null ? Boolean.TRUE : Boolean.valueOf(!state.booleanValue());
		setGlossaryLinkTitle(ureq, newState);
		prefs.putAndSave(CourseGlossaryToolLinkController.class, guiPrefsKey, newState);
	}
	
	private void setGlossaryLinkTitle(UserRequest ureq, Boolean state) {
		if(enableGlossaryLink == null) return;
		
		String oresName = ICourse.class.getSimpleName();
		Long courseID = getOlatResourceable().getResourceableId();
		// must work with SP and CP nodes, IFrameDisplayController listens to this event and expects "ICourse" resources.
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(oresName, courseID);
		if (state == null || !state.booleanValue()) {
			enableGlossaryLink.setCustomDisplayText(translate("command.glossary.on.alt"));
			setTextMarkingEnabled(false);
			ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(new MultiUserEvent("glossaryOff"), ores);
		} else {
			enableGlossaryLink.setCustomDisplayText(translate("command.glossary.off.alt"));
			setTextMarkingEnabled(true);
			ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(new MultiUserEvent("glossaryOn"), ores);
		}
	}
	
	private void doConvertToLearningPath(UserRequest ureq) {
		ICourse course = CourseFactory.loadCourse(getRepositoryEntry());
		List<CourseNode> unsupportedCourseNodes = learningPathService.getUnsupportedCourseNodes(course);
		if (!unsupportedCourseNodes.isEmpty()) {
			showUnsupportedMessage(ureq, unsupportedCourseNodes);
			return;
		}
		
		RepositoryEntry lpEntry = learningPathService.migrate(getRepositoryEntry(), getIdentity());
		String bPath = "[RepositoryEntry:" + lpEntry.getKey() + "]";
		NewControllerFactory.getInstance().launch(bPath, ureq, getWindowControl());
	}

	private void showUnsupportedMessage(UserRequest ureq, List<CourseNode> unsupportedCourseNodes) {
		unsupportedCourseNodesCtrl = new UnsupportedCourseNodesController(ureq, getWindowControl(), unsupportedCourseNodes);
		listenTo(unsupportedCourseNodesCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				unsupportedCourseNodesCtrl.getInitialComponent(), true, translate("unsupported.course.nodes.title"));
		cmc.activate();
		listenTo(cmc);
	}
	
	@Override
	protected void launchContent(UserRequest ureq) {
		super.launchContent(ureq);
		if(getRunMainController() != null) {
			addCustomCSS(ureq);
			getRunMainController().initToolbarAndProgressbar();
		}
	}
	
	private void doLearningPath(UserRequest ureq) {
		if(delayedClose == Delayed.learningPath || requestForClose(ureq)) {
			OLATResourceable ores = OresHelper.createOLATResourceableType("LearningPath");
			WindowControl swControl = addToHistory(ureq, ores, null);
			MyLearningPathController learningPathCtrl = new MyLearningPathController(ureq, swControl,
					toolbarPanel, getUserCourseEnvironment());
			
			listenTo(learningPathCtrl);
			pushController(ureq, translate("command.learning.path"), learningPathCtrl);
			currentToolCtr = learningPathCtrl;
			setActiveTool(learningPathLink);
		} else {
			delayedClose = Delayed.learningPath;
		}
	}
	
	private Activateable2 doLearningPaths(UserRequest ureq) {
		if(delayedClose == Delayed.learningPaths || requestForClose(ureq)) {
			OLATResourceable ores = OresHelper.createOLATResourceableType("LearningPaths");
			WindowControl swControl = addToHistory(ureq, ores, null);
			LearningPathIdentityListController learningPathIdentityListCtrl = new LearningPathIdentityListController(ureq, swControl,
					toolbarPanel, getUserCourseEnvironment());
			
			listenTo(learningPathIdentityListCtrl);
			pushController(ureq, translate("command.learning.paths"), learningPathIdentityListCtrl);
			currentToolCtr = learningPathIdentityListCtrl;
			setActiveTool(learningPathsLink);
			return learningPathIdentityListCtrl;
		}
		delayedClose = Delayed.learningPaths;
		return null;
	}
	
	private void launchChat(UserRequest ureq) {
		boolean vip = reSecurity.isCoach() || reSecurity.isEntryAdmin();
		ICourse course = CourseFactory.loadCourse(getRepositoryEntry());
		ChatViewConfig viewConfig = ChatViewConfig.room(course.getCourseTitle(), RosterFormDisplay.left);
		OpenInstantMessageEvent event = new OpenInstantMessageEvent(course, null, null, viewConfig, vip, false);
		ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(event, InstantMessagingService.TOWER_EVENT_ORES);
	}
	
	private void launchCourseSearch(UserRequest ureq) {
		// do not dispose SearchInputController after search to remain able to listen to its events 
		removeAsListenerAndDispose(courseSearchCalloutCtr);
		courseSearchCalloutCtr = null;
		removeAsListenerAndDispose(searchController);
		searchController = null;
		SearchServiceUIFactory searchServiceUIFactory = (SearchServiceUIFactory)CoreSpringFactory.getBean(SearchServiceUIFactory.class);
		searchController = searchServiceUIFactory.createInputController(ureq, getWindowControl(), DisplayOption.STANDARD, null);
		searchController.setContextHelpPage("manual_user/course_operation/Using_Additional_Course_Features/#course-search");
		listenTo(searchController);
		courseSearchCalloutCtr = new CloseableCalloutWindowController(ureq, getWindowControl(),
				searchController.getInitialComponent(), searchLink.getDispatchID(), null, true, null);
		courseSearchCalloutCtr.addDisposableChildController(searchController);
		courseSearchCalloutCtr.activate();
		listenTo(courseSearchCalloutCtr);
	}
	
	private void doParticipantList(UserRequest ureq) {
		if(delayedClose == Delayed.participantList || requestForClose(ureq)) {
			removeCustomCSS();
			
			OLATResourceable ores = OresHelper.createOLATResourceableType("ParticipantList");
			WindowControl swControl = addToHistory(ureq, ores, null);
			participatListCtrl = new  MembersToolRunController(ureq, swControl, getUserCourseEnvironment());

			pushController(ureq, translate("command.participant.list"), participatListCtrl);
			setActiveTool(participantListLink);
			currentToolCtr = participatListCtrl;
		} else {
			delayedClose = Delayed.participantList;
		}
	}
	
	private void doParticipantInfo(UserRequest ureq) {
		if(delayedClose == Delayed.participantInfo || requestForClose(ureq)) {
			removeCustomCSS();
			
			boolean autoSubscribe = false;
			boolean canAdd;
			boolean canAdmin;
			if(getUserCourseEnvironment().isCourseReadOnly()) {
				canAdd = false;
				canAdmin = false;
			} else {
				boolean isAdmin = getUserCourseEnvironment().isAdmin();
				canAdd = isAdmin || getUserCourseEnvironment().isCoach();
				canAdmin = isAdmin;
			}
			InfoSecurityCallback secCallback = new InfoCourseSecurityCallback(getIdentity(), canAdd, canAdmin);
			
			OLATResourceable ores = OresHelper.createOLATResourceableType("ParticipantInfos");
			WindowControl swControl = addToHistory(ureq, ores, null);
			participatInfoCtrl = new InfoRunController(ureq, swControl, getUserCourseEnvironment(), "ParticipantInfos",
					secCallback, autoSubscribe);
			listenTo(participatInfoCtrl);

			pushController(ureq, translate("command.participant.info"), participatInfoCtrl);
			setActiveTool(participantInfoLink);
			currentToolCtr = participatInfoCtrl;
		} else {
			delayedClose = Delayed.participantInfo;
		}
	}
	
	private void doEmail(UserRequest ureq) {
		if(delayedClose == Delayed.email || requestForClose(ureq)) {
			removeCustomCSS();
			
			OLATResourceable ores = OresHelper.createOLATResourceableType("email");
			WindowControl swControl = addToHistory(ureq, ores, null);
			emailCtrl = new COToolController(ureq, swControl, getUserCourseEnvironment());

			pushController(ureq, translate("command.email"), emailCtrl);
			setActiveTool(emailLink);
			currentToolCtr = emailCtrl;
		} else {
			delayedClose = Delayed.email;
		}
	}
	
	private BlogToolController doBlog(UserRequest ureq) {
		if(delayedClose == Delayed.blog || requestForClose(ureq)) {
			removeCustomCSS();
			
			OLATResourceable ores = OresHelper.createOLATResourceableType("blog");
			WindowControl swControl = addToHistory(ureq, ores, null);
			blogCtrl = new BlogToolController(ureq, swControl, getUserCourseEnvironment());
			
			pushController(ureq, translate("command.blog"), blogCtrl);
			setActiveTool(blogLink);
			currentToolCtr = blogCtrl;
			return blogCtrl;
		}
		delayedClose = Delayed.blog;
		return null;
	}
	
	private TeamsMeetingsRunController doTeams(UserRequest ureq) {
		if(delayedClose == Delayed.teams || requestForClose(ureq)) {
			removeCustomCSS();
			
			OLATResourceable ores = OresHelper.createOLATResourceableType("teams");
			WindowControl swControl = addToHistory(ureq, ores, null);
			UserCourseEnvironment userCourseEnv = getUserCourseEnvironment();
			boolean admin = userCourseEnv.isAdmin() || userCourseEnv.isCoach();
			boolean moderator = userCourseEnv.isAdmin() || userCourseEnv.isCoach();
			teamsCtrl = new TeamsMeetingsRunController(ureq, swControl, getRepositoryEntry(), null, null,
					admin, moderator, userCourseEnv.isCourseReadOnly());
			
			pushController(ureq, translate("command.teams"), teamsCtrl);
			setActiveTool(teamsLink);
			currentToolCtr = teamsCtrl;
			return teamsCtrl;
		}	
		delayedClose = Delayed.teams;
		return null;
	}
	
	private BigBlueButtonRunController doBigBlueButton(UserRequest ureq) {
		if(delayedClose == Delayed.bigbluebutton || requestForClose(ureq)) {
			removeCustomCSS();
			
			ICourse course = CourseFactory.loadCourse(getRepositoryEntry());
			final CourseConfig cc = course.getCourseConfig(); // do not cache cc, not save
			boolean moderatorStart = cc.isBigBlueButtonModeratorStartsMeeting();
			
			OLATResourceable ores = OresHelper.createOLATResourceableType("bigbluebutton");
			WindowControl swControl = addToHistory(ureq, ores, null);
			UserCourseEnvironment userCourseEnv = getUserCourseEnvironment();
			boolean admin = userCourseEnv.isAdmin() || userCourseEnv.isCoach();
			boolean moderator = userCourseEnv.isAdmin() || userCourseEnv.isCoach();
			BigBlueButtonMeetingDefaultConfiguration configuration = new BigBlueButtonMeetingDefaultConfiguration(moderatorStart);
			bigBlueButtonCtrl = new BigBlueButtonRunController(ureq, swControl, getRepositoryEntry(), null, null,
					configuration, admin, moderator, userCourseEnv.isCourseReadOnly());
			
			pushController(ureq, translate("command.bigbluebutton"), bigBlueButtonCtrl);
			setActiveTool(bigBlueButtonLink);
			currentToolCtr = bigBlueButtonCtrl;
			return bigBlueButtonCtrl;
		}
		delayedClose = Delayed.bigbluebutton;
		return null;
	}

	private ZoomRunController doZoom(UserRequest ureq) {
		if (delayedClose == Delayed.zoom || requestForClose(ureq)) {
			removeCustomCSS();

			OLATResourceable ores = OresHelper.createOLATResourceableType("zoom");
			WindowControl swControl = addToHistory(ureq, ores, null);
			UserCourseEnvironment userCourseEnv = getUserCourseEnvironment();
			RepositoryEntry entry = getRepositoryEntry();
			String subIdent = userCourseEnv.getCourseEnvironment().getCourseResourceableId().toString();
			ICourse course = CourseFactory.loadCourse(getRepositoryEntry());
			CourseConfig cc = course.getCourseConfig();
			zoomCtrl = new ZoomRunController(ureq, swControl,
					ZoomManager.ApplicationType.courseTool, entry, subIdent, null,
					userCourseEnv.isAdmin(), userCourseEnv.isCoach(), userCourseEnv.isParticipant(), cc.getZoomClientId());
			pushController(ureq, translate("command.zoom"), zoomCtrl);
			setActiveTool(zoomLink);
			currentToolCtr = zoomCtrl;
			return zoomCtrl;
		}
		delayedClose = Delayed.zoom;
		return null;
	}

	private WikiToolController doWiki(UserRequest ureq) {
		if(delayedClose == Delayed.wiki || requestForClose(ureq)) {
			removeCustomCSS();
			
			OLATResourceable ores = OresHelper.createOLATResourceableType("wiki");
			WindowControl swControl = addToHistory(ureq, ores, null);
			wikiCtrl = new WikiToolController(ureq, swControl, getUserCourseEnvironment());
			
			pushController(ureq, translate("command.wiki"), wikiCtrl);
			setActiveTool(wikiLink);
			currentToolCtr = wikiCtrl;
			return wikiCtrl;
		}
		delayedClose = Delayed.wiki;
		return null;
	}
	
	private void doForum(UserRequest ureq) {
		if(delayedClose == Delayed.forum || requestForClose(ureq)) {
			removeCustomCSS();
			
			OLATResourceable ores = OresHelper.createOLATResourceableType("forum");
			WindowControl swControl = addToHistory(ureq, ores, null);
			forumCtrl = new FOToolController(ureq, swControl, getUserCourseEnvironment());

			pushController(ureq, translate("command.forum"), forumCtrl);
			setActiveTool(forumLink);
			currentToolCtr = forumCtrl;
		} else {
			delayedClose = Delayed.forum;
		}
	}
	
	private CourseDocumentsController doDocuments(UserRequest ureq) {
		if(delayedClose == Delayed.documents || requestForClose(ureq)) {
			removeCustomCSS();
			
			OLATResourceable ores = OresHelper.createOLATResourceableType("documents");
			WindowControl swControl = addToHistory(ureq, ores, null);
			documentsCtrl = new CourseDocumentsController(ureq, swControl, getUserCourseEnvironment());

			pushController(ureq, translate("command.documents"), documentsCtrl);
			setActiveTool(documentsLink);
			currentToolCtr = documentsCtrl;
			return documentsCtrl;
		}
		delayedClose = Delayed.documents;
		return null;
	}
	
	/**
	 * Open the calendar as sub-controller.
	 * 
	 * @param ureq The user request
	 * @return The calendar controller
	 */
	private CourseCalendarController doCalendar(UserRequest ureq) {
		OLATResourceable ores = OresHelper.createOLATResourceableType("Calendar");
		WindowControl swControl = addToHistory(ureq, ores, null);
		CourseCalendarController calendarController = new CourseCalendarController(ureq, swControl, getUserCourseEnvironment());
		pushController(ureq, translate("command.calendar"), calendarController);
		setActiveTool(calendarLink);
		currentToolCtr = calendarController;
		return calendarController;
	}
	
	/**
	 * Open the course calendar as popup.
	 * 
	 * @param ureq The user request
	 */
	private void launchCalendar(UserRequest ureq) {
		ControllerCreator ctrlCreator = (lureq, lwControl) -> {
			ICourse course = CourseFactory.loadCourse(getRepositoryEntry());
			ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(getRepositoryEntry());
			WindowControl llwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, lwControl);
			CourseCalendarController calendarController = new CourseCalendarController(lureq, llwControl, getUserCourseEnvironment());
			// use a one-column main layout
			LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(lureq, llwControl, calendarController);
			layoutCtr.setCustomCSS(CourseFactory.getCustomCourseCss(lureq.getUserSession(), course.getCourseEnvironment()));
			layoutCtr.addDisposableChildController(calendarController); // dispose calendar on layout dispose
			return layoutCtr;
		};
		//wrap the content controller into a full header layout
		ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
		//open in new browser window
		PopupBrowserWindow pbw = getWindowControl().getWindowBackOffice().getWindowManager().createNewPopupBrowserWindowFor(ureq, layoutCtrlr);
		pbw.open(ureq);
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
					+ "of group or group doesn't exist. Hacker attack or group has been changed or deleted. group key :: " + groupKey);
			// refresh toolbox that contained wrong group
			reloadGroupMemberships();
		}
	}
	
	private void launchPersonalNotes(UserRequest ureq) {
		// will not be disposed on course run dispose, pop up as new browser window
		ControllerCreator ctrlCreator = (lureq, lwControl) -> {
			ICourse course = CourseFactory.loadCourse(getRepositoryEntry());
			Controller notesCtr = new NoteController(lureq, course, course.getCourseTitle(), lwControl);
			LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(lureq, lwControl, notesCtr);
			layoutCtr.setCustomCSS(CourseFactory.getCustomCourseCss(lureq.getUserSession(), course.getCourseEnvironment()));
			layoutCtr.addDisposableChildController(notesCtr); // dispose glossary on layout dispose
			return layoutCtr;
		};
		//wrap the content controller into a full header layout
		ControllerCreator popupLayoutCtr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
		//open in new browser window
		openInNewBrowserWindow(ureq, popupLayoutCtr, false);
	}
	
	private void showDisclaimer(UserRequest ureq) {
		// show the accepted disclaimer in review / read-only mode
		if (disclaimeReviewController != null) {
			removeAsListenerAndDispose(disclaimeReviewController);
		}
		disclaimeReviewController = new CourseDisclaimerReviewController(ureq, getWindowControl(), getRepositoryEntry());
		listenTo(disclaimeReviewController);	
		toolbarPanel.pushController(translate("command.disclaimer"), disclaimeReviewController);
	}
	
	private void launchGlossary(UserRequest ureq) {
		// start glossary in window
		ICourse course = CourseFactory.loadCourse(getRepositoryEntry());
		final CourseConfig cc = course.getCourseConfig(); // do not cache cc, not save
		final boolean allowGlossaryEditing = reSecurity.isEntryAdmin() || hasCourseRight(CourseRights.RIGHT_GLOSSARY);
		
		// if glossary had been opened from LR as Tab before, warn user:
		DTabs dts = Windows.getWindows(ureq).getWindow(ureq).getDTabs();
		RepositoryEntry repoEntry = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(cc.getGlossarySoftKey(), false);
		DTab dt = dts.getDTab(repoEntry.getOlatResource());
		if (dt != null) {
			List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromResourceType(allowGlossaryEditing ? "true" : "false");
			dts.activate(ureq, dt, entries);
		} else {
			ControllerCreator ctrlCreator = (lureq, lwControl) -> {
				GlossaryMainController glossaryController = CourseGlossaryFactory.createCourseGlossaryMainRunController(lwControl, lureq, cc, allowGlossaryEditing);
				listenTo(glossaryController);
				if (glossaryController == null) {
					// happens in the unlikely event of a user who is in a course and
					// now
					// tries to access the glossary
					String text = translate("error.noglossary");
					return MessageUIFactory.createInfoMessage(lureq, lwControl, null, text);
				} else {
					// use a one-column main layout
					LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(lureq, lwControl, glossaryController);
					// dispose glossary on layout dispose
					layoutCtr.addDisposableChildController(glossaryController);
					return layoutCtr;
				}
			};

			ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
			// open in new browser window
			openInNewBrowserWindow(ureq, layoutCtrlr, false);
		}
	}
	
	@Override
	protected void processClosedUnclosedEvent(UserRequest ureq) {
		super.processClosedUnclosedEvent(ureq);
		
		RunMainController runCtrl = getRunMainController();
		if(runCtrl != null && runCtrl.getCurrentCourseNode() != null) {
			runCtrl.updateCurrentCourseNode(ureq);
		}
	}

	private void processCourseConfigEvent(CourseConfigEvent event) {
		switch(event.getType()) {
			case search: {
				if(searchLink != null) {
					ICourse course = CourseFactory.loadCourse(getRepositoryEntry());
					CourseConfig cc = course.getCourseEnvironment().getCourseConfig();
					searchLink.setVisible(cc.isCourseSearchEnabled());
					toolbarPanel.setDirty(true);
				}
				break;
			}
			case calendar: {
				if(calendarLink != null) {
					ICourse course = CourseFactory.loadCourse(getRepositoryEntry());
					CourseConfig cc = course.getCourseEnvironment().getCourseConfig();
					calendarLink.setVisible(cc.isCalendarEnabled() && calendarModule.isEnabled() && calendarModule.isEnableCourseToolCalendar());
					toolbarPanel.setDirty(true);
				}
				break;
			}
			case participantList: {
				if(participantListLink != null) {
					ICourse course = CourseFactory.loadCourse(getRepositoryEntry());
					CourseConfig cc = course.getCourseEnvironment().getCourseConfig();
					participantListLink.setVisible(cc.isParticipantListEnabled());
					toolbarPanel.setDirty(true);
				}
				break;
			}
			case participantInfo: {
				if(participantInfoLink != null) {
					ICourse course = CourseFactory.loadCourse(getRepositoryEntry());
					CourseConfig cc = course.getCourseEnvironment().getCourseConfig();
					participantInfoLink.setVisible(cc.isParticipantInfoEnabled());
					toolbarPanel.setDirty(true);
				}
				break;
			}
			case email: {
				if(emailLink != null) {
					ICourse course = CourseFactory.loadCourse(getRepositoryEntry());
					CourseConfig cc = course.getCourseEnvironment().getCourseConfig();
					emailLink.setVisible(cc.isEmailEnabled());
					toolbarPanel.setDirty(true);
				}
				break;
			}
			case teams: {
				if(teamsLink != null) {
					ICourse course = CourseFactory.loadCourse(getRepositoryEntry());
					CourseConfig cc = course.getCourseEnvironment().getCourseConfig();
					teamsLink.setVisible(cc.isTeamsEnabled());
					toolbarPanel.setDirty(true);
				}
				break;
			}
			case bigbluebutton: {
				if(bigBlueButtonLink != null) {
					ICourse course = CourseFactory.loadCourse(getRepositoryEntry());
					CourseConfig cc = course.getCourseEnvironment().getCourseConfig();
					bigBlueButtonLink.setVisible(cc.isBigBlueButtonEnabled());
					toolbarPanel.setDirty(true);
				}
				break;
			}
			case zoom: {
				if (zoomLink != null) {
					ICourse course = CourseFactory.loadCourse(getRepositoryEntry());
					CourseConfig cc = course.getCourseEnvironment().getCourseConfig();
					zoomLink.setVisible(cc.isZoomEnabled() && zoomModule.isEnabled() && zoomModule.isEnabledForCourseTool());
					toolbarPanel.setDirty(true);
				}
				break;
			}
			case blog: {
				if(blogLink != null) {
					ICourse course = CourseFactory.loadCourse(getRepositoryEntry());
					CourseConfig cc = course.getCourseEnvironment().getCourseConfig();
					blogLink.setVisible(cc.isBlogEnabled());
					toolbarPanel.setDirty(true);
				}
				break;
			}
			case wiki: {
				if(wikiLink != null) {
					ICourse course = CourseFactory.loadCourse(getRepositoryEntry());
					CourseConfig cc = course.getCourseEnvironment().getCourseConfig();
					wikiLink.setVisible(cc.isWikiEnabled());
					toolbarPanel.setDirty(true);
				}
				break;
			}
			case forum: {
				if(forumLink != null) {
					ICourse course = CourseFactory.loadCourse(getRepositoryEntry());
					CourseConfig cc = course.getCourseEnvironment().getCourseConfig();
					forumLink.setVisible(cc.isForumEnabled());
					toolbarPanel.setDirty(true);
				}
				break;
			}
			case documents: {
				if(documentsLink != null) {
					ICourse course = CourseFactory.loadCourse(getRepositoryEntry());
					CourseConfig cc = course.getCourseEnvironment().getCourseConfig();
					documentsLink.setVisible(cc.isDocumentsEnabled());
					toolbarPanel.setDirty(true);
				}
				break;
			}
			case coachFolder: {
				if(coachFolderLink != null) {
					ICourse course = CourseFactory.loadCourse(getRepositoryEntry());
					CourseConfig cc = course.getCourseEnvironment().getCourseConfig();
					coachFolderLink.setVisible(cc.isCoachFolderEnabled());
					toolbarPanel.setDirty(true);
				}
				break;
			}
			case chat: {
				if(chatLink != null) {
					ICourse course = CourseFactory.loadCourse(getRepositoryEntry());
					CourseConfig cc = course.getCourseEnvironment().getCourseConfig();
					chatLink.setVisible(cc.isChatEnabled());
					toolbarPanel.setDirty(true);
				}
				break;
			}
			case efficiencyStatement: {
				if(efficiencyStatementsLink != null) {
					ICourse course = CourseFactory.loadCourse(getRepositoryEntry());
					CourseConfig cc = course.getCourseEnvironment().getCourseConfig();
					efficiencyStatementsLink.setVisible(cc.isEfficencyStatementEnabled() || certificatesManager.isCertificateEnabled(getRepositoryEntry()));
					toolbarPanel.setDirty(true);
				}
				break;
			}
			case glossary: {
				if(glossary != null) {
					ICourse course = CourseFactory.loadCourse(getRepositoryEntry());
					CourseConfig cc = course.getCourseEnvironment().getCourseConfig();
					glossary.setVisible(cc.hasGlossary() && cc.isGlossaryEnabled());
					toolbarPanel.setDirty(true);
				}
				break;
			}
			case layout: {
				//don't restart for that
				break;
			}
			case completionType: {
				doDisposeAfterEvent();
				break;
			}
		}
	}
	
	private void processBusinessGroupModifiedEvent(UserRequest ureq, BusinessGroupModifiedEvent bgme) {
		Identity identity = getIdentity();
		// only do something if this identity is affected by change and the action
		// was adding or removing of the user
		if (bgme.wasMyselfAdded(identity) || bgme.wasMyselfRemoved(identity)) {
			reloadSecurity(ureq);
			reloadGroupMemberships();
			initToolbar();
		} else if (bgme.getCommand().equals(BusinessGroupModifiedEvent.GROUPRIGHTS_MODIFIED_EVENT)) {
			// check if this affects a right group where the user does participate.
			// if so, we need to rebuild the toolboxes
			UserCourseEnvironmentImpl uce = getUserCourseEnvironment();
			if (uce != null && (PersistenceHelper.listContainsObjectByKey(uce.getParticipatingGroups(), bgme.getModifiedGroupKey()) ||
					PersistenceHelper.listContainsObjectByKey(uce.getCoachedGroups(), bgme.getModifiedGroupKey()))) {
				reloadSecurity(ureq);
				reloadGroupMemberships();
				initToolbar();
			}
		}
	}
	
	@Override
	protected void processEntryChangedEvent(EntryChangedEvent repoEvent) {
		switch(repoEvent.getChange()) {
			case modifiedAtPublish:
			case modifiedAccess:
				processEntryAccessChanged(repoEvent);
				break;
			case deleted:
				doDisposeAfterEvent();
				break;
			default:
				super.processEntryChangedEvent(repoEvent);
				break;
		}
	}
	
	private void processEntryAccessChanged(EntryChangedEvent repoEvent) {
		if(repoEvent.isMe(getIdentity())) {
			//author is not affected
		} else {
			loadRepositoryEntry();
			reSecurity.setWrappedSecurity(repositoryManager.isAllowed(getIdentity(), roles, getRepositoryEntry()));
			if(reSecurity.canLaunch()) {
				reloadStatus();
				loadRights();
			} else {
				doDisposeAfterEvent();
				loadRights();
				initToolbar();
			}
		}
		issueBadgesAutomatically();
	}

	private void issueBadgesAutomatically() {
		openBadgesManager.issueBadgesAutomatically(getRepositoryEntry(), getIdentity());
	}

	private void processChangeAssessmentModeEvents(AssessmentModeNotificationEvent event) {
		try {
			// only show the warning box if an assessment mode is started
			if(assessmentLink != null && !toolbarPanel.hasMessage()
					&& getRepositoryEntry().getKey().equals(event.getAssessementMode().getRepositoryEntryKey())
					&& (Status.leadtime == event.getAssessementMode().getStatus() || Status.assessment == event.getAssessementMode().getStatus())) {
				setAssessmentModeMessage(new SyntheticUserRequest(getIdentity(), getLocale()));
			}
		} catch (Exception e) {
			logError("", e);
		}
	}
	
	private void processChangeAssessmentModeEvents(ChangeAssessmentModeEvent event) {
		try {
			// only show the warning box if an assessment mode is started
			if(assessmentLink != null && !toolbarPanel.hasMessage()
					&& getRepositoryEntry().getKey().equals(event.getEntryKey())
					&& (Status.leadtime == event.getStatus() || Status.assessment == event.getStatus())) {
				setAssessmentModeMessage(new SyntheticUserRequest(getIdentity(), getLocale()));
			}
		} catch (Exception e) {
			logError("", e);
		}
	}
	
	private void updateCurrentUserCount() {
		if (currentUserCountLink == null) {
			// either called from event handler, not initialized yet
			// or display of user count is not configured
			return;
		}
			
		int currentUserCount = coordinatorManager.getCoordinator().getEventBus().getListeningIdentityCntFor(getOlatResourceable());
		if(currentUserCount == 0) {
			currentUserCount = 1;
		}
			
		currentUserCountLink.setCustomDisplayText(translate("participants.in.course", new String[]{String.valueOf(currentUserCount)}));
	}
	
	private void doReloadCurrentCourseNode(UserRequest ureq) {
		RunMainController runCtrl = getRunMainController();
		if (runCtrl != null) {
			runCtrl.updateCurrentCourseNode(ureq);
		}
	}

	private void doDisposeAfterEvent() {
		RunMainController run = getRunMainController();
		if(run != null) {
			run.doDisposeAfterEvent();
		}
	}
	
	private void doCopyWithWizard(UserRequest ureq) {
		removeAsListenerAndDispose(copyWrapperCtrl);

		copyWrapperCtrl = new CopyRepositoryEntryWrapperController(ureq, getWindowControl(), re, true);
		listenTo(copyWrapperCtrl);
	}
	
	private enum Delayed {
		archive,
		assessmentMode,
		assessmentSurveyStatistics,
		assessmentTestStatistics,
		assessmentTool,
		badges,
		myBadges,
		issuedBadges,
		reminders,
		lecturesAdmin,
		lectures,
		courseAreas,
		courseFolder,
		coachFolder,
		courseStatistics,
		databases,
		details,
		settings,
		learningPath,
		learningPaths,
		efficiencyStatements,
		members,
		orders,
		close,
		pop,
		participantList,
		participantInfo,
		email,
		blog,
		wiki,
		forum,
		documents,
		teams,
		bigbluebutton,
		zoom
	}
}