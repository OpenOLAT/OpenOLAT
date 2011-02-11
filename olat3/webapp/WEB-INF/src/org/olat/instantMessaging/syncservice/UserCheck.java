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

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

/**
 * Description:<br>
 * check whether an im accounts exists on the server or not
 * 
 * <P>
 * Initial Date:  13.08.2008 <br>
 * @author guido
 */
public class UserCheck extends IQ {
	
	static final String NAMESPACE = "iq:user:check";
	private String username;
	private Boolean hasAccount;

	public UserCheck(String username) {
		this.username = username;
		setType(IQ.Type.GET);
	}

	public UserCheck() {
		//
	}
	
	public void setHasAccount(Boolean account) {
		this.hasAccount = account;
	}

	protected Boolean hasAccount() {
		return hasAccount;
	}

	/**
	 * @see org.jivesoftware.smack.packet.IQ#getChildElementXML()
	 */
	@Override
	public String getChildElementXML() {
		StringBuilder buf = new StringBuilder();
		buf.append("<query xmlns=\"").append(NAMESPACE).append("\">");
    buf.append("<username>").append(username).append("</username>");
    buf.append("</query>");
    return buf.toString();
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

			UserCheck check = new UserCheck();
			boolean done = false;
			while (!done) {
				int eventType = parser.next();
				if (eventType == XmlPullParser.START_TAG) {
					if (parser.getName().equals("exists")) {
						check.setHasAccount(Boolean.valueOf(parser.nextText()));
						done = true;
					}
				}
			}
			return check;
		}
	}

}
