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
import org.olat.core.id.Organisation;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramToOrganisation;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 août 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramToOrganisationDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private CertificationProgramDAO certificationProgramDao;
	@Autowired
	private CertificationProgramToOrganisationDAO certificationProgramToOrganisationDao;

	private static Organisation defaultUnitTestOrganisation;
	
	@Before
	public void initDefaultUnitTestOrganisation() {
		if(defaultUnitTestOrganisation == null) {
			defaultUnitTestOrganisation = organisationService
					.createOrganisation("Certification-service-unit-test", "Certification-service-unit-test", "", null, null, JunitTestHelper.getDefaultActor());
		}
	}
	
	@Test
	public void createRelation() {
		CertificationProgram program = certificationProgramDao.createCertificationProgram("Program relation 1", "OpenOlat certification relation 1");
		CertificationProgramToOrganisation relation = certificationProgramToOrganisationDao.createRelation(program, defaultUnitTestOrganisation);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(relation);
		Assert.assertNotNull(relation.getKey());
		Assert.assertNotNull(relation.getCreationDate());
		Assert.assertEquals(program, relation.getCertificationProgram());
		Assert.assertEquals(defaultUnitTestOrganisation, relation.getOrganisation());
		
		CertificationProgram reloadedProgram = certificationProgramDao.loadCertificationProgram(program.getKey());
		Assert.assertNotNull(reloadedProgram);
		Assert.assertEquals(program, reloadedProgram);
		Assert.assertEquals(1, reloadedProgram.getOrganisations().size());
		Assert.assertEquals(relation, reloadedProgram.getOrganisations().iterator().next());
	}

	
}
