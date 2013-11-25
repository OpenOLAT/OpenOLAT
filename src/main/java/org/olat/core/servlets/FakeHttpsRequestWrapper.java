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
 * Universität Innsbruck
 * <p>
 */
package org.olat.core.servlets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.olat.core.helpers.Settings;

/**
 * A RequestWrapper that "fakes" a https-connection:
 * I.e. regardless of the real connection type it answers
 * "true" for isSecure, "https" for the scheme and "443" for the server port. But
 * it only deliver these settings if OpenOLAT is set to use https.
 * 
 * Necessary for automatic WSDL generation (ONYX) behind an apache server that is
 * behind a haproxy
 *
 */
public class FakeHttpsRequestWrapper extends HttpServletRequestWrapper {

	public FakeHttpsRequestWrapper(HttpServletRequest request) {
		super(request);
	}

	@Override
	public boolean isSecure() {
		return Settings.isSecurePortAvailable();
	}

	@Override
	public String getScheme() {
		String scheme = Settings.getURIScheme();
		if(scheme == null) {
			scheme = isSecure() ? "https" : "http";
		} else if(scheme.endsWith(":")) {
			scheme = scheme.substring(0, scheme.length() - 1);
		}
		return scheme;
	}

	@Override
	public int getServerPort() {
		int serverPort;
		if(isSecure()) {
			serverPort = Settings.getServerSecurePort();
		} else {
			serverPort = Settings.getServerInsecurePort();
		}
		return serverPort;
	}
}
