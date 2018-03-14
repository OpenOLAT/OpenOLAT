/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.gui.demo.guidemo;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.rules.RulesFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

/**
 * Description:<br>
 * This controller is responsible for an advanced FlexiForm including different
 * elements, layouts and show/hide rules. The form data isn't processed at all.
 * However, the formOK and formInnerEvent methods are used.
 * 
 * <P>
 * Initial Date: Jan 21, 2009 <br>
 * 
 * @author Gregor Wassmann, frentix GmbH, http://www.frentix.com
 */
public class GuiDemoFlexiFormAdvancedController extends FormBasicController {

	private SingleSelection horizontalRadioButtons;
	private SingleSelection verticalRadioButtons;
	// Usually, the keys are i18n keys and the options correspond to their
	// translated values. To avoid unnecessary translation these dummy values are
	// defined right here.
	private final static String[] keys = new String[] { "a", "b", "c" };
	private final static String[] options = new String[] { "A", "B", "C" };
	// For yes and no we'll use i18n keys and translate the values
	private final static String[] yesOrNoKeys = new String[] { "advanced_form.yes", "advanced_form.no" };

	private RichTextElement richTextElement, disabledRichTextElement;
	private MultipleSelectionElement addCheckboxesVertical;
	private MultipleSelectionElement checkboxesDropdown;
	
