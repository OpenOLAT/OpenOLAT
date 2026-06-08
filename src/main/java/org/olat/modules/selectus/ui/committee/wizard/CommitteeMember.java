/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.committee.wizard;

import org.olat.core.id.Identity;
import org.olat.core.util.CodeHelper;

/**
 * 
 * Initial date: 21 oct. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CommitteeMember {
	
	private String role;
	private String email;
	private Identity identity;
	private CommitteeMemberStatus status;
	
	private String identifier;
	
	public CommitteeMember(String role, String email, Identity identity) {
		this.role = role;
		this.email = email;
		this.identity = identity;
		if(identity.getKey() == null) {
			identifier = CodeHelper.getUniqueID();
		} else {
			identifier = identity.getKey().toString();
		}
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public String getRole() {
		return role;
	}
	
	public void setRole(String role) {
		this.role = role;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public Identity getIdentity() {
		return identity;
	}
	
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	public CommitteeMemberStatus getStatus() {
		return status;
	}

	public void setStatus(CommitteeMemberStatus status) {
		this.status = status;
	}
}
