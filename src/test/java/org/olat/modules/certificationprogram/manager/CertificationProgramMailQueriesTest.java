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

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.util.DateUtils;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.manager.CertificatesDAO;
import org.olat.course.certificate.model.CertificateConfig;
import org.olat.course.certificate.model.CertificateImpl;
import org.olat.course.certificate.model.CertificateInfos;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramMailConfiguration;
import org.olat.modules.certificationprogram.CertificationProgramMailType;
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
 * Initial date: 18 nov. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramMailQueriesTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CertificatesDAO certificatesDao;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private CreditPointServiceImpl creditPointService;
	@Autowired
	private CertificationProgramDAO certificationProgramDao;
	@Autowired
	private CertificationProgramLogDAO certificationProgramLogDao;
	@Autowired
	private CertificationProgramMailQueries certificationProgramMailQueries;
	@Autowired
	private CertificationProgramMailConfigurationDAO certificationProgramMailConfigurationDao;
	
	private static Organisation defaultUnitTestOrganisation;
	
	@Before
	public void initDefaultUnitTestOrganisation() {
		if(defaultUnitTestOrganisation == null) {
			defaultUnitTestOrganisation = organisationService
					.createOrganisation("Org-service-unit-test", "Org-service-unit-mail-test", "", null, null, JunitTestHelper.getDefaultActor());
		}
	}
	
	@Test
	public void getExpiredCertificates() {
		Identity identity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-1-1", defaultUnitTestOrganisation, null);
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-1-2", defaultUnitTestOrganisation, null);
		
		CertificationProgram program = certificationProgramDao.createCertificationProgram("PM-1", "Program mailing 1");
		program.setValidityEnabled(true);
		program.setValidityTimelapse(1);
		program.setValidityTimelapseUnit(DurationType.week);
		program = certificationProgramDao.updateCertificationProgram(program);
		
		CertificationProgramMailType type = CertificationProgramMailType.certificate_expired;
		CertificationProgramMailConfiguration configuration = certificationProgramMailConfigurationDao.createConfiguration(program, type);
		dbInstance.commit();
		
		CertificateInfos certificateInfos = new CertificateInfos(identity1, 5.0f, 10.0f, Boolean.TRUE, 0.2, "");
		CertificateConfig config = CertificateConfig.builder().build();
		Certificate expiredCertificate = certificatesManager.generateCertificate(certificateInfos, program, null, config);
		Assert.assertNotNull(expiredCertificate);
		
		CertificateInfos certificate2Infos = new CertificateInfos(identity2, 5.0f, 10.0f, Boolean.TRUE, 0.2, "");
		Certificate certificate2 = certificatesManager.generateCertificate(certificate2Infos, program, null, config);
		Assert.assertNotNull(certificate2);

		Date now = new Date();
		expiredCertificate = updateCertificate(expiredCertificate, DateUtils.addDays(now, -1), program);
		dbInstance.commit();
		
		List<Certificate> certificates = certificationProgramMailQueries.getExpiredCertificates(configuration, now);
		Assertions.assertThat(certificates)
			.hasSizeGreaterThanOrEqualTo(1)
			.containsAnyOf(expiredCertificate)
			.doesNotContain(certificate2);
	}
	
	/**
	 * Check that the expired certificate is not found after notification is sent.
	 */
	@Test
	public void getExpiredCertificatesSentNotifications() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-1-1", defaultUnitTestOrganisation, null);
		
		CertificationProgram program = certificationProgramDao.createCertificationProgram("PM-1", "Program mailing 1");
		program.setValidityEnabled(true);
		program.setValidityTimelapse(1);
		program.setValidityTimelapseUnit(DurationType.week);
		program = certificationProgramDao.updateCertificationProgram(program);
		
		CertificationProgramMailType type = CertificationProgramMailType.certificate_expired;
		CertificationProgramMailConfiguration configuration = certificationProgramMailConfigurationDao.createConfiguration(program, type);
		dbInstance.commit();
		
		CertificateInfos certificateInfos = new CertificateInfos(identity, 5.0f, 10.0f, Boolean.TRUE, 0.2, "");
		CertificateConfig config = CertificateConfig.builder().build();
		Certificate expiredCertificate = certificatesManager.generateCertificate(certificateInfos, program, null, config);
		Assert.assertNotNull(expiredCertificate);
		
		Date now = new Date();
		expiredCertificate = updateCertificate(expiredCertificate, DateUtils.addDays(now, -1), program);
		dbInstance.commit();
		
		List<Certificate> certificates = certificationProgramMailQueries.getExpiredCertificates(configuration, now);
		Assertions.assertThat(certificates)
			.hasSizeGreaterThanOrEqualTo(1)
			.containsAnyOf(expiredCertificate);
		
		certificationProgramLogDao.createMailLog(expiredCertificate, configuration);
		dbInstance.commit();
		
		List<Certificate> certificatesAfter = certificationProgramMailQueries.getExpiredCertificates(configuration, now);
		Assertions.assertThat(certificatesAfter)
			.hasSizeGreaterThanOrEqualTo(0)
			.doesNotContain(expiredCertificate);
	}
	
	@Test
	public void getRemovedCertificates() {
		Identity identity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-2-1", defaultUnitTestOrganisation, null);
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-2-2", defaultUnitTestOrganisation, null);
		
		CertificationProgram program = certificationProgramDao.createCertificationProgram("PM-2", "Program mailing 2");
		program.setValidityEnabled(true);
		program.setValidityTimelapse(1);
		program.setValidityTimelapseUnit(DurationType.week);
		program = certificationProgramDao.updateCertificationProgram(program);
		
		CertificationProgramMailType type = CertificationProgramMailType.certificate_expired;
		CertificationProgramMailConfiguration configuration = certificationProgramMailConfigurationDao.createConfiguration(program, type);
		dbInstance.commit();
		
		CertificateInfos certificateInfos = new CertificateInfos(identity1, 5.0f, 10.0f, Boolean.TRUE, 0.2, "");
		CertificateConfig config = CertificateConfig.builder().build();
		Certificate expiredCertificate = certificatesManager.generateCertificate(certificateInfos, program, null, config);
		Assert.assertNotNull(expiredCertificate);
		
		CertificateInfos certificate2Infos = new CertificateInfos(identity2, 5.0f, 10.0f, Boolean.TRUE, 0.2, "");
		Certificate certificate2 = certificatesManager.generateCertificate(certificate2Infos, program, null, config);
		Assert.assertNotNull(certificate2);

		Date now = new Date();
		expiredCertificate = updateCertificate(expiredCertificate, DateUtils.addDays(now, -1), program);
		dbInstance.commit();
		
		List<Certificate> certificates = certificationProgramMailQueries.getRemovedCertificates(configuration, now);
		Assertions.assertThat(certificates)
			.hasSizeGreaterThanOrEqualTo(1)
			.containsAnyOf(expiredCertificate)
			.doesNotContain(certificate2);
	}
	
	@Test
	public void getRemovedCertificatesWithRecertificationWindow() {
		Identity identity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-3-1", defaultUnitTestOrganisation, null);
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-3-2", defaultUnitTestOrganisation, null);
		Identity identity3 = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-3-3", defaultUnitTestOrganisation, null);
		
		CertificationProgram program = certificationProgramDao.createCertificationProgram("PM-3", "Program mailing 3");
		program.setValidityEnabled(true);
		program.setValidityTimelapse(1);
		program.setValidityTimelapseUnit(DurationType.week);
		program.setRecertificationEnabled(true);
		program.setRecertificationWindowEnabled(true);
		program.setRecertificationWindow(2);
		program.setRecertificationWindowUnit(DurationType.week);
		program = certificationProgramDao.updateCertificationProgram(program);
		
		CertificationProgramMailType type = CertificationProgramMailType.certificate_expired;
		CertificationProgramMailConfiguration configuration = certificationProgramMailConfigurationDao.createConfiguration(program, type);
		dbInstance.commit();
		
		CertificateInfos certificateInfos = new CertificateInfos(identity1, 5.0f, 10.0f, Boolean.TRUE, 0.2, "");
		CertificateConfig config = CertificateConfig.builder().build();
		Certificate expiredCertificate = certificatesManager.generateCertificate(certificateInfos, program, null, config);
		Assert.assertNotNull(expiredCertificate);
		
		CertificateInfos certificate2Infos = new CertificateInfos(identity2, 5.0f, 10.0f, Boolean.TRUE, 0.2, "");
		Certificate certificate2 = certificatesManager.generateCertificate(certificate2Infos, program, null, config);
		Assert.assertNotNull(certificate2);
		
		CertificateInfos certificate3Infos = new CertificateInfos(identity3, 5.0f, 10.0f, Boolean.TRUE, 0.2, "");
		Certificate removedCertificate = certificatesManager.generateCertificate(certificate3Infos, program, null, config);
		Assert.assertNotNull(removedCertificate);

		Date now = new Date();
		expiredCertificate = updateCertificate(expiredCertificate, DateUtils.addDays(now, -3), program);
		removedCertificate = updateCertificate(removedCertificate, DateUtils.addDays(now, -23), program);
		dbInstance.commit();
		
		List<Certificate> certificates = certificationProgramMailQueries.getRemovedCertificates(configuration, now);
		Assertions.assertThat(certificates)
			.hasSizeGreaterThanOrEqualTo(1)
			.containsAnyOf(removedCertificate)
			.doesNotContain(expiredCertificate, certificate2);
	}
	
	@Test
	public void getUpcomingCertificates() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-4-1", defaultUnitTestOrganisation, null);
		
		CertificationProgram program = certificationProgramDao.createCertificationProgram("PM-4", "Program mailing 4");
		program.setValidityEnabled(true);
		program.setValidityTimelapse(1);
		program.setValidityTimelapseUnit(DurationType.week);
		program = certificationProgramDao.updateCertificationProgram(program);
		
		CertificationProgramMailType type = CertificationProgramMailType.reminder_upcoming;
		CertificationProgramMailConfiguration configuration = certificationProgramMailConfigurationDao.createConfiguration(program, type);
		configuration.setTime(7);
		configuration.setTimeUnit(DurationType.day);
		configuration  = certificationProgramMailConfigurationDao.updateConfiguration(configuration);
		dbInstance.commit();
		
		CertificateInfos certificateInfos = new CertificateInfos(identity, 5.0f, 10.0f, Boolean.TRUE, 0.2, "");
		CertificateConfig config = CertificateConfig.builder().build();
		Certificate certificate = certificatesManager.generateCertificate(certificateInfos, program, null, config);
		Assert.assertNotNull(certificate);

		Date now = new Date();
		certificate = updateCertificate(certificate, DateUtils.addDays(now, 3), program);
		dbInstance.commit();
		
		List<Certificate> certificates = certificationProgramMailQueries.getUpcomingCertificates(configuration, now);
		Assertions.assertThat(certificates)
			.hasSizeGreaterThanOrEqualTo(1)
			.containsAnyOf(certificate);
		
		// Log sending an email
		certificationProgramLogDao.createMailLog(certificate, configuration);
		dbInstance.commit();
		
		List<Certificate> certificatesAfterMail = certificationProgramMailQueries.getUpcomingCertificates(configuration, now);
		Assertions.assertThat(certificatesAfterMail)
			.isNotNull()
			.doesNotContain(certificate);
	}
	
	
	@Test
	public void getUpcomingCertificatesWithWindow() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-5-1", defaultUnitTestOrganisation, null);
		
		CertificationProgram program = certificationProgramDao.createCertificationProgram("PM-5", "Program mailing 5");
		program.setValidityEnabled(true);
		program.setValidityTimelapse(1);
		program.setValidityTimelapseUnit(DurationType.week);
		program.setRecertificationEnabled(true);
		program.setRecertificationWindow(1);
		program.setRecertificationWindowUnit(DurationType.day);
		program = certificationProgramDao.updateCertificationProgram(program);
		
		CertificationProgramMailType type = CertificationProgramMailType.reminder_upcoming;
		CertificationProgramMailConfiguration configuration = certificationProgramMailConfigurationDao.createConfiguration(program, type);
		configuration.setTime(2);
		configuration.setTimeUnit(DurationType.day);
		configuration  = certificationProgramMailConfigurationDao.updateConfiguration(configuration);
		dbInstance.commit();
		
		CertificateInfos certificateInfos = new CertificateInfos(identity, 5.0f, 10.0f, Boolean.TRUE, 0.2, "");
		CertificateConfig config = CertificateConfig.builder().build();
		Certificate certificate = certificatesManager.generateCertificate(certificateInfos, program, null, config);
		Assert.assertNotNull(certificate);

		Date now = new Date();
		certificate = updateCertificate(certificate, DateUtils.addDays(now, 3), program);
		dbInstance.commit();
		
		List<Certificate> certificates = certificationProgramMailQueries.getUpcomingCertificates(configuration, now);
		Assertions.assertThat(certificates)
			.doesNotContain(certificate);
		
		certificate = updateCertificate(certificate, DateUtils.addDays(now, -1), program);
		dbInstance.commit();
		
		List<Certificate> remindedCertificates = certificationProgramMailQueries.getUpcomingCertificates(configuration, now);
		Assertions.assertThat(remindedCertificates)
			.hasSizeGreaterThanOrEqualTo(1)
			.containsAnyOf(certificate);
	}
	
	@Test
	public void getUpcomingCertificatesTwoDays() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-5-1", defaultUnitTestOrganisation, null);
		
		CertificationProgram program = certificationProgramDao.createCertificationProgram("PM-5", "Program mailing 5");
		program.setValidityEnabled(true);
		program.setValidityTimelapse(2);
		program.setValidityTimelapseUnit(DurationType.day);
		program.setRecertificationEnabled(false);
		program = certificationProgramDao.updateCertificationProgram(program);
		
		CertificationProgramMailType type = CertificationProgramMailType.reminder_upcoming;
		CertificationProgramMailConfiguration configuration = certificationProgramMailConfigurationDao.createConfiguration(program, type);
		configuration.setTime(2);
		configuration.setTimeUnit(DurationType.day);
		configuration  = certificationProgramMailConfigurationDao.updateConfiguration(configuration);
		dbInstance.commit();
		
		CertificateInfos certificateInfos = new CertificateInfos(identity, 5.0f, 10.0f, Boolean.TRUE, 0.2, "");
		CertificateConfig config = CertificateConfig.builder().build();
		Certificate certificate = certificatesManager.generateCertificate(certificateInfos, program, null, config);
		Assert.assertNotNull(certificate);

		Date now = new Date();
		List<Certificate> certificates = certificationProgramMailQueries.getUpcomingCertificates(configuration, now);
		Assertions.assertThat(certificates)
			.containsExactly(certificate);
	}
	
	@Test
	public void getUpcomingCertificatesTwoDaysValidityThreeDays() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-5-1", defaultUnitTestOrganisation, null);
		
		CertificationProgram program = certificationProgramDao.createCertificationProgram("PM-5", "Program mailing 5");
		program.setValidityEnabled(true);
		program.setValidityTimelapse(3);
		program.setValidityTimelapseUnit(DurationType.day);
		program.setRecertificationEnabled(false);
		program = certificationProgramDao.updateCertificationProgram(program);
		
		CertificationProgramMailType type = CertificationProgramMailType.reminder_upcoming;
		CertificationProgramMailConfiguration configuration = certificationProgramMailConfigurationDao.createConfiguration(program, type);
		configuration.setTime(2);
		configuration.setTimeUnit(DurationType.day);
		configuration  = certificationProgramMailConfigurationDao.updateConfiguration(configuration);
		dbInstance.commit();
		
		CertificateInfos certificateInfos = new CertificateInfos(identity, 5.0f, 10.0f, Boolean.TRUE, 0.2, "");
		CertificateConfig config = CertificateConfig.builder().build();
		Certificate certificate = certificatesManager.generateCertificate(certificateInfos, program, null, config);
		Assert.assertNotNull(certificate);

		Date now = new Date();
		List<Certificate> certificates = certificationProgramMailQueries.getUpcomingCertificates(configuration, now);
		Assertions.assertThat(certificates)
			.doesNotContain(certificate);
	}
	
	
	@Test
	public void getOverdueCertificates() {
		CertificationProgram program = certificationProgramDao.createCertificationProgram("PM-6", "Program mailing 6");
		program.setValidityEnabled(true);
		program.setValidityTimelapse(2);
		program.setValidityTimelapseUnit(DurationType.day);
		program.setRecertificationEnabled(true);
		program.setRecertificationWindowEnabled(true);
		program.setRecertificationWindow(1);
		program.setRecertificationWindowUnit(DurationType.month);
		program = certificationProgramDao.updateCertificationProgram(program);
		
		CertificationProgramMailType type = CertificationProgramMailType.reminder_overdue;
		CertificationProgramMailConfiguration configuration = certificationProgramMailConfigurationDao.createConfiguration(program, type);
		configuration.setTime(7);
		configuration.setTimeUnit(DurationType.day);
		configuration = certificationProgramMailConfigurationDao.updateConfiguration(configuration);

		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-6", defaultUnitTestOrganisation, null);
		dbInstance.commit();
		
		CertificateInfos certificateInfos = new CertificateInfos(identity, 5.0f, 10.0f, Boolean.TRUE, 0.2, "");
		CertificateConfig config = CertificateConfig.builder().build();
		Certificate certificate = certificatesManager.generateCertificate(certificateInfos, program, null, config);
		Assert.assertNotNull(certificate);

		Date now = new Date();
		certificate = updateCertificate(certificate, DateUtils.addDays(now, -3), program);
		dbInstance.commit();
		
		List<Certificate> certificatesNotYet = certificationProgramMailQueries.getOverdueCertificates(configuration, now);
		Assertions.assertThat(certificatesNotYet)
			.isEmpty();
		
		certificate = updateCertificate(certificate, DateUtils.addDays(now, -27), program);
		dbInstance.commit();
		
		List<Certificate> certificates = certificationProgramMailQueries.getOverdueCertificates(configuration, now);
		Assertions.assertThat(certificates)
			.hasSize(1)
			.containsExactly(certificate);
		
		// Log sending an email
		certificationProgramLogDao.createMailLog(certificate, configuration);
		dbInstance.commit();
		
		List<Certificate> certificatesAfterMail = certificationProgramMailQueries.getOverdueCertificates(configuration, now);
		Assertions.assertThat(certificatesAfterMail)
			.isEmpty();
	}
	
	@Test
	public void getOverdueCertificatesTwoDays() {
		CertificationProgram program = certificationProgramDao.createCertificationProgram("PM-6", "Program mailing 6");
		program.setValidityEnabled(true);
		program.setValidityTimelapse(2);
		program.setValidityTimelapseUnit(DurationType.day);
		program.setRecertificationEnabled(true);
		program.setRecertificationWindowEnabled(true);
		program.setRecertificationWindow(2);
		program.setRecertificationWindowUnit(DurationType.day);
		program = certificationProgramDao.updateCertificationProgram(program);
		
		CertificationProgramMailType type = CertificationProgramMailType.reminder_overdue;
		CertificationProgramMailConfiguration configuration = certificationProgramMailConfigurationDao.createConfiguration(program, type);
		configuration.setTime(2);
		configuration.setTimeUnit(DurationType.day);
		configuration = certificationProgramMailConfigurationDao.updateConfiguration(configuration);

		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-6", defaultUnitTestOrganisation, null);
		dbInstance.commit();
		
		CertificateInfos certificateInfos = new CertificateInfos(identity, 5.0f, 10.0f, Boolean.TRUE, 0.2, "");
		CertificateConfig config = CertificateConfig.builder().build();
		Certificate certificate = certificatesManager.generateCertificate(certificateInfos, program, null, config);
		Assert.assertNotNull(certificate);

		Date now = new Date();
		certificate = updateCertificate(certificate, DateUtils.addDays(now, 1), program);
		dbInstance.commit();
		
		List<Certificate> certificatesNotYet = certificationProgramMailQueries.getOverdueCertificates(configuration, now);
		Assertions.assertThat(certificatesNotYet)
			.isEmpty();
		
		certificate = updateCertificate(certificate, now, program);
		dbInstance.commit();
		
		List<Certificate> certificates = certificationProgramMailQueries.getOverdueCertificates(configuration, now);
		Assertions.assertThat(certificates)
			.hasSize(1)
			.containsExactly(certificate);
		
		// Log sending an email
		certificationProgramLogDao.createMailLog(certificate, configuration);
		dbInstance.commit();
		
		List<Certificate> certificatesAfterMail = certificationProgramMailQueries.getOverdueCertificates(configuration, now);
		Assertions.assertThat(certificatesAfterMail)
			.isEmpty();
	}
	
	@Test
	public void getOverdueCertificatesWithCreditPoints() {
		CreditPointSystem system = creditPointService.createCreditPointSystem("Unit test mail coins", "CM7", null, null, false, false);
		
		CertificationProgram program = certificationProgramDao.createCertificationProgram("PM-7", "Program mailing 7");
		program.setValidityEnabled(true);
		program.setValidityTimelapse(1);
		program.setValidityTimelapseUnit(DurationType.week);
		program.setRecertificationEnabled(true);
		program.setRecertificationWindowEnabled(true);
		program.setRecertificationWindow(1);
		program.setRecertificationWindowUnit(DurationType.month);
		program.setCreditPointSystem(system);
		program.setCreditPoints(new BigDecimal("20"));
		program = certificationProgramDao.updateCertificationProgram(program);
		
		CertificationProgramMailType type = CertificationProgramMailType.reminder_overdue;
		CertificationProgramMailConfiguration configuration = certificationProgramMailConfigurationDao.createConfiguration(program, type);
		configuration.setTime(7);
		configuration.setTimeUnit(DurationType.day);
		configuration.setCreditBalanceTooLow(true);
		configuration = certificationProgramMailConfigurationDao.updateConfiguration(configuration);

		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-7", defaultUnitTestOrganisation, null);
		dbInstance.commit();
		
		CreditPointWallet wallet = creditPointService.getOrCreateWallet(identity, system);
		creditPointService.createCreditPointTransaction(CreditPointTransactionType.deposit, new BigDecimal("30"), null, "Give away", wallet, identity, null, null, null, null, null);
		
		CertificateInfos certificateInfos = new CertificateInfos(identity, 5.0f, 10.0f, Boolean.TRUE, 0.2, "");
		CertificateConfig config = CertificateConfig.builder().build();
		Certificate certificate = certificatesManager.generateCertificate(certificateInfos, program, null, config);
		Assert.assertNotNull(certificate);

		Date now = new Date();
		certificate = updateCertificate(certificate, DateUtils.addDays(now, -27), program);
		dbInstance.commit();
		
		// Enough credit
		List<Certificate> certificatesEnoughMoney = certificationProgramMailQueries.getOverdueCertificates(configuration, now);
		Assertions.assertThat(certificatesEnoughMoney)
			.isEmpty();

		creditPointService.createCreditPointTransaction(CreditPointTransactionType.removal, new BigDecimal("-20"), null, "Get away", wallet, identity, null, null, null, null, null);
		dbInstance.commit();
		
		List<Certificate> certificates = certificationProgramMailQueries.getOverdueCertificates(configuration, now);
		Assertions.assertThat(certificates)
			.hasSize(1)
			.containsExactly(certificate);
		
		// Log sending an email
		certificationProgramLogDao.createMailLog(certificate, configuration);
		dbInstance.commit();
		
		List<Certificate> certificatesAfterMail = certificationProgramMailQueries.getOverdueCertificates(configuration, now);
		Assertions.assertThat(certificatesAfterMail)
			.isEmpty();
	}
	
	private Certificate updateCertificate(Certificate certificate, Date nextCertification, CertificationProgram program) {
		certificate.setNextRecertificationDate(CalendarUtils.endOfDay(nextCertification));
		if(program.isRecertificationWindowEnabled()) {
			Date endOfWindow = program.getRecertificationWindowUnit().toDate(nextCertification, program.getRecertificationWindow());
			((CertificateImpl)certificate).setRecertificationWindowDate(CalendarUtils.endOfDay(endOfWindow));
		}
		certificate = certificatesDao.updateCertificate(certificate);
		dbInstance.commitAndCloseSession();
		return certificate;
	}
}
