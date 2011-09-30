//<OLATCE-103>
/**
 * 
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * 
 * Copyright (c) 2005-2011 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 * 
 * All rights reserved.
 */
package de.bps.course.nodes.vc.provider.wimba;

import java.io.Serializable;

import de.bps.course.nodes.vc.DefaultVCConfiguration;

/**
 * 
 * Description:<br>
 * Configuration object for Wimba Classroom
 * 
 * <P>
 * Initial Date:  06.01.2011 <br>
 * @author skoeber
 */
public class WimbaClassroomConfiguration extends DefaultVCConfiguration implements Serializable {
	
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

	@Override
	public boolean isConfigValid() {
		// TODO implement logic
		return true;
	}

}
//</OLATCE-103>