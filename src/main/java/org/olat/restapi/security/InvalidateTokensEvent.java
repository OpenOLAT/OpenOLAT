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
package org.olat.restapi.security;

import org.olat.core.util.event.MultiUserEvent;

/**
 *
 * Initial date: 27 oct. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class InvalidateTokensEvent extends MultiUserEvent {

	private static final long serialVersionUID = -4263906482315402741L;

	private final Long identityKey;
	private final String excludedToken;

	public InvalidateTokensEvent(Long identityKey, String excludedToken) {
		super("invalidatetokens");
		this.identityKey = identityKey;
		this.excludedToken = excludedToken;
	}

	public Long getIdentityKey() {
		return identityKey;
	}

	/**
	 * @return The token which stays valid (optional)
	 */
	public String getExcludedToken() {
		return excludedToken;
	}
}
