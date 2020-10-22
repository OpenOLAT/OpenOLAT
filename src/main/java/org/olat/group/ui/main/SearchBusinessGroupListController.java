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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.services.mark.Mark;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.WindowControl;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.model.BusinessGroupQueryParams;
import org.olat.group.model.StatisticsBusinessGroupRow;
import org.olat.group.ui.main.BusinessGroupListFlexiTableModel.Cols;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SearchBusinessGroupListController extends AbstractStandardBusinessGroupListController {
	
	public SearchBusinessGroupListController(UserRequest ureq, WindowControl wControl, String prefsKey) {
		super(ureq, wControl, "group_list", true, true, prefsKey, true, null);
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
		FlexiTableColumnModel columnsModel = super.initColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.allowDelete.i18n(), Cols.allowDelete.ordinal(), TABLE_ACTION_DELETE,
			new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("table.header.delete"), TABLE_ACTION_DELETE), null)));
		return columnsModel;
	}

	@Override
	protected BusinessGroupQueryParams getSearchParams(SearchEvent event) {
		BusinessGroupQueryParams params = event.convertToBusinessGroupQueriesParams();
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
	protected BusinessGroupQueryParams getDefaultSearchParams() {
		BusinessGroupQueryParams params = new BusinessGroupQueryParams();
		//security
		if(!isAdmin()) {
			params.setOwner(true);
			params.setAttendee(true);
			params.setWaiting(true);
		}
		return params;
	}
	
	@Override
	protected void doLaunch(UserRequest ureq, BusinessGroup group) {
		if(isAdmin()) {
			ureq.getUserSession().putEntry("wild_card_" + group.getKey(), Boolean.TRUE);
		}
		super.doLaunch(ureq, group);
	}

	@Override
	protected List<BGTableItem> searchTableItems(BusinessGroupQueryParams params) {
		List<StatisticsBusinessGroupRow> rows = businessGroupService.findBusinessGroupsWithMemberships(params, getIdentity());
		List<BGTableItem> items = new ArrayList<>(rows.size());
		for(StatisticsBusinessGroupRow row:rows) {
			BusinessGroupMembership membership = row.getMember();
			Boolean allowLeave =  membership != null;
			Boolean allowDelete = isAdmin() ? Boolean.TRUE : (membership == null ? null : Boolean.valueOf(membership.isOwner()));
			
			FormLink markLink = uifactory.addFormLink("mark_" + row.getKey(), "mark", "", null, null, Link.NONTRANSLATED);
			markLink.setIconLeftCSS(row.isMarked() ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);

			BGTableItem item = new BGTableItem(row, markLink, allowLeave, allowDelete);
			item.setNumOfOwners(row.getNumOfCoaches());
			item.setNumOfParticipants(row.getNumOfParticipants());
			item.setNumWaiting(row.getNumWaiting());
			item.setNumOfPendings(row.getNumPending());
			items.add(item);
		}
		return items;
	}
}
