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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskDueDate;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.model.DueDate;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.Role;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26.02.2018<br>
 * @author Stephan Clemenz, clemenz@vcrp.de
 *
 */
public class EditMultipleDueDatesController extends FormBasicController {
	
	private DateChooser assignmentDueDateEl, submissionDueDateEl, revisionDueDateEl, solutionDueDateEl;
	
	private List<Task> tasks;
	private GTACourseNode gtaNode;
	private final Formatter formatter;
	private final RepositoryEntry courseEntry;
	private final CourseEnvironment courseEnv;
	
	@Autowired
	private GTAManager gtaManager;
	
	public EditMultipleDueDatesController(UserRequest ureq, WindowControl wControl, List<Task> tasks,
			GTACourseNode gtaNode, RepositoryEntry courseEntry, CourseEnvironment courseEnv) {
		super(ureq, wControl);
		this.tasks = tasks;
		this.gtaNode = gtaNode;
		this.courseEnv = courseEnv;
		this.courseEntry = courseEntry;
		formatter = Formatter.getInstance(getLocale());
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		ModuleConfiguration config = gtaNode.getModuleConfiguration();
		
		assignmentDueDateEl = uifactory.addDateChooser("assignment.duedate", null, formLayout);
		assignmentDueDateEl.setDateChooserTimeEnabled(true);
		DueDate standardAssignmentDueDate = gtaManager.getAssignmentDueDate(null, null, null, gtaNode, courseEntry, false);
		setDueDateExplanation(assignmentDueDateEl, standardAssignmentDueDate);
		assignmentDueDateEl.setVisible(config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT));
		
		submissionDueDateEl = uifactory.addDateChooser("submission.duedate", null, formLayout);
		submissionDueDateEl.setDateChooserTimeEnabled(true);
		DueDate standardSubmissionDueDate = gtaManager.getSubmissionDueDate(null, null, null, gtaNode, courseEntry, false);
		setDueDateExplanation(submissionDueDateEl, standardSubmissionDueDate);
		boolean submissionDeadline = config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT);
		submissionDueDateEl.setVisible(submissionDeadline);
		if(submissionDeadline) {
			for(Task task:tasks) {
				if(task.getTaskStatus().ordinal() > TaskProcess.submit.ordinal()) {
					StaticTextElement warningReopenEl = uifactory.addStaticTextElement("reopen", translate("warning.reopen"), formLayout);
					warningReopenEl.setElementCssClass("o_gta_reopen_warning");
					warningReopenEl.setLabel(null, null);
					break;
				}
			}
		}
		
		revisionDueDateEl = uifactory.addDateChooser("revisions.duedate", null, formLayout);
		revisionDueDateEl.setDateChooserTimeEnabled(true);
		revisionDueDateEl.setVisible(config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD));
		
		solutionDueDateEl = uifactory.addDateChooser("solution.duedate", null, formLayout);
		solutionDueDateEl.setDateChooserTimeEnabled(true);
		DueDate standardSolutionDueDate = gtaManager.getSolutionDueDate(null, null, null, gtaNode, courseEntry, false);
		setDueDateExplanation(solutionDueDateEl, standardSolutionDueDate);
		solutionDueDateEl.setVisible(config.getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION));

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", buttonsCont);
	}
	
	private void setDueDateExplanation(DateChooser dateEl, DueDate standardDueDate) {
		if(standardDueDate != null) {
			if(standardDueDate.getDueDate() != null) {
				dateEl.setExampleKey("duedate.standard", new String[] { formatter.formatDateAndTime(standardDueDate.getDueDate()) });
			} else if(standardDueDate.getMessageKey() != null) {
				dateEl.setExampleKey(standardDueDate.getMessageKey(), new String[] { standardDueDate.getMessageArg() });
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		for (Task task : tasks) {
			TaskDueDate dueDates = gtaManager.getDueDatesTask(task);
			dueDates.setAssignmentDueDate(assignmentDueDateEl.getDate());	
			dueDates.setSubmissionDueDate(submissionDueDateEl.getDate());	
			dueDates.setRevisionsDueDate(revisionDueDateEl.getDate());
			dueDates.setSolutionDueDate(solutionDueDateEl.getDate());
			dueDates = gtaManager.updateTaskDueDate(dueDates);
			
			if(task.getTaskStatus().ordinal() > TaskProcess.submit.ordinal()
					&& dueDates.getSubmissionDueDate() != null
					&& dueDates.getSubmissionDueDate().after(ureq.getRequestTimestamp())) {
				TaskProcess submit = gtaManager.previousStep(TaskProcess.review, gtaNode);//only submit allowed
				if(submit == TaskProcess.submit) {
					task = gtaManager.updateTask(task, submit, gtaNode, false, getIdentity(), Role.coach);
					gtaManager.log("Back to submission", "revert status of task back to submission", task,
							getIdentity(), task.getIdentity(), task.getBusinessGroup(), courseEnv, gtaNode, Role.coach);
				}
			}
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}