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
package org.olat.modules.selectus.ui.position;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

import org.olat.modules.selectus.ui.position.model.PositionDocumentRow;

/**
 * 
 * Initial date: 30 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionDocumentsDataModel extends DefaultFlexiTableDataModel<PositionDocumentRow> {
	
	private static final DocumentCols[] COLS = DocumentCols.values();

	public PositionDocumentsDataModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		PositionDocumentRow editRow = getObject(row);
		if(col >= 0 && col < COLS.length) {
			switch(COLS[col]) {
				case enabled: return editRow.getEnableEl();
				case documentName: return editRow.getDocumentNameEl();
				case mandatory: return editRow.getMandatoryEl();
				case usage: return editRow.getUsageEl();
				case combined: return editRow.getCombinedEl();
				case documentSize: return editRow.getDocumentSizeEl();
				case experts: return editRow.isExperts();
				case referees: return editRow.isReferees();
				case comparativeExperts: return editRow.isComparativeExperts();
				case editDocumentName: return editRow.getEditNameButton();
				default: return "ERROR";
			}
		} else if(col >= PositionDocumentsConfigurationController.FEEDBACKS_OFFSET) {
			int index = PositionDocumentsConfigurationController.FEEDBACKS_OFFSET - col;
			boolean[] configurations = editRow.getFeedbackMembers();
			if(configurations != null && index >= 0 && index < configurations.length) {
				return configurations[index];
			}
			return false;
		}
		return false;
	}
	
	public enum DocumentCols implements FlexiColumnDef {
		enabled("table.document.header.application"),
		documentName("table.document.header.name"),
		usage("table.document.header.usage"),
		mandatory("table.document.header.mandatory"),
		combined("table.document.header.combined"),
		documentSize("table.document.header.size"),
		experts("table.document.header.experts"),
		referees("table.document.header.referees"),
		comparativeExperts("table.document.header.comparative.experts"),
		editDocumentName("table.header.ml");
		
		private String i18nKey;
		
		private DocumentCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}
}