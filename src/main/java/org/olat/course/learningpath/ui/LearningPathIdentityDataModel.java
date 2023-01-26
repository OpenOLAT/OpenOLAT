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
package org.olat.course.learningpath.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;

/**
 * 
 * Initial date: 2 Dec 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathIdentityDataModel extends DefaultFlexiTableDataModel<LearningPathIdentityRow> 
implements SortableFlexiTableDataModel<LearningPathIdentityRow> {
	
	static final int USER_PROPS_OFFSET = 500;

	private final Locale locale;

	public LearningPathIdentityDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public Object getValueAt(int row, int col) {
		LearningPathIdentityRow identityRow = getObject(row);
		return getValueAt(identityRow, col);
	}

	@Override
	public Object getValueAt(LearningPathIdentityRow row, int col) {
		if(col >= 0 && col < LearningPathIdentityCols.values().length) {
			switch(LearningPathIdentityCols.values()[col]) {
				case progress: return row;
				case completion: return row.getCompletion();
				case passed: return row.getPassed();
				case score: return row.getScore();
				default: return "ERROR";
			}
		}
		int propPos = col - USER_PROPS_OFFSET;
		return row.getIdentityProp(propPos);
	}

	@Override
	public void sort(SortKey orderBy) {
		List<LearningPathIdentityRow> rows = new LearningPathIdentitySortDelegate(orderBy, this, locale).sort();
		super.setObjects(rows);
	}
	
	public enum LearningPathIdentityCols implements FlexiSortableColumnDef {
		progress("table.header.completion"),
		completion("table.header.completion"),
		passed("table.header.passed"),
		score("table.header.score");
		
		private final String i18nKey;
		
		private LearningPathIdentityCols(String i18nKey) {
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
