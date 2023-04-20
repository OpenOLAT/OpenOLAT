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
package org.olat.modules.jupyterhub;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Initial date: 2023-04-13<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class JupyterHubModule extends AbstractSpringModule implements ConfigOnOff {

	private static final String JUPYTER_HUB_ENABLED = "jupyterHub.enabled";
	private static final String JUPYTER_HUB_ENABLED_FOR_COURSE_ELEMENT = "jupyterHub.enabledForCourseElement";

	@Value("${jupyterHub.enabled:false}")
	private boolean enabled;

	@Value("${jupyterHub.enabledForCourseElement:false}")
	private boolean enabledForCourseElement;

	public JupyterHubModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		String enabledObj = getStringPropertyValue(JUPYTER_HUB_ENABLED, true);
		if (StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}

		String enabledForCourseElementObj = getStringPropertyValue(JUPYTER_HUB_ENABLED_FOR_COURSE_ELEMENT, true);
		if (StringHelper.containsNonWhitespace(enabledForCourseElementObj)) {
			enabledForCourseElement = "true".equals(enabledForCourseElementObj);
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
		this.enabled = enabled;
		setStringProperty(JUPYTER_HUB_ENABLED, Boolean.toString(enabled), true);
	}

	public boolean isEnabledForCourseElement() {
		return enabledForCourseElement;
	}

	public void setEnabledForCourseElement(boolean enabledForCourseElement) {
		this.enabledForCourseElement = enabledForCourseElement;
		setStringProperty(JUPYTER_HUB_ENABLED_FOR_COURSE_ELEMENT, Boolean.toString(enabled), true);
	}
}
