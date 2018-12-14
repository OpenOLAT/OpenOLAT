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

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.mail.EmailAddressValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
@Service("registrationModule")
public class RegistrationModule extends AbstractSpringModule {
	private static final OLog log = Tracing.createLoggerFor(RegistrationModule.class);
	
	@Value("${registration.enableSelfRegistration}")
	private boolean selfRegistrationEnabled;
	@Value("${registration.enableSelfRegistration.link}")
	private boolean selfRegistrationLinkEnabled;
	@Value("${registration.enableSelfRegistration.login}")
	private boolean selfRegistrationLoginEnabled;
	@Value("${registration.valid.hours.gui}")
	private Integer validUntilHoursGui;
	@Value("${registration.valid.hours.rest}")
	private Integer validUntilHoursRest;
	@Value("${registration.organisation.key:default}")
	private String selfRegistrationOrganisationKey;
	
	@Value("${registration.enableNotificationEmail}")
	private boolean registrationNotificationEmailEnabled;
	@Value("${registration.notificationEmail}")
	private String registrationNotificationEmail;
	
	@Value("${registration.staticPropertyMapping:false}")
	private boolean staticPropertyMappingEnabled;
	@Value("${registration.domainList}")
	private String domainList;
	private String staticPropertyMappingName;
	private String staticPropertyMappingValue;
	
	@Value("${registration.enableDisclaimer}")
	private boolean disclaimerEnabled;	
	@Value("${registration.disclaimerAdditionalCheckbox}")
	private boolean additionalCheckbox;
	@Value("${registration.disclaimerAdditionalCheckbox2}")
	private boolean additionalCheckbox2;
	@Value("${registration.disclaimerAdditionaLinkText}	")
	private boolean additionaLinkText;
	
	@Autowired @Qualifier("usernamePresetBean")
	private UserNameCreationInterceptor usernamePresetBean;
	
	@Autowired
	public RegistrationModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	public UserNameCreationInterceptor getUsernamePresetBean() {
		return usernamePresetBean;
	}

	public boolean isSelfRegistrationEnabled(){
	    return selfRegistrationEnabled;
	}
	
	public void setSelfRegistrationEnabled(boolean enable) {
		selfRegistrationEnabled = enable;
		String value = enable ? "true" : "false";
		setStringProperty("registration.enabled", value, true);
	}
	
	public boolean isStaticPropertyMappingEnabled(){
	    return staticPropertyMappingEnabled;
	}
	
	public void setStaticPropertyMappingEnabled(boolean enable) {
		staticPropertyMappingEnabled = enable;
		String value = enable ? "true" : "false";
		setStringProperty("static.prop.mapping.enabled", value, true);
	}
	
	public String getStaticPropertyMappingName() {
		return staticPropertyMappingName;
	}

	public void setStaticPropertyMappingName(String value) {
		staticPropertyMappingName = StringHelper.containsNonWhitespace(value) ? value : "";
		setStringProperty("static.prop.mapping", staticPropertyMappingName, true);
	}
	
	public String getStaticPropertyMappingValue() {
		return staticPropertyMappingValue;
	}

	public void setStaticPropertyMappingValue(String value) {
		staticPropertyMappingValue = StringHelper.containsNonWhitespace(value) ? value : "";
		setStringProperty("static.prop.mapping.value", staticPropertyMappingValue, true);
	}
	
	public boolean isSelfRegistrationLinkEnabled(){
		return selfRegistrationLinkEnabled;
	}
	
	public void setSelfRegistrationLinkEnabled(boolean enable) {
		selfRegistrationLinkEnabled = enable;
		String value = enable ? "true" : "false";
		setStringProperty("registration.link.enabled", value, true);
	}
	
	public boolean isSelfRegistrationLoginEnabled(){
		return selfRegistrationLoginEnabled;
	}
	
	public void setSelfRegistrationLoginEnabled(boolean enable) {
		selfRegistrationLoginEnabled = enable;
		String value = enable ? "true" : "false";
		setStringProperty("registration.login.enabled", value, true);
	}
	
	public Integer getValidUntilHoursGui() {
		return validUntilHoursGui;
	}

	public void setValidUntilHoursGui(Integer validUntilHoursGui) {
		this.validUntilHoursGui = validUntilHoursGui;
		setIntProperty("registration.valid.hours.gui", validUntilHoursGui, true);
	}

	public Integer getValidUntilHoursRest() {
		return validUntilHoursRest;
	}

	public void setValidUntilHoursRest(Integer validUntilHoursRest) {
		this.validUntilHoursRest = validUntilHoursRest;
		setIntProperty("registration.valid.hours.rest", validUntilHoursRest, true);
	}

