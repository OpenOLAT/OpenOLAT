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
package org.olat.ldap.manager;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;

import org.olat.ldap.LDAPSyncConfiguration;
import org.olat.ldap.model.LDAPUser;

/**
 * 
 * Initial date: 24.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LDAPUserVisitor implements LDAPVisitor {
	
	private final LDAPSyncConfiguration syncConfiguration;
	private final List<LDAPUser> ldapUserList = new ArrayList<>();
	
	public LDAPUserVisitor(LDAPSyncConfiguration syncConfiguration) {
		this.syncConfiguration = syncConfiguration;
	}
	
	public List<LDAPUser> getLdapUserList() {
		return ldapUserList;
	}

	@Override
	public void visit(SearchResult searchResult) throws NamingException {
		Attributes resAttribs = searchResult.getAttributes();
		String dn = searchResult.getNameInNamespace();
		
		LDAPUser ldapUser = new LDAPUser();
		ldapUser.setDn(dn);
		ldapUser.setAttributes(resAttribs);
		ldapUser.setCoach(LDAPDAO.hasAttributeValue(resAttribs, syncConfiguration.getCoachRoleAttribute(), syncConfiguration.getCoachRoleValue()));
		ldapUser.setAuthor(LDAPDAO.hasAttributeValue(resAttribs, syncConfiguration.getAuthorRoleAttribute(), syncConfiguration.getAuthorRoleValue()));
		ldapUser.setUserManager(LDAPDAO.hasAttributeValue(resAttribs, syncConfiguration.getUserManagerRoleAttribute(), syncConfiguration.getUserManagerRoleValue()));
		ldapUser.setGroupManager(LDAPDAO.hasAttributeValue(resAttribs, syncConfiguration.getGroupManagerRoleAttribute(), syncConfiguration.getGroupManagerRoleValue()));
		ldapUser.setQpoolManager(LDAPDAO.hasAttributeValue(resAttribs, syncConfiguration.getQpoolManagerRoleAttribute(), syncConfiguration.getQpoolManagerRoleValue()));
		ldapUser.setCurriculumManager(LDAPDAO.hasAttributeValue(resAttribs, syncConfiguration.getCurriculumManagerRoleAttribute(), syncConfiguration.getCurriculumManagerRoleValue()));
		ldapUser.setLearningResourceManager(LDAPDAO.hasAttributeValue(resAttribs, syncConfiguration.getLearningResourceManagerRoleAttribute(), syncConfiguration.getLearningResourceManagerRoleValue()));

		List<String> groupList = LDAPDAO.parseGroupList(resAttribs, syncConfiguration.getGroupAttribute(), syncConfiguration.getGroupAttributeSeparator());
		ldapUser.setGroupIds(groupList);
		List<String> coachedGroupList = LDAPDAO.parseGroupList(resAttribs, syncConfiguration.getCoachedGroupAttribute(), syncConfiguration.getCoachedGroupAttributeSeparator());
		ldapUser.setCoachedGroupIds(coachedGroupList);
		
		ldapUserList.add(ldapUser);
	}
	

}