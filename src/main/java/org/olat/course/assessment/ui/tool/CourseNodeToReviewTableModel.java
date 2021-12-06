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
package org.olat.course.assessment.ui.tool;

import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * 
 * Initial date: 26 Nov 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseNodeToReviewTableModel extends DefaultFlexiTableDataModel<CourseNodeToReviewRow> implements SortableFlexiTableDataModel<CourseNodeToReviewRow> {

	public CourseNodeToReviewTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}
	
	@Override
	public void sort(SortKey orderBy) {
		SortableFlexiTableModelDelegate<CourseNodeToReviewRow> sorter
				= new SortableFlexiTableModelDelegate<>(orderBy, this, null);
		List<CourseNodeToReviewRow> views = sorter.sort();
		super.setObjects(views);
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		CourseNodeToReviewRow identityRow = getObject(row);
		return getValueAt(identityRow, col);
	}

	@Override
	public Object getValueAt(CourseNodeToReviewRow row, int col) {
		if(col >= 0 && col < ToReviewCols.values().length) {
			switch(ToReviewCols.values()[col]) {
				case courseNode: return row;
				case participant: return row.getIdentityLink();
			}
		}
		return null;
	}
	
	public enum ToReviewCols implements FlexiSortableColumnDef {
		courseNode("table.header.course.node"),
		participant("table.header.participant");
		
		private final String i18nKey;
		
		private ToReviewCols(String i18nKey) {
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