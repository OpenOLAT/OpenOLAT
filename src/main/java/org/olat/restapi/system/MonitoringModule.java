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
package org.olat.restapi.system;

import java.io.File;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service("monitoringModule")
public class MonitoringModule extends AbstractSpringModule implements ConfigOnOff {
	
	private static final Logger log = Tracing.createLoggerFor(MonitoringModule.class);

	private static final String ENABLED = "monitoring.enabled";
	private static final String MONITORED_PROBES = "probes";
	private static final String SERVER = "dependency.server";
	private static final String DESCRIPTION = "description";

	@Value("${monitoring.enabled}")
	private boolean enabled;
	private String databaseHost;
	@Value("${monitored.probes}")
	private String monitoredProbes;
	@Value("${monitoring.dependency.server}")
	private String server;
	@Value("${monitoring.instance.description}")
	private String description;
	@Value("${monitoring.proc.file:}")
	private String procFile;
	
	@Autowired
	public MonitoringModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}
	
	@Override
	public void init() {
		String enabledObj = getStringPropertyValue(ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		String probesObj = getStringPropertyValue(MONITORED_PROBES, true);
		if(StringHelper.containsNonWhitespace(probesObj)) {
			monitoredProbes = probesObj;
		}
		String serverObj = getStringPropertyValue(SERVER, true);
		if(StringHelper.containsNonWhitespace(serverObj)) {
			server = serverObj;
		}
		String descriptionObj = getStringPropertyValue(DESCRIPTION, true);
		if(StringHelper.containsNonWhitespace(descriptionObj)) {
			description = descriptionObj;
		}
		
		if(StringHelper.containsNonWhitespace(procFile)) {
			File xmlFile = new File(procFile);
			if(!xmlFile.exists()) {
				File parent = xmlFile.getParentFile();
				if(!parent.exists() || !parent.canWrite()) {
					log.warn("Cannot write proc file: " + xmlFile);
				}
			} else if (!xmlFile.canWrite()) {
				log.warn("Cannot write proc file: " + xmlFile);
			}
		}
	}
	

	@Override
	protected void initFromChangedProperties() {
		init();
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		setBooleanProperty(ENABLED, enabled, true);
	}

	public String getDatabaseHost() {
		return databaseHost;
	}

	public void setDatabaseHost(String databaseHost) {
		this.databaseHost = databaseHost;
	}

	public String getMonitoredProbes() {
		return monitoredProbes;
	}

	public void setMonitoredProbes(String monitoredProbes) {
		setStringProperty(MONITORED_PROBES, monitoredProbes, true);
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		setStringProperty(SERVER, server, true);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		setStringProperty(DESCRIPTION, description, true);
	}

	public String getProcFile() {
		return procFile;
	}
}
