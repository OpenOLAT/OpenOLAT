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
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;

/**
 * 
 * Initial date: 8 mars 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ResetData2CourseElementsStep extends BasicStep {

	private final ResetDataContext dataContext;

	public ResetData2CourseElementsStep(UserRequest ureq, ResetDataContext dataContext,
			UserCourseEnvironment coachCourseEnv, AssessmentToolSecurityCallback secCallback,
			boolean withParticipantsSelection) {
		super(ureq);
		this.dataContext = dataContext;
		
		if(withParticipantsSelection) {
			setNextStep(new ResetData3ParticipantsStep(ureq, dataContext, coachCourseEnv, secCallback));
		} else {
			setNextStep(new ResetData4ConfirmationStep(ureq, dataContext, secCallback));
		}
		setI18nTitleAndDescr("wizard.select.course.elements", "wizard.select.course.elements");
	}
	
	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl,
			StepsRunContext context, Form form) {
		return new ResetDataCourseElementsSelectionController(ureq, wControl, form, context, dataContext);
	}
}
