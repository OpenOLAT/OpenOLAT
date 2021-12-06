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
package org.olat.modules.grading.ui;

import java.util.List;
import java.util.Locale;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * 
 * Initial date: 6 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssignedReferenceEntryListTableModel extends DefaultFlexiTableDataModel<AssignedReferenceEntryRow>
implements SortableFlexiTableDataModel<AssignedReferenceEntryRow> {

	private static final GEntryCol[] COLS = GEntryCol.values();
	
	private final Locale locale;
	
	public AssignedReferenceEntryListTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<AssignedReferenceEntryRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		AssignedReferenceEntryRow entryRow = getObject(row);
		return getValueAt(entryRow, col);
	}

	@Override
	public Object getValueAt(AssignedReferenceEntryRow row, int col) {
		switch(COLS[col]) {
			case id: return row.getKey();
			case displayName: return row.getDisplayname();
			case externalRef: return row.getExternalRef();
			case total: return row.getTotalAssignments();
			case done: return row.getNumOfDoneAssignments();
			case open: return row.getNumOfOpenAssignments();
			case overdue: return row.getNumOfOverdueAssignments();
			case oldestOpenAssignment: return row.getOldestOpenAssignment();
			case recordedTime: return getCorrectionTimeInMinutes(row.getRecordedTimeInSeconds());
			case recordedMetadataTime: return getCorrectionTimeInMinutes(row.getRecordedMetadataTimeInSeconds());
			case absence: return row;
			case tools: return row.getToolsLink();
			default: return "ERROR";
		}
	}
	
	private Long getCorrectionTimeInMinutes(long  timeInSeconds) {
		if(timeInSeconds > 0) {
			return CalendarUtils.convertSecondsToMinutes(timeInSeconds);
		}
		return null;
	}
	
	public enum GEntryCol implements FlexiSortableColumnDef {
		id("table.header.id"),
		displayName("table.header.reference.entry"),
		externalRef("table.header.reference.entry.external.ref"),
		total("table.header.assignments.total"),
		done("table.header.assignments.done"),
		open("table.header.assignments.open"),
		overdue("table.header.assignments.overdue"),
		oldestOpenAssignment("table.header.assignments.oldest.open"),
		recordedTime("table.header.recorded.time"),
		recordedMetadataTime("table.header.recorded.meta.time"),
		absence("table.header.absence.leave"),
		tools("table.header.tools");
		
		private final String i18nKey;
		
		private GEntryCol(String i18nKey) {
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
