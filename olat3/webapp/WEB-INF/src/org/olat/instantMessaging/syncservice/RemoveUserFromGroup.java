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

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

/**
 * 
 * Description:<br>
 * TODO: guido Class Description for AddUserToGroup
 * 
 * <P>
 * Initial Date:  04.08.2008 <br>
 * @author guido
 */
public class RemoveUserFromGroup extends IQ {

	static final String NAMESPACE = "iq:group:user:remove";
	private boolean deleted;
	private String username;
	private String groupname;

	/**
	 * 
	 * @param username
	 * @param groupname
	 */
	public RemoveUserFromGroup(String username, String groupname) {
		this.username = username;
		this.groupname = groupname;
		setType(IQ.Type.SET);
	}
	
	public RemoveUserFromGroup() {
		//
	}

	@Override
	public String getChildElementXML() {
		StringBuilder buf = new StringBuilder();
		buf.append("<query xmlns=\"").append(NAMESPACE).append("\">");
        buf.append("<username>").append(username).append("</username>");
        buf.append("<groupname>").append(groupname).append("</groupname>");
        buf.append("</query>");
        return buf.toString();
	}
	
	public static class Provider implements IQProvider {
  		
		/** Creates a new Provider. ProviderManager requires that every PacketExtensionProvider has a public,no-argument
   		constructor  */
		public Provider() {/***/}

		public IQ parseIQ(XmlPullParser parser) throws Exception {
			
			RemoveUserFromGroup removeUser = new RemoveUserFromGroup();
			boolean done = false;
			while (!done) {
				int eventType = parser.next();
			       if (eventType == XmlPullParser.START_TAG) {
			    	   removeUser.setCreated(true);
			    	   done = true;
			    	 }
			}
		return removeUser;
		}
	}

	protected void setCreated(boolean created) {
		this.deleted = created;
	}

	public boolean isCreated() {
		return deleted;
	}

}
