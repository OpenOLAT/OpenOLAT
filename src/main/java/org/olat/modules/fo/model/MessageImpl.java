/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.modules.fo.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.Message;

/**
 * @author Felix Jost
 */
@Entity(name="fomessage")
@Table(name="o_message")
public class MessageImpl implements CreateInfo, Persistable, Message {

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
	@Version
	private int version = 0;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;

	@Column(name="statuscode", nullable=false, insertable=true, updatable=true)
	private int statusCode;
	
	@Column(name="title", nullable=false, insertable=true, updatable=true)
	private String title;
	@Column(name="body", nullable=false, insertable=true, updatable=true)
	private String body;
	@Column(name="numofcharacters", nullable=true, insertable=true, updatable=true)
	private Integer numOfCharacters;
	@Column(name="numofwords", nullable=true, insertable=true, updatable=true)
	private Integer numOfWords;
	
	@Column(name="modification_date", nullable=true, insertable=true, updatable=true)
	private Date modificationDate;
	
	@ManyToOne(targetEntity=MessageImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="parent_id", nullable=true, insertable=true, updatable=true)
	private Message parent;
	@ManyToOne(targetEntity=MessageImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="topthread_id", nullable=true, insertable=true, updatable=true)
	private Message threadtop;
	@ManyToOne(targetEntity=ForumImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="forum_fk", nullable=false, insertable=true, updatable=false)
	private Forum forum;

	@Column(name="pseudonym", nullable=true, insertable=true, updatable=true)
	private String pseudonym;
	@Column(name="guest", nullable=true, insertable=true, updatable=false)
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
	public String getBody() {
		return body;
	}

	@Override
	public Identity getCreator() {
		return creator;
	}
	
	/**
	 * @param identity
	 */
	public void setCreator(Identity identity) {
		creator = identity;
	}

	@Override
	public String getPseudonym() {
		return pseudonym;
	}

	@Override
	public void setPseudonym(String pseudonym) {
		this.pseudonym = pseudonym;
	}

	@Override
	public boolean isGuest() {
		return guest;
	}

	public void setGuest(boolean guest) {
		this.guest = guest;
	}

	@Override
	public Forum getForum() {
		return forum;
	}
	
	public void setForum(Forum forum) {
		this.forum = forum;
	}

	@Override
	public Identity getModifier() {
		return modifier;
	}

	@Override
	public Message getParent() {
		return parent;
	}

	@Override
	public Long getParentKey() {
		return parent == null ? null : parent.getKey();
	}

	@Override
	public Message getThreadtop() {
		return threadtop;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setBody(String string) {
		body = string;
	}

	@Override
	public void setModifier(Identity identity) {
		modifier = identity;
	}

	@Override
	public void setParent(Message message) {
		parent = message;
	}

	@Override
	public void setThreadtop(Message message) {
		threadtop = message;
	}

	@Override
	public void setTitle(String string) {
		title = string;
	}

	@Override
	public int getStatusCode() {
		return statusCode;
	}
	
	@Override
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date date) {
		this.lastModified = date;
	}

	@Override
	public Date getModificationDate() {
		return modificationDate;
	}

	@Override
	public void setModificationDate(Date modificationDate) {
		this.modificationDate = modificationDate;
	}

	@Override
	public Integer getNumOfCharacters() {
		return numOfCharacters;
	}

	@Override
	public void setNumOfCharacters(Integer numOfCharacters) {
		this.numOfCharacters = numOfCharacters;
	}

	@Override
	public Integer getNumOfWords() {
		return numOfWords;
	}

	@Override
	public void setNumOfWords(Integer numOfWords) {
		this.numOfWords = numOfWords;
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
	public int compareTo(Message arg0) {
		//threadtop always is on top!
		if (arg0.getParent()==null) return 1;
		if (getCreationDate().after(arg0.getCreationDate())) return 1;
		return 0;
	}
}
