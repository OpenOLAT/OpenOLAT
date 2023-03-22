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
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;

/**
 * 
 * Initial date: 8 mars 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ResetData4ConfirmationStep extends BasicStep {

	private final ResetDataContext dataContext;
	private final AssessmentToolSecurityCallback secCallback;
	
	public ResetData4ConfirmationStep(UserRequest ureq, ResetDataContext dataContext,
			AssessmentToolSecurityCallback secCallback) {
		super(ureq);
		this.dataContext = dataContext;
		this.secCallback = secCallback;
		
		setNextStep(Step.NOSTEP);
		setI18nTitleAndDescr("wizard.confirmation", "wizard.confirmation");
	}
	
	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, false, true);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl,
			StepsRunContext context, Form form) {
		return new ResetDataConfirmationController(ureq, wControl, form, context);
	}
	
	public class ResetDataConfirmationController extends StepFormBasicController {
		
		private final ConfirmResetDataController confirmationCtrl;
		
		public ResetDataConfirmationController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_BAREBONE, null);
			
			confirmationCtrl = new ConfirmResetDataController(ureq, wControl, rootForm, dataContext, secCallback);
			listenTo(confirmationCtrl);
			
			initForm(ureq);
		}
		
		@Override
		protected void doDispose() {
			removeControllerListener(confirmationCtrl);
			flc.getRootForm().removeSubFormListener(confirmationCtrl);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			formLayout.add(confirmationCtrl.getInitialFormItem());
		}

		@Override
		protected boolean validateFormLogic(UserRequest ureq) {
			return confirmationCtrl.validateFormLogic(ureq);
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
