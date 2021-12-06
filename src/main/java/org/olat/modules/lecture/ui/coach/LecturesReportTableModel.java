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
package org.olat.modules.lecture.ui.coach;

import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.logging.Tracing;
import org.olat.modules.lecture.model.LectureReportRow;

/**
 * 
 * Initial date: 14 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesReportTableModel extends DefaultFlexiTableDataModel<LectureReportRow>
implements SortableFlexiTableDataModel<LectureReportRow> {
	
	private static final Logger log = Tracing.createLoggerFor(LecturesReportTableModel.class);
	
	private final Locale locale;
	
	public LecturesReportTableModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			try {
				List<LectureReportRow> rows = new LecturesReportTableModelSortDelegate(orderBy, this, locale).sort();
				setObjects(rows);
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		LectureReportRow reportRow = getObject(row);
		return getValueAt(reportRow, col);
	}

	@Override
	public Object getValueAt(LectureReportRow row, int col) {
		switch(ReportCols.values()[col]) {
			case key: return row.getKey();
			case date: return row.getStartDate();
			case start: return row.getStartDate();
			case end: return row.getEndDate();
			case lectureBlockTitle: return row.getTitle();
			case externalRef: return row.getExternalRef();
			case owners: return row.getOwners();
			case teachers: return row.getTeachers();
			case status: return row.getRollCallStatus();
			default: return null;
		}
	}
	
	public enum ReportCols implements FlexiSortableColumnDef {
		key("table.header.id"),
		date("table.header.date"),
		start("table.header.start.time"),
		end("table.header.end.time"),
		externalRef("table.header.identifier"),
		lectureBlockTitle("table.header.lecture.block"),
		owners("table.header.owners"),
		teachers("table.header.teachers"),
		status("table.header.status");
		
		private final String i18nKey;
		
		private ReportCols(String i18nKey) {
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
