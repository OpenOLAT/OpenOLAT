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
* software distributed under the License is distributed on an "AS IS" BASIS, <br />
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br />
* See the License for the specific language governing permissions and <br />
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br />
* University of Zurich, Switzerland.
* <p>
*/
package org.olat.instantMessaging.syncservice;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.olat.instantMessaging.ui.ConnectedUsersListEntry;
import org.xmlpull.v1.XmlPullParser;

/**
 * 
 * Description:<br>
 * Creates an XMPP package with a custom nameSpace and handles the response of it.
 * This is used to talk with the instant messaging server over the same protocol that is 
 * also used for chat/groupchat stuff. With this class we use the IM server to get information of all users
 * clusterwide.
 * 
 * <P>
 * Initial Date:  04.08.2008 <br>
 * @author guido
 */
public class SessionItems extends IQ {

	public static final String NAMESPACE = "iq:user:sessionitems";
	static final String NAMESPACE_ELEMENT = "<query xmlns=\"" + NAMESPACE + "\"/>";
	private List<ConnectedUsersListEntry> sessions = new ArrayList<ConnectedUsersListEntry>();
	
	public SessionItems() {
		setType(IQ.Type.GET);
	}

	@Override
	public String getChildElementXML() {
		return NAMESPACE_ELEMENT;
	}
	
	/**
	 * inner class
	 * @author guido
	 *
	 */
	public static class Provider implements IQProvider {

		/** Creates a new Provider. ProviderManager requires that every PacketExtensionProvider
		 *  has a public,no-argument constructor 
		 */
		public Provider() {
			/***/
		}

		public IQ parseIQ(XmlPullParser parser) throws Exception {
			String username = "";
			String presenceStatus ="";
			String presenceMsg ="";
			long lastActivity = 0;
			long loginTime = 0;
			String resource = "";

			IMSessionItems items = new IMSessionItems();
			IMSessionItems.Item item;
			boolean done = false;
			while (!done) {
				int eventType = parser.next();
				
        if (eventType == XmlPullParser.START_TAG && "item".equals(parser.getName())) {
            // Initialize the variables from the parsed XML
            username = parser.getAttributeValue("", "username");
            presenceStatus = parser.getAttributeValue("", "presenceStatus");
            presenceMsg = parser.getAttributeValue("", "presenceMsg");
            lastActivity = Long.valueOf(parser.getAttributeValue("", "lastActivity")).longValue();
            loginTime = Long.valueOf(parser.getAttributeValue("", "loginTime")).longValue();
            resource = parser.getAttributeValue("", "resource");
        }
        else if (eventType == XmlPullParser.END_TAG && "item".equals(parser.getName())) {
            // Create a new Item and add it to DiscoverItems.
            item = new IMSessionItems.Item(username);
            item.setPresenceStatus(presenceStatus);
            item.setPresenceMsg(presenceMsg);
            item.setLastActivity(lastActivity);
            item.setLoginTime(loginTime);
						item.setResource(resource);
            items.addItem(item);
        }
        else if (eventType == XmlPullParser.END_TAG && "query".equals(parser.getName())) {
            done = true;
        }
			}
			return items;
		}
	}

	/**
	 * 
	 * @return the active session items on the IM server
	 */
	public List<ConnectedUsersListEntry> getSessionItems() {
		return sessions;
	}

}
