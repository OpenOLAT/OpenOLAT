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
package org.olat.modules.fo.model;

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

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.modules.fo.MessageLight;
import org.olat.modules.fo.MessageRef;

/**
 * 
 * A mapping which reduce the number of join and associated sub-queries.
 * 
 * Initial date: 12.11.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="folightmessage")
@Table(name="o_message")
public class MessageLightImpl implements MessageLight, CreateInfo, Persistable, Comparable<MessageLight> {

	private static final long serialVersionUID = -7701717903560643010L;

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
	@Column(name="message_id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=false, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=false, updatable=false)
	private Date lastModified;

	@Column(name="statuscode", nullable=false, insertable=false, updatable=false)
	private int statusCode;
	
	@Column(name="title", nullable=false, insertable=false, updatable=false)
	private String title;
	@Column(name="body", nullable=false, insertable=false, updatable=false)
	private String body;
	
	@Column(name="modification_date", nullable=true, insertable=true, updatable=true)
	private Date modificationDate;

	@Column(name="parent_id", nullable=true, insertable=false, updatable=false)
	private Long parentKey;
	@ManyToOne(targetEntity=MessageRefImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="topthread_id", nullable=true, insertable=false, updatable=false)
	private MessageRef threadtop;
	@Column(name="forum_fk", nullable=false, insertable=false, updatable=false)
	private Long forumKey;

	@Column(name="pseudonym", nullable=true, insertable=false, updatable=false)
	private String pseudonym;
	@Column(name="guest", nullable=true, insertable=false, updatable=false)
	private boolean guest;
	
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="creator_id", nullable=true, insertable=true, updatable=false)
	private Identity creator;
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="modifier_id", nullable=true, insertable=true, updatable=true)
	private Identity modifier;

	@Override
	public Long getKey() {
		return key;
	}
	
	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public int getStatusCode() {
		return statusCode;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getBody() {
		return body;
	}

	@Override
	public Identity getCreator() {
		return creator;
	}

	@Override
	public String getPseudonym() {
		return pseudonym;
	}

	@Override
	public boolean isGuest() {
		return guest;
	}

	public Long getForumKey() {
		return forumKey;
	}

	@Override
	public Identity getModifier() {
		return modifier;
	}

	@Override
	public Long getParentKey() {
		return parentKey;
	}

	@Override
	public MessageRef getThreadtop() {
		return threadtop;
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}
	
	@Override
	public Date getModificationDate() {
		return modificationDate;
	}

	@Override
	public int hashCode() {
		return key == null ? 835245 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof MessageImpl) {
			MessageImpl other = (MessageImpl) obj;
			return getKey().equals(other.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

	@Override
	public int compareTo(MessageLight arg0) {
		if (arg0.getParentKey() == null) {
			return 1;
		}
		if (getCreationDate().after(arg0.getCreationDate())) {
			return 1;
		}
		return 0;
	}
}
