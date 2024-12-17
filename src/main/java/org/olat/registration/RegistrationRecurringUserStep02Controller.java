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

import static org.olat.core.gui.components.util.SelectionValues.entry;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

/**
 * Initial date: Nov 29, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RegistrationRecurringUserStep02Controller extends StepFormBasicController {

	private SingleSelection recurringUserSelection;

	private final RegistrationStepsListener registrationStepsListener;

	public RegistrationRecurringUserStep02Controller(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext,
													 RegistrationStepsListener registrationStepsListener) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		this.registrationStepsListener = registrationStepsListener;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("step3.reg.recurring.title");
		setFormInfo("step3.reg.recurring.text");

		SelectionValues recurringSV = new SelectionValues();
		recurringSV.add(entry("no.first.time", translate("step.3.reg.recurring.selection.no"), null, null, null, true));
		recurringSV.add(entry("yes.not.first.time", translate("step.3.reg.recurring.selection.yes"), null, null, null, true));
		recurringUserSelection = uifactory.addCardSingleSelectHorizontal("step3.reg.recurring.title", null, formLayout, recurringSV);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		return super.validateFormLogic(ureq);
	}

	@Override
	public void back() {
		removeAsListenerAndDispose(this);
	}

	@Override
	protected void formNext(UserRequest ureq) {
		if (recurringUserSelection.isKeySelected("yes.not.first.time")) {
			registrationStepsListener.onStepsChanged(ureq, true);
			fireEvent(ureq, StepsEvent.STEPS_CHANGED);
		} else {
			registrationStepsListener.onStepsChanged(ureq, false);
			fireEvent(ureq, StepsEvent.STEPS_CHANGED);
		}
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
