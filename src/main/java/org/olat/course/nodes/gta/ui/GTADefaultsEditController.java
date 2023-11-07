/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.course.nodes.gta.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.nodes.gta.GTAModule;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Nov 03, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class GTADefaultsEditController extends FormBasicController {

	private static final String[] onKeys = new String[]{"on"};
	private static final String[] optionalKeys = new String[]{AssessmentObligation.mandatory.name(), AssessmentObligation.optional.name()};

	private SingleSelection optionalEl;
	private MultipleSelectionElement taskAssignmentEl;
	private MultipleSelectionElement submissionEl;
	private MultipleSelectionElement lateSubmissionEl;
	private MultipleSelectionElement reviewEl;
	private MultipleSelectionElement revisionEl;
	private MultipleSelectionElement sampleEl;
	private MultipleSelectionElement gradingEl;
	private MultipleSelectionElement coachAllowedUploadEl;
	private MultipleSelectionElement coachAssignmentEnabledEl;

	@Autowired
	private GTAModule gtaModule;

	public GTADefaultsEditController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer stepsCont = FormLayoutContainer.createDefaultFormLayout("steps", getTranslator());
		stepsCont.setFormTitle(translate("task.steps.title"));
		stepsCont.setFormDescription(translate("task.steps.description"));
		stepsCont.setRootForm(mainForm);
		formLayout.add(stepsCont);

		String[] optionalValues = new String[]{
				translate("task.mandatory"), translate("task.optional"),
		};
		optionalEl = uifactory.addRadiosHorizontal("obligation", "task.obligation", stepsCont, optionalKeys, optionalValues);
		optionalEl.addActionListener(FormEvent.ONCHANGE);
		if (gtaModule.hasObligation()) {
			optionalEl.select(optionalKeys[0], true);
		} else {
			optionalEl.select(optionalKeys[1], true);
		}

		String[] assignmentValues = new String[]{translate("task.assignment.enabled")};
		taskAssignmentEl = uifactory.addCheckboxesHorizontal("task.assignment", "task.assignment", stepsCont, onKeys, assignmentValues);
		taskAssignmentEl.addActionListener(FormEvent.ONCHANGE);
		boolean assignment = gtaModule.hasAssignment();
		taskAssignmentEl.select(onKeys[0], assignment);

		String[] submissionValues = new String[]{translate("submission.enabled")};
		submissionEl = uifactory.addCheckboxesHorizontal("submission", "submission", stepsCont, onKeys, submissionValues);
		submissionEl.addActionListener(FormEvent.ONCHANGE);
		boolean submit = gtaModule.hasSubmission();
		submissionEl.select(onKeys[0], submit);

		String[] lateSubmissionValues = new String[]{translate("late.submission.enabled")};
		lateSubmissionEl = uifactory.addCheckboxesHorizontal("late.submission", "late.submission", stepsCont, onKeys, lateSubmissionValues);
		lateSubmissionEl.addActionListener(FormEvent.ONCHANGE);
		boolean lateSubmit = gtaModule.hasLateSubmission();
		lateSubmissionEl.select(onKeys[0], lateSubmit);

		//review and correction
		String[] reviewValues = new String[]{translate("review.enabled")};
		reviewEl = uifactory.addCheckboxesHorizontal("review", "review.and.correction", stepsCont, onKeys, reviewValues);
		reviewEl.addActionListener(FormEvent.ONCHANGE);
		boolean review = gtaModule.hasReviewAndCorrection();
		reviewEl.select(onKeys[0], review);

		//revision
		String[] revisionValues = new String[]{translate("revision.enabled")};
		revisionEl = uifactory.addCheckboxesHorizontal("revision", "revision.period", stepsCont, onKeys, revisionValues);
		revisionEl.addActionListener(FormEvent.ONCHANGE);
		boolean revision = gtaModule.hasRevisionPeriod();
		revisionEl.select(onKeys[0], revision);
		revisionEl.setVisible(review);

		//sample solution
		String[] sampleValues = new String[]{translate("sample.solution.enabled")};
		sampleEl = uifactory.addCheckboxesHorizontal("sample", "sample.solution", stepsCont, onKeys, sampleValues);
		sampleEl.addActionListener(FormEvent.ONCHANGE);
		boolean sample = gtaModule.hasSampleSolution();
		sampleEl.select(onKeys[0], sample);

		//grading
		String[] gradingValues = new String[]{translate("grading.enabled")};
		gradingEl = uifactory.addCheckboxesHorizontal("grading", "grading", stepsCont, onKeys, gradingValues);
		gradingEl.addActionListener(FormEvent.ONCHANGE);
		boolean grading = gtaModule.hasGrading();
		gradingEl.select(onKeys[0], grading);

		//coach allowed to upload documents
		FormLayoutContainer documentsCont = FormLayoutContainer.createDefaultFormLayout("documents", getTranslator());
		documentsCont.setFormTitle(translate("task.documents.title"));
		documentsCont.setRootForm(mainForm);
		formLayout.add(documentsCont);

		String[] onValuesCoachAllowed = new String[]{translate("task.manage.documents.coach")};
		coachAllowedUploadEl = uifactory.addCheckboxesVertical("coachTasks", "task.manage.documents", documentsCont, onKeys, onValuesCoachAllowed, 1);
		boolean coachUpload = gtaModule.canCoachUploadTasks();
		if (coachUpload) {
			coachAllowedUploadEl.select(onKeys[0], true);
		}

		// assignment coaches/participants (only for individual tasks)
		FormLayoutContainer coachingLayout = uifactory.addDefaultFormLayout("coaching", null, formLayout);
		coachingLayout.setFormTitle(translate("coach.assignment.title"));

		String[] onValuesCoachAssignmentEnabled = new String[]{translate("coach.assignment.enabled")};
		coachAssignmentEnabledEl = uifactory.addCheckboxesVertical("coach.assignment", coachingLayout, onKeys, onValuesCoachAssignmentEnabled, 1);
		coachAssignmentEnabledEl.addActionListener(FormEvent.ONCHANGE);
		boolean coachAssignment = gtaModule.canCoachAssign();
		if (coachAssignment) {
			coachAssignmentEnabledEl.select(onKeys[0], true);
		}

		// Create submit button
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout)
				.setElementCssClass("o_sel_node_editor_submit");
	}

	private void updateDefaultConfigValues() {
		gtaModule.setHasObligation(optionalEl.isKeySelected(optionalKeys[0]));
		gtaModule.setHasAssignment(taskAssignmentEl.isSelected(0));
		gtaModule.setHasSubmission(submissionEl.isSelected(0));
		gtaModule.setHasLateSubmission(lateSubmissionEl.isSelected(0));
		gtaModule.setHasReviewAndCorrection(reviewEl.isSelected(0));
		gtaModule.setHasRevisionPeriod(revisionEl.isSelected(0));
		gtaModule.setHasSampleSolution(sampleEl.isSelected(0));
		gtaModule.setHasGrading(gradingEl.isSelected(0));
		gtaModule.setCanCoachUploadTasks(coachAllowedUploadEl.isSelected(0));
		gtaModule.setCanCoachAssign(coachAssignmentEnabledEl.isSelected(0));
	}

	@Override
	protected void formOK(UserRequest ureq) {
		updateDefaultConfigValues();
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		taskAssignmentEl.clearError();
		if (!taskAssignmentEl.isAtLeastSelected(1) && !submissionEl.isAtLeastSelected(1)
				&& !reviewEl.isAtLeastSelected(1) && !revisionEl.isAtLeastSelected(1)
				&& !sampleEl.isAtLeastSelected(1) && !gradingEl.isAtLeastSelected(1)) {
			taskAssignmentEl.setErrorKey("error.select.atleastonestep");
			allOk &= false;
		}

		return allOk;
	}
}
