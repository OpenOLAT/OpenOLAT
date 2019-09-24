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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
	 * @response.representation.200.qname {http://www.example.com}environmentVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc A short summary of the number of classes
   * @response.representation.200.example {@link org.olat.restapi.system.vo.Examples#SAMPLE_ENVVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
   * @param request The HTTP request
	 * @return The informations about the environment
	 */
	@GET
	@Path("environment")
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
	 * @response.representation.200.qname {http://www.example.com}releaseVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc The verison of the instance
   * @response.representation.200.example {@link org.olat.restapi.system.vo.Examples#SAMPLE_RELEASEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
   * @param request The HTTP request
	 * @return The informations about the memory
	 */
	@GET
	@Path("release")
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
	public MonitoringWebService getImplementedProbes(@Context HttpServletRequest request) {
		if(!isMonitoringEnabled() && !isAdminOrSystemAdmin(request)) {
			return null;
		}
		return new MonitoringWebService();
	}
	
	@Path("indexer")
	public IndexerWebService getIndexer(@Context HttpServletRequest request) {
		if(!isAdminOrSystemAdmin(request)) {
			return null;
		}
		return new IndexerWebService();
	}
	
	@Path("notifications")
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