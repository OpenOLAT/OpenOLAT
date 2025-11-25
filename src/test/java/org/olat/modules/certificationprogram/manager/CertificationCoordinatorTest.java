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
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.DateUtils;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificateStatus;
import org.olat.course.certificate.manager.CertificatesDAO;
import org.olat.course.certificate.model.CertificateImpl;
import org.olat.modules.certificationprogram.CertificationCoordinator;
import org.olat.modules.certificationprogram.CertificationCoordinator.RequestMode;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramMailType;
import org.olat.modules.certificationprogram.CertificationProgramService;
import org.olat.modules.certificationprogram.RecertificationMode;
import org.olat.modules.certificationprogram.ui.CertificationIdentityStatus;
import org.olat.modules.certificationprogram.ui.CertificationStatus;
import org.olat.modules.certificationprogram.ui.component.DurationType;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.CreditPointTransactionType;
import org.olat.modules.creditpoint.CreditPointWallet;
import org.olat.modules.creditpoint.manager.CreditPointServiceImpl;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.manager.CurriculumDAO;
import org.olat.modules.curriculum.manager.CurriculumElementDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.dumbster.smtp.SmtpMessage;

/**
 * 
 * Initial date: 23 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationCoordinatorTest extends OlatTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(CertificationCoordinatorTest.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumDAO curriculumDao;
	@Autowired
	private CertificatesDAO certificatesDao;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private CurriculumElementDAO curriculumElementDao;
	@Autowired
	private CreditPointServiceImpl creditPointService;
	@Autowired
	private CertificationCoordinator certificationCoordinator;
	@Autowired
	private CertificationProgramService certificationProgramService;
	
	@Test
	public void processCertificateOfSimpleProgram() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("prog-participant-4", Locale.ENGLISH);
		CertificationProgram program = certificationProgramService.createCertificationProgram("program-to-curriculum-4", "CP1", null);
		dbInstance.commitAndCloseSession();

		boolean allOk = certificationCoordinator.processCertificationRequest(participant, program, RequestMode.AUTOMATIC, new Date(), null);
		Assert.assertTrue(allOk);
		
		List<Certificate> certificates = certificationProgramService.getCertificates(participant, program);
		Assertions.assertThat(certificates)
			.hasSize(1);
	}
	
	/**
	 * The first certificate is paid by the course. The certificate will not
	 * touch the wallet.
	 */
	@Test
	public void processCertificateOfProgramWithCreditPointsFirstFree() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("prog-participant-4", Locale.ENGLISH);
		CertificationProgram program = certificationProgramService.createCertificationProgram("program-to-curriculum-4", "CP2", null);
		CreditPointSystem system = creditPointService.createCreditPointSystem("Unit test coins", "UT1", null, null, false, false);
		program.setCreditPoints(new BigDecimal("20"));
		program.setCreditPointSystem(system);
		certificationProgramService.updateCertificationProgram(program);
		dbInstance.commit();
		
		CreditPointWallet wallet = creditPointService.getOrCreateWallet(participant, system);
		creditPointService.createCreditPointTransaction(CreditPointTransactionType.deposit, new BigDecimal("30"), null, "Give away", wallet, participant, null, null, null, null, null);
		dbInstance.commitAndCloseSession();
		
		boolean allOk = certificationCoordinator.processCertificationRequest(participant, program, RequestMode.AUTOMATIC, new Date(), null);
		dbInstance.commitAndCloseSession();
		Assert.assertTrue(allOk);

		List<Certificate> certificates = certificationProgramService.getCertificates(participant, program);
		Assertions.assertThat(certificates)
			.hasSize(1);
		assertBalance(participant, system, new BigDecimal("30"));
	}
	
	/**
	 * Certificate program costs 20 credits, participant has only 15. It cannot become
	 * the certificate.
	 */
	@Test
	public void processCertificateOfProgramWithNotEnoughCreditPoints() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("prog-participant-4", Locale.ENGLISH);
		CertificationProgram program = certificationProgramService.createCertificationProgram("program-to-curriculum-4", "CP3", null);
		CreditPointSystem system = creditPointService.createCreditPointSystem("Unit test coins", "UT1", null, null, false, false);
		program.setCreditPoints(new BigDecimal("20"));
		program.setCreditPointSystem(system);
		program.setValidityEnabled(true);
		program.setValidityTimelapse(30);
		program.setValidityTimelapseUnit(DurationType.day);
		program.setRecertificationMode(RecertificationMode.automatic);
		program.setRecertificationWindowEnabled(true);
		program.setRecertificationWindow(30);
		program.setRecertificationWindowUnit(DurationType.day);
		certificationProgramService.updateCertificationProgram(program);
		dbInstance.commit();
		
		boolean coursePaidOk = certificationCoordinator.processCertificationRequest(participant, program, RequestMode.COURSE, new Date(), participant);
		Assert.assertTrue(coursePaidOk);
		
		List<Certificate> coursePaidCertificate = certificationProgramService.getCertificates(participant, program);
		Assertions.assertThat(coursePaidCertificate)
			.hasSize(1);
		
		CreditPointWallet wallet = creditPointService.getOrCreateWallet(participant, system);
		creditPointService.createCreditPointTransaction(CreditPointTransactionType.deposit, new BigDecimal("15"), null, "Give away", wallet, participant, null, null, null, null, null);
		dbInstance.commitAndCloseSession();
		
		Certificate currentCertificate = certificatesDao.getLastCertificate(participant, program);
		currentCertificate.setNextRecertificationDate(DateUtils.addDays(new Date(), -32));
		((CertificateImpl)currentCertificate).setRecertificationWindowDate(DateUtils.addDays(new Date(), 15));
		currentCertificate = certificatesDao.updateCertificate(currentCertificate);
		dbInstance.commitAndCloseSession();
		
		boolean notOk = certificationCoordinator.processCertificationRequest(participant, program, RequestMode.AUTOMATIC, new Date(), participant);
		Assert.assertFalse(notOk);
		
		List<Certificate> certificates = certificationProgramService.getCertificates(participant, program);
		Assertions.assertThat(certificates)
			.hasSize(1)
			.containsExactlyInAnyOrderElementsOf(coursePaidCertificate);
	}
	
	/**
	 * Certificate program costs 20 credits, participant has only 15. It cannot become
	 * the certificate.
	 */
	@Test
	public void processRecertification() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("prog-participant-4", Locale.ENGLISH);
		CertificationProgram program = certificationProgramService.createCertificationProgram("program-to-curriculum-5", "CP5", null);
		CreditPointSystem system = creditPointService.createCreditPointSystem("Unit test coins", "UT5", null, null, false, false);
		program.setCreditPoints(new BigDecimal("20"));
		program.setCreditPointSystem(system);
		program.setRecertificationEnabled(true);
		program.setValidityEnabled(true);
		program.setValidityTimelapse(30);
		program.setValidityTimelapseUnit(DurationType.day);
		program.setRecertificationMode(RecertificationMode.automatic);
		program.setRecertificationWindowEnabled(true);
		program.setRecertificationWindow(30);
		program.setRecertificationWindowUnit(DurationType.day);
		certificationProgramService.updateCertificationProgram(program);
		dbInstance.commit();
		
		CreditPointWallet wallet = creditPointService.getOrCreateWallet(participant, system);
		creditPointService.createCreditPointTransaction(CreditPointTransactionType.deposit, new BigDecimal("100"), null, "Give away", wallet, participant, null, null, null, null, null);
		dbInstance.commitAndCloseSession();
		
		boolean allOk = certificationCoordinator.processCertificationRequest(participant, program, RequestMode.COURSE, new Date(), participant);
		Assert.assertTrue(allOk);
		waitMessageAreConsumed();
		
		List<Certificate> certificates = certificationProgramService.getCertificates(participant, program);
		Assertions.assertThat(certificates)
			.hasSize(1);
		assertMessage(program, CertificationProgramMailType.certificate_issued);
		
		Certificate currentCertificate = certificatesDao.getLastCertificate(participant, program);
		currentCertificate.setNextRecertificationDate(DateUtils.addDays(new Date(), -25));
		((CertificateImpl)currentCertificate).setRecertificationWindowDate(DateUtils.addDays(new Date(), 5));
		currentCertificate = certificatesDao.updateCertificate(currentCertificate);
		dbInstance.commitAndCloseSession();
		
		boolean recertOk = certificationCoordinator.processCertificationRequest(participant, program, RequestMode.COURSE, new Date(), participant);
		Assert.assertTrue(recertOk);
		waitMessageAreConsumed();
		
		List<Certificate> recertificates = certificationProgramService.getCertificates(participant, program);
		Assertions.assertThat(recertificates)
			.hasSize(2);
		Certificate reCertificate = certificatesDao.getLastCertificate(participant, program);
		Assert.assertNotEquals(currentCertificate, reCertificate);
		assertMessage(program, CertificationProgramMailType.certificate_issued);
	}
	
	/**
	 * Negative test of the recertification window 
	 */
	@Test
	public void processRecertificationNotAllowedBecauseWindow() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("prog-participant-6", Locale.ENGLISH);
		CertificationProgram program = certificationProgramService.createCertificationProgram("program-to-curriculum-6", "CP6", null);
		CreditPointSystem system = creditPointService.createCreditPointSystem("Unit test coins", "UT6", null, null, false, false);
		program.setCreditPoints(new BigDecimal("20"));
		program.setCreditPointSystem(system);
		program.setRecertificationEnabled(true);
		program.setValidityEnabled(true);
		program.setValidityTimelapse(30);
		program.setValidityTimelapseUnit(DurationType.day);
		program.setRecertificationMode(RecertificationMode.automatic);
		program.setRecertificationWindowEnabled(true);
		program.setRecertificationWindow(30);
		program.setRecertificationWindowUnit(DurationType.day);
		certificationProgramService.updateCertificationProgram(program);
		dbInstance.commit();
		
		CreditPointWallet wallet = creditPointService.getOrCreateWallet(participant, system);
		creditPointService.createCreditPointTransaction(CreditPointTransactionType.deposit, new BigDecimal("100"), null, "Give away", wallet, participant, null, null, null, null, null);
		dbInstance.commitAndCloseSession();
		
		boolean allOk = certificationCoordinator.processCertificationRequest(participant, program, RequestMode.COURSE, new Date(), participant);
		Assert.assertTrue(allOk);
		
		List<Certificate> certificates = certificationProgramService.getCertificates(participant, program);
		Assertions.assertThat(certificates)
			.hasSize(1);
		
		Certificate currentCertificate = certificatesDao.getLastCertificate(participant, program);
		currentCertificate.setNextRecertificationDate(DateUtils.addDays(new Date(), -65));
		((CertificateImpl)currentCertificate).setRecertificationWindowDate(DateUtils.addDays(new Date(), -35));
		currentCertificate = certificatesDao.updateCertificate(currentCertificate);
		dbInstance.commitAndCloseSession();
		
		boolean recertOk = certificationCoordinator.processCertificationRequest(participant, program, RequestMode.COURSE, new Date(), participant);
		Assert.assertTrue(recertOk);
		
		List<Certificate> recertificates = certificationProgramService.getCertificates(participant, program);
		Assertions.assertThat(recertificates)
			.hasSize(2);
		
		Certificate afterRecertificationTryCertificate = certificatesDao.getLastCertificate(participant, program);
		Assert.assertNotEquals(currentCertificate, afterRecertificationTryCertificate);
	}
	
	/**
	 * Certificate program costs 20 credits, but only manual renewal, participant has 100.
	 * It cannot become the certificate if the participant to renew it, or the job.
	 */
	@Test
	public void processRecertificationManualOnly() {
		Identity actor = JunitTestHelper.getDefaultActor();
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("prog-participant-7", Locale.ENGLISH);
		CertificationProgram program = certificationProgramService.createCertificationProgram("program-to-curriculum-7", "CP7", null);
		CreditPointSystem system = creditPointService.createCreditPointSystem("Unit test coins", "UT7", null, null, false, false);
		program.setCreditPoints(new BigDecimal("20"));
		program.setCreditPointSystem(system);
		program.setRecertificationEnabled(true);
		program.setValidityEnabled(true);
		program.setValidityTimelapse(30);
		program.setValidityTimelapseUnit(DurationType.day);
		program.setRecertificationMode(RecertificationMode.manual);
		program.setRecertificationWindowEnabled(true);
		program.setRecertificationWindow(30);
		program.setRecertificationWindowUnit(DurationType.day);
		certificationProgramService.updateCertificationProgram(program);
		dbInstance.commit();
		
		CreditPointWallet wallet = creditPointService.getOrCreateWallet(participant, system);
		creditPointService.createCreditPointTransaction(CreditPointTransactionType.deposit, new BigDecimal("100"), null, "Give away", wallet, participant, null, null, null, null, null);
		dbInstance.commitAndCloseSession();
		
		boolean allOk = certificationCoordinator.processCertificationRequest(participant, program, RequestMode.COURSE, new Date(), participant);
		Assert.assertTrue(allOk);
		
		List<Certificate> certificates = certificationProgramService.getCertificates(participant, program);
		Assertions.assertThat(certificates)
			.hasSize(1);
		
		Certificate currentCertificate = certificatesDao.getLastCertificate(participant, program);
		currentCertificate.setNextRecertificationDate(DateUtils.addDays(new Date(), -25));
		((CertificateImpl)currentCertificate).setRecertificationWindowDate(DateUtils.addDays(new Date(), 5));
		currentCertificate = certificatesDao.updateCertificate(currentCertificate);
		dbInstance.commitAndCloseSession();
		
		boolean recertCronJobNok = certificationCoordinator.processCertificationRequest(participant, program, RequestMode.AUTOMATIC, new Date(), null);
		Assert.assertFalse(recertCronJobNok);
		
		// Coach can renew a certificate if credit point involved
		boolean recertCoachOk = certificationCoordinator.processCertificationRequest(participant, program, RequestMode.COACH, new Date(), actor);
		Assert.assertTrue(recertCoachOk);
		
		List<Certificate> certificatesRenewed = certificationProgramService.getCertificates(participant, program);
		Assertions.assertThat(certificatesRenewed)
			.hasSize(2);
		
		boolean recertParticipantOk = certificationCoordinator.processCertificationRequest(participant, program, RequestMode.COURSE, new Date(), participant);
		Assert.assertTrue(recertParticipantOk);
		
		List<Certificate> recertificates = certificationProgramService.getCertificates(participant, program);
		Assertions.assertThat(recertificates)
			.hasSize(3);
		
		Certificate reCertificate = certificatesDao.getLastCertificate(participant, program);
		Assert.assertNotEquals(currentCertificate, reCertificate);
	}
	
	/**
	 * Use case 1 @see https://track.frentix.com/issue/OO-9065<br>
	 * 
	 * Validity: OFF<br>
	 * Recertification: OFF<br>
	 * Recertification timeframe: NONE<br>
	 * Credit points: OFF
	 */
	@Test
	public void processUseCase1() {
		Identity actor = JunitTestHelper.getDefaultActor();
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("usecase-1-participant-4", Locale.ENGLISH);
		CertificationProgram program = certificationProgramService.createCertificationProgram("usecase-1-to-curriculum-4", "UC1", null);
		program.setValidityEnabled(false);
		program.setRecertificationEnabled(false);
		program.setRecertificationWindowEnabled(false);
		program = certificationProgramService.updateCertificationProgram(program);
		dbInstance.commit();
		
		boolean courseOk = certificationCoordinator.processCertificationRequest(participant, program, RequestMode.COURSE, new Date(), participant);
		Assert.assertTrue(courseOk);
		waitMessageAreConsumed();// Wait certificate is generated
		
		List<Certificate> firstCertificates = certificationProgramService.getCertificates(participant, program);
		Assertions.assertThat(firstCertificates)
			.hasSize(1);

		Certificate firstCertificate = certificatesDao.getLastCertificate(participant, program);
		assertCertificateStatus(firstCertificate, CertificationStatus.VALID, CertificationIdentityStatus.CERTIFIED);
		assertMessage(program, CertificationProgramMailType.certificate_issued);
		
		// Revoke the certificate
		certificationCoordinator.revokeRecertification(program, participant, actor);
		dbInstance.commitAndCloseSession();
		
		Certificate noLastCertificate = certificatesDao.getLastCertificate(participant, program);
		Assert.assertNull(noLastCertificate);
		
		List<Certificate> revokedCertificates = certificationProgramService.getCertificates(participant, program);
		Assertions.assertThat(revokedCertificates)
			.hasSize(1);
		Certificate revokedCertificate = revokedCertificates.get(0);
		Assert.assertEquals(CertificateStatus.revoked, revokedCertificate.getStatus());
		Assert.assertFalse(revokedCertificate.isLast());
		assertCertificateStatus(revokedCertificate, CertificationStatus.REVOKED, CertificationIdentityStatus.REMOVED);
		assertMessage(program, CertificationProgramMailType.certificate_revoked);
		
		// Try again the course and get a second valid certificate
		boolean courseAgainOk = certificationCoordinator.processCertificationRequest(participant, program, RequestMode.COURSE, new Date(), participant);
		Assert.assertTrue(courseAgainOk);
		waitMessageAreConsumed();// Wait certificate is generated
		
		List<Certificate> secondCertificates = certificationProgramService.getCertificates(participant, program);
		Assertions.assertThat(secondCertificates)
			.hasSize(2);
		
		Certificate secondCertificate = certificatesDao.getLastCertificate(participant, program);
		assertCertificateStatus(secondCertificate, CertificationStatus.VALID, CertificationIdentityStatus.CERTIFIED);
		assertMessage(program, CertificationProgramMailType.certificate_issued);

		// Try again the course a third time and get a third valid certificate
		boolean courseThirdOk = certificationCoordinator.processCertificationRequest(participant, program, RequestMode.COURSE, new Date(), participant);
		Assert.assertTrue(courseThirdOk);
		waitMessageAreConsumed();// Wait certificate is generated
		
		List<Certificate> thirdCertificates = certificationProgramService.getCertificates(participant, program);
		Assertions.assertThat(thirdCertificates)
			.hasSize(3);
		
		Certificate thirdCertificate = certificatesDao.getLastCertificate(participant, program);
		Assert.assertNotEquals(secondCertificate, thirdCertificate);
		assertCertificateStatus(thirdCertificate, CertificationStatus.VALID, CertificationIdentityStatus.CERTIFIED);
		assertMessage(program, CertificationProgramMailType.certificate_issued);
	}
	
	/**
	 * Use case 3 @see https://track.frentix.com/issue/OO-9065<br>
	 * 
	 * Validity: ON<br>
	 * Recertification: ON (manually)<br>
	 * Recertification timeframe: NONE<br>
	 * Credit points: OFF
	 */
	@Test
	public void processUseCase3() {
		Identity actor = JunitTestHelper.getDefaultActor();
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("usecase-1-participant-4", Locale.ENGLISH);
		CertificationProgram program = certificationProgramService.createCertificationProgram("usecase-1-to-curriculum-4", "UC3", null);
		program.setValidityEnabled(true);
		program.setValidityTimelapse(7);
		program.setValidityTimelapseUnit(DurationType.day);
		program.setRecertificationEnabled(true);
		program.setRecertificationMode(RecertificationMode.manual);
		program.setRecertificationWindowEnabled(false);
		program = certificationProgramService.updateCertificationProgram(program);
		dbInstance.commit();
		
		boolean courseOk = certificationCoordinator.processCertificationRequest(participant, program, RequestMode.COURSE, new Date(), participant);
		Assert.assertTrue(courseOk);
		waitMessageAreConsumed();// Wait certificate is generated
		
		List<Certificate> firstCertificates = certificationProgramService.getCertificates(participant, program);
		Assertions.assertThat(firstCertificates)
			.hasSize(1);
		Certificate firstCertificate = certificatesDao.getLastCertificate(participant, program);
		assertCertificateStatus(firstCertificate, CertificationStatus.VALID, CertificationIdentityStatus.CERTIFIED);
		assertMessage(program, CertificationProgramMailType.certificate_issued);
		
		// Move date
		updateCertificate(firstCertificate, DateUtils.addDays(new Date(), -9), program);
		Certificate expiredLastCertificate = certificatesDao.getLastCertificate(participant, program);
		Assert.assertNotNull(expiredLastCertificate);
		assertCertificateStatus(expiredLastCertificate, CertificationStatus.EXPIRED, CertificationIdentityStatus.REMOVED);
		
		// Job does nothing
		boolean notOk = certificationCoordinator.processCertificationRequest(participant, program, RequestMode.AUTOMATIC, new Date(), null);
		Assert.assertFalse(notOk);
		
		// The manager recertifiy the participant
		boolean managerOk = certificationCoordinator.processCertificationRequest(participant, program, RequestMode.COACH, new Date(), actor);
		Assert.assertTrue(managerOk);
		waitMessageAreConsumed();// Wait certificate is generated
		
		List<Certificate> secondCertificates = certificationProgramService.getCertificates(participant, program);
		Assertions.assertThat(secondCertificates)
			.hasSize(2);
		Certificate secondCertificate = certificatesDao.getLastCertificate(participant, program);
		assertCertificateStatus(secondCertificate, CertificationStatus.VALID, CertificationIdentityStatus.CERTIFIED);
		assertMessage(program, CertificationProgramMailType.certificate_renewed);
		
		// Manager revoked the certificate
		certificationCoordinator.revokeRecertification(program, participant, actor);
		Certificate revokedCertificate = certificatesDao.getLastCertificate(participant, program);
		Assert.assertNull(revokedCertificate);
		assertMessage(program, CertificationProgramMailType.certificate_revoked);
		
		// Participant does the course again and get a new valid certificate
		boolean courseAgainOk = certificationCoordinator.processCertificationRequest(participant, program, RequestMode.COURSE, new Date(), participant);
		Assert.assertTrue(courseAgainOk);
		waitMessageAreConsumed();// Wait certificate is generated
		
		List<Certificate> thirdCertificates = certificationProgramService.getCertificates(participant, program);
		Assertions.assertThat(thirdCertificates)
			.hasSize(3);
		Certificate thirdCertificate = certificatesDao.getLastCertificate(participant, program);
		assertCertificateStatus(thirdCertificate, CertificationStatus.VALID, CertificationIdentityStatus.CERTIFIED);
		assertMessage(program, CertificationProgramMailType.certificate_issued);
		
		// Wait a little too much
		updateCertificate(thirdCertificate, DateUtils.addDays(new Date(), -8), program);
		Certificate expiredAgainLastCertificate = certificatesDao.getLastCertificate(participant, program);
		Assert.assertNotNull(expiredAgainLastCertificate);
		assertCertificateStatus(expiredAgainLastCertificate, CertificationStatus.EXPIRED, CertificationIdentityStatus.REMOVED);
		//TODO certification removed assertMessage(program, CertificationProgramMailType.program_removed);
	}
	
	@Test
	public void processUseCase4() {
		Identity actor = JunitTestHelper.getDefaultActor();
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("usecase-1-participant-4", Locale.ENGLISH);
		CertificationProgram program = certificationProgramService.createCertificationProgram("usecase-1-to-curriculum-4", "UC3", null);
		program.setValidityEnabled(true);
		program.setValidityTimelapse(7);
		program.setValidityTimelapseUnit(DurationType.day);
		program.setRecertificationEnabled(true);
		program.setRecertificationMode(RecertificationMode.manual);
		program.setRecertificationWindowEnabled(true);
		program.setRecertificationWindow(1);
		program.setRecertificationWindowUnit(DurationType.month);
		program = certificationProgramService.updateCertificationProgram(program);
		dbInstance.commit();
		
		boolean courseOk = certificationCoordinator.processCertificationRequest(participant, program, RequestMode.COURSE, new Date(), participant);
		Assert.assertTrue(courseOk);
		waitMessageAreConsumed();// Wait certificate is generated
		
		List<Certificate> firstCertificates = certificationProgramService.getCertificates(participant, program);
		Assertions.assertThat(firstCertificates)
			.hasSize(1);
		Certificate firstCertificate = certificatesDao.getLastCertificate(participant, program);
		assertCertificateStatus(firstCertificate, CertificationStatus.VALID, CertificationIdentityStatus.CERTIFIED);
		assertMessage(program, CertificationProgramMailType.certificate_issued);
		
		// Move date a little bit
		updateCertificate(firstCertificate, DateUtils.addDays(firstCertificate.getCreationDate(), -3), program);
		
		// Manager renews it
		boolean coachRenewOk = certificationCoordinator.processCertificationRequest(participant, program, RequestMode.COACH, new Date(), actor);
		Assert.assertTrue(coachRenewOk);
		
		//TODO certification
	}
	
	/**
	 * Use case 5 @see https://track.frentix.com/issue/OO-9065<br>
	 * 
	 * Validity: ON<br>
	 * Recertification: ON (automatic)<br>
	 * Recertification timeframe: NONE<br>
	 * Credit points: ON - 10 CP
	 */
	@Test
	public void processUseCase5() {
		Identity actor = JunitTestHelper.getDefaultActor();
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("usecase-5-participant-4", Locale.ENGLISH);
		CreditPointSystem system = creditPointService.createCreditPointSystem("Unit test coins", "UT1", null, null, false, false);
		CertificationProgram program = certificationProgramService.createCertificationProgram("usecase-5-to-curriculum-4", "UC5", null);
		program.setValidityEnabled(true);
		program.setValidityTimelapse(7);
		program.setValidityTimelapseUnit(DurationType.day);
		program.setRecertificationEnabled(true);
		program.setRecertificationWindowEnabled(false);
		program.setRecertificationMode(RecertificationMode.automatic);
		program.setCreditPoints(new BigDecimal("10"));
		program.setCreditPointSystem(system);
		program = certificationProgramService.updateCertificationProgram(program);
		dbInstance.commit();
		
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-el-1", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-1", "1. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addMember(element, participant, CurriculumRoles.participant, actor);
		certificationProgramService.addCurriculumElementToCertificationProgram(program, element);
		dbInstance.commit();
		
		boolean courseOk = certificationCoordinator.processCertificationRequest(participant, program, RequestMode.COURSE, new Date(), participant);
		Assert.assertTrue(courseOk);
		waitMessageAreConsumed();// Wait certificate is generated
		
		List<Certificate> firstCertificates = certificationProgramService.getCertificates(participant, program);
		Assertions.assertThat(firstCertificates)
			.hasSize(1);

		Certificate firstCertificate = certificatesDao.getLastCertificate(participant, program);
		assertCertificateStatus(firstCertificate, CertificationStatus.VALID, CertificationIdentityStatus.CERTIFIED);
		assertMessage(program, CertificationProgramMailType.certificate_issued);
		
		CreditPointWallet wallet = creditPointService.getOrCreateWallet(participant, system);
		creditPointService.createCreditPointTransaction(CreditPointTransactionType.deposit, new BigDecimal("10"), null, "Give away", wallet, participant, null, null, null, null, null);
		dbInstance.commitAndCloseSession();
		
		// Fake 
		firstCertificate.setNextRecertificationDate(DateUtils.addDays(new Date(), -1));
		firstCertificate = certificatesDao.updateCertificate(firstCertificate);
		dbInstance.commitAndCloseSession();
		
		// Can it be renewed?
		List<Identity> eligiblesIdentities = certificationProgramService.getEligiblesIdentitiesToRecertification(program, new Date());
		Assertions.assertThat(eligiblesIdentities)
			.containsAnyOf(participant);
			
		// Renew the certificate automatically
		boolean automaticOk = certificationCoordinator.processCertificationRequest(participant, program, RequestMode.AUTOMATIC, new Date(), null);
		Assert.assertTrue(automaticOk);
		waitMessageAreConsumed();// Wait certificate is generated
		
		Certificate secondCertificate = certificatesDao.getLastCertificate(participant, program);
		assertCertificateStatus(secondCertificate, CertificationStatus.VALID, CertificationIdentityStatus.CERTIFIED);
		assertMessage(program, CertificationProgramMailType.certificate_renewed);
		
		//Check balance
		assertBalance(participant, system, new BigDecimal("0"));
		
		// Fake 
		secondCertificate = updateCertificate(secondCertificate, DateUtils.addDays(new Date(), -1), program);
		
		boolean automaticNotOk = certificationCoordinator.processCertificationRequest(participant, program, RequestMode.AUTOMATIC, new Date(), null);
		Assert.assertFalse(automaticNotOk);
		
		Certificate noCertificate = certificatesDao.getLastCertificate(participant, program);
		Assert.assertNull(noCertificate);
		
		// Participant passed the course again
		boolean courseAgainOk = certificationCoordinator.processCertificationRequest(participant, program, RequestMode.COURSE, new Date(), participant);
		Assert.assertTrue(courseAgainOk);
		waitMessageAreConsumed();// Wait certificate is generated
		
		List<Certificate> thirdCertificates = certificationProgramService.getCertificates(participant, program);
		Assertions.assertThat(thirdCertificates)
			.hasSize(3);
		Certificate thirdCertificate = certificatesDao.getLastCertificate(participant, program);
		assertCertificateStatus(thirdCertificate, CertificationStatus.VALID, CertificationIdentityStatus.CERTIFIED);
		assertMessage(program, CertificationProgramMailType.certificate_issued);
		
		// Revoke
		certificationCoordinator.revokeRecertification(program, participant, actor);
		dbInstance.commitAndCloseSession();
		Certificate revokedCertificate = certificatesDao.getLastCertificate(participant, program);
		Assert.assertNull(revokedCertificate);
		revokedCertificate = certificatesDao.getCertificateById(thirdCertificate.getKey());
		assertCertificateStatus(revokedCertificate, CertificationStatus.REVOKED, CertificationIdentityStatus.REMOVED);
		assertMessage(program, CertificationProgramMailType.certificate_revoked);
	}
	
	/**
	 * Use case 5 @see https://track.frentix.com/issue/OO-9065<br>
	 * 
	 * Validity: ON<br>
	 * Recertification: ON (automatic)<br>
	 * Recertification timeframe: YES<br>
	 * Credit points: ON - 10 CP
	 */
	@Test
	public void processUseCase6() {
		Identity actor = JunitTestHelper.getDefaultActor();
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("usecase-6-participant-4", Locale.ENGLISH);
		CreditPointSystem system = creditPointService.createCreditPointSystem("Unit test coins", "UT1", null, null, false, false);
		CertificationProgram program = certificationProgramService.createCertificationProgram("usecase-6-to-curriculum-4", "UC5", null);
		program.setValidityEnabled(true);
		program.setValidityTimelapse(7);
		program.setValidityTimelapseUnit(DurationType.day);
		program.setRecertificationEnabled(true);
		program.setRecertificationWindowEnabled(true);
		program.setRecertificationWindow(7);
		program.setRecertificationWindowUnit(DurationType.day);
		program.setRecertificationMode(RecertificationMode.automatic);
		program.setCreditPoints(new BigDecimal("10"));
		program.setCreditPointSystem(system);
		program = certificationProgramService.updateCertificationProgram(program);
		dbInstance.commit();
		
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-use-case-6", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-1", "1. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addMember(element, participant, CurriculumRoles.participant, actor);
		certificationProgramService.addCurriculumElementToCertificationProgram(program, element);
		dbInstance.commit();
		
		boolean courseOk = certificationCoordinator.processCertificationRequest(participant, program, RequestMode.COURSE, new Date(), participant);
		Assert.assertTrue(courseOk);
		waitMessageAreConsumed();// Wait certificate is generated
		
		List<Certificate> firstCertificates = certificationProgramService.getCertificates(participant, program);
		Assertions.assertThat(firstCertificates)
			.hasSize(1);

		Certificate firstCertificate = certificatesDao.getLastCertificate(participant, program);
		assertCertificateStatus(firstCertificate, CertificationStatus.VALID, CertificationIdentityStatus.CERTIFIED);
		assertMessage(program, CertificationProgramMailType.certificate_issued);
		
		CreditPointWallet wallet = creditPointService.getOrCreateWallet(participant, system);
		creditPointService.createCreditPointTransaction(CreditPointTransactionType.deposit, new BigDecimal("10"), null, "Give away", wallet, participant, null, null, null, null, null);
		dbInstance.commitAndCloseSession();
		
		// Fake 
		firstCertificate.setNextRecertificationDate(DateUtils.addDays(new Date(), -1));
		firstCertificate = certificatesDao.updateCertificate(firstCertificate);
		dbInstance.commitAndCloseSession();
		
		// Can it be renewed?
		List<Identity> eligiblesIdentities = certificationProgramService.getEligiblesIdentitiesToRecertification(program, new Date());
		Assertions.assertThat(eligiblesIdentities)
			.containsAnyOf(participant);
			
		// Renew the certificate automatically
		boolean automaticOk = certificationCoordinator.processCertificationRequest(participant, program, RequestMode.AUTOMATIC, new Date(), null);
		Assert.assertTrue(automaticOk);
		waitMessageAreConsumed();// Wait certificate is generated
		
		Certificate secondCertificate = certificatesDao.getLastCertificate(participant, program);
		assertCertificateStatus(secondCertificate, CertificationStatus.VALID, CertificationIdentityStatus.CERTIFIED);
		assertMessage(program, CertificationProgramMailType.certificate_renewed);
		
		//Check balance
		assertBalance(participant, system, new BigDecimal("0"));
		
		// Fake 
		secondCertificate = updateCertificate(secondCertificate, DateUtils.addDays(new Date(), -1), program);
		
		boolean automaticNotOk = certificationCoordinator.processCertificationRequest(participant, program, RequestMode.AUTOMATIC, new Date(), null);
		Assert.assertFalse(automaticNotOk);
		
		Certificate lastValidCertificate = certificatesDao.getLastCertificate(participant, program);
		Assert.assertNotNull(lastValidCertificate);
		Assert.assertEquals(secondCertificate, lastValidCertificate);
		
		// pass the recertification time frame
		lastValidCertificate = updateCertificate(lastValidCertificate, DateUtils.addDays(new Date(), -10), program);
			
		boolean automaticNotAgainOk = certificationCoordinator.processCertificationRequest(participant, program, RequestMode.AUTOMATIC, new Date(), null);
		Assert.assertFalse(automaticNotAgainOk);
		
		Certificate expiredCertificate = certificatesDao.getLastCertificate(participant, program);
		Assert.assertNotNull(expiredCertificate);
		assertCertificateStatus(expiredCertificate, CertificationStatus.EXPIRED, CertificationIdentityStatus.REMOVED);
		
		boolean courseAgainOk = certificationCoordinator.processCertificationRequest(participant, program, RequestMode.COURSE, new Date(), participant);
		Assert.assertTrue(courseAgainOk);
		waitMessageAreConsumed();// Wait certificate is generated
	}
	

	/**
	 * Use case 7 @see https://track.frentix.com/issue/OO-9065<br>
	 * 
	 * Validity: ON<br>
	 * Recertification: ON (automatic)<br>
	 * Recertification timeframe: YES<br>
	 * Credit points: ON - 10 CP
	 */
	@Test
	public void processUseCase7() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("usecase-5-participant-4", Locale.ENGLISH);
		CreditPointSystem system = creditPointService.createCreditPointSystem("Unit test coins", "UT1", null, null, false, false);
		CertificationProgram program = certificationProgramService.createCertificationProgram("usecase-5-to-curriculum-4", "UC7", null);
		program.setValidityEnabled(true);
		program.setValidityTimelapse(7);
		program.setValidityTimelapseUnit(DurationType.day);
		program.setRecertificationEnabled(true);
		program.setRecertificationWindowEnabled(true);
		program.setRecertificationWindow(7);
		program.setRecertificationWindowUnit(DurationType.day);
		program.setRecertificationMode(RecertificationMode.automatic);
		program.setCreditPoints(new BigDecimal("10"));
		program.setCreditPointSystem(system);
		program = certificationProgramService.updateCertificationProgram(program);
		dbInstance.commit();
		
		boolean courseOk = certificationCoordinator.processCertificationRequest(participant, program, RequestMode.COURSE, new Date(), participant);
		Assert.assertTrue(courseOk);
		waitMessageAreConsumed();// Wait certificate is generated
		
		List<Certificate> firstCertificates = certificationProgramService.getCertificates(participant, program);
		Assertions.assertThat(firstCertificates)
			.hasSize(1);
		Certificate firstCertificate = certificatesDao.getLastCertificate(participant, program);
		assertCertificateStatus(firstCertificate, CertificationStatus.VALID, CertificationIdentityStatus.CERTIFIED);
		assertMessage(program, CertificationProgramMailType.certificate_issued);
		
		CreditPointWallet wallet = creditPointService.getOrCreateWallet(participant, system);
		creditPointService.createCreditPointTransaction(CreditPointTransactionType.deposit, new BigDecimal("10"), null, "Give away", wallet, participant, null, null, null, null, null);
		dbInstance.commitAndCloseSession();
		
		// Fake 
		firstCertificate = updateCertificate(firstCertificate, DateUtils.addDays(new Date(), -1), program);
			
		// Renew the certificate automatically
		boolean automaticOk = certificationCoordinator.processCertificationRequest(participant, program, RequestMode.AUTOMATIC, new Date(), null);
		Assert.assertTrue(automaticOk);
		waitMessageAreConsumed();// Wait certificate is generated
		
		Certificate secondCertificate = certificatesDao.getLastCertificate(participant, program);
		assertCertificateStatus(secondCertificate, CertificationStatus.VALID, CertificationIdentityStatus.CERTIFIED);
		assertMessage(program, CertificationProgramMailType.certificate_renewed);
		
		//Check balance
		assertBalance(participant, system, new BigDecimal("0"));
		
		// Fake 
		secondCertificate = updateCertificate(secondCertificate, DateUtils.addDays(new Date(), -1), program);
		dbInstance.commitAndCloseSession();
		// The certificate is in the recertification window
		assertCertificateStatus(secondCertificate, CertificationStatus.EXPIRED_RENEWABLE, CertificationIdentityStatus.RECERTIFYING);
		
		// Not enough money
		boolean automaticNotOk = certificationCoordinator.processCertificationRequest(participant, program, RequestMode.AUTOMATIC, new Date(), null);
		Assert.assertFalse(automaticNotOk);
		Certificate notTouchedLastCertificate = certificatesDao.getLastCertificate(participant, program);
		Assert.assertEquals(secondCertificate, notTouchedLastCertificate);
		
		// Add some credit points
		creditPointService.createCreditPointTransaction(CreditPointTransactionType.deposit, new BigDecimal("25"), null, "Give away", wallet, participant, null, null, null, null, null);
		dbInstance.commitAndCloseSession();
		
		// Enough credit points -> passed
		boolean automaticSecondOk = certificationCoordinator.processCertificationRequest(participant, program, RequestMode.AUTOMATIC, new Date(), null);
		Assert.assertTrue(automaticSecondOk);
		
		List<Certificate> thirdCertificates = certificationProgramService.getCertificates(participant, program);
		Assertions.assertThat(thirdCertificates)
			.hasSize(3);
		Certificate thirdCertificate = certificatesDao.getLastCertificate(participant, program);
		assertCertificateStatus(thirdCertificate, CertificationStatus.VALID, CertificationIdentityStatus.CERTIFIED);
		assertMessage(program, CertificationProgramMailType.certificate_renewed);
	}
	
	private Certificate updateCertificate(Certificate certificate, Date nextCertification, CertificationProgram program) {
		certificate.setNextRecertificationDate(nextCertification);
		if(program.isRecertificationWindowEnabled()) {
			Date endOfWindow = program.getRecertificationWindowUnit().toDate(nextCertification, program.getRecertificationWindow());
			((CertificateImpl)certificate).setRecertificationWindowDate(endOfWindow);
		}
		certificate = certificatesDao.updateCertificate(certificate);
		dbInstance.commitAndCloseSession();
		return certificate;
	}
	
	private void assertBalance(Identity participant, CreditPointSystem system, BigDecimal expectedBalance) {
		CreditPointWallet waller = creditPointService.getOrCreateWallet(participant, system);
		Assert.assertTrue(waller.getBalance().compareTo(expectedBalance) == 0);
	}
	
	private static void assertCertificateStatus(Certificate certificate, CertificationStatus expectedStatus, CertificationIdentityStatus expectedIdentityStatus) {
		Date now = new Date();
		CertificationStatus thirdStatus = CertificationStatus.evaluate(certificate, now);
		Assert.assertEquals(expectedStatus, thirdStatus);
		CertificationIdentityStatus thirdIdentityStatus = CertificationIdentityStatus.evaluate(certificate, now);
		Assert.assertEquals(expectedIdentityStatus, thirdIdentityStatus);
	}
	
	private void assertMessage(CertificationProgram program, CertificationProgramMailType type) {
		List<SmtpMessage> messages = getSmtpServer().getReceivedEmails();
		
		Assert.assertNotNull(messages);
		Assert.assertTrue(messages.size() >= 1);
		
		SmtpMessage lastMessage = messages.get(messages.size() - 1);
		String subject = lastMessage.getHeaderValue("Subject");
		Assert.assertNotNull(subject);
		Assert.assertTrue(subject.contains(program.getDisplayName()));
		log.info("Mail from certification program: {}", subject);
		if(type == CertificationProgramMailType.certificate_issued) {
			Assert.assertTrue(subject.contains("You have received a certification"));
		} else if(type == CertificationProgramMailType.certificate_renewed) {
			Assert.assertTrue(subject.contains("Your certificate has been renewed"));
		} else if(type == CertificationProgramMailType.certificate_expired) {
			Assert.assertTrue(subject.contains("Your certificate has expired"));
		} else if(type == CertificationProgramMailType.certificate_revoked) {
			Assert.assertTrue(subject.contains("Your certificate has been revoked"));
		} else if(type == CertificationProgramMailType.program_removed) {
			Assert.assertTrue(subject.contains("You have been removed from the program"));
		} else {
			Assert.assertNull(type);
		}
	}
}
