/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.mcp.manager;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.AuthenticationImpl;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.manager.AuthenticationDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Encoder;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.mcp.McpModule;
import org.olat.mcp.McpToken;
import org.olat.mcp.McpTokenCreation;
import org.olat.mcp.McpTokenService;
import org.olat.mcp.McpTokenValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Stores the MCP tokens as rows of o_bs_authentication with the provider
 * {@link McpTokenService#MCP_AUTH_PROVIDER}. The row holds the SHA-256 hex
 * of the plaintext, never the plaintext. The lookup by hash uses the index
 * on the credential column.
 * <p>
 * The rows are created through the {@link AuthenticationDAO} and not through
 * {@link BaseSecurity#createAndPersistAuthentication}, because the latter
 * returns the existing row of the identity and provider and allows one
 * token per user only. The generation runs in a cluster lock on the
 * identity, the same lock as BaseSecurity uses, so the maximum number of
 * tokens holds with parallel requests.
 * <p>
 * The column hashalgorithm holds "sha256": a plain, unsalted SHA-256 of the
 * token. It is not the salted, iterated {@link Encoder.Algorithm#sha256}, so
 * {@link BaseSecurity#checkCredentials} always fails for these rows. This is
 * intended: only this service validates MCP tokens.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Service
public class McpTokenServiceImpl implements McpTokenService {

	private static final Logger log = Tracing.createLoggerFor(McpTokenServiceImpl.class);

	/** Value of the column hashalgorithm: plain SHA-256 hex, no salt, one iteration */
	static final String HASH_ALGORITHM = "sha256";
	/** Random bytes of the plaintext token, 192 bit */
	static final int TOKEN_BYTES = 24;
	/** Fixed length of the base62 part of the plaintext, 62^33 > 2^192 */
	static final int TOKEN_BODY_LENGTH = 33;
	/** Random bytes of the token id, 80 bit */
	static final int TOKEN_ID_BYTES = 10;
	/** Fixed length of the base62 part of the token id, 62^14 > 2^80 */
	static final int TOKEN_ID_BODY_LENGTH = 14;
	/** Upper bound of a page of the administration list */
	static final int MAX_PAGE_SIZE = 5000;

	private static final int TOKEN_ID_ATTEMPTS = 5;
	private static final char[] BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
	private static final BigInteger SIXTY_TWO = BigInteger.valueOf(62);
	private static final SecureRandom random = new SecureRandom();

	@Autowired
	private DB dbInstance;
	@Autowired
	private McpModule mcpModule;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private AuthenticationDAO authenticationDao;
	@Autowired
	private McpIdentityCache identityCache;
	@Autowired
	private CoordinatorManager coordinatorManager;

	@Override
	public int countTokens(IdentityRef identity) {
		if(identity == null || identity.getKey() == null) return 0;

		String query = """
				select count(auth.key) from authentication as auth
				 where auth.identity.key=:identityKey and auth.provider=:provider""";
		List<Long> counts = dbInstance.getCurrentEntityManager()
				.createQuery(query, Long.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("provider", MCP_AUTH_PROVIDER)
				.getResultList();
		return counts == null || counts.isEmpty() || counts.get(0) == null ? 0 : counts.get(0).intValue();
	}

	@Override
	public McpTokenCreation generateToken(Identity identity, String label) {
		if(identity == null || identity.getKey() == null) {
			throw new IllegalArgumentException("An MCP token needs a persisted identity");
		}
		final String trimmedLabel = validateLabel(label);

		OLATResourceable lockRes = OresHelper.createOLATResourceableInstanceWithoutCheck(MCP_AUTH_PROVIDER, identity.getKey());
		Generation generation = coordinatorManager.getCoordinator().getSyncer()
				.doInSync(lockRes, () -> generateTokenInSync(identity, trimmedLabel));
		// The commit releases the cluster lock
		dbInstance.commit();

		if(generation.error() != null) {
			throw generation.error();
		}
		log.info(Tracing.M_AUDIT, "Create MCP token {} for identity {}", generation.creation().token().tokenId(), identity.getKey());
		return generation.creation();
	}

	private Generation generateTokenInSync(Identity identity, String label) {
		List<Authentication> currentTokens = authenticationDao.getAuthenticationsNoFetch(identity, MCP_AUTH_PROVIDER);
		int max = mcpModule.getUserTokenMax();
		if(max > 0 && currentTokens.size() >= max) {
			return new Generation(null, new IllegalStateException("The maximum of " + max + " MCP tokens is reached"));
		}
		for(Authentication currentToken:currentTokens) {
			if(label.equalsIgnoreCase(toLabel(currentToken.getExternalId()))) {
				return new Generation(null, new IllegalArgumentException("The identity has already an MCP token with this label"));
			}
		}

		String tokenId = generateUniqueTokenId();
		String plaintext = TOKEN_PREFIX + randomBase62(TOKEN_BYTES, TOKEN_BODY_LENGTH);
		String externalId = identity.getKey() + ":" + label;

		AuthenticationImpl auth = new AuthenticationImpl(identity, MCP_AUTH_PROVIDER, BaseSecurity.DEFAULT_ISSUER,
				externalId, tokenId, hash(plaintext), null, HASH_ALGORITHM);
		auth.setLastModified(new Date());
		Authentication persisted = authenticationDao.persist(identity, auth);
		return new Generation(new McpTokenCreation(plaintext, toToken(persisted, identity)), null);
	}

	private String generateUniqueTokenId() {
		for(int i=0; i<TOKEN_ID_ATTEMPTS; i++) {
			String tokenId = TOKEN_ID_PREFIX + randomBase62(TOKEN_ID_BYTES, TOKEN_ID_BODY_LENGTH);
			if(!tokenIdExists(tokenId)) {
				return tokenId;
			}
		}
		throw new IllegalStateException("Cannot generate a unique MCP token id");
	}

	/**
	 * The unique constraint on (provider, issuer, authusername) is case
	 * insensitive on MySQL, the check is case insensitive everywhere.
	 */
	private boolean tokenIdExists(String tokenId) {
		return !authenticationDao.getAuthenticationsByAuthusername(tokenId, MCP_AUTH_PROVIDER).isEmpty();
	}

	/**
	 * @param label The raw label
	 * @return The trimmed label
	 * @throws IllegalArgumentException if the label is empty or too long
	 */
	static String validateLabel(String label) {
		if(!StringHelper.containsNonWhitespace(label)) {
			throw new IllegalArgumentException("The label of an MCP token is mandatory");
		}
		String trimmed = label.trim();
		if(trimmed.length() > LABEL_MAX_LENGTH) {
			throw new IllegalArgumentException("The label of an MCP token has more than " + LABEL_MAX_LENGTH + " characters");
		}
		return trimmed;
	}

	@Override
	public McpTokenValidation validateToken(String plaintext) {
		if(!isWellFormed(plaintext)) {
			return McpTokenValidation.unknown(null);
		}

		String tokenHash = hash(plaintext);
		List<Authentication> auths = securityManager.findAuthenticationByToken(MCP_AUTH_PROVIDER, tokenHash);
		if(auths.isEmpty()) {
			return McpTokenValidation.unknown(tokenHash);
		}
		if(auths.size() > 1) {
			// Never seen with 192 bit tokens, refuse rather than guess the owner
			log.warn("{} MCP tokens share the same hash, the token is refused", Integer.valueOf(auths.size()));
			return McpTokenValidation.unknown(tokenHash);
		}

		Authentication auth = auths.get(0);
		if(!HASH_ALGORITHM.equals(auth.getAlgorithm())
				|| auth.getAuthusername() == null || !auth.getAuthusername().startsWith(TOKEN_ID_PREFIX)) {
			// A row of provider MCP not written by this service
			log.warn("MCP authentication {} has not the format of an MCP token, the token is refused", auth.getKey());
			return McpTokenValidation.unknown(tokenHash);
		}
		// The identity is a lazy association of the authentication. The tools
		// get it detached in later requests; as a detached Hibernate proxy it
		// breaks queries which fetch it again (for example the creator of a new
		// project), so the session keeps the entity itself.
		Identity identity = Hibernate.unproxy(auth.getIdentity(), Identity.class);
		McpToken token = toToken(auth, identity);
		if(token.isExpired(new Date())) {
			return McpTokenValidation.expired(token, tokenHash);
		}
		// The status rule of the UI login: login denied, pending, inactive and deleted are refused
		if(!securityManager.isIdentityLoginAllowed(identity, MCP_AUTH_PROVIDER)) {
			return McpTokenValidation.identityInactive(token, tokenHash);
		}
		return McpTokenValidation.valid(token, tokenHash);
	}

	/**
	 * Checks the format without any database access: the prefix, the fixed
	 * length and the base62 alphabet.
	 *
	 * @param plaintext The bearer value
	 * @return true if the value has the format of an MCP token
	 */
	static boolean isWellFormed(String plaintext) {
		if(plaintext == null
				|| plaintext.length() != TOKEN_PREFIX.length() + TOKEN_BODY_LENGTH
				|| !plaintext.startsWith(TOKEN_PREFIX)) {
			return false;
		}
		for(int i=TOKEN_PREFIX.length(); i<plaintext.length(); i++) {
			char c = plaintext.charAt(i);
			if(!((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z'))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public List<McpToken> getTokens(IdentityRef identity) {
		if(identity == null || identity.getKey() == null) return new ArrayList<>(0);

		String query = """
				select auth from authentication as auth
				 inner join fetch auth.identity as ident
				 inner join fetch ident.user as identUser
				 where ident.key=:identityKey and auth.provider=:provider
				 order by auth.creationDate asc, auth.key asc""";
		List<Authentication> auths = dbInstance.getCurrentEntityManager()
				.createQuery(query, Authentication.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("provider", MCP_AUTH_PROVIDER)
				.getResultList();
		return toTokens(auths);
	}

	@Override
	public McpToken getToken(String tokenId) {
		Authentication auth = loadByTokenId(tokenId);
		return auth == null ? null : toToken(auth, auth.getIdentity());
	}

	private Authentication loadByTokenId(String tokenId) {
		if(!StringHelper.containsNonWhitespace(tokenId)) return null;

		String query = """
				select auth from authentication as auth
				 inner join fetch auth.identity as ident
				 inner join fetch ident.user as identUser
				 where auth.authusername=:tokenId and auth.provider=:provider""";
		List<Authentication> auths = dbInstance.getCurrentEntityManager()
				.createQuery(query, Authentication.class)
				.setParameter("tokenId", tokenId)
				.setParameter("provider", MCP_AUTH_PROVIDER)
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return auths.isEmpty() ? null : auths.get(0);
	}

	@Override
	public List<McpToken> searchTokens(String searchString, int firstResult, int maxResults) {
		QueryBuilder sb = new QueryBuilder(512);
		sb.append("select auth from authentication as auth")
		  .append(" inner join fetch auth.identity as ident")
		  .append(" inner join fetch ident.user as identUser")
		  .append(" where auth.provider=:provider");
		appendSearch(sb, searchString);
		sb.append(" order by auth.creationDate desc, auth.key desc");

		TypedQuery<Authentication> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Authentication.class)
				.setParameter("provider", MCP_AUTH_PROVIDER)
				.setFirstResult(Math.max(0, firstResult))
				.setMaxResults(maxResults > 0 && maxResults < MAX_PAGE_SIZE ? maxResults : MAX_PAGE_SIZE);
		fillSearch(query, searchString);
		return toTokens(query.getResultList());
	}

	@Override
	public int countSearchTokens(String searchString) {
		QueryBuilder sb = new QueryBuilder(512);
		sb.append("select count(auth.key) from authentication as auth")
		  .append(" inner join auth.identity as ident")
		  .append(" inner join ident.user as identUser")
		  .append(" where auth.provider=:provider");
		appendSearch(sb, searchString);

		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("provider", MCP_AUTH_PROVIDER);
		fillSearch(query, searchString);
		List<Long> counts = query.getResultList();
		return counts == null || counts.isEmpty() || counts.get(0) == null ? 0 : counts.get(0).intValue();
	}

	private void appendSearch(QueryBuilder sb, String searchString) {
		if(!StringHelper.containsNonWhitespace(searchString)) return;

		sb.append(" and (")
		  .appendFuzzyLike("auth.externalId", "searchString")
		  .append(" or ").appendFuzzyLike("auth.authusername", "searchString")
		  .append(" or ").appendFuzzyLike("ident.name", "searchString")
		  .append(" or ").appendFuzzyLike("identUser.nickName", "searchString")
		  .append(" or ").appendFuzzyLike("identUser.email", "searchString")
		  .append(")");
	}

	private void fillSearch(TypedQuery<?> query, String searchString) {
		if(!StringHelper.containsNonWhitespace(searchString)) return;

		String fuzzy = PersistenceHelper.makeFuzzyQueryString(searchString.trim());
		query.setParameter("searchString", fuzzy.toLowerCase());
	}

	@Override
	public boolean revokeToken(String tokenId, IdentityRef owner) {
		Authentication auth = loadByTokenId(tokenId);
		if(auth == null) {
			return false;
		}

		Long ownerKey = auth.getIdentity().getKey();
		if(owner != null && !ownerKey.equals(owner.getKey())) {
			log.warn(Tracing.M_AUDIT, "Identity {} cannot revoke the MCP token {} of identity {}", owner.getKey(), tokenId, ownerKey);
			return false;
		}

		String tokenHash = auth.getCredential();
		int deleted = dbInstance.getCurrentEntityManager()
				.createQuery("delete from authentication as auth where auth.key=:authKey and auth.provider=:provider")
				.setParameter("authKey", auth.getKey())
				.setParameter("provider", MCP_AUTH_PROVIDER)
				.executeUpdate();
		dbInstance.getCurrentEntityManager().detach(auth);
		// Commit before the cache drops the entry, a parallel request must not cache the row again
		dbInstance.commit();

		identityCache.remove(tokenHash);
		if(deleted > 0) {
			log.info(Tracing.M_AUDIT, "Revoke MCP token {} of identity {} by {}", tokenId, ownerKey,
					owner == null ? "administrator" : "owner");
		}
		return deleted > 0;
	}

	@Override
	public int deleteExpiredTokens() {
		int days = mcpModule.getTokenValidityDays();
		if(days <= 0) {
			return 0;
		}

		Date limit = DateUtils.addDays(new Date(), -days);
		return dbInstance.getCurrentEntityManager()
				.createQuery("delete from authentication as auth where auth.provider=:provider and auth.creationDate<:limit")
				.setParameter("provider", MCP_AUTH_PROVIDER)
				.setParameter("limit", limit)
				.executeUpdate();
	}

	@Override
	public String hash(String plaintext) {
		if(plaintext == null) {
			throw new IllegalArgumentException("Cannot hash a null token");
		}
		return Encoder.sha256Exam(plaintext);
	}

	private List<McpToken> toTokens(List<Authentication> auths) {
		List<McpToken> tokens = new ArrayList<>(auths.size());
		for(Authentication auth:auths) {
			tokens.add(toToken(auth, auth.getIdentity()));
		}
		return tokens;
	}

	private McpToken toToken(Authentication auth, Identity identity) {
		Date creationDate = auth.getCreationDate();
		int days = mcpModule.getTokenValidityDays();
		Date expirationDate = days > 0 && creationDate != null ? DateUtils.addDays(creationDate, days) : null;
		return new McpToken(auth.getKey(), auth.getAuthusername(), toLabel(auth.getExternalId()), identity,
				creationDate, expirationDate);
	}

	/**
	 * @param externalId The value of the column externalid, &lt;identityKey&gt;:&lt;label&gt;
	 * @return The label
	 */
	static String toLabel(String externalId) {
		if(externalId == null) return "";
		int index = externalId.indexOf(':');
		return index >= 0 ? externalId.substring(index + 1) : externalId;
	}

	/**
	 * @param numOfBytes The number of random bytes
	 * @param length The fixed length of the result, large enough for the bytes
	 * @return The random bytes in base62, left padded with 0
	 */
	static String randomBase62(int numOfBytes, int length) {
		byte[] bytes = new byte[numOfBytes];
		random.nextBytes(bytes);
		return toBase62(bytes, length);
	}

	static String toBase62(byte[] bytes, int length) {
		char[] chars = new char[length];
		BigInteger value = new BigInteger(1, bytes);
		for(int i=length; i-->0; ) {
			BigInteger[] divRem = value.divideAndRemainder(SIXTY_TWO);
			chars[i] = BASE62[divRem[1].intValue()];
			value = divRem[0];
		}
		if(value.signum() != 0) {
			throw new IllegalArgumentException("The length " + length + " is too small for " + bytes.length + " bytes");
		}
		return new String(chars);
	}

	private record Generation(McpTokenCreation creation, RuntimeException error) {
		//
	}
}
