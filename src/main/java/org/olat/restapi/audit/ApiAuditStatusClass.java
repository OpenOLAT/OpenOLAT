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

/**
 * Groups the HTTP status codes of the audit log for the filter of the admin view.
 * 
 * Initial date: 16 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public enum ApiAuditStatusClass {
	
	/** 200 - 399 */
	success("auditlog.status.success"),
	/** 401 and 403 */
	denied("auditlog.status.denied"),
	/** 429 */
	rateLimited("auditlog.status.ratelimited"),
	/** 400 - 499 without 401, 403 and 429 */
	clientError("auditlog.status.clienterror"),
	/** 500 - 599 */
	serverError("auditlog.status.servererror");
	
	public static final List<ApiAuditStatusClass> VALUES = List.of(values());
	
	private final String i18nKey;
	
	private ApiAuditStatusClass(String i18nKey) {
		this.i18nKey = i18nKey;
	}
	
	public String getI18nKey() {
		return i18nKey;
	}
	
	public static ApiAuditStatusClass secureValueOf(String val) {
		for(ApiAuditStatusClass statusClass:values()) {
			if(statusClass.name().equals(val)) {
				return statusClass;
			}
		}
		return null;
	}
	
	/**
	 * @param status The HTTP status of the request
	 * @return The class the status belongs to, null for a status out of the known ranges
	 */
	public static ApiAuditStatusClass valueOfStatus(int status) {
		if(status == 401 || status == 403) {
			return denied;
		}
		if(status == 429) {
			return rateLimited;
		}
		if(status >= 200 && status < 400) {
			return success;
		}
		if(status >= 400 && status < 500) {
			return clientError;
		}
		if(status >= 500 && status < 600) {
			return serverError;
		}
		return null;
	}
}
