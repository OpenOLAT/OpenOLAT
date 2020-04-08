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

import java.util.List;
import java.util.stream.Collectors;

import org.olat.modules.bigbluebutton.BigBlueButtonServer;

/**
 * 
 * Initial date: 8 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonServerInfos {
	
	private double load;
	private final BigBlueButtonServer server;
	private final List<BigBlueButtonMeetingInfos> meetingsInfos;
	
	public BigBlueButtonServerInfos(BigBlueButtonServer server, List<BigBlueButtonMeetingInfos> meetingsInfos, double load) {
		this.load = load;
		this.server = server;
		this.meetingsInfos = meetingsInfos;
	}
	
	public double getLoad() {
		return load;
	}
	
	public BigBlueButtonServer getServer() {
		return server;
	}
	
	public List<BigBlueButtonMeetingInfos> getMeetingsInfos() {
		return meetingsInfos;
	}
	
	public Long getModeratorCount() {
		return meetingsInfos.stream()
			.filter(BigBlueButtonMeetingInfos::isRunning)
			.collect(Collectors.summingLong(BigBlueButtonMeetingInfos::getModeratorCount));
	}
	
	public Long getParticipantCount() {
		return meetingsInfos.stream()
			.filter(BigBlueButtonMeetingInfos::isRunning)
			.collect(Collectors.summingLong(BigBlueButtonMeetingInfos::getParticipantCount));
	}
	
	public Long getListenerCount() {
		return meetingsInfos.stream()
			.filter(BigBlueButtonMeetingInfos::isRunning)
			.collect(Collectors.summingLong(BigBlueButtonMeetingInfos::getListenerCount));
	}
	
	public Long getVoiceParticipantCount() {
		return meetingsInfos.stream()
			.filter(BigBlueButtonMeetingInfos::isRunning)
			.collect(Collectors.summingLong(BigBlueButtonMeetingInfos::getVoiceParticipantCount));
	}
	
	public Long getVideoCount() {
		return meetingsInfos.stream()
			.filter(BigBlueButtonMeetingInfos::isRunning)
			.collect(Collectors.summingLong(BigBlueButtonMeetingInfos::getVideoCount));
	}
	
	public Long getMaxUsers() {
		return meetingsInfos.stream()
			.filter(BigBlueButtonMeetingInfos::isRunning)
			.collect(Collectors.summingLong(BigBlueButtonMeetingInfos::getMaxUsers));
	}
	
	public long getRecordingMeetings() {
		return meetingsInfos.stream()
			.filter(BigBlueButtonMeetingInfos::isRunning)
			.filter(BigBlueButtonMeetingInfos::isRecording)
			.count();
	}
	
	public long getBreakoutRecordingMeetings() {
		return meetingsInfos.stream()
			.filter(BigBlueButtonMeetingInfos::isRunning)
			.filter(BigBlueButtonMeetingInfos::isRecording)
			.filter(BigBlueButtonMeetingInfos::isBreakout)
			.count();
	}
}
