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
package org.olat.repository.ui.author;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.modules.catalog.ui.CatalogBCFactory;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.model.TaxonomyLevelNamePath;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.CatalogEntry;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.CatalogManager;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.CatalogInfo;
import org.olat.resource.accesscontrol.ui.AccessConfigurationController;

/**
 * 
 * Initial date: 30 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryCatalogInfoFactory {
	
	public static String wrapTaxonomyLevels(List<TaxonomyLevelNamePath> taxonomyLevels) {
		StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"o_taxonomy_tags\">");
		for (TaxonomyLevelNamePath taxonomyLevel : taxonomyLevels) {
			sb.append("<span class=\"o_tag o_taxonomy\" title=\"");
			sb.append(StringHelper.escapeHtml(taxonomyLevel.getMaterializedPathIdentifiersWithoutSlash()));
			sb.append("\">");
			sb.append(taxonomyLevel.getDisplayName());
			sb.append("</span>");
		}
		sb.append("</div>");
		return sb.toString();
	}
	
	public static CatalogInfo createCatalogInfo(RepositoryEntry entry, Locale locale, boolean showBusinessPath, boolean showRQCode) {
		CatalogV2Module catalogV2Module = CoreSpringFactory.getImpl(CatalogV2Module.class);
		if (catalogV2Module.isEnabled()) {
			Translator translator = Util.createPackageTranslator(TaxonomyUIFactory.class, locale);
			translator = Util.createPackageTranslator(AccessConfigurationController.class, locale, translator);
			translator = Util.createPackageTranslator(RepositoryService.class, locale, translator);
			String details;
			List<TaxonomyLevel> taxonomyLevels = CoreSpringFactory.getImpl(RepositoryService.class).getTaxonomy(entry);
			if (taxonomyLevels.isEmpty()) {
				details = translator.translate("access.taxonomy.level.not.yet");
			} else {
				List<TaxonomyLevelNamePath> taxonomyLevelPaths = TaxonomyUIFactory.getNamePaths(translator, taxonomyLevels);
				details = wrapTaxonomyLevels(taxonomyLevelPaths);
			}
			String editBusinessPath = null;
			if (showBusinessPath) {
				editBusinessPath = "[RepositoryEntry:" + entry.getKey() + "][Settings:0][Metadata:0]";
			}
			return new CatalogInfo(true, catalogV2Module.isWebPublishEnabled(),
					translator.translate("offer.period.status"), true, translator.translate("access.taxonomy.level"),
					details, false, getStatusNotAvailable(translator, entry.getEntryStatus()), false, editBusinessPath,
					translator.translate("access.open.metadata"),
					CatalogBCFactory.get(false).getOfferUrl(entry.getOlatResource()), taxonomyLevels, showRQCode);
		} else if (CoreSpringFactory.getImpl(RepositoryModule.class).isCatalogEnabled()) {
			Translator translator = Util.createPackageTranslator(RepositoryService.class, locale);
			translator = Util.createPackageTranslator(AccessConfigurationController.class, locale, translator);
			String details = null;
			boolean notAvailableEntry;
			List<CatalogEntry> catalogEntries = CoreSpringFactory.getImpl(CatalogManager.class).getCatalogCategoriesFor(entry);
			if (catalogEntries.isEmpty()) {
				details = translator.translate("access.no.catalog.entry");
				notAvailableEntry = true;
			} else {
				List<String> catalogEntryPaths = new ArrayList<>(catalogEntries.size());
				for (CatalogEntry catalogEntry : catalogEntries) {
					List<String> names = new ArrayList<>();
					addParentNames(names, catalogEntry);
					Collections.reverse(names);
					String path = names.stream().collect(Collectors.joining("/"));
					path = "/" + path;
					catalogEntryPaths.add(path);
				}
				details = catalogEntryPaths.stream()
						.sorted()
						.collect(Collectors.joining(", "));
				notAvailableEntry = false;
			}
			String editBusinessPath = null;
			if (showBusinessPath) {
				editBusinessPath = "[RepositoryEntry:" + entry.getKey() + "][Settings:0][Catalog:0]";
			}
			return new CatalogInfo(true, false, translator.translate("offer.period.status"), true,
					translator.translate("access.info.catalog.entries"), details, notAvailableEntry, null, false,
					editBusinessPath, translator.translate("access.open.catalog"), null, null, showRQCode);
		}
		return CatalogInfo.UNSUPPORTED;
	}

	private static void addParentNames(List<String> names, CatalogEntry catalogEntry) {
		String name = StringHelper.containsNonWhitespace(catalogEntry.getShortTitle())
				? catalogEntry.getShortTitle()
				: catalogEntry.getName();
		names.add(name);
		if (catalogEntry.getParent() != null) {
			addParentNames(names, catalogEntry.getParent());
		}
	}
	
	public static final String getStatusNotAvailable(Translator translator, RepositoryEntryStatusEnum reStatus) {
		CatalogV2Module catalogV2Module = CoreSpringFactory.getImpl(CatalogV2Module.class);
		if (catalogV2Module.isEnabled()) {
			if (!RepositoryEntryStatusEnum.isInArray(reStatus, ACService.RESTATUS_ACTIVE_METHOD)) {
				return translator.translate(reStatus.i18nKey());
			}
		}
		return null;
	}
	

}
