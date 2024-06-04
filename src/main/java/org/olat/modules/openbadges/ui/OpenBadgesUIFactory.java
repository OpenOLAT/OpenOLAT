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
package org.olat.modules.openbadges.ui;

import java.util.Locale;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

import org.apache.logging.log4j.Level;

/**
 * Initial date: 2023-06-26<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OpenBadgesUIFactory {

	public static String getBundleName() {
		return OpenBadgesUIFactory.class.getPackage().getName();
	}

	public static String getTemplateNameI18nKey(String identifier) {
		return "template.name.id." + identifier;
	}

	public static String getTemplateDescriptionI18nKey(String identifier) {
		return "template.description.id." + identifier;
	}

	public static String getTemplateNameFallbackKey() {
		return "form.template.name.placeholder";
	}

	public static Translator getTranslator(Locale locale) {
		return Util.createPackageTranslator(OpenBadgesUIFactory.class, locale);
	}

	public static String translateTemplateName(Translator translator, String identifier) {
		String i18nKey = getTemplateNameI18nKey(identifier);
		String translation = translator.translate(i18nKey, null, Level.OFF);
		if (i18nKey.equals(translation) || translation.length() > 256) {
			return translator.translate(getTemplateNameFallbackKey());
		}
		return StringHelper.xssScan(translation);
	}

	public static String translateTemplateDescription(Translator translator, String identifier) {
		String i18nKey = getTemplateDescriptionI18nKey(identifier);
		String translation = translator.translate(i18nKey, null, Level.OFF);
		if (i18nKey.equals(translation) || translation.length() > 256) {
			return "";
		}
		return StringHelper.xssScan(translation);
	}
}
