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
package org.olat.modules.teams.manager;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.teams.TeamsUser;
import org.olat.modules.teams.model.TeamsUserImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 27 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class TeamsUserDAO {

	@Autowired
	private DB dbInstance;
	
	public TeamsUser createUser(Identity identity, String identifier, String displayName) {
		TeamsUserImpl user = new TeamsUserImpl();
		user.setCreationDate(new Date());
		user.setLastModified(user.getCreationDate());
		user.setIdentifier(identifier);
		user.setDisplayName(displayName);
		user.setIdentity(identity);
		dbInstance.getCurrentEntityManager().persist(user);
		return user;
	}
	
	/**
	 * The query doesn't fetch identity and user. It returns only the Teams user.
	 * 
	 * @param identity The identity
	 * @return A Teams user
	 */
	public TeamsUser getUser(IdentityRef identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select tUser from teamsuser as tUser")
		  .append(" where tUser.identity.key=:identityKey");
		
		List<TeamsUser> users = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), TeamsUser.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		return users == null || users.isEmpty() ? null : users.get(0);
	}
}
