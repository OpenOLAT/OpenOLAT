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
 * also used for chat/groupchat stuff
 * 
 * <P>
 * Initial Date:  04.08.2008 <br>
 * @author guido
 */
public class GroupDelete extends IQ {
	
	static final String NAMESPACE = "iq:group:delete";
	private String groupname;
	private boolean deleted;

	/**
	 * 
	 */
	public GroupDelete(String username) {
		this.groupname = username;
		setType(IQ.Type.SET);
	}
	
	/**
	 * 
	 */
	public GroupDelete() {
		//
	}

	@Override
	public String getChildElementXML() {
		StringBuilder buf = new StringBuilder();
		buf.append("<query xmlns=\"").append(NAMESPACE).append("\">");
        buf.append("<groupname>").append(groupname).append("</groupname>");
        buf.append("</query>");
        return buf.toString();
	}
	
	
	protected void setDeleted(boolean success) {
		this.deleted = success;
	}
	
	public boolean isDeleted() {
		return this.deleted;
	}
	
	public static class Provider implements IQProvider {
		  		
				/** Creates a new Provider. ProviderManager requires that every PacketExtensionProvider has a public,no-argument
		   		constructor  */
				public Provider() {/***/}

				public IQ parseIQ(XmlPullParser parser) throws Exception {
					
					GroupDelete groupDelete = new GroupDelete();
					boolean done = false;
					while (!done) {
						int eventType = parser.next();
					       if (eventType == XmlPullParser.START_TAG) {
					    	   groupDelete.setDeleted(true);
					    	   done = true;
					       }
					}
				return groupDelete;
		}
	}
	
	
}
