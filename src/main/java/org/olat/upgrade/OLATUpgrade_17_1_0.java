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
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.i18n.I18nModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28.09.2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_17_1_0 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_17_1_0.class);

	private static final String VERSION = "OLAT_17.1.0";
	private static final String GUEST_ASSESSMENT_ENTRIES_DELETE = "GUEST ASSESSMENT ENTRIES DELETE";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private I18nModule i18nModule;
	
	public OLATUpgrade_17_1_0() {
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
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_17_1_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_17_1_0 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	private boolean deleteGuestAssessmentEntries(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(GUEST_ASSESSMENT_ENTRIES_DELETE)) {
			try {
				log.info("Start delete guest assessment entries.");
				int count = deleteGuestAssessmentEntries();
				log.info("All guest assessment entries deleted: {}", count);
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(GUEST_ASSESSMENT_ENTRIES_DELETE, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private int deleteGuestAssessmentEntries() {
		List<Long> guestsKey = getGuests().stream().map(Identity::getKey).collect(Collectors.toList());
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete from assessmententry aentry");
		sb.and().append("aentry.anonymousIdentifier is null");
		sb.and().append("aentry.identity.key in :guestKeys");
		sb.and().append("aentry.key not in (select asession.assessmentEntry.key from qtiassessmenttestsession as asession)");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("guestKeys", guestsKey)
				.executeUpdate();
	}
	
	private List<Identity> getGuests() {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select membership.identity");
		sb.append("  from bgroupmember membership");
		sb.append("       inner join organisation org");
		sb.append("         on membership.group.key = org.group.key");
		sb.and().append(" membership.role = '").append(OrganisationRoles.guest.name()).append("'");
		
		List<Identity> guests = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Identity.class).getResultList();
		
		Set<String> guestNames = getGuestNames();
		guests.removeIf(guest -> !guestNames.contains(guest.getName()));
		guests.removeIf(this::hasNonGuestRoles);
		
		return guests;
	}
	
	private final Set<String> getGuestNames() {
		return i18nModule.getAvailableLanguageKeys().stream().map(key -> "guest_" + key).collect(Collectors.toSet());
	}

	private boolean hasNonGuestRoles(Identity guest) {
		return securityManager.getRolesAsString(guest).stream().distinct().count() > 1;
	}
	
}
