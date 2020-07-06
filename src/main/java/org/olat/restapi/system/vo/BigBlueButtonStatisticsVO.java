package org.olat.restapi.system.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "bigBlueButtonStatisticsVO")
public class BigBlueButtonStatisticsVO {
	
	private long attendeeCount;
	private long meetingCount;
	private long recordingCount;
	private long videoCount;
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
