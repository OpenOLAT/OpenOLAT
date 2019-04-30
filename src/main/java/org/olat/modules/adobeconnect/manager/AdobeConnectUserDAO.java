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
package org.olat.modules.adobeconnect.manager;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.adobeconnect.AdobeConnectUser;
import org.olat.modules.adobeconnect.model.AdobeConnectUserImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 17 avr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AdobeConnectUserDAO {

	@Autowired
	private DB dbInstance;
	
	public AdobeConnectUser createUser(String principalId, String envName, Identity identity) {
		AdobeConnectUserImpl user = new AdobeConnectUserImpl();
		user.setCreationDate(new Date());
		user.setLastModified(user.getCreationDate());
		user.setPrincipalId(principalId);
		user.setEnvName(envName);
		user.setIdentity(identity);
		dbInstance.getCurrentEntityManager().persist(user);
		return user;
	}
	
	public AdobeConnectUser getUser(IdentityRef identity, String envName) {
		List<AdobeConnectUser> users = dbInstance.getCurrentEntityManager()
			.createNamedQuery("loadAdobeConnectUserByIdentity", AdobeConnectUser.class)
			.setParameter("identityKey", identity.getKey())
			.setParameter("envName", envName)
			.getResultList();
		return users == null || users.isEmpty() ? null : users.get(0);
	}
	
	public int deleteAdobeConnectUser(IdentityRef identity) {
		String q = "delete from adobeconnectuser as auser where auser.identity.key=:identityKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q)
				.setParameter("identityKey", identity.getKey())
				.executeUpdate();
	}

}
