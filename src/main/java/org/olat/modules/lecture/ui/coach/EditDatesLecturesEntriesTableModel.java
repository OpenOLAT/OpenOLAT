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
package org.olat.modules.lecture.ui.coach;

import java.time.DayOfWeek;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.lecture.LectureBlock;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 7 avr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditDatesLecturesEntriesTableModel extends DefaultFlexiTableDataModel<EditDatesLecturesEntryRow>
implements SortableFlexiTableDataModel<EditDatesLecturesEntryRow>, FlexiTableCssDelegate {
	
	private static final DateCols[] COLS = DateCols.values();
	
	protected static final String DAY_FILTER = "day-of-week";
	protected static final String ENTRY_FILTER = "entry";
	
	private final Locale locale;
	private final UserManager userManager;
	
	private List<EditDatesLecturesEntryRow> backupRows;
	
	public EditDatesLecturesEntriesTableModel(FlexiTableColumnModel columnsModel, UserManager userManager, Locale locale) {
		super(columnsModel);
		this.locale = locale;
		this.userManager = userManager;
	}
	

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<EditDatesLecturesEntryRow> rows = new EditDatesLecturesEntriesTableModelSortDelegate(orderBy, Set.of(), this, locale).sort();
			super.setObjects(rows);
		}
	}
	
	public void sort(SortKey orderBy, Set<EditDatesLecturesEntryRow> selectedRows) {
		if(orderBy != null) {
			List<EditDatesLecturesEntryRow> rows = new EditDatesLecturesEntriesTableModelSortDelegate(orderBy, selectedRows, this, locale).sort();
			super.setObjects(rows);
		}
	}
	
	public void filter(List<FlexiTableFilter> filters) {
		FlexiTableFilter dayFilter = FlexiTableFilter.getFilter(filters, DAY_FILTER);
		List<DayOfWeek> dayFilters = null;
		if (dayFilter != null) {
			List<String> filterValues = ((FlexiTableExtendedFilter)dayFilter).getValues();
			if (filterValues != null && !filterValues.isEmpty()) {
				dayFilters = filterValues.stream()
					.map(DayOfWeek::valueOf)
					.collect(Collectors.toList());
			}
		}
		
		FlexiTableFilter entryFilter = FlexiTableFilter.getFilter(filters, ENTRY_FILTER);
		List<Long> entryKeys = null;
		if(entryFilter != null) {
			List<String> filterValues = ((FlexiTableExtendedFilter)entryFilter).getValues();
			if (filterValues != null && !filterValues.isEmpty()) {
				entryKeys = filterValues.stream()
					.filter(StringHelper::isLong)
					.map(Long::valueOf)
					.collect(Collectors.toList());
			}
		}
		
		if(dayFilters == null && entryKeys == null) {
			super.setObjects(backupRows);
		} else {
			final Set<DayOfWeek> dayFilterSet = (dayFilters == null) ? null : Set.copyOf(dayFilters);
			final Set<Long> entryKeysSet = (entryKeys == null) ? null : Set.copyOf(entryKeys);
			List<EditDatesLecturesEntryRow> filteredRows = backupRows.stream()
					.filter(r -> accept(r, dayFilterSet, entryKeysSet))
					.collect(Collectors.toList());
			super.setObjects(filteredRows);
		}
	}
	
	private boolean accept(EditDatesLecturesEntryRow row, Set<DayOfWeek> dayFilterSet, Set<Long> entryKeysSet) {
		LectureBlock block = row.getLectureBlock();
		
		if(dayFilterSet != null) {
			Date start = block.getStartDate();
			DayOfWeek dayOfWeek = Formatter.getDayOfWeek(start);
			if(!dayFilterSet.contains(dayOfWeek)) {
				return false;
			}
		}
		
		if(entryKeysSet != null) {
			RepositoryEntry entry = block.getEntry();
			if(!entryKeysSet.contains(entry.getKey())) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		EditDatesLecturesEntryRow obj = getObject(row);
		return getValueAt(obj, col);
	}

	@Override
	public Object getValueAt(EditDatesLecturesEntryRow row, int col) {
		switch(COLS[col]) {
			case id: return row.getLectureBlock().getKey();
			case courseTitle: return row.getEntry().getDisplayname();
			case courseExternalId: return row.getEntry().getExternalId();
			case courseExternalRef: return row.getEntry().getExternalRef();
			case date:
			case time: return row.getLectureBlock();
			case numOfLectures: return row.getLectureBlock().getCalculatedLecturesNumber();
			case lectureTitle: return row.getLectureBlock().getTitle();
			case teachers: return getTeachers(row);
			default: return "ERROR";
		}
	}
	/*
	formatter.formatDate(block.getStartDate()), 		// 2
	formatter.formatTimeShort(block.getStartDate()), 	// 3
	formatter.formatTimeShort(block.getEndDate()),
	*/
	
	private String getTeachers(EditDatesLecturesEntryRow row) {
		StringBuilder sb = new StringBuilder();
		for(Identity teacher:row.getTeachers()) {
			if(sb.length() > 0) sb.append("; ");
			String fullName = userManager.getUserDisplayName(teacher);
			sb.append(StringHelper.escapeHtml(fullName));
		}
		return sb.toString();
	}
	
	@Override
	public void setObjects(List<EditDatesLecturesEntryRow> objects) {
		backupRows = objects;
		super.setObjects(objects);
	}
	
	public Integer indexOf(EditDatesLecturesEntryRow row) {
		List<EditDatesLecturesEntryRow> rows = getObjects();
		for(int i=0; i<rows.size(); i++) {
			if(row.equals(rows.get(i))) {
				return Integer.valueOf(i);
			}
		}
		return null;
	}
	
	public Integer indexOf(Long lectureBlockKey) {
		List<EditDatesLecturesEntryRow> rows = getObjects();
		for(int i=0; i<rows.size(); i++) {
			if(lectureBlockKey.equals(rows.get(i).getLectureBlockKey())) {
				return Integer.valueOf(i);
			}
		}
		return null;
	}
	
	@Override
	public String getWrapperCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getTableCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getRowCssClass(FlexiTableRendererType type, int pos) {
		EditDatesLecturesEntryRow row = getObject(pos);
		return row == null ? null : row.getCssClass();
	}
	
	public enum DateCols implements FlexiSortableColumnDef {
		id("table.header.id"),
		courseTitle("table.header.entry"),
		courseExternalId("table.header.entry.external.id"),
		courseExternalRef("table.header.entry.external.ref"),
		date("table.header.day"),
		time("table.header.times"),
		numOfLectures("table.header.num.lecture.block"),
		lectureTitle("table.header.lecture.block"),
		teachers("table.header.teachers");
		
		private final String i18nKey;
		
		private DateCols(String i18nKey) {
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
