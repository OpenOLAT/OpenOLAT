/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.restapi.audit;

/**
 * The audit settings of one API channel. The REST module implements it for
 * the channel {@link ApiAuditChannel#rest}, the MCP module for the channel
 * {@link ApiAuditChannel#mcp}. The audit log service collects every
 * implementation and reads the settings of the channel of an entry. A
 * channel is audited independently of the other channels: the MCP channel
 * writes rows while the REST API is switched off.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public interface ApiAuditChannelSettings {

	/**
	 * @return The channel these settings apply to
	 */
	ApiAuditChannel getAuditChannel();

	/**
	 * @return true if the channel writes rows in o_api_audit_log
	 */
	boolean isAuditLogEnabled();

	/**
	 * @return true if read operations of the channel are written too
	 */
	boolean isAuditLogReads();

	/**
	 * @return true if the request body or the arguments are stored, masked
	 */
	boolean isAuditLogBody();

	/**
	 * @return The maximum stored size of the body in bytes
	 */
	int getAuditLogBodyMaxSize();

}
