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
package org.olat.modules.quality.analysis.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * 
 * Initial date: 04.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class AnalysisDataModel extends DefaultFlexiTableDataModel<AnalysisRow>
		implements SortableFlexiTableDataModel<AnalysisRow> {
	
	private final Locale locale;
	
	AnalysisDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}
	
	public AnalysisRow getObjectByFormEntryKey(Long key) {
		List<AnalysisRow> rows = getObjects();
		for (AnalysisRow row: rows) {
			if (row != null && row.getFormEntry().getKey().equals(key)) {
				return row;
			}
		}
		return null;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		List<AnalysisRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		AnalysisRow generator = getObject(row);
		return getValueAt(generator, col);
	}

	@Override
	public Object getValueAt(AnalysisRow row, int col) {
		switch(AnalysisCols.values()[col]) {
			case formTitle: return row.getFormTitle();
			case formCreated: return row.getFormCreatedDate();
			case numberDataCollections: return row.getNumberDataCollections();
			case soonest: return row.getSoonestDataCollectionDate();
			case latest: return row.getLatestDataCollectionFinishedDate();
			case numberParticipations: return row.getNumberParticipationsDone();
			default: return null;
		}
	}
	
	enum AnalysisCols implements FlexiSortableColumnDef {
		formTitle("analysis.table.form.title"),
		formCreated("analysis.table.form.created"),
		numberDataCollections("analysis.table.data.collections.number"),
		soonest("analysis.table.data.collections.soonest"),
		latest("analysis.table.data.collections.latest"),
		numberParticipations("analysis.table.participations.number");
		
		private final String i18nKey;
		
		private AnalysisCols(String i18nKey) {
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
