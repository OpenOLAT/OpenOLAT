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
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * Initial date: 17 oct. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GuiDemoFlexiFormValidating extends FormBasicController {

	private TextElement firstNameEl;
	private TextElement lastNameEl;
	private TextElement inlineEditEl;
	private RichTextElement descriptionEl;
	private DateChooser birthdayEl;
	private SingleSelection colorRadioEl;
	private SingleSelection colorDropdownEl;
	private MultipleSelectionElement colorMulticheckEl;

	public GuiDemoFlexiFormValidating(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}
	

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("guidemo_flexi_form_simpleform");
		setInlineValidationOn(true);

		firstNameEl = uifactory.addTextElement("firstname", "guidemo.flexi.form.firstname", 256, "", formLayout);
		firstNameEl.setNotEmptyCheck();
		firstNameEl.setMandatory(true);
		firstNameEl.setPlaceholderText("Hans");
		firstNameEl.setHelpText("If you have a middle name, add it to the first name input field");
		firstNameEl.setHelpUrlForManualPage("manual_user/personal/Configuration/#profile");

		lastNameEl = uifactory.addTextElement("lastname", "guidemo.flexi.form.lastname", 256, "", formLayout);
		lastNameEl.setNotEmptyCheck();
		lastNameEl.setPlaceholderText("Muster");

		
		SelectionValues colors = new SelectionValues();
		colors.add(SelectionValues.entry("yellow", "Yellow"));
		colors.add(SelectionValues.entry("green", "Green"));
		colors.add(SelectionValues.entry("purple", "Purple"));
		colorDropdownEl = uifactory.addDropdownSingleselect("inlined.preferred.color", formLayout, colors.keys(), colors.values());
		
		colorMulticheckEl = uifactory.addCheckboxesVertical("inlined.preferred.color.multi", formLayout, colors.keys(), colors.values(), 1);
		
		inlineEditEl = uifactory.addInlineTextElement("inlined.inline.text", "Inline", formLayout, this);
		inlineEditEl.setLabel("inlined.inline.text", null);
		
		birthdayEl = uifactory.addDateChooser("birthday", "birthday", null, formLayout);
		birthdayEl.setNotEmptyCheck("guidemo.flexi.form.mustbefilled");
		birthdayEl.setDateChooserTimeEnabled(true);

		descriptionEl = uifactory.addRichTextElementForStringDataCompact("description", "description", "<p>Hello</p>",
				6, 60, null, formLayout, ureq.getUserSession(), getWindowControl());
		descriptionEl.setNotEmptyCheck("guidemo.flexi.form.mustbefilled");
		
		colorRadioEl = uifactory.addRadiosVertical("inlined.preferred.color.radio", formLayout, colors.keys(), colors.values());
		
		FormItemContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}	

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected boolean validateFormItem(UserRequest ureq, FormItem item) {
		boolean ok = super.validateFormItem(ureq, item);
		if(item == firstNameEl && "hans".equalsIgnoreCase(firstNameEl.getValue())) {
			firstNameEl.setWarningKey("guidemo.flexi.form.warning");
		}
		
		if(item == colorDropdownEl) {
			if(colorDropdownEl.isOneSelected() && "purple".equalsIgnoreCase(colorDropdownEl.getSelectedKey())) {
				colorDropdownEl.setWarningKey("guidemo.flexi.form.warning");
			} else {
				colorDropdownEl.clearWarning();
			}
		}
		
		if(item == colorMulticheckEl) {
			if(colorMulticheckEl.getSelectedKeys().contains("purple")) {
				colorMulticheckEl.setWarningKey("guidemo.flexi.form.warning");
			} else {
				colorDropdownEl.clearWarning();
			}
		}
		
		if(item == colorRadioEl) {
			if(colorRadioEl.isOneSelected() && colorRadioEl.getSelectedKey().contains("purple")) {
				colorRadioEl.setWarningKey("guidemo.flexi.form.warning");
			} else {
				colorRadioEl.clearWarning();
			}
		}
		
		if(item == birthdayEl) {
			birthdayEl.setWarningKey("guidemo.flexi.form.warning");
		}
		
		if(item == descriptionEl) {
			if(descriptionEl.getValue().toLowerCase().contains("hello")) {
				descriptionEl.setWarningKey("guidemo.flexi.form.warning.hello");
			} else {
				descriptionEl.clearWarning();
			}
		}
		
		return ok;
	}
	
}
