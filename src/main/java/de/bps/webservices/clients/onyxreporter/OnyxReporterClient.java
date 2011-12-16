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
