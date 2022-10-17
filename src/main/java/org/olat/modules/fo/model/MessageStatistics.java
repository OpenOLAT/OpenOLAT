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
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;

/**
 * 
 * Initial date: 11.11.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="fomessageforstatistics")
@Table(name="o_message")
public class MessageStatistics implements Persistable {

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
	@Column(name="lastmodified", nullable=false, insertable=false, updatable=false)
	private Date lastModified;

	@Column(name="statuscode", nullable=false, insertable=false, updatable=false)
	private int statusCode;
	
	@Column(name="numofcharacters", nullable=true, insertable=false, updatable=false)
	private Integer numOfCharacters;
	@Column(name="numofwords", nullable=true, insertable=false, updatable=false)
	private Integer numOfWords;

	@Column(name="topthread_id", nullable=true, insertable=false, updatable=false)
	private Long threadtopKey;
	
	@Column(name="forum_fk", nullable=true, insertable=false, updatable=false)
	private Long forumKey;

	@Column(name="pseudonym", nullable=true, insertable=false, updatable=false)
	private String pseudonym;
	@Column(name="guest", nullable=true, insertable=false, updatable=false)
	private boolean guest;
	
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="creator_id", nullable=true, insertable=false, updatable=false)
	private Identity creator;

	@Override
	public Long getKey() {
		return key;
	}

	/**
	 * @return
	 */
	public Identity getCreator() {
		return creator;
	}

	public String getPseudonym() {
		return pseudonym;
	}
	
	public boolean isGuest() {
		return guest;
	}

	public Long getThreadtopKey() {
		return threadtopKey;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public Integer getNumOfCharacters() {
		return numOfCharacters;
	}

	public Integer getNumOfWords() {
		return numOfWords;
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
		if (obj instanceof MessageStatistics) {
			MessageStatistics other = (MessageStatistics) obj;
			return getKey().equals(other.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
