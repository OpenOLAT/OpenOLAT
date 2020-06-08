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
package org.olat.basesecurity.manager;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 25 oct. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class IdentityDAO {
	
	@Autowired
	private DB dbInstance;
	
	public Identity findIdentityByName(String identityName) {
		if (identityName == null) throw new AssertException("findIdentitybyName: name was null");

		StringBuilder sb = new StringBuilder();
		sb.append("select ident from ").append(IdentityImpl.class.getName()).append(" as ident")
		  .append(" inner join fetch ident.user user")
		  .append(" where ident.name=:username");
		
		List<Identity> identities = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("username", identityName)
				.getResultList();
		
		if(identities.isEmpty()) {
			return null;
		}
		return identities.get(0);
	}
	
	public void setIdentityLastLogin(IdentityRef identity, Date lastLogin) {
		dbInstance.getCurrentEntityManager()
				.createNamedQuery("updateIdentityLastLogin")
				.setParameter("identityKey", identity.getKey())
				.setParameter("now", lastLogin)
				.executeUpdate();
	}
	
	public Identity saveIdentity(Identity identity) {
		return dbInstance.getCurrentEntityManager().merge(identity);
	}
	
	

}
