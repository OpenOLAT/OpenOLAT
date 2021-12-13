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
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 22 Nov 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentCoachingTableModel extends DefaultFlexiTableDataModel<AssessmentCoachingRow>
implements SortableFlexiTableDataModel<AssessmentCoachingRow> {
	
	public static final String USER_PROPS_ID = AssessmentCoachingTableModel.class.getCanonicalName();
	public static final int USER_PROPS_OFFSET = 500;
	private static final AssessmentCoachingsCol[] COLS = AssessmentCoachingsCol.values();
	
	private final Locale locale;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	public AssessmentCoachingTableModel(FlexiTableColumnModel columnModel,
			List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(columnModel);
		this.locale = locale;
		this.userPropertyHandlers = userPropertyHandlers;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<AssessmentCoachingRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		AssessmentCoachingRow assignmentRow = getObject(row);
		return getValueAt(assignmentRow, col);
	}

	@Override
	public Object getValueAt(AssessmentCoachingRow row, int col) {
		if(col >= 0 && col < COLS.length) {
			switch(COLS[col]) {
			case course: return row.getRepositoryEntryName();
			case courseNode: return row;
			case lastUserModified: return row.getLastUserModified();
			case statusDoneBy: return row.getStatusDoneBy();
			case statusDoneAt: return row.getStatusDoneAt();
			default: return "ERROR";
			}
		}
		
		if (col >= USER_PROPS_OFFSET && col < userPropertyHandlers.size() + USER_PROPS_OFFSET) {
			int propPos = col - USER_PROPS_OFFSET;
			return row.getIdentityProp(propPos);
		}
		return "ERROR";
	}
	
	public enum AssessmentCoachingsCol implements FlexiSortableColumnDef {
		course("table.header.course"),
		courseNode("table.header.course.node"),
		lastUserModified("table.header.lastUserModificationDate"),
		statusDoneBy("table.header.status.done.by"),
		statusDoneAt("table.header.status.done.at");
		
		private final String i18nKey;
		
		private AssessmentCoachingsCol(String i18nKey) {
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
