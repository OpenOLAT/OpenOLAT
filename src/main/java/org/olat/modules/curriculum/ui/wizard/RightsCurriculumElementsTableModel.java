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
package org.olat.modules.curriculum.ui.wizard;

import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableFooterModel;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.ui.member.MembershipModification;

/**
 * 
 * Initial date: 9 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RightsCurriculumElementsTableModel extends DefaultFlexiTreeTableDataModel<RightsCurriculumElementRow>
implements FlexiTableFooterModel {

	private static final RightsElementsCols[] COLS = RightsElementsCols.values();

	public RightsCurriculumElementsTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		//
	}
	
	@Override
	public boolean hasChildren(int row) {
		return false;
	}
	
	@Override
	public boolean hasOpenCloseAll() {
		return false;
	}
	
	public boolean isParentOf(RightsCurriculumElementRow parentRow, RightsCurriculumElementRow node) {
		for(RightsCurriculumElementRow parent=node.getParent(); parent != null; parent=parent.getParent()) {
			if(parent != null && parent.getKey().equals(parentRow.getKey())) {
				return true;
			}
		}
		return false;
	}
	
	public int getIndexOf(RightsCurriculumElementRow row) {
		int numOfObjects = getRowCount();
		for(int i=0; i<numOfObjects; i++) {
			RightsCurriculumElementRow object = getObject(i);
			if(object.getKey().equals(row.getKey())) {
				return i;
			}
		}
		return -1;
	}
	
	public RightsCurriculumElementRow getObject(CurriculumElement curriculumElement) {
		List<RightsCurriculumElementRow> rows = getObjects();
		for(RightsCurriculumElementRow row:rows) {
			if(curriculumElement.getKey().equals(row.getKey())) {
				return row;
			}
		}
		return null;
	}
	
	public boolean hasModifications() {
		List<RightsCurriculumElementRow> rows = getObjects();
		for(RightsCurriculumElementRow row:rows) {
			if(row.getModification() != null) {
				return true;
			}
		}
		return false;
		
	}

	@Override
	public Object getValueAt(int row, int col) {
		RightsCurriculumElementRow detailsRow = getObject(row);
		CurriculumElement element = detailsRow.getCurriculumElement();
		return switch(COLS[col]) {
			case modifications -> detailsRow.getModification() != null;
			case key -> element.getKey();
			case displayName -> element.getDisplayName();
			case externalRef -> element.getIdentifier();
			case externalId -> element.getExternalId();
			case roleToModify -> getAction(detailsRow);
			case note -> detailsRow.getNoteButton();
			default -> "ERROR";
		};
	}
	
	private Object getAction(RightsCurriculumElementRow detailsRow) {
		MembershipModification mod = detailsRow.getModification();
		return mod == null ? detailsRow.getAddButton() : mod.nextStatus();
	}

	@Override
	public String getFooterHeader() {
		return "";
	}

	@Override
	public Object getFooterValueAt(int col) {
		if(col == RightsElementsCols.roleToModify.ordinal()) {
			int count = countRoles();
			return count + "/" + getRowCount() ;	
		}
		return null;
	}
	
	private int countRoles() {
		int count = 0;
		for(RightsCurriculumElementRow detailsRow:getObjects()) {
			if(detailsRow.getModification() != null) {
				count++;
			}
		}
		return count;
	}

	public enum RightsElementsCols implements FlexiSortableColumnDef {
		modifications("table.header.modification"),
		key("table.header.key"),
		displayName("table.header.displayName"),
		externalRef("table.header.external.ref"),
		externalId("table.header.external.id"),
		roleToModify("table.header.external.id"),
		note("table.header.external.id");
		
		private final String i18nKey;
		
		private RightsElementsCols(String i18nKey) {
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
