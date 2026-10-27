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
import org.olat.modules.coach.model.CoachingSecurity;
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
	
	private static final String SCOPE_COURSES = GroupRoles.owner.name();
	private static final String SCOPE_OTHERS = "Others";
	
	private final List<Scope> scopes;
	private final ScopeSelection scopeEl;
	private final VelocityContainer mainVC;
	
	private final CoachingSecurity coachingSec;
	
	private CourseListController coursesListCtrl;
	
	@Autowired
	private CoachingService coachingService;
	
	public CoursesAndOthersController(UserRequest ureq, WindowControl wControl, CoachingSecurity coachingSec) {
		super(ureq, wControl);
		this.coachingSec = coachingSec;
		
		mainVC = createVelocityContainer("courses_and_others");
		
		scopes = new ArrayList<>(4);
		scopes.add(ScopeFactory.createScope(SCOPE_COURSES, translate("scope.courses"), null, "o_icon o_icon_coaching_tool"));
	
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
			doOpenCourses(ureq).activate(ureq, List.of(), null);
			scopeEl.setSelectedKey(SCOPE_COURSES);
			return;
		}
		
		String scope = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(SCOPE_COURSES.equalsIgnoreCase(scope)) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			doOpenCourses(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
			scopeEl.setSelectedKey(SCOPE_COURSES);
		} else if(SCOPE_OTHERS.equalsIgnoreCase(scope)) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			doOpenOthers(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
			scopeEl.setSelectedKey(SCOPE_OTHERS);
		} else {
			doOpenCourses(ureq).activate(ureq, entries, null);
			scopeEl.setSelectedKey(SCOPE_COURSES);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(scopeEl == source) {
			if (event instanceof ScopeEvent se) {
				if(SCOPE_COURSES.equals(se.getSelectedKey())) {
					doOpenCourses(ureq).activate(ureq, List.of(), null);
				} else if(SCOPE_OTHERS.equals(se.getSelectedKey())) {
					doOpenOthers(ureq).activate(ureq, List.of(), null);
				}
			}
		}
	}
	
	private CourseListController doOpenCourses(UserRequest ureq) {
		List<GroupRoles> roles = new ArrayList<>();
		if(coachingSec.owner()) {
			roles.add(GroupRoles.owner);
		}
		if(coachingSec.coach()) {
			roles.add(GroupRoles.coach);
		}
		return doOpenCourses(ureq, SCOPE_COURSES, roles,
				CoursesStatisticsRuntimeTypesGroup.standaloneAndCurricular);
	}
	
	private CourseListController doOpenOthers(UserRequest ureq) {
		return doOpenCourses(ureq, SCOPE_OTHERS, List.of(GroupRoles.owner),
				CoursesStatisticsRuntimeTypesGroup.embeddedAndTemplate);
	}
	
	private CourseListController doOpenCourses(UserRequest ureq, String scope, List<GroupRoles> roles, CoursesStatisticsRuntimeTypesGroup runtimesGroup) {
		removeAsListenerAndDispose(coursesListCtrl);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(scope, 0l);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = addToHistory(ureq, ores, null);
		CourseListConfig config = CourseListConfig.defaultConfig();
		coursesListCtrl = new CourseListController(ureq, bwControl, roles, runtimesGroup, config);
		listenTo(coursesListCtrl);
		mainVC.put("component", coursesListCtrl.getInitialComponent());
		return coursesListCtrl;
	}
}
