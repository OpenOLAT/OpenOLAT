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

package org.olat.shibboleth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.id.UserConstants;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.registration.UserNameCreationInterceptor;
import org.olat.resource.accesscontrol.provider.auto.IdentifierKey;
import org.olat.shibboleth.util.AttributeTranslator;
import org.olat.user.UserModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Initial Date:  16.07.2004
 *
 * @author Mike Stock
 */
@Service("shibbolethModule")
public class ShibbolethModule extends AbstractSpringModule implements ConfigOnOff {

	private static final Logger log = Tracing.createLoggerFor(ShibbolethModule.class);

	private static final String AUTHOR_CONTAINS_SPLIT_VALUE = ",";

	/**
	 * Path identifier for shibboleth registration workflows.
	 */
	static final String PATH_REGISTER_SHIBBOLETH = "shibregister";
	static final String PATH_DISCLAIMER_SHIBBOLETH = "shibdisclaimer";

	@Value("${shibboleth.enable}")
	private boolean enableShibbolethLogins = false;

	@Resource(name="${shibboleth.attribute.translator}")
	private AttributeTranslator attributeTranslator;

	@Autowired @Qualifier("shibbolethOperators")
	private ArrayList<String> operators;

	@Value("${shibboleth.template.login:shibbolethlogin}")
	private String loginTemplate;
	@Value("${shibboleth.template.login.default:default_shibbolethlogin}")
	private String loginTemplateDefault;
	
	@Value("${shibboleth.organisation.strategy}")
	private String organisationStrategy;
	@Value("${shibboleth.organisation.default}")
	private String defaultOrganisation;
	@Value("${shibboleth.organisation.shib}")
	private String shibbolethOrganisation;

	public static final String MULTIVALUE_SEPARATOR = ";";

	@Value("${shibboleth.preferred.language.shib}")
	private String preferredLanguageAttribute;
	@Value("${shibboleth.uid.shib}")
	private String uidAttributeName;
	@Autowired @Qualifier("shibbolethUserMapping")
	private HashMap<String, String> userMapping;
	@Autowired @Qualifier("shibbolethAttributeHandler")
	private HashMap<String, String> attributeHandler;
	@Autowired @Qualifier("shibbolethDeleteIfNull")
	private HashMap<String, Boolean> deleteIfNull;

	@Value("${shibboleth.role.mapping.author.enable:false}")
	private boolean authorMappingEnabled;
	@Value("${shibboleth.role.mapping.author.shib}")
	private String authorMappingAttributeName;
	@Value("${shibboleth.role.mapping.author.contains}")
	private String authorMappingContains;
	@Value("${shibboleth.ac.byAttributes:false}")
	private boolean accessControlByAttributes;
	@Value("${shibboleth.ac.attribute1:#{null}}")
	private String attribute1;
	@Value("${shibboleth.ac.attribute1Values:#{null}}")
	private String attribute1Values;
	@Value("${shibboleth.ac.attribute2:#{null}}")
	private String attribute2;
	@Value("${shibboleth.ac.attribute2Values:#{null}}")
	private String attribute2Values;
	
	@Autowired @Qualifier("shibbolethUsernamePresetBean")
	private UserNameCreationInterceptor usernamePresetBean;

	@Value("${method.auto.shib.identifiers}")
	private String acAutoIdentifiersString;
	private Set<IdentifierKey> acAutoIdentifiers;
	@Value("${method.auto.shib.shib}")
	private String acAutoAttributeName;
	@Value("${method.auto.shib.splitter}")
	private String acAutoSplitter;

	@Autowired
	private UserModule userModule;
	
	@Autowired
	public ShibbolethModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		if (enableShibbolethLogins) {
			log.info("Shibboleth logins enabled.");
		} else {
			log.info("Shibboleth logins disabled.");
		}

		//module enabled/disabled
		String accessControlByAttributesObj = getStringPropertyValue("accessControlByAttributes", true);
		if(StringHelper.containsNonWhitespace(accessControlByAttributesObj)) {
			accessControlByAttributes = "true".equals(accessControlByAttributesObj);
		}

		ensureDefaultsForMandatoryUserProperties();

		String attribute1Obj = getStringPropertyValue("attribute1", true);
		if(StringHelper.containsNonWhitespace(attribute1Obj)) {
			attribute1 = attribute1Obj;
		}

		String attribute1ValuesObj = getStringPropertyValue("attribute1Values", true);
		if(StringHelper.containsNonWhitespace(attribute1ValuesObj)) {
			attribute1Values = attribute1ValuesObj;
		}

