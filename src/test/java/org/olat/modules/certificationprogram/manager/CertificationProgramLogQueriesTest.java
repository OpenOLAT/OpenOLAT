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
import org.olat.modules.certificationprogram.CertificationProgramLogAction;
import org.olat.modules.certificationprogram.ui.component.DurationType;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 janv. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramLogQueriesTest extends OlatTestCase {
	
	private static Organisation defaultUnitTestOrganisation;

	@Autowired
	private DB dbInstance;
	@Autowired
	private CertificatesDAO certificatesDao;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private CertificationProgramDAO certificationProgramDao;
	@Autowired
	private CertificationProgramLogDAO certificationProgramLogDao;
	@Autowired
	private CertificationProgramLogQueries certificationProgramLogQueries;
	
	@Before
	public void initDefaultUnitTestOrganisation() {
		if(defaultUnitTestOrganisation == null) {
			defaultUnitTestOrganisation = organisationService
					.createOrganisation("Org-service-unit-test", "Org-service-unit-mail-test", "", null, null, JunitTestHelper.getDefaultActor());
		}
	}
	
	@Test
	public void getRemovedCertificates() {
		Identity actor = JunitTestHelper.getDefaultActor();
		Identity identity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-log-1", defaultUnitTestOrganisation, null);
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-log-2", defaultUnitTestOrganisation, null);
		Identity identity3 = JunitTestHelper.createAndPersistIdentityAsRndUser("cer-log-3", defaultUnitTestOrganisation, null);
		
		CertificationProgram program = certificationProgramDao.createCertificationProgram("PM-2", "Program mailing 2");
		program.setValidityEnabled(true);
		program.setValidityTimelapse(1);
		program.setValidityTimelapseUnit(DurationType.week);
		program = certificationProgramDao.updateCertificationProgram(program);
		dbInstance.commit();
		
		CertificateInfos certificateInfos = new CertificateInfos(identity1, 5.0f, 10.0f, Boolean.TRUE, 0.2, "", null);
		CertificateConfig config = CertificateConfig.builder().build();
		Certificate expiredCertificate = certificatesManager.generateCertificate(certificateInfos, program, null, config);
		Assert.assertNotNull(expiredCertificate);
		
		CertificateInfos certificate2Infos = new CertificateInfos(identity2, 5.0f, 10.0f, Boolean.TRUE, 0.2, "", null);
		Certificate certificate2 = certificatesManager.generateCertificate(certificate2Infos, program, null, config);
		Assert.assertNotNull(certificate2);
		
		CertificateInfos certificate3Infos = new CertificateInfos(identity3, 5.0f, 10.0f, Boolean.TRUE, 0.2, "", null);
		CertificateConfig config3 = CertificateConfig.builder().build();
		Certificate expiredLoggedCertificate = certificatesManager.generateCertificate(certificate3Infos, program, null, config3);
		
		Date now = new Date();
		expiredCertificate = updateCertificate(expiredCertificate, DateUtils.addDays(now, -1), program);
		certificationProgramLogDao.createLog(expiredLoggedCertificate, program, CertificationProgramLogAction.remove_membership, null, null, null, null, null, null, actor);
		dbInstance.commit();
		
		List<Certificate> certificates = certificationProgramLogQueries.getRemovedCertificates(program, now);
		Assertions.assertThat(certificates)
			.hasSizeGreaterThanOrEqualTo(1)
			.containsAnyOf(expiredCertificate)
			.doesNotContain(expiredLoggedCertificate, certificate2);
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
