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
package org.olat.course.assessment.ui.reset;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.ui.reset.ResetDataContext.ResetCourse;
import org.olat.course.assessment.ui.reset.ResetDataContext.ResetParticipants;
import org.olat.course.assessment.ui.reset.ResetWizardContext.ResetDataStep;

/**
 * 
 * Initial date: 8 mars 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ResetData1OptionsStep extends BasicStep {
	
	private ResetWizardContext wizardContext;
	
	public ResetData1OptionsStep(UserRequest ureq, ResetWizardContext wizardContext) {
		super(ureq);
		this.wizardContext = wizardContext;
		
		setI18nTitleAndDescr("wizard.general.options", "wizard.general.options");
		updateNextStep(ureq);
	}
	
	void updateNextStep(UserRequest ureq) {
		setNextStep(wizardContext.createNextStep(ureq, ResetDataStep.options));
	}
	
	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		wizardContext.setCurrent(ResetDataStep.options);
		return new PrevNextFinishConfig(false, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl,
			StepsRunContext context, Form form) {
		return new ResetDataOptionsController(ureq, wControl, form, context);
	}
	
	public class ResetDataOptionsController extends StepFormBasicController {
		
		private SingleSelection participantsEl;
		private SingleSelection courseElementsEl;

		public ResetDataOptionsController(UserRequest ureq, WindowControl wControl, Form rootForm,
				StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
			
			initForm(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			setFormInfo("wizard.general.options.description");
			setFormInfoHelp("manual_user/learningresources/Assessment_tool_overview/#course_reset");
			
			String displayname = wizardContext.getDataContext().getRepositoryEntry().getDisplayname();
			uifactory.addStaticTextElement("course.title", StringHelper.escapeHtml(displayname), formLayout);
			
			if(StringHelper.containsNonWhitespace(wizardContext.getDataContext().getRepositoryEntry().getExternalRef())) {
				uifactory.addStaticTextElement("course.external.ref", StringHelper.escapeHtml(wizardContext.getDataContext().getRepositoryEntry().getExternalRef()), formLayout);
			}
			
			SelectionValues courseElementsKV = new SelectionValues();
			courseElementsKV.add(SelectionValues.entry(ResetCourse.all.name(), translate("option.course.element.all.course"),
					translate("option.course.element.all.course.desc"), null, null, true));
			courseElementsKV.add(SelectionValues.entry(ResetCourse.elements.name(), translate("option.course.element.elements"),
					translate("option.course.element.elements.desc"), null, null, true));
			courseElementsEl = uifactory.addCardSingleSelectHorizontal("option.course.element", "option.course.element", formLayout, courseElementsKV);
			courseElementsEl.select(ResetCourse.all.name(), true);
			courseElementsEl.setVisible(wizardContext.isWithCourseNodeSelection());
			
			SelectionValues participantsKV = new SelectionValues();
			participantsKV.add(SelectionValues.entry(ResetParticipants.all.name(), translate("option.participants.all"),
					translate("option.participants.all.desc"), null, null, true));
			participantsKV.add(SelectionValues.entry(ResetParticipants.selected.name(), translate("option.participants.selection"),
					translate("option.participants.selection.desc"), null, null, true));
			participantsEl = uifactory.addCardSingleSelectHorizontal("option.participants", "option.participants", formLayout, participantsKV);
			participantsEl.select(ResetParticipants.all.name(), true);
			participantsEl.setVisible(wizardContext.isWithParticipantsSelection());
		}

		@Override
		protected void formNext(UserRequest ureq) {
			if(courseElementsEl.isVisible() && courseElementsEl.isOneSelected()) {
				ResetCourse resetCourseOption = ResetCourse.valueOf(courseElementsEl.getSelectedKey());
				wizardContext.getDataContext().setResetCourse(resetCourseOption);
			}
			
			if(participantsEl.isVisible() && participantsEl.isOneSelected()) {
				ResetParticipants resetParticipantsOption = ResetParticipants.valueOf(participantsEl.getSelectedKey());
				wizardContext.getDataContext().setResetParticipants(resetParticipantsOption);
			}
			
			if (wizardContext.isRecalculationStep(ResetDataStep.options)) {
				wizardContext.recalculateAvailableSteps();
				updateNextStep(ureq);
				
				fireEvent(ureq, StepsEvent.STEPS_CHANGED);
				fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
			}
		}

		@Override
		protected void formOK(UserRequest ureq) {
			//
		}
	}

}
