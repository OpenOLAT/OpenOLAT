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
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.DropdownUIFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.scope.Scope;
import org.olat.core.gui.components.scope.ScopeEvent;
import org.olat.core.gui.components.scope.ScopeFactory;
import org.olat.core.gui.components.scope.ScopeSelection;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.coach.CoachingModule;
import org.olat.modules.coach.ui.AbstractParticipantsListController.NextPreviousController;
import org.olat.modules.coach.ui.curriculum.course.CourseListWrapperController;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.curriculum.ui.ImplementationsListConfig;
import org.olat.repository.ui.list.ImplementationsListController;
import org.olat.user.PortraitUser;
import org.olat.user.UserInfoProfileConfig;
import org.olat.user.UserManager;
import org.olat.user.UserPortraitService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Description:<br>
 * Overview of all students under the scrutiny of a coach.
 *
 * <P>
 * Initial Date:  8 févr. 2012 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class CoursesIdentityController extends BasicController implements NextPreviousController, Activateable2 {

	private static final String CMD_ALL_COURSES = "List";
	private static final String CMD_IMPLEMENTATIONS_LIST = "Implementations";

	private VelocityContainer mainVC;
	private ScopeSelection scopesSelection;
	private TooledStackedPanel implementationsListStackPanel;

	private Link nextStudent;
	private Link previousStudent;
	private Link resetLink;

	private final int numOfStudents;
	private final boolean fullAccess;
	private final Object userObject;
	private final Identity assessedIdentity;

	private CloseableModalController cmc;
	private TooledStackedPanel coursesListStackPanel;
	private CoursesIdentityListController coursesListCtrl;
	private ImplementationsListController implementationsListCtrl;
	private UserChangePasswordController userChangePasswordCtlr;

	@Autowired
	private UserManager userManager;
	@Autowired
	private CoachingModule coachingModule;
	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private BaseSecurityManager securityManager;
	@Autowired
	private UserPortraitService userPortraitService;

	public CoursesIdentityController(UserRequest ureq, WindowControl wControl, Object userObject, Identity student,
			int numOfStudents, boolean fullAccess) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(CourseListWrapperController.class, getLocale(), getTranslator()));

		this.assessedIdentity = student;
		this.userObject = userObject;
		this.fullAccess = fullAccess;
		this.numOfStudents = numOfStudents;

		mainVC = createVelocityContainer("student_course_list");

		String fullName = userManager.getUserDisplayName(assessedIdentity);
		mainVC.contextPut("studentName", fullName);

		initButtons();
		initPortrait(ureq);
		initScopes();
		doOpenAllCourses(ureq);

		putInitialPanel(mainVC);
	}

	private void initButtons() {
		previousStudent = LinkFactory.createLink("previous.student", "previous.student", "previous.student", null,
				getTranslator(), mainVC, this, Link.BUTTON | Link.NONTRANSLATED);
		previousStudent.setIconLeftCSS("o_icon o_icon_slide_backward");
		previousStudent.setTitle(translate("previous.student"));
		previousStudent.setEnabled(numOfStudents > 1);
		mainVC.put("previous.student", previousStudent);

		nextStudent = LinkFactory.createLink("next.student", "next.student", "next.student", null,
				getTranslator(), mainVC, this, Link.BUTTON | Link.NONTRANSLATED);
		nextStudent.setIconLeftCSS("o_icon o_icon_slide_forward");
		nextStudent.setTitle(translate("next.student"));
		nextStudent.setEnabled(numOfStudents > 1);
		mainVC.put("next.student", nextStudent);

		Roles roles = securityManager.getRoles(assessedIdentity);
		if (coachingModule.isResetPasswordEnabled() && !(roles.isMoreThanUser())) {
			Dropdown moreDropdown = DropdownUIFactory.createMoreDropdown("cmds", getTranslator());
			moreDropdown.setButton(true);
			resetLink = LinkFactory.createToolLink("reset.link", translate("reset.link"), this);
			resetLink.setIconLeftCSS("o_icon o_icon-fw o_icon_password");
			moreDropdown.addComponent(resetLink);
			mainVC.put("cmds", moreDropdown);
		}
	}

	private void initPortrait(UserRequest ureq) {
		UserInfoProfileConfig profileConfig = userPortraitService.createProfileConfig();
		PortraitUser portraitUser = userPortraitService.createPortraitUser(getLocale(), assessedIdentity);
		CoachedIdentityInfoController profile = new CoachedIdentityInfoController(ureq, getWindowControl(),
				assessedIdentity, profileConfig, portraitUser);
		listenTo(profile);
		mainVC.put("portrait", profile.getInitialComponent());
	}

	private void initScopes() {
		if (!curriculumModule.isEnabled()) {
			mainVC.contextPut("showScopes", Boolean.FALSE);
			return;
		}
		mainVC.contextPut("showScopes", Boolean.TRUE);
		List<Scope> scopes = new ArrayList<>(2);
		scopes.add(ScopeFactory.createScope(CMD_ALL_COURSES, translate("all.courses"), null, "o_icon o_icon-fw o_icon_curriculum"));
		scopes.add(ScopeFactory.createScope(CMD_IMPLEMENTATIONS_LIST, translate("search.education.products"), null, "o_icon o_icon-fw o_icon_curriculum"));
		scopesSelection = ScopeFactory.createScopeSelection("scopes", mainVC, this, scopes);
	}

	@Override
	public Object getUserObject() {
		return userObject;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == previousStudent) {
			fireEvent(ureq, new Event("previous.student"));
		} else if (source == nextStudent) {
			fireEvent(ureq, new Event("next.student"));
		} else if (source == resetLink) {
			resetPassword(ureq);
		} else if (source == scopesSelection) {
			if (event instanceof ScopeEvent se) {
				if (CMD_ALL_COURSES.equals(se.getSelectedKey())) {
					doOpenAllCourses(ureq);
				} else if (CMD_IMPLEMENTATIONS_LIST.equals(se.getSelectedKey())) {
					doOpenImplementations(ureq);
				}
			}
		} else if (source == coursesListStackPanel) {
			if (event instanceof PopEvent
					&& coursesListStackPanel.getLastController() == coursesListCtrl) {
				if (scopesSelection != null) {
					scopesSelection.setSelectedKey(CMD_ALL_COURSES);
				}
			}
		} else if (source == implementationsListStackPanel) {
			if (event instanceof PopEvent
					&& implementationsListStackPanel.getLastController() == implementationsListCtrl) {
				if (scopesSelection != null) {
					scopesSelection.setSelectedKey(CMD_IMPLEMENTATIONS_LIST);
				}
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == coursesListCtrl) {
			if (event == Event.CHANGED_EVENT) {
				fireEvent(ureq, event);
			}
		} else if (source == userChangePasswordCtlr) {
			cmc.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(userChangePasswordCtlr);
		removeAsListenerAndDispose(cmc);
		userChangePasswordCtlr = null;
		cmc = null;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries == null || entries.isEmpty()) return;

		ContextEntry ce = entries.get(0);
		String type = ce.getOLATResourceable().getResourceableTypeName();

		if (CMD_IMPLEMENTATIONS_LIST.equalsIgnoreCase(type)) {
			Activateable2 ctrl = doOpenImplementations(ureq);
			ctrl.activate(ureq, entries.subList(1, entries.size()), ce.getTransientState());
		} else if (CMD_ALL_COURSES.equalsIgnoreCase(type)) {
			Activateable2 ctrl = doOpenAllCourses(ureq);
			ctrl.activate(ureq, entries.subList(1, entries.size()), ce.getTransientState());
		} else if ("RepositoryEntry".equals(type)) {
			Activateable2 ctrl = doOpenAllCourses(ureq);
			ctrl.activate(ureq, entries, state);
		}
	}

	private Activateable2 doOpenAllCourses(UserRequest ureq) {
		if (coursesListCtrl == null) {
			coursesListStackPanel = new TooledStackedPanel("coursesliststack", getTranslator(), this);
			coursesListStackPanel.setToolbarEnabled(false);

			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType(CMD_ALL_COURSES), null);
			coursesListCtrl = new CoursesIdentityListController(ureq, bwControl, coursesListStackPanel, assessedIdentity, fullAccess);
			listenTo(coursesListCtrl);
			coursesListStackPanel.pushController(translate("all.courses"), coursesListCtrl);
		} else {
			coursesListStackPanel.popUpToRootController(ureq);
		}
		mainVC.put("content", coursesListStackPanel);
		addToHistory(ureq, coursesListCtrl);
		return coursesListCtrl;
	}

	private Activateable2 doOpenImplementations(UserRequest ureq) {
		if (implementationsListCtrl == null) {
			implementationsListStackPanel = new TooledStackedPanel("implliststack", getTranslator(), this);
			implementationsListStackPanel.setToolbarEnabled(false);

			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType(CMD_IMPLEMENTATIONS_LIST), null);
			ImplementationsListConfig.Builder configBuilder = ImplementationsListConfig.builder(List.of(GroupRoles.participant))
					.setCoachIdentity(getIdentity())
					.enableFormTitle()
					.enablePreparation()
					.enablePreparationWarning()
					.enableId()
					.enableExtRefVisibilityDefault()
					.enableStatus()
					.enableCompletion()
					.enableCalendar()
					.enableCancelledFilter();
			implementationsListCtrl = new ImplementationsListController(ureq, bwControl, implementationsListStackPanel,
					assessedIdentity, configBuilder.build());
			listenTo(implementationsListCtrl);
			implementationsListStackPanel.pushController(translate("search.implementations.list"), implementationsListCtrl);
		} else {
			implementationsListStackPanel.popUpToRootController(ureq);
		}
		mainVC.put("content", implementationsListStackPanel);
		addToHistory(ureq, implementationsListCtrl);
		return implementationsListCtrl;
	}

	private void resetPassword(UserRequest ureq) {
		removeAsListenerAndDispose(userChangePasswordCtlr);
		removeAsListenerAndDispose(cmc);

		userChangePasswordCtlr = new UserChangePasswordController(ureq, getWindowControl(), assessedIdentity);
		listenTo(userChangePasswordCtlr);
		String name = assessedIdentity.getUser().getFirstName() + " " + assessedIdentity.getUser().getLastName();
		cmc = new CloseableModalController(getWindowControl(), translate("close"), userChangePasswordCtlr.getInitialComponent(), true, translate("reset.title", name));
		cmc.activate();
		listenTo(cmc);
	}
}
