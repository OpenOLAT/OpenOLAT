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
package org.olat.repository.model;

import org.olat.repository.RepositoryEntrySecurity;

/**
 * 
 * Initial date: 19.08.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntrySecurityImpl implements RepositoryEntrySecurity {
	
	private final boolean owner;
	private final boolean canLaunch;
	private final boolean entryAdmin;
	private final boolean readOnly;
	private final boolean author;
	private final boolean principal;
	private final boolean masterCoach;
	
	private final boolean courseParticipant;
	private final boolean courseCoach;
	private final boolean groupParticipant;
	private final boolean groupCoach;
	private final boolean groupWaiting;
	private final boolean curriculumParticipant;
	private final boolean curriculumCoach;
	
	
	public RepositoryEntrySecurityImpl(boolean entryAdmin, boolean owner,
			boolean courseParticipant, boolean courseCoach,
			boolean groupParticipant, boolean groupCoach, boolean groupWaiting,
			boolean curriculumParticipant, boolean curriculumCoach, boolean masterCoach,
			boolean author, boolean principal,
			boolean canLaunch, boolean readOnly) {
		this.owner = owner;
		this.canLaunch = canLaunch;
		this.entryAdmin = entryAdmin;
		this.author = author;
		this.principal = principal;
		this.masterCoach = masterCoach;
		
		this.courseParticipant = courseParticipant;
		this.courseCoach = courseCoach;
		this.groupParticipant = groupParticipant;
		this.groupCoach = groupCoach;
		this.groupWaiting = groupWaiting;
		this.curriculumParticipant = curriculumParticipant;
		this.curriculumCoach = curriculumCoach;
		this.readOnly = readOnly;
	}
	
	@Override
	public boolean isOwner() {
		return owner;
	}
	
	/**
	 * 
	 * @return true if the user is coach of the repository entry, a group
	 *   or a curriculum element linked to the repository entry.
	 */
	@Override
	public boolean isCoach() {
		return courseCoach || groupCoach || curriculumCoach;
	}
	
	/**
	 * 
	 * @return true if the user is participant of the repository entry, a group
	 *   or a curriculum element linked to the repository entry.
	 */
	@Override
	public boolean isParticipant() {
		return courseParticipant || groupParticipant || curriculumParticipant;
	}
	
	@Override
	public boolean isMasterCoach() {
		return masterCoach;
	}
	
	@Override
	public boolean isEntryAdmin() {
		return entryAdmin;
	}
	
	@Override
	public boolean canLaunch() {
		return canLaunch;
	}
	
	@Override
	public boolean isReadOnly() {
		return readOnly;
	}

	@Override
	public boolean isCourseParticipant() {
		return courseParticipant;
	}

	@Override
	public boolean isCourseCoach() {
		return courseCoach;
	}

	@Override
	public boolean isGroupParticipant() {
		return groupParticipant;
	}

	@Override
	public boolean isGroupCoach() {
		return groupCoach;
	}

	@Override
	public boolean isGroupWaiting() {
		return groupWaiting;
	}

	@Override
	public boolean isCurriculumParticipant() {
		return curriculumParticipant;
	}

	@Override
	public boolean isCurriculumCoach() {
		return curriculumCoach;
	}

	@Override
	public boolean isMember() {
		return owner || courseParticipant || courseCoach || groupParticipant || groupCoach || curriculumParticipant || curriculumCoach;
	}
	
	/**
	 * @return true if the user has the role author in an organization
	 * 		linked by the repository entry
	 */
	@Override
	public boolean isAuthor() {
		return author;
	}
	
	/**
	 * @return true if the user has the role principal
	 */
	@Override
	public boolean isPrincipal() {
		return principal;
	}
	
	/**
	 * @return true if the user has the role principal but
	 * 		is not a member or an administrator of the repository
	 * 		entry.
	 */
	@Override
	public boolean isOnlyPrincipal() {
		return principal && !isMember() && !isEntryAdmin();
	}
	
	@Override
	public boolean isOnlyMasterCoach() {
		return masterCoach && !isMember() && !isEntryAdmin();
	}
}
