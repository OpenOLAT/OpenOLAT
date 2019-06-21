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

import java.util.function.Predicate;

import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 13 May 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class AtLeastCharacterRule extends DescriptionRule {

	private final Predicate<Character> filter;
	private final int minCount;

	AtLeastCharacterRule(ValidationDescription description, Predicate<Character> filter, int minCount) {
		super(description);
		this.filter = filter;
		this.minCount = minCount;
	}

	@Override
	public boolean validate(String value, Identity identity) {
		int count = 0;
		for (int i = 0; i < value.length(); i++) {
			char character = value.charAt(i);
			if (filter.test(character)) {
				count++;
				if (count >= minCount) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean isIdentityRule() {
		return false;
	}

}
