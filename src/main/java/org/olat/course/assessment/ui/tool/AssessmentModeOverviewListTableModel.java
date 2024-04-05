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
package org.olat.course.assessment.ui.tool;

import java.util.Collections;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.course.assessment.model.EnhancedStatus;

/**
 * 
 * Initial date: 15 déc. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class AssessmentModeOverviewListTableModel extends DefaultFlexiTableDataModel<AssessmentModeOverviewRow> {
	
	public AssessmentModeOverviewListTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		AssessmentModeOverviewRow mode = getObject(row);
		return switch (ModeCols.values()[col]) {
			case status -> new EnhancedStatus(mode.getAssessmentMode().getStatus(),
					mode.getAssessmentMode().getEndStatus(), Collections.emptyList());
			case name -> mode.getAssessmentMode().getName();
			case begin -> mode.getAssessmentMode().getBegin();
			case end -> mode.getAssessmentMode().getEnd();
			case leadTime -> mode.getAssessmentMode().getLeadTime();
			case followupTime -> mode.getAssessmentMode().getFollowupTime();
			default -> "ERROR";
		};
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
