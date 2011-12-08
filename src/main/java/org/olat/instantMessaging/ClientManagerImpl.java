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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br />
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br />
 * See the License for the specific language governing permissions and <br />
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br />
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.instantMessaging;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.gui.control.Controller;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.event.GenericEventListener;

/**
 * Description: <br />
 * Manager class for creation and manipulation of the instant messaging client
 * for each user.
 * <P>
 * Initial Date: 14.10.2004
 * 
 * @author Guido Schnider
 */
public class ClientManagerImpl extends BasicManager implements ClientManager {
	// each client gets stored in a map under the username
	// o_clusterNOK cache ?? what if the clientmanager runs on each vm? ok? why?
	private Map<String, InstantMessagingClient> clients = Collections.synchronizedMap(new HashMap<String, InstantMessagingClient>());
	Map<String, GenericEventListener> listeners = Collections.synchronizedMap(new HashMap<String, GenericEventListener>());
	/**
	 * empty constructor
	 */
	private ClientManagerImpl() {
	// nothing to do
	}


	/**
	 * Creates an new instant messaging client and connects automatically
	 * to the server. This method should only be used in a constructor and not 
	 * be triggered by GUI events, otherwise clients that lost connections may get recreated
	 * again.
	 * @param username
	 * @return JabberClient even if IM service is down
	 */
	public InstantMessagingClient getInstantMessagingClient(String username) {
		// no need to sync over whole method (get/put) since only called once per
		// UserRequest and username.
		// we do not sync since "new InstantMessagingClient(...)" may last quite
		// long.
		InstantMessagingClient client;
		client = clients.get(username);
		if (client == null) {
			String password = getInstantMessagingCredentialsForUser(username);
			client = new InstantMessagingClient(username, password);
				clients.put(username, client);
			return client;
		} else {
			return client;
		}
	}
	
	/**
	 * Check whether a user has already an IM  client running
	 * Use this method when fetching clients outside a controller constructor as users may have several sessions
	 * to avoid reconnection of an duplicate session 
	 * @param username
	 * @return
	 */
	public boolean hasActiveInstantMessagingClient(String username){
			return clients.containsKey(username);
	}

	/**
	 * 
	 * @param username
	 * @param listener
	 * @param listenToAllMessages - only the main controller needs to listen to all messages
	 */
	public void registerEventListener(String username, GenericEventListener listener, boolean listenToAllMessages) {
			if (listenToAllMessages) {
				listeners.put(username, listener);
			} else {
				listeners.put(username+listener.hashCode(), listener);
			}
	}
	

	public GenericEventListener getRegisteredEventListeners(String username, Controller controller) {
		return listeners.get(username+controller.hashCode());
	}

	public void deregisterControllerListener(String username, Controller controller) {
			listeners.remove(username+controller.hashCode());
	}
	
	public Chat createChat(final String username, String chatPartnerJid, final Controller controller) {
		ChatManager chatmanager = getInstantMessagingClient(username).getConnection().getChatManager();
		Chat chat = chatmanager.createChat(chatPartnerJid, new MessageListener() {

			public void processMessage(Chat chat, Message message) {
				message.setProperty("receiveTime", new Long(new Date().getTime()));
				GenericEventListener listener = listeners.get(username+controller.hashCode());
				listener.event(new InstantMessagingEvent(message, "chatmessage"));
			}
		});
		return chat;
	}

	/**
	 * @param username
	 */
	public void addMessageListener(final String username) {
		PacketListener packetListener = new PacketListener() {
			public void processPacket(Packet packet) {
				Message jabbmessage = (Message) packet;
				//TODO:gs:b see issue: http://bugs.olat.org/jira/browse/OLAT-2966
				//filter <script> msg. out - security risk of cross site scripting!
				//or may user ext.util.strip script tag method on client side
				jabbmessage.setProperty("receiveTime", new Long(new Date().getTime()));
				GenericEventListener listener = listeners.get(username);
				if (listener != null){
					listener.event(new InstantMessagingEvent(packet, "message"));
					if (isLogDebugEnabled()) logDebug("routing message event to controller of: "+packet.getTo());
				} else {
					logWarn("could not find listener for IM message for username: "+username, null);
				}
			}
		};
		getInstantMessagingClient(username).getConnection().addPacketListener(packetListener, new PacketTypeFilter(Message.class));
	}

	/**
	 * 
	 * @param username
	 */
	public void addPresenceListener(final String username) {
		PacketListener packetListener = new PacketListener() {
			public void processPacket(Packet packet) {
				try {
					GenericEventListener listener = listeners.get(username);
					if (listener == null) {
						logWarn("could not route presence event as presence listener is null for user: "+username, null);
					} else {
						listener.event(new InstantMessagingEvent(packet, "presence"));
						Presence presence = (Presence) packet;
						if (isLogDebugEnabled()) logDebug("routing presence event to controller of: "+presence.getTo());
					}
				} catch(Throwable th){
					logWarn("Presence package", th);
				}
			}
		};
		getInstantMessagingClient(username).getConnection().addPacketListener(packetListener, new PacketTypeFilter(Presence.class));
	}
	
