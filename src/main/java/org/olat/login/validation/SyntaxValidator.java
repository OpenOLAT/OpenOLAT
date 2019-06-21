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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 12 May 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SyntaxValidator {

	private final List<ValidationRule> rules;
	private final boolean validateAll;

	public SyntaxValidator(ValidationRulesProvider provider, boolean validateAll) {
		this.rules = provider.getRules();
		this.validateAll = validateAll;
	}
	
	public List<ValidationDescription> getAllDescriptions() {
		return getAllDescriptions(false);
	}
	
	public List<ValidationDescription> getAllDescriptions(boolean ignoreIdentityValidations) {
		return rules.stream()
				.filter(identityRuleFilter(ignoreIdentityValidations))
				.map(ValidationRule::getDescription)
				.collect(Collectors.toList());
	}

	private Predicate<? super ValidationRule> identityRuleFilter(boolean ignoreIdentityRules) {
		return rule -> ignoreIdentityRules? !rule.isIdentityRule(): true;
	}

	public ValidationResult validate(String value) {
		ValidationResultImpl result = new ValidationResultImpl();
		for (ValidationRule rule : rules) {
			if (!rule.isIdentityRule()) {
				if (!rule.validate(value, null)) {
					result.addInvalidRule(rule.getDescription());
					if (!validateAll) {
						return result;
					}
				}
			}
		}
		return result;
	}

	public ValidationResult validate(String value, Identity identity) {
		ValidationResultImpl result = new ValidationResultImpl();
		for (ValidationRule rule : rules) {
			if (!rule.validate(value, identity)) {
				result.addInvalidRule(rule.getDescription());
				if (!validateAll) {
					return result;
				}
			}
		}
		return result;
	}
	
	private static class ValidationResultImpl implements ValidationResult {
		
		private final List<ValidationDescription> invalidDescription = new ArrayList<>();

		@Override
		public boolean isValid() {
			return invalidDescription.isEmpty();
		}

		@Override
		public List<ValidationDescription> getInvalidDescriptions() {
			return invalidDescription;
		}

		public void addInvalidRule(ValidationDescription description) {
			invalidDescription.add(description);
		}

	}

}
