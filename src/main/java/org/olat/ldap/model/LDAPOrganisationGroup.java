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

import java.util.List;
import java.util.Set;

import org.olat.core.id.Organisation;

/**
 * 
 * Initial date: 8 déc. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LDAPOrganisationGroup {
	
	private final String commonName;
	private final Set<String> members;
	private final Organisation organisation;
	
	private LDAPOrganisationGroup(Organisation organisation, String commonName, List<String> members) {
		this.commonName = commonName;
		this.organisation = organisation;
		this.members = Set.copyOf(members);
	}
	
	public static LDAPOrganisationGroup valueOf(LDAPGroup group, Organisation organisation) {
		return new LDAPOrganisationGroup(organisation, group.getCommonName(), group.getMembers());
	}
	
	public String getCommonName() {
		return commonName;
	}
	
	public Organisation getOrganisation() {
		return organisation;
	}
	
	public boolean isMember(String member) {
		return members.contains(member);
	}

}
