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

/**
 * 
 * Initial date: 13 May 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TranslatedDescription implements ValidationDescription {

	private final Translator translator;
	private final String i18nKey;
	private final String[] args;

	public TranslatedDescription(Translator translator, String i18nKey) {
		this(translator, i18nKey, null);
	}
	
	public TranslatedDescription(Translator translator, String i18nKey, String[] args) {
		this.translator = translator;
		this.i18nKey = i18nKey;
		this.args = args;
	}

	@Override
	public String getText(Locale locale) {
		translator.setLocale(locale);
		return translator.translate(i18nKey, args);
	}

}
