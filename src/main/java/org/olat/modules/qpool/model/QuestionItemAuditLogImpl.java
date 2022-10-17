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
package org.olat.modules.qpool.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.core.id.Persistable;
import org.olat.modules.qpool.QuestionItemAuditLog;

/**
 * 
 * Initial date: 21.01.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="qitemauditlog")
@Table(name="o_qp_item_audit_log")
public class QuestionItemAuditLogImpl implements QuestionItemAuditLog, Persistable {

	private static final long serialVersionUID = 4527466872257325171L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;

	@Column(name="q_action", nullable=true, insertable=true, updatable=false)
	private String action;	
	@Column(name="q_val_before", nullable=true, insertable=true, updatable=false)
	private String before;	
	@Column(name="q_val_after", nullable=true, insertable=true, updatable=false)
	private String after;
	@Column(name="q_lic_before", nullable=true, insertable=true, updatable=false)
	private String licenseBefore;	
	@Column(name="q_lic_after", nullable=true, insertable=true, updatable=false)
	private String licenseAfter;
	@Column(name="q_message", nullable=true, insertable=true, updatable=false)
	private String message;
	
	@Column(name="fk_item_id", nullable=true, insertable=true, updatable=false)
	private Long questionItemKey;

	@Column(name="fk_author_id", nullable=true, insertable=true, updatable=false)
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
	public String getLicenseBefore() {
		return licenseBefore;
	}

	public void setLicenseBefore(String licenseBefore) {
		this.licenseBefore = licenseBefore;
	}

	@Override
	public String getLicenseAfter() {
		return licenseAfter;
	}

	public void setLicenseAfter(String licenseAfter) {
		this.licenseAfter = licenseAfter;
	}

	@Override
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public Long getQuestionItemKey() {
		return questionItemKey;
	}

	public void setQuestionItemKey(Long questionItemKey) {
		this.questionItemKey = questionItemKey;
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
		return key == null ? 328435 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof QuestionItemAuditLogImpl) {
			QuestionItemAuditLogImpl auditLog = (QuestionItemAuditLogImpl)obj;
			return key != null && key.equals(auditLog.getKey());
		}
		return super.equals(obj);
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

}