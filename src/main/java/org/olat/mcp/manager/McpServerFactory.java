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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.mcp.McpModule;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpStatelessServerFeatures;
import io.modelcontextprotocol.server.McpStatelessSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStatelessServerTransport;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpStatelessServerTransport;

/**
 * Builds the one stateless MCP server of this node at startup: the servlet
 * transport of the SDK (POST only, JSON only, no session), the grant
 * filtering decorator and the server with every tool of the registry. The
 * filter puts the tool context, the user session and the request trace into
 * request attributes, the context extractor copies them into the transport
 * context of the SDK, where the decorator and the tool wrapper read them.
 * The server runs the tools on the servlet request thread (immediate
 * execution), see {@link #createServer}.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Service
public class McpServerFactory implements InitializingBean, DisposableBean {

	private static final Logger log = Tracing.createLoggerFor(McpServerFactory.class);

	/** Default of mcp.request.maxsize: requests above this size are refused by the transport, 10 MiB */
	public static final int MAX_REQUEST_SIZE = 10 * 1024 * 1024;

	public static final String SERVER_NAME = "OpenOlat";
	public static final String INSTRUCTIONS = "This server exposes the OpenOlat learning management system. "
			+ "Call openolat_whoami first to learn which account you act for and which tools are granted. "
			+ "Every tool acts for that account only. Tool results carry stable keys and URLs into OpenOlat.";

	@Autowired
	private McpModule mcpModule;
	@Autowired
	private McpToolRegistry toolRegistry;

	private HttpServletStatelessServerTransport transport;
	private McpStatelessSyncServer server;

	@Override
	public void afterPropertiesSet() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		McpJsonMapper jsonMapper = new JacksonMcpJsonMapper(objectMapper);
		int maxRequestSize = mcpModule.getRequestMaxSize() > 0 ? mcpModule.getRequestMaxSize() : MAX_REQUEST_SIZE;
		transport = createTransport(jsonMapper, maxRequestSize);
		server = createServer(new McpGrantFilteringTransport(transport, toolRegistry), objectMapper, jsonMapper,
				toolRegistry.specifications());
		log.info("MCP server ready on {} with {} tools", McpModule.MCP_ENDPOINT, toolRegistry.getProviders().size());
	}

	/**
	 * @param jsonMapper The JSON mapper
	 * @param maxRequestSize The maximum size of a request in bytes
	 * @return The stateless servlet transport of the SDK
	 */
	static HttpServletStatelessServerTransport createTransport(McpJsonMapper jsonMapper, int maxRequestSize) {
		return HttpServletStatelessServerTransport.builder()
				.jsonMapper(jsonMapper)
				.messageEndpoint(McpModule.MCP_ENDPOINT)
				.contextExtractor(McpServerFactory::extractContext)
				.maxRequestSize(maxRequestSize)
				.build();
	}

	/**
	 * Builds the server with immediate execution: the SDK does not offload the
	 * synchronous tool handlers to a Reactor worker (boundedElastic), the tool
	 * runs on the servlet request thread, which blocks on the result anyway.
	 * The thread locals of the request (activity logger, database session,
	 * request tracing) are therefore the ones of the tool.
	 *
	 * @param transport The transport, decorated or not
	 * @param objectMapper The object mapper of the schema validator
	 * @param jsonMapper The JSON mapper
	 * @param tools The tool specifications
	 * @return The server
	 */
	static McpStatelessSyncServer createServer(McpStatelessServerTransport transport, ObjectMapper objectMapper,
			McpJsonMapper jsonMapper, List<McpStatelessServerFeatures.SyncToolSpecification> tools) {
		return McpServer.sync(transport)
				.serverInfo(SERVER_NAME, Settings.getVersion())
				.instructions(INSTRUCTIONS)
				.capabilities(McpSchema.ServerCapabilities.builder().tools(Boolean.FALSE).build())
				.jsonMapper(jsonMapper)
				.jsonSchemaValidator(new McpJsonSchemaValidator(objectMapper))
				.immediateExecution(true)
				.tools(tools)
				.build();
	}

	@Override
	public void destroy() throws Exception {
		if(server != null) {
			server.close();
		}
	}

	/**
	 * Copies the attributes the filter set into the transport context.
	 *
	 * @param request The current request
	 * @return The context for the decorator and the tool wrapper
	 */
	static McpTransportContext extractContext(HttpServletRequest request) {
		Map<String,Object> context = new HashMap<>();
		copyAttribute(request, context, McpToolRegistry.CTX_TOOL_CONTEXT);
		copyAttribute(request, context, McpToolRegistry.CTX_USER_SESSION);
		copyAttribute(request, context, McpRequestTrace.KEY);
		return McpTransportContext.create(context);
	}

	private static void copyAttribute(HttpServletRequest request, Map<String,Object> context, String key) {
		Object value = request.getAttribute(key);
		if(value != null) {
			context.put(key, value);
		}
	}

	/**
	 * Hands the request to the transport. The transport is a servlet; its
	 * public service method dispatches to doPost and doGet.
	 *
	 * @param request The request
	 * @param response The response
	 */
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		transport.service((ServletRequest)request, (ServletResponse)response);
	}
}
