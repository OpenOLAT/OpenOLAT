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
* <p>
*/
package ch.goodsolutions.demoextension.controller;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

/**
 * Description:<br>
 * TODO: matthai Class Description for SampleFlexiForm
 * 
 * <P>
 * Initial Date:  Nov 24, 2009 <br>
 * @author matthai
 */
class SampleFlexiForm extends FormBasicController {

	private TextElement lastName;
	private TextElement firstName;

	public SampleFlexiForm(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		//do data preparation stuff here, which is then used for the elements in the init method.
		
		initForm(ureq); //as the last thing always call this init method
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formOK(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected boolean validateFormLogic (UserRequest ureq) {
		//only text fields, not much to validate
		return true;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer, org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		//Formular title and description i18nified.
		setFormTitle("form.title");
		setFormDescription("form.descr");
		
		// add a few TextElement fields to the form layout
		lastName = uifactory.addTextElement("lastname", "search.form.last", 256, "", formLayout);
		lastName.setNotEmptyCheck("error.lastname.mandatory");
		lastName.setMandatory(true);
		
		firstName = uifactory.addTextElement("firstname", "search.form.first", 256, "", formLayout);
		firstName.setNotEmptyCheck("error.firstname.mandatory");
		firstName.setMandatory(true);
		
		//create a new layout to group the buttons of the form
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		formLayout.add(buttonGroupLayout);
		
		//now add the buttons into their new  group layout
		uifactory.addFormSubmitButton("submit", buttonGroupLayout);
		uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		// nothing to do
	}
	
	protected String getLastName(){
		return lastName.getValue();
	}
	
	protected String getFirstName(){
		return firstName.getValue();
	}	
}
