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
package org.olat.restapi;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.olat.core.configuration.PersistedProperties;
import org.olat.core.configuration.PersistedPropertiesChangedEvent;
import org.olat.core.gui.control.Event;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;

/**
 * 
 * Description:<br>
 * Configuration of the REST API
 * 
 * <P>
 * Initial Date:  18 juin 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class RestModule implements GenericEventListener {
	
	private static final String ENABLED = "enabled";
	
	private Boolean enabled;
	private Boolean defaultEnabled;
	private String ipsByPass;
	private PersistedProperties persistedProperties;
	
	private String monitoredProbes;
	
	public RestModule() {
		//
	}
	
	public void setCoordinator(CoordinatorManager coordinatorManager) {
		//nothing to do
	}
	
	/**
	 * [used by spring]
	 * @param persistedProperties
	 */
	public void setPersistedProperties(PersistedProperties persistedProperties) {
		this.persistedProperties = persistedProperties;
	}
	
	public Boolean getDefaultEnabled() {
		return defaultEnabled;
	}

	public void setDefaultEnabled(Boolean defaultEnabled) {
		this.defaultEnabled = defaultEnabled;
	}
	
	public boolean isEnabled() {
		if(enabled == null) {
			String enabledStr = persistedProperties.getStringPropertyValue(ENABLED, true);
			enabled = StringHelper.containsNonWhitespace(enabledStr) ? "enabled".equals(enabledStr) : defaultEnabled.booleanValue();
		}
		return enabled.booleanValue();
	}
	
	public void setEnabled(boolean enabled) {
		if (getPersistedProperties() != null) {
			String enabledStr = enabled ? "enabled" : "disabled";
			getPersistedProperties().setStringProperty(ENABLED, enabledStr, true);
		}
	}
	
	public String getIpsByPass() {
		return ipsByPass;
	}
	
	public List<String> getIpsWithSystemAccess() {
		List<String> ips = new ArrayList<String>();
		for(StringTokenizer tokenizer=new StringTokenizer(ipsByPass, ",;|"); tokenizer.hasMoreTokens(); ) {
			String token = tokenizer.nextToken();
			if(StringHelper.containsNonWhitespace(token)) {
				ips.add(token);
			}
		}
		return ips;
	}

	public void setIpsByPass(String ipsByPass) {
		this.ipsByPass = ipsByPass;
	}

	/**
	 * @return the persisted properties
	 */
	private PersistedProperties getPersistedProperties() {
		return persistedProperties;
	}
	
	@Override
	public void event(Event event) {
		if (event instanceof PersistedPropertiesChangedEvent) {
			// Reload the properties
			if (!((PersistedPropertiesChangedEvent)event).isEventOnThisNode()) {
				persistedProperties.loadPropertiesFromFile();
			}
			enabled = null;
		}
	}

	public String getMonitoredProbes() {
		return monitoredProbes;
	}

	public void setMonitoredProbes(String monitoredProbes) {
		this.monitoredProbes = monitoredProbes;
	}
	
	

}
