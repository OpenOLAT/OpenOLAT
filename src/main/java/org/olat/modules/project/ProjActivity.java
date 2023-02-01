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
			Action.projectContentContent,
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
			Action.noteCreate,
			Action.noteContentUpdate,
			Action.noteStatusDelete);
	
	
	public enum Action {
		projectCreate(ActionTarget.project),
		projectContentContent(ActionTarget.project),
		projectStatusActive(ActionTarget.project),
		projectStatusDone(ActionTarget.project),
		projectStatusDelete(ActionTarget.project),
		projectOrganisationAdd(ActionTarget.project),
		projectOrganisationRemove(ActionTarget.project),
		projectMemberAdd(ActionTarget.project),
		projectMemberRemove(ActionTarget.project),
		projectRolesUpdate(ActionTarget.project),
		projectRead(ActionTarget.project),
		fileCreate(ActionTarget.file),
		fileUpload(ActionTarget.file),
		fileContentUpdate(ActionTarget.file),
		fileMemberAdd(ActionTarget.file),
		fileMemberRemove(ActionTarget.file),
		fileRolesUpdate(ActionTarget.file),
		fileReferenceAdd(ActionTarget.file),
		fileReferenceRemove(ActionTarget.file),
		fileStatusDelete(ActionTarget.file),
		fileRead(ActionTarget.file),
		fileDownload(ActionTarget.file),
		fileEdit(ActionTarget.file),
		noteCreate(ActionTarget.note),
		noteContentUpdate(ActionTarget.note),
		noteMemberAdd(ActionTarget.note),
		noteMemberRemove(ActionTarget.note),
		noteRolesUpdate(ActionTarget.note),
		noteReferenceAdd(ActionTarget.note),
		noteReferenceRemove(ActionTarget.note),
		noteStatusDelete(ActionTarget.note),
		noteRead(ActionTarget.note),
		noteDownload(ActionTarget.note);
		
		private final ActionTarget target;
		
		private Action(ActionTarget target) {
			this.target = target;
		}

		public ActionTarget getTarget() {
			return target;
		}

		public static Action addMember(String type) {
			switch (type) {
			case ProjFile.TYPE: return fileMemberAdd;
			case ProjNote.TYPE: return noteMemberAdd;
			default:
				throw new AssertException("No add member project action for " + type);
			}
		}
		
		public static Action removeMember(String type) {
			switch (type) {
			case ProjFile.TYPE: return fileMemberRemove;
			case ProjNote.TYPE: return noteMemberRemove;
			default:
				throw new AssertException("No remove member project action for " + type);
			}
		}
		
		public static Action updateRoles(String type) {
			switch (type) {
			case ProjFile.TYPE: return fileRolesUpdate;
			case ProjNote.TYPE: return noteRolesUpdate;
			default:
				throw new AssertException("No update roles project action for " + type);
			}
		}
		
		public static Action addReference(String type) {
			switch (type) {
			case ProjFile.TYPE: return fileReferenceAdd;
			case ProjNote.TYPE: return noteReferenceAdd;
			default:
				throw new AssertException("No add reference project action for " + type);
			}
		}
		
		public static Action removeReference(String type) {
			switch (type) {
			case ProjFile.TYPE: return fileReferenceRemove;
			case ProjNote.TYPE: return noteReferenceRemove;
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
		note;
		
		private static final Set<String> NAMES = Arrays.stream(values()).map(ActionTarget::name).collect(Collectors.toSet());
		
		public static boolean isValid(String name) {
			return NAMES.contains(name);
		}
	}

}
