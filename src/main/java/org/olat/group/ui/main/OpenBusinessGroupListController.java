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

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.WindowControl;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.BusinessGroupStatusEnum;
import org.olat.group.model.BusinessGroupQueryParams;
import org.olat.group.model.OpenBusinessGroupRow;
import org.olat.group.ui.main.BusinessGroupListFlexiTableModel.Cols;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OpenBusinessGroupListController extends AbstractBusinessGroupListController {
	
	public OpenBusinessGroupListController(UserRequest ureq, WindowControl wControl, String prefsKey) {
		super(ureq, wControl, "group_list", prefsKey, false);
	}
	
	@Override
	protected void initButtons(FormItemContainer formLayout, UserRequest ureq) {
		//
	}
	
	@Override
	protected FlexiTableColumnModel initColumnModel() {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.name.i18nHeaderKey(), Cols.name.ordinal(), TABLE_ACTION_LAUNCH,
				true, Cols.name.name(), new StaticFlexiCellRenderer(TABLE_ACTION_LAUNCH, new BusinessGroupNameCellRenderer())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.key));
		if(groupModule.isManagedBusinessGroups()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.externalId));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.description.i18nHeaderKey(), Cols.description.ordinal(),
				false, null, FlexiColumnModel.ALIGNMENT_LEFT, new TextFlexiCellRenderer(EscapeMode.antisamy)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.resources.i18nHeaderKey(), Cols.resources.ordinal(),
				true, Cols.resources.name(), FlexiColumnModel.ALIGNMENT_LEFT, new BGResourcesCellRenderer(flc)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.freePlaces.i18nHeaderKey(), Cols.freePlaces.ordinal(), TABLE_ACTION_LAUNCH,
				true, Cols.freePlaces.name(), new TextFlexiCellRenderer(EscapeMode.none)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.accessTypes.i18nHeaderKey(), Cols.accessTypes.ordinal(),
				true, Cols.accessTypes.name(), FlexiColumnModel.ALIGNMENT_LEFT, new BGAccessControlledCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.role.i18nHeaderKey(), Cols.role.ordinal(),
				true, Cols.role.name(), FlexiColumnModel.ALIGNMENT_LEFT, new BGRoleCellRenderer(getLocale())));
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.accessControlLaunch));
		
		return columnsModel;
	}
	
	@Override
	protected void initFilters() {
		tableEl.setSearchEnabled(true);
	}
	
	@Override
	protected void doLaunch(UserRequest ureq, BusinessGroup group) {	
		if(businessGroupService.isIdentityInBusinessGroup(getIdentity(), group)) {
			super.doLaunch(ureq, group);
		} else {
			String businessPath = "[GroupCard:" + group.getKey() + "]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		}
	}

	@Override
	protected BusinessGroupQueryParams getDefaultSearchParams() {
		BusinessGroupQueryParams params = new BusinessGroupQueryParams();
		params.setPublicGroups(Boolean.TRUE);
		params.setTechnicalTypes(List.of(BusinessGroup.BUSINESS_TYPE));
		params.setGroupStatus(List.of(BusinessGroupStatusEnum.active));
		return params;
	}
	
	@Override
	protected void changeFilterTab(UserRequest ureq, FlexiFiltersTab tab) {
		//
	}

	@Override
	protected List<BGTableItem> searchTableItems(BusinessGroupQueryParams params) {
		List<OpenBusinessGroupRow> rows = businessGroupService.findPublishedBusinessGroups(params, getIdentity());
		List<BGTableItem> items = new ArrayList<>(rows.size());
		for(OpenBusinessGroupRow row:rows) {
			BusinessGroupMembership membership = row.getMember();
			Boolean allowLeave =  membership != null;
			BGTableItem item = new BGTableItem(row, null, allowLeave, Boolean.FALSE);
			addAccessLink(item);
			items.add(item);
		}
		return items;
	}
	
	protected void addAccessLink(BGTableItem item) {
		String action;
		BusinessGroupMembership membership = item.getMembership();
		if(membership != null && membership.isOwner()) {
			return;
		} else if(membership != null && (membership.isParticipant() || membership.isWaiting())) {
			action = TABLE_ACTION_LEAVE;
		} else if(item.isFull() && !item.isWaitingListEnabled()) {
			action = null;
		} else {
			action = TABLE_ACTION_ACCESS;
		}
		
		String i18nKey;
		if (membership != null && membership.isParticipant()) {
			i18nKey = "table.header.leave";
		} else if (membership != null && membership.isWaiting()) {
			i18nKey = "table.header.leave.waiting";
		} else if(item.isFull()) {
			if(item.isWaitingListEnabled()) {
				i18nKey = "table.access.waitingList";
			} else {
				i18nKey = "table.header.group.full";
			}
		} else if(item.isWaitingListEnabled()) {
			if(item.isFull()) {
				i18nKey = "table.access.waitingList";
			}	else {
				i18nKey = "table.access";
			}
		} else {
			i18nKey = "table.access";
		}
		
		FormLink accessLink = uifactory.addFormLink("open_" + item.getBusinessGroupKey(), action, i18nKey,
				null, null, Link.LINK);
		if(action == null) {
			accessLink.setEnabled(false);
		}
		accessLink.setUserObject(item);
		item.setAccessLink(accessLink);
	}
}