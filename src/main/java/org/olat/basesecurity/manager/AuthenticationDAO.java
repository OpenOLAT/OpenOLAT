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

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.AuthenticationImpl;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.webdav.manager.WebDAVAuthManager;
import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Encoder;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webauthn4j.data.attestation.authenticator.AAGUID;

/**
 * 
 * Initial date: 3 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AuthenticationDAO {

	private static final Logger log = Tracing.createLoggerFor(AuthenticationDAO.class);
	
	@Autowired
	private DB dbInstance;
	
	
	public Authentication createAndPersistAuthentication(final Identity ident, final String provider, final String issuer,
			final String externalId, final String authUserName, final String credentials) {
		AuthenticationImpl auth = new AuthenticationImpl();
		auth.setCreationDate(new Date());
		auth.setLastModified(auth.getCreationDate());
		auth.setIdentity(ident);
		auth.setProvider(provider);
		auth.setIssuer(issuer);
		auth.setExternalId(externalId);
		auth.setAuthusername(authUserName);
		auth.setCredential(credentials);
		dbInstance.getCurrentEntityManager().persist(auth);
		return auth;
	}
	
	public Authentication createAndPersistAuthenticationHash(final Identity ident, final String provider, final String issuer,
			final String externalId, final String authUserName, final String credentials, final Encoder.Algorithm algorithm) {
		AuthenticationImpl auth = new AuthenticationImpl();
		auth.setCreationDate(new Date());
		auth.setLastModified(auth.getCreationDate());
		auth.setIdentity(ident);
		auth.setProvider(provider);
		auth.setIssuer(issuer);
		auth.setExternalId(externalId);
		auth.setAuthusername(authUserName);

		String salt = algorithm.isSalted() ? Encoder.getSalt() : null;
		String hash = Encoder.encrypt(credentials, salt, algorithm);
		auth.setCredential(hash);
		auth.setSalt(salt);
		auth.setAlgorithm(algorithm.name());
		dbInstance.getCurrentEntityManager().persist(auth);
		return auth;
	}
	
	public Authentication createAndPersistAuthenticationWebAuthn(Identity ident, String provider,
			String authUserName, byte[] userHandle, byte[] credentialId, byte[] aaGuid, byte[] coseKey,
			String attestationObject, String clientExtensions, String authenticatorExtensions, String transports) {
		AuthenticationImpl auth = new AuthenticationImpl();
		auth.setCreationDate(new Date());
		auth.setLastModified(auth.getCreationDate());
		auth.setIdentity(ident);
		auth.setProvider(provider);
		auth.setIssuer(new AAGUID(aaGuid).getValue().toString());// An issuer per device
		auth.setAuthusername(authUserName);
		
		auth.setUserHandle(userHandle);
		auth.setCredentialId(credentialId);
		auth.setAaGuid(aaGuid);
		auth.setCoseKey(coseKey);
		auth.setCounter(0l);
		auth.setAttestationObject(attestationObject);
		auth.setClientExtensions(clientExtensions);
		auth.setAuthenticatorExtensions(authenticatorExtensions);
		auth.setTransports(transports);

		dbInstance.getCurrentEntityManager().persist(auth);
		return auth;
	}
	
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
	
	public List<Authentication> getAuthenticationsByAuthusername(String authusername, String provider) {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select auth from ").append(AuthenticationImpl.class.getName()).append(" as auth")
		  .append(" inner join fetch auth.identity ident")
		  .append(" inner join fetch ident.user identUser")
		  .append(" where auth.provider=:provider and ").lowerEqual("auth.authusername").append(":authusername");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Authentication.class)
				.setParameter("provider", provider)
				.setParameter("authusername", authusername.toLowerCase())
				.getResultList();
	}
	
	public List<String> getAuthenticationsProvidersByAuthusername(String authusername) {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select distinct auth.provider from ").append(AuthenticationImpl.class.getName()).append(" as auth")
		  .append(" inner join auth.identity ident")
		  .append(" inner join ident.user identUser")
		  .append(" where ").lowerEqual("auth.authusername").append(":authusername")
		  .append(" or ").lowerEqual("ident.name").append(":authusername");
		if(authusername.contains("@")) {
			sb.append(" or ").lowerEqual("identUser.institutionalEmail").append(":authusername")
			  .append(" or ").lowerEqual("identUser.email").append(":authusername");
		}

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), String.class)
				.setParameter("authusername", authusername.toLowerCase())
				.getResultList();
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
	 * @param authusername The user name (mandatory)
	 * @param externalIds The external identifiers (optional)
	 * @param provider The provider (mandatory)
	 * @param issuer The issuer (mandatory)
	 * @return A list of authentication which match username or external identifiers.
	 */
	public List<Authentication> getAuthentications(String authusername, List<String> externalIds, String provider, String issuer) {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select auth from ").append(AuthenticationImpl.class.getName()).append(" as auth")
		  .append(" inner join fetch auth.identity ident")
		  .append(" inner join fetch ident.user identUser")
		  .where().append("auth.provider=:provider and auth.issuer=:issuer and (").lowerEqual("auth.authusername").append(":authusername");
		if(externalIds != null && !externalIds.isEmpty()) {
			sb.append(" or ").lowerIn("auth.externalId").append(" (:externalIds)");
		}
		sb.append(")");
		
		TypedQuery<Authentication> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Authentication.class)
				.setParameter("provider", provider)
				.setParameter("issuer", issuer)
				.setParameter("authusername", authusername.toLowerCase());
		if(externalIds != null && !externalIds.isEmpty()) {
			query.setParameter("externalIds", externalIds);
		}
		return query.getResultList();
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
	
	/**
	 * 
	 * @param provider The authentication provider
	 * @return A list of identities (the user is not fetched)
	 */
	public List<Identity> getIdentitiesWithAuthenticationWithoutOrgnisation(String provider) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select ident from ").append(AuthenticationImpl.class.getName()).append(" as auth")
		  .append(" inner join auth.identity as ident")
		  .append(" where auth.provider=:provider")
		  .append(" and not exists (select orgtomember.key from bgroupmember as orgtomember ")
		  .append("  inner join organisation as org on (org.group.key=orgtomember.group.key)")
		  .append("  where orgtomember.identity.key=ident.key and orgtomember.role ").in(OrganisationRoles.user)
		  .append(" )");
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
	
	public Authentication loadByCredentialId(byte[] credentialId) {
		if (credentialId == null) return null;
		
		StringBuilder sb = new StringBuilder();
		sb.append("select auth from ").append(AuthenticationImpl.class.getName()).append(" as auth")
		  .append(" inner join auth.identity as ident")
		  .append(" inner join ident.user as user")
		  .append(" where auth.credentialId=:credentialId");
		
		List<Authentication> results = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Authentication.class)
				.setParameter("credentialId", credentialId)
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
	
	public Authentication getAuthenticationByExternalId(String externalId, String provider, String issuer) {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select auth from ").append(AuthenticationImpl.class.getName()).append(" as auth")
		  .append(" inner join fetch auth.identity as ident")
		  .append(" inner join fetch ident.user as user")
		  .append(" where ").lowerEqual("auth.externalId").append(":externalId and auth.provider=:provider and auth.issuer=:issuer");
		List<Authentication> authentications = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Authentication.class)
				.setFlushMode(FlushModeType.COMMIT)
				.setParameter("externalId", externalId.toLowerCase())
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
		String query = """
				select auth from authentication as auth
				 where auth.identity.key=:identityKey""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, Authentication.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}
	
	public List<Authentication> getAuthenticationsNoFetch(IdentityRef identity, String provider) {
		String query = """
				select auth from authentication as auth
				 where auth.identity.key=:identityKey and auth.provider=:provider""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, Authentication.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("provider", provider)
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
			query.setParameter("maxDate", cal.getTime(), TemporalType.TIMESTAMP);
		}
		
		List<Long> keys = query
			.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return keys != null && !keys.isEmpty() && keys.get(0) != null;
	}
	
	public long countIdentityWithOnlyPasskey() {
		String sb = """
				select count(distinct auth.key) from authentication as auth
				where auth.provider='PASSKEY' and not exists (select oAuth from authentication as oAuth
				 where oAuth.provider='OLAT' and oAuth.identity.key=auth.identity.key
				)""";

		List<Number> counters = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.getResultList();
		return counters == null || counters.isEmpty() ? 0l : counters.get(0).longValue();
	}
	
	public void deleteAuthentication(Authentication auth) {
		if(auth == null || auth.getKey() == null) return;//nothing to do
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("select auth from ").append(AuthenticationImpl.class.getName()).append(" as auth")
			  .append(" where auth.key=:authKey");

			AuthenticationImpl authRef = dbInstance.getCurrentEntityManager().find(AuthenticationImpl.class,  auth.getKey());
			if(authRef != null) {
				dbInstance.getCurrentEntityManager().remove(authRef);
			}
		} catch (EntityNotFoundException e) {
			log.error("", e);
		}
	}
	
	public void deleteWebDAVAuthenticationsByEmail(String email) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("delete from ").append(AuthenticationImpl.class.getName()).append(" as auth");
		sb.append(" where auth.authusername=:authusername");
		sb.append("   and auth.provider in (:providers)");
		
		List<String> providers = Arrays.asList(
				WebDAVAuthManager.PROVIDER_HA1_EMAIL,
				WebDAVAuthManager.PROVIDER_HA1_INSTITUTIONAL_EMAIL,
				WebDAVAuthManager.PROVIDER_WEBDAV_EMAIL,
				WebDAVAuthManager.PROVIDER_WEBDAV_INSTITUTIONAL_EMAIL);
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("authusername", email)
				.setParameter("providers", providers)
				.executeUpdate();
	}

}
