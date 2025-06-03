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

import org.olat.modules.openbadges.model.BadgeCryptoKey;
import org.json.JSONObject;

/**
 * Initial date: 2025-06-02<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CryptographicKey {
	private static final String TYPE_VALUE = "CryptographicKey";
	private static final String OWNER_KEY = "owner";
	private static final String PUBLIC_KEY_PEM_KEY = "publicKeyPem";

	private String id;
	private String owner;
	private String publicKeyPem;
	
	public CryptographicKey(BadgeCryptoKey badgeCryptoKey) {
		id = badgeCryptoKey.publicKeyUrl();
		owner = badgeCryptoKey.organizationUrl();
		publicKeyPem = badgeCryptoKey.publicKeyPem();
	}
	
	public JSONObject asJsonObject() {
		JSONObject jsonObject = new JSONObject();
		
		jsonObject.put(Constants.TYPE_KEY, TYPE_VALUE);
		jsonObject.put(Constants.CONTEXT_KEY, Constants.CONTEXT_VALUE);
		jsonObject.put(Constants.ID_KEY, id);
		jsonObject.put(OWNER_KEY, owner);
		jsonObject.put(PUBLIC_KEY_PEM_KEY, publicKeyPem);
		return jsonObject;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getOwner() {
		return owner;
	}
	
	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	public String getPublicKeyPem() {
		return publicKeyPem;
	}
	
	public void setPublicKeyPem(String publicKeyPem) {
		this.publicKeyPem = publicKeyPem;
	}
}
