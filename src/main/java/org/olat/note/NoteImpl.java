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

package org.olat.note;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.core.logging.AssertException;

/**
 * Description: <br>
 * Implementation of the Note Interface
 * 
 * @author Alexander Schneider
 */
@Entity(name="note")
@Table(name="o_note")
@NamedQuery(name="noteByOwner", query="select n from note as n inner join fetch n.owner as noteowner where noteowner.key=:noteowner")
@NamedQuery(name="noteByOwnerAndResource", query="select n from note as n where n.owner.key=:ownerKey and n.resourceTypeName=:resName and n.resourceTypeId=:resId")
public class NoteImpl implements Note, Persistable, CreateInfo {

	private static final long serialVersionUID = -403450817851666464L;
	private static final int RESOURCETYPENAME_MAXLENGTH = 50;
	private static final int SUBTYPE_MAXLENGTH = 50;
	
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
	@Column(name="note_id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	@Version
	private int version = 0;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	
	@Column(name="resourcetypename", nullable=false, insertable=true, updatable=false)
	private String resourceTypeName;
	@Column(name="resourcetypeid", nullable=false, insertable=true, updatable=false)
	private Long resourceTypeId;
	@Column(name="sub_type", nullable=true, insertable=true, updatable=false)
	private String subtype;

	@Column(name="notetitle", nullable=true, insertable=true, updatable=true)
	private String noteTitle;
	@Column(name="notetext", nullable=true, insertable=true, updatable=true)
	private String noteText;
	
	@OneToOne(targetEntity=IdentityImpl.class)
	@JoinColumn(name="owner_id", nullable=false, insertable=true, updatable=false)
	private Identity owner;

	/**
	 * Default construcor
	 */
	public NoteImpl() {
	// nothing to do
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

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date date) {
		this.lastModified = date;
	}

	/**
	 * @return Returns the noteText.
	 */
	@Override
	public String getNoteText() {
		return noteText;
	}

	/**
	 * @param noteText The noteText to set.
	 */
	@Override
	public void setNoteText(String noteText) {
		this.noteText = noteText;
	}

	/**
	 * @return Returns the noteTitle.
	 */
	@Override
	public String getNoteTitle() {
		return noteTitle;
	}

	/**
	 * @param noteTitle The noteTitle to set.
	 */
	@Override
	public void setNoteTitle(String noteTitle) {
		this.noteTitle = noteTitle;
	}

	/**
	 * @return Returns the owner.
	 */
	@Override
	public Identity getOwner() {
		return owner;
	}

	/**
	 * @param owner The owner to set.
	 */
	@Override
	public void setOwner(Identity owner) {
		this.owner = owner;
	}

	/**
	 * @return Returns the resourceTypeId.
	 */
	@Override
	public Long getResourceTypeId() {
		return resourceTypeId;
	}

	/**
	 * @param resourceTypeId The resourceTypeId to set.
	 */
	@Override
	public void setResourceTypeId(Long resourceTypeId) {
		this.resourceTypeId = resourceTypeId;
	}

	/**
	 * @return Returns the resourceTypeName.
	 */
	@Override
	public String getResourceTypeName() {
		return resourceTypeName;
	}

	/**
	 * @param resourceTypeName The resourceTypeName to set.
	 */
	@Override
	public void setResourceTypeName(String resourceTypeName) {
		if (resourceTypeName.length() > RESOURCETYPENAME_MAXLENGTH)
			throw new AssertException("resourcetypename in o_note too long");
		this.resourceTypeName = resourceTypeName;
	}

	/**
	 * @return Returns the subtype.
	 */
	@Override
	public String getSubtype() {
		return subtype;
	}

	/**
	 * @param subtype The subtype to set.
	 */
	@Override
	public void setSubtype(String subtype) {
		if (subtype != null && subtype.length() > SUBTYPE_MAXLENGTH)
			throw new AssertException("subtype of o_note too long");
		this.subtype = subtype;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 2609815 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof NoteImpl) {
			NoteImpl note = (NoteImpl)obj;
			return getKey() != null && getKey().equals(note.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}