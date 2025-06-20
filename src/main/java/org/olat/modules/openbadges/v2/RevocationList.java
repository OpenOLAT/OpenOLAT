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
package org.olat.modules.openbadges.v2;

import java.util.Collection;

import org.olat.modules.openbadges.OpenBadgesFactory;
import org.json.JSONObject;

/**
 * Initial date: 2025-06-17<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RevocationList {
	private static final String TYPE_VALUE = "RevocationList";
	private static final String REVOKED_ASSERTIONS_KEY = "revokedAssertions";
	private String id;
	private String issuer;
	private Collection<String> revokedAssertions;

	public RevocationList(String badgeClassUuid, String issuer, Collection<String> revokedAssertions) {
		setId(OpenBadgesFactory.createRevocationListUrl(badgeClassUuid));
		setIssuer(issuer);
		setRevokedAssertions(revokedAssertions);
	}
	
	public JSONObject asJsonObject() {
		JSONObject jsonObject = new JSONObject();
		
		jsonObject.put(Constants.TYPE_KEY, TYPE_VALUE);
		jsonObject.put(Constants.CONTEXT_KEY, Constants.CONTEXT_VALUE);
		jsonObject.put(Constants.ID_KEY, id);
		jsonObject.put(Constants.ISSUER_KEY, issuer);
		jsonObject.put(REVOKED_ASSERTIONS_KEY, revokedAssertions);
		
		return jsonObject;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	public Collection<String> getRevokedAssertions() {
		return revokedAssertions;
	}

	public void setRevokedAssertions(Collection<String> revokedAssertions) {
		this.revokedAssertions = revokedAssertions;
	}
}
