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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.olat.login.validation.TestingRuleProvider.of;

import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 13 May 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class PasswordSyntaxValidatorTest {

	@Test
	public void shouldValidateTrue() {
		ValidationRule rule1 = new BooleanRule(true);
		ValidationRule rule2 = new BooleanRule(true);
		SyntaxValidator sut = new SyntaxValidator(of(rule1, rule2), true);
		
		ValidationResult result = sut.validate("123", null);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(result.isValid()).isTrue();
		softly.assertThat(result.getInvalidDescriptions()).isEmpty();
		softly.assertAll();
	}
	
	@Test
	public void shouldValidateFalse() {
		ValidationRule rule1 = new BooleanRule(true);
		ValidationRule rule2 = new BooleanRule(false);
		SyntaxValidator sut = new SyntaxValidator(of(rule1, rule2), true);
		
		ValidationResult result = sut.validate("123", null);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(result.isValid()).isFalse();
		softly.assertThat(result.getInvalidDescriptions()).hasSize(1);
		softly.assertAll();
	}
	
	@Test
	public void shouldValidateAll() {
		ValidationRule rule1 = new BooleanRule(false);
		ValidationRule rule2 = new BooleanRule(false);
		SyntaxValidator sut = new SyntaxValidator(of(rule1, rule2), true);
		
		ValidationResult result = sut.validate("123", null);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(result.isValid()).isFalse();
		softly.assertThat(result.getInvalidDescriptions()).hasSize(2);
		softly.assertAll();
	}
	
	@Test
	public void shouldBreakOnFirsInvalidRule() {
		ValidationRule rule1 = new BooleanRule(false);
		ValidationRule rule2 = new BooleanRule(false);
		SyntaxValidator sut = new SyntaxValidator(of(rule1, rule2), false);
		
		ValidationResult result = sut.validate("123", null);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(result.isValid()).isFalse();
		softly.assertThat(result.getInvalidDescriptions()).hasSize(1);
		softly.assertAll();
	}

	
	@Test
	public void shouldGetAllDescriptions() {
		ValidationRule rule1 = new BooleanRule(true);
		ValidationRule rule2 = new BooleanRule(true);
		SyntaxValidator sut = new SyntaxValidator(of(rule1, rule2), true);
		
		List<ValidationDescription> allDescriptions = sut.getAllDescriptions();
		
		assertThat(allDescriptions).hasSize(2);
	}
	
	
	private static class BooleanRule implements ValidationRule {
		
		private final boolean valid;

		private BooleanRule(boolean valid) {
			this.valid = valid;
		}

		@Override
		public boolean validate(String value, Identity identity) {
			return valid;
		}

		@Override
		public ValidationDescription getDescription() {
			return mock(ValidationDescription.class);
		}

		@Override
		public boolean isIdentityRule() {
			return false;
		}

	}

}
