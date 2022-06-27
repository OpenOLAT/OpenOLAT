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
package org.olat.core.commons.services.csp;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.csp.model.CSPReport;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Initial date: 19 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("cspDispatcher")
public class CSPDispatcher implements Dispatcher {
	
	private static final Logger log = Tracing.createLoggerFor(CSPDispatcher.class);

	private static final JsonFactory jsonFactory = new JsonFactory();
	private static final ObjectMapper mapper = new ObjectMapper(jsonFactory);
	
	@Autowired
	private CSPModule cspModule;
	@Autowired
	private CSPManager cspManager;

	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		if(cspModule.isContentSecurityPolicyEnabled()
				&& "application/csp-report".equals(request.getHeader("content-type"))) {
			report(request);
		}
		response.setStatus(HttpServletResponse.SC_OK);
	}
	
	private void report(HttpServletRequest request) {
		try {
			Identity identity = null;
			HttpSession session = request.getSession(false);
			if(session != null) {
				UserSession us = (UserSession) session.getAttribute(UserSession.class.getName());
				if(us != null) {
					identity = us.getIdentity();
				}
			}
			CSPReport report = readReport(request.getInputStream());
			if(report != null) {
				cspManager.log(report, identity);
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	protected static CSPReport readReport(InputStream stream) {
		try {
			JsonNode report = mapper.readTree(stream);
			JsonNode reportNode = report.get("csp-report");
			return mapper.treeToValue(reportNode, CSPReport.class);
		} catch (IllegalArgumentException | IOException e) {
			log.warn("", e);
			return null;
		}
	}
}
