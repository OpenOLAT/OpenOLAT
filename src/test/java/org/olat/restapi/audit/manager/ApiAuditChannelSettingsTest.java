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
package org.olat.restapi.audit.manager;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.mcp.McpModule;
import org.olat.restapi.RestModule;
import org.olat.restapi.audit.ApiAuditChannel;
import org.olat.restapi.audit.ApiAuditChannelSettings;
import org.olat.restapi.audit.ApiAuditEntry;
import org.olat.restapi.audit.ApiAuditLogSearchParams;
import org.olat.restapi.audit.ApiAuditLogService;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Tests the channel-aware decisions of the audit log service: the REST
 * channel keeps the rule of the HTTP method, the MCP channel uses the read
 * or write mark of the entry and its own settings.
 * 
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class ApiAuditChannelSettingsTest extends OlatTestCase {
	
	private static final List<String> METHODS = List.of("GET", "POST", "DELETE");
	private static final List<Integer> STATUSES = List.of(200, 401, 403, 429, 500);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RestModule restModule;
	@Autowired
	private McpModule mcpModule;
	@Autowired
	private ApiAuditLogDAO auditLogDao;
	@Autowired
	private ApiAuditLogService auditLogService;
	
	private boolean restEnabled;
	private boolean restReads;
	private boolean mcpEnabled;
	private boolean mcpReads;
	
	@Before
	public void saveSettings() {
		restEnabled = restModule.isAuditLogEnabled();
		restReads = restModule.isAuditLogReads();
		mcpEnabled = mcpModule.isAuditLogEnabled();
		mcpReads = mcpModule.isAuditLogReads();
	}
	
	@After
	public void restoreSettings() {
		restModule.setAuditLogEnabled(restEnabled);
		restModule.setAuditLogReads(restReads);
		mcpModule.setAuditLogEnabled(mcpEnabled);
		mcpModule.setAuditLogReads(mcpReads);
		waitMessageAreConsumed();
	}
	
	@Test
	public void channelsOfTheModules() {
		Assert.assertEquals(ApiAuditChannel.rest, restModule.getAuditChannel());
		Assert.assertEquals(ApiAuditChannel.mcp, mcpModule.getAuditChannel());
	}
	
	@Test
	public void restEntryEqualsMethodRule() {
		for(boolean reads:List.of(Boolean.FALSE, Boolean.TRUE)) {
			restModule.setAuditLogReads(reads);
			waitMessageAreConsumed();
			
			for(String method:METHODS) {
				for(Integer status:STATUSES) {
					boolean expected = auditLogService.isAuditable(method, status.intValue());
					String message = method + " " + status + " reads=" + reads;
					Assert.assertEquals(message, expected,
							auditLogService.isAuditable(entry(ApiAuditChannel.rest, method, status.intValue(), null)));
					// an entry without channel falls back to the REST settings
					Assert.assertEquals(message, expected,
							auditLogService.isAuditable(entry(null, method, status.intValue(), null)));
				}
			}
		}
	}
	
	@Test
	public void restEntryIgnoresMcpSettings() {
		restModule.setAuditLogReads(false);
		mcpModule.setAuditLogReads(true);
		waitMessageAreConsumed();
		
		Assert.assertFalse(auditLogService.isAuditable(entry(ApiAuditChannel.rest, "GET", 200, null)));
		Assert.assertTrue(auditLogService.isAuditable(entry(ApiAuditChannel.mcp, "POST", 200, Boolean.TRUE)));
	}
	
	@Test
	public void mcpEntryReadsOff() {
		restModule.setAuditLogReads(true);
		mcpModule.setAuditLogReads(false);
		waitMessageAreConsumed();
		
		// the mark decides, not the POST of the MCP request
		Assert.assertFalse(auditLogService.isAuditable(entry(ApiAuditChannel.mcp, "POST", 200, Boolean.TRUE)));
		Assert.assertTrue(auditLogService.isAuditable(entry(ApiAuditChannel.mcp, "POST", 200, Boolean.FALSE)));
		for(Integer status:List.of(401, 403, 422, 429, 500, 503)) {
			Assert.assertTrue("read " + status,
					auditLogService.isAuditable(entry(ApiAuditChannel.mcp, "POST", status.intValue(), Boolean.TRUE)));
			Assert.assertTrue("write " + status,
					auditLogService.isAuditable(entry(ApiAuditChannel.mcp, "POST", status.intValue(), Boolean.FALSE)));
		}
	}
	
	@Test
	public void mcpEntryReadsOn() {
		restModule.setAuditLogReads(false);
		mcpModule.setAuditLogReads(true);
		waitMessageAreConsumed();
		
		Assert.assertTrue(auditLogService.isAuditable(entry(ApiAuditChannel.mcp, "POST", 200, Boolean.TRUE)));
		Assert.assertTrue(auditLogService.isAuditable(entry(ApiAuditChannel.mcp, "POST", 200, Boolean.FALSE)));
		Assert.assertTrue(auditLogService.isAuditable(entry(ApiAuditChannel.mcp, "POST", 500, Boolean.TRUE)));
	}
	
	@Test
	public void mcpDisabledRestEnabled() {
		restModule.setAuditLogEnabled(true);
		mcpModule.setAuditLogEnabled(false);
		waitMessageAreConsumed();
		
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("api-audit-chan-1");
		auditLogService.log(entry(id, ApiAuditChannel.mcp, "POST", 200, Boolean.FALSE));
		auditLogService.logInNewTransaction(entry(id, ApiAuditChannel.mcp, "POST", 500, Boolean.FALSE));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(0, countOf(id, ApiAuditChannel.mcp));
		
		auditLogService.log(entry(id, ApiAuditChannel.rest, "POST", 200, null));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(1, countOf(id, ApiAuditChannel.rest));
	}
	
	@Test
	public void restDisabledMcpEnabled() {
		restModule.setAuditLogEnabled(false);
		mcpModule.setAuditLogEnabled(true);
		waitMessageAreConsumed();
		
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("api-audit-chan-2");
		auditLogService.log(entry(id, ApiAuditChannel.rest, "POST", 200, null));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(0, countOf(id, ApiAuditChannel.rest));
		
		auditLogService.log(entry(id, ApiAuditChannel.mcp, "POST", 200, Boolean.FALSE));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(1, countOf(id, ApiAuditChannel.mcp));
	}
	
	@Test
	public void mcpReadWithoutReadsWritesNoRow() {
		mcpModule.setAuditLogEnabled(true);
		mcpModule.setAuditLogReads(false);
		waitMessageAreConsumed();
		
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("api-audit-chan-3");
		auditLogService.log(entry(id, ApiAuditChannel.mcp, "POST", 200, Boolean.TRUE));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(0, countOf(id, ApiAuditChannel.mcp));
		
		auditLogService.log(entry(id, ApiAuditChannel.mcp, "POST", 403, Boolean.TRUE));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(1, countOf(id, ApiAuditChannel.mcp));
	}
	
	/**
	 * A service which knows only the settings of the REST channel uses them
	 * for an entry of the MCP channel.
	 */
	@Test
	public void missingChannelFallsBackToRest() {
		ApiAuditLogServiceImpl service = new ApiAuditLogServiceImpl();
		ReflectionTestUtils.setField(service, "restModule", restModule);
		ReflectionTestUtils.setField(service, "channelSettingsList", List.of((ApiAuditChannelSettings)restModule));
		service.initChannelSettings();
		
		restModule.setAuditLogReads(false);
		mcpModule.setAuditLogReads(true);
		waitMessageAreConsumed();
		Assert.assertFalse(service.isAuditable(entry(ApiAuditChannel.mcp, "POST", 200, Boolean.TRUE)));
		
		restModule.setAuditLogReads(true);
		mcpModule.setAuditLogReads(false);
		waitMessageAreConsumed();
		Assert.assertTrue(service.isAuditable(entry(ApiAuditChannel.mcp, "POST", 200, Boolean.TRUE)));
	}
	
	/**
	 * A test double of the MCP settings replaces the module: the service reads
	 * the settings of the channel of the entry from the double.
	 */
	@Test
	public void channelSettingsDouble() {
		ApiAuditLogServiceImpl service = new ApiAuditLogServiceImpl();
		ReflectionTestUtils.setField(service, "restModule", restModule);
		ReflectionTestUtils.setField(service, "channelSettingsList",
				List.of(restModule, new McpSettingsDouble(true)));
		service.initChannelSettings();
		
		restModule.setAuditLogReads(false);
		waitMessageAreConsumed();
		Assert.assertTrue(service.isAuditable(entry(ApiAuditChannel.mcp, "POST", 200, Boolean.TRUE)));
		Assert.assertFalse(service.isAuditable(entry(ApiAuditChannel.rest, "GET", 200, null)));
		
		service = new ApiAuditLogServiceImpl();
		ReflectionTestUtils.setField(service, "restModule", restModule);
		ReflectionTestUtils.setField(service, "channelSettingsList",
				List.of(restModule, new McpSettingsDouble(false)));
		service.initChannelSettings();
		Assert.assertFalse(service.isAuditable(entry(ApiAuditChannel.mcp, "POST", 200, Boolean.TRUE)));
	}
	
	private int countOf(Identity identity, ApiAuditChannel channel) {
		ApiAuditLogSearchParams params = new ApiAuditLogSearchParams();
		params.setIdentityKey(identity.getKey());
		params.setChannels(List.of(channel));
		return auditLogDao.count(params);
	}
	
	private ApiAuditEntry entry(ApiAuditChannel channel, String method, int status, Boolean readOperation) {
		return entry(null, channel, method, status, readOperation);
	}
	
	private ApiAuditEntry entry(Identity identity, ApiAuditChannel channel, String method, int status, Boolean readOperation) {
		ApiAuditEntry entry = new ApiAuditEntry();
		entry.setChannel(channel);
		entry.setIdentity(identity);
		entry.setAuthProvider(channel == ApiAuditChannel.mcp ? "MCP" : ApiAuditLogService.AUTH_API_KEY);
		entry.setIp("127.0.0.1");
		entry.setUserAgent("junit");
		entry.setMethod(method);
		entry.setPath(channel == ApiAuditChannel.mcp ? "/mcp" : "/restapi/users");
		entry.setResourceClass(channel == ApiAuditChannel.mcp ? "tools/call" : "UserWebService");
		entry.setResourceMethod(channel == ApiAuditChannel.mcp ? "openolat_whoami" : "create");
		entry.setStatus(status);
		entry.setReadOperation(readOperation);
		entry.setDurationMs(Long.valueOf(3));
		entry.setNodeId(Integer.valueOf(1));
		return entry;
	}
	
	private static class McpSettingsDouble implements ApiAuditChannelSettings {
		
		private final boolean reads;
		
		public McpSettingsDouble(boolean reads) {
			this.reads = reads;
		}

		@Override
		public ApiAuditChannel getAuditChannel() {
			return ApiAuditChannel.mcp;
		}

		@Override
		public boolean isAuditLogEnabled() {
			return true;
		}

		@Override
		public boolean isAuditLogReads() {
			return reads;
		}

		@Override
		public boolean isAuditLogBody() {
			return false;
		}

		@Override
		public int getAuditLogBodyMaxSize() {
			return 0;
		}
	}
}
