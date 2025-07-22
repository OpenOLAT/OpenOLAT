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
package org.olat.modules.creditpoint.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.RepositoryEntryCreditPointConfiguration;
import org.olat.modules.creditpoint.model.RepositoryEntryCreditPointConfigurationImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 15 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class RepositoryEntryCreditPointConfigurationDAO {
	
	@Autowired
	private DB dbInstance;
	
	public RepositoryEntryCreditPointConfiguration createConfiguration(RepositoryEntry entry, CreditPointSystem system) {
		RepositoryEntryCreditPointConfigurationImpl config = new RepositoryEntryCreditPointConfigurationImpl();
		config.setCreationDate(new Date());
		config.setLastModified(config.getCreationDate());
		config.setEnabled(false);
		config.setCreditPointSystem(system);
		config.setRepositoryEntry(entry);
		dbInstance.getCurrentEntityManager().persist(config);
		return config;
	}
	
	public RepositoryEntryCreditPointConfiguration loadConfiguration(RepositoryEntryRef entry) {
		String query = """
				select config from creditpointrepositoryentry as config
				inner join fetch config.repositoryEntry as v
				left join fetch config.creditPointSystem as sys
				where v.key=:entryKey""";
		List<RepositoryEntryCreditPointConfiguration> configs = dbInstance.getCurrentEntityManager()
				.createQuery(query, RepositoryEntryCreditPointConfiguration.class).setParameter("entryKey", entry.getKey())
				.getResultList();
		return configs == null || configs.isEmpty() ? null : configs.get(0);
	}
	
	public RepositoryEntryCreditPointConfiguration updateConfiguration(RepositoryEntryCreditPointConfiguration config) {
		config.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(config);
	}
}
