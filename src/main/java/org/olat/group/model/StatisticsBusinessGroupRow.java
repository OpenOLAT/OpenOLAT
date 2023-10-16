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
 * Initial date: 29.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class StatisticsBusinessGroupRow extends BusinessGroupRow {
	
	private final int numOfCoaches;
	private final int numOfParticipants;
	private final int numWaiting;
	private final int numOfCoachReservations;
	private final int numOfParticipantReservations;
	
	public StatisticsBusinessGroupRow(BusinessGroupToSearch businessGroup,
			Number coaches, Number participants, Number waiting,
			Number numCoachReservations, Number numParticipantReservations) {
		super(businessGroup);
		numOfCoaches = coaches == null ? 0 : coaches.intValue();
		numOfParticipants = participants == null ? 0 : participants.intValue();
		numWaiting = waiting == null ? 0 : waiting.intValue();
		numOfCoachReservations = numCoachReservations == null ? 0 : numCoachReservations.intValue();
		numOfParticipantReservations = numParticipantReservations == null ? 0 : numParticipantReservations.intValue();
	}

	public int getNumOfCoaches() {
		return numOfCoaches;
	}
	
	public int getNumOfParticipants() {
		return numOfParticipants;
	}
	
	public int getNumWaiting() {
		return numWaiting;
	}
	
	public int getNumPending() {
		return numOfCoachReservations + numOfParticipantReservations;
	}

	public int getNumOfCoachReservations() {
		return numOfCoachReservations;
	}

	public int getNumOfParticipantReservations() {
		return numOfParticipantReservations;
	}
}
