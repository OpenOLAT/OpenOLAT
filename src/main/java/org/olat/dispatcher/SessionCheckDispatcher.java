/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.dispatcher;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.logging.Tracing;
import org.olat.core.util.UserSession;
import org.olat.core.util.session.UserSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A very minimal dispatcher which return 200 if the session of the user
 * is authenticated, 401 otherwise.
 * 
 * Initial date: 22 mars 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("sessioncheckerbean")
public class SessionCheckDispatcher implements Dispatcher {
	
	private static final Logger log = Tracing.createLoggerFor(SessionCheckDispatcher.class);

	@Autowired
	private UserSessionManager userSessionManager;
	
	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		UserSession usess = userSessionManager.getUserSessionIfAlreadySet(request);

		JSONObject json = new JSONObject();
		if(usess != null && usess.isAuthenticated()) {
			json.put("hello", true);
			response.setStatus(HttpServletResponse.SC_OK);
		} else {
			json.put("hello", false);
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		}

		try(ServletOutputStream out=response.getOutputStream()) {
			String txt = json.toString();
			out.print(txt);
			response.setContentLengthLong(txt.getBytes().length);
			response.setContentType("application/json");
		} catch(Exception e) {
			log.error("", e);
		}
	}
}
