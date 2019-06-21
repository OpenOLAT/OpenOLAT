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

import org.olat.login.LoginModule;
import org.olat.login.validation.PasswordValidationConfig.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 12 May 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class PasswordValidationRulesFactory {

	@Autowired
	private PasswordValidationRuleFactory ruleFactory;
	@Autowired
	private LoginModule loginModule;

	public ValidationRulesProvider createRules() {
		Builder configBuilder = PasswordValidationConfig.builder();
		configBuilder.withPasswordMinLength(loginModule.getPasswordMinLength());
		configBuilder.withPasswordMaxLength(loginModule.getPasswordMaxLength());
		configBuilder.withPasswordLetters(loginModule.getPasswordLetters());
		configBuilder.withPasswordLettersUppercase(loginModule.getPasswordLettersUppercase());
		configBuilder.withPasswordLettersLowercase(loginModule.getPasswordLettersLowercase());
		configBuilder.withPasswordDigitsAndSpecialSigns(loginModule.getPasswordDigitsAndSpecialSigns());
		configBuilder.withPasswordDigits(loginModule.getPasswordDigits());
		configBuilder.withPasswordSpecialSigns(loginModule.getPasswordSpecialSigns());
		configBuilder.withPasswordUsernameForbidden(loginModule.isPasswordUsernameForbidden());
		configBuilder.withPasswordFirstnameForbidden(loginModule.isPasswordFirstnameForbidden());
		configBuilder.withPasswordLastnameForbidden(loginModule.isPasswordLastnameForbidden());
		configBuilder.withPasswordHistory(loginModule.getPasswordHistory());
		return createRules(configBuilder.build());
	}
	
	public ValidationRulesProvider createRules(PasswordValidationConfig config) {
		ValidationRulesProviderBuilder providerBuilder = new ValidationRulesProviderBuilder();
		
		ValidationRule visibleCharactersRule = ruleFactory.createVisibleCharactersRule(config.getPasswordMinLength(),
				config.getPasswordMaxLength());
		providerBuilder.add(visibleCharactersRule);
		
		if (LoginModule.VALIDATE_SEPARATELY.equals(config.getPasswordLetters())) {
			ValidationRule lettersUppercaseRule = createLettersUppercaseRule(config.getPasswordLettersUppercase());
			providerBuilder.add(lettersUppercaseRule);
			ValidationRule lettersLowercaseRule = createLettersLowercaseRule(config.getPasswordLettersLowercase());
			providerBuilder.add(lettersLowercaseRule);
		} else {
			ValidationRule lettersRule = createLettersRule(config.getPasswordLetters());
			providerBuilder.add(lettersRule);
		}
		
		if (LoginModule.VALIDATE_SEPARATELY.equals(config.getPasswordDigitsAndSpecialSigns())) {
			ValidationRule digitsRule = createDigitsRule(config.getPasswordDigits());
			providerBuilder.add(digitsRule);
			ValidationRule specialSignsRule = createSpecialSignsRule(config.getPasswordSpecialSigns());
			providerBuilder.add(specialSignsRule);
		} else {
			ValidationRule digitsOrSpecialSignsRule = createDigitsAndSpecialSignsRule(config.getPasswordDigitsAndSpecialSigns());
			providerBuilder.add(digitsOrSpecialSignsRule);
		}
		
		if (config.isPasswordUsernameForbidden()) {
			ValidationRule usernameForbiddenRule = ruleFactory.createUsernameForbiddenRule();
			providerBuilder.add(usernameForbiddenRule);
		}
		if (config.isPasswordFirstnameForbidden()) {
			ValidationRule userFirstnameForbiddenRule = ruleFactory.createUserFirstnameForbiddenRule();
			providerBuilder.add(userFirstnameForbiddenRule);
		}
		if (config.isPasswordLastnameForbidden()) {
			ValidationRule userLastnameForbiddenRule = ruleFactory.createUserLastnameForbiddenRule();
			providerBuilder.add(userLastnameForbiddenRule);
		}
		
		if (config.getPasswordHistory() > 0) {
			ValidationRule historyRule = ruleFactory.createHistoryRule(config.getPasswordHistory());
			providerBuilder.add(historyRule);
		}
		
		return providerBuilder.create();
	}
	
	private ValidationRule createLettersRule(String passwordLetters) {
		switch (passwordLetters) {
		case LoginModule.AT_LEAST_1: return ruleFactory.createAtLeastLettersRule(1);
		case LoginModule.AT_LEAST_2: return ruleFactory.createAtLeastLettersRule(2);
		case LoginModule.AT_LEAST_3: return ruleFactory.createAtLeastLettersRule(3);
		case LoginModule.FORBIDDEN: return ruleFactory.createLettersForbiddenRule();
		case LoginModule.DISABLED:
		default:
			return ruleFactory.createLettersPermittedRule();
		}
	}
	
	private ValidationRule createLettersUppercaseRule(String passwordLettersUppercase) {
		switch (passwordLettersUppercase) {
		case LoginModule.AT_LEAST_1: return ruleFactory.createAtLeastLettersUppercaseRule(1);
		case LoginModule.AT_LEAST_2: return ruleFactory.createAtLeastLettersUppercaseRule(2);
		case LoginModule.AT_LEAST_3: return ruleFactory.createAtLeastLettersUppercaseRule(3);
		case LoginModule.FORBIDDEN: return ruleFactory.createLettersUppercaseForbiddenRule();
		case LoginModule.DISABLED:
		default:
			return ruleFactory.createLettersPermittedRule();
		}
	}
	
	private ValidationRule createLettersLowercaseRule(String passwordLettersLowercase) {
		switch (passwordLettersLowercase) {
		case LoginModule.AT_LEAST_1: return ruleFactory.createAtLeastLettersLowercaseRule(1);
		case LoginModule.AT_LEAST_2: return ruleFactory.createAtLeastLettersLowercaseRule(2);
		case LoginModule.AT_LEAST_3: return ruleFactory.createAtLeastLettersLowercaseRule(3);
		case LoginModule.FORBIDDEN: return ruleFactory.createLettersLowercaseForbiddenRule();
		case LoginModule.DISABLED:
		default:
			return ruleFactory.createLettersPermittedRule();
		}
	}
	
	private ValidationRule createDigitsAndSpecialSignsRule(String passwordDigitsOrSpecialSigns) {
		switch (passwordDigitsOrSpecialSigns) {
		case LoginModule.AT_LEAST_1: return ruleFactory.createAtLeastDigitsOrSpecialSignsRule(1);
		case LoginModule.AT_LEAST_2: return ruleFactory.createAtLeastDigitsOrSpecialSignsRule(2);
		case LoginModule.AT_LEAST_3: return ruleFactory.createAtLeastDigitsOrSpecialSignsRule(3);
		case LoginModule.FORBIDDEN: return ruleFactory.createDigitsAndSpecialSignsForbiddenRule();
		case LoginModule.DISABLED:
		default:
			return ruleFactory.createDigitsAndSpecialSignsPermittedRule();
		}
	}
	
	private ValidationRule createDigitsRule(String passwordDigits) {
		switch (passwordDigits) {
		case LoginModule.AT_LEAST_1: return ruleFactory.createAtLeastDigitsRule(1);
		case LoginModule.AT_LEAST_2: return ruleFactory.createAtLeastDigitsRule(2);
		case LoginModule.AT_LEAST_3: return ruleFactory.createAtLeastDigitsRule(3);
		case LoginModule.FORBIDDEN: return ruleFactory.createDigitsForbiddenRule();
		case LoginModule.DISABLED:
		default:
			return ruleFactory.createDigitsPermittedRule();
		}
	}
	
	private ValidationRule createSpecialSignsRule(String passwordSpecialSigns) {
		switch (passwordSpecialSigns) {
		case LoginModule.AT_LEAST_1: return ruleFactory.createAtLeastSpecialSignsRule(1);
		case LoginModule.AT_LEAST_2: return ruleFactory.createAtLeastSpecialSignsRule(2);
		case LoginModule.AT_LEAST_3: return ruleFactory.createAtLeastSpecialSignsRule(3);
		case LoginModule.FORBIDDEN: return ruleFactory.createSpecialSignsForbiddenRule();
		case LoginModule.DISABLED:
		default:
			return ruleFactory.createSpecialSignsPermittedRule();
		}
	}

}
