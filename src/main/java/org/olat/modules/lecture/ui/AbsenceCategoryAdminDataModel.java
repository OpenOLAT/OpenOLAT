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
package org.olat.modules.lecture.ui;

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
 * Initial date: 12 mai 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AbsenceCategoryAdminDataModel extends DefaultFlexiTableDataModel<AbsenceCategoryRow>
implements SortableFlexiTableDataModel<AbsenceCategoryRow> {
	
	private static final CategoryCols[] COLS = CategoryCols.values();
	private final Locale locale;
	
	public AbsenceCategoryAdminDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		List<AbsenceCategoryRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		AbsenceCategoryRow category = getObject(row);
		return getValueAt(category, col);
	}

	@Override
	public Object getValueAt(AbsenceCategoryRow row, int col) {
		switch(COLS[col]) {
			case id: return row.getCategory().getKey();
			case title: return row.getCategory().getTitle();
			case description: return row.getCategory().getDescription();
			case enabled: return Boolean.valueOf(row.getCategory().isEnabled());
			case tools: return row.getToolsLink();
			default: return null;
		}
	}
	
	public enum CategoryCols implements FlexiSortableColumnDef {
		id("reason.id"),
		title("reason.title"),
		description("reason.description"),
		enabled("table.header.enabled"),
		tools("table.header.actions");
		
		private final String i18nKey;
		
		private CategoryCols(String i18nKey) {
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
