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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jakarta.persistence.FlushModeType;
import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.AuthenticationImpl;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 3 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AuthenticationDAO {
	
	@Autowired
	private DB dbInstance;
	
	public Authentication getAuthenticationByAuthusername(String authusername, String provider, String issuer) {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select auth from ").append(AuthenticationImpl.class.getName()).append(" as auth")
		  .append(" inner join fetch auth.identity ident")
		  .append(" inner join fetch ident.user identUser")
		  .append(" where auth.provider=:provider and auth.issuer=:issuer and ").lowerEqual("auth.authusername").append(":authusername");

		List<Authentication> results = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Authentication.class)
				.setParameter("provider", provider)
				.setParameter("issuer", issuer)
				.setParameter("authusername", authusername.toLowerCase())
				.getResultList();
		if (results.isEmpty()) return null;
		if (results.size() != 1) {
			throw new AssertException("more than one entry for the a given authusername and provider, should never happen (even db has a unique constraint on those columns combined) ");
		}
		return results.get(0);
	}
	
	public List<Authentication> getAuthenticationsByAuthusername(String authusername) {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select auth from ").append(AuthenticationImpl.class.getName()).append(" as auth")
		  .append(" inner join fetch auth.identity ident")
		  .append(" inner join fetch ident.user identUser")
		  .append(" where ").lowerEqual("auth.authusername").append(":authusername");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Authentication.class)
				.setParameter("authusername", authusername.toLowerCase())
				.getResultList();
	}
	
	public List<Authentication> getAuthenticationsByAuthusername(String authusername, List<String> providers) {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select auth from ").append(AuthenticationImpl.class.getName()).append(" as auth")
		  .append(" inner join fetch auth.identity ident")
		  .append(" inner join fetch ident.user identUser")
		  .append(" where ").lowerEqual("auth.authusername").append(":authusername and auth.provider in (:providers)");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Authentication.class)
				.setParameter("authusername", authusername.toLowerCase())
				.setParameter("providers", providers)
				.getResultList();
	}
	
	/**
	 * 
	 * @param provider The authentication provider
	 * @return A list of identities (the user is not fetched)
	 */
	public List<Identity> getIdentitiesWithLogin(String login) {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select ident from ").append(AuthenticationImpl.class.getName()).append(" as auth")
		  .append(" inner join auth.identity as ident")
		  .append(" inner join ident.user as user")
		  .append(" where ").lowerEqual("auth.authusername").append(":login");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("login", login.toLowerCase())
				.getResultList();
	}
	
	/**
	 * 
	 * @param provider The authentication provider
	 * @return A list of identities (the user is not fetched)
	 */
	public List<Identity> getIdentitiesWithAuthentication(String provider) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select ident from ").append(AuthenticationImpl.class.getName()).append(" as auth")
		  .append(" inner join auth.identity as ident")
		  .append(" where auth.provider=:provider");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("provider", provider)
				.getResultList();
	}
	
	public long countIdentitiesWithAuthentication(String provider) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select count(auth.identity.key) from ").append(AuthenticationImpl.class.getName()).append(" as auth")
		  .append(" where auth.provider=:provider");
		List<Long> count = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("provider", provider)
				.getResultList();
		return count == null || count.isEmpty() || count.get(0) == null ? 0l : count.get(0).longValue();
	}
	
	/**
	 * 
	 * @param provider The authentication provider
	 * @return A list of identities (the user is not fetched)
	 */
	public List<Authentication> getAuthentications(String provider) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select auth from ").append(AuthenticationImpl.class.getName()).append(" as auth")
		  .append(" inner join fetch auth.identity as ident")
		  .append(" inner join fetch ident.user as identUser")
		  .append(" where auth.provider=:provider");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Authentication.class)
				.setParameter("provider", provider)
				.getResultList();
	}
	
	public Authentication loadByKey(Long authenticationKey) {
		if (authenticationKey == null) return null;
		
		StringBuilder sb = new StringBuilder();
		sb.append("select auth from ").append(AuthenticationImpl.class.getName()).append(" as auth")
		  .append(" inner join auth.identity as ident")
		  .append(" inner join ident.user as user")
		  .append(" where auth.key=:authenticationKey");
		
		List<Authentication> results = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Authentication.class)
				.setParameter("authenticationKey", authenticationKey)
				.getResultList();
		return results != null && !results.isEmpty() ? results.get(0) : null;
	}

	public Authentication getAuthentication(IdentityRef identity, String provider, String issuer) {
		if (identity == null || !StringHelper.containsNonWhitespace(provider)) {
			throw new IllegalArgumentException("identity must not be null");
		}
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select auth from ").append(AuthenticationImpl.class.getName()).append(" as auth")
		  .append(" inner join auth.identity as ident")
		  .append(" inner join ident.user as user")
		  .append(" where auth.identity.key=:identityKey and auth.provider=:provider and auth.issuer=:issuer");
		
		List<Authentication> results = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Authentication.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("provider", provider)
				.setParameter("issuer", issuer)
				.getResultList();
		if (results == null || results.isEmpty()) {
			return null;
		}
		if (results.size() > 1) {
			throw new AssertException("Found more than one Authentication for a given subject and a given provider.");
		}
		return results.get(0);
	}
	
	public String getAuthenticationName(IdentityRef identity, String provider, String issuer) {
		if (identity==null) {
			throw new IllegalArgumentException("identity must not be null");
		}
		
		StringBuilder sb = new StringBuilder(128);
		sb.append("select auth.authusername from ").append(AuthenticationImpl.class.getName())
		  .append(" as auth where auth.identity.key=:identityKey and auth.provider=:provider and auth.issuer=:issuer");
		
		List<String> results = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), String.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("provider", provider)
				.setParameter("issuer", issuer)
				.getResultList();
		if (results == null || results.isEmpty()) return null;
		if (results.size() > 1) {
			throw new AssertException("Found more than one Authentication for a given subject and a given provider.");
		}
		return results.get(0);
	}
	
	/**
	 * The flush mode is set to COMMIT for performance reason. Especially for LDAP synchronization
	 * on very large groups. Identity and user are fetched with.
	 * 
	 * @param authUsername The authentication user name
	 * @param provider The provider
	 * @return An authentication
	 */
	public Authentication getAuthentication(String authUsername, String provider, String issuer) {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select auth from ").append(AuthenticationImpl.class.getName()).append(" as auth")
		  .append(" inner join fetch auth.identity as ident")
		  .append(" inner join fetch ident.user as user")
		  .append(" where ").lowerEqual("auth.authusername").append(":authUsername and auth.provider=:provider and auth.issuer=:issuer");
		List<Authentication> authentications = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Authentication.class)
				.setFlushMode(FlushModeType.COMMIT)
				.setParameter("authUsername", authUsername.toLowerCase())
				.setParameter("provider", provider)
				.setParameter("issuer", issuer)
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return authentications != null && !authentications.isEmpty() ? authentications.get(0) : null;
	}
	
	public boolean hasAuthentication(IdentityRef identity, String provider) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select auth.key from ").append(AuthenticationImpl.class.getName()).append(" as auth")
		  .append(" where auth.identity.key=:identityKey and auth.provider=:provider");
		List<Long> authentications = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("provider", provider)
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return authentications != null && !authentications.isEmpty();
	}
	
	/**
	 * The query fetch the identity.
	 * 
	 * @param identity The identity to search authentication for
	 * @return A list of authentications
	 */
	public List<Authentication> getAuthentications(IdentityRef identity) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select auth from ").append(AuthenticationImpl.class.getName()).append(" as auth")
		  .append(" inner join auth.identity as ident")
		  .append(" where ident.key=:identityKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Authentication.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}
	
	
	/**
	 * The query doesn't fetch the identity.
	 * 
	 * @param identity The identity to search authentication for
	 * @return A list of authentications
	 */
	public List<Authentication> getAuthenticationsNoFetch(IdentityRef identity) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select auth from ").append(AuthenticationImpl.class.getName()).append(" as auth")
		  .append(" where auth.identity.key=:identityKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Authentication.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}
	
	public Authentication updateAuthentication(Authentication authentication) {
		return dbInstance.getCurrentEntityManager().merge(authentication);
	}
	
	/**
	 * Quick update of the credential, don't do a full update of the authentication object.
	 * 
	 * @param auth
	 * @param token
	 */
	public void updateCredential(Authentication auth, String token) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("update ").append(AuthenticationImpl.class.getName()).append(" set credential=:token,lastModified=:now where key=:authKey");
		dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString())
			.setParameter("authKey", auth.getKey())
			.setParameter("token", token)
			.setParameter("now", new Date())
			.executeUpdate();
		dbInstance.commit();
	}
	
	/**
	 * The query return as valid OLAT authentication a fallback for LDAP.
	 * 
	 * @param identity The identity to check
	 * @param changeOnce If the identity need to change its password at least once
	 * @param maxAge The max. age of the authentication in seconds
	 * @return
	 */
	public boolean hasValidOlatAuthentication(IdentityRef identity, boolean changeOnce, int maxAge,
			List<String> exceptionProviders) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select auth.key from ").append(AuthenticationImpl.class.getName()).append(" as auth")
		  .append(" where auth.identity.key=:identityKey and ((auth.provider=:olatProvider");
		
		if(changeOnce) {
			sb.append(" and not(auth.creationDate=auth.lastModified)");
		}
		if(maxAge > 0) {
			sb.append(" and auth.lastModified>=:maxDate");
		}
		sb.append(") or auth.provider in (:providers))");
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Long.class)
			.setParameter("identityKey", identity.getKey())
			.setParameter("olatProvider", BaseSecurityModule.getDefaultAuthProviderIdentifier())
			.setParameter("providers", exceptionProviders);
		
		if(maxAge > 0) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.SECOND, -maxAge);
			query.setParameter("maxDate", cal.getTime());
		}
		
		List<Long> keys = query
			.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return keys != null && !keys.isEmpty() && keys.get(0) != null;
	}

}
