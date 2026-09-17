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

import static org.olat.ims.qti21.ui.assessment.CorrectionIdentityListController.FILTER_ADJUSTED;
import static org.olat.ims.qti21.ui.assessment.CorrectionIdentityListController.FILTER_MANUAL;
import static org.olat.ims.qti21.ui.assessment.CorrectionIdentityListController.FILTER_TO_CORRECT;
import static org.olat.ims.qti21.ui.assessment.CorrectionIdentityListController.FILTER_TO_REVIEW;

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
import org.olat.ims.qti21.ui.assessment.model.CorrectionIdentityRow;
import org.olat.modules.lecture.ui.ParticipantListRepositoryController;

/**
 * 
 * Initial date: 28 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CorrectionIdentityTableModel extends DefaultFlexiTableDataModel<CorrectionIdentityRow>
implements SortableFlexiTableDataModel<CorrectionIdentityRow>, FilterableFlexiTableModel {
	
	private static final IdentityCols[] COLS = IdentityCols.values();
	
	private final Locale locale;
	
	private List<CorrectionIdentityRow> backupList;
	
	public CorrectionIdentityTableModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<CorrectionIdentityRow> rows = new CorrectionIdentityTableSort(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if (filters != null && !filters.isEmpty()) {
			List<CorrectionIdentityRow> filteredRows = new ArrayList<>();
		
			boolean toCorrect = isFilterSelected(filters, FILTER_TO_CORRECT);
			boolean toReview = isFilterSelected(filters, FILTER_TO_REVIEW);
			boolean manual = isFilterSelected(filters, FILTER_MANUAL);
			boolean adjusted = isFilterSelected(filters, FILTER_ADJUSTED);
			
			for (CorrectionIdentityRow row : backupList) {
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
	
	private boolean acceptToCorrect(boolean toCorrect, CorrectionIdentityRow row) {
		if(!toCorrect) return true;
		return row.getNumNotCorrected() > 0;
	}
	
	private boolean acceptToReview(boolean toReview, CorrectionIdentityRow row) {
		if(!toReview) return true;
		return row.getNumToReview() > 0;
	}
	
	private boolean acceptManual(boolean manual, CorrectionIdentityRow row) {
		if(!manual) return true;
		return row.getNumNotCorrected() > 0 || row.getNumCorrected() > 0;
	}
	
	private boolean acceptAdjusted(boolean toReview, CorrectionIdentityRow row) {
		if(!toReview) return true;
		return row.getNumOfAdjusted() > 0;
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
		CorrectionIdentityRow identRow = getObject(row);
		return getValueAt(identRow, col);
	}

	@Override
	public Object getValueAt(CorrectionIdentityRow row, int col) {
		if(col < CorrectionIdentityListController.USER_PROPS_OFFSET) {
			return switch(COLS[col]) {
				case user -> row.getUser();
				case lastSession -> row.getCandidateSession() != null? row.getCandidateSession().getKey(): null;
				case score -> row.getCandidateSession().getFinalScore();
				case answered -> row.getNumAnswered();
				case notAnswered -> row.getNumNotAnswered();
				case autoCorrected -> row.getNumAutoCorrected() > 0 ? row.getNumAutoCorrected() : null;
				case corrected -> row.getNumCorrected() > 0 ? row.getNumCorrected() : null;
				case adjusted -> row.getNumOfAdjusted() > 0 ? row.getNumOfAdjusted() : null;
				case notCorrected -> row.getNumNotCorrected();
				case toReview -> row.getNumToReview();
				default -> "ERROR";
			};
		}
		
		int propPos = col - ParticipantListRepositoryController.USER_PROPS_OFFSET;
		return row.getIdentityProp(propPos);
	}
	
	@Override
	public void setObjects(List<CorrectionIdentityRow> objects) {
		this.backupList = objects;
		super.setObjects(objects);
	}

	public enum IdentityCols implements FlexiSortableColumnDef {
		user("table.header.user.identifier"),
		lastSession("table.header.run.id"),
		score("table.header.score"),
		answered("table.header.num.answered"),
		notAnswered("table.header.notAnswered"),
		autoCorrected("table.header.auto.corrected"),
		corrected("table.header.manually.corrected"),
		adjusted("table.header.adjusted"),
		toReview("table.header.to.review"),
		notCorrected("table.header.to.correct");
		
		
		private final String i18n;
		
		private IdentityCols(String i18n) {
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
