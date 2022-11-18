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

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.manager.IdentityDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28.09.2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_17_1_3 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_17_1_3.class);

	private static final String VERSION = "OLAT_17.1.3";
	private static final String GUEST_INACTIVATION = "GUEST INACTIVATION";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private IdentityDAO identityDao;
	
	public OLATUpgrade_17_1_3() {
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
		
		allOk &= deleteGuestAssessmentEntries(upgradeManager, uhd);
		
		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_17_1_3 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_17_1_3 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	private boolean deleteGuestAssessmentEntries(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(GUEST_INACTIVATION)) {
			try {
				log.info("Start removing all inactivation dates from guests.");
				List<IdentityImpl> guests = getGuests();
				for(IdentityImpl guest:guests) {
					guest.setStatus(Identity.STATUS_ACTIV);
					guest.setDeletionEmailDate(null);
					guest.setExpirationDate(null);
					guest.setExpirationEmailDate(null);
					guest.setInactivationDate(null);
					guest.setInactivationEmailDate(null);
					guest.setReactivationDate(null);
					identityDao.saveIdentity(guest);
				}
				dbInstance.commitAndCloseSession();
				log.info("All guest updated: {}", guests.size());
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(GUEST_INACTIVATION, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private List<IdentityImpl> getGuests() {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select membership.identity from bgroupmember membership")
		  .and().append(" membership.role = '").append(OrganisationRoles.guest.name()).append("'");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), IdentityImpl.class).getResultList();
	}
	
	
}
