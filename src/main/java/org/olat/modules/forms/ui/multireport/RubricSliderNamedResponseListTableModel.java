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
package org.olat.modules.forms.ui.multireport;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * 
 * Initial date: 2 août 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RubricSliderNamedResponseListTableModel extends DefaultFlexiTableDataModel<RubricSliderNamedResponseRow> {
	
	private static final RubricUserResponseCols[] COLS = RubricUserResponseCols.values();

	public RubricSliderNamedResponseListTableModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		RubricSliderNamedResponseRow responseRow = getObject(row);
		if(col >= 0 && col < COLS.length) {
			return switch(COLS[col]) {
				case user -> responseRow.getUser();
				case slider -> responseRow.getRatingComponent();
				case noResponse -> responseRow.getNoResponseComponent();
				case comment -> responseRow.getComment();
				default -> "ERROR";
			};
		}
		
		if(col >= RubricSliderNamedResponseListTableController.STEPS_OFFSET) {
			return responseRow;
		}
		return "ERROR";
	}
	
	public enum RubricUserResponseCols implements FlexiSortableColumnDef {
		user("table.header.user"),
		slider("table.header.rubric.slider"),
		noResponse("table.header.rubric.no.response"),
		comment("table.header.rubric.comments");
		
		private final String i18nKey;
		
		private RubricUserResponseCols(String i18nKey) {
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
