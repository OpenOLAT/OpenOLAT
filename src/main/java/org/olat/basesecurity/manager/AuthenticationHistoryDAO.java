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

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.AuthenticationHistory;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.model.AuthenticationHistoryImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 18 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("authenticationHistoryDao")
public class AuthenticationHistoryDAO {
	
	@Autowired
	private DB dbInstance;
	
	public void createHistory(Authentication authentication, Identity identity) {
		AuthenticationHistoryImpl hPoint = new AuthenticationHistoryImpl();
		hPoint.setCreationDate(new Date());
		hPoint.setAuthusername(authentication.getAuthusername());
		hPoint.setProvider(authentication.getProvider());
		hPoint.setCredential(authentication.getCredential());
		hPoint.setSalt(authentication.getSalt());
		hPoint.setAlgorithm(authentication.getAlgorithm());
		hPoint.setIdentity(identity);
		dbInstance.getCurrentEntityManager().persist(hPoint);
	}
	
	public List<AuthenticationHistory> loadHistory(IdentityRef identity, String provider, int firstResult, int maxResults) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select hAuth from authenticationhistory as hAuth")
		  .append(" where hAuth.identity.key=:identityKey and hAuth.provider=:provider")
		  .append(" order by hAuth.creationDate desc");
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), AuthenticationHistory.class)
			.setParameter("identityKey", identity.getKey())
			.setParameter("provider", provider)
			.setFirstResult(firstResult)
			.setMaxResults(maxResults)
			.getResultList();
	}
	
	public int historyLength(IdentityRef identity, String provider) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select count(hAuth.key) from authenticationhistory as hAuth")
		  .append(" where hAuth.identity.key=:identityKey and hAuth.provider=:provider");
		List<Long> count = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Long.class)
			.setParameter("identityKey", identity.getKey())
			.setParameter("provider", provider)
			.getResultList();
		return count == null || count.isEmpty() || count.get(0) == null ? 0 : count.get(0).intValue();
	}
	
	public int deleteAuthenticationHistory(IdentityRef identity) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("delete from authenticationhistory where identity.key=:identityKey");
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString())
			.setParameter("identityKey", identity.getKey())
			.executeUpdate();
	}
	
	public void deleteAuthenticationHistory(AuthenticationHistory history) {
		dbInstance.getCurrentEntityManager().remove(history);
	}
}
