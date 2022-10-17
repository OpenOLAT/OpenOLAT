/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.restapi.system;

import static org.olat.restapi.security.RestSecurityHelper.getRoles;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.restapi.security.RestApiLoginFilter;
import org.olat.restapi.security.RestSecurityHelper;
import org.olat.restapi.system.vo.EnvironmentInformationsVO;
import org.olat.restapi.system.vo.ReleaseInfosVO;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 
 * <h3>Description:</h3>
 * <p>
 * Initial Date:  18 jun. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
@Tag(name = "System")
@Path("system")
@Component
public class SystemWebService {
	
	@Path("log")
	public LogWebService getLogsWS(@Context HttpServletRequest request) {
		if(!isAdminOrSystemAdmin(request)) {
			return null;
		}
		return new LogWebService();
	}
	
	/**
	 * Return some informations about the environment.
	 *
   * @param request The HTTP request
	 * @return The informations about the environment
	 */
	@GET
	@Path("environment")
	@Operation(summary = "Return some informations about the environment", description = "Return some informations about the environment")
	@ApiResponse(responseCode = "200", description = "A short summary of the number of classes", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = EnvironmentInformationsVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = EnvironmentInformationsVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getEnvironnementXml(@Context HttpServletRequest request) {
		if(!isAdminOrSystemAdmin(request)) {
			return null;
		}
		OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
		RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
		EnvironmentInformationsVO vo = new EnvironmentInformationsVO(os, runtime);
		return Response.ok(vo).build();
	}
	
	/**
	 * Return the version of the instance.
	 *
   * @param request The HTTP request
	 * @return The informations about the memory
	 */
	@GET
	@Path("release")
	@Operation(summary = "Return the version of the instance", description = "Return the version of the instance")
	@ApiResponse(responseCode = "200", description = "The version of the instance", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = ReleaseInfosVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = ReleaseInfosVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getReleaseInfos(@Context HttpServletRequest request) {
		if(!isAdminOrSystemAdmin(request)) {
			return null;
		}

		ReleaseInfosVO version = new ReleaseInfosVO();
		if(StringHelper.containsNonWhitespace(WebappHelper.getRevisionNumber())) {
			String v = WebappHelper.getRevisionNumber() + ":" + WebappHelper.getChangeSet();
			version.setBuildVersion(v);
			version.setRepoRevision(v);
		} else {
			version.setBuildVersion(Settings.getBuildIdentifier());
			version.setRepoRevision(Settings.getRepoRevision());
		}
		version.setOlatVersion(Settings.getVersion());
		version.setInstanceID(WebappHelper.getInstanceId());
		return Response.ok(version).build();
	}
	
	@Path("monitoring")
	@Operation(summary = "Return the version of the instance", description = "Return the version of the instance")
	@ApiResponse(responseCode = "200", description = "The version of the instance", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = MonitoringWebService.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = MonitoringWebService.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	public MonitoringWebService getImplementedProbes(@Context HttpServletRequest request) {
		if(!isMonitoringEnabled() && !isAdminOrSystemAdmin(request)) {
			return null;
		}
		return new MonitoringWebService();
	}
	
	@Path("indexer")
	@Operation(summary = "Return the version of the instance", description = "Return the version of the instance")
	@ApiResponse(responseCode = "200", description = "The version of the instance", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = IndexerWebService.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = IndexerWebService.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	public IndexerWebService getIndexer(@Context HttpServletRequest request) {
		if(!isAdminOrSystemAdmin(request)) {
			return null;
		}
		return new IndexerWebService();
	}
	
	@Path("notifications")
	@Operation(summary = "Return the version of the instance", description = "Return the version of the instance")
	@ApiResponse(responseCode = "200", description = "The version of the instance", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = NotificationsAdminWebService.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = NotificationsAdminWebService.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	public NotificationsAdminWebService getNotifications(@Context HttpServletRequest request) {
		if(!isAdminOrSystemAdmin(request)) {
			return null;
		}
		return new NotificationsAdminWebService();
	}
	
	private boolean isMonitoringEnabled() {
		MonitoringModule module = CoreSpringFactory.getImpl(MonitoringModule.class);
		return module.isEnabled();
	}
	
	private boolean isAdminOrSystemAdmin(HttpServletRequest request) {
		try {
			Roles roles = getRoles(request);
			if(roles.isAdministrator()) {
				return true;
			}
			UserRequest ureq = (UserRequest)request.getAttribute(RestSecurityHelper.SEC_USER_REQUEST);
			return ureq != null && ureq.getUserSession() != null
					&& ureq.getUserSession().getEntry(RestApiLoginFilter.SYSTEM_MARKER) != null;
		} catch (Exception e) {
			return false;
		}
	}
}