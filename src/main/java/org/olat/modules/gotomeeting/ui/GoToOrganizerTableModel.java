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
package org.olat.modules.gotomeeting.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.util.StringHelper;
import org.olat.modules.gotomeeting.GoToOrganizer;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 22.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GoToOrganizerTableModel extends DefaultFlexiTableDataModel<GoToOrganizer> implements SortableFlexiTableDataModel<GoToOrganizer> {
	
	private final UserManager userManager;
	
	public GoToOrganizerTableModel(FlexiTableColumnModel columnModel, UserManager userManager) {
		super(columnModel);
		this.userManager = userManager;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<GoToOrganizer> views = new GoToOrganizerTableModelSort(orderBy, this, null).sort();
			super.setObjects(views);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		GoToOrganizer organizer = getObject(row);
		return getValueAt(organizer, col);
	}

	@Override
	public Object getValueAt(GoToOrganizer organizer, int col) {
		switch(OrganizerCols.values()[col]) {
			case key: return organizer.getKey();
			case name: return organizer.getName();
			case firstName: return organizer.getFirstName();
			case lastName: return organizer.getLastName();
			case email: return organizer.getEmail();
			case renewDate: return StringHelper.containsNonWhitespace(organizer.getRefreshToken())
					? organizer.getRenewRefreshDate() : organizer.getRenewDate();
			case owner: return userManager.getUserDisplayName(organizer.getOwner());
			case refresh:
			case type: return Boolean.valueOf(StringHelper.containsNonWhitespace(organizer.getRefreshToken()));
			case remove: return organizer.getOwner() == null;
		}
		return null;
	}

	public enum OrganizerCols implements FlexiSortableColumnDef {
		
		key("organizer.key"),
		name("account.name"),
		firstName("organizer.firsName"),
		lastName("organizer.lastName"),
		email("organizer.email"),
		renewDate("organizer.renew.date"),
		owner("organizer.owner"),
		type("organizer.type"),
		remove("remove"),
		refresh("refresh.organizer");
		
		private final String i18nHeaderKey;
		
		private OrganizerCols(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nHeaderKey;
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
	
	private static class GoToOrganizerTableModelSort extends SortableFlexiTableModelDelegate<GoToOrganizer> {
		
		public GoToOrganizerTableModelSort(SortKey orderBy, SortableFlexiTableDataModel<GoToOrganizer> tableModel, Locale locale) {
			super(orderBy, tableModel, locale);
		}

		@Override
		protected void sort(List<GoToOrganizer> rows) {
			int columnIndex = getColumnIndex();
			OrganizerCols column = OrganizerCols.values()[columnIndex];
			switch(column) {
				default: {
					super.sort(rows);
				}
			}
		}
	}
}
