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
package org.olat.modules.roommanagement;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Initial date: 22 Apr 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com
 */
@Service
public class RoomManagementModule extends AbstractSpringModule implements ConfigOnOff {

	private static final String ROOM_MANAGEMENT_ENABLED = "roommanagement.enabled";
	private static final String ROOM_MANAGEMENT_MAP_ENABLED = "roommanagement.map.enabled";

	@Value("${roommanagement.enabled:false}")
	private boolean enabled;
	@Value("${roommanagement.map.enabled:true}")
	private boolean mapEnabled;

	@Autowired
	public RoomManagementModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		updateProperties();
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}

	private void updateProperties() {
		String enabledObj = getStringPropertyValue(ROOM_MANAGEMENT_ENABLED, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}

		String mapEnabledObj = getStringPropertyValue(ROOM_MANAGEMENT_MAP_ENABLED, true);
		if (StringHelper.containsNonWhitespace(mapEnabledObj)) {
			mapEnabled = "true".equals(mapEnabledObj);
		}
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setStringProperty(ROOM_MANAGEMENT_ENABLED, Boolean.toString(enabled), true);
	}

	public boolean isMapEnabled() {
		return mapEnabled;
	}

	public void setMapEnabled(boolean mapEnabled) {
		this.mapEnabled = mapEnabled;
		setStringProperty(ROOM_MANAGEMENT_MAP_ENABLED, Boolean.toString(mapEnabled), true);
	}
}
