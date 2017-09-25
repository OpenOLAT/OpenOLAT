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
package org.olat.shibboleth.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.olat.core.id.User;
import org.olat.core.util.StringHelper;
import org.olat.shibboleth.ShibbolethModule;
import org.olat.shibboleth.handler.ShibbolethAttributeHandler;
import org.olat.shibboleth.handler.ShibbolethAttributeHandlerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * Initial date: 06.08.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
@Scope("prototype")
public class ShibbolethAttributes {

	private Map<String, String> shibbolethMap = new HashMap<>(0);

	@Autowired
	private ShibbolethModule shibbolethModule;
	@Autowired
	private ShibbolethAttributeHandlerFactory shibbolethAttributeHandlerFactory;
	@Autowired
	private DifferenceChecker differenceChecker;

	public void init(Map<String, String> attributes) {
		Collection<String> shibbolethAttributeNames = shibbolethModule.getShibbolethAttributeNames();
		shibbolethMap = new HashMap<>(shibbolethAttributeNames.size());

		for (String attributeName : shibbolethAttributeNames) {
			String attributeValue = attributes.get(attributeName);
			ShibbolethAttributeHandler handler = getAttributeHandler(attributeName);
			String parsedValue = handler.parse(attributeValue);
			shibbolethMap.put(attributeName, parsedValue);
		}
	}

	private ShibbolethAttributeHandler getAttributeHandler(String attributeName) {
		Map<String, String> handlerNames = shibbolethModule.getAttributeHandlerNames();
		String handlerName = handlerNames.get(attributeName);
		return shibbolethAttributeHandlerFactory.getHandler(handlerName);
	}

	public String getUID() {
		String uidAttributeName = shibbolethModule.getUIDAttributeName();
		return getValueForAttributeName(uidAttributeName);

	}

	public String getPreferredLanguage() {
		String langAttributeName = shibbolethModule.getPreferredLanguageAttributeName();
		return getValueForAttributeName(langAttributeName);
	}

	public String getAcRawValues() {
		String langAttributeName = shibbolethModule.getAcAutoAttributeName();
		return getValueForAttributeName(langAttributeName);
	}

	public String getValueForAttributeName(String attributeName) {
		return shibbolethMap.get(attributeName);
	}

	public void setValueForUserPropertyName(String propertyName, String value) {
		String attributeName = getShibbolethAttributeName(propertyName);
		if (StringHelper.containsNonWhitespace(attributeName)) {
			shibbolethMap.put(attributeName, value);
		}
	}

	public String getValueForUserPropertyName(String propertyName) {
		String attributeName = getShibbolethAttributeName(propertyName);
		return getValueForAttributeName(attributeName);
	}

	private String getShibbolethAttributeName(String userProperty) {
		if (userProperty == null) return null;

		for (Entry<String, String> mapping : getUserMappingEntrySet()) {
	        if (userProperty.equals(mapping.getValue())) {
	            return mapping.getKey();
	        }
	    }
	    return null;
	}

	public Map<String, String> toMap() {
		return new HashMap<>(shibbolethMap);
	}

	public boolean isAuthor() {
		try {
			String attributeValue = getValueForAttributeName(shibbolethModule.getAuthorMappingAttributeName());
			for (String isContained : shibbolethModule.getAuthorMappingContains()) {
				if (attributeValue.contains(isContained)) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	public User syncUser(User user) {
		for (Entry<String, String> mapping : getUserMappingEntrySet()) {
			String shibbolethName = mapping.getKey();
			String shibbolethValue = getValueForAttributeName(shibbolethName);
			String userValue = user.getProperty(mapping.getValue(), null);
			String userPropertyName = mapping.getValue();
			if (differenceChecker.isDifferent(shibbolethName, shibbolethValue, userValue)) {
				user.setProperty(userPropertyName, shibbolethValue);
			}
		}
		return user;
	}

	public boolean hasDifference(User user) {
		for (Entry<String, String> mapping : getUserMappingEntrySet()) {
			String shibbolethName = mapping.getKey();
			String shibbolethValue = getValueForAttributeName(shibbolethName);
			String userValue = user.getProperty(mapping.getValue(), null);
	        if (differenceChecker.isDifferent(shibbolethName, shibbolethValue, userValue)) {
	            return true;
	        }
	    }
		return false;
	}

	private Set<Entry<String, String>> getUserMappingEntrySet() {
		return shibbolethModule.getUserMapping().entrySet();
	}

}
