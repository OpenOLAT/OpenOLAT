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
package org.olat.modules.coach.ui.manager;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * Initial date: 2025-01-24<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ReportTemplatesDataModel extends DefaultFlexiTableDataModel<ReportTemplatesRow> 
		implements SortableFlexiTableDataModel<ReportTemplatesRow> {

	private static final ReportTemplateCols[] COLS = ReportTemplateCols.values();
	private final Locale locale;

	public ReportTemplatesDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey sortKey) {
		if (sortKey != null) {
			List<ReportTemplatesRow> rows = new SortableFlexiTableModelDelegate<>(sortKey, this, locale).sort();
			super.setObjects(rows);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		ReportTemplatesRow reportTemplatesRow = getObject(row);
		return getValueAt(reportTemplatesRow, col);
	}
	
	@Override
	public Object getValueAt(ReportTemplatesRow row, int col) {
		return switch (COLS[col]) {
			case name -> row.getName();
			case category -> row.getCategory();
			case description -> row.getDescription();
			case type -> row.getType();
			case run -> row.getRun();
		};
	}

	public enum ReportTemplateCols implements FlexiSortableColumnDef {
		name("table.header.name"),
		category("table.header.category"),
		description("table.header.description"),
		type("table.header.type"),
		run("table.header.run");
		
		private final String i18nKey;
		
		ReportTemplateCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}


		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}
}
