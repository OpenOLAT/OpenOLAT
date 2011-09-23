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

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.packet.IQ;

/**
 * Description:<br>
 * fetches a list of all currently logged in users on the IM server
 * 
 * <P>
 * Initial Date:  06.08.2008 <br>
 * @author guido
 */
public class IMSessionItems<E> extends IQ {


	private List<Item> items = new ArrayList<Item>();

	/**
	 * @see org.jivesoftware.smack.packet.IQ#getChildElementXML()
	 */
	@Override
	public String getChildElementXML() {
		StringBuilder buf = new StringBuilder();
    buf.append("<query xmlns=\"iq:user:sessionitems\"");
    synchronized (items) {
        for (Item item : items) {
            buf.append(item.toXML());
        }
    }
    buf.append("</query>");
    return buf.toString();
	}

	public  void addItem(Item item) {
		items.add(item);
	}
	
	public List<Item> getItems() {
		return this.items;
	}


	
	

	public static class Item {
		
		

		private String username;
		private long loginTime;
		private long lastActivity;
		private String presenceMsg;
		private String presenceStatus;
		private String resource;

		Item(String username) {
			this.username = username;
		}
		
		public Object toXML() {
			StringBuilder buf = new StringBuilder();
      buf.append("<item username=\"").append(username).append("\"");
      if (presenceStatus != null) {
          buf.append(" presenceStatus=\"").append(presenceStatus).append("\"");
      }
      if (presenceMsg != null) {
          buf.append(" presenceMsg=\"").append(presenceMsg).append("\"");
      }
      if (lastActivity != 0) {
          buf.append(" lastActivity=\"").append(lastActivity).append("\"");
      }
      if (loginTime != 0) {
        buf.append(" loginTime=\"").append(loginTime).append("\"");
    }
      buf.append("/>");
      return buf.toString();
		}

		public void setLoginTime(long loginTime) {
			this.loginTime = loginTime;
		}

		public void setLastActivity(long lastActivity) {
			this.lastActivity = lastActivity;
		}

		public void setPresenceMsg(String presenceMsg) {
			this.presenceMsg = presenceMsg;
		}

		public void setPresenceStatus(String presenceStatus) {
			this.presenceStatus = presenceStatus;
		}

		public String getUsername() {
			return username;
		}

		public long getLoginTime() {
			return loginTime;
		}

		public long getLastActivity() {
			return lastActivity;
		}

		public String getPresenceMsg() {
			return presenceMsg;
		}

		public String getPresenceStatus() {
			return presenceStatus;
		}

		public void setResource(String resource) {
			this.resource = resource;
		}

		public String getResource() {
			return resource;
		}
		
	}


}
