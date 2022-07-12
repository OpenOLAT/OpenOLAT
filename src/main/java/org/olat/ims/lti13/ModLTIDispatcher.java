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
 * Mimic the LTI implementation of Moodle.
 * 
 * Initial date: 4 juil. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service(value="modltidispatcherbean")
public class ModLTIDispatcher implements Dispatcher {

	private static final Logger log = Tracing.createLoggerFor(ModLTIDispatcher.class);
	
	@Autowired
	private LTI13Dispatcher lti13Dispatcher;
	
	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		final String uriPrefix = DispatcherModule.getLegacyUriPrefix(request);
		final String origUri = request.getRequestURI();
		String ltiUri = origUri.substring(uriPrefix.length());
		String rewritedUri = ltiUri;
		if(ltiUri.contains("auth.php")) {
			rewritedUri = "auth";
		} else if(ltiUri.contains("token.php")) {
			rewritedUri = "token";
		} else if(ltiUri.contains("certs.php")) {
			rewritedUri = "keys";
		}

		log.debug("Mod LTI dispatcher: {} (original: {})", rewritedUri, ltiUri);
		lti13Dispatcher.execute(rewritedUri, request, response);
	}
}
