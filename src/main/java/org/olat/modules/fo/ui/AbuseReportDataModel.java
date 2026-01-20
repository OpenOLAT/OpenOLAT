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
package org.olat.modules.fo.ui;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.fo.ui.AbuseReportAdminController.AbuseReportRow;

/**
 * Data model for abuse reports table.
 * 
 * Initial date: January 2026<br>
 * @author OpenOLAT Community
 */
public class AbuseReportDataModel extends DefaultFlexiTableDataModel<AbuseReportRow> 
		implements SortableFlexiTableDataModel<AbuseReportRow> {
	
	public AbuseReportDataModel(FlexiTableColumnModel columnModel, Translator translator) {
		super(columnModel);
	}

	@Override
	public void sort(SortKey orderBy) {
		// Sorting not implemented for now
	}

	@Override
	public Object getValueAt(int row, int col) {
		AbuseReportRow report = getObject(row);
		return getValueAt(report, col);
	}

	@Override
	public Object getValueAt(AbuseReportRow row, int col) {
		switch(AbuseReportCols.values()[col]) {
			case message: return row.getMessageTitle();
			case reporter: return row.getReporterName();
			case date: return row.getReportDate();
			case reason: return row.getReason();
			case status: return row.getStatus();
			case actions: return row;
			default: return null;
		}
	}
	
	public enum AbuseReportCols implements FlexiSortableColumnDef {
		message("abuse.reports.message"),
		reporter("abuse.reports.reporter"),
		date("abuse.reports.date"),
		reason("abuse.reports.reason"),
		status("abuse.reports.status.pending"),
		actions("abuse.reports.actions");
		
		private final String i18nKey;
		
		private AbuseReportCols(String i18nKey) {
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
