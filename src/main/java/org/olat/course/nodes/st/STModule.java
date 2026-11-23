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
package org.olat.course.nodes.st;

import org.olat.core.CoreSpringFactory;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Module containing the default config values for new STCourseNodes.
 *
 * Initial date: 2026-11-23<br>
 * @author cpfranger, christoph.pfranger@frentix.com, https://www.frentix.com
 */
@Service
public class STModule extends AbstractSpringModule {

	public static final String CONFIG_KEY_ST_DISPLAY_TYPE = "st.display.type";
	public static final String CONFIG_KEY_ST_COLUMNS = "st.columns";

	@Value("${st.display.type:delegate}")
	private String displayType;
	@Value("${st.columns:2}")
	private int columns;

	public STModule(CoordinatorManager coordinatorManager) {
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
		displayType = getStringPropertyValue(CONFIG_KEY_ST_DISPLAY_TYPE, displayType);
		columns = getIntPropertyValue(CONFIG_KEY_ST_COLUMNS, columns);
	}

	public String getDisplayType() {
		return displayType;
	}

	public void setDisplayType(String displayType) {
		this.displayType = displayType;
		setStringProperty(CONFIG_KEY_ST_DISPLAY_TYPE, displayType, true);
	}

	public int getColumns() {
		return columns;
	}

	public void setColumns(int columns) {
		this.columns = columns;
		setIntProperty(CONFIG_KEY_ST_COLUMNS, columns, true);
	}

	public void resetProperties() {
		removeProperty(CONFIG_KEY_ST_DISPLAY_TYPE, false);
		displayType = CoreSpringFactory.resolveProperty("st.display.type:delegate");
		removeProperty(CONFIG_KEY_ST_COLUMNS, false);
		columns = Integer.parseInt(CoreSpringFactory.resolveProperty("st.columns:2"));
		savePropertiesAndFireChangedEvent();
	}
}
