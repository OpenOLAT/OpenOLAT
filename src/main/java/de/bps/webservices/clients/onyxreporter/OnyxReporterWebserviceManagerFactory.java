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
package de.bps.webservices.clients.onyxreporter;

import org.olat.core.logging.Tracing;

import de.bps.webservices.WebServiceModule;

/**
 * Description:<br>
 * TODO: thomasw Class Description for OnyxReporterWebserviceManagerFactory
 *
 * <P>
 * Initial Date:  28.08.2009 <br>
 * @author thomasw@bps-system.de
 */
public class OnyxReporterWebserviceManagerFactory {

	private static OnyxReporterWebserviceManagerFactory instance = new OnyxReporterWebserviceManagerFactory();

	/**
	 * @return Returns the instance.
	 */
	public static OnyxReporterWebserviceManagerFactory getInstance() {
		return instance;
	}

	/**
	 * Fabricates the webservice manager
	 * @param serviceKey
	 * @return webservice manager for magma
	 */
	public OnyxReporterWebserviceManager fabricate(String serviceKey) {
		OnyxReporterWebserviceManager manager;
		String target = WebServiceModule.getService(serviceKey).getAddress();
		try {
			manager = new OnyxReporterWebserviceManager(target);
			return manager;
		} catch (Exception e) {
			Tracing.createLoggerFor(OnyxReporterWebserviceManager.class).error("Creating webservice client failed", e);
			return null;
		}
	}

}
