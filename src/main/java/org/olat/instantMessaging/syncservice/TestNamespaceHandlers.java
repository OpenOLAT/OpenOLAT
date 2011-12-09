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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.ProviderManager;


public class TestNamespaceHandlers {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ConnectionConfiguration connConfig = new ConnectionConfiguration("idlnxgs.unizh.ch", 5222);
		connConfig.setNotMatchingDomainCheckEnabled(false);
		connConfig.setSASLAuthenticationEnabled(false);
		connConfig.setReconnectionAllowed(false);
		connConfig.setDebuggerEnabled(true);

		ProviderManager providerMgr = ProviderManager.getInstance();
		providerMgr.addIQProvider("query", UserCreate.NAMESPACE, new UserCreate.Provider());
		providerMgr.addIQProvider("query", UserDelete.NAMESPACE, new UserCreate.Provider());
		
		providerMgr.addIQProvider("query", GroupCreate.NAMESPACE, new GroupCreate.Provider());
		providerMgr.addIQProvider("query", GroupDelete.NAMESPACE, new GroupDelete.Provider());
		
		providerMgr.addIQProvider("query", AddUserToGroup.NAMESPACE, new AddUserToGroup.Provider());
		providerMgr.addIQProvider("query", RemoveUserFromGroup.NAMESPACE, new RemoveUserFromGroup.Provider());
		
		providerMgr.addIQProvider("query", SessionCount.NAMESPACE, new SessionCount.Provider());
		
		providerMgr.addIQProvider("query", SessionItems.NAMESPACE, new SessionItems.Provider());
		
		providerMgr.addIQProvider("query", UserCheck.NAMESPACE, new UserCheck.Provider());
		
		providerMgr.addIQProvider("query", PluginVersion.NAMESPACE, new PluginVersion.Provider());
		
		XMPPConnection connection = new XMPPConnection(connConfig);
		
		
		try {
			connection.connect();
			connection.login("admin", "admin");
			
			String username="demo";
			String pw="test";
			String email="test@test.ch";
			String fullName="gregor grunz";
			int i = 7;
			
			List packets = new ArrayList();
//			for (int j = 1; j < 300; j++) {
//				packets.add(new UserDelete(username+j));
//				
//			}
			//packets.add(new UserCreate(username, pw, email, fullName));
			packets.add(new UserCheck("administrator"));
			packets.add(new UserCheck("author"));
			//packets.add(new UserCheck("demo3"));
			//packets.add(new UserCheck("demo4"));
			//packets.add(new UserCheck("demo5"));
			
			//packets.add(new PluginVersion());
			
			//packets.add(new GroupCreate("testgroup","testgroup bla",  "this is the testgroup"));
			//packets.add(new GroupCreate("testgroup","testgroup bla",  "this is the testgroup"));
			//packets.add(new AddUserToGroup(username+i, "testgroup"));
			//packets.add(new AddUserToGroup("admin", "testgroup"));
			//packets.add(new RemoveUserFromGroup(username+i, "testgroup"));
			//packets.add(new GroupDelete("testgroup"));
			//packets.add(new GroupDelete("testgroup"));
			//packets.add(new UserDelete(username+i));
			//packets.add(new SessionCount());
			//packets.add(new SessionItems());
			
			
			
			Thread.sleep(1000);
			long start = System.currentTimeMillis();
			for (Iterator iterator = packets.iterator(); iterator.hasNext();) {
				IQ iqPacket = (IQ) iterator.next();
				iqPacket.setFrom(connection.getUser());
				PacketCollector collector = connection.createPacketCollector(new PacketIDFilter(iqPacket.getPacketID()));
				connection.sendPacket(iqPacket);
				IQ response = (IQ) collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
				collector.cancel();
				
				if (response == null) {
					throw new XMPPException("No response from server on status set.");
				}
				if (response.getError() != null) {
					throw new XMPPException("Error . Cause: "+response.getError().getMessage());
				} else if (response.getType() == IQ.Type.ERROR) {
					System.out.println("could not create user....");
					throw new XMPPException("Error . Cause: "+response.getError().getMessage());
				}
				if (response instanceof SessionCount) {
					SessionCount sCount = (SessionCount) response;
					System.out.println("number of sessions: "+sCount.getNumberOfSessions());
				}
				
				if (response instanceof IMSessionItems) {
					IMSessionItems items = (IMSessionItems) response;
					List<IMSessionItems.Item> l = items.getItems();
					for (Iterator iterator2 = l.iterator(); iterator2.hasNext();) {
						IMSessionItems.Item item = (IMSessionItems.Item) iterator2.next();
						System.out.println("username: "+item.getUsername() );
						System.out.println("presence msg: "+item.getPresenceMsg() );
						System.out.println("status: "+item.getPresenceStatus() );
						System.out.println("last acivity: "+item.getLastActivity() );
						System.out.println("logintime: "+item.getLoginTime() );
					}
				}
				if (response instanceof UserCheck) {
					UserCheck check = (UserCheck) response;
					System.out.println("has account responses: "+check.hasAccount());
				}
				if (response instanceof PluginVersion) {
					PluginVersion version = (PluginVersion) response;
					System.out.println("plugin version is: "+version.getVersion());
				}
				
				
				
			}
			long stop = System.currentTimeMillis();
			System.out.println("time..."+(stop-start));
	        
	        
	        Thread.sleep(1000);
	        
			connection.disconnect();
			
			Thread.sleep(10000);
			System.exit(0);
		} catch (XMPPException e) {
			System.out.println("error occured 1: "+e);
		} catch (InterruptedException e) {
			System.out.println("error occured 2: "+e);
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("error occured 3 : "+e);
		} catch (Throwable e) {
			System.out.println("error occured 4: "+e);
		}
		try {
			Thread.sleep(30000);
			System.exit(0);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
