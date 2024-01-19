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
package org.olat.course.assessment.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentInspectionConfiguration;
import org.olat.course.assessment.model.AssessmentInspectionConfigurationImpl;
import org.olat.course.assessment.model.AssessmentInspectionConfigurationWithUsage;
import org.olat.course.assessment.model.SafeExamBrowserConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 12 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AssessmentInspectionConfigurationDAO {
	
	@Autowired
	private DB dbInstance;
	
	public AssessmentInspectionConfiguration createInspectionConfiguration(RepositoryEntry entry) {
		AssessmentInspectionConfigurationImpl inspection = new AssessmentInspectionConfigurationImpl();
		inspection.setCreationDate(new Date());
		inspection.setLastModified(inspection.getCreationDate());
		inspection.setDuration(900);
		inspection.setRepositoryEntry(entry);
		inspection.setRestrictAccessIps(false);
		inspection.setSafeExamBrowserConfigDownload(true);
		return inspection;
	}

	public AssessmentInspectionConfiguration duplicateInspectionConfiguration(AssessmentInspectionConfiguration sourceConfiguration, String newName) {
		AssessmentInspectionConfigurationImpl configuration = new AssessmentInspectionConfigurationImpl();
		configuration.setCreationDate(new Date());
		configuration.setLastModified(configuration.getCreationDate());
		configuration.setName(newName);
		configuration.setDuration(sourceConfiguration.getDuration());
		configuration.setRepositoryEntry(sourceConfiguration.getRepositoryEntry());
		configuration.setRestrictAccessIps(StringHelper.containsNonWhitespace( sourceConfiguration.getIpList()));
		configuration.setIpList(sourceConfiguration.getIpList());
		configuration.setOverviewOptions(sourceConfiguration.getOverviewOptions());
		
		SafeExamBrowserConfiguration sebConfiguration = sourceConfiguration.getSafeExamBrowserConfiguration();
		if(sebConfiguration != null) {
			configuration.setSafeExamBrowserConfiguration(sebConfiguration);
		}
		configuration.setSafeExamBrowserKey(sourceConfiguration.getSafeExamBrowserKey());
		configuration.setSafeExamBrowserHint(sourceConfiguration.getSafeExamBrowserHint());
		configuration.setSafeExamBrowserConfigDownload(sourceConfiguration.isSafeExamBrowserConfigDownload());
		return saveConfiguration(configuration);
	}
	
	public AssessmentInspectionConfiguration saveConfiguration(AssessmentInspectionConfiguration configuration) {
		if(configuration.getKey() == null) {
			dbInstance.getCurrentEntityManager().persist(configuration);
		} else {
			((AssessmentInspectionConfigurationImpl)configuration).setLastModified(new Date());
			configuration = dbInstance.getCurrentEntityManager().merge(configuration);
		}
		return configuration;
	}
	
	public void deleteConfiguation(AssessmentInspectionConfiguration configuration) {
		dbInstance.getCurrentEntityManager().remove(configuration);
	}
	
	public AssessmentInspectionConfiguration getConfigurationById(Long key) {
		String query = """
				select config from courseassessmentinspectionconfig as config
				inner join fetch config.repositoryEntry as entry
				where config.key=:configurationKey""";
		
		List<AssessmentInspectionConfiguration> configurations = dbInstance.getCurrentEntityManager()
				.createQuery(query, AssessmentInspectionConfiguration.class)
				.setParameter("configurationKey", key)
				.getResultList();
		return configurations== null || configurations.isEmpty() ? null : configurations.get(0);
	}
	
	public List<AssessmentInspectionConfiguration> loadConfigurationsByEntry(RepositoryEntryRef entry) {
		String query = """
				select config from courseassessmentinspectionconfig as config
				inner join fetch config.repositoryEntry as entry
				where entry.key=:entryKey""";
		
		return dbInstance.getCurrentEntityManager().createQuery(query, AssessmentInspectionConfiguration.class)
				.setParameter("entryKey", entry.getKey())
				.getResultList();
	}
	
	public List<AssessmentInspectionConfigurationWithUsage> loadConfigurationsWithUsageByEntry(RepositoryEntryRef entry) {
		String query = """
				select config,
				(select count(inspection.key) from courseassessmentinspection as inspection
				  where inspection.configuration.key=config.key
				) as usage
				from courseassessmentinspectionconfig as config
				inner join fetch config.repositoryEntry as entry
				where entry.key=:entryKey""";
		
		List<Object[]> rawObjects = dbInstance.getCurrentEntityManager().createQuery(query, Object[].class)
				.setParameter("entryKey", entry.getKey())
				.getResultList();
		List<AssessmentInspectionConfigurationWithUsage> usages = new ArrayList<>(rawObjects.size());
		for(Object[] objects:rawObjects) {
			AssessmentInspectionConfiguration configuration = (AssessmentInspectionConfiguration)objects[0];
			long usage = PersistenceHelper.extractPrimitiveInt(objects, 1);
			usages.add(new AssessmentInspectionConfigurationWithUsage(configuration, usage));
		}
		return usages;
	}
}
