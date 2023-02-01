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
package org.olat.modules.project.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjFileRef;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjNoteRef;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectRef;

/**
 * 
 * Initial date: 6 Jan 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjectBCFactory {
	
	public static final String TYPE_PROJECTS = "Projects";
	public static final String TYPE_MEMBERS_MANAGEMENT = "MembersMgmt";
	public static final String TYPE_FILES = "Files";
	public static final String TYPE_FILE = "Projectfile";
	public static final String TYPE_NOTES = "Notes";
	public static final String TYPE_NOTE = "Note";
	
	private static List<ContextEntry> createProjectsCes() {
		List<ContextEntry> ces = new ArrayList<>();
		ces.add(BusinessControlFactory.getInstance().createContextEntry(OresHelper.createOLATResourceableType(TYPE_PROJECTS)));
		return ces;
	}
	
	private static List<ContextEntry> createProjectCes(ProjProjectRef ref) {
		List<ContextEntry> ces = createProjectsCes();
		ces.add(BusinessControlFactory.getInstance().createContextEntry(OresHelper.createOLATResourceableInstance(ProjProject.TYPE, ref.getKey())));
		return ces;
	}
	
	private static List<ContextEntry> createFilesCes(ProjProjectRef projectRef) {
		List<ContextEntry> ces = createProjectCes(projectRef);
		ces.add(BusinessControlFactory.getInstance().createContextEntry(OresHelper.createOLATResourceableType(TYPE_FILES)));
		return ces;
	}
	
	private static List<ContextEntry> createFileCes(ProjProjectRef projectRef, ProjFileRef fileRef) {
		List<ContextEntry> ces = createFilesCes(projectRef);
		ces.add(createFileCe(fileRef));
		return ces;
	}
	
	public static ContextEntry createFileCe(ProjFileRef fileRef) {
		return BusinessControlFactory.getInstance().createContextEntry(OresHelper.createOLATResourceableInstance(TYPE_FILE, fileRef.getKey()));
	}
	
	private static List<ContextEntry> createNotesCes(ProjProjectRef projectRef) {
		List<ContextEntry> ces = createProjectCes(projectRef);
		ces.add(BusinessControlFactory.getInstance().createContextEntry(OresHelper.createOLATResourceableType(TYPE_NOTES)));
		return ces;
	}
	
	private static List<ContextEntry> createNoteCes(ProjProjectRef projectRef, ProjNoteRef noteRef) {
		List<ContextEntry> ces = createNotesCes(projectRef);
		ces.add(createNoteCe(noteRef));
		return ces;
	}
	
	public static ContextEntry createNoteCe(ProjNoteRef noteRef) {
		return BusinessControlFactory.getInstance().createContextEntry(OresHelper.createOLATResourceableInstance(TYPE_NOTE, noteRef.getKey()));
	}
	
	public static String getProjectUrl(ProjProjectRef projectRef) {
		List<ContextEntry> ces = createProjectCes(projectRef);
		return BusinessControlFactory.getInstance().getAsURIString(ces, false);
	}
	
	public static String getFilesUrl(ProjProjectRef projectRef) {
		List<ContextEntry> ces = createFilesCes(projectRef);
		return BusinessControlFactory.getInstance().getAsURIString(ces, false);
	}
	
	public static String getFileUrl(ProjFile file) {
		return getFileUrl(file.getArtefact().getProject(), file);
	}
	
	public static String getFileUrl(ProjProjectRef projectRef, ProjFileRef fileRef) {
		List<ContextEntry> ces = createFileCes(projectRef, fileRef);
		return BusinessControlFactory.getInstance().getAsURIString(ces, false);
	}
	
	public static String getNotesUrl(ProjProjectRef projectRef) {
		List<ContextEntry> ces = createNotesCes(projectRef);
		return BusinessControlFactory.getInstance().getAsURIString(ces, false);
	}
	
	public static String getNoteUrl(ProjNote note) {
		return getNoteUrl(note.getArtefact().getProject(), note);
	}
	
	public static String getNoteUrl(ProjProjectRef projectRef, ProjNoteRef noteRef) {
		List<ContextEntry> ces = createNoteCes(projectRef, noteRef);
		return BusinessControlFactory.getInstance().getAsURIString(ces, false);
	}
	
	public static String getArtefactUrl(ProjProjectRef project, String artefactType, Long key) {
		switch (artefactType) {
		case ProjFile.TYPE: return getFileUrl(project, () -> key);
		case ProjNote.TYPE: return getNoteUrl(project, () -> key);
		default: return getProjectUrl(project);
		}
	}

}
