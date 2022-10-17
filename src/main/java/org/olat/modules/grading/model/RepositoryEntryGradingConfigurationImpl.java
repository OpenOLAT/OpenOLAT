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
package org.olat.modules.grading.model;

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
import org.olat.modules.grading.GradingAssessedIdentityVisibility;
import org.olat.modules.grading.GradingNotificationType;
import org.olat.modules.grading.RepositoryEntryGradingConfiguration;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 13 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="gradingconfiguration")
@Table(name="o_grad_configuration")
public class RepositoryEntryGradingConfigurationImpl implements RepositoryEntryGradingConfiguration, Persistable {

	private static final long serialVersionUID = -6540752125923625856L;

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
	
	@Column(name="g_grading_enabled", nullable=true, insertable=true, updatable=true)
	private boolean gradingEnabled;
	
	@Column(name="g_identity_visibility", nullable=true, insertable=true, updatable=true)
	private String identityVisibility;

	@Column(name="g_grading_period", nullable=true, insertable=true, updatable=true)
	private Integer gradingPeriod;
	@Column(name="g_notification_type", nullable=true, insertable=true, updatable=true)
	private String notificationType;
	@Column(name="g_notification_subject", nullable=true, insertable=true, updatable=true)
	private String notificationSubject;
	@Column(name="g_notification_body", nullable=true, insertable=true, updatable=true)
	private String notificationBody;
	
	@Column(name="g_first_reminder", nullable=true, insertable=true, updatable=true)
	private Integer firstReminder;
	@Column(name="g_first_reminder_subject", nullable=true, insertable=true, updatable=true)
	private String firstReminderSubject;
	@Column(name="g_first_reminder_body", nullable=true, insertable=true, updatable=true)
	private String firstReminderBody;
	
	@Column(name="g_second_reminder", nullable=true, insertable=true, updatable=true)
	private Integer secondReminder;
	@Column(name="g_second_reminder_subject", nullable=true, insertable=true, updatable=true)
	private String secondReminderSubject;
	@Column(name="g_second_reminder_body", nullable=true, insertable=true, updatable=true)
	private String secondReminderBody;

	@ManyToOne(targetEntity=RepositoryEntry.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_entry", nullable=false, insertable=true, updatable=false, unique=true)
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
	
	public void setCreationDate(Date date) {
		creationDate = date;
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date date) {
		lastModified = date;
	}

	@Override
	public boolean isGradingEnabled() {
		return gradingEnabled;
	}

	@Override
	public void setGradingEnabled(boolean gradingEnabled) {
		this.gradingEnabled = gradingEnabled;
	}
	
	public String getIdentityVisibility() {
		return identityVisibility;
	}

	public void setIdentityVisibility(String identityVisibility) {
		this.identityVisibility = identityVisibility;
	}

	@Override
	public GradingAssessedIdentityVisibility getIdentityVisibilityEnum() {
		return GradingAssessedIdentityVisibility.valueOf(identityVisibility);
	}

	@Override
	public void setIdentityVisibilityEnum(GradingAssessedIdentityVisibility identityVisibility) {
		this.identityVisibility = identityVisibility.name();
	}

	public String getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(String notificationType) {
		this.notificationType = notificationType;
	}
	
	public GradingNotificationType getNotificationTypeEnum() {
		return GradingNotificationType.valueOf(notificationType);
	}

	public void setNotificationTypeEnum(GradingNotificationType type) {
		this.notificationType = type.name();
	}

	@Override
	public Integer getGradingPeriod() {
		return gradingPeriod;
	}

	@Override
	public void setGradingPeriod(Integer days) {
		this.gradingPeriod = days;
	}

	@Override
	public String getNotificationSubject() {
		return notificationSubject;
	}

	@Override
	public void setNotificationSubject(String subject) {
		this.notificationSubject = subject;
	}

	@Override
	public String getNotificationBody() {
		return notificationBody;
	}

	@Override
	public void setNotificationBody(String body) {
		this.notificationBody = body;
	}

	@Override
	public Integer getFirstReminder() {
		return firstReminder;
	}

	@Override
	public void setFirstReminder(Integer days) {
		this.firstReminder = days;
	}

	@Override
	public String getFirstReminderSubject() {
		return firstReminderSubject;
	}

	@Override
	public void setFirstReminderSubject(String subject) {
		this.firstReminderSubject = subject;
	}

	@Override
	public String getFirstReminderBody() {
		return firstReminderBody;
	}

	@Override
	public void setFirstReminderBody(String body) {
		this.firstReminderBody = body;
	}

	@Override
	public Integer getSecondReminder() {
		return secondReminder;
	}

	@Override
	public void setSecondReminder(Integer days) {
		this.secondReminder = days;
	}

	@Override
	public String getSecondReminderSubject() {
		return secondReminderSubject;
	}

	@Override
	public void setSecondReminderSubject(String subject) {
		this.secondReminderSubject = subject;
	}

	@Override
	public String getSecondReminderBody() {
		return secondReminderBody;
	}

	@Override
	public void setSecondReminderBody(String body) {
		this.secondReminderBody = body;
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
		return getKey() == null ? -236478 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof RepositoryEntryGradingConfigurationImpl) {
			RepositoryEntryGradingConfigurationImpl config = (RepositoryEntryGradingConfigurationImpl)obj;
			return getKey() != null && getKey().equals(config.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
