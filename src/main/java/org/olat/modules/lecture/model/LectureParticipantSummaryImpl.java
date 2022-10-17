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
package org.olat.modules.lecture.model;

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

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.modules.lecture.LectureParticipantSummary;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 31 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="lectureparticipantsummary")
@Table(name="o_lecture_participant_summary")
public class LectureParticipantSummaryImpl implements Persistable, LectureParticipantSummary {

	private static final long serialVersionUID = -1918497355892493305L;

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

	@Column(name="l_first_admission_date", nullable=true, insertable=true, updatable=true)
	private Date firstAdmissionDate;
	@Column(name="l_required_attendance_rate", nullable=true, insertable=true, updatable=true)
	private Double requiredAttendanceRate;
	
	@Column(name="l_attended_lectures", nullable=false, insertable=true, updatable=true)
	private long attendedLectures;
	@Column(name="l_absent_lectures", nullable=false, insertable=true, updatable=true)
	private long absentLectures;
	@Column(name="l_excused_lectures", nullable=false, insertable=true, updatable=true)
	private long excusedLectures;
	@Column(name="l_planneds_lectures", nullable=false, insertable=true, updatable=true)
	private long plannedLectures;
	@Column(name="l_attendance_rate", nullable=true, insertable=true, updatable=true)
	private Double attendanceRate;

	//these fields are updated per update statement
	@Column(name="l_cal_sync", nullable=false, insertable=true, updatable=false)
	private boolean calendarSync;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="l_cal_last_sync_date", nullable=true, insertable=true, updatable=false)
	private Date calendarLastSyncDate;
	
	@ManyToOne(targetEntity=RepositoryEntry.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_entry", nullable=false, insertable=true, updatable=false)
	private RepositoryEntry entry;
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_identity", nullable=false, insertable=true, updatable=false)
	private Identity identity;
	
	@Override
	public Long getKey() {
		return key;
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
	public Double getRequiredAttendanceRate() {
		return requiredAttendanceRate;
	}

	@Override
	public void setRequiredAttendanceRate(Double requiredAttendanceRate) {
		this.requiredAttendanceRate = requiredAttendanceRate;
	}

	@Override
	public Date getFirstAdmissionDate() {
		return firstAdmissionDate;
	}

	@Override
	public void setFirstAdmissionDate(Date firstAdmissionDate) {
		this.firstAdmissionDate = firstAdmissionDate;
	}

	public long getAttendedLectures() {
		return attendedLectures;
	}

	public void setAttendedLectures(long attendedLectures) {
		this.attendedLectures = attendedLectures;
	}

	public long getAbsentLectures() {
		return absentLectures;
	}

	public void setAbsentLectures(long absentLectures) {
		this.absentLectures = absentLectures;
	}

	public long getExcusedLectures() {
		return excusedLectures;
	}

	public void setExcusedLectures(long excusedLectures) {
		this.excusedLectures = excusedLectures;
	}

	public long getPlannedLectures() {
		return plannedLectures;
	}

	public void setPlannedLectures(long plannedLectures) {
		this.plannedLectures = plannedLectures;
	}
	
	@Override
	public Double getAttendanceRate() {
		return attendanceRate;
	}

	public void setAttendanceRate(Double attendanceRate) {
		this.attendanceRate = attendanceRate;
	}

	public boolean isCalendarSync() {
		return calendarSync;
	}

	public void setCalendarSync(boolean calendarSync) {
		this.calendarSync = calendarSync;
	}

	public Date getCalendarLastSyncDate() {
		return calendarLastSyncDate;
	}

	public void setCalendarLastSyncDate(Date calendarLastSyncDate) {
		this.calendarLastSyncDate = calendarLastSyncDate;
	}

	public RepositoryEntry getEntry() {
		return entry;
	}

	public void setEntry(RepositoryEntry entry) {
		this.entry = entry;
	}

	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? -87894 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if(obj instanceof LectureParticipantSummaryImpl) {
			LectureParticipantSummaryImpl summary = (LectureParticipantSummaryImpl)obj;
			return getKey() != null && getKey().equals(summary.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
