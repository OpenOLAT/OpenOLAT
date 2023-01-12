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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.user.UserDataDeletable;
import org.olat.user.UserDataExportable;
import org.olat.user.manager.ManifestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description:
 * 
 * @author Alexander Schneider
 */
@Service
public class NoteManager implements UserDataDeletable, UserDataExportable {
	
	private static final Logger log = Tracing.createLoggerFor(NoteManager.class);

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
		NoteImpl n = new NoteImpl();
		n.setCreationDate(new Date());
		n.setLastModified(n.getCreationDate());
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
		List<Note> notes = dbInstance.getCurrentEntityManager()
				.createNamedQuery("noteByOwnerAndResource", Note.class)
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
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("noteByOwner", Note.class).
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
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		List<Note> userNotes = listUserNotes(identity);
		for (Note userNote:userNotes) {
			deleteNote(userNote);			
		}
	}
	
	@Override
	public String getExporterID() {
		return "notes";
	}

	@Override
	public void export(Identity identity, ManifestBuilder manifest, File archiveDirectory, Locale locale) {
		File noteArchive = new File(archiveDirectory, "Notes.xlsx");
		try(OutputStream out = new FileOutputStream(noteArchive);
			OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
			OpenXMLWorksheet sheet = workbook.nextWorksheet();
			sheet.setHeaderRows(1);
			
			Row row = sheet.newRow();
			row.addCell(0, "Created");
			row.addCell(1, "Modified");
			row.addCell(2, "Title");
			row.addCell(3, "Content");
			row.addCell(4, "Course");
			row.addCell(5, "Course URL");
			
			List<Note> notes = listUserNotes(identity);
			for(Note note:notes) {
				exportNoteData(note, sheet, workbook);
			}
		} catch (IOException e) {
			log.error("Unable to export xlsx", e);
		}

		manifest.appendFile("Notes.xlsx");
	}

	private void exportNoteData(Note note, OpenXMLWorksheet sheet, OpenXMLWorkbook workbook) {
		Row row = sheet.newRow();
		row.addCell(0, note.getCreationDate(), workbook.getStyles().getDateTimeStyle());
		row.addCell(1, note.getLastModified(), workbook.getStyles().getDateTimeStyle());
		row.addCell(2, note.getNoteTitle());
		row.addCell(3, note.getNoteText());
		if("CourseModule".equals(note.getResourceTypeName())) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(note.getResourceTypeName(), note.getResourceTypeId());
			RepositoryEntry entry = RepositoryManager.getInstance().lookupRepositoryEntry(ores, false);
			if(entry != null) {
				row.addCell(4, entry.getDisplayname());
				row.addCell(5, Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + entry.getKey());
			}
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
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(noteEvent, eventBusOres);		
	}
}