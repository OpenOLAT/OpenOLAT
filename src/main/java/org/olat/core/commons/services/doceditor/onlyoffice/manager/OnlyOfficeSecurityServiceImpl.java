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
package org.olat.core.commons.services.doceditor.onlyoffice.manager;

import java.io.IOException;
import java.security.Key;
import java.util.Map.Entry;

import org.olat.core.commons.services.doceditor.onlyoffice.Callback;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeModule;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeSecurityService;
import org.olat.core.commons.services.doceditor.onlyoffice.model.CallbackImpl;
import org.olat.core.commons.services.doceditor.onlyoffice.model.DocumentImpl;
import org.olat.core.commons.services.doceditor.onlyoffice.model.EditorConfigImpl;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * 
 * Initial date: 19 Apr 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class OnlyOfficeSecurityServiceImpl implements OnlyOfficeSecurityService {

	private static final Logger log = Tracing.createLoggerFor(OnlyOfficeSecurityServiceImpl.class);
	
	private static ObjectMapper mapper = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	@Autowired
	private OnlyOfficeModule onlyOfficeModule;

	@Override
	public boolean isValidSecret(String secret) {
		try {
			Key signKey = Keys.hmacShaKeyFor(secret.getBytes());
			Jwts.builder().setIssuer("issuer").signWith(signKey).compact();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	@Override
	public Callback getCallback(String jwtToken) {
		try {
			return tryGetCallback(jwtToken);
		} catch (Exception e) {
			log.error("Error while converting JWT token to Callback", e);
		}
		return null;
	}

	private Callback tryGetCallback(String jwtToken) throws IOException, JsonParseException, JsonMappingException {
		Claims body = Jwts.parser()
				.setSigningKey(onlyOfficeModule.getJwtSignKey())
				.parseClaimsJws(jwtToken)
				.getBody();
		
		if (log.isDebugEnabled()) {
			log.debug("JWT claims for ONLYOFFICE callback:");
			for (Entry<String, Object> entry : body.entrySet()) {
				log.debug("  JWT claim " + entry.getKey() + ": " + entry.getValue());
			}
		}
		
		Object payload = body.get("payload");
		Callback callback = mapper.convertValue(payload, CallbackImpl.class);
		
		if (log.isDebugEnabled()) log.debug("Converted callback: " + callback);
		return callback;
	}

	@Override
	public String getApiConfigToken(DocumentImpl document, EditorConfigImpl editorConfig) {
		return Jwts.builder()
				.claim("document", document)
				.claim("editorConfig", editorConfig)
				.signWith(onlyOfficeModule.getJwtSignKey())
				.compact();
	}

	@Override
	public String getFileDonwloadToken() {
		return Jwts.builder()
				.setSubject("download") // not specified by ONLYOFFICE, but jjwt forces us to set something
				.signWith(onlyOfficeModule.getJwtSignKey())
				.compact();
	}

}
