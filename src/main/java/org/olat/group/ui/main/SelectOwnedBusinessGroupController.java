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
package org.olat.group.ui.main;

import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.control.WindowControl;
import org.olat.group.BusinessGroup;
import org.olat.group.model.BusinessGroupQueryParams;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SelectOwnedBusinessGroupController extends AbstractSelectBusinessGroupListController {
	
	public SelectOwnedBusinessGroupController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "group_list", "sel-owned", false);
		
		searchCtrl.setPreselectedRoles(GroupRoles.coach);
	}

	@Override
	protected boolean canCreateBusinessGroup(UserRequest ureq) {
		return false;
	}

	@Override
	protected void initButtons(FormItemContainer formLayout, UserRequest ureq) {
		initButtons(formLayout, ureq, false, true, false);
		tableEl.setSearchEnabled(false);
	}

	@Override
	protected BusinessGroupQueryParams getSearchParams(SearchEvent event) {
		BusinessGroupQueryParams params = event.convertToBusinessGroupQueriesParams();
		params.setTechnicalTypes(List.of(BusinessGroup.BUSINESS_TYPE));
		params.setOwner(true);
		params.setAttendee(false);
		params.setWaiting(false);
		return params;
	}

	@Override
	protected BusinessGroupQueryParams getDefaultSearchParams() {
		BusinessGroupQueryParams params = new BusinessGroupQueryParams(true, false);
		params.setTechnicalTypes(List.of(BusinessGroup.BUSINESS_TYPE));
		params.setOwner(true);
		params.setAttendee(false);
		params.setWaiting(false);
		return params;
	}
}
