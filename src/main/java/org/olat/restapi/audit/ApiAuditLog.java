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

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;

/**
 * One row of the API audit log. A row is written once and never updated.
 * 
 * Initial date: 16 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public interface ApiAuditLog extends CreateInfo {
	
	public Long getKey();
	
	public ApiAuditChannel getChannel();
	
	/**
	 * @return the authenticated identity, null if the request was not authenticated
	 */
	public Identity getIdentity();
	
	/**
	 * @return the user name of a failed authentication attempt, null if the request was authenticated
	 */
	public String getLoginAttempt();
	
	public String getAuthProvider();
	
	public String getIp();
	
	public String getUserAgent();
	
	public String getMethod();
	
	public String getPath();
	
	public String getQuery();
	
	/**
	 * @return the simple name of the web service class which served the request, null if none was reached
	 */
	public String getResourceClass();
	
	public String getResourceMethod();
	
	/**
	 * @return the path parameters of the request as a JSON object
	 */
	public String getPathParams();
	
	public int getStatus();
	
	public Long getDurationMs();
	
	/**
	 * @return the JSON request body with masked secrets, truncated to the configured maximum size
	 */
	public String getRequestBody();
	
	/**
	 * @return the correlation id of the request, matches the ref of the log files
	 */
	public String getRef();
	
	public Integer getNodeId();
}
