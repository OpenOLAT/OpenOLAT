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
import java.net.URL;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.manager.CertificatesDAO;
import org.olat.course.certificate.model.CertificateConfig;
import org.olat.course.certificate.model.CertificateImpl;
import org.olat.course.certificate.model.CertificateInfos;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramStatusEnum;
import org.olat.modules.certificationprogram.CertificationProgramToCurriculumElement;
import org.olat.modules.certificationprogram.CertificationRoles;
import org.olat.modules.certificationprogram.RecertificationMode;
import org.olat.modules.certificationprogram.model.CertificationProgramWithStatistics;
import org.olat.modules.certificationprogram.ui.component.DurationType;
import org.olat.modules.creditpoint.CreditPointService;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.CreditPointTransactionType;
import org.olat.modules.creditpoint.CreditPointWallet;
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
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 août 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private CurriculumDAO curriculumDao;
	@Autowired
	private CertificatesDAO certificatesDao;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private CreditPointService creditPointService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private CurriculumElementDAO curriculumElementDao;
	@Autowired
	private CertificationProgramDAO certificationProgramDao;
	@Autowired
	private CertificationProgramToOrganisationDAO certificationProgramToOrganisationDao;
	@Autowired
	private CertificationProgramToCurriculumElementDAO certificationProgramToCurriculumElementDao;
	
	@Test
	public void createCertificationProgram() {
		String programIdentifier = "Program 1";
		String programName = "OpenOlat certification 1";
		
		CertificationProgram program = certificationProgramDao.createCertificationProgram(programIdentifier, programName);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(program);
		Assert.assertNotNull(program.getKey());
		Assert.assertNotNull(program.getCreationDate());
		Assert.assertEquals(programIdentifier, program.getIdentifier());
		Assert.assertEquals(programName, program.getDisplayName());
	}
	
	@Test
	public void loadCertificationProgram() {
		String programIdentifier = "Program 2";
		String programName = "OpenOlat certification 2";
		
		CertificationProgram program = certificationProgramDao.createCertificationProgram(programIdentifier, programName);
		dbInstance.commitAndCloseSession();
		
		CertificationProgram loadedProgram = certificationProgramDao.loadCertificationProgram(program.getKey());
		
		Assert.assertNotNull(loadedProgram);
		Assert.assertNotNull(loadedProgram.getKey());
		Assert.assertNotNull(loadedProgram.getCreationDate());
		Assert.assertEquals(program, loadedProgram);
		Assert.assertEquals(programIdentifier, loadedProgram.getIdentifier());
		Assert.assertEquals(programName, loadedProgram.getDisplayName());
	}
	
	@Test
	public void loadCertificationPrograms() {
		String programIdentifier = "Program 3";
		String programName = "OpenOlat certification 3";
		
		CertificationProgram program = certificationProgramDao.createCertificationProgram(programIdentifier, programName);
		dbInstance.commitAndCloseSession();
		
		List<CertificationProgram> programs = certificationProgramDao.loadCertificationPrograms();
		Assertions.assertThat(programs)
			.hasSizeGreaterThanOrEqualTo(1)
			.contains(program);
	}
	
	@Test
	public void loadCertificationProgramsByOrganisations() {
		String programIdentifier = "PO - 10";
		String programName = "OpenOlat certification 10 (organisation)";
		
		CertificationProgram program = certificationProgramDao.createCertificationProgram(programIdentifier, programName);
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		certificationProgramToOrganisationDao.createRelation(program, defOrganisation);
		dbInstance.commitAndCloseSession();
		
		List<CertificationProgram> programs = certificationProgramDao.loadCertificationPrograms(List.of(defOrganisation));
		Assertions.assertThat(programs)
			.hasSizeGreaterThanOrEqualTo(1)
			.contains(program);
	}
	
	@Test
	public void isCertificationProgram() {
		Identity actor = JunitTestHelper.getDefaultActor();
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-program-1", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-1", "1. Element of the prgram",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CertificationProgram program = certificationProgramDao.createCertificationProgram("program-to-curriculum-1", "Program to curriculum");
		CertificationProgramToCurriculumElement relation = certificationProgramToCurriculumElementDao.createRelation(program, element);
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(actor);
		curriculumService.addRepositoryEntry(element, entry, true);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(element);
		Assert.assertNotNull(program);
		Assert.assertNotNull(relation);
		Assert.assertNotNull(entry);
		
		boolean certificationEnabled = certificationProgramDao.isCertificationProgram(entry);
		Assert.assertTrue(certificationEnabled);
	}
	
	@Test
	public void getCertificationPrograms() {
		Identity actor = JunitTestHelper.getDefaultActor();
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-program-2", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-2", "2. Element of the program",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CertificationProgram program = certificationProgramDao.createCertificationProgram("program-to-curriculum-2", "Program to curriculum");
		CertificationProgramToCurriculumElement relation = certificationProgramToCurriculumElementDao.createRelation(program, element);
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(actor);
		curriculumService.addRepositoryEntry(element, entry, true);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(element);
		Assert.assertNotNull(program);
		Assert.assertNotNull(relation);
		Assert.assertNotNull(entry);
		
		List<CertificationProgram> programs = certificationProgramDao.getCertificationPrograms(entry);
		Assertions.assertThat(programs)
			.hasSize(1)
			.contains(program);
	}
	
	@Test
	public void getAssessmentEntries() {
		Identity actor = JunitTestHelper.getDefaultActor();
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("prog-participant-1");
		
		URL courseUrl = JunitTestHelper.class.getResource("file_resources/course_with_assessment.zip");
		RepositoryEntry entry = JunitTestHelper.deployCourse(actor, "QTI 2.1 Course", courseUrl);
		dbInstance.commitAndCloseSession();
		
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-program-3", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-3", "3. Element of the program",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CertificationProgram program = certificationProgramDao.createCertificationProgram("program-to-curriculum-3", "Program to curriculum");
		CertificationProgramToCurriculumElement relation = certificationProgramToCurriculumElementDao.createRelation(program, element);
		curriculumService.addRepositoryEntry(element, entry, true);
		curriculumService.addMember(element, participant, CurriculumRoles.participant, actor);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(element);
		Assert.assertNotNull(program);
		Assert.assertNotNull(relation);
		Assert.assertNotNull(entry);
		// Wait event processing
		waitMessageAreConsumed();
		
		List<AssessmentEntry> entries = certificationProgramDao.getAssessmentEntries(program, participant,
				List.of(RepositoryEntryStatusEnum.coachPublishedToClosed()));
		Assertions.assertThat(entries)
			.hasSize(1);
	}
	
	@Test
	public void loadCertificationProgramsWithStatistics() {
		Identity owner = JunitTestHelper.getDefaultActor();
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("prog-participant-4");
		
		URL courseUrl = JunitTestHelper.class.getResource("file_resources/course_with_assessment.zip");
		RepositoryEntry entry = JunitTestHelper.deployCourse(owner, "QTI 2.1 Course", courseUrl);
		dbInstance.commitAndCloseSession();
		
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-program-4", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-4", "4. Element of the program",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CertificationProgram program = certificationProgramDao.createCertificationProgram("program-to-curriculum-4", "Program to curriculum");
		groupDao.addMembershipOneWay(program.getGroup(), owner, CertificationRoles.programowner.name());
		CertificationProgramToCurriculumElement relation = certificationProgramToCurriculumElementDao.createRelation(program, element);
		curriculumService.addRepositoryEntry(element, entry, true);
		curriculumService.addMember(element, participant, CurriculumRoles.participant, owner);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(element);
		Assert.assertNotNull(program);
		Assert.assertNotNull(relation);
		Assert.assertNotNull(entry);
		// Wait event processing
		waitMessageAreConsumed();
		
		List<CertificationProgramWithStatistics> statisticsList = certificationProgramDao.loadCertificationProgramsWithStatistics(owner, new Date());
		Assertions.assertThat(statisticsList)
			.hasSizeGreaterThanOrEqualTo(1)
			.filteredOn(statistics -> program.equals(statistics.certificationProgram()))
			.hasSize(1);
	}
	
	/**
	 * Test different cases at once
	 */
	@Test
	public void getEligibleForRecertificationsWithCreditPoints() {
		Identity actor = JunitTestHelper.getDefaultActor();
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("prog-participant-4");
		Identity outOfProgramParticipant = JunitTestHelper.createAndPersistIdentityAsRndUser("prog-participant-5");
		Identity notParticipant = JunitTestHelper.createAndPersistIdentityAsRndUser("prog-participant-6");
		Identity poorParticipant = JunitTestHelper.createAndPersistIdentityAsRndUser("prog-participant-7");
		
		URL courseUrl = JunitTestHelper.class.getResource("file_resources/course_with_assessment.zip");
		RepositoryEntry entry = JunitTestHelper.deployCourse(actor, "QTI 2.1 Course", courseUrl);
		dbInstance.commitAndCloseSession();
		
		CreditPointSystem system = creditPointService.createCreditPointSystem("Unit test coins", "UT1", null, null, false, false);
		CreditPointWallet participantWallet = creditPointService.getOrCreateWallet(participant, system);
		creditPointService.createCreditPointTransaction(CreditPointTransactionType.deposit, new BigDecimal("30"), null, "Testing", participantWallet, actor, null, null, null, null, null);
		
		CreditPointWallet outOfProgramParticipantWallet = creditPointService.getOrCreateWallet(outOfProgramParticipant, system);
		creditPointService.createCreditPointTransaction(CreditPointTransactionType.deposit, new BigDecimal("30"), null, "Testing", outOfProgramParticipantWallet, actor, null, null, null, null, null);
		
		CreditPointWallet poorParticipantWallet = creditPointService.getOrCreateWallet(poorParticipant, system);
		creditPointService.createCreditPointTransaction(CreditPointTransactionType.deposit, new BigDecimal("5"), null, "Testing", poorParticipantWallet, actor, null, null, null, null, null);
		
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-program-4", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-4", "4. Element of the program",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		
		CertificationProgram program = certificationProgramDao.createCertificationProgram("program-to-curriculum-4", "Program to curriculum");
		program.setRecertificationMode(RecertificationMode.automatic);
		program.setStatus(CertificationProgramStatusEnum.active);
		program.setCreditPoints(new BigDecimal("20"));
		program.setCreditPointSystem(system);
		// Valid for 30 days
		program.setValidityEnabled(true);
		program.setValidityTimelapse(30);
		program.setValidityTimelapseUnit(DurationType.day);
		// Recertification with a 30 days window
		program.setRecertificationEnabled(true);
		program.setRecertificationWindowEnabled(true);
		program.setRecertificationWindow(30);
		program.setRecertificationWindowUnit(DurationType.day);
		program = certificationProgramDao.updateCertificationProgram(program);
		
		CertificationProgramToCurriculumElement relation = certificationProgramToCurriculumElementDao.createRelation(program, element);
		curriculumService.addRepositoryEntry(element, entry, true);
		curriculumService.addMember(element, participant, CurriculumRoles.participant, actor);
		curriculumService.addMember(element, poorParticipant, CurriculumRoles.participant, actor);
		curriculumService.addMember(element, outOfProgramParticipant, CurriculumRoles.participant, actor);
		dbInstance.commit();
		Assert.assertNotNull(relation);

		Date now = new Date();
		generateCertificate(participant, program, now, -5, 15);
		generateCertificate(outOfProgramParticipant, program, now, -60, -30);
		generateCertificate(notParticipant, program, now, -5, 15);
		generateCertificate(poorParticipant, program, now, -5, 15);
		
		dbInstance.commitAndCloseSession();

		
		List<Identity> eligibleIdentities = certificationProgramDao.getEligibleForRecertificationsWithCreditPoints(program, now);
		Assertions.assertThat(eligibleIdentities)
			.hasSize(1)
			.containsExactlyInAnyOrder(participant)
			.doesNotContain(outOfProgramParticipant, notParticipant, poorParticipant);
	}
	

	/**
	 * Test different cases at once in case there is not payment involved.
	 */
	@Test
	public void getEligibleForRecertifications() {
		Identity actor = JunitTestHelper.getDefaultActor();
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("prog-participant-8");
		Identity outOfProgramParticipant = JunitTestHelper.createAndPersistIdentityAsRndUser("prog-participant-9");
		Identity notParticipant = JunitTestHelper.createAndPersistIdentityAsRndUser("prog-participant-10");
		
		URL courseUrl = JunitTestHelper.class.getResource("file_resources/course_with_assessment.zip");
		RepositoryEntry entry = JunitTestHelper.deployCourse(actor, "QTI 2.1 Course", courseUrl);
		dbInstance.commitAndCloseSession();
		
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-program-5", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-5", "5. Element of the program",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		
		CertificationProgram program = certificationProgramDao.createCertificationProgram("program-to-curriculum-5", "Program to curriculum");
		program.setRecertificationMode(RecertificationMode.automatic);
		program.setStatus(CertificationProgramStatusEnum.active);
		// Valid for 30 days
		program.setValidityEnabled(true);
		program.setValidityTimelapse(30);
		program.setValidityTimelapseUnit(DurationType.day);
		// Recertification with a 30 days window
		program.setRecertificationEnabled(true);
		program.setRecertificationWindowEnabled(true);
		program.setRecertificationWindow(30);
		program.setRecertificationWindowUnit(DurationType.day);
		program = certificationProgramDao.updateCertificationProgram(program);
		
		CertificationProgramToCurriculumElement relation = certificationProgramToCurriculumElementDao.createRelation(program, element);
		curriculumService.addRepositoryEntry(element, entry, true);
		curriculumService.addMember(element, participant, CurriculumRoles.participant, actor);
		curriculumService.addMember(element, outOfProgramParticipant, CurriculumRoles.participant, actor);
		dbInstance.commit();
		Assert.assertNotNull(relation);

		Date now = new Date();
		generateCertificate(participant, program, now, -5, 15);
		generateCertificate(outOfProgramParticipant, program, now, -60, -30);
		generateCertificate(notParticipant, program, now, -5, 15);
		
		List<Identity> eligibleIdentities = certificationProgramDao.getEligibleForRecertifications(program, now);
		Assertions.assertThat(eligibleIdentities)
			.hasSize(1)
			.containsExactlyInAnyOrder(participant)
			.doesNotContain(outOfProgramParticipant, notParticipant);
	}
	
	private Certificate generateCertificate(Identity participant, CertificationProgram program, Date now, int nextRecertification, int window) {
		CertificateConfig config = CertificateConfig.builder().build();
		CertificateInfos certificateInfos = new CertificateInfos(participant, null, null, null, null, "");
		Certificate certificate = certificatesManager.generateCertificate(certificateInfos, program, null, config);
		waitMessageAreConsumed();
		certificate.setNextRecertificationDate(DateUtils.addDays(now, nextRecertification));
		((CertificateImpl)certificate).setRecertificationWindowDate(DateUtils.addDays(now, window));
		certificate = certificatesDao.updateCertificate(certificate);
		return certificate;
	}
}
