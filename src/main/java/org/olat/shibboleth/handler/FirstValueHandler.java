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
package org.olat.shibboleth.handler;

import org.olat.core.util.StringHelper;
import org.olat.shibboleth.ShibbolethModule;
import org.springframework.stereotype.Component;

/**
 * A Shibboleth attribute may contain multiple values delimited by ";". This
 * handler splits the Shibboleth attribute in its separate values and returns
 * the first value.
 *
 * Initial date: 21.07.2017<br>
 *
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component("FirstValue")
class FirstValueHandler implements ShibbolethAttributeHandler {

	@Override
	public String parse(String shibbolethAttributeValue) {
		if (shibbolethAttributeValue != null) {
			String[] values = shibbolethAttributeValue.split(ShibbolethModule.MULTIVALUE_SEPARATOR);
			// Return the first not empty value
			for (String value: values) {
				if (StringHelper.containsNonWhitespace(value)) {
					return value;
				}
			}
		}
		return null;
	}

}
