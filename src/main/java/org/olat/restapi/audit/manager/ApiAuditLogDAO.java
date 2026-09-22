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
package org.olat.restapi.audit.manager;

import java.util.Date;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.util.StringHelper;
import org.olat.restapi.RestModule;
import org.olat.restapi.audit.ApiAuditChannel;
import org.olat.restapi.audit.ApiAuditEntry;
import org.olat.restapi.audit.ApiAuditLog;
import org.olat.restapi.audit.ApiAuditLogSearchParams;
import org.olat.restapi.audit.ApiAuditLogSearchParams.OrderBy;
import org.olat.restapi.audit.ApiAuditStatusClass;
import org.olat.restapi.audit.model.ApiAuditLogImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 16 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
@Service
public class ApiAuditLogDAO {
	
	public static final int MAX_LOGIN_ATTEMPT = 128;
	public static final int MAX_AUTH_PROVIDER = 32;
	public static final int MAX_IP = 64;
	public static final int MAX_USER_AGENT = 255;
	public static final int MAX_METHOD = 8;
	public static final int MAX_PATH = 1024;
	public static final int MAX_QUERY = 1024;
	public static final int MAX_RESOURCE_CLASS = 255;
	public static final int MAX_RESOURCE_METHOD = 128;
	public static final int MAX_REF = 64;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RestModule restModule;
	
	public ApiAuditLog create(ApiAuditEntry entry) {
		ApiAuditLogImpl logEntry = new ApiAuditLogImpl();
		logEntry.setCreationDate(new Date());
		logEntry.setChannel(entry.getChannel() == null ? ApiAuditChannel.rest : entry.getChannel());
		logEntry.setIdentity(entry.getIdentity());
		logEntry.setLoginAttempt(truncate(entry.getLoginAttempt(), MAX_LOGIN_ATTEMPT));
		logEntry.setAuthProvider(truncate(entry.getAuthProvider(), MAX_AUTH_PROVIDER));
		logEntry.setIp(truncate(entry.getIp(), MAX_IP));
		logEntry.setUserAgent(truncate(entry.getUserAgent(), MAX_USER_AGENT));
		logEntry.setMethod(truncate(entry.getMethod(), MAX_METHOD));
		logEntry.setPath(truncate(entry.getPath(), MAX_PATH));
		logEntry.setQuery(truncate(ApiAuditMasking.maskQuery(entry.getQuery()), MAX_QUERY));
		logEntry.setResourceClass(truncate(entry.getResourceClass(), MAX_RESOURCE_CLASS));
		logEntry.setResourceMethod(truncate(entry.getResourceMethod(), MAX_RESOURCE_METHOD));
		logEntry.setPathParams(entry.getPathParams());
		logEntry.setStatus(entry.getStatus());
		logEntry.setDurationMs(entry.getDurationMs());
		logEntry.setRequestBody(restModule.isAuditLogBody() ? ApiAuditMasking.maskJson(entry.getRequestBody()) : null);
		logEntry.setRef(truncate(entry.getRef(), MAX_REF));
		logEntry.setNodeId(entry.getNodeId());
		dbInstance.getCurrentEntityManager().persist(logEntry);
		return logEntry;
	}
	
	private static String truncate(String text, int maxLength) {
		if(text == null || text.length() <= maxLength) {
			return text;
		}
		return text.substring(0, maxLength);
	}
	
	public ApiAuditLog create(ApiAuditLogImpl row) {
		row.setCreationDate(new Date());
		dbInstance.getCurrentEntityManager().persist(row);
		return row;
	}
	
	public ApiAuditLog loadByKey(Long key) {
		return dbInstance.getCurrentEntityManager()
				.find(ApiAuditLogImpl.class, key);
	}
	
