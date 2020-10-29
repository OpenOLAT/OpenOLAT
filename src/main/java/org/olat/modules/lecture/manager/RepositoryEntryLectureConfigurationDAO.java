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
package org.olat.modules.lecture.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.modules.lecture.LectureBlockRef;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;
import org.olat.modules.lecture.model.RepositoryEntryLectureConfigurationImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 3 avr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class RepositoryEntryLectureConfigurationDAO {
	
	@Autowired
	private DB dbInstance;
	
	public RepositoryEntryLectureConfiguration createConfiguration(RepositoryEntry entry) {
		RepositoryEntryLectureConfigurationImpl config = new RepositoryEntryLectureConfigurationImpl();
		config.setCreationDate(new Date());
		config.setLastModified(config.getCreationDate());
		config.setLectureEnabled(false);
		config.setOverrideModuleDefault(false);
		config.setEntry(entry);
		dbInstance.getCurrentEntityManager().persist(config);
		return config;
	}
	
	public RepositoryEntryLectureConfiguration cloneConfiguration(RepositoryEntryLectureConfiguration sourceConfig,  RepositoryEntry entry) {
		RepositoryEntryLectureConfigurationImpl config = new RepositoryEntryLectureConfigurationImpl();
		config.setCreationDate(new Date());
		config.setLastModified(config.getCreationDate());
		config.setLectureEnabled(sourceConfig.isLectureEnabled());
		config.setOverrideModuleDefault(sourceConfig.isOverrideModuleDefault());
		config.setCalculateAttendanceRate(sourceConfig.getCalculateAttendanceRate());
		config.setRequiredAttendanceRate(sourceConfig.getRequiredAttendanceRate());
		config.setParticipantCalendarSyncEnabled(sourceConfig.getParticipantCalendarSyncEnabled());
		config.setTeacherCalendarSyncEnabled(sourceConfig.getTeacherCalendarSyncEnabled());
		config.setRollCallEnabled(sourceConfig.getRollCallEnabled());
		config.setEntry(entry);
		dbInstance.getCurrentEntityManager().persist(config);
		return config;
	}
	
	public RepositoryEntryLectureConfiguration getConfiguration(RepositoryEntryRef entry) {
		List<RepositoryEntryLectureConfiguration> configs = dbInstance.getCurrentEntityManager()
				.createNamedQuery("lectureconfigByRepositoryEntry", RepositoryEntryLectureConfiguration.class)
				.setParameter("entryKey", entry.getKey())
				.getResultList();
		return configs == null || configs.isEmpty() ? null : configs.get(0);
	}
	
	public boolean isConfigurationEnabledFor(RepositoryEntryRef entry) {
		String query = "select config.key from lectureentryconfig config where config.entry.key=:entryKey and config.lectureEnabled=true";

		List<Long> configs = dbInstance.getCurrentEntityManager()
				.createQuery(query, Long.class)
				.setParameter("entryKey", entry.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return configs != null && !configs.isEmpty() && configs.get(0).longValue() > 0;
	}
	
	public RepositoryEntryLectureConfiguration getConfiguration(LectureBlockRef entry) {
		StringBuilder sb = new StringBuilder();
		sb.append("select config from lectureentryconfig config")
		  .append(" where config.entry.key in (select block.entry.key from lectureblock block")
		  .append("  where block.key=:blockKey")
		  .append(" )");
		
		List<RepositoryEntryLectureConfiguration> configs = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntryLectureConfiguration.class)
				.setParameter("blockKey", entry.getKey())
				.getResultList();
		return configs == null || configs.isEmpty() ? null : configs.get(0);
	}
	
	public RepositoryEntryLectureConfiguration update(RepositoryEntryLectureConfiguration config) {
		return dbInstance.getCurrentEntityManager().merge(config);
	}
	
	public int deleteConfiguration(RepositoryEntryRef entry) {
		String sb = "delete from  lectureentryconfig config where config.entry.key=:repoEntryKey";
		return dbInstance.getCurrentEntityManager().createQuery(sb)
				.setParameter("repoEntryKey", entry.getKey())
				.executeUpdate();
	}
}
