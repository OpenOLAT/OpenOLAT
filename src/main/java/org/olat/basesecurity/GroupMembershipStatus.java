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
package org.olat.basesecurity;

import org.olat.modules.curriculum.CurriculumRoles;

/**
 * 
 * Initial date: 1 nov. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum GroupMembershipStatus {
	
	booking,
	parentBooking,
	transfer,
	reservation,
	active,
	declined,
	cancel,
	cancelWithFee,
	resourceDeleted,
	finished,
	removed;
	
	public static final GroupMembershipStatus[] statusWithNextStep() {
		return new GroupMembershipStatus[] { cancel, cancelWithFee, removed, declined, active, reservation };
	}
	
	public static final boolean allowedAsNextStep(GroupMembershipStatus current, GroupMembershipStatus next, CurriculumRoles roles) {
		if(current == next) {
			return false; // Move forward
		}
		// Steps allowed to start the process
		if((current == null || current == cancel || current == cancelWithFee || current == removed || current == declined)) {
			return (next == reservation && roles == CurriculumRoles.participant) || (next == active);
		}
		// Reservation can be accepted or declined
		if(current == reservation) {
			return (next == declined || next == active || next == cancel || next == cancelWithFee);
		}
		// A membership can be canceled or removed
		if(current == active) {
			return (next == cancel || next == cancelWithFee || next == removed);
		}
		return false;
	}

	/**
	 * help method to evaluate the states of the membership and the next possible steps.
	 * 
	 * @param status The status (can be null) to evaluate
	 * @return A status or null
	 */
	public static final GroupMembershipStatus[] possibleNextStatus(GroupMembershipStatus status, CurriculumRoles roles) {
		if(status == null) {
			return roles == CurriculumRoles.participant
					? new GroupMembershipStatus[] { reservation, active }
					: new GroupMembershipStatus[] { active };
		}
		
		return switch(status) {
			case cancel, cancelWithFee, removed, declined -> roles == CurriculumRoles.participant
					? new GroupMembershipStatus[] { reservation, active }
					: new GroupMembershipStatus[] { reservation };
			case reservation -> new GroupMembershipStatus[] { active, declined };
			case active -> new GroupMembershipStatus[] { removed };// cancelled and cancelled with fee worked with a separate process
			default -> null;
		};
	}
}
