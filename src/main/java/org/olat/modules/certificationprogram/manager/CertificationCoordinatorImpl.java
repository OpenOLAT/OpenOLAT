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
import java.time.LocalDateTime;
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
import org.olat.core.util.DateUtils;
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
import org.olat.modules.certificationprogram.CertificationProgramLogAction;
import org.olat.modules.certificationprogram.CertificationProgramMailConfiguration;
import org.olat.modules.certificationprogram.CertificationProgramMailConfigurationStatus;
import org.olat.modules.certificationprogram.CertificationProgramMailType;
import org.olat.modules.certificationprogram.CertificationProgramStatusEnum;
import org.olat.modules.certificationprogram.RecertificationMode;
import org.olat.modules.certificationprogram.ui.CertificationStatus;
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
	private CertificationProgramLogQueries certificationProgramLogQueries;
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
				generateCertificate(identity, certificationProgram, null, requestMode, CertificationProgramMailType.certificate_issued, doer);
				certificationProgramLogDao.createLog(certificate, certificationProgram, CertificationProgramLogAction.add_membership,
						null, null, "certified", null, null, null, doer);
				
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
		boolean allowedMode = isCertificationAllowedByRequestMode(certificationProgram, requestMode);
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
			if(requestMode == RequestMode.COURSE) {
				// Nothing, payment from the course itself
			} else if(wallet == null || wallet.getBalance() == null || amount.compareTo(wallet.getBalance()) > 0) {
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
		CertificationProgramMailType mailType = requestMode == RequestMode.COURSE
				? CertificationProgramMailType.certificate_issued
				: CertificationProgramMailType.certificate_renewed;
		CertificationStatus currentStatus = certificate == null ? null : CertificationStatus.evaluate(certificate, referenceDate);
		Certificate newCertificate = generateCertificate(identity, certificationProgram, null, requestMode, mailType, doer);

		CertificationProgramLogAction action = CertificationProgramLogAction.add_membership;
		String beforeStatus = currentStatus == null ? null : currentStatus.name();
		certificationProgramLogDao.createLog(newCertificate, certificationProgram, action,
				beforeStatus, null, "certified", null, null, null, doer);
		dbInstance.commit();// Prevent deadlock with MySQL

		return true;
	}
	
	private void removeFromCertificationProgram(Identity identity, CertificationProgram certificationProgram, Certificate certificate,
			Date referenceDate, RequestMode requestMode, Identity doer) {
		CertificationStatus currentStatus = CertificationStatus.evaluate(certificate, referenceDate); 
		((CertificateImpl)certificate).setLast(false);
		((CertificateImpl)certificate).setRemovalDate(referenceDate);
		certificate = certificatesDao.updateCertificate(certificate);
		dbInstance.commit();
		
		log.info("User {} removed of program {} by {0}", identity, certificationProgram, doer);
		activityLog(identity, certificationProgram, CertificationLoggingAction.CERTIFICATE_REMOVED, requestMode, doer);
		String beforeStatus = currentStatus == null ? null : currentStatus.name();
		certificationProgramLogDao.createLog(certificate, certificationProgram, CertificationProgramLogAction.remove_membership,
				beforeStatus, null, "removed", null, null, null, doer);
		
		CreditPointWallet wallet = loadWallet(identity, certificationProgram);
		sendMail(identity, certificationProgram, certificate, wallet, CertificationProgramMailType.program_removed, doer);
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
	
	private boolean isCertificationAllowedByRequestMode(CertificationProgram certificationProgram, RequestMode requestMode) {
		// No recertification, only automatic renewal is forbidden
		if(!certificationProgram.isValidityEnabled() || !certificationProgram.isRecertificationEnabled()) {
			return requestMode != RequestMode.AUTOMATIC;
		}
		
		// In automatic mode, a manager can renew the certificate, the participant too
		if(certificationProgram.getRecertificationMode() == RecertificationMode.automatic) {
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
		} else if (requestMode == RequestMode.COURSE) {
			// If a participant does the course again, there are no dates restrictions
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
			
			allowed = (nextRecertificationDate != null && nextRecertificationDate.compareTo(referenceDate) <= 0
					&& (recertificationWindowDate == null || recertificationWindowDate.compareTo(referenceDate) >= 0));
		} else {
			allowed = false;
		}
		return allowed;
	}
	
	@Override
	public Certificate generateCertificate(Identity identity, CertificationProgram certificationProgram, Date issuedDate,
			RequestMode requestMode, CertificationProgramMailType notificationType, Identity actor) {
		// Archive the last certificate
		certificatesDao.removeLastFlag(identity, certificationProgram);
		dbInstance.commit();
		
		// Generate a new certificate
		// No course informations, only certification program informations
		CertificateInfos certificateInfos = CertificateInfos.valueOf(identity, null, null, actor);
		if(issuedDate != null) {
			certificateInfos.setCreationDate(issuedDate);
		}
		CertificateConfig config = CertificateConfig.builder()
				.withCustom1(certificationProgram.getCertificateCustom1())
				.withCustom2(certificationProgram.getCertificateCustom2())
				.withCustom3(certificationProgram.getCertificateCustom3())
				.withSendEmailBcc(true)
				.withSendEmailLinemanager(true)
				.withSendEmailIdentityRelations(true)
				.withCertificationProgramMailType(notificationType)
				.build();
		Certificate certificate = certificatesManager.generateCertificate(certificateInfos, certificationProgram, null, config);
		
		activityLog(identity, certificationProgram, CertificationLoggingAction.CERTIFICATE_ISSUED, requestMode, actor);
		certificationProgramLogDao.createLog(certificate, certificationProgram, CertificationProgramLogAction.issue_certificate,
				null, null, CertificationStatus.VALID.name(), null, null, null, actor);
		dbInstance.commit();// Prevent deadlock with MySQL
		
		return certificate;
	}
	
	@Override
	public Certificate revokeRecertification(CertificationProgram certificationProgram, Identity identity, Identity actor) {
		Certificate certificate = certificatesDao.getLastCertificate(identity, certificationProgram);
		if(certificate != null) {
			CertificationStatus currentStatus = CertificationStatus.evaluate(certificate, new Date());
			certificatesManager.revokeCertificate(certificate);

			log.info("Certificate revoked {} for {} and program {} by {}", certificate, identity, certificationProgram, actor);
			activityLog(identity, certificationProgram, CertificationLoggingAction.CERTIFICATE_REVOKED, RequestMode.COACH, actor);
			sendMail(identity, certificationProgram, certificate, null, CertificationProgramMailType.certificate_revoked, actor);
			
			certificationProgramLogDao.createLog(certificate, certificationProgram, CertificationProgramLogAction.revoke_certificate,
					currentStatus.name(), null, CertificationStatus.REVOKED.name(), null, null, null, actor);
		}
		return certificate;
	}
	
	@Override
	public void sendExpiredNotifications(Date referenceDate) {
		List<CertificationProgramMailConfiguration> configurations = certificationProgramMailConfigurationDao
				.getConfigurations(CertificationProgramMailType.certificate_expired,
						CertificationProgramMailConfigurationStatus.active, CertificationProgramStatusEnum.active);
		
		for(CertificationProgramMailConfiguration configuration:configurations) {
			CertificationProgram program = configuration.getCertificationProgram();
			List<Certificate> toNotify = certificationProgramMailQueries.getExpiredCertificates(configuration, referenceDate);
			dbInstance.commit();
			for(Certificate certificate:toNotify) {
				Identity recipient = certificate.getIdentity();
				CreditPointWallet wallet = loadWallet(recipient, program);
				log.debug("Send certificate expired notification: {} for user {}-{} with configuration {} ({})", program.getKey(), certificate.getKey(), recipient.getKey(), configuration.getKey(), configuration.getType());
				sendMail(recipient, program, certificate, wallet, configuration, null);
			}
		}
	}
	
	@Override
	public void logExpiredMemberships(Date referenceDate) {
		Date beforeDate = DateUtils.addDays(referenceDate, -1);
		List<CertificationProgram> programs = certificationProgramDao.loadCertificationPrograms();
		for(CertificationProgram program:programs) {
			if(program.getStatus() == CertificationProgramStatusEnum.inactive || !program.isValidityEnabled()) continue;
			
			List<Certificate> expiredCertificates = certificationProgramLogQueries.getExpiredCertificates(program, referenceDate);
			for(Certificate expiredCertificate:expiredCertificates) {
				LocalDateTime date;
				CertificationStatus status;
				if(expiredCertificate.getNextRecertificationDate() != null) {
					// Set the date the next day at 00:00:00
					date = getStartNextDay(expiredCertificate.getNextRecertificationDate());
					status = CertificationStatus.evaluate(expiredCertificate, DateUtils.addHours(expiredCertificate.getNextRecertificationDate(), -1));
				} else {
					date = DateUtils.toLocalDateTime(referenceDate);
					status = CertificationStatus.evaluate(expiredCertificate, beforeDate);
				}
				String beforeStatus = status == null ? null : status.name();
				certificationProgramLogDao.createLog(date, expiredCertificate, program, CertificationProgramLogAction.expire_certificate,
						beforeStatus, null, CertificationStatus.EXPIRED.name(), null, null, null, null);
				dbInstance.commit();
			}
			dbInstance.commitAndCloseSession();
		}
		dbInstance.commitAndCloseSession();
	}

	@Override
	public void sendRemovedNotifications(Date referenceDate) {
		List<CertificationProgramMailConfiguration> configurations = certificationProgramMailConfigurationDao
				.getConfigurations(CertificationProgramMailType.program_removed,
						CertificationProgramMailConfigurationStatus.active, CertificationProgramStatusEnum.active);
		
		for(CertificationProgramMailConfiguration configuration:configurations) {
			CertificationProgram program = configuration.getCertificationProgram();
			List<Certificate> toNotify = certificationProgramMailQueries.getRemovedCertificates(configuration, referenceDate);
			dbInstance.commit();
			for(Certificate certificate:toNotify) {
				Identity recipient = certificate.getIdentity();
				if(certificate.getRemovalDate() == null) {
					// Let the last flag for renewing (Use case 5 for example)
					((CertificateImpl)certificate).setRemovalDate(referenceDate);
					certificate = certificatesDao.updateCertificate(certificate);
					dbInstance.commit();
					if(certificate.isLast()) {
						log.info("User {} removed of program {} automatically", recipient, program);
						activityLog(recipient, program, CertificationLoggingAction.CERTIFICATE_REMOVED, RequestMode.AUTOMATIC, null);
						certificationProgramLogDao.createMailLog(certificate, program, configuration, null);
					}
				}
				
				CreditPointWallet wallet = loadWallet(recipient, program);
				sendMail(recipient, program, certificate, wallet, configuration, null);
			}
		}
	}
	
	@Override
	public void logRemovedMemberships(Date referenceDate) {
		Date beforeDate = DateUtils.addDays(referenceDate, -1);
		List<CertificationProgram> programs = certificationProgramDao.loadCertificationPrograms();
		for(CertificationProgram program:programs) {
			if(program.getStatus() == CertificationProgramStatusEnum.inactive || !program.isValidityEnabled()) continue;
			
			List<Certificate> removedCertificates = certificationProgramLogQueries.getRemovedCertificates(program, referenceDate);
			for(Certificate removedCertificate:removedCertificates) {

				LocalDateTime date;
				CertificationStatus status; 
				if(removedCertificate.getRemovalDate() != null) {
					date = DateUtils.toLocalDateTime(removedCertificate.getRemovalDate());
					status = CertificationStatus.evaluate(removedCertificate, DateUtils.addDays(removedCertificate.getRemovalDate(), -1));
				} else if(removedCertificate.getRecertificationWindowDate() != null) {
					// Set the date the next day at 00:00:00
					date = getStartNextDay(removedCertificate.getRecertificationWindowDate());
					status = CertificationStatus.evaluate(removedCertificate, DateUtils.addDays(removedCertificate.getRecertificationWindowDate(), -1));
				} else if(removedCertificate.getNextRecertificationDate() != null) {
					// Set the date the next day at 00:00:00
					date = getStartNextDay(removedCertificate.getNextRecertificationDate());
					status = CertificationStatus.evaluate(removedCertificate, DateUtils.addDays(removedCertificate.getNextRecertificationDate(), -1));
				} else {
					date = DateUtils.toLocalDateTime(referenceDate);
					status = CertificationStatus.evaluate(removedCertificate, beforeDate);
				}
				String beforeStatus = status == null ? null : status.name();
				certificationProgramLogDao.createLog(date, removedCertificate, program, CertificationProgramLogAction.remove_membership,
						beforeStatus, null, "removed", null, null, null, null);
				dbInstance.commit();
			}
			dbInstance.commitAndCloseSession();
		}
		dbInstance.commitAndCloseSession();
	}
	
	private LocalDateTime getStartNextDay(Date date) {
		if(date == null) return null;
		return DateUtils.getStartOfDay(DateUtils.toLocalDateTime(date).plusDays(1));
	}

	@Override
	public void sendUpcomingReminders(Date referenceDate) {
		List<CertificationProgramMailConfiguration> configurations = certificationProgramMailConfigurationDao
				.getConfigurations(CertificationProgramMailType.reminder_upcoming,
						CertificationProgramMailConfigurationStatus.active, CertificationProgramStatusEnum.active);
		
		for(CertificationProgramMailConfiguration configuration:configurations) {
			CertificationProgram program = configuration.getCertificationProgram();
			List<Certificate> toNotify = certificationProgramMailQueries.getUpcomingCertificates(configuration, referenceDate);
			dbInstance.commit();
			for(Certificate certificate:toNotify) {
				Identity recipient = certificate.getIdentity();
				CreditPointWallet wallet = loadWallet(recipient, program);
				sendMail(recipient, program, certificate, wallet, configuration, null);
			}
		}
	}

	@Override
	public void sendOverdueReminders(Date referenceDate) {
		List<CertificationProgramMailConfiguration> configurations = certificationProgramMailConfigurationDao
				.getConfigurations(CertificationProgramMailType.reminder_overdue,
						CertificationProgramMailConfigurationStatus.active, CertificationProgramStatusEnum.active);
		
		for(CertificationProgramMailConfiguration configuration:configurations) {
			CertificationProgram program = configuration.getCertificationProgram();
			List<Certificate> toNotify = certificationProgramMailQueries.getOverdueCertificates(configuration, referenceDate);
			dbInstance.commit();
			for(Certificate certificate:toNotify) {
				Identity recipient = certificate.getIdentity();
				CreditPointWallet wallet = loadWallet(recipient, program);
				sendMail(recipient, program, certificate, wallet, configuration, null);
			}
		}
	}
	
	private CreditPointWallet loadWallet(Identity identity, CertificationProgram program) {
		return program.hasCreditPoints()
				? creditPointService.getOrCreateWallet(identity, program.getCreditPointSystem())
				: null;
	}
	
	/**
	 * Write an entry in the generic logging table.
	 * 
	 * @param identity The identity
	 * @param program The certification program
	 * @param action The action
	 * @param requestMode The request mode
	 * @param actor The actor
	 */
	private void activityLog(Identity identity, CertificationProgram program, ILoggingAction action,
			RequestMode requestMode, Identity actor) {
		Long identityKey = actor == null ? null : actor.getKey();
		List<ILoggingResourceable> loggingResourceableList = new ArrayList<>();
		loggingResourceableList.add(CoreLoggingResourceable.wrap(program, OlatResourceableType.certificationProgram, program.getDisplayName()));
		loggingResourceableList.add(CoreLoggingResourceable.wrap(identity));

		activityLogService.log(action, action.getResourceActionType(), "-", identityKey, getClass(), requestMode == RequestMode.AUTOMATIC,
				"", List.of(), loggingResourceableList);
	}
	
	private void sendMail(Identity recipient, CertificationProgram program, Certificate certificate, CreditPointWallet wallet,
			CertificationProgramMailType type, Identity actor) {
		CertificationProgramMailConfiguration configuration = certificationProgramMailConfigurationDao.getConfiguration(program, type);
		if(configuration == null || configuration.getStatus() == CertificationProgramMailConfigurationStatus.inactive) return;
		
		sendMail(recipient, program, certificate, wallet, configuration, actor);
	}
	
	private void sendMail(Identity recipient, CertificationProgram program, Certificate certificate, CreditPointWallet wallet,
			CertificationProgramMailConfiguration configuration, Identity actor) {
		if(configuration == null || configuration.getStatus() == CertificationProgramMailConfigurationStatus.inactive) return;
		
		MailTemplate template = CertificationProgramMailing.getTemplate(program, configuration, recipient, certificate, null, wallet, actor);
		MailerResult result = new MailerResult();
		MailContext context = new MailContextImpl(null, null, "[HomeSite:" + recipient.getKey() + "][Certificates:0][All:0]");
		MailBundle bundle = mailService.makeMailBundle(context, recipient, template, actor, null, result);
		if(bundle != null) {
			certificationProgramLogDao.createMailLog(certificate, program, configuration, actor);
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
