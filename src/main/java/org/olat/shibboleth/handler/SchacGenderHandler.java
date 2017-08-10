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

import org.springframework.stereotype.Component;

/**
 * Handles the values from SchacGender (http://macedir.org/ontologies/attribute/2012-11-10/attributeOntologyDoc/schacgender.html)
 *
 * Initial date: 21.07.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component("SchacGender")
class SchacGenderHandler implements ShibbolethAttributeHandler {

	private static final String GNEDER_DEFAULT = "-";

	@Override
	public String parse(String shibbolethAttributeValue) {
		if (shibbolethAttributeValue == null) return GNEDER_DEFAULT;

		switch(shibbolethAttributeValue) {
			case "1":
				return "male";
			case "2":
				return "female";
			default:
				return GNEDER_DEFAULT;
		}
	}

}
