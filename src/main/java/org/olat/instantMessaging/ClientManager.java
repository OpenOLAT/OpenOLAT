package org.olat.instantMessaging;

import java.util.Map;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Presence;
import org.olat.core.gui.control.Controller;
import org.olat.core.util.event.GenericEventListener;

public interface ClientManager {

	/**
	 * instant messaging needs its own password
	 */
	public static final String PROVIDER_INSTANT_MESSAGING = "INST-MSG";

	/**
	 * Creates an new instant messaging client and connects automatically
	 * to the server. This method should only be used in a constructor and not 
	 * be triggered by GUI events, otherwise clients that lost connections may get recreated
	 * again.
	 * @param username
	 * @return JabberClient even if IM service is down
	 */
	public InstantMessagingClient getInstantMessagingClient(String username);

	/**
	 * Check whether a user has already an IM  client running
	 * Use this method when fetching clients outside a controller constructor as users may have several sessions
	 * to avoid reconnection of an duplicate session 
	 * @param username
	 * @return
	 */
	public boolean hasActiveInstantMessagingClient(String username);

	/**
	 * 
	 * @param username
	 * @param listener
	 * @param listenToAllMessages - only the main controller needs to listen to all messages
	 */
	public void registerEventListener(String username, GenericEventListener listener, boolean listenToAllMessages);

	public GenericEventListener getRegisteredEventListeners(String username, Controller controller);

	public void deregisterControllerListener(String username, Controller controller);

	public Chat createChat(final String username, String chatPartnerJid, final Controller controller);

	/**
	 * @param username
	 */
	public void addMessageListener(final String username);

	/**
	 * 
	 * @param username
	 */
	public void addPresenceListener(final String username);

	/**
	 * helper method to trigger a presence update even if the server does not send
	 * a presence packet itself (e.g. entering a test but no other buddies are online)
	 * @param username
	 */
	public void sendPresenceEvent(Presence.Type type, String username);

	/**
	 * When a user logs out of olat we logout the client from the jabber server
	 * and free the ressource
	 * 
	 * @param username
	 */
	public void destroyInstantMessagingClient(String username);

	/**
	 * returns the map (its iterator is safe)
	 * 
	 * @return map
	 */
	public Map<String, InstantMessagingClient> getClients();

	/**
	 * Looks if user has credentials for IM. If not (auth == null) a new accounts
	 * with a random generated password gets created otherwise the password gets
	 * returned.
	 * 
	 * @param username the OLAT username
	 * @return the password used for instant messaging
	 */
	public String getInstantMessagingCredentialsForUser(String username);

}