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
 * Initial date: 4 Dec 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumLearningPathRepositoryDataModel extends DefaultFlexiTableDataModel<CurriculumLearningPathRepositoryRow> 
implements SortableFlexiTableDataModel<CurriculumLearningPathRepositoryRow> {

	private final Locale locale;

	public CurriculumLearningPathRepositoryDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		CurriculumLearningPathRepositoryRow identityRow = getObject(row);
		return getValueAt(identityRow, col);
	}

	@Override
	public Object getValueAt(CurriculumLearningPathRepositoryRow row, int col) {
		if(col >= 0 && col < LearningPathRepositoryCols.values().length) {
			switch(LearningPathRepositoryCols.values()[col]) {
				case reponame: return row.getRepositoryEntry().getDisplayname();
				case completion: return row;
				case passed: return row.getPassed();
				case score: return row.getScore();
				case learningPath: return row.getLearningPathLink();
				default: return "ERROR";
			}
		}
		return "ERROR";
	}

	@Override
	public void sort(SortKey orderBy) {
		List<CurriculumLearningPathRepositoryRow> rows = new CurriculumLearningPathRepositorySortDelegate(orderBy, this, locale).sort();
		super.setObjects(rows);
	}
	
	public enum LearningPathRepositoryCols implements FlexiSortableColumnDef {
		reponame("table.header.reponame"),
		completion("table.header.completion"),
		passed("table.header.passed"),
		score("table.header.score"),
		learningPath("table.header.learning.path.icon");
		
		private final String i18nKey;
		
		private LearningPathRepositoryCols(String i18nKey) {
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