		String attribute2Obj = getStringPropertyValue("attribute2", true);
		if(StringHelper.containsNonWhitespace(attribute2Obj)) {
			attribute2 = attribute2Obj;
		}

		String attribute2ValuesObj = getStringPropertyValue("attribute2Values", true);
		if(StringHelper.containsNonWhitespace(attribute2ValuesObj)) {
			attribute2Values = attribute2ValuesObj;
		}

		acAutoIdentifiers = parseAcIdentifiers(acAutoIdentifiersString);
	}

	private Set<IdentifierKey> parseAcIdentifiers(String raw) {
		Set<IdentifierKey> keys = new HashSet<>();
		List<String> keyStrings = Arrays.asList(raw.split(AUTHOR_CONTAINS_SPLIT_VALUE));
		for (String keyString : keyStrings) {
			try {
				if (StringHelper.containsNonWhitespace(keyString)) {
					keys.add(IdentifierKey.valueOf(keyString));
				}
			} catch (Exception e) {
				log.warn("The value '" + keyString + "' for the property 'method.auto.shib.identifiers' is not valid.");
			}
		}
		return keys;
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}

	private void ensureDefaultsForMandatoryUserProperties() {
		ensureMandatoryPropertesAreInUserMapping();
		ensureMandatoryPropertiesAreNeverDeleted();
	}

	private void ensureMandatoryPropertesAreInUserMapping() {
		getMandatoryUserProperties().forEach(userPropertyName -> addToUserMappingWithDefault(userPropertyName));
	}

	private void addToUserMappingWithDefault(String userProperty) {
		String attributeName = getShibbolethAttributeName(userProperty);
		if (!StringHelper.containsNonWhitespace(attributeName)) {
			userMapping.put("oo" + userProperty, userProperty);
		}
	}

	private void ensureMandatoryPropertiesAreNeverDeleted() {
		getMandatoryUserProperties().forEach(userPropertyName -> addToDeleteIfNullFalse(userPropertyName));
	}

	private void addToDeleteIfNullFalse(String userPropertyName) {
		String attributeName = getShibbolethAttributeName(userPropertyName);
		deleteIfNull.put(attributeName, false);
	}

	public boolean isEnableShibbolethLogins() {
		return enableShibbolethLogins;
	}
	
	/**
	 * @return true: use the attribute configurator in the course editor based
	 *         on the attribute translator; false: don's use the attribute
	 *         configurator
	 */
	public boolean isEnableShibbolethCourseEasyConfig() {
		return isEnableShibbolethLogins() && getAttributeTranslator().getTranslateableAttributes().size() > 0;
	}

	@Override
	public boolean isEnabled() {
		return isEnableShibbolethLogins();
	}

	public AttributeTranslator getAttributeTranslator() {
		return attributeTranslator;
	}

	public List<String> getOperatorKeys() {
		return operators;
	}

	public String getUIDAttributeName() {
		return uidAttributeName;
	}

	public String getLoginTemplate() {
		return loginTemplate;
	}

	public void setLoginTemplate(String loginTemplate) {
		this.loginTemplate = loginTemplate;
	}

	public String getLoginTemplateDefault() {
		return loginTemplateDefault;
	}

	public void setLoginTemplateDefault(String loginTemplateDefault) {
		this.loginTemplateDefault = loginTemplateDefault;
	}
	
	public ShibbolethOrganisationStrategy getOrganisationStrategy() {
		return ShibbolethOrganisationStrategy.secureValue(organisationStrategy);
	}

	public void setOrganisationStrategy(String organisationStrategy) {
		this.organisationStrategy = organisationStrategy;
	}

	public String getDefaultOrganisation() {
		return defaultOrganisation;
	}

	public void setDefaultOrganisation(String defaultOrganisation) {
		this.defaultOrganisation = defaultOrganisation;
	}

	public String getShibbolethOrganisation() {
		return shibbolethOrganisation;
	}

	public void setShibbolethOrganisation(String shibbolethOrganisation) {
		this.shibbolethOrganisation = shibbolethOrganisation;
	}

	public String getPreselectedAttributeKey(String userAttribute) {
		String shibKey = userMapping.get(userAttribute);
		return attributeTranslator.translateAttribute(shibKey);
	}

	public boolean isAccessControlByAttributes() {
		return accessControlByAttributes;
	}

	public void setAccessControlByAttributes(boolean accessControlByAttributes) {
		this.accessControlByAttributes = accessControlByAttributes;
		setStringProperty("accessControlByAttributes", accessControlByAttributes ? "true" : "false", true);
	}

	public String getAttribute1() {
		return attribute1;
	}

	public void setAttribute1(String attribute1) {
		this.attribute1 = attribute1;
		setStringProperty("attribute1", attribute1, true);
	}

	public String getAttribute1Values() {
		return attribute1Values;
	}

	public void setAttribute1Values(String attribute1Values) {
		this.attribute1Values = attribute1Values;
		setStringProperty("attribute1Values", attribute1Values, true);
	}

	public String getAttribute2() {
		return attribute2;
	}

	public void setAttribute2(String attribute2) {
		this.attribute2 = attribute2;
		setStringProperty("attribute2", attribute2, true);
	}

	public String getAttribute2Values() {
		return attribute2Values;
	}

	public void setAttribute2Values(String attribute2Values) {
		this.attribute2Values = attribute2Values;
		setStringProperty("attribute2Values", attribute2Values, true);
	}

	public String getPreferredLanguageAttributeName() {
		return preferredLanguageAttribute;
	}

	/**
	 * Returns the mapping of a Shibboleth user to an OpenOLAT user. The key
	 * is the Shibboleth attribute name. The value is the user property.
	 *
	 * @return
	 */
	public Map<String, String> getUserMapping() {
		return userMapping;
	}

	public Map<String, String> getAttributeHandlerNames() {
		return attributeHandler;
	}

	public HashMap<String, Boolean> getDeleteIfNull() {
		return deleteIfNull;
	}

	/**
	 * Returns the name of a Shibboleth attribute for a given user property name
	 * of null if not found.
	 *
	 * @param userProperty
	 * @return
	 */
	public String getShibbolethAttributeName(String userProperty) {
		for (Entry<String, String> mapping : userMapping.entrySet()) {
	        if (userProperty != null && userProperty.equals(mapping.getValue())) {
	            return mapping.getKey();
	        }
	    }
	    return null;
	}

	public boolean isAuthorMappingEnabled() {
		return authorMappingEnabled;
	}

	public String getAuthorMappingAttributeName() {
		return authorMappingAttributeName;
	}

	public Collection<String> getAuthorMappingContains() {
		Collection<String> containsValues = Collections.<String>emptyList();
		if (StringHelper.containsNonWhitespace(authorMappingContains)) {
			containsValues = Arrays.asList(authorMappingContains.split(AUTHOR_CONTAINS_SPLIT_VALUE));
		}
		return containsValues;
	}

	/**
	 * Get the names of all Shibboleth attributes which are configured either in
	 * the context.xml or in olat.local.properties
	 */
	public Collection<String> getShibbolethAttributeNames() {
		Set<String> attributeNames = new HashSet<>();
		for (String userMappingKey : attributeTranslator.getTranslateableAttributes()) {
			addAttributeNameIfDefined(userMappingKey, attributeNames);			
		}
		for (String userMappingKey : userMapping.keySet()) {
			addAttributeNameIfDefined(userMappingKey, attributeNames);
		}
		addAttributeNameIfDefined(uidAttributeName, attributeNames);
		addAttributeNameIfDefined(preferredLanguageAttribute, attributeNames);
		addAttributeNameIfDefined(authorMappingAttributeName, attributeNames);
		addAttributeNameIfDefined(attribute1, attributeNames);
		addAttributeNameIfDefined(attribute2, attributeNames);
		addAttributeNameIfDefined(acAutoAttributeName, attributeNames);
		addAttributeNameIfDefined(shibbolethOrganisation, attributeNames);
		return attributeNames;
	}
	
	private void addAttributeNameIfDefined(String attributeName, Set<String> attributeNames) {
		if (StringHelper.containsNonWhitespace(attributeName)) {
			attributeNames.add(attributeName);
		}		
	}
	
	public Collection<String> getMandatoryUserProperties() {
		Set<String> mandatoryUserProperties = new HashSet<>();
		mandatoryUserProperties.add(UserConstants.FIRSTNAME);
		mandatoryUserProperties.add(UserConstants.LASTNAME);
		if (userModule.isEmailMandatory()) {
			mandatoryUserProperties.add(UserConstants.EMAIL);
		}
		return mandatoryUserProperties;
	}
	

	public Set<IdentifierKey> getAcAutoIdentifiers() {
		return acAutoIdentifiers;
	}

	public String getAcAutoAttributeName() {
		return acAutoAttributeName;
	}

	public String getAcAutoSplitter() {
		return acAutoSplitter;
	}

	public UserNameCreationInterceptor getUsernamePresetBean() {
		return usernamePresetBean;
	}

	public void setUsernamePresetBean(UserNameCreationInterceptor usernamePresetBean) {
		this.usernamePresetBean = usernamePresetBean;
	}

}