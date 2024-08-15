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
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.webdav.manager.WebDAVAuthManager;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 août 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_19_1_0 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_19_1_0.class);
	
	private static final String VERSION = "OLAT_19.1.0";

	private static final String DELETE_WEBDAV_AUTH = "DELETED WEBDAV AUTH";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;

	public OLATUpgrade_19_1_0() {
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
		allOk &= updateDeletedFolderMetadata(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_19_1_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_19_1_0 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	private boolean updateDeletedFolderMetadata(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(DELETE_WEBDAV_AUTH)) {
			try {
				log.info("Start to delete WebDAV basic authentications.");
				
				int counter = 0;
				@SuppressWarnings("deprecation")
				List<String> providers = List.of(WebDAVAuthManager.LEGACY_PROVIDER_WEBDAV,
						WebDAVAuthManager.LEGACY_PROVIDER_WEBDAV_EMAIL, WebDAVAuthManager.LEGACY_PROVIDER_WEBDAV_INSTITUTIONAL_EMAIL);
				List<Authentication> webDavAuthentications;
				do {
					webDavAuthentications = loadWebDAVAuthentication(500, providers);
					for(int i=0; i<webDavAuthentications.size(); i++) {
						Authentication authentication = webDavAuthentications.get(i);
						securityManager.deleteAuthentication(authentication);
						if(i % 25 == 0) {
							dbInstance.commitAndCloseSession();
						}
					}
					counter += webDavAuthentications.size();
					log.info(Tracing.M_AUDIT, "Deleted WebDAV basic authentications: {}", counter);
					dbInstance.commitAndCloseSession();
				} while (!webDavAuthentications.isEmpty());
				
				log.info("Finish to delete WebDAV basic authentications.");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(DELETE_WEBDAV_AUTH, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private List<Authentication> loadWebDAVAuthentication(int maxResults, List<String> providers) {
		String query = """
				select auth from authentication as auth 
				where auth.provider in (:providers)
				order by auth.key""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, Authentication.class)
				.setParameter("providers", providers)
				.setFirstResult(0)
				.setMaxResults(maxResults)
				.getResultList();
	}
	
}
