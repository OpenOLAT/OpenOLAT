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
package org.olat.modules.adobeconnect.manager;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupRef;
import org.olat.modules.adobeconnect.AdobeConnectMeeting;
import org.olat.modules.adobeconnect.model.AdobeConnectMeetingImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 4 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AdobeConnectMeetingDAO {
	
	@Autowired
	private DB dbInstance;
	
	public AdobeConnectMeeting createMeeting(String name, String description,
			boolean permanent, Date start, long leadTime, Date end, long followupTime,
			String templateId, String scoId, String folderId, String envName,
			RepositoryEntry entry, String subIdent, BusinessGroup businessGroup) {
		AdobeConnectMeetingImpl meeting = new AdobeConnectMeetingImpl();
		meeting.setCreationDate(new Date());
		meeting.setLastModified(meeting.getCreationDate());
		meeting.setName(name);
		meeting.setDescription(description);
		meeting.setOpened(false);
		meeting.setPermanent(permanent);
		updateDates(meeting, start, leadTime, end, followupTime);
		meeting.setTemplateId(templateId);
		meeting.setScoId(scoId);
		meeting.setFolderId(folderId);
		meeting.setEnvName(envName);
		if(entry != null) {
			meeting.setEntry(entry);
			meeting.setSubIdent(subIdent);
		} else {
			meeting.setBusinessGroup(businessGroup);
		}
		dbInstance.getCurrentEntityManager().persist(meeting);
		return meeting;
	}
	
	public AdobeConnectMeeting updateMeeting(AdobeConnectMeeting meeting) {
		AdobeConnectMeetingImpl meet = (AdobeConnectMeetingImpl)meeting;
		meet.setLastModified(new Date());
		updateDates(meet, meet.getStartDate(), meet.getLeadTime(), meet.getEndDate(), meet.getFollowupTime());
		return dbInstance.getCurrentEntityManager().merge(meet);
	}
	
	private void updateDates(AdobeConnectMeetingImpl meet, Date start, long leadTime, Date end, long followupTime) {
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
	 * Remove seconds and milliseconds
	 * @return
	 */
	private Date cleanDate(Date date) {
		if(date == null) return null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	public AdobeConnectMeeting loadByKey(Long key) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select meeting from adobeconnectmeeting as meeting")
		  .append(" left join fetch meeting.entry as v")
		  .append(" left join fetch v.olatResource as resource")
		  .append(" left join fetch meeting.businessGroup as businessGroup")
		  .append(" where meeting.key=:key");
		
		List<AdobeConnectMeeting> meetings = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AdobeConnectMeeting.class)
				.setParameter("key", key)
				.getResultList();
		return meetings == null || meetings.isEmpty() ? null : meetings.get(0);
	}
	
	public List<AdobeConnectMeeting> getMeetingsBefore(Date date) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select meeting from adobeconnectmeeting as meeting")
		  .append(" inner join fetch meeting.entry as v")
		  .append(" inner join fetch v.olatResource as resource")
		  .append(" where meeting.endWithFollowupTime is not null and meeting.endWithFollowupTime<:date");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AdobeConnectMeeting.class)
				.setParameter("date", date, TemporalType.TIMESTAMP)
				.getResultList();
	}
	
	public List<AdobeConnectMeeting> getMeetings(RepositoryEntryRef entry, String subIdent) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select meeting from adobeconnectmeeting as meeting")
		  .append(" inner join fetch meeting.entry as v")
		  .append(" inner join fetch v.olatResource as resource")
		  .append(" where v.key=:entryKey");
		if(StringHelper.containsNonWhitespace(subIdent)) {
			sb.append(" and meeting.subIdent=:subIdent");
		}
		
		TypedQuery<AdobeConnectMeeting> query= dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AdobeConnectMeeting.class)
				.setParameter("entryKey", entry.getKey());
		if(StringHelper.containsNonWhitespace(subIdent)) {
			query.setParameter("subIdent", subIdent);
		}
		return query.getResultList();
	}
	
	public boolean hasMeetings(RepositoryEntryRef entry, String subIdent) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select meeting.key from adobeconnectmeeting as meeting")
		  .append(" inner join meeting.entry as v")
		  .append(" where v.key=:entryKey");
		if(StringHelper.containsNonWhitespace(subIdent)) {
			sb.append(" and meeting.subIdent=:subIdent");
		}
		
		TypedQuery<Long> query= dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("entryKey", entry.getKey());
		if(StringHelper.containsNonWhitespace(subIdent)) {
			query.setParameter("subIdent", subIdent);
		}
		List<Long> keys = query.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return keys != null && !keys.isEmpty() && keys.get(0) != null && keys.get(0).longValue() >= 0;
	}
	
	public List<AdobeConnectMeeting> getMeetings(BusinessGroupRef businessGroup) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select meeting from adobeconnectmeeting as meeting")
		  .append(" inner join fetch meeting.businessGroup as businessGroup")
		  .append(" where businessGroup.key=:groupKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AdobeConnectMeeting.class)
				.setParameter("groupKey", businessGroup.getKey())
				.getResultList();
	}
	
	public boolean hasMeetings(BusinessGroupRef businessGroup) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select meeting.key from adobeconnectmeeting as meeting")
		  .append(" where meeting.businessGroup.key=:groupKey");
		
		List<Long> keys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("groupKey", businessGroup.getKey())
				.getResultList();
		return keys != null && !keys.isEmpty() && keys.get(0) != null && keys.get(0).longValue() >= 0;
	}
	
	public List<AdobeConnectMeeting> getAllMeetings() {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select meeting from adobeconnectmeeting as meeting")
		  .append(" left join fetch meeting.entry as v")
		  .append(" left join fetch meeting.businessGroup as businessGroup");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AdobeConnectMeeting.class)
				.getResultList();
	}
	
	public void deleteMeeting(AdobeConnectMeeting meeting) {
		dbInstance.getCurrentEntityManager().remove(meeting);
	}

}
