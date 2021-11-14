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
package org.olat.registration;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormCancel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * 
 * <P>
 * Initial Date: Jul 31, 2009 <br>
 * 
 * @author twuersch
 * @author gnaegi
 */
public class DisclaimerFormController extends FormBasicController {
	
	public  static final String DCL_CHECKBOX_KEY = "dclchkbox";
	public  static final String DCL_CHECKBOX_KEY2 = "dclchkbox2";
	public  static final String DCL_CHECKBOX_KEY3 = "dclchkbox3";
	public  static final String DCL_ACCEPT = "dcl.accept";
	private static final String NLS_DISCLAIMER_ACKNOWLEDGED = "disclaimer.acknowledged";
	private static final String NLS_DISCLAIMER_OK = "disclaimer.ok";
	private static final String NLS_DISCLAIMER_NOK = "disclaimer.nok";
	private static final String ACKNOWLEDGE_CHECKBOX_NAME = "acknowledge_checkbox";
	private static final String ADDITIONAL_CHECKBOX_NAME = "additional_checkbox";
	private static final String ADDITIONAL_CHECKBOX_2_NAME = "additional_checkbox_2";
	
	protected MultipleSelectionElement acceptCheckbox;
	protected MultipleSelectionElement additionalCheckbox;
	protected MultipleSelectionElement additionalCheckbox2;
	private boolean readOnly;

	@Autowired
	private RegistrationModule registrationModule;
	
	public DisclaimerFormController(UserRequest ureq, WindowControl wControl, boolean readOnly) {
		super(ureq, wControl, FormBasicController.LAYOUT_VERTICAL);
		this.readOnly = readOnly;
		initForm(ureq);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void formNOK(UserRequest ureq) {
		fireEvent(ureq, Event.FAILED_EVENT);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// Add the "accept" checkbox to the form.
		acceptCheckbox = uifactory.addCheckboxesVertical(ACKNOWLEDGE_CHECKBOX_NAME, null, formLayout, new String[] {DCL_CHECKBOX_KEY}, new String[] {translate(NLS_DISCLAIMER_ACKNOWLEDGED)}, 1);
		acceptCheckbox.setEscapeHtml(false);
		acceptCheckbox.setMandatory(false);
		acceptCheckbox.select(DCL_CHECKBOX_KEY, readOnly);
		
		// Add the additional checkbox to the form (depending on the configuration)
		if(registrationModule.isDisclaimerAdditionalCheckbox()) {
			String additionalCheckboxText = translate("disclaimer.additionalcheckbox");
			if (additionalCheckboxText != null) {
				additionalCheckbox = uifactory.addCheckboxesVertical(ADDITIONAL_CHECKBOX_NAME, null, formLayout, new String[] {DCL_CHECKBOX_KEY2}, new String[] {additionalCheckboxText}, 1);
				additionalCheckbox.setEscapeHtml(false);
				additionalCheckbox.select(DCL_CHECKBOX_KEY2, readOnly);
			}
			if(registrationModule.isDisclaimerAdditionalCheckbox2()) {
				String additionalCheckbox2Text = translate("disclaimer.additionalcheckbox2");
				if (additionalCheckbox2Text != null) {
					additionalCheckbox2 = uifactory.addCheckboxesVertical(ADDITIONAL_CHECKBOX_2_NAME, null, formLayout, new String[] {DCL_CHECKBOX_KEY3}, new String[] {additionalCheckbox2Text}, 1);
					additionalCheckbox2.setEscapeHtml(false);
					additionalCheckbox2.select(DCL_CHECKBOX_KEY3, readOnly);
				}
			}
		}
				
		if (readOnly) {
			// Disable when set to read only
			formLayout.setEnabled(!readOnly);
		} else {
			// Create submit and cancel buttons
			final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
			formLayout.add(buttonLayout);
			buttonLayout.setElementCssClass("o_sel_disclaimer_buttons");

			FormCancel cancelButton = uifactory.addFormCancelButton(NLS_DISCLAIMER_NOK, buttonLayout, ureq, getWindowControl());	
			cancelButton.setI18nKey(NLS_DISCLAIMER_NOK);
			uifactory.addFormSubmitButton(DCL_ACCEPT, NLS_DISCLAIMER_OK, buttonLayout);
		}
	}
}
