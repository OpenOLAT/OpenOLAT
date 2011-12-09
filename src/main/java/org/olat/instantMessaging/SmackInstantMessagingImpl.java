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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;

import org.apache.commons.lang.RandomStringUtils;
import org.jivesoftware.smack.packet.Presence;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.IdentityShort;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.taskExecutor.TaskExecutorManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.AutoCreator;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.LogDelegator;
import org.olat.course.CourseModule;
import org.olat.group.BusinessGroup;
import org.olat.group.context.BGContext;
import org.olat.group.context.BGContextManager;
import org.olat.group.context.BGContextManagerImpl;
import org.olat.instantMessaging.groupchat.GroupChatManagerController;
import org.olat.instantMessaging.rosterandchat.InstantMessagingMainController;
import org.olat.instantMessaging.syncservice.InstantMessagingGroupSynchronisation;
import org.olat.instantMessaging.syncservice.InstantMessagingServerPluginVersion;
import org.olat.instantMessaging.syncservice.InstantMessagingSessionCount;
import org.olat.instantMessaging.syncservice.InstantMessagingSessionItems;
import org.olat.instantMessaging.syncservice.RemoteAccountCreation;
import org.olat.instantMessaging.ui.ConnectedUsersListEntry;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;

/**
 * 
 * Implementation of the InstantMessaging Interface based on
 * the SMACK instant messaging library from jivesoftware.org
 * 
 * <P>
 * Initial Date: 18.01.2005 <br />
 * @author guido
 */
public class SmackInstantMessagingImpl extends LogDelegator implements InstantMessaging {

	private IMConfig config;
	private InstantMessagingGroupSynchronisation buddyGroupService;
	private InstantMessagingSessionCount sessionCountService;
	private InstantMessagingSessionItems sessionItemsService;
	private RemoteAccountCreation accountService;
	ClientManager clientManager;
	private IMNameHelper nameHelper;
	private AdminUserConnection adminConnecion;
	private String clientVersion;
	private InstantMessagingServerPluginVersion pluginVersion;
	private AutoCreator actionControllerCreator;
	private volatile int sessionCount;
	private long timeOfLastSessionCount;
	
	/**
	 * [spring]
	 */
	private SmackInstantMessagingImpl() {
		//
	}

	/**
	 * @see org.olat.instantMessaging.InstantMessaging#createClientController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl)
	 */
	public Controller createClientController(UserRequest ureq, WindowControl wControl) {
		InstantMessagingClient client = clientManager.getInstantMessagingClient(ureq.getIdentity().getName());
		//there are two versions of the controller, either join the course chat automatically or upon request
		client.setGroupChatManager((GroupChatManagerController) actionControllerCreator.createController(ureq, wControl));
		return new InstantMessagingMainController(ureq, wControl);
	}
	
	/**
	 * [used by spring]
	 */
	public void setActionController(ControllerCreator actionControllerCreator) {
		this.actionControllerCreator = (AutoCreator) actionControllerCreator;
	}
	
	/**
	 * @see org.olat.instantMessaging.InstantMessaging#getGroupChatManagerController()
	 */
	public GroupChatManagerController getGroupChatManagerController(UserRequest ureq) {
		return clientManager.getInstantMessagingClient(ureq.getIdentity().getName()).getGroupChatManagerController();
	}

	/**
	 * @see org.olat.instantMessaging.InstantMessaging#addUserToFriendsRoster(java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 *  o_clusterOK by:fj - nodes can access the IM server concurrently but only one thread should add a users to a group at
	 *  the same time.
	 *  Sync over whole clazz, not time critical as accessed by backgrounded threads
	 */
	//TODO:gs does this need to be synchronized?
	public synchronized boolean addUserToFriendsRoster(String groupOwnerUsername, String groupId, String groupname, String addedUsername) {
		// we have to make sure the user has an account on the instant messaging
		// server
		// by calling this it gets created if not yet exists.
		String imUsername = nameHelper.getIMUsernameByOlatUsername(addedUsername);
		groupOwnerUsername = nameHelper.getIMUsernameByOlatUsername(groupOwnerUsername);
		
		boolean hasAccount = accountService.hasAccount(imUsername);
		if (!hasAccount) clientManager.getInstantMessagingCredentialsForUser(addedUsername);
		// we do not check whether a group already exists, we create it each time
		List<String> list = new ArrayList<String>();
		list.add(groupOwnerUsername);
		buddyGroupService.createSharedGroup(groupId, groupname, list);
		
		logDebug("Adding user to roster group::" + groupId + " username: " + addedUsername);
		
		return buddyGroupService.addUserToSharedGroup(groupId, addedUsername);
	}

