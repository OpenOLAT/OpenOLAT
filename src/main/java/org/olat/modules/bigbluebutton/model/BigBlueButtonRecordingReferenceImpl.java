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
package org.olat.modules.bigbluebutton.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.core.id.Persistable;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingsPublishedRoles;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingReference;

/**
 * 
 * Initial date: 7 août 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="bigbluebuttonrecording")
@Table(name="o_bbb_recording")
public class BigBlueButtonRecordingReferenceImpl implements Persistable, BigBlueButtonRecordingReference {

	private static final long serialVersionUID = -9035038509507193398L;

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
	
	@Column(name="b_recording_id", nullable=false, insertable=true, updatable=false)
	private String recordingId;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="b_start_date", nullable=true, insertable=true, updatable=true)
	private Date startDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="b_end_date", nullable=true, insertable=true, updatable=true)
	private Date endDate;
	@Column(name="b_url", nullable=true, insertable=true, updatable=true)
	private String url;
	@Column(name="b_type", nullable=true, insertable=true, updatable=true)
	private String type;
	
	@Column(name="b_permanent", nullable=true, insertable=true, updatable=true)
	private Boolean permanent;
	
	@Column(name="b_publish_to", nullable=true, insertable=true, updatable=true)
	private String publishTo;
	
	@ManyToOne(targetEntity=BigBlueButtonMeetingImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_meeting_id", nullable=false, insertable=true, updatable=false)
	private BigBlueButtonMeeting meeting;
	
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

	public String getPublishTo() {
		return publishTo;
	}
	
	public void setPublishTo(String publishTo) {
		this.publishTo = publishTo;
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
	public BigBlueButtonRecordingsPublishedRoles[] getPublishToEnum() {
		return BigBlueButtonRecordingsPublishedRoles.toArray(publishTo);
	}

	@Override
	public void setPublishToEnum(BigBlueButtonRecordingsPublishedRoles[] publishTo) {
		this.publishTo = BigBlueButtonRecordingsPublishedRoles.toString(publishTo);
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
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public BigBlueButtonMeeting getMeeting() {
		return meeting;
	}

	public void setMeeting(BigBlueButtonMeeting meeting) {
		this.meeting = meeting;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 964210765 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof BigBlueButtonRecordingReferenceImpl) {
			BigBlueButtonRecordingReferenceImpl reference = (BigBlueButtonRecordingReferenceImpl)obj;
			return getKey() != null && getKey().equals(reference.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
