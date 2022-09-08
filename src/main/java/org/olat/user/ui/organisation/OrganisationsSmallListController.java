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
package org.olat.user.ui.organisation;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.model.OrganisationWithParents;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 6 sept. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationsSmallListController extends BasicController {

	public OrganisationsSmallListController(UserRequest ureq, WindowControl wControl,
			List<OrganisationWithParents> organisations) {
		super(ureq, wControl);
		
		VelocityContainer mainVC = createVelocityContainer("organisation_small_list");
		
		List<OrganisationItem> organisationsList = new ArrayList<>();
		for(OrganisationWithParents organisation:organisations) {
			StringBuilder sb = new StringBuilder();
			for(Organisation parent:organisation.getParents()) {
				sb.append(StringHelper.escapeHtml(parent.getDisplayName())).append(" / ");
			}
			String displayName = StringHelper.escapeHtml(organisation.getDisplayName());
			organisationsList.add(new OrganisationItem(displayName, sb.toString()));
		}
		
		mainVC.contextPut("organisations", organisationsList);
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	public class OrganisationItem {
		
		private final String displayName;
		private final String parentLine;
		
		public OrganisationItem(String displayName, String parentLine) {
			this.displayName = displayName;
			this.parentLine = parentLine;
		}

		public String getDisplayName() {
			return displayName;
		}

		public String getParentLine() {
			return parentLine;
		}
	}
}
