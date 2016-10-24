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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.onyx.plugin.wsclient;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;

import org.olat.core.CoreSpringFactory;

import de.bps.onyx.plugin.OnyxModule;

public class OnyxExamMode extends Service {

	private final static URL EXAM_SERVICE_WSDL_LOCATION;
	private final static Logger logger = Logger.getLogger(OnyxExamMode.class.getName());

	static {
		URL url = null;
		String location = CoreSpringFactory.getImpl(OnyxModule.class).getOnyxExamModeLocation();
		try {
			URL baseUrl;
			baseUrl = de.bps.onyx.plugin.wsclient.OnyxExamMode.class.getResource(".");
			url = new URL(baseUrl, location + "?wsdl");
		} catch (MalformedURLException e) {
			logger.warning("Failed to create URL for the wsdl Location: '" + location + "?wsdl', retrying as a local file");
			logger.warning(e.getMessage());
		}
		EXAM_SERVICE_WSDL_LOCATION = url;
	}

	public OnyxExamMode() {
		super(EXAM_SERVICE_WSDL_LOCATION, new QName("http://server.webservice.plugin.bps.de/", "OnyxExamService"));
	}
	/**
	 * 
	 * @returnURL returns OnyxPluginServices
	 */
	@WebEndpoint(name = "OnyxExamServicePort")
	public OnyxExamModeService getOnyxExamModeServicesPort() {
		return super.getPort(new QName("http://server.webservice.plugin.bps.de/", "OnyxExamServicePort"), OnyxExamModeService.class);
	}
}

/*
history:

$Log: OnyxExamMode.java,v $
Revision 1.2  2012-04-05 13:49:41  blaw
OLATCE-1425
* added history
* better indention
* refactored referencess for ExamPoolManagers to the abstract class
* added yesNoDialog for StartExam-function
* added more gui-warnings and / or fallback-values if student- or exam-values are not available


*/