	public int count(ApiAuditLogSearchParams params) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(log.key) from apiauditlog log")
		  .append(" left join log.identity ident")
		  .append(" left join ident.user usr");
		appendFilters(sb, params);
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class);
		applyParameters(query, params);
		Long count = query.getSingleResult();
		return count == null ? 0 : count.intValue();
	}
	
	public List<ApiAuditLog> search(ApiAuditLogSearchParams params, int firstResult, int maxResults) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select log from apiauditlog log")
		  .append(" left join fetch log.identity ident")
		  .append(" left join fetch ident.user usr");
		appendFilters(sb, params);
		appendOrderBy(sb, params);
		
		TypedQuery<ApiAuditLog> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ApiAuditLog.class);
		applyParameters(query, params);
		if(firstResult > 0) {
			query.setFirstResult(firstResult);
		}
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		return query.getResultList();
	}
	
	/**
	 * @param before Rows created before this date are deleted
	 * @return The number of deleted rows
	 */
	public int deleteOlderThan(Date before) {
		return dbInstance.getCurrentEntityManager()
				.createQuery("delete from apiauditlog log where log.creationDate < :before")
				.setParameter("before", before)
				.executeUpdate();
	}
	
	private void appendFilters(QueryBuilder sb, ApiAuditLogSearchParams params) {
		if(params.getCreatedAfter() != null) {
			sb.and().append("log.creationDate >= :createdAfter");
		}
		if(params.getCreatedBefore() != null) {
			sb.and().append("log.creationDate < :createdBefore");
		}
		if(params.getIdentityKey() != null) {
			sb.and().append("ident.key = :identityKey");
		}
		if(params.getChannels() != null && !params.getChannels().isEmpty()) {
			sb.and().append("log.channel in (:channels)");
		}
		if(params.getMethods() != null && !params.getMethods().isEmpty()) {
			sb.and().append("log.method in (:methods)");
		}
		if(StringHelper.containsNonWhitespace(params.getResourceClass())) {
			sb.and().likeFuzzy("log.resourceClass", "resourceClass");
		}
		if(StringHelper.containsNonWhitespace(params.getUserSearch())) {
			sb.and().append("(").likeFuzzy("usr.nickName", "userSearch")
			  .append(" or ").likeFuzzy("usr.email", "userSearch")
			  .append(" or ").likeFuzzy("usr.firstName", "userSearch")
			  .append(" or ").likeFuzzy("usr.lastName", "userSearch")
			  .append(" or ").likeFuzzy("log.loginAttempt", "userSearch")
			  .append(")");
		}
		if(params.getStatusClasses() != null && !params.getStatusClasses().isEmpty()) {
			sb.and().append("(");
			boolean first = true;
			for(ApiAuditStatusClass statusClass:params.getStatusClasses()) {
				if(!first) {
					sb.append(" or ");
				}
				first = false;
				appendStatusClass(sb, statusClass);
			}
			sb.append(")");
		}
	}
	
	private void appendStatusClass(QueryBuilder sb, ApiAuditStatusClass statusClass) {
		switch(statusClass) {
			case success -> sb.append("(log.status >= 200 and log.status < 400)");
			case denied -> sb.append("log.status in (401,403)");
			case rateLimited -> sb.append("log.status = 429");
			case clientError -> sb.append("(log.status >= 400 and log.status < 500 and log.status not in (401,403,429))");
			case serverError -> sb.append("(log.status >= 500 and log.status < 600)");
		}
	}
	
	private void applyParameters(TypedQuery<?> query, ApiAuditLogSearchParams params) {
		if(params.getCreatedAfter() != null) {
			query.setParameter("createdAfter", params.getCreatedAfter());
		}
		if(params.getCreatedBefore() != null) {
			query.setParameter("createdBefore", params.getCreatedBefore());
		}
		if(params.getIdentityKey() != null) {
			query.setParameter("identityKey", params.getIdentityKey());
		}
		if(params.getChannels() != null && !params.getChannels().isEmpty()) {
			query.setParameter("channels", params.getChannels());
		}
		if(params.getMethods() != null && !params.getMethods().isEmpty()) {
			query.setParameter("methods", params.getMethods());
		}
		if(StringHelper.containsNonWhitespace(params.getResourceClass())) {
			query.setParameter("resourceClass", PersistenceHelper.makeFuzzyQueryString(params.getResourceClass()));
		}
		if(StringHelper.containsNonWhitespace(params.getUserSearch())) {
			query.setParameter("userSearch", PersistenceHelper.makeFuzzyQueryString(params.getUserSearch()));
		}
	}
	
	private void appendOrderBy(QueryBuilder sb, ApiAuditLogSearchParams params) {
		OrderBy order = params.getOrder() == null ? OrderBy.creationDate : params.getOrder();
		boolean asc = params.isOrderAsc();
		switch(order) {
			case creationDate -> sb.append(" order by log.creationDate ").append("asc", "desc", asc);
			case channel -> sb.append(" order by log.channel ").append("asc", "desc", asc).append(", log.creationDate desc");
			case identity -> sb.append(" order by lower(usr.lastName) ").append("asc", "desc", asc).append(" nulls last, log.creationDate desc");
			case authProvider -> sb.append(" order by lower(log.authProvider) ").append("asc", "desc", asc).append(" nulls last, log.creationDate desc");
			case ip -> sb.append(" order by log.ip ").append("asc", "desc", asc).append(" nulls last, log.creationDate desc");
			case method -> sb.append(" order by log.method ").append("asc", "desc", asc).append(", log.creationDate desc");
			case path -> sb.append(" order by lower(log.path) ").append("asc", "desc", asc).append(", log.creationDate desc");
			case resourceClass -> sb.append(" order by lower(log.resourceClass) ").append("asc", "desc", asc).append(" nulls last, log.creationDate desc");
			case resourceMethod -> sb.append(" order by lower(log.resourceMethod) ").append("asc", "desc", asc).append(" nulls last, log.creationDate desc");
			case status -> sb.append(" order by log.status ").append("asc", "desc", asc).append(", log.creationDate desc");
			case durationMs -> sb.append(" order by log.durationMs ").append("asc", "desc", asc).append(" nulls last, log.creationDate desc");
		}
		sb.append(", log.key ").append("asc", "desc", asc);
	}
}
