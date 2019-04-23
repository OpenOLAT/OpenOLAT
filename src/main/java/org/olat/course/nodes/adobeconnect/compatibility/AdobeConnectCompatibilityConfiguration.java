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
package org.olat.course.nodes.adobeconnect.compatibility;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * Initial date: 4 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AdobeConnectCompatibilityConfiguration implements Serializable {

	private static final long serialVersionUID = -90842707525111572L;
	
	private String providerId;
	private boolean guestAccessAllowed;
	private boolean guestStartMeetingAllowed;
	
	private String templateKey;
	private boolean useMeetingDates;
	private boolean createMeetingImmediately;

	private List<MeetingCompatibilityDate> meetingDatas;
 
	public String getProviderId() {
		return providerId;
	}

	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}

	public boolean isGuestAccessAllowed() {
		return guestAccessAllowed;
	}
	
	public void setGuestAccessAllowed(boolean guestAccessAllowed) {
		this.guestAccessAllowed = guestAccessAllowed;
	}
	
	public boolean isGuestStartMeetingAllowed() {
		return guestStartMeetingAllowed;
	}
	
	public void setGuestStartMeetingAllowed(boolean guestStartMeetingAllowed) {
		this.guestStartMeetingAllowed = guestStartMeetingAllowed;
	}

	public String getTemplateKey() {
		return templateKey;
	}

	public void setTemplateKey(String templateKey) {
		this.templateKey = templateKey;
	}

	public boolean isUseMeetingDates() {
		return useMeetingDates;
	}

	public void setUseMeetingDates(boolean useMeetingDates) {
		this.useMeetingDates = useMeetingDates;
	}

	public boolean isCreateMeetingImmediately() {
		return createMeetingImmediately;
	}

	public void setCreateMeetingImmediately(boolean createMeetingImmediately) {
		this.createMeetingImmediately = createMeetingImmediately;
	}

	public List<MeetingCompatibilityDate> getMeetingDatas() {
		return meetingDatas;
	}

	public void setMeetingDatas(List<MeetingCompatibilityDate> meetingDatas) {
		this.meetingDatas = meetingDatas;
	}
}
