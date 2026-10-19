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
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.mcp.McpModule;
import org.olat.mcp.McpTokenService;
import org.olat.restapi.audit.ApiAuditChannel;
import org.olat.restapi.audit.ApiAuditEntry;
import org.olat.restapi.audit.ApiAuditLogService;
import org.olat.restapi.audit.manager.ApiAuditMasking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Builds the audit row of an MCP request from the trace and hands it to the
 * shared audit log service in a transaction of its own, after the response.
 * Mapping: resource class = JSON-RPC method, resource method =
 * tool name, path params = action, profiles and token id, request body =
 * masked tool arguments, status = HTTP status of a rejected request or the
 * outcome of the tool call.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpAuditWriter {

	private static final Logger log = Tracing.createLoggerFor(McpAuditWriter.class);

	private static final ObjectMapper mapper = new ObjectMapper();

	private final McpModule mcpModule;
	private final ApiAuditLogService auditLogService;

	public McpAuditWriter(McpModule mcpModule, ApiAuditLogService auditLogService) {
		this.mcpModule = mcpModule;
		this.auditLogService = auditLogService;
	}

	/**
	 * Writes the row. Never throws.
	 *
	 * @param request The request
	 * @param response The response with the final status
	 * @param trace The trace, may be null for a request rejected before the transport
	 * @param entry The identity entry, null for a rejected request
	 */
	public void audit(HttpServletRequest request, HttpServletResponse response, McpRequestTrace trace, McpIdentityEntry entry) {
		try {
			if(request.getAttribute(ApiAuditLogService.REQ_ATTR_DONE) != null) {
				return;
			}
			auditLogService.logInNewTransaction(toEntry(request, response, trace, entry));
		} catch (Exception e) {
			log.error("Cannot audit the MCP request", e);
		}
	}

	/**
	 * @param request The request
	 * @param response The response
	 * @param trace The trace or null
	 * @param entry The identity entry or null
	 * @return The audit entry
	 */
	ApiAuditEntry toEntry(HttpServletRequest request, HttpServletResponse response, McpRequestTrace trace, McpIdentityEntry entry) {
		int status = response.getStatus();
		if(status == HttpServletResponse.SC_OK && trace != null) {
			status = trace.getAuditStatus();
		}
		ApiAuditEntry auditEntry = ApiAuditEntry.valueOf(request, status, ApiAuditChannel.mcp);
		auditEntry.setAuthProvider(entry == null ? ApiAuditLogService.AUTH_NONE : McpTokenService.MCP_AUTH_PROVIDER);
		if(entry != null) {
			auditEntry.setIdentity(entry.getIdentity());
		}
		if(trace != null) {
			auditEntry.setResourceClass(trace.getJsonRpcMethod());
			auditEntry.setResourceMethod(trace.getToolName());
			auditEntry.setPathParams(pathParams(trace, entry));
			auditEntry.setReadOperation(Boolean.valueOf(trace.isReadOperation()));
			// MCP has no REST user request, the reference of a server error links the row and the access line to the error log
			if(trace.getErrorReference() != null) {
				auditEntry.setRef(trace.getErrorReference());
			}
			// build the body only for a row the audit log stores
			boolean stored = mcpModule.isAuditLogEnabled() && mcpModule.isAuditLogBody()
					&& auditLogService.isAuditable(auditEntry);
			auditEntry.setRequestBody(stored ? arguments(trace) : null);
		}
		return auditEntry;
	}

	private String pathParams(McpRequestTrace trace, McpIdentityEntry entry) {
		Map<String,Object> params = new LinkedHashMap<>();
		if(trace.getAction() != null) {
			params.put("action", trace.getAction());
		}
		if(entry != null) {
			params.put("profiles", entry.grants().profileIdentifiers());
			params.put("token", entry.token().tokenId());
		}
		if(trace.getErrorReference() != null) {
			params.put("errorReference", trace.getErrorReference());
		}
		return params.isEmpty() ? null : toJson(params);
	}

	/**
	 * The arguments of the trace are a copy with every long string already
	 * cut (see {@link McpRequestTrace#MAX_ARGUMENT_LENGTH}). The copy is
	 * masked as a tree, serialized once and cut to the maximum body size.
	 *
	 * @param trace The trace
	 * @return The masked and bounded JSON or null
	 */
	String arguments(McpRequestTrace trace) {
		if(trace.getArguments() == null || trace.getArguments().isEmpty()) {
			return null;
		}
		String json;
		try {
			JsonNode tree = mapper.valueToTree(trace.getArguments());
			ApiAuditMasking.maskNode(tree);
			json = mapper.writeValueAsString(tree);
		} catch (IllegalArgumentException | JsonProcessingException e) {
			log.error("", e);
			return null;
		}
		byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
		int max = mcpModule.getAuditLogBodyMaxSize();
		if(max > 0 && bytes.length > max) {
			int cut = max;
			// do not cut inside a multi-byte sequence: step back over continuation bytes
			while(cut > 0 && (bytes[cut] & 0xC0) == 0x80) {
				cut--;
			}
			return new String(bytes, 0, cut, StandardCharsets.UTF_8);
		}
		return json;
	}

	private static String toJson(Object value) {
		try {
			return mapper.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			log.error("", e);
			return null;
		}
	}
}
