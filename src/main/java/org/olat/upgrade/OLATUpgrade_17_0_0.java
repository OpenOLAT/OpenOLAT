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
package org.olat.upgrade;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Organisation;
import org.olat.core.logging.Tracing;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.CatalogManager;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.Offer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22.04.2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_17_0_0 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_17_0_0.class);

	private static final String VERSION = "OLAT_17.0.0";
	private static final String RE_PUBLIC_VISIBILE_INIT = "RE PUBLIC VISIBILE INIT";
	private static final String OFFER_TO_ORG_INIT = "OFFER TO ORG INIT";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private ACService acService;
	@Autowired
	private OrganisationService organisationServics;
	@Autowired
	private CatalogManager catalogV1Manager;
	
	public OLATUpgrade_17_0_0() {
		super();
	}
	
	@Override
	public String getVersion() {
		return VERSION;
	}
	
	@Override
	public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (uhd == null) {
			// has never been called, initialize
			uhd = new UpgradeHistoryData();
		} else if (uhd.isInstallationComplete()) {
			return false;
		}
		
		boolean allOk = true;
		
		allOk &= initRePublicVisible(upgradeManager, uhd);
		allOk &= initOfferToOrgs(upgradeManager, uhd);
		
		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_17_0_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_17_0_0 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	private boolean initRePublicVisible(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(RE_PUBLIC_VISIBILE_INIT)) {
			try {
				log.info("Start re public visible initialization.");
				initRePublicVisible();
				log.info("All re public visible initialized.");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(RE_PUBLIC_VISIBILE_INIT, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	@SuppressWarnings("deprecation")
	private void initRePublicVisible() {
		Set<Organisation> rootOrganisations = getRootOrganisations();
		List<RepositoryEntry> entries = getPublicVisibleEntries();
		
		AtomicInteger migrationCounter = new AtomicInteger(0);
		for (RepositoryEntry entry : entries) {
			if (entry.isBookable() || RepositoryEntryStatusEnum.isInArray(entry.getEntryStatus(), RepositoryEntryStatusEnum.preparationToClosed())) {
				entry.setPublicVisible(true);
				dbInstance.getCurrentEntityManager().merge(entry);
				if (RepositoryEntryStatusEnum.isInArray(entry.getEntryStatus(), RepositoryEntryStatusEnum.preparationToClosed())) {
					if (entry.isAllUsers()) {
						initOpenAccess(entry, rootOrganisations);
					}
					if (entry.isGuests()) {
						initGuestAccess(entry);
					}
				}
				migrationCounter.incrementAndGet();
				dbInstance.commitAndCloseSession();
				if(migrationCounter.get() % 100 == 0) {
					log.info("Init re public visible: num. of offers: {}", migrationCounter);
				}
			}
		}
	}
	private Set<Organisation> getRootOrganisations() {
		return organisationServics.getOrganisations().stream()
				.filter(org -> org.getParent() == null)
				.collect(Collectors.toSet());
	}

	private List<RepositoryEntry> getPublicVisibleEntries() {
		StringBuilder sb = new StringBuilder();
		sb.append("select re from repositoryentry re")
		  .append(" where re.allUsers is true or re.guests is true or re.bookable is true");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.getResultList();
	}
	
	private void initOpenAccess(RepositoryEntry entry, Set<Organisation> rootOrganisations) {
		Offer offer= acService.getOffers(entry, true, false, null, null).stream()
				.filter(Offer::isOpenAccess)
				.findFirst()
				.orElseGet(() -> {
					Offer newOffer = acService.createOffer(entry.getOlatResource(), entry.getDisplayname());
					newOffer.setOpenAccess(true);
					newOffer = acService.save(newOffer);
					return newOffer;
				});
		acService.updateOfferOrganisations(offer, rootOrganisations);
	}

	
	private void initGuestAccess(RepositoryEntry entry) {
		acService.getOffers(entry, true, false, null, null).stream()
				.filter(Offer::isGuestAccess)
				.findFirst()
				.orElseGet(() -> {
					Offer newOffer = acService.createOffer(entry.getOlatResource(), entry.getDisplayname());
					newOffer.setGuestAccess(true);
					newOffer = acService.save(newOffer);
					return newOffer;
				});
	}

	private boolean initOfferToOrgs(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(OFFER_TO_ORG_INIT)) {
			try {
				log.info("Start offer to org initialization.");
				initCourseElements();
				log.info("All offer to org initialized.");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(OFFER_TO_ORG_INIT, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private void initCourseElements() {
		List<Offer> offers = loadOffers();
		
		AtomicInteger migrationCounter = new AtomicInteger(0);
		for (Offer offer : offers) {
			initOfferToOrg(offer);
			updateOfferCatalog(offer);
			migrationCounter.incrementAndGet();
			dbInstance.commitAndCloseSession();
			if(migrationCounter.get() % 100 == 0) {
				log.info("Init offer to org: num. of offers: {}", migrationCounter);
			}
		}
	}

	private List<Offer> loadOffers() {
		StringBuilder sb = new StringBuilder();
		sb.append("select offer from acoffer offer")
		  .append(" left join fetch offer.resource resource");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Offer.class)
				.getResultList();
	}
	
	private void initOfferToOrg(Offer offer) {
		if (offer.getResource() == null || "BusinessGroup".equals(offer.getResource().getResourceableTypeName())) {
			return;
		}
		List<Organisation> organisations = getReOfferOrgansation(offer);
		
		if (organisations.isEmpty()) {
			log.warn("Offer has no organisation: {}, {}::{}, {}", offer.getKey(), offer.getResourceTypeName(),
					offer.getResourceId(), offer.getResourceDisplayName());
		}
		acService.updateOfferOrganisations(offer, organisations);
	}

	private List<Organisation> getReOfferOrgansation(Offer offer) {
		RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(offer.getResource(), false);
		if (repositoryEntry != null) {
			return repositoryService.getOrganisations(repositoryEntry);
		}
		return Collections.emptyList();
	}

	private void updateOfferCatalog(Offer offer) {
		boolean hasCatalogV1Entry = hasCatalogV1Entry(offer);
		offer.setCatalogPublish(hasCatalogV1Entry);
		offer.setCatalogWebPublish(false);
		acService.save(offer);
	}
	
	public boolean hasCatalogV1Entry(Offer offer) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select cei.key");
		sb.append("  from catalogentry as cei");
		sb.append("   inner join cei.repositoryEntry as re");
		sb.append("   inner join re.olatResource as resource");
		sb.and().append("resource.key = :resourceKey");
		
		return !dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("resourceKey", offer.getResource().getKey())
				.getResultList().isEmpty();
	}
	
}
