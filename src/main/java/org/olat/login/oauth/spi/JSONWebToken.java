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
package org.olat.login.oauth.spi;

import org.json.JSONException;
import org.json.JSONObject;
import org.olat.core.util.StringHelper;

import com.github.scribejava.core.model.OAuth2AccessToken;

/**
 * 
 * Initial date: 07.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class JSONWebToken {
	
	private final String header;
	private final String payload;
	private final JSONObject jsonPayload;
	
	public JSONWebToken(String header, String payload, JSONObject jsonPayload) {
		this.header = header;
		this.payload = payload;
		this.jsonPayload = jsonPayload;
	}
	
	public String getHeader() {
		return header;
	}

	public String getPayload() {
		return payload;
	}
	
	public JSONObject getJsonPayload() {
		return jsonPayload;
	}

	public static JSONWebToken parse(OAuth2AccessToken token) throws JSONException {
		String accessToken= token.getAccessToken();
		
		int firstIndex = accessToken.indexOf('.');
		int secondIndex = accessToken.indexOf('.', firstIndex + 1);
		
		String header = StringHelper.decodeBase64(accessToken.substring(0, firstIndex));
		String payload = StringHelper.decodeBase64(accessToken.substring(firstIndex, secondIndex));
		JSONObject jsonPayload = new JSONObject(payload);
		return new JSONWebToken(header, payload, jsonPayload);
	}
	
	public static JSONWebToken parse(String accessToken) throws JSONException {
		int firstIndex = accessToken.indexOf('.');
		int secondIndex = accessToken.indexOf('.', firstIndex + 1);
		
		String header = StringHelper.decodeBase64(accessToken.substring(0, firstIndex));
		String payload = StringHelper.decodeBase64(accessToken.substring(firstIndex, secondIndex));
		JSONObject jsonPayload = new JSONObject(payload);
		return new JSONWebToken(header, payload, jsonPayload);
	}
}
