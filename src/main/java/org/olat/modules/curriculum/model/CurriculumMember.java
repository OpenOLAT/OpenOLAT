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
package org.olat.modules.curriculum.model;

import java.util.Date;

import org.olat.basesecurity.GroupMembershipInheritance;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 19 juin 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumMember implements CreateInfo {
	
	private final Identity identity;
	private final String role;
	private final GroupMembershipInheritance inheritanceMode;
	private final Date creationDate;
	
	public CurriculumMember(Identity identity, String role, GroupMembershipInheritance inheritanceMode, Date creationDate) {
		this.identity = identity;
		this.role = role;
		this.inheritanceMode = inheritanceMode;
		this.creationDate = creationDate;
	}

	public Identity getIdentity() {
		return identity;
	}

	public String getRole() {
		return role;
	}

	public GroupMembershipInheritance getInheritanceMode() {
		return inheritanceMode;
	}
	
	@Override
	public Date getCreationDate() {
		return creationDate;
	}
}
