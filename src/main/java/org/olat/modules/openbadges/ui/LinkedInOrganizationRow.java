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
package org.olat.modules.openbadges.ui;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.openbadges.BadgeOrganization;

/**
 * Initial date: 2024-06-13<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class LinkedInOrganizationRow {

	private Object organizationId;
	private Object organizationName;
	private FormLink toolLink;
	private BadgeOrganization badgeOrganization;

	public Object getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(Object organizationId) {
		this.organizationId = organizationId;
	}

	public Object getOrganizationName() {
		return organizationName;
	}

	public void setOrganizationName(Object organizationName) {
		this.organizationName = organizationName;
	}

	public void setToolLink(FormLink toolLink) {
		this.toolLink = toolLink;
	}

	public FormLink getToolLink() {
		return toolLink;
	}

	public BadgeOrganization getBadgeOrganization() {
		return badgeOrganization;
	}

	public void setBadgeOrganization(BadgeOrganization badgeOrganization) {
		this.badgeOrganization = badgeOrganization;
	}
}
