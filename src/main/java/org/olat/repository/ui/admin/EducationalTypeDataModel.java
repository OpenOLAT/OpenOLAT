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
package org.olat.repository.ui.admin;

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
 * Initial date: 18 Jan 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.comm
 *
 */
public class EducationalTypeDataModel extends DefaultFlexiTableDataModel<EducationalTypeRow>
implements SortableFlexiTableDataModel<EducationalTypeRow> {
	
	private static final EducationalTypeCols[] COLS = EducationalTypeCols.values();
	private final Locale locale;
	
	public EducationalTypeDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		List<EducationalTypeRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		EducationalTypeRow reason = getObject(row);
		return getValueAt(reason, col);
	}

	@Override
	public Object getValueAt(EducationalTypeRow row, int col) {
		switch(COLS[col]) {
			case identifier: return row.getEducationalType().getIdentifier();
			case translaton: return row.getTranslation();
			case cssClass: return row.getEducationalType().getCssClass();
			case numberOfCourses: return row.getNumberOfCourse();
			case edit: return Boolean.TRUE;
			case delete: return Boolean.valueOf(!row.getEducationalType().isPredefined());
			default: return null;
		}
	}

	@Override
	public DefaultFlexiTableDataModel<EducationalTypeRow> createCopyWithEmptyList() {
		return new EducationalTypeDataModel(getTableColumnModel(), locale);
	}
	
	public enum EducationalTypeCols implements FlexiSortableColumnDef {
		identifier("educational.type.identifier"),
		translaton("educational.type.translation"),
		cssClass("educational.type.css.class"),
		numberOfCourses("educational.type.number.of.courses"),
		edit("educational.type.edit"),
		delete("educational.type.delete");
		
		
		private final String i18nKey;
		
		private EducationalTypeCols(String i18nKey) {
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
