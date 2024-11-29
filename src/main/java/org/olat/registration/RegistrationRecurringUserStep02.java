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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

/**
 * Initial date: Nov 29, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RegistrationRecurringUserStep02 extends BasicStep {

	private final boolean isAdditionalRegistrationFormEnabled;

	public RegistrationRecurringUserStep02(UserRequest ureq, boolean isAdditionalRegistrationFormEnabled) {
		super(ureq);
		this.isAdditionalRegistrationFormEnabled = isAdditionalRegistrationFormEnabled;

		setI18nTitleAndDescr("admin.enable.recurring.user.title", "step3.reg.recurring.text");
		updateNextStep(ureq, false);
	}

	private void updateNextStep(UserRequest ureq, boolean isRecurringUser) {
		if (isRecurringUser) {
			setNextStep(new RegistrationSupportFormStep03(ureq));
		} else {
			setNextStep(new RegistrationMailStep03(ureq, isAdditionalRegistrationFormEnabled));
		}

	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(false, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext runContext, Form form) {
		return new RegistrationRecurringUserStep02Controller(ureq, windowControl, form, runContext, this::updateNextStep);
	}
}
