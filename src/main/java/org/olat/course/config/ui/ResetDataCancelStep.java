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
package org.olat.course.config.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.course.assessment.ui.reset.ResetWizardContext;
import org.olat.course.assessment.ui.reset.ResetWizardContext.ResetDataStep;
import org.olat.modules.assessment.AssessmentService;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 Jun 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ResetDataCancelStep extends BasicStep {
	
	public static final String KEY_APPLY = "apply";
	
	private PrevNextFinishConfig prevNextFinishConfig = PrevNextFinishConfig.NEXT;
	private ResetWizardContext wizardContext;
	
	public ResetDataCancelStep(UserRequest ureq, ResetWizardContext wizardContext) {
		super(ureq);
		this.wizardContext = wizardContext;
		
		setI18nTitleAndDescr("assessment.reset.changes.step", "assessment.reset.changes.step");
		updateNextStep(ureq);
	}
	
	void updateNextStep(UserRequest ureq) {
		setNextStep(wizardContext.createNextStep(ureq, ResetDataStep.courseElements));
	}
	
	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return prevNextFinishConfig;
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl,
			StepsRunContext context, Form form) {
		return new ResetDataCancelController(ureq, wControl, form, context);
	}
	
	public class ResetDataCancelController extends StepFormBasicController {
		
		private SingleSelection changesEl;
		
		private final StepsRunContext runContext;
		private final Long assessmentEntriesCount;
		
		@Autowired
		private AssessmentService assessmentService;

		public ResetDataCancelController(UserRequest ureq, WindowControl wControl, Form rootForm,
				StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
			this.runContext = runContext;
			
			RepositoryEntry courseEntry = wizardContext.getDataContext().getRepositoryEntry();
			String rootNodeIdent = wizardContext.getCoachCourseEnv().getCourseEnvironment().getRunStructure().getRootNode().getIdent();
			assessmentEntriesCount = assessmentService.getAssessmentEntriesCount(courseEntry, rootNodeIdent);
			
			initForm(ureq);
			updateUI(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			setFormWarning("assessment.reset.desc", new String[] { String.valueOf(assessmentEntriesCount) });

			FormLayoutContainer optionsCont = FormLayoutContainer.createDefaultFormLayout("options", getTranslator());
			optionsCont.setRootForm(mainForm);
			formLayout.add(optionsCont);

			SelectionValues changesKV = new SelectionValues();
			changesKV.add(SelectionValues.entry(KEY_APPLY, translate("assessment.reset.changes.apply"),
					translate("assessment.reset.changes.apply.desc"), null, null, true));
			changesKV.add(SelectionValues.entry("key.discard", translate("assessment.reset.changes.discard"),
					translate("assessment.reset.changes.discard.desc"), null, null, true));
			changesEl = uifactory.addCardSingleSelectHorizontal("changes", "assessment.reset.changes", optionsCont,
					changesKV);
			changesEl.select(KEY_APPLY, true);
			changesEl.addActionListener(FormEvent.ONCHANGE);
		}

		private void updateUI(UserRequest ureq) {
			boolean apply = changesEl.isOneSelected() && changesEl.isKeySelected(KEY_APPLY);
			if (apply) {
				runContext.put(KEY_APPLY, Boolean.TRUE);
				setNextStep(wizardContext.createNextStep(ureq, ResetDataStep.courseElements));
				if (nextStep() == Step.NOSTEP) {
					prevNextFinishConfig = new PrevNextFinishConfig(false, false ,true);
				} else {
					prevNextFinishConfig = PrevNextFinishConfig.NEXT;
				}
			} else {
				runContext.put(KEY_APPLY, Boolean.FALSE);
				prevNextFinishConfig = new PrevNextFinishConfig(false, false ,true);
				setNextStep(Step.NOSTEP);
			}
			fireEvent(ureq, StepsEvent.STEPS_CHANGED);
		}
		
		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if (source == changesEl) {
				updateUI(ureq);
			}
			super.formInnerEvent(ureq, source, event);
		}
		@Override
		protected void formNext(UserRequest ureq) {
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}
		
		@Override
		protected void formFinish(UserRequest ureq) {
			fireEvent(ureq, StepsEvent.INFORM_FINISHED);
		}
		
		@Override
		protected void formOK(UserRequest ureq) {
			//
		}
	}

}
