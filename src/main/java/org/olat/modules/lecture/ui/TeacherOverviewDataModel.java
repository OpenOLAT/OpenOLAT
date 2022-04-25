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
package org.olat.modules.lecture.ui;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.modules.lecture.model.LectureBlockRow;

/**
 * 
 * Initial date: 30 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeacherOverviewDataModel extends DefaultFlexiTableDataModel<LectureBlockRow>
	implements SortableFlexiTableDataModel<LectureBlockRow> {
	
	private static final TeachCols[] COLS = TeachCols.values();
	
	private final Locale locale;

	public TeacherOverviewDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		List<LectureBlockRow> rows = new TeacherOverviewSortDelegate(orderBy, this, locale).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		LectureBlockRow block = getObject(row);
		return getValueAt(block, col);
	}

	@Override
	public Object getValueAt(LectureBlockRow row, int col) {
		switch(COLS[col]) {
			case date: return row.getLectureBlock().getStartDate();
			case startTime: return row.getLectureBlock().getStartDate();
			case endTime: return row.getLectureBlock().getEndDate();
			case externalRef: return row.getEntryExternalRef();
			case entry: return row.getEntryDisplayname();
			case lectureBlock: return row.getLectureBlock().getTitle();
			case compulsory: return row.getLectureBlock().isCompulsory();
			case teachers: return row.getTeachers();
			case location: return row.getLectureBlock().getLocation();
			case status: return row.getLectureBlock();
			case details: {
				Date end = row.getLectureBlock().getEndDate();
				Date start = row.getLectureBlock().getStartDate();
				Date now = new Date();
				return end.before(new Date()) || (row.isIamTeacher() && start.compareTo(now) <= 0);
			}
			case assessmentMode: return Boolean.valueOf(row.isAssessmentMode());
			case tools: return row.getToolsLink();
			default: return null;
		}
	}
	
	public enum TeachCols implements FlexiSortableColumnDef {
		externalRef("table.header.external.ref"),
		entry("table.header.entry"),
		date("table.header.date"),
		startTime("table.header.start.time"),
		endTime("table.header.end.time"),
		lectureBlock("table.header.lecture.block"),
		location("table.header.location"),
		teachers("table.header.teachers"),
		status("table.header.status"),
		details("table.header.details"),
		tools("table.header.tools"),
		compulsory("table.header.compulsory.long"),
		assessmentMode("table.header.assessment.mode");
		
		private final String i18nKey;
		
		private TeachCols(String i18nKey) {
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
