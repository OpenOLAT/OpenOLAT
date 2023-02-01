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

import org.olat.core.id.Identity;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ProjectStatus;

/**
 * 
 * Initial date: 1 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RoleProjectSecurityCallback implements ProjProjectSecurityCallback {
	
	private static final Collection<ProjectRole> OWN_OBJECTS = of(ProjectRole.owner, ProjectRole.leader,
			ProjectRole.projectOffice, ProjectRole.participant, ProjectRole.supplier);
	private static final Collection<ProjectRole> OTHER_OBJECTS = of(ProjectRole.owner, ProjectRole.leader,
			ProjectRole.projectOffice, ProjectRole.supplier);
	
	private final boolean projectReadOnly;
	private final Set<ProjectRole> roles;

	public RoleProjectSecurityCallback(ProjectStatus status, Set<ProjectRole> roles) {
		this.projectReadOnly = ProjectStatus.deleted == status;
		this.roles = roles;
	}

	@Override
	public boolean canViewProjectMetadata() {
		return hasRole(of(ProjectRole.owner, ProjectRole.leader, ProjectRole.projectOffice));
	}

	@Override
	public boolean canEditProjectMetadata() {
		return !projectReadOnly && canViewProjectMetadata();
	}

	@Override
	public boolean canEditProjectStatus() {
		return hasRole(of(ProjectRole.owner, ProjectRole.leader, ProjectRole.projectOffice));
	}

	@Override
	public boolean canDeleteProject() {
		return roles.contains(ProjectRole.owner);
	}

	@Override
	public boolean canViewMembers() {
		return canEditMembers();
	}

	@Override
	public boolean canEditMembers() {
		return !projectReadOnly && hasRole(of(ProjectRole.owner, ProjectRole.leader, ProjectRole.projectOffice));
	}
	
	@Override
	public boolean canCreateFiles() {
		return !projectReadOnly && hasRole(OWN_OBJECTS);
	}

	@Override
	public boolean canEditFiles() {
		return !projectReadOnly && hasRole(OTHER_OBJECTS);
	}

	@Override
	public boolean canEditFile(ProjFile file, Identity identity) {
		return !projectReadOnly 
				&& ProjectStatus.deleted != file.getArtefact().getStatus()
				&& ( hasRole(OTHER_OBJECTS) || (hasRole(OWN_OBJECTS) && file.getVfsMetadata().getFileInitializedBy().getKey().equals(identity.getKey())));
	}

	@Override
	public boolean canDeleteFile(ProjFile file, Identity identity) {
		return !projectReadOnly 
				&& ProjectStatus.deleted != file.getArtefact().getStatus()
				&& (hasRole(of(ProjectRole.owner)) || (hasRole(OWN_OBJECTS) && file.getVfsMetadata().getFileInitializedBy().getKey().equals(identity.getKey()))
			);
	}
	
	@Override
	public boolean canCreateNotes() {
		return !projectReadOnly && hasRole(OWN_OBJECTS);
	}

	@Override
	public boolean canEditNotes() {
		return !projectReadOnly && hasRole(OTHER_OBJECTS);
	}

	@Override
	public boolean canEditNote(ProjNote note, boolean participant) {
		return !projectReadOnly 
				&& ProjectStatus.deleted != note.getArtefact().getStatus() 
				&& (hasRole(OTHER_OBJECTS) || (hasRole(OWN_OBJECTS) && participant));
	}

	@Override
	public boolean canDeleteNote(ProjNote note, boolean participant) {
		return !projectReadOnly 
				&& ProjectStatus.deleted != note.getArtefact().getStatus() 
				&& (hasRole(of(ProjectRole.owner)) || (hasRole(OWN_OBJECTS) && participant));
	}

	private boolean hasRole(Collection<ProjectRole> targetRoles) {
		return roles.stream().anyMatch(role -> targetRoles.contains(role));
	}

}
