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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.mcp.McpModule;
import org.olat.mcp.McpTokenCreation;
import org.olat.mcp.McpTokenService;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Runs the nightly job that deletes the expired MCP tokens.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpTokenCleanupJobTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private McpModule mcpModule;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private McpTokenService tokenService;

	private int currentTokenMax;
	private int currentValidityDays;

	@Before
	public void saveModuleSettings() {
		currentTokenMax = mcpModule.getUserTokenMax();
		currentValidityDays = mcpModule.getTokenValidityDays();
		mcpModule.setUserTokenMax(5);
	}

	@After
	public void restoreModuleSettings() {
		mcpModule.setUserTokenMax(currentTokenMax);
		mcpModule.setTokenValidityDays(currentValidityDays);
	}

	@Test
	public void jobDeletesExpiredTokens() {
		mcpModule.setTokenValidityDays(90);
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-job-1");
		McpTokenCreation expired = tokenService.generateToken(id, "Expired");
		McpTokenCreation fresh = tokenService.generateToken(id, "Fresh");
		dbInstance.commitAndCloseSession();
		setCreationDate(expired.token().key(), 91);
		setCreationDate(fresh.token().key(), 89);

		new McpTokenCleanupJob().executeWithDB(null);
		dbInstance.commitAndCloseSession();

		Assert.assertNull(securityManager.findAuthenticationByKey(expired.token().key()));
		Assert.assertNotNull(securityManager.findAuthenticationByKey(fresh.token().key()));
		Assert.assertTrue(tokenService.validateToken(fresh.plaintext()).isValid());
	}

	@Test
	public void jobKeepsEverythingWithoutExpiry() {
		mcpModule.setTokenValidityDays(0);
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-job-2");
		McpTokenCreation old = tokenService.generateToken(id, "Old");
		dbInstance.commitAndCloseSession();
		setCreationDate(old.token().key(), 2000);

		new McpTokenCleanupJob().executeWithDB(null);
		dbInstance.commitAndCloseSession();

		Assert.assertNotNull(securityManager.findAuthenticationByKey(old.token().key()));
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
