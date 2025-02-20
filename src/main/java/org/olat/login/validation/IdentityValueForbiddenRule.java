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
package org.olat.login.validation;

import java.util.List;
import java.util.function.Function;

import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 14 May 2019<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
class IdentityValueForbiddenRule extends DescriptionRule {

	private final Function<Identity, List<String>> forbiddenValues;

	IdentityValueForbiddenRule(ValidationDescription description, Function<Identity, List<String>> forbiddenValues) {
		super(description);
		this.forbiddenValues = forbiddenValues;
	}

	@Override
	public boolean validate(String value, Identity identity) {
		if (identity == null) return true;

		List<String> identityValues = forbiddenValues.apply(identity);
		if (identityValues == null || identityValues.isEmpty()) return true;

		// Check if the password contains any of the forbidden values
		for (String forbiddenValue : identityValues) {
			if (StringHelper.containsNonWhitespace(forbiddenValue) &&
					value.toLowerCase().contains(forbiddenValue.toLowerCase())) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean isIdentityRule() {
		return true;
	}

}
