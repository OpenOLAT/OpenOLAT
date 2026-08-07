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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificateStatus;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.EmailStatus;
import org.olat.course.certificate.model.AbstractCertificate;
import org.olat.course.certificate.model.CertificateConfig;
import org.olat.course.certificate.model.CertificateImpl;
import org.olat.course.certificate.model.CertificateInfos;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.manager.CertificationProgramDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificatesDAOTest extends OlatTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(CertificatesDAOTest.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CertificatesDAO certificatesDao;
	@Autowired
	private CertificationProgramDAO certificationProgramDao;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	private static Organisation defaultUnitTestOrganisation;
	
	@Before
	public void initDefaultUnitTestOrganisation() {
		if(defaultUnitTestOrganisation == null) {
			defaultUnitTestOrganisation = organisationService
					.createOrganisation("Org-service-unit-test", "Org-service-unit-test", "", null, null, JunitTestHelper.getDefaultActor());
		}
	}
	
	@Test
	public void getCertificatesByCertificationProgram() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-user-program-1", defaultUnitTestOrganisation, null);
		CertificationProgram program = certificationProgramDao.createCertificationProgram("cer-program-1", "Program");
		dbInstance.commitAndCloseSession();
		
		CertificateInfos certificateInfos = new CertificateInfos(identity, null, null, null, null, "", null);
		CertificateConfig config = CertificateConfig.builder().build();
		Certificate certificate = certificatesManager.generateCertificate(certificateInfos, program, null, config);
		Assert.assertNotNull(certificate);
		dbInstance.commitAndCloseSession();
		
		List<Certificate> certificates = certificatesDao.getCertificates(identity, program);
		Assertions.assertThat(certificates)
			.hasSize(1)
			.containsExactly(certificate);
	}
	
	@Test
	public void getLastCertificate() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-user-program-2", defaultUnitTestOrganisation, null);
		CertificationProgram program = certificationProgramDao.createCertificationProgram("cer-program-2", "Program");
		dbInstance.commitAndCloseSession();
		
		CertificateInfos certificateInfos = new CertificateInfos(identity, null, null, null, null, "", null);
		CertificateConfig config = CertificateConfig.builder().build();
		Certificate certificate = certificatesManager.generateCertificate(certificateInfos, program, null, config);
		Assert.assertNotNull(certificate);
		dbInstance.commitAndCloseSession();
		
		Certificate lastCertificate = certificatesDao.getLastCertificate(identity, program);
		Assert.assertNotNull(lastCertificate);
		Assert.assertEquals(certificate, lastCertificate);
	}
	
	@Test
	public void certificationCount() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-user-program-8", defaultUnitTestOrganisation, null);
		CertificationProgram program = certificationProgramDao.createCertificationProgram("cer-program-8", "Program");
		dbInstance.commitAndCloseSession();
		
		CertificateInfos certificateInfos = new CertificateInfos(identity, null, null, null, null, "", null);
		CertificateConfig config = CertificateConfig.builder().build();
		Certificate certificate = certificatesManager.generateCertificate(certificateInfos, program, null, config);
		Assert.assertNotNull(certificate);
		dbInstance.commitAndCloseSession();
		
		long count = certificatesDao.certificationCount(identity, program);
		Assert.assertEquals(1l, count);
	}
	
	@Test
	public void getCertificatesToProcess() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-owner-1", defaultUnitTestOrganisation, null);
		CertificationProgram program = certificationProgramDao.createCertificationProgram("cer-program-10", "Program");
		
		CertificateImpl certificate = new CertificateImpl();
		certificate.setCreationDate(new Date());
		certificate.setLastModified(certificate.getCreationDate());
		certificate.setStatus(CertificateStatus.pending);
		certificate.setUuid(UUID.randomUUID().toString());
		certificate.setArchivedResourceKey(Long.valueOf(1));
		certificate.setIdentity(identity);
		certificate.setOlatResource(program.getResource());
		certificate.setCertificationProgram(program);
		dbInstance.getCurrentEntityManager().persist(certificate);
		
		dbInstance.commitAndCloseSession();
		
		List<Long> pendingCertificates = certificatesDao.getPendingCertificates();
		Assertions.assertThat(pendingCertificates)
			.hasSizeGreaterThanOrEqualTo(1)
			.contains(certificate.getKey());
	}
	
	@Test
	public void getBrokenCertificates() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-user-program-8", defaultUnitTestOrganisation, null);
		CertificationProgram program = certificationProgramDao.createCertificationProgram("cer-program-8", "Program");
		dbInstance.commitAndCloseSession();
		
		CertificateInfos certificateInfos = new CertificateInfos(identity, null, null, null, null, "", null);
		CertificateConfig config = CertificateConfig.builder()
				.withSendEmail(true)
				.build();
		Certificate certificate = certificatesManager.generateCertificate(certificateInfos, program, null, config);
		Assert.assertNotNull(certificate);
		dbInstance.commitAndCloseSession();
		
		triggerAndWaitCertificate(certificate.getKey());
		
		// Reload the certificate
		certificate = certificatesManager.getCertificateById(certificate.getKey());
		
		VFSItem item = vfsRepositoryService.getItemFor(certificate.getMetadata());
		if(item instanceof VFSLeaf leaf) {
			try(InputStream in = new ByteArrayInputStream(new byte[0])) {
				VFSManager.copyContent(in, leaf, identity);	
			} catch(IOException e) {
				log.error("", e);
			}
		}
		
		// Load all broken certificates
		List<Certificate> certificates = certificatesDao.getBrokenCertificates(null, null);
		Assertions.assertThat(certificates)
			.hasSizeGreaterThanOrEqualTo(1)
			.contains(certificate);
		
		LocalDateTime start = LocalDateTime.now().minusDays(1);
		LocalDateTime end = LocalDateTime.now().plusDays(1);
		List<Certificate> certificatesByDates = certificatesDao.getBrokenCertificates(start, end);
		Assertions.assertThat(certificatesByDates)
			.hasSizeGreaterThanOrEqualTo(1)
			.contains(certificate);
	}
	
	@Test
	public void removeLastFlagByCertificationProgram() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-user-program-3", defaultUnitTestOrganisation, null);
		CertificationProgram program = certificationProgramDao.createCertificationProgram("cer-program-3", "Program");
		dbInstance.commitAndCloseSession();
		
		CertificateInfos certificateInfos = new CertificateInfos(identity, null, null, null, null, "", null);
		CertificateConfig config = CertificateConfig.builder().build();
		Certificate certificate = certificatesManager.generateCertificate(certificateInfos, program, null, config);
		Assert.assertNotNull(certificate);
		dbInstance.commitAndCloseSession();
		
		// Has a last certificate
		Certificate lastCertificate = certificatesDao.getLastCertificate(identity, program);
		Assert.assertNotNull(lastCertificate);
		
		certificatesDao.removeLastFlag(identity, program);
		dbInstance.commitAndCloseSession();
		
		// Hasn't a last certificate
		Certificate noLastCertificate = certificatesDao.getLastCertificate(identity, program);
		Assert.assertNull(noLastCertificate);

		// Check the flags
		Certificate reloadCertificate = certificatesDao.getCertificateById(certificate.getKey());
		Assert.assertNotNull(reloadCertificate);
		Assert.assertFalse(reloadCertificate.isLast());
		Assert.assertNotEquals(CertificateStatus.archived, reloadCertificate.getStatus());
		Assert.assertNotEquals(CertificateStatus.revoked, reloadCertificate.getStatus());
	}
	
	@Test
	public void removeLastFlagByResourceKey() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-user-program-4", defaultUnitTestOrganisation, null);
		CertificationProgram program = certificationProgramDao.createCertificationProgram("cer-program-4", "Program");
		dbInstance.commitAndCloseSession();
		
		CertificateInfos certificateInfos = new CertificateInfos(identity, null, null, null, null, "", null);
		CertificateConfig config = CertificateConfig.builder().build();
		Certificate certificate = certificatesManager.generateCertificate(certificateInfos, program, null, config);
		Assert.assertNotNull(certificate);
		dbInstance.commitAndCloseSession();
		
		// Has a last certificate
		Certificate lastCertificate = certificatesDao.getLastCertificate(identity, program);
		Assert.assertNotNull(lastCertificate);
		
		certificatesDao.removeLastFlag(identity, program.getResource().getKey());
		dbInstance.commitAndCloseSession();
		
		// Has a last certificate
		Certificate noLastCertificate = certificatesDao.getLastCertificate(identity, program);
		Assert.assertNull(noLastCertificate);
	}
	
	@Test
	public void removeLastFlagByResourceKeyButNotLast() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-user-program-6", defaultUnitTestOrganisation, null);
		CertificationProgram program = certificationProgramDao.createCertificationProgram("cer-program-6", "Program");
		dbInstance.commitAndCloseSession();
		
		CertificateInfos certificateInfos = new CertificateInfos(identity, null, null, null, null, "", null);
		CertificateConfig config = CertificateConfig.builder()
				.withSendEmail(true)
				.build();
		Certificate certificate1 = certificatesManager.generateCertificate(certificateInfos, program, null, config);
		Assert.assertNotNull(certificate1);
		dbInstance.commitAndCloseSession();
		triggerAndWaitCertificate(certificate1.getKey());
		
		Certificate certificate2 = certificatesManager.generateCertificate(certificateInfos, program, null, config);
		Assert.assertNotNull(certificate2);
		dbInstance.commitAndCloseSession();
		triggerAndWaitCertificate(certificate2.getKey());
		
		// Has a last certificate
		Certificate lastCertificate = certificatesDao.getLastCertificate(identity, program);
		Assert.assertNotNull(lastCertificate);
		Assert.assertEquals(certificate2, lastCertificate);
		
		certificatesDao.removeLastFlag(identity, program.getResource().getKey(), lastCertificate);
		dbInstance.commitAndCloseSession();
		
		// Has a last certificate
		Certificate stillLastCertificate = certificatesDao.getLastCertificate(identity, program);
		Assert.assertNotNull(stillLastCertificate);
		Assert.assertEquals(certificate2, stillLastCertificate);
	}
	
	/**
	 * Wait that the certificate is generated, email sent, and flag last set.
	 * 
	 * @param certificateKey The primary key of the certificate
	 */
	private void triggerAndWaitCertificate(Long certificateKey) {
		certificatesManager.triggerGenerationJob();
		//wait until the certificate is created
		waitForCondition(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				Certificate reloadedCertificate = certificatesManager.getCertificateById(certificateKey);
				return CertificateStatus.ok.equals(reloadedCertificate.getStatus())
						&& ((AbstractCertificate)reloadedCertificate).isLast()
						&& EmailStatus.ok.equals(((AbstractCertificate)reloadedCertificate).getEmailStatus());
			}
		}, 30000);
	}
}
