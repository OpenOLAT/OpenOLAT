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
package org.olat.course.nodes.en;

import org.olat.group.BusinessGroupRef;
import org.olat.group.BusinessGroupStatusEnum;


/**
 * 
 * Initial date: 31.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EnrollmentRow implements BusinessGroupRef {
	
	private final Long groupKey;
	private final String name;
	private final String description;
	private final int maxParticipants;
	private final boolean waitingListEnabled;
	private final BusinessGroupStatusEnum groupStatus;
	
	private boolean waiting;
	private boolean participant;
	private int numInWaitingList;
	private int numOfParticipants;
	private int numOfReservations;
	private int positionInWaitingList;
	private int sortKey;
	
	public EnrollmentRow(Long groupKey, String name, String description, BusinessGroupStatusEnum groupStatus,
			int maxParticipants, boolean waitingListEnabled) {
		this.groupKey = groupKey;
		this.groupStatus = groupStatus;
		this.name = name;
		this.description = description;
		this.maxParticipants = maxParticipants;
		this.waitingListEnabled = waitingListEnabled;
	}
	
	@Override
	public Long getKey() {
		return groupKey;
	}
	
	public boolean isActive() {
		return groupStatus == BusinessGroupStatusEnum.active;
	}
	
	public int getSortKey() {
		return sortKey;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}

	public int getMaxParticipants() {
		return maxParticipants;
	}
	
	public boolean isWaitingListEnabled() {
		return waitingListEnabled;
	}

	public boolean isWaiting() {
		return waiting;
	}
	
	public void setWaiting(boolean waiting) {
		this.waiting = waiting;
	}
	
	public boolean isParticipant() {
		return participant;
	}
	
	public void setSortKey(int sortKey) {
		this.sortKey = sortKey;
	}
	
	public void setParticipant(boolean participant) {
		this.participant = participant;
	}
	
	public int getNumInWaitingList() {
		return numInWaitingList < 0 ? 0 : numInWaitingList;
	}
	
	public void setNumInWaitingList(int numInWaitingList) {
		this.numInWaitingList = numInWaitingList;
	}

	public int getNumOfParticipants() {
		return numOfParticipants < 0 ? 0 : numOfParticipants;
	}

	public void setNumOfParticipants(int numOfParticipants) {
		this.numOfParticipants = numOfParticipants;
	}

	public int getNumOfReservations() {
		return numOfReservations < 0 ? 0 : numOfReservations;
	}

	public void setNumOfReservations(int numOfReservations) {
		this.numOfReservations = numOfReservations;
	}
	
	public int getPositionInWaitingList() {
		return positionInWaitingList;
	}

	public void setPositionInWaitingList(int positionInWaitingList) {
		this.positionInWaitingList = positionInWaitingList;
	}
}
