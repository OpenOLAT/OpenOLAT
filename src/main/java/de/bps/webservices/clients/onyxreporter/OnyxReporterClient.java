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

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;

//<ONYX-705>

@WebServiceClient(name = "ReporterWSService", targetNamespace = "http://server.webservice.plugin.bps.de/")
class OnyxReporterClient extends Service {

    private final static URL REPORTERSERVICE_WSDL_LOCATION;
    private final static OLog logger = Tracing.createLoggerFor(de.bps.webservices.clients.onyxreporter.OnyxReporterClient.class);

    static {
        URL url = null;
        try {
            url = new URL(OnyxReporterTarget.getTarget() + "?wsdl");
        } catch (MalformedURLException e) {
            logger.error("Failed to create URL for the wsdl Location: '"+ OnyxReporterTarget.getTarget() + "?wsdl', what now ?!?");
        }
        REPORTERSERVICE_WSDL_LOCATION = url;
    }
	
    
    OnyxReporterClient(){
    	super(REPORTERSERVICE_WSDL_LOCATION, new QName("http://server.webservice.plugin.bps.de/", "ReporterWSService"));
    }
    
    @WebEndpoint(name = "ReporterServicesPort")
    OnyxReporterServices getService() {
        return super.getPort(new QName("http://server.webservice.plugin.bps.de/", "ReporterServicesPort"), OnyxReporterServices.class);
    }
}
