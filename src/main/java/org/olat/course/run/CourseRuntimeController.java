package org.olat.course.run;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.NewControllerFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.controllers.linkchooser.CustomLinkTreeModel;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.commons.modules.bc.FolderRunController;
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
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.dtabs.DTab;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.OLATSecurityException;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.prefs.Preferences;
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
import org.olat.course.config.CourseConfig;
import org.olat.course.config.CourseConfigEvent;
import org.olat.course.config.ui.CourseOptionsController;
import org.olat.course.config.ui.courselayout.CourseLayoutGeneratorController;
import org.olat.course.db.CourseDBManager;
import org.olat.course.db.CustomDBMainController;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.groupsandrights.CourseRights;
import org.olat.course.member.MembersManagementMainController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.calendar.CourseCalendarController;
import org.olat.course.run.glossary.CourseGlossaryFactory;
import org.olat.course.run.glossary.CourseGlossaryToolLinkController;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.course.statistic.StatisticCourseNodesController;
import org.olat.course.statistic.StatisticMainController;
import org.olat.course.tree.CourseInternalLinkTreeModel;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupRef;
import org.olat.group.BusinessGroupService;
import org.olat.group.ui.edit.BusinessGroupModifiedEvent;
import org.olat.ims.qti.statistics.QTIType;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.OpenInstantMessageEvent;
import org.olat.note.NoteController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.model.RepositoryEntrySecurity;
import org.olat.repository.ui.RepositoryEntryRuntimeController;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

public class CourseRuntimeController extends RepositoryEntryRuntimeController implements GenericEventListener  {
	
	private static final String JOINED = "joined";
	private static final String LEFT   = "left";
	private static final String CMD_START_GROUP_PREFIX = "cmd.group.start.ident.";
	//tools
	private Link folderLink,
		assessmentLink, archiverLink,
		courseStatisticLink, surveyStatisticLink, testStatisticLink,
		areaLink, dbLink,
		//settings
		layoutLink, optionsLink,
		//my course
		efficiencyStatementsLink, calendarLink, noteLink, chatLink,
		//glossary
		openGlossaryLink, enableGlossaryLink;
	private Link currentUserCountLink;
	private Dropdown myCourse, glossary;
	
	private CourseAreasController areasCtrl;
	private ArchiverMainController archiverCtrl;
	private CustomDBMainController databasesCtrl;
	private FolderRunController courseFolderCtrl;
	private StatisticMainController statisticsCtrl;
	private CourseOptionsController optionsToolCtr;
	private AssessmentMainController assessmentToolCtr;
	private MembersManagementMainController membersCtrl;
	private StatisticCourseNodesController statsToolCtr;
	private CourseLayoutGeneratorController courseLayoutCtrl;

	private int currentUserCount;
	private boolean isCourseCoach = false;
	private Map<String, Boolean> courseRightsCache;

	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CoordinatorManager coordinatorManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private EfficiencyStatementManager efficiencyStatementManager;
	
