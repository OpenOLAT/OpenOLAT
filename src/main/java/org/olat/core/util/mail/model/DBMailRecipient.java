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

package org.olat.core.util.mail.model;

import java.util.Date;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;

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

/**
 * Initial Date:  24 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Entity
@Table(name="o_mail_recipient")
public class DBMailRecipient implements Persistable, CreateInfo {

	private static final long serialVersionUID = -6793421633217512631L;
	
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
	@Column(name="recipient_id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;

	@Column(name="email", nullable=true, insertable=true, updatable=true)
	private String emailAddress;
	@Column(name="deleted", nullable=true, insertable=true, updatable=true)
	private Boolean deleted;
	@Column(name="recipientvisible", nullable=true, insertable=true, updatable=true)
	private Boolean visible;
	@Column(name="mailread", nullable=true, insertable=true, updatable=true)
	private Boolean read;
	@Column(name="mailmarked", nullable=true, insertable=true, updatable=true)
	private Boolean marked;
	@Column(name="recipientgroup", nullable=true, insertable=true, updatable=true)
	private String group;
	
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_recipient_id", nullable=true, insertable=true, updatable=true)
	private Identity recipient;
	
	public DBMailRecipient() {
		//make Hibernate happy
	}
	
	public DBMailRecipient(Identity recipient) {
		this.recipient = recipient;
	}
	
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

	public Identity getRecipient() {
		return recipient;
	}

	public void setRecipient(Identity recipient) {
		this.recipient = recipient;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public Boolean getVisible() {
		return visible;
	}

	public void setVisible(Boolean visible) {
		this.visible = visible;
	}

	public Boolean getRead() {
		return read;
	}

	public void setRead(Boolean read) {
		this.read = read;
	}

	public Boolean getMarked() {
		return marked;
	}

	public void setMarked(Boolean marked) {
		this.marked = marked;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? 2981 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof DBMailRecipient mailRecipient) {
			return getKey() != null && getKey().equals(mailRecipient.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
