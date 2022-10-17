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
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.core.id.Persistable;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 3 avr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="lectureentryconfig")
@Table(name="o_lecture_entry_config")
@NamedQuery(name="lectureconfigByRepositoryEntry", query="select config from lectureentryconfig config where config.entry.key=:entryKey")
public class RepositoryEntryLectureConfigurationImpl implements Persistable, RepositoryEntryLectureConfiguration {

	private static final long serialVersionUID = -728141275261361935L;

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

	@Column(name="l_lecture_enabled", nullable=true, insertable=true, updatable=true)
	private boolean lectureEnabled;
	@Column(name="l_override_module_def", nullable=false, insertable=true, updatable=true)
	private boolean overrideModuleDefault;
	@Column(name="l_rollcall_enabled", nullable=true, insertable=true, updatable=true)
	private Boolean rollCallEnabled;
	@Column(name="l_calculate_attendance_rate", nullable=true, insertable=true, updatable=true)
	private Boolean calculateAttendanceRate;
	@Column(name="l_required_attendance_rate", nullable=true, insertable=true, updatable=true)
	private Double requiredAttendanceRate;
	@Column(name="l_sync_calendar_teacher", nullable=true, insertable=true, updatable=true)
	private Boolean teacherCalendarSyncEnabled;
	@Column(name="l_sync_calendar_participant", nullable=true, insertable=true, updatable=true)
	private Boolean participantCalendarSyncEnabled;
	@Column(name="l_sync_calendar_course", nullable=true, insertable=true, updatable=true)
	private Boolean courseCalendarSyncEnabled;
	
	@Column(name="l_assessment_mode", nullable=true, insertable=true, updatable=true)
	private Boolean assessmentModeEnabled;
	@Column(name="l_assessment_mode_lead", nullable=true, insertable=true, updatable=true)
	private Integer assessmentModeLeadTime;
	@Column(name="l_assessment_mode_followup", nullable=true, insertable=true, updatable=true)
	private Integer assessmentModeFollowupTime;
	@Column(name="l_assessment_mode_ips", nullable=true, insertable=true, updatable=true)
	private String assessmentModeAdmissibleIps;
	@Column(name="l_assessment_mode_seb", nullable=true, insertable=true, updatable=true)
	private String assessmentModeSebKeys;
	
	@ManyToOne(targetEntity=RepositoryEntry.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_entry", nullable=false, insertable=true, updatable=false, unique=true)
	private RepositoryEntry entry;

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
	public boolean isOverrideModuleDefault() {
		return overrideModuleDefault;
	}

	@Override
	public void setOverrideModuleDefault(boolean overrideModuleDefault) {
		this.overrideModuleDefault = overrideModuleDefault;
	}

	@Override
	public boolean isLectureEnabled() {
		return lectureEnabled;
	}

	@Override
	public void setLectureEnabled(boolean lectureEnabled) {
		this.lectureEnabled = lectureEnabled;
	}

	@Override
	public Boolean getRollCallEnabled() {
		return rollCallEnabled;
	}

	@Override
	public void setRollCallEnabled(Boolean rollCallEnabled) {
		this.rollCallEnabled = rollCallEnabled;
	}

	@Override
	public Boolean getCalculateAttendanceRate() {
		return calculateAttendanceRate;
	}

	@Override
	public void setCalculateAttendanceRate(Boolean calculateAttendanceRate) {
		this.calculateAttendanceRate = calculateAttendanceRate;
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
	public Boolean getTeacherCalendarSyncEnabled() {
		return teacherCalendarSyncEnabled;
	}

	@Override
	public void setTeacherCalendarSyncEnabled(Boolean teacherCalendarSyncEnabled) {
		this.teacherCalendarSyncEnabled = teacherCalendarSyncEnabled;
	}

	@Override
	public Boolean getParticipantCalendarSyncEnabled() {
		return participantCalendarSyncEnabled;
	}

	@Override
	public void setParticipantCalendarSyncEnabled(Boolean participantCalendarSyncEnabled) {
		this.participantCalendarSyncEnabled = participantCalendarSyncEnabled;
	}

	@Override
	public Boolean getCourseCalendarSyncEnabled() {
		return courseCalendarSyncEnabled;
	}

	@Override
	public void setCourseCalendarSyncEnabled(Boolean courseCalendarSyncEnabled) {
		this.courseCalendarSyncEnabled = courseCalendarSyncEnabled;
	}

	@Override
	public Boolean getAssessmentModeEnabled() {
		return assessmentModeEnabled;
	}

	@Override
	public void setAssessmentModeEnabled(Boolean assessmentModeEnabled) {
		this.assessmentModeEnabled = assessmentModeEnabled;
	}

	@Override
	public Integer getAssessmentModeLeadTime() {
		return assessmentModeLeadTime;
	}

	@Override
	public void setAssessmentModeLeadTime(Integer assessmentModeLeadTime) {
		this.assessmentModeLeadTime = assessmentModeLeadTime;
	}

	@Override
	public Integer getAssessmentModeFollowupTime() {
		return assessmentModeFollowupTime;
	}

	@Override
	public void setAssessmentModeFollowupTime(Integer assessmentModeFollowupTime) {
		this.assessmentModeFollowupTime = assessmentModeFollowupTime;
	}

	@Override
	public String getAssessmentModeAdmissibleIps() {
		return assessmentModeAdmissibleIps;
	}

	@Override
	public void setAssessmentModeAdmissibleIps(String assessmentModeAdmissibleIps) {
		this.assessmentModeAdmissibleIps = assessmentModeAdmissibleIps;
	}

	@Override
	public String getAssessmentModeSebKeys() {
		return assessmentModeSebKeys;
	}

	@Override
	public void setAssessmentModeSebKeys(String assessmentModeSebKeys) {
		this.assessmentModeSebKeys = assessmentModeSebKeys;
	}

	@Override
	public RepositoryEntry getEntry() {
		return entry;
	}

	public void setEntry(RepositoryEntry entry) {
		this.entry = entry;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 7894857 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof RepositoryEntryLectureConfigurationImpl) {
			RepositoryEntryLectureConfigurationImpl config = (RepositoryEntryLectureConfigurationImpl)obj;
			return getKey() != null && getKey().equals(config.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}