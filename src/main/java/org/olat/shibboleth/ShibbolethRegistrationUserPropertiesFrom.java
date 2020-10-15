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
package org.olat.shibboleth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.registration.RegistrationForm2;
import org.olat.shibboleth.manager.ShibbolethAttributes;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 09.08.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ShibbolethRegistrationUserPropertiesFrom extends FormBasicController {

	public static final String USERPROPERTIES_FORM_IDENTIFIER = ShibbolethRegistrationUserPropertiesFrom.class.getCanonicalName();

	private final ShibbolethAttributes shibbolethAttributes;
	private final List<UserPropertyHandler> userPropertyHandlers;

	private Map<String,FormItem> propFormItems = new HashMap<>();

	@Autowired
	private UserManager userManager;

	public ShibbolethRegistrationUserPropertiesFrom(UserRequest ureq, WindowControl wControl, ShibbolethAttributes shibbolethAttributes) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RegistrationForm2.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(UserPropertyHandler.class, getLocale(), getTranslator()));
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USERPROPERTIES_FORM_IDENTIFIER, false);

		this.shibbolethAttributes = shibbolethAttributes;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener,	UserRequest ureq) {
		// Add all available user fields to this form
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler != null) {
				FormItem fi = userPropertyHandler.addFormItem(getLocale(), null, USERPROPERTIES_FORM_IDENTIFIER, false, formLayout);
				propFormItems.put(userPropertyHandler.getName(), fi);
				if(fi instanceof TextElement) {
					String value = shibbolethAttributes.getValueForUserPropertyName( userPropertyHandler.getName());
					if(StringHelper.containsNonWhitespace(value)) {
						TextElement formElement = (TextElement)fi;
						formElement.setValue(value);
						formElement.setEnabled(false);
					}
				}
			}
		}

		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		// validate each user field
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			FormItem fi = propFormItems.get(userPropertyHandler.getName());
			if (!userPropertyHandler.isValid(null, fi, null)) {
				allOk &= false;
			}
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			updateShibboletAttribute(userPropertyHandler);
		}

		fireEvent (ureq, Event.DONE_EVENT);
	}

	private void updateShibboletAttribute(UserPropertyHandler userPropertyHandler) {
		String propertyName = userPropertyHandler.getName();
		FormItem propertyItem = this.flc.getFormComponent(propertyName);
		String propertyValue = userPropertyHandler.getStringValue(propertyItem);
		shibbolethAttributes.setValueForUserPropertyName(propertyName, propertyValue);
	}

	@Override
	protected void doDispose() {
		//
	}
}
