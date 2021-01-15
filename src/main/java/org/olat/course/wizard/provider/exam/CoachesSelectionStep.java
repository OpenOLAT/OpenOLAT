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
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.BasicStepCollection;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepCollection;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.member.wizard.ImportMemberByUsernamesController;
import org.olat.course.wizard.CourseWizardCallback;
import org.olat.course.wizard.CourseWizardService;

/**
 * 
 * Initial date: 7 Jan 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CoachesSelectionStep extends BasicStep {
	
	public static BasicStep create(UserRequest ureq, ExamCourseSteps examCourseSteps) {
		Translator translator = Util.createPackageTranslator(CourseWizardService.class, ureq.getLocale());
		BasicStepCollection retestSteps = new BasicStepCollection();
		retestSteps.setTitle(translator, "wizard.title.members");
		if (examCourseSteps.isCoaches()) {
			return new CoachesSelectionStep(ureq, examCourseSteps, retestSteps);
		}
		return ParticipantsSelectionStep.create(ureq, examCourseSteps, retestSteps);
	}
	
	private CoachesSelectionStep(UserRequest ureq, ExamCourseSteps examCourseSteps, StepCollection stepCollection) {
		super(ureq);
		setTranslator(Util.createPackageTranslator(CourseWizardService.class, getLocale(), getTranslator()));
		setI18nTitleAndDescr("wizard.title.coaches.selection", null);
		setStepCollection(stepCollection);
		setNextStep(new CoachesOverviewStep(ureq, examCourseSteps, stepCollection));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return PrevNextFinishConfig.BACK_NEXT;
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl,
			StepsRunContext stepsRunContext, Form form) {
		String formTitle = getTranslator().translate("wizard.title.coaches.selection");
		return new ImportMemberByUsernamesController(ureq, wControl, form, stepsRunContext,
				CourseWizardCallback.RUN_CONTEXT_COACHES, formTitle);
	}

}
