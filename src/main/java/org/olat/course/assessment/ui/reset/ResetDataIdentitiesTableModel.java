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
package org.olat.course.assessment.ui.reset;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.course.assessment.ui.tool.AssessmentToolConstants;

/**
 * 
 * Initial date: 8 mars 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ResetDataIdentitiesTableModel extends DefaultFlexiTableDataModel<ResetDataIdentityRow>
implements SortableFlexiTableDataModel<ResetDataIdentityRow> {
	
	private static final IdentityCols[] COLS = IdentityCols.values();
	
	public ResetDataIdentitiesTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}
	
	@Override
	public void sort(SortKey sortKey) {
		//
	}

	@Override
	public Object getValueAt(int row, int col) {
		ResetDataIdentityRow pRow = getObject(row);
		return getValueAt(pRow, col);
	}

	@Override
	public Object getValueAt(ResetDataIdentityRow row, int col) {
		if(col >= 0 && col < COLS.length) {
			switch(COLS[col]) {
				case score: return row;
				case passed: return row.getPassed();
				case assessmentStatus: return row.getAssessmentStatus();
				case initialLaunchDate: return row.getInitialCourseLaunchDate();
				case lastModified: return row.getLastModified();
				case lastUserModified: return row.getLastUserModified();
				case lastCoachModified: return row.getLastCoachModified();
				default: return "ERROR";
			}
		}
		
		if(col >= AssessmentToolConstants.USER_PROPS_OFFSET) {
			int propPos = col - AssessmentToolConstants.USER_PROPS_OFFSET;
			return row.getIdentityProp(propPos);
		}
		return "ERROR";
	}
	
	public enum IdentityCols implements FlexiSortableColumnDef {
		score("table.header.score"),
		assessmentStatus("table.header.assessmentStatus"),
		passedOverriden("table.header.passed.overriden"),
		passed("table.header.passed"),
		initialLaunchDate("table.header.initialLaunchDate"),
		lastModified("table.header.lastScoreDate"),
		lastUserModified("table.header.lastUserModificationDate"),
		lastCoachModified("table.header.lastCoachModificationDate");
		
		private final String i18nKey;

		private IdentityCols(String i18nKey) {
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