	/**
	 * @see org.olat.instantMessaging.InstantMessaging#removeUserFromFriendsRoster(java.lang.String,
	 *      java.lang.String)
	 */
	public boolean removeUserFromFriendsRoster(String groupId, String username) {
		String imUsername = nameHelper.getIMUsernameByOlatUsername(username);
		
		logDebug("Deleting user from roster group::" + groupId + " username: " + imUsername);
		
		return buddyGroupService.removeUserFromSharedGroup(groupId, imUsername);
	}

	/**
	 * @see org.olat.instantMessaging.InstantMessaging#deleteRosterGroup(java.lang.String)
	 */
	public boolean deleteRosterGroup(String groupId) {
		//groupId is already converted to single/multiple instance version
		
		logDebug("Deleting roster group from instant messaging server::" + groupId);
		
		return buddyGroupService.deleteSharedGroup(groupId);

	}

	/**
	 * @param groupId
	 * @param displayName
	 */
	public boolean renameRosterGroup(String groupId, String displayName) {
		
		logDebug("Renaming roster group on instant messaging server::" + groupId);
		
		return buddyGroupService.renameSharedGroup(groupId, displayName);
	}

	/**
	 * @see org.olat.instantMessaging.InstantMessaging#sendStatus(java.lang.String,
	 *      java.lang.String)
	 */
	public void sendStatus(String username, String message) {
		//only send status if client is active otherwise course dispose may recreate an connection
		if (clientManager.hasActiveInstantMessagingClient(username)) {
			InstantMessagingClient imc = clientManager.getInstantMessagingClient(username);
			String recentStatus = imc.getStatus();
			//awareness presence packets get only sended if not "unavailable". Otherwise the unavailable status gets overwritten by an available one. 
			if (!recentStatus.equals(InstantMessagingConstants.PRESENCE_MODE_UNAVAILABLE)) 
				imc.sendPresence(Presence.Type.available, message, 0, Presence.Mode.valueOf(imc.getStatus()));
		}
	}
 
	/**
	 * 
	 * @see org.olat.instantMessaging.InstantMessaging#createAccount(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean createAccount(String username, String password, String fullname, String email) {
		boolean success;
		success =  accountService.createAccount(nameHelper.getIMUsernameByOlatUsername(username), password, fullname, email);
		
		logDebug("Creating new user account on IM server for user:" + username + " returned: "+success);
		
		return success;
	}

	/**
	 * 
	 * @see org.olat.instantMessaging.InstantMessaging#deleteAccount(java.lang.String)
	 */
	public boolean deleteAccount(String username) {
		boolean success;
		success = accountService.deleteAccount(nameHelper.getIMUsernameByOlatUsername(username));
		
		logDebug("Deleting user account on IM server for user:" + username + " returned: "+success);
		
		return success;
	}

	/**
	 * @see org.olat.instantMessaging.InstantMessaging#getIMPassword(java.lang.String)
	 */
	public String getIMPassword(String username) {
		return clientManager.getInstantMessagingClient(username).getPassword();

	}

	/**
	 * @return Set containing the usernames
	 */
	public Set getUsernamesFromConnectedUsers() {
		return new HashSet<String>(getClients().keySet());
	}

	/**
	 * @see org.olat.instantMessaging.InstantMessaging#getClients()
	 */
	public Map getClients() {
		return clientManager.getClients();
	}

	/**
	 * @see org.olat.instantMessaging.InstantMessaging#enableChat(java.lang.String)
	 */
	public void enableChat(String username) {
		clientManager.getInstantMessagingClient(username).enableCollaboration();
		
		logDebug("Enabling chat for user::" + username);
		
	}

	/**
	 * @param username
	 * @param reason A resason why the chat is disabled like "Doing test"
	 * @see org.olat.instantMessaging.InstantMessaging#disableChat(java.lang.String,
	 *      java.lang.String)
	 */
	public void disableChat(String username, String reason) {
		clientManager.getInstantMessagingClient(username).disableCollaboration(reason);
		
		logDebug("Disabling chat for user::" + username + "and reason" + reason);
		
	}

	/**
	 * @see org.olat.instantMessaging.InstantMessaging#countConnectedUsers()
	 */
	public int countConnectedUsers() {
		long now = System.currentTimeMillis();
		if ((now - timeOfLastSessionCount) > 30000) { //only grab session count every 30s
			logDebug("Getting session count from IM server");
			try{
				TaskExecutorManager.getInstance().runTask(new CountSessionsOnServerTask(sessionCountService, this));
			} catch(RejectedExecutionException e) {
				logError("countConnectedUsers: TaskExecutorManager rejected execution of CountSessionsOnServerTask. Cannot update user count", e);
			}
			timeOfLastSessionCount = System.currentTimeMillis();
		}
		return sessionCount;
	}
	
