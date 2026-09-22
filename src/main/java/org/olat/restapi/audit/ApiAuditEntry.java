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

import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.gui.UserRequest;
import org.olat.core.id.Identity;
import org.olat.core.util.UserSession;
import org.olat.core.util.WebappHelper;
import org.olat.restapi.security.RestSecurityHelper;

/**
 * Everything the filters collect about one request. The service masks the
 * secrets, truncates the values and turns it into a row of the audit log.
 * 
 * Initial date: 16 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class ApiAuditEntry {
	
	private ApiAuditChannel channel;
	private Identity identity;
	private String loginAttempt;
	private String authProvider;
	private String ip;
	private String userAgent;
	private String method;
	private String path;
	private String query;
	private String resourceClass;
	private String resourceMethod;
	private String pathParams;
	private int status;
	private Long durationMs;
	private String requestBody;
	private String ref;
	private Integer nodeId;
	
	public ApiAuditEntry() {
		//
	}
	
	public static ApiAuditEntry valueOf(HttpServletRequest request, int status, ApiAuditChannel channel) {
		ApiAuditEntry entry = new ApiAuditEntry();
		entry.setChannel(channel);
		entry.setMethod(request.getMethod());
		entry.setPath(request.getRequestURI());
		entry.setQuery(request.getQueryString());
		entry.setIp(request.getRemoteAddr());
		entry.setUserAgent(request.getHeader("User-Agent"));
		entry.setStatus(status);
		entry.setNodeId(Integer.valueOf(WebappHelper.getNodeId()));
		entry.setAuthProvider((String)request.getAttribute(ApiAuditLogService.REQ_ATTR_AUTH_PROVIDER));
		entry.setLoginAttempt((String)request.getAttribute(ApiAuditLogService.REQ_ATTR_LOGIN_ATTEMPT));
		
		Object start = request.getAttribute(ApiAuditLogService.REQ_ATTR_START_NANOS);
		if(start instanceof Long startNanos) {
			entry.setDurationMs(Long.valueOf((System.nanoTime() - startNanos.longValue()) / 1000000l));
		}
		
		UserRequest ureq = RestSecurityHelper.getUserRequest(request);
		if(ureq != null) {
			entry.setRef(ureq.getUuid());
			entry.setIdentity(getAuthenticatedIdentity(ureq.getUserSession()));
		}
		
		entry.setRequestBody(getRequestBody(request));
		return entry;
	}
	
	/**
	 * The IP protected space creates a session with a pseudo identity of key -1,
	 * only a real identity belongs in the audit log.
	 */
	private static Identity getAuthenticatedIdentity(UserSession usess) {
		if(usess == null || usess.getIdentity() == null) {
			return null;
		}
		Identity identity = usess.getIdentity();
		if(identity.getKey() == null || identity.getKey().longValue() <= 0) {
			return null;
		}
		return identity;
	}
	
	private static String getRequestBody(HttpServletRequest request) {
		// the JAX-RS filter receives a proxy of the request, the servlet filter
		// puts the wrapper itself in an attribute to make it reachable
		if(request.getAttribute(ApiAuditLogService.REQ_ATTR_CACHED_REQUEST) instanceof CachedBodyHttpServletRequest cachedRequest) {
			return cachedRequest.getCachedBody();
		}
		if(request instanceof CachedBodyHttpServletRequest cachedRequest) {
			return cachedRequest.getCachedBody();
		}
		
		String contentType = request.getContentType();
		if(contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("multipart/")) {
			// the parts cannot be read here, the resource needs the stream
			return "{\"contentType\":\"multipart/form-data\",\"contentLength\":"
					+ request.getContentLengthLong() + "}";
		}
		return null;
	}
	
	public ApiAuditChannel getChannel() {
		return channel;
	}
	
	public void setChannel(ApiAuditChannel channel) {
		this.channel = channel;
	}
	
	public Identity getIdentity() {
		return identity;
	}
	
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}
	
	public String getLoginAttempt() {
		return loginAttempt;
	}
	
	public void setLoginAttempt(String loginAttempt) {
		this.loginAttempt = loginAttempt;
	}
	
	public String getAuthProvider() {
		return authProvider;
	}
	
	public void setAuthProvider(String authProvider) {
		this.authProvider = authProvider;
	}
	
	public String getIp() {
		return ip;
	}
	
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public String getUserAgent() {
		return userAgent;
	}
	
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}
	
	public String getMethod() {
		return method;
	}
	
	public void setMethod(String method) {
		this.method = method;
	}
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public String getQuery() {
		return query;
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
	
	public String getResourceClass() {
		return resourceClass;
	}
	
	public void setResourceClass(String resourceClass) {
		this.resourceClass = resourceClass;
	}
	
	public String getResourceMethod() {
		return resourceMethod;
	}
	
	public void setResourceMethod(String resourceMethod) {
		this.resourceMethod = resourceMethod;
	}
	
	public String getPathParams() {
		return pathParams;
	}
	
	public void setPathParams(String pathParams) {
		this.pathParams = pathParams;
	}
	
	public int getStatus() {
		return status;
	}
	
	public void setStatus(int status) {
		this.status = status;
	}
	
	public Long getDurationMs() {
		return durationMs;
	}
	
	public void setDurationMs(Long durationMs) {
		this.durationMs = durationMs;
	}
	
	public String getRequestBody() {
		return requestBody;
	}
	
	public void setRequestBody(String requestBody) {
		this.requestBody = requestBody;
	}
	
	public String getRef() {
		return ref;
	}
	
	public void setRef(String ref) {
		this.ref = ref;
	}
	
	public Integer getNodeId() {
		return nodeId;
	}
	
	public void setNodeId(Integer nodeId) {
		this.nodeId = nodeId;
	}
}
