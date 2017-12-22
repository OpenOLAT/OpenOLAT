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
package org.olat.course.assessment.ui.tool;

import java.util.Collections;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.model.EnhancedStatus;

/**
 * 
 * Initial date: 15 déc. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentModeOverviewListTableModel extends DefaultFlexiTableDataModel<AssessmentMode> {
	
	public AssessmentModeOverviewListTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		AssessmentMode mode = getObject(row);
		switch(ModeCols.values()[col]) {
			case status: return new EnhancedStatus(mode.getStatus(), Collections.emptyList());
			case name: return mode.getName();
			case begin: return mode.getBegin();
			case end: return mode.getEnd();
			case leadTime: return mode.getLeadTime();
			case followupTime: return mode.getFollowupTime();
			default: return "ERROR";
		}
	}
	
	@Override
	public DefaultFlexiTableDataModel<AssessmentMode> createCopyWithEmptyList() {
		return new AssessmentModeOverviewListTableModel(getTableColumnModel());
	}
	
	public enum ModeCols implements FlexiSortableColumnDef {
		status("table.header.status"),
		name("table.header.name"),
		begin("table.header.begin"),
		end("table.header.end"),
		leadTime("table.header.leadTime"),
		followupTime("table.header.followupTime");
		
		private final String i18nKey;
		
		private ModeCols(String i18nKey) {
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
