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

import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.user.UserDataDeletable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description:
 * 
 * @author Alexander Schneider
 */
@Service
public class NoteManager implements UserDataDeletable {

	@Autowired
	private DB dbInstance;

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
	private Note findNote(IdentityRef owner, String resourceTypeName, Long resourceTypeId) {

		String query = "select n from org.olat.note.NoteImpl as n where n.owner.key=:ownerKey and n.resourceTypeName=:resName and n.resourceTypeId=:resId";
		List<Note> notes = dbInstance.getCurrentEntityManager().createQuery(query, Note.class)
				.setParameter("ownerKey", owner.getKey())
				.setParameter("resName", resourceTypeName)
				.setParameter("resId", resourceTypeId)
				.getResultList();
		if (notes == null || notes.size() != 1) {
			return null;
		}
		return notes.get(0);
	}

	/**
	 * @param owner
	 * @return a list of notes belonging to the owner
	 */
	public List<Note> listUserNotes(IdentityRef owner) {
		String query = "select n from org.olat.note.NoteImpl as n inner join fetch n.owner as noteowner where noteowner.key=:noteowner";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), Note.class).
				setParameter("noteowner", owner.getKey())
				.getResultList();
	}

	/**
	 * Deletes a note on the database
	 * 
	 * @param n the note
	 */
	public void deleteNote(Note n) {
		Note reloadedNote = dbInstance.getCurrentEntityManager().find(NoteImpl.class, n.getKey());
		dbInstance.getCurrentEntityManager().remove(reloadedNote);
		fireBookmarkEvent(n.getOwner());
	}

	/**
	 * Save a note
	 * 
	 * @param n
	 */
	public void saveNote(Note n) {
		n.setLastModified(new Date());
		dbInstance.getCurrentEntityManager().persist(n);
		fireBookmarkEvent(n.getOwner());
	}

	/**
	 * Update a note
	 * 
	 * @param n
	 */
	public Note updateNote(Note n) {
		Note reloadedNote = dbInstance.getCurrentEntityManager()
				.find(NoteImpl.class, n.getKey());
		reloadedNote.setLastModified(new Date());
		reloadedNote.setNoteTitle(n.getNoteTitle());
		reloadedNote.setNoteText(n.getNoteText());
		
		Note mergedNote = dbInstance.getCurrentEntityManager().merge(reloadedNote);
		fireBookmarkEvent(n.getOwner());
		return mergedNote;
	}

	/**
	 * Delete all notes for certain identity.
	 * @param identity  Delete notes for this identity.
	 */
	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName, File archivePath) {
		List<Note> userNotes = listUserNotes(identity);
		for (Iterator<Note> iter = userNotes.iterator(); iter.hasNext();) {
			deleteNote( iter.next() );			
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