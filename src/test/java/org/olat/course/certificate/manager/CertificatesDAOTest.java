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

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.model.CertificateConfig;
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
		
		CertificateInfos certificateInfos = new CertificateInfos(identity, null, null, null, null, "");
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
		
		CertificateInfos certificateInfos = new CertificateInfos(identity, null, null, null, null, "");
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
		
		CertificateInfos certificateInfos = new CertificateInfos(identity, null, null, null, null, "");
		CertificateConfig config = CertificateConfig.builder().build();
		Certificate certificate = certificatesManager.generateCertificate(certificateInfos, program, null, config);
		Assert.assertNotNull(certificate);
		dbInstance.commitAndCloseSession();
		
		long count = certificatesDao.certificationCount(identity, program);
		Assert.assertEquals(1l, count);
	}
	
	@Test
	public void removeLastFlagByCertificationProgram() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-user-program-3", defaultUnitTestOrganisation, null);
		CertificationProgram program = certificationProgramDao.createCertificationProgram("cer-program-3", "Program");
		dbInstance.commitAndCloseSession();
		
		CertificateInfos certificateInfos = new CertificateInfos(identity, null, null, null, null, "");
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
		Assert.assertFalse(reloadCertificate.isRevoked());
	}
	
	@Test
	public void revokeByCertificationProgram() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-user-program-3", defaultUnitTestOrganisation, null);
		CertificationProgram program = certificationProgramDao.createCertificationProgram("cer-program-3", "Program");
		dbInstance.commitAndCloseSession();
		
		CertificateInfos certificateInfos = new CertificateInfos(identity, null, null, null, null, "");
		CertificateConfig config = CertificateConfig.builder().build();
		Certificate certificate = certificatesManager.generateCertificate(certificateInfos, program, null, config);
		Assert.assertNotNull(certificate);
		dbInstance.commitAndCloseSession();
		
		// Has a last certificate
		Certificate lastCertificate = certificatesDao.getLastCertificate(identity, program);
		Assert.assertNotNull(lastCertificate);
		
		certificatesDao.revoke(identity, program);
		dbInstance.commitAndCloseSession();
		
		// Hasn't a last certificate
		Certificate noLastCertificate = certificatesDao.getLastCertificate(identity, program);
		Assert.assertNull(noLastCertificate);
		
		// Check the flags
		Certificate reloadCertificate = certificatesDao.getCertificateById(certificate.getKey());
		Assert.assertNotNull(reloadCertificate);
		Assert.assertFalse(reloadCertificate.isLast());
		Assert.assertTrue(reloadCertificate.isRevoked());
	}
	
	@Test
	public void removeLastFlagByResourceKey() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-user-program-4", defaultUnitTestOrganisation, null);
		CertificationProgram program = certificationProgramDao.createCertificationProgram("cer-program-4", "Program");
		dbInstance.commitAndCloseSession();
		
		CertificateInfos certificateInfos = new CertificateInfos(identity, null, null, null, null, "");
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
}
