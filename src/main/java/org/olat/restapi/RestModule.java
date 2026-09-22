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
package org.olat.restapi;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Description:<br>
 * Configuration of the REST API
 * 
 * <P>
 * Initial Date:  18 juin 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
@Service("restModule")
public class RestModule extends AbstractSpringModule implements ConfigOnOff {
	
	public static final String RESTAPI_AUTH = "API-Key";
	
	private static final String ENABLED = "enabled";
	private static final String API_ACCESS = "restapi.api.access";
	private static final String USER_ALLOWED_GENERATE_APIKEY = "restapi.user.generate.apikey";
	private static final String AUDITLOG_ENABLED = "restapi.auditlog.enabled";
	private static final String AUDITLOG_READS = "restapi.auditlog.reads";
	private static final String AUDITLOG_BODY = "restapi.auditlog.body";
	private static final String AUDITLOG_BODY_MAXSIZE = "restapi.auditlog.body.maxsize";
	private static final String AUDITLOG_RETENTION_DAYS = "restapi.auditlog.retention.days";

	@Value("${restapi.enable:false}")
	private boolean enabled;
	@Value("${restapi.ips.system}")
	private String ipsByPass;
	@Value("${restapi.user.generate.apikey:false}")
	private boolean userAllowedGenerateApiKey;
	@Value("${restapi.api.access:all}")
	private String apiAccess;
	@Value("${restapi.auditlog.enabled:true}")
	private String auditLogEnabled;
	@Value("${restapi.auditlog.reads:false}")
	private String auditLogReads;
	@Value("${restapi.auditlog.body:true}")
	private String auditLogBody;
	@Value("${restapi.auditlog.body.maxsize:16384}")
	private int auditLogBodyMaxSize;
	@Value("${restapi.auditlog.retention.days:365}")
	private int auditLogRetentionDays;

	@Autowired
	public RestModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}
	
	@Override
	public void init() {
		//module enabled/disabled
		String enabledObj = getStringPropertyValue(ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "enabled".equals(enabledObj);
		}
		
		String enabledGenerateApiKeyObj = getStringPropertyValue(USER_ALLOWED_GENERATE_APIKEY, userAllowedGenerateApiKey ? "enabled" : "disabled");
		userAllowedGenerateApiKey = "enabled".equals(enabledGenerateApiKeyObj);
		
		apiAccess = getStringPropertyValue(API_ACCESS, apiAccess);
		if(!ApiAccess.isValue(apiAccess)) {
			apiAccess = ApiAccess.all.name();
		}
		
		auditLogEnabled = getStringPropertyValue(AUDITLOG_ENABLED, auditLogEnabled );
		auditLogReads = getStringPropertyValue(AUDITLOG_READS, auditLogReads);
		auditLogBody = getStringPropertyValue(AUDITLOG_BODY, auditLogBody);
		auditLogBodyMaxSize = getIntPropertyValue(AUDITLOG_BODY_MAXSIZE, auditLogBodyMaxSize);
		auditLogRetentionDays = getIntPropertyValue(AUDITLOG_RETENTION_DAYS, auditLogRetentionDays);
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		String enabledStr = enabled ? "enabled" : "disabled";
		setStringProperty(ENABLED, enabledStr, true);
	}
	
	public boolean isUserAllowedGenerateApiKey() {
		return userAllowedGenerateApiKey;
	}

	public void setUserAllowedGenerateApiKey(boolean enable) {
		userAllowedGenerateApiKey = enable;
		String enabledStr = enable ? "enabled" : "disabled";
		setStringProperty(USER_ALLOWED_GENERATE_APIKEY, enabledStr, true);
	}

	/**
	 * @return true if the REST API writes a row per write and per denied call in o_api_audit_log
	 */
	public boolean isAuditLogEnabled() {
		return "true".equals(auditLogEnabled);
	}

	public void setAuditLogEnabled(boolean enable) {
		auditLogEnabled = enable ? "true" : "false";
		setStringProperty(AUDITLOG_ENABLED, auditLogEnabled, true);
	}

	/**
	 * @return true if read requests (GET, HEAD) which succeed are logged too. High volume.
	 */
	public boolean isAuditLogReads() {
		return "true".equals(auditLogReads);
	}

	public void setAuditLogReads(boolean enable) {
		auditLogReads = enable ? "true" : "false";
		setStringProperty(AUDITLOG_READS, auditLogReads, true);
	}

	/**
	 * @return true if the JSON request body is stored, with masked secrets
	 */
	public boolean isAuditLogBody() {
		return "true".equals(auditLogBody);
	}

	public void setAuditLogBody(boolean enable) {
		auditLogBody = enable ? "true" : "false";
		setStringProperty(AUDITLOG_BODY, auditLogBody, true);
	}

	/**
	 * @return the maximum number of bytes of the request body which are stored, larger bodies are truncated
	 */
	public int getAuditLogBodyMaxSize() {
		return auditLogBodyMaxSize;
	}

	public void setAuditLogBodyMaxSize(int maxSize) {
		auditLogBodyMaxSize = maxSize;
		setIntProperty(AUDITLOG_BODY_MAXSIZE, maxSize, true);
	}

	/**
	 * @return the number of days the rows are kept, 0 keeps all rows
	 */
	public int getAuditLogRetentionDays() {
		return auditLogRetentionDays;
	}

	public void setAuditLogRetentionDays(int days) {
		auditLogRetentionDays = days;
		setIntProperty(AUDITLOG_RETENTION_DAYS, days, true);
	}

	public ApiAccess getApiAccess() {
		return StringHelper.containsNonWhitespace(apiAccess) ? ApiAccess.valueOf(apiAccess) : ApiAccess.all;
	}

	public void setApiAccess(ApiAccess access) {
		this.apiAccess = access == null ? ApiAccess.all.name() : access.name();
		setStringProperty(API_ACCESS, this.apiAccess, true);
	}

	public String getIpsByPass() {
		return ipsByPass;
	}
	
	public List<String> getIpsWithSystemAccess() {
		List<String> ips = new ArrayList<>();
		for(StringTokenizer tokenizer=new StringTokenizer(ipsByPass, ",;|"); tokenizer.hasMoreTokens(); ) {
			String token = tokenizer.nextToken();
			if(StringHelper.containsNonWhitespace(token)) {
				ips.add(token);
			}
		}
		return ips;
	}

	public void setIpsByPass(String ipsByPass) {
		this.ipsByPass = ipsByPass;
	}
	
	public enum ApiAccess {
		all,
		apikey;
		
		public static boolean isValue(String val) {
			for(ApiAccess a:values()) {
				if(a.name().equals(val)) {
					return true;
				}
			}
			return false;
		}
		
	}
}
