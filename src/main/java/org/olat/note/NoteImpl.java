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

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;

/**
 * Description: <br>
 * Implementation of the Note Interface
 * 
 * @author Alexander Schneider
 */
public class NoteImpl extends PersistentObject implements Note {

	private static final long serialVersionUID = -403450817851666464L;
	
	private Identity owner;
	private String resourceTypeName;
	private Long resourceTypeId;
	private String subtype;
	private String noteTitle;
	private String noteText;
	private Date lastModified;
	private static final int RESOURCETYPENAME_MAXLENGTH = 50;
	private static final int SUBTYPE_MAXLENGTH = 50;

	/**
	 * Default construcor
	 */
	public NoteImpl() {
	// nothing to do
	}

	/**
	 * @return Returns the noteText.
	 */
	public String getNoteText() {
		return noteText;
	}

	/**
	 * @param noteText The noteText to set.
	 */
	public void setNoteText(String noteText) {
		this.noteText = noteText;
	}

	/**
	 * @return Returns the noteTitle.
	 */
	public String getNoteTitle() {
		return noteTitle;
	}

	/**
	 * @param noteTitle The noteTitle to set.
	 */
	public void setNoteTitle(String noteTitle) {
		this.noteTitle = noteTitle;
	}

	/**
	 * @return Returns the owner.
	 */
	public Identity getOwner() {
		return owner;
	}

	/**
	 * @param owner The owner to set.
	 */
	public void setOwner(Identity owner) {
		this.owner = owner;
	}

	/**
	 * @return Returns the resourceTypeId.
	 */
	public Long getResourceTypeId() {
		return resourceTypeId;
	}

	/**
	 * @param resourceTypeId The resourceTypeId to set.
	 */
	public void setResourceTypeId(Long resourceTypeId) {
		this.resourceTypeId = resourceTypeId;
	}

	/**
	 * @return Returns the resourceTypeName.
	 */
	public String getResourceTypeName() {
		return resourceTypeName;
	}

	/**
	 * @param resourceTypeName The resourceTypeName to set.
	 */
	public void setResourceTypeName(String resourceTypeName) {
		if (resourceTypeName.length() > RESOURCETYPENAME_MAXLENGTH)
			throw new AssertException("resourcetypename in o_note too long");
		this.resourceTypeName = resourceTypeName;
	}

	/**
	 * @return Returns the subtype.
	 */
	public String getSubtype() {
		return subtype;
	}

	/**
	 * @param subtype The subtype to set.
	 */
	public void setSubtype(String subtype) {
		if (subtype != null && subtype.length() > SUBTYPE_MAXLENGTH)
			throw new AssertException("subtype of o_note too long");
		this.subtype = subtype;
	}

	/**
	 * 
	 * @see org.olat.core.id.ModifiedInfo#getLastModified()
	 */
	public Date getLastModified() {
		return lastModified;
	}

	/**
	 * 
	 * @see org.olat.core.id.ModifiedInfo#setLastModified(java.util.Date)
	 */
	public void setLastModified(Date date) {
		this.lastModified = date;
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
}