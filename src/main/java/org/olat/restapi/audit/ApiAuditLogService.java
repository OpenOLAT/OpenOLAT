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

import java.util.List;

import org.olat.restapi.RestModule;

/**
 * The single entry point to write and to read the audit log of the API. The REST
 * API writes with channel {@link ApiAuditChannel#rest}, the MCP server will use
 * {@link ApiAuditChannel#mcp}.
 * 
 * Initial date: 16 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public interface ApiAuditLogService {
	
	/** Attributes shared between the servlet filter and the JAX-RS response filter */
	public static final String REQ_ATTR_START_NANOS = "olat-api-audit-start";
	public static final String REQ_ATTR_AUTH_PROVIDER = "olat-api-audit-auth-provider";
	public static final String REQ_ATTR_LOGIN_ATTEMPT = "olat-api-audit-login-attempt";
	public static final String REQ_ATTR_CACHED_REQUEST = "olat-api-audit-cached-request";
	public static final String REQ_ATTR_DONE = "olat-api-audit-done";
	public static final String REQ_ATTR_PENDING = "olat-api-audit-pending";
	
	public static final String AUTH_API_KEY = RestModule.RESTAPI_AUTH;
	public static final String AUTH_PASSWORD = "OLAT";
	public static final String AUTH_TOKEN = "token";
	public static final String AUTH_SESSION = "session";
	public static final String AUTH_IP = "IP";
	public static final String AUTH_NONE = "none";
	
	/**
	 * @param method The HTTP method of the request
	 * @param status The HTTP status of the response
	 * @return true if the request produces a row of the audit log
	 */
	boolean isAuditable(String method, int status);
	
	/**
	 * Writes the row in the transaction of the caller.
	 * 
	 * @param entry The data collected about the request
	 */
	void log(ApiAuditEntry entry);
	
	/**
	 * Writes the row in a transaction of its own and commits it. Use it when the
	 * transaction of the request is lost, for example after an error. Never throws.
	 * 
	 * @param entry The data collected about the request
	 */
	void logInNewTransaction(ApiAuditEntry entry);
	
	/**
	 * @param days The number of days the rows are kept, 0 or less keeps all rows
	 * @return The number of deleted rows
	 */
	int deleteOlderThan(int days);
	
	List<ApiAuditLog> search(ApiAuditLogSearchParams params, int firstResult, int maxResults);
	
	int count(ApiAuditLogSearchParams params);
	
	ApiAuditLog loadByKey(Long key);
}
