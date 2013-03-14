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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.instantMessaging;

import org.olat.core.configuration.AbstractOLATModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.configuration.PersistedProperties;
import org.olat.core.util.StringHelper;
import org.olat.core.util.event.GenericEventListener;


/**
 * Description: <br />
 * For configuration see olat.properties and put overwrite values in 
 * olat.local.properties or directly edit spring config of instant messaging
 * <P>
 * Initial Date: 14.10.2004
 * 
 * @author Guido Schnider
 */
public class InstantMessagingModule extends AbstractOLATModule implements ConfigOnOff, GenericEventListener {

	private static final String CONFIG_ENABLED = "im.enabled";
	private static final String CONFIG_GROUP_ENABLED = "im.enabled.group";
	private static final String CONFIG_COURSE_ENABLED = "im.enabled.course";
	private static final String CONFIG_PRIVATE_ENABLED = "im.enabled.private";
	private static final String CONFIG_ONLINESTATUS_ENABLED = "im.enabled.onlinestatus";
	private static final String CONFIG_GROUPPEERS_ENABLED = "im.enabled.grouppeers";

	private boolean enabled = false;
	private boolean groupEnabled = false;
	private boolean courseEnabled = false;
	private boolean privateEnabled = false;
	private boolean onlineStatusEnabled = false;
	private boolean groupPeersEnabled = false;

	public void init() {
		String enabledObj = getStringPropertyValue(CONFIG_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_GROUP_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			groupEnabled = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_COURSE_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			courseEnabled = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_PRIVATE_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			privateEnabled = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_ONLINESTATUS_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			onlineStatusEnabled = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_GROUPPEERS_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			groupPeersEnabled = "true".equals(enabledObj);
		}
	}

	@Override
	protected void initDefaultProperties() {
		enabled = getBooleanConfigParameter(CONFIG_ENABLED, true);
		groupEnabled = getBooleanConfigParameter(CONFIG_GROUP_ENABLED, true);
		courseEnabled = getBooleanConfigParameter(CONFIG_COURSE_ENABLED, true);
		privateEnabled = getBooleanConfigParameter(CONFIG_PRIVATE_ENABLED, true);
		onlineStatusEnabled = getBooleanConfigParameter(CONFIG_ONLINESTATUS_ENABLED, true);
		groupPeersEnabled = getBooleanConfigParameter(CONFIG_GROUPPEERS_ENABLED, true);
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}
	
	@Override
	public void setPersistedProperties(PersistedProperties persistedProperties) {
		this.moduleConfigProperties = persistedProperties;
	}

	/**
	 * Global flag to turn the IM module on and off
	 * @return true: the IM module is enabled; false: IM functionality is not enabled
	 */
	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		setStringProperty(CONFIG_ENABLED, Boolean.toString(enabled), true);
	}

	/**
	 * Flag to enable/disable the group chat. If enabled, group users are
	 * allowed to chat in the group chat room. See isPrivateEnabled() and
	 * isGroupPeersEnabled() to check if group users are allowed to send private
	 * messages to each others.
	 * 
	 * @return true: the group chat tool is enabled; false: the group chat tool
	 *         is disabled
	 */
	public boolean isGroupEnabled() {
		return groupEnabled;
	}

	public void setGroupEnabled(boolean enabled) {
		setStringProperty(CONFIG_GROUP_ENABLED, Boolean.toString(enabled), true);
	}

	/**
	 * Flag to enable/disable the course chat. If enabled, the course users are
	 * allowed to chat in the course chat room. See isPrivateEnabled() to check
	 * if users are allowed to send private messages to each others.
	 * 
	 * @return true: course chat room can be used; false: no course chat rooms
	 *         available
	 */
	public boolean isCourseEnabled() {
		return courseEnabled;
	}

	public void setCourseEnabled(boolean enabled) {
		setStringProperty(CONFIG_COURSE_ENABLED, Boolean.toString(enabled), true);
	}

	/**
	 * Flag to enable/disable private messaging between any user. When enabled
	 * users are allowed to send private messages to other users.
	 * 
	 * @return true: private messaging between users enabled; false: private
	 *         messaging between users disabled
	 */
	public boolean isPrivateEnabled() {
		return privateEnabled;
	}

	public void setPrivateEnabled(boolean enabled) {
		setStringProperty(CONFIG_PRIVATE_ENABLED, Boolean.toString(enabled), true);
	}

	/**
	 * Flag to enable/disable the visibility of the online status. This has only
	 * effect in conjunction with isPrivateEnabled() or isGroupPeersEnabled().
	 * When set to false, the user does not know if the sent message can be
	 * delivered immediately or if it will be sent deferred.
	 * 
	 * @return true: show the user online status; false: hide the user status.
	 */
	public boolean isOnlineStatusEnabled() {
		return onlineStatusEnabled;
	}

	public void setOnlineStatusEnabled(boolean enabled) {
		setStringProperty(CONFIG_ONLINESTATUS_ENABLED, Boolean.toString(enabled), true);
	}

	/**
	 * Flag to enable/disable the listing of group peers in the roster for quick
	 * private messaging access. The listing includes all members of groups in
	 * which the group members are configured to be visible to the group.
	 * 
	 * @return true: group peers visible in roster; false: no roster with group peers.
	 */
	public boolean isGroupPeersEnabled() {
		return groupPeersEnabled;
	}

	public void setGroupPeersEnabled(boolean enabled) {
		setStringProperty(CONFIG_GROUPPEERS_ENABLED, Boolean.toString(enabled), true);
	}
}
