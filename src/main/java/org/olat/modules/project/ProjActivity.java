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
package org.olat.modules.project;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.logging.AssertException;

/**
 * 
 * Initial date: 16 Jan 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface ProjActivity extends CreateInfo {
	
	public Long getKey();
	
	public Action getAction();
	
	public ActionTarget getActionTarget();
	
	public String getBefore();
	
	public String getAfter();

	public String getTempIdentifier();
	
	public Identity getDoer();
	
	public ProjProject getProject();
	
	public ProjArtefact getArtefact();

	public ProjArtefact getArtefactReference();
	
	public Identity getMember();
	
	public Organisation getOrganisation();
	
	public static List<Action> TIMELINE_ACTIONS = List.of(
			Action.projectCreate,
			Action.projectContentUpdate,
			Action.projectStatusActive,
			Action.projectStatusDone,
			Action.projectStatusDelete,
			Action.projectMemberAdd,
			Action.projectMemberRemove,
			Action.fileCreate,
			Action.fileUpload,
			Action.fileEdit,
			Action.fileContentUpdate,
			Action.fileStatusDelete,
			Action.toDoCreate,
			Action.toDoContentUpdate,
			Action.toDoStatusDelete,
			Action.noteCreate,
			Action.noteContentUpdate,
			Action.noteStatusDelete,
			Action.appointmentCreate,
			Action.appointmentStatusDelete,
			Action.milestoneCreate,
			Action.milestoneContentUpdate,
			Action.milestoneStatusDelete
		);
	
	public static Action[] QUICK_START_ACTIONS = {
			ProjActivity.Action.fileCreate,
			ProjActivity.Action.fileUpload,
			ProjActivity.Action.fileEdit,
			ProjActivity.Action.fileContentUpdate,
			ProjActivity.Action.fileRead,
			ProjActivity.Action.fileDownload,
			ProjActivity.Action.noteCreate,
			ProjActivity.Action.noteContentUpdate,
			ProjActivity.Action.noteRead,
			ProjActivity.Action.noteDownload
	};
	
	public enum Action {
		projectCreate(ActionTarget.project),
		projectContentUpdate(ActionTarget.project),
		projectStatusActive(ActionTarget.project),
		projectStatusDone(ActionTarget.project),
		projectStatusDelete(ActionTarget.project),
		projectOrganisationAdd(ActionTarget.project),
		projectOrganisationRemove(ActionTarget.project),
		projectMemberAdd(ActionTarget.project),
		projectMemberRemove(ActionTarget.project),
		projectRolesUpdate(ActionTarget.project),
		projectRead(ActionTarget.project),
		fileCopyInitialized(ActionTarget.file),
		fileCreate(ActionTarget.file),
		fileUpload(ActionTarget.file),
		fileContentUpdate(ActionTarget.file),
		fileTagsUpdate(ActionTarget.file),
		fileMemberAdd(ActionTarget.file),
		fileMemberRemove(ActionTarget.file),
		fileRolesUpdate(ActionTarget.file),
		fileReferenceAdd(ActionTarget.file),
		fileReferenceRemove(ActionTarget.file),
		fileStatusDelete(ActionTarget.file),
		fileRead(ActionTarget.file),
		fileDownload(ActionTarget.file),
		fileEdit(ActionTarget.file),
		toDoCopyInitialized(ActionTarget.toDo),
		toDoCreate(ActionTarget.toDo),
		toDoContentUpdate(ActionTarget.toDo),
		toDoTagsUpdate(ActionTarget.toDo),
		toDoMemberAdd(ActionTarget.toDo),
		toDoMemberRemove(ActionTarget.toDo),
		toDoRolesUpdate(ActionTarget.toDo),
		toDoReferenceAdd(ActionTarget.toDo),
		toDoReferenceRemove(ActionTarget.toDo),
		toDoStatusDelete(ActionTarget.toDo),
		noteCopyInitialized(ActionTarget.note),
		noteCreate(ActionTarget.note),
		noteContentUpdate(ActionTarget.note),
		noteTagsUpdate(ActionTarget.note),
		noteMemberAdd(ActionTarget.note),
		noteMemberRemove(ActionTarget.note),
		noteRolesUpdate(ActionTarget.note),
		noteReferenceAdd(ActionTarget.note),
		noteReferenceRemove(ActionTarget.note),
		noteStatusDelete(ActionTarget.note),
		noteRead(ActionTarget.note),
		noteDownload(ActionTarget.note),
		appointmentCreate(ActionTarget.appointment),
		appointmentContentUpdate(ActionTarget.appointment),
		appointmentTagsUpdate(ActionTarget.appointment),
		appointmentMemberAdd(ActionTarget.appointment),
		appointmentMemberRemove(ActionTarget.appointment),
		appointmentRolesUpdate(ActionTarget.appointment),
		appointmentReferenceAdd(ActionTarget.appointment),
		appointmentReferenceRemove(ActionTarget.appointment),
		appointmentOccurrenceDelete(ActionTarget.appointment),
		appointmentStatusDelete(ActionTarget.appointment),
		milestoneCreate(ActionTarget.milestone),
		milestoneContentUpdate(ActionTarget.milestone),
		milestoneTagsUpdate(ActionTarget.milestone),
		milestoneStatusDelete(ActionTarget.milestone);
		
		private final ActionTarget target;
		
		private Action(ActionTarget target) {
			this.target = target;
		}

		public ActionTarget getTarget() {
			return target;
		}
		
		public static Action updateTags(String type) {
			switch (type) {
			case ProjFile.TYPE: return fileTagsUpdate;
			case ProjToDo.TYPE: return toDoTagsUpdate;
			case ProjNote.TYPE: return noteTagsUpdate;
			case ProjAppointment.TYPE: return appointmentTagsUpdate;
			case ProjMilestone.TYPE: return milestoneTagsUpdate;
			default:
				throw new AssertException("No update tags project action for " + type);
			}
		}
			
		public static Action addMember(String type) {
			switch (type) {
			case ProjFile.TYPE: return fileMemberAdd;
			case ProjToDo.TYPE: return toDoMemberAdd;
			case ProjNote.TYPE: return noteMemberAdd;
			case ProjAppointment.TYPE: return appointmentMemberAdd;
			default:
				throw new AssertException("No add member project action for " + type);
			}
		}
		
		public static Action removeMember(String type) {
			switch (type) {
			case ProjFile.TYPE: return fileMemberRemove;
			case ProjToDo.TYPE: return toDoMemberRemove;
			case ProjNote.TYPE: return noteMemberRemove;
			case ProjAppointment.TYPE: return appointmentMemberRemove;
			default:
				throw new AssertException("No remove member project action for " + type);
			}
		}
		
		public static Action updateRoles(String type) {
			switch (type) {
			case ProjFile.TYPE: return fileRolesUpdate;
			case ProjToDo.TYPE: return toDoRolesUpdate;
			case ProjNote.TYPE: return noteRolesUpdate;
			case ProjAppointment.TYPE: return appointmentRolesUpdate;
			default:
				throw new AssertException("No update roles project action for " + type);
			}
		}
		
		public static Action addReference(String type) {
			switch (type) {
			case ProjFile.TYPE: return fileReferenceAdd;
			case ProjToDo.TYPE: return toDoReferenceAdd;
			case ProjNote.TYPE: return noteReferenceAdd;
			case ProjAppointment.TYPE: return appointmentReferenceAdd;
			default:
				throw new AssertException("No add reference project action for " + type);
			}
		}
		
		public static Action removeReference(String type) {
			switch (type) {
			case ProjFile.TYPE: return fileReferenceRemove;
			case ProjToDo.TYPE: return toDoReferenceRemove;
			case ProjNote.TYPE: return noteReferenceRemove;
			case ProjAppointment.TYPE: return appointmentReferenceRemove;
			default:
				throw new AssertException("No remove reference project action for " + type);
			}
		}

		public static Action read(String type) {
			switch (type) {
			case ProjFile.TYPE: return fileRead;
			case ProjNote.TYPE: return noteRead;
			default:
				throw new AssertException("No read project action for " + type);
			}
		}

		public static Action download(String type) {
			switch (type) {
			case ProjFile.TYPE: return fileDownload;
			case ProjNote.TYPE: return noteDownload;
			default:
				throw new AssertException("No download project action for " + type);
			}
		}
		
	}
	
	public enum ActionTarget {
		project,
		file,
		toDo,
		note,
		appointment,
		milestone;
		
		private static final Set<String> NAMES = Arrays.stream(values()).map(ActionTarget::name).collect(Collectors.toSet());
		
		public static boolean isValid(String name) {
			return NAMES.contains(name);
		}
	}

}
