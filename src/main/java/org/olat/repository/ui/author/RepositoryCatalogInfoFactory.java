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
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.CatalogEntry;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.CatalogManager;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.CatalogInfo;
import org.olat.resource.accesscontrol.CatalogInfo.CatalogStatusEvaluator;
import org.olat.resource.accesscontrol.CatalogInfo.SortPriorityProvider;
import org.olat.resource.accesscontrol.ui.AccessConfigurationController;

/**
 * 
 * Initial date: 30 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryCatalogInfoFactory {
	
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
				details = TaxonomyUIFactory.getTags(translator, taxonomyLevels);
			}
			String editBusinessPath = null;
			if (showBusinessPath) {
				editBusinessPath = "[RepositoryEntry:" + entry.getKey() + "][Settings:0][Metadata:0]";
			}
			return new CatalogInfo(true, catalogV2Module.isWebPublishEnabled(),
					false, true,
					true, translator.translate("access.taxonomy.level"), details,
					null, getCatalogStatusEvaluator(entry.getEntryStatus()), translator.translate("offer.available.in.status.course"),
					false,
					false, 
					editBusinessPath,
					translator.translate("access.open.metadata"),
					CatalogBCFactory.get(false).getOfferUrl(entry.getOlatResource()),
					catalogV2Module.isWebPublishEnabled()? CatalogBCFactory.get(true).getOfferUrl(entry.getOlatResource()): null,
					taxonomyLevels,
					showRQCode,
					catalogV2Module.isPrioritySortingEnabled()? new RepositoryEntryCatalogSortPriorityProvider(entry): null);
		} else if (CoreSpringFactory.getImpl(RepositoryModule.class).isCatalogEnabled()) {
			Translator translator = Util.createPackageTranslator(RepositoryService.class, locale);
			translator = Util.createPackageTranslator(AccessConfigurationController.class, locale, translator);
			List<CatalogEntry> catalogEntries = CoreSpringFactory.getImpl(CatalogManager.class).getCatalogCategoriesFor(entry);
			RepositoryEntryCatalogV1StatusEvaluator statusEvaluator = new RepositoryEntryCatalogV1StatusEvaluator(!catalogEntries.isEmpty());
			String details = null;
			if (catalogEntries.isEmpty()) {
				details = translator.translate("access.no.catalog.entry");
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
			}
			String editBusinessPath = null;
			if (showBusinessPath) {
				editBusinessPath = "[RepositoryEntry:" + entry.getKey() + "][Settings:0][Catalog:0]";
			}
			return new CatalogInfo(true, false, false, true, true, translator.translate("access.info.catalog.entries"),
					details, null, statusEvaluator, translator.translate("offer.available.in.status.course"), false,
					false, editBusinessPath, translator.translate("access.open.catalog"), null, null, null, showRQCode, null);
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
	
	public static final CatalogStatusEvaluator getCatalogStatusEvaluator(RepositoryEntryStatusEnum status) {
		if (CoreSpringFactory.getImpl(CatalogV2Module.class).isEnabled()) {
			return new RepositoryEntryCatalogV2StatusEvaluator(status);
		}
		return null;
	}
	
	private static final class RepositoryEntryCatalogV1StatusEvaluator implements CatalogStatusEvaluator {
		
		private final boolean catalogEntryAvailable;
		
		public RepositoryEntryCatalogV1StatusEvaluator(boolean catalogEntryAvailable) {
			this.catalogEntryAvailable = catalogEntryAvailable;
		}

		@Override
		public boolean isVisibleStatusNoPeriod() {
			return catalogEntryAvailable;
		}

		@Override
		public boolean isVisibleStatusPeriod() {
			return catalogEntryAvailable;
		}
		
	}
	
	private static final class RepositoryEntryCatalogV2StatusEvaluator implements CatalogStatusEvaluator {

		private final RepositoryEntryStatusEnum status;

		public RepositoryEntryCatalogV2StatusEvaluator(RepositoryEntryStatusEnum status) {
			this.status = status;
		}

		@Override
		public boolean isVisibleStatusNoPeriod() {
			return RepositoryEntryStatusEnum.isInArray(status, ACService.RESTATUS_ACTIVE_METHOD);
		}

		@Override
		public boolean isVisibleStatusPeriod() {
			return RepositoryEntryStatusEnum.isInArray(status, ACService.RESTATUS_ACTIVE_METHOD_PERIOD);
		}
		
	}
	
	public static final class RepositoryEntryCatalogSortPriorityProvider implements SortPriorityProvider {
		
		private final RepositoryEntryRef entry;
		private Integer priority;

		public RepositoryEntryCatalogSortPriorityProvider(RepositoryEntry entry) {
			this.entry = entry;
			this.priority = entry.getCatalogSortPriority();
		}

		@Override
		public Integer getPriority() {
			return priority;
		}

		@Override
		public void setPriority(Integer priority) {
			RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
			RepositoryEntry repositoryEntry = repositoryService.loadBy(entry);
			if (repositoryEntry != null) {
				repositoryEntry.setCatalogSortPriority(priority);
				repositoryService.update(repositoryEntry);
				this.priority = priority;
			}
		}
		
	}

}
