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
package org.olat.modules.grade.ui.wizard;

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
 * Initial date: 21.04.2021<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GradeChangeTableModel extends DefaultFlexiTableDataModel<GradeChangeRow>
	implements SortableFlexiTableDataModel<GradeChangeRow> {
	
	public static final int USER_PROPS_OFFSET = 500;
	public static final String USAGE_IDENTIFIER = GradeChangeTableModel.class.getCanonicalName();
	private static final GradeChangeCols[] COLS = GradeChangeCols.values();
	
	private final Locale locale;
	
	public GradeChangeTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if (orderBy != null) {
			List<GradeChangeRow> views = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		GradeChangeRow identityRow = getObject(row);
		return getValueAt(identityRow, col);
	}

	@Override
	public Object getValueAt(GradeChangeRow row, int col) {
		if(col >= 0 && col < GradeChangeCols.values().length) {
			switch(COLS[col]) {
				case score: return row.getScore();
				case grade: return row.getGrade();
				case newGrade: return row.getNewGrade();
				default: return "ERROR";
			}
		}
		int propPos = col - USER_PROPS_OFFSET;
		return row.getIdentityProp(propPos);
	}
	
	public enum GradeChangeCols implements FlexiSortableColumnDef {
		score("table.header.score"),
		grade("table.header.grade"),
		newGrade("table.header.grade.new");
		
		private final String i18nKey;

		private GradeChangeCols(String i18nKey) {
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