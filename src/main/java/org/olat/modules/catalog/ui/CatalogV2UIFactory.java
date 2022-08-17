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
package org.olat.modules.catalog.ui;

import java.util.Comparator;

import org.olat.core.gui.translator.Translator;
import org.olat.modules.catalog.CatalogLauncher;
import org.olat.modules.catalog.CatalogLauncherHandler;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;

/**
 * 
 * Initial date: 20 May 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogV2UIFactory {
	
	public static Comparator<TaxonomyLevel> getTaxonomyLevelComparator(Translator translator) {
		return Comparator
			.comparing(
					TaxonomyLevel::getSortOrder,
					Comparator.nullsLast(Integer::compare))
			.thenComparing(Comparator.comparing(
					level -> TaxonomyUIFactory.translateDisplayName(translator, level),
					Comparator.nullsLast(String::compareTo)));
		
	}

	public static String translateLauncherName(Translator translator, CatalogLauncherHandler handler, CatalogLauncher catalogLauncher) {
		if (catalogLauncher == null) return translator.translate(handler.getTypeI18nKey());
		
		return translateLauncherName(translator, handler, catalogLauncher.getIdentifier());
	}
	
	public static String translateLauncherName(Translator translator, CatalogLauncherHandler handler, String launcherIdentifier) {
		String i18nKey = getLauncherNameI18nKey(launcherIdentifier);
		String translation = translator.translate(i18nKey);
		if (i18nKey.equals(translation) || translation.length() > 256) {
			translation = translator.translate(handler.getTypeI18nKey());
		}
		return translation;
	}

	public static String getLauncherNameI18nKey(String identifier) {
		return "launcher.name.id." + identifier;
	}

}
