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
package org.olat.modules.dcompensation.ui;

import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * 
 * Initial date: 22 sept. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserDisadvantageCompensationTableModel extends DefaultFlexiTableDataModel<UserDisadvantageCompensationRow>
implements SortableFlexiTableDataModel<UserDisadvantageCompensationRow>, FilterableFlexiTableModel {
	
	private static final CompensationCols[] COLS = CompensationCols.values();
	
	private List<UserDisadvantageCompensationRow> backups;
	
	public UserDisadvantageCompensationTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			SortableFlexiTableModelDelegate<UserDisadvantageCompensationRow> sorter = new SortableFlexiTableModelDelegate<>(orderBy, this, null);
			List<UserDisadvantageCompensationRow> views = sorter.sort();
			super.setObjects(views);
		}
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if(filters != null && !filters.isEmpty() && filters.get(0) != null && !filters.get(0).isShowAll()) {
			String filterVal = filters.get(0).getFilter();
			List<UserDisadvantageCompensationRow> filteredRows = backups.stream()
						.filter(row -> filterVal.equals(row.getStatus().name()))
						.collect(Collectors.toList());
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backups);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		UserDisadvantageCompensationRow compensationRow = getObject(row);
		return getValueAt(compensationRow, col);
	}

	@Override
	public Object getValueAt(UserDisadvantageCompensationRow row, int col) {
		switch(COLS[col]) {
			case id: return row.getKey();
			case creator: return row.getCreatorFullName();
			case creationDate: return row.getCreationDate();
			case entry: return row.getEntryDisplayName();
			case entryKey: return row.getEntryKey();
			case externalRef: return row.getEntryExternalRef();
			case courseElement: return row.getCourseElement();
			case courseElementIdent: return row.getCourseElementId();
			case extraTime: return row.getExtraTime();
			case approvedBy: return row.getApprovedBy();
			case approvalDate: return row.getApprovalDate();
			case status: return row.getStatus();
			case tools: return row.getToolsLink();
			default: return "ERROR";
		}
	}

	@Override
	public void setObjects(List<UserDisadvantageCompensationRow> objects) {
		this.backups = objects;
		super.setObjects(objects);
	}

	@Override
	public UserDisadvantageCompensationTableModel createCopyWithEmptyList() {
		return new UserDisadvantageCompensationTableModel(getTableColumnModel());
	}

	public enum CompensationCols implements FlexiSortableColumnDef {
		id("table.header.id"),
		creationDate("table.header.creationdate"),
		creator("table.header.creator"),
		entryKey("table.header.entry.key"),
		entry("table.header.entry.displayname"),
		externalRef("table.header.entry.external.ref"),
		courseElement("table.header.course.element"),
		courseElementIdent("table.header.course.element.ident"),
		extraTime("table.header.extra.time"),
		approvedBy("table.header.approved.by"),
		approvalDate("table.header.approval.date"),
		status("table.header.status"),
		tools("table.header.tools");

		private final String i18nKey;
		
		private CompensationCols(String i18nKey) {
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
