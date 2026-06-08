/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.committee.wizard;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Initial date: 21 oct. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Committee {
	
	private String role;
	private final List<CommitteeMember> members = new ArrayList<>();
	
	public Committee() {
		//
	}
	
	public String getRole() {
		return role;
	}
	
	public void setRole(String role) {
		this.role = role;
	}

	public List<CommitteeMember> getMembers() {
		return members;
	}
	
	public boolean hasMember(String email) {
		for(CommitteeMember member:members) {
			if(email.equalsIgnoreCase(member.getEmail())) {
				return true;
			}
		}
		return false;
	}
	
	public CommitteeMember getMember(String email) {
		for(CommitteeMember member:members) {
			if(email.equalsIgnoreCase(member.getEmail())) {
				return member;
			}
		}
		return null;
	}
	
	public void addMember(CommitteeMember member) {
		members.add(member);
	}
	
	public List<CommitteeMember> getMembersToComplete() {
		List<CommitteeMember> toComplete = new ArrayList<>(members.size());
		for(CommitteeMember member:members) {
			if(member.getIdentity() == null) {
				toComplete.add(member);
			}
		}
		return toComplete;
	}
	
	public boolean isComplete() {
		for(CommitteeMember member:members) {
			if((member.getIdentity() == null || member.getStatus() == null || member.getStatus() == CommitteeMemberStatus.notValid)
					&& member.getStatus() != CommitteeMemberStatus.skipped) {
				return false;
			}
		}
		return true;
	}
}
