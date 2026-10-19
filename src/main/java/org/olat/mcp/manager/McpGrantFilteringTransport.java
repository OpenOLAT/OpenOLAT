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

import org.olat.mcp.McpGrants;
import org.olat.mcp.tool.McpToolContext;
import org.olat.mcp.tool.McpToolProvider;

import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.server.McpStatelessServerHandler;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpStatelessServerTransport;
import reactor.core.publisher.Mono;

/**
 * The MCP Java SDK 2.0.1 has no per-request tool filter (it exists on the SDK
 * main branch only). This decorator wraps the stateless transport, intercepts
 * the handler the server installs and rewrites the result of tools/list to
 * the tools granted to the caller. The order of the tools is the order of the
 * registry (pack, then name), so tools/list is deterministic. It also records
 * the JSON-RPC method in the request trace for the audit row. When the SDK
 * ships its filter, this class is replaced by one builder call.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpGrantFilteringTransport implements McpStatelessServerTransport {

	private final McpStatelessServerTransport delegate;
	private final McpToolRegistry toolRegistry;

	/**
	 * @param delegate The transport of the SDK
	 * @param toolRegistry The registry, null hides nothing but the ungranted tools
	 */
	public McpGrantFilteringTransport(McpStatelessServerTransport delegate, McpToolRegistry toolRegistry) {
		this.delegate = delegate;
		this.toolRegistry = toolRegistry;
	}

	@Override
	public void setMcpHandler(McpStatelessServerHandler handler) {
		delegate.setMcpHandler(new McpStatelessServerHandler() {
			@Override
			public Mono<McpSchema.JSONRPCResponse> handleRequest(McpTransportContext transportContext, McpSchema.JSONRPCRequest request) {
				traceMethod(transportContext, request.method());
				if(McpSchema.METHOD_TOOLS_CALL.equals(request.method())) {
					McpSchema.JSONRPCResponse denied = denyUngrantedCall(transportContext, request);
					if(denied != null) {
						return Mono.just(denied);
					}
				}
				Mono<McpSchema.JSONRPCResponse> response = handler.handleRequest(transportContext, request)
						.map(r -> traceError(transportContext, r));
				if(McpSchema.METHOD_TOOLS_LIST.equals(request.method())) {
					return response.map(r -> filterTools(transportContext, r, toolRegistry));
				}
				if(McpSchema.METHOD_TOOLS_CALL.equals(request.method())) {
					return response.map(r -> traceToolError(transportContext, r));
				}
				return response;
			}

			@Override
			public Mono<Void> handleNotification(McpTransportContext transportContext, McpSchema.JSONRPCNotification notification) {
				traceMethod(transportContext, notification.method());
				return handler.handleNotification(transportContext, notification);
			}
		});
	}

	/**
	 * The grant check before the SDK touches the call: an ungranted or
	 * unavailable tool gets the uniform error result without the SDK revealing
	 * its input schema through the validation message. The trace records the
	 * tool name and the arguments for the audit row in every case.
	 * 
	 * @param transportContext The context of the request
	 * @param request The tools/call request
	 * @return The error response, or null when the call may proceed
	 */
	@SuppressWarnings("unchecked")
	private McpSchema.JSONRPCResponse denyUngrantedCall(McpTransportContext transportContext, McpSchema.JSONRPCRequest request) {
		if(!(request.params() instanceof Map<?,?> params)) {
			return null;
		}
		String name = params.get("name") instanceof String n ? n : null;
		Map<String,Object> arguments = params.get("arguments") instanceof Map<?,?> args ? (Map<String,Object>)args : Map.of();
		McpToolProvider provider = toolRegistry == null ? null : toolRegistry.getProvider(name);
		McpRequestTrace trace = transportContext != null && transportContext.get(McpRequestTrace.KEY) instanceof McpRequestTrace t ? t : null;
		if(trace != null) {
			trace.toolCall(name, provider == null ? null : provider.getScope(), arguments);
		}
		boolean granted = name != null && grantsOf(transportContext).isGranted(name) && (provider == null || provider.isAvailable());
		if(granted) {
			return null;
		}
		if(trace != null) {
			trace.setOutcome(McpRequestTrace.Outcome.denied);
		}
		McpSchema.CallToolResult result = McpSchema.CallToolResult.builder()
				.isError(Boolean.TRUE)
				.addTextContent(McpToolRegistry.notGrantedText(name))
				.build();
		return new McpSchema.JSONRPCResponse(McpSchema.JSONRPC_VERSION, request.id(), result, null);
	}
	
	/**
	 * A tool result with isError that the registry did not produce (the SDK
	 * input validation) is a failed call for the audit row.
	 */
	private static McpSchema.JSONRPCResponse traceToolError(McpTransportContext transportContext, McpSchema.JSONRPCResponse response) {
		if(response != null && response.result() instanceof McpSchema.CallToolResult result
				&& Boolean.TRUE.equals(result.isError())
				&& transportContext != null && transportContext.get(McpRequestTrace.KEY) instanceof McpRequestTrace trace
				&& trace.getOutcome() == McpRequestTrace.Outcome.success) {
			trace.setOutcome(McpRequestTrace.Outcome.toolError);
		}
		return response;
	}
	
	/**
	 * A JSON-RPC error (unknown tool, invalid arguments, unknown method) is a
	 * failed call for the audit row, the status class is 422.
	 */
	private static McpSchema.JSONRPCResponse traceError(McpTransportContext transportContext, McpSchema.JSONRPCResponse response) {
		if(response != null && response.error() != null && transportContext != null
				&& transportContext.get(McpRequestTrace.KEY) instanceof McpRequestTrace trace
				&& trace.getOutcome() == McpRequestTrace.Outcome.success) {
			trace.setOutcome(McpRequestTrace.Outcome.toolError);
		}
		return response;
	}
	
	private static void traceMethod(McpTransportContext transportContext, String method) {
		if(transportContext != null && transportContext.get(McpRequestTrace.KEY) instanceof McpRequestTrace trace) {
			trace.setJsonRpcMethod(method);
		}
	}

	/**
	 * @param transportContext The context of the request
	 * @param response The response of the server
	 * @param toolRegistry The registry to hide the tools of disabled modules, may be null
	 * @return The response with the list reduced to the granted and available tools
	 */
	static McpSchema.JSONRPCResponse filterTools(McpTransportContext transportContext, McpSchema.JSONRPCResponse response, McpToolRegistry toolRegistry) {
		if(!(response.result() instanceof McpSchema.ListToolsResult listResult)) {
			return response;
		}
		McpGrants grants = grantsOf(transportContext);
		List<McpSchema.Tool> visible = listResult.tools().stream()
				.filter(tool -> grants.isGranted(tool.name()))
				.filter(tool -> isAvailable(toolRegistry, tool.name()))
				.toList();
		McpSchema.ListToolsResult filtered = new McpSchema.ListToolsResult(visible, listResult.nextCursor(), listResult.meta());
		return new McpSchema.JSONRPCResponse(response.jsonrpc(), response.id(), filtered, response.error());
	}

	private static boolean isAvailable(McpToolRegistry toolRegistry, String toolName) {
		if(toolRegistry == null) {
			return true;
		}
		McpToolProvider provider = toolRegistry.getProvider(toolName);
		return provider != null && provider.isAvailable();
	}
	
	static McpGrants grantsOf(McpTransportContext transportContext) {
		if(transportContext != null && transportContext.get(McpToolRegistry.CTX_TOOL_CONTEXT) instanceof McpToolContext toolContext
				&& toolContext.grants() != null) {
			return toolContext.grants();
		}
		return McpGrants.EMPTY;
	}

	@Override
	public Mono<Void> closeGracefully() {
		return delegate.closeGracefully();
	}

	@Override
	public List<String> protocolVersions() {
		return delegate.protocolVersions();
	}
}
