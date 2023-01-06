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
package org.olat.core.commons.services.export.ui;

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
 * Initial date: 3 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ExportsListDataModel extends DefaultFlexiTableDataModel<ExportRow>
implements SortableFlexiTableDataModel<ExportRow> {
	
	private static final ExportsCols[] COLS = ExportsCols.values();
	private final Locale locale;
	
	public ExportsListDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey sortKey) {
		if(sortKey != null) {
			List<ExportRow> rows = new SortableFlexiTableModelDelegate<>(sortKey, this, locale).sort();
			super.setObjects(rows);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		ExportRow exportRow = getObject(row);
		return getValueAt(exportRow, col);
	}

	@Override
	public Object getValueAt(ExportRow row, int col) {
		switch(COLS[col]) {
			case title: return row.getTitle();
			case creationDate: return row.getCreationDate();
			case creator: return getCreator();
			default: return "ERROR";
		}
	}
	
	private String getCreator() {
		return null;
	}
	
	public enum ExportsCols implements FlexiSortableColumnDef {
		title("table.header.title"),
		creationDate("table.header.creation.date"),
		creator("table.header.creator");
		
		private final String i18nKey;
		
		private ExportsCols(String i18nKey) {
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
