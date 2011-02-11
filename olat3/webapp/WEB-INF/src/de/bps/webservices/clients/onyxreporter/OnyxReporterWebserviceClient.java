
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
* Copyright (c) 1999-2007 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/
package de.bps.webservices.clients.onyxreporter;

import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.http.HTTPConstants;
import org.olat.core.logging.Tracing;

import de.bps.security.SSLConfigurationModule;
import de.bps.webservices.clients.onyxreporter.stubs.OnyxReporterStub;

/**
 * Description:<br>
 * TODO: thomasw Class Description for OnyxReporterWebserviceClient
 *
 * <P>
 * Initial Date:  28.08.2009 <br>
 * @author thomasw@bps-system.de
 */
public class OnyxReporterWebserviceClient {

	private OnyxReporterStub onyxReporterStub;

	public OnyxReporterWebserviceClient(String target) throws Exception {
		if(isServiceAvailable(target))
			try {
				init(target);
			} catch (AxisFault e) {
				throw new Exception(getClass() + " could not be initialised!", e);
			}
		else
			throw new Exception("Service at " + target + " not available !");
	}

	/**
	 * Checks if service is available
	 * @param target
	 */
	protected void init(String target) throws AxisFault {
		onyxReporterStub = new OnyxReporterStub();
		onyxReporterStub._getServiceClient().getOptions().setProperty(HTTPConstants.CHUNKED, false);
	}

	public boolean isServiceAvailable(String target) {

		HostnameVerifier hv = new HostnameVerifier() {
			public boolean verify(String urlHostName, SSLSession session) {
				if (urlHostName.equals(session.getPeerHost()))
					return true;
				else
					return false;
			}
		};
		HttpsURLConnection.setDefaultHostnameVerifier(hv);

		try {
			URL url = new URL(target + "?wsdl");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			if (con instanceof HttpsURLConnection) {
				HttpsURLConnection sslconn = (HttpsURLConnection) con;
				SSLContext context = SSLContext.getInstance("SSL");
				context.init(SSLConfigurationModule.getKeyManagers(),
						SSLConfigurationModule.getTrustManagers(),
						new java.security.SecureRandom());
				sslconn.setSSLSocketFactory(context.getSocketFactory());
				sslconn.connect();
				if (sslconn.getResponseCode() == HttpURLConnection.HTTP_OK) {
					sslconn.disconnect();
					return true;
				}
			} else {
				con.connect();
				if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
					con.disconnect();
					return true;
				}
			}
		} catch (Exception e) {
			Tracing.createLoggerFor(getClass()).error("Error while trying to connect to webservice: " + target, e);
		}
		return false;
	}

	/**
	 * @return Returns the onyxReporterStub.
	 */
	protected OnyxReporterStub getOnyxReporterStub() {
		return onyxReporterStub;
	}
}
