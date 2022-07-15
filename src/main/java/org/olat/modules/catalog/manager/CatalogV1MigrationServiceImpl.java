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
package org.olat.modules.catalog.manager;

import static org.olat.modules.catalog.launcher.TextLauncherHandler.I18N_PREFIX;
import static org.olat.modules.taxonomy.ui.TaxonomyUIFactory.PREFIX_DESCRIPTION;
import static org.olat.modules.taxonomy.ui.TaxonomyUIFactory.PREFIX_DISPLAY_NAME;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.I18nItem;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.catalog.CatalogFilter;
import org.olat.modules.catalog.CatalogFilterSearchParams;
import org.olat.modules.catalog.CatalogLauncher;
import org.olat.modules.catalog.CatalogLauncherSearchParams;
import org.olat.modules.catalog.CatalogV1MigrationService;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.modules.catalog.CatalogV2Module.CatalogV1Migration;
import org.olat.modules.catalog.CatalogV2Service;
import org.olat.modules.catalog.filter.TaxonomyLevelChildrenHandler;
import org.olat.modules.catalog.launcher.StaticHandler;
import org.olat.modules.catalog.launcher.TaxonomyLevelLauncherHandler;
import org.olat.modules.catalog.launcher.TextLauncherHandler;
import org.olat.modules.catalog.ui.CatalogV2UIFactory;
import org.olat.modules.catalog.ui.admin.CatalogLauncherTextEditController;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.CatalogEntry;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryModule;
import org.olat.repository.manager.CatalogManager;
import org.olat.repository.manager.RepositoryEntryToTaxonomyLevelDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 11 Jul 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CatalogV1MigrationServiceImpl implements CatalogV1MigrationService {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CatalogV2Module catalogModule;
	@Autowired
	private CatalogV2Service catalogService;
	@Autowired
	private StaticHandler staticHandler;
	@Autowired
	private TextLauncherHandler textLauncherHandler;
	@Autowired
	private TaxonomyLevelLauncherHandler taxonomyLevelLauncherHandler;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private CatalogManager catalogManager;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private RepositoryEntryToTaxonomyLevelDAO repositoryEntryToTaxonomyLevelDao;
	@Autowired
	private I18nModule i18nModule;
	@Autowired
	private I18nManager i18nManager;

	@Override
	public void migrate(Identity executor) {
		if (CatalogV1Migration.pending != catalogModule.getCatalogV1Migration()) {
			return;
		}
		
		catalogModule.setCatalogV1Migration(CatalogV1Migration.running);
		
		// Get the root catalog entry
		List<CatalogEntry> rootCatalogEntries = catalogManager.getRootCatalogEntries();
		if (rootCatalogEntries.isEmpty()) {
			return;
		}
		CatalogEntry rootCatalogEntry = rootCatalogEntries.get(0);
		
		// Update the taxonomy image style
		catalogModule.setLauncherTaxonomyLevelStyle(CatalogV2Module.TAXONOMY_LEVEL_LAUNCHER_STYLE_RECTANGLE);
		
		// Create a new taxonomy...
		Taxonomy taxonomy = taxonomyService.createTaxonomy(rootCatalogEntry.getShortTitle(),
				rootCatalogEntry.getName(), rootCatalogEntry.getDescription(), null);
		
		// ... and add it to the repository entry taxonomies
		List<TaxonomyRef> taxonomyRefs = new ArrayList<>(repositoryModule.getTaxonomyRefs());
		taxonomyRefs.add(taxonomy);
		repositoryModule.setTaxonomyRefs(taxonomyRefs);
		
		// Create a TaxonomyLevel for each catalog entry
		migrateCatalogEntry(executor, rootCatalogEntry, taxonomy, null, null);
		
		// Create launchers and filters
		createLaunchers(rootCatalogEntry, taxonomy);
		createFilter();
	
		// Mark as migrated
		catalogModule.setCatalogV1Migration(CatalogV1Migration.done);
	}

	private void migrateCatalogEntry(Identity executor, CatalogEntry catalogEntry, Taxonomy taxonomy, TaxonomyLevel parentLevel, Integer sortOder) {
		TaxonomyLevel taxonomyLevel = null;
		
		// The root catalogEntry is migrated to the taxonomy instead of a taxonomy level.
		if (catalogEntry.getParent() != null) {
			String i18nSuffix = taxonomyService.createI18nSuffix();
			taxonomyLevel = taxonomyService.createTaxonomyLevel(catalogEntry.getShortTitle(), i18nSuffix, null, null, parentLevel, taxonomy);
			if (sortOder != null) {
				taxonomyLevel.setSortOrder(sortOder);
				taxonomyLevel = taxonomyService.updateTaxonomyLevel(taxonomyLevel);
			}
			
			Locale overlayDefaultLocale = i18nModule.getOverlayLocales().get(I18nModule.getDefaultLocale());
			I18nItem displayNameItem = i18nManager.getI18nItem(TaxonomyUIFactory.BUNDLE_NAME, PREFIX_DISPLAY_NAME + taxonomyLevel.getI18nSuffix(), overlayDefaultLocale);
			i18nManager.saveOrUpdateI18nItem(displayNameItem, catalogEntry.getName());
			
			if (StringHelper.containsNonWhitespace(catalogEntry.getDescription())) {
				I18nItem descriptionItem = i18nManager.getI18nItem(TaxonomyUIFactory.BUNDLE_NAME, PREFIX_DESCRIPTION + taxonomyLevel.getI18nSuffix(), overlayDefaultLocale);
				i18nManager.saveOrUpdateI18nItem(descriptionItem, catalogEntry.getDescription());
			}
			
			VFSLeaf catalogImage = catalogManager.getImage(catalogEntry);
			if (catalogImage != null && catalogImage instanceof LocalFileImpl) {
				taxonomyService.storeTeaserImage(taxonomyLevel, executor, ((LocalFileImpl)catalogImage).getBasefile(), catalogImage.getName());
			}
		}
		
		List<CatalogEntry> children = catalogManager.getChildrenOf(catalogEntry, 0, -1, CatalogEntry.OrderBy.name, true);
		List<CatalogEntry> nodes = new ArrayList<>();
		List<CatalogEntry> rootLauncher = new ArrayList<>();
		for (CatalogEntry child : children) {
			if (child.getType() == CatalogEntry.TYPE_NODE) {
				nodes.add(child);
			} else if (child.getType() == CatalogEntry.TYPE_LEAF) {
				if (taxonomyLevel != null) {
					repositoryEntryToTaxonomyLevelDao.createRelation(child.getRepositoryEntry(), taxonomyLevel);
				} else {
					rootLauncher.add(child);
				}
			}
		}
		
		// Create a launcher for the repository entries of the root catalog entry
		if (!rootLauncher.isEmpty()) {
			createRootEntriesLauncher(catalogEntry, rootLauncher);
		}
		
		dbInstance.commit();
		
		// Migration of the child nodes
		boolean categorySortingManually = catalogManager.isCategorySortingManually(catalogEntry);
		if (categorySortingManually) {
			nodes.sort(Comparator.comparingInt(CatalogEntry::getPosition));
		}
		
		int counter = 0;
		for (CatalogEntry nodeEntry : nodes) {
			Integer childSortOder = categorySortingManually ? Integer.valueOf(counter++): null;
			migrateCatalogEntry(executor, nodeEntry, taxonomy, taxonomyLevel, childSortOder);
		}
	}

	private void createRootEntriesLauncher(CatalogEntry catalogEntry, List<CatalogEntry> rootLauncher) {
		Comparator<CatalogEntry> catalogEntryComparator = catalogManager.isEntrySortingManually(catalogEntry)
				? Comparator.comparingInt(CatalogEntry::getPosition)
				: (c1, c2) -> c1.getRepositoryEntry().getDisplayname().compareToIgnoreCase(c2.getRepositoryEntry().getDisplayname());
				
		List<RepositoryEntry> repositoryEntries = rootLauncher.stream()
				.filter(ce -> ce.getRepositoryEntry() != null && RepositoryEntryStatusEnum.isInArray(ce.getRepositoryEntry().getEntryStatus(), RepositoryEntryStatusEnum.preparationToClosed()))
				.sorted(catalogEntryComparator)
				.map(CatalogEntry::getRepositoryEntry)
				.collect(Collectors.toList());
		
		String launcherIdentifier = catalogService.createLauncherIdentifier();
		CatalogLauncher catalogLauncher = catalogService.createCatalogLauncher(StaticHandler.TYPE, launcherIdentifier);
		catalogLauncher.setEnabled(true);
		catalogLauncher.setConfig(staticHandler.getConfig(repositoryEntries));
		catalogLauncher = catalogService.update(catalogLauncher);
		moveToTop(catalogLauncher);
		
		Locale overlayDefaultLocale = i18nModule.getOverlayLocales().get(I18nModule.getDefaultLocale());
		I18nItem displayNameItem = i18nManager.getI18nItem(CatalogV2UIFactory.class.getPackageName(), launcherIdentifier, overlayDefaultLocale);
		i18nManager.saveOrUpdateI18nItem(displayNameItem, catalogEntry.getName());
	}
	
	private void createLaunchers(CatalogEntry rootCatalogEntry, Taxonomy taxonomy) {
		createTaxonomyLevelLauncher(taxonomy);
		createTextLauncher(rootCatalogEntry);
	}

	private void createTaxonomyLevelLauncher(Taxonomy taxonomy) {
		TaxonomyLevelLauncherHandler.Config taxonomyLauncherConfig = new TaxonomyLevelLauncherHandler.Config();
		taxonomyLauncherConfig.setTaxonomyKey(taxonomy.getKey());
		
		String launcherIdentifier = catalogService.createLauncherIdentifier();
		CatalogLauncher catalogLauncher = catalogService.createCatalogLauncher(TaxonomyLevelLauncherHandler.TYPE, launcherIdentifier);
		catalogLauncher.setEnabled(true);
		catalogLauncher.setConfig(taxonomyLevelLauncherHandler.toXML(taxonomyLauncherConfig));
		catalogLauncher = catalogService.update(catalogLauncher);
		moveToTop(catalogLauncher);
		dbInstance.commit();
	}
	
	private void createTextLauncher(CatalogEntry rootCatalogEntry) {
		if (StringHelper.containsNonWhitespace(rootCatalogEntry.getDescription())) {
			String i18nSuffix = UUID.randomUUID().toString().replace("-", "");
			Locale overlayDefaultLocale = i18nModule.getOverlayLocales().get(I18nModule.getDefaultLocale());
			I18nItem descriptionItem = i18nManager.getI18nItem(CatalogLauncherTextEditController.BUNDLE_NAME, I18N_PREFIX + i18nSuffix, overlayDefaultLocale);
			i18nManager.saveOrUpdateI18nItem(descriptionItem, rootCatalogEntry.getDescription());
			
			TextLauncherHandler.Config textLauncherConfig = new TextLauncherHandler.Config();
			textLauncherConfig.setI18nSuffix(i18nSuffix);
			
			String launcherIdentifier = catalogService.createLauncherIdentifier();
			CatalogLauncher catalogLauncher = catalogService.createCatalogLauncher(TextLauncherHandler.TYPE, launcherIdentifier);
			catalogLauncher.setEnabled(true);
			catalogLauncher.setConfig(textLauncherHandler.toXML(textLauncherConfig));
			catalogLauncher = catalogService.update(catalogLauncher);
			moveToTop(catalogLauncher);
			dbInstance.commit();
		}
	}
	
	private void moveToTop(CatalogLauncher catalogLauncher) {
		int targetSortOrder = 1;
		CatalogLauncherSearchParams searchParams = new CatalogLauncherSearchParams();
		List<CatalogLauncher> catalogLaunchers = catalogService.getCatalogLaunchers(searchParams);
		if (!catalogLaunchers.isEmpty()) {
			targetSortOrder = catalogLaunchers.stream().sorted().findFirst().get().getSortOrder();
		}
		
		int currentSortOrder = catalogLauncher.getSortOrder();
		while (currentSortOrder > targetSortOrder) {
			catalogService.doMove(catalogLauncher, true);
			currentSortOrder = catalogService.getCatalogLauncher(catalogLauncher).getSortOrder();
		}
	}
	
	private void createFilter() {
		boolean filterPresent = catalogService.getCatalogFilters(new CatalogFilterSearchParams()).stream()
				.anyMatch(filter -> TaxonomyLevelChildrenHandler.TYPE.equals(filter.getType()));
		if (filterPresent) return;
		
		CatalogFilter catalogFilter = catalogService.createCatalogFilter(TaxonomyLevelChildrenHandler.TYPE);
		catalogFilter.setEnabled(true);
		catalogFilter.setDefaultVisible(true);
		catalogFilter.setConfig(TaxonomyLevelChildrenHandler.KEY_HIDE);
		catalogFilter = catalogService.update(catalogFilter);
		dbInstance.commit();
	}


}
