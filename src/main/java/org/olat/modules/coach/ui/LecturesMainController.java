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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.scope.Scope;
import org.olat.core.gui.components.scope.ScopeEvent;
import org.olat.core.gui.components.scope.ScopeFactory;
import org.olat.core.gui.components.scope.ScopeSelection;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.coach.model.CoachingSecurity;
import org.olat.modules.lecture.ui.LectureRoles;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.modules.lecture.ui.LecturesSecurityCallbackFactory;
import org.olat.modules.lecture.ui.coach.LecturesCoachingController;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * 
 * Initial date: 7 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class LecturesMainController extends BasicController implements Activateable2 {

	private static final String MAIN_CONTROLLER = "component";
	
	private static final String TEACHER_SCOPE = "teacher";
	private static final String MASTER_COACH_SCOPE = "mastercoach";
	
	private final VelocityContainer mainVC;
	private final TooledStackedPanel content;
	private final ScopeSelection searchScopes;
	
	private LecturesCoachingController lecturesCtrl;
	
	public LecturesMainController(UserRequest ureq, WindowControl wControl, TooledStackedPanel content, CoachingSecurity coachingSec) {
		super(ureq, wControl);
		this.content = content;

		mainVC = createVelocityContainer("lectures");
		
		List<Scope> scopes = new ArrayList<>(4);
		if(coachingSec.isTeacher()) {
			scopes.add(ScopeFactory.createScope(TEACHER_SCOPE, translate("lectures.teacher.menu.title"), null, "o_icon o_icon_coaching_tool"));
		}
		if(coachingSec.isMasterCoachForLectures()) {
			scopes.add(ScopeFactory.createScope(MASTER_COACH_SCOPE, translate("lectures.mastercoach.menu.title"), null, "o_icon o_icon_num_participants"));
		}
		searchScopes = ScopeFactory.createScopeSelection("search.scopes", mainVC, this, scopes);
		searchScopes.setVisible(scopes.size() > 1);
		
		putInitialPanel(mainVC);
		
		if(coachingSec.isTeacher()) {
			searchScopes.setSelectedKey(TEACHER_SCOPE);
			doOpenAsTeacher(ureq);
		} else if(coachingSec.isMasterCoachForLectures()) {
			searchScopes.setSelectedKey(MASTER_COACH_SCOPE);
			doOpenAsMasterCoach(ureq);
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(TEACHER_SCOPE.equalsIgnoreCase(type)) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			doOpenAsTeacher(ureq).activate(ureq, subEntries, state);
			searchScopes.setSelectedKey(TEACHER_SCOPE);
		} else if(MASTER_COACH_SCOPE.equalsIgnoreCase(type)) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			doOpenAsMasterCoach(ureq).activate(ureq, subEntries, state);
			searchScopes.setSelectedKey(MASTER_COACH_SCOPE);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(searchScopes == source) {
			if(event instanceof ScopeEvent se) {
				doOpen(ureq, se.getSelectedKey());
			}
		}
	}
	
	private void doOpen(UserRequest ureq, String scope) {
		switch(scope) {
			case TEACHER_SCOPE -> doOpenAsTeacher(ureq);
			case MASTER_COACH_SCOPE -> doOpenAsMasterCoach(ureq);
			default -> {}
		}
	}
	
	private LecturesCoachingController doOpenAsTeacher(UserRequest ureq) {
		removeAsListenerAndDispose(lecturesCtrl);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Teacher", 0l);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = addToHistory(ureq, ores, null);
		LecturesSecurityCallback secCallback = LecturesSecurityCallbackFactory
				.getSecurityCallback(false, false, false, true, List.of(), List.of(), LectureRoles.teacher);
		lecturesCtrl = new LecturesCoachingController(ureq, bwControl, content, secCallback);
		listenTo(lecturesCtrl);
		mainVC.put(MAIN_CONTROLLER, lecturesCtrl.getInitialComponent());
		return lecturesCtrl;
	}
	
	private LecturesCoachingController doOpenAsMasterCoach(UserRequest ureq) {
		removeAsListenerAndDispose(lecturesCtrl);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("MasterCoach", 0l);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = addToHistory(ureq, ores, null);
		LecturesSecurityCallback secCallback = LecturesSecurityCallbackFactory
				.getSecurityCallback(false, false, true, false, List.of(), List.of(), LectureRoles.mastercoach);
		lecturesCtrl = new LecturesCoachingController(ureq, bwControl, content, secCallback);
		listenTo(lecturesCtrl);
		mainVC.put(MAIN_CONTROLLER, lecturesCtrl.getInitialComponent());
		return lecturesCtrl;
	}
}
