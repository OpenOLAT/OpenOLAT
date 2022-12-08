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

package org.olat.ldap;

import java.util.Date;
import java.util.List;

import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapContext;

import org.olat.basesecurity.Authentication;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;

public interface LDAPLoginManager {

	public static final OLATResourceable ldapSyncLockOres = OresHelper.createOLATResourceableInstance(LDAPLoginManager.class, 0l);

	public LdapContext bindSystem();

	public Attributes bindUser(String uid, String pwd, LDAPError errors);
	
	public Identity authenticate(String username, String pwd, LDAPError ldapError);

	public boolean changePassword(Authentication auth, String pwd, LDAPError errors);
	

	public Identity createAndPersistUser(String uid);
	
	public Identity createAndPersistUser(Attributes userAttributes);
	
	public List<Identity> getIdentitiesDeletedInLdap(LdapContext ctx);
	
	public Identity findIdentityByLdapAuthentication(Attributes attrs, LDAPError errors);
	
	public void syncUserGroups(Identity identity);
	
	public void syncUserOrganisations(Identity identity);
	
	public void deleteIdentities(List<Identity> identityList, Identity doer);
	
	public void inactivateIdentities(List<Identity> identityList, Identity doer);

	public boolean doBatchSync(LDAPError errors);
	
	public Date getLastSyncDate();
	
	public boolean acquireSyncLock();
	
	public void freeSyncLock();
	
	/**
	 * A filter is build from the login attribute value and the resulting
	 * attributes are sync to the specified identity.
	 * 
	 * @param ident The identity to synchronize
	 */
	public void doSyncSingleUserWithLoginAttribute(Identity ident);

	public void removeFallBackAuthentications();

	/**
	 * returns true, if the given identity is member of the LDAP-securitygroup
	 * 
	 * @param ident
	 * @return
	 */
	public boolean isIdentityInLDAPSecGroup(Identity ident);
}
