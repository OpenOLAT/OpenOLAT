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

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskDueDate;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.model.DueDate;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.Role;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 août 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditDueDatesController extends FormBasicController {
	
	private DateChooser assignmentDueDateEl;
	private DateChooser submissionDueDateEl;
	private DateChooser revisionDueDateEl;
	private DateChooser solutionDueDateEl;
	
	private Task task;
	private GTACourseNode gtaNode;
	private Identity assessedIdentity;
	private BusinessGroup assessedGroup;
	private final Formatter formatter;
	private final RepositoryEntry courseEntry;
	private final CourseEnvironment courseEnv;
	
	@Autowired
	private GTAManager gtaManager;
	
	public EditDueDatesController(UserRequest ureq, WindowControl wControl, Task task,
			Identity assessedIdentity, BusinessGroup assessedGroup,
			GTACourseNode gtaNode, RepositoryEntry courseEntry, CourseEnvironment courseEnv) {
		super(ureq, wControl);
		this.task = task;
		this.gtaNode = gtaNode;
		this.courseEnv = courseEnv;
		this.courseEntry = courseEntry;
		this.assessedGroup = assessedGroup;
		this.assessedIdentity = assessedIdentity;
		formatter = Formatter.getInstance(getLocale());
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		ModuleConfiguration config = gtaNode.getModuleConfiguration();
		
		Date assignmentDueDate = task.getAssignmentDueDate();
		assignmentDueDateEl = uifactory.addDateChooser("assignment.duedate", assignmentDueDate, formLayout);
		assignmentDueDateEl.setDateChooserTimeEnabled(true);
		DueDate standardAssignmentDueDate = gtaManager.getAssignmentDueDate(task, assessedIdentity, assessedGroup, gtaNode, courseEntry, false);
		setDueDateExplanation(assignmentDueDateEl, standardAssignmentDueDate, null);
		assignmentDueDateEl.setVisible(config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT));
		
		Date submissionDueDate = task.getSubmissionDueDate();
		submissionDueDateEl = uifactory.addDateChooser("submission.duedate", submissionDueDate, formLayout);
		submissionDueDateEl.setDateChooserTimeEnabled(true);
		DueDate standardSubmissionDueDate = gtaManager.getSubmissionDueDate(task, assessedIdentity, assessedGroup, gtaNode, courseEntry, false);
		DueDate lateSubmissionDueDate = gtaManager.getLateSubmissionDueDate(task, assessedIdentity, assessedGroup, gtaNode, courseEntry, true);
		
		setDueDateExplanation(submissionDueDateEl, standardSubmissionDueDate, lateSubmissionDueDate);
		boolean submissionDeadline = config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT);
		submissionDueDateEl.setVisible(submissionDeadline);
		if(submissionDeadline && task.getTaskStatus().ordinal() > TaskProcess.submit.ordinal()) {
			StaticTextElement warningReopenEl = uifactory.addStaticTextElement("reopen", translate("warning.reopen"), formLayout);
			warningReopenEl.setElementCssClass("o_gta_reopen_warning");
			warningReopenEl.setLabel(null, null);
		}
		
		Date revisionsDueDate = task.getRevisionsDueDate();
		revisionDueDateEl = uifactory.addDateChooser("revisions.duedate", revisionsDueDate, formLayout);
		revisionDueDateEl.setDateChooserTimeEnabled(true);
		revisionDueDateEl.setVisible(config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD));
		
		Date solutionDueDate = task.getSolutionDueDate();
		solutionDueDateEl = uifactory.addDateChooser("solution.duedate", solutionDueDate, formLayout);
		solutionDueDateEl.setDateChooserTimeEnabled(true);
		DueDate standardSolutionDueDate = gtaManager.getSolutionDueDate(task, assessedIdentity, assessedGroup, gtaNode, courseEntry, true);
		setDueDateExplanation(solutionDueDateEl, standardSolutionDueDate, null);
		solutionDueDateEl.setVisible(config.getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION));

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", buttonsCont);
	}
	
	private void setDueDateExplanation(DateChooser dateEl, DueDate standardDueDate, DueDate lateDueDate) {
		if(standardDueDate != null) {
			if(lateDueDate != null) {
				dateEl.setExampleKey("duedate.late", new String[] { formatter.formatDateAndTime(standardDueDate.getReferenceDueDate()),
						formatter.formatDateAndTime(lateDueDate.getReferenceDueDate()) });
			} else if(standardDueDate.getDueDate() != null) {
				dateEl.setExampleKey("duedate.standard", new String[] { formatter.formatDateAndTime(standardDueDate.getDueDate()) });
			} else if(standardDueDate.getMessageKey() != null) {
				dateEl.setExampleKey(standardDueDate.getMessageKey(), new String[] { standardDueDate.getMessageArg() });
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
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
				gtaManager.log("Back to submission", "revert status of task back to submission", task, getIdentity(),
						assessedIdentity, assessedGroup, courseEnv, gtaNode, Role.coach);
			}
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}