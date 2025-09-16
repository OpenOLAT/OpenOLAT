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
import org.olat.admin.user.groups.GroupOverviewController;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.collaboration.CollaborationTools;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarModule;
import org.olat.commons.calendar.model.CalendarUserConfiguration;
import org.olat.commons.calendar.ui.CalendarController;
import org.olat.commons.calendar.ui.WeeklyCalendarController;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.DropdownUIFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.tabbedpane.TabbedPaneChangedEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
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
import org.olat.course.CourseFactory;
import org.olat.course.certificate.CertificatesManager;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.login.webauthn.OLATWebAuthnManager;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.modules.catalog.ui.BookedEvent;
import org.olat.modules.co.ContactFormController;
import org.olat.modules.coach.CoachingService;
import org.olat.modules.coach.RoleSecurityCallback;
import org.olat.modules.coach.ui.AbstractParticipantsListController.NextPreviousController;
import org.olat.modules.coach.ui.curriculum.certificate.CertificateAndEfficiencyStatementWrapperController;
import org.olat.modules.coach.ui.curriculum.course.CourseListWrapperController;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumSecurityCallbackFactory;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.ui.ParticipantLecturesOverviewController;
import org.olat.modules.openbadges.ui.BadgesController;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.manager.ACReservationDAO;
import org.olat.resource.accesscontrol.ui.UserOrderController;
import org.olat.user.HomePageDisplayController;
import org.olat.user.ProfileAndHomePageEditController;
import org.olat.user.UserManager;
import org.olat.user.UserPropertiesInfoController;
import org.olat.user.ui.admin.UserAccountController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Gives an overview to a specific user
 *
 * Initial Date:  02.06.2020
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class UserOverviewController extends BasicController implements NextPreviousController, GenericEventListener {

	public static final String usageIdentifier = UserOverviewController.class.getCanonicalName();

	private static final String CMD_CALENDAR = "Calendar";
	private static final String CMD_BOOKINGS = "Bookings";
	private static final String CMD_LECTURES = "Lectures";
	private static final String CMD_STATEMENTS = "Statements";
	private static final String CMD_BADGES = "Badges";
	private static final String CMD_GROUPS = "Groups";
	private static final String CMD_PROFILE = "Profile";
	private static final String CMD_ENROLLMENTS = "Enrollments";
	private static final String CMD_ACCOUNT = "Account";

	private final VelocityContainer mainVC;

	private int orderTabIndex;
	private int courseTabIndex;
	private int lecturesTabIndex;
	private int certificatesTabIndex;
	private int badgesTabIndex;
	private int groupTabIndex;
	private int calendarTabIndex;
	private int profileTabIndex;
	private int accountTabIndex;
	
	private Link contactLink;
	private Link resetLink;
	private Link nextStudent;
	private Link previousStudent;
	private Link bookOnBehalfOfLink;
	private Link confirmationLink;
	private final TooledStackedPanel stackPanel;

	private CloseableModalController cmc;
	private ContactFormController contactController;
	private UserChangePasswordController userChangePasswordController;
	private ProfileAndHomePageEditController profileAndHomePageEditController;
	private HomePageDisplayController homePageDisplayController;
	private GroupOverviewController groupOverviewController;
	private UserOrderController userOrderController;
	private ParticipantLecturesOverviewController lecturesController;
	private WeeklyCalendarController calendarController;
	private CourseListWrapperController courseListWrapperController;
	private CertificateAndEfficiencyStatementWrapperController certificateAndEfficiencyStatementWrapperController;
	private BadgesController badgesController;
	private BookOnBehalfOfController bookOnBehalfOfController;
	private UserAccountController userAccountController;

	private TabbedPane functionsTabbedPane;

	private final int index;
	private final int numOfStudents;
	private final Identity mentee;
	private final Object statEntry;
	private final String role;

	private final RoleSecurityCallback roleSecurityCallback;

	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityManager securityManager;
	@Autowired
	private CalendarManager calendarManager;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private CoachingService coachingService;
	@Autowired
	private OLATWebAuthnManager webAuthnManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private CalendarModule calendarModule;
	@Autowired
	private CatalogV2Module catalogV2Module;
	@Autowired
	private ACReservationDAO reservationDAO;
	@Autowired
	private ACService acService;
	
	public UserOverviewController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
								  Object statEntry, Identity mentee, int index, int numOfStudents, String role, RoleSecurityCallback roleSecurityCallback) {
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
		initPendingMembershipWarning();
		initButtons();
		initTabbedPane(ureq);

		putInitialPanel(mainVC);
	}
	
	@Override
	public Object getUserObject() {
		return statEntry;
	}

	private void initButtons() {
		Dropdown moreDropdown = DropdownUIFactory.createMoreDropdown("details", getTranslator());
		moreDropdown.setButton(true);
		
		if (roleSecurityCallback.canContact()) {
			contactLink = LinkFactory.createToolLink("contact.link", translate("contact.link"), this);
			contactLink.setIconLeftCSS("o_icon o_icon_mail");
			moreDropdown.addComponent(contactLink);
		}

		if (roleSecurityCallback.canResetPassword()) {
			Roles roles = securityManager.getRoles(mentee);
			if (!(roles.isAuthor() || roles.isManager() || roles.isAdministrator() || roles.isSystemAdmin() || roles.isPrincipal())) {
				resetLink = LinkFactory.createToolLink("reset.link", translate("reset.link"), this);
				resetLink.setIconLeftCSS("o_icon o_icon_password");
				moreDropdown.addComponent(resetLink);
			}
		}
		
		if(moreDropdown.size() > 0) {
			mainVC.put("moreMenu", moreDropdown);
		}
		
		if (roleSecurityCallback.canCreateBookingOnBehalfOf()) {
			bookOnBehalfOfLink = LinkFactory.createButton("book.on.behalf.of.link", mainVC, this);
			bookOnBehalfOfLink.setIconLeftCSS("o_icon o_icon_booking");
			mainVC.put("bookOnBehalf", bookOnBehalfOfLink);
		}

		previousStudent = LinkFactory.createLink("previous.student", "previous.student", "previous.student", null, getTranslator(), mainVC, this, Link.BUTTON | Link.NONTRANSLATED);
		previousStudent.setIconLeftCSS("o_icon o_icon_slide_backward");
		previousStudent.setTitle(translate("previous.student"));
		previousStudent.setEnabled(index > 0 && numOfStudents > 1);
		mainVC.put("previous", previousStudent);

		nextStudent = LinkFactory.createLink("next.student", "next.student", "next.student", null, getTranslator(), mainVC, this, Link.BUTTON | Link.NONTRANSLATED);
		nextStudent.setIconLeftCSS("o_icon o_icon_slide_forward");
		nextStudent.setTitle(translate("next.student"));
		nextStudent.setEnabled(index < (numOfStudents - 1) && numOfStudents > 1);
		mainVC.put("next", nextStudent);
	}

	private void initUserDetails(UserRequest ureq, Identity identity) {
		// Add user's name and relation
		StringBuilder relationAndName = new StringBuilder(256);
		relationAndName.append(role != null ? role + " " : "")
		               .append(StringHelper.escapeHtml(mentee.getUser().getFirstName()))
		               .append(" ")
		               .append(StringHelper.escapeHtml(mentee.getUser().getLastName()));
		mainVC.contextPut("relationAndName", relationAndName);

		// Add user details
		UserPropertiesInfoController infoCtrl = new UserPropertiesInfoController(ureq, getWindowControl(), identity);
		listenTo(infoCtrl);
		mainVC.put("userDetails", infoCtrl.getInitialComponent());
	}
	
	private void initPendingMembershipWarning() {
		if (!roleSecurityCallback.canViewPendingCourseBookings()) {
			return;
		}
		long pendingMembershipCount = acService.getReservationsWithOrders(mentee).stream()
				.filter(r -> StringHelper.containsNonWhitespace(r.getType()))
				.filter(r -> r.getType().startsWith(CurriculumService.RESERVATION_PREFIX)).count();
		if (pendingMembershipCount > 1) {
			String warning = translate("warning.pending.membership.plural", Long.toString(pendingMembershipCount));
			mainVC.contextPut("pendingMembershipWarning", warning);
		} else if (pendingMembershipCount == 1) {
			String warning = translate("warning.pending.membership.singular");
			mainVC.contextPut("pendingMembershipWarning", warning);
		} else {
			mainVC.contextRemove("pendingMembershipWarning");
		}
		if (pendingMembershipCount > 0) {
			confirmationLink = LinkFactory.createToolLink("go.to.confirmation", translate("go.to.confirmation"), this);
			confirmationLink.setIconRightCSS("o_icon o_icon-fw o_icon_start");
			mainVC.put("pendingMembershipWarningLink", confirmationLink);
		}
	}

	private void initTabbedPane(UserRequest ureq) {
		functionsTabbedPane = new TabbedPane("functionsTabbedPane", ureq.getLocale());
		functionsTabbedPane.addListener(this);

		if (roleSecurityCallback.canViewCoursesAndCurriculum()) {
			courseTabIndex = functionsTabbedPane.addTabControllerCreator(ureq, translate("enrollments"), uureq -> {
				WindowControl bwControl = addToHistory(uureq, OresHelper.createOLATResourceableType(CMD_ENROLLMENTS), null);
				List<Curriculum> curriculumRefs = curriculumService.getMyCurriculums(mentee);
				CurriculumSecurityCallback curriculumSecurityCallback = CurriculumSecurityCallbackFactory.createDefaultCallback();
				courseListWrapperController = new CourseListWrapperController(uureq, bwControl, stackPanel, mentee, curriculumSecurityCallback, roleSecurityCallback,
						List.copyOf(curriculumRefs), statEntry);
				listenTo(courseListWrapperController);
				return courseListWrapperController;
			});
		}

		if (catalogV2Module.isEnabled() && roleSecurityCallback.canViewResourcesAndBookings()) {
			orderTabIndex = functionsTabbedPane.addTabControllerCreator(ureq, translate("bookings"), uureq -> {
				WindowControl bwControl = addToHistory(uureq, OresHelper.createOLATResourceableType(CMD_BOOKINGS), null);
				userOrderController = new UserOrderController(uureq, bwControl, mentee, roleSecurityCallback.canViewPendingCourseBookings());
				listenTo(userOrderController);
				return userOrderController;
			});
		}

		if (lectureModule.isEnabled() && roleSecurityCallback.canViewLecturesAndAbsences()) {
			lecturesTabIndex = functionsTabbedPane.addTabControllerCreator(ureq, translate("lectures"), uureq -> {
				WindowControl bwControl = addToHistory(uureq, OresHelper.createOLATResourceableType(CMD_LECTURES), null);
				lecturesController = new ParticipantLecturesOverviewController(uureq, bwControl, mentee, null, true, true, true, true, false, true, false,
						roleSecurityCallback.limitToRole());
				lecturesController.setBreadcrumbPanel(stackPanel);
				listenTo(lecturesController);
				return lecturesController;
			});
		}

		if (roleSecurityCallback.canViewEfficiencyStatements()) {
			certificatesTabIndex = functionsTabbedPane.addTabControllerCreator(ureq, translate("statements"), uureq -> {
				WindowControl bwControl = addToHistory(uureq, OresHelper.createOLATResourceableType(CMD_STATEMENTS), null);
				certificateAndEfficiencyStatementWrapperController = new CertificateAndEfficiencyStatementWrapperController(uureq, bwControl, stackPanel,
						mentee, roleSecurityCallback);
				listenTo(certificateAndEfficiencyStatementWrapperController);
				return certificateAndEfficiencyStatementWrapperController;
			});
		}

		if (roleSecurityCallback.canShowBadges()) {
			badgesTabIndex = functionsTabbedPane.addTab(ureq, translate("badges"), uureq -> {
				WindowControl bwControl = addToHistory(uureq, OresHelper.createOLATResourceableType(CMD_BADGES), null);
				badgesController = new BadgesController(uureq, bwControl, mentee);
				listenTo(badgesController);
				return badgesController.getInitialComponent();
			});
		}

		if (roleSecurityCallback.canViewGroupMemberships()) {
			groupTabIndex = functionsTabbedPane.addTab(ureq, translate("groups.menu.title"), uureq -> {
				WindowControl bwControl = addToHistory(uureq, OresHelper.createOLATResourceableType(CMD_GROUPS), null);
				groupOverviewController = new GroupOverviewController(uureq, bwControl, mentee, false, false);
				listenTo(groupOverviewController);
				return groupOverviewController.getInitialComponent();
			});
		}

		if (roleSecurityCallback.canViewCalendar()) {
			calendarTabIndex = functionsTabbedPane.addTab(ureq, translate("calendar"), uureq -> doOpenCalendar(uureq).getInitialComponent());
		}

		if (roleSecurityCallback.canEditProfile()) {
			profileTabIndex = functionsTabbedPane.addTab(ureq, translate("profile"), uureq -> {
				WindowControl bwControl = addToHistory(uureq, OresHelper.createOLATResourceableType(CMD_PROFILE), null);
				profileAndHomePageEditController =  new ProfileAndHomePageEditController(uureq, bwControl, mentee, roleSecurityCallback.isAdministrativeUser());
				listenTo(profileAndHomePageEditController);
				return profileAndHomePageEditController.getInitialComponent();
			});
		} else if (roleSecurityCallback.canViewProfile()) {
			profileTabIndex = functionsTabbedPane.addTab(ureq, translate("profile"), uureq -> {
				WindowControl bwControl = addToHistory(uureq, OresHelper.createOLATResourceableType(CMD_PROFILE), null);
				profileAndHomePageEditController =  new ProfileAndHomePageEditController(uureq, bwControl, mentee, 
						roleSecurityCallback.isAdministrativeUser(), true);
				listenTo(profileAndHomePageEditController);
				return profileAndHomePageEditController.getInitialComponent();
			});			
		}
		
		if (roleSecurityCallback.canDeactivateAccounts()) {
			accountTabIndex = functionsTabbedPane.addTab(ureq, translate("account"), uureq -> {
				WindowControl bwControl = addToHistory(uureq, OresHelper.createOLATResourceableType(CMD_ACCOUNT), null);
						userAccountController = new UserAccountController(bwControl, uureq, mentee, true);
				listenTo(userAccountController);
			return userAccountController.getInitialComponent();
			});
		}

		mainVC.put("functionsTabbedPane", functionsTabbedPane);
	}

	@Override
	protected void doDispose() {
		stackPanel.removeListener(this);
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.deregisterFor(this, CertificatesManager.ORES_CERTIFICATE_EVENT);
        super.doDispose();
	}

	@Override
	public void event(Event event) {
		//
	}

	public Object getEntry() {
		return statEntry;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(previousStudent == source || nextStudent == source) {
			fireEvent(ureq, event);
		} else if (source == contactLink) {
			contact(ureq);
		} else if (source == resetLink) {
			resetPassword(ureq);
		} else if (source == bookOnBehalfOfLink) {
			doBookOnBehalfOf(ureq);
		} else if (source == functionsTabbedPane) {
			if(event instanceof TabbedPaneChangedEvent pce && pce.getNewController() != null) {
				addToHistory(ureq, pce.getNewController());
			}
		} else if (source == confirmationLink) {
			doConfirmation(ureq);
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
		} else if (source == bookOnBehalfOfController) {
			cleanUpNonModalControllers();
			if (event instanceof BookedEvent bookedEvent) {
				doDisplayInfo(bookedEvent);
			}
		} else if (source == userOrderController) {
			if (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				initPendingMembershipWarning();
			}
		}
		super.event(ureq, source, event);
	}

	private void doDisplayInfo(BookedEvent bookedEvent) {
		if (bookedEvent.getCurriculumElement() != null) {
			Identity reloadedIdentity = securityManager.loadIdentityByKey(mentee.getKey());
			String curriculumElementDisplayName = StringHelper.escapeHtml(bookedEvent.getCurriculumElement().getDisplayName());
			String userDisplayName = StringHelper.escapeHtml(reloadedIdentity.getUser().getFirstName() + " " + reloadedIdentity.getUser().getLastName());
			if (reservationDAO.loadReservation(mentee, bookedEvent.getCurriculumElement().getResource()) == null) {
				showInfo("booked.on.behalf.of", 
						new String[] {curriculumElementDisplayName, userDisplayName});	
			} else {
				showInfo("reserved.on.behalf.of", 
						new String[] {curriculumElementDisplayName, userDisplayName});
			}
		}
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
		} else if (CMD_BADGES.equalsIgnoreCase(type) && badgesTabIndex >= 0) {
			functionsTabbedPane.setSelectedPane(ureq, badgesTabIndex);
		} else if(CMD_GROUPS.equalsIgnoreCase(type) && groupTabIndex >= 0) {
			functionsTabbedPane.setSelectedPane(ureq, groupTabIndex);
		} else if(CMD_PROFILE.equalsIgnoreCase(type) && profileTabIndex >= 0) {
			functionsTabbedPane.setSelectedPane(ureq, profileTabIndex);
		} else if (CMD_ACCOUNT.equalsIgnoreCase(type) && accountTabIndex >= 0) {
			functionsTabbedPane.setSelectedPane(ureq, accountTabIndex);
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
		List<Authentication> authentications = webAuthnManager.getPasskeyAuthentications(mentee);
		if(authentications != null && !authentications.isEmpty()) {
			showWarning("warning.user.passkey");
		} else {
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(userChangePasswordController);

			userChangePasswordController = new UserChangePasswordController(ureq, getWindowControl(), mentee);
			listenTo(userChangePasswordController);
			String name = mentee.getUser().getFirstName() + " " + mentee.getUser().getLastName();
			cmc = new CloseableModalController(getWindowControl(), translate("close"), userChangePasswordController.getInitialComponent(), true, translate("reset.title", name));
			cmc.activate();
			listenTo(cmc);
		}
	}

	private void cleanUpNonModalControllers() {
		bookOnBehalfOfController = cleanUpNonModalController(bookOnBehalfOfController);
		homePageDisplayController = cleanUpNonModalController(homePageDisplayController);
	}
	
	private <T extends Controller> T cleanUpNonModalController(T controller) {
		if (controller != null && stackPanel.hasController(controller)) {
			stackPanel.popController(controller);
			removeAsListenerAndDispose(controller);
		}
		return null;
	}
	
	private void doBookOnBehalfOf(UserRequest ureq) {
		cleanUpNonModalControllers();

		bookOnBehalfOfController = new BookOnBehalfOfController(ureq, getWindowControl(), mentee, stackPanel);
		listenTo(bookOnBehalfOfController);

		stackPanel.pushController(translate("book.on.behalf.of.link"), bookOnBehalfOfController);
	}

	private WeeklyCalendarController doOpenCalendar(UserRequest ureq) {
		removeAsListenerAndDispose(calendarController);
		
		List<KalendarRenderWrapper> calendars = new ArrayList<>();
		
		if(calendarModule.isEnablePersonalCalendar()) {
			KalendarRenderWrapper calendarWrapper = calendarManager.getPersonalCalendar(mentee);
			configCalendar(ureq, calendarWrapper, calendars);
		}
		
		if(calendarModule.isEnableCourseElementCalendar() || calendarModule.isEnableCourseToolCalendar()) {
			List<RepositoryEntry> courses = coachingService.getUserCourses(mentee, false);
			for (RepositoryEntry courseEntry : courses) {
				configCalendar(ureq, calendarManager.getCourseCalendar(CourseFactory.loadCourse(courseEntry)), calendars);
			}
		}
		
		if (calendarModule.isEnableGroupCalendar()) {
			SearchBusinessGroupParams groupParams = new SearchBusinessGroupParams(mentee, true, true);
			groupParams.addTools(CollaborationTools.TOOL_CALENDAR);
			List<BusinessGroup> groupsWithCalendar = businessGroupService.findBusinessGroups(groupParams, null, 0, -1);
			for (BusinessGroup group : groupsWithCalendar) {
				configCalendar(ureq, calendarManager.getGroupCalendar(group), calendars);
			}
		}

		OLATResourceable callerOres = OresHelper.createOLATResourceableInstance(mentee.getName(), mentee.getKey());
		WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType(CMD_CALENDAR), null);
		calendarController = new WeeklyCalendarController(ureq, bwControl, calendars,
				CalendarController.CALLER_CURRICULUM, callerOres, false);
		listenTo(calendarController);

		return calendarController;
	}
	
	private void configCalendar(UserRequest ureq, KalendarRenderWrapper calendarWrapper, List<KalendarRenderWrapper> calendars) {
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
		
		calendars.add(calendarWrapper);
	}
	
	private void doConfirmation(UserRequest ureq) {
		functionsTabbedPane.setSelectedPane(ureq, orderTabIndex);
		userOrderController.goToPendingMemberships(ureq);
	}
}
