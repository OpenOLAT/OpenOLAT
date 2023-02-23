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
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;

/**
 * 
 * Initial date: 17 févr. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoachAssignmentListTableModel extends DefaultFlexiTableDataModel<IdentityAssignmentRow>
implements SortableFlexiTableDataModel<IdentityAssignmentRow> {
	
	private static final CACols[] COLS = CACols.values();
	
	private Locale locale;
	
	public CoachAssignmentListTableModel( FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<IdentityAssignmentRow> rows = new CoachAssignmentListTableModelSortDelegate(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		IdentityAssignmentRow assignmentRow = getObject(row);
		return getValueAt(assignmentRow, col);
	}

	@Override
	public Object getValueAt(IdentityAssignmentRow assignmentRow, int col) {
		if(col >= 0 && col < COLS.length) {
			switch(COLS[col]) {
				case taskTitle: return assignmentRow.getTaskTitle();
				case notAssigned: return assignmentRow;
				default: return "ERROR";
			}
		}
		if(col >= GTACoachedGroupGradingController.USER_PROPS_OFFSET && col < CoachAssignmentListController.COACH_OFFSET) {
			int propIndex = col - GTACoachedGroupGradingController.USER_PROPS_OFFSET;
			return assignmentRow.getIdentityProp(propIndex);
		}
		if(col > CoachAssignmentListController.COACH_OFFSET) {
			return assignmentRow;
		}
		return null;
	}
	
	public enum CACols implements FlexiSortableColumnDef {
		taskTitle("table.header.group.taskTitle"),
		notAssigned("table.header.not.assigned");
		
		private final String i18nKey;
		
		private CACols(String i18nKey) {
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
