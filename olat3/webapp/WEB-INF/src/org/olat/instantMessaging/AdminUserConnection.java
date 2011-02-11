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

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.StartupException;
import org.olat.core.logging.Tracing;

/**
 * Description:<br>
 * create a connection object for the admin user
 * 
 * <P>
 * Initial Date:  11.08.2008 <br>
 * @author guido
 */
public class AdminUserConnection {
	
	final static OLog log = Tracing.createLoggerFor(AdminUserConnection.class);
	
	private XMPPConnection connection;
	private String serverName;
	private String adminUsername;
	private String adminPassword;
	private String nodeId;

	/**
	 * [used by spring]
	 * @param serverName
	 * @param adminUsername
	 * @param adminPassword
	 * @param nodeId
	 */
	private AdminUserConnection(String serverName, String adminUsername, String adminPassword, String nodeId, boolean enabled) {
		if (serverName == null || adminUsername == null || adminPassword == null) {
			throw new StartupException("Instant Messaging settings for admin user are not correct");
		}
		this.serverName = serverName;
		this.adminUsername = adminUsername;
		this.adminPassword = adminPassword;
		this.nodeId = nodeId;
		if (enabled) {
			final int LOOP_CNT=5;
			for(int i=0; i<LOOP_CNT; i++) {
				try{
					connect(serverName, adminUsername, adminPassword, nodeId);
					break; // leave if connect works fine
				} catch(AssertException e) {
					if (i+1==LOOP_CNT) {
						throw e;
					}
					log.error("Could not connect to IM (yet?). Retrying in 2sec...", e);
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e1) {
						// this empty catch is okay
					}
				}
			}
		}
	}

	private void connect(String servername, String adminUsername, String adminPassword, String nodeId) {
		try {
			ConnectionConfiguration conConfig = new ConnectionConfiguration(servername, 5222);
			conConfig.setNotMatchingDomainCheckEnabled(false);
			conConfig.setSASLAuthenticationEnabled(false);
			//the admin reconnects automatically upon server restart or failure but *not* on a resource conflict and a manual close of the connection
			conConfig.setReconnectionAllowed(true);
			//conConfig.setDebuggerEnabled(true);
			if (connection == null || !connection.isAuthenticated()) {
				connection = new XMPPConnection(conConfig);
				connection.connect();
				connection.addConnectionListener(new ConnectionListener() {
	
					public void connectionClosed() {
						log.warn("IM admin connection closed!");
					}
	
					public void connectionClosedOnError(Exception arg0) {
						log.warn("IM admin connection closed on exception!", arg0);
					}
	
					public void reconnectingIn(int arg0) {
						log.warn("IM admin connection reconnectingIn "+arg0);
					}
	
					public void reconnectionFailed(Exception arg0) {
						log.warn("IM admin connection reconnection failed: ", arg0);
					}
	
					public void reconnectionSuccessful() {
						log.warn("IM admin connection reconnection successful");
					}
					
				});
				if (nodeId != null) {
					connection.login(adminUsername, adminPassword, "node"+nodeId);
					log.info("connected to IM at "+servername+" with user "+adminUsername+" and nodeId "+nodeId);
				} else {
					connection.login(adminUsername, adminPassword);
					log.info("connected to IM at "+servername+" with user "+adminUsername);
				}
			}
			//TODO:gs why does auto reconnect not work
		} catch (XMPPException e) {
			throw new AssertException("Instant Messaging is enabled in olat.properties but instant messaging server not running: "
					+ e.getMessage());
		} catch (IllegalStateException e) {
			throw new AssertException(
					"Instant Messaging is enabled in olat.properties but could not connect with admin user. Does the admin user account exist on the IM server?: "
							+ e.getMessage());
		}
		if (connection == null || !connection.isAuthenticated()) {
		 throw new StartupException("could not connect to instant messaging server with admin user!");
		}
		
	}

	/**
	 * [used by spring]
	 * @return a valid connection to the IM server for the admin user
	 */
	public XMPPConnection getConnection() {
		return connection;
	}
	
	public void dispose() {
		if (connection != null) {
			connection.disconnect();
		}
	}
	
	/**
	 * manually trigger reconnection of the admin user
	 */
	public void resetAndReconnect() {
		if (connection != null) {
			if (connection.isConnected()) connection.disconnect();
			connect(this.serverName, this.adminUsername, this.adminPassword, this.nodeId);
		}
	}

}
