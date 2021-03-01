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
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.AuthenticationImpl;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.manager.AuthenticationHistoryDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.Tracing;
import org.olat.properties.Property;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13.03.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_12_4_1 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_12_4_1.class);
	
	private static final String VERSION = "OLAT_12.4.1";
	private static final String MIGRATE_AGE_POLICY = "MIGRATE CHANGE PASSWORD";
	private static final String MIGRATE_HISTORY = "MIGRATE PASSWORD HISTORY";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private AuthenticationHistoryDAO authenticationHistoryDao;
	
	public OLATUpgrade_12_4_1() {
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
		allOk &= migratePasswordChanges(upgradeManager, uhd);
		allOk &= migratePasswordHistory(upgradeManager, uhd);
		
		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_12_4_1 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_12_4_1 not finished, try to restart OpenOLAT!");
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
					Authentication authentication = securityManager.findAuthentication(property.getIdentity(), "OLAT", BaseSecurity.DEFAULT_ISSUER);
					if(authentication != null
						&& (authentication.getLastModified() == null || authentication.getLastModified().before(property.getLastModified()))) {
						authentication.setLastModified(property.getLastModified());
						securityManager.updateAuthentication(authentication);
						if(count++ % 50 == 0) {
							dbInstance.commitAndCloseSession();
							log.info("Update {} password last modification dates", count);
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
	
	private boolean migratePasswordHistory(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_HISTORY)) {
			try {
				int count = 0;
				String olatProvider = BaseSecurityModule.getDefaultAuthProviderIdentifier();
				List<Authentication> authentications = getOlatAuthentications();
				dbInstance.commitAndCloseSession();
				for(Authentication authentication:authentications) {
					if(authenticationHistoryDao.historyLength(authentication.getIdentity(), olatProvider) == 0) {
						authenticationHistoryDao.createHistory(authentication, authentication.getIdentity());
					}
					if(count++ % 25 == 0) {
						dbInstance.commitAndCloseSession();
					}
					if(count % 100 == 0) {
						log.info("Add " + count + " password in password history");
					}
				}
			} catch (Exception e) {
				log.error("", e);
				allOk &= false;
			}

			uhd.setBooleanDataValue(MIGRATE_HISTORY, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private List<Authentication> getOlatAuthentications() {
		StringBuilder sb = new StringBuilder();
		sb.append("select auth from ").append(AuthenticationImpl.class.getName()).append(" as auth")
		  .append(" inner join fetch auth.identity as ident")
		  .append(" where auth.provider=:provider");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Authentication.class)
				.setParameter("provider", BaseSecurityModule.getDefaultAuthProviderIdentifier())
				.getResultList();	
	}
}
