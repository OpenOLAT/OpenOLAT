package org.olat.modules.lecture.ui.coach;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.lecture.ui.coach.EditDatesLecturesEntriesTableModel.DateCols;

/**
 * 
 * Initial date: 7 avr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditDatesLecturesEntriesTableModelSortDelegate extends SortableFlexiTableModelDelegate<EditDatesLecturesEntryRow> {

	private static final DateCols[] COLS = DateCols.values();
	
	private final Set<EditDatesLecturesEntryRow> selectedRows;

	public EditDatesLecturesEntriesTableModelSortDelegate(SortKey orderBy, Set<EditDatesLecturesEntryRow> selectedRows,
			EditDatesLecturesEntriesTableModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
		this.selectedRows = selectedRows;
	}
	
	@Override
	protected void sort(List<EditDatesLecturesEntryRow> rows) {
		SortKey sortKey = getOrderBy();
		int columnIndex = getColumnIndex();
		if(sortKey != null && "selected".equals(sortKey.getKey())) {
			Collections.sort(rows, new EditDatesLecturesEntryRowComparator(selectedRows));
		} else if(columnIndex >= 0 && columnIndex < COLS.length) {
			switch(COLS[columnIndex]) {
				case date:
				case time: Collections.sort(rows, new StartDatesLecturesEntryRowComparator()); break;
				default: super.sort(rows); break;
			}
		} else {
			super.sort(rows);
		}
	}
	
	private int compareStartDate(EditDatesLecturesEntryRow o1, EditDatesLecturesEntryRow o2) {
		Date d1 = o1.getLectureBlock().getStartDate();
		Date d2 = o2.getLectureBlock().getStartDate();
		
		int c = 0;
		if(d1 == null || d2 == null) {
			c = compareNullObjects(d1, d2);
		} else {
			c = compareDateAndTimestamps(d1, d2);
		}
		return c;
	}
	
	private class StartDatesLecturesEntryRowComparator implements Comparator<EditDatesLecturesEntryRow> {
		
		@Override
		public int compare(EditDatesLecturesEntryRow o1, EditDatesLecturesEntryRow o2) {
			int c = 0;
			if(o1 == null || o2 == null) {
				c = compareNullObjects(o1, o2);
			} else {
				c = compareStartDate(o1, o2);
			}
			return c;
		}
	}
	
	private class EditDatesLecturesEntryRowComparator implements Comparator<EditDatesLecturesEntryRow> {

		private final Set<EditDatesLecturesEntryRow> selectedRows;
		
		public EditDatesLecturesEntryRowComparator(Set<EditDatesLecturesEntryRow> selectedRows) {
			this.selectedRows = selectedRows;
		}

		@Override
		public int compare(EditDatesLecturesEntryRow o1, EditDatesLecturesEntryRow o2) {
			int c = 0;
			if(o1 == null || o2 == null) {
				c = compareNullObjects(o1, o2);
			} else {
				boolean s1 = selectedRows.contains(o1);
				boolean s2 = selectedRows.contains(o2);
				c = -Boolean.compare(s1, s2);
			}
			
			if(c == 0 && o1 != null && o2 != null) {
				c = compareStartDate(o1, o2);
			}
			return c;
		}
	}
}
