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
* <p>
*/
package org.olat.instantMessaging.syncservice;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

/**
 * 
 * Description:<br>
 * Creates an XMPP package with a custom nameSpace and handles the response of it.
 * This is used to talk with the instant messaging server over the same protocol that is 
 * also used for chat/groupchat stuff. With this class we use the IM server to count all users
 * clusterwide.
 * 
 * <P>
 * Initial Date:  04.08.2008 <br>
 * @author guido
 */
public class SessionCount extends IQ {

	public static final String NAMESPACE = "iq:user:sessioncount";
	static final String NAMESPACE_ELEMENT = "<query xmlns=\"" + NAMESPACE + "\"/>";
	private int numberOfSessions;
	
	public SessionCount() {
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

			SessionCount sCount = new SessionCount();
			boolean done = false;
			while (!done) {
				int eventType = parser.next();
				if (eventType == XmlPullParser.START_TAG) {
					if (parser.getName().equals("sessioncount")) {
						String sessions = parser.nextText();
						sCount.setNumberOfSessions(Integer.valueOf(sessions).intValue());
						done = true;
					}
				}
			}
			return sCount;
		}
	}

	void setNumberOfSessions(int numberOfSessions) {
		this.numberOfSessions = numberOfSessions;
		
	}
	/**
	 * 
	 * @return the number of active sessions on the IM server
	 */
	public int getNumberOfSessions() {
		return this.numberOfSessions;
	}

}
