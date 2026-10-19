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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.olat.core.id.Identity;
import org.olat.mcp.McpGrants;
import org.olat.mcp.McpModule;
import org.olat.mcp.tool.McpBinaryContent;
import org.olat.mcp.tool.McpInvalidArgumentException;
import org.olat.mcp.tool.McpPermissionException;
import org.olat.mcp.tool.McpToolContext;
import org.olat.mcp.tool.McpToolDefinition;
import org.olat.mcp.tool.McpToolProvider;
import org.olat.mcp.tool.McpToolResult;
import org.olat.mcp.tool.McpToolScope;
import org.olat.mcp.tool.core.WhoAmIToolProvider;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpStatelessServerFeatures;
import io.modelcontextprotocol.server.McpStatelessSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStatelessServerTransport;
import io.modelcontextprotocol.spec.McpSchema;

/**
 * The execution wrapper and the mapping to the wire format.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpToolRegistryTest extends OlatTestCase {

	@Autowired
	private McpToolRegistry toolRegistry;

	private static McpToolProvider provider(String name, McpToolScope scope, BiFunction<McpToolContext,Map<String,Object>,McpToolResult> body) {
		return provider(name, McpToolProvider.PACK_CORE, "Tests", scope, body);
	}

	private static McpToolProvider provider(String name, String pack, String objectTitle, McpToolScope scope,
			BiFunction<McpToolContext,Map<String,Object>,McpToolResult> body) {
		return new McpToolProvider() {
			@Override
			public String getToolName() { return name; }
			@Override
			public String getPack() { return pack; }
			@Override
			public McpToolScope getScope() { return scope; }
			@Override
			public boolean isAvailable() { return true; }
			@Override
			public McpToolDefinition getDefinition() {
				return new McpToolDefinition("T", objectTitle, "Test tool with a description of three sentences.", null, null, scope.isWrite(), !scope.isWrite());
			}
			@Override
			public McpToolResult execute(McpToolContext context, Map<String,Object> arguments) {
				return body.apply(context, arguments);
			}
		};
	}

	private static McpTransportContext context(McpRequestTrace trace, Identity identity, String... grantedTools) {
		McpGrants grants = new McpGrants(Set.of(grantedTools), null, List.of("test"));
		McpToolContext toolContext = new McpToolContext(identity, null, grants, null, null, "127.0.0.1");
		return McpTransportContext.create(Map.of(McpToolRegistry.CTX_TOOL_CONTEXT, toolContext, McpRequestTrace.KEY, trace));
	}

	@Test
	public void registryHoldsWhoAmIFirst() {
		Assertions.assertThat(toolRegistry.getProviders()).isNotEmpty();
		Assertions.assertThat(toolRegistry.getProvider(WhoAmIToolProvider.NAME)).isInstanceOf(WhoAmIToolProvider.class);
		Assertions.assertThat(toolRegistry.getProviders().get(0).getPack()).isEqualTo(McpToolProvider.PACK_CORE);
		Assertions.assertThat(toolRegistry.specifications()).hasSize(toolRegistry.getProviders().size());
	}

	@Test
	public void registryProvidesObjectsAndObjectTitles() {
		Assertions.assertThat(toolRegistry.getProvider(WhoAmIToolProvider.NAME).getObject()).isEqualTo("whoami");
		Assertions.assertThat(toolRegistry.getProvider(WhoAmIToolProvider.NAME).getDefinition().objectTitle()).isEqualTo("Account");
		Map<String,String> objectTitles = new HashMap<>();
		String previousPack = "";
		for(McpToolProvider provider:toolRegistry.getProviders()) {
			Assertions.assertThat(provider.getPack()).matches(McpToolProvider.PACK_PATTERN);
			Assertions.assertThat(provider.getPack()).isGreaterThanOrEqualTo(previousPack);
			previousPack = provider.getPack();
			Assertions.assertThat(provider.getToolName()).contains(provider.getObject());
			Assertions.assertThat(provider.getDefinition().objectTitle()).isNotBlank();
			String title = objectTitles.putIfAbsent(provider.getObject(), provider.getDefinition().objectTitle());
			if(title != null) {
				Assertions.assertThat(provider.getDefinition().objectTitle()).isEqualTo(title);
			}
		}
		McpToolProvider appointmentQuery = toolRegistry.getProvider("appointment_query");
		if(appointmentQuery != null) {
			Assertions.assertThat(appointmentQuery.getObject()).isEqualTo("appointment");
			Assertions.assertThat(toolRegistry.getProvider("appointment_modify").getObject()).isEqualTo("appointment");
		}
	}

	@Test
	public void validateSortsByPackThenName() {
		BiFunction<McpToolContext,Map<String,Object>,McpToolResult> body = (c, a) -> McpToolResult.of(null, "");
		List<McpToolProvider> sorted = McpToolRegistry.validateAndSort(List.of(
				provider("b_query", "zeta", "Bs", McpToolScope.READ, body),
				provider("a_modify", "alpha", "As", McpToolScope.WRITE, body),
				provider("a_query", "alpha", "As", McpToolScope.READ, body)));
		Assertions.assertThat(sorted).extracting(McpToolProvider::getToolName)
			.containsExactly("a_modify", "a_query", "b_query");
		Assertions.assertThat(sorted).extracting(McpToolProvider::getObject)
			.containsExactly("a", "a", "b");
	}

	@Test
	public void validateRejectsInvalidPackName() {
		BiFunction<McpToolContext,Map<String,Object>,McpToolResult> body = (c, a) -> McpToolResult.of(null, "");
		for(String pack:new String[] { null, "", "Project", "1pack", "pack-name", "a23456789012345678901234567890123" }) {
			Assertions.assertThatThrownBy(() -> McpToolRegistry.validateAndSort(List.of(
					provider("x_query", pack, "Xs", McpToolScope.READ, body))))
				.isInstanceOf(IllegalStateException.class);
		}
		Assertions.assertThat(McpToolRegistry.validateAndSort(List.of(
				provider("x_query", "a2345678901234567890123456789012", "Xs", McpToolScope.READ, body)))).hasSize(1);
	}

	@Test
	public void validateRejectsDifferentObjectTitles() {
		BiFunction<McpToolContext,Map<String,Object>,McpToolResult> body = (c, a) -> McpToolResult.of(null, "");
		Assertions.assertThatThrownBy(() -> McpToolRegistry.validateAndSort(List.of(
				provider("x_query", "core", "Xs", McpToolScope.READ, body),
				provider("x_modify", "core", "Other", McpToolScope.WRITE, body))))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("object titles");
		Assertions.assertThatThrownBy(() -> McpToolRegistry.validateAndSort(List.of(
				provider("x_query", "core", null, McpToolScope.READ, body))))
			.isInstanceOf(IllegalStateException.class);
	}

	@Test
	public void toToolMapsAnnotations() {
		McpSchema.Tool read = McpToolRegistry.toTool(provider("x_query", McpToolScope.READ, (c, a) -> McpToolResult.of(null, "")));
		Assertions.assertThat(read.name()).isEqualTo("x_query");
		Assertions.assertThat(read.annotations().readOnlyHint()).isTrue();
		Assertions.assertThat(read.annotations().destructiveHint()).isFalse();
		Assertions.assertThat(read.annotations().openWorldHint()).isFalse();
		Assertions.assertThat(read.inputSchema()).containsEntry("type", "object");

		McpSchema.Tool write = McpToolRegistry.toTool(provider("x_modify", McpToolScope.WRITE, (c, a) -> McpToolResult.of(null, "")));
		Assertions.assertThat(write.annotations().readOnlyHint()).isFalse();
		Assertions.assertThat(write.annotations().destructiveHint()).isTrue();
	}

	@Test
	public void toCallToolResultWithAttachments() {
		byte[] png = new byte[] { (byte)0x89, 'P', 'N', 'G' };
		byte[] pdf = new byte[] { '%', 'P', 'D', 'F' };
		McpToolResult result = McpToolResult.of(Map.of("k", "v"), "two parts", List.of(
				new McpBinaryContent("openolat://project/1/file/2/a.png", "image/png", png),
				new McpBinaryContent("openolat://project/1/file/3/b.pdf", "application/pdf", pdf)));

		McpSchema.CallToolResult wire = McpToolRegistry.toCallToolResult(result);
		Assertions.assertThat(wire.isError()).isFalse();
		Assertions.assertThat(wire.content()).hasSize(3);
		Assertions.assertThat(((McpSchema.TextContent)wire.content().get(0)).text()).isEqualTo("two parts");
		McpSchema.ImageContent image = (McpSchema.ImageContent)wire.content().get(1);
		Assertions.assertThat(image.mimeType()).isEqualTo("image/png");
		Assertions.assertThat(Base64.getDecoder().decode(image.data())).isEqualTo(png);
		McpSchema.EmbeddedResource embedded = (McpSchema.EmbeddedResource)wire.content().get(2);
		McpSchema.BlobResourceContents blob = (McpSchema.BlobResourceContents)embedded.resource();
		Assertions.assertThat(blob.uri()).isEqualTo("openolat://project/1/file/3/b.pdf");
		Assertions.assertThat(blob.mimeType()).isEqualTo("application/pdf");
		Assertions.assertThat(Base64.getDecoder().decode(blob.blob())).isEqualTo(pdf);

		// an error result never carries attachments
		Assertions.assertThat(McpToolResult.error("x").attachments()).isEmpty();
		Assertions.assertThat(McpToolRegistry.toCallToolResult(McpToolResult.of(null, "plain")).content()).hasSize(1);
	}

	@Test
	public void callSuccessWithStructuredContent() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-reg-1");
		McpRequestTrace trace = new McpRequestTrace();
		McpToolProvider provider = provider("x_query", McpToolScope.READ,
				(c, a) -> McpToolResult.of(Map.of("who", c.identity().getKey(), "arg", a.get("k")), "ok"));

		McpSchema.CallToolResult result = toolRegistry.call(provider, context(trace, identity, "x_query"),
				new McpSchema.CallToolRequest("x_query", Map.of("k", "v"), null));
		Assertions.assertThat(result.isError()).isFalse();
		Assertions.assertThat(result.structuredContent()).isInstanceOf(Map.class);
		Assertions.assertThat(((Map<?,?>)result.structuredContent()).get("arg")).isEqualTo("v");
		Assertions.assertThat(trace.getOutcome()).isEqualTo(McpRequestTrace.Outcome.success);
		Assertions.assertThat(trace.getToolName()).isEqualTo("x_query");
		Assertions.assertThat(trace.isReadOperation()).isTrue();
	}

	@Test
	public void callWithoutGrantIsDenied() {
		McpRequestTrace trace = new McpRequestTrace();
		boolean[] executed = new boolean[1];
		McpToolProvider provider = provider("x_query", McpToolScope.READ, (c, a) -> { executed[0] = true; return McpToolResult.of(null, "ok"); });

		McpSchema.CallToolResult result = toolRegistry.call(provider, context(trace, null, "other_query"),
				new McpSchema.CallToolRequest("x_query", Map.of(), null));
		Assertions.assertThat(result.isError()).isTrue();
		Assertions.assertThat(executed[0]).isFalse();
		Assertions.assertThat(trace.getOutcome()).isEqualTo(McpRequestTrace.Outcome.denied);
		Assertions.assertThat(trace.getAuditStatus()).isEqualTo(403);

		McpSchema.CallToolResult noContext = toolRegistry.call(provider, McpTransportContext.EMPTY,
				new McpSchema.CallToolRequest("x_query", null, null));
		Assertions.assertThat(noContext.isError()).isTrue();
	}

	@Test
	public void callToolErrorAndPermissionAndException() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-reg-2");

		McpRequestTrace errorTrace = new McpRequestTrace();
		McpSchema.CallToolResult error = toolRegistry.call(provider("x_modify", McpToolScope.WRITE, (c, a) -> McpToolResult.error("bad value")),
				context(errorTrace, identity, "x_modify"), new McpSchema.CallToolRequest("x_modify", Map.of("action", "update"), null));
		Assertions.assertThat(error.isError()).isTrue();
		Assertions.assertThat(errorTrace.getOutcome()).isEqualTo(McpRequestTrace.Outcome.toolError);
		Assertions.assertThat(errorTrace.getAction()).isEqualTo("update");
		Assertions.assertThat(errorTrace.isReadOperation()).isFalse();

		McpRequestTrace deniedTrace = new McpRequestTrace();
		McpSchema.CallToolResult denied = toolRegistry.call(provider("x_modify", McpToolScope.WRITE, (c, a) -> {
					throw new McpPermissionException("project 'Alpha'", "create ToDos");
				}), context(deniedTrace, identity, "x_modify"), new McpSchema.CallToolRequest("x_modify", Map.of(), null));
		Assertions.assertThat(denied.isError()).isTrue();
		Assertions.assertThat(((McpSchema.TextContent)denied.content().get(0)).text()).contains("create ToDos").contains("project 'Alpha'");
		Assertions.assertThat(deniedTrace.getOutcome()).isEqualTo(McpRequestTrace.Outcome.denied);

		McpRequestTrace invalidTrace = new McpRequestTrace();
		McpSchema.CallToolResult invalid = toolRegistry.call(provider("x_query", McpToolScope.READ, (c, a) -> {
					throw new McpInvalidArgumentException("The argument from must be an ISO date");
				}), context(invalidTrace, identity, "x_query"), new McpSchema.CallToolRequest("x_query", Map.of(), null));
		Assertions.assertThat(invalid.isError()).isTrue();
		Assertions.assertThat(invalidTrace.getOutcome()).isEqualTo(McpRequestTrace.Outcome.toolError);
		Assertions.assertThat(((McpSchema.TextContent)invalid.content().get(0)).text()).isEqualTo("Invalid argument: The argument from must be an ISO date");
		Assertions.assertThat(invalidTrace.getAuditStatus()).isEqualTo(422);
		
		McpRequestTrace failedTrace = new McpRequestTrace();
		McpSchema.CallToolResult failed = toolRegistry.call(provider("x_query", McpToolScope.READ, (c, a) -> {
					throw new IllegalStateException("boom");
				}), context(failedTrace, identity, "x_query"), new McpSchema.CallToolRequest("x_query", Map.of(), null));
		Assertions.assertThat(failed.isError()).isTrue();
		Assertions.assertThat(failedTrace.getOutcome()).isEqualTo(McpRequestTrace.Outcome.serverError);
		Assertions.assertThat(failedTrace.getErrorReference()).isNotBlank();
		Assertions.assertThat(((McpSchema.TextContent)failed.content().get(0)).text()).contains(failedTrace.getErrorReference()).doesNotContain("boom");
	}
	/**
	 * The server of {@link McpServerFactory} runs the tool on the thread that
	 * calls the servlet transport (immediate execution), not on a Reactor
	 * worker, so the thread locals of the request apply to the tool.
	 */
	@Test
	public void toolRunsOnServletThread() throws Exception {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-reg-3");
		Thread[] toolThread = new Thread[1];
		McpToolProvider provider = provider("x_thread_query", McpToolScope.READ, (c, a) -> {
			toolThread[0] = Thread.currentThread();
			return McpToolResult.of(null, "ok");
		});
		McpStatelessServerFeatures.SyncToolSpecification specification = McpStatelessServerFeatures.SyncToolSpecification.builder()
				.tool(McpToolRegistry.toTool(provider))
				.callHandler((transportContext, request) -> toolRegistry.call(provider, transportContext, request))
				.build();

		ObjectMapper objectMapper = new ObjectMapper();
		McpJsonMapper jsonMapper = new JacksonMcpJsonMapper(objectMapper);
		HttpServletStatelessServerTransport transport = McpServerFactory.createTransport(jsonMapper, McpServerFactory.MAX_REQUEST_SIZE);
		McpStatelessSyncServer server = McpServerFactory.createServer(transport, objectMapper, jsonMapper, List.of(specification));
		try {
			MockHttpServletRequest request = new MockHttpServletRequest("POST", McpModule.MCP_ENDPOINT);
			request.setRequestURI(McpModule.MCP_ENDPOINT);
			request.setServletPath(McpModule.MCP_ENDPOINT);
			request.addHeader("Accept", "application/json, text/event-stream");
			request.setContentType("application/json");
			request.setContent("""
					{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{"name":"x_thread_query","arguments":{}}}
					""".getBytes(StandardCharsets.UTF_8));
			McpGrants grants = new McpGrants(Set.of("x_thread_query"), null, List.of("test"));
			request.setAttribute(McpToolRegistry.CTX_TOOL_CONTEXT, new McpToolContext(identity, null, grants, null, null, "127.0.0.1"));
			MockHttpServletResponse response = new MockHttpServletResponse();

			transport.service((ServletRequest)request, (ServletResponse)response);

			Assertions.assertThat(response.getStatus()).isEqualTo(200);
			Assertions.assertThat(response.getContentAsString()).contains("\"ok\"").doesNotContain("\"isError\":true");
			Assertions.assertThat(toolThread[0]).isSameAs(Thread.currentThread());
		} finally {
			server.close();
		}
	}
}
