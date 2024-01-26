/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.assessment.ui.inspection;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

/**
 * 
 * Initial date: 15 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CreateInspection_4_NotificationsStep extends BasicStep {

	private final CreateInspectionContext context;
	
	public CreateInspection_4_NotificationsStep(UserRequest ureq, CreateInspectionContext context) {
		super(ureq);
		this.context = context;
		
		if(context.getEditedCompensation() != null) {
			if(context.getEditedCompensation() != null) {
				setNextStep(new CreateInspection_5_CompensationStep(ureq, context));
			} else {
				setNextStep(NOSTEP);
			}
			
		} else if(context.getParticipantsCompensations() != null && !context.getParticipantsCompensations().isEmpty()) {
			setNextStep(new CreateInspection_5_CompensationStep(ureq, context));
		} else {
			setNextStep(NOSTEP);
		}

		setI18nTitleAndDescr("wizard.notification.title", "wizard.notification.descr");
	}
	
	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, true, true);
	}
	
	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl,
			StepsRunContext stepsRunContext, Form form) {
		return new NotificationsController(ureq, wControl, context, stepsRunContext, form);
	}
}
