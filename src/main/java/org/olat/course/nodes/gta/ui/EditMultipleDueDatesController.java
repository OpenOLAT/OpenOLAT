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
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
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
	private MultipleSelectionElement assignmentTakeOverEl, submissionTakeOverEl, revisionTakeOverEl, solutionTakeOverEl;
	
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
		SelectionValues takeOverSV = new SelectionValues();
		takeOverSV.add(SelectionValues.entry("key", translate("take.over")));
		
		assignmentTakeOverEl = uifactory.addCheckboxesVertical("assignment.take.over", "assignment.duedate", formLayout, takeOverSV.keys(), takeOverSV.values(), 1);
		assignmentTakeOverEl.addActionListener(FormEvent.ONCHANGE);
		
		assignmentDueDateEl = uifactory.addDateChooser("assignment.duedate", null, formLayout);
		assignmentDueDateEl.setDateChooserTimeEnabled(true);
		initDate(assignmentTakeOverEl, assignmentDueDateEl, Task::getAssignmentDueDate, config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT));
		DueDate standardAssignmentDueDate = gtaManager.getAssignmentDueDate(null, null, null, gtaNode, courseEntry, false);
		setDueDateExplanation(assignmentDueDateEl, standardAssignmentDueDate);
		
		submissionTakeOverEl = uifactory.addCheckboxesVertical("submission.take.over", "submission.duedate", formLayout, takeOverSV.keys(), takeOverSV.values(), 1);
		submissionTakeOverEl.addActionListener(FormEvent.ONCHANGE);
		
		submissionDueDateEl = uifactory.addDateChooser("submission.duedate", null, formLayout);
		submissionDueDateEl.setDateChooserTimeEnabled(true);
		boolean submissionDeadline = config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT);
		initDate(submissionTakeOverEl, submissionDueDateEl, Task::getSubmissionDueDate, submissionDeadline);
		DueDate standardSubmissionDueDate = gtaManager.getSubmissionDueDate(null, null, null, gtaNode, courseEntry, false);
		setDueDateExplanation(submissionDueDateEl, standardSubmissionDueDate);
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
		
		revisionTakeOverEl = uifactory.addCheckboxesVertical("revision.take.over", "revision.duedate", formLayout, takeOverSV.keys(), takeOverSV.values(), 1);
		revisionTakeOverEl.addActionListener(FormEvent.ONCHANGE);
		
		revisionDueDateEl = uifactory.addDateChooser("revisions.duedate", null, formLayout);
		revisionDueDateEl.setDateChooserTimeEnabled(true);
		initDate(revisionTakeOverEl, revisionDueDateEl, Task::getRevisionsDueDate, config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD));
		
		solutionTakeOverEl = uifactory.addCheckboxesVertical("solution.take.over", "solution.duedate", formLayout, takeOverSV.keys(), takeOverSV.values(), 1);
		solutionTakeOverEl.addActionListener(FormEvent.ONCHANGE);
		
		solutionDueDateEl = uifactory.addDateChooser("solution.duedate", null, formLayout);
		solutionDueDateEl.setDateChooserTimeEnabled(true);
		initDate(solutionTakeOverEl, solutionDueDateEl, Task::getSolutionDueDate, config.getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION));
		DueDate standardSolutionDueDate = gtaManager.getSolutionDueDate(null, null, null, gtaNode, courseEntry, false);
		setDueDateExplanation(solutionDueDateEl, standardSolutionDueDate);

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	private void initDate(MultipleSelectionElement takeOverEl, DateChooser dateEl, Function<Task, Date> dateFunction, boolean configEnabled) {
		if (!configEnabled) {
			takeOverEl.setVisible(false);
			dateEl.setVisible(false);
			return;
		}
		
		boolean firstDate = true;
		Date date = null;
		
		for (Task task : tasks) {
			Date taskDate = dateFunction.apply(task);
			if (firstDate) {
				date = taskDate;
				firstDate = false;
			} else if (!Objects.equals(date, taskDate)) {
				dateEl.setEnabled(false);
				dateEl.setLabel(null, null);
				dateEl.setWarningKey("duedate.individuals");
				return;
			}
		}
		
		dateEl.setDate(date);
		takeOverEl.setVisible(false);
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
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == assignmentTakeOverEl) {
			updateDateUI(assignmentTakeOverEl, assignmentDueDateEl);
		} else if (source == submissionTakeOverEl) {
			updateDateUI(submissionTakeOverEl, submissionDueDateEl);
		} else if (source == revisionTakeOverEl) {
			updateDateUI(revisionTakeOverEl, revisionDueDateEl);
		} else if (source == solutionTakeOverEl) {
			updateDateUI(solutionTakeOverEl, solutionDueDateEl);
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void updateDateUI(MultipleSelectionElement takeOverEl, DateChooser dateEl) {
		boolean takeOver = takeOverEl.isAtLeastSelected(1);
		dateEl.setEnabled(takeOver);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		for (Task task : tasks) {
			TaskDueDate dueDates = gtaManager.getDueDatesTask(task);
			if (assignmentDueDateEl.isEnabled()) {
				gtaManager.logIfChanged(assignmentDueDateEl.getDate(), dueDates.getAssignmentDueDate(), TaskProcess.assignment, task,
						getIdentity(), task.getIdentity(), task.getBusinessGroup(), courseEnv, gtaNode, Role.coach, formatter);
				dueDates.setAssignmentDueDate(assignmentDueDateEl.getDate());
			}
			if (submissionDueDateEl.isEnabled()) {
				gtaManager.logIfChanged(submissionDueDateEl.getDate(), dueDates.getSubmissionDueDate(), TaskProcess.submit, task,
						getIdentity(), task.getIdentity(), task.getBusinessGroup(), courseEnv, gtaNode, Role.coach, formatter);
				dueDates.setSubmissionDueDate(submissionDueDateEl.getDate());
			}
			if (revisionDueDateEl.isEnabled()) {
				gtaManager.logIfChanged(revisionDueDateEl.getDate(), dueDates.getRevisionsDueDate(), TaskProcess.revision, task,
						getIdentity(), task.getIdentity(), task.getBusinessGroup(), courseEnv, gtaNode, Role.coach, formatter);
				dueDates.setRevisionsDueDate(revisionDueDateEl.getDate());
			}
			if (solutionDueDateEl.isEnabled()) {
				gtaManager.logIfChanged(solutionDueDateEl.getDate(), dueDates.getSolutionDueDate(), TaskProcess.solution, task,
						getIdentity(), task.getIdentity(), task.getBusinessGroup(), courseEnv, gtaNode, Role.coach, formatter);
				dueDates.setSolutionDueDate(solutionDueDateEl.getDate());
			}
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