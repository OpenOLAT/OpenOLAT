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

import static org.olat.group.ui.main.AbstractBusinessGroupListController.TABLE_ACTION_DELETE;
import static org.olat.group.ui.main.AbstractBusinessGroupListController.TABLE_ACTION_LAUNCH;
import static org.olat.group.ui.main.AbstractBusinessGroupListController.TABLE_ACTION_LEAVE;
import static org.olat.group.ui.main.AbstractBusinessGroupListController.TABLE_ACTION_SELECT;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.filter.FilterFactory;
import org.olat.group.BusinessGroupManagedFlag;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.BusinessGroupModule;

/**
 * 
 * Initial date: 10.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BusinessGroupFlexiTableModel extends DefaultFlexiTableDataModel<BGTableItem>
	implements SortableFlexiTableDataModel<BGTableItem> {

	private final Translator trans;

	/**
	 * @param owned list of business groups
	 */
	public BusinessGroupFlexiTableModel(Translator trans, FlexiTableColumnModel columnModel) {
		super(new ArrayList<BGTableItem>(), columnModel);
		this.trans = trans;
	}
	
	public static FlexiTableColumnModel getStandardColumnModel(boolean delete, FormLayoutContainer flc, BusinessGroupModule groupModule, Translator translator) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		//mark
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.mark.i18n(), Cols.mark.ordinal(),
				true, Cols.mark.name()));
		//group name
		columnsModel.addFlexiColumnModel(new StaticFlexiColumnModel(Cols.name.i18n(), Cols.name.ordinal(), TABLE_ACTION_LAUNCH,
				true, Cols.name.name(), new StaticFlexiCellRenderer(TABLE_ACTION_LAUNCH, new BusinessGroupNameCellRenderer())));
		//id and reference
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.key.i18n(), Cols.key.ordinal(), true, Cols.key.name()));
		if(groupModule.isManagedBusinessGroups()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.externalId.i18n(), Cols.externalId.ordinal(),
					true, Cols.externalId.name()));
		}
		//description
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.description.i18n(), Cols.description.ordinal(),
				false, null, FlexiColumnModel.ALIGNMENT_LEFT, new TextFlexiCellRenderer(EscapeMode.antisamy)));
		//courses
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.resources.i18n(), Cols.resources.ordinal(),
				true, Cols.resources.name(), FlexiColumnModel.ALIGNMENT_LEFT, new BGResourcesCellRenderer(flc)));
		//access
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.accessTypes.i18n(), Cols.accessTypes.ordinal(),
				true, Cols.accessTypes.name(), FlexiColumnModel.ALIGNMENT_LEFT, new BGAccessControlledCellRenderer()));
		//launch dates
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.firstTime.i18n(), Cols.firstTime.ordinal(),
				true, Cols.firstTime.name(), FlexiColumnModel.ALIGNMENT_LEFT, new DateFlexiCellRenderer(translator.getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.lastTime.i18n(), Cols.lastTime.ordinal(),
				true, Cols.lastTime.name(), FlexiColumnModel.ALIGNMENT_LEFT, new DateFlexiCellRenderer(translator.getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.lastUsage.i18n(), Cols.lastUsage.ordinal(),
				true, Cols.lastUsage.name()));
		//roles
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.role.i18n(), Cols.role.ordinal(),
				true, Cols.role.name(), FlexiColumnModel.ALIGNMENT_LEFT, new BGRoleCellRenderer(translator.getLocale())));
		
		//actions
		columnsModel.addFlexiColumnModel(new StaticFlexiColumnModel(Cols.allowLeave.i18n(), Cols.allowLeave.ordinal(), TABLE_ACTION_LEAVE,
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translator.translate("table.header.leave"), TABLE_ACTION_LEAVE), null)));
		if(delete) {
			columnsModel.addFlexiColumnModel(new StaticFlexiColumnModel(Cols.allowDelete.i18n(), Cols.allowDelete.ordinal(), TABLE_ACTION_DELETE,
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translator.translate("table.header.delete"), TABLE_ACTION_DELETE), null)));
		}
		
		return columnsModel;
	}
	
	public static FlexiTableColumnModel getSelectColumnModel(FormLayoutContainer flc, BusinessGroupModule groupModule, Translator translator) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		//mark
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.mark.i18n(), Cols.mark.ordinal(),
				true, Cols.mark.name()));
		//group name
		columnsModel.addFlexiColumnModel(new StaticFlexiColumnModel(Cols.name.i18n(), Cols.name.ordinal(), TABLE_ACTION_LAUNCH,
				true, Cols.name.name(), new StaticFlexiCellRenderer(TABLE_ACTION_LAUNCH, new BusinessGroupNameCellRenderer())));
		//id and reference
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.key.i18n(), Cols.key.ordinal(), true, Cols.key.name()));
		if(groupModule.isManagedBusinessGroups()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.externalId.i18n(), Cols.externalId.ordinal(),
					true, Cols.externalId.name()));
		}
		//description
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.description.i18n(), Cols.description.ordinal(),
				false, null, FlexiColumnModel.ALIGNMENT_LEFT, new TextFlexiCellRenderer(EscapeMode.antisamy)));
		//courses
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.resources.i18n(), Cols.resources.ordinal(),
				true, Cols.resources.name(), FlexiColumnModel.ALIGNMENT_LEFT, new BGResourcesCellRenderer(flc)));

		//stats
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.tutorsCount.i18n(), Cols.tutorsCount.ordinal(),
				true, Cols.tutorsCount.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.participantsCount.i18n(), Cols.participantsCount.ordinal(),
				true, Cols.participantsCount.name()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.freePlaces.i18n(), Cols.freePlaces.ordinal(),
				true, Cols.freePlaces.name(), FlexiColumnModel.ALIGNMENT_LEFT, new TextFlexiCellRenderer(EscapeMode.none)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, Cols.waitingListCount.i18n(), Cols.waitingListCount.ordinal(),
				true, Cols.waitingListCount.name()));
		
		//actions
		columnsModel.addFlexiColumnModel(new StaticFlexiColumnModel("select", translator.translate("select"), TABLE_ACTION_SELECT));
		return columnsModel;
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int row, int col) {
		BGTableItem wrapped = getObject(row);
		return getValueAt(wrapped, col);
	}	
		
	@Override
	public Object getValueAt(BGTableItem wrapped, int col) {
		switch (Cols.values()[col]) {
			case name:
				return wrapped.getBusinessGroup();
			case description:
				String description = wrapped.getBusinessGroupDescription();
				description = FilterFactory.getHtmlTagsFilter().filter(description);
				description = Formatter.truncate(description, 256);
				return description;
			case allowLeave: {
				Boolean allowed = wrapped.getAllowLeave();
				if(allowed != null && allowed.booleanValue()) {
					//check managed groups
					if(BusinessGroupManagedFlag.isManaged(wrapped.getManagedFlags(), BusinessGroupManagedFlag.membersmanagement)) {
						return Boolean.FALSE;
					}
				}
				return allowed;
			}
			case allowDelete: {
				Boolean allowed =  wrapped.getAllowDelete();
				if(allowed != null && allowed.booleanValue()) {
					//check managed groups
					if(BusinessGroupManagedFlag.isManaged(wrapped.getManagedFlags(), BusinessGroupManagedFlag.delete)) {
						return Boolean.FALSE;
					}
				}
				return allowed;
			}
			case resources:
				return wrapped;
			case accessControl:
				return new Boolean(wrapped.isAccessControl());
			case accessControlLaunch:
				/*if(wrapped.isAccessControl()) {
					if(wrapped.getMembership() != null) {
						return trans.translate("select");
					}
					return trans.translate("table.access");
				}*/
				return wrapped.getAccessLink();
			case accessTypes:
				return wrapped.getAccessTypes();
			case mark:
				return wrapped.getMarkLink();
			case lastUsage:
				return wrapped.getBusinessGroupLastUsage();
			case role:
				return wrapped.getMembership();
			case firstTime: {
				BusinessGroupMembership membership = wrapped.getMembership();
				return membership == null ? null : membership.getCreationDate();
			}
			case lastTime: {
				BusinessGroupMembership membership = wrapped.getMembership();
				return membership == null ? null : membership.getLastModified();
			}
			case key:
				return wrapped.getBusinessGroupKey();
			case freePlaces: {
				Integer maxParticipants = wrapped.getMaxParticipants();
				if(maxParticipants != null && maxParticipants.intValue() >= 0) {
					long free = maxParticipants - (wrapped.getNumOfParticipants() + wrapped.getNumOfPendings());
					return new GroupNumber(free);
				}
				return GroupNumber.INFINITE;
			}
			case participantsCount: {
				long count = wrapped.getNumOfParticipants() + wrapped.getNumOfPendings();
				return count < 0 ? GroupNumber.ZERO : new GroupNumber(count);
			}
			case tutorsCount: {
				long count = wrapped.getNumOfOwners();
				return count < 0 ? GroupNumber.ZERO : new GroupNumber(count);
			}
			case waitingListCount: {
				if(wrapped.isWaitingListEnabled()) {
					long count = wrapped.getNumWaiting();
					return count < 0 ? GroupNumber.ZERO : new GroupNumber(count);
				}
				return GroupNumber.NONE;
			}
			case wrapper:
				return wrapped;
			case externalId:
				return wrapped.getBusinessGroupExternalId();
			case unlink: {	
				boolean managed = BusinessGroupManagedFlag.isManaged(wrapped.getManagedFlags(), BusinessGroupManagedFlag.resources);
				return managed ? Boolean.FALSE : Boolean.TRUE;
			}
			default:
				return "ERROR";
		}
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<BGTableItem> views = new BusinessGroupFlexiTableModelSort(orderBy, this, null).sort();
			super.setObjects(views);
		}
	}

	@Override
	public BusinessGroupFlexiTableModel createCopyWithEmptyList() {
		return new BusinessGroupFlexiTableModel(trans, getTableColumnModel());
	}
	
	public boolean filterEditableGroupKeys(UserRequest ureq, List<Long> groupKeys) {
		if(ureq.getUserSession().getRoles().isOLATAdmin() || ureq.getUserSession().getRoles().isGroupManager()) {
			return false;
		}
		
		int countBefore = groupKeys.size();
		
		for(BGTableItem item:getObjects()) {
			Long groupKey = item.getBusinessGroupKey();
			if(groupKeys.contains(groupKey)) {
				BusinessGroupMembership membership = item.getMembership();
				if(membership == null || !membership.isOwner()) {
					groupKeys.remove(groupKey);
				}
			}
		}
		
		return groupKeys.size() != countBefore;
	}

	/**
	 * @param owned
	 */
	public void setEntries(List<BGTableItem> owned) {
		setObjects(owned);
	}
	
	public void removeBusinessGroup(Long bgKey) {
		if(bgKey == null) return;
		
		boolean removed = false;
		List<BGTableItem> items = getObjects();
		for(int i=items.size(); i-->0; ) {
			BGTableItem wrapped = items.get(i);
			if(bgKey.equals(wrapped.getBusinessGroupKey())) {
				items.remove(i);
				removed = true;
			}
		}
		if(removed) {
			setObjects(items);
		}
	}
	
	public enum Cols {
		name("table.header.bgname"),
		description("table.header.description"),
		groupType(""),
		allowLeave("table.header.leave"),
		allowDelete("table.header.delete"),
		resources("table.header.resources"),
		accessControl(""),
		accessControlLaunch("table.header.ac"),
		accessTypes("table.header.ac.method"),
		mark("table.header.mark"),
		lastUsage("table.header.lastUsage"),
		role("table.header.role"),
		firstTime("table.header.firstTime"),
		lastTime("table.header.lastTime"),
		key("table.header.key"),
		freePlaces("table.header.freePlaces"),
		participantsCount("table.header.participantsCount"),
		tutorsCount("table.header.tutorsCount"),
		waitingListCount("table.header.waitingListCount"),
		wrapper(""),
		card("table.header.businesscard"),
		externalId("table.header.externalid"),
		unlink("table.header.unlink");
		
		private final String i18n;
		
		private Cols(String i18n) {
			this.i18n = i18n;
		}
		
		public String i18n() {
			return i18n;
		}
	}
}