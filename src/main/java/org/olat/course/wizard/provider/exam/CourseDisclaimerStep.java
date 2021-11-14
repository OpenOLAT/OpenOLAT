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
package org.olat.course.wizard.provider.exam;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.olat.course.disclaimer.ui.CourseDisclaimerController;
import org.olat.repository.RepositoryEntry;

/**
 * Initial date: 09.02.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class CourseDisclaimerStep extends BasicStep {

	public static final String RUN_CONTEXT_KEY = "courseDisclaimer";
	
	private RepositoryEntry entry;
	
	public static BasicStep create(UserRequest ureq, RepositoryEntry entry, ExamCourseSteps examCourseSteps) {
		if (examCourseSteps.isDisclaimer()) {
			return new CourseDisclaimerStep(ureq, entry, examCourseSteps);
		}
		return new TestSelectionStep(ureq, entry, examCourseSteps);
	}
	
	public CourseDisclaimerStep(UserRequest ureq, RepositoryEntry entry, ExamCourseSteps examCourseSteps) {
		super(ureq);
		
		this.entry = entry; 
		
		setTranslator(Util.createPackageTranslator(CourseDisclaimerController.class, getLocale(), getTranslator()));
		setI18nTitleAndDescr("course.disclaimer.headline", null);
		setNextStep(new TestSelectionStep(ureq, entry, examCourseSteps));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		return new CourseDisclaimerWrapperController(ureq, windowControl, form, stepsRunContext);
	}
	
	private class CourseDisclaimerWrapperController extends StepFormBasicController {
		
		private final CourseDisclaimerController disclaimerController; 

		public CourseDisclaimerWrapperController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);

			disclaimerController = new CourseDisclaimerController(ureq, wControl, rootForm, entry);
			initForm (ureq);
		}

		@Override
		protected void formNext(UserRequest ureq) {
			addToRunContext(RUN_CONTEXT_KEY, disclaimerController.getSummary());
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			//do nothing
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			formLayout.add("courseDisclaimer", disclaimerController.getInitialFormItem());
		}

		@Override
		public void dispose() {
			// prevent dispose because rich text
		}
	}
}
