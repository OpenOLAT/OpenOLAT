/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.certificate.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.course.certificate.RepositoryEntryCertificateConfiguration;
import org.olat.course.certificate.model.RepositoryEntryCertificateConfigurationImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 5 avr. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class RepositoryEntryCertificateConfigurationDAO {

	@Autowired
	private DB dbInstance;
	
	public RepositoryEntryCertificateConfiguration createConfiguration(RepositoryEntry entry) {
		RepositoryEntryCertificateConfigurationImpl config = new RepositoryEntryCertificateConfigurationImpl();
		config.setCreationDate(new Date());
		config.setLastModified(config.getCreationDate());
		config.setEntry(entry);
		dbInstance.getCurrentEntityManager().persist(config);
		return config;
	}
	
	public RepositoryEntryCertificateConfiguration cloneConfiguration(RepositoryEntryCertificateConfiguration sourceConfig,  RepositoryEntry entry) {
		RepositoryEntryCertificateConfigurationImpl config = new RepositoryEntryCertificateConfigurationImpl();
		cloneConfiguration(sourceConfig, config);
		config.setEntry(entry);
		dbInstance.getCurrentEntityManager().persist(config);
		return config;
	}
	
	public RepositoryEntryCertificateConfiguration cloneConfiguration(RepositoryEntryCertificateConfiguration sourceConfig,  RepositoryEntryCertificateConfiguration config) {
		((RepositoryEntryCertificateConfigurationImpl)config).setCreationDate(new Date());
		config.setLastModified(config.getCreationDate());
		config.setManualCertificationEnabled(sourceConfig.isManualCertificationEnabled());
		config.setAutomaticCertificationEnabled(sourceConfig.isAutomaticCertificationEnabled());
		config.setCertificateCustom1(sourceConfig.getCertificateCustom1());
		config.setCertificateCustom2(sourceConfig.getCertificateCustom2());
		config.setCertificateCustom3(sourceConfig.getCertificateCustom3());

		config.setValidityEnabled(sourceConfig.isValidityEnabled());
		config.setValidityTimelapse(sourceConfig.getValidityTimelapse());
		config.setValidityTimelapseUnit(sourceConfig.getValidityTimelapseUnit());
		
		config.setRecertificationEnabled(sourceConfig.isRecertificationEnabled());
		config.setRecertificationLeadTimeEnabled(sourceConfig.isRecertificationLeadTimeEnabled());
		config.setRecertificationLeadTimeInDays(sourceConfig.getRecertificationLeadTimeInDays());
		return config;
	}
	
	public RepositoryEntryCertificateConfiguration getConfiguration(RepositoryEntryRef entry) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select config from certificateentryconfig config")
		  .append(" left join fetch config.template as template")
		  .append(" where config.entry.key=:entryKey");

		List<RepositoryEntryCertificateConfiguration> configurations = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), RepositoryEntryCertificateConfiguration.class)
			.setParameter("entryKey", entry.getKey())
			.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return configurations == null || configurations.isEmpty() ? null : configurations.get(0);
	}
	
	public RepositoryEntryCertificateConfiguration updateConfiguration(RepositoryEntryCertificateConfiguration configuration) {
		configuration.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(configuration);
	}
	
	public boolean isCertificateEnabled(RepositoryEntryRef entry) {
		List<Long> configurations = dbInstance.getCurrentEntityManager()
				.createNamedQuery("enabledCertification", Long.class)
				.setParameter("entryKey", entry.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return configurations != null && !configurations.isEmpty()
				&& configurations.get(0) != null && configurations.get(0).longValue() > 0;
	}
	
	public boolean isAutomaticCertificationEnabled(RepositoryEntryRef entry) {
		List<Long> configurations = dbInstance.getCurrentEntityManager()
				.createNamedQuery("enabledAutomaticCertification", Long.class)
				.setParameter("entryKey", entry.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return configurations != null && !configurations.isEmpty()
				&& configurations.get(0) != null && configurations.get(0).longValue() > 0;
	}
	
	public int deleteConfiguration(RepositoryEntryRef entry) {
		StringBuilder sb = new StringBuilder();
		sb.append("delete certificateentryconfig config where config.entry.key=:entryKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("entryKey", entry.getKey())
				.executeUpdate();
	}
}
