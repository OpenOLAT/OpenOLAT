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
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableFooterModel;

/**
 * 
 * Initial date: 31 juil. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RubricSliderResponsesTableModel extends DefaultFlexiTableDataModel<RubricSliderResponseRow> implements FlexiTableFooterModel {
	
	private static final RubricResponsesCols[] COLS = RubricResponsesCols.values();
	
	private RubricSliderResponseRow footerRow;

	public RubricSliderResponsesTableModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}
	
	public void setFooterRow(RubricSliderResponseRow footerRow) {
		this.footerRow = footerRow;
	}

	@Override
	public String getFooterHeader() {
		return null;
	}

	@Override
	public Object getFooterValueAt(int col) {
		return getValueAt(footerRow, col);
	}

	@Override
	public Object getValueAt(int row, int col) {
		RubricSliderResponseRow responseRow = getObject(row);
		return getValueAt(responseRow, col);
	}
	
	public Object getValueAt(RubricSliderResponseRow responseRow, int col) {
		return switch(COLS[col]) {
			case startLabel -> responseRow.getLabel();
			case evaluation -> responseRow.getAssessmentsPlot();
			case numOfNoResponses -> responseRow.getNumOfNoResponses();
			case numOfResponses -> responseRow.getNumOfResponses();
			case numOfComments -> responseRow.getNumOfComments();
			default -> "ERROR";
		};
	}
	
	public enum RubricResponsesCols implements FlexiSortableColumnDef {
		startLabel("table.header.rubric.start.label"),
		evaluation("table.header.rubric.evaluation"),
		numOfNoResponses("table.header.rubric.no.responses"),
		numOfResponses("table.header.rubric.responses"),
		numOfComments("table.header.rubric.comments");
		
		private final String i18nKey;
		
		private RubricResponsesCols(String i18nKey) {
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
