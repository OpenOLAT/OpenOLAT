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
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Persistable;
import org.olat.core.util.mail.MailAttachment;

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
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  8 sept. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Entity
@Table(name="o_mail_attachment")
public class DBMailAttachment implements Persistable, CreateInfo, MailAttachment {

	private static final long serialVersionUID = -1713863670528439651L;
	
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
	@Column(name="attachment_id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	
	@Column(name="datas_size", nullable=true, insertable=true, updatable=true)
	private Long size;
	@Column(name="datas_name", nullable=true, insertable=true, updatable=true)
	private String name;
	@Column(name="mimetype", nullable=true, insertable=true, updatable=true)
	private String mimetype;
	@Column(name="datas_checksum", nullable=true, insertable=true, updatable=true)
	private Long checksum;
	@Column(name="datas_path", nullable=true, insertable=true, updatable=true)
	private String path;
	@Column(name="datas_lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	
	@ManyToOne(targetEntity=DBMailImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_att_mail_id", nullable=false, insertable=true, updatable=false)
	private DBMail mail;
	
	public DBMailAttachment() {
		//
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

	public DBMail getMail() {
		return mail;
	}

	public void setMail(DBMail mail) {
		this.mail = mail;
	}

	@Override
	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}
	
	public String getCssClass() {
		return CSSHelper.createFiletypeIconCssClassFor(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMimetype() {
		return mimetype;
	}

	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}
	
	public Long getChecksum() {
		return checksum;
	}

	public void setChecksum(Long checksum) {
		this.checksum = checksum;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 2951 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof DBMailAttachment attachment) {
			return getKey() != null && getKey().equals(attachment.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
