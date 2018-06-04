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
package org.olat.modules.curriculum.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;

/**
 * 
 * Initial date: 13 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumManagerDataModel extends DefaultFlexiTableDataModel<CurriculumRow>
implements SortableFlexiTableDataModel<CurriculumRow> {
	
	private final Locale locale;
	
	public CurriculumManagerDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<CurriculumRow> rows = new CurriculumManagerTableSort(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		CurriculumRow curriculum = getObject(row);
		return getValueAt(curriculum, col);
	}

	@Override
	public Object getValueAt(CurriculumRow row, int col) {
		switch(CurriculumCols.values()[col]) {
			case key: return row.getKey();
			case displayName: return row.getDisplayName();
			case identifier: return row.getIdentifier();
			case tools: return row.getTools();
			default: return "ERROR";
		}
	}

	@Override
	public CurriculumManagerDataModel createCopyWithEmptyList() {
		return new CurriculumManagerDataModel(getTableColumnModel(), locale);
	}
	
	public enum CurriculumCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		displayName("table.header.displayName"),
		identifier("table.header.identifier"),
		tools("table.header.tools");
		
		private final String i18nHeaderKey;
		
		private CurriculumCols(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
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
			return i18nHeaderKey;
		}
	}
}
