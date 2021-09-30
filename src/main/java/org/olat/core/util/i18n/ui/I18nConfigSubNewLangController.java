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
package org.olat.core.util.i18n.ui;

import java.util.Locale;

import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.logging.AssertException;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * This form allows the user to enter a new language key and create the language
 * immediately. The form implements the user interface and calls the necessary
 * manager code to create the language.
 * 
 * <h3>Events fired by this controller</h3>
 * <ul>
 * <li>Event.CANCELLED_EVENT</li>
 * </ul>
 * When the user presses the save button, the system language preference of this
 * user will be set to the new user and he will be logged out immediately.
 * <P>
 * Initial Date: 27.11.2008 <br>
 * 
 * @author gnaegi
 */
class I18nConfigSubNewLangController extends FormBasicController {
	private TextElement newLanguage, newCountry, newVariant, newTranslatedInEnglish, newTranslatedInLanguage, newTranslator;
	private FormLink cancelButton;
	
	@Autowired
	private I18nModule i18nModule;

	/**
	 * Constructor for the new-language workflow
	 * 
	 * @param ureq
	 * @param control
	 */
	protected I18nConfigSubNewLangController(UserRequest ureq, WindowControl control) {
		super(ureq, control);
		if (!i18nModule.isTransToolEnabled()) {
			throw new AssertException("New languages can only be created when the translation tool is enabled and the translation tool source pathes are configured in the olat.properties");
		}
		initForm(ureq);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// New language elements:
		// A title, displayed in fieldset
		String[] args = new String[] { "<a href='http://www.ics.uci.edu/pub/ietf/http/related/iso639.txt' target='_blank'><i class='o_icon o_icon_link_extern'> </i> ISO639</a>",
				"<a href='http://www.chemie.fu-berlin.de/diverse/doc/ISO_3166.html' target='_blank'><i class='o_icon o_icon_link_extern'> </i> ISO3166</a>" };
		setFormDescription("configuration.management.create.description", args);
		//
		// a) the language code
		newLanguage = uifactory.addTextElement("configuration.management.create.language", "configuration.management.create.language", 2, "",
				formLayout);
		newLanguage.setExampleKey("configuration.management.create.language.example", null);
		newLanguage.setMandatory(true);
		newLanguage.setRegexMatchCheck("[a-z]{2}", "configuration.management.create.language.error");
		newLanguage.setDisplaySize(2);
		newLanguage.addActionListener(FormEvent.ONCHANGE);
		// b) the country code
		newCountry = uifactory.addTextElement("configuration.management.create.country", "configuration.management.create.country", 2, "",
				formLayout);
		newCountry.setExampleKey("configuration.management.create.country.example", null);
		newCountry.setRegexMatchCheck("[A-Z]{0,2}", "configuration.management.create.country.error");
		newCountry.addActionListener(FormEvent.ONCHANGE);
		newCountry.setDisplaySize(2);
		// c) the variant, only available when country code is filled out
		newVariant = uifactory.addTextElement("configuration.management.create.variant", "configuration.management.create.variant", 50, "",
				formLayout);
		newVariant.setExampleKey("configuration.management.create.variant.example", null);
		newVariant.setRegexMatchCheck("[A-Za-z0-9_]*", "configuration.management.create.variant.error");
		newVariant.setDisplaySize(10);
		newVariant.setVisible(false);
		// Language name and translator data
		newTranslatedInEnglish = uifactory.addTextElement("configuration.management.create.inEnglish", "configuration.management.create.inEnglish", 255,
				"", formLayout);
		newTranslatedInEnglish.setExampleKey("configuration.management.create.inEnglish.example", null);
		newTranslatedInEnglish.setMandatory(true);
		newTranslatedInEnglish.setNotEmptyCheck("configuration.management.create.inEnglish.error");
		newTranslatedInLanguage = uifactory.addTextElement("configuration.management.create.inYourLanguage", "configuration.management.create.inYourLanguage", 255,
				"", formLayout);
		newTranslatedInLanguage.setExampleKey("configuration.management.create.inYourLanguage.example", null);
		newTranslator = uifactory.addTextElement("configuration.management.create.translator", "configuration.management.create.translator", 255,
				"", formLayout);
		newTranslator.setExampleKey("configuration.management.create.translator.example", null);
		// Add warn message
		String warnPage = Util.getPackageVelocityRoot(this.getClass()) + "/i18nConfigurationNewWarnMessage.html";
		FormLayoutContainer logoutWarnMessage = FormLayoutContainer.createCustomFormLayout("logoutWarnMessage", getTranslator(), warnPage);
		formLayout.add(logoutWarnMessage);
		// Add cancel and submit in button group layout
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		formLayout.add(buttonGroupLayout);
		cancelButton = uifactory.addFormLink("cancel", buttonGroupLayout, Link.BUTTON);
		uifactory.addFormSubmitButton("configuration.management.create", buttonGroupLayout);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formOK(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formOK(UserRequest ureq) {
		String localeKey = newLanguage.getValue().trim();
		if (!newCountry.isEmpty()) {
			localeKey += "_" + newCountry.getValue().trim();
			if (!newVariant.isEmpty()) {
				localeKey += "_" + newVariant.getValue().trim();
			}
		}
		String translatedInEnglish = newTranslatedInEnglish.getValue().trim();
		String translatedInLanguage = newTranslatedInLanguage.getValue().trim();
		String authors = newTranslator.getValue().trim();
		// Try creating the language
		boolean success = I18nManager.getInstance().createNewLanguage(localeKey, translatedInEnglish, translatedInLanguage, authors);
		if (success) {
			// wow, everything worked fine
			logAudit("Created new language::" + localeKey);
			// Since 6.2 workflow is that when creating a new language the users
			// language is changed to this new language and the user gets
			// redirect to the login screen. 
			updateUserLocaleAndLogout(ureq, localeKey);
		} else {
			// language does already exist or another error
			showError("configuration.management.create.error.exists", localeKey);
		}
	}

	/**
	 * Helper method to update the language in the user profile of the current
	 * user to the given language and send the user to the page.
	 * 
	 * @param ureq
	 * @param localeKey
	 */
	private void updateUserLocaleAndLogout(UserRequest ureq, String localeKey) {
		Identity identity = ureq.getIdentity();
		User currUser = identity.getUser();
		// direct DB calls have to be made here because the 
		// user manager is not available in the core
		currUser = UserManager.getInstance().loadUserByKey(currUser.getKey());
		currUser.getPreferences().setLanguage(localeKey);
		UserManager.getInstance().updateUser(identity, currUser);
		DispatcherModule.redirectToDefaultDispatcher(ureq.getHttpResp());
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);
		if (source == cancelButton) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		} else if (source == newLanguage) {
			Locale testLoc = new Locale(newLanguage.getValue());
			// Check for valid language
			if (!testLoc.getLanguage().equals(newLanguage.getValue())) {
				newLanguage.setValue(testLoc.getLanguage());
			}
		} else if (source == newCountry) {
			String val = newCountry.getValue();
			if (StringHelper.containsNonWhitespace(val)) {
				// Check for valid language / country
				Locale testLoc = new Locale(newLanguage.getValue(), val);
				if (!testLoc.getLanguage().equals(newLanguage.getValue()) && testLoc.getCountry().equals(val)) {
					newLanguage.setValue(testLoc.getLanguage());
					newCountry.setValue(testLoc.getCountry());
				}
			} else {
				// Reset country and variant
				newCountry.setValue("");
				newVariant.setValue("");
			}
		
			boolean visible = val != null && val.matches(".{2}");
			newVariant.setVisible(visible);
		}
		if (!newVariant.isEmpty() && newCountry.isEmpty()) {
			newCountry.setErrorKey("configuration.management.create.variant.error.noCountry", null);
		}
	}

	@Override
	protected void doDispose() {
		//
	}
}
