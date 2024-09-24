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
package org.olat.course.nodes.cns.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * 
 * Initial date: 22 Sep 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CNSParticipantDetailsDataModel extends DefaultFlexiTableDataModel<CNSParticipantDetailsRow> {
	
	static final CNSParticipantDetailsCols[] COLS = CNSParticipantDetailsCols.values();
	
	public CNSParticipantDetailsDataModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		CNSParticipantDetailsRow identityDetailsRow = getObject(row);
		return getValueAt(identityDetailsRow, col);
	}

	public Object getValueAt(CNSParticipantDetailsRow row, int col) {
		switch(COLS[col]) {
		case courseNode: return row;
		case learningProgress: return row.getEvaluation();
		case status: return row.getLearningPathStatus();
		case obligation: return row.getObligationFormItem();
		default: return null;
		}
	}
	
	public enum CNSParticipantDetailsCols implements FlexiSortableColumnDef {
		courseNode("selection.node"),
		learningProgress("table.header.learning.progress"),
		status("table.header.status"),
		obligation("table.header.obligation");
		
		private final String i18nKey;

		private CNSParticipantDetailsCols(String i18nKey) {
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