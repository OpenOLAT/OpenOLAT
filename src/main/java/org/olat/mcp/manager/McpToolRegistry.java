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

import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ThreadLocalUserActivityLoggerInstaller;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.mcp.tool.McpBinaryContent;
import org.olat.mcp.tool.McpInvalidArgumentException;
import org.olat.mcp.tool.McpPermissionException;
import org.olat.mcp.tool.McpToolContext;
import org.olat.mcp.tool.McpToolDefinition;
import org.olat.mcp.tool.McpToolProvider;
import org.olat.mcp.tool.McpToolResult;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.server.McpStatelessServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;

/**
 * Collects every {@link McpToolProvider} bean, validates the names at startup
 * (fail fast on a duplicate or an invalid name), orders the tools by pack and
 * name, maps the definitions to the wire format and wraps every call with the
 * grant check, the activity logger, the transaction discipline and the
 * exception mapping. A provider never sees the SDK.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Service
public class McpToolRegistry implements InitializingBean {

	private static final Logger log = Tracing.createLoggerFor(McpToolRegistry.class);

	/** Key of the {@link McpToolContext} in the transport context */
	public static final String CTX_TOOL_CONTEXT = "org.olat.mcp.toolcontext";
	/** Key of the {@link UserSession} in the transport context */
	public static final String CTX_USER_SESSION = "org.olat.mcp.usersession";

	private static final Pattern NAME_PATTERN = Pattern.compile(McpToolProvider.NAME_PATTERN);
	private static final Pattern PACK_PATTERN = Pattern.compile(McpToolProvider.PACK_PATTERN);

	@Autowired
	private DB dbInstance;
	@Autowired
	private List<McpToolProvider> providerBeans;

	private List<McpToolProvider> providers;
	private Map<String,McpToolProvider> providersByName;

	@Override
	public void afterPropertiesSet() throws Exception {
		List<McpToolProvider> sorted = validateAndSort(providerBeans);
		Map<String,McpToolProvider> byName = new HashMap<>();
		for(McpToolProvider provider:sorted) {
			byName.put(provider.getToolName(), provider);
		}
		providers = List.copyOf(sorted);
		providersByName = Map.copyOf(byName);
		log.info("MCP tool registry ready with {} tools", providers.size());
	}

	/**
	 * Checks the contract of the providers and fails fast: the tool name and
	 * the pack name match their patterns, the names are unique, every tool has
	 * a scope and a definition with an object title, and the query and modify
	 * tools of one object carry the same object title.
	 *
	 * @param providerList The providers
	 * @return The providers ordered by pack, then tool name
	 * @throws IllegalStateException on a violation of the contract
	 */
	static List<McpToolProvider> validateAndSort(List<McpToolProvider> providerList) {
		Map<String,McpToolProvider> byName = new HashMap<>();
		Map<String,McpToolProvider> byObject = new HashMap<>();
		for(McpToolProvider provider:providerList) {
			String name = provider.getToolName();
			if(name == null || !NAME_PATTERN.matcher(name).matches()) {
				throw new IllegalStateException("Invalid MCP tool name '" + name + "' in " + provider.getClass().getName()
						+ ", the pattern is " + McpToolProvider.NAME_PATTERN);
			}
			String pack = provider.getPack();
			if(pack == null || !PACK_PATTERN.matcher(pack).matches()) {
				throw new IllegalStateException("Invalid MCP pack name '" + pack + "' of tool '" + name
						+ "', the pattern is " + McpToolProvider.PACK_PATTERN);
			}
			McpToolDefinition definition = provider.getDefinition();
			if(provider.getScope() == null || definition == null) {
				throw new IllegalStateException("MCP tool '" + name + "' has no scope or definition");
			}
			if(!StringHelper.containsNonWhitespace(definition.objectTitle())) {
				throw new IllegalStateException("MCP tool '" + name + "' has no object title");
			}
			String object = provider.getObject();
			if(!StringHelper.containsNonWhitespace(object)) {
				throw new IllegalStateException("MCP tool '" + name + "' has no object");
			}
			McpToolProvider duplicate = byName.put(name, provider);
			if(duplicate != null) {
				throw new IllegalStateException("Duplicate MCP tool name '" + name + "' in "
						+ duplicate.getClass().getName() + " and " + provider.getClass().getName());
			}
			McpToolProvider partner = byObject.put(pack + "/" + object, provider);
			if(partner != null && !definition.objectTitle().equals(partner.getDefinition().objectTitle())) {
				throw new IllegalStateException("The MCP tools '" + partner.getToolName() + "' and '" + name
						+ "' of the object '" + object + "' have different object titles: '"
						+ partner.getDefinition().objectTitle() + "' and '" + definition.objectTitle() + "'");
			}
		}
		List<McpToolProvider> sorted = new ArrayList<>(providerList);
		sorted.sort(Comparator.comparing(McpToolProvider::getPack).thenComparing(McpToolProvider::getToolName));
		return sorted;
	}

	/**
	 * @return Every registered tool, ordered by pack and name, available or not
	 */
	public List<McpToolProvider> getProviders() {
		return providers;
	}

	/**
	 * @param name A tool name
	 * @return The provider or null
	 */
	public McpToolProvider getProvider(String name) {
		return name == null ? null : providersByName.get(name);
	}

	/**
	 * @return The SDK specifications of every tool, built once per server
	 */
	public List<McpStatelessServerFeatures.SyncToolSpecification> specifications() {
		List<McpStatelessServerFeatures.SyncToolSpecification> specifications = new ArrayList<>(providers.size());
		for(McpToolProvider provider:providers) {
			specifications.add(McpStatelessServerFeatures.SyncToolSpecification.builder()
					.tool(toTool(provider))
					.callHandler((transportContext, request) -> call(provider, transportContext, request))
					.build());
		}
		return specifications;
	}

	/**
	 * @param provider A provider
	 * @return The wire definition of the tool
	 */
	static McpSchema.Tool toTool(McpToolProvider provider) {
		McpToolDefinition definition = provider.getDefinition();
		boolean readOnly = !provider.getScope().isWrite();
		McpSchema.ToolAnnotations annotations = new McpSchema.ToolAnnotations(definition.title(),
				Boolean.valueOf(readOnly),
				Boolean.valueOf(!readOnly && definition.destructive()),
				Boolean.valueOf(definition.idempotent()),
				Boolean.FALSE, null);
		McpSchema.Tool.Builder builder = McpSchema.Tool.builder(provider.getToolName(), definition.inputSchema())
				.title(definition.title())
				.description(definition.description())
				.annotations(annotations);
		if(definition.outputSchema() != null) {
			builder.outputSchema(definition.outputSchema());
		}
		return builder.build();
	}

	/**
	 * The execution wrapper. The server is built with immediate execution, so
	 * the SDK runs it on the servlet request thread, after {@link McpAuthFilter}.
	 * The wrapper installs the activity logger of the user session for the
	 * tool and resets it in its finally block (the filter does not install it).
	 * It commits the database session on success and rolls it back on an
	 * exception, so a failed tool never leaves a half written change for the
	 * commit of the filter.
	 *
	 * @param provider The tool
	 * @param transportContext The context of the request
	 * @param request The validated call
	 * @return The result, never an exception
	 */
	McpSchema.CallToolResult call(McpToolProvider provider, McpTransportContext transportContext, McpSchema.CallToolRequest request) {
		Map<String,Object> arguments = request.arguments() == null ? Map.of() : request.arguments();
		McpRequestTrace trace = transportContext.get(McpRequestTrace.KEY) instanceof McpRequestTrace t ? t : new McpRequestTrace();
		trace.toolCall(provider.getToolName(), provider.getScope(), arguments);

		McpToolContext context = transportContext.get(CTX_TOOL_CONTEXT) instanceof McpToolContext c ? c : null;
		if(context == null || !context.grants().isGranted(provider.getToolName())) {
			trace.setOutcome(McpRequestTrace.Outcome.denied);
			return error(notGrantedText(provider.getToolName()));
		}
		if(!provider.isAvailable()) {
			trace.setOutcome(McpRequestTrace.Outcome.denied);
			return error("Tool " + provider.getToolName() + " is disabled on this OpenOlat instance.");
		}

		UserSession userSession = transportContext.get(CTX_USER_SESSION) instanceof UserSession s ? s : null;
		if(userSession != null) {
			ThreadLocalUserActivityLoggerInstaller.initUserActivityLogger(userSession);
		}
		try {
			McpToolResult result = provider.execute(context, arguments);
			if(result == null) {
				throw new IllegalStateException("Tool " + provider.getToolName() + " returned null");
			}
			dbInstance.commitAndCloseSession();
			trace.setOutcome(result.error() ? McpRequestTrace.Outcome.toolError : McpRequestTrace.Outcome.success);
			return toCallToolResult(result);
		} catch (McpInvalidArgumentException e) {
			dbInstance.rollbackAndCloseSession();
			trace.setOutcome(McpRequestTrace.Outcome.toolError);
			return error(invalidArgumentText(e));
		} catch (McpPermissionException e) {
			dbInstance.rollbackAndCloseSession();
			trace.setOutcome(McpRequestTrace.Outcome.denied);
			log.info("MCP permission denied for identity {} on tool {}: {}", identityKey(context), provider.getToolName(), e.getMessage());
			return error("Permission denied: this account may not " + e.getPermission() + " on " + e.getObjectDescription() + ".");
		} catch (Exception e) {
			dbInstance.rollbackAndCloseSession();
			String reference = UUID.randomUUID().toString();
			trace.setOutcome(McpRequestTrace.Outcome.serverError);
			trace.setErrorReference(reference);
			log.error("MCP tool {} failed for identity {}, reference {}", provider.getToolName(), identityKey(context), reference, e);
			return error("The tool " + provider.getToolName() + " failed with an internal error. "
					+ "Report the reference " + reference + " to the OpenOlat administrator.");
		} finally {
			if(userSession != null) {
				ThreadLocalUserActivityLoggerInstaller.resetUserActivityLogger();
			}
		}
	}

	/**
	 * @param toolName The name of the called tool
	 * @return The text of the error result for a tool without grant, the same
	 *         for an unknown tool so the model learns nothing about other tools
	 */
	static String notGrantedText(String toolName) {
		return "Tool " + toolName + " is not available for this account. Call openolat_whoami to see the granted tools.";
	}

	/**
	 * @param e An invalid argument of a tool
	 * @return The text of the error result the model gets
	 */
	public static String invalidArgumentText(McpInvalidArgumentException e) {
		return "Invalid argument: " + e.getMessage();
	}

	private static Long identityKey(McpToolContext context) {
		return context == null || context.identity() == null ? null : context.identity().getKey();
	}

	static McpSchema.CallToolResult toCallToolResult(McpToolResult result) {
		McpSchema.CallToolResult.Builder builder = McpSchema.CallToolResult.builder()
				.isError(Boolean.valueOf(result.error()))
				.addTextContent(result.text() == null ? "" : result.text());
		if(!result.error() && result.structuredContent() != null) {
			builder.structuredContent(result.structuredContent());
		}
		for(McpBinaryContent attachment:result.attachments()) {
			String base64 = Base64.getEncoder().encodeToString(attachment.data());
			if(McpBinaryContent.isInlineImage(attachment.mimeType())) {
				builder.addContent(new McpSchema.ImageContent(null, base64, attachment.mimeType(), null));
			} else {
				McpSchema.BlobResourceContents blob = new McpSchema.BlobResourceContents(attachment.uri(), attachment.mimeType(), base64, null);
				builder.addContent(new McpSchema.EmbeddedResource(null, blob, null));
			}
		}
		return builder.build();
	}

	private static McpSchema.CallToolResult error(String message) {
		return McpSchema.CallToolResult.builder()
				.isError(Boolean.TRUE)
				.addTextContent(message)
				.build();
	}
}
