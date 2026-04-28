/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.upgrade;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.core.logging.Tracing;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.manager.ACOfferDAO;
import org.olat.resource.accesscontrol.manager.ACOfferToOrganisationDAO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 oct. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class OLATUpgrade_20_3_2 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_20_3_2.class);

	private static final String VERSION = "OLAT_20.3.2";
	private static final String UPDATE_ADMINISTRATIVE_ACCESS = "UPDATE REPOSITORY ENTRY ADMINISTRATIVE ACCESS";
	private static final String UPDATE_OFFER_ORGANISATION = "UPDATE OFFER ORGANISATION";

	@Autowired
	private DB dbInstance;
	@Autowired
	private ACService acService;
	@Autowired
	private ACOfferDAO acOfferDao;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private ACOfferToOrganisationDAO acOfferToOrganisationDao;

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (uhd == null) {
			uhd = new UpgradeHistoryData();
		} else if (uhd.isInstallationComplete()) {
			return false;
		}

		boolean allOk = true;
		allOk &= updateAdministrativeAccess(upgradeManager, uhd);
		allOk &= updateOfferOrganisation(upgradeManager, uhd);
		
		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);

		if (allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_20_3_2 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_20_3_2 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	private boolean updateAdministrativeAccess(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(UPDATE_ADMINISTRATIVE_ACCESS)) {
			try {
				// Update only if organisation is enabled
				if(!organisationModule.isEnabled()) {
					log.info("Enforce every course administrative access");
					List<Long> entryKeys = getRepositoryEntriesWithoutOrganisation();
					for(Long entryKey:entryKeys) {
						updateAdministrativeAccess(entryKey);
						dbInstance.commitAndCloseSession();
					}
					log.info("End update administrative access: {}", entryKeys.size());
				}
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(UPDATE_ADMINISTRATIVE_ACCESS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private boolean updateOfferOrganisation(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(UPDATE_OFFER_ORGANISATION)) {
			try {
				if(!organisationModule.isEnabled()) {
					log.info("Enforce every offer an organisation");
					List<Long> offerKeys = getOffersWithoutOrganisation();
					for(Long offerKey:offerKeys) {
						updateOfferOrganisation(offerKey);
						dbInstance.commitAndCloseSession();
					}
					log.info("End update offer organisation: {}", offerKeys.size());
				}
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(UPDATE_OFFER_ORGANISATION, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	/**
	 * We ensure all repository entries have at least the default organisation,
	 * the deleted ones too. The relation to organisations is only deleted if
	 * the entry is permanently deleted.
	 * 
	 * @param entryKey the entry key
	 */
	private void updateAdministrativeAccess(Long entryKey) {
		RepositoryEntry entry = repositoryService.loadByKey(entryKey);
		List<Organisation> organisations = repositoryService.getOrganisations(entry);
		if(organisations.isEmpty()) {
			List<Organisation> defOrganisations = List.of(organisationService.getDefaultOrganisation());
			repositoryManager.setAccess(entry, entry.isPublicVisible(), entry.getAllowToLeaveOption(),
					entry.getCanCopy(), entry.getCanReference(), entry.getCanDownload(), entry.getCanIndexMetadata(),
					defOrganisations);
		}
	}
	
	private void updateOfferOrganisation(Long offerKey) {
		Offer offer = acOfferDao.loadOfferByKey(offerKey);
		if(offer.isGuestAccess()) {
			// No organisation for guests
			return;
		}
		
		OLATResource resource = offer.getResource();
		if(resource == null || "BusinessGroup".equals(resource.getResourceableTypeName())) {
			return;
		}
		
		List<Organisation> organisations = acOfferToOrganisationDao.loadOrganisations(offer);
		if(organisations.isEmpty()) {
			RepositoryEntry entry = repositoryService.loadByResourceKey(offer.getResource().getKey());
			if(entry != null) {// Update only courses (repository entries)
				List<Organisation> reOrganisations = repositoryService.getOrganisations(entry);
				if(!reOrganisations.isEmpty()) {
					acService.updateOfferOrganisations(offer, reOrganisations);
				} else {
					List<Organisation> defOrganisations = List.of(organisationService.getDefaultOrganisation());
					acService.updateOfferOrganisations(offer, defOrganisations);
				}
			}
		}
	}
	
	private List<Long> getRepositoryEntriesWithoutOrganisation() {
		String query = """
				select v.key from repositoryentry as v
				where not exists (
				  select relOrg.key from organisation as relOrg
				  inner join relOrg.group as bGroup
				  inner join repoentrytogroup as rel on (bGroup.key=rel.group.key)
				  where rel.entry.key=v.key
				)""";
		
		return dbInstance.getCurrentEntityManager().createQuery(query, Long.class)
				.getResultList();
	}
	
	private List<Long> getOffersWithoutOrganisation() {
		String query = """
				select offer.key from acoffer as offer
				where not exists (select relOrg.key from offertoorganisation relOrg
				  where relOrg.offer.key=offer.key
				)""";
		
		return dbInstance.getCurrentEntityManager().createQuery(query, Long.class)
				.getResultList();
	}
}
