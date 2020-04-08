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
package org.olat.modules.bigbluebutton.ui;

import org.olat.modules.bigbluebutton.BigBlueButtonServer;
import org.olat.modules.bigbluebutton.model.BigBlueButtonServerInfos;

/**
 * 
 * Initial date: 8 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonServerRow {
	
	private final BigBlueButtonServer server;
	private final BigBlueButtonServerInfos serverInfos;
	
	public BigBlueButtonServerRow(BigBlueButtonServer server, BigBlueButtonServerInfos serverInfos) {
		this.server = server;
		this.serverInfos = serverInfos;
	}
	
	public String getUrl() {
		return server.getUrl();
	}
	
	public boolean isEnabled() {
		return server.isEnabled();
	}
	
	public Double getCapacityFactor() {
		return server.getCapacityFactory();
	}
	
	public double getLoad() {
		return serverInfos.getLoad();
	}
	
	public Long getModeratorCount() {
		return serverInfos.getModeratorCount();
	}
	
	public Long getParticipantCount() {
		return serverInfos.getParticipantCount();
	}

	public Long getListenerCount() {
		return serverInfos.getListenerCount();
	}
	
	public Long getVoiceParticipantCount() {
		return serverInfos.getVoiceParticipantCount();
	}
	
	public Long getVideoCount() {
		return serverInfos.getVideoCount();
	}
	
	public Long getMaxUsers() {
		return serverInfos.getMaxUsers();
	}
	
	public Long getRecordingMeetings() {
		return serverInfos.getRecordingMeetings();
	}
	
	public Long getBreakoutRecordingMeetings() {
		return serverInfos.getBreakoutRecordingMeetings();
	}

}
