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

import org.olat.core.id.OLATResourceable;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.resource.OresHelper;

/**
 * Fired on every node after a profile or a grant changed. Listeners drop
 * cached grants. The event carries the key of the profile when one profile
 * changed, null when the change concerns every profile.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpProfileChangedEvent extends MultiUserEvent {

	private static final long serialVersionUID = 2026100202L;

	public static final String COMMAND = "mcp-profile-changed";
	public static final OLATResourceable ORES = OresHelper.createOLATResourceableType("McpProfile");

	private final Long profileKey;

	public McpProfileChangedEvent(Long profileKey) {
		super(COMMAND);
		this.profileKey = profileKey;
	}

	/**
	 * @return The key of the changed profile, null if every profile is affected
	 */
	public Long getProfileKey() {
		return profileKey;
	}
}
