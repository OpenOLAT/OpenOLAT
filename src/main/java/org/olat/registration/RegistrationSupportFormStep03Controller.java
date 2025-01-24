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
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.WebappHelper;

/**
 * Initial date: Nov 29, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RegistrationSupportFormStep03Controller extends StepFormBasicController {

	private final StepsRunContext runContext;
	private final boolean isOrgMismatch;

	public RegistrationSupportFormStep03Controller(UserRequest ureq, WindowControl wControl, Form rootForm,
												   StepsRunContext runContext, boolean isOrgMismatch) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		this.runContext = runContext;
		this.isOrgMismatch = isOrgMismatch;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("step3.reg.support.form.title");
		if (isOrgMismatch) {
			setFormInfo("step3.reg.mismatch.form.text", new String[]{WebappHelper.getMailConfig("mailSupport")});
		} else {
			setFormInfo("step3.reg.support.form.text", new String[]{WebappHelper.getMailConfig("mailSupport")});
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		runContext.put(RegWizardConstants.RECURRINGDETAILS, "done");
		fireEvent(ureq, StepsEvent.INFORM_FINISHED);
	}


}
