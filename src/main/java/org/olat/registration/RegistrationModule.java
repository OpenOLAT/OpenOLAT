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

package org.olat.registration;

import org.olat.core.configuration.AbstractOLATModule;
import org.olat.core.configuration.PersistedProperties;
import org.olat.core.util.mail.EmailAddressValidator;

/**
 * Initial Date: May 4, 2004
 * 
 * @author gnaegi <a href="http://www.frentix.com">frentix GmbH</a>
 * @author guido
 * 
 *         <p>
 *         Comment:
 *         <p>
 *         The registration module deals with system registration and the
 *         disclaimer that has to be accepted by users when entering the system
 *         the first time.
 */
public class RegistrationModule extends AbstractOLATModule {
	// registration config
	private static final String CONFIG_SELFREGISTRATION = "enableSelfregistration";
	private static final String CONFIG_REGISTRATION_NOTIFICATION ="registrationNotificationEnabled";
	private static final String CONFIG_REGISTRATION_NOTIFICATION_EMAIL ="registrationNotificationEmail";
	private static boolean selfRegistrationEnabled;
	private static String registrationNotificationEmail;
	// disclaimer config
	private static final String CONFIG_DISCLAIMER = "disclaimerEnabled";
	private static final String CONFIG_ADDITIONAL_CHECKBOX ="disclaimerAdditionalCheckbox";
	private static final String CONFIG_ADDITIONAL_LINK ="disclaimerAdditionaLinkText";	
	private static boolean disclaimerEnabled;	
	private static boolean additionalCheckbox;
	private static boolean additionaLinkText;
	
	private static UserNameCreationInterceptor usernamePresetBean;
	
	/**
	 * [used by spring]
	 */
	private RegistrationModule() {
		//
	}

	public static UserNameCreationInterceptor getUsernamePresetBean() {
		return RegistrationModule.usernamePresetBean;
	}

	public void setUsernamePresetBean(UserNameCreationInterceptor usernamePresetBean) {
		RegistrationModule.usernamePresetBean = usernamePresetBean;
	}

	/**
	 * @return true if self registration is turned on, false otherwhise
	 */
	public static boolean isSelfRegistrationEnabled(){
	    return selfRegistrationEnabled;
	}

	/**
	 * Returns the notification email address that should be used in case 
	 * of new user registrations. Returns null if no such notification
	 * should be applied.
	 * @return String or null if not configured or disabled
	 */
	public static String getRegistrationNotificationEmail() {
		return registrationNotificationEmail;
	}

	/**
	 * @return true to force acceptance of disclaimer on first login; true to skip disclaimer
	 */
	public static boolean isDisclaimerEnabled() {
		return disclaimerEnabled;
	}
	
	/**
	 * @return true to add a second checkbox to the disclaimer
	 */
	public static boolean isDisclaimerAdditionalCheckbox() {
		return additionalCheckbox;
	}

	/**
	 * @return true to add a link to the disclaimer
	 */
	public static boolean isDisclaimerAdditionaLinkText() {
		return additionaLinkText;
	}

	@Override
	public void init() {
		// Nothing to initialize
	}

	@Override
	protected void initDefaultProperties() {
		
		selfRegistrationEnabled = getBooleanConfigParameter(CONFIG_SELFREGISTRATION, false);
		if (selfRegistrationEnabled) {
		  logInfo("Selfregistration is turned ON");
		} else {
			logInfo("Selfregistration is turned OFF");
		}

		// Check for registration email notification configuration
		Boolean regNoti = getBooleanConfigParameter(CONFIG_REGISTRATION_NOTIFICATION, true);
		registrationNotificationEmail = getStringConfigParameter(CONFIG_REGISTRATION_NOTIFICATION_EMAIL, "", true);
		if (EmailAddressValidator.isValidEmailAddress(registrationNotificationEmail) || !regNoti) {
			logInfo("Registration notification email is turned OFF by configuration or because given email::" + registrationNotificationEmail + "  is not valid.");
			registrationNotificationEmail = null;
		} else {				
			logInfo("Registration notification email is turned ON, email used is '" + registrationNotificationEmail + "'");								
		}
		
		// disclaimer configuration
		disclaimerEnabled = getBooleanConfigParameter(CONFIG_DISCLAIMER, false);
		if (disclaimerEnabled) {
		  logInfo("Disclaimer is turned ON");
		} else {
			logInfo("Disclaimer is turned OFF");
		}
		// optional disclaimer elements
		additionalCheckbox = getBooleanConfigParameter(CONFIG_ADDITIONAL_CHECKBOX, false);
		additionaLinkText = getBooleanConfigParameter(CONFIG_ADDITIONAL_LINK, false);
	}

	@Override
	protected void initFromChangedProperties() {
		// Nothing to init
	}

	@Override
	public void setPersistedProperties(PersistedProperties persistedProperties) {
		this.moduleConfigProperties = persistedProperties;
	}
}