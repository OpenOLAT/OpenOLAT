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

import javax.persistence.TypedQuery;

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
	
	public AdobeConnectMeeting createMeeting(String name, String description, Date start, Date end,
			String scoId, String folderId, String envName,
			RepositoryEntry entry, String subIdent, BusinessGroup businessGroup) {
		AdobeConnectMeetingImpl meeting = new AdobeConnectMeetingImpl();
		meeting.setCreationDate(new Date());
		meeting.setLastModified(meeting.getCreationDate());
		meeting.setName(name);
		meeting.setDescription(description);
		meeting.setStartDate(cleanDate(start));
		meeting.setEndDate(cleanDate(end));
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
		meet.setStartDate(cleanDate(meet.getStartDate()));
		meet.setEndDate(cleanDate(meet.getEndDate()));
		return dbInstance.getCurrentEntityManager().merge(meet);
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
