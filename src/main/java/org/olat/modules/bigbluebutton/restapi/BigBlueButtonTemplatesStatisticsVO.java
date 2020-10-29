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
package org.olat.modules.bigbluebutton.restapi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * Initial date: 29 oct. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "bigBlueButtonTemplatesStatisticsVO")
public class BigBlueButtonTemplatesStatisticsVO {
	
	private Integer rooms;
	private Integer maxParticipants;
	private Integer roomsWithRecord;
	private Integer maxParticipantsWithRecord;
	private Integer roomsWithAutoStartRecording;
	private Integer maxParticipantsWithAutoStartRecording;
	private Integer roomsWithBreakout;
	private Integer maxParticipantsWithBreakout;
	private Integer roomsWithWebcamsOnlyForModerator;
	private Integer maxParticipantsWithWebcamsOnlyForModerator;
	
	public BigBlueButtonTemplatesStatisticsVO() {
		//
	}
	
	public Integer getRooms() {
		return rooms;
	}

	public void setRooms(Integer rooms) {
		this.rooms = rooms;
	}

	public Integer getMaxParticipants() {
		return maxParticipants;
	}

	public void setMaxParticipants(Integer maxParticipants) {
		this.maxParticipants = maxParticipants;
	}

	public Integer getRoomsWithRecord() {
		return roomsWithRecord;
	}
	
	public void setRoomsWithRecord(Integer roomsWithRecord) {
		this.roomsWithRecord = roomsWithRecord;
	}
	
	public Integer getMaxParticipantsWithRecord() {
		return maxParticipantsWithRecord;
	}
	
	public void setMaxParticipantsWithRecord(Integer maxParticipantsWithRecord) {
		this.maxParticipantsWithRecord = maxParticipantsWithRecord;
	}
	
	public Integer getRoomsWithAutoStartRecording() {
		return roomsWithAutoStartRecording;
	}
	
	public void setRoomsWithAutoStartRecording(Integer roomsWithAutoStartRecording) {
		this.roomsWithAutoStartRecording = roomsWithAutoStartRecording;
	}
	
	public Integer getMaxParticipantsWithAutoStartRecording() {
		return maxParticipantsWithAutoStartRecording;
	}
	
	public void setMaxParticipantsWithAutoStartRecording(Integer maxParticipantsWithAutoStartRecording) {
		this.maxParticipantsWithAutoStartRecording = maxParticipantsWithAutoStartRecording;
	}

	public Integer getRoomsWithBreakout() {
		return roomsWithBreakout;
	}

	public void setRoomsWithBreakout(Integer roomsWithBreakout) {
		this.roomsWithBreakout = roomsWithBreakout;
	}

	public Integer getMaxParticipantsWithBreakout() {
		return maxParticipantsWithBreakout;
	}

	public void setMaxParticipantsWithBreakout(Integer maxParticipantsWithBreakout) {
		this.maxParticipantsWithBreakout = maxParticipantsWithBreakout;
	}

	public Integer getRoomsWithWebcamsOnlyForModerator() {
		return roomsWithWebcamsOnlyForModerator;
	}

	public void setRoomsWithWebcamsOnlyForModerator(Integer roomsWithWebcamsOnlyForModerator) {
		this.roomsWithWebcamsOnlyForModerator = roomsWithWebcamsOnlyForModerator;
	}

	public Integer getMaxParticipantsWithWebcamsOnlyForModerator() {
		return maxParticipantsWithWebcamsOnlyForModerator;
	}

	public void setMaxParticipantsWithWebcamsOnlyForModerator(Integer maxParticipantsWithWebcamsOnlyForModerator) {
		this.maxParticipantsWithWebcamsOnlyForModerator = maxParticipantsWithWebcamsOnlyForModerator;
	}
}
