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
package org.olat.modules.coach.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.admin.user.UserChangePasswordController;
import org.olat.admin.user.UserShortDescription;
import org.olat.admin.user.groups.GroupOverviewController;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.model.CalendarUserConfiguration;
import org.olat.commons.calendar.ui.CalendarController;
import org.olat.commons.calendar.ui.WeeklyCalendarController;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.tabbedpane.TabbedPaneChangedEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.spacesaver.ToggleBoxController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.ui.CertificateAndEfficiencyStatementListController;
import org.olat.modules.co.ContactFormController;
import org.olat.modules.coach.RoleSecurityCallback;
import org.olat.modules.coach.model.StudentStatEntry;
import org.olat.modules.coach.ui.curriculum.certificate.CertificateAndEfficiencyStatementWrapperController;
import org.olat.modules.coach.ui.curriculum.course.CourseListWrapperController;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumSecurityCallbackFactory;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.ui.ParticipantLecturesOverviewController;
import org.olat.resource.accesscontrol.ui.UserOrderController;
import org.olat.user.DisplayPortraitController;
import org.olat.user.HomePageConfig;
import org.olat.user.HomePageConfigManager;
import org.olat.user.HomePageDisplayController;
import org.olat.user.ProfileAndHomePageEditController;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Gives an overview to a specific user
 *
 * Initial Date:  02.06.2020
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class UserOverviewController extends BasicController implements Activateable2, GenericEventListener, TooledController {

	public static final String usageIdentifier = UserOverviewController.class.getCanonicalName();

	private static final String CMD_CALENDAR = "Calendar";
	private static final String CMD_BOOKINGS = "Bookings";
	private static final String CMD_LECTURES = "Lectures";
	private static final String CMD_STATEMENTS = "Statements";
	private static final String CMD_GROUPS = "Groups";
	private static final String CMD_PROFILE = "Profile";
	private static final String CMD_ENROLLMENTS = "Enrollments";

	private final VelocityContainer mainVC;

	private int orderTabIndex;
	private int courseTabIndex;
	private int lecturesTabIndex;
	private int certificatesTabIndex;
	private int groupTabIndex;
	private int calendarTabIndex;
	private int profileTabIndex;
	
	private Link homeLink, contactLink, resetLink;
	private Link nextStudent, detailsStudentCmp, previousStudent;

	private final TooledStackedPanel stackPanel;

	private CloseableModalController cmc;
	private ContactFormController contactController;
	private UserChangePasswordController userChangePasswordController;
	private ToggleBoxController userDetailsToggleController;
	private UserShortDescription userShortDescriptionController;
	private DisplayPortraitController displayPortraitController;
	private ProfileAndHomePageEditController profileAndHomePageEditController;
	private HomePageDisplayController homePageDisplayController;
	private GroupOverviewController groupOverviewController;
	private UserOrderController userOrderController;
	private ParticipantLecturesOverviewController lecturesController;
	private CertificateAndEfficiencyStatementListController efficiencyStatementListController;
	private WeeklyCalendarController calendarController;
	private CourseListWrapperController courseListWrapperController;
	private CertificateAndEfficiencyStatementWrapperController certificateAndEfficiencyStatementWrapperController;

	private TabbedPane functionsTabbedPane;

	private final int index;
	private final int numOfStudents;
	private final Identity mentee;
	private final StudentStatEntry statEntry;
	private final String role;

	private final RoleSecurityCallback roleSecurityCallback;

	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityManager securityManager;
	@Autowired
	private CalendarManager calendarManager;
	@Autowired
	private HomePageConfigManager homePageConfigManager;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private CurriculumService curriculumService;

	public UserOverviewController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
								  StudentStatEntry statEntry, Identity mentee, int index, int numOfStudents, String role, RoleSecurityCallback roleSecurityCallback) {
		super(ureq, wControl);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));

		this.index = index;
		this.mentee = mentee;
		this.statEntry = statEntry;
		this.stackPanel = stackPanel;
		this.numOfStudents = numOfStudents;
		this.roleSecurityCallback = roleSecurityCallback;
		this.role = role;

		mainVC = createVelocityContainer("user_relation_overview");

		initUserDetails(ureq, mentee);
		initTabbedPane(ureq);

		putInitialPanel(mainVC);
	}

	@Override
	public void initTools() {
		if (roleSecurityCallback.canContact()) {
			contactLink = LinkFactory.createToolLink("contact.link", translate("contact.link"), this);
			contactLink.setIconLeftCSS("o_icon o_icon_mail");
			stackPanel.addTool(contactLink, Align.left, true);
		}

		homeLink = LinkFactory.createToolLink("home.link", translate("home.link"), this);
		homeLink.setIconLeftCSS("o_icon o_icon_home");
		stackPanel.addTool(homeLink, Align.left, true);

		if (roleSecurityCallback.canResetPassword()) {
			Roles roles = securityManager.getRoles(mentee);
			if (!(roles.isAuthor() || roles.isManager() || roles.isAdministrator() || roles.isSystemAdmin() || roles.isPrincipal())) {
				resetLink = LinkFactory.createToolLink("reset.link", translate("reset.link"), this);
				resetLink.setIconLeftCSS("o_icon o_icon_password");
				stackPanel.addTool(resetLink, Align.left, true);
			}
		}

		previousStudent = LinkFactory.createToolLink("previous.student", translate("previous.student"), this);
		previousStudent.setIconLeftCSS("o_icon o_icon_previous");
		previousStudent.setEnabled(numOfStudents > 1);
		//stackPanel.addTool(previousStudent, true);

		String fullName = StringHelper.escapeHtml(userManager.getUserDisplayName(mentee));
		String details = translate("students.details", new String[]{
				fullName, Integer.toString(index + 1), Integer.toString(numOfStudents)
		});
		detailsStudentCmp = LinkFactory.createToolLink("details.student", details, this);
		detailsStudentCmp.setIconLeftCSS("o_icon o_icon_user");
		stackPanel.addTool(detailsStudentCmp, true);

		nextStudent = LinkFactory.createToolLink("next.student", translate("next.student"), this);
		nextStudent.setIconLeftCSS("o_icon o_icon_next");
		nextStudent.setEnabled(numOfStudents > 1);
		//stackPanel.addTool(nextStudent, true);
		stackPanel.addListener(this);
	}

	private void initUserDetails(UserRequest ureq, Identity identity) {
		// Add user's name and relation
		StringBuilder relationAndName = new StringBuilder(256);
		relationAndName.append(role != null ? role + " " : "");
		relationAndName.append(mentee.getUser().getFirstName()).append(" ");
		relationAndName.append(mentee.getUser().getLastName());
		mainVC.contextPut("relationAndName", relationAndName);

		// Add user details
		VelocityContainer userDetails = createVelocityContainer("user_relation_overview_details");
		userDetails.setDomReplacementWrapperRequired(false);

		userDetailsToggleController = new ToggleBoxController(ureq, getWindowControl(), "user_details",
				translate("user.details.open"), translate("user.details.close"), userDetails);
		mainVC.put("user_details", userDetailsToggleController.getInitialComponent());

		removeAsListenerAndDispose(displayPortraitController);
		displayPortraitController = new DisplayPortraitController(ureq, getWindowControl(), identity, true, true);
		userDetails.put("portrait", displayPortraitController.getInitialComponent());

		removeAsListenerAndDispose(userShortDescriptionController);
		userShortDescriptionController = new UserShortDescription(ureq, getWindowControl(), identity, roleSecurityCallback.isAdministrativeUser());
		userDetails.put("userShortDescription", userShortDescriptionController.getInitialComponent());
	}
	


	private void initTabbedPane(UserRequest ureq) {
		functionsTabbedPane = new TabbedPane("functionsTabbedPane", ureq.getLocale());
		functionsTabbedPane.addListener(this);

		if (roleSecurityCallback.canViewCoursesAndCurriculum()) {
			List<CurriculumRef> curriculumRefs = curriculumService.getMyActiveCurriculumRefs(mentee);
			CurriculumSecurityCallback curriculumSecurityCallback = CurriculumSecurityCallbackFactory.createDefaultCallback();

			courseTabIndex = functionsTabbedPane.addTabControllerCreator(ureq, translate("enrollments"), uureq -> {
				WindowControl bwControl = addToHistory(uureq, OresHelper.createOLATResourceableType(CMD_ENROLLMENTS), null);
				courseListWrapperController = new CourseListWrapperController(uureq, bwControl, stackPanel, mentee, curriculumSecurityCallback, roleSecurityCallback, curriculumRefs, statEntry);
				listenTo(courseListWrapperController);
				return courseListWrapperController;
			});
		}

		if (roleSecurityCallback.canViewResourcesAndBookings()) {
			orderTabIndex = functionsTabbedPane.addTabControllerCreator(ureq, translate("bookings"), uureq -> {
				WindowControl bwControl = addToHistory(uureq, OresHelper.createOLATResourceableType(CMD_BOOKINGS), null);
				userOrderController = new UserOrderController(uureq, bwControl, mentee);
				listenTo(userOrderController);
				return userOrderController;
			});
		}

		if (lectureModule.isEnabled() && roleSecurityCallback.canViewLecturesAndAbsences()) {
			lecturesTabIndex = functionsTabbedPane.addTabControllerCreator(ureq, translate("lectures"), uureq -> {
				WindowControl bwControl = addToHistory(uureq, OresHelper.createOLATResourceableType(CMD_LECTURES), null);
				lecturesController = new ParticipantLecturesOverviewController(uureq, bwControl, mentee, null, true, true, true, true, false, true);
				lecturesController.setBreadcrumbPanel(stackPanel);
				listenTo(lecturesController);
				return lecturesController;
			});
		}

		if (roleSecurityCallback.canViewEfficiencyStatements()) {
			List<CurriculumRef> curriculumRefs = curriculumService.getMyActiveCurriculumRefs(mentee);
			CurriculumSecurityCallback curriculumSecurityCallback = CurriculumSecurityCallbackFactory.createDefaultCallback();

			certificatesTabIndex = functionsTabbedPane.addTabControllerCreator(ureq, translate("statements"), uureq -> {
				WindowControl bwControl = addToHistory(uureq, OresHelper.createOLATResourceableType(CMD_STATEMENTS), null);
				certificateAndEfficiencyStatementWrapperController = new CertificateAndEfficiencyStatementWrapperController(uureq, bwControl, stackPanel, mentee, curriculumSecurityCallback, roleSecurityCallback, curriculumRefs, statEntry);
				listenTo(certificateAndEfficiencyStatementWrapperController);
				return certificateAndEfficiencyStatementWrapperController;
			});
		}

		if (roleSecurityCallback.canViewGroupMemberships()) {
			groupTabIndex = functionsTabbedPane.addTab(ureq, translate("groups.menu.title"), uureq -> {
				WindowControl bwControl = addToHistory(uureq, OresHelper.createOLATResourceableType(CMD_GROUPS), null);
				groupOverviewController = new GroupOverviewController(ureq, bwControl, mentee, false, false);
				listenTo(groupOverviewController);
				return groupOverviewController.getInitialComponent();
			});
		}

		if (roleSecurityCallback.canViewCalendar()) {
			calendarTabIndex = functionsTabbedPane.addTab(ureq, translate("calendar"), uureq -> doOpenCalendar(uureq).getInitialComponent());
		}

		if (roleSecurityCallback.canViewAndEditProfile()) {
			profileTabIndex = functionsTabbedPane.addTab(ureq, translate("profile"), uureq -> {
				WindowControl bwControl = addToHistory(uureq, OresHelper.createOLATResourceableType(CMD_PROFILE), null);
				profileAndHomePageEditController =  new ProfileAndHomePageEditController(ureq, bwControl, mentee, roleSecurityCallback.isAdministrativeUser());
				listenTo(profileAndHomePageEditController);
				return profileAndHomePageEditController.getInitialComponent();
			});
		}

		mainVC.put("functionsTabbedPane", functionsTabbedPane);
	}

	@Override
	protected void doDispose() {
		stackPanel.removeListener(this);
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.deregisterFor(this, CertificatesManager.ORES_CERTIFICATE_EVENT);
	}

	@Override
	public void event(Event event) {
		//
	}

	public StudentStatEntry getEntry() {
		return statEntry;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(previousStudent == source || nextStudent == source) {
			fireEvent(ureq, event);
		} else if (source == homeLink) {
			openHome(ureq);
		} else if (source == contactLink) {
			contact(ureq);
		} else if (source == resetLink) {
			resetPassword(ureq);
		} else if (source == functionsTabbedPane) {
			if(event instanceof TabbedPaneChangedEvent) {
				TabbedPaneChangedEvent pce = (TabbedPaneChangedEvent)event;
				if(pce.getNewController() != null) {
					addToHistory(ureq, pce.getNewController());
				}
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == cmc) {
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(contactController);
			removeAsListenerAndDispose(userChangePasswordController);
			cmc = null;
			contactController = null;
			userChangePasswordController = null;
		} else if (source == contactController) {
			cmc.deactivate();
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(contactController);
			cmc = null;
			contactController = null;
		} else if (source == userChangePasswordController) {
			cmc.deactivate();
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(userChangePasswordController);
			cmc = null;
			userChangePasswordController = null;
		}
		super.event(ureq, source, event);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;

		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(CMD_ENROLLMENTS.equalsIgnoreCase(type) && courseTabIndex >= 0) {
			functionsTabbedPane.setSelectedPane(ureq, courseTabIndex);
			if(courseListWrapperController != null) {
				courseListWrapperController.activate(ureq, entries.subList(1, entries.size()), null);
			}
		} else if(CMD_BOOKINGS.equalsIgnoreCase(type) && orderTabIndex >= 0) {
			functionsTabbedPane.setSelectedPane(ureq, orderTabIndex);
		} else if(CMD_LECTURES.equalsIgnoreCase(type) && lecturesTabIndex >= 0) {
			functionsTabbedPane.setSelectedPane(ureq, lecturesTabIndex);
			if(lecturesController != null) {
				lecturesController.activate(ureq, entries.subList(1, entries.size()), null);
			}
		} else if(CMD_CALENDAR.equalsIgnoreCase(type) && calendarTabIndex >= 0) {
			functionsTabbedPane.setSelectedPane(ureq, calendarTabIndex);
			if(calendarController != null) {
				calendarController.activate(ureq, entries.subList(1, entries.size()), null);
			}
		} else if(CMD_STATEMENTS.equalsIgnoreCase(type) && certificatesTabIndex >= 0) {
			functionsTabbedPane.setSelectedPane(ureq, certificatesTabIndex);
			if (certificateAndEfficiencyStatementWrapperController != null) {
				certificateAndEfficiencyStatementWrapperController.activate(ureq, entries.subList(1, entries.size()), null);
			}
		} else if(CMD_GROUPS.equalsIgnoreCase(type) && groupTabIndex >= 0) {
			functionsTabbedPane.setSelectedPane(ureq, groupTabIndex);
		} else if(CMD_PROFILE.equalsIgnoreCase(type) && profileTabIndex >= 0) {
			functionsTabbedPane.setSelectedPane(ureq, profileTabIndex);
		}
	}

	private void contact(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);

		ContactMessage cmsg = new ContactMessage(getIdentity());
		String fullName = userManager.getUserDisplayName(mentee);
		ContactList contactList = new ContactList(fullName);
		contactList.add(mentee);
		cmsg.addEmailTo(contactList);
		contactController = new ContactFormController(ureq, getWindowControl(), true, false, false, cmsg);
		listenTo(contactController);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), contactController.getInitialComponent());
		cmc.activate();
		listenTo(cmc);
	}

	private void resetPassword(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);

		userChangePasswordController = new UserChangePasswordController(ureq, getWindowControl(), mentee);
		listenTo(userChangePasswordController);
		String name = mentee.getUser().getFirstName() + " " + mentee.getUser().getLastName();
		cmc = new CloseableModalController(getWindowControl(), translate("close"), userChangePasswordController.getInitialComponent(), true, translate("reset.title", name));
		cmc.activate();
		listenTo(cmc);
	}

	private void openHome(UserRequest ureq) {
		if (stackPanel.getLastController() != homePageDisplayController) {
			HomePageConfig homePageConfig = homePageConfigManager.loadConfigFor(mentee);
			removeAsListenerAndDispose(homePageDisplayController);
			homePageDisplayController = new HomePageDisplayController(ureq, getWindowControl(), mentee, homePageConfig);
			listenTo(homePageDisplayController);

			stackPanel.pushController("Visiting card", homePageDisplayController);
		}
	}

	private WeeklyCalendarController doOpenCalendar(UserRequest ureq) {
		removeAsListenerAndDispose(calendarController);

		KalendarRenderWrapper calendarWrapper = calendarManager.getPersonalCalendar(mentee);
		CalendarUserConfiguration config = calendarManager.findCalendarConfigForIdentity(calendarWrapper.getKalendar(), getIdentity());
		if (config != null) {
			calendarWrapper.setConfiguration(config);
		}

		calendarWrapper.setPrivateEventsVisible(true);
		if (mentee.equals(ureq.getIdentity())) {
			calendarWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_WRITE);
		} else {
			calendarWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_ONLY);
		}
		List<KalendarRenderWrapper> calendars = new ArrayList<>();
		calendars.add(calendarWrapper);

		OLATResourceable callerOres = OresHelper.createOLATResourceableInstance(mentee.getName(), mentee.getKey());
		WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType(CMD_CALENDAR), null);
		calendarController = new WeeklyCalendarController(ureq, bwControl, calendars,
				CalendarController.CALLER_CURRICULUM, callerOres, false);
		listenTo(calendarController);

		return calendarController;
	}
}
