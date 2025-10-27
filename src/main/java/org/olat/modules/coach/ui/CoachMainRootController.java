/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.coach.ui;

import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRelationshipService;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.coach.model.CoachingSecurity;
import org.olat.modules.coach.security.PendingAccountActivationRightProvider;
import org.olat.modules.coach.ui.component.SearchEvent;
import org.olat.modules.coach.ui.component.SearchStateEntry;
import org.olat.modules.coach.ui.manager.CoachReportsController;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.grading.GradingModule;
import org.olat.modules.grading.GradingSecurityCallback;
import org.olat.modules.grading.GradingSecurityCallbackFactory;
import org.olat.modules.grading.model.GradingSecurity;
import org.olat.modules.lecture.LectureModule;
import org.olat.repository.ui.list.ImplementationsListController;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CoachMainRootController extends BasicController implements Activateable2 {
	
	private Link peopleButton;
	private Link ordersButton;
	private Link coursesButton;
	private Link reportsButton;
	private Link lecturesButton;
	private Link ordersAdminButton;
	private Link pendingConfirmationsButton;
	private Link businessGroupsButton;
	private Link implementationsButton;
	
	private final VelocityContainer mainVC;
	private final TooledStackedPanel content;
	
	private GradingSecurity gradingSec;
	private CoachingSecurity coachingSec;
	private final boolean showPeopleView;
	private final boolean coachAssignmentsAvailable;
	private final boolean showPendingConfirmations;
	
	private GroupListController groupListCtrl;
	private LecturesMainController lecturesCtrl;
	private UserSearchController userSearchCtrl;
	private CoachReportsController reportsCtrl;
	private CoachPeopleController peopleListCtrl;
	private OrdersAdminController ordersAdminCtrl;
	private CoursesAndOthersController courseListCtrl;
	private OrdersOverviewController ordersOverviewCtrl;
	private CoachParticipantsListController quickSearchCtrl;
	private ImplementationsListController implementationsCtrl;
	private final CoachMainSearchHeaderController searchFieldCtrl;
	private PendingConfirmationsController pendingConfirmationsCtrl;
	
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private GradingModule gradingModule;
	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private IdentityRelationshipService identityRelationsService;
	@Autowired
	private AccessControlModule accessControlModule;
	@Autowired
	private OrganisationService organisationService;
	
	public CoachMainRootController(UserRequest ureq, WindowControl wControl, TooledStackedPanel content,
			CoachingSecurity coachingSec, GradingSecurity gradingSec) {
		super(ureq, wControl);
		this.content = content;
		this.gradingSec = gradingSec;
		this.coachingSec = coachingSec;

		Roles roles = ureq.getUserSession().getRoles();
		coachAssignmentsAvailable = roles.isAdministrator() || roles.isLearnResourceManager() || roles.isPrincipal() || roles.isAuthor();
		showPeopleView = coachingSec.coach() || coachingSec.owner() || roles.isPrincipal() || roles.isLineManager() ||roles.isEducationManager()
				|| !identityRelationsService.getRelationsAsSource(getIdentity()).isEmpty();
		showPendingConfirmations = curriculumModule.isEnabled() && accessControlModule.isInvoiceEnabled() && canActivatePendingAccounts();
		mainVC = createVelocityContainer("coaching");
		
		searchFieldCtrl = new CoachMainSearchHeaderController(ureq, getWindowControl());
		listenTo(searchFieldCtrl);
		mainVC.put("searchField", searchFieldCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
		initButtons();
	}
	
	private boolean canActivatePendingAccounts() {
		return organisationService.hasOrganisationRight(getIdentity(), PendingAccountActivationRightProvider.RELATION_RIGHT);
	}

	private void initButtons() {
		pendingConfirmationsButton = LinkFactory.createLink("pending.confirmations.menu.title", "pending.confirmations.menu.title", getTranslator(), mainVC, this, Link.LINK_CUSTOM_CSS);
		pendingConfirmationsButton.setIconLeftCSS("o_icon o_icon-xl o_icon_pending_confirmations");
		pendingConfirmationsButton.setElementCssClass("btn btn-default o_button_mega");
		pendingConfirmationsButton.setVisible(showPendingConfirmations);

		lecturesButton = LinkFactory.createLink("lectures.menu.title", "lectures.menu.title", getTranslator(), mainVC, this, Link.LINK_CUSTOM_CSS);
		lecturesButton.setIconLeftCSS("o_icon o_icon-xl o_icon_calendar_day");
		lecturesButton.setElementCssClass("btn btn-default o_button_mega o_sel_coaching_lectures");
		lecturesButton.setVisible(lectureModule.isEnabled() && (coachingSec.isTeacher() || coachingSec.isMasterCoachForLectures()));
		
		ordersButton = LinkFactory.createLink("orders.menu.title", "orders.menu.title", getTranslator(), mainVC, this, Link.LINK_CUSTOM_CSS);
		ordersButton.setIconLeftCSS("o_icon o_icon-xl o_icon_assessment_tool");
		ordersButton.setElementCssClass("btn btn-default o_button_mega o_sel_coaching_orders");
		ordersButton.setVisible(coachingSec.coach() || coachingSec.owner() || (gradingModule.isEnabled() && (gradingSec.isGrader() || gradingSec.isGradedResourcesManager())));
	
		ordersAdminButton = LinkFactory.createLink("orders.admin.menu.title", "orders.admin.menu.title", getTranslator(), mainVC, this, Link.LINK_CUSTOM_CSS);
		ordersAdminButton.setIconLeftCSS("o_icon o_icon-xl o_icon_courselog");
		ordersAdminButton.setElementCssClass("btn btn-default o_button_mega o_sel_coaching_orders_admin");
		ordersAdminButton.setVisible((gradingModule.isEnabled() && gradingSec.isGradedResourcesManager()) || coachAssignmentsAvailable);
		
		reportsButton = LinkFactory.createLink("reports.menu.title", "reports.menu.title", getTranslator(), mainVC, this, Link.LINK_CUSTOM_CSS);
		reportsButton.setIconLeftCSS("o_icon o_icon-xl o_icon_chart_simple");
		reportsButton.setElementCssClass("btn btn-default o_button_mega o_sel_coaching_reports");
		reportsButton.setVisible(coachingSec.coach() || coachingSec.owner() || coachingSec.isLineManager() || coachingSec.isEducationManager());

		peopleButton = LinkFactory.createLink("students.menu.title", "students.menu.title", getTranslator(), mainVC, this, Link.LINK_CUSTOM_CSS);
		peopleButton.setIconLeftCSS("o_icon o_icon-xl o_icon_user");
		peopleButton.setElementCssClass("btn btn-default o_button_mega o_sel_coaching_people");
		peopleButton.setVisible(showPeopleView);
		
		coursesButton = LinkFactory.createLink("courses.menu.title", "courses.menu.title", getTranslator(), mainVC, this, Link.LINK_CUSTOM_CSS);
		coursesButton.setIconLeftCSS("o_icon o_icon-xl o_CourseModule_icon");
		coursesButton.setElementCssClass("btn btn-default o_button_mega o_sel_coaching_courses");
		coursesButton.setVisible(coachingSec.coach() || coachingSec.owner());
	
		businessGroupsButton = LinkFactory.createLink("groups.menu.title", "groups.menu.title", getTranslator(), mainVC, this, Link.LINK_CUSTOM_CSS);
		businessGroupsButton.setIconLeftCSS("o_icon o_icon-xl o_icon_group");
		businessGroupsButton.setElementCssClass("btn btn-default o_button_mega o_sel_coaching_groups");
		businessGroupsButton.setVisible(coachingSec.coach() || coachingSec.owner());	
		
		implementationsButton = LinkFactory.createLink("implementations.menu.title", "implementations.menu.title", getTranslator(), mainVC, this, Link.LINK_CUSTOM_CSS);
		implementationsButton.setIconLeftCSS("o_icon o_icon-xl o_icon_curriculum");
		implementationsButton.setElementCssClass("btn btn-default o_button_mega o_sel_coaching_implementations");
		implementationsButton.setVisible(curriculumModule.isEnabled() && (coachingSec.coach() || coachingSec.owner()));	
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		content.popUpToController(this);
		cleanUp();

		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("People".equalsIgnoreCase(type)) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			doPeople(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
		} else if("Events".equalsIgnoreCase(type) || "Lectures".equalsIgnoreCase(type)) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			doLectures(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
		} else if("Groups".equalsIgnoreCase(type)) {
			doBusinessGroups(ureq);
		} else if("Courses".equalsIgnoreCase(type)) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			doCourses(ureq).activate(ureq, subEntries, null);
		} else if("Orders".equalsIgnoreCase(type)) {
			doAssignmentOrders(ureq);
		} else if("OrdersAdmin".equalsIgnoreCase(type)) {
			doAdminAssignmentOrders(ureq);
		} else if("Reports".equalsIgnoreCase(type)) {
			doReport(ureq);
		} else if("UserSearch".equalsIgnoreCase(type) || "UsersSearch".equalsIgnoreCase(type)) {
			doUserSearch(ureq);
		} else if("Implementations".equalsIgnoreCase(type)) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			doImplementations(ureq).activate(ureq, subEntries, null);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(peopleButton == source) {
			doPeople(ureq);
		} else if(ordersButton == source) {
			doAssignmentOrders(ureq);
		} else if(coursesButton == source) {
			doCourses(ureq).activate(ureq, List.of(), null);
		} else if(reportsButton == source) {
			doReport(ureq);
		} else if(ordersAdminButton == source) {
			doAdminAssignmentOrders(ureq);
		} else if(businessGroupsButton == source) {
			doBusinessGroups(ureq);
		} else if(lecturesButton == source) {
			doLectures(ureq);
		} else if(implementationsButton == source) {
			doImplementations(ureq);
		} else if (pendingConfirmationsButton == source) {
			doPendingConfirmations(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(searchFieldCtrl == source) {
			if(event instanceof SearchEvent se) {
				if(SearchEvent.SEARCH_USERS.equals(se.getCommand())) {
					doUserSearch(ureq);
				} else if(SearchEvent.SEARCH.equals(se.getCommand())) {
					doQuickSearch(ureq, se.getSearchString());
				}
			}
		}
	}

	private void cleanUp() {
		removeAsListenerAndDispose(implementationsCtrl);
		removeAsListenerAndDispose(ordersOverviewCtrl);
		removeAsListenerAndDispose(ordersAdminCtrl);
		removeAsListenerAndDispose(quickSearchCtrl);
		removeAsListenerAndDispose(peopleListCtrl);
		removeAsListenerAndDispose(userSearchCtrl);
		removeAsListenerAndDispose(groupListCtrl);
		removeAsListenerAndDispose(courseListCtrl);
		removeAsListenerAndDispose(lecturesCtrl);
		removeAsListenerAndDispose(reportsCtrl);
		implementationsCtrl = null;
		ordersOverviewCtrl = null;
		ordersAdminCtrl = null;
		quickSearchCtrl = null;
		peopleListCtrl = null;
		courseListCtrl = null;
		userSearchCtrl = null;
		groupListCtrl = null;
		lecturesCtrl = null;
		reportsCtrl = null;
	}
	
	private CoachPeopleController doPeople(UserRequest ureq) {
		content.popUpToController(this);
		cleanUp();
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("People", 0l);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = addToHistory(ureq, ores, null);
		peopleListCtrl = new CoachPeopleController(ureq, bwControl, content, coachingSec);
		listenTo(peopleListCtrl);
		content.pushController(translate("students.menu.title"), peopleListCtrl);
		return peopleListCtrl;
	}
	
	private void doQuickSearch(UserRequest ureq, String searchString) {
		content.popUpToController(this);
		cleanUp();
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("QuickSearch", 0l);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = addToHistory(ureq, ores, null);
		quickSearchCtrl = new CoachParticipantsListController(ureq, bwControl, content, GroupRoles.coach);
		listenTo(quickSearchCtrl);
		quickSearchCtrl.activate(ureq, List.of(), new SearchStateEntry(searchString));
		content.pushController(translate("students.menu.title"), quickSearchCtrl);
	}
	
	private void doBusinessGroups(UserRequest ureq) {
		content.popUpToController(this);
		cleanUp();
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Groups", 0l);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = addToHistory(ureq, ores, null);
		groupListCtrl = new GroupListController(ureq, bwControl, content);
		listenTo(groupListCtrl);
		content.pushController(translate("groups.menu.title"), groupListCtrl);
	}
	
	private CoursesAndOthersController doCourses(UserRequest ureq) {
		content.popUpToController(this);
		cleanUp();
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Courses", 0l);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = addToHistory(ureq, ores, null);
		courseListCtrl = new CoursesAndOthersController(ureq, bwControl);
		listenTo(courseListCtrl);
		content.pushController(translate("courses.menu.title"), courseListCtrl);
		return courseListCtrl;
	}
	
	private ImplementationsListController doImplementations(UserRequest ureq) {
		content.popUpToController(this);
		cleanUp();
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Implementations", 0l);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = addToHistory(ureq, ores, null);
		implementationsCtrl = new ImplementationsListController(ureq, bwControl, content, List.of(GroupRoles.owner, GroupRoles.coach), true);
		listenTo(implementationsCtrl);
		content.pushController(translate("implementations.title"), implementationsCtrl);
		return implementationsCtrl;
	}
	
	private LecturesMainController doLectures(UserRequest ureq) {
		content.popUpToController(this);
		cleanUp();
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Events", 0l);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = addToHistory(ureq, ores, null);
		lecturesCtrl = new LecturesMainController(ureq, bwControl, content, coachingSec);
		listenTo(lecturesCtrl);
		content.pushController(translate("lectures.title"), lecturesCtrl);
		return lecturesCtrl;
	}
	
	private void doAssignmentOrders(UserRequest ureq) {
		content.popUpToController(this);
		cleanUp();
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Orders", 0l);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = addToHistory(ureq, ores, null);
		GradingSecurityCallback secCallback = GradingSecurityCallbackFactory.getSecurityCalllback(getIdentity(), gradingSec);
		ordersOverviewCtrl = new OrdersOverviewController(ureq, bwControl, content, null, coachingSec, secCallback, translate("orders.menu.title"));
		listenTo(ordersOverviewCtrl);
		content.pushController(translate("orders.menu.title"), ordersOverviewCtrl);
	}
	
	private void doAdminAssignmentOrders(UserRequest ureq) {
		content.popUpToController(this);
		cleanUp();
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("OrdersAdmin", 0l);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = addToHistory(ureq, ores, null);
		GradingSecurityCallback secCallback = GradingSecurityCallbackFactory.getSecurityCalllback(getIdentity(), gradingSec);
		ordersAdminCtrl = new OrdersAdminController(ureq, bwControl, content, secCallback, gradingSec);
		listenTo(ordersAdminCtrl);
		content.pushController(translate("orders.admin.menu.title"), ordersAdminCtrl);
	}
	
	private void doReport(UserRequest ureq) {
		content.popUpToController(this);
		cleanUp();
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Reports", 0L);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = addToHistory(ureq, ores, null);
		reportsCtrl = new CoachReportsController(ureq, bwControl);
		listenTo(reportsCtrl);
		content.pushController(translate("reports.menu.title"), reportsCtrl);
	}

	private void doUserSearch(UserRequest ureq) {
		content.popUpToController(this);
		cleanUp();
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("UsersSearch", 0l);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = addToHistory(ureq, ores, null);
		userSearchCtrl = new UserSearchController(ureq, bwControl, content);
		listenTo(userSearchCtrl);
		content.pushController(translate("search.menu.title"), userSearchCtrl);
	}
	
	private void doPendingConfirmations(UserRequest ureq) {
		content.popUpToController(this);
		cleanUp();

		OLATResourceable ores = OresHelper.createOLATResourceableInstance("PendingConfirmations", 0l);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = addToHistory(ureq, ores, null);
		pendingConfirmationsCtrl = new PendingConfirmationsController(ureq, bwControl);
		listenTo(pendingConfirmationsCtrl);
		content.pushController(translate("pending.confirmations"), pendingConfirmationsCtrl);
	}
}
