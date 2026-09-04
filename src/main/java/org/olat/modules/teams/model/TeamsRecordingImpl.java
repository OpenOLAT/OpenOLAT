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
package org.olat.modules.teams.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.model.VFSMetadataImpl;
import org.olat.core.id.Persistable;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.TeamsRecording;
import org.olat.modules.teams.TeamsRecordingStatusEnum;
import org.olat.modules.teams.TeamsRecordingsPublishedRoles;

/**
 * 
 * Initial date: 25 août 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Entity(name="teamsrecording")
@Table(name="o_teams_recording")
public class TeamsRecordingImpl implements TeamsRecording, Persistable {
	
	private static final long serialVersionUID = 28770981837346706L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="t_start_date", nullable=true, insertable=true, updatable=true)
	private Date startDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="t_end_date", nullable=true, insertable=true, updatable=true)
	private Date endDate;

	@Enumerated(EnumType.STRING)
	@Column(name="t_status", nullable=false, insertable=true, updatable=true)
	private TeamsRecordingStatusEnum status;
	@Column(name="t_recording_id", nullable=true, insertable=true, updatable=true)
	private String recordingId;
	@Column(name="t_attempts", nullable=false, insertable=true, updatable=true)
	private int attempts;
	@Column(name="t_permanent", nullable=true, insertable=true, updatable=true)
	private Boolean permanent;
	@Column(name="t_publish_to", nullable=true, insertable=true, updatable=true)
	private String publishTo;
	
	@ManyToOne(targetEntity=VFSMetadataImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_recording_metadata_id", nullable=true, insertable=true, updatable=true)
	private VFSMetadata recordingMetadata;
	
	@ManyToOne(targetEntity=TeamsMeetingImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_meeting_id", nullable=false, insertable=true, updatable=false)
	private TeamsMeeting meeting;
	
	public TeamsRecordingImpl() {
		//
	}
	
	@Override
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public String getRecordingId() {
		return recordingId;
	}

	public void setRecordingId(String recordingId) {
		this.recordingId = recordingId;
	}

	@Override
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	@Override
	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	@Override
	public TeamsRecordingStatusEnum getStatus() {
		return status;
	}

	@Override
	public void setStatus(TeamsRecordingStatusEnum status) {
		this.status = status;
	}

	@Override
	public Boolean getPermanent() {
		return permanent;
	}

	@Override
	public void setPermanent(Boolean permanent) {
		this.permanent = permanent;
	}
	
	@Override
	public TeamsRecordingsPublishedRoles[] getPublishToEnum() {
		return TeamsRecordingsPublishedRoles.toArray(publishTo);
	}

	@Override
	public void setPublishToEnum(TeamsRecordingsPublishedRoles[] publishTo) {
		this.publishTo = TeamsRecordingsPublishedRoles.toString(publishTo);
	}

	public String getPublishTo() {
		return publishTo;
	}

	public void setPublishTo(String publishTo) {
		this.publishTo = publishTo;
	}

	public int getAttempts() {
		return attempts;
	}

	public void setAttempts(int attempts) {
		this.attempts = attempts;
	}

	@Override
	public VFSMetadata getRecordingMetadata() {
		return recordingMetadata;
	}

	@Override
	public void setRecordingMetadata(VFSMetadata recordingMetadata) {
		this.recordingMetadata = recordingMetadata;
	}

	@Override
	public TeamsMeeting getMeeting() {
		return meeting;
	}

	public void setMeeting(TeamsMeeting meeting) {
		this.meeting = meeting;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? -286712 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof TeamsRecordingImpl recording) {
			return getKey() != null && getKey().equals(recording.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

}
