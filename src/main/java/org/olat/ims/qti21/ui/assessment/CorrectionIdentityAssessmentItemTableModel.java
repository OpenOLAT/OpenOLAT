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
package org.olat.ims.qti21.ui.assessment;

import static org.olat.ims.qti21.ui.assessment.CorrectionIdentityAssessmentItemListController.FILTER_ADJUSTED;
import static org.olat.ims.qti21.ui.assessment.CorrectionIdentityAssessmentItemListController.FILTER_MANUAL;
import static org.olat.ims.qti21.ui.assessment.CorrectionIdentityAssessmentItemListController.FILTER_STATUS;
import static org.olat.ims.qti21.ui.assessment.CorrectionIdentityAssessmentItemListController.FILTER_TO_CORRECT;
import static org.olat.ims.qti21.ui.assessment.CorrectionIdentityAssessmentItemListController.FILTER_TO_REVIEW;
import static org.olat.ims.qti21.ui.assessment.CorrectionIdentityAssessmentItemListController.STATUS_ANSWERED_KEY;
import static org.olat.ims.qti21.ui.assessment.CorrectionIdentityAssessmentItemListController.STATUS_NOT_ANSWERED_KEY;
import static org.olat.ims.qti21.ui.assessment.CorrectionIdentityAssessmentItemListController.STATUS_NOT_READ_KEY;

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
import org.olat.ims.qti21.ui.assessment.model.CorrectionIdentityAssessmentItemRow;

/**
 * 
 * Initial date: 2 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CorrectionIdentityAssessmentItemTableModel extends DefaultFlexiTableDataModel<CorrectionIdentityAssessmentItemRow>
implements SortableFlexiTableDataModel<CorrectionIdentityAssessmentItemRow>, FilterableFlexiTableModel {
	
	private static final IdentityItemCols[] COLS = IdentityItemCols.values();
	
	private final Locale locale;
	private List<CorrectionIdentityAssessmentItemRow> backupList;
	
	public CorrectionIdentityAssessmentItemTableModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<CorrectionIdentityAssessmentItemRow> rows = new CorrectionIdentityAssessmentItemTableSort(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if (filters != null && !filters.isEmpty()) {
			List<CorrectionIdentityAssessmentItemRow> filteredRows = new ArrayList<>();
		
			boolean toCorrect = isFilterSelected(filters, FILTER_TO_CORRECT);
			boolean toReview = isFilterSelected(filters, FILTER_TO_REVIEW);
			List<String> statusList = getFilteredList(filters, FILTER_STATUS);
			boolean manual = isFilterSelected(filters, FILTER_MANUAL);
			boolean adjusted = isFilterSelected(filters, FILTER_ADJUSTED);
			
			for (CorrectionIdentityAssessmentItemRow row : backupList) {
				if (acceptToCorrect(toCorrect, row)
						&& acceptToReview(toReview, row)
						&& acceptStatus(statusList, row)
						&& acceptManual(manual, row)
						&& acceptAdjusted(adjusted, row)) {
					filteredRows.add(row);
				}
			}
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backupList);
		}
	}
	
	private boolean acceptStatus(List<String> statusList, CorrectionIdentityAssessmentItemRow row) {
		if(statusList == null || statusList.isEmpty()) return true;
		
		if(statusList.contains(STATUS_ANSWERED_KEY) && row.isAnswered()) {
			return true;
		}
		if(statusList.contains(STATUS_NOT_ANSWERED_KEY) && !row.isAnswered() && row.isEntered()) {
			return true;
		}
		if(statusList.contains(STATUS_NOT_READ_KEY) && !row.isEntered()) {
			return true;
		}
		return false;
	}
	
	private boolean acceptToCorrect(boolean toCorrect, CorrectionIdentityAssessmentItemRow row) {
		if(!toCorrect) return true;
		return row.isManualCorrection() && !row.isCorrected();
	}
	
	private boolean acceptToReview(boolean toReview, CorrectionIdentityAssessmentItemRow elementRow) {
		if(!toReview) return true;
		return elementRow.isToReview();
	}
	
	private boolean acceptManual(boolean manual, CorrectionIdentityAssessmentItemRow elementRow) {
		if(!manual) return true;
		return elementRow.isManualCorrection();
	}
	
	private boolean acceptAdjusted(boolean toReview, CorrectionIdentityAssessmentItemRow elementRow) {
		if(!toReview) return true;
		return !elementRow.isManualCorrection() && elementRow.getManualScore() != null;
	}
	
	private List<String> getFilteredList(List<FlexiTableFilter> filters, String filterName) {
    	FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, filterName);
		if(filter instanceof FlexiTableExtendedFilter extendedFilter) {
			List<String> filterValues = extendedFilter.getValues();
			return filterValues != null && !filterValues.isEmpty() ? filterValues : null;
		}
		return null;
	}
	
	private boolean isFilterSelected(List<FlexiTableFilter> filters, String id) {
		FlexiTableFilter filter = FlexiTableFilter.getFilter(filters, id);
		if (filter != null) {
			List<String> filterValues = ((FlexiTableExtendedFilter)filter).getValues();
			return filterValues != null && filterValues.contains(id);
		}
		return false;
	}

	@Override
	public Object getValueAt(int row, int col) {
		CorrectionIdentityAssessmentItemRow itemRow = getObject(row);
		return getValueAt(itemRow, col);
	}

	@Override
	public Object getValueAt(CorrectionIdentityAssessmentItemRow row, int col) {
		return switch(COLS[col]) {
			case section -> row.getSectionTitle();
			case itemTitle -> row.getItemTitle();
			case itemType -> row.getItemType();
			case itemKeywords -> row.getKeywords();
			case score -> row.getFinalScore();
			case autoScore -> !row.isManualCorrection() ? null : row.getScore();
			case manualScore -> row.isManualCorrection() ? row.getManualScore() : null;
			case answered -> row.isAnswered();
			case toCorrect -> row;
			case toReview -> row.isToReview();
			default -> "ERROR";
		};
	}
	
	@Override
	public void setObjects(List<CorrectionIdentityAssessmentItemRow> objects) {
		this.backupList = objects;
		super.setObjects(objects);
	}

	public enum IdentityItemCols implements FlexiSortableColumnDef {
		section("table.header.section"),
		itemTitle("table.header.item.title"),
		itemType("table.header.item.type"),
		itemKeywords("table.header.item.keywords"),
		score("table.header.score"),
		autoScore("table.header.score.auto"),
		manualScore("table.header.score.manual"),
		answered("table.header.answered"),
		toCorrect("table.header.to.correct"),
		toReview("table.header.to.review");
		
		private final String i18n;
		
		private IdentityItemCols(String i18n) {
			this.i18n = i18n;
		}

		@Override
		public String i18nHeaderKey() {
			return i18n;
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