	/**
	 * helper method to trigger a presence update even if the server does not send
	 * a presence packet itself (e.g. entering a test but no other buddies are online)
	 * @param username
	 */
	public void sendPresenceEvent(Presence.Type type, String username) {
		Presence presence = new Presence(type);
		presence.setTo(username);
		GenericEventListener listener = listeners.get(username);
		if (listener != null) {
			listener.event(new InstantMessagingEvent(presence, "presence"));
		}
	}

	/**
	 * Looks if user has credentials for IM. If not (auth == null) a new accounts
	 * with a random generated password gets created otherwise the password gets
	 * returned.
	 * 
	 * @param username the OLAT username
	 * @return the password used for instant messaging
	 */
	public String getInstantMessagingCredentialsForUser(String username) {
		Identity identity = BaseSecurityManager.getInstance().findIdentityByName(username);
		if (identity==null) {
			// OLAT-3556: masking this error temporarily
			//@TODO
			//@FIXME
			logWarn("Identity not found for username="+username, null);
			return null;
		}
		return getInstantMessagingCredentialsForUser(identity);
	}

	//fxdiff: FXOLAT-219 decrease the load for synching groups
	public String getInstantMessagingCredentialsForUser(Identity identity) {
		if(identity == null) return null;
		// synchronized: not needed here, since the credentials are only created once for a user (when that user logs in for the very first time).
		// And a user will almost! never log in at the very same time from two different machines.
		Authentication auth = BaseSecurityManager.getInstance().findAuthentication(identity, PROVIDER_INSTANT_MESSAGING);
		
		if (auth == null) { // create new authentication for provider and also a new IM-account
			auth = createIMAuthentication(identity);
		} 
		/**
		 * this does not decouple IM from the loginprocess, move account recreations in background thread somewhere else
		 * maybe to the login background thread...
		 */
		//fxdiff: FXOLAT-233 
		if (auth != null) return auth.getCredential();
		else return "";
	}
	
	@Override
	//fxdiff: FXOLAT-219 decrease the load for synching groups
	public void checkInstantMessagingCredentialsForUser(Long identityKey) {
		if(identityKey == null) return;
		boolean auth = BaseSecurityManager.getInstance().hasAuthentication(identityKey, PROVIDER_INSTANT_MESSAGING);
		if (!auth) { // create new authentication for provider and also a new IM-account
			 Identity identity = BaseSecurityManager.getInstance().loadIdentityByKey(identityKey, false);
			 if(identity != null) {
				 createIMAuthentication(identity);
			 }
		} 
	}

	//fxdiff: FXOLAT-219 decrease the load for synching groups
	private Authentication createIMAuthentication(Identity identity) {
		String username = identity.getName();
		InstantMessaging im = InstantMessagingModule.getAdapter();
	//if account exists on IM server but not on OLAT delete it first
		if (im.hasAccount(username)) {
			im.deleteAccount(username);
		}
		
		String pw = RandomStringUtils.randomAlphanumeric(6);
		if (im.createAccount(username, pw,
				getFullname(identity), identity.getUser().getProperty(UserConstants.EMAIL, null))) {
			Authentication auth = BaseSecurityManager.getInstance().createAndPersistAuthentication(identity, PROVIDER_INSTANT_MESSAGING,
					identity.getName().toLowerCase(), pw);
			logAudit("New instant messaging authentication account created for user:" + username, null);
			return auth;
		} else {
			logWarn("new instant messaging account creation failed for user: " + username, null);
			return null;
		}
	}


	/**
	 * 
	 * @param identity
	 * @return
	 */
	private String getFullname(Identity identity) {
		return identity.getUser().getProperty(UserConstants.FIRSTNAME, null) + " " + identity.getUser().getProperty(UserConstants.LASTNAME, null);
	}

	/**
	 * When a user logs out of olat we logout the client from the jabber server
	 * and free the ressource
	 * 
	 * @param username
	 */
	public void destroyInstantMessagingClient(String username) {
		InstantMessagingClient client;
		client = clients.get(username);
		if (client != null) {
				listeners.remove(username);
				client.closeConnection(false);
				clients.remove(username);
		}
	}

	/**
	 * returns the map (its iterator is safe)
	 * 
	 * @return map
	 */
	public Map<String, InstantMessagingClient> getClients() {
			HashMap<String, InstantMessagingClient> hm = new HashMap<String, InstantMessagingClient>(clients);
			return hm;
	}

}