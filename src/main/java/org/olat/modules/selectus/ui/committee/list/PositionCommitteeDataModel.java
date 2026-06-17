/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.committee.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.user.propertyhandlers.UserPropertyHandler;

import org.olat.modules.selectus.ui.comparator.IdentityLastnameComparator;

/**
 * 
 * Initial date: 20 déc. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionCommitteeDataModel extends DefaultFlexiTableDataModel<CommitteeMemberRow>
implements SortableFlexiTableDataModel<CommitteeMemberRow>, FilterableFlexiTableModel {

	private static final CommitteeCols[] COLS =  CommitteeCols.values();
	
	private List<CommitteeMemberRow> backups;
	
	private final Locale locale;
	private final Translator translator;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	public PositionCommitteeDataModel(FlexiTableColumnModel columnsModel, List<UserPropertyHandler> userPropertyHandlers,
			Translator translator) {
		super(columnsModel);
		this.locale = translator.getLocale();
		this.translator = translator;
		this.userPropertyHandlers = userPropertyHandlers;
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		String key = filters == null || filters.isEmpty() || filters.get(0) == null ? null : filters.get(0).getFilter();
		if(StringHelper.containsNonWhitespace(key) && "hasnotrated".equals(key)) {
			List<CommitteeMemberRow> filteredRows = new ArrayList<>();
			for(CommitteeMemberRow row:backups) {
				if(row.isCanRate() && row.getNumOfRatings() <= 0) {
					filteredRows.add(row);
				}
			}
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backups);
		}
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<CommitteeMemberRow> members = new PositionCommitteeSortDelegate(orderBy, this, locale).sort();
			super.setObjects(members);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		CommitteeMemberRow identity = getObject(row);
		return getValueAt(identity, col);
	}

	@Override
	public Object getValueAt(CommitteeMemberRow row, int col) {
		Identity identity = row.getIdentity();
		if(col < PositionCommitteeController.USER_PROP_OFFSET) {
			return switch(COLS[col]) {
				case title -> getTitle(row);
				case name -> identity.getUser().getProperty(UserConstants.LASTNAME, locale) + ", " 
						+ identity.getUser().getProperty(UserConstants.FIRSTNAME, locale);
				case role -> translator.translate(row.getRole().role());
				case institution -> identity.getUser().getProperty(UserConstants.INSTITUTIONALNAME, locale);
				case assignments -> row.getNumOfAssignments();
				case hasAssignments -> row.getNumOfAssignments() > 0;
				default -> "ERROR";
			};
		} else if(col >= PositionCommitteeController.USER_PROP_OFFSET) {
			int propIndex = col - PositionCommitteeController.USER_PROP_OFFSET;
			UserPropertyHandler prop = userPropertyHandlers.get(propIndex);
			return prop.getUserProperty(identity.getUser(), translator.getLocale());
		}
		return "ERROR";
	}
	
	private String getTitle(CommitteeMemberRow row) {
		String title = row.getIdentity().getUser().getProperty(UserConstants.TITLE, locale);
		return "-".equals(title) ? "" : title;
	}
	
	@Override
	public void setObjects(List<CommitteeMemberRow> members) {
		super.setObjects(members);
		this.backups = members;
	}

	public enum CommitteeCols implements FlexiSortableColumnDef {
		title("edit.committee.title"),
		name("edit.committee.name"),
		role("edit.committee.role"),
		institution("edit.committee.institution"),
		assignments("table.header.assignments.stats"),
		hasAssignments("table.header.assignments.stats");

		private final String key;
		
		private CommitteeCols(String key) {
			this.key = key;
		}
		
		@Override
		public String i18nHeaderKey() {
			return key;
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
	
	private static class PositionCommitteeSortDelegate extends SortableFlexiTableModelDelegate<CommitteeMemberRow> {
		
		public PositionCommitteeSortDelegate(SortKey orderBy, PositionCommitteeDataModel tableModel, Locale locale) {
			super(orderBy, tableModel, locale);
		}
		
		@Override
		protected void sort(List<CommitteeMemberRow> rows) {
			int columnIndex = getColumnIndex();
			if(columnIndex == CommitteeCols.name.ordinal()) {
				Collections.sort(rows, new CommitteeMemberLastnameComparator());
			} else {
				super.sort(rows);
			}
		}
		
		private static class CommitteeMemberLastnameComparator implements Comparator<CommitteeMemberRow> {
			
			private static final IdentityLastnameComparator lastNameComparator = new IdentityLastnameComparator();

			@Override
			public int compare(CommitteeMemberRow r1, CommitteeMemberRow r2) {
				return lastNameComparator.compare(r1.getIdentity(), r2.getIdentity());
			}
		}
	}
}
