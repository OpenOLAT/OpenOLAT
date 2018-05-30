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

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;

/**
 * 
 * Initial date: 11 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementWithViewsDataModel extends DefaultFlexiTableDataModel<CurriculumElementWithViewsRow>
implements SortableFlexiTableDataModel<CurriculumElementWithViewsRow> {
	
	public CurriculumElementWithViewsDataModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}

	@Override
	public void sort(SortKey sortKey) {
		//
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		CurriculumElementWithViewsRow curriculum = getObject(row);
		return getValueAt(curriculum, col);
	}

	@Override
	public Object getValueAt(CurriculumElementWithViewsRow row, int col) {
		switch(ElementViewCols.values()[col]) {
			case key: return row.getKey();
			case elementDisplayName: return row.getCurriculumElementDisplayName();
			case elementIdentifier: return row.getCurriculumElementIdentifier();
			case entryDisplayName: return row.getRepositoryEntryName();
			case start: return row.getStartLink();
			default: return "ERROR";
		}
	}

	@Override
	public CurriculumElementWithViewsDataModel createCopyWithEmptyList() {
		return new CurriculumElementWithViewsDataModel(getTableColumnModel());
	}
	
	public enum ElementViewCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		elementDisplayName("table.header.curriculum.element.displayName"),
		elementIdentifier("table.header.curriculum.element.identifier"),
		entryDisplayName("table.header.repository.entry.displayName"),
		start("table.header.start");
		
		private final String i18nHeaderKey;
		
		private ElementViewCols(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}

		@Override
		public boolean sortable() {
			return false;
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
