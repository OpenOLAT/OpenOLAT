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
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityImpl;
import org.olat.commons.lifecycle.LifeCycleEntry;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 juin 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_15_1_0 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_15_1_0.class);

	private static final String VERSION = "OLAT_15.1.0";
	private static final String MIGRATE_USER_LIFECYCLE = "MIGRATE USER LIFECYCLE";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;

	public OLATUpgrade_15_1_0() {
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
		allOk &= migrateUsersLifecycle(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_15_1_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_15_1_0 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	private boolean migrateUsersLifecycle(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_USER_LIFECYCLE)) {
			List<LifeCycleEntry> lifeCycleEntries = getSendDeleteEmailEntries();
			for(LifeCycleEntry lifeCycleEntry:lifeCycleEntries) {
				migrateUserLifecycle(lifeCycleEntry);
			}
			uhd.setBooleanDataValue(MIGRATE_USER_LIFECYCLE, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private void migrateUserLifecycle(LifeCycleEntry lifeCycleEntry) {
		Long identityKey = lifeCycleEntry.getPersistentRef();
		Identity identity = securityManager.loadIdentityByKey(identityKey);
		if(identity != null
				&& !identity.getStatus().equals(Identity.STATUS_PERMANENT)
				&& !identity.getStatus().equals(Identity.STATUS_DELETED)) {
			((IdentityImpl)identity).setInactivationDate(lifeCycleEntry.getLcTimestamp());
			((IdentityImpl)identity).setStatus(Identity.STATUS_INACTIVE);
			dbInstance.getCurrentEntityManager().merge(identity);
		}
	}
	
	private List<LifeCycleEntry> getSendDeleteEmailEntries() {
		StringBuilder sb = new StringBuilder(); 
		sb.append("select e from ").append(LifeCycleEntry.class.getName()).append(" as e")
		  .append(" where e.action=:action and e.persistentTypeName=:persistentTypeName");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LifeCycleEntry.class)
				.setParameter("action", "sendDeleteEmail")
				.setParameter("persistentTypeName", "org.olat.basesecurity.IdentityImpl")
				.getResultList();
	}
}
