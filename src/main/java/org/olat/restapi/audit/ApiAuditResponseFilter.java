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
package org.olat.restapi.audit;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Priority;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.logging.Tracing;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Writes the audit row of every request which reached a web service. It runs
 * before {@link org.olat.restapi.security.OpenOLATContainerResponseFilter}:
 * response filters run in descending order of their priority, so the row is
 * written in the transaction of the change, before the commit.
 * <p>
 * Requests which never reach a web service, and rows whose transaction failed,
 * are written by RestApiLoginFilter after the commit.
 * 
 * Initial date: 16 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
@Provider
@Priority(Priorities.USER + 1000)
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.WILDCARD)
public class ApiAuditResponseFilter implements ContainerResponseFilter {
	
	private static final Logger log = Tracing.createLoggerFor(ApiAuditResponseFilter.class);
	
	private static final ObjectMapper mapper = new ObjectMapper();
	
	@Context
	private ResourceInfo resourceInfo;
	@Context
	private HttpServletRequest httpRequest;
	
	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {
		try {
			if(httpRequest == null || httpRequest.getAttribute(ApiAuditLogService.REQ_ATTR_DONE) != null) {
				return;
			}
			
			int status = responseContext.getStatus();
			ApiAuditEntry entry = ApiAuditEntry.valueOf(httpRequest, status, ApiAuditChannel.rest);
			appendResource(entry);
			appendPathParameters(entry, requestContext);
			
			DB dbInstance = DBFactory.getInstance();
			if(status >= 500 || dbInstance.isError()) {
				// the transaction of the request is lost, the servlet filter writes
				// the row in a transaction of its own after the rollback
				httpRequest.setAttribute(ApiAuditLogService.REQ_ATTR_PENDING, entry);
			} else {
				CoreSpringFactory.getImpl(ApiAuditLogService.class).log(entry);
			}
			httpRequest.setAttribute(ApiAuditLogService.REQ_ATTR_DONE, Boolean.TRUE);
		} catch (Exception e) {
			log.error("Cannot audit the API request", e);
		}
	}
	
	private void appendResource(ApiAuditEntry entry) {
		if(resourceInfo == null) {
			return;
		}
		if(resourceInfo.getResourceClass() != null) {
			entry.setResourceClass(resourceInfo.getResourceClass().getSimpleName());
		}
		if(resourceInfo.getResourceMethod() != null) {
			entry.setResourceMethod(resourceInfo.getResourceMethod().getName());
		}
	}
	
	private void appendPathParameters(ApiAuditEntry entry, ContainerRequestContext requestContext) throws IOException {
		if(requestContext.getUriInfo() == null) {
			return;
		}
		
		MultivaluedMap<String, String> pathParameters = requestContext.getUriInfo().getPathParameters(true);
		if(pathParameters == null || pathParameters.isEmpty()) {
			return;
		}
		
		Map<String, String> parameters = new LinkedHashMap<>();
		for(Map.Entry<String, List<String>> pathParameter:pathParameters.entrySet()) {
			List<String> values = pathParameter.getValue();
			parameters.put(pathParameter.getKey(), values == null || values.isEmpty() ? null : values.get(0));
		}
		entry.setPathParams(mapper.writeValueAsString(parameters));
	}
}
