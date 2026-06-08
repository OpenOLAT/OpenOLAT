/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.committee.assignment;

import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.user.propertyhandlers.UserPropertyHandler;

import org.olat.modules.selectus.ui.committee.list.PositionCommitteeController;

/**
 * 
 * Initial date: 24 oct. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CommitteeListDataModel extends DefaultFlexiTableDataModel<AssigneeRow>
implements SortableFlexiTableDataModel<AssigneeRow> {
	
	private static final CommitteeCols[] COLUMNS = CommitteeCols.values();
	
	private final Translator translator;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	public CommitteeListDataModel(FlexiTableColumnModel columnsModel, List<UserPropertyHandler> userPropertyHandlers,
			Translator translator) {
		super(columnsModel);
		this.translator = translator;
		this.userPropertyHandlers = userPropertyHandlers;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<AssigneeRow> members = new SortableFlexiTableModelDelegate<>(orderBy, this, null).sort();
			super.setObjects(members);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		AssigneeRow assigneeRow = getObject(row);
		return getValueAt(assigneeRow, col);
	}

	@Override
	public Object getValueAt(AssigneeRow row, int col) {
		if(col >= 0 && col < COLUMNS.length) {
			switch(COLUMNS[col]) {
				case title: return toTitle(row.getIdentity());
				case name: return row.getIdentity().getUser().getProperty(UserConstants.LASTNAME, null) + ", " 
						+ row.getIdentity().getUser().getProperty(UserConstants.FIRSTNAME, null);
				case role: return row.getPositionRole();
				case institution: return row.getIdentity().getUser().getProperty(UserConstants.INSTITUTIONALNAME, null);
				case assignments: return row.getNumOfAssignments();
				default: return "ERROR";
			}
		} else if(col >= PositionCommitteeController.USER_PROP_OFFSET) {
			int propIndex = col - PositionCommitteeController.USER_PROP_OFFSET;
			UserPropertyHandler prop = userPropertyHandlers.get(propIndex);
			return prop.getUserProperty(row.getIdentity().getUser(), translator.getLocale());
		}
		return "ERROR";
	}
	
	private String toTitle(Identity identity) {
		String title = identity.getUser().getProperty("title", null);
		return "-".equals(title) ? "" : title;
	}
	
	public enum CommitteeCols implements FlexiSortableColumnDef {
		title("edit.committee.title"),
		name("edit.committee.name"),
		role("edit.committee.role"),
		institution("edit.committee.institution"),
		assignments("table.header.assignments.stats");

		private final String key;
		
		private CommitteeCols(String key) {
			this.key = key;
		}
		
		@Override
		public String i18nHeaderKey() {
			return key;
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
