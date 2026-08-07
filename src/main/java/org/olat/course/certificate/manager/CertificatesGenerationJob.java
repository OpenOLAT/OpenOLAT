
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
package org.olat.course.certificate.manager;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.scheduler.JobWithDB;
import org.olat.core.logging.Tracing;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificateStatus;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.CertificatesModule;
import org.olat.course.certificate.model.CertificateImpl;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * 
 * Initial date: 5 août 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@DisallowConcurrentExecution
public class CertificatesGenerationJob extends JobWithDB {
	
	private static final Logger log = Tracing.createLoggerFor(CertificatesGenerationJob.class);

	@Override
	public void executeWithDB(JobExecutionContext arg0) throws JobExecutionException {
		log.debug("Check certificates to generate");
		CertificatesManager certificatesManager = CoreSpringFactory.getImpl(CertificatesManager.class);
		CertificatesModule certificatesModule = CoreSpringFactory.getImpl(CertificatesModule.class);
		
		int circuitBreakerFailures = certificatesModule.getCertificateGenerationCircuitBreakerFailures();
		
		int errors = 0;
		List<Long> pendingCertificatesKey = certificatesManager.getPendingCertificatesToProcess();		
		List<Long> certificatesToReprocessKeys = certificatesManager.getCertificatesToReprocess(LocalDateTime.now());
		pendingCertificatesKey.addAll(certificatesToReprocessKeys);
		DBFactory.getInstance().commitAndCloseSession();
		
		for(Long pendingCertificateKey:pendingCertificatesKey) {
			boolean ok = generateCertificate(pendingCertificateKey, certificatesManager);
			DBFactory.getInstance().commitAndCloseSession();
			if(!ok) {
				errors++;
			} else {
				errors = 0;
			}
			if(errors >= circuitBreakerFailures) {
				log.error("Too many consecutive failures during certificates generation process. Stop it.");
				break;
			}
		}
	}
	
	private boolean generateCertificate(Long pendingCertificateKey, CertificatesManager certificatesManager) {
		boolean allOk = true;
		try {
			Certificate certificate = certificatesManager.generateCertificateFile(pendingCertificateKey);
			if(certificate != null &&
					(certificate.getStatus() == CertificateStatus.error || certificate.getStatus() == CertificateStatus.failed)) {
				allOk = false;
			}
		} catch (Exception e) {
			log.error("Error processing certificate: {}", pendingCertificateKey, e);
			allOk = false;
			
			// Unexpected error, don't try again, marks as failed
			failedCertificate(pendingCertificateKey, certificatesManager);
		}
		return allOk;
	}
	
	private void failedCertificate(Long certificateKey, CertificatesManager certificatesManager) {
		DBFactory.getInstance().rollbackAndCloseSession();
		
		try {
			Certificate certificate = certificatesManager.getCertificateById(certificateKey);
			if(certificate.getStatus() != CertificateStatus.ok) {
				((CertificateImpl)certificate).setStatus(CertificateStatus.failed);
				CoreSpringFactory.getImpl(CertificatesDAO.class).updateCertificate(certificate);
			}
		} catch (Exception e) {
			log.error("Error processing certificate: {}", certificateKey, e);
		} finally {
			DBFactory.getInstance().commitAndCloseSession();
		}
	}
}
