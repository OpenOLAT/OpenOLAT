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

import org.apache.commons.lang3.time.DateUtils;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.manager.CertificatesDAO;
import org.olat.course.certificate.model.CertificateImpl;
import org.olat.modules.certificationprogram.CertificationCoordinator;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramService;
import org.olat.modules.certificationprogram.RecertificationMode;
import org.olat.modules.certificationprogram.ui.component.DurationType;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.CreditPointTransactionType;
import org.olat.modules.creditpoint.CreditPointWallet;
import org.olat.modules.creditpoint.manager.CreditPointServiceImpl;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationCoordinatorTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CertificatesDAO certificatesDao;
	@Autowired
	private CreditPointServiceImpl creditPointService;
	@Autowired
	private CertificationCoordinator certificationCoordinator;
	@Autowired
	private CertificationProgramService certificationProgramService;
	
	@Test
	public void processCertificateOfSimpleProgram() {
		Identity actor = JunitTestHelper.getDefaultActor();
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("prog-participant-4");
		CertificationProgram program = certificationProgramService.createCertificationProgram("program-to-curriculum-4", "Program to curriculum", null);
		dbInstance.commitAndCloseSession();

		boolean allOk = certificationCoordinator.processCertificationDemand(participant, program, new Date(), actor);
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
		Identity actor = JunitTestHelper.getDefaultActor();
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("prog-participant-4");
		CertificationProgram program = certificationProgramService.createCertificationProgram("program-to-curriculum-4", "Program to curriculum", null);
		CreditPointSystem system = creditPointService.createCreditPointSystem("Unit test coins", "UT1", null, null);
		program.setCreditPoints(new BigDecimal("20"));
		program.setCreditPointSystem(system);
		certificationProgramService.updateCertificationProgram(program);
		dbInstance.commit();
		
		CreditPointWallet wallet = creditPointService.getOrCreateWallet(participant, system);
		creditPointService.createCreditPointTransaction(CreditPointTransactionType.deposit, new BigDecimal("30"), null, "Give away", wallet, participant, null, null, null, null, null);
		dbInstance.commitAndCloseSession();
		
		boolean allOk = certificationCoordinator.processCertificationDemand(participant, program, new Date(), actor);
		dbInstance.commitAndCloseSession();
		Assert.assertTrue(allOk);

		List<Certificate> certificates = certificationProgramService.getCertificates(participant, program);
		Assertions.assertThat(certificates)
			.hasSize(1);
		
		CreditPointWallet unchangedWallet = creditPointService.getOrCreateWallet(participant, system);
		Assert.assertTrue(unchangedWallet.getBalance().compareTo(new BigDecimal("30")) == 0);
	}
	
	/**
	 * Certificate program costs 20 credits, participant has only 15. It cannot become
	 * the certificate.
	 */
	@Test
	public void processCertificateOfProgramWithNotEnoughCreditPoints() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("prog-participant-4");
		CertificationProgram program = certificationProgramService.createCertificationProgram("program-to-curriculum-4", "Program to curriculum", null);
		CreditPointSystem system = creditPointService.createCreditPointSystem("Unit test coins", "UT1", null, null);
		program.setCreditPoints(new BigDecimal("20"));
		program.setCreditPointSystem(system);
		program.setPrematureRecertificationByUserEnabled(true);
		program.setValidityEnabled(true);
		program.setValidityTimelapse(30);
		program.setValidityTimelapseUnit(DurationType.day);
		program.setRecertificationMode(RecertificationMode.automatic);
		program.setRecertificationWindowEnabled(true);
		program.setRecertificationWindow(30);
		program.setRecertificationWindowUnit(DurationType.day);
		certificationProgramService.updateCertificationProgram(program);
		dbInstance.commit();
		
		boolean coursePaidOk = certificationCoordinator.processCertificationDemand(participant, program, new Date(), null);
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
		
		boolean allOk = certificationCoordinator.processCertificationDemand(participant, program, new Date(), null);
		Assert.assertFalse(allOk);
		
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
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("prog-participant-4");
		CertificationProgram program = certificationProgramService.createCertificationProgram("program-to-curriculum-5", "Program to curriculum", null);
		CreditPointSystem system = creditPointService.createCreditPointSystem("Unit test coins", "UT5", null, null);
		program.setCreditPoints(new BigDecimal("20"));
		program.setCreditPointSystem(system);
		program.setPrematureRecertificationByUserEnabled(true);
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
		
		boolean allOk = certificationCoordinator.processCertificationDemand(participant, program, new Date(), null);
		Assert.assertTrue(allOk);
		
		List<Certificate> certificates = certificationProgramService.getCertificates(participant, program);
		Assertions.assertThat(certificates)
			.hasSize(1);
		
		Certificate currentCertificate = certificatesDao.getLastCertificate(participant, program);
		currentCertificate.setNextRecertificationDate(DateUtils.addDays(new Date(), -25));
		((CertificateImpl)currentCertificate).setRecertificationWindowDate(DateUtils.addDays(new Date(), 5));
		currentCertificate = certificatesDao.updateCertificate(currentCertificate);
		dbInstance.commitAndCloseSession();
		
		boolean recertOk = certificationCoordinator.processCertificationDemand(participant, program, new Date(), null);
		Assert.assertTrue(recertOk);
		
		List<Certificate> recertificates = certificationProgramService.getCertificates(participant, program);
		Assertions.assertThat(recertificates)
			.hasSize(2);
		
		Certificate reCertificate = certificatesDao.getLastCertificate(participant, program);
		Assert.assertNotEquals(currentCertificate, reCertificate);
	}
	
	/**
	 * Negative test of the recertification window 
	 */
	@Test
	public void processRecertificationNotAllowed() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("prog-participant-6");
		CertificationProgram program = certificationProgramService.createCertificationProgram("program-to-curriculum-6", "Program to curriculum", null);
		CreditPointSystem system = creditPointService.createCreditPointSystem("Unit test coins", "UT6", null, null);
		program.setCreditPoints(new BigDecimal("20"));
		program.setCreditPointSystem(system);
		program.setPrematureRecertificationByUserEnabled(true);
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
		
		boolean allOk = certificationCoordinator.processCertificationDemand(participant, program, new Date(), null);
		Assert.assertTrue(allOk);
		
		List<Certificate> certificates = certificationProgramService.getCertificates(participant, program);
		Assertions.assertThat(certificates)
			.hasSize(1);
		
		Certificate currentCertificate = certificatesDao.getLastCertificate(participant, program);
		currentCertificate.setNextRecertificationDate(DateUtils.addDays(new Date(), -65));
		((CertificateImpl)currentCertificate).setRecertificationWindowDate(DateUtils.addDays(new Date(), -35));
		currentCertificate = certificatesDao.updateCertificate(currentCertificate);
		dbInstance.commitAndCloseSession();
		
		boolean recertNok = certificationCoordinator.processCertificationDemand(participant, program, new Date(), null);
		Assert.assertFalse(recertNok);
		
		List<Certificate> recertificates = certificationProgramService.getCertificates(participant, program);
		Assertions.assertThat(recertificates)
			.hasSize(1);
		
		Certificate afterRecertificationTryCertificate = certificatesDao.getLastCertificate(participant, program);
		Assert.assertEquals(currentCertificate, afterRecertificationTryCertificate);
	}
}
