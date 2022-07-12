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
package org.olat.ims.lti13;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 8 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service(value="ltidispatcherbean")
public class LTI13Dispatcher implements Dispatcher {
	
	private static final Logger log = Tracing.createLoggerFor(LTI13Dispatcher.class);
	
	public static final String LTI_PATH = "/lti";
	public static final String LTI_AUTHORIZATION_PATH = LTI_PATH + "/auth";
	public static final String LTI_TOKEN_PATH = LTI_PATH + "/token";
	public static final String LTI_JWKSET_PATH = LTI_PATH + "/keys";
	public static final String LTI_NRPS_PATH = LTI_PATH + "/nrps";
	public static final String LTI_AGS_PATH = LTI_PATH + "/ags";
	public static final String LTI_BS_PATH = LTI_PATH + "/outcomes";

	public static final String LTI_LOGIN_INITIATION_PATH = LTI_PATH + "/login_initiation";
	public static final String LTI_LOGIN_REDIRECT_PATH = LTI_PATH + "/login";
	
	@Autowired
	private LTI13Module lti13Module;
	/**
	 * OpenOlat as a tool for other LMS.
	 */
	@Autowired
	private LTI13PlatformDispatcherDelegate platformDelegate;
	
	/**
	 * OpenOlat handles external LTI tools started as course elements.
	 */
	@Autowired
	private LTI13ToolDispatcherDelegate toolDelegate;
	
	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		final String uriPrefix = DispatcherModule.getLegacyUriPrefix(request);
		final String origUri = request.getRequestURI();
		String ltiUri = origUri.substring(uriPrefix.length());
		log.debug("LTI dispatcher: {}", ltiUri);
		execute(ltiUri, request, response);
	}
	
	protected void execute(String ltiUri, HttpServletRequest request, HttpServletResponse response) {
		if(!lti13Module.isEnabled()) {
			DispatcherModule.sendForbidden("not_enabled", response);
			return;
		}
		
		String[] path = ltiUri.split("[/]");
		if(path.length >= 1) {
			String first = path[0];
			log.debug("Handle: {}", first);
			
			if("login_initiation".equals(first)) {
				toolDelegate.handleInitiationLogin(request, response);// platform
			} else if("login".equals(first)) {
				toolDelegate.handleLogin(request, response);// platform
			} else if("keys".equals(first)) {
				if(path.length >= 2) {
					platformDelegate.handleKey(path[1], response);
				} else {
					platformDelegate.handleKeys(response);
				}
			} else if("auth".equals(first)) {
				platformDelegate.handleAuthorization(request, response);// tool
			} else  if("nrps".equals(first)) {
				platformDelegate.handleNrps(path, request, response);// tool
			} else if("ags".equals(first)) {
				platformDelegate.handleAgs(path, request, response);// tool
			} else if("token".equals(first)) {
				if(path.length >= 2) {// tool
					String obj = path[1];
					log.debug("Return token: {}", obj);
					platformDelegate.handleToken(obj, request, response);
				} else {
					platformDelegate.handleToken(request, response);
				}
			}
		}
	}

}
