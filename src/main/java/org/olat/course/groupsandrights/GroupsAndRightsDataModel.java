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
package org.olat.course.groupsandrights;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.translator.Translator;
import org.olat.group.right.BGRightsRole;

/**
 * 
 * Initial date: 23 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
 public class GroupsAndRightsDataModel extends DefaultFlexiTableDataModel<BGRightsRow> {
	 
	 private static final GroupRightCols[] COLS = GroupRightCols.values();
	
	private final Translator translator;

	public GroupsAndRightsDataModel(FlexiTableColumnModel columnModel, Translator translator) {
		super(columnModel);
		this.translator = translator;
	}

	@Override
	public Object getValueAt(int row, int col) {
		BGRightsRow rightsRow = getObject(row);
		if(col >= 0 && col < COLS.length) {
			switch(COLS[col]) {
				case key: return rightsRow.getKey();
				case title: return rightsRow;
				case externalId: return rightsRow.getExternalId();
				case role: return getRole(rightsRow);
				default: return "ERROR";
			}
		}
		if(col >= GroupsAndRightsController.PERMISSIONS_OFFSET) {
			int rightPos = col - GroupsAndRightsController.PERMISSIONS_OFFSET;
			return rightsRow.getRightsEl().get(rightPos).getSelection();
		}
		return null;
	}
	
	private String getRole(BGRightsRow rightsRow) {
		BGRightsRole role = rightsRow.getRole();
		switch(role) {
			case tutor: return rightsRow.getResourceType() == BGRightsResourceType.businessGroup
					? translator.translate("tutor") : translator.translate("repo.tutor");
			case participant: return rightsRow.getResourceType() == BGRightsResourceType.businessGroup
					? translator.translate("participant") : translator.translate("repo.participant");
			default: return "ERROR";
		}
	}
	
	public enum GroupRightCols implements FlexiSortableColumnDef {
		key("table.header.id"),
		title("table.header.groups"),
		externalId("table.header.externalid"),
		role("table.header.role");
		
		private final String i18nKey;
		
		private GroupRightCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return false;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
