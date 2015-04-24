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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.control.WindowControl;
import org.olat.group.BusinessGroup;
import org.olat.group.model.SearchBusinessGroupParams;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SearchBusinessGroupListController extends AbstractBusinessGroupListController {
	
	public SearchBusinessGroupListController(UserRequest ureq, WindowControl wControl, String prefsKey) {
		super(ureq, wControl, "group_list", true, true, prefsKey, null);
	}

	@Override
	protected void initButtons(FormItemContainer formLayout, UserRequest ureq) {
		initButtons(formLayout, ureq, true, false, true);
		if(isAdmin()) {
			searchCtrl.enableHeadless(true);
			searchCtrl.enableRoles(true);
			searchCtrl.enableId(true);
		}
	}
	
	@Override
	protected FlexiTableColumnModel initColumnModel() {
		return BusinessGroupFlexiTableModel.getStandardColumnModel(true, flc, groupModule, getTranslator());
	}

	@Override
	protected SearchBusinessGroupParams getSearchParams(SearchEvent event) {
		SearchBusinessGroupParams params = event.convertToSearchBusinessGroupParams(getIdentity());
		//security
		if(!isAdmin() && !params.isAttendee() && !params.isOwner() && !params.isWaiting()
				&& (params.getPublicGroups() == null || !params.getPublicGroups().booleanValue())) {
			params.setOwner(true);
			params.setAttendee(true);
			params.setWaiting(true);
		}
		return params;
	}

	@Override
	protected SearchBusinessGroupParams getDefaultSearchParams() {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		//security
		if(!isAdmin()) {
			params.setOwner(true);
			params.setAttendee(true);
			params.setWaiting(true);
		}
		params.setIdentity(getIdentity());
		return params;
	}
	
	@Override
	protected void doLaunch(UserRequest ureq, BusinessGroup group) {
		if(isAdmin()) {
			ureq.getUserSession().putEntry("wild_card_" + group.getKey(), Boolean.TRUE);
		}
		super.doLaunch(ureq, group);
	}
}
