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

import javax.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.model.BigBlueButtonMeetingImpl;
import org.olat.repository.RepositoryEntry;
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
			RepositoryEntry entry, String subIdent, BusinessGroup businessGroup) {
		BigBlueButtonMeetingImpl meeting = new BigBlueButtonMeetingImpl();
		meeting.setCreationDate(new Date());
		meeting.setLastModified(meeting.getCreationDate());
		meeting.setName(name);
		meeting.setMeetingId(UUID.randomUUID().toString());
		meeting.setAttendeePassword(UUID.randomUUID().toString());
		meeting.setModeratorPassword(UUID.randomUUID().toString());
		
		meeting.setEntry(entry);
		meeting.setSubIdent(subIdent);
		meeting.setBusinessGroup(businessGroup);
		
		dbInstance.getCurrentEntityManager().persist(meeting);
		return meeting;
	}
	
	public BigBlueButtonMeeting loadByKey(Long key) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select meeting from bigbluebuttonmeeting as meeting")
		  .append(" left join fetch meeting.entry as entry")
		  .append(" left join fetch meeting.businessGroup as businessGroup")
		  .append(" left join fetch meeting.template as template")
		  .append(" where meeting.key=:meetingKey");
		
		List<BigBlueButtonMeeting> meetings = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BigBlueButtonMeeting.class)
				.setParameter("meetingKey", key)
				.getResultList();
		return meetings == null || meetings.isEmpty() ? null : meetings.get(0);
	}
	
	public BigBlueButtonMeeting updateMeeting(BigBlueButtonMeeting meeting) {
		meeting.setLastModified(new Date());
		updateDates((BigBlueButtonMeetingImpl)meeting,
				meeting.getStartDate(), meeting.getLeadTime(), meeting.getEndDate(), meeting.getFollowupTime());
		return dbInstance.getCurrentEntityManager().merge(meeting);
	}
	
	public void deleteMeeting(BigBlueButtonMeeting meeting) {
		dbInstance.getCurrentEntityManager().remove(meeting);
	}
	
	private void updateDates(BigBlueButtonMeetingImpl meet, Date start, long leadTime, Date end, long followupTime) {
		Calendar cal = Calendar.getInstance();
		if(start == null) {
			meet.setStartDate(null);
			meet.setLeadTime(0);
			meet.setStartWithLeadTime(null);
		} else {
			start = cleanDate(start);
			if(leadTime > 0) {
				cal.add(Calendar.MINUTE, -(int)leadTime);
			}
			meet.setStartDate(start);
			meet.setLeadTime(leadTime);
			meet.setStartWithLeadTime(cal.getTime());
		}
		
		if(end == null) {
			meet.setEndDate(null);
			meet.setFollowupTime(0);
			meet.setEndWithFollowupTime(null);
		} else {
			end = cleanDate(end);
			cal.setTime(end);
			if(followupTime > 0) {
				cal.add(Calendar.MINUTE, (int)followupTime);
			}
			meet.setEndDate(end);
			meet.setFollowupTime(followupTime);
			meet.setEndWithFollowupTime(cal.getTime());
		}
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
		  .append(" left join fetch meeting.template as template");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BigBlueButtonMeeting.class)
				.getResultList();
	}
	
	public List<BigBlueButtonMeeting> getMeetings(RepositoryEntry entry, String subIdent, BusinessGroup businessGroup) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select meeting from bigbluebuttonmeeting as meeting")
		  .append(" left join fetch meeting.template as template");
		if(entry != null) {
			sb.and().append("meeting.entry.key=:entryKey");
			if(StringHelper.containsNonWhitespace(subIdent)) {
				sb.and().append("meeting.subIdent=:subIdent");
			}
		}
		if(businessGroup != null) {
			sb.and().append("meeting.businessGroup.key=:businessGroupKey");
		}
		
		TypedQuery<BigBlueButtonMeeting> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), BigBlueButtonMeeting.class);
		
		if(entry != null) {
			query.setParameter("entryKey", entry.getKey());
			sb.and().append("meeting.entry.key=:entryKey");
			if(StringHelper.containsNonWhitespace(subIdent)) {
				query.setParameter("subIdent", subIdent);
			}
		}
		if(businessGroup != null) {
			query.setParameter("businessGroupKey", businessGroup.getKey());
		}
		return query.getResultList();
	}

}
