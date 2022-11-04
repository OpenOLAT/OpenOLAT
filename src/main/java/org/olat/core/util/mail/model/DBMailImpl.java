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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.Persistable;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * 
 * Description:<br>
 * 
 * 
 * <P>
 * Initial Date:  28 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Entity
@Table(name="o_mail")
public class DBMailImpl implements Persistable, DBMail {

	private static final long serialVersionUID = 6407865711769961684L;
	
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
	@Column(name="mail_id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;

	@Column(name="subject", nullable=true, insertable=true, updatable=true)
	private String subject;
	@Column(name="body", nullable=true, insertable=true, updatable=true)
	private String body;
	@Column(name="meta_mail_id", nullable=true, insertable=true, updatable=true)
	private String metaId;
	
	@ManyToOne(targetEntity=DBMailRecipient.class,fetch=FetchType.LAZY,optional=true,cascade= { CascadeType.ALL })
	@JoinColumn(name="fk_from_id", nullable=true, insertable=true, updatable=true)
	private DBMailRecipient from;
	
	@ManyToMany(cascade = { CascadeType.ALL })
    @JoinTable(name = "o_mail_to_recipient", joinColumns = { @JoinColumn(name = "fk_mail_id") },
    		inverseJoinColumns = { @JoinColumn(name = "fk_recipient_id") })
	private List<DBMailRecipient> recipients;
	
	@Embedded
    @AttributeOverride(name="resName", column = @Column(name="resname"))
    @AttributeOverride(name="resId", column = @Column(name="resid"))
    @AttributeOverride(name="resSubPath", column = @Column(name="ressubpath"))
    @AttributeOverride(name="businessPath", column = @Column(name="businesspath"))
	private DBMailContext context;
	
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
	public DBMailRecipient getFrom() {
		return from;
	}

	public void setFrom(DBMailRecipient from) {
		this.from = from;
	}
	
	@Override
	public String getMetaId() {
		return metaId;
	}

	public void setMetaId(String metaId) {
		this.metaId = metaId;
	}

	@Override
	public String getSubject() {
		return subject;
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String getBody() {
		return body;
	}
	
	public void setBody(String body) {
		this.body = body;
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
	public List<DBMailRecipient> getRecipients() {
		if(recipients == null) {
			recipients = new ArrayList<>();
		}
		return recipients;
	}

	public void setRecipients(List<DBMailRecipient> recipients) {
		this.recipients = recipients;
	}

	@Override
	public DBMailContext getContext() {
		if(context == null) {
			context = new DBMailContext();
		}
		return context;
	}

	public void setContext(DBMailContext context) {
		this.context = context;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 2991 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof DBMailImpl mail) {
			return getKey() != null && getKey().equals(mail.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