	public CourseRuntimeController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry re, RepositoryEntrySecurity reSecurity, RuntimeControllerCreator runtimeControllerCreator,
			boolean offerBookmark, boolean showCourseConfigLink) {
		super(ureq, wControl, re, reSecurity, runtimeControllerCreator, offerBookmark, showCourseConfigLink);
		
		ICourse course = CourseFactory.loadCourse(getOlatResourceable());

		coordinatorManager.getCoordinator().getEventBus().registerFor(this, getIdentity(), getOlatResourceable());
		// - group modification events
		coordinatorManager.getCoordinator().getEventBus().registerFor(this, getIdentity(), getRepositoryEntry());
		
		if (CourseModule.displayParticipantsCount()) {
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

	@Override
	protected boolean isCorrupted(RepositoryEntry entry) {
		try {
			CourseFactory.loadCourse(entry.getOlatResource());
			return false;
		} catch (Exception e) {
			return true;
		}
	}

	@Override
	protected void loadRights(RepositoryEntrySecurity reSecurity) {
		super.loadRights(reSecurity);
		
		ICourse course = CourseFactory.loadCourse(getOlatResourceable());
		CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
		isCourseCoach = cgm.isIdentityCourseCoach(getIdentity());
		// 3) all other rights are defined in the groupmanagement using the learning
		// group rights
		UserCourseEnvironmentImpl uce = getUserCourseEnvironment();
		if(uce != null) {
			uce.setUserRoles(isEntryAdmin, isCourseCoach);
		}
		
		List<String> rights = cgm.getRights(getIdentity());
		courseRightsCache = new HashMap<String, Boolean>();
		courseRightsCache.put(CourseRights.RIGHT_GROUPMANAGEMENT, new Boolean(rights.contains(CourseRights.RIGHT_GROUPMANAGEMENT)));
		courseRightsCache.put(CourseRights.RIGHT_COURSEEDITOR, new Boolean(rights.contains(CourseRights.RIGHT_COURSEEDITOR)));
		courseRightsCache.put(CourseRights.RIGHT_ARCHIVING, new Boolean(rights.contains(CourseRights.RIGHT_ARCHIVING)));
		courseRightsCache.put(CourseRights.RIGHT_ASSESSMENT, new Boolean(rights.contains(CourseRights.RIGHT_ASSESSMENT)));
		courseRightsCache.put(CourseRights.RIGHT_GLOSSARY, new Boolean(rights.contains(CourseRights.RIGHT_GLOSSARY)));
		courseRightsCache.put(CourseRights.RIGHT_STATISTICS, new Boolean(rights.contains(CourseRights.RIGHT_STATISTICS)));
		courseRightsCache.put(CourseRights.RIGHT_DB, new Boolean(rights.contains(CourseRights.RIGHT_DB)));
	}

	private boolean hasCourseRight(String right) {
		Boolean bool = courseRightsCache.get(right);
		return bool == null ? false : bool.booleanValue();
	}
	
	private UserCourseEnvironmentImpl getUserCourseEnvironment() {
		RunMainController run = getRunMainController();
		return run == null ? null : run.getUce();
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
		return run == null ? false: run.isInEditor();
	}
	
	private void setIsInEditor(boolean editor) {
		RunMainController run = getRunMainController();
		if(run != null) run.setInEditor(editor);
	}
	
	private void reloadGroupMemberships() {
		RunMainController run = getRunMainController();
		if(run != null) {
			run.reloadGroupMemberships();
		}
	}
	
	private void toolControllerDone(UserRequest ureq) {
		RunMainController run = getRunMainController();
		if(run != null) {
			addCustomCSS(ureq);
			run.toolCtrDone(ureq);
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
		ICourse course = CourseFactory.loadCourse(getOlatResourceable());
		CustomCSS customCSS = CourseFactory.getCustomCourseCss(ureq.getUserSession(), course.getCourseEnvironment());
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

	@Override
	protected void initToolbar(Dropdown toolsDropdown, Dropdown settingsDropdown) {
		ICourse course = CourseFactory.loadCourse(getRepositoryEntry().getOlatResource());
		UserCourseEnvironmentImpl uce = getUserCourseEnvironment();
		
		toolbarPanel.removeAllTools();
		
		initTools(toolsDropdown, course, uce);
		initSettings(settingsDropdown);
		initToolsMyCourse(course, uce);
		initGeneralTools(course);
		
		if(getRunMainController() != null) {
			getRunMainController().initToolbar();
		}
	}
	
	private void initTools(Dropdown tools, ICourse course, final UserCourseEnvironmentImpl uce) {
		// 1) administrative tools
		if (isEntryAdmin || isCourseCoach || hasCourseRight(CourseRights.RIGHT_COURSEEDITOR)
				|| hasCourseRight(CourseRights.RIGHT_GROUPMANAGEMENT) || hasCourseRight(CourseRights.RIGHT_ARCHIVING)
				|| hasCourseRight(CourseRights.RIGHT_STATISTICS) || hasCourseRight(CourseRights.RIGHT_DB)
				|| hasCourseRight(CourseRights.RIGHT_ASSESSMENT)) {

			tools = new Dropdown("editTools", "header.tools", false, getTranslator());
			tools.setElementCssClass("o_sel_course_tools");
			tools.setIconCSS("o_icon o_icon_tools");

			if (hasCourseRight(CourseRights.RIGHT_COURSEEDITOR) || isEntryAdmin) {

				boolean managed = RepositoryEntryManagedFlag.isManaged(getRepositoryEntry(), RepositoryEntryManagedFlag.editcontent);
				editLink = LinkFactory.createToolLink("edit.cmd", translate("command.openeditor"), this, "o_icon_courseeditor");
				editLink.setElementCssClass("o_sel_course_editor");
				editLink.setEnabled(!corrupted && !managed);
				tools.addComponent(editLink);
				
				folderLink = LinkFactory.createToolLink("cfd", translate("command.coursefolder"), this, "o_icon_coursefolder");
				folderLink.setElementCssClass("o_sel_course_folder");
				tools.addComponent(folderLink);
				tools.addComponent(new Spacer(""));
			}
			
			if (hasCourseRight(CourseRights.RIGHT_GROUPMANAGEMENT) || isEntryAdmin) {
				membersLink = LinkFactory.createToolLink("unifiedusermngt", translate("command.opensimplegroupmngt"), this, "o_icon_membersmanagement");
				tools.addComponent(membersLink);
			}
			if (hasCourseRight(CourseRights.RIGHT_ASSESSMENT) || isCourseCoach || isEntryAdmin) {
				assessmentLink = LinkFactory.createToolLink("assessment",translate("command.openassessment"), this, "o_icon_assessment_tool");
				tools.addComponent(assessmentLink);
			}
			if (hasCourseRight(CourseRights.RIGHT_ARCHIVING) || isEntryAdmin) {
				archiverLink = LinkFactory.createToolLink("archiver", translate("command.openarchiver"), this, "o_icon_archive_tool");
				tools.addComponent(archiverLink);
			}
			tools.addComponent(new Spacer(""));
			
			
			if (hasCourseRight(CourseRights.RIGHT_STATISTICS) || isEntryAdmin) {
				courseStatisticLink = LinkFactory.createToolLink("statistic",translate("command.openstatistic"), this, "o_icon_statistics_tool");
				tools.addComponent(courseStatisticLink);
			}
			if (uce != null && (hasCourseRight(CourseRights.RIGHT_STATISTICS) || isEntryAdmin || isCourseCoach)) {
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

			if (hasCourseRight(CourseRights.RIGHT_COURSEEDITOR) || isEntryAdmin) {
				areaLink = LinkFactory.createToolLink("careas", translate("command.courseareas"), this, "o_icon_courseareas");
				areaLink.setElementCssClass("o_sel_course_areas");
				tools.addComponent(areaLink);
			}
			if (CourseDBManager.getInstance().isEnabled() && (hasCourseRight(CourseRights.RIGHT_DB) || isEntryAdmin)) {
				dbLink = LinkFactory.createToolLink("customDb",translate("command.opendb"), this, "o_icon_coursedb");
				tools.addComponent(dbLink);
			}
			
			ordersLink = LinkFactory.createToolLink("bookings", translate("details.orders"), this, "o_sel_repo_booking");
			ordersLink.setIconLeftCSS("o_icon o_icon-fw o_icon_booking");
			boolean booking = acService.isResourceAccessControled(getRepositoryEntry().getOlatResource(), null);
			ordersLink.setVisible(!corrupted && booking);
			tools.addComponent(ordersLink);
			
			toolbarPanel.addTool(tools, Align.left, true);
		}
	}
	
	private void initSettings(Dropdown settings) {
		if (isEntryAdmin || hasCourseRight(CourseRights.RIGHT_COURSEEDITOR)) {
			boolean managed = RepositoryEntryManagedFlag.isManaged(getRepositoryEntry(), RepositoryEntryManagedFlag.editcontent);
			
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
			settings.addComponent(catalogLink);
			
			settings.addComponent(new Spacer(""));
			
			boolean layoutManaged = RepositoryEntryManagedFlag.isManaged(getRepositoryEntry(), RepositoryEntryManagedFlag.layout);
			layoutLink = LinkFactory.createToolLink("access.cmd", translate("command.layout"), this, "o_icon_layout");
			layoutLink.setElementCssClass("o_sel_course_layout");
			layoutLink.setEnabled(!layoutManaged);
			settings.addComponent(layoutLink);
			
			optionsLink = LinkFactory.createToolLink("access.cmd", translate("command.options"), this, "o_icon_options");
			optionsLink.setElementCssClass("o_sel_course_options");
			settings.addComponent(optionsLink);

			toolbarPanel.addTool(settings, Align.left, true);
		}
	}
	
	private void initToolsMyCourse(ICourse course, UserCourseEnvironmentImpl uce) {

		myCourse = new Dropdown("myCourse", "header.tools.mycourse", false, getTranslator());
		myCourse.setIconCSS("o_icon o_icon_user");

		// Personal tools on right side
		CourseConfig cc = course.getCourseConfig();
		if (course.hasAssessableNodes() && !isGuestOnly && uce != null) {
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
						.getUserEfficiencyStatementLight(getRepositoryEntry().getKey(), getIdentity());
				efficiencyStatementsLink.setEnabled(es != null);
			}
		}
		
		if (!isGuestOnly) {
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

		if(uce != null) {
			if(myCourse.size() > 0 && (uce.getCoachedGroups().size() > 0 || uce.getParticipatingGroups().size() > 0 || uce.getWaitingLists().size() > 0)) {
				myCourse.addComponent(new Spacer(""));
			}
			
			// 2) add coached groups
			if (uce.getCoachedGroups().size() > 0) {
				for (BusinessGroup group:uce.getCoachedGroups()) {
					Link link = LinkFactory.createToolLink(CMD_START_GROUP_PREFIX + group.getKey(), "group", StringHelper.escapeHtml(group.getName()), this);
					link.setUserObject(group);
					myCourse.addComponent(link);
					
				}
			}
	
			// 3) add participating groups
			if (uce.getParticipatingGroups().size() > 0) {
				for (BusinessGroup group: uce.getParticipatingGroups()) {
					Link link = LinkFactory.createToolLink(CMD_START_GROUP_PREFIX + group.getKey(), "group", StringHelper.escapeHtml(group.getName()), this);
					link.setUserObject(group);
					myCourse.addComponent(link);
				}
			}
	
			// 5) add waiting-list groups
			if (uce.getWaitingLists().size() > 0) {
				for (BusinessGroup group:uce.getWaitingLists()) {
					int pos = businessGroupService.getPositionInWaitingListFor(getIdentity(), group);
					String name = StringHelper.escapeHtml(group.getName()) + " (" + pos + ")";
					Link link = LinkFactory.createToolLink(CMD_START_GROUP_PREFIX + group.getKey(), "group", name, this);
					link.setUserObject(group);
					link.setEnabled(false);
					myCourse.addComponent(link);
				}
			}
		}
		if(myCourse.size() > 0) {
			toolbarPanel.addTool(myCourse, Align.right);
		}
	}
	
	private void initGeneralTools(ICourse course) {
		CourseConfig cc = course.getCourseConfig();
		if (showInfos) {
			detailsLink = LinkFactory.createToolLink("courseconfig",translate("command.courseconfig"), this, "o_icon_details");
			toolbarPanel.addTool(detailsLink);
		}		
		if (!isGuestOnly) {
			calendarLink = LinkFactory.createToolLink("calendar",translate("command.calendar"), this, "o_icon_calendar");
			calendarLink.setPopup(new LinkPopupSettings(950, 750, "cal"));
			calendarLink.setVisible(cc.isCalendarEnabled());
			toolbarPanel.addTool(calendarLink);
		}
		
		glossary = new Dropdown("glossary", "command.glossary", false, getTranslator());
		glossary.setIconCSS("o_icon o_FileResource-GLOSSARY_icon");
		glossary.setVisible(cc.hasGlossary());

		openGlossaryLink = LinkFactory.createToolLink("command.glossary.open", translate("command.glossary.open"), this);
		openGlossaryLink.setPopup(new LinkPopupSettings(950, 750, "gloss"));
		glossary.addComponent(openGlossaryLink);

		enableGlossaryLink = LinkFactory.createToolLink("command.glossary.on.off", translate("command.glossary.on.alt"), this);
		glossary.addComponent(enableGlossaryLink);
		toolbarPanel.addTool(glossary);
		
		//add group chat to toolbox
		InstantMessagingModule imModule = CoreSpringFactory.getImpl(InstantMessagingModule.class);
		boolean chatIsEnabled = !isGuestOnly && imModule.isEnabled() && imModule.isCourseEnabled();
		if(chatIsEnabled) {
			chatLink = LinkFactory.createToolLink("chat",translate("command.coursechat"), this, "o_icon_chat");
			chatLink.setVisible(CourseModule.isCourseChatEnabled() && cc.isChatEnabled());
			toolbarPanel.addTool(chatLink);
		}
	}

	@Override
	protected void doDispose() {
		super.doDispose();
		
		if (CourseModule.displayParticipantsCount()) {
			coordinatorManager.getCoordinator().getEventBus().fireEventToListenersOf(new MultiUserEvent(LEFT), getOlatResourceable());
		}
	}

	@Override
	public void event(Event event) {
		if(event instanceof CourseConfigEvent) {				
			processCourseConfigEvent((CourseConfigEvent)event);
		} else if (event instanceof AssessmentChangedEvent) {
			String assessmentChangeType = event.getCommand();
			AssessmentChangedEvent ace = (AssessmentChangedEvent) event;
			if(!isGuestOnly && ace.getIdentityKey().equals(getIdentity().getKey())
					&& assessmentChangeType.equals(AssessmentChangedEvent.TYPE_EFFICIENCY_STATEMENT_CHANGED)) {
				// update tools, maybe efficiency statement link has changed
				UserEfficiencyStatement es = efficiencyStatementManager
					.getUserEfficiencyStatementLight(this.getRepositoryEntry().getKey(), this.getIdentity());
				efficiencyStatementsLink.setEnabled(es != null);
			}
		} else if (event instanceof EntryChangedEvent ) {
			EntryChangedEvent repoEvent = (EntryChangedEvent) event;
			if (getRepositoryEntry().getKey().equals(repoEvent.getChangedEntryKey())) {
				processEntryChangedEvent(repoEvent);
			}
		//All events are MultiUserEvent, check with command at the end
		} else if (event instanceof MultiUserEvent) {
			if (event.getCommand().equals(JOINED) || event.getCommand().equals(LEFT)) {
				updateCurrentUserCount();
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(layoutLink == source) {
			doLayout(ureq);
		} else if(optionsLink == source) {
			doOptions(ureq);
		} else if(archiverLink == source) {
			doArchive(ureq);
		} else if(folderLink == source) {
			doCourseFolder(ureq);
		} else if(areaLink == source) {
			doCourseAreas(ureq);
		} else if(dbLink == source) {
			doDatabases(ureq);
		} else if(courseStatisticLink == source) {
			doCourseStatistics(ureq);
		} else if(testStatisticLink == source) {
			doAssessmentStatistics(ureq, "command.openteststatistic", "TestStatistics", QTIType.test, QTIType.onyx);
		} else if(surveyStatisticLink == source) {
			doAssessmentStatistics(ureq, "command.opensurveystatistic", "SurveyStatistics", QTIType.survey);
		} else if(assessmentLink == source) {
			doAssessmentTool(ureq);
		} else if(calendarLink == source) {
			launchCalendar(ureq);
		} else if(chatLink == source) {
			launchChat(ureq);
		} else if(efficiencyStatementsLink == source) {
			launchEfficiencyStatements(ureq);
		} else if(noteLink == source) {
			launchPersonalNotes(ureq);
		} else if(openGlossaryLink == source) {
			launchGlossary(ureq);
		} else if(source instanceof Link && "group".equals(((Link)source).getCommand())) {
			BusinessGroupRef ref = (BusinessGroupRef)((Link)source).getUserObject();
			launchGroup(ureq, ref.getKey());
		} else if(source == toolbarPanel) {
			if(event instanceof PopEvent) {
				PopEvent pop = (PopEvent)event;
				if(pop.getController() != getRunMainController()) {
					toolControllerDone(ureq);
				}
			}
		} else if(enableGlossaryLink == source) {
			toggleGlossary(ureq);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(getRunMainController() == source) {
			if(event instanceof BusinessGroupModifiedEvent) {
				processBusinessGroupModifiedEvent((BusinessGroupModifiedEvent)event);
			}
		} else if (currentToolCtr == source) {
			if (event == Event.DONE_EVENT) {
				// special check for editor
				toolControllerDone(ureq);
			}
		} else if(optionsToolCtr == source) {
			if(event == Event.CHANGED_EVENT) {
				initToolbar();
				toolControllerDone(ureq);
			}
		} else if(accessCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				initToolbar();
				toolControllerDone(ureq);
			}
		}
		
		super.event(ureq, source, event);
	}
	
	@Override
	protected void cleanUp() {
		removeAsListenerAndDispose(assessmentToolCtr);
		removeAsListenerAndDispose(courseFolderCtrl);
		removeAsListenerAndDispose(courseLayoutCtrl);
		removeAsListenerAndDispose(optionsToolCtr);
		removeAsListenerAndDispose(statisticsCtrl);
		removeAsListenerAndDispose(databasesCtrl);
		removeAsListenerAndDispose(archiverCtrl);
		removeAsListenerAndDispose(statsToolCtr);
		removeAsListenerAndDispose(membersCtrl);
		removeAsListenerAndDispose(areasCtrl);
		assessmentToolCtr = null;
		courseFolderCtrl = null;
		courseLayoutCtrl = null;
		optionsToolCtr = null;
		statisticsCtrl = null;
		databasesCtrl = null;
		archiverCtrl = null;
		statsToolCtr = null;
		membersCtrl = null;
		areasCtrl = null;

		super.cleanUp();
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {

		entries = removeRepositoryEntry(entries);
		if(entries != null && entries.size() > 0) {
			String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if("Editor".equals(type)) {
				if (!isInEditor() && !RepositoryEntryManagedFlag.isManaged(getRepositoryEntry(), RepositoryEntryManagedFlag.editcontent)) {
					doEdit(ureq);
				}
			} else if("Catalog".equals(type)) {
				doCatalog(ureq);
			} else if("Infos".equals(type)) {
				doDetails(ureq);	
			} else if("EditDescription".equals(type)) {
				doEditSettings(ureq);
			} else if("MembersMgmt".equals(type)) {
				Activateable2 members = doMembers(ureq);
				if(members != null) {
					try {
						List<ContextEntry> subEntries = entries.subList(1, entries.size());
						members.activate(ureq, subEntries, entries.get(0).getTransientState());
					} catch (OLATSecurityException e) {
						//the wrong link to the wrong person
					}
				}	
			} else if ("assessmentTool".equalsIgnoreCase(type)) {
				//check the security before, the link is perhaps in the wrong hands
				if(hasCourseRight(CourseRights.RIGHT_ASSESSMENT) || isEntryAdmin || isCourseCoach) {
					try {
						Activateable2 assessmentCtrl = doAssessmentTool(ureq);
						List<ContextEntry> subEntries;
						if(entries.size() > 1 && entries.get(1).getOLATResourceable().getResourceableTypeName().equals(type)) {
							subEntries = entries.subList(2, entries.size());
						} else {
							subEntries = entries.subList(1, entries.size());
						}
						assessmentCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
					} catch (OLATSecurityException e) {
						//the wrong link to the wrong person
					}
				}
			} else if ("TestStatistics".equalsIgnoreCase(type) || "SurveyStatistics".equalsIgnoreCase(type)) {
				//check the security before, the link is perhaps in the wrong hands
				if(hasCourseRight(CourseRights.RIGHT_ASSESSMENT) || isEntryAdmin || isCourseCoach) {
					try {
						Activateable2 assessmentCtrl = null;
						if("TestStatistics".equalsIgnoreCase(type)) {
							assessmentCtrl = doAssessmentStatistics(ureq, "command.openteststatistic", "TestStatistics", QTIType.test, QTIType.onyx);
						} else {
							assessmentCtrl = doAssessmentStatistics(ureq, "command.opensurveystatistic", "SurveyStatistics", QTIType.survey);
						}
						
						List<ContextEntry> subEntries;
						if(entries.size() > 1 && entries.get(1).getOLATResourceable().getResourceableTypeName().equals(type)) {
							subEntries = entries.subList(2, entries.size());
						} else {
							subEntries = entries.subList(1, entries.size());
						}
						assessmentCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
					} catch (OLATSecurityException e) {
						//the wrong link to the wrong person
					}
				}
			} 
		}

		if(getRunMainController() != null) {
			getRunMainController().activate(ureq, entries, state);
		}

		//super.activate(ureq, entries, state);
	}

	@Override
	protected void doAccess(UserRequest ureq) {
		if (hasCourseRight(CourseRights.RIGHT_COURSEEDITOR) || isEntryAdmin) {
			removeCustomCSS(ureq);
			super.doAccess(ureq);
		}
	}

	@Override
	protected void doEdit(UserRequest ureq) {
		if (hasCourseRight(CourseRights.RIGHT_COURSEEDITOR) || isEntryAdmin) {
			removeCustomCSS(ureq);
			popToRoot(ureq);
			cleanUp();
			
			ICourse course = CourseFactory.loadCourse(getRepositoryEntry().getOlatResource());
			CourseNode currentCourseNode = getCurrentCourseNode();
			editorCtrl = CourseFactory.createEditorController(ureq, getWindowControl(), toolbarPanel, course, currentCourseNode);
			//user activity logger which was initialized with course run
			if(editorCtrl != null){
				listenTo(editorCtrl);
				setIsInEditor(true);
				currentToolCtr = editorCtrl;
			}
		}
	}

	@Override
	protected void doEditSettings(UserRequest ureq) {
		if (hasCourseRight(CourseRights.RIGHT_COURSEEDITOR) || isEntryAdmin) {
			removeCustomCSS(ureq);
			super.doEditSettings(ureq);
		}
	}

	@Override
	protected Activateable2 doMembers(UserRequest ureq) {
		if (hasCourseRight(CourseRights.RIGHT_GROUPMANAGEMENT) || isEntryAdmin) {
			removeCustomCSS(ureq);
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("MembersMgmt", 0l);
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
			MembersManagementMainController ctrl = new MembersManagementMainController(ureq, addToHistory(ureq, bwControl), getRepositoryEntry());
			listenTo(ctrl);
			membersCtrl = pushController(ureq, translate("command.opensimplegroupmngt"), ctrl);
			currentToolCtr = membersCtrl;
		}
		return membersCtrl;
	}

	@Override
	protected void doCatalog(UserRequest ureq) {
		if (hasCourseRight(CourseRights.RIGHT_COURSEEDITOR) || isEntryAdmin) {
			super.doCatalog(ureq);
		}
	}

	private void doLayout(UserRequest ureq) {
		if (hasCourseRight(CourseRights.RIGHT_COURSEEDITOR) || isEntryAdmin) {
			removeCustomCSS(ureq);
			ICourse course = CourseFactory.loadCourse(getRepositoryEntry().getOlatResource());
			boolean managedLayout = RepositoryEntryManagedFlag.isManaged(getRepositoryEntry(), RepositoryEntryManagedFlag.layout);
			CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig().clone();
			CourseLayoutGeneratorController ctrl = new CourseLayoutGeneratorController(ureq, getWindowControl(), course, courseConfig,
			  		course.getCourseEnvironment(), !managedLayout);
			listenTo(ctrl);
			courseLayoutCtrl = pushController(ureq, translate("command.layout"), ctrl);
			currentToolCtr = courseLayoutCtrl;
		}
	}
	
	private void doOptions(UserRequest ureq) {
		if (hasCourseRight(CourseRights.RIGHT_COURSEEDITOR) || isEntryAdmin) {
			removeCustomCSS(ureq);
			ICourse course = CourseFactory.loadCourse(getOlatResourceable());
			CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig().clone();
			CourseOptionsController ctrl = new CourseOptionsController(ureq, getWindowControl(), getRepositoryEntry(), courseConfig, true);
			optionsToolCtr = pushController(ureq, translate("command.options"), ctrl);
			currentToolCtr = optionsToolCtr;
		}
	}
	
	private void doArchive(UserRequest ureq) {
		if (hasCourseRight(CourseRights.RIGHT_ARCHIVING) || isEntryAdmin) {
			removeCustomCSS(ureq);
			ICourse course = CourseFactory.loadCourse(getOlatResourceable());
			ArchiverMainController ctrl = new ArchiverMainController(ureq, getWindowControl(), course, new FullAccessArchiverCallback());
			listenTo(ctrl);
			archiverCtrl = pushController(ureq, translate("command.openarchiver"), ctrl);
			currentToolCtr = archiverCtrl;
		}
	}
	
	private void doCourseFolder(UserRequest ureq) {
		removeCustomCSS(ureq);
		// Folder for course with custom link model to jump to course nodes
		ICourse course = CourseFactory.loadCourse(getOlatResourceable());
		VFSContainer namedCourseFolder = new NamedContainerImpl(translate("command.coursefolder"), course.getCourseFolderContainer());
		CustomLinkTreeModel customLinkTreeModel = new CourseInternalLinkTreeModel(course.getEditorTreeModel());
		
		FolderRunController ctrl = new FolderRunController(namedCourseFolder, true, true, true, ureq, getWindowControl(), null, customLinkTreeModel);
		ctrl.addLoggingResourceable(LoggingResourceable.wrap(course));
		courseFolderCtrl = pushController(ureq, translate("command.coursefolder"), ctrl);
		currentToolCtr = courseFolderCtrl;
	}
	
	private void doCourseAreas(UserRequest ureq) {
		removeCustomCSS(ureq);
		ICourse course = CourseFactory.loadCourse(getOlatResourceable());
		CourseAreasController ctrl = new CourseAreasController(ureq, getWindowControl(), getRepositoryEntry().getOlatResource());
		ctrl.addLoggingResourceable(LoggingResourceable.wrap(course));
		areasCtrl = pushController(ureq, translate("command.courseareas"), ctrl);
		currentToolCtr = areasCtrl;
	}
	
	private void doDatabases(UserRequest ureq) {
		if (hasCourseRight(CourseRights.RIGHT_DB) || isEntryAdmin) {
			removeCustomCSS(ureq);
			ICourse course = CourseFactory.loadCourse(getOlatResourceable());
			CustomDBMainController ctrl = new CustomDBMainController(ureq, getWindowControl(), course);
			listenTo(ctrl);
			databasesCtrl = pushController(ureq, translate("command.opendb"), ctrl);
			currentToolCtr = databasesCtrl;
		}
	}
	
	private void doCourseStatistics(UserRequest ureq) {
		if (hasCourseRight(CourseRights.RIGHT_STATISTICS) || isEntryAdmin) {
			removeCustomCSS(ureq);
			ICourse course = CourseFactory.loadCourse(getOlatResourceable());
			StatisticMainController ctrl = new StatisticMainController(ureq, getWindowControl(), course);
			listenTo(ctrl);
			statisticsCtrl = pushController(ureq, translate("command.openstatistic"), ctrl);
			currentToolCtr = statisticsCtrl;
		}
	}
	
	private Activateable2 doAssessmentStatistics(UserRequest ureq, String i18nCrumbKey, String typeName, QTIType... types) {
		OLATResourceable ores = OresHelper.createOLATResourceableType(typeName);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl swControl = addToHistory(ureq, ores, null);
		if (hasCourseRight(CourseRights.RIGHT_STATISTICS) || isEntryAdmin || isCourseCoach) {
			removeCustomCSS(ureq);
			UserCourseEnvironmentImpl uce = getUserCourseEnvironment();
			StatisticCourseNodesController ctrl = new StatisticCourseNodesController(ureq, swControl, toolbarPanel, uce, types);
			listenTo(ctrl);
			statsToolCtr = pushController(ureq, translate(i18nCrumbKey), ctrl);
			currentToolCtr = statsToolCtr;
			return statsToolCtr;
		}
		return null;
	}
	
	private Activateable2 doAssessmentTool(UserRequest ureq) {
		OLATResourceable ores = OresHelper.createOLATResourceableType("assessmentTool");
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl swControl = addToHistory(ureq, ores, null);
		
		// 1) course admins and users with tool right: full access
		if (hasCourseRight(CourseRights.RIGHT_ASSESSMENT) || isEntryAdmin) {
			removeCustomCSS(ureq);
			AssessmentMainController ctrl = new AssessmentMainController(ureq, swControl, toolbarPanel,
					getOlatResourceable(), new FullAccessAssessmentCallback(isEntryAdmin));
			ctrl.activate(ureq, null, null);
			listenTo(ctrl);
			assessmentToolCtr = pushController(ureq, translate("command.openassessment"), ctrl);
			currentToolCtr = assessmentToolCtr;
			return assessmentToolCtr;
		}
		// 2) users with coach right: limited access to coached groups
		if (isCourseCoach) {
			removeCustomCSS(ureq);
			AssessmentMainController ctrl = new AssessmentMainController(ureq, swControl, toolbarPanel,
					getOlatResourceable(), new CoachingGroupAccessAssessmentCallback());
			ctrl.activate(ureq, null, null);
			listenTo(ctrl);
			assessmentToolCtr = pushController(ureq, translate("command.openassessment"), ctrl);
			currentToolCtr = assessmentToolCtr;
			return assessmentToolCtr;
		}
		return null;
	}
	
	private void toggleGlossary(UserRequest ureq) {
		// enable / disable glossary highlighting according to user prefs
		Preferences prefs = ureq.getUserSession().getGuiPreferences();
		ICourse course = CourseFactory.loadCourse(getOlatResourceable());
		Boolean state = (Boolean) prefs.get(CourseGlossaryToolLinkController.class, CourseGlossaryFactory.createGuiPrefsKey(course));
		Boolean newState = state == null ? Boolean.TRUE : new Boolean(!state.booleanValue());
		setGlossaryLinkTitle(ureq, newState);
	}
	
	private void setGlossaryLinkTitle(UserRequest ureq, Boolean state) {
		String oresName = ICourse.class.getSimpleName();
		Long courseID = getOlatResourceable().getResourceableId();
		// must work with SP and CP nodes, IFrameDisplayController listens to this event and expects "ICourse" resources.
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(oresName, courseID);
		if (state == null || !state.booleanValue()) {
			enableGlossaryLink.setCustomDisplayText(translate("command.glossary.on.alt"));
			enableGlossaryLink.setUserObject(Boolean.TRUE);
			setTextMarkingEnabled(false);
			ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(new MultiUserEvent("glossaryOff"), ores);
		} else {
			enableGlossaryLink.setCustomDisplayText(translate("command.glossary.off.alt"));
			enableGlossaryLink.setUserObject(Boolean.FALSE);
			setTextMarkingEnabled(true);
			ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(new MultiUserEvent("glossaryOn"), ores);
		}
	}
	
	@Override
	protected void launchContent(UserRequest ureq, RepositoryEntrySecurity reSecurity) {
		super.launchContent(ureq, reSecurity);
		if(getRunMainController() != null) {
			addCustomCSS(ureq);
			getRunMainController().initToolbar();
		}
	}

	private void launchChat(UserRequest ureq) {
		boolean vip = isCourseCoach || isEntryAdmin;
		ICourse course = CourseFactory.loadCourse(getRepositoryEntry().getOlatResource());
		OpenInstantMessageEvent event = new OpenInstantMessageEvent(ureq, course, course.getCourseTitle(), vip);
		ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(event, InstantMessagingService.TOWER_EVENT_ORES);
	}
	
	
	private void launchCalendar(UserRequest ureq) {
		ControllerCreator ctrlCreator = new ControllerCreator() {
			public Controller createController(UserRequest lureq, WindowControl lwControl) {
				ICourse course = CourseFactory.loadCourse(getRepositoryEntry().getOlatResource());
				ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(getRepositoryEntry());
				WindowControl llwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, lwControl);
				CourseCalendarController calendarController = new CourseCalendarController(lureq, llwControl, course);					
				// use a one-column main layout
				LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(lureq, llwControl, calendarController);
				layoutCtr.setCustomCSS(CourseFactory.getCustomCourseCss(lureq.getUserSession(), course.getCourseEnvironment()));
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
	
	private void launchEfficiencyStatements(UserRequest ureq) {
		// will not be disposed on course run dispose, popus up as new browserwindow
		ControllerCreator ctrlCreator = new ControllerCreator() {
			public Controller createController(UserRequest lureq, WindowControl lwControl) {
				ICourse course = CourseFactory.loadCourse(getRepositoryEntry().getOlatResource());
				EfficiencyStatementController efficiencyStatementController = new EfficiencyStatementController(lwControl, lureq, getRepositoryEntry().getKey());
				LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(lureq, getWindowControl(), efficiencyStatementController);
				layoutCtr.setCustomCSS(CourseFactory.getCustomCourseCss(lureq.getUserSession(), course.getCourseEnvironment()));
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
			reloadGroupMemberships();
		}
	}
	
	private void launchPersonalNotes(UserRequest ureq) {
		// will not be disposed on course run dispose, popus up as new browserwindow
		ControllerCreator ctrlCreator = new ControllerCreator() {
			public Controller createController(UserRequest lureq, WindowControl lwControl) {
				ICourse course = CourseFactory.loadCourse(getRepositoryEntry().getOlatResource());
				Controller notesCtr = new NoteController(lureq, course, course.getCourseTitle(), lwControl);
				LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(lureq, lwControl, notesCtr);
				layoutCtr.setCustomCSS(CourseFactory.getCustomCourseCss(lureq.getUserSession(), course.getCourseEnvironment()));
				layoutCtr.addDisposableChildController(notesCtr); // dispose glossary on layout dispose
				return layoutCtr;
			}					
		};
		//wrap the content controller into a full header layout
		ControllerCreator popupLayoutCtr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
		//open in new browser window
		openInNewBrowserWindow(ureq, popupLayoutCtr);
	}
	
	private void launchGlossary(UserRequest ureq) {
		// start glossary in window
		ICourse course = CourseFactory.loadCourse(getOlatResourceable());
		final CourseConfig cc = course.getCourseConfig(); // do not cache cc, not save
		final boolean allowGlossaryEditing = hasCourseRight(CourseRights.RIGHT_GLOSSARY) || isEntryAdmin;
		
		// if glossary had been opened from LR as Tab before, warn user:
		DTabs dts = Windows.getWindows(ureq).getWindow(ureq).getDTabs();
		RepositoryEntry repoEntry = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(cc.getGlossarySoftKey(), false);
		DTab dt = dts.getDTab(repoEntry.getOlatResource());
		if (dt != null) {
			List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromResourceType(allowGlossaryEditing ? "true" : "false");
			dts.activate(ureq, dt, entries);
		} else {
			ControllerCreator ctrlCreator = new ControllerCreator() {
				public Controller createController(UserRequest lureq, WindowControl lwControl) {
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
				}
			};

			ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
			// open in new browser window
			openInNewBrowserWindow(ureq, layoutCtrlr);
			return;// immediate return after opening new browser window!
		}
	}

	private void processCourseConfigEvent(CourseConfigEvent event) {
		switch(event.getType()) {
			case calendar: {
				if(calendarLink != null) {
					ICourse course = CourseFactory.loadCourse(getOlatResourceable());
					CourseConfig cc = course.getCourseEnvironment().getCourseConfig();
					calendarLink.setVisible(cc.isCalendarEnabled());
					toolbarPanel.setDirty(true);
				}
				break;
			}
			case chat: {
				if(chatLink != null) {
					ICourse course = CourseFactory.loadCourse(getOlatResourceable());
					CourseConfig cc = course.getCourseEnvironment().getCourseConfig();
					chatLink.setVisible(cc.isChatEnabled());
					toolbarPanel.setDirty(true);
				}
				break;
			}
			case efficiencyStatement: {
				if(efficiencyStatementsLink != null) {
					ICourse course = CourseFactory.loadCourse(getOlatResourceable());
					CourseConfig cc = course.getCourseEnvironment().getCourseConfig();
					efficiencyStatementsLink.setVisible(cc.isEfficencyStatementEnabled());
					if(cc.isEfficencyStatementEnabled()) {
						UserEfficiencyStatement es = efficiencyStatementManager
								.getUserEfficiencyStatementLight(getRepositoryEntry().getKey(), getIdentity());
						efficiencyStatementsLink.setEnabled(es != null);
					}
					toolbarPanel.setDirty(true);
				}
				break;
			}
			case glossary: {
				if(glossary != null) {
					ICourse course = CourseFactory.loadCourse(getOlatResourceable());
					CourseConfig cc = course.getCourseEnvironment().getCourseConfig();
					glossary.setVisible(cc.hasGlossary());
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
		Identity identity = getIdentity();
		// only do something if this identity is affected by change and the action
		// was adding or removing of the user
		if (bgme.wasMyselfAdded(identity) || bgme.wasMyselfRemoved(identity)) {
			reloadGroupMemberships();
			RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(getIdentity(), roles, getRepositoryEntry());
			loadRights(reSecurity);
			initToolbar();
		} else if (bgme.getCommand().equals(BusinessGroupModifiedEvent.GROUPRIGHTS_MODIFIED_EVENT)) {
			// check if this affects a right group where the user does participate.
			// if so, we need to rebuild the toolboxes
			UserCourseEnvironmentImpl uce = getUserCourseEnvironment();
			if (uce != null && (PersistenceHelper.listContainsObjectByKey(uce.getParticipatingGroups(), bgme.getModifiedGroupKey()) ||
					PersistenceHelper.listContainsObjectByKey(uce.getCoachedGroups(), bgme.getModifiedGroupKey()))) {

				reloadGroupMemberships();
				RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(getIdentity(), roles, getRepositoryEntry());
				loadRights(reSecurity);
				initToolbar();
			}
		}
	}
	
	private void processEntryChangedEvent(EntryChangedEvent repoEvent) {
		switch(repoEvent.getChange()) {
			case modifiedAtPublish:
			case modifiedAccess:
				processEntryAccessChanged(repoEvent);
				break;
			case deleted:
				doDisposeAfterEvent();
				break;
			default: {}
		}
	}
	
	private void processEntryAccessChanged(EntryChangedEvent repoEvent) {
		if(repoEvent.getAuthorKey() != null && getIdentity().getKey().equals(repoEvent.getAuthorKey())) {
			//author is not affected
		} else {
			loadRepositoryEntry();
			RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(getIdentity(), roles, getRepositoryEntry());
			if(reSecurity.canLaunch()) {
				loadRights(reSecurity);
			} else {
				doDisposeAfterEvent();
				loadRights(reSecurity);
				initToolbar();
			}
		}
	}
	
	private void updateCurrentUserCount() {
		if (currentUserCountLink == null) {
			// either called from event handler, not initialized yet
			// or display of user count is not configured
			return;
		}
			
		currentUserCount = coordinatorManager.getCoordinator().getEventBus().getListeningIdentityCntFor(getOlatResourceable());
		if(currentUserCount == 0) {
			currentUserCount = 1;
		}
			
		currentUserCountLink.setCustomDisplayText(translate("participants.in.course", new String[]{String.valueOf(currentUserCount)}));
	}

	private void doDisposeAfterEvent() {
		RunMainController run = getRunMainController();
		if(run != null) {
			run.doDisposeAfterEvent();
		}
	}
}