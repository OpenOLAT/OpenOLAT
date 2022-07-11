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

import static org.olat.modules.taxonomy.ui.TaxonomyUIFactory.BUNDLE_NAME;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Organisation;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.I18nItem;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.olat.ims.lti13.model.LTI13ToolDeploymentImpl;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.manager.CurriculumRepositoryEntryRelationDAO;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.manager.QualityGeneratorConfigsImpl;
import org.olat.modules.quality.generator.provider.courselectures.CourseLecturesProvider;
import org.olat.modules.quality.generator.ui.CurriculumElementBlackListController;
import org.olat.modules.quality.generator.ui.CurriculumElementWhiteListController;
import org.olat.modules.quality.generator.ui.RepositoryEntryBlackListController;
import org.olat.modules.quality.generator.ui.RepositoryEntryWhiteListController;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.manager.TaxonomyLevelDAO;
import org.olat.modules.taxonomy.model.TaxonomyLevelImpl;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
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
	private static final String RE_PUBLISHED_DATE_INIT = "RE PUBLISHED DATE INIT";
	private static final String OFFER_TO_ORG_INIT = "OFFER TO ORG INIT";
	private static final String TAXONOMY_TRANSLATIONS = "TAXONOMY TRANSLATIONS";
	private static final String MIGRATE_LTI_13_DEPLOYMENTS = "MIGRATE LTI 13 DEPLOYMENTS";
	private static final String QM_GENERATOR_LIST = "QM GENERATOR LIST";
	
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
	private I18nModule i18nModule;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	@Autowired
	private I18nManager i18nManager;
	@Autowired
	private CurriculumRepositoryEntryRelationDAO curriculumRepositoryEntryRelationDAO;
	
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
		allOk &= initRePublishedDate(upgradeManager, uhd);
		allOk &= initOfferToOrgs(upgradeManager, uhd);
		allOk &= initTaxonomyTranslations(upgradeManager, uhd);
		allOk &= migrateLTI13ToolDeployment(upgradeManager, uhd);
		allOk &= migrateQmGeneratorList(upgradeManager, uhd);
		
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
	
	private boolean initRePublishedDate(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(RE_PUBLISHED_DATE_INIT)) {
			try {
				log.info("Start re published date initialization.");
				initRePublishedDate();
				log.info("All re published date initialized.");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(RE_PUBLISHED_DATE_INIT, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	/*
	 * Best guess to have reasonable courses in the catalog launcher.
	 */
	private void initRePublishedDate() {
		List<RepositoryEntry> entries = getPublishedEntries();
		
		AtomicInteger migrationCounter = new AtomicInteger(0);
		for (RepositoryEntry entry : entries) {
			entry.setStatusPublishedDate(entry.getCreationDate());
			dbInstance.getCurrentEntityManager().merge(entry);
			migrationCounter.incrementAndGet();
			dbInstance.commitAndCloseSession();
			if(migrationCounter.get() % 100 == 0) {
				log.info("Init re public visible: num. of offers: {}", migrationCounter);
			}
		}
	}
	
	public List<RepositoryEntry> getPublishedEntries() {
		QueryBuilder sb = new QueryBuilder(1200);
		sb.append("select v from repositoryentry as v");
		sb.append(" inner join fetch v.olatResource as res");
		sb.and().append("v.status ").in(RepositoryEntryStatusEnum.published);
		sb.and().append("v.statusPublishedDate is null");
		
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
	
	private boolean initTaxonomyTranslations(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(TAXONOMY_TRANSLATIONS)) {
			try {
				log.info("Start taxonomy translations initialization.");
				initTaxonomyTranslations();
				log.info("All taxonomy translations initialized.");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(TAXONOMY_TRANSLATIONS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

	@SuppressWarnings("deprecation")
	private void initTaxonomyTranslations() {
		Locale overlayDefaultLocale = i18nModule.getOverlayLocales().get(I18nModule.getDefaultLocale());

		List<TaxonomyLevel> taxonomyLevels = getTaxonomyLevels();
		
		AtomicInteger migrationCounter = new AtomicInteger(0);
		for (TaxonomyLevel taxonomyLevel : taxonomyLevels) {
			if (taxonomyLevel instanceof TaxonomyLevelImpl) {
				TaxonomyLevelImpl impl = (TaxonomyLevelImpl)taxonomyLevel;
				
				String mediaPath = taxonomyLevelDao.createLevelMediaStorage(taxonomyLevel.getTaxonomy(), taxonomyLevel);
				impl.setMediaPath(mediaPath);
				
				String i18nSuffix = taxonomyService.createI18nSuffix();
				impl.setI18nSuffix(i18nSuffix);
				dbInstance.getCurrentEntityManager().merge(impl);
				
				String displayNameKey = TaxonomyUIFactory.PREFIX_DISPLAY_NAME + i18nSuffix;
				String displayName = i18nManager.getLocalizedString(BUNDLE_NAME, displayNameKey, null, overlayDefaultLocale, true, false);
				if (!StringHelper.containsNonWhitespace(displayName)) {
					I18nItem displayNameItem = i18nManager.getI18nItem(BUNDLE_NAME, displayNameKey, overlayDefaultLocale);
					i18nManager.saveOrUpdateI18nItem(displayNameItem, impl.getDisplayName());
				}
				
				String descriptionKey = TaxonomyUIFactory.PREFIX_DESCRIPTION + i18nSuffix;
				String description = i18nManager.getLocalizedString(BUNDLE_NAME, descriptionKey, null, overlayDefaultLocale, true, false);
				if (!StringHelper.containsNonWhitespace(description)) {
					I18nItem descriptionItem = i18nManager.getI18nItem(BUNDLE_NAME, descriptionKey, overlayDefaultLocale);
					i18nManager.saveOrUpdateI18nItem(descriptionItem, impl.getDescription());
				}
				
				migrationCounter.incrementAndGet();
				dbInstance.commitAndCloseSession();
				if (migrationCounter.get() % 100 == 0) {
					log.info("Init taxonomy translations: num. of taxonomy levels: {}", migrationCounter);
				}
			}
		}
	}

	private List<TaxonomyLevel> getTaxonomyLevels() {
		String query = "select level from ctaxonomylevel as level";
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(query, TaxonomyLevel.class)
			.getResultList();
	}
	
	public boolean migrateLTI13ToolDeployment(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_LTI_13_DEPLOYMENTS)) {
			try {
				log.info("Migrate LTI 1.3 deployment context ids.");
				
				int counter = 0;
				List<LTI13ToolDeployment> deployments = getToolDeployment();
				for(LTI13ToolDeployment deployment:deployments) {
					((LTI13ToolDeploymentImpl)deployment).setContextId(deployment.getEntry().getKey().toString());
					dbInstance.getCurrentEntityManager().merge(deployment);
					
					if(++counter % 25 == 0) {
						dbInstance.commitAndCloseSession();
					} else {
						dbInstance.commit();
					}
				}
				dbInstance.commitAndCloseSession();
				log.info("End migration LTI 1.3 deployment context ids: {}", deployments.size());
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(MIGRATE_LTI_13_DEPLOYMENTS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private List<LTI13ToolDeployment> getToolDeployment() {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select deployment from ltitooldeployment as deployment")
		  .append(" inner join fetch deployment.tool tool")
		  .append(" inner join fetch deployment.entry re")
		  .append(" where deployment.contextId is null");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LTI13ToolDeployment.class)
				.getResultList();
	}
	
	private boolean migrateQmGeneratorList(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(QM_GENERATOR_LIST)) {
			try {
				log.info("Start migration of quality generator list");
				migrateQmGeneratorList();
				log.info("End of migration of quality generator list");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(QM_GENERATOR_LIST, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private void migrateQmGeneratorList() {
		List<QualityGenerator> generators = getGenerators();
		
		for (QualityGenerator generator : generators) {
			migrateQmGeneratorList(generator);
			dbInstance.commitAndCloseSession();
		}
	}

	private List<QualityGenerator> getGenerators() {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select generator");
		sb.append("  from qualitygenerator as generator");
		sb.append(" where generator.type").in(CourseLecturesProvider.TYPE);
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityGenerator.class)
				.getResultList();
	}
	
	private void migrateQmGeneratorList(QualityGenerator generator) {
		QualityGeneratorConfigsImpl configs = new QualityGeneratorConfigsImpl(generator);
		List<CurriculumElementRef> whiteListRefs = CurriculumElementWhiteListController.getCurriculumElementRefs(configs);
		List<RepositoryEntry> whiteListEntries = curriculumRepositoryEntryRelationDAO.getRepositoryEntries(whiteListRefs, RepositoryEntryStatusEnum.values(), false, null, null);
		RepositoryEntryWhiteListController.setRepositoryEntryRefs(configs, whiteListEntries);
		
		List<CurriculumElementRef> blackListRefs = CurriculumElementBlackListController.getCurriculumElementRefs(configs);
		List<RepositoryEntry> blackListEntries = curriculumRepositoryEntryRelationDAO.getRepositoryEntries(blackListRefs, RepositoryEntryStatusEnum.values(), false, null, null);
		RepositoryEntryBlackListController.setRepositoryEntryRefs(configs, blackListEntries);
	}
	
}
