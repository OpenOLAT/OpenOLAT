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
package org.olat.modules.scorm.assessment;

import static org.olat.modules.scorm.assessment.ScormResultDetailsController.FILTER_LAST_RUN;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentHelper;

/**
 * 
 * Initial date: 16 oct. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ScoSummaryTableModel extends DefaultFlexiTableDataModel<ScoSummaryRow>
implements SortableFlexiTableDataModel<ScoSummaryRow>, FilterableFlexiTableModel {
	
	private static final SummaryCols[] COLS = SummaryCols.values();
	
	private final Locale locale;
	private List<ScoSummaryRow> backupRows;
	
	public ScoSummaryTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<ScoSummaryRow> rows = new ScoSummaryTableSortDelegate(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if (StringHelper.containsNonWhitespace(searchString) || (filters != null && !filters.isEmpty())) {
			List<ScoSummaryRow> filteredRows = new ArrayList<>();
			searchString = searchString.toLowerCase();

			final boolean lastRun = isFilterSelected(filters, FILTER_LAST_RUN);
			for (ScoSummaryRow row : backupRows) {
				if (accept(searchString, row)
						&& acceptLastRun(lastRun, row)) {
					filteredRows.add(row);
				}
			}
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backupRows);
		}
	}
	
	private boolean isFilterSelected(List<FlexiTableFilter> filters, String id) {
		FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, id);
		if (filter != null) {
			List<String> filterValues = ((FlexiTableExtendedFilter)filter).getValues();
			return filterValues != null && filterValues.contains(id);
		}
		return false;
	}
	
	private boolean acceptLastRun(boolean lastRun, ScoSummaryRow row) {
		return !lastRun || row.isLastRun();
	}
	
	private boolean accept(String searchString, ScoSummaryRow entry) {
		return accept(searchString, entry.getScoId());
	}

	private boolean accept(String searchString, String value) {
		return StringHelper.containsNonWhitespace(value) && value.toLowerCase().contains(searchString);
	}

	@Override
	public Object getValueAt(int row, int col) {
		ScoSummaryRow summaryRow = getObject(row);
		return getValueAt(summaryRow, col);
	}
	
	@Override
	public Object getValueAt(ScoSummaryRow summaryRow, int col) {
		return switch (COLS[col]) {
			case lastRun -> summaryRow.isLastRun();
			case scoId -> summaryRow.getScoId();
			case date -> summaryRow.getDate();
			case duration -> summaryRow.getTotalTime();
			case score -> AssessmentHelper.getRoundedScore(summaryRow.getScore());
			default -> "ERROR";
		};
	}
	
	@Override
	public void setObjects(List<ScoSummaryRow> objects) {
		backupRows = objects;
		super.setObjects(objects);
	}
	
	public enum SummaryCols implements FlexiSortableColumnDef {
		lastRun("summary.column.header.last.run"),
		scoId("summary.column.header.scoid"),
		date("summary.column.header.date"),
		duration("summary.column.header.duration"),
		score("summary.column.header.assesspoints");
		
		private final String i18nKey;
		
		private SummaryCols(String i18nKey) {
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