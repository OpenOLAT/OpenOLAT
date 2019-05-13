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
package org.olat.core.commons.services.webdav.manager;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.http.Authorization;

/**
 * 
 * Contains the informations needed to test the authentication with
 * the Digest algorithm
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DigestAuthentication {
	
	private static final Logger log = Tracing.createLoggerFor(DigestAuthentication.class);
	
	private final String username;
	private final String realm;
	private final String nonce;
	private final String uri;
	private final String cnonce;
	private final String nc;
	private final String response;
	private final String qop;
	
	public DigestAuthentication(String username, String realm, String nonce, String uri, String cnonce, String nc, String response, String qop) {
		this.username = username;
		this.realm = realm;
		this.nonce = nonce;
		this.uri = uri;
		this.cnonce = cnonce;
		this.nc = nc;
		this.response = response;
		this.qop = qop;
	}
	
	public String getUsername() {
		return username;
	}

	public String getRealm() {
		return realm;
	}

	public String getNonce() {
		return nonce;
	}

	public String getUri() {
		return uri;
	}

	public String getCnonce() {
		return cnonce;
	}

	public String getNc() {
		return nc;
	}

	public String getResponse() {
		return response;
	}

	public String getQop() {
		return qop;
	}

	public static DigestAuthentication parse(String header) {
		String username = null;
		String realm = null;
		String nonce = null;
		String uri = null;
		String cnonce = null;
		String nc = null;
		String response = null;
		String qop = null;
		
		try {
			Map<String,String> parsedHeader = Authorization.parseAuthorizationDigest(new StringReader(header));
			username = parsedHeader.get("username");
			realm = parsedHeader.get("realm");
			nonce = parsedHeader.get("nonce");
			uri = parsedHeader.get("uri");
			cnonce = parsedHeader.get("cnonce");
			nc = parsedHeader.get("nc");
			response = parsedHeader.get("response");
			qop = parsedHeader.get("qop");
		} catch (IOException | IllegalArgumentException e) {
			log.error("", e);
		}
		return new DigestAuthentication(username, realm, nonce, uri, cnonce, nc, response, qop);
	}
}
