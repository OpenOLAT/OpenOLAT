/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.admin.user.groups;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.group.BusinessGroupManagedFlag;
import org.olat.group.BusinessGroupMembership;

/**
 * @author gnaegi
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
class BusinessGroupTableModelWithType extends DefaultFlexiTableDataModel<GroupOverviewRow>
implements SortableFlexiTableDataModel<GroupOverviewRow> {
	
	private static final Cols[] COLS = Cols.values(); 
	
	private final Locale locale;
	
	public BusinessGroupTableModelWithType(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<GroupOverviewRow> views = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		GroupOverviewRow wrapped = getObject(row);
		return getValueAt(wrapped, col);
	}

	@Override
	public Object getValueAt(GroupOverviewRow wrapped, int col) {
		switch (COLS[col]) {
			case key: return wrapped.getKey();
			case name: return wrapped;
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
			case role: return wrapped.getMembership();
			case firstTime: {
				BusinessGroupMembership membership = wrapped.getMembership();
				return membership == null ? null : membership.getCreationDate();
			}
			case lastTime: {
				BusinessGroupMembership membership = wrapped.getMembership();
				return membership == null ? null : membership.getLastModified();
			}
			case invitationLink: return wrapped.getInvitationLink();
			default: return "ERROR";
		}
	}
	
	public enum Cols implements FlexiSortableColumnDef {
		name("table.header.bgname"),
		key("table.header.key"),
		firstTime("table.header.firstTime"),
		lastTime("table.header.lastTime"),
		role("table.header.role"),
		allowLeave("table.header.leave"),
		invitationLink("table.header.invitation");
		
		private final String i18nKey;
		
		private Cols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}