/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.upgrade;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupMembership;
import org.olat.basesecurity.GroupMembershipInheritance;
import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.manager.GroupMembershipHistoryDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30 mai 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_20_0_4 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_20_0_4.class);
	
	private static final String VERSION = "OLAT_20.0.4";
	
	private static final String INIT_ORGANISATION_MEMBERSHIP_HISTORY = "INIT ORGANISATION MEMBERSHIP HISTORY";

	private static final int BATCH_SIZE = 10000;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private GroupMembershipHistoryDAO membershipHistoryDao;

	public OLATUpgrade_20_0_4() {
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
		allOk &= initOrganisationMembershipHistory(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_20_0_4 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_20_0_4 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	private boolean initOrganisationMembershipHistory(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(INIT_ORGANISATION_MEMBERSHIP_HISTORY)) {
			try {
				log.info("Start initialization of organisation member history.");
				
				int newEntries = 0;
				List<Organisation> organisations = organisationService.getOrganisations();
				for(Organisation organisation:organisations) {
					Group group = organisation.getGroup();
					List<GroupMembership> memberships = getMembership(group);
					do {
						for(GroupMembership membership:memberships) {
							membershipHistoryDao.createMembershipHistory(group, membership.getIdentity(), membership.getRole(),
									GroupMembershipStatus.active, membership.getInheritanceMode() == GroupMembershipInheritance.inherited,
									null, null, null, "Initialization");
							if(newEntries++ % 25 == 0) {
								dbInstance.commitAndCloseSession();
							}
							if(newEntries % 1000 == 0) {
								log.info("Initialization of organisation {} member history: {}", organisation.getDisplayName(), newEntries);
							}
						}

						dbInstance.commitAndCloseSession();
						memberships = getMembership(group);
					} while (!memberships.isEmpty());
					
					dbInstance.commitAndCloseSession();
				}
				
				log.info("End initialization of organisation member history ({} new entries)", newEntries);
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(INIT_ORGANISATION_MEMBERSHIP_HISTORY, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private List<GroupMembership> getMembership(Group group) {
		String query = """
				select member from bgroupmember as member
				inner join fetch member.identity as ident
				where member.group.key=:groupKey
				and not exists (select history.key from bgroupmemberhistory as history
					where history.identity.key=ident.key and history.group.key=member.group.key
				)
				order by member.key
				""";
		return dbInstance.getCurrentEntityManager().createQuery(query, GroupMembership.class)
				.setParameter("groupKey", group.getKey())
				.setFirstResult(0)
				.setMaxResults(BATCH_SIZE)
				.getResultList();
	}
}
