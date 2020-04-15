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
package org.olat.modules.bigbluebutton.model;

import java.util.Date;

/**
 * 
 * Initial date: 8 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonMeetingInfos {
	
	private final String meetingId;
	private final boolean running;
	private final boolean hasBeenForciblyEnded;
	
	private Date startTime;
	
	private long videoCount;
	private long listenerCount;
	private long voiceParticipantCount;

	private long participantCount;
	private long moderatorCount;
	
	private boolean recording;
	private boolean breakout;
	
	private long maxUsers;

	private double load;
	
	public BigBlueButtonMeetingInfos(String meetingId, boolean running, boolean hasBeenForciblyEnded) {
		this.meetingId = meetingId;
		this.running = running;
		this.hasBeenForciblyEnded = hasBeenForciblyEnded;
	}
	
	public String getMeetingId() {
		return meetingId;
	}
	
	public boolean isRunning() {
		return running;
	}

	public boolean isHasBeenForciblyEnded() {
		return hasBeenForciblyEnded;
	}
	
	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public long getVideoCount() {
		return videoCount;
	}

	public void setVideoCount(long videoCount) {
		this.videoCount = videoCount;
	}

	public long getListenerCount() {
		return listenerCount;
	}

	public void setListenerCount(long listenerCount) {
		this.listenerCount = listenerCount;
	}

	public long getVoiceParticipantCount() {
		return voiceParticipantCount;
	}

	public void setVoiceParticipantCount(long voiceParticipantCount) {
		this.voiceParticipantCount = voiceParticipantCount;
	}

	public long getParticipantCount() {
		return participantCount;
	}

	public void setParticipantCount(long participantCount) {
		this.participantCount = participantCount;
	}

	public long getModeratorCount() {
		return moderatorCount;
	}

	public void setModeratorCount(long moderatorCount) {
		this.moderatorCount = moderatorCount;
	}

	public boolean isRecording() {
		return recording;
	}

	public void setRecording(boolean recording) {
		this.recording = recording;
	}

	public boolean isBreakout() {
		return breakout;
	}

	public void setBreakout(boolean breakout) {
		this.breakout = breakout;
	}

	public long getMaxUsers() {
		return maxUsers;
	}

	public void setMaxUsers(long maxUsers) {
		this.maxUsers = maxUsers;
	}

	public double getLoad() {
		return load;
	}

	public void setLoad(double load) {
		this.load = load;
	}

	@Override
	public int hashCode() {
		return meetingId == null ? 127846 : meetingId.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof BigBlueButtonMeetingInfos) {
			BigBlueButtonMeetingInfos meeting = (BigBlueButtonMeetingInfos)obj;
			return meetingId != null && meetingId.equals(meeting.getMeetingId());
		}
		return false;
	}
}
