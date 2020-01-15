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
package org.olat.course.condition.model;

/**
 * 
 * Initial date: 9 Jan 2020<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 *
 */
public class GroupSelectionTableContentRow {
	private final Long key;
	private final String groupName;
	private final Integer takenPlaces;
	private final Integer places;
	private final Integer waitingList;
	private final Boolean password;
	
	public GroupSelectionTableContentRow(Long key, String groupName) {
		this.key = key;
		this.groupName = groupName;
		this.takenPlaces = null;
		this.places = null;
		this. waitingList = null;
		this.password = null;
	}
	
	public Long getKey() {
		return key;
	}
	
	public String getGroupName() {
		return groupName;
	}

	public Integer getTakenPlaces() {
		return takenPlaces;
	}

	public Integer getPlaces() {
		return places;
	}

	public Integer getWaitingList() {
		return waitingList;
	}

	public Boolean getPassword() {
		return password;
	}
}
