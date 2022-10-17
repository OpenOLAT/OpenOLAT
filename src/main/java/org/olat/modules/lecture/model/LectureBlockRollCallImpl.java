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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
import jakarta.persistence.Transient;

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;
import org.olat.modules.lecture.AbsenceCategory;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockAppealStatus;
import org.olat.modules.lecture.LectureBlockRollCall;

/**
 * 
 * Initial date: 20 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="lectureblockrollcall")
@Table(name="o_lecture_block_roll_call")
public class LectureBlockRollCallImpl implements Persistable, LectureBlockRollCall {

	private static final long serialVersionUID = -2730191013161730807L;

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

	@Column(name="l_lectures_attended", nullable=true, insertable=true, updatable=true)
	private String lecturesAttended;
	@Column(name="l_lectures_absent", nullable=true, insertable=true, updatable=true)
	private String lecturesAbsent;
	@Column(name="l_lectures_attended_num", nullable=true, insertable=true, updatable=true)
	private int lecturesAttendedNumber;
	@Column(name="l_lectures_absent_num", nullable=true, insertable=true, updatable=true)
	private int lecturesAbsentNumber;
	
	@Column(name="l_comment", nullable=true, insertable=true, updatable=true)
	private String comment;

	@Column(name="l_absence_reason", nullable=true, insertable=true, updatable=true)
	private String absenceReason;
	@Column(name="l_absence_authorized", nullable=true, insertable=true, updatable=true)
	private Boolean absenceAuthorized;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="l_absence_supervisor_noti_date", nullable=true, insertable=true, updatable=true)
	private Date absenceSupervisorNotificationDate;
	
	@Column(name="l_absence_notice_lectures", nullable=true, insertable=true, updatable=true)
	private String absenceNoticeLectures;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="l_absence_appeal_date", nullable=true, insertable=true, updatable=true)
	private Date appealDate;
	@Column(name="l_appeal_reason", nullable=true, insertable=true, updatable=true)
	private String appealReason;
	@Column(name="l_appeal_status", nullable=true, insertable=true, updatable=true)
	private String appealStatusString;
	@Column(name="l_appeal_status_reason", nullable=true, insertable=true, updatable=true)
	private String statusReason;
	
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_identity", nullable=false, insertable=true, updatable=false)
	private Identity identity;
	@ManyToOne(targetEntity=LectureBlockImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_lecture_block", nullable=false, insertable=true, updatable=false)
	private LectureBlock lectureBlock;
	@ManyToOne(targetEntity=AbsenceCategoryImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_absence_category", nullable=true, insertable=true, updatable=true)
	private AbsenceCategory absenceCategory;
	
	@ManyToOne(targetEntity=AbsenceNoticeImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_absence_notice", nullable=true, insertable=true, updatable=false)
	private AbsenceNotice absenceNotice;
	
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
	public String getComment() {
		return comment;
	}

	@Override
	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public String getAbsenceReason() {
		return absenceReason;
	}

	@Override
	public void setAbsenceReason(String absenceReason) {
		this.absenceReason = absenceReason;
	}

	@Override
	public Boolean getAbsenceAuthorized() {
		return absenceAuthorized;
	}

	@Override
	public void setAbsenceAuthorized(Boolean absenceAuthorized) {
		this.absenceAuthorized = absenceAuthorized;
	}

	@Override
	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}
	
	@Transient
	public List<Integer> getLecturesAttendedList() {
		return deserialize(getLecturesAttended());
	}
	
	@Transient
	public void setLecturesAttendedList(List<Integer> lectures) {
		setLecturesAttended(serialize(lectures));
	}

	public String getLecturesAttended() {
		return lecturesAttended;
	}

	public void setLecturesAttended(String lecturesAttended) {
		this.lecturesAttended = lecturesAttended;
	}

	public String getLecturesAbsent() {
		return lecturesAbsent;
	}

	public void setLecturesAbsent(String lecturesAbsent) {
		this.lecturesAbsent = lecturesAbsent;
	}

	@Override
	@Transient
	public List<Integer> getLecturesAbsentList() {
		return deserialize(getLecturesAbsent());
	}

	@Transient
	public void setLecturesAbsentList(List<Integer> lecturesAbsent) {
		setLecturesAbsent(serialize(lecturesAbsent));
	}
	
	private String serialize(List<Integer> lectures) {
		StringBuilder sb = new StringBuilder();
		if(lectures != null && !lectures.isEmpty()) {
			if(lectures.size() > 1) {
				Collections.sort(lectures);
			}
			for(Integer lecture:lectures) {
				if(sb.length() > 0) sb.append("|");
				sb.append(lecture.intValue());
			}
		}
		return sb.toString();
	}
	
	private List<Integer> deserialize(String string) {
		List<Integer> list = new ArrayList<>();
		if(StringHelper.containsNonWhitespace(string)) {
			String[] currentAttendeeArr = string.split("[|]");
			for(String current:currentAttendeeArr) {
				list.add(Integer.valueOf(current));
			}
		}
		return list;
	}

	@Override
	public int getLecturesAttendedNumber() {
		return lecturesAttendedNumber;
	}

	public void setLecturesAttendedNumber(int lecturesAttendedNumber) {
		this.lecturesAttendedNumber = lecturesAttendedNumber;
	}

	@Override
	public int getLecturesAbsentNumber() {
		return lecturesAbsentNumber;
	}

	public void setLecturesAbsentNumber(int lecturesAbsentNumber) {
		this.lecturesAbsentNumber = lecturesAbsentNumber;
	}

	@Override
	public Date getAbsenceSupervisorNotificationDate() {
		return absenceSupervisorNotificationDate;
	}

	@Override
	public void setAbsenceSupervisorNotificationDate(Date absenceSupervisorNotificationDate) {
		this.absenceSupervisorNotificationDate = absenceSupervisorNotificationDate;
	}
	
	@Override
	public Date getAppealDate() {
		return appealDate;
	}

	@Override
	public void setAppealDate(Date appealDate) {
		this.appealDate = appealDate;
	}
	
	public String getAppealStatusString() {
		return appealStatusString;
	}

	public void setAppealStatusString(String statusString) {
		this.appealStatusString = statusString;
	}

	@Override
	public LectureBlockAppealStatus getAppealStatus() {
		return StringHelper.containsNonWhitespace(appealStatusString)
				? LectureBlockAppealStatus.valueOf(appealStatusString) : null;
	}

	@Override
	public void setAppealStatus(LectureBlockAppealStatus appealStatus) {
		if(appealStatus == null) {
			appealStatusString = null;
		} else {
			appealStatusString = appealStatus.name();
		}
	}

	@Override
	public String getAppealReason() {
		return appealReason;
	}

	@Override
	public void setAppealReason(String appealReason) {
		this.appealReason = appealReason;
	}

	@Override
	public String getAppealStatusReason() {
		return statusReason;
	}

	@Override
	public void setAppealStatusReason(String statusReason) {
		this.statusReason = statusReason;
	}

	@Override
	public LectureBlock getLectureBlock() {
		return lectureBlock;
	}

	public void setLectureBlock(LectureBlock lectureBlock) {
		this.lectureBlock = lectureBlock;
	}

	@Override
	public AbsenceCategory getAbsenceCategory() {
		return absenceCategory;
	}

	@Override
	public void setAbsenceCategory(AbsenceCategory absenceCategory) {
		this.absenceCategory = absenceCategory;
	}

	public String getAbsenceNoticeLectures() {
		return absenceNoticeLectures;
	}

	public void setAbsenceNoticeLectures(String absenceNoticeLectures) {
		this.absenceNoticeLectures = absenceNoticeLectures;
	}

	@Override
	public AbsenceNotice getAbsenceNotice() {
		return absenceNotice;
	}

	/**
	 * This property is insert only. Update happens through a specialized method
	 * to prevent overwriting the field by concurrently saving the roll call (concurrent
	 * absence management and roll call).
	 * 
	 * @param absenceNotice The absence notice
	 */
	public void setAbsenceNotice(AbsenceNotice absenceNotice) {
		this.absenceNotice = absenceNotice;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? -987967346 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if(obj instanceof LectureBlockRollCallImpl) {
			LectureBlockRollCallImpl rollCall = (LectureBlockRollCallImpl)obj;
			return getKey() != null && getKey().equals(rollCall.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
