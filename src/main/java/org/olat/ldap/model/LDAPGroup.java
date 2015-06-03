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
package org.olat.ldap.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Initial date: 24.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LDAPGroup {
	
	private String commonName;
	private List<String> members;
	private List<LDAPUser> participants = new ArrayList<>();
	private List<LDAPUser> coaches = new ArrayList<>();
	
	public LDAPGroup() {
		//
	}
	
	public LDAPGroup(String commonName) {
		this.commonName = commonName;
		this.members = new ArrayList<>();
	}
	
	public String getCommonName() {
		return commonName;
	}
	
	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}
	
	public List<String> getMembers() {
		return members;
	}
	
	public void setMembers(List<String> members) {
		this.members = members;
	}
	
	
	public List<LDAPUser> getParticipants() {
		return participants;
	}

	public List<LDAPUser> getCoaches() {
		return coaches;
	}

	@Override
	public int hashCode() {
		return commonName == null ? 987234895 : commonName.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof LDAPGroup) {
			LDAPGroup group = (LDAPGroup)obj;
			return commonName != null && commonName.equals(group.getCommonName());
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "ldapGroup[cn=" + commonName + "]" + super.toString();
	}
}