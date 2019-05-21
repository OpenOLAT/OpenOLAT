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
* <p>
*/ 

package org.olat.note;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.resource.OLATResource;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date:  Dec 9, 2004
 *
 * @author Alexander Schneider
 * 
 * Comment:  
 * 
 */
public class NoteTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private NoteManager noteManager;

	@Test
	public void testGenericLoadDeleteNote() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("note-1-");
	    OLATResource resource = JunitTestHelper.createRandomResource();
	    
	    Note n = noteManager.loadNoteOrCreateInRAM(identity, resource.getResourceableTypeName(), resource.getResourceableId());
	    n.setNoteTitle("Notiz Titel");
	    n.setNoteText("Notiz Text");
	    noteManager.saveNote(n);
	    
	    dbInstance.commitAndCloseSession();
	    
	    Note note = noteManager.loadNoteOrCreateInRAM(identity, resource.getResourceableTypeName(), resource.getResourceableId());
	    Assert.assertNotNull(note);
	    
	    noteManager.deleteNote(note);
	    
	    List<Note> notes = noteManager.listUserNotes(identity);
	    Assert.assertTrue(notes.isEmpty());
	}
	
	@Test
	public void saveUpdateNote() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("note-2-");
	    OLATResource resource = JunitTestHelper.createRandomResource();
		Note note = noteManager.loadNoteOrCreateInRAM(identity, resource.getResourceableTypeName(), resource.getResourceableId());
		note.setNoteTitle("Very important");
	    note.setNoteText("Critical update with new features");
	    noteManager.saveNote(note);
	    dbInstance.commitAndCloseSession();
	    
	    Note updateNote = noteManager.loadNoteOrCreateInRAM(identity, resource.getResourceableTypeName(), resource.getResourceableId());
	    updateNote.setNoteTitle("Important");
	    updateNote.setNoteText("Cool update with new features");
	    noteManager.saveNote(updateNote);
	    dbInstance.commitAndCloseSession();
	    
	    List<Note> notes = noteManager.listUserNotes(identity);
	    Assert.assertNotNull(notes);
	    Assert.assertEquals(1, notes.size());
	    Assert.assertEquals(updateNote, notes.get(0));
	    Note reloadedNote = notes.get(0);
	    Assert.assertEquals("Important", reloadedNote.getNoteTitle());
	    Assert.assertEquals("Cool update with new features", reloadedNote.getNoteText());
	}
	
	@Test
	public void findNote() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("note-2-");
	    OLATResource resource = JunitTestHelper.createRandomResource();
		Note note = noteManager.loadNoteOrCreateInRAM(identity, resource.getResourceableTypeName(), resource.getResourceableId());
		note.setNoteTitle("Very important");
	    note.setNoteText("Critical update with new features");
	    noteManager.saveNote(note);
	    dbInstance.commitAndCloseSession();
	    
	    Note reloadedNote = noteManager.loadNoteOrCreateInRAM(identity, resource.getResourceableTypeName(), resource.getResourceableId());
	    Assert.assertNotNull(reloadedNote);
	    Assert.assertEquals(note, reloadedNote);
	}
}
