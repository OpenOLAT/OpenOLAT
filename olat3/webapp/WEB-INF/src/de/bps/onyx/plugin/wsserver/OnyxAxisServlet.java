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
* <p>
*/
package de.bps.onyx.plugin.wsserver;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.transport.http.AxisServlet;
import org.olat.core.helpers.Settings;

import de.bps.onyx.plugin.OnyxModule;

/**
 * Description:<br>
 * This class extends the AxisServlet class to provide an alternate
 * "getEPRsForService" method.
 * 
 * <P>
 * Initial Date:  15.07.2010 <br>
 * @author thomasw
 */
public class OnyxAxisServlet extends AxisServlet {

	/**
	 * 
	 */
	public OnyxAxisServlet() {
		super();
	}

	/**
	 * The original method always sets "http" (never https) and the ip adress in the wsdl, but this is not what we need.
	 * @see org.apache.axis2.transport.http.AxisServlet#getEPRsForService(java.lang.String, java.lang.String)
	 */
	@Override
	public EndpointReference[] getEPRsForService(String serviceName,
      String ip) throws AxisFault {
		EndpointReference epr = null;
	
		String eprString = Settings.createServerURI();
		if (configContext.getServiceContextPath().startsWith("/")) {
			epr = new EndpointReference( eprString + configContext.getServiceContextPath() + "/" + serviceName + "/");
		} else {
			epr = new EndpointReference( eprString + "/" + configContext.getServiceContextPath() + "/" + serviceName + "/");
		}
		return new EndpointReference[] {epr};
	}
	
}
