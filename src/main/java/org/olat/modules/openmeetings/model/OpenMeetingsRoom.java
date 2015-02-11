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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.openmeetings.model;



/**
 * 
 * Initial date: 07.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OpenMeetingsRoom {
	
	private long roomId;
	private String name;
	private String comment;
	private long type;
	private long size;
	private boolean moderated;
	private boolean audioOnly;
	private boolean recordingAllowed;
	private boolean closed;
	
	private transient OpenMeetingsRoomReference reference;
	private transient int numOfUsers;
	private transient String resourceName;
	
	public OpenMeetingsRoom() {
		//
	}
	
	public OpenMeetingsRoom(OpenMeetingsRoomReference reference) {
		this.reference = reference;
	}
	
	public long getRoomId() {
		return roomId;
	}
	
	public void setRoomId(long roomId) {
		this.roomId = roomId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getComment() {
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public long getType() {
		return type;
	}
	
	public void setType(long type) {
		this.type = type;
	}
	
	public long getSize() {
		return size;
	}
	
	public void setSize(long size) {
		this.size = size;
	}
	
	public boolean isModerated() {
		return moderated;
	}
	
	public void setModerated(boolean moderated) {
		this.moderated = moderated;
	}
	
	public boolean isAudioOnly() {
		return audioOnly;
	}
	
	public void setAudioOnly(boolean audioOnly) {
		this.audioOnly = audioOnly;
	}

	public boolean isRecordingAllowed() {
		return recordingAllowed;
	}

	public OpenMeetingsRoomReference getReference() {
		return reference;
	}

	public void setReference(OpenMeetingsRoomReference reference) {
		this.reference = reference;
	}

	public int getNumOfUsers() {
		return numOfUsers;
	}

	public void setNumOfUsers(int numOfUsers) {
		this.numOfUsers = numOfUsers;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}
	
	public void setClosed(Boolean closed) {
		this.closed = closed == null ? false : closed.booleanValue();
	}
}
