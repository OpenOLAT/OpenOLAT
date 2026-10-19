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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.olat.mcp.McpModule;
import org.olat.mcp.tool.McpToolScope;
import org.olat.restapi.audit.ApiAuditEntry;
import org.olat.restapi.audit.ApiAuditLogService;
import org.olat.restapi.audit.manager.ApiAuditMasking;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * The body of the audit row: masked, bounded and only built for a row the
 * audit log stores.
 *
 * Initial date: 4 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpAuditWriterTest {

	private static McpModule module(boolean enabled, boolean body, int maxSize) {
		McpModule module = mock(McpModule.class);
		when(module.isAuditLogEnabled()).thenReturn(enabled);
		when(module.isAuditLogBody()).thenReturn(body);
		when(module.getAuditLogBodyMaxSize()).thenReturn(maxSize);
		return module;
	}

	private static ApiAuditLogService service(boolean auditable) {
		ApiAuditLogService service = mock(ApiAuditLogService.class);
		when(service.isAuditable(any(ApiAuditEntry.class))).thenReturn(auditable);
		return service;
	}

	private static ApiAuditEntry toEntry(McpAuditWriter writer, McpRequestTrace trace) {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/olat/mcp");
		MockHttpServletResponse response = new MockHttpServletResponse();
		return writer.toEntry(request, response, trace, null);
	}

	private static McpRequestTrace writeCall(Map<String,Object> args) {
		McpRequestTrace trace = new McpRequestTrace();
		trace.setJsonRpcMethod("tools/call");
		trace.toolCall("file_modify", McpToolScope.WRITE, args);
		return trace;
	}

	@Test
	public void bodyIsMasked() {
		McpAuditWriter writer = new McpAuditWriter(module(true, true, 16384), service(true));
		ApiAuditEntry entry = toEntry(writer, writeCall(Map.of("action", "create", "password", "secret1", "nested", Map.of("token", "abc"))));
		Assertions.assertThat(entry.getRequestBody())
			.contains("\"action\":\"create\"")
			.contains("\"password\":\"" + ApiAuditMasking.MASK + "\"")
			.contains("\"token\":\"" + ApiAuditMasking.MASK + "\"")
			.doesNotContain("secret1")
			.doesNotContain("abc");
	}

	@Test
	public void bodyIsBounded() {
		McpAuditWriter writer = new McpAuditWriter(module(true, true, 2000), service(true));
		Map<String,Object> args = new LinkedHashMap<>();
		args.put("action", "create");
		args.put("content", "a".repeat(1_000_000));
		args.put("other", "b".repeat(1_000_000));
		ApiAuditEntry entry = toEntry(writer, writeCall(args));
		Assertions.assertThat(entry.getRequestBody())
			.startsWith("{\"action\":\"create\",\"content\":\"aaa")
			.contains(" \u2026 [1000000 chars]");
		Assertions.assertThat(entry.getRequestBody().getBytes(StandardCharsets.UTF_8).length).isLessThanOrEqualTo(2000);
	}

	@Test
	public void noBodyForAReadThatIsNotStored() {
		McpAuditWriter writer = new McpAuditWriter(module(true, true, 16384), service(false));
		McpRequestTrace trace = new McpRequestTrace();
		trace.setJsonRpcMethod("tools/call");
		trace.toolCall("course_query", McpToolScope.READ, Map.of("action", "list", "password", "x"));
		ApiAuditEntry entry = toEntry(writer, trace);
		Assertions.assertThat(entry.getRequestBody()).isNull();
		Assertions.assertThat(entry.getReadOperation()).isTrue();
		Assertions.assertThat(entry.getResourceMethod()).isEqualTo("course_query");
	}

	@Test
	public void noBodyWhenBodyLoggingIsOff() {
		McpAuditWriter writer = new McpAuditWriter(module(true, false, 16384), service(true));
		ApiAuditEntry entry = toEntry(writer, writeCall(Map.of("action", "create")));
		Assertions.assertThat(entry.getRequestBody()).isNull();
	}

	@Test
	public void noBodyWhenAuditIsOff() {
		McpAuditWriter writer = new McpAuditWriter(module(false, true, 16384), service(true));
		ApiAuditEntry entry = toEntry(writer, writeCall(Map.of("action", "create")));
		Assertions.assertThat(entry.getRequestBody()).isNull();
	}
	@Test
	public void errorReferenceInRowAndAccessLine() {
		McpAuditWriter writer = new McpAuditWriter(module(true, true, 16384), service(true));
		McpRequestTrace trace = writeCall(Map.of("action", "create"));
		trace.setOutcome(McpRequestTrace.Outcome.serverError);
		trace.setErrorReference("17894119-8606-4400-be80-60b9006e7fc3");
		ApiAuditEntry entry = toEntry(writer, trace);
		Assertions.assertThat(entry.getStatus()).isEqualTo(500);
		Assertions.assertThat(entry.getRef()).isEqualTo("17894119-8606-4400-be80-60b9006e7fc3");
		Assertions.assertThat(entry.getPathParams()).contains("\"errorReference\":\"17894119-8606-4400-be80-60b9006e7fc3\"");
	}

	@Test
	public void noReferenceWithoutError() {
		McpAuditWriter writer = new McpAuditWriter(module(true, true, 16384), service(true));
		ApiAuditEntry entry = toEntry(writer, writeCall(Map.of("action", "create")));
		Assertions.assertThat(entry.getRef()).isNull();
	}
}
