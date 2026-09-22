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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.Tracing;
import org.olat.core.util.DateUtils;
import org.olat.restapi.RestModule;
import org.olat.restapi.audit.ApiAuditEntry;
import org.olat.restapi.audit.ApiAuditLog;
import org.olat.restapi.audit.ApiAuditLogSearchParams;
import org.olat.restapi.audit.ApiAuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 16 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
@Service
public class ApiAuditLogServiceImpl implements ApiAuditLogService {
	
	private static final Logger log = Tracing.createLoggerFor(ApiAuditLogServiceImpl.class);
	/** One line per audited request, see the commented logger in log4j2.xml */
	private static final Logger accessLog = LogManager.getLogger("org.olat.restapi.access");
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RestModule restModule;
	@Autowired
	private ApiAuditLogDAO auditLogDao;
	
	@Override
	public boolean isAuditable(String method, int status) {
		if(status == 401 || status == 403 || status == 429 || status >= 500) {
			return true;
		}
		if("PUT".equals(method) || "POST".equals(method) || "DELETE".equals(method) || "PATCH".equals(method)) {
			return true;
		}
		return restModule.isAuditLogReads();
	}
	
	@Override
	public void log(ApiAuditEntry entry) {
		if(entry == null) {
			return;
		}
		// the access log covers every request, the table only the auditable ones
		writeAccessLine(entry);
		if(!isLoggable(entry)) {
			return;
		}
		
		try {
			auditLogDao.create(entry);
		} catch (Exception e) {
			log.error("Cannot write the API audit row of {} {}", entry.getMethod(), entry.getPath(), e);
		}
	}
	
	@Override
	public void logInNewTransaction(ApiAuditEntry entry) {
		if(entry == null) {
			return;
		}
		writeAccessLine(entry);
		if(!isLoggable(entry)) {
			return;
		}
		
		try {
			auditLogDao.create(entry);
			dbInstance.commitAndCloseSession();
		} catch (Exception e) {
			log.error("Cannot write the API audit row of {} {}", entry.getMethod(), entry.getPath(), e);
			dbInstance.rollbackAndCloseSession();
		}
	}
	
	/**
	 * Writes one line per audited request on the logger org.olat.restapi.access:
	 * warn for the denied and failed calls, info for the writes, debug for the
	 * reads. The request body is never part of the line.
	 * 
	 * @param entry The data collected about the request
	 */
	private void writeAccessLine(ApiAuditEntry entry) {
		int status = entry.getStatus();
		if(!isAccessLineEnabled(entry, status)) {
			return;
		}
		
		String line = String.format("%s %s%s %d %dms channel=%s auth=%s actor=%s ip=%s resource=%s ref=%s",
				entry.getMethod(), entry.getPath(), formatQuery(entry.getQuery()), Integer.valueOf(status),
				Long.valueOf(entry.getDurationMs() == null ? -1l : entry.getDurationMs().longValue()),
				entry.getChannel(), entry.getAuthProvider(), formatActor(entry), entry.getIp(),
				formatResource(entry), entry.getRef());
		
		if(isDenied(status)) {
			accessLog.warn(Tracing.M_REST, line);
		} else if(isRead(entry.getMethod())) {
			accessLog.debug(Tracing.M_REST, line);
		} else {
			accessLog.info(Tracing.M_REST, line);
		}
	}
	
	private boolean isAccessLineEnabled(ApiAuditEntry entry, int status) {
		if(isDenied(status)) {
			return accessLog.isWarnEnabled();
		}
		return isRead(entry.getMethod()) ? accessLog.isDebugEnabled() : accessLog.isInfoEnabled();
	}
	
	private boolean isDenied(int status) {
		return status == 401 || status == 403 || status == 429 || status >= 500;
	}
	
	private boolean isRead(String method) {
		return "GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method);
	}
	
	private String formatQuery(String query) {
		return query == null ? "" : "?" + ApiAuditMasking.maskQuery(query);
	}
	
	private String formatActor(ApiAuditEntry entry) {
		if(entry.getIdentity() != null) {
			return entry.getIdentity().getKey().toString();
		}
		return entry.getLoginAttempt() == null ? "-" : "attempt:" + entry.getLoginAttempt();
	}
	
	private String formatResource(ApiAuditEntry entry) {
		if(entry.getResourceClass() == null) {
			return "-";
		}
		return entry.getResourceClass() + "." + entry.getResourceMethod();
	}
	
	private boolean isLoggable(ApiAuditEntry entry) {
		return restModule.isAuditLogEnabled()
				&& isAuditable(entry.getMethod(), entry.getStatus());
	}
	
	@Override
	public int deleteOlderThan(int days) {
		if(days <= 0) {
			return 0;
		}
		return auditLogDao.deleteOlderThan(DateUtils.addDays(new Date(), -days));
	}
	
	@Override
	public List<ApiAuditLog> search(ApiAuditLogSearchParams params, int firstResult, int maxResults) {
		return auditLogDao.search(params, firstResult, maxResults);
	}
	
	@Override
	public int count(ApiAuditLogSearchParams params) {
		return auditLogDao.count(params);
	}
	
	@Override
	public ApiAuditLog loadByKey(Long key) {
		return auditLogDao.loadByKey(key);
	}
}
