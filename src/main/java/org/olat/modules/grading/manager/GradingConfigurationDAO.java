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
package org.olat.modules.grading.manager;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.util.StringHelper;
import org.olat.modules.grading.GradingAssessedIdentityVisibility;
import org.olat.modules.grading.GradingNotificationType;
import org.olat.modules.grading.RepositoryEntryGradingConfiguration;
import org.olat.modules.grading.model.RepositoryEntryGradingConfigurationImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 13 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class GradingConfigurationDAO {
	
	@Autowired
	private DB dbInstance;
	
	public RepositoryEntryGradingConfiguration createConfiguration(RepositoryEntry entry) {
		RepositoryEntryGradingConfigurationImpl config = new RepositoryEntryGradingConfigurationImpl();
		config.setCreationDate(new Date());
		config.setLastModified(config.getCreationDate());
		config.setGradingEnabled(false);
		config.setIdentityVisibilityEnum(GradingAssessedIdentityVisibility.anonymous);
		config.setNotificationTypeEnum(GradingNotificationType.afterTestSubmission);
		
		config.setEntry(entry);
		dbInstance.getCurrentEntityManager().persist(config);
		return config;
	}
	
	public RepositoryEntryGradingConfiguration getConfiguration(RepositoryEntryRef entry) {
		StringBuilder sb = new StringBuilder();
		sb.append("select config from gradingconfiguration as config")
		  .append(" where config.entry.key=:entryKey");
		
		List<RepositoryEntryGradingConfiguration> configs = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntryGradingConfiguration.class)
				.setParameter("entryKey", entry.getKey())
				.getResultList();
		return configs == null || configs.isEmpty() ? null : configs.get(0);
	}
	
	public List<RepositoryEntryGradingConfiguration> getConfiguration(RepositoryEntryRef entry, String softKey) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select config from gradingconfiguration as config")
		  .append(" inner join config.entry as entry");
		
		if(StringHelper.containsNonWhitespace(softKey)) {
			sb.and().append("entry.softkey=:softkey");
		}
		if(entry != null) {
			sb.and().append("entry.key=:entryKey");
		}
		
		TypedQuery<RepositoryEntryGradingConfiguration> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntryGradingConfiguration.class);
		if(StringHelper.containsNonWhitespace(softKey)) {
			query.setParameter("softkey", softKey);
		}
		if(entry != null) {
			query.setParameter("entryKey", entry.getKey());
		}
		return query.getResultList();
	}
	
	public Map<Long,GradingAssessedIdentityVisibility> getIdentityVisibility(Collection<RepositoryEntryRef> entries) {
		StringBuilder sb = new StringBuilder();
		sb.append("select config.entry.key, config.identityVisibility from gradingconfiguration as config");
		if(entries != null && !entries.isEmpty()) {
			sb.append(" where config.entry.key in (:entriesKey)");
		}
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class);
		if(entries != null && !entries.isEmpty()) {
			List<Long> entriesKeys = entries.stream()
					.map(RepositoryEntryRef::getKey)
					.collect(Collectors.toList());
			query.setParameter("entriesKey", entriesKeys);
		}
		
		List<Object[]> rawObjects = query.getResultList();
		Map<Long,GradingAssessedIdentityVisibility> visibilityMap = new HashMap<>();
		for(Object[] object:rawObjects) {
			Long entryKey = (Long)object[0];
			String visibility = (String)object[1];
			if(entryKey != null && StringHelper.containsNonWhitespace(visibility)) {
				visibilityMap.put(entryKey, GradingAssessedIdentityVisibility.valueOf(visibility));
			}	
		}
		return visibilityMap;
	}
	
	public RepositoryEntryGradingConfiguration updateConfiguration(RepositoryEntryGradingConfiguration config) {
		((RepositoryEntryGradingConfigurationImpl)config).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(config);
	}
	
	public void deleteConfiguration(RepositoryEntryGradingConfiguration config) {
		dbInstance.getCurrentEntityManager().remove(config);
	}
}
