/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.instantMessaging.ui;

import org.olat.instantMessaging.model.Buddy;


/**
 * 
 * Initial date: 07.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RosterEntry implements Comparable<RosterEntry>{

	private final Long identityKey;
	
	private String fullName;
	private String nickName;
	
	private boolean anonym;
	private String status;
	
	public RosterEntry(Buddy buddy) {
		this(buddy.getIdentityKey(), null, buddy.getFullname(), false, buddy.getStatus());
	}
	
	public RosterEntry(Long identityKey, String nickName, String fullName, boolean anonym, String status) {
		this.identityKey = identityKey;
		this.nickName = nickName;
		this.fullName = fullName;
		this.anonym = anonym;
		this.status = status;
	}
	
	public Long getIdentityKey() {
		return identityKey;
	}
	
	public String getName() {
		if(anonym) {
			return nickName;
		}
		return fullName;
	}
	
	public String getFullName() {
		return fullName;
	}
	
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	
	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public boolean isAnonym() {
		return anonym;
	}
	
	public void setAnonym(boolean anonym) {
		this.anonym = anonym;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	@Override
	public int compareTo(RosterEntry o) {
		if(o == null) return 1;
		int result = 0;
		
		String n1 = getName();
		String n2 = o.getName();
		if(n1 == null) return -1;
		if(n2 == null) return 1;
		result = n1.compareTo(n2);
		if(result == 0 && identityKey != null && o.identityKey != null) {
			result = identityKey.compareTo(o.identityKey);
		}
		return result;
	}

	@Override
	public int hashCode() {
		return identityKey == null ? 934785 : identityKey.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof RosterEntry) {
			RosterEntry b = (RosterEntry)obj;
			return identityKey != null && identityKey.equals(b.identityKey);
		}	
		return false;
	}

	@Override
	public String toString() {
		return "rosterEntry[identityKey=" + identityKey + ":fullName=" + fullName + ":nickName=" + nickName + ":anonym=" + anonym + ":status=" + status + "]";
	}
}
