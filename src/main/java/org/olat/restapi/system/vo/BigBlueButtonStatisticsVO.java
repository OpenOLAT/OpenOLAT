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
package org.olat.restapi.system.vo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "bigBlueButtonStatisticsVO")
public class BigBlueButtonStatisticsVO {
	
	@XmlAttribute(name="attendeeCount", required=false)
	private long attendeeCount;
	@XmlAttribute(name="meetingCount", required=false)
	private long meetingCount;
	@XmlAttribute(name="recordingCount", required=false)
	private long recordingCount;
	@XmlAttribute(name="videoCount", required=false)
	private long videoCount;
	@XmlAttribute(name="maximalUserCount", required=false)
	private long maximalUserCount;
	
	
	public long getAttendeesCount() {
		return attendeeCount;
	}

	public void setAttendeesCount(long attendees) {
		this.attendeeCount = attendees;
	}
	
	public long getMeetingsCount() {
		return meetingCount;
	}

	public void setMeetingsCount(long meetings) {
		this.meetingCount = meetings;
	}
	
	public long getRecordingCount() {
		return recordingCount;
	}

	public void setRecordingCount(long recordingCount) {
		this.recordingCount = recordingCount;
	}
	
	public long getVideoCount() {
		return videoCount;
	}
	
	public void setVideoCount(long videoCount) {
		this.videoCount = videoCount;
	}
	
	public long getCapacity() {
		return maximalUserCount;
	}
	
	public void setCapacity(long capacity) {
		this.maximalUserCount = capacity;
	}
}
