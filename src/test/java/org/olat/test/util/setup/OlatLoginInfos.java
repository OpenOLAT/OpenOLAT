/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
*/
package org.olat.test.util.setup;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Simple wrapper object around login information
 * to log into OLAT.
 * <p>
 * The information provided here should be sufficient to
 * create a selenium session with it and log into OLAT.
 * Hence we need the hostname of the selenium server
 * and the hostname + login details of the OLAT server.
 * @author Stefan
 *
 */
public class OlatLoginInfos {

	private final String seleniumHostname_;
	private final String seleniumBrowserId_;
	private final URL fullOlatServerUrl_;
	private final String username_;
	private final String password_;
	
	public OlatLoginInfos(String seleniumHostname, String seleniumBrowserId,
							String fullOlatServerUrl, String username, String password) throws MalformedURLException {
		if (isNullOrEmpty(seleniumHostname)) {
			throw new IllegalArgumentException("seleniumHostname is null or empty");
		}
		if (isNullOrEmpty(seleniumBrowserId)) {
			throw new IllegalArgumentException("seleniumBrowserId is null or empty");
		}
		if (isNullOrEmpty(fullOlatServerUrl)) {
			throw new IllegalArgumentException("fullOlatServerUrl is null or empty");
		}
		if (isNullOrEmpty(username)) {
			throw new IllegalArgumentException("username is null or empty");
		}
		if (isNullOrEmpty(password)) {
			throw new IllegalArgumentException("password is null or empty");
		}
		seleniumHostname_ = seleniumHostname;
		seleniumBrowserId_ = seleniumBrowserId;
		fullOlatServerUrl_ = new URL(fullOlatServerUrl);
		username_ = username;
		password_ = password;
	}
	
	private boolean isNullOrEmpty(String param) {
		return param==null || param.length()==0;
	}

	public String getSeleniumHostname() {
		return seleniumHostname_;
	}
	
	public String getSeleniumBrowserId() {
		return seleniumBrowserId_;
	}
	
	public String getFullOlatServerUrl() {
		return fullOlatServerUrl_.toExternalForm();
	}
	
	public String getUsername() {
		return username_;
	}
	
	public String getPassword() {
		return password_;
	}

	public String getLanguage() {
		return "English";
	}
}
