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
package org.olat.modules.quality.generator.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementType;

/**
 * 
 * Initial date: 21.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class CurriculumElementListDataModel extends DefaultFlexiTableDataModel<CurriculumElement>
		implements SortableFlexiTableDataModel<CurriculumElement> {
	
	private final Locale locale;
	
	CurriculumElementListDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		List<CurriculumElement> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		CurriculumElement curriculumElement = getObject(row);
		return getValueAt(curriculumElement, col);
	}

	@Override
	public Object getValueAt(CurriculumElement row, int col) {
		switch(Cols.values()[col]) {
			case displayName: return row.getDisplayName();
			case identifier: return row.getIdentifier();
			case typeName: {
				CurriculumElementType type = row.getType();
				return type != null? type.getDisplayName(): null;
			}
			case begin: return row.getBeginDate();
			case end: return row.getEndDate();
			default: return null;
		}
	}

	@Override
	public DefaultFlexiTableDataModel<CurriculumElement> createCopyWithEmptyList() {
		return new CurriculumElementListDataModel(getTableColumnModel(), locale);
	}
	
	public enum Cols implements FlexiSortableColumnDef {
		displayName("curriculum.element.display.name"),
		identifier("curriculum.element.identifier"),
		typeName("curriculum.element.type.name"),
		begin("curriculum.element.begin"),
		end("curriculum.element.end");
		
		private final String i18nHeaderKey;
		
		private Cols(String i18nHeaderKey) {
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
