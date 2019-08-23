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
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.util.Formatter;
import org.olat.core.util.filter.FilterFactory;
import org.olat.group.BusinessGroupManagedFlag;
import org.olat.group.BusinessGroupMembership;

/**
 * 
 * Initial date: 29.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BusinessGroupListFlexiTableModel extends DefaultFlexiTableDataModel<BGTableItem>
	implements SortableFlexiTableDataModel<BGTableItem> {
	
	private final Locale locale;

	/**
	 * @param owned list of business groups
	 */
	public BusinessGroupListFlexiTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(new ArrayList<BGTableItem>(), columnModel);
		this.locale = locale;
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int row, int col) {
		BGTableItem wrapped = getObject(row);
		return getValueAt(wrapped, col);
	}
		
	public Object getValueAt(BGTableItem wrapped, int col) {
		switch (Cols.values()[col]) {
			case name:
				return wrapped;
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
	
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<BGTableItem> views = new BusinessGroupFlexiTableModelSort(orderBy, this, locale).sort();
			super.setObjects(views);
		}
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

	@Override
	public DefaultFlexiTableDataModel<BGTableItem> createCopyWithEmptyList() {
		return new BusinessGroupListFlexiTableModel(getTableColumnModel(), locale);
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
		unlink("table.header.unlink"),
		export("table.header.export");

		private final String i18n;
		
		private Cols(String i18n) {
			this.i18n = i18n;
		}
		
		public String i18n() {
			return i18n;
		}
	}

}
