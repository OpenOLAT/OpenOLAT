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
package org.olat.modules.openbadges.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.modules.openbadges.BadgeEntryConfiguration;
import org.olat.modules.openbadges.model.BadgeEntryConfigurationImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 2023-06-13<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class BadgeEntryConfigurationDAO {

	@Autowired
	private DB dbInstance;

	public BadgeEntryConfiguration createConfiguration(RepositoryEntry entry) {
		BadgeEntryConfigurationImpl configuration = new BadgeEntryConfigurationImpl();
		configuration.setCreationDate(new Date());
		configuration.setLastModified(configuration.getCreationDate());
		configuration.setEntry(entry);
		dbInstance.getCurrentEntityManager().persist(configuration);
		return configuration;
	}

	public BadgeEntryConfiguration getConfiguration(RepositoryEntryRef entry) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select config from badgeentryconfig config ");
		sb.append("where config.entry.key =: entryKey");
		List<BadgeEntryConfiguration> configurations = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BadgeEntryConfiguration.class)
				.setParameter("entryKey", entry.getKey())
				.getResultList();
		return configurations == null || configurations.isEmpty() ? null : configurations.get(0);
	}

	public BadgeEntryConfiguration updateConfiguration(BadgeEntryConfiguration configuration) {
		configuration.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(configuration);
	}

	public int delete(RepositoryEntryRef entry) {
		StringBuilder sb = new StringBuilder();
		sb.append("delete badgeentryconfig config where config.entry.key =: entryKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("entryKey", entry.getKey())
				.executeUpdate();
	}
}
