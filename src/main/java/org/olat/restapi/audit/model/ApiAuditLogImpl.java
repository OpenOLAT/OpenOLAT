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
package org.olat.restapi.audit.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.restapi.audit.ApiAuditChannel;
import org.olat.restapi.audit.ApiAuditLog;

/**
 * The rows are written once and never updated, all columns are not updatable.
 * There is no foreign key on fk_identity: the table holds a high volume of rows
 * and the actor key must survive the anonymisation of an identity.
 * 
 * Initial date: 16 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
@Entity(name = "apiauditlog")
@Table(name = "o_api_audit_log")
public class ApiAuditLogImpl implements ApiAuditLog, Persistable {

	private static final long serialVersionUID = 2026091601L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true, insertable = true, updatable = false)
	private Long key;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "creationdate", nullable = false, insertable = true, updatable = false)
	private Date creationDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "a_channel", nullable = false, insertable = true, updatable = false)
	private ApiAuditChannel channel;

	@ManyToOne(targetEntity = IdentityImpl.class, fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "fk_identity", nullable = true, insertable = true, updatable = false)
	private Identity identity;

	@Column(name = "a_login_attempt", nullable = true, insertable = true, updatable = false)
	private String loginAttempt;
	@Column(name = "a_auth_provider", nullable = true, insertable = true, updatable = false)
	private String authProvider;
	@Column(name = "a_ip", nullable = true, insertable = true, updatable = false)
	private String ip;
	@Column(name = "a_user_agent", nullable = true, insertable = true, updatable = false)
	private String userAgent;
	@Column(name = "a_method", nullable = false, insertable = true, updatable = false)
	private String method;
	@Column(name = "a_path", nullable = false, insertable = true, updatable = false)
	private String path;
	@Column(name = "a_query", nullable = true, insertable = true, updatable = false)
	private String query;
	@Column(name = "a_resource_class", nullable = true, insertable = true, updatable = false)
	private String resourceClass;
	@Column(name = "a_resource_method", nullable = true, insertable = true, updatable = false)
	private String resourceMethod;
	@Column(name = "a_path_params", nullable = true, insertable = true, updatable = false)
	private String pathParams;
	@Column(name = "a_status", nullable = false, insertable = true, updatable = false)
	private int status;
	@Column(name = "a_duration_ms", nullable = true, insertable = true, updatable = false)
	private Long durationMs;
	@Column(name = "a_request_body", nullable = true, insertable = true, updatable = false)
	private String requestBody;
	@Column(name = "a_ref", nullable = true, insertable = true, updatable = false)
	private String ref;
	@Column(name = "a_node_id", nullable = true, insertable = true, updatable = false)
	private Integer nodeId;

	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public ApiAuditChannel getChannel() {
		return channel;
	}

	public void setChannel(ApiAuditChannel channel) {
		this.channel = channel;
	}

	@Override
	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	@Override
	public String getLoginAttempt() {
		return loginAttempt;
	}

	public void setLoginAttempt(String loginAttempt) {
		this.loginAttempt = loginAttempt;
	}

	@Override
	public String getAuthProvider() {
		return authProvider;
	}

	public void setAuthProvider(String authProvider) {
		this.authProvider = authProvider;
	}

	@Override
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	@Override
	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	@Override
	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	@Override
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	@Override
	public String getResourceClass() {
		return resourceClass;
	}

	public void setResourceClass(String resourceClass) {
		this.resourceClass = resourceClass;
	}

	@Override
	public String getResourceMethod() {
		return resourceMethod;
	}

	public void setResourceMethod(String resourceMethod) {
		this.resourceMethod = resourceMethod;
	}

	@Override
	public String getPathParams() {
		return pathParams;
	}

	public void setPathParams(String pathParams) {
		this.pathParams = pathParams;
	}

	@Override
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	@Override
	public Long getDurationMs() {
		return durationMs;
	}

	public void setDurationMs(Long durationMs) {
		this.durationMs = durationMs;
	}

	@Override
	public String getRequestBody() {
		return requestBody;
	}

	public void setRequestBody(String requestBody) {
		this.requestBody = requestBody;
	}

	@Override
	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	@Override
	public Integer getNodeId() {
		return nodeId;
	}

	public void setNodeId(Integer nodeId) {
		this.nodeId = nodeId;
	}

	@Override
	public int hashCode() {
		return key == null ? 2650937 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof ApiAuditLogImpl log) {
			return key != null && key.equals(log.key);
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
