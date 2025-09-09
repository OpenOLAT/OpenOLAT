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
public class RegistrationMailStep03 extends BasicStep {

	private final Invitation invitation;
	private final boolean isAdditionalRegistrationFormEnabled;

	public RegistrationMailStep03(UserRequest ureq, boolean isAdditionalRegistrationFormEnabled, Invitation invitation) {
		super(ureq);
		this.invitation = invitation;
		this.isAdditionalRegistrationFormEnabled = isAdditionalRegistrationFormEnabled;

		setI18nTitleAndDescr("admin.enable.email.validation", "step1.reg.text");
		// assuming the domain is allowed, so initially the user gets shown personalData as next step
		updateNextStep(ureq, true);
	}

	private void updateNextStep(UserRequest ureq, boolean isDomainAllowed) {
		if (isDomainAllowed) {
			setNextStep(new RegistrationPersonalDataStep04(ureq, invitation, isAdditionalRegistrationFormEnabled));
		} else {
			setNextStep(new RegistrationSupportFormStep03(ureq, true));
		}
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		return new RegistrationMailStep03Controller(ureq, wControl, form, invitation, runContext, this::updateNextStep);
	}
}
