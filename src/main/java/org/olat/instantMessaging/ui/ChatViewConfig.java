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
package org.olat.instantMessaging.ui;

import java.util.List;

import org.olat.basesecurity.IdentityRef;

/**
 * 
 * Initial date: 1 mars 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ChatViewConfig {
	private String roomName;
	
	private String welcome;
	private String welcomeFrom;
	private String resourceInfos;
	private String resourceIconCssClass;
	
	private String errorMessage;
	
	private String sendMessagePlaceholder;

	private boolean canClose = false;
	private boolean canReactivate = false;
	private boolean canMeeting = false;
	private boolean assessmentAllowed = false;
	
	private boolean createRosterEntry = true;

	private List<IdentityRef> toNotifyRequests;
	
	private int width = 550;
	private int height = 380;
	private RosterFormDisplay rosterDisplay;
	
	public ChatViewConfig() {
		//
	}
	
	public static ChatViewConfig valueOf(ChatViewConfig original) {
		ChatViewConfig copy = new ChatViewConfig();
		copy.roomName = original.roomName;
		copy.welcome = original.welcome;
		copy.welcomeFrom = original.welcomeFrom;
		copy.resourceInfos = original.resourceInfos;
		copy.resourceIconCssClass = original.resourceIconCssClass;
		copy.errorMessage = original.errorMessage;
		copy.sendMessagePlaceholder = original.sendMessagePlaceholder;
		copy.toNotifyRequests = original.toNotifyRequests == null ? null : List.copyOf(original.toNotifyRequests);
		copy.canClose = original.canClose;
		copy.canReactivate = original.canReactivate;
		copy.canMeeting = original.canMeeting;
		copy.createRosterEntry = original.createRosterEntry;
		copy.width = original.width;
		copy.height = original.height;
		copy.rosterDisplay = original.rosterDisplay;
		return copy;
	}
	
	public static ChatViewConfig room(String roomName, RosterFormDisplay rosterDisplay) {
		ChatViewConfig cvc = new ChatViewConfig();
		cvc.setRoomName(roomName);
		cvc.setRosterDisplay(rosterDisplay);
		return cvc;
	}

	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	public String getWelcome() {
		return welcome;
	}

	public void setWelcome(String welcome) {
		this.welcome = welcome;
	}

	public String getWelcomeFrom() {
		return welcomeFrom;
	}

	public void setWelcomeFrom(String welcomeFrom) {
		this.welcomeFrom = welcomeFrom;
	}

	public String getResourceInfos() {
		return resourceInfos;
	}

	public void setResourceInfos(String resourceInfos) {
		this.resourceInfos = resourceInfos;
	}

	public String getResourceIconCssClass() {
		return resourceIconCssClass;
	}

	public void setResourceIconCssClass(String resourceIconCssClass) {
		this.resourceIconCssClass = resourceIconCssClass;
	}

	public String getSendMessagePlaceholder() {
		return sendMessagePlaceholder;
	}

	public void setSendMessagePlaceholder(String sendMessagePlaceholder) {
		this.sendMessagePlaceholder = sendMessagePlaceholder;
	}

	public boolean isCanClose() {
		return canClose;
	}

	public void setCanClose(boolean canClose) {
		this.canClose = canClose;
	}

	public boolean isCanReactivate() {
		return canReactivate;
	}

	public void setCanReactivate(boolean canReactivate) {
		this.canReactivate = canReactivate;
	}

	public boolean isCanMeeting() {
		return canMeeting;
	}

	public void setCanMeeting(boolean canMeeting) {
		this.canMeeting = canMeeting;
	}

	public boolean isAssessmentAllowed() {
		return assessmentAllowed;
	}

	public void setAssessmentAllowed(boolean assessmentAllowed) {
		this.assessmentAllowed = assessmentAllowed;
	}

	public List<IdentityRef> getToNotifyRequests() {
		return toNotifyRequests;
	}

	public void setToNotifyRequests(List<IdentityRef> toNotifyRequests) {
		this.toNotifyRequests = toNotifyRequests;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public RosterFormDisplay getRosterDisplay() {
		return rosterDisplay;
	}

	public void setRosterDisplay(RosterFormDisplay rosterDisplay) {
		this.rosterDisplay = rosterDisplay;
	}

	public boolean isCreateRosterEntry() {
		return createRosterEntry;
	}

	public void setCreateRosterEntry(boolean createRosterEntry) {
		this.createRosterEntry = createRosterEntry;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}
