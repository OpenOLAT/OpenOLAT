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
import org.olat.course.nodes.gta.model.DueDate;
import org.olat.modules.ModuleConfiguration;
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
	
	@Autowired
	private GTAManager gtaManager;
	
	public EditMultipleDueDatesController(UserRequest ureq, WindowControl wControl, List<Task> tasks,
			GTACourseNode gtaNode, RepositoryEntry courseEntry) {
		super(ureq, wControl);
		this.tasks = tasks;
		this.gtaNode = gtaNode;
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
		submissionDueDateEl.setVisible(config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT));
		
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
	protected void doDispose() {
		// 
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
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}