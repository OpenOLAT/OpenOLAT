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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.olat.basesecurity.OrganisationDataDeletable;
import org.olat.core.commons.services.commentAndRating.manager.UserRatingsDAO;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.ResourceLicense;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.util.resource.Resourceable;
import org.olat.modules.catalog.CatalogEntry;
import org.olat.modules.catalog.CatalogEntrySearchParams;
import org.olat.modules.catalog.CatalogFilter;
import org.olat.modules.catalog.CatalogFilterHandler;
import org.olat.modules.catalog.CatalogFilterRef;
import org.olat.modules.catalog.CatalogFilterSearchParams;
import org.olat.modules.catalog.CatalogLauncher;
import org.olat.modules.catalog.CatalogLauncherHandler;
import org.olat.modules.catalog.CatalogLauncherRef;
import org.olat.modules.catalog.CatalogLauncherSearchParams;
import org.olat.modules.catalog.CatalogLauncherToOrganisation;
import org.olat.modules.catalog.CatalogV2Service;
import org.olat.modules.catalog.model.CatalogEntryImpl;
import org.olat.modules.catalog.model.RepositoryEntryInfos;
import org.olat.modules.creditpoint.CreditPointFormat;
import org.olat.modules.creditpoint.RepositoryEntryCreditPointConfiguration;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryEntryLicenseHandler;
import org.olat.repository.model.RepositoryEntryStatistics;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.resource.accesscontrol.manager.ACReservationDAO;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;
import org.olat.resource.accesscontrol.model.SearchReservationParameters;
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
	private CatalogQueries queries;
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
	private UserRatingsDAO userRatingsDao;
	@Autowired
	private ACReservationDAO reservationDao;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private TaxonomyModule taxonomyModule;
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private RepositoryEntryLicenseHandler repositoryEntryLicenseHandler;
	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private CurriculumService curriculumService;
	
	@PostConstruct
	public void initHandlers() {
		typeToCatalogLauncherHandler = catalogLauncherHandlers.stream()
				.collect(Collectors.toMap(CatalogLauncherHandler::getType, Function.identity()));
		typeToCatalogFilterHandler = catalogFilterHandlers.stream()
				.collect(Collectors.toMap(CatalogFilterHandler::getType, Function.identity()));
	}
	
	@Override
	public void excludeLevelsWithoutEntries(List<TaxonomyLevel> taxonomyLevels, List<CatalogEntry> entries) {
		if (taxonomyLevels == null) return;
		
		Set<String> taxonomyLevelKeyPathsWithOffers = entries.stream()
				.map(CatalogEntry::getTaxonomyLevels)
				.filter(Objects::nonNull)
				.flatMap(Set::stream)
				.map(TaxonomyLevel::getMaterializedPathKeys)
				.collect(Collectors.toSet());
		
		taxonomyLevels.removeIf(taxonomyLevel ->  hasNoOffer(taxonomyLevelKeyPathsWithOffers, taxonomyLevel));
	}
	
	private boolean hasNoOffer(Collection<String> taxonomyLevelKeyPathsWithOffers, TaxonomyLevel taxonomyLevel) {
		String materializedPathKeys = taxonomyLevel.getMaterializedPathKeys();
		for (String keyPath : taxonomyLevelKeyPathsWithOffers) {
			if (keyPath.indexOf(materializedPathKeys) > -1 ) {
				return false;
			}
		}
		return true;
	}

	@Override
	public List<CatalogEntry> getCatalogEntries(CatalogEntrySearchParams searchParams) {
		// RepositoryEntries
		List<RepositoryEntryInfos> repositoryEntriesInfos = queries.loadRepositoryEntries(searchParams);
		
		Map<RepositoryEntryRef, List<TaxonomyLevel>> reToTaxonomyLevels = loadRepositoryEntryToTaxonomyLevels(repositoryEntriesInfos);
		
		List<Long> reMembershipKeys = loadRepositoryEntryMembershipKeys(repositoryEntriesInfos, searchParams.getMember());
		
		List<UserRating> ratings = searchParams.getMember() == null || !repositoryModule.isRatingEnabled()
				? List.of()
				: userRatingsDao.getAllRatings(searchParams.getMember());
		Map<Long,Integer> ratingsMap = ratings.stream()
				.filter(rating -> "RepositoryEntry".equals(rating.getResName()))
				.collect(Collectors.toMap(UserRating::getResId, UserRating::getRating, (u, v) -> u));
		
		Map<Resourceable, ResourceLicense> licenses = null;
		if (licenseModule.isEnabled(repositoryEntryLicenseHandler)) {
			licenses = loadLicenses(repositoryEntriesInfos);
		}
		
		List<CatalogEntry> catalogEntries = new ArrayList<>(repositoryEntriesInfos.size());
		List<OLATResource> resourcesWithAC = new ArrayList<>(repositoryEntriesInfos.size());
		for (RepositoryEntryInfos repositoryEntryInfos : repositoryEntriesInfos) {
			RepositoryEntry repositoryEntry = repositoryEntryInfos.entry();
			RepositoryEntryStatistics statistics = repositoryEntryInfos.statistics();
			CatalogEntryImpl catalogEntry = new CatalogEntryImpl(repositoryEntry, statistics);
			
			List<TaxonomyLevel> levels = reToTaxonomyLevels.get(repositoryEntryInfos);
			catalogEntry.setTaxonomyLevels(levels != null ? new HashSet<>(levels): null);
			
			catalogEntry.setMember(reMembershipKeys.contains(catalogEntry.getRepositoryEntryKey()));
			
			if (licenses != null && !licenses.isEmpty()) {
				ResourceLicense license = licenses.get(new Resourceable(repositoryEntry.getOlatResource()));
				catalogEntry.setLicense(license);
			}
			
			catalogEntries.add(catalogEntry);
			
			if (repositoryEntry.isPublicVisible()) {
				resourcesWithAC.add(repositoryEntry.getOlatResource());
			}
			
			Integer rating = ratingsMap.get(repositoryEntry.getKey());
			catalogEntry.setMyRating(rating);
			
			boolean hasCertificate = repositoryEntryInfos.certificateConfiguration() != null
					&& repositoryEntryInfos.certificateConfiguration().isCertificateEnabled();
			catalogEntry.setHasCertificate(hasCertificate);
			
			RepositoryEntryCreditPointConfiguration creditPointConf = repositoryEntryInfos.creditPointConfiguration();
			String amount = creditPointConf == null || creditPointConf.getCreditPoints() == null
					? null
					: CreditPointFormat.format(creditPointConf.getCreditPoints(), creditPointConf.getCreditPointSystem());
			catalogEntry.setCreditPointAmount(amount);
		}
		
		// CurriculumElements
		List<OLATResource> ceResourcesWithAC = List.of();
		if (curriculumModule.isEnabled()) {
			List<CurriculumElement> curriculumElements = queries.loadCurriculumElements(searchParams);
			
			ceResourcesWithAC = new ArrayList<>(curriculumElements.size());
			
			Map<Long, List<TaxonomyLevel>> ceKeyTaxonomyLevels = loadCurriculumElementToTaxonomyLevels(curriculumElements);
			Map<Long, Long> ceKeyToNumParticipants = curriculumService.getCurriculumElementKeyToNumParticipants(curriculumElements, true);
			Map<Long, RepositoryEntry> ceKeySingleCourse = loadCurriculumElementToSingleCourse(curriculumElements);
			Set<Long> ceMembershipKeys = loadCurriculumElementMembershipKeys(curriculumElements, searchParams.getMember());
			
			for (CurriculumElement curriculumElement : curriculumElements) {
				CatalogEntryImpl catalogEntry = new CatalogEntryImpl(curriculumElement);
				
				List<TaxonomyLevel> levels = ceKeyTaxonomyLevels.get(curriculumElement.getKey());
				catalogEntry.setTaxonomyLevels(levels != null ? new HashSet<>(levels): null);
				catalogEntry.setNumParticipants(ceKeyToNumParticipants.get(curriculumElement.getKey()));
				catalogEntry.setSingleCourse(ceKeySingleCourse.get(curriculumElement.getKey()));
				catalogEntry.setMember(ceMembershipKeys.contains(curriculumElement.getKey()));
				
				catalogEntries.add(catalogEntry);
				resourcesWithAC.add(curriculumElement.getResource());
			}
		}
		
		// Access (CurriculumElement has never open or guest access)
		List<OLATResource> oresOpenAccess = loadResourceOpenAccess(resourcesWithAC, searchParams.isWebPublish(), searchParams.getOfferOrganisations());
		List<OLATResource> oresGuestAccess = loadResourceGuestAccess(resourcesWithAC, searchParams.isWebPublish());
		
		resourcesWithAC.addAll(ceResourcesWithAC);
		Map<OLATResource, List<OLATResourceAccess>> resourceToResourceAccess = getResourceToResourceAccess(resourcesWithAC, searchParams.getOfferOrganisations());
		Set<OLATResource> resourceWithReservation = loadResourceWithReservation(resourcesWithAC, searchParams.getMember());
		
		for (CatalogEntry entry : catalogEntries) {
			if (entry instanceof CatalogEntryImpl catalogEntry) {
				if (catalogEntry.getRepositoryEntryKey() != null) {
					catalogEntry.setOpenAccess(oresOpenAccess.contains(catalogEntry.getOlatResource()));
					catalogEntry.setGuestAccess(oresGuestAccess.contains(catalogEntry.getOlatResource()));
				}
				
				List<OLATResourceAccess> resourceAccess = resourceToResourceAccess.getOrDefault(catalogEntry.getOlatResource(), List.of());
				catalogEntry.setResourceAccess(resourceAccess);
				
				if (resourceWithReservation.contains(catalogEntry.getOlatResource())) {
					catalogEntry.setReservationAvailable(true);
				}
			}
		}
		
		return catalogEntries;
	}

	private Map<RepositoryEntryRef, List<TaxonomyLevel>> loadRepositoryEntryToTaxonomyLevels(List<RepositoryEntryInfos> repositoryEntries) {
		Map<RepositoryEntryRef, List<TaxonomyLevel>> reToTaxonomyLevels;
		if (!repositoryEntries.isEmpty() && taxonomyModule.isEnabled() && !repositoryModule.getTaxonomyRefs().isEmpty()) {
			reToTaxonomyLevels = repositoryService.getTaxonomy(repositoryEntries, false);
		} else {
			reToTaxonomyLevels = Map.of();
		}
		return reToTaxonomyLevels;
	}
	
	private List<Long> loadRepositoryEntryMembershipKeys(List<RepositoryEntryInfos> repositoryEntries, Identity identity) {
		if (identity == null) return List.of();
		
		List<Long> reMembershipKeys = repositoryEntries.stream().map(RepositoryEntryRef::getKey).collect(Collectors.toList());
		repositoryService.filterMembership(identity, reMembershipKeys);
		return reMembershipKeys;
	}
	
	private Map<Long, List<TaxonomyLevel>> loadCurriculumElementToTaxonomyLevels(List<CurriculumElement> curriculumElements) {
		if (!curriculumElements.isEmpty() && taxonomyModule.isEnabled()) {
			return curriculumService.getCurriculumElementKeyToTaxonomyLevels(curriculumElements);
		}
		return Map.of();
	}

	private Map<Long, RepositoryEntry> loadCurriculumElementToSingleCourse(List<CurriculumElement> curriculumElements) {
		if (curriculumElements == null || curriculumElements.isEmpty()) {
			return Map.of();
		}
		
		List<CurriculumElement> singleCourseElements = curriculumElements.stream()
				.filter(CurriculumElement::isSingleCourseImplementation)
				.toList();
		if (singleCourseElements.isEmpty()) {
			return Map.of();
		}
		
		return curriculumService.getCurriculumElementKeyToRepositoryEntries(singleCourseElements).entrySet().stream()
				.filter(curriculumElementKeyToRepositoryEntries -> curriculumElementKeyToRepositoryEntries.getValue().size() == 1)
				.collect(Collectors.toMap(
						Entry::getKey,
						entry -> new ArrayList<>(entry.getValue()).get(0)));
	}

	private Set<Long> loadCurriculumElementMembershipKeys(List<CurriculumElement> curriculumElements, Identity member) {
		if (!curriculumElements.isEmpty() && member != null) {
			
			return curriculumService.getCurriculumElementMemberships(curriculumElements, member)
					.stream()
					.map(CurriculumElementMembership::getCurriculumElementKey)
					.collect(Collectors.toSet());
		}
		return Set.of();
	}

	private List<OLATResource> loadResourceOpenAccess(List<OLATResource> resourcesWithAC,
			boolean webCatalog, List<? extends OrganisationRef> offerOrganisations) {
		if (!acModule.isEnabled()) return List.of();
		
		Boolean webPublish = webCatalog? Boolean.TRUE: null;
		return acService.filterResourceWithOpenAccess(resourcesWithAC, webPublish, offerOrganisations);
	}
	
	private List<OLATResource> loadResourceGuestAccess(List<OLATResource> resourcesWithAC, boolean webPublish) {
		if (!acModule.isEnabled()) return List.of();
		
		if (webPublish) {
			return acService.filterResourceWithGuestAccess(resourcesWithAC);
		}
		
		// In internal catalog guest and other access are never displayed mixed.
		return List.of();
	}

	@Override
	public Map<OLATResource, List<OLATResourceAccess>> getResourceToResourceAccess(
			List<OLATResource> resource, List<? extends OrganisationRef> offerOrganisations) {
		if (!acModule.isEnabled()) return Collections.emptyMap();
		
		return acService.filterResourceWithAC(resource, offerOrganisations).stream()
				.collect(Collectors.groupingBy(OLATResourceAccess::getResource));
	}
	
	private Set<OLATResource> loadResourceWithReservation( List<OLATResource> resourcesWithAC, Identity identity) {
		if (!acModule.isEnabled() || identity == null) return Set.of();
		
		SearchReservationParameters searchParams = new SearchReservationParameters(resourcesWithAC);
		searchParams.setIdentities(List.of(identity));
		return reservationDao.loadReservations(searchParams).stream()
				.map(ResourceReservation::getResource)
				.collect(Collectors.toSet());
	}
	
	private Map<Resourceable,ResourceLicense> loadLicenses(List<RepositoryEntryInfos> repositoryEntries) {
		Collection<? extends OLATResourceable> resources = repositoryEntries.stream().map(RepositoryEntryInfos::getOlatResource).toList();
		List<ResourceLicense> licenses = licenseService.loadLicenses(resources);
		return licenses.stream().collect(Collectors
				.toMap(license -> new Resourceable(license.getResName(), license.getResId()), license -> license, (u, v) -> v));
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
