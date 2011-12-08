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
import org.olat.core.logging.LogDelegator;
import org.olat.core.logging.StartupException;

/**
 * Description:<br>
 * create a connection object for the admin user
 * 
 * <P>
 * Initial Date:  11.08.2008 <br>
 * @author guido
 */
public class AdminUserConnection extends LogDelegator {

	private XMPPConnection connection;
	private IMConfig imConfig;

	/**
	 * [used by spring]
	 * @param serverName
	 * @param adminUsername
	 * @param adminPassword
	 * @param nodeId
	 */
	private AdminUserConnection(IMConfig imConfig) {
		if (imConfig == null || imConfig.getAdminName() == null || imConfig.getAdminPassword() == null || imConfig.getServername() == null) {
			throw new StartupException("Instant Messaging settings for admin user are not correct");
		}
		this.imConfig = imConfig;

		if (imConfig.isEnabled()) {
			final int LOOP_CNT=5;
			for(int i=0; i<LOOP_CNT; i++) {
				try{
					connect(imConfig.getServername(), imConfig.getAdminName(), imConfig.getAdminPassword(), imConfig.getNodeId());
					break; // leave if connect works fine
				} catch(AssertException e) {
					if (i+1==LOOP_CNT) {
						// fxdiff: allow startup without IM
						logError("Could not connect to IM within " + LOOP_CNT + " attempts. IM will not be available for this OLAT instance! You could try to enable it later by reconnect in IMAdmin.", e);
						return;
//						throw e; // dont throw anything, spring gets confused during startup!
					}
					logError("Could not connect to IM (yet?). Retrying in 2sec...", e);
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
			//fxdiff: FXOLAT-233 don't reconnect upon lost connection here, each sendPacket will try for its own. this reconnection never stops!
			conConfig.setReconnectionAllowed(false);
			//conConfig.setDebuggerEnabled(true);
			if (connection == null || !connection.isAuthenticated()) {
				connection = new XMPPConnection(conConfig);
				connection.connect();
				connection.addConnectionListener(new ConnectionListener() {
	
					public void connectionClosed() {
						logWarn("IM admin connection closed!", null);
					}
	
					public void connectionClosedOnError(Exception arg0) {
						logWarn("IM admin connection closed on exception!", arg0);
					}
	
					public void reconnectingIn(int arg0) {
						logWarn("IM admin connection reconnectingIn "+arg0, null);
					}
	
					public void reconnectionFailed(Exception arg0) {
						logWarn("IM admin connection reconnection failed: ", arg0);
					}
	
					public void reconnectionSuccessful() {
						logWarn("IM admin connection reconnection successful", null);
					}
					
				});
				if (nodeId != null) {
					connection.login(adminUsername, adminPassword, "node"+nodeId);
					logInfo("connected to IM at "+servername+" with user "+adminUsername+" and nodeId "+nodeId);
				} else {
					connection.login(adminUsername, adminPassword);
					logInfo("connected to IM at "+servername+" with user "+adminUsername);
				}
			}
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
		if (!precheckConnection()) return null;
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
			connect(imConfig.getServername(), imConfig.getAdminName(), imConfig.getAdminPassword(), imConfig.getNodeId());
		}
	}
	
	//fxdiff: FXOLAT-233 precheck if connection is still available, or try once to reconnect.
	private boolean precheckConnection(){
		if (connection != null && connection.isConnected()) return true;
		else {
			try {
				resetAndReconnect();
			} catch (Exception e) {
				logWarn("the lost IM connection could not be recovered", null);
				return false;
			}
			return (connection != null && connection.isConnected());
		}
	}

}
