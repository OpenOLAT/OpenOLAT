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
package org.olat.modules.selectus.ui.category;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

import org.olat.modules.selectus.model.Category;

/**
 * 
 * Initial date: 17 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CategoryDataModel extends DefaultFlexiTableDataModel<Category>
implements SortableFlexiTableDataModel<Category> {
	
	private final Locale locale;
	
	public CategoryDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<Category> categories = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(categories);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		Category cat = getObject(row);
		return getValueAt(cat, col);
	}

	@Override
	public Object getValueAt(Category row, int col) {
		CategoryCols field = CategoryCols.values()[col];
		switch(field) {
			case category: return row.getName();
			case color: return row.getColor();
			default: return "ERROR";
		}
	}
	
	public enum CategoryCols implements FlexiSortableColumnDef {
		category("table.header.category"),
		color("table.header.color");
		
		private final String key;
		
		private CategoryCols(String key) {
			this.key = key;
		}
		
		public String key() {
			return key;
		}

		@Override
		public String i18nHeaderKey() {
			return key;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return key();
		}
	}
}
