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
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.olat.course.wizard.CourseWizardCallback;
import org.olat.course.wizard.CourseWizardService;
import org.olat.course.wizard.IQTESTCourseNodeContext;
import org.olat.course.wizard.ui.TitleAndEntryController;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.wizard.ui.QTI21Figures;

/**
 * 
 * Initial date: 18 Dec 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RetestSelectionStep extends BasicStep {
	
	public static BasicStep create(UserRequest ureq, RepositoryEntry entry, ExamCourseSteps examCourseSteps) {
		if (examCourseSteps.isRetest()) {
			return new RetestSelectionStep(ureq, entry, examCourseSteps);
		}
		return CertificateStep.create(ureq, entry, examCourseSteps);
	}
	
	private RetestSelectionStep(UserRequest ureq, RepositoryEntry entry, ExamCourseSteps examCourseSteps) {
		super(ureq);
		setTranslator(Util.createPackageTranslator(CourseWizardService.class, getLocale(), getTranslator()));
		setI18nTitleAndDescr("wizard.title.retest.selection", null);
		BasicStepCollection stepCollection = new BasicStepCollection();
		stepCollection.setTitle(getTranslator(), "wizard.title.retest");
		setStepCollection(stepCollection);
		setNextStep(new RetestConfigStep(ureq, entry, examCourseSteps, stepCollection));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return PrevNextFinishConfig.BACK_NEXT;
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl,
			StepsRunContext stepsRunContext, Form form) {
		return new TitleAndEntryController(ureq, windowControl, form, stepsRunContext,
				CourseWizardCallback.RUN_CONTEXT_RETEST, IQTESTCourseNodeContext::new, "wizard.title.retest",
				ImsQTI21Resource.TYPE_NAME, new QTI21Figures());
	}

}
