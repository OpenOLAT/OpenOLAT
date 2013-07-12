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
package org.olat.group.ui.main;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CourseMembership {
	
	private boolean repoOwner;
	private boolean repoTutor;
	private boolean repoParticipant;
	private boolean groupTutor;
	private boolean groupParticipant;
	private boolean groupWaiting;
	private boolean pending;
	private boolean managedMembersRepo;
	
	public CourseMembership() {
		//
	}

	public boolean isOwner() {
		return repoOwner;
	}
	
	public boolean isTutor() {
		return repoTutor || groupTutor;
	}
	
	public boolean isParticipant() {
		return repoParticipant || groupParticipant;
	}

	public boolean isWaiting() {
		return groupWaiting;
	}

	public boolean isPending() {
		return pending;
	}

	public void setPending(boolean pending) {
		this.pending = pending;
	}

	public boolean isManagedMembersRepo() {
		return managedMembersRepo;
	}

	public void setManagedMembersRepo(boolean managedMembersRepo) {
		this.managedMembersRepo = managedMembersRepo;
	}

	public boolean isRepoOwner() {
		return repoOwner;
	}
	
	public void setRepoOwner(boolean repoOwner) {
		this.repoOwner = repoOwner;
	}

	public boolean isRepoTutor() {
		return repoTutor;
	}

	public void setRepoTutor(boolean repoTutor) {
		this.repoTutor = repoTutor;
	}

	public boolean isRepoParticipant() {
		return repoParticipant;
	}

	public void setRepoParticipant(boolean repoParticipant) {
		this.repoParticipant = repoParticipant;
	}

	public boolean isGroupTutor() {
		return groupTutor;
	}

	public void setGroupTutor(boolean groupTutor) {
		this.groupTutor = groupTutor;
	}

	public boolean isGroupParticipant() {
		return groupParticipant;
	}

	public void setGroupParticipant(boolean groupParticipant) {
		this.groupParticipant = groupParticipant;
	}

	public boolean isGroupWaiting() {
		return groupWaiting;
	}

	public void setGroupWaiting(boolean groupWaiting) {
		this.groupWaiting = groupWaiting;
	}
}