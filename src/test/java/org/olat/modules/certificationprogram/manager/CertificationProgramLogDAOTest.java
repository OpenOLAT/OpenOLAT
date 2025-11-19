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
import org.olat.modules.certificationprogram.CertificationProgramMailConfiguration;
import org.olat.modules.certificationprogram.CertificationProgramLog;
import org.olat.modules.certificationprogram.CertificationProgramMailType;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 nov. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramLogDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private CertificationProgramDAO certificationProgramDao;
	@Autowired
	private CertificationProgramLogDAO certificationProgramLogDao;
	@Autowired
	private CertificationProgramMailConfigurationDAO certificationProgramMailConfigurationDao;

	private static Organisation defaultUnitTestOrganisation;
	
	@Before
	public void initDefaultUnitTestOrganisation() {
		if(defaultUnitTestOrganisation == null) {
			defaultUnitTestOrganisation = organisationService
					.createOrganisation("Org-service-unit-test", "Org-service-unit-test", "", null, null, JunitTestHelper.getDefaultActor());
		}
	}

	@Test
	public void createLog() {
		CertificationProgram program = certificationProgramDao.createCertificationProgram("PM-1", "Program mailing 1");
		CertificationProgramMailType type = CertificationProgramMailType.certificate_issued;
		CertificationProgramMailConfiguration configuration = certificationProgramMailConfigurationDao.createConfiguration(program, type);

		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-1", defaultUnitTestOrganisation, null);
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(identity, defaultUnitTestOrganisation);
		dbInstance.commit();
		
		CertificateInfos certificateInfos = new CertificateInfos(identity, 5.0f, 10.0f, Boolean.TRUE, 0.2, "");
		CertificateConfig config = CertificateConfig.builder().build();
		Certificate certificate = certificatesManager.generateCertificate(certificateInfos, entry, null, config);
		Assert.assertNotNull(certificate);
		dbInstance.commitAndCloseSession();
		
		CertificationProgramLog mailLog = certificationProgramLogDao.createMailLog(certificate, configuration);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mailLog);
		Assert.assertNotNull(mailLog.getKey());
		Assert.assertEquals(certificate, mailLog.getCertificate());
		Assert.assertEquals(configuration, mailLog.getMailConfiguration());
	}
}
