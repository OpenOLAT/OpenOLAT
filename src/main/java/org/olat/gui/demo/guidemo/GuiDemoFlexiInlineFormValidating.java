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
package org.olat.gui.demo.guidemo;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormErrorsGroupItem;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * Initial date: 17 oct. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GuiDemoFlexiInlineFormValidating extends FormBasicController {

	private TextElement firstNameEl;
	private TextElement lastNameEl;
	private FormErrorsGroupItem errorsEl;

	public GuiDemoFlexiInlineFormValidating(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("guidemo_flexi_form_simpleform");
		setInlineValidationOn(true);
		
		String page = velocity_root + "/guidemo-inline.html";
		FormLayoutContainer inlineContainer = FormLayoutContainer.createCustomFormLayout("inline-validating", getTranslator(), page);
		inlineContainer.setLabel("inlined.content", null);
		inlineContainer.showLabel(true);
		formLayout.add(inlineContainer);
		
		errorsEl = new FormErrorsGroupItem("errors", inlineContainer);
		inlineContainer.add("errors", errorsEl);

		firstNameEl = uifactory.addTextElement("firstname", "guidemo.flexi.form.firstname", 256, "", inlineContainer);
		firstNameEl.setNotEmptyCheck();
		firstNameEl.getComponent().setSpanAsDomReplaceable(true);
		firstNameEl.setPlaceholderText("Hans");

		lastNameEl = uifactory.addTextElement("lastname", "guidemo.flexi.form.lastname", 256, "", inlineContainer);
		lastNameEl.setNotEmptyCheck();
		lastNameEl.getComponent().setSpanAsDomReplaceable(true);
		lastNameEl.setPlaceholderText("Muster");
		
		FormItemContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	@Override
	protected boolean validateFormItem(UserRequest ureq, FormItem item) {
		boolean ok = super.validateFormItem(ureq, item);
		if(item == firstNameEl) {
			if(!item.hasError() && "hans".equalsIgnoreCase(firstNameEl.getValue())) {
				firstNameEl.setWarningKey("guidemo.flexi.form.warning");
			} else {
				firstNameEl.clearWarning();
			}
		}
		return ok;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
