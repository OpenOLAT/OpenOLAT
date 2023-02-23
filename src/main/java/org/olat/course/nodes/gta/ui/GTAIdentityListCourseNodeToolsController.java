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
package org.olat.course.nodes.gta.ui;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.course.assessment.ui.tool.AssignCoachController;
import org.olat.course.assessment.ui.tool.tools.AbstractToolsController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.Role;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAIdentityListCourseNodeToolsController extends AbstractToolsController {

	private Link extendLink;
	private Link coachAssignmentLink;
	
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	
	private CloseableModalController cmc;
	private AssignCoachController assignCoachCtrl;
	private EditDueDatesController editDueDatesCtrl;
	
	public GTAIdentityListCourseNodeToolsController(UserRequest ureq, WindowControl wControl,
			CourseNode courseNode, Identity assessedIdentity, UserCourseEnvironment coachCourseEnv) {
		super(ureq, wControl, courseNode, assessedIdentity, coachCourseEnv);
		
		initTools();
	}

	@Override
	protected void initResetAttempts() {
		if(GTAType.individual.name().equals(courseNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_TYPE))) {
			super.initResetAttempts();
		}
	}

	@Override
	protected void initStatus() {
		super.initStatus();

		ModuleConfiguration config =  courseNode.getModuleConfiguration();
		if(GTAType.individual.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))
				&& gtaManager.isDueDateEnabled((GTACourseNode)courseNode)
				&& !config.getBooleanSafe(GTACourseNode.GTASK_RELATIVE_DATES)) {
			addSeparator();

			extendLink = addLink("duedates", "duedates", "o_icon o_icon-fw o_icon_extra_time");
		}
		
		if(assessmentConfig.hasCoachAssignment()) {
			coachAssignmentLink = addLink("assign.coach", "assign.coach", "o_icon o_icon-fw o_icon_coach");
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editDueDatesCtrl == source || assignCoachCtrl == source) {
			if(event == Event.DONE_EVENT) {
				fireEvent(ureq, Event.CHANGED_EVENT);
			} else {
				fireEvent(ureq, Event.DONE_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void cleanUp() {
		super.cleanUp();
		removeControllerListener(editDueDatesCtrl);
		removeControllerListener(assignCoachCtrl);
		removeControllerListener(cmc);
		editDueDatesCtrl = null;
		assignCoachCtrl = null;
		cmc = null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(extendLink == source) {
			fireEvent(ureq, Event.CLOSE_EVENT);
			doEditDueDate(ureq);
		} else if(coachAssignmentLink == source) {
			fireEvent(ureq, Event.CLOSE_EVENT);
			doAssignCoach(ureq);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void reopenEvaluation() {
		super.reopenEvaluation();

		TaskList taskList = gtaManager.getTaskList(getCourseRepositoryEntry(), (GTACourseNode)courseNode);
		Task task = gtaManager.getTask(assessedIdentity, taskList);
		if(task != null && task.getTaskStatus() == TaskProcess.graded) {
			gtaManager.updateTask(task, TaskProcess.grading, (GTACourseNode)courseNode, false, getIdentity(), Role.coach);
		}
	}

	@Override
	protected void doneEvalution() {
		super.doneEvalution();
		
		TaskList taskList = gtaManager.getTaskList(getCourseRepositoryEntry(), (GTACourseNode)courseNode);
		Task task = gtaManager.getTask(assessedIdentity, taskList);
		if(task != null) {
			gtaManager.updateTask(task, TaskProcess.graded, (GTACourseNode)courseNode, false, getIdentity(), Role.coach);
		}
	}
	
	private void doEditDueDate(UserRequest ureq) {
		if(guardModalController(editDueDatesCtrl)) return;
		
		GTACourseNode gtaNode = (GTACourseNode)courseNode;
		RepositoryEntry entry = getCourseRepositoryEntry();
		TaskList taskList = gtaManager.getTaskList(entry, gtaNode);
		Task task = gtaManager.getTask(assessedIdentity, taskList);
		if(task == null) {
			TaskProcess firstStep = gtaManager.firstStep(gtaNode);
			task = gtaManager.createAndPersistTask(null, taskList, firstStep, null, assessedIdentity, gtaNode);
		}

		CourseEnvironment courseEnv = assessedUserCourseEnv.getCourseEnvironment();
		editDueDatesCtrl = new EditDueDatesController(ureq, getWindowControl(), task, assessedIdentity, null, gtaNode, entry, courseEnv);
		listenTo(editDueDatesCtrl);
		
		String fullname = userManager.getUserDisplayName(assessedIdentity);
		String title = translate("duedates.user", fullname);
		cmc = new CloseableModalController(getWindowControl(), "close", editDueDatesCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAssignCoach(UserRequest ureq) {
		Identity currentCoach = null;
		AssessmentEntry assessmentEntry = courseAssessmentService.getAssessmentEntry(courseNode, assessedUserCourseEnv);
		if(assessmentEntry.getCoach() != null) {
			currentCoach = securityManager.loadIdentityByKey(assessmentEntry.getCoach().getKey());
		}
		CourseEnvironment courseEnv = assessedUserCourseEnv.getCourseEnvironment();
		assignCoachCtrl = new AssignCoachController(ureq, getWindowControl(), assessedIdentity, currentCoach, courseEnv, courseNode);
		listenTo(assignCoachCtrl);

		String fullname = userManager.getUserDisplayName(assessedIdentity);
		String title = translate("assign.coach.to", fullname);
		cmc = new CloseableModalController(getWindowControl(), "close", assignCoachCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
}
