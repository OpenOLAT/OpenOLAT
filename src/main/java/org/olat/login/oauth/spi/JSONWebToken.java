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

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

import com.github.scribejava.core.model.OAuth2AccessToken;

/**
 * 
 * Initial date: 07.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class JSONWebToken {
	
	private static final Logger log = Tracing.createLoggerFor(JSONWebToken.class);
	
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
		return parse(token.getAccessToken());
	}

	public static JSONWebToken parse(String accessToken) throws JSONException {
		try {
			int firstIndex = accessToken.indexOf('.');
			int secondIndex = accessToken.indexOf('.', firstIndex + 1);
			String header = StringHelper.decodeBase64(accessToken.substring(0, firstIndex));
			String payload = decodeBase64(accessToken.substring(firstIndex + 1, secondIndex));
			log.debug("JWT Payload: {}", payload);
			JSONObject jsonPayload = new JSONObject(payload);
			return new JSONWebToken(header, payload, jsonPayload);
		} catch (JSONException e) {
			log.error("Cannot parse token: {}", accessToken);
			throw e;
		} catch (Exception e) {
			log.error("Cannot parse token: {}", accessToken);
			throw new JSONException(e);
		}
	}
	
	/**
	 * The method try 2 different way to decode the content and check
	 * that we can parse the JSON content.
	 * 
	 * @param content The content to decode
	 * @return The decoded string
	 * @throws Exception
	 */
	private static final String decodeBase64(String content) throws Exception {
		try {
			String decodedContent = StringHelper.decodeBase64(content);
			new JSONObject(decodedContent);
			return decodedContent;
		} catch (JSONException e) {
			try {
				byte[] contentBytes = Base64.getUrlDecoder().decode(content);
				String decodedContent = new String(contentBytes, StandardCharsets.UTF_8);
				new JSONObject(decodedContent);
				return decodedContent;
			} catch (Exception e1) {
				throw e;
			}
		}
	}
}
