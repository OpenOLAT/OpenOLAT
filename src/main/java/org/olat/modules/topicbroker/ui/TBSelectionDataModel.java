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
package org.olat.modules.topicbroker.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * 
 * Initial date: 29 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBSelectionDataModel extends DefaultFlexiTableDataModel<TBSelectionRow> {
	
	private static final SelectionCols[] COLS = SelectionCols.values();

	public TBSelectionDataModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		TBSelectionRow project = getObject(row);
		return getValueAt(project, col);
	}

	public Object getValueAt(TBSelectionRow row, int col) {
		switch(COLS[col]) {
		case priority: return row.getPriorityLabel();
		case title: return row.getTitle();
		case status: return row;
		case minParticipants: return row.getMinParticipants();
		case maxParticipants: return row.getMaxParticipants();
		case upDown: return row.getUpDown();
		case selectionTools: return row.getSelectionToolsLink();
		case topicTools: return row.getTopicToolsLink();
		default: return null;
		}
	}
	
	public enum SelectionCols implements FlexiColumnDef {
		priority("selection.priority"),
		title("topic.title"),
		status("selection.status"),
		minParticipants("topic.participants.min"),
		maxParticipants("topic.participants.max"),
		upDown("updown"),
		selectionTools("tools"),
		topicTools("tools");
		
		private final String i18nKey;
		
		private SelectionCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}
}
