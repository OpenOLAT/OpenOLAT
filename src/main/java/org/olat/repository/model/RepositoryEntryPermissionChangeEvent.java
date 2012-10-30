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

import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class RepositoryEntryPermissionChangeEvent extends Event {
	private static final long serialVersionUID = 8499004967313689825L;

	private final Identity member;
	
	private Boolean repoOwner;
	private Boolean repoTutor;
	private Boolean repoParticipant;
	
	public RepositoryEntryPermissionChangeEvent(Identity member) {
		super("id-perm-changed");
		this.member = member;
	}
	
	public RepositoryEntryPermissionChangeEvent(Identity member, RepositoryEntryPermissionChangeEvent origin) {
		this(member);
		repoOwner = origin.repoOwner;
		repoTutor = origin.repoTutor;
		repoParticipant = origin.repoParticipant;
	}
	
	public Identity getMember() {
		return member;
	}

	public Boolean getRepoOwner() {
		return repoOwner;
	}

	public void setRepoOwner(Boolean repoOwner) {
		this.repoOwner = repoOwner;
	}

	public Boolean getRepoTutor() {
		return repoTutor;
	}

	public void setRepoTutor(Boolean repoTutor) {
		this.repoTutor = repoTutor;
	}

	public Boolean getRepoParticipant() {
		return repoParticipant;
	}

	public void setRepoParticipant(Boolean repoParticipant) {
		this.repoParticipant = repoParticipant;
	}
	
	public int size() {
		return (repoOwner == null ? 0 : 1) + (repoTutor == null ? 0 : 1) + (repoParticipant == null ? 0 : 1);
	}
}