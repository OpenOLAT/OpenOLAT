/**
* OLAT - Online Learning and Training<br />
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br />
* you may not use this file except in compliance with the License.<br />
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br />
* software distributed under the License is distributed on an "AS IS" BASIS, <br />
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br />
* See the License for the specific language governing permissions and <br />
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br />
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.instantMessaging;

import java.util.List;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.olat.admin.user.delete.service.UserDeletionManager;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.configuration.Destroyable;
import org.olat.core.configuration.Initializable;
import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.event.FrameworkStartedEvent;
import org.olat.core.util.event.FrameworkStartupEventChannel;
import org.olat.core.util.event.GenericEventListener;
import org.olat.properties.Property;
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
	private IMConfig config;
	private static boolean enabled = false;
	private static final String CONFIG_SYNCED_BUDDY_GROUPS = "issynced";
	private static final String CONFIG_SYNCED_LEARNING_GROUPS = "syncedlearninggroups";
	OLog log = Tracing.createLoggerFor(this.getClass());
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
		this.config = config;
		enabled = config.isEnabled();
	}

	/**
	 * @see org.olat.core.configuration.OLATModule#init(com.anthonyeden.lib.config.Configuration)
	 */
	@SuppressWarnings("unchecked")
	public void init() {
		if (config.isEnabled()) {
			
			//create test accounts local and on the IM server
			if (config.generateTestUsers()) checkAndCreateTestUsers();

			// synchronizing of existing buddygroups with the instant messaging
			// server
			// if done we set a property (it gets only done once, to reactivate delete
			// entry in table o_property)
			/**
			 * delete from o_property where name='org.olat.instantMessaging.InstantMessagingModule::syncedbuddygroups';
			 */
			
			List props = propertyManager.findProperties(null, null, null, "classConfig", createPropertyName(this.getClass(), CONFIG_SYNCED_BUDDY_GROUPS));
			if (props.size() == 0) {
				
				if (config.isSyncPersonalGroups()) instantMessaging.synchronizeAllBuddyGroupsWithIMServer();
				Property property = propertyManager.createPropertyInstance(null, null, null, "classConfig", createPropertyName(this.getClass(), CONFIG_SYNCED_BUDDY_GROUPS), null,  null, Boolean.toString(true), null);
				propertyManager.saveProperty(property);
			}

			// Cleanup, otherwise this subjects will have problems in normal OLAT
			// operation
			DBFactory.getInstance().intermediateCommit();
			
		}// end if enabled

	}
	
	/**
   * Internal helper to create a property name for a class configuration property
   * @param clazz
   * @param configurationName
   * @return String
   */
  private String createPropertyName(Class clazz, String configurationName) {
          return clazz.getName() + "::" + configurationName;
  }




	/**
	 * if enabled in the configuration some testusers for IM are created in the
	 * database. It has nothing to do with accounts on the jabber server itself.
	 */
	private void checkAndCreateTestUsers() {
		Identity identity;
		Authentication auth;
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		identity = securityManager.findIdentityByName("author");
		auth = BaseSecurityManager.getInstance().findAuthentication(identity, ClientManager.PROVIDER_INSTANT_MESSAGING);
		if (auth == null) { // create new authentication for provider
			BaseSecurityManager.getInstance().createAndPersistAuthentication(identity, ClientManager.PROVIDER_INSTANT_MESSAGING, identity.getName(),
					"test");
			instantMessaging.createAccount("author", "test", "Aurich Throw", "author@olat-newinstallation.org");
		}

		identity = securityManager.findIdentityByName("administrator");
		auth = BaseSecurityManager.getInstance().findAuthentication(identity, ClientManager.PROVIDER_INSTANT_MESSAGING);
		if (auth == null) { // create new authentication for provider
			BaseSecurityManager.getInstance().createAndPersistAuthentication(identity, ClientManager.PROVIDER_INSTANT_MESSAGING, identity.getName(),
					"olat");
			instantMessaging.createAccount("administrator", "olat", "Administrator", "administrator@olat-newinstallation.org");
		}

		identity = securityManager.findIdentityByName("learner");
		auth = BaseSecurityManager.getInstance().findAuthentication(identity, ClientManager.PROVIDER_INSTANT_MESSAGING);
		if (auth == null) { // create new authentication for provider
			BaseSecurityManager.getInstance().createAndPersistAuthentication(identity, ClientManager.PROVIDER_INSTANT_MESSAGING, identity.getName(),
					"test");
			instantMessaging.createAccount("learner", "test", "Leise Arnerich", "learner@olat-newinstallation.org");
		}

		identity = securityManager.findIdentityByName("test");
		auth = BaseSecurityManager.getInstance().findAuthentication(identity, ClientManager.PROVIDER_INSTANT_MESSAGING);
		if (auth == null) { // create new authentication for provider
			BaseSecurityManager.getInstance().createAndPersistAuthentication(identity, ClientManager.PROVIDER_INSTANT_MESSAGING, identity.getName(),
					"test");
			instantMessaging.createAccount("test", "test", "Thomas Est", "test@olat-newinstallation.org");
		}
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
			String imUsername = instantMessaging.getIMUsername(identity.getName());
			instantMessaging.deleteAccount(imUsername);
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


	public static boolean isSyncLearningGroups() {
		return instantMessaingStatic.getConfig().isEnabled() && instantMessaingStatic.getConfig().isSyncLearningGroups();
	}

	@Override
	public void event(Event event) {
		// synchronistion of learning groups needs the whole olat course stuff loaded
		if (event instanceof FrameworkStartedEvent) {
			boolean success = false;
			try {
				List props = propertyManager.findProperties(null, null, null, "classConfig", createPropertyName(this.getClass(), CONFIG_SYNCED_LEARNING_GROUPS));
				if (props.size() == 0) {
					if (isSyncLearningGroups()) {
						instantMessaging.synchronizeLearningGroupsWithIMServer();
						Property property = propertyManager.createPropertyInstance(null, null, null, "classConfig", createPropertyName(this.getClass(), CONFIG_SYNCED_LEARNING_GROUPS), null,  null, Boolean.toString(true), null);
						propertyManager.saveProperty(property);
					}
				}
				database.commitAndCloseSession();
				success = true;
			} finally {
				if (!success) {
					database.rollbackAndCloseSession();
				}
			}
		}
	}
}