	public String getDomainListRaw() {
		return domainList;
	}
	
	public void setDomainListRaw(String list) {
		domainList = normalizeDomainList(list);
		setStringProperty("registration.domains", domainList, true);
	}
	
	private String normalizeDomainList(String list) {
		if(list == null) list = "";
		return list.replace("\n", ",").replace("\r", "").replace(" ", ",").replace("\t", ",");
	}
	
	public List<String> getDomainList() {
		if(StringHelper.containsNonWhitespace(domainList)) {
			return Arrays.asList(domainList.split(","));
		}
		return new ArrayList<>(1);
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
	 * @return true to add a third checkbox to the disclaimer
	 */
	public boolean isDisclaimerAdditionalCheckbox2() {
		return additionalCheckbox2;
	}

	/**
	 * @return true to add a link to the disclaimer
	 */
	public boolean isDisclaimerAdditionaLinkText() {
		return additionaLinkText;
	}

	public String getSelfRegistrationOrganisationKey() {
		return selfRegistrationOrganisationKey;
	}
	
	public void setselfRegistrationOrganisationKey(String key) {
		selfRegistrationOrganisationKey = key;
		setStringProperty("registration.organisation.key", key, true);
	}
	
	@Override
	public void init() {
		//registration enabled/disabled
		String enabledObj = getStringPropertyValue("registration.enabled", true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			selfRegistrationEnabled = "true".equals(enabledObj);
		}
		
		//link registration enabled/disabled (rest)
		String linkEnabledObj = getStringPropertyValue("registration.link.enabled", true);
		if(StringHelper.containsNonWhitespace(linkEnabledObj)) {
			selfRegistrationLinkEnabled = "true".equals(linkEnabledObj);
		}
		
		//link on the login page for registration enabled/disabled 
		String loginEnabledObj = getStringPropertyValue("registration.login.enabled", true);
		if(StringHelper.containsNonWhitespace(loginEnabledObj)) {
			selfRegistrationLoginEnabled = "true".equals(loginEnabledObj);
		}

		String organisationObj = getStringPropertyValue("registration.organisation.key", false);
		if(StringHelper.containsNonWhitespace(organisationObj)) {
			selfRegistrationOrganisationKey = organisationObj;
		}
		
		int validUntilHoursGuiInt = getIntPropertyValue("registration.valid.hours.gui");
		if (validUntilHoursGuiInt > 0) {
			validUntilHoursGui = validUntilHoursGuiInt;
		}
		
		int validUntilHoursRestInt = getIntPropertyValue("registration.valid.hours.rest");
		if (validUntilHoursRestInt > 0) {
			validUntilHoursRest = validUntilHoursRestInt;
		}
		
		//white list of domains
		String domainObj = getStringPropertyValue("registration.domains", true);
		if(StringHelper.containsNonWhitespace(domainObj)) {
			domainList = domainObj;
		} else {// allowed because to olat.properties for this
			domainList = null; // reset
		}

		//static property mapping enabled/disabled
		String enabledPropObj = getStringPropertyValue("static.prop.mapping.enabled", true);
		if(StringHelper.containsNonWhitespace(enabledPropObj)) {
			staticPropertyMappingEnabled = "true".equals(enabledPropObj);
		}
		
		String propKeyObj = getStringPropertyValue("static.prop.mapping", false);
		if(StringHelper.containsNonWhitespace(propKeyObj)) {
			staticPropertyMappingName = propKeyObj;
		} else {
			staticPropertyMappingName = null; // reset
		}
		String propValueObj = getStringPropertyValue("static.prop.mapping.value", false);
		if(StringHelper.containsNonWhitespace(propValueObj)) {
			staticPropertyMappingValue = propValueObj;
		} else {
			staticPropertyMappingValue = null; // reset
		}
	}

	@Override
	protected void initDefaultProperties() {
		super.initDefaultProperties();
		
		if (selfRegistrationEnabled) {
			log.info("Selfregistration is turned ON");
		} else {
			log.info("Selfregistration is turned OFF");
		}

		// Check for registration email notification configuration
		if (EmailAddressValidator.isValidEmailAddress(registrationNotificationEmail) || !registrationNotificationEmailEnabled) {
			log.info("Registration notification email is turned OFF by configuration or because given email::" + registrationNotificationEmail + "  is not valid.");
			registrationNotificationEmail = null;
		} else {				
			log.info("Registration notification email is turned ON, email used is '" + registrationNotificationEmail + "'");								
		}
		
		// disclaimer configuration
		if (disclaimerEnabled) {
			log.info("Disclaimer is turned ON");
		} else {
			log.info("Disclaimer is turned OFF");
		}
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}
}