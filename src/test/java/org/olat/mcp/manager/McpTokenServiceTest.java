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

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.hibernate.proxy.HibernateProxy;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.AuthenticationImpl;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.manager.AuthenticationDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.mcp.McpModule;
import org.olat.mcp.McpToken;
import org.olat.mcp.McpTokenCreation;
import org.olat.mcp.McpTokenService;
import org.olat.mcp.McpTokenValidation;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Integration tests of the MCP tokens stored in o_bs_authentication.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpTokenServiceTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private McpModule mcpModule;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private McpTokenService tokenService;
	@Autowired
	private AuthenticationDAO authenticationDao;

	private int currentTokenMax;
	private int currentValidityDays;

	@Before
	public void saveModuleSettings() {
		currentTokenMax = mcpModule.getUserTokenMax();
		currentValidityDays = mcpModule.getTokenValidityDays();
		mcpModule.setUserTokenMax(5);
		mcpModule.setTokenValidityDays(365);
	}

	@After
	public void restoreModuleSettings() {
		mcpModule.setUserTokenMax(currentTokenMax);
		mcpModule.setTokenValidityDays(currentValidityDays);
	}

	@Test
	public void generateToken() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-token-1");
		McpTokenCreation creation = tokenService.generateToken(id, "Claude Desktop");
		dbInstance.commitAndCloseSession();

		Assert.assertNotNull(creation);
		String plaintext = creation.plaintext();
		Assert.assertTrue(plaintext.startsWith(McpTokenService.TOKEN_PREFIX));
		Assert.assertEquals(McpTokenService.TOKEN_PREFIX.length() + McpTokenServiceImpl.TOKEN_BODY_LENGTH, plaintext.length());
		Assert.assertTrue(McpTokenServiceImpl.isWellFormed(plaintext));
		Assert.assertFalse("The plaintext is not part of toString()", creation.toString().contains(plaintext));

		McpToken token = creation.token();
		Assert.assertNotNull(token.key());
		Assert.assertTrue(token.tokenId().startsWith(McpTokenService.TOKEN_ID_PREFIX));
		Assert.assertEquals(McpTokenService.TOKEN_ID_PREFIX.length() + McpTokenServiceImpl.TOKEN_ID_BODY_LENGTH, token.tokenId().length());
		Assert.assertEquals("Claude Desktop", token.label());
		Assert.assertEquals(id, token.identity());
		Assert.assertNotNull(token.creationDate());
		Assert.assertEquals(DateUtils.addDays(token.creationDate(), 365), token.expirationDate());
	}

	@Test
	public void generateToken_distinct() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-token-2");
		McpTokenCreation creation1 = tokenService.generateToken(id, "Client 1");
		McpTokenCreation creation2 = tokenService.generateToken(id, "Client 2");
		dbInstance.commitAndCloseSession();

		Assert.assertNotEquals(creation1.plaintext(), creation2.plaintext());
		Assert.assertNotEquals(creation1.token().tokenId(), creation2.token().tokenId());
		Assert.assertNotEquals(creation1.token().key(), creation2.token().key());
		Assert.assertEquals(2, tokenService.countTokens(id));
	}

	@Test
	public void generateToken_rowHoldsHashNotPlaintext() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-token-3");
		McpTokenCreation creation = tokenService.generateToken(id, "  Cursor  ");
		dbInstance.commitAndCloseSession();

		Authentication auth = securityManager.findAuthenticationByKey(creation.token().key());
		Assert.assertNotNull(auth);
		Assert.assertEquals(McpTokenService.MCP_AUTH_PROVIDER, auth.getProvider());
		Assert.assertEquals(BaseSecurity.DEFAULT_ISSUER, auth.getIssuer());
		Assert.assertEquals(creation.token().tokenId(), auth.getAuthusername());
		Assert.assertEquals(id.getKey() + ":Cursor", auth.getExternalId());
		Assert.assertEquals(tokenService.hash(creation.plaintext()), auth.getCredential());
		Assert.assertNotEquals(creation.plaintext(), auth.getCredential());
		Assert.assertFalse(auth.getCredential().contains(creation.plaintext().substring(McpTokenService.TOKEN_PREFIX.length())));
		Assert.assertEquals(McpTokenServiceImpl.HASH_ALGORITHM, auth.getAlgorithm());
		Assert.assertNull(auth.getSalt());
		Assert.assertFalse("BaseSecurity cannot check an MCP token", securityManager.checkCredentials(auth, creation.plaintext()));
	}

	@Test
	public void hash() {
		String hash1 = tokenService.hash("oomcp_abc");
		String hash2 = tokenService.hash("oomcp_abc");
		Assert.assertEquals(hash1, hash2);
		Assert.assertEquals(64, hash1.length());
		Assert.assertTrue(hash1.matches("[0-9a-f]{64}"));
		Assert.assertNotEquals(hash1, tokenService.hash("oomcp_abd"));
		// Test vector of FIPS 180-2
		Assert.assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", tokenService.hash("abc"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void hash_null() {
		tokenService.hash(null);
	}

	@Test
	public void validateToken_valid() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-token-4");
		McpTokenCreation creation = tokenService.generateToken(id, "Valid");
		dbInstance.commitAndCloseSession();

		McpTokenValidation validation = tokenService.validateToken(creation.plaintext());
		Assert.assertTrue(validation.isValid());
		Assert.assertEquals(McpTokenValidation.Status.valid, validation.status());
		Assert.assertEquals(tokenService.hash(creation.plaintext()), validation.tokenHash());
		Assert.assertEquals(creation.token().key(), validation.token().key());
		Assert.assertEquals(creation.token().tokenId(), validation.token().tokenId());
		Assert.assertEquals("Valid", validation.token().label());
		Assert.assertEquals(id.getKey(), validation.token().identity().getKey());
		// the tools work with this identity detached, a proxy breaks later queries
		Assert.assertFalse(validation.token().identity() instanceof HibernateProxy);
	}

	@Test
	public void validateToken_unknown() {
		String wellFormed = McpTokenService.TOKEN_PREFIX
				+ McpTokenServiceImpl.randomBase62(McpTokenServiceImpl.TOKEN_BYTES, McpTokenServiceImpl.TOKEN_BODY_LENGTH);
		McpTokenValidation validation = tokenService.validateToken(wellFormed);
		Assert.assertEquals(McpTokenValidation.Status.unknown, validation.status());
		Assert.assertFalse(validation.isValid());
		Assert.assertNull(validation.token());
		Assert.assertEquals(tokenService.hash(wellFormed), validation.tokenHash());
	}

	@Test
	public void validateToken_malformed() {
		String body = McpTokenServiceImpl.randomBase62(McpTokenServiceImpl.TOKEN_BYTES, McpTokenServiceImpl.TOKEN_BODY_LENGTH);
		String[] malformed = new String[] {
			null,
			"",
			"   ",
			"oomcp_",
			"oomcp_short",
			"Bearer oomcp_" + body,
			"xxmcp_" + body,
			"OOMCP_" + body,
			"oomcp_" + body + "0",
			"oomcp_" + body.substring(1) + "-",
			"oomcp_" + body.substring(1) + "é",
			UUID.randomUUID().toString()
		};
		for(String plaintext:malformed) {
			McpTokenValidation validation = tokenService.validateToken(plaintext);
			Assert.assertEquals(McpTokenValidation.Status.unknown, validation.status());
			Assert.assertNull("No hash for a malformed token: " + plaintext, validation.tokenHash());
			Assert.assertNull(validation.token());
		}
	}

	/**
	 * Security (L2): a row with the provider MCP that this service did not
	 * write is never accepted, even when its credential is the hash.
	 */
	@Test
	public void validateToken_foreignMcpRow() {
		String plaintext = McpTokenService.TOKEN_PREFIX
				+ McpTokenServiceImpl.randomBase62(McpTokenServiceImpl.TOKEN_BYTES, McpTokenServiceImpl.TOKEN_BODY_LENGTH);
		String tokenHash = tokenService.hash(plaintext);

		// plain credential, no hash algorithm
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-token-24");
		Authentication plainRow = securityManager.createAndPersistAuthentication(id1, McpTokenService.MCP_AUTH_PROVIDER,
				BaseSecurity.DEFAULT_ISSUER, null, "t_" + UUID.randomUUID().toString().replace("-", ""), tokenHash, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(plainRow);
		Assert.assertNull(plainRow.getAlgorithm());

		McpTokenValidation validation = tokenService.validateToken(plaintext);
		Assert.assertEquals(McpTokenValidation.Status.unknown, validation.status());
		Assert.assertNull(validation.token());
		securityManager.deleteAuthentication(plainRow);
		dbInstance.commitAndCloseSession();

		// right algorithm, but no token id
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-token-25");
		AuthenticationImpl noTokenIdRow = new AuthenticationImpl(id2, McpTokenService.MCP_AUTH_PROVIDER, BaseSecurity.DEFAULT_ISSUER,
				null, id2.getName(), tokenHash, null, McpTokenServiceImpl.HASH_ALGORITHM);
		noTokenIdRow.setLastModified(new Date());
		authenticationDao.persist(id2, noTokenIdRow);
		dbInstance.commitAndCloseSession();

		validation = tokenService.validateToken(plaintext);
		Assert.assertEquals(McpTokenValidation.Status.unknown, validation.status());
		Assert.assertNull(validation.token());
		securityManager.deleteAuthentication(noTokenIdRow);
		dbInstance.commitAndCloseSession();
	}

	@Test
	public void validateToken_expired() {
		mcpModule.setTokenValidityDays(30);
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-token-5");
		McpTokenCreation creation = tokenService.generateToken(id, "Expired");
		dbInstance.commitAndCloseSession();
		setCreationDate(creation.token().key(), 31);

		McpTokenValidation validation = tokenService.validateToken(creation.plaintext());
		Assert.assertEquals(McpTokenValidation.Status.expired, validation.status());
		Assert.assertNotNull(validation.token());
		Assert.assertTrue(validation.token().isExpired(new Date()));
		Assert.assertEquals(tokenService.hash(creation.plaintext()), validation.tokenHash());

		// one day before the end of the validity
		setCreationDate(creation.token().key(), 29);
		Assert.assertTrue(tokenService.validateToken(creation.plaintext()).isValid());
	}

	@Test
	public void validateToken_noExpiry() {
		mcpModule.setTokenValidityDays(0);
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-token-6");
		McpTokenCreation creation = tokenService.generateToken(id, "Forever");
		dbInstance.commitAndCloseSession();
		setCreationDate(creation.token().key(), 3650);

		McpTokenValidation validation = tokenService.validateToken(creation.plaintext());
		Assert.assertTrue(validation.isValid());
		Assert.assertNull(validation.token().expirationDate());
	}

	@Test
	public void validateToken_identityInactive() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-token-7");
		Identity admin = JunitTestHelper.getDefaultActor();
		McpTokenCreation creation = tokenService.generateToken(id, "Inactive");
		dbInstance.commitAndCloseSession();

		Integer[] statusList = new Integer[] { Identity.STATUS_INACTIVE, Identity.STATUS_LOGIN_DENIED, Identity.STATUS_PENDING, Identity.STATUS_DELETED };
		for(Integer status:statusList) {
			securityManager.saveIdentityStatus(id, status, admin);
			dbInstance.commitAndCloseSession();

			McpTokenValidation validation = tokenService.validateToken(creation.plaintext());
			Assert.assertEquals(McpTokenValidation.Status.identityInactive, validation.status());
			Assert.assertNotNull(validation.token());
		}

		securityManager.saveIdentityStatus(id, Identity.STATUS_PERMANENT, admin);
		dbInstance.commitAndCloseSession();
		Assert.assertTrue(tokenService.validateToken(creation.plaintext()).isValid());

		securityManager.saveIdentityStatus(id, Identity.STATUS_ACTIV, admin);
		dbInstance.commitAndCloseSession();
		Assert.assertTrue(tokenService.validateToken(creation.plaintext()).isValid());
	}

	@Test
	public void generateToken_maxPerUser() {
		mcpModule.setUserTokenMax(2);
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-token-8");
		tokenService.generateToken(id, "One");
		tokenService.generateToken(id, "Two");
		dbInstance.commitAndCloseSession();

		try {
			tokenService.generateToken(id, "Three");
			Assert.fail("The maximum of 2 tokens is reached");
		} catch (IllegalStateException e) {
			// expected
		}
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(2, tokenService.countTokens(id));

		// 0 means no limit
		mcpModule.setUserTokenMax(0);
		tokenService.generateToken(id, "Three");
		tokenService.generateToken(id, "Four");
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(4, tokenService.countTokens(id));
	}

	@Test
	public void generateToken_labelValidation() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-token-9");

		String[] invalidLabels = new String[] { null, "", "   ", "\t", "a".repeat(McpTokenService.LABEL_MAX_LENGTH + 1) };
		for(String label:invalidLabels) {
			try {
				tokenService.generateToken(id, label);
				Assert.fail("Invalid label accepted: " + label);
			} catch (IllegalArgumentException e) {
				// expected
			}
		}
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(0, tokenService.countTokens(id));

		String maxLabel = "b".repeat(McpTokenService.LABEL_MAX_LENGTH);
		McpTokenCreation maxCreation = tokenService.generateToken(id, maxLabel);
		McpTokenCreation paddedCreation = tokenService.generateToken(id, "  " + "c".repeat(McpTokenService.LABEL_MAX_LENGTH) + "  ");
		McpTokenCreation trimmedCreation = tokenService.generateToken(id, "  My agent \n");
		dbInstance.commitAndCloseSession();

		Assert.assertEquals(maxLabel, tokenService.getToken(maxCreation.token().tokenId()).label());
		Assert.assertEquals("c".repeat(McpTokenService.LABEL_MAX_LENGTH), tokenService.getToken(paddedCreation.token().tokenId()).label());
		Assert.assertEquals("My agent", tokenService.getToken(trimmedCreation.token().tokenId()).label());
	}

	@Test
	public void generateToken_duplicateLabel() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-token-10");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-token-11");
		tokenService.generateToken(id1, "Same label");
		dbInstance.commitAndCloseSession();

		try {
			tokenService.generateToken(id1, "same LABEL ");
			Assert.fail("Duplicate label of the same identity accepted");
		} catch (IllegalArgumentException e) {
			// expected
		}
		dbInstance.commitAndCloseSession();

		// Another identity may use the same label
		McpTokenCreation creation = tokenService.generateToken(id2, "Same label");
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(creation);
		Assert.assertEquals(1, tokenService.countTokens(id1));
		Assert.assertEquals(1, tokenService.countTokens(id2));
	}

	@Test
	public void getTokens_ordered() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-token-12");
		Identity other = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-token-13");
		McpTokenCreation creation1 = tokenService.generateToken(id, "First");
		McpTokenCreation creation2 = tokenService.generateToken(id, "Second");
		McpTokenCreation creation3 = tokenService.generateToken(id, "Third");
		tokenService.generateToken(other, "Other");
		dbInstance.commitAndCloseSession();
		// Make the order independent of the timer resolution
		setCreationDate(creation1.token().key(), 3);
		setCreationDate(creation2.token().key(), 2);
		setCreationDate(creation3.token().key(), 1);

		List<McpToken> tokens = tokenService.getTokens(id);
		Assert.assertEquals(3, tokens.size());
		Assert.assertEquals(creation1.token().tokenId(), tokens.get(0).tokenId());
		Assert.assertEquals(creation2.token().tokenId(), tokens.get(1).tokenId());
		Assert.assertEquals(creation3.token().tokenId(), tokens.get(2).tokenId());
		Assert.assertEquals("First", tokens.get(0).label());

		Assert.assertTrue(tokenService.getTokens(JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-token-14")).isEmpty());
	}

	@Test
	public void getToken() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-token-15");
		McpTokenCreation creation = tokenService.generateToken(id, "Load me");
		dbInstance.commitAndCloseSession();

		McpToken token = tokenService.getToken(creation.token().tokenId());
		Assert.assertNotNull(token);
		Assert.assertEquals(creation.token().key(), token.key());
		Assert.assertEquals("Load me", token.label());
		Assert.assertEquals(id, token.identity());
		Assert.assertEquals(creation.token().expirationDate(), token.expirationDate());

		Assert.assertNull(tokenService.getToken("t_unknown"));
		Assert.assertNull(tokenService.getToken(null));
		Assert.assertNull(tokenService.getToken(""));
	}

	@Test
	public void searchTokens() {
		String marker = "srch" + UUID.randomUUID().toString().replace("-", "");
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-token-16");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-token-17");
		McpTokenCreation creation1 = tokenService.generateToken(id1, marker + " one");
		McpTokenCreation creation2 = tokenService.generateToken(id1, marker + " two");
		McpTokenCreation creation3 = tokenService.generateToken(id2, marker + " three");
		McpTokenCreation unrelated = tokenService.generateToken(id2, "Unrelated");
		dbInstance.commitAndCloseSession();
		setCreationDate(creation1.token().key(), 3);
		setCreationDate(creation2.token().key(), 2);
		setCreationDate(creation3.token().key(), 1);

		// by label, ordered by creation date descending
		Assert.assertEquals(3, tokenService.countSearchTokens(marker));
		List<McpToken> all = tokenService.searchTokens(marker, 0, 10);
		Assert.assertEquals(3, all.size());
		Assert.assertEquals(creation3.token().tokenId(), all.get(0).tokenId());
		Assert.assertEquals(creation2.token().tokenId(), all.get(1).tokenId());
		Assert.assertEquals(creation1.token().tokenId(), all.get(2).tokenId());

		// pagination
		List<McpToken> page1 = tokenService.searchTokens(marker, 0, 2);
		List<McpToken> page2 = tokenService.searchTokens(marker, 2, 2);
		Assert.assertEquals(2, page1.size());
		Assert.assertEquals(1, page2.size());
		Assert.assertEquals(creation1.token().tokenId(), page2.get(0).tokenId());

		// case insensitive
		Assert.assertEquals(3, tokenService.countSearchTokens(marker.toUpperCase()));

		// by username
		List<McpToken> byUsername = tokenService.searchTokens(id2.getName(), 0, 10);
		Assert.assertEquals(2, byUsername.size());
		Assert.assertTrue(byUsername.stream().anyMatch(token -> token.tokenId().equals(unrelated.token().tokenId())));
		Assert.assertEquals(2, tokenService.countSearchTokens(id2.getName()));

		// by token id
		List<McpToken> byTokenId = tokenService.searchTokens(creation2.token().tokenId(), 0, 10);
		Assert.assertEquals(1, byTokenId.size());
		Assert.assertEquals(creation2.token().key(), byTokenId.get(0).key());

		// no search string returns every token
		Assert.assertTrue(tokenService.countSearchTokens(null) >= 4);
		Assert.assertFalse(tokenService.searchTokens(null, 0, 2).isEmpty());
		Assert.assertEquals(0, tokenService.countSearchTokens("nomatch" + UUID.randomUUID()));
	}

	@Test
	public void revokeToken_byOwner() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-token-18");
		McpTokenCreation creation = tokenService.generateToken(id, "Revoke");
		dbInstance.commitAndCloseSession();

		Assert.assertTrue(tokenService.revokeToken(creation.token().tokenId(), id));
		dbInstance.commitAndCloseSession();

		Assert.assertNull(securityManager.findAuthenticationByKey(creation.token().key()));
		Assert.assertEquals(McpTokenValidation.Status.unknown, tokenService.validateToken(creation.plaintext()).status());
		// a second time does nothing
		Assert.assertFalse(tokenService.revokeToken(creation.token().tokenId(), id));
	}

	@Test
	public void revokeToken_byAdministrator() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-token-19");
		McpTokenCreation creation = tokenService.generateToken(id, "Revoke");
		dbInstance.commitAndCloseSession();

		Assert.assertTrue(tokenService.revokeToken(creation.token().tokenId(), null));
		dbInstance.commitAndCloseSession();
		Assert.assertNull(securityManager.findAuthenticationByKey(creation.token().key()));
	}

	@Test
	public void revokeToken_foreignOwnerDenied() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-token-20");
		Identity intruder = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-token-21");
		McpTokenCreation creation = tokenService.generateToken(id, "Mine");
		dbInstance.commitAndCloseSession();

		Assert.assertFalse(tokenService.revokeToken(creation.token().tokenId(), intruder));
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(securityManager.findAuthenticationByKey(creation.token().key()));
		Assert.assertTrue(tokenService.validateToken(creation.plaintext()).isValid());

		Assert.assertFalse(tokenService.revokeToken("t_unknown", null));
		Assert.assertFalse(tokenService.revokeToken(null, null));
	}

	@Test
	public void deleteExpiredTokens() {
		mcpModule.setTokenValidityDays(30);
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-token-22");
		McpTokenCreation expired = tokenService.generateToken(id, "Expired");
		McpTokenCreation fresh = tokenService.generateToken(id, "Fresh");
		Authentication otherProvider = securityManager.createAndPersistAuthentication(id, "REST", BaseSecurity.DEFAULT_ISSUER,
				null, id.getName(), UUID.randomUUID().toString(), null);
		dbInstance.commitAndCloseSession();
		setCreationDate(expired.token().key(), 31);
		setCreationDate(otherProvider.getKey(), 400);

		int deleted = tokenService.deleteExpiredTokens();
		dbInstance.commitAndCloseSession();

		Assert.assertTrue(deleted >= 1);
		Assert.assertNull(securityManager.findAuthenticationByKey(expired.token().key()));
		Assert.assertNotNull(securityManager.findAuthenticationByKey(fresh.token().key()));
		Assert.assertNotNull(securityManager.findAuthenticationByKey(otherProvider.getKey()));
	}

	@Test
	public void deleteExpiredTokens_noExpiry() {
		mcpModule.setTokenValidityDays(0);
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-token-23");
		McpTokenCreation old = tokenService.generateToken(id, "Old");
		dbInstance.commitAndCloseSession();
		setCreationDate(old.token().key(), 4000);

		Assert.assertEquals(0, tokenService.deleteExpiredTokens());
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(securityManager.findAuthenticationByKey(old.token().key()));
	}

	@Test
	public void toBase62() {
		Assert.assertEquals("000", McpTokenServiceImpl.toBase62(new byte[] { 0 }, 3));
		Assert.assertEquals("00z", McpTokenServiceImpl.toBase62(new byte[] { 61 }, 3));
		Assert.assertEquals("010", McpTokenServiceImpl.toBase62(new byte[] { 62 }, 3));
		Assert.assertEquals("047", McpTokenServiceImpl.toBase62(new byte[] { (byte)0xFF }, 3));
		// the largest values fit in the fixed lengths
		byte[] max24 = new byte[McpTokenServiceImpl.TOKEN_BYTES];
		java.util.Arrays.fill(max24, (byte)0xFF);
		Assert.assertEquals(McpTokenServiceImpl.TOKEN_BODY_LENGTH, McpTokenServiceImpl.toBase62(max24, McpTokenServiceImpl.TOKEN_BODY_LENGTH).length());
		byte[] max10 = new byte[McpTokenServiceImpl.TOKEN_ID_BYTES];
		java.util.Arrays.fill(max10, (byte)0xFF);
		Assert.assertEquals(McpTokenServiceImpl.TOKEN_ID_BODY_LENGTH, McpTokenServiceImpl.toBase62(max10, McpTokenServiceImpl.TOKEN_ID_BODY_LENGTH).length());
	}

	@Test(expected = IllegalArgumentException.class)
	public void toBase62_tooShort() {
		McpTokenServiceImpl.toBase62(new byte[] { (byte)0xFF, (byte)0xFF }, 2);
	}

	@Test
	public void toLabel() {
		Assert.assertEquals("My label", McpTokenServiceImpl.toLabel("1234:My label"));
		Assert.assertEquals("a:b", McpTokenServiceImpl.toLabel("1234:a:b"));
		Assert.assertEquals("", McpTokenServiceImpl.toLabel(null));
	}

	private void setCreationDate(Long authKey, int daysAgo) {
		dbInstance.getCurrentEntityManager()
			.createQuery("update authentication set creationDate=:creationDate where key=:key")
			.setParameter("creationDate", DateUtils.addDays(new Date(), -daysAgo))
			.setParameter("key", authKey)
			.executeUpdate();
		dbInstance.commitAndCloseSession();
	}
}
