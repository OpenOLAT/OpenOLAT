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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.olat.core.configuration.AbstractOLATModule;
import org.olat.core.configuration.PersistedProperties;
import org.olat.core.util.StringHelper;
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
	private static final String CONFIG_STATIC_PROPERTY_MAPPING = "enableStaticPropertyMapping";
	private static final String CONFIG_STATIC_PROPERTY_MAPPING_KEY = "staticPropertyMapping";
	private static final String CONFIG_STATIC_PROPERTY_MAPPING_VAL = "staticPropertyMappingValue";
	private static final String CONFIG_SELFREGISTRATION_LINK = "enableSelfregistrationLink";
	private static final String CONFIG_SELFREGISTRATION_LOGIN = "enableSelfregistrationLogin";
	private static final String CONFIG_REGISTRATION_NOTIFICATION ="registrationNotificationEnabled";
	private static final String CONFIG_REGISTRATION_NOTIFICATION_EMAIL ="registrationNotificationEmail";
	private boolean selfRegistrationEnabled;
	private boolean selfRegistrationLinkEnabled;
	private boolean selfRegistrationLoginEnabled;
	private boolean staticPropertyMappingEnabled;
	private String registrationNotificationEmail;
	private String domainList;
	private String staticPropertyMappingName;
	private String staticPropertyMappingValue;
	
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
	public boolean isSelfRegistrationEnabled(){
	    return selfRegistrationEnabled;
	}
	
	public void setSelfRegistrationEnabled(boolean enable) {
		String value = enable ? "true" : "false";
		setStringProperty("registration.enabled", value, true);
	}
	
	/**
	 * @return true if self registration is turned on, false otherwhise
	 */
	public boolean isStaticPropertyMappingEnabled(){
	    return staticPropertyMappingEnabled;
	}
	
	public void setStaticPropertyMappingEnabled(boolean enable) {
		String value = enable ? "true" : "false";
		setStringProperty("static.prop.mapping.enabled", value, true);
	}
	
	public String getStaticPropertyMappingName() {
    return staticPropertyMappingName;
	}

	public void setStaticPropertyMappingName(String value) {
		value = StringHelper.containsNonWhitespace(value) ? value : "";
		setStringProperty("static.prop.mapping", value, true);
	}
	
	public String getStaticPropertyMappingValue() {
    return staticPropertyMappingValue;
	}

	public void setStaticPropertyMappingValue(String value) {
		value = StringHelper.containsNonWhitespace(value) ? value : "";
		setStringProperty("static.prop.mapping.value", value, true);
	}
	
	public boolean isSelfRegistrationLinkEnabled(){
    return selfRegistrationLinkEnabled;
	}
	
	public void setSelfRegistrationLinkEnabled(boolean enable) {
		String value = enable ? "true" : "false";
		setStringProperty("registration.link.enabled", value, true);
	}
	
	public boolean isSelfRegistrationLoginEnabled(){
    return selfRegistrationLoginEnabled;
	}
	
	public void setSelfRegistrationLoginEnabled(boolean enable) {
		String value = enable ? "true" : "false";
		setStringProperty("registration.login.enabled", value, true);
	}
	
	public String getDomainListRaw() {
		return domainList;
	}
	
	public void setDomainListRaw(String list) {
		setStringProperty("registration.domains", normalizeDomainList(list), true);
	}
	
	private String normalizeDomainList(String list) {
		if(list == null) list = "";
		return list.replace("\n", ",").replace("\r", "").replace(" ", ",").replace("\t", ",");
	}
	
	public List<String> getDomainList() {
		if(StringHelper.containsNonWhitespace(domainList)) {
			return Arrays.asList(domainList.split(","));
		}
		return new ArrayList<String>(1);
	}
	
	public List<String> getDomainList(String list) {
		return Arrays.asList(normalizeDomainList(list).split(","));
	}

	/**
	 * Returns the notification email address that should be used in case 
	 * of new user registrations. Returns null if no such notification
	 * should be applied.
	 * @return String or null if not configured or disabled
	 */
	public String getRegistrationNotificationEmail() {
		return registrationNotificationEmail;
	}

	/**
	 * @return true to force acceptance of disclaimer on first login; true to skip disclaimer
	 */
	public boolean isDisclaimerEnabled() {
		return disclaimerEnabled;
	}
	
	/**
	 * @return true to add a second checkbox to the disclaimer
	 */
	public boolean isDisclaimerAdditionalCheckbox() {
		return additionalCheckbox;
	}

	/**
	 * @return true to add a link to the disclaimer
	 */
	public boolean isDisclaimerAdditionaLinkText() {
		return additionaLinkText;
	}

	@Override
	public void init() {
		//registration enabled/disabled
		String enabledObj = getStringPropertyValue("registration.enabled", true);
		selfRegistrationEnabled = "true".equals(enabledObj);
		
		//link registration enabled/disabled (rest)
		String linkEnabledObj = getStringPropertyValue("registration.link.enabled", true);
		selfRegistrationLinkEnabled = "true".equals(linkEnabledObj);
		
		//link on the login page for registration enabled/disabled 
		String loginEnabledObj = getStringPropertyValue("registration.login.enabled", true);
		selfRegistrationLoginEnabled = "true".equals(loginEnabledObj);
		
		//white list of domains
		String domainObj = getStringPropertyValue("registration.domains", true);
		if(StringHelper.containsNonWhitespace(domainObj)) {
			domainList = domainObj;
		} else {
			domainList = null; // reset
		}

		//static property mapping enabled/disabled
		String enabledPropObj = getStringPropertyValue("static.prop.mapping.enabled", true);
		staticPropertyMappingEnabled = "true".equals(enabledPropObj);
		
		String propKeyObj = getStringPropertyValue("static.prop.mapping", true);
		if(StringHelper.containsNonWhitespace(propKeyObj)) {
			staticPropertyMappingName = propKeyObj;
		} else {
			staticPropertyMappingName = null; // reset
		}
		String propValueObj = getStringPropertyValue("static.prop.mapping.value", true);
		if(StringHelper.containsNonWhitespace(propValueObj)) {
			staticPropertyMappingValue = propValueObj;
		} else {
			staticPropertyMappingValue = null; // reset
		}
	}

	@Override
	protected void initDefaultProperties() {
		selfRegistrationEnabled = getBooleanConfigParameter(CONFIG_SELFREGISTRATION, false);
		if (selfRegistrationEnabled) {
		  logInfo("Selfregistration is turned ON");
		} else {
			logInfo("Selfregistration is turned OFF");
		}
		
		selfRegistrationLinkEnabled = getBooleanConfigParameter(CONFIG_SELFREGISTRATION_LINK, false);
		selfRegistrationLoginEnabled = getBooleanConfigParameter(CONFIG_SELFREGISTRATION_LOGIN, false);

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

		staticPropertyMappingEnabled = getBooleanConfigParameter(CONFIG_STATIC_PROPERTY_MAPPING, false);
		staticPropertyMappingName = getStringConfigParameter(CONFIG_STATIC_PROPERTY_MAPPING_KEY, "", true);
		staticPropertyMappingValue = getStringConfigParameter(CONFIG_STATIC_PROPERTY_MAPPING_VAL, "", true);
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}

	@Override
	public void setPersistedProperties(PersistedProperties persistedProperties) {
		this.moduleConfigProperties = persistedProperties;
	}
}