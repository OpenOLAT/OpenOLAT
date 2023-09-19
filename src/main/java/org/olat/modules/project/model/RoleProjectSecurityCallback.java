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
package org.olat.modules.project.model;

import static java.util.Set.of;

import java.util.Collection;
import java.util.Set;

import org.olat.basesecurity.IdentityRef;
import org.olat.modules.project.ProjAppointment;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjDecision;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjMilestone;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ProjToDo;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;

/**
 * 
 * Initial date: 1 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RoleProjectSecurityCallback implements ProjProjectSecurityCallback {
	
	private static final Collection<ProjectRole> PROJECT_ADMIN =of(
			ProjectRole.owner,
			ProjectRole.leader,
			ProjectRole.projectOffice);
	private static final Collection<ProjectRole> ARTEFACT_UPDATE = of(
			ProjectRole.owner,
			ProjectRole.leader,
			ProjectRole.projectOffice,
			ProjectRole.participant,
			ProjectRole.supplier);
	
	private final boolean projectReadOnly;
	private final Set<ProjectRole> roles;
	private final boolean manager;
	private final boolean canCreateProject;
	private final boolean template;
	private final boolean templateManager;

	public RoleProjectSecurityCallback(ProjProject project, Set<ProjectRole> roles, boolean manager, boolean canCreateProject) {
		this.manager = manager;
		this.canCreateProject = canCreateProject;
		this.template = project.isTemplatePrivate() || project.isTemplatePublic();
		this.templateManager = manager && template;
		this.projectReadOnly = project.getStatus() == ProjectStatus.deleted || (template && !manager && !roles.contains(ProjectRole.owner));
		this.roles = roles;
	}

	@Override
	public boolean canViewProjectMetadata() {
		return true;
	}

	@Override
	public boolean canEditProjectMetadata() {
		return !projectReadOnly && (manager || hasRole(PROJECT_ADMIN));
	}

	@Override
	public boolean canEditProjectStatus() {
		return canEditProjectMetadata();
	}
	
	@Override
	public boolean canCopyProject() {
		return !template && canCreateProject && (manager || roles.contains(ProjectRole.owner));
	}
	
	@Override
	public boolean canCreateTemplate() {
		return !template && canCreateProject && (manager || roles.contains(ProjectRole.owner));
	}

	@Override
	public boolean canDeleteProject() {
		return manager || roles.contains(ProjectRole.owner);
	}
	
	@Override
	public boolean canSubscribe() {
		return  !roles.isEmpty();
	}

	@Override
	public boolean canViewMembers() {
		return true;
	}

	@Override
	public boolean canEditMembers() {
		return !template && !projectReadOnly && (manager || hasRole(PROJECT_ADMIN));
	}
	
	@Override
	public boolean canViewTimeline() {
		return templateManager || !roles.isEmpty();
	}

	@Override
	public boolean canViewWhiteboard() {
		return canViewArtefacts();
	}
	
	@Override
	public boolean canEditWhiteboard() {
		return canEditArtefacts();
	}

	@Override
	public boolean canViewFiles() {
		return canViewArtefacts();
	}
	
	@Override
	public boolean canCreateFiles() {
		return canCreateArtefacts();
	}

	@Override
	public boolean canEditFiles() {
		return canEditArtefacts();
	}

	@Override
	public boolean canEditFile(ProjFile file) {
		return canEditArtefact(file.getArtefact());
	}

	@Override
	public boolean canDeleteFile(ProjFile file, IdentityRef identity) {
		return canDeleteArtefact(file.getArtefact(), identity);
	}
	
	@Override
	public boolean canViewToDos() {
		return canViewArtefacts();
	}
	
	@Override
	public boolean canCreateToDos() {
		return canCreateArtefacts();
	}
	
	@Override
	public boolean canCreateToDoTasks() {
		return canCreateToDos();
	}

	@Override
	public boolean canEditToDos() {
		return canEditArtefacts();
	}

	@Override
	public boolean canEditToDo(ProjToDo toDo, boolean participant) {
		return canEditArtefact(toDo.getArtefact()) || participant;
	}

	@Override
	public boolean canEdit(ToDoTask toDoTask, boolean assignee, boolean delegatee) {
		return !projectReadOnly
				&& ToDoStatus.deleted != toDoTask.getStatus() 
				&& (hasRole(ARTEFACT_UPDATE) || assignee || delegatee || templateManager);
	}
	
	@Override
	public boolean canBulkDeleteToDoTasks() {
		return !projectReadOnly;
	}

	@Override
	public boolean canDeleteToDo(ProjToDo toDo, IdentityRef identity) {
		return canDeleteArtefact(toDo.getArtefact(), identity);
	}

	@Override
	public boolean canDelete(ToDoTask toDoTask, boolean creator, boolean assignee, boolean delegatee) {
		return !projectReadOnly 
				&& ToDoStatus.deleted != toDoTask.getStatus() 
				&& (hasRole(PROJECT_ADMIN) || creator || templateManager);
	}
	
	@Override
	public boolean canViewDecisions() {
		return canViewArtefacts();
	}
	
	@Override
	public boolean canCreateDecisions() {
		return canCreateArtefacts();
	}

	@Override
	public boolean canEditDecisions() {
		return canEditArtefacts();
	}

	@Override
	public boolean canEditDecision(ProjDecision decision) {
		return canEditArtefact(decision.getArtefact());
	}

	@Override
	public boolean canDeleteDecision(ProjDecision decision, IdentityRef identity) {
		return canDeleteArtefact(decision.getArtefact(), identity);
	}
	
	@Override
	public boolean canViewNotes() {
		return canViewArtefacts();
	}
	
	@Override
	public boolean canCreateNotes() {
		return canCreateArtefacts();
	}

	@Override
	public boolean canEditNotes() {
		return canEditArtefacts();
	}

	@Override
	public boolean canEditNote(ProjNote note) {
		return canEditArtefact(note.getArtefact());
	}

	@Override
	public boolean canDeleteNote(ProjNote note, IdentityRef identity) {
		return canDeleteArtefact(note.getArtefact(), identity);
	}

	@Override
	public boolean canViewAppointments() {
		return canViewArtefacts();
	}
	
	@Override
	public boolean canCreateAppointments() {
		return canCreateArtefacts();
	}

	@Override
	public boolean canEditAppointments() {
		return canEditArtefacts();
	}

	@Override
	public boolean canEditAppointment(ProjAppointment appointment) {
		return canEditArtefact(appointment.getArtefact());
	}

	@Override
	public boolean canDeleteAppointment(ProjAppointment appointment, IdentityRef identity) {
		return canDeleteArtefact(appointment.getArtefact(), identity);
	}

	@Override
	public boolean canViewMilestones() {
		return canViewArtefacts();
	}
	
	@Override
	public boolean canCreateMilestones() {
		return canCreateArtefacts();
	}

	@Override
	public boolean canEditMilestones() {
		return canEditArtefacts();
	}

	@Override
	public boolean canEditMilestone(ProjMilestone milestone) {
		return canEditArtefact(milestone.getArtefact());
	}

	@Override
	public boolean canDeleteMilestone(ProjMilestone milestone, IdentityRef identity) {
		return canDeleteArtefact(milestone.getArtefact(), identity);
	}
	
	private boolean canViewArtefacts() {
		return templateManager || !roles.isEmpty();
	}
	
	private boolean canCreateArtefacts() {
		return !projectReadOnly && (templateManager || hasRole(ARTEFACT_UPDATE));
	}

	private boolean canEditArtefacts() {
		return !projectReadOnly && (templateManager || hasRole(ARTEFACT_UPDATE));
	}

	private boolean canEditArtefact(ProjArtefact artefact) {
		return !projectReadOnly 
				&& ProjectStatus.deleted != artefact.getStatus()
				&& (templateManager || hasRole(ARTEFACT_UPDATE));
	}

	private boolean canDeleteArtefact(ProjArtefact artefact, IdentityRef identity) {
		return !projectReadOnly 
				&& ProjectStatus.deleted != artefact.getStatus()
				&& (templateManager || hasRole(PROJECT_ADMIN) || identity.getKey().equals(artefact.getCreator().getKey()));
	}

	private boolean hasRole(Collection<ProjectRole> targetRoles) {
		return roles.stream().anyMatch(role -> targetRoles.contains(role));
	}

}
