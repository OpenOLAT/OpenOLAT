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

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.scope.Scope;
import org.olat.core.gui.components.scope.ScopeEvent;
import org.olat.core.gui.components.scope.ScopeFactory;
import org.olat.core.gui.components.scope.ScopeSelection;
import org.olat.core.gui.components.velocity.VelocityContainer;
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
import org.olat.modules.coach.CoachingService;
import org.olat.modules.coach.model.CoursesStatisticsRuntimeTypesGroup;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CoursesAndOthersController extends BasicController implements Activateable2 {
	
	private static final String SCOPE_COACH = GroupRoles.coach.name();
	private static final String SCOPE_OWNER = GroupRoles.owner.name();
	private static final String SCOPE_OTHERS = "Others";
	
	private final List<Scope> scopes;
	private final ScopeSelection scopeEl;
	private final VelocityContainer mainVC;
	
	private CourseListController coursesListCtrl;
	
	@Autowired
	private CoachingService coachingService;
	
	public CoursesAndOthersController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("courses_and_others");
		
		scopes = new ArrayList<>(4);
		scopes.add(ScopeFactory.createScope(SCOPE_COACH, translate("lectures.teacher.menu.title"), null, "o_icon o_icon_coaching_tool"));
		
		if(coachingService.hasResourcesAsOwner(getIdentity(), CoursesStatisticsRuntimeTypesGroup.standaloneAndCurricular)) {
			scopes.add(ScopeFactory.createScope(GroupRoles.owner.name(), translate("coaching.courses.owner"), null, "o_icon o_icon_gear"));
		}
	
		Roles roles = ureq.getUserSession().getRoles();
		if(!roles.isAuthor() && !roles.isLearnResourceManager() && !roles.isAdministrator()
				&& coachingService.hasResourcesAsOwner(getIdentity(), CoursesStatisticsRuntimeTypesGroup.embeddedAndTemplate)) {
			scopes.add(ScopeFactory.createScope(SCOPE_OTHERS, translate("other.resources"), null, "o_icon o_icon_resources"));
		}
		scopeEl = ScopeFactory.createScopeSelection("scopes", mainVC, this, scopes);
		scopeEl.setVisible(scopes.size() > 1);
		
		putInitialPanel(mainVC);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			doOpenCoach(ureq).activate(ureq, List.of(), null);
			scopeEl.setSelectedKey(SCOPE_COACH);
			return;
		}
		
		String scope = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(SCOPE_OWNER.equalsIgnoreCase(scope)) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			doOpenOwner(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
			scopeEl.setSelectedKey(SCOPE_OWNER);
		} else if(SCOPE_COACH.equalsIgnoreCase(scope)) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			doOpenCoach(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
			scopeEl.setSelectedKey(SCOPE_COACH);
		} else if(SCOPE_OTHERS.equalsIgnoreCase(scope)) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			doOpenOthers(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
			scopeEl.setSelectedKey(SCOPE_OTHERS);
		} else {
			doOpenCoach(ureq).activate(ureq, entries, null);
			scopeEl.setSelectedKey(SCOPE_COACH);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(scopeEl == source) {
			if (event instanceof ScopeEvent se) {
				if(SCOPE_COACH.equals(se.getSelectedKey())) {
					doOpenCoach(ureq).activate(ureq, List.of(), null);
				} else if(SCOPE_OWNER.equals(se.getSelectedKey())) {
					doOpenOwner(ureq).activate(ureq, List.of(), null);
				} else if(SCOPE_OTHERS.equals(se.getSelectedKey())) {
					doOpenOthers(ureq).activate(ureq, List.of(), null);
				}
			}
		}
	}
	
	private CourseListController doOpenCoach(UserRequest ureq) {
		return doOpenCourses(ureq, SCOPE_COACH, GroupRoles.coach, CoursesStatisticsRuntimeTypesGroup.standaloneAndCurricular);
	}
	
	private CourseListController doOpenOwner(UserRequest ureq) {
		return doOpenCourses(ureq, SCOPE_OWNER, GroupRoles.owner, CoursesStatisticsRuntimeTypesGroup.standaloneAndCurricular);
	}
	
	private CourseListController doOpenOthers(UserRequest ureq) {
		return doOpenCourses(ureq, SCOPE_OTHERS, GroupRoles.owner, CoursesStatisticsRuntimeTypesGroup.embeddedAndTemplate);
	}
	
	private CourseListController doOpenCourses(UserRequest ureq, String scope, GroupRoles role, CoursesStatisticsRuntimeTypesGroup runtimesGroup) {
		removeAsListenerAndDispose(coursesListCtrl);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(scope, 0l);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = addToHistory(ureq, ores, null);
		coursesListCtrl = new CourseListController(ureq, bwControl, role, runtimesGroup);
		listenTo(coursesListCtrl);
		mainVC.put("component", coursesListCtrl.getInitialComponent());
		return coursesListCtrl;
	}
}
