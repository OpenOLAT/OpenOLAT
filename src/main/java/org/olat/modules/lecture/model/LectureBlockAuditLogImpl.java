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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.olat.core.id.Persistable;
import org.olat.modules.lecture.LectureBlockAuditLog;

/**
 * 
 * 
 * Initial date: 11 juil. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="lectureblockauditlog")
@Table(name="o_lecture_block_audit_log")
public class LectureBlockAuditLogImpl implements LectureBlockAuditLog, Persistable {

	private static final long serialVersionUID = -1009831288341614553L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;

	@Column(name="l_action", nullable=true, insertable=true, updatable=false)
	private String action;	
	@Column(name="l_val_before", nullable=true, insertable=true, updatable=false)
	private String before;	
	@Column(name="l_val_after", nullable=true, insertable=true, updatable=false)
	private String after;
	@Column(name="l_message", nullable=true, insertable=true, updatable=false)
	private String message;
	
	@Column(name="fk_lecture_block", nullable=true, insertable=true, updatable=false)
	private Long lectureBlockKey;
	@Column(name="fk_roll_call", nullable=true, insertable=true, updatable=false)
	private Long rollCallKey;
	@Column(name="fk_absence_notice", nullable=true, insertable=true, updatable=false)
	private Long absenceNoticeKey;

	@Column(name="fk_entry", nullable=true, insertable=true, updatable=false)
	private Long entryKey;
	@Column(name="fk_identity", nullable=true, insertable=true, updatable=false)
	private Long identityKey;
	@Column(name="fk_author", nullable=true, insertable=true, updatable=false)
	private Long authorKey;

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
	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	@Override
	public String getBefore() {
		return before;
	}

	public void setBefore(String before) {
		this.before = before;
	}

	@Override
	public String getAfter() {
		return after;
	}

	public void setAfter(String after) {
		this.after = after;
	}

	@Override
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public Long getLectureBlockKey() {
		return lectureBlockKey;
	}

	public void setLectureBlockKey(Long lectureBlockKey) {
		this.lectureBlockKey = lectureBlockKey;
	}

	@Override
	public Long getRollCallKey() {
		return rollCallKey;
	}

	public void setRollCallKey(Long rollCallKey) {
		this.rollCallKey = rollCallKey;
	}

	@Override
	public Long getEntryKey() {
		return entryKey;
	}

	public void setEntryKey(Long entryKey) {
		this.entryKey = entryKey;
	}

	@Override
	public Long getAbsenceNoticeKey() {
		return absenceNoticeKey;
	}

	public void setAbsenceNoticeKey(Long absenceNoticeKey) {
		this.absenceNoticeKey = absenceNoticeKey;
	}

	@Override
	public Long getIdentityKey() {
		return identityKey;
	}

	public void setIdentityKey(Long identityKey) {
		this.identityKey = identityKey;
	}

	@Override
	public Long getAuthorKey() {
		return authorKey;
	}

	public void setAuthorKey(Long authorKey) {
		this.authorKey = authorKey;
	}

	@Override
	public int hashCode() {
		return key == null ? 236520 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof LectureBlockAuditLogImpl) {
			LectureBlockAuditLogImpl auditLog = (LectureBlockAuditLogImpl)obj;
			return key != null && key.equals(auditLog.getKey());
		}
		return super.equals(obj);
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}