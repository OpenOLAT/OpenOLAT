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
import org.olat.course.wizard.CourseWizardService;
import org.olat.course.wizard.ui.ExamCourseStepsController;
import org.olat.course.wizard.ui.ExamCourseStepsController.ExamCourseStepsListener;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 11 Jan 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ExamCourseSetpsStep extends BasicStep {
	
	private final RepositoryEntry entry;
	private final ExamCourseSteps examCourseSteps;

	public ExamCourseSetpsStep(UserRequest ureq, RepositoryEntry entry, ExamCourseSteps examCourseSteps) {
		super(ureq);
		this.entry = entry;
		this.examCourseSteps = examCourseSteps;
		setTranslator(Util.createPackageTranslator(CourseWizardService.class, getLocale(), getTranslator()));
		setI18nTitleAndDescr("wizard.title.exam.steps", null);
		setNextStep(new InfoMetadataStep(ureq, entry, examCourseSteps));
	}
	
	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return PrevNextFinishConfig.NEXT;
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl,
			StepsRunContext stepsRunContext, Form form) {
		return new ExamCourseStepsController(ureq, windowControl, form, stepsRunContext, examCourseSteps,
				new ExamCourseStepsistenerImpl());
	}
	
	private final class ExamCourseStepsistenerImpl implements ExamCourseStepsListener {
		
		@Override
		public void onStepsChanged(UserRequest ureq) {
			setNextStep(new InfoMetadataStep(ureq, entry, examCourseSteps));
		}
		
	}

}
