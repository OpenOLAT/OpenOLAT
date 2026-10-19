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

import static io.undertow.servlet.Servlets.defaultContainer;
import static io.undertow.servlet.Servlets.deployment;
import static io.undertow.servlet.Servlets.filter;
import static io.undertow.servlet.Servlets.servlet;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletException;

import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.logging.Tracing;
import org.olat.mcp.McpModule;
import org.olat.mcp.McpProfile;
import org.olat.mcp.McpProfileType;
import org.olat.mcp.McpService;
import org.olat.mcp.McpTokenCreation;
import org.olat.mcp.McpTokenService;
import org.olat.mcp.tool.core.WhoAmIToolProvider;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.todo.ToDoModule;
import org.olat.restapi.audit.ApiAuditChannel;
import org.olat.restapi.audit.ApiAuditLog;
import org.olat.restapi.audit.ApiAuditLogSearchParams;
import org.olat.restapi.audit.ApiAuditLogService;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;

/**
 * End-to-end: the MCP client of the SDK against the filter and the servlet
 * in an embedded Undertow, with real tokens, profiles and the audit log.
 * Milestone M1 of the implementation plan.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpEndpointTest extends OlatTestCase {

	private static final Logger log = Tracing.createLoggerFor(McpEndpointTest.class);

	private static final String HOST = "localhost";
	private static final String CONTEXT_PATH = "olat";

	/** A free port per class: a fixed one collides with the other embedded servers of the suite (WebDAV holds 9997). */
	private static int port;
	private static Undertow webServer;
	private static DeploymentManager deploymentManager;

	@Autowired
	private DB dbInstance;
	@Autowired
	private McpModule mcpModule;
	@Autowired
	private McpService mcpService;
	@Autowired
	private McpTokenService tokenService;
	@Autowired
	private ApiAuditLogService auditLogService;
	@Autowired
	private ToDoModule toDoModule;
	@Autowired
	private LectureModule lectureModule;

	private boolean enabledBefore;

	@Before
	public void startServer() {
		if(webServer == null) {
			try {
				port = findFreePort();
				DeploymentInfo servletBuilder = deployment()
				        .setClassLoader(McpEndpointTest.class.getClassLoader())
				        .setContextPath("/" + CONTEXT_PATH)
				        .setDeploymentName("mcp.war")
				        .addServlets(servlet("mcpservlet", McpEndpointServlet.class).addMapping(McpModule.MCP_ENDPOINT))
				        .addFilters(filter("McpAuthFilter", McpAuthFilter.class))
				        .addFilterUrlMapping("McpAuthFilter", McpModule.MCP_ENDPOINT, DispatcherType.REQUEST);

				deploymentManager = defaultContainer().addDeployment(servletBuilder);
				deploymentManager.deploy();
				deploymentManager.getDeployment().getServletContext()
					.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, applicationContext);

				HttpHandler servletHandler = deploymentManager.start();
				PathHandler path = Handlers.path(Handlers.redirect("/" + CONTEXT_PATH))
						.addPrefixPath("/" + CONTEXT_PATH, servletHandler);
				Undertow server = Undertow.builder()
						.addHttpListener(port, HOST)
						.setHandler(path)
						.build();
				server.start();
				webServer = server;
			} catch (ServletException e) {
				log.error("", e);
				throw new IllegalStateException("Cannot deploy the MCP endpoint", e);
			}
		}
		enabledBefore = mcpModule.isEnabled();
		mcpModule.setEnabled(true);
	}

	@After
	public void restoreModule() {
		mcpModule.setEnabled(enabledBefore);
	}

	@AfterClass
	public static void stopServer() {
		try {
			if(webServer != null) {
				webServer.stop();
			}
			if(deploymentManager != null) {
				DeploymentInfo deploymentInfo = deploymentManager.getDeployment().getDeploymentInfo();
				deploymentManager.stop();
				deploymentManager.undeploy();
				defaultContainer().removeDeployment(deploymentInfo);
			}
		} catch (Exception e) {
			log.error("", e);
		} finally {
			webServer = null;
			deploymentManager = null;
		}
	}

	private static int findFreePort() {
		try(ServerSocket socket = new ServerSocket(0, 1, InetAddress.getByName(HOST))) {
			socket.setReuseAddress(true);
			return socket.getLocalPort();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static String baseUrl() {
		return "http://" + HOST + ":" + port;
	}

	private McpSyncClient client(String bearer) {
		HttpClientStreamableHttpTransport transport = HttpClientStreamableHttpTransport
				.builder(baseUrl())
				.endpoint("/" + CONTEXT_PATH + McpModule.MCP_ENDPOINT)
				.requestBuilder(HttpRequest.newBuilder().header("Authorization", "Bearer " + bearer))
				.connectTimeout(Duration.ofSeconds(10))
				.build();
		return McpClient.sync(transport)
				.requestTimeout(Duration.ofSeconds(30))
				.clientInfo(new McpSchema.Implementation("McpEndpointTest", "1"))
				.build();
	}

	private McpTokenCreation tokenWithProfile(Identity identity, String... tools) {
		McpProfile profile = mcpService.createProfile("e2e-" + UUID.randomUUID().toString().substring(0, 12), "E2E test", McpProfileType.user, defaultOrganisations());
		profile.setTools(Set.of(tools));
		profile.setEnabled(true);
		profile = mcpService.updateProfile(profile);
		mcpService.addGrant(profile, identity);
		dbInstance.commitAndCloseSession();
		McpTokenCreation creation = tokenService.generateToken(identity, "e2e");
		dbInstance.commitAndCloseSession();
		return creation;
	}

	private static List<Organisation> defaultOrganisations() {
		return List.of(JunitTestHelper.getDefaultOrganisation());
	}

	@Test
	public void initializeListAndCallWhoAmI() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-e2e-1");
		McpTokenCreation creation = tokenWithProfile(identity, WhoAmIToolProvider.NAME);

		try(McpSyncClient client = client(creation.plaintext())) {
			McpSchema.InitializeResult initialized = client.initialize();
			Assertions.assertThat(initialized.serverInfo().name()).isEqualTo(McpServerFactory.SERVER_NAME);
			Assertions.assertThat(initialized.instructions()).contains("openolat_whoami");

			McpSchema.ListToolsResult tools = client.listTools();
			Assertions.assertThat(tools.tools()).extracting(McpSchema.Tool::name).containsExactly(WhoAmIToolProvider.NAME);
			McpSchema.Tool whoami = tools.tools().get(0);
			Assertions.assertThat(whoami.annotations().readOnlyHint()).isTrue();
			Assertions.assertThat(whoami.outputSchema()).isNotNull();

			McpSchema.CallToolResult result = client.callTool(new McpSchema.CallToolRequest(WhoAmIToolProvider.NAME, Map.of(), null));
			Assertions.assertThat(result.isError()).isFalse();
			Assertions.assertThat(result.structuredContent()).isInstanceOf(Map.class);
			Map<?,?> structured = (Map<?,?>)result.structuredContent();
			Assertions.assertThat(((Number)structured.get("identityKey")).longValue()).isEqualTo(identity.getKey().longValue());
			Assertions.assertThat(structured.get("username")).isIn(identity.getName(), identity.getUser().getNickName());
			Assertions.assertThat((List<?>)structured.get("tools")).hasSize(1).first().isEqualTo(WhoAmIToolProvider.NAME);
			Assertions.assertThat(((McpSchema.TextContent)result.content().get(0)).text()).contains("key " + identity.getKey());
		}

		// a read tool call is not audited by default, the row count stays 0
		ApiAuditLogSearchParams params = new ApiAuditLogSearchParams();
		params.setIdentityKey(identity.getKey());
		params.setChannels(List.of(ApiAuditChannel.mcp));
		List<ApiAuditLog> rows = auditLogService.search(params, 0, 10);
		Assertions.assertThat(rows).isEmpty();
	}

	@Test
	public void whoAmIListsOnlyAvailableTools() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-e2e-who");
		// lecture_query of a disabled module and a tool name the registry does not know
		McpTokenCreation creation = tokenWithProfile(identity, WhoAmIToolProvider.NAME, "lecture_query", "course_query", "no_such_tool");
		boolean lectureEnabledBefore = lectureModule.isEnabled();
		lectureModule.setEnabled(false);
		try(McpSyncClient client = client(creation.plaintext())) {
			client.initialize();
			List<String> listed = client.listTools().tools().stream().map(McpSchema.Tool::name).toList();
			Assertions.assertThat(listed).containsExactly(WhoAmIToolProvider.NAME, "course_query");

			McpSchema.CallToolResult result = client.callTool(new McpSchema.CallToolRequest(WhoAmIToolProvider.NAME, Map.of(), null));
			Assertions.assertThat(result.isError()).isFalse();
			Map<?,?> structured = (Map<?,?>)result.structuredContent();
			// the same tools as tools/list, in the sort order of the names
			List<String> whoAmITools = ((List<?>)structured.get("tools")).stream().map(Object::toString).toList();
			Assertions.assertThat(whoAmITools).containsExactly("course_query", WhoAmIToolProvider.NAME);
			Assertions.assertThat(((McpSchema.TextContent)result.content().get(0)).text())
				.doesNotContain("lecture_query").doesNotContain("no_such_tool");
		} finally {
			lectureModule.setEnabled(lectureEnabledBefore);
		}
	}

	@Test
	public void callOfUngrantedToolIsAnErrorResult() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-e2e-2");
		// the profile grants a tool that does not exist, whoami is not granted
		McpTokenCreation creation = tokenWithProfile(identity, "nothing_query");

		try(McpSyncClient client = client(creation.plaintext())) {
			client.initialize();
			Assertions.assertThat(client.listTools().tools()).isEmpty();
			McpSchema.CallToolResult result = client.callTool(new McpSchema.CallToolRequest(WhoAmIToolProvider.NAME, Map.of(), null));
			Assertions.assertThat(result.isError()).isTrue();
			Assertions.assertThat(((McpSchema.TextContent)result.content().get(0)).text()).contains("not available");
		}

		// a denied call is audited with status 403
		ApiAuditLogSearchParams params = new ApiAuditLogSearchParams();
		params.setIdentityKey(identity.getKey());
		params.setChannels(List.of(ApiAuditChannel.mcp));
		List<ApiAuditLog> rows = auditLogService.search(params, 0, 10);
		Assertions.assertThat(rows).hasSize(1);
		Assertions.assertThat(rows.get(0).getStatus()).isEqualTo(403);
		Assertions.assertThat(rows.get(0).getResourceClass()).isEqualTo(McpSchema.METHOD_TOOLS_CALL);
		Assertions.assertThat(rows.get(0).getResourceMethod()).isEqualTo(WhoAmIToolProvider.NAME);
		Assertions.assertThat(rows.get(0).getPathParams()).contains(creation.token().tokenId());
	}

	@Test
	public void domainToolsOverTheWire() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-e2e-3");
		McpTokenCreation creation = tokenWithProfile(identity, WhoAmIToolProvider.NAME, "course_query", "todo_query", "todo_modify");
		String personalCreate = toDoModule.getPersonalCreate();
		toDoModule.setPersonalCreate(ToDoModule.PERSONAL_CREATE_ALL);
		try(McpSyncClient client = client(creation.plaintext())) {
			client.initialize();
			List<McpSchema.Tool> tools = client.listTools().tools();
			Assertions.assertThat(tools).extracting(McpSchema.Tool::name)
				.containsExactly(WhoAmIToolProvider.NAME, "course_query", "todo_modify", "todo_query");
			// the query output schema defines the item and the person once, the server validates the results against it
			Map<String,Object> queryOutputSchema = tools.get(3).outputSchema();
			Assertions.assertThat(((Map<?,?>)queryOutputSchema.get("$defs")).keySet()).extracting(Object::toString).containsExactlyInAnyOrder("item", "person");
			
			// courses: the fresh user has none
			McpSchema.CallToolResult courses = client.callTool(new McpSchema.CallToolRequest("course_query", Map.of("limit", 5), null));
			Assertions.assertThat(courses.isError()).isFalse();
			Map<?,?> courseList = (Map<?,?>)courses.structuredContent();
			Assertions.assertThat((List<?>)courseList.get("items")).isEmpty();
			Assertions.assertThat(courseList.get("total")).isEqualTo(0);
			
			// create a personal ToDo through the write tool
			String title = "E2E ToDo " + UUID.randomUUID().toString().substring(0, 8);
			McpSchema.CallToolResult created = client.callTool(new McpSchema.CallToolRequest("todo_modify",
					Map.of("action", "create", "context", "personal", "title", title, "dueDate", "2030-01-31"), null));
			Assertions.assertThat(created.isError()).as(((McpSchema.TextContent)created.content().get(0)).text()).isFalse();
			// the modify result is the minimal item, todo_query get returns the full ToDo
			Map<?,?> createdItem = (Map<?,?>)((Map<?,?>)created.structuredContent()).get("item");
			Assertions.assertThat(createdItem.keySet()).extracting(Object::toString).containsExactly("key", "title", "status", "url");
			Assertions.assertThat(createdItem.get("title")).isEqualTo(title);
			Assertions.assertThat(((McpSchema.TextContent)created.content().get(0)).text())
				.startsWith("Created: ").endsWith("Call todo_query get for the full item.");
			
			// and read it back through the query tool
			McpSchema.CallToolResult todos = client.callTool(new McpSchema.CallToolRequest("todo_query", Map.of("context", "personal"), null));
			Assertions.assertThat(todos.isError()).isFalse();
			Map<?,?> todoList = (Map<?,?>)todos.structuredContent();
			Assertions.assertThat((List<?>)todoList.get("items")).hasSize(1);
			Assertions.assertThat(((Map<?,?>)((List<?>)todoList.get("items")).get(0)).get("title")).isEqualTo(title);
			
			// an invalid date is a tool error the model can correct, not an internal error
			McpSchema.CallToolResult invalid = client.callTool(new McpSchema.CallToolRequest("todo_query", Map.of("dueBefore", "tomorrow"), null));
			Assertions.assertThat(invalid.isError()).isTrue();
			Assertions.assertThat(((McpSchema.TextContent)invalid.content().get(0)).text()).doesNotContain("Report the reference");
		} finally {
			toDoModule.setPersonalCreate(personalCreate);
		}
		
		// the write and the failed call are audited, the reads are not
		ApiAuditLogSearchParams params = new ApiAuditLogSearchParams();
		params.setIdentityKey(identity.getKey());
		params.setChannels(List.of(ApiAuditChannel.mcp));
		List<ApiAuditLog> rows = auditLogService.search(params, 0, 10);
		Assertions.assertThat(rows).extracting(ApiAuditLog::getResourceMethod).containsExactlyInAnyOrder("todo_modify", "todo_query");
		Assertions.assertThat(rows).extracting(ApiAuditLog::getStatus).containsExactlyInAnyOrder(200, 422);
	}
	
	@Test
	public void responseCarriesNoSessionCookie() throws Exception {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-e2e-4");
		McpTokenCreation creation = tokenWithProfile(identity, WhoAmIToolProvider.NAME);
		java.net.http.HttpClient http = java.net.http.HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder(java.net.URI.create(baseUrl() + "/" + CONTEXT_PATH + McpModule.MCP_ENDPOINT))
				.header("Accept", "application/json, text/event-stream")
				.header("Content-Type", "application/json")
				.header("Authorization", "Bearer " + creation.plaintext())
				.POST(java.net.http.HttpRequest.BodyPublishers.ofString("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"name\":\"openolat_whoami\",\"arguments\":{}}}"))
				.build();
		java.net.http.HttpResponse<String> response = http.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
		Assertions.assertThat(response.statusCode()).isEqualTo(200);
		Assertions.assertThat(response.body()).contains("\"identityKey\":" + identity.getKey());
		// the MCP identity is never bound to a container session, no cookie may leave the server
		Assertions.assertThat(response.headers().allValues("Set-Cookie")).isEmpty();
	}
	
	@Test
	public void invalidTokenFailsTheInitialize() {
		try(McpSyncClient client = client("oomcp_" + UUID.randomUUID())) {
			Assertions.assertThatThrownBy(client::initialize).isInstanceOf(RuntimeException.class);
		}
	}
}
