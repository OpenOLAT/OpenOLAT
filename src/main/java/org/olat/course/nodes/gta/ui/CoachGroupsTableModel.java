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
package org.olat.course.nodes.gta.ui;

import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.gta.ui.component.SubmissionDateCellRenderer;

/**
 * 
 * Initial date: 11.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoachGroupsTableModel extends DefaultFlexiTableDataModel<CoachedGroupRow> implements SortableFlexiTableDataModel<CoachedGroupRow> {
	
	public CoachGroupsTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<CoachedGroupRow> views = new CoachGroupsModelSort(orderBy, this, null).sort();
			super.setObjects(views);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		CoachedGroupRow coachedGroup = getObject(row);
		return getValueAt(coachedGroup, col);
	}
	
	@Override
	public Object getValueAt(CoachedGroupRow row, int col) {
		switch(CGCols.values()[col]) {
			case name: return row.getName();
			case taskName: return row.getDownloadTaskFileLink() == null ? row.getTaskName() : row.getDownloadTaskFileLink();
			case taskTitle: return getTaskTitle(row);
			case taskStatus: return row.getTaskStatus();
			case submissionDate: return SubmissionDateCellRenderer.cascading(row);
			default: return "ERROR";
		}
	}
	
	private String getTaskTitle(CoachedGroupRow row) {
		String title = row.getTaskTitle();
		if(!StringHelper.containsNonWhitespace(title)) {
			title = row.getTaskName();
		}
		return title;
	}
	
	public enum CGCols implements FlexiSortableColumnDef {
		name("table.header.group.name"),
		taskName("table.header.group.taskName"),
		taskTitle("table.header.group.taskTitle"),
		taskStatus("table.header.group.step"),
		submissionDate("table.header.submissionDate");
		
		private final String i18nKey;
		
		private CGCols(String i18nKey) {
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
