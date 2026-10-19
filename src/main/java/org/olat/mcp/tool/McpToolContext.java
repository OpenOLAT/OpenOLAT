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
package org.olat.mcp.tool;

import java.util.Locale;

import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.mcp.McpGrants;
import org.olat.mcp.McpToken;

/**
 * What a tool knows about the caller. Built by the filter per request, the
 * identity is authenticated and the grants are resolved. A tool never trusts
 * the grants for object permissions: it re-checks them with the security
 * callbacks of its module.
 *
 * @param identity The authenticated owner of the token
 * @param roles The roles of the identity
 * @param grants The resolved grants, never empty inside a tool
 * @param token The token metadata, for the audit trail
 * @param locale The locale of the identity, for display values
 * @param remoteAddr The client IP
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public record McpToolContext(Identity identity, Roles roles, McpGrants grants, McpToken token, Locale locale, String remoteAddr) {

}
