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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.Tracing;
import org.olat.core.util.DateUtils;
import org.olat.restapi.RestModule;
import org.olat.restapi.audit.ApiAuditChannel;
import org.olat.restapi.audit.ApiAuditChannelSettings;
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
	@Autowired
	private List<ApiAuditChannelSettings> channelSettingsList;
	
	private Map<ApiAuditChannel, ApiAuditChannelSettings> channelSettings;
	
	/**
	 * The settings of every channel, from the beans which implement
	 * {@link ApiAuditChannelSettings}. The injected list is complete here.
	 */
	@PostConstruct
	void initChannelSettings() {
		Map<ApiAuditChannel, ApiAuditChannelSettings> settingsMap = new EnumMap<>(ApiAuditChannel.class);
		if(channelSettingsList != null) {
			for(ApiAuditChannelSettings settings:channelSettingsList) {
				if(settings.getAuditChannel() != null) {
					settingsMap.putIfAbsent(settings.getAuditChannel(), settings);
				}
			}
		}
		channelSettings = settingsMap;
	}
	
	@Override
	public boolean isAuditable(String method, int status) {
		return isAuditable(method, status, restModule);
	}
	
	@Override
	public boolean isAuditable(ApiAuditEntry entry) {
		if(entry == null) {
			return false;
		}
		
		int status = entry.getStatus();
		ApiAuditChannelSettings settings = getChannelSettings(entry.getChannel());
		Boolean readOperation = entry.getReadOperation();
		if(readOperation == null) {
			// REST: the HTTP method decides, unchanged
			return isAuditable(entry.getMethod(), status, settings);
		}
		// a channel which marks reads and writes also audits the tool errors (422)
		if(isDenied(status) || status == 422) {
			return true;
		}
		return !readOperation.booleanValue() || settings.isAuditLogReads();
	}
	
	private boolean isAuditable(String method, int status, ApiAuditChannelSettings settings) {
		if(isDenied(status)) {
			return true;
		}
		if("PUT".equals(method) || "POST".equals(method) || "DELETE".equals(method) || "PATCH".equals(method)) {
			return true;
		}
		return settings.isAuditLogReads();
	}
	
	/**
	 * @param channel The channel of the entry
	 * @return The settings of the channel, the REST settings if the channel is
	 * 		null or has no settings
	 */
	private ApiAuditChannelSettings getChannelSettings(ApiAuditChannel channel) {
		if(channel == null) {
			return restModule;
		}
		
		ApiAuditChannelSettings settings = channelSettings.get(channel);
		return settings == null ? restModule : settings;
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
			auditLogDao.create(entry, getChannelSettings(entry.getChannel()).isAuditLogBody());
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
			auditLogDao.create(entry, getChannelSettings(entry.getChannel()).isAuditLogBody());
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
		} else if(isRead(entry)) {
			accessLog.debug(Tracing.M_REST, line);
		} else {
			accessLog.info(Tracing.M_REST, line);
		}
	}
	
	private boolean isAccessLineEnabled(ApiAuditEntry entry, int status) {
		if(isDenied(status)) {
			return accessLog.isWarnEnabled();
		}
		return isRead(entry) ? accessLog.isDebugEnabled() : accessLog.isInfoEnabled();
	}
	
	private boolean isDenied(int status) {
		return status == 401 || status == 403 || status == 429 || status >= 500;
	}
	
	/**
	 * The read flag of the entry wins (MCP sets it, every MCP request is a
	 * POST), else the HTTP method decides as for REST.
	 */
	private boolean isRead(ApiAuditEntry entry) {
		if(entry.getReadOperation() != null) {
			return entry.getReadOperation().booleanValue();
		}
		String method = entry.getMethod();
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
		return getChannelSettings(entry.getChannel()).isAuditLogEnabled()
				&& isAuditable(entry);
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
