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
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockStatus;

/**
 * 
 * Initial date: 30 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeacherOverviewDataModel extends DefaultFlexiTableDataModel<LectureBlock>
	implements SortableFlexiTableDataModel<LectureBlock> {
	
	private final Locale locale;

	public TeacherOverviewDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		List<LectureBlock> rows = new TeacherOverviewSortDelegate(orderBy, this, locale).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		LectureBlock block = getObject(row);
		return getValueAt(block, col);
	}

	@Override
	public Object getValueAt(LectureBlock row, int col) {
		switch(TeachCols.values()[col]) {
			case date: return row.getStartDate();
			case startTime: return row.getStartDate();
			case endTime: return row.getEndDate();
			case lectureBlock: return row.getTitle();
			case status: return row.getStatus();
			case details: {
				Date end = row.getEndDate();
				return end.before(new Date());
			}
			case export: {
				Date start = row.getStartDate();
				LectureBlockStatus status = row.getStatus();
				return new Date().after(start) && (status.equals(LectureBlockStatus.partiallydone) || status.equals(LectureBlockStatus.done));
			}
			default: return null;
		}
	}

	@Override
	public DefaultFlexiTableDataModel<LectureBlock> createCopyWithEmptyList() {
		return new TeacherOverviewDataModel(getTableColumnModel(), locale);
	}
	
	public enum TeachCols implements FlexiSortableColumnDef {
		date("table.header.date"),
		startTime("table.header.start.time"),
		endTime("table.header.end.time"),
		lectureBlock("table.header.lecture.block"),
		status("table.header.status"),
		details("table.header.details"),
		export("table.header.export");
		
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
