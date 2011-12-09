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
package org.olat.ldap;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Description: Helper methods for the LDAP package
 * <p>
 * LDAPHelper
 * <ul>
 * </ul>
 * <p>
 * 
 * @author Maurus Rohrer
 */

public class LDAPHelper {
	private static OLog log = Tracing.createLoggerFor(LDAPHelper.class);

	private LDAPHelper() {
	// do nothing
	}

	/**
	 * Maps LDAP Attributes to the OLAT Property 
	 * 
	 * Configuration: LDAP Attributes Map = olatextconfig.xml (property=userAttrs)
	 * 
	 * @param attrID LDAP Attribute
	 * @return OLAT Property
	 */
	public static String mapLdapAttributeToOlatProperty(String attrID) {
		Map<String, String> userAttrMapper = LDAPLoginModule.getUserAttributeMapper();
		String olatProperty = userAttrMapper.get(attrID);
		return olatProperty;
	}

	
	/**
	 * Maps OLAT Property to the LDAP Attributes 
	 * 
	 * Configuration: LDAP Attributes Map = olatextconfig.xml (property=userAttrs)
	 * 
	 * @param olatProperty OLAT PropertyattrID 
	 * @return LDAP Attribute
	 */
	public static String mapOlatPropertyToLdapAttribute(String olatProperty) {
		Map<String, String> userAttrMapper = LDAPLoginModule.getReqAttrs();
		if (userAttrMapper.containsValue(olatProperty)) {
			Iterator<String> itr = userAttrMapper.keySet().iterator();
			while (itr.hasNext()) {
				String key = itr.next();
				if (userAttrMapper.get(key).compareTo(olatProperty) == 0) return key;
			}
		}
		return null;
	}

	/**
	 * Extracts Value out of LDAP Attribute
	 * 
	 * 
	 * @param attribute LDAP Naming Attribute 
	 * @return String value of Attribute, null on Exception
	 * 
	 * @throws NamingException
	 */
	public static String getAttributeValue(Attribute attribute) {
		try {
			String attrValue = (String) attribute.get();
			return attrValue;
		} catch (NamingException e) {
			log.error("NamingException when trying to get attribute value for attribute::" + attribute, e);
			return null;
		}
	}

	
	/**
	 * Checks if Collection of naming Attributes contain defined required properties for OLAT
	 * 
	 * 	 * Configuration: LDAP Required Map = olatextconfig.xml (property=reqAttrs)
	 * 
	 * @param attributes Collection of LDAP Naming Attribute 
	 * @return null If all required Attributes are found, otherwise String[] of missing Attributes
	 * 
	 */
	public static String[] checkReqAttr(Attributes attrs) {
		Map<String, String> reqAttr = LDAPLoginModule.getReqAttrs();
		String[] missingAttr = new String[reqAttr.size()];
		int y = 0;
		Iterator<String> reqItr = reqAttr.keySet().iterator();
		while (reqItr.hasNext()) {
			String attKey = reqItr.next().trim();
			if (attrs.get(attKey) == null) {
				missingAttr[y++] = attKey;
			}
		}
		if (y == 0) return null;
		else return missingAttr;
	}
	
	/**
	 * Checks if defined OLAT Properties in olatextconfig.xml exist in OLAT.
	 * 
	 * 	 Configuration: LDAP Attributes Map = olatextconfig.xml (property=reqAttrs, property=userAttributeMapper)
	 * 
	 * @param attrs Map of OLAT Properties from of the LDAP configuration 
	 * @return true All exist OK, false Error
	 * 
	 */
	public static boolean checkIfOlatPropertiesExists(Map<String, String> attrs) {
		List<UserPropertyHandler> upHandler = UserManager.getInstance().getAllUserPropertyHandlers();
		for (String ldapAttribute : attrs.keySet()) {
			boolean propertyExists = false;
			String olatProperty = attrs.get(ldapAttribute);
			if (olatProperty.equals(LDAPConstants.LDAP_USER_IDENTIFYER)) {
				// LDAP user identifyer is not a user propery, it's the username
				continue;
			}
			for (UserPropertyHandler userPropItr : upHandler) {
				if (olatProperty.equals(userPropItr.getName())) {
					// ok, this property exist, continue with next one
					propertyExists = true;
					break;
				}
			}
			if ( ! propertyExists ) {
				log
						.error("Error in checkIfOlatPropertiesExists(): configured LDAP attribute::"
								+ ldapAttribute
								+ " configured to map to OLAT user property::"
								+ olatProperty
								+ " but no such user property configured in olat_userconfig.xml");
				return false;				
			}
		}
		return true;
	}

	/**
	 * Checks if defined Static OLAT Property in olatextconfig.xml exist in OLAT.
	 * 
	 * 	 Configuration: olatextconfig.xml (property=staticUserProperties)
	 * 
	 * @param olatProperties Set of OLAT Properties from of the LDAP configuration 
	 * @return true All exist OK, false Error
	 * 
	 */
	public static boolean checkIfStaticOlatPropertiesExists(Set<String> olatProperties) {
		List<UserPropertyHandler> upHandler = UserManager.getInstance().getAllUserPropertyHandlers();
		for (String olatProperty : olatProperties) {
			boolean propertyExists = false;
			for (UserPropertyHandler userPropItr : upHandler) {
				if (olatProperty.equals(userPropItr.getName())) {
					// ok, this property exist, continue with next one
					propertyExists = true;
					break;
				}
			}
			if ( ! propertyExists ) {
				log
				.error("Error in checkIfStaticOlatPropertiesExists(): configured static OLAT user property::"
						+ olatProperty
						+ " is not configured in olat_userconfig.xml");
				return false;				
			}			
		}
		return true;
	}
	
	/**
	 * Checks if SSL certification is know and accepted by Java JRE.
	 * 
	 * 
	 * @param dayFromNow Checks expiration 
	 * @return true Certification accepted, false No valid certification
	 * 
	 * @throws Exception
	 * 
	 */
	public static boolean checkServerCertValidity(int daysFromNow) {
		KeyStore keyStore;
			try {
				keyStore = KeyStore.getInstance(LDAPLoginModule.getTrustStoreType());
				keyStore.load(new FileInputStream(LDAPLoginModule.getTrustStoreLocation()), (LDAPLoginModule.getTrustStorePwd() != null) ? LDAPLoginModule.getTrustStorePwd().toCharArray() : null);
				Enumeration aliases = keyStore.aliases();
				while (aliases.hasMoreElements()) {
					String alias = (String) aliases.nextElement();
					Certificate cert = keyStore.getCertificate(alias);
					if (cert instanceof X509Certificate) {
						return isCertificateValid((X509Certificate)cert, daysFromNow);
					}
				}
			}	catch (Exception e) {
				return false;
			}
			return false;
		}
	
	private static boolean isCertificateValid(X509Certificate x509Cert, int daysFromNow) {
		try {
			x509Cert.checkValidity();
			if (daysFromNow > 0) {
				Date nowPlusDays = new Date(System.currentTimeMillis() + (new Long(daysFromNow).longValue() * 24l * 60l * 60l * 1000l));
				x509Cert.checkValidity(nowPlusDays);
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