	//fxdiff: FXOLAT-219 decrease the load for synching groups
	private boolean synchonizeBuddyRoster(BusinessGroup group, Set<Long> checkedIdentities) {
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		List<SecurityGroup> secGroups = new ArrayList<SecurityGroup>();
		secGroups.add(group.getOwnerGroup());
		secGroups.add(group.getPartipiciantGroup());
		List<IdentityShort> users = securityManager.getIdentitiesShortOfSecurityGroups(secGroups);

		int counter = 0;
		List<String> usernames = new ArrayList<String>();
		for (IdentityShort ident:users) {
			logDebug("getting im credentials for user::" + ident.getName());
			// as jive only adds users to a group that already exist we have to make
			// sure they have an account.
			if(checkedIdentities == null || !checkedIdentities.contains(ident.getKey())) {
				clientManager.checkInstantMessagingCredentialsForUser(ident.getKey());
				if(checkedIdentities != null) {
					checkedIdentities.add(ident.getKey());
				}
				
				if (counter % 25 == 0) {
					DBFactory.getInstance().intermediateCommit();
				}
				counter++;
			}
			usernames.add(nameHelper.getIMUsernameByOlatUsername(ident.getName()));
		}
		String groupId = InstantMessagingModule.getAdapter().createChatRoomString(group);
		if (users.size() > 0 ) { // only sync groups with users
			if (!buddyGroupService.createSharedGroup(groupId, group.getName(), usernames)){
				logError("could not create shared group: "+groupId, null);
			}
			logDebug("synchronizing group::" + group.toString());
		} else {
			logDebug("empty group: not synchronizing group::" + group.toString());
		}
		//when looping over all buddygroups and learninggroups close transaction after each group
		DBFactory.getInstance().intermediateCommit();
		return true;
	}
	
	/**
	 * 
	 * @see org.olat.instantMessaging.InstantMessaging#synchronizeLearningGroupsWithIMServer()
	 */

	//fxdiff: FXOLAT-219 decrease the load for synching groups
	public boolean synchronizeLearningGroupsWithIMServer() {
		if (!(adminConnecion != null && adminConnecion.getConnection() != null && adminConnecion.getConnection().isConnected())) {
			return false;
		}
		logInfo("Starting synchronisation of LearningGroups with IM server");
		long start = System.currentTimeMillis();
		
		RepositoryManager rm = RepositoryManager.getInstance();
		BGContextManager contextManager = BGContextManagerImpl.getInstance();
		//pull as admin
		Roles roles = new Roles(true, true, true, true, false, true, false);
		List<RepositoryEntry> allCourses = rm.queryByTypeLimitAccess(null, CourseModule.getCourseTypeName(), roles);
		boolean syncLearn = InstantMessagingModule.getAdapter().getConfig().isSyncLearningGroups();
		Set<Long> checkedIdentities = new HashSet<Long>();
		
		int counter = 0;
		for (RepositoryEntry entry: allCourses) {
			OLATResource courseResource = entry.getOlatResource();
			List<BGContext> contexts = contextManager.findBGContextsForResource(courseResource, BusinessGroup.TYPE_LEARNINGROUP, true, true);
			
			List<BusinessGroup> groups = new ArrayList<BusinessGroup>();
			for (BGContext bgContext:contexts) {
				groups.addAll(contextManager.getGroupsOfBGContext(bgContext));
			}
	
			for (BusinessGroup group:groups) {
					boolean isLearn = group.getType().equals(BusinessGroup.TYPE_LEARNINGROUP);
					if (isLearn && !syncLearn) {
						String groupID = InstantMessagingModule.getAdapter().createChatRoomString(group);
						if (deleteRosterGroup(groupID)) {
							logInfo("deleted unwanted group: "+group.getResourceableTypeName()+" "+groupID, null);
						}
					} else if (!synchonizeBuddyRoster(group, checkedIdentities)) {
						logError("couldn't sync group: "+group.getResourceableTypeName(), null);
					}
					counter++;
					if (counter%6==0) {
						DBFactory.getInstance(false).intermediateCommit();
					}
					
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
			}
				
			if (counter%6==0) {
				DBFactory.getInstance(false).intermediateCommit();
			}
		}
		logInfo("Ended synchronisation of LearningGroups with IM server: Synched "+counter+" groups in " + (System.currentTimeMillis() - start) + " (ms)");
		return true;
	}

