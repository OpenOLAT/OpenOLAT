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
 * return plugin version 
 * 
 * <P>
 * Initial Date:  07.08.2008 <br>
 * @author guido
 */
public class RemotePluginVersionOverXMPP implements InstantMessagingServerPluginVersion {
	
	OLog log = Tracing.createLoggerFor(this.getClass());
	private String version;
	private AdminUserConnection adminUser;
	
	RemotePluginVersionOverXMPP() {
		//register IQ handler
		ProviderManager providerMgr = ProviderManager.getInstance();
		providerMgr.addIQProvider("query", PluginVersion.NAMESPACE, new PluginVersion.Provider());
		
	}
	
	public void setConnection(AdminUserConnection adminUser) {
		this.adminUser = adminUser;
	}

	/**
	 * @see org.olat.instantMessaging.syncservice.IInstantMessagingSessionCount#countSessions()
	 */
	public String getPluginVersion() {
		XMPPConnection con = adminUser.getConnection();
		if (con != null && con.isConnected()) {
			PluginVersion response;
			try {
				IQ packet = new PluginVersion();
				packet.setFrom(con.getUser());
				PacketCollector collector = con.createPacketCollector(new PacketIDFilter(packet.getPacketID()));
				//TODO:gs is sending packets over one connection thread save?
				con.sendPacket(packet);
				response = (PluginVersion) collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
				collector.cancel();
				if (response == null) {
					log.error("Error while trying to get version at IM server. Response was null!");
					return version;
				}
				if (response.getError() != null) {
					log.error("Error while trying to get version at IM server. "+response.getError().getMessage());
					return version;
				} else if (response.getType() == IQ.Type.ERROR) {
					log.error("Error while trying to get version at IM server");
					return version;
				}
				return response.getVersion();
				
			} catch (Exception e) {
				log.error("Error while trying to get version at IM server. Response was null!", e);
				return version;
			}
		
		} return version;
	}

}
