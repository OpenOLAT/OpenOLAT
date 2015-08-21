
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
package de.bps.webservices;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.configuration.Initializable;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;

/**
 * 
 */
public class WebServiceModule implements Initializable {

	private static Map<String, Service> configuration = new HashMap<String, Service>();

	private static Map<String, List<String>> services;

	private final static OLog log = Tracing.createLoggerFor(WebServiceModule.class);

	/**
	 * @return Returns the services.
	 */
	public static Map<String, List<String>> getServices() {
		return services;
	}

	/**
	 * @param services The services to set.
	 */
	public void setServices(Map<String, List<String>> services) {
		WebServiceModule.services = services;
	}

	/*
	private static final String SERVICE = "service";
	private static final String SERVICE_NAME = "name";
	private static final String TARGET = "target";
	private static final String ID = "id";
	private static final String INSTITUTIONAL_NAME = "institutional_name";
	private static final String ADDRESS = "address";
	*/

	/**
	 * [used by spring]
	 */
	private WebServiceModule() {
		//
	}

	@Override
	public void init() {
		try {
			if (services != null) {
				for (String serviceName : services.keySet()) {
					Map<String, String> mapIdToInstName = new HashMap<String, String>();
					Map<String, String> mapInstNameToAddress = new HashMap<String, String>();
						String id = services.get(serviceName).get(0);
						String instName = Service.ALL_INSTITUTIONS;
						String address = services.get(serviceName).get(1);
						mapIdToInstName.put(id, instName);
						mapInstNameToAddress.put(instName, address);
						log.audit(serviceName + " # " + id + " # " + instName + " # " + address);
					Service service = new Service(serviceName, mapIdToInstName, mapInstNameToAddress);
					configuration.put(service.getName(), service);
				}
			}else {
				log.info("No services found to initialize.");
			}
		} catch (Exception e) {
			log.error("Initialization failed", e);
		}
	}
	
	/**
	 * returns a list of all loaded service objects
	 *
	 * @see Service return list of service objects
	 */
	public static List<Service> getAllServices() {
		return (List<Service>) configuration.values();
	}

	/**
	 * @return true if a service with this name exists, has no correlation to
	 * any institution
	 */
	public static boolean existService(String service) {
		if (configuration.get(service) != null)
			return true;
		return false;
	}

	/**
	 * @return the service object for the given name
	 */
	public static Service getService(String serviceKey) {
		return configuration.get(serviceKey);
	}

}