	/**
	 * Synchronize the groups with the IM system
	 * To synchronize buddygroups, use the null-context.
	 * Be aware that this action might take some time!
	 * @param groupContext
	 * @return true if successfull, false if IM server is not running
	 */
	public boolean synchronizeAllBuddyGroupsWithIMServer() {
		if (adminConnecion != null && adminConnecion.getConnection() != null && adminConnecion.getConnection().isConnected()) {
			logInfo("Started synchronisation of BuddyGroups with IM server.");
			BGContextManager cm = BGContextManagerImpl.getInstance();
			//null as argument pulls all buddygroups
			List<BusinessGroup> groups = cm.getGroupsOfBGContext(null);
			int counter = 0;
			//fxdiff: FXOLAT-219 decrease the load for synching groups
			Set<Long> checkedIdentites = new HashSet<Long>();
			for (BusinessGroup group: groups) {
				if(synchonizeBuddyRoster(group, checkedIdentites)) {
					counter++;
				}
				//make an intermediate commit already
			}
			logInfo("Ended synchronisation of BuddyGroups with IM server: Synched "+counter+" groups");
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @see org.olat.instantMessaging.InstantMessaging#createChatRoomString(org.olat.core.id.OLATResourceable
	 */
	public String createChatRoomString(OLATResourceable ores) {
		String roomName = ores.getResourceableTypeName() + "-" + ores.getResourceableId(); 
		return nameHelper.getGroupnameForOlatInstance(roomName);
	}
	
	public String createChatRoomJID(OLATResourceable ores) {
		return createChatRoomString(ores)+"@"+config.getConferenceServer();
	}
	
	/**
	 * @see org.olat.instantMessaging.InstantMessaging#getAllConnectedUsers()
	 */
	public List<ConnectedUsersListEntry> getAllConnectedUsers(Identity currentUser) {
		return sessionItemsService.getConnectedUsers(currentUser);
	}

	/**
	 * [used by spring]
	 * @param sessionCountService
	 */
	public void setSessionCountService(InstantMessagingSessionCount sessionCountService) {
		this.sessionCountService = sessionCountService;
	}
	
	/**
	 * [used by spring]
	 * @param sessionCountService
	 */
	public void setBuddyGroupService(InstantMessagingGroupSynchronisation buddyGroupService) {
		this.buddyGroupService = buddyGroupService;
	}

	/**
	 * [used by spring]
	 * @param sessionItemsService
	 */
	public void setSessionItemsService(InstantMessagingSessionItems sessionItemsService) {
		this.sessionItemsService = sessionItemsService;
	}

	/**
	 * [used by spring]
	 * @param accountService
	 */
	public void setAccountService(RemoteAccountCreation accountService) {
		this.accountService = accountService;
	}

	/**
	 * [used by spring]
	 * @param clientManager
	 */
	public void setClientManager(ClientManager clientManager) {
		this.clientManager = clientManager;
	}
	
	/**
	 * 
	 * @return client manager where you have access to the IM client itself
	 */
	public ClientManager getClientManager() {
		return clientManager;
	}

	public IMConfig getConfig() {
		return config;
	}

	public void setConfig(IMConfig config) {
		this.config = config;
	}

	/**
	 * 
	 * @see org.olat.instantMessaging.InstantMessaging#hasAccount(java.lang.String)
	 */
	public boolean hasAccount(String username) {
		return accountService.hasAccount(nameHelper.getIMUsernameByOlatUsername(username));
	}

	/**
	 * 
	 * @see org.olat.instantMessaging.InstantMessaging#getUserJid(java.lang.String)
	 */
	public String getUserJid(String username) {
		return nameHelper.getIMUsernameByOlatUsername(username)+"@"+config.getServername();
	}

	/**
	 * 
	 * @see org.olat.instantMessaging.InstantMessaging#getUsernameFromJid(java.lang.String)
	 */
	public String getUsernameFromJid(String jid) {
		return nameHelper.extractOlatUsername(jid);
	}

	public String getIMUsername(String username) {
		return nameHelper.getIMUsernameByOlatUsername(username);
	}

	public void setNameHelper(IMNameHelper nameHelper) {
		this.nameHelper = nameHelper;
	}

	/**
	 * [spring]
	 * @param adminConnection
	 */
	public void  setAdminConnection(AdminUserConnection adminConnection) {
		this.adminConnecion = adminConnection;
	}
	
	// use rarely, as this is normally linked by spring!
	public AdminUserConnection getAdminUserConnection(){
		return this.adminConnecion;
	}
	
	/**
	 * 
	 * @see org.olat.instantMessaging.InstantMessaging#resetAdminConnection()
	 */
	public void resetAdminConnection() {
		this.adminConnecion.resetAndReconnect();
	}
	
	/**
	 * [spring]
	 * @param clientVersion
	 */
	public void setClientVersion(String clientVersion) {
		this.clientVersion = clientVersion;
	}
	
	public void setServerPluginVersion(InstantMessagingServerPluginVersion pluginVersion) {
		this.pluginVersion = pluginVersion;
	}

	/**
	 * 
	 * @see org.olat.instantMessaging.InstantMessaging#checkServerPlugin()
	 */
	public String checkServerPlugin() {
		if (clientVersion.equals(pluginVersion.getPluginVersion())) {
			return "<b>Jupee!</b> Server plugin and OLAT client run on the same version: "+pluginVersion.getPluginVersion();
		} else if (pluginVersion.getPluginVersion() == null) {
			return "The server does not respond with a version. Do you have the plugin installed? Does the admin user have a running connection to the IM server?";
		}
		return "OLAT runs on client version: "+clientVersion +" but the server version is: "+pluginVersion.getPluginVersion()+"<br/><b>Plese upgrade!</b>";
	}

	@Override
	public IMNameHelper getNameHelper() {
		return nameHelper;
	}

	void setSessionCount(int sessionCount) {
		this.sessionCount = sessionCount;
	}
	
	public String synchronizeAllOLATUsers(){
		StringBuilder sb = new StringBuilder();
		InstantMessaging im = InstantMessagingModule.getAdapter();
		logInfo("Started synchronisation of all OLAT users with IM server.");
		List<Identity> allIdentities = BaseSecurityManager.getInstance().getVisibleIdentitiesByPowerSearch(null, null, false, null, null, null, null, null);
		int identCount = 0;
		int authCount = 0;
		int imDelCount = 0;
		int imCreateCount = 0;
		for (Identity identity : allIdentities) {
			if (BaseSecurityManager.getInstance().getRoles(identity).isGuestOnly()) continue;
			String userName = identity.getName();
			boolean hasIMAccount = im.hasAccount(userName);
			// try to detect double wrapped users on IM-Server
			String imUserName = im.getIMUsername(userName);
			int index = imUserName.indexOf("_");
			if (index >= 0){ // only for names in multipleInstance-mode
				String instanceID = imUserName.substring(index);
				String badIMName = (imUserName + instanceID).toLowerCase();
				if (accountService.hasAccount(badIMName)) {
					logWarn("found an invalid IM account with double encoded username in multi instance mode. will remove this account in IM-server: " + badIMName, null);
					sb.append("removed IM account with double instanceID:").append(badIMName);
					accountService.deleteAccount(badIMName);
				}			
			}
			
			//TODO: try to connect with auth-token, if not working, resync IM-PW
			
			Authentication auth = BaseSecurityManager.getInstance().findAuthentication(identity, ClientManager.PROVIDER_INSTANT_MESSAGING);
			if (auth != null && !hasIMAccount) {
				// there is an invalid auth token, but no IM-account, remove auth
				BaseSecurityManager.getInstance().deleteAuthentication(auth);
				DBFactory.getInstance().intermediateCommit();
				auth = null;
				authCount++;
			}
			if (auth == null) {
				if (hasIMAccount) {
					// remove existing account, to later have auth and im-pw in sync!
					im.deleteAccount(userName);
					imDelCount++;
				}				
				String pw = RandomStringUtils.randomAlphanumeric(6);
				String fullName = identity.getUser().getProperty(UserConstants.FIRSTNAME, null) + " " + identity.getUser().getProperty(UserConstants.LASTNAME, null);
				boolean success = im.createAccount(userName, pw, fullName, identity.getUser().getProperty(UserConstants.EMAIL, null));
				if (success) {
					auth = BaseSecurityManager.getInstance().createAndPersistAuthentication(identity, ClientManager.PROVIDER_INSTANT_MESSAGING, userName.toLowerCase(), pw);
					imCreateCount++;
				}					
			}
						
			// TODO: sync users buddylist!?
			
			identCount ++;
			if (identCount % 20 == 0) {
				DBFactory.getInstance().intermediateCommit();
			}	
		}	
		sb.append(" looped over ").append(identCount).append(" identities to sync.");
		sb.append("  removed invalid authentication tokens: ").append(authCount);
		sb.append("  deleted IM-accounts before recreation: ").append(imDelCount);
		sb.append("  recreated IM-accounts and authentications: ").append(imCreateCount);
		String status = sb.toString();
		logInfo(" Sync status: " + status);
		logInfo("Ended synchronisation of all OLAT users.");
		return status;
	}
	

}