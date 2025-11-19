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

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramMailConfiguration;
import org.olat.modules.certificationprogram.CertificationProgramMailType;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 nov. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramMailConfigurationDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CertificationProgramDAO certificationProgramDao;
	@Autowired
	private CertificationProgramMailConfigurationDAO certificationProgramMailConfigurationDao;
	
	
	@Test
	public void createConfiguration() {
		CertificationProgram program = certificationProgramDao.createCertificationProgram("PM-1", "Program mailing 1");
		CertificationProgramMailType type = CertificationProgramMailType.certificate_issued;
		CertificationProgramMailConfiguration configuration = certificationProgramMailConfigurationDao.createConfiguration(program, type);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(configuration);
		Assert.assertNotNull(configuration.getKey());
		Assert.assertNotNull(configuration.getCreationDate());
		Assert.assertNotNull(configuration.getLastModified());
		Assert.assertEquals(program, configuration.getCertificationProgram());
	}
	
	@Test
	public void getConfigurations() {
		CertificationProgram program = certificationProgramDao.createCertificationProgram("PM-2", "Program mailing 2");
		CertificationProgramMailType type = CertificationProgramMailType.certificate_issued;
		CertificationProgramMailConfiguration configuration = certificationProgramMailConfigurationDao.createConfiguration(program, type);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(configuration);
		
		List<CertificationProgramMailConfiguration> configurations = certificationProgramMailConfigurationDao.getConfigurations(program);
		Assertions.assertThat(configurations)
			.hasSize(1)
			.containsExactly(configuration);
	}
	
	@Test
	public void getConfigurationByCertificationProgramAndType() {
		CertificationProgram program = certificationProgramDao.createCertificationProgram("PM-2", "Program mailing 2");
		CertificationProgramMailType type = CertificationProgramMailType.certificate_issued;
		CertificationProgramMailConfiguration configuration = certificationProgramMailConfigurationDao.createConfiguration(program, type);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(configuration);
		
		CertificationProgramMailConfiguration issuedConfiguration = certificationProgramMailConfigurationDao
				.getConfiguration(program, CertificationProgramMailType.certificate_issued);
		Assert.assertEquals(configuration, issuedConfiguration);
	}
	

}
