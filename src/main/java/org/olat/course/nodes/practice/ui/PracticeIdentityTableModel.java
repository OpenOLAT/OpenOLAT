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
package org.olat.course.nodes.practice.ui;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.course.assessment.ui.tool.AssessmentToolConstants;

/**
 * 
 * Initial date: 11 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PracticeIdentityTableModel extends DefaultFlexiTableDataModel<PracticeIdentityRow>
implements SortableFlexiTableDataModel<PracticeIdentityRow> {
	
	private static final PracticeIdentityCols[] COLS = PracticeIdentityCols.values();
	
	public PracticeIdentityTableModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}
	
	@Override
	public void sort(SortKey sortKey) {
		// TODO practice
	}

	@Override
	public Object getValueAt(int row, int col) {
		PracticeIdentityRow practiceRow = getObject(row);
		return getValueAt(practiceRow, col);
	}

	@Override
	public Object getValueAt(PracticeIdentityRow row, int col) {
		if(col >= 0 && col < COLS.length) {
			switch(COLS[col]) {
				case challenges: return row.getSeries();
				case status: return row.getAssessmentEntryStatus();
				default: return "ERROR";
			}
		}
		
		int propPos = col - AssessmentToolConstants.USER_PROPS_OFFSET;
		return row.getIdentityProp(propPos);
	}
	
	public enum PracticeIdentityCols implements FlexiSortableColumnDef {
		challenges("table.header.challenges"),
		status("table.header.status");
		
		private final String i18nKey;

		private PracticeIdentityCols(String i18nKey) {
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
