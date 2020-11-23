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

import org.olat.group.BusinessGroup;
import org.olat.group.model.StatisticsBusinessGroupRow;

/**
 * 
 * Initial date: 23 Dec 2019<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 *
 */
public class ENEditGroupTableContentRow {
	
	private final Long key;
	private final String groupName;
	private final String description;
	private final int minParticipants;
	private final int maxParticipants;
	private final int coaches;
	private final int participants;
	private final int onWaitinglist;
	private final boolean waitinglistEnabled;
	
	public ENEditGroupTableContentRow() {
		key = null;
		groupName = null;
		description = null;
		minParticipants = 0;
		maxParticipants = 0;
		participants = 0;
		coaches = 0;
		onWaitinglist = 0;
		waitinglistEnabled = false;
	}
	
	public ENEditGroupTableContentRow(BusinessGroup group, EnrollmentRow enrollment) {
		this.key = group.getKey();
		this.groupName = group.getName();
		this.description = group.getDescription() != null ? group.getDescription() : "";
		this.maxParticipants = group.getMaxParticipants() != null ? group.getMaxParticipants() : -1;
		this.minParticipants = group.getMinParticipants() != null ? group.getMinParticipants() : -1;
		this.waitinglistEnabled = group.getWaitingListEnabled();
		this.onWaitinglist = enrollment.getNumInWaitingList();
		this.participants = enrollment.getNumOfParticipants();
		this.coaches = -1;
	}
	
	public ENEditGroupTableContentRow(BusinessGroup group, StatisticsBusinessGroupRow stats) {
		this.key = group.getKey();
		this.groupName = group.getName();
		this.description = group.getDescription() != null ? group.getDescription() : "";
		this.maxParticipants = group.getMaxParticipants() != null ? group.getMaxParticipants() : -1;
		this.minParticipants = group.getMinParticipants() != null ? group.getMinParticipants() : -1;
		this.waitinglistEnabled = group.getWaitingListEnabled();
		this.onWaitinglist = stats.getNumWaiting();
		this.participants = stats.getNumOfParticipants();
		this.coaches = stats.getNumOfCoaches();
	}
	
	
	public Long getKey() {
		return key;
	}
	
	public String getGroupName() {
		return groupName;
	}
	
	public String getDescription() {
		return description;
	}

	public String getMinParticipants() {
		return minParticipants > -1 ? String.valueOf(minParticipants) : " - ";
	}

	public String getMaxParticipants() {
		return maxParticipants > -1 ? String.valueOf(maxParticipants) : " - ";
	}

	public int getCoaches() {
		return coaches;
	}

	public int getParticipants() {
		return participants;
	}

	public int getOnWaitinglist() {
		return onWaitinglist;
	}

	public boolean isWaitinglistEnabled() {
		return waitinglistEnabled;
	}
	
	
}
