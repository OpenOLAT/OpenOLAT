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
package org.olat.modules.certificationprogram.manager;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.model.CertificateConfig;
import org.olat.course.certificate.model.CertificateInfos;
import org.olat.modules.certificationprogram.CertificationCoordinator;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramStatusEnum;
import org.olat.modules.certificationprogram.ui.component.Duration;
import org.olat.modules.certificationprogram.ui.component.DurationType;
import org.olat.modules.creditpoint.CreditPointService;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.CreditPointTransactionType;
import org.olat.modules.creditpoint.CreditPointWallet;
import org.olat.modules.creditpoint.manager.CreditPointWalletDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 23 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class CertificationCoordinatorImpl implements CertificationCoordinator {
	
	private static final Logger log = Tracing.createLoggerFor(CertificationCoordinatorImpl.class);
	
	@Autowired
	private CreditPointService creditPointService;
	@Autowired
	private CreditPointWalletDAO creditPointWalletDao;
	@Autowired
	private CertificationProgramDAO certificationProgramDao;
	@Autowired
	private CertificatesManager certificatesManager;
	
	@Override
	public boolean processCertification(Identity identity, CertificationProgram certificationProgram, Date referenceDate, Identity doer) {
		if(identity == null || certificationProgram == null || certificationProgram.getKey() == null) return false;
		
		certificationProgram = certificationProgramDao.loadCertificationProgram(certificationProgram.getKey());
		if(certificationProgram != null && certificationProgram.getStatus() == CertificationProgramStatusEnum.active) {
			BigDecimal amount = certificationProgram.getCreditPoints();
			CreditPointSystem system = certificationProgram.getCreditPointSystem();
			
			boolean allowed = true;
			if(system != null && amount != null) {
				BigDecimal amountToRemove = amount.negate();
				String note = "Certification \"" + certificationProgram.getDisplayName() + "\"";
				CreditPointWallet wallet = creditPointWalletDao.getWallet(identity, system);
				if(wallet == null || wallet.getBalance() == null || amount.compareTo(wallet.getBalance()) > 0) {
					allowed = false;
				} else {
					creditPointService.createCreditPointTransaction(CreditPointTransactionType.removal, amountToRemove, null,
							note, wallet, identity, certificationProgram.getResource(), null, null, null, null);
				}
			}
			
			if(allowed) {
				log.info("Generate certificate for {} in certification program {}", identity.getKey(), certificationProgram.getKey());
				generateCertificate(identity, certificationProgram);
			}
		}
		return false;
	}

	@Override
	public boolean processCertificationDemand(Identity identity, CertificationProgram certificationProgram, Date referenceDate) {
		if(identity == null || certificationProgram == null || certificationProgram.getKey() == null) return false;
		
		certificationProgram = certificationProgramDao.loadCertificationProgram(certificationProgram.getKey());
		if(certificationProgram != null && certificationProgram.getStatus() == CertificationProgramStatusEnum.active) {
			BigDecimal amount = certificationProgram.getCreditPoints();
			CreditPointSystem system = certificationProgram.getCreditPointSystem();
			Certificate certificate = certificatesManager. getLastCertificate(identity, certificationProgram.getResource().getKey());

			boolean allowed = isCertificationAllowedByDate(certificate, certificationProgram, referenceDate);

			if(allowed && system != null && amount != null) {
				BigDecimal amountToRemove = amount.negate();
				String note = "Certification \"" + certificationProgram.getDisplayName() + "\"";
				CreditPointWallet wallet = creditPointWalletDao.getWallet(identity, system);
				if(wallet == null || wallet.getBalance() == null || amount.compareTo(wallet.getBalance()) > 0) {
					allowed = false;
				} else {
					creditPointService.createCreditPointTransaction(CreditPointTransactionType.removal, amountToRemove, null,
							note, wallet, identity, certificationProgram.getResource(), null, null, null, null);
				}
			}
			
			if(allowed) {
				generateCertificate(identity, certificationProgram);
				return true;
			}
		}
		return false;
	}
	
	public boolean isCertificationAllowedByDate(Certificate certificate, CertificationProgram certificationProgram, Date referenceDate) {
		boolean allowed;
		if(certificate == null) {
			allowed = true;
		} else if(certificationProgram.isRecertificationEnabled()) {
			Date nextRecertificationDate = certificate.getNextRecertificationDate();
			if(nextRecertificationDate == null) {
				Duration duration = certificationProgram.getValidityTimelapseDuration();
				if(duration != null) {
					nextRecertificationDate = duration.unit().toDate(certificate.getCreationDate(), duration.value());
				}
			}
			
			Date recertificationWindowDate = certificate.getRecertificationWindowDate();
			if(recertificationWindowDate == null && certificationProgram.isRecertificationWindowEnabled()) {
				DurationType duration = certificationProgram.getRecertificationWindowUnit();
				if(duration != null) {
					recertificationWindowDate = duration.toDate(certificate.getCreationDate(), certificationProgram.getRecertificationWindow());
				}
			}
			
			allowed = certificationProgram.isPrematureRecertificationByUserEnabled()
					|| ((nextRecertificationDate != null && nextRecertificationDate.compareTo(referenceDate) <= 0
					&& (recertificationWindowDate == null || recertificationWindowDate.compareTo(referenceDate) >= 0)));
		} else {
			allowed = false;
		}
		return allowed;
	}
	
	@Override
	public void generateCertificate(Identity identity, CertificationProgram certificationProgram) {
		// No course informations, only certification program informations
		CertificateInfos certificateInfos = CertificateInfos.valueOf(identity, null, null);
		CertificateConfig config = CertificateConfig.builder()
				.withCustom1(certificationProgram.getCertificateCustom1())
				.withCustom2(certificationProgram.getCertificateCustom2())
				.withCustom3(certificationProgram.getCertificateCustom3())
				.withSendEmailBcc(true)
				.withSendEmailLinemanager(true)
				.withSendEmailIdentityRelations(true)
				.build();
		certificatesManager.generateCertificate(certificateInfos, certificationProgram, null, config);
	}
}
