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
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.Submit;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

/**
 * <P>
 * Initial Date:  10.09.2007 <br>
 * @author patrickb
 */
public class GuiDemoFlexiFormHideUnhide extends FormBasicController {

	private TextElement firstName;
	private TextElement lastName;
	private TextElement institution;
	private Submit submit;
	private GuiDemoFlexiFormPersonData personData;
	private VelocityContainer confirm;
	private GuiDemoFlexiForm confirmController;
	private MultipleSelectionElement checkbox;

	
	public GuiDemoFlexiFormHideUnhide(UserRequest ureq, WindowControl wControl, GuiDemoFlexiFormPersonData data) {
		super(ureq, wControl);
		// first you may preprocess data to fit into the form items
		// if all preprocessing is done, create the form items
		//
		// example for simple preprocessing - check for NULL
		if(data != null) {
			personData = data;
		} else {
			personData = new GuiDemoFlexiFormPersonData();
		}
		//
		// calls our initForm(formlayout,listener,ureq) with default values.
		initForm(ureq);
		//
		// after initialisation you may need to do some stuff
		// but typically initForm(..) is the last call in the constructor.
		updateVisibility();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// this method is called if the form has validated
		// which means that all form items are filled without error
		// and all complex business rules validated also to true.
		//
		// typically the form values are now read out and persisted
		//
		// in our case, save value to data object and prepare a confirm page
		personData.setFirstName(firstName.getValue());
		personData.setLastName(lastName.getValue());
		personData.setInstitution(institution.getValue());
		personData.setReadOnly(true);
		//show the same form in readonly mode.
		confirmController = new GuiDemoFlexiForm(ureq, getWindowControl(), personData);
		confirm = createVelocityContainer("confirm");
		confirm.put("data", confirmController.getInitialComponent());
		initialPanel.pushContent(confirm);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(checkbox == source) {
			updateVisibility();
		}
		super.formInnerEvent(ureq, source, event);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer, org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("guidemo_flexi_form_hideunhide");

		final boolean inputMode = !personData.isReadOnly();
		/*
		 * hide unhide chooser
		 */		
		checkbox = uifactory.addCheckboxesVertical("checkbox", "guidemo.flexi.form.show", formLayout, new String[] { "ison" }, new String[] { "" }, 1);
		checkbox.select("ison", true);
		// register for on click event to hide/disable other elements
		checkbox.addActionListener(FormEvent.ONCLICK);
		//rule to hide/unhide is defined at the end
		
		firstName = uifactory.addTextElement("firstname", "guidemo.flexi.form.firstname", 256, personData.getFirstName(), formLayout);
		firstName.setNotEmptyCheck("guidemo.flexi.form.mustbefilled");
		firstName.setMandatory(true);
		firstName.setEnabled(inputMode);
		
		lastName = uifactory.addTextElement("firstname", "guidemo.flexi.form.lastname", 256, personData.getLastName(), formLayout);
		lastName.setNotEmptyCheck("guidemo.flexi.form.mustbefilled");
		lastName.setMandatory(true);
		lastName.setEnabled(inputMode);
		
		institution = uifactory.addTextElement("institution", "guidemo.flexi.form.institution", 256, personData.getInstitution(), formLayout);
		institution.setEnabled(inputMode);
		
		if(inputMode) {
			//submit only if in input mode
			submit = new FormSubmit("submit","submit");
			formLayout.add(submit);
		}
	}
	
	private void updateVisibility() {
		boolean visible = checkbox.isAtLeastSelected(1);
		firstName.setVisible(visible);
		lastName.setVisible(visible);
		institution.setVisible(visible);
		submit.setVisible(visible);
	}
}