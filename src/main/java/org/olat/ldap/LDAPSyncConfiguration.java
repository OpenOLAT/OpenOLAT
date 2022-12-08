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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.naming.directory.Attributes;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Hold all configurations to use a LDAP server to sync
 * users with OpenOLAT.<br />
 * 
 * Initial date: 24.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LDAPSyncConfiguration {
	
	private static final Logger log = Tracing.createLoggerFor(LDAPSyncConfiguration.class);
	
	private String ldapUserFilter;
	private String ldapGroupFilter;
	private String ldapOrganisationsGroupFilter;
	
	private String ldapOrganisationManagedFlag;
	
	private List<String> ldapBases;
	private List<String> ldapGroupBases;
	private List<String> ldapOrganisationsGroupBases;
	
	private String ldapUserCreatedTimestampAttribute;
	private String ldapUserLastModifiedTimestampAttribute;
	private String ldapUserPasswordAttribute;
	private String ldapUserLoginAttribute;
	
	private String[] userAttributes;
	
	private String coachRoleAttribute;
	private String coachRoleValue;
	
	private String groupAttribute;
	private String groupAttributeSeparator;

	private String coachedGroupAttribute;
	private String coachedGroupAttributeSeparator;
	
	private Map<String, String> requestAttributes;
	private Map<String, String> userAttributeMap;
	private Set<String> syncOnlyOnCreateProperties;

	private List<String> authorsGroupBase;
	private String authorRoleAttribute;
	private String authorRoleValue;
	
	private List<String> userManagersGroupBase;
	private String userManagerRoleAttribute;
	private String userManagerRoleValue;
	
	private List<String> groupManagersGroupBase;
	private String groupManagerRoleAttribute;
	private String groupManagerRoleValue;
	
	private List<String> qpoolManagersGroupBase;
	private String qpoolManagerRoleAttribute;
	private String qpoolManagerRoleValue;
	
	private List<String> curriculumManagersGroupBase;
	private String curriculumManagerRoleAttribute;
	private String curriculumManagerRoleValue;
	
	private List<String> learningResourceManagersGroupBase;
	private String learningResourceManagerRoleAttribute;
	private String learningResourceManagerRoleValue;
	
	private String groupCoachAsParticipant;
	
	/**
	 * Static user properties that should be added to user when syncing
	 */ 
	private Map<String, String> staticUserProperties;
	
	private UserManager userManager;
	
	/**
	 * [used by Spring]
	 * @param userManager
	 */
	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}

	public String getLdapUserCreatedTimestampAttribute() {
		return ldapUserCreatedTimestampAttribute;
	}
	
	public List<String> getLdapBases() {
		return ldapBases;
	}

	public void setLdapBases(List<String> bases) {
		ldapBases = toList(bases);
	}
	
	public List<String> getLdapGroupBases() {
		return ldapGroupBases;
	}
	
	public void setLdapGroupBases(List<String> bases) {
		ldapGroupBases = toList(bases);
	}
	
	public List<String> getLdapOrganisationsGroupBases() {
		return ldapOrganisationsGroupBases;
	}
	
	public void setLdapOrganisationsGroupBases(List<String> bases) {
		ldapOrganisationsGroupBases = toList(bases);
	}
	
	private List<String> toList(List<String> list) {
		List<String> listToUse = new ArrayList<>();
		if (list != null) {
			for (String entry : list) {
				if (StringHelper.containsNonWhitespace(entry) && entry.contains("!#")) {
					String[] oneLineList = entry.split("!#");
					for (String oneLineEntry : oneLineList) {
						if (StringHelper.containsNonWhitespace(oneLineEntry)) {
							listToUse.add(oneLineEntry.trim());
						}
					}
				} else if (StringHelper.containsNonWhitespace(entry)) {
					listToUse.add(entry.trim());
				}
			}
		}
		return listToUse;
	}
	
	/**
	 * @return A filter expression enclosed in () brackets to filter for valid users or NULL for no filtering
	 */
	public String getLdapUserFilter() {
		return ldapUserFilter;
	}

	public void setLdapUserFilter(String filter) {
		if (StringHelper.containsNonWhitespace(filter)) {
			ldapUserFilter = filter.trim();			
		} else {
			// set explicitly to null for no filter
			ldapUserFilter = null;
		}
	}

	public String getLdapGroupFilter() {
		return ldapGroupFilter;
	}

	public void setLdapGroupFilter(String filter) {
		if (StringHelper.containsNonWhitespace(filter)) {
			ldapGroupFilter = filter.trim();			
		} else {
			ldapGroupFilter = null;
		}
	}
	
	public String getLdapOrganisationsGroupFilter() {
		return ldapOrganisationsGroupFilter;
	}

	public void setLdapOrganisationsGroupFilter(String filter) {
		if(StringHelper.containsNonWhitespace(filter)) {
			ldapOrganisationsGroupFilter = filter.trim();
		} else {
			ldapOrganisationsGroupFilter = null;
		}
	}

	public OrganisationManagement getLdapOrganisationManagedFlag() {
		boolean hasOrganisationGroups = ldapOrganisationsGroupBases != null && !ldapOrganisationsGroupBases.isEmpty();
		if(!hasOrganisationGroups) {
			return OrganisationManagement.none;
		}
		if("default".equalsIgnoreCase(ldapOrganisationManagedFlag)) {
			return OrganisationManagement.def;
		}
		return OrganisationManagement.managed;
	}

	public void setLdapOrganisationManagedFlag(String ldapOrganisationManagedFlag) {
		this.ldapOrganisationManagedFlag = ldapOrganisationManagedFlag;
	}

	public boolean syncGroupWithLDAPGroup() {
		return ldapGroupBases != null && !ldapGroupBases.isEmpty();
	}
	
	public boolean syncGroupWithAttribute() {
		return StringHelper.containsNonWhitespace(groupAttribute) || StringHelper.containsNonWhitespace(coachedGroupAttribute);
	}

	public String getCoachRoleAttribute() {
		return coachRoleAttribute;
	}

	public void setCoachRoleAttribute(String attribute) {
		this.coachRoleAttribute = attribute;
	}

	public String getCoachRoleValue() {
		return coachRoleValue;
	}

	public void setCoachRoleValue(String coachRoleValue) {
		this.coachRoleValue = coachRoleValue;
	}

	public String getGroupAttribute() {
		return groupAttribute;
	}

	public void setGroupAttribute(String attribute) {
		this.groupAttribute = attribute;
	}

	public String getGroupAttributeSeparator() {
		return groupAttributeSeparator;
	}

	public void setGroupAttributeSeparator(String groupAttributeSeparator) {
		this.groupAttributeSeparator = groupAttributeSeparator;
	}

	public String getCoachedGroupAttribute() {
		return coachedGroupAttribute;
	}

	public void setCoachedGroupAttribute(String coachedGroupAttribute) {
		this.coachedGroupAttribute = coachedGroupAttribute;
	}

	public String getCoachedGroupAttributeSeparator() {
		return coachedGroupAttributeSeparator;
	}

	public void setCoachedGroupAttributeSeparator(String coachedGroupAttributeSeparator) {
		this.coachedGroupAttributeSeparator = coachedGroupAttributeSeparator;
	}

	public List<String> getAuthorsGroupBase() {
		return authorsGroupBase;
	}

	public void setAuthorsGroupBase(List<String> bases) {
		authorsGroupBase = toList(bases);
	}

	public String getAuthorRoleAttribute() {
		return authorRoleAttribute;
	}

	public void setAuthorRoleAttribute(String attribute) {
		this.authorRoleAttribute = attribute;
	}

	public String getAuthorRoleValue() {
		return authorRoleValue;
	}

	public void setAuthorRoleValue(String value) {
		this.authorRoleValue = value;
	}
	
	public boolean isGroupCoachParticipant() {
		return "true".equals(groupCoachAsParticipant);
	}

	public String getGroupCoachAsParticipant() {
		return groupCoachAsParticipant;
	}

	public void setGroupCoachAsParticipant(String groupCoachAsParticipant) {
		this.groupCoachAsParticipant = groupCoachAsParticipant;
	}

	public List<String> getUserManagersGroupBase() {
		return userManagersGroupBase;
	}

	public void setUserManagersGroupBase(List<String> bases) {
		userManagersGroupBase = toList(bases);
	}

	public String getUserManagerRoleAttribute() {
		return userManagerRoleAttribute;
	}

	public void setUserManagerRoleAttribute(String attribute) {
		userManagerRoleAttribute = attribute;
	}

	public String getUserManagerRoleValue() {
		return userManagerRoleValue;
	}

	public void setUserManagerRoleValue(String value) {
		userManagerRoleValue = value;
	}

	public List<String> getGroupManagersGroupBase() {
		return groupManagersGroupBase;
	}

	public void setGroupManagersGroupBase(List<String> bases) {
		this.groupManagersGroupBase = toList(bases);
	}

	public String getGroupManagerRoleAttribute() {
		return groupManagerRoleAttribute;
	}

	public void setGroupManagerRoleAttribute(String attribute) {
		this.groupManagerRoleAttribute = attribute;
	}

	public String getGroupManagerRoleValue() {
		return groupManagerRoleValue;
	}

	public void setGroupManagerRoleValue(String value) {
		this.groupManagerRoleValue = value;
	}

	public List<String> getQpoolManagersGroupBase() {
		return qpoolManagersGroupBase;
	}

	public void setQpoolManagersGroupBase(List<String> bases) {
		this.qpoolManagersGroupBase = toList(bases);
	}

	public String getQpoolManagerRoleAttribute() {
		return qpoolManagerRoleAttribute;
	}

	public void setQpoolManagerRoleAttribute(String attribute) {
		this.qpoolManagerRoleAttribute = attribute;
	}

	public String getQpoolManagerRoleValue() {
		return qpoolManagerRoleValue;
	}

	public void setQpoolManagerRoleValue(String value) {
		this.qpoolManagerRoleValue = value;
	}
	
	public List<String> getCurriculumManagersGroupBase() {
		return curriculumManagersGroupBase;
	}

	public void setCurriculumManagersGroupBase(List<String> bases) {
		this.curriculumManagersGroupBase = toList(bases);
	}

	public String getCurriculumManagerRoleAttribute() {
		return curriculumManagerRoleAttribute;
	}

	public void setCurriculumManagerRoleAttribute(String attribute) {
		this.curriculumManagerRoleAttribute = attribute;
	}

	public String getCurriculumManagerRoleValue() {
		return curriculumManagerRoleValue;
	}

	public void setCurriculumManagerRoleValue(String value) {
		this.curriculumManagerRoleValue = value;
	}

	public List<String> getLearningResourceManagersGroupBase() {
		return learningResourceManagersGroupBase;
	}

	public void setLearningResourceManagersGroupBase(List<String> bases) {
		this.learningResourceManagersGroupBase = toList(bases);
	}

	public String getLearningResourceManagerRoleAttribute() {
		return learningResourceManagerRoleAttribute;
	}

	public void setLearningResourceManagerRoleAttribute(String attribute) {
		this.learningResourceManagerRoleAttribute = attribute;
	}

	public String getLearningResourceManagerRoleValue() {
		return learningResourceManagerRoleValue;
	}

	public void setLearningResourceManagerRoleValue(String attribute) {
		this.learningResourceManagerRoleValue = attribute;
	}
	
	public boolean syncOrganisationWithLDAPGroup() {
		return ldapOrganisationsGroupBases != null && !ldapGroupBases.isEmpty();
	}
	
	public List<OrganisationRoles> getSynchronizedRoles() {
		List<OrganisationRoles> roles = new ArrayList<>(12);
		if(StringHelper.containsNonWhitespace(authorRoleAttribute)
				|| (authorsGroupBase != null && !authorsGroupBase.isEmpty())) {
			roles.add(OrganisationRoles.author);
		}
		if(StringHelper.containsNonWhitespace(userManagerRoleAttribute)
				|| (userManagersGroupBase != null && !userManagersGroupBase.isEmpty())) {
			roles.add(OrganisationRoles.usermanager);
		}
		if(StringHelper.containsNonWhitespace(groupManagerRoleAttribute)
				|| (groupManagersGroupBase != null && !groupManagersGroupBase.isEmpty())) {
			roles.add(OrganisationRoles.groupmanager);
		}
		if(StringHelper.containsNonWhitespace(qpoolManagerRoleAttribute)
				|| (qpoolManagersGroupBase != null && !qpoolManagersGroupBase.isEmpty())) {
			roles.add(OrganisationRoles.poolmanager);
		}
		if(StringHelper.containsNonWhitespace(curriculumManagerRoleAttribute)
				|| (curriculumManagersGroupBase != null && !curriculumManagersGroupBase.isEmpty())) {
			roles.add(OrganisationRoles.curriculummanager);
		}
		if(StringHelper.containsNonWhitespace(learningResourceManagerRoleAttribute)
				|| (learningResourceManagersGroupBase != null && !learningResourceManagersGroupBase.isEmpty())) {
			roles.add(OrganisationRoles.learnresourcemanager);
		}
		return roles;
	}


	public void setLdapUserCreatedTimestampAttribute(String attribute) {
		this.ldapUserCreatedTimestampAttribute = attribute;
	}
	
	public String getLdapUserLastModifiedTimestampAttribute() {
		return ldapUserLastModifiedTimestampAttribute;
	}
	
	public void setLdapUserLastModifiedTimestampAttribute(String attribute) {
		this.ldapUserLastModifiedTimestampAttribute = attribute;
	}
	
	public String getLdapUserPasswordAttribute() {
		return ldapUserPasswordAttribute;
	}
	
	public void setLdapUserPasswordAttribute(String attribute) {
		this.ldapUserPasswordAttribute = attribute;
	}

	public List<String> getLdapUserLoginAttributes() {
		if(StringHelper.containsNonWhitespace(ldapUserLoginAttribute)) {
			String[] attrs = ldapUserLoginAttribute.split("[,]");
			return List.of(attrs);
		}
		return List.of();
	}
	
	/**
	 * @return One or several attributes comma separated
	 */
	public String getLdapUserLoginAttribute() {
		return ldapUserLoginAttribute;
	}

	/**
	 * @param ldapUserLoginAttribute One or several attributes comma separated
	 */
	public void setLdapUserLoginAttribute(String ldapUserLoginAttribute) {
		this.ldapUserLoginAttribute = ldapUserLoginAttribute;
	}

	public Map<String, String> getRequestAttributes() {
		return requestAttributes;
	}

	public void setRequestAttributes(Map<String, String> mapping) {
		requestAttributes = new HashMap<>();
		for (Map.Entry<String, String>  entry : mapping.entrySet()) {
			requestAttributes.put(entry.getKey().trim(), entry.getValue().trim());
		}	
	}
	
	public String[] getUserAttributes() {
		return userAttributes;
	}

	public Map<String, String> getUserAttributeMap() {
		return userAttributeMap;
	}

	public void setUserAttributeMap(Map<String, String> mapping) {
		userAttributeMap = new HashMap<>();
		for (Entry<String, String>  entry : mapping.entrySet()) {
			String ldapAttrib = entry.getKey();
			String olatProp = entry.getValue();
			if (StringHelper.containsNonWhitespace(ldapAttrib) && StringHelper.containsNonWhitespace(olatProp)){
				userAttributeMap.put(ldapAttrib.trim(), olatProp.trim());
			}
		}		
		// optimizes for later usage
		userAttributes = userAttributeMap.keySet().toArray(new String[userAttributeMap.size()]);
	}

	public Map<String, String> getStaticUserProperties() {
		return staticUserProperties;
	}

	public void setStaticUserProperties(Map<String, String> mapping) {
		staticUserProperties = new HashMap<>();
		for (Map.Entry<String, String>  entry : mapping.entrySet()) {
			String olatPropKey = entry.getKey();
			String staticValue = entry.getValue();
			if (StringHelper.containsNonWhitespace(olatPropKey) && StringHelper.containsNonWhitespace(staticValue)){
				staticUserProperties.put(olatPropKey.trim(), staticValue.trim());
			}
		}
	}

	public Set<String> getSyncOnlyOnCreateProperties() {
		return syncOnlyOnCreateProperties;
	}

	public void setSyncOnlyOnCreateProperties(Set<String> properties) {
		syncOnlyOnCreateProperties = new HashSet<>();
		for (String property : properties) {
			if (StringHelper.containsNonWhitespace(property)){
				syncOnlyOnCreateProperties.add(property.trim());
			}
		}
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
	protected boolean checkIfOlatPropertiesExists(Map<String, String> attrs) {
		List<UserPropertyHandler> upHandler = userManager.getAllUserPropertyHandlers();
		for (Map.Entry<String, String> attributes : attrs.entrySet()) {
			String ldapAttribute = attributes.getKey();
			String olatProperty = attributes.getValue();
			if (olatProperty.equals(LDAPConstants.LDAP_USER_IDENTIFYER)) {
				// LDAP user identifyer is not a user propery, it's the username
				continue;
			}
			
			boolean propertyExists = false;
			for (UserPropertyHandler userPropItr : upHandler) {
				if (olatProperty.equals(userPropItr.getName())) {
					// ok, this property exist, continue with next one
					propertyExists = true;
					break;
				}
			}
			if (!propertyExists) {
				log.error("Error in checkIfOlatPropertiesExists(): configured LDAP attribute::{} configured to map to OLAT user property::{} but no such user property configured in olat_userconfig.xml",
						ldapAttribute, olatProperty);
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
	protected boolean checkIfStaticOlatPropertiesExists(Set<String> olatProperties) {
		List<UserPropertyHandler> upHandler = userManager.getAllUserPropertyHandlers();
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
				log.error("Error in checkIfStaticOlatPropertiesExists(): configured static OLAT user property::{} is not configured in olat_userconfig.xml", olatProperty);
				return false;				
			}			
		}
		return true;
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
	public List<String> checkRequestAttributes(Attributes attrs, boolean emailMandatory) {
		Map<String, String> reqAttrMap = getRequestAttributes();
		List<String> missingAttrList = new ArrayList<>();
		for (String attKey : reqAttrMap.keySet()) {
			attKey = attKey.trim();
			boolean missing = attrs.get(attKey) == null;
			boolean optional = !emailMandatory && "email".equals(userAttributeMap.get(attKey));
			if (missing && !optional) {
				missingAttrList.add(attKey);
			}
		}
		return missingAttrList;
	}
	
	/**
	 * Maps OLAT Property to the LDAP Attributes 
	 * 
	 * Configuration: LDAP Attributes Map = ldapContext.xml (property=userAttrs)
	 * 
	 * @param olatProperty OLAT Property attribute ID 
	 * @return LDAP Attribute
	 */
	public String getOlatPropertyToLdapAttribute(String olatProperty) {
		Map<String, String> userAttrMapper = getRequestAttributes();
		if (userAttrMapper.containsValue(olatProperty)) {
			Iterator<String> itr = userAttrMapper.keySet().iterator();
			while (itr.hasNext()) {
				String key = itr.next();
				if (userAttrMapper.get(key).compareTo(olatProperty) == 0) return key;
			}
		}
		return null;
	}
	
	public enum OrganisationManagement {
		none,
		def,
		managed
	}
}
