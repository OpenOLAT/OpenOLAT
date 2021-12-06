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
package org.olat.course.condition.model;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * 
 * Initial date: 9 Jan 2020<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 *
 */
public class GroupSelectionTableModel extends DefaultFlexiTableDataModel<GroupSelectionTableContentRow> {
	
	public GroupSelectionTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		GroupSelectionTableContentRow group = getObject(row);
		
		return getValueAt(group, col);
	}
	
	public List<String> getNames() {
		List<String> names = new ArrayList<>();

		for (GroupSelectionTableContentRow row : getObjects()) {
			names.add(row.getGroupName());
		}

		return names;
	}

	public List<Long> getKeys() {
		List<Long> keys = new ArrayList<>();

		for (GroupSelectionTableContentRow row : getObjects()) {
			keys.add(row.getKey());
		}

		return keys;
	}
	
	public Object getValueAt(GroupSelectionTableContentRow row, int col) {
		switch (GroupSelectionTableColumns.values()[col]) {
		case key:
			return row.getKey();
		case groupName:
			return row.getGroupName();
		case takenPlaces:
			return row.getTakenPlaces();
		case places:
			return row.getPlaces();
		case waitinglist:
			return row.getWaitingList();
		case password:
			return row.getPassword();
			
		default:
			return "ERROR";
		}
	}
	
	public enum GroupSelectionTableColumns implements FlexiColumnDef {
		key("groupselection.key"),
		groupName("groupselection.groupname"),
		takenPlaces("groupselection.takenplaces"),
		places("groupselection.places"),
		waitinglist("groupselection.waitinglist"),
		password("groupselection.password");
		
		private final String i18nHeaderKey;
		
		private GroupSelectionTableColumns(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nHeaderKey;
		}
	}
}
