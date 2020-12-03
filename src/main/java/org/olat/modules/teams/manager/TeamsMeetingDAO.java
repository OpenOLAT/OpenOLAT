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

import java.util.Date;
import java.util.List;

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

import com.microsoft.graph.models.generated.AccessLevel;
import com.microsoft.graph.models.generated.OnlineMeetingPresenters;

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
		meeting.setEndDate(endDate);
		meeting.setAllowedPresenters(OnlineMeetingPresenters.EVERYONE.name());
		meeting.setAccessLevel(AccessLevel.EVERYONE.name());
		meeting.setEntryExitAnnouncement(true);
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
	
	public TeamsMeeting updateMeeting(TeamsMeeting meeting) {
		((TeamsMeetingImpl)meeting).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(meeting);
	}
	
	public void deleteMeeting(TeamsMeeting meeting) {
		dbInstance.getCurrentEntityManager().remove(meeting);
	}
	
	public List<TeamsMeeting> getMeetings(RepositoryEntryRef entry, String subIdent, BusinessGroup businessGroup) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select meeting from teamsmeeting as meeting");
		if(entry != null) {
			sb.and().append("meeting.entry.key=:entryKey");
			if(StringHelper.containsNonWhitespace(subIdent)) {
				sb.and().append("meeting.subIdent=:subIdent");
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

}
