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
import java.util.UUID;

import org.olat.core.commons.persistence.DB;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramMailConfiguration;
import org.olat.modules.certificationprogram.CertificationProgramMailConfigurationStatus;
import org.olat.modules.certificationprogram.CertificationProgramMailType;
import org.olat.modules.certificationprogram.model.CertificationProgramMailConfigurationImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 11 nov. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class CertificationProgramMailConfigurationDAO {
	
	@Autowired
	private DB dbInstance;
	
	public CertificationProgramMailConfiguration createConfiguration(CertificationProgram program, CertificationProgramMailType type) {
		CertificationProgramMailConfigurationImpl configuration = new CertificationProgramMailConfigurationImpl();
		configuration.setCreationDate(new Date());
		configuration.setLastModified(configuration.getCreationDate());
		configuration.setType(type);
		configuration.setStatus(CertificationProgramMailConfigurationStatus.active);
		configuration.setCustomized(false);
		configuration.setI18nSuffix(UUID.randomUUID().toString().replace("-", ""));
		configuration.setCertificationProgram(program);
		dbInstance.getCurrentEntityManager().persist(configuration);
		return configuration;
	}
	
	public CertificationProgramMailConfiguration updateConfiguration(CertificationProgramMailConfiguration configuration) {
		configuration.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(configuration);
	}
	
	public CertificationProgramMailConfiguration getConfiguration(Long key) {
		String query = """
				select config from certificationprogrammailconfiguration as config
				inner join fetch config.certificationProgram as program
				where config.key=:configurationKey""";
		List<CertificationProgramMailConfiguration> configs = dbInstance.getCurrentEntityManager()
				.createQuery(query, CertificationProgramMailConfiguration.class)
				.setParameter("configurationKey", key)
				.getResultList();
		return configs == null || configs.isEmpty() ? null : configs.get(0);
	}
	
	public List<CertificationProgramMailConfiguration> getConfigurations(CertificationProgramMailType type,
			CertificationProgramMailConfigurationStatus status) {
		String query = """
				select config from certificationprogrammailconfiguration as config
				inner join fetch config.certificationProgram as program
				where config.type=:type and config.status=:status""";
		return dbInstance.getCurrentEntityManager().createQuery(query, CertificationProgramMailConfiguration.class)
				.setParameter("type", type)
				.setParameter("status", status)
				.getResultList();
	}
	
	public List<CertificationProgramMailConfiguration> getConfigurations(CertificationProgram program) {
		String query = """
				select config from certificationprogrammailconfiguration as config
				inner join fetch config.certificationProgram as program
				where program.key=:programKey""";
		return dbInstance.getCurrentEntityManager().createQuery(query, CertificationProgramMailConfiguration.class)
				.setParameter("programKey", program.getKey())
				.getResultList();
	}
	
	public CertificationProgramMailConfiguration getConfiguration(CertificationProgram program, CertificationProgramMailType type) {
		String query = """
				select config from certificationprogrammailconfiguration as config
				inner join fetch config.certificationProgram as program
				where program.key=:programKey and config.type=:type
				order by config.creationDate desc""";
		List<CertificationProgramMailConfiguration> configs = dbInstance.getCurrentEntityManager().createQuery(query, CertificationProgramMailConfiguration.class)
				.setParameter("programKey", program.getKey())
				.setParameter("type", type)
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return configs == null || configs.isEmpty() ? null : configs.get(0);
	}

}
