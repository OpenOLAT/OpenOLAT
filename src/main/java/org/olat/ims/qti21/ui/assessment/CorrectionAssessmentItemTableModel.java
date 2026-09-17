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

import static org.olat.ims.qti21.ui.assessment.CorrectionAssessmentItemListController.FILTER_ADJUSTED;
import static org.olat.ims.qti21.ui.assessment.CorrectionAssessmentItemListController.FILTER_MANUAL;
import static org.olat.ims.qti21.ui.assessment.CorrectionAssessmentItemListController.FILTER_TO_CORRECT;
import static org.olat.ims.qti21.ui.assessment.CorrectionAssessmentItemListController.FILTER_TO_REVIEW;

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
import org.olat.ims.qti21.ui.assessment.model.CorrectionAssessmentItemRow;

/**
 * 
 * Initial date: 26 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CorrectionAssessmentItemTableModel extends DefaultFlexiTableDataModel<CorrectionAssessmentItemRow>
implements SortableFlexiTableDataModel<CorrectionAssessmentItemRow>, FilterableFlexiTableModel {

	private final Locale locale;
	
	private List<CorrectionAssessmentItemRow> backupList;
	
	public CorrectionAssessmentItemTableModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<CorrectionAssessmentItemRow> rows = new CorrectionAssessmentItemTableSort(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}
		
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if (filters != null && !filters.isEmpty()) {
			List<CorrectionAssessmentItemRow> filteredRows = new ArrayList<>();
		
			boolean toCorrect = isFilterSelected(filters, FILTER_TO_CORRECT);
			boolean toReview = isFilterSelected(filters, FILTER_TO_REVIEW);
			boolean manual = isFilterSelected(filters, FILTER_MANUAL);
			boolean adjusted = isFilterSelected(filters, FILTER_ADJUSTED);
			
			for (CorrectionAssessmentItemRow row : backupList) {
				if (acceptToCorrect(toCorrect, row)
						&& acceptToReview(toReview, row)
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
	
	private boolean acceptToCorrect(boolean toCorrect, CorrectionAssessmentItemRow row) {
		if(!toCorrect) return true;
		return row.isManualCorrection() && row.getNumNotCorrected() > 0;
	}
	
	private boolean acceptToReview(boolean toReview, CorrectionAssessmentItemRow row) {
		if(!toReview) return true;
		return row.getNumToReview() > 0;
	}
	
	private boolean acceptManual(boolean manual, CorrectionAssessmentItemRow row) {
		if(!manual) return true;
		return row.isManualCorrection();
	}
	
	private boolean acceptAdjusted(boolean toReview, CorrectionAssessmentItemRow row) {
		if(!toReview) return true;
		return !row.isManualCorrection() && row.getNumOfAdjusted() > 0;
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
		CorrectionAssessmentItemRow itemRow = getObject(row);
		return getValueAt(itemRow, col);
	}

	@Override
	public Object getValueAt(CorrectionAssessmentItemRow row, int col) {
		return switch(ItemCols.values()[col]) {
			case section -> row.getSectionTitle();
			case itemTitle -> row.getItemTitle();
			case itemKeywords -> row.getKeywords();
			case itemType -> row.getItemType();
			case answered -> row.getNumAnswered();
			case autoCorrected -> row.isManualCorrection() ? null : row.getNumAutoCorrected();
			case manuallyCorrected -> row.isManualCorrection() ? row.getNumManuallyCorrected() : null;
			case adjusted -> row.isManualCorrection() || row.getNumOfAdjusted() <= 0 ? null : row.getNumOfAdjusted();
			case notCorrected -> row.getNotCorrectedLink();
			case toReview -> row.getNumToReview();
			case tools -> row.getToolsLink();
			default -> "ERROR";
		};
	}

	@Override
	public void setObjects(List<CorrectionAssessmentItemRow> objects) {
		this.backupList = objects;
		super.setObjects(objects);
	}

	public enum ItemCols implements FlexiSortableColumnDef {
		section("table.header.section"),
		itemTitle("table.header.item.title"),
		itemKeywords("table.header.item.keywords"),
		itemType("table.header.item.type"),
		answered("table.header.num.answered"),
		autoCorrected("table.header.auto.corrected"),
		manuallyCorrected("table.header.manually.corrected"),
		adjusted("table.header.adjusted"),
		notCorrected("table.header.to.correct"),
		toReview("table.header.to.review"),
		tools("action.more");
		
		private final String i18n;
		
		private ItemCols(String i18n) {
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
