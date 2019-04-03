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

import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.manager.OrganisationDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 avr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_13_2_4 extends OLATUpgrade {
	
	private static final String VERSION = "OLAT_13.2.4";
	private static final String ORPHAN_IDENTITIES = "ORPHAN IDENTITIES";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private OrganisationDAO organisationDao;
	@Autowired
	private OrganisationService organisationService;
	
	public OLATUpgrade_13_2_4() {
		super();
	}
	
	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public boolean doPreSystemInitUpgrade(UpgradeManager upgradeManager) {
		return false;
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
		allOk &= updateCurriculumElementChildren(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.audit("Finished OLATUpgrade_13_2_4 successfully!");
		} else {
			log.audit("OLATUpgrade_13_2_4 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}
	
	/**
	 * Find identities without organizations but not deleted.
	 * 
	 * @param upgradeManager The upgrade manager
	 * @param uhd The upgrade history
	 * @return true if successful
	 */
	private boolean updateCurriculumElementChildren(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(ORPHAN_IDENTITIES)) {
			List<Identity> orphans = organisationDao.getIdentitiesWithoutOrganisations();
			if(orphans.isEmpty()) {
				for(Identity orphan:orphans) {
					organisationService.addMember(orphan, OrganisationRoles.user);
				}
				log.info("Add " + orphans.size() + " identities without organisations to the default org (wasn't in users sec. group)");
			}
			dbInstance.commitAndCloseSession();
			uhd.setBooleanDataValue(ORPHAN_IDENTITIES, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
}
