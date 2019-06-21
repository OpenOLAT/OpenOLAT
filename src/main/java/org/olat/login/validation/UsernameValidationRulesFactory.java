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

import java.util.Locale;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.login.LoginModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 19 Jun 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class UsernameValidationRulesFactory {

	@Autowired
	private LoginModule loginModule;
	
	public ValidationRulesProvider createRules() {
		ValidationRulesProviderBuilder providerBuilder = new ValidationRulesProviderBuilder();
		providerBuilder.add(createUsernameSyntaxRule());
		providerBuilder.add(createBlackListRule());
		providerBuilder.add(createUsernameExistsRule());
		return providerBuilder.create();
	}

	protected ValidationRule createUsernameSyntaxRule() {
		ValidationDescription description = createDescription("username.rule.syntax");
		return new RegexRule(description, loginModule.getUsernamePattern());
	}

	protected ValidationRule createBlackListRule() {
		ValidationDescription description = createDescription("username.rule.blacklist");
		return new UsernameBlackListRule(description);
	}
	
	private ValidationRule createUsernameExistsRule() {
		ValidationDescription description = createDescription("username.rule.in.use");
		return new UsernameInUseRule(description);
	}
	
	private ValidationDescription createDescription(String i18nKey) {
		return new TranslatedDescription(createTranslator(), i18nKey);
	}
	
	protected Translator createTranslator() {
		// The user locale is set later, so use the default locale to construct the translator.
		return Util.createPackageTranslator(LoginModule.class, Locale.getDefault());
	}
}
