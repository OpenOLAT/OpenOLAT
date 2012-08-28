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

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.olat.admin.user.delete.service.UserDeletionManager;
import org.olat.core.commons.persistence.DB;
import org.olat.core.configuration.Destroyable;
import org.olat.core.configuration.Initializable;
import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.event.FrameworkStartupEventChannel;
import org.olat.core.util.event.GenericEventListener;
import org.olat.properties.PropertyManager;
import org.olat.user.UserDataDeletable;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Description: <br />
 * For configuration see olat.properties and put overwrite values in 
 * olat.local.properties or directly edit spring config of instant messaging
 * <P>
 * Initial Date: 14.10.2004
 * 
 * @author Guido Schnider
 */
public class InstantMessagingModule implements Initializable, Destroyable, UserDataDeletable, GenericEventListener {

	private static ConnectionConfiguration connConfig;
	@Autowired
	private static XMPPConnection adminConnection;
	//FIXME: used for legacy access
	private static InstantMessaging instantMessaingStatic;
	private static boolean enabled = false;
	//fxdiff: FXOLAT-219 decrease the load for synching groups
	public static final String CONFIG_SYNCED_LEARNING_GROUPS = "syncedlearninggroups";
	private static final OLog log = Tracing.createLoggerFor(InstantMessagingModule.class);
	@Autowired
	private PropertyManager propertyManager;
	@Autowired
	private DB database;
	private InstantMessaging instantMessaging;
	
	
	/**
	 * [used by spring]
	 */
	private InstantMessagingModule() {
		super();
		FrameworkStartupEventChannel.registerForStartupEvent(this);
	}
	
	/**
	 * 
	 * @param instantMessaging
	 */
	@Autowired(required=true)
	public void setInstantMessaing(InstantMessaging instantMessaging) {
		this.instantMessaging = instantMessaging;
		instantMessaingStatic = instantMessaging;
	}
	
	/**
	 * [spring]
	 * @param userDeletionManager
	 */
	@Autowired(required=true)
	public void setUserDeletionManager(UserDeletionManager userDeletionManager) {
		userDeletionManager.registerDeletableUserData(this);
	}
	
	/**
	 * [spring]
	 * @param config
	 */
	@Autowired(required=true)
	public void setIMConfig(IMConfig config) {
		enabled = config.isEnabled();
	}

	public void init() {
		//synched moved to the job 
	}
	
	/**
   * Internal helper to create a property name for a class configuration property
   * @param clazz
   * @param configurationName
   * @return String
   */

	//fxdiff: FXOLAT-219 decrease the load for synching groups
  public static String createPropertyName(Class<?> clazz, String configurationName) {
  	return clazz.getName() + "::" + configurationName;
  }

	/**
	 * @see org.olat.core.configuration.OLATModule#destroy()
	 */
	public void destroy() {
	 if (adminConnection != null) {
		 adminConnection.disconnect();
	 }
	}


	/**
	 * @return the adapter instance
	 */
	public static InstantMessaging getAdapter() {
		return instantMessaingStatic;
	}

	/**
	 * @return Returns the enabled.
	 */
	public static boolean isEnabled() {
		return enabled;
	}


	/**
	 * @return a reused connection configuration for connecting to the im server
	 */
	protected static ConnectionConfiguration getConnectionConfiguration() {
		if (connConfig == null) {
			// 5222 is the default unsecured jabber server port
			connConfig = new ConnectionConfiguration(instantMessaingStatic.getConfig().getServername(), 5222);
			connConfig.setNotMatchingDomainCheckEnabled(false);
			connConfig.setSASLAuthenticationEnabled(false);
			connConfig.setReconnectionAllowed(false);
		}
		return connConfig;
	}

	/**
	 * 
	 * @see org.olat.user.UserDataDeletable#deleteUserData(org.olat.core.id.Identity)
	 */
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		if (instantMessaging.getConfig().isEnabled()) {
			instantMessaging.deleteAccount(identity.getName());
			log.debug("Deleted IM account for identity=" + identity);
		}
	}

	/**
	 * @return Returns the iDLE_POLLTIME.
	 */
	public static int getIDLE_POLLTIME() {
		return instantMessaingStatic.getConfig().getIdlePolltime();
	}

	/**
	 * @param idle_polltime The iDLE_POLLTIME to set.
	 */
	public static void setIDLE_POLLTIME(int idle_polltime) {
		instantMessaingStatic.getConfig().setIdlePolltime(idle_polltime);
	}

	/**
	 * @return Returns the cHAT_POLLTIME.
	 */
	public static int getCHAT_POLLTIME() {
		return instantMessaingStatic.getConfig().getChatPolltime();
	}

	/**
	 * @param chat_polltime The cHAT_POLLTIME to set.
	 */
	public static void setCHAT_POLLTIME(int chat_polltime) {
		instantMessaingStatic.getConfig().setChatPolltime(chat_polltime);
	}


	public static boolean isSyncGroups() {
		return instantMessaingStatic.getConfig().isEnabled()
				&& (IMConfigSync.allGroups.equals(instantMessaingStatic.getConfig().getSyncGroupsConfig())
						|| IMConfigSync.perConfig.equals(instantMessaingStatic.getConfig().getSyncGroupsConfig()));
	}

	@Override
	public void event(Event event) {
		// synchronistion of learning groups needs the whole olat course stuff loaded
		//fxdiff: FXOLAT-219 decrease the load for synching groups
	}
}
