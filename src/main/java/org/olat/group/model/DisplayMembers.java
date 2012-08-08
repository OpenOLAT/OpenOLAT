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

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class DisplayMembers {
	
	private boolean showOwners;
	private boolean showParticipants;
	private boolean showWaitingList;
	
	private boolean ownersPublic;
	private boolean participantsPublic;
	private boolean waitingListPublic;
	
	public DisplayMembers() {
		//
	}
	
	public DisplayMembers(boolean showOwners, boolean showParticipants, boolean showWaitingList) {
		this.showOwners = showOwners;
		this.showParticipants = showParticipants;
		this.showWaitingList = showWaitingList;
	}
	
	public boolean isShowOwners() {
		return showOwners;
	}
	
	public void setShowOwners(boolean showOwners) {
		this.showOwners = showOwners;
	}
	
	public boolean isShowParticipants() {
		return showParticipants;
	}
	
	public void setShowParticipants(boolean showParticipants) {
		this.showParticipants = showParticipants;
	}
	
	public boolean isShowWaitingList() {
		return showWaitingList;
	}
	
	public void setShowWaitingList(boolean showWaitingList) {
		this.showWaitingList = showWaitingList;
	}

	public boolean isOwnersPublic() {
		return ownersPublic;
	}

	public void setOwnersPublic(boolean ownersPublic) {
		this.ownersPublic = ownersPublic;
	}

	public boolean isParticipantsPublic() {
		return participantsPublic;
	}

	public void setParticipantsPublic(boolean participantsPublic) {
		this.participantsPublic = participantsPublic;
	}

	public boolean isWaitingListPublic() {
		return waitingListPublic;
	}

	public void setWaitingListPublic(boolean waitingListPublic) {
		this.waitingListPublic = waitingListPublic;
	}
}
