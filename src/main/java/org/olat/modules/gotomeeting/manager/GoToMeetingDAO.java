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
package org.olat.modules.gotomeeting.manager;

import java.util.Date;
import java.util.List;

import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupRef;
import org.olat.modules.gotomeeting.GoToMeeting;
import org.olat.modules.gotomeeting.GoToOrganizer;
import org.olat.modules.gotomeeting.model.GoToMeetingImpl;
import org.olat.modules.gotomeeting.model.GoToType;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 21.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class GoToMeetingDAO {
	
	@Autowired
	private DB dbInstance;
	
	public GoToMeeting createTraining(String name, String externalId, String description,
			String meetingKey, Date start, Date end, GoToOrganizer organizer,
			RepositoryEntry resourceOwner, String subIdentifier, BusinessGroup businessGroup) {
		GoToMeetingImpl meeting = new GoToMeetingImpl();
		meeting.setCreationDate(new Date());
		meeting.setLastModified(meeting.getCreationDate());
		meeting.setGoToType(GoToType.training);
		meeting.setName(name);
		meeting.setExternalId(externalId);
		meeting.setDescription(description);
		meeting.setStartDate(start);
		meeting.setEndDate(end);
		meeting.setMeetingKey(meetingKey);
		meeting.setOrganizer(organizer);
		meeting.setEntry(resourceOwner);
		meeting.setSubIdent(subIdentifier);
		meeting.setBusinessGroup(businessGroup);

		dbInstance.getCurrentEntityManager().persist(meeting);
		return meeting;
	}
	
	public GoToMeeting loadMeetingByKey(Long key) {
		String q = "select meeting from gotomeeting meeting inner join fetch meeting.organizer organizer where meeting.key=:meetingKey";
		
		List<GoToMeeting> meetings = dbInstance.getCurrentEntityManager()
				.createQuery(q, GoToMeeting.class)
				.setParameter("meetingKey", key)
				.getResultList();

		return meetings == null || meetings.isEmpty() ? null : meetings.get(0);
	}
	
	public GoToMeeting loadMeetingByExternalId(String externalId) {
		String q = "select meeting from gotomeeting meeting inner join fetch meeting.organizer organizer where meeting.externalId=:externalId";
		
		List<GoToMeeting> meetings = dbInstance.getCurrentEntityManager()
				.createQuery(q, GoToMeeting.class)
				.setParameter("externalId", externalId)
				.getResultList();

		return meetings == null || meetings.isEmpty() ? null : meetings.get(0);
	}
	
	public int countMeetingsOrganizedBy(GoToOrganizer organizer) {
		String q = "select count(meeting.key) from gotomeeting meeting where meeting.organizer.key=:organizerKey";

		List<Number> counts = dbInstance.getCurrentEntityManager()
				.createQuery(q, Number.class)
				.setParameter("organizerKey", organizer.getKey())
				.getResultList();
		return counts == null || counts.isEmpty() || counts.get(0) == null ? 0 : counts.get(0).intValue();
	}
	
	public List<GoToMeeting> getMeetings() {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select meeting from gotomeeting meeting")
		  .append(" inner join fetch meeting.organizer organizer")
		  .append(" left join fetch meeting.entry entry")
		  .append(" left join fetch meeting.businessGroup bGroup");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), GoToMeeting.class)
				.getResultList();
	}
	
	public List<GoToMeeting> getMeetings(GoToType type, RepositoryEntryRef entry, String subIdent, BusinessGroupRef businessGroup) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select meeting from gotomeeting meeting inner join fetch meeting.organizer organizer where meeting.type=:type");
		if(entry != null) {
			sb.append(" and meeting.entry.key=:entryKey");
		}
		if(StringHelper.containsNonWhitespace(subIdent)) {
			sb.append(" and meeting.subIdent=:subIdent");
		}
		if(businessGroup != null) {
			sb.append(" and meeting.businessGroup.key=:groupKey");
		}
		
		TypedQuery<GoToMeeting> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), GoToMeeting.class)
				.setParameter("type", type.name());
		if(entry != null) {
			query.setParameter("entryKey", entry.getKey());
		}
		if(StringHelper.containsNonWhitespace(subIdent)) {
			query.setParameter("subIdent", subIdent);
		}
		if(businessGroup != null) {
			query.setParameter("groupKey", businessGroup.getKey());
		}
		return query.getResultList();
	}
	
	public List<GoToMeeting> getMeetingsOverlap(GoToType type, GoToOrganizer organizer, Date start, Date end) {
		StringBuilder sb = new StringBuilder();
		sb.append("select meeting from gotomeeting meeting")
		  .append(" inner join meeting.organizer organizer on organizer.key=:organizerKey")
		  .append(" where meeting.type=:type")
		  .append(" and ((meeting.startDate<=:start and meeting.endDate>=:start)")
		  .append(" or (meeting.startDate<=:end and meeting.endDate>=:end)")
		  .append(" or (meeting.startDate>=:start and meeting.endDate<=:end))");

		List<GoToMeeting> meetings = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), GoToMeeting.class)
			.setParameter("organizerKey", organizer.getKey())
			.setParameter("type", type.name())
			.setParameter("start", start, TemporalType.TIMESTAMP)
			.setParameter("end", end, TemporalType.TIMESTAMP)
			.getResultList();
		return meetings;
	}
	
	public GoToMeeting update(GoToMeeting meeting) {
		((GoToMeetingImpl)meeting).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(meeting);
	}
	
	public int delete(GoToMeeting meeting) {
		//delete registrants
		String d1 = "delete from gotoregistrant as registrant where registrant.meeting.key=:meetingKey";
		int deletedRows = dbInstance.getCurrentEntityManager()
				.createQuery(d1)
				.setParameter("meetingKey", meeting.getKey())
				.executeUpdate();

		String d2 = "delete from gotomeeting as meeting where meeting.key=:meetingKey";
		deletedRows += dbInstance.getCurrentEntityManager()
				.createQuery(d2)
				.setParameter("meetingKey", meeting.getKey())
				.executeUpdate();
		return deletedRows;
	}
}
