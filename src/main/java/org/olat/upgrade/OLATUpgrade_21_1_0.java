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
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificateStatus;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.model.CertificateImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 août 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class OLATUpgrade_21_1_0 extends OLATUpgrade {
	
	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_21_1_0.class);

	private static final String VERSION = "OLAT_21.1.0";
	
	private static final String MIGRATE_CERTIFICATE_IN_ERROR_STATUS = "MIGRATE CERTIFICATE IN ERROR STATUS";

	@Autowired
	private DB dbInstance;
	@Autowired
	private CertificatesManager certificatesManager;

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
		allOk &= migrateCertificateInErrorStatus(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);

		if (allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_21_1_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_21_1_0 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	private boolean migrateCertificateInErrorStatus(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_CERTIFICATE_IN_ERROR_STATUS)) {
			try {
				log.info("Migration certificates in error status");

				int count = 0;
				List<Long> certificatesKeys = getCertificatesWithError();
				for (Long certificateKey : certificatesKeys) {
					Certificate certificate = certificatesManager.getCertificateById(certificateKey);
					if(certificate instanceof CertificateImpl certificateImpl
							&& certificate.getStatus() == CertificateStatus.error
							&& !StringHelper.containsNonWhitespace(certificateImpl.getGenerationData())) {
						certificateImpl.setStatus(CertificateStatus.failed);
						certificateImpl.setGenerationNextDate(null);
						certificateImpl.setGenerationRetries(0);
						dbInstance.getCurrentEntityManager().merge(certificateImpl);
						count++;
					}
					dbInstance.commitAndCloseSession();
				}
				dbInstance.commitAndCloseSession();
				log.info("End migration certificates in error status: {}", count);
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(MIGRATE_CERTIFICATE_IN_ERROR_STATUS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

	private List<Long> getCertificatesWithError() {
		String query = """
				select cer.key from certificate cer
				inner join cer.olatResource as rsrc
				inner join cer.identity as ident
				where cer.statusString=:errorStatus
				order by cer.creationDate""";

		return dbInstance.getCurrentEntityManager()
				.createQuery(query, Long.class)
				.setParameter("errorStatus", CertificateStatus.error.name())
				.getResultList();
	}
}
