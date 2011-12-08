package org.olat.instantMessaging.rosterandchat;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.packet.Presence;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.instantMessaging.IMConfig;
import org.olat.instantMessaging.InstantMessaging;
import org.olat.instantMessaging.InstantMessagingClient;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.instantMessaging.XMPPConnListener;

/**
 * 
 * Description:<br>
 * connecting in the background to the instant messaging server to decouple the login process to IM server 
 * to not disturb the login process in case of errors/slow in the IM server
 * 
 * <P>
 * Initial Date:  30.07.2010 <br>
 * @author guido
 */

public class ConnectToServerTask implements Runnable {

	private InstantMessagingClient client;
	OLog log = Tracing.createLoggerFor(ConnectToServerTask.class);
	private InstantMessaging im;
	
	public ConnectToServerTask(InstantMessagingClient client) {
		this.client = client;
		this.im = InstantMessagingModule.getAdapter();
	}

	public void run() {
		for (int i = 0; i < 2; i++) {
			try {
				XMPPConnection connection = client.getConnection();
				connection.connect();
				connection.login(client.getChatUsername(), client.getPassword(), IMConfig.RESOURCE);
				
				if (log.isDebug()) {
					log.debug("Connecting to IM server with username: "+client.getChatUsername() +" and password: "+client.getPassword());
				}
				
				client.setIsConnected(connection.isConnected() && connection.isAuthenticated());
				if (client.isConnected() && connection.isAuthenticated()) {
					im.getClientManager().addMessageListener(client.getUsername());
					im.getClientManager().addPresenceListener(client.getUsername());
					client.addSubscriptionListener();
					
					connection.addConnectionListener(new XMPPConnListener(client));
					client.setRoster(connection.getRoster());
					// subscription accept all = 0
					client.getRoster().setSubscriptionMode(SubscriptionMode.accept_all);
					
					String defaultStatus = client.getDefaultRosterStatus();
					if (defaultStatus.equals(Presence.Type.unavailable.toString())) client.sendPresenceUnavailable();
					else client.sendPresenceAvailable(Presence.Mode.valueOf(defaultStatus));
					break;
					
				} else {
					
					if (!im.hasAccount(client.getUsername())) {
						boolean success = im.createAccount(client.getUsername(), client.getPassword(), client.getFullName(), client.getEmail());
						if (success) {
							log.audit("New instant messaging authentication account created for user:" + client.getChatUsername());
							client.closeConnection(true);
							continue;
						}
					}
					
					log.warn("Error while trying to connect to Instant Messaging server (username, server): " + client.getChatUsername() + ", " + client.getServerName()
							+ " After login connection.isConnected() returned false ");
				}
				
			} catch (XMPPException e) {
				client.setIsConnected(false);
				if (e.getXMPPError()!=null && e.getXMPPError().getCode() == 401) { //401 --> "not authorised", normally this users do not have an account on the server
					log.info("User is not authorized to connect to Instant Messaging server (username, server): " + client.getChatUsername() + ", " + client.getServerName()+
							". Make sure this users have an account on the IM server. I will try to recreate the account now");
					
					if (!im.hasAccount(client.getUsername())) {
						boolean success = im.createAccount(client.getUsername(), client.getPassword(), client.getFullName(), client.getEmail());
						if (success) {
							log.audit("New instant messaging account created for user:" + client.getChatUsername());
							client.closeConnection(true);
							continue;
						}
					}
					
				}
				client.closeConnection(true);
				log.warn("Error while trying to connect to Instant Messaging server (username, server): " + client.getChatUsername() + ", " + client.getServerName(),e);
			} catch (Exception e) { // also catch java.lang.IllegalStateException: Not
				// connected to server. -> at
				// org.jivesoftware.smack.XMPPConnection.addPacketListener(XMPPConnection.java:581)
				// and so on
				client.closeConnection(true);
				client.setIsConnected(false);
				log.warn("Error while trying to connect to Instant Messaging server (username, server): " + client.getChatUsername() + ", " + client.getServerName(),e);
			}
		}
	}

}
