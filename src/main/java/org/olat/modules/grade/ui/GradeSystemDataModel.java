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
package org.olat.modules.grade.ui;

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
 * Initial date: 17 Feb 2022<br
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.comm
 *
 */
public class GradeSystemDataModel extends DefaultFlexiTableDataModel<GradeSystemRow>
implements SortableFlexiTableDataModel<GradeSystemRow> {
	
	private static final GradeSystemCols[] COLS = GradeSystemCols.values();
	private final Locale locale;
	
	public GradeSystemDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		List<GradeSystemRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		GradeSystemRow gradeSystemRow = getObject(row);
		return getValueAt(gradeSystemRow, col);
	}

	@Override
	public Object getValueAt(GradeSystemRow row, int col) {
		switch(COLS[col]) {
			case identifier: return row.getGradeSystem().getIdentifier();
			case name: return row.getName();
			case label: return row.getLabel();
			case usageCount: return row.getScaleCount();
			case enabled: return Boolean.valueOf(row.getGradeSystem().isEnabled());
			case tools: return row.getToolsLink();
			default: return null;
		}
	}
	
	public enum GradeSystemCols implements FlexiSortableColumnDef {
		identifier("grade.system.identifier"),
		name("grade.system.name"),
		label("grade.system.label"),
		usageCount("grade.system.usage.count"),
		enabled("grade.system.enabled"),
		tools("table.header.actions");
		
		private final String i18nKey;
		
		private GradeSystemCols(String i18nKey) {
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
