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
package org.olat.modules.coach.ui.curriculum.course;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.scope.Scope;
import org.olat.core.gui.components.scope.ScopeEvent;
import org.olat.core.gui.components.scope.ScopeFactory;
import org.olat.core.gui.components.scope.ScopeSelection;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.coach.RoleSecurityCallback;
import org.olat.modules.coach.ui.EnrollmentListController;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.curriculum.ui.ImplementationsListConfig;
import org.olat.repository.ui.list.ImplementationsListController;
import org.springframework.beans.factory.annotation.Autowired;

public class CourseListWrapperController extends BasicController implements Activateable2 {

    static final String CMD_ALL_COURSES = "List";
	static final String CMD_IMPLEMENTATION = "Implementation";
	static final String CMD_IMPLEMENTATIONS_LIST = "Implementations";
    
    private VelocityContainer mainVC;
    private ScopeSelection scopesSelection;
    private final TooledStackedPanel stackPanel;
	private TooledStackedPanel implementationsListStackPanel;
    
    private final Identity mentee;
	private final Identity coach;
    private final Object statEntry;
    private final RoleSecurityCallback roleSecurityCallback;

    private EnrollmentListController allCoursesCtrl;
    private ImplementationsListController implementationsListCtrl;

    @Autowired
    private CurriculumModule curriculumModule;

	public CourseListWrapperController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			Identity mentee, Identity coach, RoleSecurityCallback roleSecurityCallback, Object statEntry,
			boolean onlyImplementations) {
		super(ureq, wControl);
		
		this.stackPanel = stackPanel;
		this.mentee = mentee;
		this.coach = coach;
		this.roleSecurityCallback = roleSecurityCallback;
		this.statEntry = statEntry;

		mainVC = createVelocityContainer("course_list_wrapper");

		List<Scope> scopes = new ArrayList<>(4);
		scopes.add(ScopeFactory.createScope(CMD_ALL_COURSES, translate("all.courses"), null, "o_icon o_icon-fw o_icon_curriculum"));
		if(curriculumModule.isEnabled()) {
			scopes.add(ScopeFactory.createScope(CMD_IMPLEMENTATIONS_LIST, translate("search.education.products"),
					null, "o_icon o_icon-fw o_icon_curriculum"));
		}
		scopesSelection = ScopeFactory.createScopeSelection("scopes", mainVC, this, scopes);
		
		if (onlyImplementations) {
			mainVC.contextPut("showTitle", Boolean.TRUE);
			doOpenImplementations(ureq);
		} else {
			mainVC.contextPut("showScopes", true);
			doOpenAllCourses(ureq);
		}

        putInitialPanel(mainVC);
    }

    @Override
    public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
        if(entries == null || entries.isEmpty()) return;
        
        ContextEntry currentEntry = entries.get(0);
        String cmd = currentEntry.getOLATResourceable().getResourceableTypeName();

        Activateable2 selectedCtrl = null;
        if(CMD_ALL_COURSES.equalsIgnoreCase(cmd)) {
            selectedCtrl = doOpenAllCourses(ureq);
        } else if (CMD_IMPLEMENTATIONS_LIST.equalsIgnoreCase(cmd)) {
            doOpenImplementations(ureq);
        }
        
        if(selectedCtrl != null) {
        	List<ContextEntry> subEntries = entries.subList(1, entries.size());
        	selectedCtrl.activate(ureq, subEntries, currentEntry.getTransientState());
        }
    }

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == scopesSelection) {
			if(event instanceof ScopeEvent se) {
				if(CMD_ALL_COURSES.equals(se.getSelectedKey())) {
					doOpenAllCourses(ureq);
				} else if(CMD_IMPLEMENTATIONS_LIST.equals(se.getSelectedKey())) {
					doOpenImplementations(ureq);
				} else if(se.getSelectedKey().startsWith(CMD_IMPLEMENTATION)) {
					Long implementationKey = Long.valueOf(se.getSelectedKey().replace(CMD_IMPLEMENTATION, ""));
					List<ContextEntry> entries = BusinessControlFactory.getInstance()
							.createCEListFromString(OresHelper.createOLATResourceableInstance(CurriculumElement.class, implementationKey));
					doOpenImplementations(ureq).activate(ureq, entries, null);
					scopesSelection.setSelectedKey(se.getSelectedKey());// Reselect because with listen to pop
				}
			}
		 } else if(implementationsListStackPanel == source) {
			if(event instanceof PopEvent 
					&& implementationsListStackPanel.getLastController() == implementationsListCtrl) {
				scopesSelection.setSelectedKey(CMD_IMPLEMENTATIONS_LIST);
			}
		}
	}

	private Activateable2 doOpenImplementations(UserRequest ureq) {
		if(implementationsListCtrl == null) {
			implementationsListStackPanel = new TooledStackedPanel("myliststack", getTranslator(), this);
			implementationsListStackPanel.setToolbarEnabled(false);
			
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType(CMD_IMPLEMENTATIONS_LIST), null);
			ImplementationsListConfig.Builder configBuilder = ImplementationsListConfig.builder(List.of(GroupRoles.participant))
					.setCoachIdentity(coach)
					.enableId()
					.enableExtRefVisibilityDefault()
					.enableRoles();
			if (roleSecurityCallback.canViewCourseProgressAndStatus()) {
				configBuilder.enableStatus().enableCompletion();
			}
			if (roleSecurityCallback.canViewCalendar()) {
				configBuilder.enableCalendar();
			}
			ImplementationsListConfig config = configBuilder.build();
			implementationsListCtrl = new ImplementationsListController(ureq, bwControl, implementationsListStackPanel, mentee, config);
			implementationsListStackPanel.pushController(translate("search.implementations.list"), implementationsListCtrl);
		} else {
			implementationsListStackPanel.popUpToRootController(ureq);
		}

		mainVC.put("content", implementationsListStackPanel);
		addToHistory(ureq, implementationsListCtrl);
		return implementationsListCtrl;
    }

    private Activateable2 doOpenAllCourses(UserRequest ureq) {
        if (allCoursesCtrl == null) {
            WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType(CMD_ALL_COURSES), null);
            allCoursesCtrl = new EnrollmentListController(ureq, bwControl, stackPanel, statEntry, mentee, roleSecurityCallback);
            listenTo(allCoursesCtrl);
        }

        mainVC.put("content", allCoursesCtrl.getInitialComponent());
    	addToHistory(ureq, allCoursesCtrl);
        return allCoursesCtrl;
    }
}
