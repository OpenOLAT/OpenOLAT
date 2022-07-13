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
package org.olat.modules.taxonomy.ui;

import java.util.function.Supplier;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.modules.taxonomy.TaxonomyLevel;

/**
 * 
 * Initial date: 27 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyUIFactory {
	
	public static final String BUNDLE_NAME = TaxonomyUIFactory.class.getPackageName();
	public static final String PREFIX_DISPLAY_NAME = "multi.display.name.";
	public static final String PREFIX_DESCRIPTION = "multi.description.";

	private static String getDisplayNameI18nKey(TaxonomyLevel level) {
		return PREFIX_DISPLAY_NAME + level.getI18nSuffix();
	}
	
	public static String translateDisplayName(Translator translator, TaxonomyLevel level) {
		return translateDisplayName(translator, level, StringHelper.NULL);
	}
	
	public static String translateDisplayName(Translator translator, TaxonomyLevel level, Supplier<String> notFound) {
		if (level == null) return notFound.get();
		
		String i18nKey = getDisplayNameI18nKey(level);
		// Always fallback to default, even in debug modus
		String translation = translator.translate(i18nKey, null, 0, true);
		if (translation == null || i18nKey.equals(translation) || translation.length() > 256) {
			translation = notFound.get();
		}
		return translation;
	}
	
	private static String getDescriptionI18nKey(TaxonomyLevel level) {
		return PREFIX_DESCRIPTION + level.getI18nSuffix();
	}
	
	public static String translateDescription(Translator translator, TaxonomyLevel level) {
		if (level == null) return null;
		
		String i18nKey = getDescriptionI18nKey(level);
		// Always fallback to default, even in debug modus
		String translation = translator.translate(i18nKey, null, 0, true);
		if (translation == null || i18nKey.equals(translation) || translation.length() > 256) {
			translation = null;
		}
		return translation;
	}

}
