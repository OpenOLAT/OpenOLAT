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

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
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
 * 
 * Comment:  
 * 
 */
@Service("shibbolethModule")
public class ShibbolethModule extends AbstractSpringModule implements ConfigOnOff {
	
	private static final OLog log = Tracing.createLoggerFor(ShibbolethModule.class);
	
	/**
	 * Path identifier for shibboleth registration workflows.
	 */
	static final String PATH_REGISTER_SHIBBOLETH = "shibregister";	
	
	private static final String CONF_OLATUSERMAPPING_FIRSTNAME = "FirstName";
	private static final String CONF_OLATUSERMAPPING_LASTNAME = "LastName";
	private static final String CONF_OLATUSERMAPPING_EMAIL = "EMail";
	public static final String CONF_OLATUSERMAPPING_INSTITUTIONALNAME = "InstitutionalName";
	private static final String CONF_OLATUSERMAPPING_INSTITUTIONALEMAIL = "InstitutionalEMail";
	private static final String CONF_OLATUSERMAPPING_INSTITUTIONALUSERIDENTIFIER = "InstitutionalUserIdentifier";
	private static final String CONF_OLATUSERMAPPING_PREFERED_LANGUAGE = "PreferedLanguage";
	
	@Value("${shibboleth.enable}")
	private boolean enableShibbolethLogins = false;
	
	@Autowired
	private AttributeTranslator attributeTranslator;

	@Value("${language.enable}")
	private boolean useLanguageInReq = false;
	@Value("${language.param:en}")
	private String languageParamName;
	
	@Autowired @Qualifier("shibbolethOperators")
	private ArrayList<String> operators;

	@Value("${shibboleth.template.login:shibbolethlogin}")
	private String loginTemplate;
	@Value("${shibboleth.template.login.default:default_shibbolethlogin}")
	private String loginTemplateDefault;

	public final String MULTIVALUE_SEPARATOR = ";";
	
	@Value("${shibboleth.defaultUID:Shib-SwissEP-UniqueID}")
	private String defaultUIDAttribute;
	@Autowired @Qualifier("shibbolethUserMapping")
	private HashMap<String, String> userMapping;

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
			
			if(useLanguageInReq) {
				log.info("Language code is sent as parameter in the AAI request with lang: "+languageParamName);
			} else {
				log.info("Language code is not sent with AAI request.");
			}
		} else {
			log.info("Shibboleth logins disabled.");
		}
		
		//module enabled/disabled
		String accessControlByAttributesObj = getStringPropertyValue("accessControlByAttributes", true);
		if(StringHelper.containsNonWhitespace(accessControlByAttributesObj)) {
			accessControlByAttributes = "true".equals(accessControlByAttributesObj);
		}
		
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
	 * @return True if shibboleth logins are allowed.
	 */
	public boolean isEnableShibbolethLogins() {
		return enableShibbolethLogins;
	}
	
	@Override
	public boolean isEnabled() {
		return isEnableShibbolethLogins();
	}

	/**
	 * @return true if the language should be sent in the aai request
	 */
	public boolean useLanguageInReq() {
		return useLanguageInReq;
	}

	/**
	 * @return the get request parameter name to be used sending the language code.
	 */
	public String getLanguageParamName() {
		return languageParamName;
	}

	public AttributeTranslator getAttributeTranslator() {
		return attributeTranslator;
	}

	public String[] getRegisteredOperatorKeys() {
		return null;
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
	
	/**
	 * @param attributesMap
	 * @return First Name value from shibboleth attributes.
	 */
	public String getFirstName() {
		return userMapping.get(CONF_OLATUSERMAPPING_FIRSTNAME);
	}
	
	/**
	 * @return Last Name value from shibboleth attributes.
	 */
	public String getLastName() {
		return userMapping.get(CONF_OLATUSERMAPPING_LASTNAME);
	}
	
	/**
	 * @return EMail value from shibboleth attributes.
	 */
	public String getEMail() {
		return userMapping.get(CONF_OLATUSERMAPPING_EMAIL);
	}
	
	/**
	 * @return Institutional EMail value from shibboleth attributes.
	 */
	public String getInstitutionalEMail() {
		return userMapping.get(CONF_OLATUSERMAPPING_INSTITUTIONALEMAIL);
	}
	
	/**
	 * @return Institutional Name value from shibboleth attributes.
	 */
	public String getInstitutionalName() {
		return userMapping.get(CONF_OLATUSERMAPPING_INSTITUTIONALNAME);
	}
	
	/**
	 * @return Institutional User Identifyer value from shibboleth attributes.
	 */
	public String getInstitutionalUserIdentifier() {
		return userMapping.get(CONF_OLATUSERMAPPING_INSTITUTIONALUSERIDENTIFIER);
	}
	
	/**
	 * @return Prefered language value from shibboleth attributes.
	 */
	public String getPreferedLanguage() {
		return userMapping.get(CONF_OLATUSERMAPPING_PREFERED_LANGUAGE);
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
}