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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.modules.lecture.LectureBlock;
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
	@Column(name="l_absence_appeal_date", nullable=true, insertable=true, updatable=true)
	private Date absenceAppealDate;

	@Column(name="l_log", nullable=true, insertable=true, updatable=true)
	private String log;
	
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_identity", nullable=false, insertable=true, updatable=false)
	private Identity identity;
	@ManyToOne(targetEntity=LectureBlockImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_lecture_block", nullable=false, insertable=true, updatable=false)
	private LectureBlock lectureBlock;
	
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

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getAbsenceReason() {
		return absenceReason;
	}

	public void setAbsenceReason(String absenceReason) {
		this.absenceReason = absenceReason;
	}

	public Boolean getAbsenceAuthorized() {
		return absenceAuthorized;
	}

	public void setAbsenceAuthorized(Boolean absenceAuthorized) {
		this.absenceAuthorized = absenceAuthorized;
	}

	public Date getAbsenceAppealDate() {
		return absenceAppealDate;
	}

	public void setAbsenceAppealDate(Date absenceAppealDate) {
		this.absenceAppealDate = absenceAppealDate;
	}

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}

	@Override
	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	@Override
	public LectureBlock getLectureBlock() {
		return lectureBlock;
	}

	public void setLectureBlock(LectureBlock lectureBlock) {
		this.lectureBlock = lectureBlock;
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
