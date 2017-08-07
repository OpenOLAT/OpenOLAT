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

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.olat.core.id.User;
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

	private Map<String, String> shibbolethMap;

	@Autowired
	private ShibbolethModule shibbolethModule;
	@Autowired
	private ShibbolethAttributeHandlerFactory shibbolethAttributeHandlerFactory;

	public void setAttributesMap(Map<String, String> attributesMap) {
		this.shibbolethMap = attributesMap;
	}

	public String getValueForAttributeName(String attributeName) {
		String attributeValue = shibbolethMap.get(attributeName);
		ShibbolethAttributeHandler handler = getAttributeHandler(attributeName);
		return handler.parse(attributeValue);
	}

	private ShibbolethAttributeHandler getAttributeHandler(String attributeName) {
		Map<String, String> handlerNames = shibbolethModule.getAttributeHandlerNames();
		String handlerName = handlerNames.get(attributeName);
		return shibbolethAttributeHandlerFactory.getHandler(handlerName);
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

	public User syncUser(User user) {
		for (Entry<String, String> mapping : getUserMappingEntrySet()) {
			String shibbolethValue = getValueForAttributeName(mapping.getKey());
			String userPropertyName = mapping.getValue();
			user.setProperty(userPropertyName, shibbolethValue);
		}
		return user;
	}

	public boolean hasDifference(User user) {
		for (Entry<String, String> mapping : getUserMappingEntrySet()) {
			String shibbolethValue = getValueForAttributeName(mapping.getKey());
			String userValue = user.getProperty(mapping.getValue(), null);
	        if (hasDifference(shibbolethValue, userValue)) {
	            return true;
	        }
	    }
		return false;
	}

	private boolean hasDifference(String shibbolethValue, String userValue) {
		return (shibbolethValue == null && userValue != null)
			|| (shibbolethValue != null && userValue == null)
			|| (shibbolethValue != null && userValue != null && !shibbolethValue.equals(userValue));
	}

	private Set<Entry<String, String>> getUserMappingEntrySet() {
		return shibbolethModule.getUserMapping().entrySet();
	}

}
