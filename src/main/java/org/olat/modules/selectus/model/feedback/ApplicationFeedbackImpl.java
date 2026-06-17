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
package org.olat.modules.selectus.model.feedback;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.ApplicationImpl;
import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.ReferenceStatus;

/**
 * 
 * Initial date: 24 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="rappfeedback")
@Table(name="o_selectus_app_feedback")
public class ApplicationFeedbackImpl implements ApplicationFeedback, Persistable  {

	private static final long serialVersionUID = -2177105489965505163L;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "enhanced-sequence", parameters={
		@Parameter(name="sequence_name", value="hibernate_unique_key"),
		@Parameter(name="force_table_use", value="true"),
		@Parameter(name="optimizer", value="legacy-hilo"),
		@Parameter(name="value_column", value="next_hi"),
		@Parameter(name="increment_size", value="32767"),
		@Parameter(name="initial_value", value="32767")
	})
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key = null;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	protected Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="last_modified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	
	@Column(name="r_comment", nullable=true, insertable=true, updatable=true)
	private String comment;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="r_comment_date", nullable=true, insertable=true, updatable=true)
	private Date commentDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="r_deadline", nullable=true, insertable=true, updatable=true)
	private Date deadline;
	@Column(name="r_status", nullable=true, unique=false, insertable=true, updatable=true)
	private String status;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="r_request_date", nullable=true, insertable=true, updatable=true)
	private Date request;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="r_last_reminder_date", nullable=true, insertable=true, updatable=true)
	private Date lastReminder;
	
	@ManyToOne(targetEntity=ApplicationsFeedbackConfigurationImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_config_id", nullable=false, insertable=true, updatable=false)
	private ApplicationsFeedbackConfiguration configuration;
	
	@ManyToOne(targetEntity=ApplicationImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_application_id", nullable=false, insertable=true, updatable=false)
	private Application application;
	
	@ManyToOne(targetEntity=IdentityImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_identity_id", nullable=false, insertable=true, updatable=false)
	private Identity identity;

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
	public String getComment() {
		return comment;
	}

	@Override
	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public Date getCommentDate() {
		return commentDate;
	}

	@Override
	public void setCommentDate(Date commentDate) {
		this.commentDate = commentDate;
	}

	@Override
	public Date getDeadline() {
		return deadline;
	}

	@Override
	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	@Transient
	@Override
	public ReferenceStatus getReferenceStatus() {
		return StringHelper.containsNonWhitespace(status) ? ReferenceStatus.valueOf(status) : null;
	}
	
	@Override
	public void setReferenceStatus(ReferenceStatus referenceStatus) {
		if(referenceStatus == null) {
			status = null;
		} else {
			status = referenceStatus.name();
		}
	}

	@Override
	public Date getRequest() {
		return request;
	}

	@Override
	public void setRequest(Date request) {
		this.request = request;
	}

	@Override
	public Date getLastReminder() {
		return lastReminder;
	}

	@Override
	public void setLastReminder(Date lastReminder) {
		this.lastReminder = lastReminder;
	}

	@Override
	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	@Override
	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	public ApplicationsFeedbackConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(ApplicationsFeedbackConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 6562325 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		
		if(obj instanceof ApplicationFeedbackImpl) {
			ApplicationFeedbackImpl feedback = (ApplicationFeedbackImpl)obj;
			return getKey() != null && getKey().equals(feedback.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
