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

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.modules.lecture.model.LectureBlockStatistics;

/**
 * 
 * Initial date: 28 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ParticipantLecturesDataModel extends DefaultFlexiTableDataModel<LectureBlockStatistics>
implements SortableFlexiTableDataModel<LectureBlockStatistics> {
	
	private final Locale locale;
	
	public ParticipantLecturesDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		List<LectureBlockStatistics> rows = new ParticipantLecturesSortDelegate(orderBy, this, locale).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		LectureBlockStatistics stats = getObject(row);
		return getValueAt(stats, col);
	}
	
	@Override
	public Object getValueAt(LectureBlockStatistics row, int col) {
		switch(LecturesCols.values()[col]) {
			case entry: return row.getDisplayName();
			case plannedLectures: return row.getTotalPersonalPlannedLectures();
			case attendedLectures: return row.getTotalAttendedLectures();
			case authorizedAbsentLectures: return row.getTotalAuthorizedAbsentLectures();
			case absentLectures: return row.getTotalAbsentLectures();
			case progress: return row;
			case rateWarning: {
				if(row.getTotalEffectiveLectures() <= 0) {
					return null;
				}
				return row;
			}
			case rate: {
				if(row.getTotalEffectiveLectures() <= 0) {
					return null;
				}
				return row.getAttendanceRate();
			}
			default: return null;
		}
	}

	@Override
	public DefaultFlexiTableDataModel<LectureBlockStatistics> createCopyWithEmptyList() {
		return new ParticipantLecturesDataModel(getTableColumnModel(), locale);
	}
	
	public enum LecturesCols implements FlexiSortableColumnDef {
		entry("table.header.entry"),
		plannedLectures("table.header.planned.lectures"),
		attendedLectures("table.header.attended.lectures"),
		authorizedAbsentLectures("table.header.authorized.absence"),
		absentLectures("table.header.absent.lectures"),
		progress("table.header.progress"),
		rateWarning("table.header.rate.warning"),
		rate("table.header.rate");
		
		private final String i18nKey;
		
		private LecturesCols(String i18nKey) {
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
