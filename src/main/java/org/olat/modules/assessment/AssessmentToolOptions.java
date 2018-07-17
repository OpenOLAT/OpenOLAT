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
package org.olat.modules.assessment;

import java.util.List;

import org.olat.basesecurity.Group;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;

/**
 * 
 * Initial date: 20.12.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentToolOptions {
	
	private boolean admin;
	private BusinessGroup group;
	private List<Group> groups;
	private List<Identity> identities;
	
	private boolean nonMembers;
	
	public AssessmentToolOptions() {
		//
	}
	
	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public boolean isNonMembers() {
		return nonMembers;
	}

	public void setNonMembers(boolean nonMembers) {
		this.nonMembers = nonMembers;
	}

	public BusinessGroup getGroup() {
		return group;
	}
	
	public void setGroup(BusinessGroup group) {
		this.group = group;
	}
	
	public List<Identity> getIdentities() {
		return identities;
	}
	
	public void setIdentities(List<Identity> identities) {
		this.identities = identities;
	}

	/**
	 * This is a list of groups which eventually (if set) can
	 * used instead of the list of identities.
	 * 
	 * @return A list of base groups.
	 */
	public List<Group> getAlternativeGroupsOfIdentities() {
		return groups;
	}

	public void setAlternativeGroupsOfIdentities(List<Group> groups) {
		this.groups = groups;
	}
	
	
	
}
