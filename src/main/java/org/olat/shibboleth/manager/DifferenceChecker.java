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

import org.olat.shibboleth.ShibbolethModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Helper to check if the value of an Shibboleth attribute and a user property
 * are the same or if they are different. The comparison considers the settings
 * in the OpenOLAT configuration.
 *
 * Initial date: 10.08.2017<br>
 *
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
public class DifferenceChecker {

	@Autowired
	private ShibbolethModule shibbolethModule;

	public boolean isDifferent(String shibbolethAttributeName, String shibbolethAttributeValue, String userPropertyValue) {
		return (shibbolethAttributeValue == null && userPropertyValue != null && getDeleteIfNull(shibbolethAttributeName))
			|| (shibbolethAttributeValue != null && userPropertyValue == null)
			|| (shibbolethAttributeValue != null && userPropertyValue != null && !shibbolethAttributeValue.equals(userPropertyValue));
	}

	private boolean getDeleteIfNull(String attributeName) {
		try {
			return shibbolethModule.getDeleteIfNull().get(attributeName);
		} catch (Exception e) {
			return true;
		}
	}

}
