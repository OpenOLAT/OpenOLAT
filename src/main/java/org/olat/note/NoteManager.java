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
import java.util.Iterator;
import java.util.List;

import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.user.UserDataDeletable;

/**
 * Description:
 * 
 * @author Alexander Schneider
 */
public class NoteManager extends BasicManager implements UserDataDeletable {
	private static NoteManager instance;

	/**
	 * [spring]
	 * @param userDeletionManager
	 */
	private NoteManager() {
		instance = this;
	}

	/**
	 * @return the singleton
	 */
	public static NoteManager getInstance() {
		return instance;
	}

	/**
	 * @param owner
	 * @param resourceTypeName
	 * @param resourceTypeId
	 * @return a note, either a new one in RAM, or the persisted if found using
	 *         the params
	 */
	public Note loadNoteOrCreateInRAM(Identity owner, String resourceTypeName, Long resourceTypeId) {
		Note note = findNote(owner, resourceTypeName, resourceTypeId);
		if (note == null) {
			note = createNote(owner, resourceTypeName, resourceTypeId);
		}
		return note;
	}

	/**
	 * @param owner
	 * @param resourceTypeName
	 * @param resourceTypeId
	 * @return the note
	 */
	private Note createNote(Identity owner, String resourceTypeName, Long resourceTypeId) {
		Note n = new NoteImpl();
		n.setOwner(owner);
		n.setResourceTypeName(resourceTypeName);
		n.setResourceTypeId(resourceTypeId);
		return n;
	}

	/**
	 * @param owner
	 * @param resourceTypeName
	 * @param resourceTypeId
	 * @return the note
	 */
	private Note findNote(Identity owner, String resourceTypeName, Long resourceTypeId) {

		String query = "from org.olat.note.NoteImpl as n where n.owner = ? and n.resourceTypeName = ? and n.resourceTypeId = ?";
		List notes = DBFactory.getInstance().find(query, new Object[] { owner.getKey(), resourceTypeName, resourceTypeId },
				new Type[] { StandardBasicTypes.LONG, StandardBasicTypes.STRING, StandardBasicTypes.LONG });

		if (notes == null || notes.size() != 1) {
			return null;
		} else {
			return (Note) notes.get(0);
		}
	}

	/**
	 * @param owner
	 * @return a list of notes belonging to the owner
	 */
	public List<Note> listUserNotes(Identity owner) {
		String query = "from org.olat.note.NoteImpl as n inner join fetch n.owner as noteowner where noteowner = :noteowner";
		DBQuery dbQuery = DBFactory.getInstance().createQuery(query.toString());
		dbQuery.setEntity("noteowner", owner);
		List<Note> notes = dbQuery.list();
		return notes;
	}

	/**
	 * Deletes a note on the database
	 * 
	 * @param n the note
	 */
	public void deleteNote(Note n) {
		n = (Note)DBFactory.getInstance().loadObject(n);
		DBFactory.getInstance().deleteObject(n);
		fireBookmarkEvent(n.getOwner());
	}

	/**
	 * Save a note
	 * 
	 * @param n
	 */
	public void saveNote(Note n) {
		n.setLastModified(new Date());
		DBFactory.getInstance().saveObject(n);
		fireBookmarkEvent(n.getOwner());
	}

	/**
	 * Update a note
	 * 
	 * @param n
	 */
	public void updateNote(Note n) {
		n.setLastModified(new Date());
		DBFactory.getInstance().updateObject(n);
		fireBookmarkEvent(n.getOwner());
	}

	/**
	 * Delete all notes for certain identity.
	 * @param identity  Delete notes for this identity.
	 */
	@SuppressWarnings("unused")
	public void deleteUserData(Identity identity,	String newDeletedUserName) {
		List<Note> userNotes = this.listUserNotes(identity);
		for (Iterator<Note> iter = userNotes.iterator(); iter.hasNext();) {
			this.deleteNote( iter.next() );			
		}
		if (isLogDebugEnabled()) {
			logDebug("All notes deleted for identity=" + identity, null );
		}
	}
	
	/**
	 * Fire NoteEvent for a specific user after save/update/delete note.
	 * @param identity
	 */
	private void fireBookmarkEvent(Identity identity) {
		//event this identity
		NoteEvent noteEvent = new NoteEvent(identity.getKey());
		OLATResourceable eventBusOres = OresHelper.createOLATResourceableInstance(Identity.class, identity.getKey());							
		//TODO: LD: use SingleUserEventCenter
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(noteEvent, eventBusOres);		
	}
}