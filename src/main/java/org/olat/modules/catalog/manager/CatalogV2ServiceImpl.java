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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.olat.basesecurity.OrganisationDataDeletable;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.modules.catalog.CatalogFilter;
import org.olat.modules.catalog.CatalogFilterHandler;
import org.olat.modules.catalog.CatalogFilterRef;
import org.olat.modules.catalog.CatalogFilterSearchParams;
import org.olat.modules.catalog.CatalogLauncher;
import org.olat.modules.catalog.CatalogLauncherHandler;
import org.olat.modules.catalog.CatalogLauncherRef;
import org.olat.modules.catalog.CatalogLauncherSearchParams;
import org.olat.modules.catalog.CatalogLauncherToOrganisation;
import org.olat.modules.catalog.CatalogRepositoryEntry;
import org.olat.modules.catalog.CatalogRepositoryEntrySearchParams;
import org.olat.modules.catalog.CatalogSearchTerm;
import org.olat.modules.catalog.CatalogV2Service;
import org.olat.modules.catalog.model.CatalogRepositoryEntryImpl;
import org.olat.modules.catalog.model.CatalogSearchTermImpl;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyModule;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 25 May 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CatalogV2ServiceImpl implements CatalogV2Service, OrganisationDataDeletable {
	
	@Autowired
	private List<CatalogLauncherHandler> catalogLauncherHandlers;
	private Map<String, CatalogLauncherHandler> typeToCatalogLauncherHandler;
	@Autowired
	private List<CatalogFilterHandler> catalogFilterHandlers;
	private Map<String, CatalogFilterHandler> typeToCatalogFilterHandler;
	
	@Autowired
	private CatalogRepositoryEntryQueries queries;
	@Autowired
	private CatalogLauncherDAO catalogLauncherDao;
	@Autowired
	private CatalogLauncherToOrganisationDAO catalogLauncherToOrganisationDAO;
	@Autowired
	private CatalogFilterDAO catalogFilterDao;
	@Autowired
	private AccessControlModule acModule;
	@Autowired
	private ACService acService;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private TaxonomyModule taxonomyModule;
	@Autowired
	private I18nManager i18nManager;
	
	@PostConstruct
	public void initHandlers() {
		typeToCatalogLauncherHandler = catalogLauncherHandlers.stream()
				.collect(Collectors.toMap(CatalogLauncherHandler::getType, Function.identity()));
		typeToCatalogFilterHandler = catalogFilterHandlers.stream()
				.collect(Collectors.toMap(CatalogFilterHandler::getType, Function.identity()));
	}

	@Override
	public Integer countRepositoryEntries(CatalogRepositoryEntrySearchParams searchParams) {
		return queries.countRepositoryEntries(searchParams);
	}
	
	@Override
	public List<String> getTaxonomyLevelPathKeysWithOffers(CatalogRepositoryEntrySearchParams searchParams) {
		return queries.loadTaxonomyLevelPathKeysWithOffers(searchParams);
	}
	
	@Override
	public void excludeLevelsWithoutOffers(List<TaxonomyLevel> taxonomyLevels, CatalogRepositoryEntrySearchParams searchParams) {
		if (taxonomyLevels == null) return;
		
		List<String> taxonomyLevelKeyPathsWithOffers = getTaxonomyLevelPathKeysWithOffers(searchParams);
		taxonomyLevels.removeIf(taxonomyLevel ->  hasNoOffer(taxonomyLevelKeyPathsWithOffers, taxonomyLevel));
	}
	
	private boolean hasNoOffer(List<String> taxonomyLevelKeyPathsWithOffers, TaxonomyLevel taxonomyLevel) {
		String materializedPathKeys = taxonomyLevel.getMaterializedPathKeys();
		for (String keyPath : taxonomyLevelKeyPathsWithOffers) {
			if (keyPath.indexOf(materializedPathKeys) > -1 ) {
				return false;
			}
		}
		return true;
	}

	@Override
	public List<CatalogRepositoryEntry> getRepositoryEntries(CatalogRepositoryEntrySearchParams searchParams, int firstResult, int maxResults) {
		// Load RepositoryEntry
		List<RepositoryEntry> repositoryEntries = queries.loadRepositoryEntries(searchParams, firstResult, maxResults);
		
		// Load Taxonomy
		Map<RepositoryEntryRef, List<TaxonomyLevel>> reToTaxonomyLevels = loadRepositoryEntryToTaxonomyLevels(repositoryEntries);
		
		// Load membership
		List<Long> reMembershipKeys = loadRepositoryEntryMembershipKeys(repositoryEntries, searchParams.getMember());
		
		// Load open access
		List<Long> reOpenAccessKeys = loadRepositoryEntryOpenAccessKeys(repositoryEntries, searchParams.getOfferOrganisations());
		
		// Load access methods
		Map<RepositoryEntry, List<OLATResourceAccess>> reToResourceAccess = loadRepositoryEntryToResourceAccess(repositoryEntries, searchParams.getOfferOrganisations());
		
		
		List<CatalogRepositoryEntry> views = new ArrayList<>(repositoryEntries.size());
		for (RepositoryEntry repositoryEntry : repositoryEntries) {
			CatalogRepositoryEntryImpl view = new CatalogRepositoryEntryImpl(repositoryEntry);
			
			List<TaxonomyLevel> levels = reToTaxonomyLevels.get(repositoryEntry);
			view.setTaxonomyLevels(levels != null ? new HashSet<>(levels): null);
			
			view.setMember(reMembershipKeys.contains(view.getKey()));
			
			view.setOpenAccess(reOpenAccessKeys.contains(view.getKey()));
			
			List<OLATResourceAccess> resourceAccess = reToResourceAccess.get(repositoryEntry);
			view.setResourceAccess(resourceAccess);
			
			views.add(view);
		}
		
		return views;
	}

	private Map<RepositoryEntryRef, List<TaxonomyLevel>> loadRepositoryEntryToTaxonomyLevels(List<RepositoryEntry> repositoryEntries) {
		Map<RepositoryEntryRef, List<TaxonomyLevel>> reToTaxonomyLevels;
		if (!repositoryEntries.isEmpty() && taxonomyModule.isEnabled() && !repositoryModule.getTaxonomyRefs().isEmpty()) {
			reToTaxonomyLevels = repositoryService.getTaxonomy(repositoryEntries, false);
		} else {
			reToTaxonomyLevels = Collections.emptyMap();
		}
		return reToTaxonomyLevels;
	}
	
	private List<Long> loadRepositoryEntryMembershipKeys(List<RepositoryEntry> repositoryEntries, Identity identity) {
		if (identity == null) return Collections.emptyList();
		
		List<Long> reMembershipKeys = repositoryEntries.stream().map(RepositoryEntry::getKey).collect(Collectors.toList());
		repositoryService.filterMembership(identity, reMembershipKeys);
		return reMembershipKeys;
	}

	private List<Long> loadRepositoryEntryOpenAccessKeys(List<RepositoryEntry> repositoryEntries,
			List<? extends OrganisationRef> offerOrganisations) {
		if (!acModule.isEnabled()) return Collections.emptyList();
		
		List<OLATResource> resourcesWithAC = repositoryEntries.stream()
				.filter(RepositoryEntry::isPublicVisible)
				.map(RepositoryEntry::getOlatResource)
				.collect(Collectors.toList());
		List<OLATResource> resourceWithOpenAccess = acService.filterResourceWithOpenAccess(resourcesWithAC, offerOrganisations);
		
		return repositoryEntries.stream()
				.filter(re -> resourceWithOpenAccess.contains(re.getOlatResource()))
				.map(RepositoryEntry::getKey)
				.collect(Collectors.toList());
	}

	private Map<RepositoryEntry, List<OLATResourceAccess>> loadRepositoryEntryToResourceAccess(
			List<RepositoryEntry> repositoryEntries, List<? extends OrganisationRef> offerOrganisations) {
		if (!acModule.isEnabled()) return Collections.emptyMap();
		
		List<OLATResource> resourcesWithAC = repositoryEntries.stream()
				.filter(RepositoryEntry::isPublicVisible)
				.map(RepositoryEntry::getOlatResource)
				.collect(Collectors.toList());
		Map<OLATResource, List<OLATResourceAccess>> resourceToAccess = acService.filterResourceWithAC(resourcesWithAC, offerOrganisations).stream()
				.collect(Collectors.groupingBy(OLATResourceAccess::getResource));
		
		Map<RepositoryEntry, List<OLATResourceAccess>> reToResourceAccess = new HashMap<>(repositoryEntries.size());
		for (RepositoryEntry repositoryEntry : repositoryEntries) {
			List<OLATResourceAccess> resourceAccess = resourceToAccess.getOrDefault(repositoryEntry.getOlatResource(), Collections.emptyList());
			reToResourceAccess.put(repositoryEntry, resourceAccess);
		}
		
		return reToResourceAccess;
	}
	
	@Override
	public List<CatalogSearchTerm> getSearchTems(String queryString, Locale locale) {
		List<CatalogSearchTerm> searchTerms = null;
		
		if (StringHelper.containsNonWhitespace(queryString)) {
			String[] queryParts = queryString.split(" ");
			searchTerms = new ArrayList<>(queryParts.length);
			for (String queryPart : queryParts) {
				if (StringHelper.containsNonWhitespace(queryPart)) {
					Set<String> taxonomyLevelI18nSuffix = null;
					if (taxonomyModule.isEnabled()) {
						taxonomyLevelI18nSuffix = i18nManager
						.findI18nKeysByOverlayValue(queryPart, TaxonomyUIFactory.PREFIX_DISPLAY_NAME, locale,
								TaxonomyUIFactory.BUNDLE_NAME, false)
						.stream().map(key -> key.substring(TaxonomyUIFactory.PREFIX_DISPLAY_NAME.length()))
						.collect(Collectors.toSet());
					}
					searchTerms.add(new CatalogSearchTermImpl(queryPart, taxonomyLevelI18nSuffix));
				}
			}
		}
		
		return searchTerms;
	}

	@Override
	public List<CatalogLauncherHandler> getCatalogLauncherHandlers() {
		return catalogLauncherHandlers;
	}

	@Override
	public CatalogLauncherHandler getCatalogLauncherHandler(String type) {
		return typeToCatalogLauncherHandler.get(type);
	}

	@Override
	public String createLauncherIdentifier() {
		return UUID.randomUUID().toString().toLowerCase().replace("-", "");
	}

	@Override
	public CatalogLauncher createCatalogLauncher(String type, String identifier) {
		return catalogLauncherDao.create(type, identifier);
	}

	@Override
	public CatalogLauncher update(CatalogLauncher catalogLauncher) {
		return catalogLauncherDao.save(catalogLauncher);
	}
	
	@Override
	public void updateLauncherOrganisations(CatalogLauncher catalogLauncher, Collection<Organisation> organisations) {
		if (organisations == null || organisations.isEmpty()) {
			catalogLauncherToOrganisationDAO.delete(catalogLauncher);
			return;
		}
		
		List<CatalogLauncherToOrganisation> currentRelations = catalogLauncherToOrganisationDAO.loadRelations(catalogLauncher, null);
		List<Organisation> currentOrganisations = currentRelations.stream()
				.map(CatalogLauncherToOrganisation::getOrganisation)
				.collect(Collectors.toList());
		
		// Create relation for new organisations
		organisations.stream()
				.filter(org -> !currentOrganisations.contains(org))
				.forEach(org -> catalogLauncherToOrganisationDAO.createRelation(catalogLauncher, org));

		// Create relation of old organisations
		currentRelations.stream()
				.filter(rel -> !organisations.contains(rel.getOrganisation()))
				.forEach(rel -> catalogLauncherToOrganisationDAO.delete(rel));
	}
	
	@Override
	public void doMove(CatalogLauncherRef catalogLauncherRef, boolean up) {
		CatalogLauncher catalogLauncher = catalogLauncherDao.loadByKey(catalogLauncherRef);
		if (catalogLauncher == null) return;
		
		int sortOrder = catalogLauncher.getSortOrder();
		CatalogLauncher swapCatalogLauncher = catalogLauncherDao.loadNext(sortOrder, up, null);
		if (swapCatalogLauncher == null) return;
		int swapSortOrder = swapCatalogLauncher.getSortOrder();
		
		catalogLauncher.setSortOrder(swapSortOrder);
		swapCatalogLauncher.setSortOrder(sortOrder);
		catalogLauncherDao.save(catalogLauncher);
		catalogLauncherDao.save(swapCatalogLauncher);
	}

	@Override
	public void deleteCatalogLauncher(CatalogLauncherRef catalogLauncher) {
		CatalogLauncher reloadedLancher = getCatalogLauncher(catalogLauncher);
		if (reloadedLancher != null) {
			CatalogLauncherHandler handler = getCatalogLauncherHandler(reloadedLancher.getType());
			if (handler != null) {
				handler.deleteLauncherData(reloadedLancher);
			}
			catalogLauncherToOrganisationDAO.delete(reloadedLancher);
			catalogLauncherDao.delete(reloadedLancher);
		}
	}
	
	@Override
	public CatalogLauncher getCatalogLauncher(CatalogLauncherRef catalogLauncher) {
		return catalogLauncherDao.loadByKey(catalogLauncher);
	}

	@Override
	public List<CatalogLauncher> getCatalogLaunchers(CatalogLauncherSearchParams searchParams) {
		return catalogLauncherDao.load(searchParams);
	}
	
	@Override
	public List<Organisation> getCatalogLauncherOrganisations(CatalogLauncherRef catalogLauncher) {
		return catalogLauncherToOrganisationDAO.loadOrganisations(catalogLauncher);
	}

	@Override
	public List<CatalogFilterHandler> getCatalogFilterHandlers() {
		return catalogFilterHandlers;
	}

	@Override
	public CatalogFilterHandler getCatalogFilterHandler(String type) {
		return typeToCatalogFilterHandler.get(type);
	}

	@Override
	public CatalogFilter createCatalogFilter(String type) {
		return catalogFilterDao.create(type);
	}

	@Override
	public CatalogFilter update(CatalogFilter catalogFilter) {
		return catalogFilterDao.save(catalogFilter);
	}
	
	@Override
	public void doMove(CatalogFilterRef catalogFilterRef, boolean up) {
		CatalogFilter catalogFilter = catalogFilterDao.loadByKey(catalogFilterRef);
		if (catalogFilter == null) return;
		
		int sortOrder = catalogFilter.getSortOrder();
		CatalogFilter swapCatalogFilter = catalogFilterDao.loadNext(sortOrder, up, null);
		if (swapCatalogFilter == null) return;
		int swapSortOrder = swapCatalogFilter.getSortOrder();
		
		catalogFilter.setSortOrder(swapSortOrder);
		swapCatalogFilter.setSortOrder(sortOrder);
		catalogFilterDao.save(catalogFilter);
		catalogFilterDao.save(swapCatalogFilter);
	}

	@Override
	public void deleteCatalogFilter(CatalogFilterRef catalogFilter) {
		catalogFilterDao.delete(catalogFilter);
	}
	
	@Override
	public CatalogFilter getCatalogFilter(CatalogFilterRef catalogFilter) {
		return catalogFilterDao.loadByKey(catalogFilter);
	}

	@Override
	public List<CatalogFilter> getCatalogFilters(CatalogFilterSearchParams searchParams) {
		return catalogFilterDao.load(searchParams);
	}

	@Override
	public boolean deleteOrganisationData(Organisation organisation, Organisation replacementOrganisation) {
		catalogLauncherToOrganisationDAO.loadRelations(null, organisation).stream()
				.map(CatalogLauncherToOrganisation::getLauncher)
				.distinct()
				.forEach(launcher -> deleteOrganisationData(launcher, organisation, replacementOrganisation));
		return true;
	}

	private void deleteOrganisationData(CatalogLauncher launcher, Organisation organisation, Organisation replacementOrganisation) {
		List<Organisation> organisations = catalogLauncherToOrganisationDAO.loadOrganisations(launcher);
		organisations.remove(organisation);
		if (replacementOrganisation != null && !organisations.contains(replacementOrganisation)) {
			organisations.add(replacementOrganisation);
		}
		updateLauncherOrganisations(launcher, organisations);
		
		// Deactivate launcher so that it is not visible to all users
		if (organisations.isEmpty()) {
			CatalogLauncher reloadedLauncher = getCatalogLauncher(launcher);
			reloadedLauncher.setEnabled(false);
			update(reloadedLauncher);
		}
	}

}
