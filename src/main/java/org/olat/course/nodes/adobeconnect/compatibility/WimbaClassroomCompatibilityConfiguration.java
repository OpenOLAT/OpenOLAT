//<OLATCE-103>
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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package org.olat.course.nodes.adobeconnect.compatibility;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * Description:<br>
 * Configuration object for Wimba Classroom
 * 
 * <P>
 * Initial Date:  06.01.2011 <br>
 * @author skoeber
 */
public class WimbaClassroomCompatibilityConfiguration implements Serializable {

	private static final long serialVersionUID = -5335478926224404373L;
	
	/** presentation tools are available only to instructors or to both students and instructors */
	private boolean toolsToStudentsEnabled;
	/** allow students to speak by default */
	private boolean studentsSpeakAllowed;
	/** allow students to show their video by default */
	private boolean studentsVideoAllowed;
	/** enable students to use text chat */
	private boolean studentsChatAllowed;
	/** enable private text chat among student */
	private boolean studentsPrivateChatAllowed;
	/** enable user status indicators */
	private boolean userStatusIndicatorsEnabled;
	/** enable user status updates appear in chat */
	private boolean userStatusUpdateInChatEnabled;
	/** enable students to use the eBoard by default */
	private boolean studentEBoardEnabled;
	/** enable breakout rooms */
	private boolean breakoutRoomsEnabled;
	/** enable archiving */
	private boolean archivingEnabled;
	/** automatically open new archives */
	private boolean autoOpenNewArchives;
	/** enable appshare */
	private boolean appshareEnabled;
	/** enable on-the-fly PowerPoint import */
	private boolean powerPointImportEnabled;
	/** enable guest access */
	private boolean guestAccessAllowed;
	/** enable registered users access */
	private boolean regUsersAccessAllowed;
	
	private String providerId;
	private String templateKey;
	private List<MeetingCompatibilityDate> meetingDatas;
	private boolean useMeetingDates;
	private boolean createMeetingImmediately;
	
	/* be compatible with old configuration versions */
	@SuppressWarnings("unused")	private transient boolean chatEnabled;
	@SuppressWarnings("unused")	private transient boolean privateChatEnabled;

	public boolean isToolsToStudentsEnabled() {
		return toolsToStudentsEnabled;
	}

	public boolean isStudentsSpeakAllowed() {
		return studentsSpeakAllowed;
	}

	public boolean isStudentsVideoAllowed() {
		return studentsVideoAllowed;
	}

	public boolean isUserStatusIndicatorsEnabled() {
		return userStatusIndicatorsEnabled;
	}

	public boolean isUserStatusUpdateInChatEnabled() {
		return userStatusUpdateInChatEnabled;
	}

	public boolean isStudentEBoardEnabled() {
		return studentEBoardEnabled;
	}

	public boolean isBreakoutRoomsEnabled() {
		return breakoutRoomsEnabled;
	}

	public boolean isArchivingEnabled() {
		return archivingEnabled;
	}
	
	public boolean isAutoOpenNewArchives() {
		return autoOpenNewArchives;
	}

	public boolean isAppshareEnabled() {
		return appshareEnabled;
	}

	public boolean isPowerPointImportEnabled() {
		return powerPointImportEnabled;
	}

	public boolean isGuestAccessAllowed() {
		return guestAccessAllowed;
	}

	public boolean isRegUsersAccessAllowed() {
		return regUsersAccessAllowed;
	}

	public void setToolsToStudentsEnabled(boolean toolsToStudentsEnabled) {
		this.toolsToStudentsEnabled = toolsToStudentsEnabled;
	}

	public void setStudentsSpeakAllowed(boolean studentsSpeakAllowed) {
		this.studentsSpeakAllowed = studentsSpeakAllowed;
	}

	public void setStudentsVideoAllowed(boolean studentsVideoAllowed) {
		this.studentsVideoAllowed = studentsVideoAllowed;
	}

	public void setUserStatusIndicatorsEnabled(boolean userStatusIndicatorsEnabled) {
		this.userStatusIndicatorsEnabled = userStatusIndicatorsEnabled;
	}

	public void setUserStatusUpdateInChatEnabled(boolean userStatusUpdateInChatEnabled) {
		this.userStatusUpdateInChatEnabled = userStatusUpdateInChatEnabled;
	}

	public void setStudentEBoardEnabled(boolean studentEBoardEnabled) {
		this.studentEBoardEnabled = studentEBoardEnabled;
	}

	public void setBreakoutRoomsEnabled(boolean breakoutRoomsEnabled) {
		this.breakoutRoomsEnabled = breakoutRoomsEnabled;
	}

	public void setArchivingEnabled(boolean archivingEnabled) {
		this.archivingEnabled = archivingEnabled;
	}
	
	public void setAutoOpenNewArchives(boolean autoOpenNewArchives) {
		this.autoOpenNewArchives = autoOpenNewArchives;
	}

	public void setAppshareEnabled(boolean appshareEnabled) {
		this.appshareEnabled = appshareEnabled;
	}

	public void setPowerPointImportEnabled(boolean powerPointImportEnabled) {
		this.powerPointImportEnabled = powerPointImportEnabled;
	}

	public void setGuestAccessAllowed(boolean guestAccessAllowed) {
		this.guestAccessAllowed = guestAccessAllowed;
	}

	public void setRegUsersAccessAllowed(boolean regUsersAccessAllowed) {
		this.regUsersAccessAllowed = regUsersAccessAllowed;
	}

	public void setStudentsChatAllowed(boolean studentsChatAllowed) {
		this.studentsChatAllowed = studentsChatAllowed;
	}

	public boolean isStudentsChatAllowed() {
		return studentsChatAllowed;
	}

	public void setStudentsPrivateChatAllowed(boolean studentsPrivateChatAllowed) {
		this.studentsPrivateChatAllowed = studentsPrivateChatAllowed;
	}

	public boolean isStudentsPrivateChatAllowed() {
		return studentsPrivateChatAllowed;
	}

	public String getProviderId() {
		return providerId;
	}

	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}

	public String getTemplateKey() {
		return templateKey;
	}

	public void setTemplateKey(String templateKey) {
		this.templateKey = templateKey;
	}

	public List<MeetingCompatibilityDate> getMeetingDatas() {
		return meetingDatas;
	}

	public void setMeetingDatas(List<MeetingCompatibilityDate> meetingDatas) {
		this.meetingDatas = meetingDatas;
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

	public boolean isChatEnabled() {
		return chatEnabled;
	}

	public void setChatEnabled(boolean chatEnabled) {
		this.chatEnabled = chatEnabled;
	}

	public boolean isPrivateChatEnabled() {
		return privateChatEnabled;
	}

	public void setPrivateChatEnabled(boolean privateChatEnabled) {
		this.privateChatEnabled = privateChatEnabled;
	}

	public boolean isConfigValid() {
		return true;
	}

}