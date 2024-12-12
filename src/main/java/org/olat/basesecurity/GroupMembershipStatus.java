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
	
	public static final boolean allowedAsNextStep(GroupMembershipStatus current, GroupMembershipStatus next) {
		if(current == next) {
			return false; // Move forward
		}
		// Steps allowed to start the process
		if((current == null || current == cancel || current == cancelWithFee || current == removed || current == declined)
				&& (next == reservation || next == active)) {
			return true;
		}
		// Reservation can be accepted or declined
		if(current == reservation && (next == declined || next == active)) {
			return true;
		}
		// A membership can b canceled or removed
		if(current == active && (next == cancel || next == cancelWithFee || next == removed)) {
			return true;
		}
		return false;
	}

	/**
	 * help method to evaluate the states of the membership and the next possible steps.
	 * 
	 * @param status The status (can be null) to evaluate
	 * @return A status or null
	 */
	public static final GroupMembershipStatus[] possibleNextStatus(GroupMembershipStatus status) {
		if(status == null) {
			return new GroupMembershipStatus[] { reservation, active };
		}
		
		return switch(status) {
			case cancel, cancelWithFee, removed, declined -> new GroupMembershipStatus[] { reservation, active };
			case reservation -> new GroupMembershipStatus[] { declined, active };
			case active -> new GroupMembershipStatus[] { removed, cancel, cancelWithFee };
			default -> null;
		};
	}
}
