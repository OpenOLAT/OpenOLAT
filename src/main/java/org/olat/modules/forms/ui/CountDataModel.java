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
package org.olat.modules.forms.ui;

import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.forms.ui.model.CountRatioResult;

/**
 * 
 * Initial date: 20.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CountDataModel extends DefaultFlexiTableDataModel<CountRatioResult>
implements SortableFlexiTableDataModel<CountRatioResult> {
	
	public CountDataModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}

	@Override
	public void sort(SortKey orderBy) {
		List<CountRatioResult> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, null).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		CountRatioResult countResult = getObject(row);
		return getValueAt(countResult, col);
	}

	@Override
	public Object getValueAt(CountRatioResult row, int col) {
		switch(CountReportCols.values()[col]) {
			case name: return row.getName();
			case count: return row.getCount();
			case percent: return row.getRatio();
			default: return null;
		}
	}
	
	public enum CountReportCols implements FlexiSortableColumnDef {
		name("report.count.name.title"),
		count("report.count.count.title"),
		percent("report.count.percent.title");
		
		private final String i18nKey;
		
		private CountReportCols(String i18nKey) {
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
