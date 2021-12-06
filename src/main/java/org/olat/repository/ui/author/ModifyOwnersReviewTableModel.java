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

import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * Initial date: Jan 4, 2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ModifyOwnersReviewTableModel extends DefaultFlexiTreeTableDataModel<ModifyOwnersReviewTableRow>{

	public ModifyOwnersReviewTableModel(FlexiTableColumnModel columnModel, List<ModifyOwnersReviewTableRow> tableRows) {
		super(columnModel);
		setObjects(tableRows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		ModifyOwnersReviewTableRow tableRow = getObject(row);
		
		switch (ModifyOwnersReviewCols.values()[col]) {
			case resourceOrIdentity:
				return tableRow.getResourceOrIdentity();
			case state:
				return tableRow.getState();
			default:
				return "ERROR";
		}
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		
	}
	
	@Override
	public boolean hasChildren(int row) {
		return getObject(row).hasChildren();
	}
	
	public enum ModifyOwnersReviewCols implements FlexiColumnDef {
		resourceOrIdentity("modify.owners.review.resource"),
		state("modify.owners.review.state");

		private final String i18nKey;
		
		private ModifyOwnersReviewCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
		
	}

}
