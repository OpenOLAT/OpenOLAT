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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.user.ChangePasswordForm;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 déc. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class RegistrationAdditionalPersonalDataController extends FormBasicController {
	public static final String USERPROPERTIES_FORM_IDENTIFIER = RegistrationAdditionalPersonalDataController.class.getCanonicalName();
	
	private final Map<String,FormItem> propFormItems = new HashMap<>();
	private final List<UserPropertyHandler> userPropertyHandlers;

	@Autowired
	private UserManager userManager;

	public RegistrationAdditionalPersonalDataController(UserRequest ureq, WindowControl wControl, Form mainForm) {
		super(ureq, wControl, null, Util.createPackageTranslator(ChangePasswordForm.class, ureq.getLocale()));
		this.mainForm = mainForm;
		flc.setRootForm(mainForm);
		this.mainForm.addSubFormListener(this);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USERPROPERTIES_FORM_IDENTIFIER, false);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		initForm(ureq);
	}
	
	protected List<UserPropertyHandler> getUserProperties() {
		return userPropertyHandlers;
	}

	protected FormItem getPropFormItem(String k) {
		return propFormItems.get(k);
	}

	public Map<String, FormItem> getPropFormItems() {
		return propFormItems;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("step.add.reg.title");

		// Add all available user fields to this form
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;
			FormItem fi = userPropertyHandler.addFormItem(getLocale(), null, USERPROPERTIES_FORM_IDENTIFIER, false, formLayout);
			propFormItems.put(userPropertyHandler.getName(), fi);
		}
	}

	@Override
	protected boolean validateFormLogic (UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		// validate each user field
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			FormItem fi = getPropFormItem(userPropertyHandler.getName());
			if(fi instanceof TextElement textEl && !RegistrationPersonalDataController.validateElement(textEl)) {
				allOk &= false;
			} else if (!userPropertyHandler.isValid(null, fi, null)) {
				allOk &= false;
			}
		}

		return allOk;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}