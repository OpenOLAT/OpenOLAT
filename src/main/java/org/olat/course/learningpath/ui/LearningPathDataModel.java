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

import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * 
 * Initial date: 16 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathDataModel extends DefaultFlexiTreeTableDataModel<LearningPathRow> {

	public LearningPathDataModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		//
	}
	
	@Override
	public boolean hasChildren(int row) {
		LearningPathRow viewRow = getObject(row);
		return viewRow.hasChildren();
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		LearningPathRow reason = getObject(row);
		return getValueAt(reason, col);
	}

	public Object getValueAt(LearningPathRow row, int col) {
		switch(LearningPathCols.values()[col]) {
			case learningPathStatus: return row.getLearningPathNode();
			case progress: return row.getLearningPathNode();
			case learningProgress: return row.getLearningPathNode();
			case node: return row;
			case start: return row.getStartDate();
			case end: return row.getEndDateFormItem() != null
					? row.getEndDateFormItem()
					: row.getEndDate() != null? row.getEndDate().getCurrent(): null;
			case obligation: return row.getObligationFormItem() != null
					? row.getObligationFormItem()
					: row.getTranslatedObligation();
			case duration: return row.getDuration();
			case firstVisit: return row.getFirstVisit();
			case lastVisit: return row.getLastVisit();
			case status: return row.getStatus();
			case fullyAssessedDate: return row.getFullyAssessedDate();
			default: return null;
		}
	}

	@Override
	public DefaultFlexiTableDataModel<LearningPathRow> createCopyWithEmptyList() {
		return new LearningPathDataModel(getTableColumnModel());
	}
	
	public enum LearningPathCols implements FlexiColumnDef {
		learningPathStatus("table.header.learning.path.status"),
		node("table.header.node"),
		progress("table.header.progress"),
		learningProgress("table.header.learning.progress"),
		start("table.header.start"),
		end("table.header.end"),
		obligation("table.header.obligation"),
		duration("table.header.duration"),
		firstVisit("table.header.first.visit"),
		lastVisit("table.header.last.visit"),
		status("table.header.status"),
		fullyAssessedDate("table.header.fully.assessed.date");
		
		private final String i18nKey;
		
		private LearningPathCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

	}
}
