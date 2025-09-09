/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.registration;

import org.olat.basesecurity.Invitation;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

/**
 * Initial date: Okt 25, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RegistrationDisclaimerStep01 extends BasicStep {

	public RegistrationDisclaimerStep01(UserRequest ureq, Invitation invitation,
										boolean isEmailValidationEnabled, boolean isAdditionalRegistrationFormEnabled,
										boolean isRecurringRegistrationEnabled) {
		super(ureq);

		setI18nTitleAndDescr("disclaimer.header", "step4.reg.text");
		if (invitation == null && isRecurringRegistrationEnabled) {
			setNextStep(new RegistrationRecurringUserStep02(ureq, isAdditionalRegistrationFormEnabled, isEmailValidationEnabled, invitation));
		} else if (isEmailValidationEnabled) {
			setNextStep(new RegistrationMailStep03(ureq, isAdditionalRegistrationFormEnabled, invitation));
		} else {
			setNextStep(new RegistrationPersonalDataStep04(ureq, invitation, isAdditionalRegistrationFormEnabled));
		}
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		return new RegistrationDisclaimerStep01Controller(ureq, wControl, form, runContext);
	}
}
