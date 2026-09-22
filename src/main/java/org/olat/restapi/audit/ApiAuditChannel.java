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
 * The channel which produced an audit log row. The REST API writes
 * <code>rest</code>, the MCP server will write <code>mcp</code>.
 * 
 * Initial date: 16 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public enum ApiAuditChannel {
	
	rest("auditlog.channel.rest"),
	mcp("auditlog.channel.mcp");
	
	public static final List<ApiAuditChannel> VALUES = List.of(values());
	
	private final String i18nKey;
	
	private ApiAuditChannel(String i18nKey) {
		this.i18nKey = i18nKey;
	}
	
	public String i18nKey() {
		return i18nKey;
	}
	
	public static ApiAuditChannel secureValueOf(String val) {
		for(ApiAuditChannel channel:values()) {
			if(channel.name().equals(val)) {
				return channel;
			}
		}
		return null;
	}
}
