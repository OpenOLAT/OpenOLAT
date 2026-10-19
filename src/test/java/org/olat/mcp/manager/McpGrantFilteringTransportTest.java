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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.olat.mcp.McpGrants;
import org.olat.mcp.tool.McpToolContext;

import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.server.McpStatelessServerHandler;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpStatelessServerTransport;
import reactor.core.publisher.Mono;

/**
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpGrantFilteringTransportTest {

	private static McpSchema.Tool tool(String name) {
		return McpSchema.Tool.builder(name, Map.of("type", "object")).description("Tool " + name).build();
	}

	private static McpTransportContext contextWithGrants(String... grantedTools) {
		McpGrants grants = new McpGrants(Set.of(grantedTools), null, List.of("p"));
		McpToolContext toolContext = new McpToolContext(null, null, grants, null, null, "127.0.0.1");
		return McpTransportContext.create(Map.of(McpToolRegistry.CTX_TOOL_CONTEXT, toolContext));
	}

	@Test
	public void filterToolsKeepsGrantedOnly() {
		McpSchema.ListToolsResult list = new McpSchema.ListToolsResult(List.of(tool("a_query"), tool("b_query"), tool("c_modify")), null, null);
		McpSchema.JSONRPCResponse response = new McpSchema.JSONRPCResponse("2.0", 1, list, null);

		McpSchema.JSONRPCResponse filtered = McpGrantFilteringTransport.filterTools(contextWithGrants("c_modify", "a_query"), response, null);
		McpSchema.ListToolsResult result = (McpSchema.ListToolsResult)filtered.result();
		Assertions.assertThat(result.tools()).extracting(McpSchema.Tool::name).containsExactly("a_query", "c_modify");
		Assertions.assertThat(filtered.id()).isEqualTo(1);
	}

	@Test
	public void filterToolsWithoutContextHidesEverything() {
		McpSchema.ListToolsResult list = new McpSchema.ListToolsResult(List.of(tool("a_query")), null, null);
		McpSchema.JSONRPCResponse response = new McpSchema.JSONRPCResponse("2.0", 2, list, null);

		McpSchema.JSONRPCResponse filtered = McpGrantFilteringTransport.filterTools(McpTransportContext.EMPTY, response, null);
		Assertions.assertThat(((McpSchema.ListToolsResult)filtered.result()).tools()).isEmpty();

		McpSchema.JSONRPCResponse filteredNull = McpGrantFilteringTransport.filterTools(null, response, null);
		Assertions.assertThat(((McpSchema.ListToolsResult)filteredNull.result()).tools()).isEmpty();
	}

	@Test
	public void filterToolsLeavesOtherResultsUntouched() {
		McpSchema.JSONRPCResponse response = new McpSchema.JSONRPCResponse("2.0", 3, Map.of("x", "y"), null);
		McpSchema.JSONRPCResponse filtered = McpGrantFilteringTransport.filterTools(contextWithGrants(), response, null);
		Assertions.assertThat(filtered).isSameAs(response);
	}

	@Test
	public void handlerDecoratorFiltersListAndTracesMethod() {
		McpSchema.ListToolsResult list = new McpSchema.ListToolsResult(List.of(tool("a_query"), tool("b_query")), null, null);
		McpStatelessServerHandler original = new McpStatelessServerHandler() {
			@Override
			public Mono<McpSchema.JSONRPCResponse> handleRequest(McpTransportContext transportContext, McpSchema.JSONRPCRequest request) {
				return Mono.just(new McpSchema.JSONRPCResponse("2.0", request.id(), list, null));
			}
			@Override
			public Mono<Void> handleNotification(McpTransportContext transportContext, McpSchema.JSONRPCNotification notification) {
				return Mono.empty();
			}
		};
		McpStatelessServerHandler[] installed = new McpStatelessServerHandler[1];
		McpStatelessServerTransport delegate = new McpStatelessServerTransport() {
			@Override
			public void setMcpHandler(McpStatelessServerHandler mcpHandler) {
				installed[0] = mcpHandler;
			}
			@Override
			public Mono<Void> closeGracefully() {
				return Mono.empty();
			}
		};
		new McpGrantFilteringTransport(delegate, null).setMcpHandler(original);
		Assertions.assertThat(installed[0]).isNotNull().isNotSameAs(original);

		McpRequestTrace trace = new McpRequestTrace();
		McpGrants grants = new McpGrants(Set.of("b_query"), null, List.of("p"));
		McpToolContext toolContext = new McpToolContext(null, null, grants, null, null, "127.0.0.1");
		McpTransportContext context = McpTransportContext.create(Map.of(
				McpToolRegistry.CTX_TOOL_CONTEXT, toolContext, McpRequestTrace.KEY, trace));

		McpSchema.JSONRPCRequest request = new McpSchema.JSONRPCRequest("2.0", McpSchema.METHOD_TOOLS_LIST, 7, null);
		McpSchema.JSONRPCResponse response = installed[0].handleRequest(context, request).block();
		Assertions.assertThat(((McpSchema.ListToolsResult)response.result()).tools()).extracting(McpSchema.Tool::name).containsExactly("b_query");
		Assertions.assertThat(trace.getJsonRpcMethod()).isEqualTo(McpSchema.METHOD_TOOLS_LIST);
		Assertions.assertThat(trace.getOutcome()).isEqualTo(McpRequestTrace.Outcome.success);
	}
	
	@Test
	public void jsonRpcErrorIsTracedAsFailedCall() {
		McpStatelessServerHandler original = new McpStatelessServerHandler() {
			@Override
			public Mono<McpSchema.JSONRPCResponse> handleRequest(McpTransportContext transportContext, McpSchema.JSONRPCRequest request) {
				return Mono.just(new McpSchema.JSONRPCResponse("2.0", request.id(), null,
						new McpSchema.JSONRPCResponse.JSONRPCError(-32602, "Unknown tool", null)));
			}
			@Override
			public Mono<Void> handleNotification(McpTransportContext transportContext, McpSchema.JSONRPCNotification notification) {
				return Mono.empty();
			}
		};
		McpStatelessServerHandler[] installed = new McpStatelessServerHandler[1];
		McpStatelessServerTransport delegate = new McpStatelessServerTransport() {
			@Override
			public void setMcpHandler(McpStatelessServerHandler mcpHandler) {
				installed[0] = mcpHandler;
			}
			@Override
			public Mono<Void> closeGracefully() {
				return Mono.empty();
			}
		};
		new McpGrantFilteringTransport(delegate, null).setMcpHandler(original);
		McpRequestTrace trace = new McpRequestTrace();
		McpTransportContext context = McpTransportContext.create(Map.of(McpRequestTrace.KEY, trace));
		McpSchema.JSONRPCResponse response = installed[0]
				.handleRequest(context, new McpSchema.JSONRPCRequest("2.0", McpSchema.METHOD_TOOLS_CALL, 9, null)).block();
		Assertions.assertThat(response.error()).isNotNull();
		Assertions.assertThat(trace.getJsonRpcMethod()).isEqualTo(McpSchema.METHOD_TOOLS_CALL);
		Assertions.assertThat(trace.getOutcome()).isEqualTo(McpRequestTrace.Outcome.toolError);
		Assertions.assertThat(trace.getAuditStatus()).isEqualTo(422);
	}
}
