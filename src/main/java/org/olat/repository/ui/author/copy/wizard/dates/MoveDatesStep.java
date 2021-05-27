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
package org.olat.repository.ui.author.copy.wizard.dates;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.BasicStepCollection;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;
import org.olat.repository.ui.author.copy.wizard.CopyCourseSteps;
import org.olat.repository.ui.author.copy.wizard.CopyCourseStepsStep;
/**
 * Initial date: 21.04.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class MoveDatesStep extends BasicStep {

	public static Step create(UserRequest ureq, CopyCourseSteps steps) {
		if (steps.isMoveDates()) {
			return new MoveDatesStep(ureq, steps);
		} else {
			return GeneralDatesStep.create(ureq, null, steps);
		}
	}
	
	public MoveDatesStep(UserRequest ureq, CopyCourseSteps steps) {
		super(ureq);
		
		setTranslator(Util.createPackageTranslator(CopyCourseStepsStep.class, getLocale(), getTranslator()));
		
		BasicStepCollection dateStepCollection = new BasicStepCollection();
		dateStepCollection.setTitle(getTranslator(), "steps.move.dates.title");
		setStepCollection(dateStepCollection);
		
		setI18nTitleAndDescr("dates.start", null);
		setNextStep(new GeneralDatesStep(ureq, dateStepCollection, steps));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return PrevNextFinishConfig.BACK_NEXT;
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		return new MoveDatesStepController(ureq, windowControl, form, stepsRunContext);
	}
	
	private class MoveDatesStepController extends StepFormBasicController {
		
		private DateChooser newStartDateEl;
		
		private CopyCourseContext context;
		
		public MoveDatesStepController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT_2_10, null);
			
			setTranslator(Util.createPackageTranslator(CopyCourseStepsStep.class, getLocale(), getTranslator()));
			
			context = (CopyCourseContext) runContext.get(CopyCourseContext.CONTEXT_KEY);
			
			initForm(ureq);
		}

		@Override
		protected void doDispose() {
			// Nothing to dispose
		}
		
		@Override
		protected boolean validateFormLogic(UserRequest ureq) {
			boolean allOk = super.validateFormLogic(ureq);
			
			return allOk;
		}

		@Override
		protected void formOK(UserRequest ureq) {
			if (newStartDateEl.getDate() != null) {
				context.setNewStartDate(newStartDateEl.getDate());
				
				long dateDifference = newStartDateEl.getDate().getTime() - context.getRepositoryLifeCycle().getValidFrom().getTime();
				context.setDateDifference(dateDifference);
			}
			
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			newStartDateEl = uifactory.addDateChooser("dates.new.start", context.getNewStartDate(), formLayout);
			
			
			if (context.getNewStartDate() == null) {
				newStartDateEl.setVisible(false);
				setFormDescription("dates.no.start.date");
			} else {
				setFormDescription("dates.start.date.exists");
			}
		}
		
	}

}
