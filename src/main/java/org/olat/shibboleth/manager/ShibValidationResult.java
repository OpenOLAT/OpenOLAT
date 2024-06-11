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
package org.olat.shibboleth.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.login.LoginModule;
import org.olat.login.validation.TranslatedDescription;
import org.olat.login.validation.ValidationDescription;
import org.olat.login.validation.ValidationResult;

/**
 * 
 * Initial date: 11 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ShibValidationResult implements ValidationResult {
	
	private final boolean valid;
	private final List<ValidationDescription> errors = new ArrayList<>();
	
	private ShibValidationResult(boolean valid) {
		this.valid = valid;
	}
	
	public static final ValidationResult allOk() {
		return new ShibValidationResult(true);
	}
	
	public static final ValidationResult error(String i18nKey) {
		ShibValidationResult result = new ShibValidationResult(false);
		Translator translator = Util.createPackageTranslator(LoginModule.class, Locale.getDefault());
		TranslatedDescription description = new TranslatedDescription(translator, i18nKey);
		result.errors.add(description);
		return result;
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	@Override
	public List<ValidationDescription> getInvalidDescriptions() {
		return errors;
	}
}
