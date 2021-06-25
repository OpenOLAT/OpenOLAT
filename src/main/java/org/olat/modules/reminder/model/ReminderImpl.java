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
package org.olat.modules.reminder.model;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;
import org.olat.modules.reminder.EmailCopy;
import org.olat.modules.reminder.Reminder;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 01.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */

@Entity(name="reminder")
@Table(name="o_rem_reminder")
@NamedQueries({
	@NamedQuery(name="loadReminderByKey", query="select rem from reminder rem left join fetch rem.entry as v where rem.key=:reminderKey")
	
})
public class ReminderImpl implements Reminder, Persistable {

	private static final long serialVersionUID = 2273068238347207165L;

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
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_creator", nullable=false, insertable=true, updatable=false)
	private Identity creator;

	@Column(name="r_description", nullable=false, insertable=true, updatable=true)
	private String description;
	
	@Column(name="r_sendtime", nullable=true, insertable=true, updatable=true)
	private String sendTime;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="r_start", nullable=true, insertable=true, updatable=true)
	private Date startDate;
	@Column(name="r_configuration", nullable=true, insertable=true, updatable=true)
	private String configuration;
	@Column(name="r_email_Subject", nullable=true, insertable=true, updatable=true)
	private String emailSubject;
	@Column(name="r_email_body", nullable=true, insertable=true, updatable=true)
	private String emailBody;
	@Column(name = "r_email_copy", nullable = true, insertable = true, updatable = true)
	private String emailCopyStr;
	private transient Set<EmailCopy> emailCopy;
	@Column(name="r_email_custom_copy", nullable=true, insertable=true, updatable=true)
	private String customEmailCopy;
	
	@ManyToOne(targetEntity=RepositoryEntry.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_entry", nullable=true, insertable=true, updatable=false)
	private RepositoryEntry entry;
	
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
	
	public Identity getCreator() {
		return creator;
	}

	public void setCreator(Identity creator) {
		this.creator = creator;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public String getSendTime() {
		return sendTime;
	}

	public void setSendTime(String sendTime) {
		this.sendTime = sendTime;
	}

	@Override
	public String getConfiguration() {
		return configuration;
	}

	@Override
	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}

	@Override
	public String getEmailSubject() {
		return emailSubject;
	}

	@Override
	public void setEmailSubject(String emailSubject) {
		this.emailSubject = emailSubject;
	}

	@Override
	public String getEmailBody() {
		return emailBody;
	}

	@Override
	public void setEmailBody(String emailBody) {
		this.emailBody = emailBody;
	}

	@Override
	public Set<EmailCopy> getEmailCopy() {
		if (emailCopy == null && StringHelper.containsNonWhitespace(emailCopyStr)) {
			emailCopy = EmailCopy.split(emailCopyStr);
		}
		return emailCopy;
	}

	@Override
	public void setEmailCopy(Set<EmailCopy> emailCopy) {
		this.emailCopy = emailCopy;
		this.emailCopyStr = EmailCopy.join(emailCopy);
	}

	@Override
	public String getCustomEmailCopy() {
		return customEmailCopy;
	}

	@Override
	public void setCustomEmailCopy(String customEmailCopy) {
		this.customEmailCopy = customEmailCopy;
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
		return key == null ? -239575484 : key.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof ReminderImpl) {
			ReminderImpl reminder = (ReminderImpl)obj;
			return getKey() != null && getKey().equals(reminder.getKey());
		}
		return false;
	}
	
	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
