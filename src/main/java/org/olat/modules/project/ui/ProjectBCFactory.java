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
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.project.ProjAppointment;
import org.olat.modules.project.ProjAppointmentRef;
import org.olat.modules.project.ProjDecision;
import org.olat.modules.project.ProjDecisionRef;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjFileRef;
import org.olat.modules.project.ProjMilestone;
import org.olat.modules.project.ProjMilestoneRef;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjNoteRef;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectRef;
import org.olat.modules.project.ProjToDo;
import org.olat.modules.project.ProjToDoRef;
import org.olat.modules.todo.ui.ToDoTaskListController;

/**
 * 
 * Initial date: 6 Jan 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjectBCFactory {
	
	public static final String TYPE_PROJECTS = "Projects";
	public static final String TYPE_MY = "My";
	public static final String TYPE_TEMPLATES = "Templates";
	public static final String TYPE_ADMIN = "Admin";
	
	public static final String TYPE_MEMBERS_MANAGEMENT = "MembersMgmt";
	public static final String TYPE_PROJECT = "Project";
	public static final String TYPE_FILES = "Files";
	public static final String TYPE_FILE = "Projectfile";
	public static final String TYPE_TODOS = "ToDos";
	public static final String TYPE_TODO = ToDoTaskListController.TYPE_TODO;
	public static final String TYPE_DECISIONS = "Decisions";
	public static final String TYPE_DECISION = "Decision";
	public static final String TYPE_NOTES = "Notes";
	public static final String TYPE_NOTE = "Note";
	public static final String TYPE_CALENDAR = "Calendar";
	public static final String TYPE_APPOINTMENT = "Appointment";
	public static final String TYPE_MILESTONE = "Milestone";
	
	public static final ProjectBCFactory createFactory(ProjProject project) {
		return project.isTemplatePrivate() || project.isTemplatePublic()
				? createFactoryTemplate()
				: createFactoryProject();
	}
	
	public static final ProjectBCFactory createFactoryProject() {
		return createProjectsFactory(TYPE_MY);
	}
	
	public static final ProjectBCFactory createFactoryTemplate() {
		return createProjectsFactory(TYPE_TEMPLATES);
	}
	
	private static final ProjectBCFactory createProjectsFactory(String segmentOresType) {
		ProjectBCFactory bcFactory = new ProjectBCFactory();
		bcFactory.addBaseCe(BusinessControlFactory.getInstance().createContextEntry(OresHelper.createOLATResourceableType(TYPE_PROJECTS)));
		bcFactory.addBaseCe(BusinessControlFactory.getInstance().createContextEntry(OresHelper.createOLATResourceableType(segmentOresType)));
		return bcFactory;
	}
	
	
	private final List<ContextEntry> baseCes = new ArrayList<>();
	
	public void addBaseCe(ContextEntry ce) {
		baseCes.add(ce);
	}
	
	public List<ContextEntry> createProjectCes(ProjProjectRef ref) {
		List<ContextEntry> ces = new ArrayList<>(baseCes);
		ces.add(BusinessControlFactory.getInstance().createContextEntry(OresHelper.createOLATResourceableInstance(TYPE_PROJECT, ref.getKey())));
		return ces;
	}
	
	private List<ContextEntry> createFilesCes(ProjProjectRef projectRef) {
		List<ContextEntry> ces = createProjectCes(projectRef);
		ces.add(BusinessControlFactory.getInstance().createContextEntry(OresHelper.createOLATResourceableType(TYPE_FILES)));
		return ces;
	}
	
	private List<ContextEntry> createFileCes(ProjProjectRef projectRef, ProjFileRef fileRef) {
		List<ContextEntry> ces = createFilesCes(projectRef);
		ces.add(createFileCe(fileRef));
		return ces;
	}
	
	public static ContextEntry createFileCe(ProjFileRef fileRef) {
		return BusinessControlFactory.getInstance().createContextEntry(OresHelper.createOLATResourceableInstance(TYPE_FILE, fileRef.getKey()));
	}
	
	private List<ContextEntry> createToDosCes(ProjProjectRef projectRef) {
		List<ContextEntry> ces = createProjectCes(projectRef);
		ces.add(BusinessControlFactory.getInstance().createContextEntry(OresHelper.createOLATResourceableType(TYPE_TODOS)));
		return ces;
	}
	
	private List<ContextEntry> createToDoCes(ProjProjectRef projectRef, ProjToDoRef toDoRef) {
		List<ContextEntry> ces = createToDosCes(projectRef);
		ces.add(createToDoCe(toDoRef));
		return ces;
	}
	
	public static ContextEntry createToDoCe(ProjToDoRef toDoRef) {
		return BusinessControlFactory.getInstance().createContextEntry(OresHelper.createOLATResourceableInstance(TYPE_TODO, toDoRef.getKey()));
	}
	
	private List<ContextEntry> createDecisionsCes(ProjProjectRef projectRef) {
		List<ContextEntry> ces = createProjectCes(projectRef);
		ces.add(BusinessControlFactory.getInstance().createContextEntry(OresHelper.createOLATResourceableType(TYPE_DECISIONS)));
		return ces;
	}
	
	private List<ContextEntry> createDecisionCes(ProjProjectRef projectRef, ProjDecisionRef decisionRef) {
		List<ContextEntry> ces = createDecisionsCes(projectRef);
		ces.add(createDecisionCe(decisionRef));
		return ces;
	}
	
	public static ContextEntry createDecisionCe(ProjDecisionRef decisionRef) {
		return BusinessControlFactory.getInstance().createContextEntry(OresHelper.createOLATResourceableInstance(TYPE_DECISION, decisionRef.getKey()));
	}
	
	private List<ContextEntry> createNotesCes(ProjProjectRef projectRef) {
		List<ContextEntry> ces = createProjectCes(projectRef);
		ces.add(BusinessControlFactory.getInstance().createContextEntry(OresHelper.createOLATResourceableType(TYPE_NOTES)));
		return ces;
	}
	
	private List<ContextEntry> createNoteCes(ProjProjectRef projectRef, ProjNoteRef noteRef) {
		List<ContextEntry> ces = createNotesCes(projectRef);
		ces.add(createNoteCe(noteRef));
		return ces;
	}
	
	public static ContextEntry createNoteCe(ProjNoteRef noteRef) {
		return BusinessControlFactory.getInstance().createContextEntry(OresHelper.createOLATResourceableInstance(TYPE_NOTE, noteRef.getKey()));
	}
	
	private List<ContextEntry> createCalendarCes(ProjProjectRef projectRef) {
		List<ContextEntry> ces = createProjectCes(projectRef);
		ces.add(BusinessControlFactory.getInstance().createContextEntry(OresHelper.createOLATResourceableType(TYPE_CALENDAR)));
		return ces;
	}
	
	private List<ContextEntry> createAppointmentCes(ProjProjectRef projectRef, ProjAppointmentRef appointmentRef) {
		List<ContextEntry> ces = createCalendarCes(projectRef);
		ces.add(createAppointmentCe(appointmentRef));
		return ces;
	}
	
	public static ContextEntry createAppointmentCe(ProjAppointmentRef appointmentRef) {
		return BusinessControlFactory.getInstance().createContextEntry(OresHelper.createOLATResourceableInstance(TYPE_APPOINTMENT, appointmentRef.getKey()));
	}
	
	private List<ContextEntry> createMilestoneCes(ProjProjectRef projectRef, ProjMilestoneRef milestoneRef) {
		List<ContextEntry> ces = createCalendarCes(projectRef);
		ces.add(createMilestoneCe(milestoneRef));
		return ces;
	}
	
	public static ContextEntry createMilestoneCe(ProjMilestoneRef milestoneRef) {
		return BusinessControlFactory.getInstance().createContextEntry(OresHelper.createOLATResourceableInstance(TYPE_MILESTONE, milestoneRef.getKey()));
	}
	
	public String getProjectUrl(ProjProjectRef projectRef) {
		List<ContextEntry> ces = createProjectCes(projectRef);
		return BusinessControlFactory.getInstance().getAsURIString(ces, false);
	}
	
	public String getFilesUrl(ProjProjectRef projectRef) {
		List<ContextEntry> ces = createFilesCes(projectRef);
		return BusinessControlFactory.getInstance().getAsURIString(ces, false);
	}
	
	public String getFileUrl(ProjFile file) {
		return getFileUrl(file.getArtefact().getProject(), file);
	}
	
	public String getFileUrl(ProjProjectRef projectRef, ProjFileRef fileRef) {
		List<ContextEntry> ces = createFileCes(projectRef, fileRef);
		return BusinessControlFactory.getInstance().getAsURIString(ces, false);
	}
	
	public String getToDosUrl(ProjProjectRef projectRef) {
		List<ContextEntry> ces = createToDosCes(projectRef);
		return BusinessControlFactory.getInstance().getAsURIString(ces, false);
	}
	
	public String getToDoUrl(ProjToDo toDo) {
		return getToDoUrl(toDo.getArtefact().getProject(), toDo);
	}
	
	public String getToDoUrl(ProjProjectRef projectRef, ProjToDoRef toDoRef) {
		List<ContextEntry> ces = createToDoCes(projectRef, toDoRef);
		return BusinessControlFactory.getInstance().getAsURIString(ces, false);
	}
	
	public String getDecisionsUrl(ProjProjectRef projectRef) {
		List<ContextEntry> ces = createDecisionsCes(projectRef);
		return BusinessControlFactory.getInstance().getAsURIString(ces, false);
	}
	
	public String getDecisionUrl(ProjDecision decision) {
		return getDecisionUrl(decision.getArtefact().getProject(), decision);
	}
	
	public String getDecisionUrl(ProjProjectRef projectRef, ProjDecisionRef decisionRef) {
		List<ContextEntry> ces = createDecisionCes(projectRef, decisionRef);
		return BusinessControlFactory.getInstance().getAsURIString(ces, false);
	}
	
	public String getNotesUrl(ProjProjectRef projectRef) {
		List<ContextEntry> ces = createNotesCes(projectRef);
		return BusinessControlFactory.getInstance().getAsURIString(ces, false);
	}
	
	public String getNoteUrl(ProjNote note) {
		return getNoteUrl(note.getArtefact().getProject(), note);
	}
	
	public String getNoteUrl(ProjProjectRef projectRef, ProjNoteRef noteRef) {
		List<ContextEntry> ces = createNoteCes(projectRef, noteRef);
		return BusinessControlFactory.getInstance().getAsURIString(ces, false);
	}
	
	public String getCalendarUrl(ProjProjectRef projectRef) {
		List<ContextEntry> ces = createCalendarCes(projectRef);
		return BusinessControlFactory.getInstance().getAsURIString(ces, false);
	}
	
	public String getAppointmentUrl(ProjAppointment appointment) {
		return getAppointmentUrl(appointment.getArtefact().getProject(), appointment);
	}
	
	public String getAppointmentUrl(ProjProjectRef projectRef, ProjAppointmentRef appointmentRef) {
		List<ContextEntry> ces = createAppointmentCes(projectRef, appointmentRef);
		return BusinessControlFactory.getInstance().getAsURIString(ces, false);
	}
	
	public String getMilestoneUrl(ProjMilestone milestone) {
		return getMilestoneUrl(milestone.getArtefact().getProject(), milestone);
	}
	
	public String getMilestoneUrl(ProjProjectRef projectRef, ProjMilestoneRef milestoneRef) {
		List<ContextEntry> ces = createMilestoneCes(projectRef, milestoneRef);
		return BusinessControlFactory.getInstance().getAsURIString(ces, false);
	}
	
	public String getArtefactUrl(ProjProjectRef project, String artefactType, Long key) {
		switch (StringHelper.blankIfNull(artefactType)) {
		case ProjFile.TYPE: return getFileUrl(project, () -> key);
		case ProjToDo.TYPE: return getToDoUrl(project, () -> key);
		case ProjDecision.TYPE: return getDecisionUrl(project, () -> key);
		case ProjNote.TYPE: return getNoteUrl(project, () -> key);
		case ProjAppointment.TYPE: return getAppointmentUrl(project, () -> key);
		case ProjMilestone.TYPE: return getMilestoneUrl(project, () -> key);
		default: return getProjectUrl(project);
		}
	}
	
	public String getBusinessPath(ProjProjectRef project, String artefactType, Long key) {
		switch (StringHelper.blankIfNull(artefactType)) {
		case ProjFile.TYPE: return BusinessControlFactory.getInstance().getBusinessControlString(createFileCes(project, () -> key));
		case ProjToDo.TYPE: return BusinessControlFactory.getInstance().getBusinessControlString(createToDoCes(project, () -> key));
		case ProjDecision.TYPE: return BusinessControlFactory.getInstance().getBusinessControlString(createDecisionCes(project, () -> key));
		case ProjNote.TYPE: return BusinessControlFactory.getInstance().getBusinessControlString(createNoteCes(project, () -> key));
		case ProjAppointment.TYPE: return BusinessControlFactory.getInstance().getBusinessControlString(createAppointmentCes(project, () -> key));
		case ProjMilestone.TYPE: return BusinessControlFactory.getInstance().getBusinessControlString(createMilestoneCes(project, () -> key));
		default: return BusinessControlFactory.getInstance().getBusinessControlString(createProjectCes(project));
		}
	}
	
}
