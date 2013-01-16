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
	private static final String CONFIG_ONLINEUSERS_ENABLED = "im.enabled.onlineusers";
	private static final String CONFIG_GROUPPEERS_ENABLED = "im.enabled.grouppeers";
	private static final String CONFIG_VIEW_ONLINE_USERS_ENABLED = "im.enabled.viewonlineusers";

	private boolean enabled = false;
	private boolean groupEnabled = false;
	private boolean courseEnabled = false;
	private boolean privateEnabled = false;
	private boolean onlineUsersEnabled = false;
	private boolean groupPeersEnabled = false;
	private boolean viewOnlineUsersEnabled = false;

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
		enabledObj = getStringPropertyValue(CONFIG_ONLINEUSERS_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			onlineUsersEnabled = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_GROUPPEERS_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			groupPeersEnabled = "true".equals(enabledObj);
		}
		enabledObj = getStringPropertyValue(CONFIG_VIEW_ONLINE_USERS_ENABLED, false);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			viewOnlineUsersEnabled = "true".equals(enabledObj);
		}
	}

	@Override
	protected void initDefaultProperties() {
		enabled = getBooleanConfigParameter(CONFIG_ENABLED, true);
		groupEnabled = getBooleanConfigParameter(CONFIG_GROUP_ENABLED, true);
		courseEnabled = getBooleanConfigParameter(CONFIG_COURSE_ENABLED, true);
		privateEnabled = getBooleanConfigParameter(CONFIG_PRIVATE_ENABLED, true);
		onlineUsersEnabled = getBooleanConfigParameter(CONFIG_ONLINEUSERS_ENABLED, true);
		groupPeersEnabled = getBooleanConfigParameter(CONFIG_GROUPPEERS_ENABLED, true);
		viewOnlineUsersEnabled = getBooleanConfigParameter(CONFIG_VIEW_ONLINE_USERS_ENABLED, false);
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
	 * @return Returns the enabled.
	 */
	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		setStringProperty(CONFIG_ENABLED, Boolean.toString(enabled), true);
	}

	public boolean isGroupEnabled() {
		return groupEnabled;
	}

	public void setGroupEnabled(boolean enabled) {
		setStringProperty(CONFIG_GROUP_ENABLED, Boolean.toString(enabled), true);
	}

	public boolean isCourseEnabled() {
		return courseEnabled;
	}

	public void setCourseEnabled(boolean enabled) {
		setStringProperty(CONFIG_COURSE_ENABLED, Boolean.toString(enabled), true);
	}

	public boolean isPrivateEnabled() {
		return privateEnabled;
	}

	public void setPrivateEnabled(boolean enabled) {
		setStringProperty(CONFIG_PRIVATE_ENABLED, Boolean.toString(enabled), true);
	}

	public boolean isOnlineUsersEnabled() {
		return onlineUsersEnabled;
	}

	public void setOnlineUsersEnabled(boolean enabled) {
		setStringProperty(CONFIG_ONLINEUSERS_ENABLED, Boolean.toString(enabled), true);
	}

	public boolean isGroupPeersEnabled() {
		return groupPeersEnabled;
	}

	public void setGroupPeersEnabled(boolean enabled) {
		setStringProperty(CONFIG_GROUPPEERS_ENABLED, Boolean.toString(enabled), true);
	}

	public boolean isViewOnlineUsersEnabled() {
		return viewOnlineUsersEnabled;
	}

	public void setViewOnlineUsersEnabled(boolean enabled) {
		setStringProperty(CONFIG_VIEW_ONLINE_USERS_ENABLED, Boolean.toString(enabled), true);
	}
}
