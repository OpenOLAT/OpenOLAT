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
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.Submit;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

/**
 * Initial Date:  10.09.2007 <br>
 * @author patrickb
 */
public class GuiDemoFlexiFormCustomlayout extends FormBasicController{

	private TextElement firstName;
	private TextElement lastName;
	private TextElement institution;
	private Submit submit;
	private GuiDemoFlexiFormPersonData personData;
	private VelocityContainer confirm;
	private GuiDemoFlexiForm confirmController;

	public GuiDemoFlexiFormCustomlayout(UserRequest ureq, WindowControl wControl, GuiDemoFlexiFormPersonData data) {
		// this whole class is copy-pasted from the GuiDemoFlexiForm.java
		// but the super constructor is called with a velocity page which serves then
		// as layouting velocity page.
		//
		// inside this page one can render all the form elements with the wellknown
		// $r.render("compName") paradigma. But with the form comes some naming convention
		// to render the label, error, example of a form element.
		// read the instructions in the customformlayout velocity page.
		super(ureq, wControl, "customformlayout");
		// first you may preprocess data to fit into the form items
		// if all preprocessing is done, create the form items
		//
		// example for simple preprocessing - check for NULL
		if(data != null){
			personData = data;
		}else{
			personData = new GuiDemoFlexiFormPersonData();
		}
		//
		// calls our initForm(formlayout,listener,ureq) with default values.
		initForm(ureq);
		//
		// after initialisation you may need to do some stuff
		// but typically initForm(..) is the last call in the constructor.
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
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		/*
		 * create a form with a title and 4 input fields to enter some persons data
		 */
		setFormTitle("guidemo_flexi_form_customlayout");
		
		final boolean inputMode = !personData.isReadOnly();
		
		firstName = uifactory.addTextElement("firstname", "guidemo.flexi.form.firstname", 256, personData.getFirstName(), formLayout);
		firstName.setNotEmptyCheck("guidemo.flexi.form.mustbefilled");
		firstName.setMandatory(true);
		firstName.setEnabled(inputMode);
		
		lastName = uifactory.addTextElement("lastname", "guidemo.flexi.form.lastname", 256, personData.getLastName(), formLayout);
		lastName.setNotEmptyCheck("guidemo.flexi.form.mustbefilled");
		lastName.setMandatory(true);
		lastName.setEnabled(inputMode);
		
		institution = uifactory.addTextElement("institution","guidemo.flexi.form.institution", 256,personData.getInstitution(), formLayout);
		institution.setEnabled(inputMode);
		
		if(inputMode){
			//submit only if in input mode
			submit = new FormSubmit("submit","submit");
			formLayout.add(submit);
		}
	}

}
