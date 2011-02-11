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
package org.olat.instantMessaging.syncservice;

import java.util.Iterator;
import java.util.List;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.ProviderManager;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.instantMessaging.AdminUserConnection;

/**
 * Description:<br>
 * creates groups on the remote IM server
 * 
 * <P>
 * Initial Date:  31.07.2008 <br>
 * @author guido
 */
public class RemoteGroupCreationOverXMPP implements InstantMessagingGroupSynchronisation {
	
	OLog log = Tracing.createLoggerFor(this.getClass());
	private AdminUserConnection adminUser;

	public RemoteGroupCreationOverXMPP() {
		
		ProviderManager providerMgr = ProviderManager.getInstance();
		//register iq handler
		providerMgr.addIQProvider("query", GroupCreate.NAMESPACE, new GroupCreate.Provider());
		providerMgr.addIQProvider("query", GroupDelete.NAMESPACE, new GroupDelete.Provider());
		
		providerMgr.addIQProvider("query", AddUserToGroup.NAMESPACE, new AddUserToGroup.Provider());
		providerMgr.addIQProvider("query", RemoveUserFromGroup.NAMESPACE, new RemoveUserFromGroup.Provider());
	}
	
	public void setConnection(AdminUserConnection adminUser) {
		this.adminUser = adminUser;
	}

	/**
	 * @see org.olat.instantMessaging.InstantMessagingGroupSynchronisation#addUserToSharedGroup(java.lang.String, org.olat.core.id.Identity)
	 */
	public boolean addUserToSharedGroup(String groupId, String username) {
		boolean statusOk = sendPacket(new AddUserToGroup(username, groupId));
		if (!statusOk) {
			log.error("failed to add user to shard group: "+groupId + " username: "+username);
		}
		return statusOk;
		
	}

	/**
	 * @see org.olat.instantMessaging.InstantMessagingGroupSynchronisation#addUsersToSharedGroup(java.lang.String, java.util.List)
	 */
	public boolean addUsersToSharedGroup(String groupId, List<String> users) {
		boolean statusOk = true;
		for (Iterator<String> iterator = users.iterator(); iterator.hasNext();) {
			String username = iterator.next();
			if (!sendPacket(new AddUserToGroup(username, groupId))) {
				log.error("adding user to shard group failed. group: "+groupId+" and user: "+username);
				statusOk = false;
			}
		}
		return statusOk;
	}

	/**
	 * @see org.olat.instantMessaging.InstantMessagingGroupSynchronisation#createSharedGroup(java.lang.String, java.lang.String, java.util.List)
	 */
	public boolean createSharedGroup(String groupId, String displayName, List<String> initialMembers) {
		//TODO:gs: pass description from groups as well
		boolean statusOk = true;
		if (!sendPacket(new GroupCreate(groupId, displayName, null))) {
			log.error("could not create shared group: "+groupId);
			statusOk = false;
		}
		if (initialMembers != null) {
			int cnt=0;
			for (Iterator<String> iterator = initialMembers.iterator(); iterator.hasNext();) {
				String username = iterator.next();
				if (!sendPacket(new AddUserToGroup(username, groupId))) {
					log.error("adding user to shard group failed. group: "+groupId+" and user: "+username);
					statusOk = false;
				}
				if (++cnt%8==0) {
					DBFactory.getInstance().intermediateCommit();
				}
			}
		}
		return statusOk;
	}
	
	public boolean createSharedGroup(String groupId, String displayName) {
		return createSharedGroup(groupId, displayName, null);
	}

	/**
	 * @see org.olat.instantMessaging.InstantMessagingGroupSynchronisation#deleteSharedGroup(java.lang.String)
	 */
	public boolean deleteSharedGroup(String groupId) {
		boolean statusOk = sendPacket(new GroupDelete(groupId));
		if(!statusOk) {
			// OLAT-5384: error happens frequently, lowering to WARN
			log.warn("could not delete shared group: "+groupId);
		}
		return statusOk;
	}

	/**
	 * @see org.olat.instantMessaging.InstantMessagingGroupSynchronisation#removeUserFromSharedGroup(java.lang.String, java.lang.String)
	 */
	public boolean removeUserFromSharedGroup(String groupId, String username) {
		boolean statusOk =  sendPacket(new RemoveUserFromGroup(username, groupId));
		if(!statusOk) {
			log.error("could not remove user from shared group: "+groupId + " username: "+username);
		}
		return statusOk;
	}

	/**
	 * @see org.olat.instantMessaging.InstantMessagingGroupSynchronisation#removeUsersFromSharedGroup(java.lang.String, java.lang.String[])
	 */
	public boolean removeUsersFromSharedGroup(String groupId, String[] users) {
		for (int i = 0; i < users.length; i++) {
			String username = users[i];
			if (!sendPacket(new RemoveUserFromGroup(username, groupId))) {
				log.error("removing user from shared group failed. group: "+groupId+" and user: "+username);
			}
		}
		return true;
	}

	/**
	 * @see org.olat.instantMessaging.InstantMessagingGroupSynchronisation#renameSharedGroup(java.lang.String, java.lang.String)
	 */
	public boolean renameSharedGroup(String groupId, String displayName) {
		//if group exists it will be renamed
		return createSharedGroup(groupId, displayName, null);
	}
	
	private boolean sendPacket(IQ packet) {
		XMPPConnection con = adminUser.getConnection();
		try {
			packet.setFrom(con.getUser());
			PacketCollector collector = con.createPacketCollector(new PacketIDFilter(packet.getPacketID()));
			con.sendPacket(packet);
			IQ response = (IQ) collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
			collector.cancel();
			
			if (response == null) {
				log.error("Error while trying to create/delete group at IM server. Response was null! packet type: "+packet.getClass());
				return false;
			}
			if (response.getError() != null) {
				if (response.getError().getCode() == 409) {
					//409 -> conflict / group already exists
					return true;
				} else if (response.getError().getCode() == 404) {
					//404 -> not found, does not matter when trying to delete
					return true;
				}
				log.error("Error while trying to create/delete group at IM server. "+response.getError().getMessage());
				return false;
			} else if (response.getType() == IQ.Type.ERROR) {
				log.error("Error while trying to create/delete group at IM server");
				return false;
			}
			return true;
		} catch (RuntimeException e) {
			log.error("Error while trying to create/delete group at IM server");
			return false;
		}
	}

}
