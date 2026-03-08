/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.gui.control.generic.dashboard;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thoughtworks.xstream.XStream;

/**
 * Manages system-wide default dashboard configurations stored in the
 * o_property table. Uses PropertyManager with null identity/group/resource
 * for system-level storage.
 *
 * Initial date: Mar 08, 2026<br>
 * @author gnaegi, https://www.frentix.com
 */
@Service
public class DashboardSystemDefaultsManager {

	private static final Logger log = Tracing.createLoggerFor(DashboardSystemDefaultsManager.class);
	private static final String CATEGORY = "dashboard.system.default";

	private static final XStream xstream = XStreamHelper.createXStreamInstance();
	static {
		XStreamHelper.allowDefaultPackage(xstream);
	}

	@Autowired
	private PropertyManager propertyManager;

	/**
	 * Load the system default configuration for a dashboard.
	 *
	 * @param dashboardId the unique dashboard identifier
	 * @return the system default preferences, or null if none are configured
	 */
	public DashboardPrefs loadSystemDefault(String dashboardId) {
		Property prop = propertyManager.findProperty(null, null, null, CATEGORY, dashboardId);
		if (prop != null && prop.getTextValue() != null) {
			try {
				Object obj = xstream.fromXML(prop.getTextValue());
				if (obj instanceof DashboardPrefs prefs) {
					return prefs;
				}
			} catch (Exception e) {
				log.error("Failed to deserialize system default for dashboard: {}", dashboardId, e);
			}
		}
		return null;
	}

	/**
	 * Save or update the system default configuration for a dashboard.
	 * Creates a new property entry if none exists, otherwise updates the existing one.
	 *
	 * @param dashboardId the unique dashboard identifier
	 * @param prefs the default widget configuration to store
	 */
	public void saveSystemDefault(String dashboardId, DashboardPrefs prefs) {
		String xml = xstream.toXML(prefs);
		Property prop = propertyManager.findProperty(null, null, null, CATEGORY, dashboardId);
		if (prop != null) {
			prop.setTextValue(xml);
			propertyManager.updateProperty(prop);
		} else {
			prop = propertyManager.createPropertyInstance(null, null, null,
					CATEGORY, dashboardId, null, null, null, xml);
			propertyManager.saveProperty(prop);
		}
	}

	/**
	 * Delete the system default configuration for a dashboard.
	 * After deletion, dashboards without personal preferences will
	 * fall back to showing all widgets in registration order.
	 *
	 * @param dashboardId the unique dashboard identifier
	 */
	public void deleteSystemDefault(String dashboardId) {
		Property prop = propertyManager.findProperty(null, null, null, CATEGORY, dashboardId);
		if (prop != null) {
			propertyManager.deleteProperty(prop);
		}
	}
}
