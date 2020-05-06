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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.mockito.Mock;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.User;

/**
 * 
 * Initial date: 12 May 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class PasswordValidationRuleFactoryTest {
	
	@Mock
	private Identity iMock = mock(Identity.class);
	
	PasswordValidationRuleFactory sut = new TestableValidationRuleFactory();

	@Test
	public void shouldCreateVisibleCharactersRule() {
		ValidationRule rule = sut.createVisibleCharactersRule(4, 7);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(rule.validate("12", iMock)).isFalse();
		softly.assertThat(rule.validate("123", iMock)).isFalse();
		softly.assertThat(rule.validate("1234", iMock)).isTrue();
		softly.assertThat(rule.validate("12345", iMock)).isTrue();
		softly.assertThat(rule.validate("123456", iMock)).isTrue();
		softly.assertThat(rule.validate("1234567", iMock)).isTrue();
		softly.assertThat(rule.validate("12345678", iMock)).isFalse();
		softly.assertThat(rule.validate("abc", iMock)).isFalse();
		softly.assertThat(rule.validate("üäö", iMock)).isFalse();
		softly.assertThat(rule.validate("abcdefghi", iMock)).isFalse();
		softly.assertThat(rule.validate("ab  cd", iMock)).isFalse();
		softly.assertThat(rule.validate("abcd e", iMock)).isFalse();
		softly.assertThat(rule.validate("ab\ncd", iMock)).isFalse();
		softly.assertThat(rule.validate("\\u0001\\u0001\\u0001\\u0001\\u0001", iMock)).isFalse();
		softly.assertThat(rule.validate("abcd", iMock)).isTrue();
		softly.assertThat(rule.validate("()%&*", iMock)).isTrue();
		softly.assertThat(rule.validate("abcdö", iMock)).isTrue();
		softly.assertThat(rule.validate("ab__cd", iMock)).isTrue();
		softly.assertThat(rule.validate("я́блоня", iMock)).isTrue();
		softly.assertThat(rule.validate("😀😀😀😀", iMock)).isTrue();
		softly.assertAll();
	}

	@Test
	public void shouldCreateAtLeastLetterRule() {
		ValidationRule rule = sut.createAtLeastLettersRule(4);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(rule.validate("123", iMock)).isFalse();
		softly.assertThat(rule.validate("abc", iMock)).isFalse();
		softly.assertThat(rule.validate("ABC", iMock)).isFalse();
		softly.assertThat(rule.validate("üäö", iMock)).isFalse();
		softly.assertThat(rule.validate("ಮಣೆ", iMock)).isFalse();
		softly.assertThat(rule.validate("1234", iMock)).isFalse();
		softly.assertThat(rule.validate("abc4", iMock)).isFalse();
		softly.assertThat(rule.validate("abc$", iMock)).isFalse();
		softly.assertThat(rule.validate("abc_", iMock)).isFalse();
		softly.assertThat(rule.validate("abc.", iMock)).isFalse();
		softly.assertThat(rule.validate("😀😀😀😀", iMock)).isFalse();
		softly.assertThat(rule.validate("\\u0001\\u0001\\u0001", iMock)).isFalse();
		softly.assertThat(rule.validate("abcd", iMock)).isTrue();
		softly.assertThat(rule.validate("ABCD", iMock)).isTrue();
		softly.assertThat(rule.validate("üäöä", iMock)).isTrue();
		softly.assertThat(rule.validate("я́блоня", iMock)).isTrue();
		softly.assertAll();
	}
	
	@Test
	public void shouldCreateAtLeastLetterUppercaseRule() {
		ValidationRule rule = sut.createAtLeastLettersUppercaseRule(4);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(rule.validate("123", iMock)).isFalse();
		softly.assertThat(rule.validate("abc", iMock)).isFalse();
		softly.assertThat(rule.validate("ABC", iMock)).isFalse();
		softly.assertThat(rule.validate("üäö", iMock)).isFalse();
		softly.assertThat(rule.validate("ಮಣೆ", iMock)).isFalse();
		softly.assertThat(rule.validate("1234", iMock)).isFalse();
		softly.assertThat(rule.validate("ABC4", iMock)).isFalse();
		softly.assertThat(rule.validate("ABC_", iMock)).isFalse();
		softly.assertThat(rule.validate("ABC$", iMock)).isFalse();
		softly.assertThat(rule.validate("ABC.", iMock)).isFalse();
		softly.assertThat(rule.validate("abcd", iMock)).isFalse();
		softly.assertThat(rule.validate("😀😀😀😀", iMock)).isFalse();
		softly.assertThat(rule.validate("ABCD", iMock)).isTrue();
		softly.assertThat(rule.validate("üÜüÜüÜüÜ", iMock)).isTrue();
		softly.assertThat(rule.validate("КГБКГБ", iMock)).isTrue();
		softly.assertAll();
	}

	@Test
	public void shouldCreateAtLeastLetterLowercaseRule() {
		ValidationRule rule = sut.createAtLeastLettersLowercaseRule(4);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(rule.validate("123", iMock)).isFalse();
		softly.assertThat(rule.validate("abc", iMock)).isFalse();
		softly.assertThat(rule.validate("ABC", iMock)).isFalse();
		softly.assertThat(rule.validate("üäö", iMock)).isFalse();
		softly.assertThat(rule.validate("ಮಣೆ", iMock)).isFalse();
		softly.assertThat(rule.validate("1234", iMock)).isFalse();
		softly.assertThat(rule.validate("ABC4", iMock)).isFalse();
		softly.assertThat(rule.validate("ABC_", iMock)).isFalse();
		softly.assertThat(rule.validate("ABC$", iMock)).isFalse();
		softly.assertThat(rule.validate("ABC.", iMock)).isFalse();
		softly.assertThat(rule.validate("ABCD", iMock)).isFalse();
		softly.assertThat(rule.validate("КГБКГБ", iMock)).isFalse();
		softly.assertThat(rule.validate("😀😀😀😀", iMock)).isFalse();
		softly.assertThat(rule.validate("abcd", iMock)).isTrue();
		softly.assertThat(rule.validate("üÜüÜüÜüÜ", iMock)).isTrue();
		softly.assertThat(rule.validate("я́блоня", iMock)).isTrue();
		softly.assertAll();
	}

	@Test
	public void shouldCreateAtLeastDigitsRule() {
		ValidationRule rule = sut.createAtLeastDigitsRule(4);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(rule.validate("123", iMock)).isFalse();
		softly.assertThat(rule.validate("abc123", iMock)).isFalse();
		softly.assertThat(rule.validate("ab_123", iMock)).isFalse();
		softly.assertThat(rule.validate("😀😀😀😀", iMock)).isFalse();
		softly.assertThat(rule.validate("1234", iMock)).isTrue();
		softly.assertThat(rule.validate("abc1234", iMock)).isTrue();
		softly.assertThat(rule.validate("a1b2c3d4e", iMock)).isTrue();
		softly.assertThat(rule.validate("1a2b3c4e5", iMock)).isTrue();
		softly.assertAll();
	}
	
	@Test
	public void shouldCreateAtLeastSpecalSignsRule() {
		ValidationRule rule = sut.createAtLeastSpecialSignsRule(2);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(rule.validate("*", iMock)).isFalse();
		softly.assertThat(rule.validate("123", iMock)).isFalse();
		softly.assertThat(rule.validate("abc", iMock)).isFalse();
		softly.assertThat(rule.validate("äöü", iMock)).isFalse();
		softly.assertThat(rule.validate("я́блоня", iMock)).isFalse();
		softly.assertThat(rule.validate("a+.", iMock)).isTrue();
		softly.assertThat(rule.validate("a__", iMock)).isTrue();
		softly.assertThat(rule.validate("a+b£", iMock)).isTrue();
		softly.assertThat(rule.validate("a+b£c", iMock)).isTrue();
		softly.assertThat(rule.validate("!a+b£c", iMock)).isTrue();
		softly.assertThat(rule.validate("!a+b£c.", iMock)).isTrue();
		softly.assertThat(rule.validate("a$$$$$", iMock)).isTrue();
		softly.assertThat(rule.validate("12?^", iMock)).isTrue();
		softly.assertThat(rule.validate("😀😀😀😀", iMock)).isTrue();
		// Should not be valid, but at least these passwords are invalid in LengthRule.
		softly.assertThat(rule.validate("a   d", iMock)).isTrue();
		softly.assertThat(rule.validate("\\u0001\\u0001\\u0001", iMock)).isTrue();
		softly.assertAll();
	}
	
	@Test
	public void shouldCreateAtLeastDigitsOrSpecalSignsRule() {
		ValidationRule rule = sut.createAtLeastDigitsOrSpecialSignsRule(2);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(rule.validate("*", iMock)).isFalse();
		softly.assertThat(rule.validate("1", iMock)).isFalse();
		softly.assertThat(rule.validate("abc", iMock)).isFalse();
		softly.assertThat(rule.validate("äöü", iMock)).isFalse();
		softly.assertThat(rule.validate("я́блоня", iMock)).isFalse();
		softly.assertThat(rule.validate("4§", iMock)).isTrue();
		softly.assertThat(rule.validate("a1/", iMock)).isTrue();
		softly.assertThat(rule.validate("a+.", iMock)).isTrue();
		softly.assertThat(rule.validate("a_2", iMock)).isTrue();
		softly.assertThat(rule.validate("a+b£", iMock)).isTrue();
		softly.assertThat(rule.validate("a1b£", iMock)).isTrue();
		softly.assertThat(rule.validate("a+b£", iMock)).isTrue();
		softly.assertThat(rule.validate("a+b£c2", iMock)).isTrue();
		softly.assertThat(rule.validate("!a2b£c", iMock)).isTrue();
		softly.assertThat(rule.validate("!a3b£c.", iMock)).isTrue();
		softly.assertThat(rule.validate("a$$$$$", iMock)).isTrue();
		softly.assertThat(rule.validate("12?^", iMock)).isTrue();
		// Should not be valid, but at least these passwords are invalid in LengthRule.
		softly.assertThat(rule.validate("😀😀😀😀", iMock)).isTrue();
		softly.assertThat(rule.validate("a   d", iMock)).isTrue();
		softly.assertThat(rule.validate("\\u0001\\u0001\\u0001", iMock)).isTrue();
		softly.assertAll();
	}
	
	@Test
	public void shouldCreateLettersForbiddenRule() {
		ValidationRule rule = sut.createLettersForbiddenRule();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(rule.validate("1c", iMock)).isFalse();
		softly.assertThat(rule.validate("abc", iMock)).isFalse();
		softly.assertThat(rule.validate("äöü", iMock)).isFalse();
		softly.assertThat(rule.validate("я́блоня", iMock)).isFalse();
		softly.assertThat(rule.validate("*", iMock)).isTrue();
		softly.assertThat(rule.validate("1", iMock)).isTrue();
		softly.assertThat(rule.validate("4§", iMock)).isTrue();
		softly.assertThat(rule.validate("___", iMock)).isTrue();
		softly.assertThat(rule.validate("😀😀😀😀", iMock)).isTrue();
		softly.assertThat(rule.validate(" ", iMock)).isTrue();
		softly.assertAll();
	}
	
	@Test
	public void shouldCreateLettersUppercaseForbiddenRule() {
		ValidationRule rule = sut.createLettersUppercaseForbiddenRule();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(rule.validate("1C", iMock)).isFalse();
		softly.assertThat(rule.validate("aBc", iMock)).isFalse();
		softly.assertThat(rule.validate("äÖü", iMock)).isFalse();
		softly.assertThat(rule.validate("Гя́блоня", iMock)).isFalse();
		softly.assertThat(rule.validate("*", iMock)).isTrue();
		softly.assertThat(rule.validate("1", iMock)).isTrue();
		softly.assertThat(rule.validate("1c", iMock)).isTrue();
		softly.assertThat(rule.validate("4§", iMock)).isTrue();
		softly.assertThat(rule.validate("äü", iMock)).isTrue();
		softly.assertThat(rule.validate("abc", iMock)).isTrue();
		softly.assertThat(rule.validate("___", iMock)).isTrue();
		softly.assertThat(rule.validate("😀😀😀😀", iMock)).isTrue();
		softly.assertThat(rule.validate(" ", iMock)).isTrue();
		softly.assertAll();
	}
	
	@Test
	public void shouldCreateLettersLowercaseForbiddenRule() {
		ValidationRule rule = sut.createLettersLowercaseForbiddenRule();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(rule.validate("1c", iMock)).isFalse();
		softly.assertThat(rule.validate("aBc", iMock)).isFalse();
		softly.assertThat(rule.validate("äÖü", iMock)).isFalse();
		softly.assertThat(rule.validate("Гя́блоня", iMock)).isFalse();
		softly.assertThat(rule.validate("*", iMock)).isTrue();
		softly.assertThat(rule.validate("1", iMock)).isTrue();
		softly.assertThat(rule.validate("1C", iMock)).isTrue();
		softly.assertThat(rule.validate("4§", iMock)).isTrue();
		softly.assertThat(rule.validate("ÖÜ", iMock)).isTrue();
		softly.assertThat(rule.validate("ABC", iMock)).isTrue();
		softly.assertThat(rule.validate("___", iMock)).isTrue();
		softly.assertThat(rule.validate("😀😀😀😀", iMock)).isTrue();
		softly.assertThat(rule.validate(" ", iMock)).isTrue();
		softly.assertAll();
	}
	
	@Test
	public void shouldCreateDigitsForbiddenRule() {
		ValidationRule rule = sut.createDigitsForbiddenRule();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(rule.validate("1", iMock)).isFalse();
		softly.assertThat(rule.validate("1c", iMock)).isFalse();
		softly.assertThat(rule.validate("4§", iMock)).isFalse();
		softly.assertThat(rule.validate("123", iMock)).isFalse();
		softly.assertThat(rule.validate("abd", iMock)).isTrue();
		softly.assertThat(rule.validate("äöü", iMock)).isTrue();
		softly.assertThat(rule.validate("я́блоня", iMock)).isTrue();
		softly.assertThat(rule.validate("*", iMock)).isTrue();
		softly.assertThat(rule.validate("_", iMock)).isTrue();
		softly.assertThat(rule.validate("😀😀😀😀", iMock)).isTrue();
		softly.assertThat(rule.validate(" ", iMock)).isTrue();
		softly.assertAll();
	}
	
	@Test
	public void shouldCreateSpecialSignsForbiddenRule() {
		ValidationRule rule = sut.createSpecialSignsForbiddenRule();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(rule.validate("*", iMock)).isFalse();
		softly.assertThat(rule.validate("4%", iMock)).isFalse();
		softly.assertThat(rule.validate("a_a", iMock)).isFalse();
		softly.assertThat(rule.validate("😀😀😀😀", iMock)).isFalse();
		softly.assertThat(rule.validate("a v", iMock)).isFalse();
		softly.assertThat(rule.validate("\\u0001cd", iMock)).isFalse();
		softly.assertThat(rule.validate("1c", iMock)).isTrue();
		softly.assertThat(rule.validate("aBc", iMock)).isTrue();
		softly.assertThat(rule.validate("äÖü", iMock)).isTrue();
		softly.assertThat(rule.validate("я́блоня", iMock)).isTrue();
		softly.assertThat(rule.validate("1", iMock)).isTrue();
		softly.assertThat(rule.validate("1C", iMock)).isTrue();
		softly.assertThat(rule.validate("ÖÜ", iMock)).isTrue();
		softly.assertThat(rule.validate("ABC", iMock)).isTrue();
		softly.assertAll();
	}
	
	@Test
	public void shouldCreateDigitsAndSpecialSignsForbiddenRule() {
		ValidationRule rule = sut.createDigitsAndSpecialSignsForbiddenRule();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(rule.validate("*", iMock)).isFalse();
		softly.assertThat(rule.validate("4%", iMock)).isFalse();
		softly.assertThat(rule.validate("😀😀😀😀", iMock)).isFalse();
		softly.assertThat(rule.validate("a v", iMock)).isFalse();
		softly.assertThat(rule.validate("a_v", iMock)).isFalse();
		softly.assertThat(rule.validate("\\u0001cd", iMock)).isFalse();
		softly.assertThat(rule.validate("1", iMock)).isFalse();
		softly.assertThat(rule.validate("1C", iMock)).isFalse();
		softly.assertThat(rule.validate("1c", iMock)).isFalse();
		softly.assertThat(rule.validate("aBc", iMock)).isTrue();
		softly.assertThat(rule.validate("äÖü", iMock)).isTrue();
		softly.assertThat(rule.validate("я́блоня", iMock)).isTrue();
		softly.assertThat(rule.validate("ÖÜ", iMock)).isTrue();
		softly.assertThat(rule.validate("ABC", iMock)).isTrue();
		softly.assertAll();
	}
	
	@Test
	public void shouldUsernameForbiddenRule() {
		ValidationRule rule = sut.createUsernameForbiddenRule();
		
		Identity identity = mock(Identity.class);
		when(identity.getName()).thenReturn("myname");
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(rule.validate("myname", identity)).isFalse();
		softly.assertThat(rule.validate("MYNAME", identity)).isFalse();
		softly.assertThat(rule.validate("$$$MYNAME$$$", identity)).isFalse();
		softly.assertThat(rule.validate("myname01", identity)).isFalse();
		softly.assertThat(rule.validate("superman", identity)).isTrue();
		softly.assertAll();
	}
	
	@Test
	public void shouldUserFirstnameForbiddenRule() {
		ValidationRule rule = sut.createUserFirstnameForbiddenRule();
		
		User user = mock(User.class);
		when(user.getFirstName()).thenReturn("myname");
		Identity identity = mock(Identity.class);
		when(identity.getUser()).thenReturn(user);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(rule.validate("myname", identity)).isFalse();
		softly.assertThat(rule.validate("MYNAME", identity)).isFalse();
		softly.assertThat(rule.validate("$$$MYNAME$$$", identity)).isFalse();
		softly.assertThat(rule.validate("myname01", identity)).isFalse();
		softly.assertThat(rule.validate("superman", identity)).isTrue();
		softly.assertAll();
	}
	
	@Test
	public void shouldUserLastnameForbiddenRule() {
		ValidationRule rule = sut.createUserLastnameForbiddenRule();
		
		User user = mock(User.class);
		when(user.getLastName()).thenReturn("myname");
		Identity identity = mock(Identity.class);
		when(identity.getUser()).thenReturn(user);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(rule.validate("myname", identity)).isFalse();
		softly.assertThat(rule.validate("MYNAME", identity)).isFalse();
		softly.assertThat(rule.validate("$$$MYNAME$$$", identity)).isFalse();
		softly.assertThat(rule.validate("myname01", identity)).isFalse();
		softly.assertThat(rule.validate("superman", identity)).isTrue();
		softly.assertAll();
	}
	
	private static class TestableValidationRuleFactory extends PasswordValidationRuleFactory {

		@Override
		protected Translator createTranslator() {
			return mock(Translator.class);
		}
		
	}

}
