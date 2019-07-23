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
package org.olat.group.model;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.id.Identity;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MembershipModification {

	private List<Identity> addOwners = new ArrayList<>();
	private List<Identity> addParticipants = new ArrayList<>();
	private List<Identity> addToWaitingList = new ArrayList<>();
	private List<Identity> removedIdentities = new ArrayList<>();
	
	public List<Identity> getAddOwners() {
		return addOwners;
	}
	
	public void setAddOwners(List<Identity> addOwnerIdentities) {
		this.addOwners = addOwnerIdentities;
	}
	
	public List<Identity> getAddParticipants() {
		return addParticipants;
	}
	
	public void setAddParticipants(List<Identity> addParticipantIdentities) {
		this.addParticipants = addParticipantIdentities;
	}
	
	public List<Identity> getAddToWaitingList() {
		return addToWaitingList;
	}

	public void setAddToWaitingList(List<Identity> addToWaitingList) {
		this.addToWaitingList = addToWaitingList;
	}

	public List<Identity> getRemovedIdentities() {
		return removedIdentities;
	}
	
	public void setRemovedIdentities(List<Identity> removedIdentities) {
		this.removedIdentities = removedIdentities;
	}
	
	public boolean isEmpty() {
		return addOwners.isEmpty() && addParticipants.isEmpty()
				&& addToWaitingList.isEmpty() && removedIdentities.isEmpty();
	}
	
	public int size() {
		return addOwners.size() + addParticipants.size() + addToWaitingList.size() + removedIdentities.size();
	}
	
	public List<Identity> getAllIdentities() {
		List<Identity> allIdentities = new ArrayList<>();
		allIdentities.addAll(addOwners);
		allIdentities.addAll(addParticipants);
		allIdentities.addAll(addToWaitingList);
		allIdentities.addAll(removedIdentities);
		return allIdentities;
	}
}