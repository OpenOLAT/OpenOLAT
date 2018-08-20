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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureParticipantSummary;
import org.olat.modules.lecture.model.LectureBlockStatistics;
import org.olat.modules.lecture.model.LectureParticipantSummaryImpl;
import org.olat.modules.lecture.model.ParticipantAndLectureSummary;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 31 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LectureParticipantSummaryDAO {

	@Autowired
	private DB dbInstance;
	
	public LectureParticipantSummary createSummary(RepositoryEntry entry, Identity identity, Date firstAdmissionDate) {
		return createSummary(entry, identity, firstAdmissionDate, null);
	}

	public LectureParticipantSummary createSummary(RepositoryEntry entry, Identity identity,
			Date firstAdmissionDate, LectureBlockStatistics statistics) {
		LectureParticipantSummaryImpl summary = new LectureParticipantSummaryImpl();
		summary.setCreationDate(new Date());
		summary.setLastModified(summary.getCreationDate());
		summary.setFirstAdmissionDate(firstAdmissionDate);
		if(statistics == null) {
			summary.setAbsentLectures(0);
			summary.setAttendedLectures(0);
			summary.setExcusedLectures(0);
			summary.setPlannedLectures(0);
		} else {
			summary.setAbsentLectures(statistics.getTotalAbsentLectures());
			summary.setAttendedLectures(statistics.getTotalAttendedLectures());
			summary.setExcusedLectures(0);
			summary.setPlannedLectures(statistics.getTotalPersonalPlannedLectures());
			if(statistics.isCalculateRate()) {
				summary.setAttendanceRate(statistics.getAttendanceRate());
			}
		}
		summary.setIdentity(identity);
		summary.setEntry(entry);
		dbInstance.getCurrentEntityManager().persist(summary);
		return summary;
	}
	
	public List<ParticipantAndLectureSummary> getLectureParticipantSummaries(LectureBlock block) {
		StringBuilder sb = new StringBuilder();
		sb.append("select participant, summary from lectureblock block")
		  .append(" inner join block.groups blockGroup")
		  .append(" inner join blockGroup.group bGroup")
		  .append(" inner join bGroup.members membership on (membership.role='").append(GroupRoles.participant.name()).append("')")
		  .append(" inner join membership.identity participant")
		  .append(" inner join participant.user participantUser")
		  .append(" left join lectureparticipantsummary summary on (summary.identity.key=participant.key and summary.entry.key=block.entry.key)")
		  .append(" where block.key=:blockKey");

		List<Object[]> rawObjects = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("blockKey", block.getKey())
				.getResultList();
		List<ParticipantAndLectureSummary> summaries = new ArrayList<>(rawObjects.size());
		for(Object[] rawObject:rawObjects) {
			Identity identity = (Identity)rawObject[0];
			LectureParticipantSummary summary = (LectureParticipantSummary)rawObject[1];
			summaries.add(new ParticipantAndLectureSummary(identity, summary));
		}
		return summaries;
	}
	
	public LectureParticipantSummary getSummary(RepositoryEntryRef entry, IdentityRef identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select summary from lectureparticipantsummary summary")
		  .append(" inner join fetch summary.entry entry")
		  .append(" inner join fetch summary.identity ident")
		  .append(" inner join fetch ident.user identUser")
		  .append(" where entry.key=:entryKey and ident.key=:identityKey");
		
		List<LectureParticipantSummary> summaries = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), LectureParticipantSummary.class)
			.setParameter("entryKey", entry.getKey())
			.setParameter("identityKey", identity.getKey())
			.getResultList();
		return summaries == null || summaries.isEmpty() ? null : summaries.get(0);
	}
	
	public List<LectureParticipantSummary> getSummary(RepositoryEntryRef entry) {
		StringBuilder sb = new StringBuilder();
		sb.append("select summary from lectureparticipantsummary summary")
		  .append(" inner join fetch summary.entry entry")
		  .append(" inner join fetch summary.identity ident")
		  .append(" inner join fetch ident.user identUser")
		  .append(" where entry.key=:entryKey");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), LectureParticipantSummary.class)
			.setParameter("entryKey", entry.getKey())
			.getResultList();
	}
	
	public int updateStatistics(LectureBlockStatistics statistics) {
		StringBuilder sb = new StringBuilder();
		sb.append("update lectureparticipantsummary set lastModified=:now,")
		  .append(" attendedLectures=:attendedLectures, absentLectures=:absentLectures,")
		  .append(" plannedLectures=:plannedLectures, attendanceRate=:attendanceRate")
		  .append(" where entry.key=:repoKey and identity.key=:identityKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("repoKey", statistics.getRepoKey())
				.setParameter("identityKey", statistics.getIdentityKey())
				.setParameter("attendedLectures", statistics.getTotalAttendedLectures())
				.setParameter("absentLectures", statistics.getTotalAbsentLectures())
				.setParameter("plannedLectures", statistics.getTotalPersonalPlannedLectures())
				.setParameter("attendanceRate", statistics.getAttendanceRate())
				.setParameter("now", new Date())
				.executeUpdate();
	}
	
	public int updateCalendarSynchronization(RepositoryEntryRef entry, IdentityRef identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("update lectureparticipantcalsummary set calendarSync=true, calendarLastSyncDate=:now")
		  .append("  where entry.key=:repoKey and identity.key=:identityKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("repoKey", entry.getKey())
				.setParameter("identityKey", identity.getKey())
				.setParameter("now", new Date())
				.executeUpdate();
	}
	
	public LectureParticipantSummary update(LectureParticipantSummary summary) {
		return dbInstance.getCurrentEntityManager().merge(summary);
	}
	
	public int deleteSummaries(RepositoryEntryRef entry) {
		//delete summaries
		String deleteSummaries = "delete from lectureparticipantsummary summary where summary.entry.key=:repoEntryKey";
		return dbInstance.getCurrentEntityManager()
			.createQuery(deleteSummaries)
			.setParameter("repoEntryKey", entry.getKey())
			.executeUpdate();
	}
	
	public int deleteSummaries(Identity identity) {
		//delete summaries
		String deleteSummaries = "delete from lectureparticipantsummary summary where summary.identity.key=:identityKey";
		return dbInstance.getCurrentEntityManager()
			.createQuery(deleteSummaries)
			.setParameter("identityKey", identity.getKey())
			.executeUpdate();
	}
}
