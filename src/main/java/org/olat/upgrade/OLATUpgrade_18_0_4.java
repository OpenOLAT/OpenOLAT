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
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.model.CertificateImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 août 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_18_0_4 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_18_0_4.class);

	private static final int BATCH_SIZE = 1000;

	private static final String VERSION = "OLAT_18.0.4";

	private static final String MIGRATE_CERTIFICATE_METADATA = "MIGRATE CERTIFICATE METADATA";
	
	@Autowired
	private DB dbInstance;
 	@Autowired
	private CertificatesManager certificatesManager;
 	@Autowired
 	private VFSRepositoryService vfsRepositoryService;

	public OLATUpgrade_18_0_4() {
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
		allOk &= initCertificateMetadata(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_18_0_4 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_18_0_4 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}

	private boolean initCertificateMetadata(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;

		if (!uhd.getBooleanDataValue(MIGRATE_CERTIFICATE_METADATA)) {
			try {
				log.info("Start init metadata of certificates");

				int counter = 0;
				List<Certificate> certificates;
				do {
					certificates = getCertificates(counter, BATCH_SIZE);
					for (Certificate certificate:certificates) {
						if(certificate.getMetadata() == null) {
							VFSLeaf certificateLeaf = certificatesManager.getCertificateLeaf(certificate);
							if(certificateLeaf != null && certificateLeaf.canMeta() == VFSStatus.YES) {
								VFSMetadata metadata = vfsRepositoryService.getMetadataFor(certificateLeaf);
								((CertificateImpl)certificate).setMetadata(metadata);
								dbInstance.getCurrentEntityManager().merge(certificate);
							}
						}
					}
					counter += certificates.size();
					log.info(Tracing.M_AUDIT, "Init metadata of certificates: {} total processed ({})", certificates.size(), counter);
					dbInstance.commitAndCloseSession();
				} while (certificates.size() == BATCH_SIZE);

				log.info("Init metadata of certificates finished.");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(MIGRATE_CERTIFICATE_METADATA, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}

		return allOk;
	}
	
	private List<Certificate> getCertificates(int firstResult, int maxResults) {
		String query = "select cert from certificate as cert order by cert.key";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, Certificate.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResults).getResultList();
	}
}
