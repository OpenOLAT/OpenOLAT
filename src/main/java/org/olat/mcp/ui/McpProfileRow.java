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
package org.olat.mcp.ui;

import org.olat.mcp.McpProfile;
import org.olat.mcp.McpProfileRef;
import org.olat.mcp.McpProfileType;

/**
 * One row of the profile table.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpProfileRow implements McpProfileRef {

	private final McpProfile profile;
	private final String translatedType;
	private final Integer numOfMembers;
	private final String organisations;

	public McpProfileRow(McpProfile profile, String translatedType, Integer numOfMembers, String organisations) {
		this.profile = profile;
		this.translatedType = translatedType;
		this.numOfMembers = numOfMembers;
		this.organisations = organisations;
	}

	@Override
	public Long getKey() {
		return profile.getKey();
	}

	public String getIdentifier() {
		return profile.getIdentifier();
	}

	public String getName() {
		return profile.getName();
	}

	/**
	 * @return The display names of the organisations of the profile, sorted and separated by commas
	 */
	public String getOrganisations() {
		return organisations;
	}

	public String getTranslatedType() {
		return translatedType;
	}

	public boolean isUserType() {
		return profile.getType() == McpProfileType.user;
	}

	public boolean isEnabled() {
		return profile.isEnabled();
	}

	public int getNumOfTools() {
		return profile.getTools().size();
	}

	/**
	 * @return The number of explicit grants of a user profile, null for a system profile
	 */
	public Integer getNumOfMembers() {
		return numOfMembers;
	}

	public Integer getRateLimitRequestsPerMinute() {
		return profile.getRateLimitRequestsPerMinute();
	}

	@Override
	public int hashCode() {
		return profile.getKey() == null ? 7 : profile.getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof McpProfileRow row) {
			return getKey() != null && getKey().equals(row.getKey());
		}
		return false;
	}
}
