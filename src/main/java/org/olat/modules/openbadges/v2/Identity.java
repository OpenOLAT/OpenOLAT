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

import org.json.JSONObject;

/**
 * Initial date: 2023-05-12<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class Identity {
	private static final String IDENTITY_KEY = "identity";
	private static final String TYPE_VALUE = "email";
	private static final String HASHED_KEY = "hashed";
	private static final String SALT_KEY = "salt";

	private String identity;
	private boolean hashed = false;
	private String salt = "";

	public Identity(String serializedJson) {
		this(new JSONObject(serializedJson));
	}
	public Identity(JSONObject jsonObject) throws IllegalArgumentException {
		for (String key : jsonObject.keySet()) {
			if (IDENTITY_KEY.equals(key)) {
				if (jsonObject.get(IDENTITY_KEY) instanceof String identityString) {
					setIdentity(identityString);
				} else {
					throw new IllegalArgumentException("Invalid identity.");
				}
			} else if (Constants.TYPE_KEY.equals(key)) {
				if (!TYPE_VALUE.equals(jsonObject.get(Constants.TYPE_KEY))) {
					throw new IllegalArgumentException("Only identity type 'email' supported.");
				}
			} else if (HASHED_KEY.equals(key)) {
				if (jsonObject.get(HASHED_KEY) instanceof Boolean hashedBoolean) {
					setHashed(hashedBoolean);
				} else {
					throw new IllegalArgumentException("Invalid identity 'hashed' flag.");
				}
			} else if (SALT_KEY.equals(key)) {
				if (jsonObject.get(SALT_KEY) instanceof String saltString) {
					setSalt(saltString);
				} else {
					throw new IllegalArgumentException("Invalid identity 'salt'.");
				}
			}
		}
	}

	public JSONObject asJsonObject() {
		JSONObject jsonObject = new JSONObject();

		jsonObject.put(Constants.TYPE_KEY, TYPE_VALUE);
		jsonObject.put(IDENTITY_KEY, getIdentity());
		jsonObject.put(HASHED_KEY, isHashed());
		jsonObject.put(SALT_KEY, getSalt());

		return jsonObject;
	}

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public boolean isHashed() {
		return hashed;
	}

	public void setHashed(boolean hashed) {
		this.hashed = hashed;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}
}
