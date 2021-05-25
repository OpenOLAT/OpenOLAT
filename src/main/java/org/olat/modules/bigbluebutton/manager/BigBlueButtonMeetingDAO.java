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
package org.olat.modules.bigbluebutton.manager;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.LockModeType;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingLayoutEnum;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingTemplate;
import org.olat.modules.bigbluebutton.BigBlueButtonServer;
import org.olat.modules.bigbluebutton.JoinPolicyEnum;
import org.olat.modules.bigbluebutton.model.BigBlueButtonMeetingImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 18 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class BigBlueButtonMeetingDAO {

	@Autowired
	private DB dbInstance;
	
	public BigBlueButtonMeeting createAndPersistMeeting(String name,
			RepositoryEntry entry, String subIdent, BusinessGroup businessGroup, Identity creator) {
		BigBlueButtonMeetingImpl meeting = new BigBlueButtonMeetingImpl();
		meeting.setCreationDate(new Date());
		meeting.setLastModified(meeting.getCreationDate());
		meeting.setName(name);
		// ID for BBB used as meetingID, in metadata as "externalId"
		meeting.setMeetingId(UUID.randomUUID().toString());
		meeting.setAttendeePassword(UUID.randomUUID().toString());
		meeting.setModeratorPassword(UUID.randomUUID().toString());
		meeting.setMeetingLayout(BigBlueButtonMeetingLayoutEnum.standard);
		
		meeting.setGuest(false);
		meeting.setJoinPolicyEnum(JoinPolicyEnum.disabled);
		// ID for OO internal dispatcher (guest access). BBB does not now this ID. 
		meeting.setIdentifier(UUID.randomUUID().toString());
		
		meeting.setEntry(entry);
		// ID for OO internal context subinformation such as the course node ID
		meeting.setSubIdent(subIdent);
		meeting.setBusinessGroup(businessGroup);
		
		meeting.setCreator(creator);
		
		dbInstance.getCurrentEntityManager().persist(meeting);
		
		return meeting;
	}
	
	public BigBlueButtonMeeting loadByKey(Long key) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select meeting from bigbluebuttonmeeting as meeting")
		  .append(" left join fetch meeting.entry as entry")
		  .append(" left join fetch meeting.businessGroup as businessGroup")
		  .append(" left join fetch meeting.template as template")
		  .append(" left join fetch meeting.server as server")
		  .append(" left join fetch meeting.creator as creator")
		  .append(" left join fetch creator.user as creatorUser")
		  .append(" where meeting.key=:meetingKey");
		
		List<BigBlueButtonMeeting> meetings = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BigBlueButtonMeeting.class)
				.setParameter("meetingKey", key)
				.getResultList();
		return meetings == null || meetings.isEmpty() ? null : meetings.get(0);
	}
	
	public BigBlueButtonMeeting loadByIdentifier(String identifier) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select meeting from bigbluebuttonmeeting as meeting")
		  .append(" left join fetch meeting.entry as entry")
		  .append(" left join fetch meeting.businessGroup as businessGroup")
		  .append(" left join fetch meeting.template as template")
		  .append(" left join fetch meeting.server as server")
		  .append(" left join fetch meeting.creator as creator")
		  .append(" left join fetch creator.user as creatorUser")
		  .append(" where meeting.identifier=:identifier or meeting.readableIdentifier=:identifier");
		
		List<BigBlueButtonMeeting> meetings = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BigBlueButtonMeeting.class)
				.setParameter("identifier", identifier)
				.getResultList();
		return meetings == null || meetings.isEmpty() ? null : meetings.get(0);
	}
	
	public boolean isIdentifierInUse(String identifier, BigBlueButtonMeeting reference) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select meeting.key from bigbluebuttonmeeting as meeting")
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
	
	public List<String> getMeetingsIds(Date from, Date to ) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select meeting.meetingId from bigbluebuttonmeeting as meeting")
		  .append(" where meeting.permanent=true or (meeting.startDate>:from and meeting.startDate<:to)");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), String.class)
				.setParameter("from", from, TemporalType.TIMESTAMP)
				.setParameter("to", to, TemporalType.TIMESTAMP)
				.getResultList();
	}
	
	public List<BigBlueButtonMeeting> loadMeetingsByEnd(Date endFrom, Date endTo) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select meeting from bigbluebuttonmeeting as meeting")
		  .append(" where meeting.endDate>:from and meeting.endDate<:to");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BigBlueButtonMeeting.class)
				.setParameter("from", endFrom, TemporalType.TIMESTAMP)
				.setParameter("to", endTo, TemporalType.TIMESTAMP)
				.getResultList();
	}
	
	public List<BigBlueButtonMeeting> loadPermanentMeetings() {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select meeting from bigbluebuttonmeeting as meeting")
		  .append(" where meeting.permanent=true");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BigBlueButtonMeeting.class)
				.getResultList();
	}
	
	public BigBlueButtonMeeting loadForUpdate(BigBlueButtonMeeting meeting) {
		//first remove it from caches
		dbInstance.getCurrentEntityManager().detach(meeting);

		StringBuilder sb = new StringBuilder();
		sb.append("select meeting from bigbluebuttonmeeting as meeting")
		  .append(" where meeting.key=:meetingKey");

		List<BigBlueButtonMeeting> meetings = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BigBlueButtonMeeting.class)
				.setParameter("meetingKey", meeting.getKey())
				.setLockMode(LockModeType.PESSIMISTIC_WRITE)
				.getResultList();
		return meetings == null || meetings.isEmpty() ? null : meetings.get(0);
	}
	
	public List<BigBlueButtonMeeting> getMeetings(BigBlueButtonServer server) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select meeting from bigbluebuttonmeeting as meeting")
		  .append(" left join fetch meeting.entry as entry")
		  .append(" left join fetch meeting.businessGroup as businessGroup")
		  .append(" left join fetch meeting.template as template")
		  .append(" left join fetch meeting.server as server")
		  .append(" where meeting.server.key=:serverKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BigBlueButtonMeeting.class)
				.setParameter("serverKey", server.getKey())
				.getResultList();
	}
	
	public BigBlueButtonMeeting updateMeeting(BigBlueButtonMeeting meeting) {
		meeting.setLastModified(new Date());
		if(!StringHelper.containsNonWhitespace(meeting.getIdentifier())) {
			((BigBlueButtonMeetingImpl)meeting).setIdentifier(UUID.randomUUID().toString());
		}
		updateDates((BigBlueButtonMeetingImpl)meeting,
				meeting.getStartDate(), meeting.getLeadTime(), meeting.getEndDate(), meeting.getFollowupTime());
		return dbInstance.getCurrentEntityManager().merge(meeting);
	}
	
	public void deleteMeeting(BigBlueButtonMeeting meeting) {
		dbInstance.getCurrentEntityManager().remove(meeting);
	}
	
	private void updateDates(BigBlueButtonMeetingImpl meet, Date start, long leadTime, Date end, long followupTime) {
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
	
	public List<BigBlueButtonMeeting> getAllMeetings() {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select meeting from bigbluebuttonmeeting as meeting")
		  .append(" left join fetch meeting.entry as entry")
		  .append(" left join fetch meeting.businessGroup as businessGroup")
		  .append(" left join fetch meeting.template as template")
		  .append(" left join fetch meeting.server as server");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BigBlueButtonMeeting.class)
				.getResultList();
	}
	
	public List<Long> getConcurrentMeetings(BigBlueButtonMeetingTemplate template, Date start, Date end) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct meeting.key from bigbluebuttonmeeting as meeting")
		  .append(" inner join meeting.template as template")
		  .append(" where template.key=:templateKey")
		  .append(" and (")
		  .append("  (meeting.startDate>=:startDate and meeting.startDate<:endDate)")
		  .append("  or")
		  .append("  (meeting.endDate>:startDate and meeting.endDate<=:endDate)")
		  .append("  or")
		  .append("  (meeting.startDate>=:startDate and meeting.endDate<=:endDate)")
		  .append("  or")
		  .append("  (meeting.startDate<=:startDate and meeting.endDate>=:endDate)")
		  .append("  or")
		  .append("  meeting.permanent=true")
		  .append(")");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("templateKey", template.getKey())
				.setParameter("startDate", start)
				.setParameter("endDate", end)
				.getResultList();
	}
	
	public List<BigBlueButtonMeeting> getMeetings(RepositoryEntryRef entry, String subIdent, BusinessGroup businessGroup, boolean guestOnly) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select meeting from bigbluebuttonmeeting as meeting")
		  .append(" left join fetch meeting.template as template")
		  .append(" left join fetch meeting.server as server");
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
		if(guestOnly) {
			sb.and().append("meeting.guest=true");
		}
		
		TypedQuery<BigBlueButtonMeeting> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), BigBlueButtonMeeting.class);
		
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
	
	public List<BigBlueButtonMeeting> getUpcomingMeetings(RepositoryEntryRef entry, String subIdent, int maxResults) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select meeting from bigbluebuttonmeeting as meeting")
		  .append(" left join fetch meeting.template as template")
		  .append(" left join fetch meeting.server as server")
		  .append(" where meeting.entry.key=:entryKey and meeting.permanent=false")
		  .append(" and meeting.startDate is not null and meeting.endDate is not null");
		if(StringHelper.containsNonWhitespace(subIdent)) {
			sb.append(" and meeting.subIdent=:subIdent");
		} else {
			sb.append(" and meeting.subIdent is null");
		}
		sb.append(" and meeting.endDate>=:now")
		  .append(" order by meeting.startDate asc");

		TypedQuery<BigBlueButtonMeeting> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), BigBlueButtonMeeting.class)
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
