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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.shibboleth.util.AttributeTranslator;
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

	private static final OLog log = Tracing.createLoggerFor(ShibbolethModule.class);

	/**
	 * Path identifier for shibboleth registration workflows.
	 */
	static final String PATH_REGISTER_SHIBBOLETH = "shibregister";
	private static final String DEFAULT_ATTRIBUTE_HANDLER = "DoNothingHandler";

	@Value("${shibboleth.enable}")
	private boolean enableShibbolethLogins = false;

	@Autowired
	private AttributeTranslator attributeTranslator;

	@Autowired @Qualifier("shibbolethOperators")
	private ArrayList<String> operators;

	@Value("${shibboleth.template.login:shibbolethlogin}")
	private String loginTemplate;
	@Value("${shibboleth.template.login.default:default_shibbolethlogin}")
	private String loginTemplateDefault;

	public static final String MULTIVALUE_SEPARATOR = ";";

	@Value("${shibboleth.preferred.language}")
	private String preferredLanguageAttribute;
	@Value("${shibboleth.defaultUID:Shib-SwissEP-UniqueID}")
	private String defaultUIDAttribute;
	@Autowired @Qualifier("shibbolethUserMapping")
	private HashMap<String, String> userMapping;
	@Autowired @Qualifier("shibbolethAttributeHandler")
	private HashMap<String, String> attributeHandler;

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

		if (!checkShibboletAttributeNameIsNotEmpty(UserConstants.EMAIL)) return;
		if (!checkShibboletAttributeNameIsNotEmpty(UserConstants.FIRSTNAME)) return;
		if (!checkShibboletAttributeNameIsNotEmpty(UserConstants.LASTNAME)) return;

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
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}

	/**
	 * Internal helper to check for empty configuration variables
	 *
	 * @param param
	 * @return true: not empty; false: empty or null
	 */
	private boolean checkShibboletAttributeNameIsNotEmpty(String userProperty) {
		String attributeName = getShibbolethAttributeName(userProperty);
		if (StringHelper.containsNonWhitespace(attributeName)) {
			return true;
		}

		log.error("Missing configuration for user property '" + userProperty
					+ "'. Add this configuration to olat.local.properties first. Disabling Shibboleth.");
		enableShibbolethLogins = false;
		return false;
	}

	/**
	 * @return True if shibboleth logins are allowed.
	 */
	public boolean isEnableShibbolethLogins() {
		return enableShibbolethLogins;
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

	/**
	 *
	 * @return the shib. default attribute which identifies an user by an unique key
	 */
	public String getDefaultUIDAttribute() {
		return defaultUIDAttribute;
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

	public String getPreferredLanguageAttribute() {
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

	/**
	 * Returns the name of the ShibbolethAttributeHandler for the name of a
	 * Shibboleth attribute.
	 *
	 * @param attributeName
	 * @return
	 */
	public String getShibbolethAttributeHandlerName(String attributeName) {
		String attributeHandlerName = attributeHandler.get(attributeName);
		if (!StringHelper.containsNonWhitespace(attributeHandlerName)) {
			attributeHandlerName = DEFAULT_ATTRIBUTE_HANDLER;
		}
		return attributeHandlerName;
	}

}