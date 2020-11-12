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

import static java.util.regex.Pattern.UNICODE_CHARACTER_CLASS;

import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.login.LoginModule;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 12 May 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class PasswordValidationRuleFactory {
	
	public ValidationRule createVisibleCharactersRule(int min, int max) {
		ValidationDescription description = createDescription("password.rule.length", min, max);
		Pattern pattern = Pattern.compile("^\\p{Graph}{" + min + "," + max + "}$", UNICODE_CHARACTER_CLASS);
		return new RegexRule(description, pattern);
	}
	
	public ValidationRule createAtLeastLettersRule(int min) {
		String i18nKey = min > 1 ? "password.rule.at.least.letters" : "password.rule.at.least.letter";
		ValidationDescription description = createDescription(i18nKey, min);
		return new AtLeastCharacterRule(description, Character::isLetter, min);
	}
	
	public ValidationRule createAtLeastLettersUppercaseRule(int min) {
		String i18nKey = min > 1 ? "password.rule.at.least.letters.uppercase" : "password.rule.at.least.letter.uppercase";
		ValidationDescription description = createDescription(i18nKey, min);
		return new AtLeastCharacterRule(description, Character::isUpperCase, min);
	}
	
	public ValidationRule createAtLeastLettersLowercaseRule(int min) {
		String i18nKey = min > 1 ? "password.rule.at.least.letters.lowercase" : "password.rule.at.least.letter.lowercase";
		ValidationDescription description = createDescription(i18nKey, min);
		return new AtLeastCharacterRule(description, Character::isLowerCase, min);
	}
	
	public ValidationRule createAtLeastDigitsRule(int min) {
		String i18nKey = min > 1 ? "password.rule.at.least.digits" : "password.rule.at.least.digit";
		ValidationDescription description = createDescription(i18nKey, min);
		return new AtLeastCharacterRule(description, Character::isDigit, min);
	}
	
	public ValidationRule createAtLeastSpecialSignsRule(int min) {
		String i18nKey = min > 1 ? "password.rule.at.least.specials" : "password.rule.at.least.special";
		ValidationDescription description = createDescription(i18nKey, min);
		// at least 2: .*\\W.*\\W.*
		String regex = Stream.generate(() -> ".*").limit(min + 1).collect(Collectors.joining("[_|\\W]"));
		Pattern pattern = Pattern.compile(regex, UNICODE_CHARACTER_CLASS);
		return new RegexRule(description, pattern);
	}
	
	public ValidationRule createAtLeastDigitsOrSpecialSignsRule(int min) {
		String i18nKey = min > 1 ? "password.rule.at.least.digits.specials" : "password.rule.at.least.digit.special";
		ValidationDescription description = createDescription(i18nKey, min);
		String regex = Stream.generate(() -> ".*").limit(min + 1).collect(Collectors.joining("[_|\\d|\\W]"));
		Pattern pattern = Pattern.compile(regex, UNICODE_CHARACTER_CLASS);
		return new RegexRule(description, pattern);
	}
	
	public ValidationRule createLettersPermittedRule() {
		ValidationDescription description = createDescription("password.rule.permitted.letters");
		return new TrueRule(description);
	}
	
	public ValidationRule createLettersUppercasePermittedRule() {
		ValidationDescription description = createDescription("password.rule.permitted.letters.uppercase");
		return new TrueRule(description);
	}
	
	public ValidationRule createLettersLowercasePermittedRule() {
		ValidationDescription description = createDescription("password.rule.permitted.letters.lowercase");
		return new TrueRule(description);
	}
	
	public ValidationRule createDigitsAndSpecialSignsPermittedRule() {
		ValidationDescription description = createDescription("password.rule.permitted.digits.specials");
		return new TrueRule(description);
	}
	
	public ValidationRule createDigitsPermittedRule() {
		ValidationDescription description = createDescription("password.rule.permitted.digits");
		return new TrueRule(description);
	}
	
	public ValidationRule createSpecialSignsPermittedRule() {
		ValidationDescription description = createDescription("password.rule.permitted.specials");
		return new TrueRule(description);
	}
	
	public ValidationRule createLettersForbiddenRule() {
		ValidationDescription description = createDescription("password.rule.forbidden.letters");
		return new OppositeRule(new AtLeastCharacterRule(description, Character::isLetter, 1));
	}
	
	public ValidationRule createLettersUppercaseForbiddenRule() {
		ValidationDescription description = createDescription("password.rule.forbidden.letters.uppercase");
		return new OppositeRule(new AtLeastCharacterRule(description, Character::isUpperCase, 1));
	}
	
	public ValidationRule createLettersLowercaseForbiddenRule() {
		ValidationDescription description = createDescription("password.rule.forbidden.letters.lowercase");
		return new OppositeRule(new AtLeastCharacterRule(description, Character::isLowerCase, 1));
	}
	
	public ValidationRule createDigitsForbiddenRule() {
		ValidationDescription description = createDescription("password.rule.forbidden.digits");
		return new OppositeRule(new AtLeastCharacterRule(description, Character::isDigit, 1));
	}
	
	public ValidationRule createSpecialSignsForbiddenRule() {
		ValidationDescription description = createDescription("password.rule.forbidden.specials");
		Pattern pattern = Pattern.compile(".*[_|\\W]+.*", UNICODE_CHARACTER_CLASS);
		return new OppositeRule(new RegexRule(description, pattern));
	}
	
	public ValidationRule createDigitsAndSpecialSignsForbiddenRule() {
		ValidationDescription description = createDescription("password.rule.forbidden.specials");
		Pattern pattern = Pattern.compile(".*[_|\\W|\\d]+.*", UNICODE_CHARACTER_CLASS);
		return new OppositeRule(new RegexRule(description, pattern));
	}

	public ValidationRule createUsernameForbiddenRule() {
		ValidationDescription description = createDescription("password.rule.forbidden.username");
		Function<Identity, String> forbiddenValue = i -> i.getName();
		return new IdentityValueForbiddenRule(description, forbiddenValue);
	}

	public ValidationRule createUserFirstnameForbiddenRule() {
		ValidationDescription description = createDescription("password.rule.forbidden.user.firstname");
		Function<Identity, String> forbiddenValue = i -> i.getUser().getFirstName();
		return new IdentityValueForbiddenRule(description, forbiddenValue);
	}

	public ValidationRule createUserLastnameForbiddenRule() {
		ValidationDescription description = createDescription("password.rule.forbidden.user.lastname");
		Function<Identity, String> forbiddenValue = i -> i.getUser().getLastName();
		return new IdentityValueForbiddenRule(description, forbiddenValue);
	}

	public ValidationRule createHistoryRule(int numberChanges) {
		ValidationDescription description = createDescription("password.rule.history");
		return new HistoryRule(description, numberChanges);
	}

	private ValidationDescription createDescription(String i18nKey) {
		return new TranslatedDescription(createTranslator(), i18nKey);
	}
	
	private ValidationDescription createDescription(String i18nKey, int arg) {
		return createDescription(i18nKey, new String[] { String.valueOf(arg) } );
	}
	
	private ValidationDescription createDescription(String i18nKey, int arg1, int arg2) {
		return createDescription(i18nKey, new String[] { String.valueOf(arg1), String.valueOf(arg2) } );
	}
	
	private ValidationDescription createDescription(String i18nKey, String[] args) {
		return new TranslatedDescription(createTranslator(), i18nKey, args);
	}

	protected Translator createTranslator() {
		// The user locale is set later, so use the default locale to construct the translator.
		return Util.createPackageTranslator(LoginModule.class, Locale.getDefault());
	}

}
