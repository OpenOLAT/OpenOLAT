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
package org.olat.user;

import java.util.List;

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 27.10.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class UserDAO {
	
	@Autowired
	private DB dbInstance;

	public Identity findUniqueIdentityByEmail(String email) {
		StringBuilder query = new StringBuilder(255)
				.append("select identity from ").append(IdentityImpl.class.getName()).append(" identity")
				.append(" inner join fetch identity.user user")
				.append(" where");
		boolean mysql = "mysql".equals(dbInstance.getDbVendor());
		if(mysql) {
			query.append(" user.").append(UserConstants.EMAIL).append("=:email or user.").append(UserConstants.INSTITUTIONALEMAIL).append("=:email");
		} else {
			query.append(" lower(user.").append(UserConstants.EMAIL).append(")=:email or lower(user.").append(UserConstants.INSTITUTIONALEMAIL).append(")=:email");
		}
		try {
			return dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), Identity.class)
				.setParameter("email", email.toLowerCase())
				.getSingleResult();
		} catch (Exception e) {
			return null;
		}
	}

	public boolean isEmailInUse(String email) {
		if (!StringHelper.containsNonWhitespace(email)) return false;
		
		StringBuilder query = new StringBuilder(255)
				.append("select count(*) from ").append(UserImpl.class.getName()).append(" user")
				.append(" where");
		
		boolean mysql = "mysql".equals(dbInstance.getDbVendor());
		if(mysql) {
			query.append(" user.").append(UserConstants.EMAIL).append("=:email or user.").append(UserConstants.INSTITUTIONALEMAIL).append("=:email");
		} else {
			query.append(" lower(user.").append(UserConstants.EMAIL).append(")=:email or lower(user.").append(UserConstants.INSTITUTIONALEMAIL).append(")=:email");
		}
		
		Long numberOfUsers = dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), Long.class)
				.setParameter("email", email.toLowerCase())
				.getSingleResult();
		
		return numberOfUsers > 0;
	}

	public List<Identity> findVisibleIdentitiesWithoutEmail() {
		String query = new StringBuilder()
				.append("select identity from ").append(IdentityImpl.class.getName()).append(" identity ")
				.append(" inner join fetch identity.user user ")
				.append(" where user.email is null")
				.append("   and identity.status<:status")
				.toString();
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, Identity.class)
				.setParameter("status", Identity.STATUS_VISIBLE_LIMIT)
				.getResultList();
	}

	public List<Identity> findVisibleIdentitiesWithEmailDuplicates() {
		String query = new StringBuilder()
				.append("select identity from ").append(IdentityImpl.class.getName()).append(" identity ")
				.append(" inner join fetch identity.user user ")
				.append(" where identity.status<:status")
				.append("   and user.email in (")
				.append("       select dupUser.email from ").append(IdentityImpl.class.getName()).append(" dupIdentity ") 
				.append("        inner join dupIdentity.user dupUser")
				.append("        where dupIdentity.status<:status")
				.append("          and dupUser.email is not null")
				.append("     group by dupUser.email")
				.append("       having count(*) > 1)")
				.toString();
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, Identity.class)
				.setParameter("status", Identity.STATUS_VISIBLE_LIMIT)
				.getResultList();
	}

}
