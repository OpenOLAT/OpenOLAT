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
*/
package org.olat.instantMessaging.syncservice;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.ProviderManager;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.instantMessaging.AdminUserConnection;

/**
 * Description:<br>
 * counts IM sessions cluster wide by sending custom xmpp packet to IM server
 * 
 * <P>
 * Initial Date:  07.08.2008 <br>
 * @author guido
 */
public class RemoteSessionCountOverXMPP implements InstantMessagingSessionCount {
	
	OLog log = Tracing.createLoggerFor(this.getClass());
	private int sessionCount = 0;
	private AdminUserConnection adminUser;
	
	RemoteSessionCountOverXMPP() {
		//register IQ handler
		ProviderManager providerMgr = ProviderManager.getInstance();
		providerMgr.addIQProvider("query", SessionCount.NAMESPACE, new SessionCount.Provider());
		
	}
	
	public void setConnection(AdminUserConnection adminUser) {
		this.adminUser = adminUser;
	}

	/**
	 * @see org.olat.instantMessaging.syncservice.IInstantMessagingSessionCount#countSessions()
	 */
	public int countSessions() {
		XMPPConnection con = adminUser.getConnection();
		if (con != null && con.isConnected()) {
			//TODO:gs may put in other thread???
			SessionCount response;
			try {
				IQ packet = new SessionCount();
				packet.setFrom(con.getUser());
				PacketCollector collector = con.createPacketCollector(new PacketIDFilter(packet.getPacketID()));
				con.sendPacket(packet);
				response = (SessionCount) collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
				collector.cancel();
				if (response == null) {
					log.warn("Error while trying to count sessions at IM server. Response was null!");
					return sessionCount;
				}
				if (response.getError() != null) {
					log.warn("Error while trying to count sessions at IM server. "+response.getError().getMessage());
					return sessionCount;
				} else if (response.getType() == IQ.Type.ERROR) {
					log.warn("Error while trying to count sessions at IM server");
					return sessionCount;
				}
				sessionCount = response.getNumberOfSessions();
				if (sessionCount > 0) return sessionCount-1;
				return sessionCount;
	
			} catch (Exception e) {
				log.warn("Error while trying to count sessions at IM server. Response was null!", e);
				return sessionCount;
			}
		
		} return sessionCount;
	}

}
