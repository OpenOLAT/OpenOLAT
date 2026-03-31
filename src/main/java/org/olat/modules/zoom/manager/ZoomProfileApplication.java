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
package org.olat.modules.zoom.manager;

import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.ims.lti13.LTI13Context;
import org.olat.modules.zoom.ZoomManager;

/**
 * Initial date: 2026-03-30<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ZoomProfileApplication {
	private final String name;
	private final String description;

	private final LTI13Context lti13Context;

	public ZoomProfileApplication(Object[] objectArray) {
		this.name = PersistenceHelper.extractString(objectArray, 0);
		this.description = PersistenceHelper.extractString(objectArray, 1);
		this.lti13Context = (LTI13Context) objectArray[2];
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public LTI13Context getLti13Context() {
		return lti13Context;
	}

	public ZoomManager.ApplicationType getApplicationType() {
		if (description == null) {
			return null;
		}

		String[] parts = description.split("-");
		if (parts.length < 2) {
			return null;
		}

		ZoomManager.ApplicationType applicationType = ZoomManager.ApplicationType.valueOf(parts[0]);
		if (applicationType != ZoomManager.ApplicationType.groupTool && parts.length < 3) {
			return null;
		}

		return applicationType;
	}
}
