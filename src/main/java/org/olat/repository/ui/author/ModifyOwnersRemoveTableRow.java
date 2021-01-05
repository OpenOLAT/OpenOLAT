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
package org.olat.repository.ui.author;

import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.Identity;

/**
 * Initial date: Jan 3, 2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ModifyOwnersRemoveTableRow {
	
	private Identity identity;
	private List<AuthoringEntryRow> resources;
	private FormLink detailsLink;
	
	public ModifyOwnersRemoveTableRow(Identity identity, List<AuthoringEntryRow> resources) {
		this.identity = identity;
		this.resources = resources;
	}
	
	public Identity getIdentity() {
		return identity;
	}
	
	public List<AuthoringEntryRow> getResources() {
		return resources;
	}
	
	public int getResourcesCount() {
		if (resources == null || resources.isEmpty()) {
			return 0;
		} else {
			return resources.size();
		}
	}
	
	public FormLink getDetailsLink() {
		return detailsLink;
	}
	
	public void setDetailsLink(FormLink detailsLink) {
		this.detailsLink = detailsLink;
	}
}
