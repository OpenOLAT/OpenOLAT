/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.teams.manager;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.util.DateUtils;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.TeamsRecording;
import org.olat.modules.teams.TeamsRecordingStatusEnum;
import org.olat.modules.teams.model.TeamsRecordingImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.microsoft.graph.models.CallRecording;

/**
 * 
 * Initial date: 25 août 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class TeamsRecordingDAO {
	
	@Autowired
	private DB dbInstance;
	
	public TeamsRecording createRecording(TeamsMeeting meeting, CallRecording callRecording) {
		TeamsRecordingImpl recording = new TeamsRecordingImpl();
		recording.setCreationDate(new Date());
		recording.setLastModified(recording.getCreationDate());
		recording.setStatus(TeamsRecordingStatusEnum.PENDING);
		OffsetDateTime startDate = callRecording.getCreatedDateTime();
		recording.setStartDate(DateUtils.toDate(startDate));
		OffsetDateTime endDate = callRecording.getEndDateTime();
		recording.setEndDate(DateUtils.toDate(endDate));
		recording.setRecordingId(callRecording.getId());
		recording.setMeeting(meeting);
		recording.setPublishToEnum(meeting.getRecordingsPublishingEnum());
		recording.setPermanent(Boolean.FALSE);
		dbInstance.getCurrentEntityManager().persist(recording);
		return recording;
	}
	
	public List<TeamsRecording> getRecordings(TeamsMeeting meeting) {
		String query = """
				select rec from teamsrecording as rec
				left join fetch rec.recordingMetadata as metadata
				where rec.meeting.key=:meetingKey""";
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, TeamsRecording.class)
				.setParameter("meetingKey", meeting.getKey())
				.getResultList();
	}
	
	public TeamsRecording loadRecordingByKey(Long recordingKey) {
		String query = """
				select rec from teamsrecording as rec
				left join fetch rec.recordingMetadata as metadata
				where rec.key=:recordingKey""";
		
		List<TeamsRecording> recordings = dbInstance.getCurrentEntityManager()
				.createQuery(query, TeamsRecording.class)
				.setParameter("recordingKey", recordingKey)
				.getResultList();
		return recordings == null || recordings.isEmpty() ? null : recordings.get(0);
	}
	
	public List<Long> getRecordingsToDelete(LocalDateTime referenceDate) {
		String query = """
				select rec.key from teamsrecording as rec
				where rec.endDate<:referenceDate
				and (rec.permanent is null or rec.permanent=false)
				and rec.status in (:status)""";
		
		List<TeamsRecordingStatusEnum> statusList = List.of(TeamsRecordingStatusEnum.AVAILABLE,
				TeamsRecordingStatusEnum.ERROR, TeamsRecordingStatusEnum.PENDING);
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, Long.class)
				.setParameter("referenceDate", referenceDate)
				.setParameter("status", statusList)
				.getResultList();
	}
	
	public TeamsRecording updateRecording(TeamsRecording recording) {
		recording.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(recording);
	}

	public void deleteRecording(TeamsRecording recording) {
		dbInstance.getCurrentEntityManager().remove(recording);
	}
}
