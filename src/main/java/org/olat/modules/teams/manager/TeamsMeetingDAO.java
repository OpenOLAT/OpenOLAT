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
package org.olat.modules.teams.manager;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.model.TeamsMeetingImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.microsoft.graph.models.AccessLevel;
import com.microsoft.graph.models.LobbyBypassScope;
import com.microsoft.graph.models.OnlineMeetingPresenters;

/**
 * 
 * Initial date: 23 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class TeamsMeetingDAO {
	
	@Autowired
	private DB dbInstance;
	
	public TeamsMeeting createMeeting(String subject, Date startDate, Date endDate,
			RepositoryEntry entry, String subIdent, BusinessGroup businessGroup, Identity creator) {
		TeamsMeetingImpl meeting = new TeamsMeetingImpl();
		meeting.setCreationDate(new Date());
		meeting.setLastModified(meeting.getCreationDate());
		meeting.setSubject(subject);
		meeting.setStartDate(startDate);
		meeting.setLeadTime(0l);
		meeting.setEndDate(endDate);
		meeting.setFollowupTime(0l);
		// ID for OO internal dispatcher (guest access). BBB does not now this ID. 
		meeting.setIdentifier(UUID.randomUUID().toString());
		
		meeting.setParticipantsCanOpen(false);
		meeting.setAllowedPresenters(OnlineMeetingPresenters.EVERYONE.name());
		meeting.setAccessLevel(AccessLevel.EVERYONE.name());
		meeting.setEntryExitAnnouncement(true);
		meeting.setLobbyBypassScope(LobbyBypassScope.ORGANIZATION.name());
		meeting.setEntry(entry);
		meeting.setSubIdent(subIdent);
		meeting.setBusinessGroup(businessGroup);
		meeting.setCreator(creator);
		dbInstance.getCurrentEntityManager().persist(meeting);
		return meeting;
	}
	
	public TeamsMeeting loadByKey(Long key) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select meeting from teamsmeeting as meeting")
		  .append(" left join fetch meeting.entry as entry")
		  .append(" left join fetch meeting.businessGroup as businessGroup")
		  .append(" where meeting.key=:meetingKey");
		
		List<TeamsMeeting> meetings = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), TeamsMeeting.class)
				.setParameter("meetingKey", key)
				.getResultList();
		return meetings == null || meetings.isEmpty() ? null : meetings.get(0);
	}
	
	public TeamsMeeting loadForUpdate(TeamsMeeting meeting) {
		//first remove it from caches
		dbInstance.getCurrentEntityManager().detach(meeting);

		QueryBuilder sb = new QueryBuilder();
		sb.append("select meeting from teamsmeeting as meeting")
		  .append(" where meeting.key=:meetingKey");

		List<TeamsMeeting> meetings = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), TeamsMeeting.class)
				.setParameter("meetingKey", meeting.getKey())
				.setLockMode(LockModeType.PESSIMISTIC_WRITE)
				.getResultList();
		return meetings == null || meetings.isEmpty() ? null : meetings.get(0);
	}
	
	public TeamsMeeting loadByIdentifier(String identifier) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select meeting from teamsmeeting as meeting")
		  .append(" left join fetch meeting.entry as entry")
		  .append(" left join fetch meeting.businessGroup as businessGroup")
		  .append(" left join fetch meeting.creator as creator")
		  .append(" left join fetch creator.user as creatorUser")
		  .append(" where meeting.identifier=:identifier or meeting.readableIdentifier=:identifier");
		
		List<TeamsMeeting> meetings = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), TeamsMeeting.class)
				.setParameter("identifier", identifier)
				.getResultList();
		return meetings == null || meetings.isEmpty() ? null : meetings.get(0);
	}
	
	public boolean isIdentifierInUse(String identifier, TeamsMeeting reference) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select meeting.key from teamsmeeting as meeting")
		  .append(" where (meeting.identifier=:identifier or meeting.readableIdentifier=:identifier)");
		if(reference != null) {
			sb.append(" and meeting.key<>:referenceKey");
		}
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("identifier", identifier)
				.setFirstResult(0)
				.setMaxResults(1);
		if(reference != null) {
			query.setParameter("referenceKey", reference.getKey());
		}
		
		List<Long> otherKeys = query.getResultList();
		return otherKeys != null && !otherKeys.isEmpty() && otherKeys.get(0) != null && otherKeys.get(0).longValue() > 0;
	}
	
	public TeamsMeeting updateMeeting(TeamsMeeting meeting) {
		TeamsMeetingImpl meetingImpl = (TeamsMeetingImpl)meeting;
		meetingImpl.setLastModified(new Date());
		updateDates(meetingImpl, meeting.getStartDate(), meeting.getLeadTime(),
				meeting.getEndDate(), meeting.getFollowupTime());
		return dbInstance.getCurrentEntityManager().merge(meeting);
	}
	
	public void deleteMeeting(TeamsMeeting meeting) {
		dbInstance.getCurrentEntityManager().remove(meeting);
	}
	
	public List<TeamsMeeting> getAllMeetings() {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select meeting from teamsmeeting as meeting");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), TeamsMeeting.class)
				.getResultList();
	}
	
	/**
	 * Returns all the meeting of the specified entry. Sub-identifier
	 * will be ignored.
	 * 
	 * @param entry The repository entry
	 * @return A list of meetings
	 */
	public List<TeamsMeeting> getMeetings(RepositoryEntryRef entry) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select meeting from teamsmeeting as meeting");
		if(entry != null) {
			sb.and().append("meeting.entry.key=:entryKey");
		}
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), TeamsMeeting.class)
			.setParameter("entryKey", entry.getKey())
			.getResultList();
	}
	
	public List<TeamsMeeting> getMeetings(RepositoryEntryRef entry, String subIdent, BusinessGroup businessGroup) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select meeting from teamsmeeting as meeting");
		if(entry != null) {
			sb.and().append("meeting.entry.key=:entryKey");
			if(StringHelper.containsNonWhitespace(subIdent)) {
				sb.and().append("meeting.subIdent=:subIdent");
			} else {
				sb.and().append("meeting.subIdent is null");
			}
		}
		if(businessGroup != null) {
			sb.and().append("meeting.businessGroup.key=:businessGroupKey");
		}
		
		TypedQuery<TeamsMeeting> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), TeamsMeeting.class);
		
		if(entry != null) {
			query.setParameter("entryKey", entry.getKey());
			if(StringHelper.containsNonWhitespace(subIdent)) {
				query.setParameter("subIdent", subIdent);
			}
		}
		if(businessGroup != null) {
			query.setParameter("businessGroupKey", businessGroup.getKey());
		}
		return query.getResultList();
	}
	
	public List<TeamsMeeting> getUpcomingMeetings(RepositoryEntryRef entry, String subIdent, int maxResults) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select meeting from teamsmeeting as meeting")
		  .append(" where meeting.entry.key=:entryKey")
		  .append(" and meeting.startDate is not null and meeting.endDate is not null");
		if(StringHelper.containsNonWhitespace(subIdent)) {
			sb.append(" and meeting.subIdent=:subIdent");
		} else {
			sb.append(" and meeting.subIdent is null");
		}
		sb.append(" and meeting.endDate>=:now")
		  .append(" order by meeting.startDate asc");

		TypedQuery<TeamsMeeting> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), TeamsMeeting.class)
			.setFirstResult(0)
			.setMaxResults(maxResults)
			.setParameter("entryKey", entry.getKey())
			.setParameter("now", new Date());
		if(StringHelper.containsNonWhitespace(subIdent)) {
			query.setParameter("subIdent", subIdent);
		}
		return query.getResultList();
	}
	
	private void updateDates(TeamsMeetingImpl meet, Date start, long leadTime, Date end, long followupTime) {
		if(start == null) {
			meet.setStartDate(null);
			meet.setLeadTime(0);
			meet.setStartWithLeadTime(null);
		} else {
			meet.setStartDate(start);
			meet.setLeadTime(leadTime);
			meet.setStartWithLeadTime(calculateStartWithLeadTime(start, leadTime));
		}
		
		if(end == null) {
			meet.setEndDate(null);
			meet.setFollowupTime(0);
			meet.setEndWithFollowupTime(null);
		} else {
			meet.setEndDate(end);
			meet.setFollowupTime(followupTime);
			meet.setEndWithFollowupTime(calculateEndWithFollowupTime(end, followupTime));
		}
	}
	
	protected Date calculateStartWithLeadTime(Date start, long leadTime) {
		start = cleanDate(start);
		Calendar cal = Calendar.getInstance();
		cal.setTime(start);
		if(leadTime > 0) {
			cal.add(Calendar.MINUTE, -(int)leadTime);
		}
		return cal.getTime();
	}
	
	protected Date calculateEndWithFollowupTime(Date end, long followupTime) {
		end = cleanDate(end);
		Calendar cal = Calendar.getInstance();
		cal.setTime(end);
		if(followupTime > 0) {
			cal.add(Calendar.MINUTE, (int)followupTime);
		}
		return cal.getTime();
	}
	
	/**
	 * Remove seconds and milliseconds.
	 * 
	 * @return A date without seconds and milliseconds
	 */
	private Date cleanDate(Date date) {
		if(date == null) return null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

}
