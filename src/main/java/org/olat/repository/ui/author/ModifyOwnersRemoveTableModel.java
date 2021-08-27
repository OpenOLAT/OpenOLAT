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
package org.olat.repository.ui.author;

import java.util.List;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * Initial date: Jan 3, 2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ModifyOwnersRemoveTableModel extends DefaultFlexiTableDataModel<ModifyOwnersRemoveTableRow>{

	public ModifyOwnersRemoveTableModel(FlexiTableColumnModel columnModel, List<ModifyOwnersRemoveTableRow> rows) {
		super(rows, columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		ModifyOwnersRemoveTableRow tableRow = getObject(row);
		
		return getValueAt(tableRow, col);
	}
	
	public Object getValueAt(ModifyOwnersRemoveTableRow tableRow, int col) {
		switch (ModifyOwnersStep1Cols.values()[col]) {
			case firstName:
				return tableRow.getIdentity().getUser().getFirstName();
			case lastName:
				return tableRow.getIdentity().getUser().getLastName();
			case nickName:
				return tableRow.getIdentity().getUser().getNickName();
			case resourcesCount:
				return tableRow.getResourcesCount();
			case resourcesDetails:
				return tableRow.getDetailsLink();
			default: 
				return "error";
		}
	}

	@Override
	public DefaultFlexiTableDataModel<ModifyOwnersRemoveTableRow> createCopyWithEmptyList() {
		return null;
	}
	
	public enum ModifyOwnersStep1Cols implements FlexiColumnDef {
		firstName("table.name.firstName"),
		lastName("table.name.lastName"),
		nickName("table.name.nickName"),
		resourcesCount("modify.owners.resources.count"),
		resourcesDetails("modify.owners.resources.details");
		
		private final String i18nHeaderKey;
		
		private ModifyOwnersStep1Cols(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nHeaderKey;
		}
	}

}
