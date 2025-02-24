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
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.mail.EmailAddressValidator;
import org.olat.user.UserModule;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.olat.user.propertyhandlers.UserPropertyUsageContext;
import org.olat.user.propertyhandlers.ui.UsrPropCfgManager;
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
	private static final Logger log = Tracing.createLoggerFor(RegistrationModule.class);
	
	public static final String SEPARATOR = ";";
	
	public static final int VALID_UNTIL_30_DAYS_IN_HOURS = 30 * 24;
	public static final int VALID_UNTIL_30_DAYS_IN_MINUTES = VALID_UNTIL_30_DAYS_IN_HOURS * 60;
	
	private static final String EMAIL_VLIDATION_ENABLED = "email.validation.enabled";
	private static final String ACCOUNT_EXPIRATION = "registration.account.expiration";
	private static final String AUTO_ENROLMENT_COURSES = "registration.auto.enrolment.courses";
	private static final String RECURRING_USERS_ENABLED = "registration.recurring.users.enabled";
	private static final String ADD_DEFAULT_ORG_ADDITIONALLY = "registration.add.default.org.enabled";
	
	@Value("${registration.enableSelfRegistration}")
	private boolean selfRegistrationEnabled;
	@Value("${registration.enableSelfRegistration.link}")
	private boolean selfRegistrationLinkEnabled;
	@Value("${registration.enableSelfRegistration.login}")
	private boolean selfRegistrationLoginEnabled;
	@Value("${registration.email.validation}")
	private boolean emailValidationEnabled;
	@Value("${registration.recurring.user}")
	private boolean allowRecurringUserEnabled;
	@Value("${registration.add.default.org}")
	private boolean addDefaultOrgEnabled;
	@Value("${registration.valid.minutes.gui}")
	private Integer validUntilMinutesGui;
	@Value("${registration.valid.hours.rest:720}")
	private Integer validUntilHoursRest;
	@Value("${registration.organisation.key:default}")
	private String selfRegistrationOrganisationKey;
	@Value("${registration.auto.enrolment.courses:}")
	private String selfRegistrationAutoEnrolmentCourseKeys;
	@Value("${registration.account.expiration}")
	private String accountExpirationInDays;
	
	@Value("${registration.enableNotificationEmail}")
	private boolean registrationNotificationEmailEnabled;
	@Value("${registration.notificationEmail}")
	private String registrationNotificationEmail;
	@Value("${registration.pending.status:active}")
	private String registrationPendingStatus;
	@Value("${registration.pending.property.name1:}")
	private String registrationPendingPropertyName1;
	@Value("${registration.pending.property.value1:}")
	private String registrationPendingPropertyValue1;
	@Value("${registration.pending.property.name2:}")
	private String registrationPendingPropertyName2;
	@Value("${registration.pending.property.value2:}")
	private String registrationPendingPropertyValue2;
	@Value("${registration.pending.property.name3:}")
	private String registrationPendingPropertyName3;
	@Value("${registration.pending.property.value3:}")
	private String registrationPendingPropertyValue3;
	@Value("${registration.pending.property.name4:}")
	private String registrationPendingPropertyName4;
	@Value("${registration.pending.property.value4:}")
	private String registrationPendingPropertyValue4;
	@Value("${registration.pending.property.name5:}")
	private String registrationPendingPropertyName5;
	@Value("${registration.pending.property.value5:}")
	private String registrationPendingPropertyValue5;
	
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
	@Value("${registration.disclaimerAdditionaLinkText}")
	private boolean additionaLinkText;
	
	@Autowired @Qualifier("usernamePresetBean")
	private UserNameCreationInterceptor usernamePresetBean;

	@Autowired
	private UserModule userModule;
	@Autowired
	private UsrPropCfgManager usrPropCfgMng;
	
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
	
	
	public boolean isEmailValidationEnabled() {
		return emailValidationEnabled;
	}

	public void setEmailValidationEnabled(boolean emailValidationEnabled) {
		this.emailValidationEnabled = emailValidationEnabled;
		setStringProperty(EMAIL_VLIDATION_ENABLED, Boolean.toString(emailValidationEnabled), true);
		resetEmailUserProperty();
	}

	public boolean isAllowRecurringUserEnabled() {
		return allowRecurringUserEnabled;
	}

	public void setAllowRecurringUserEnabled(boolean allowRecurringUserEnabled) {
		this.allowRecurringUserEnabled = allowRecurringUserEnabled;
		setStringProperty(RECURRING_USERS_ENABLED, Boolean.toString(allowRecurringUserEnabled), true);
	}

	public boolean isAddDefaultOrgEnabled() {
		return addDefaultOrgEnabled;
	}

	public void setAddDefaultOrgEnabled(boolean addDefaultOrgEnabled) {
		this.addDefaultOrgEnabled = addDefaultOrgEnabled;
		setStringProperty(ADD_DEFAULT_ORG_ADDITIONALLY, Boolean.toString(addDefaultOrgEnabled), true);
	}
	
	public void resetEmailUserProperty() {
		List<UserPropertyHandler> handlers = usrPropCfgMng.getUserPropertiesConfigObject().getPropertyHandlers();
		
		UserPropertyHandler emailHandler = null;
		for(UserPropertyHandler handler:handlers) {
			if(UserConstants.EMAIL.equals(handler.getName())) {
				emailHandler = handler;
			}
		}
		
		if(emailHandler != null) {
			UserPropertyUsageContext context = usrPropCfgMng.getUserPropertiesConfigObject()
					.getUsageContexts().get(RegistrationPersonalDataController.USERPROPERTIES_FORM_IDENTIFIER);
			context.addPropertyHandler(0, emailHandler);
			context.setAsUserViewReadOnly(emailHandler, isEmailValidationEnabled());
			context.setAsAdminstrativeUserOnly(emailHandler, false);
			context.setAsMandatoryUserProperty(emailHandler, userModule.isEmailMandatory());
		}
	}

	public Integer getValidUntilMinutesGui() {
		return validUntilMinutesGui;
	}

	public void setValidUntilMinutesGui(Integer validUntilMinutesGui) {
		this.validUntilMinutesGui = validUntilMinutesGui;
		setIntProperty("registration.valid.minutes.gui", validUntilMinutesGui, true);
	}
	
	/**
	 * 
	 * @return A value in minutes
	 */
	public int getRESTValidityOfTemporaryKey() {
		return validUntilHoursRest == null
				? VALID_UNTIL_30_DAYS_IN_MINUTES
				: (validUntilHoursRest.intValue() * 60) ;
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
	
	public void setRegistrationNotificationEmail(String email) {
		registrationNotificationEmail = email;
		setStringProperty("registration.notificationEmail", email, true);
	}
	
	public boolean isRegistrationNotificationEmailEnabled() {
		return registrationNotificationEmailEnabled;
	}
	
	public void setRegistrationNotificationEmailEnabled(boolean enabled) {
		String enabledStr = enabled ? "true" : "false";
		registrationNotificationEmailEnabled = enabled;
		setStringProperty("registration.enableNotificationEmail", enabledStr, true);
	}
	
	public RegistrationPendingStatus getRegistrationPendingStatus() {
		return RegistrationPendingStatus.valueOf(registrationPendingStatus);
	}
	
	public void setRegistrationPendingStatus(RegistrationPendingStatus status) {
		this.registrationPendingStatus = status.name();
		setStringProperty("registration.pending.status", status.name(), true);	
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
	
	public void setSelfRegistrationOrganisationKey(String key) {
		selfRegistrationOrganisationKey = key;
		setStringProperty("registration.organisation.key", key, true);
	}
	
	public List<Long> getAutoEnrolmentCourseKeys() {
		List<Long> courseKeys = new ArrayList<>();
		
		if (StringHelper.containsNonWhitespace(selfRegistrationAutoEnrolmentCourseKeys)) {
			for (String courseKey : selfRegistrationAutoEnrolmentCourseKeys.split(SEPARATOR)) {
				try {
					courseKeys.add(Long.valueOf(courseKey));
				} catch (Exception e) {
					log.atError().log("Self-registration conifg contains errors: " + courseKey + " is not a numerical value!");
				}
			}
		}
		
		return courseKeys;
	}
	
	public String getAutoEnrolmentRawValue() {
		return this.selfRegistrationAutoEnrolmentCourseKeys;
	}
	
	public void addCourseToAutoEnrolment(Long courseKey) {
		List<Long> courseKeys = getAutoEnrolmentCourseKeys();
		
		if (!courseKeys.contains(courseKey)) {
			courseKeys.add(courseKey);
			
			saveCourseKeys(courseKeys);
		}
	}
	
	public void removeCourseFromAutoEnrolment(Long courseKey) {
		List<Long> courseKeys = getAutoEnrolmentCourseKeys();
		
		if (courseKeys.contains(courseKey)) {
			courseKeys.remove(courseKey);
			
			saveCourseKeys(courseKeys);
		}
	}
	
	public void saveCourseKeys(List<Long> courseKeys) {
		this.selfRegistrationAutoEnrolmentCourseKeys = courseKeys.stream().map(String::valueOf).collect(Collectors.joining(SEPARATOR));
		setStringProperty(AUTO_ENROLMENT_COURSES, selfRegistrationAutoEnrolmentCourseKeys, true);
	}
	
	public String getRegistrationPendingPropertyName1() {
		return registrationPendingPropertyName1;
	}

	public void setRegistrationPendingProperty1(String propertyName, String propertyValue) {
		this.registrationPendingPropertyName1 = propertyName;
		this.registrationPendingPropertyValue1 = propertyValue;
		setStringProperty("registration.pending.property.name1", propertyName, false);
		setStringProperty("registration.pending.property.value1", propertyValue, true);
	}

	public String getRegistrationPendingPropertyValue1() {
		return registrationPendingPropertyValue1;
	}

	public String getRegistrationPendingPropertyName2() {
		return registrationPendingPropertyName2;
	}

	public void setRegistrationPendingProperty2(String propertyName, String propertyValue) {
		this.registrationPendingPropertyName2 = propertyName;
		this.registrationPendingPropertyValue2 = propertyValue;
		setStringProperty("registration.pending.property.name2", propertyName, false);
		setStringProperty("registration.pending.property.value2", propertyValue, true);
	}

	public String getRegistrationPendingPropertyValue2() {
		return registrationPendingPropertyValue2;
	}

	public String getRegistrationPendingPropertyName3() {
		return registrationPendingPropertyName3;
	}

	public void setRegistrationPendingProperty3(String propertyName, String propertyValue) {
		this.registrationPendingPropertyName3 = propertyName;
		this.registrationPendingPropertyValue3 = propertyValue;
		setStringProperty("registration.pending.property.name3", propertyName, false);
		setStringProperty("registration.pending.property.value3", propertyValue, true);
	}

	public String getRegistrationPendingPropertyValue3() {
		return registrationPendingPropertyValue3;
	}

	public String getRegistrationPendingPropertyName4() {
		return registrationPendingPropertyName4;
	}

	public void setRegistrationPendingProperty4(String propertyName, String propertyValue) {
		this.registrationPendingPropertyName4 = propertyName;
		this.registrationPendingPropertyValue4 = propertyValue;
		setStringProperty("registration.pending.property.name4", propertyName, false);
		setStringProperty("registration.pending.property.value4", propertyValue, true);
	}

	public String getRegistrationPendingPropertyValue4() {
		return registrationPendingPropertyValue4;
	}

	public String getRegistrationPendingPropertyName5() {
		return registrationPendingPropertyName5;
	}

	public void setRegistrationPendingProperty5(String propertyName, String propertyValue) {
		this.registrationPendingPropertyName5 = propertyName;
		this.registrationPendingPropertyValue5 = propertyValue;
		setStringProperty("registration.pending.property.name5", propertyName, false);
		setStringProperty("registration.pending.property.value5", propertyValue, true);
	}

	public String getRegistrationPendingPropertyValue5() {
		return registrationPendingPropertyValue5;
	}
	
	/**
	 * @return
	 */
	public Integer getAccountExpirationInDays() {
		if(StringHelper.containsNonWhitespace(accountExpirationInDays)) {
			return Integer.valueOf(accountExpirationInDays);
		}
		return null;
	}

	public void setAccountExpirationInDays(Integer days) {
		accountExpirationInDays = days == null ? null : Integer.toString(days.intValue());
		setStringProperty(ACCOUNT_EXPIRATION, accountExpirationInDays, true);
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

		String emailValidationObj = getStringPropertyValue(EMAIL_VLIDATION_ENABLED, true);
		if(StringHelper.containsNonWhitespace(emailValidationObj)) {
			emailValidationEnabled = Boolean.parseBoolean(emailValidationObj);
		}
		resetEmailUserProperty();

		String recurringUserObj = getStringPropertyValue(RECURRING_USERS_ENABLED, true);
		if(StringHelper.containsNonWhitespace(recurringUserObj)) {
			allowRecurringUserEnabled = Boolean.parseBoolean(recurringUserObj);
		}

		String addDefaultOrgObj = getStringPropertyValue(ADD_DEFAULT_ORG_ADDITIONALLY, true);
		if(StringHelper.containsNonWhitespace(addDefaultOrgObj)) {
			addDefaultOrgEnabled = Boolean.parseBoolean(addDefaultOrgObj);
		}

		String enableNotificationEmailObj = getStringPropertyValue("registration.enableNotificationEmail", true);
		if(StringHelper.containsNonWhitespace(enableNotificationEmailObj)) {
			registrationNotificationEmailEnabled = "true".equals(enableNotificationEmailObj);
		}
		
		String notificationEmailObj = getStringPropertyValue("registration.notificationEmail", true);
		if(StringHelper.containsNonWhitespace(notificationEmailObj)) {
			registrationNotificationEmail = notificationEmailObj;
		}
	
		String pendingPropName1Obj = getStringPropertyValue("registration.pending.property.name1", true);
		String pendingPropValue1Obj = getStringPropertyValue("registration.pending.property.value1", true);
		if(StringHelper.containsNonWhitespace(pendingPropName1Obj) && StringHelper.containsNonWhitespace(pendingPropValue1Obj)) {
			registrationPendingPropertyName1 = pendingPropName1Obj;
			registrationPendingPropertyValue1 = pendingPropValue1Obj;
		}
		
		String pendingPropName2Obj = getStringPropertyValue("registration.pending.property.name2", true);
		String pendingPropValue2Obj = getStringPropertyValue("registration.pending.property.value2", true);
		if(StringHelper.containsNonWhitespace(pendingPropName2Obj) && StringHelper.containsNonWhitespace(pendingPropValue2Obj)) {
			registrationPendingPropertyName2 = pendingPropName2Obj;
			registrationPendingPropertyValue2 = pendingPropValue2Obj;
		}
		
		String pendingPropName3Obj = getStringPropertyValue("registration.pending.property.name3", true);
		String pendingPropValue3Obj = getStringPropertyValue("registration.pending.property.value3", true);
		if(StringHelper.containsNonWhitespace(pendingPropName3Obj) && StringHelper.containsNonWhitespace(pendingPropValue3Obj)) {
			registrationPendingPropertyName3 = pendingPropName3Obj;
			registrationPendingPropertyValue3 = pendingPropValue3Obj;
		}

		String pendingPropName4Obj = getStringPropertyValue("registration.pending.property.name4", true);
		String pendingPropValue4Obj = getStringPropertyValue("registration.pending.property.value4", true);
		if(StringHelper.containsNonWhitespace(pendingPropName4Obj) && StringHelper.containsNonWhitespace(pendingPropValue4Obj)) {
			registrationPendingPropertyName4 = pendingPropName4Obj;
			registrationPendingPropertyValue4 = pendingPropValue4Obj;
		}
		
		String pendingPropName5Obj = getStringPropertyValue("registration.pending.property.name5", true);
		String pendingPropValue5Obj = getStringPropertyValue("registration.pending.property.value5", true);
		if(StringHelper.containsNonWhitespace(pendingPropName5Obj) && StringHelper.containsNonWhitespace(pendingPropValue5Obj)) {
			registrationPendingPropertyName5 = pendingPropName5Obj;
			registrationPendingPropertyValue5 = pendingPropValue5Obj;
		}
		
		String pendingStatusObj = getStringPropertyValue("registration.pending.status", true);
		if(StringHelper.containsNonWhitespace(pendingStatusObj)) {
			registrationPendingStatus = pendingStatusObj;
		}

		String organisationObj = getStringPropertyValue("registration.organisation.key", false);
		if(StringHelper.containsNonWhitespace(organisationObj)) {
			selfRegistrationOrganisationKey = organisationObj;
		}
		
		int validUntilMinutesGuiInt = getIntPropertyValue("registration.valid.minutes.gui");
		if (validUntilMinutesGuiInt > 0) {
			validUntilMinutesGui = validUntilMinutesGuiInt;
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
		
		// Auto-enrolment courses
		String selfRegistrationAutoEnrolmentCourseKeysString = getStringPropertyValue(AUTO_ENROLMENT_COURSES, true);
		if (StringHelper.containsNonWhitespace(selfRegistrationAutoEnrolmentCourseKeysString)) {
			selfRegistrationAutoEnrolmentCourseKeys = selfRegistrationAutoEnrolmentCourseKeysString;
		} else {
			selfRegistrationAutoEnrolmentCourseKeys = null;
		}
		
		accountExpirationInDays = getStringPropertyValue(ACCOUNT_EXPIRATION, accountExpirationInDays);		
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
			log.info("Registration notification email is turned OFF by configuration or because given email::{}  is not valid.", registrationNotificationEmail);
			registrationNotificationEmail = null;
		} else {				
			log.info("Registration notification email is turned ON, email used is '{}'", registrationNotificationEmail);								
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