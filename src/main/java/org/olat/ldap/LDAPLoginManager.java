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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.ldap;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapContext;

import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.resource.OresHelper;

public abstract class LDAPLoginManager extends BasicManager {

	public static final OLATResourceable ldapSyncLockOres = OresHelper.createOLATResourceableInstance(LDAPLoginManager.class, 0l);

	public abstract LdapContext bindSystem();

	public abstract Attributes bindUser(String uid, String pwd, LDAPError errors);

	public abstract void changePassword(Identity identity, String pwd, LDAPError errors);
		
	public abstract List<Attributes> getUserAttributesModifiedSince(Date syncTime, LdapContext ctx);
	
	public abstract void createAndPersistUser(Attributes userAttributes);
	
	public abstract Map<String,String> prepareUserPropertyForSync(Attributes attributes, Identity identity);
	
	public abstract List<Identity> getIdentitysDeletedInLdap(LdapContext ctx);
	
	public abstract Identity findIdentyByLdapAuthentication(String uid, LDAPError errors);
	
	public abstract void syncUser(Map<String,String> olatPropertyMap, Identity identity);
	
	public abstract void deletIdentities(List<Identity> identityList);

	public abstract boolean doBatchSync(LDAPError errors);
	
	public abstract Date getLastSyncDate();
	
	public abstract boolean acquireSyncLock();
	
	public abstract void freeSyncLock();
	
	public abstract void doSyncSingleUser(Identity ident);

	public abstract void removeFallBackAuthentications();

	/**
	 * returns true, if the given identity is member of the LDAP-securitygroup
	 * 
	 * @param ident
	 * @return
	 */
	public abstract boolean isIdentityInLDAPSecGroup(Identity ident);
}
