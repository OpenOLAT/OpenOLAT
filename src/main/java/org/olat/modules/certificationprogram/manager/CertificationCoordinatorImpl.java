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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ActivityLogService;
import org.olat.core.logging.activity.CoreLoggingResourceable;
import org.olat.core.logging.activity.ILoggingAction;
import org.olat.core.logging.activity.ILoggingResourceable;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.manager.CertificatesDAO;
import org.olat.course.certificate.model.CertificateConfig;
import org.olat.course.certificate.model.CertificateImpl;
import org.olat.course.certificate.model.CertificateInfos;
import org.olat.modules.certificationprogram.CertificationCoordinator;
import org.olat.modules.certificationprogram.CertificationLoggingAction;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramMailConfiguration;
import org.olat.modules.certificationprogram.CertificationProgramMailConfigurationStatus;
import org.olat.modules.certificationprogram.CertificationProgramMailType;
import org.olat.modules.certificationprogram.CertificationProgramStatusEnum;
import org.olat.modules.certificationprogram.RecertificationMode;
import org.olat.modules.certificationprogram.ui.component.Duration;
import org.olat.modules.certificationprogram.ui.component.DurationType;
import org.olat.modules.creditpoint.CreditPointService;
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
	private DB dbInstance;
	@Autowired
	private MailManager mailService;
	@Autowired
	private CertificatesDAO certificatesDao;
	@Autowired
	private ActivityLogService activityLogService;
	@Autowired
	private CreditPointService creditPointService;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private CreditPointWalletDAO creditPointWalletDao;
	@Autowired
	private CertificationProgramDAO certificationProgramDao;
	@Autowired
	private CertificationProgramLogDAO certificationProgramLogDao;
	@Autowired
	private CertificationProgramMailQueries certificationProgramMailQueries;
	@Autowired
	private CertificationProgramMailConfigurationDAO certificationProgramMailConfigurationDao;

	@Override
	public boolean processCertificationRequest(Identity identity, CertificationProgram certificationProgram,
			RequestMode requestMode, Date referenceDate, Identity doer) {
		if(identity == null || certificationProgram == null || certificationProgram.getKey() == null) return false;
		
		boolean accepted = false;
		certificationProgram = certificationProgramDao.loadCertificationProgram(certificationProgram.getKey());
		if(certificationProgram != null && certificationProgram.getStatus() == CertificationProgramStatusEnum.active) {
			Certificate certificate = certificatesDao.getLastCertificate(identity, certificationProgram.getResource().getKey());
			if(certificate == null) {
				//First certificate is free (paid by the course fee)
				log.info("Generate first certificate for {} in certification program {} by {}", identity.getKey(), certificationProgram.getKey(), (doer == null ? null : doer.getKey()));
				generateCertificate(identity, certificationProgram, requestMode, doer);
				accepted = true;
			} else {
				accepted = processRecertificationRequest(identity, certificationProgram, certificate, requestMode, referenceDate, doer);
			}
		}
		return accepted;
	}

	private boolean processRecertificationRequest(Identity identity, CertificationProgram certificationProgram, Certificate certificate,
			RequestMode requestMode, Date referenceDate, Identity doer) {
		
		// Check if the request mode is allowed by the actor (prevent job to issue manual only certificates)
		boolean allowedMode = isCertificationAllowedByRequestMode(certificationProgram, certificate, requestMode);
		if(!allowedMode) {
			return false;
		}
		
		boolean allowed = isCertificationAllowedByDate(certificate, certificationProgram, requestMode, referenceDate);
		if(!allowed) {
			return false;
		}
		
		BigDecimal amount = certificationProgram.getCreditPoints();	
		if(certificationProgram.getCreditPointSystem() != null && amount != null) {
			CreditPointWallet wallet = creditPointWalletDao.getWallet(identity, certificationProgram.getCreditPointSystem());
			if(wallet == null || wallet.getBalance() == null || amount.compareTo(wallet.getBalance()) > 0) {
				// Not enough credit
				if(isRemovedFromProgramNotEnoughCreditPoints(certificate, certificationProgram, requestMode, referenceDate)) {
					removeFromCertificationProgram(identity, certificationProgram, certificate, referenceDate, requestMode, doer);
				}
				return false;
			} else {
				BigDecimal amountToRemove = amount.negate();
				String note = "Certification \"" + certificationProgram.getDisplayName() + "\"";
				creditPointService.createCreditPointTransaction(CreditPointTransactionType.removal, amountToRemove, null,
						note, wallet, identity, certificationProgram.getResource(), null, null, null, null);
			}
		}
		log.info("Generate paid certificate for {} in certification program {} by {}", identity.getKey(), certificationProgram.getKey(), (doer == null ? null : doer.getKey()));
		generateCertificate(identity, certificationProgram, requestMode, doer);
		return true;
	}
	
	private void removeFromCertificationProgram(Identity identity, CertificationProgram certificationProgram, Certificate certificate,
			Date referenceDate, RequestMode requestMode, Identity doer) {
		((CertificateImpl)certificate).setLast(false);
		((CertificateImpl)certificate).setRemovalDate(referenceDate);
		certificate = certificatesDao.updateCertificate(certificate);
		dbInstance.commit();
		
		log.info("User {} removed of program {} by {0}", identity, certificationProgram, doer);
		activityLog(identity, certificationProgram, CertificationLoggingAction.CERTIFICATE_REMOVED, requestMode, doer);
		sendMail(identity, certificationProgram, certificate, CertificationProgramMailType.program_removed, doer);
	}
	
	/**
	 * 
	 * @param certificate The last certificate
	 * @param certificationProgram The certification program
	 * @param requestMode The request mode
	 * @param referenceDate The date where the check happens	
	 * @return true if removed
	 */
	private boolean isRemovedFromProgramNotEnoughCreditPoints(Certificate certificate, CertificationProgram certificationProgram,
			RequestMode requestMode, Date referenceDate) {
		if(requestMode == RequestMode.AUTOMATIC) {
			if(certificationProgram.isRecertificationWindowEnabled()) {
				Date endWindowDate = certificate.getRecertificationWindowDate();
				if(endWindowDate != null && referenceDate.compareTo(endWindowDate) > 0) {
					return true;
				}
			} else {
				return true;
			}
		}
		return false;
	}
	
	private boolean isCertificationAllowedByRequestMode(CertificationProgram certificationProgram, Certificate certificate, RequestMode requestMode) {
		// No recertification, only automatic renewal is forbidden
		if(!certificationProgram.isValidityEnabled() || !certificationProgram.isRecertificationEnabled()) {
			return requestMode != RequestMode.AUTOMATIC;
		}
		
		// In automatic mode, a manager can renew the certificate, the participant too
		if(certificationProgram.getRecertificationMode() == RecertificationMode.automatic) {
			if(requestMode == RequestMode.AUTOMATIC && certificate != null && certificate.isRecertificationPaused()) {
				return false;
			}
			return true;
		}
		return (certificationProgram.getRecertificationMode() == RecertificationMode.manual && requestMode != RequestMode.AUTOMATIC);
	}

	private boolean isCertificationAllowedByDate(Certificate certificate, CertificationProgram certificationProgram, RequestMode requestMode, Date referenceDate) {
		boolean allowed;
		if(certificate == null) {
			allowed = true;
		} else if(requestMode == RequestMode.COACH && certificationProgram.isRecertificationEnabled()) {
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
			
			allowed = (certificationProgram.isPrematureRecertificationByUserEnabled()
						&& nextRecertificationDate != null && nextRecertificationDate.compareTo(referenceDate) >= 0)
					|| ((nextRecertificationDate != null && nextRecertificationDate.compareTo(referenceDate) <= 0
						&& (recertificationWindowDate == null || recertificationWindowDate.compareTo(referenceDate) >= 0)));
		} else if (requestMode == RequestMode.COURSE) {
			allowed = true;
		} else {
			allowed = false;
		}
		return allowed;
	}
	
	@Override
	public void generateCertificate(Identity identity, CertificationProgram certificationProgram,
			RequestMode requestMode, Identity actor) {
		// Archive the last certificate
		certificatesDao.removeLastFlag(identity, certificationProgram);
		dbInstance.commit();
		
		CertificationProgramMailType mailType = requestMode == RequestMode.COURSE
				? CertificationProgramMailType.certificate_issued
				: CertificationProgramMailType.certificate_renewed;
		
		// Generate a new certificate
		// No course informations, only certification program informations
		CertificateInfos certificateInfos = CertificateInfos.valueOf(identity, null, null);
		CertificateConfig config = CertificateConfig.builder()
				.withCustom1(certificationProgram.getCertificateCustom1())
				.withCustom2(certificationProgram.getCertificateCustom2())
				.withCustom3(certificationProgram.getCertificateCustom3())
				.withSendEmailBcc(true)
				.withSendEmailLinemanager(true)
				.withSendEmailIdentityRelations(true)
				.withCertificationProgramMailType(mailType)
				.build();
		certificatesManager.generateCertificate(certificateInfos, certificationProgram, null, config);
		activityLog(identity, certificationProgram, CertificationLoggingAction.CERTIFICATE_ISSUED, requestMode, actor);
	}
	
	@Override
	public void revokeRecertification(CertificationProgram certificationProgram, Identity identity, Identity actor) {
		Certificate certificate = certificatesDao.getLastCertificate(identity, certificationProgram);
		int revokedCertificates = certificatesDao.revoke(identity, certificationProgram);
		if(revokedCertificates > 0)
		
		log.info("Certificate revoked {} for {} and program {} by {0}", revokedCertificates, identity, certificationProgram, actor);
		activityLog(identity, certificationProgram, CertificationLoggingAction.CERTIFICATE_REVOKED, RequestMode.COACH, actor);
		sendMail(identity, certificationProgram, certificate, CertificationProgramMailType.program_removed, actor);
	}
	
	public void sendReminders(CertificationProgramMailType type, Date referenceDate) {
		List<CertificationProgramMailConfiguration> configurations = certificationProgramMailConfigurationDao
				.getConfigurations(type, CertificationProgramMailConfigurationStatus.active);
		
		for(CertificationProgramMailConfiguration configuration:configurations) {
			CertificationProgram program = configuration.getCertificationProgram();
			List<Certificate> toNotify = certificationProgramMailQueries.getOverdueCertificates(configuration, referenceDate);
			dbInstance.commit();
			for(Certificate certificate:toNotify) {
				Identity recipient = certificate.getIdentity();
				sendMail(recipient, program, certificate, configuration.getType(), null);
			}
		}
	}
	
	private void activityLog(Identity identity, CertificationProgram program, ILoggingAction action, RequestMode requestMode, Identity actor) {
		Long identityKey = actor == null ? null : actor.getKey();
		List<ILoggingResourceable> loggingResourceableList = new ArrayList<>();
		loggingResourceableList.add(CoreLoggingResourceable.wrap(program, OlatResourceableType.certificationProgram, program.getDisplayName()));
		loggingResourceableList.add(CoreLoggingResourceable.wrap(identity));

		activityLogService.log(action, action.getResourceActionType(), "-", identityKey, getClass(), requestMode == RequestMode.AUTOMATIC,
				"", List.of(), loggingResourceableList);
	}
	
	private void sendMail(Identity recipient, CertificationProgram program, Certificate certificate, CertificationProgramMailType type, Identity actor) {
		CertificationProgramMailConfiguration configuration = certificationProgramMailConfigurationDao.getConfiguration(program, type);
		if(configuration == null || configuration.getStatus() == CertificationProgramMailConfigurationStatus.inactive) return;
		
		MailTemplate template = CertificationProgramMailing.getTemplate(program, configuration, recipient, certificate, actor);

		MailerResult result = new MailerResult();
		MailContext context = new MailContextImpl(null, null, "[HomeSite:" + recipient.getKey() + "][Certificates:0][All:0]");
		MailBundle bundle = mailService.makeMailBundle(context, recipient, template, actor, null, result);
		if(bundle != null) {
			certificationProgramLogDao.createMailLog(certificate, configuration);
			dbInstance.commit();
			mailService.sendMessage(bundle);
		}
	}
	
	public enum Permission {
		FIRST,
		DO_NOTHING, // Don't renew the certificate
		RENEW,
		REVOKED
	}
}
