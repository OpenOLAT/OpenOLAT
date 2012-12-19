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
package org.olat.instantMessaging.model;

import org.olat.core.id.OLATResourceable;

/**
 * 
 * Initial date: 05.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Buddy implements OLATResourceable, Comparable<Buddy> {

	private final Long identityKey;
	private final String fullname;
	private final String nickName;
	
	public Buddy(Long identityKey, String fullname, String nickName) {
		this.identityKey = identityKey;
		this.fullname = fullname;
		this.nickName = nickName;
	}
	
	public String getStatus() {
		return "available";
	}

	public Long getIdentityKey() {
		return identityKey;
	}

	public String getNickName() {
		return nickName;
	}

	public String getFullname() {
		return fullname;
	}

	@Override
	public String getResourceableTypeName() {
		return "Buddy";
	}

	@Override
	public Long getResourceableId() {
		return identityKey;
	}

	@Override
	public int compareTo(Buddy o) {
		if(o == null) return -1;
		int result = 0;
		if(fullname != null && o.fullname != null) {
			result = fullname.compareTo(o.fullname);
		}
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
		if(obj instanceof Buddy) {
			Buddy b = (Buddy)obj;
			return identityKey != null && identityKey.equals(b.identityKey);
		}	
		return false;
	}

	@Override
	public String toString() {
		return "buddy[identityKey=" + identityKey + ":fullname=" + fullname + "]";
	}
}