	public GuiDemoFlexiFormAdvancedController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#doDispose()
	 */
	@Override
	protected void doDispose() {
	// nothing to dispose
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formOK(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formOK(UserRequest ureq) {

		addCheckboxesVertical.getSelectedKeys().forEach(System.out::println);
//		showInfo("advanced_form.successfully_submitted", file.getUploadFileName());
		// add your code here:
		// file.getUploadInputStream() ...
		// verticalRadioButtons.getSelectedKey() ...
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("guidemo_flexi_form_advanced");
		setFormDescription("advanced_form.description");
		setFormContextHelp("Introduction");

		// Mandatory and read-only text fields		
		this.addTextFields(formLayout);

		// More form items: Date, link and file selector
		this.addDateLinkAndFileItems(formLayout);

		// Separator with line
		uifactory.addSpacerElement("spacer", formLayout, false);

		// Single and multible selections (radio buttons and checkboxes)
		this.addSelections(formLayout);
		
		// Separator without line
		uifactory.addSpacerElement("spacernoline", formLayout, true);

		// Sublayout (shown if no is selected)
		this.addSublayout(formLayout);

		// Here's a text area
		uifactory.addTextAreaElement("guidemo.form.textarea", 0, 2, null, formLayout);

		// Add some rich text elements
		richTextElement = uifactory.addRichTextElementForStringData("guidemo.form.richtext.simple", "guidemo.form.richtext.simple", "click <i>to</i> <b>edit</b>. This one has an event listener and an <b>external menu with auto hide</b>", -1, -1, false, null, null, formLayout, ureq.getUserSession(), getWindowControl());
		//richTextElement.addActionListener(FormEvent.ONCHANGE);

		uifactory.addRichTextElementForStringData("guidemo.form.richtext.simple2", null, "one <i>with</i> <b>height</b> and <span style='color:red'>no</span> event listener and an <b>internal</b> menu", 10, 40, true, null, null, formLayout, ureq.getUserSession(), getWindowControl());

		disabledRichTextElement = uifactory.addRichTextElementForStringData("guidemo.form.richtext.simple3", "guidemo.form.richtext.simple", "this <i>is</i> <b>disabled</b>", -1, -1, false, null, null, formLayout, ureq.getUserSession(), getWindowControl());
		disabledRichTextElement.setEnabled(false);

		// Button layout
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);

		// Submit and cancel buttons (without effect)
		uifactory.addFormSubmitButton("advanced_form.submit", buttonLayout);
		uifactory.addFormLink("advanced_form.cancel", buttonLayout, Link.BUTTON);
	}

	/**
	 * Adds a mandatory and a read-only text field to the provided form.
	 * 
	 * @param formItemsFactory
	 * @param form
	 */
	private void addTextFields(FormItemContainer form) {
		// Mandatory text field
		final TextElement textField = uifactory.addTextElement("textField", "guidemo.form.text", 256, "", form);
		textField.setMandatory(true);
		textField.setNotEmptyCheck("guidemo.form.error.notempty");
		textField.setExampleKey("advanced_form.text_field.example", null);

		// Read-only text field
		final TextElement readOnly = uifactory.addTextElement("readOnly", "guidemo.form.readonly", 256, "forever", form);
		readOnly.setEnabled(false);
	}

	/**
	 * Adds a date chooser, a link and a file chooser to the given form.
	 * 
	 * @param formItemsFactory
	 * @param form
	 */
	private void addDateLinkAndFileItems(FormItemContainer form) {
		// Date picker
		uifactory.addDateChooser("dateChooser", "guidemo.form.date", null, form);

		// Link
		final FormLink link = uifactory.addFormLink("link", form);
		link.setLabel("guidemo.form.link", null);

		// File Chooser
		// There is a multipart parameter problem with that element.
		uifactory.addFileElement(getWindowControl(), "file", "advanced_form.file", form);
	}

	/**
	 * Adds different selection elements to the given form. Like radio buttons and
	 * checkboxes.
	 * 
	 * @param formItemsFactory
	 * @param form
	 */
	private void addSelections(FormItemContainer form) {
		// drop-down list
		uifactory.addDropdownSingleselect("guidemo.form.pulldown", form, keys, options, null);

		// vertical radio buttons
		verticalRadioButtons = uifactory.addRadiosVertical("guidemo.form.radio1", form, keys, options);
		// As an example on how to use the formInnerEvent method we'll catch events
		// on these radio buttons and therefore need to add the current controller
		// to their listeners.
		verticalRadioButtons.addActionListener(FormEvent.ONCLICK);

		addCheckboxesVertical = uifactory.addCheckboxesVertical("checkboxes", "advanced_form.checkboxes", form, keys, options, 1);
		addCheckboxesVertical.select(keys[0], true);
		addCheckboxesVertical.addActionListener(FormEvent.ONCHANGE);

		// Translate the keys to the yes and no option values
		final String[] yesOrNoOptions = new String[yesOrNoKeys.length];
		for (int i = 0; i < yesOrNoKeys.length; i++) {
			yesOrNoOptions[i] = translate(yesOrNoKeys[i]);
		}
	
		// Drop down checkboxes 
		String[] keys = new String[] { "phone", "clock", "book", "letter" };
		String[] values = new String[] {
				getTranslator().translate("guidemo.cbdropdown.phone"), 
				getTranslator().translate("guidemo.cbdropdown.clock"),
				getTranslator().translate("guidemo.cbdropdown.book"),
				getTranslator().translate("guidemo.cbdropdown.letter")};
		String[] cssClasses = new String[] {"", "", "", "o_userbulk_changedcell"};
		String[] iconLeftCSS = new String[] {
				"o_icon o_icon-fw o_icon_phone",
				"o_icon o_icon-fw o_icon_time",
				"o_icon o_icon-fw o_icon_lecture",
				"o_icon o_icon-fw o_icon_mail"};
		checkboxesDropdown = uifactory.addCheckboxesDropdown("dropdown", "advanced_form.cbdropdown", form, keys, values,
				cssClasses, iconLeftCSS);
		checkboxesDropdown.setNonSelectedText(getTranslator().translate("guidemo.cbdropdown.non.selected"));
		checkboxesDropdown.select(keys[0], true);
		checkboxesDropdown.setEnabled("book", false);
		checkboxesDropdown.addActionListener(FormEvent.ONCLICK);
//		checkboxesDropdown.setEnabled(false);
//		checkboxesDropdown.setAjaxOnly(true);

		// Horizontal radio buttons. Choice between Yes or No.
		horizontalRadioButtons = uifactory.addRadiosHorizontal("guidemo.form.radio2", form, yesOrNoKeys,
				yesOrNoOptions);
		// A default value is needed for show/hide rules
		horizontalRadioButtons.select(yesOrNoKeys[0], true);
		horizontalRadioButtons.addActionListener(FormEvent.ONCLICK); // Radios/Checkboxes need onclick because of IE bug OLAT-5753
	}

	/**
	 * Adds a sublayout to the form. If the user selects no from the vertical
	 * radio buttons, the sublayout shows up and the user is asked to provide more
	 * information.
	 * 
	 * @param formItemsFactory
	 * @param form
	 */
	private void addSublayout(FormItemContainer form) {
		// Default Sublayout
		final FormLayoutContainer subLayout = FormLayoutContainer.createDefaultFormLayout("why_not_form", getTranslator());
		// This doesn't work for some reason
		// subLayout.setVisible(false);
		form.add(subLayout);

		// Add a text element
		uifactory.addTextElement("why_not", "advanced_form.why_not?", 512, null, subLayout);

		// Let's try some show/hide rules. If 'No' is selected, the sublayout shows
		// up. Note: It's important to set the horizontal radio buttons to a default
		// value.
		RulesFactory.createHideRule(horizontalRadioButtons, yesOrNoKeys[0], subLayout, form);
		RulesFactory.createShowRule(horizontalRadioButtons, yesOrNoKeys[1], subLayout, form);
	}

	/**
	 * Called if an element inside the form triggers an event. In this particular
	 * case clicks on the vertical radio buttons are caught and the user gets
	 * notified by his choice.
	 * 
	 * @param ureq
	 * @param source
	 * @param event
	 */
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == verticalRadioButtons) {
			if (event.wasTriggerdBy(FormEvent.ONCLICK)) {
				int selectedIndex = verticalRadioButtons.getSelected();
				String selection = options[selectedIndex];
				showInfo("advanced_form.your_selection_is", selection);
			}
		} else if (source == richTextElement) {
			getWindowControl().setInfo("Wow, you just changed the html editor area. The new content is now: " + richTextElement.getValue());
		}
	}
}
