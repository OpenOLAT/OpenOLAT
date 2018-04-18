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

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.properties.Property;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13.03.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_12_4_1 extends OLATUpgrade {
	
	private static final String VERSION = "OLAT_12.4.1";
	private static final String MIGRATE_AGE_POLICY = "MIGRATE CHANGE PASSWORD";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	
	public OLATUpgrade_12_4_1() {
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
		allOk &= migratePasswordChanges(upgradeManager, uhd);
		
		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.audit("Finished OLATUpgrade_12_4_1 successfully!");
		} else {
			log.audit("OLATUpgrade_12_4_1 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}

	
	private boolean migratePasswordChanges(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_AGE_POLICY)) {
			try {
				int count = 0;
				List<Property> properties = getPasswordChanges();
				for(Property property:properties) {
					Authentication authentication = securityManager.findAuthentication(property.getIdentity(), "OLAT");
					if(authentication != null
						&& (authentication.getLastModified() == null || authentication.getLastModified().before(property.getLastModified()))) {
						authentication.setLastModified(property.getLastModified());
						securityManager.updateAuthentication(authentication);
						if(count++ % 50 == 0) {
							dbInstance.commitAndCloseSession();
							log.info("Update " + count + " password last modification dates");
						}
					}
				}
			} catch (Exception e) {
				log.error("", e);
				allOk &= false;
			}

			uhd.setBooleanDataValue(MIGRATE_AGE_POLICY, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private List<Property> getPasswordChanges() {
		StringBuilder sb = new StringBuilder();
		sb.append("select v from ").append(Property.class.getName()).append(" as v ")
		  .append(" inner join fetch v.identity identity ")
		  .append(" where v.category=:cat and v.name=:name and v.stringValue=:val");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Property.class)
				.setParameter("cat", "afterLogin")
				.setParameter("name", "org.olat.user.ChangePasswordController")
				.setParameter("val", "true")
				.getResultList();	
	}

}
