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
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
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
public class ParticipantsSelectionStep extends BasicStep {
	
	public static BasicStep create(UserRequest ureq, ExamCourseSteps examCourseSteps) {
		if (examCourseSteps.isParticipants()) {
			return new ParticipantsSelectionStep(ureq);
		}
		return new PublicationStep(ureq);
	}
	
	private ParticipantsSelectionStep(UserRequest ureq) {
		super(ureq);
		setTranslator(Util.createPackageTranslator(CourseWizardService.class, getLocale(), getTranslator()));
		setI18nTitleAndDescr("wizard.title.participants.selection", null);
		setNextStep(new ParticipantsOverviewStep(ureq));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return PrevNextFinishConfig.BACK_NEXT;
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl,
			StepsRunContext stepsRunContext, Form form) {
		String formTitle = getTranslator().translate("wizard.title.participants.selection");
		return new ImportMemberByUsernamesController(ureq, windowControl, form, stepsRunContext,
				CourseWizardCallback.RUN_CONTEXT_PARTICIPANTS, formTitle);
	}

}
