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

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
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
implements SortableFlexiTableDataModel<CorrectionAssessmentItemRow> {

	private final Locale locale;
	
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
	public Object getValueAt(int row, int col) {
		CorrectionAssessmentItemRow itemRow = getObject(row);
		return getValueAt(itemRow, col);
	}

	@Override
	public Object getValueAt(CorrectionAssessmentItemRow row, int col) {
		switch(ItemCols.values()[col]) {
			case section: return row.getSectionTitle();
			case itemTitle: return row.getItemTitle();
			case itemKeywords: return row.getKeywords();
			case itemType: return row.getItemType();
			case answered: return row.getNumAnswered();
			case notAnswered: return row.getNumNotAnswered();
			case autoCorrected:
			case corrected:
			case notCorrected: return row;
			case toReview: return row.getNumToReview();
			case tools: return row.getToolsLink();
			default: return "ERROR";
		}
	}

	public enum ItemCols implements FlexiSortableColumnDef {
		section("table.header.section"),
		itemTitle("table.header.item.title"),
		itemKeywords("table.header.item.keywords"),
		itemType("table.header.item.type"),
		answered("table.header.answered"),
		notAnswered("table.header.notAnswered"),
		autoCorrected("table.header.autoCorrected"),
		corrected("table.header.corrected"),
		notCorrected("table.header.not.corrected"),
		toReview("table.header.to.review"),
		tools("table.header.action");
		
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
