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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.DB;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonRecording;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingReference;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingsPublishedRoles;
import org.olat.modules.bigbluebutton.model.BigBlueButtonRecordingReferenceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 7 août 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class BigBlueButtonRecordingReferenceDAO {
	
	@Autowired
	private DB dbInstance;
	
	public BigBlueButtonRecordingReference createReference(BigBlueButtonRecording recording, BigBlueButtonMeeting meeting,
			BigBlueButtonRecordingsPublishedRoles[] publishTo) {
		BigBlueButtonRecordingReferenceImpl ref = new BigBlueButtonRecordingReferenceImpl();
		ref.setCreationDate(new Date());
		ref.setLastModified(ref.getCreationDate());
		ref.setRecordingId(recording.getRecordId());
		ref.setStartDate(recording.getStart());
		ref.setEndDate(recording.getEnd());
		ref.setType(recording.getType());
		ref.setUrl(recording.getUrl());
		ref.setPublishToEnum(publishTo);
		ref.setMeeting(meeting);
		dbInstance.getCurrentEntityManager().persist(ref);
		return ref;
	}
	
	public BigBlueButtonRecordingReference updateRecordingReference(BigBlueButtonRecordingReference reference) {
		((BigBlueButtonRecordingReferenceImpl)reference).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(reference);
	}
	
	public List<BigBlueButtonRecordingReference> getRecordingReferences(BigBlueButtonMeeting meeting) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select record from bigbluebuttonrecording as record")
		  .append(" where record.meeting.key=:meetingKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BigBlueButtonRecordingReference.class)
				.setParameter("meetingKey", meeting.getKey())
				.getResultList();
	}
	
	public BigBlueButtonRecordingReference loadRecordingReferenceByKey(Long referenceKey) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select record from bigbluebuttonrecording as record")
		  .append(" left join fetch record.meeting as meeting")
		  .append(" left join fetch meeting.server as server")
		  .append(" where record.key=:referenceKey");
		
		List<BigBlueButtonRecordingReference> refs = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BigBlueButtonRecordingReference.class)
				.setParameter("referenceKey", referenceKey)
				.getResultList();
		return refs == null || refs.isEmpty() ? null : refs.get(0);
	}
	
	public List<BigBlueButtonRecordingReference> getRecordingReferences(Collection<BigBlueButtonMeeting> meetings) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select record from bigbluebuttonrecording as record")
		  .append(" inner join fetch record.meeting as meeting")
		  .append(" left join fetch meeting.server as server")
		  .append(" where meeting.key in (:meetingKeys)");
		
		List<Long> meetingKeys = meetings.stream().map(BigBlueButtonMeeting::getKey).collect(Collectors.toList());
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BigBlueButtonRecordingReference.class)
				.setParameter("meetingKeys", meetingKeys)
				.getResultList();
	}
	
	public int deleteRecordingReferences(BigBlueButtonMeeting meeting) {
		String query = "delete from bigbluebuttonrecording as record where record.meeting.key=:meetingKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("meetingKey", meeting.getKey())
				.executeUpdate();
	}

}
