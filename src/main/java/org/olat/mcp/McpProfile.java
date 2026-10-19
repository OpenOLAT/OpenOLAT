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
package org.olat.mcp;

import java.util.List;
import java.util.Set;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;

/**
 * An admin-defined toolset: which MCP tools a group of users gets. A profile
 * never widens the rights of a user, the tools re-check the object permissions
 * of OpenOlat. Several profiles of one user add up (union), see
 * {@link McpService#resolveGrants(org.olat.basesecurity.IdentityRef)}.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public interface McpProfile extends McpProfileRef, CreateInfo, ModifiedInfo {

	/** Pattern of the identifier: lowercase letters, digits and hyphens, 3 to 64 characters */
	public static final String IDENTIFIER_PATTERN = "^[a-z0-9][a-z0-9-]{2,63}$";

	/**
	 * @return The unique admin slug, never part of an URL
	 */
	String getIdentifier();

	String getName();

	void setName(String name);

	String getDescription();

	void setDescription(String description);

	boolean isEnabled();

	void setEnabled(boolean enabled);

	McpProfileType getType();

	/**
	 * @return The names of the granted tools, never null, in no specific order
	 */
	Set<String> getTools();

	void setTools(Set<String> tools);

	/**
	 * @return The organisation roles of a system profile, empty for a user profile
	 */
	List<OrganisationRoles> getOrganisationRoles();

	void setOrganisationRoles(List<OrganisationRoles> roles);

	/**
	 * @return The override of the requests per minute, null for the module default
	 */
	Integer getRateLimitRequestsPerMinute();

	void setRateLimitRequestsPerMinute(Integer requestsPerMinute);

}
