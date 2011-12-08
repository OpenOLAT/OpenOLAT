/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/
package org.olat.instantMessaging;

import org.olat.core.logging.StartupException;

/**
 * Description:<br>
 * IM configuration set by spring
 * 
 * <P>
 * Initial Date:  10.08.2008 <br>
 * @author guido
 */
public class IMConfig {
	
	private boolean enabled = false;
	private String servername;
	private boolean multipleInstances;
	private String replaceStringForEmailAt;
	private String adminUsername = "admin";
	private String adminPassword;
	private boolean generateTestUsers = false;
	private String CONFERENCE_PREFIX = "conference";
	private int idlePolltime;
	private int chatPolltime;
	private boolean syncPersonalGroups;
	private boolean syncLearningGroups;
	public static final String RESOURCE = "OLAT";
	// fxdiff: FXOLAT-46
	private boolean hideExternalClientInfo;
	private String adminName;
	private String nodeId;
	
	
	
	/**
	 * properties injected by spring
	 */
	public IMConfig() {
	//
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public String getServername() {
		return servername;
	}
	
	public void setServername(String servername) {
		this.servername = servername;
	}
	
	public boolean isMultipleInstances() {
		return multipleInstances;
	}
	
	public void setMultipleInstances(boolean multipleInstances) {
		this.multipleInstances = multipleInstances;
	}
	
	public String getReplaceStringForEmailAt() {
		return replaceStringForEmailAt;
	}
	
	public void setReplaceStringForEmailAt(String replaceStringForEmailAt) {
		if (replaceStringForEmailAt.contains("#") || replaceStringForEmailAt.contains("%")) {
			throw new StartupException("do not use '#' or '%' as replace string as they get url encoded!");
		}
		this.replaceStringForEmailAt = replaceStringForEmailAt;
	}
	
	public String getAdminPassword() {
		return adminPassword;
	}
	
	public void setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
	}
	
	public void setAdminName(String adminName) {
		this.adminName = adminName;
	}
	
	public String getAdminName(){
		return adminName;
	}
	
	public void setNodeId(String nodeId){
		this.nodeId = nodeId;
	}
	
	public String getNodeId(){
		return nodeId;
	}
	
	public boolean generateTestUsers() {
		return generateTestUsers;
	}
	
	public void setGenerateTestUsers(boolean generateTestUsers) {
		this.generateTestUsers = generateTestUsers;
	}
	
	public String getConferenceServer() {
		return CONFERENCE_PREFIX+"."+servername;
	}
	
	// attention! always returns the default admin-name "admin"
	public String getAdminUsername() {
		return adminUsername;
	}

	public int getIdlePolltime() {
		return idlePolltime;
	}

	public void setIdlePolltime(int idlePolltime) {
		this.idlePolltime = idlePolltime;
	}

	public int getChatPolltime() {
		return chatPolltime;
	}

	public void setChatPolltime(int chatPolltime) {
		this.chatPolltime = chatPolltime;
	}

	public boolean isSyncPersonalGroups() {
		return syncPersonalGroups;
	}

	public void setSyncPersonalGroups(boolean syncPersonalGroups) {
		this.syncPersonalGroups = syncPersonalGroups;
	}

	public boolean isSyncLearningGroups() {
		return syncLearningGroups;
	}

	public void setSyncLearningGroups(boolean syncLearningGroups) {
		this.syncLearningGroups = syncLearningGroups;
	}
	
	// fxdiff: FXOLAT-46
	/**
	 * @param hideExternalClientInfo The hideExternalClientInfo to set.
	 */
	public void setHideExternalClientInfo(boolean hideExternalClientInfo) {
		this.hideExternalClientInfo = hideExternalClientInfo;
	}

	/**
	 * @return Returns the hideExternalClientInfo.
	 */
	public boolean isHideExternalClientInfo() {
		return hideExternalClientInfo;
	}

}
