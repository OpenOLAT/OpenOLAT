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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.util.StringHelper;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.model.LectureBlockAndRollCall;

/**
 * 
 * Initial date: 29 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ParticipantLectureBlocksDataModel extends DefaultFlexiTableDataModel<LectureBlockAndRollCallRow>
implements SortableFlexiTableDataModel<LectureBlockAndRollCallRow>, FilterableFlexiTableModel {
	
	private final Locale locale;
	private final boolean authorizedAbsenceEnabled;
	private final boolean absenceDefaultAuthorized;
	
	private List<LectureBlockAndRollCallRow> backups;
	
	public ParticipantLectureBlocksDataModel(FlexiTableColumnModel columnModel,
			boolean authorizedAbsenceEnabled, boolean absenceDefaultAuthorized, Locale locale) {
		super(columnModel);
		this.locale = locale;
		this.authorizedAbsenceEnabled = authorizedAbsenceEnabled;
		this.absenceDefaultAuthorized = absenceDefaultAuthorized;
	}

	@Override
	public void sort(SortKey orderBy) {
		List<LectureBlockAndRollCallRow> rows = new ParticipantLectureBlocksSortDelegate(orderBy, this, locale).sort();
		super.setObjects(rows);
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		String key = filters == null || filters.isEmpty() || filters.get(0) == null ? null : filters.get(0).getFilter();
		if(StringHelper.containsNonWhitespace(key)) {
			List<LectureBlockAndRollCallRow> filteredRows;
			if("mandatory".equals(key)) {
				filteredRows = backups.stream()
						.filter(node -> node.getRow().isCompulsory())
						.collect(Collectors.toList());
			} else {
				filteredRows = new ArrayList<>(backups);
			}
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backups);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		LectureBlockAndRollCallRow block = getObject(row);
		return getValueAt(block, col);
	}

	@Override
	public Object getValueAt(LectureBlockAndRollCallRow row, int col) {
		switch(ParticipantCols.values()[col]) {
			case date: return row.getRow().getDate();
			case entry: return row.getRow().getEntryDisplayname();
			case lectureBlock: return row.getRow().getLectureBlockTitle();
			case coach: return row.getRow().getCoach();
			case plannedLectures: {
				if(LectureBlockStatus.cancelled.equals(row.getRow().getStatus())) {
					return null;
				}
				return row.getRow().getPlannedLecturesNumber();
			}
			case attendedLectures: {
				if(!isDataVisible(row.getRow())) {
					return null;
				}
				if(row.getRow().isCompulsory()) {
					return positive(row.getRow().getLecturesAttendedNumber());
				}
				return null;
			}
			case unauthorizedAbsentLectures:
			case absentLectures: {
				if(!isDataVisible(row.getRow())) {
					return null;
				}
				if(row.getRow().isCompulsory()) {
					long value;
					if(isAuthorized(row.getRow())) {
						value = 0l;
					} else {
						value = positive(row.getRow().getLecturesAbsentNumber());
					}
					return value;
				}
				return null;
			}
			case authorizedAbsentLectures: {
				if(!isDataVisible(row.getRow())) {
					return null;
				}
				if(row.getRow().isCompulsory()) {
					long value;
					if(isAuthorized(row.getRow())) {
						value = positive(row.getRow().getLecturesAbsentNumber());
					} else {
						value = 0l;
					}
					return value;
				}
				return null;
			}
			case status: return row;
			case appeal: return row.getAppealButton();
			default: return null;
		}
	}
	
	private boolean isDataVisible(LectureBlockAndRollCall row) {
		LectureBlockStatus status = row.getStatus();
		if(LectureBlockStatus.cancelled.equals(status)) {
			return false;
		}
		LectureRollCallStatus rollCallStatus = row.getRollCallStatus();
		return status == LectureBlockStatus.done
			&& (rollCallStatus == LectureRollCallStatus.closed || rollCallStatus == LectureRollCallStatus.autoclosed);
	}
	
	private boolean isAuthorized(LectureBlockAndRollCall row) {
		boolean authorized;
		if(authorizedAbsenceEnabled) {
			if((row.getLecturesAuthorizedAbsent() == null && absenceDefaultAuthorized)
					|| (row.getLecturesAuthorizedAbsent() != null && row.getLecturesAuthorizedAbsent().booleanValue())) {
				authorized = true;
			} else {
				authorized = false;
			}
		} else {
			authorized = false;
		}
		return authorized;
	}

	private int positive(int num) {
		return num < 0 ? 0 : num;
	}
	
	@Override
	public void setObjects(List<LectureBlockAndRollCallRow> objects) {
		super.setObjects(objects);
		backups = objects;
	}
	
	public enum ParticipantCols implements FlexiSortableColumnDef {
		date("table.header.date"),
		entry("table.header.entry"),
		lectureBlock("table.header.lecture.block"),
		coach("table.header.teachers"),
		plannedLectures("table.header.planned.lectures"),
		attendedLectures("table.header.attended.lectures"),
		absentLectures("table.header.absent.lectures"),
		unauthorizedAbsentLectures("table.header.unauthorized.absence"),
		authorizedAbsentLectures("table.header.authorized.absence"),
		status("table.header.status"),
		appeal("table.header.appeal");
		
		private final String i18nKey;
		
		private ParticipantCols(String i18nKey) {
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
