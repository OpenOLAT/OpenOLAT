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
 * Initial date: Jan 31, 2025
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class PwChangeVSelectionStep01Controller extends StepFormBasicController {

	public static final String PW_CHANGE_VAL_TYPE_MAIL = "pwchangevaltypemail";
	public static final String PW_CHANGE_VAL_TYPE_SMS = "pwchangevaltypesms";
	private final StepsRunContext runContext;

	private SingleSelection validationTypeSelection;


	public PwChangeVSelectionStep01Controller(UserRequest ureq, WindowControl wControl,
											  Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		this.runContext = runContext;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormInfo("step2.pw.selection.text");

		SelectionValues validationSV = new SelectionValues();
		validationSV.add(entry(PW_CHANGE_VAL_TYPE_MAIL, translate("step2.pw.selection.mail"), null, null, null, true));
		validationSV.add(entry(PW_CHANGE_VAL_TYPE_SMS, translate("step2.pw.selection.sms"), null, null, null, true));
		validationTypeSelection = uifactory.addCardSingleSelectHorizontal("step2.pw.selection.title", null, formLayout, validationSV);
	}

	@Override
	protected void formNext(UserRequest ureq) {
		runContext.put(PwChangeWizardConstants.VALTYPE, validationTypeSelection.getSelectedKey());
		fireEvent(ureq, StepsEvent.STEPS_CHANGED);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
