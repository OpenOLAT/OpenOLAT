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
package org.olat.core.commons.services.webdav;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.olat.core.commons.services.webdav.servlets.WebResourceRoot;
import org.olat.core.util.UserSession;

/**
 * <h3>Description:</h3>
 * The web dav manager interface must be implemented to deal with user
 * authentication and so forth. Use the spring configuration to enable your
 * manager implementation.
 * <p>
 * Initial Date: 16.05.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public interface WebDAVManager {
	

	public static final String REQUEST_USERSESSION_KEY = "__usess";

	/**
	 * Handles authentication of OLAT users for the WevDAV servlet.
	 * 
	 * @param req
	 * @param resp
	 * @return True if user is successfully authenticated as a valid OLAT user,
	 *         false otherwise.
	 */
	public boolean handleAuthentication(HttpServletRequest req, HttpServletResponse resp);

	/**
	 * Calculate the user session object for a request
	 * 
	 * @param req
	 * @return
	 */
	public UserSession getUserSession(HttpServletRequest req);
	
	public WebResourceRoot getWebDAVRoot(HttpServletRequest req);

}