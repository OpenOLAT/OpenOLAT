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
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.modules.taxonomy.model.TaxonomyLevelNamePath;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.CatalogEntry;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.CatalogManager;
import org.olat.resource.accesscontrol.CatalogInfo;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.ui.AccessConfigurationController;

/**
 * 
 * Initial date: 30 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryCatalogInfoFactory {
	
	public static CatalogInfo createCatalogInfo(RepositoryEntry entry, Locale locale, boolean showBusinessPath) {
		if (CoreSpringFactory.getImpl(CatalogV2Module.class).isEnabled()) {
			Translator translator = Util.createPackageTranslator(TaxonomyUIFactory.class, locale);
			translator = Util.createPackageTranslator(AccessConfigurationController.class, locale, translator);
			translator = Util.createPackageTranslator(RepositoryService.class, locale, translator);
			String details = null;
			String editBusinessPath = null;
			List<TaxonomyLevelNamePath> taxonomyLevels = TaxonomyUIFactory.getNamePaths(translator,
					CoreSpringFactory.getImpl(RepositoryService.class).getTaxonomy(entry));
			if (taxonomyLevels.isEmpty()) {
				details = translator.translate("access.no.taxonomy.level");
				if (showBusinessPath) {
					editBusinessPath = "[RepositoryEntry:" + entry.getKey() + "][Settings:0][Metadata:0]";
				}
			} else {
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
				details = sb.toString();
			}
			Predicate<Offer> catalogVisibility = offer -> offer.isGuestAccess() || offer.isOpenAccess() || offer.isCatalogPublish();
			return new CatalogInfo(true, true, details, catalogVisibility, editBusinessPath, translator.translate("access.open.metadata"));
		} else if (CoreSpringFactory.getImpl(RepositoryModule.class).isCatalogEnabled()) {
			Translator translator = Util.createPackageTranslator(RepositoryService.class, locale);
			String details = null;
			Predicate<Offer> catalogVisibility = null;
			String editBusinessPath = null;
			List<CatalogEntry> catalogEntries = CoreSpringFactory.getImpl(CatalogManager.class).getCatalogCategoriesFor(entry);
			if (catalogEntries.isEmpty()) {
				details = translator.translate("access.no.catalog.entry");
				catalogVisibility = offer -> false;
				if (showBusinessPath) {
					editBusinessPath = "[RepositoryEntry:" + entry.getKey() + "][Settings:0][Catalog:0]";
				}
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
				catalogVisibility = offer -> true;
			}
			return new CatalogInfo(true, true, details, catalogVisibility, editBusinessPath, translator.translate("access.open.catalog"));
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

}
