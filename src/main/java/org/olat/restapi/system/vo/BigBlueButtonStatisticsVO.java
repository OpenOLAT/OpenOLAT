package org.olat.restapi.system.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

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
