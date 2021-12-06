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
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.modules.lecture.LectureBlockAppealStatus;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.model.LectureBlockAndRollCall;

/**
 * 
 * Initial date: 13 juin 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AppealListRepositoryDataModel extends DefaultFlexiTableDataModel<AppealRollCallRow>
implements SortableFlexiTableDataModel<AppealRollCallRow>, FilterableFlexiTableModel {
	
	private static final AppealCols[] COLS = AppealCols.values();
	
	private final Locale locale;
	private final boolean authorizedAbsenceEnabled;
	private final boolean absenceDefaultAuthorized;
	
	private List<AppealRollCallRow> backups;
	
	public AppealListRepositoryDataModel(FlexiTableColumnModel columnModel,
			boolean authorizedAbsenceEnabled, boolean absenceDefaultAuthorized, Locale locale) {
		super(columnModel);
		this.locale = locale;
		this.authorizedAbsenceEnabled = authorizedAbsenceEnabled;
		this.absenceDefaultAuthorized = absenceDefaultAuthorized;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<AppealRollCallRow> rows = new AppealListRepositorySortDelegate(orderBy, this,
					authorizedAbsenceEnabled, absenceDefaultAuthorized, locale).sort();
			super.setObjects(rows);
		}
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if(filters != null && !filters.isEmpty() && filters.get(0) != null) {
			List<AppealRollCallRow> filteredRows = backups.stream()
						.filter(row -> accept(row, filters))
						.collect(Collectors.toList());
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backups);
		}
	}
	
	private boolean accept(AppealRollCallRow row, List<FlexiTableFilter> filters) {
		boolean accepted = false;
		LectureBlockAppealStatus status = row.getRollCall().getAppealStatus();
		if(status != null) {
			for(FlexiTableFilter filter:filters) {
				if(status.name().equals(filter.getFilter())) {
					accepted = true;
				}
			}
		}
		return accepted;
	}

	@Override
	public Object getValueAt(int row, int col) {
		AppealRollCallRow rollCallRow = getObject(row);
		return getValueAt(rollCallRow, col);
	}

	@Override
	public Object getValueAt(AppealRollCallRow row, int col) {
		if(col < AppealListRepositoryController.USER_PROPS_OFFSET) {
			switch(COLS[col]) {
				case lectureBlockDate: return row.getRollCall().getLectureBlock().getStartDate();
				case lectureBlockName: return row.getRollCall().getLectureBlock().getTitle();
				case coach: return row.getCoach();
				case plannedLectures: {
					if(LectureBlockStatus.cancelled.equals(row.getLectureBlockAndRollCall().getStatus())) {
						return null;
					}
					return row.getLectureBlockAndRollCall().getPlannedLecturesNumber();
				}
				case attendedLectures: {
					if(!isDataVisible(row.getLectureBlockAndRollCall())) {
						return null;
					}
					if(row.getLectureBlockAndRollCall().isCompulsory()) {
						return positive(row.getLectureBlockAndRollCall().getLecturesAttendedNumber());
					}
					return null;
				}
				case unauthorizedAbsentLectures:
				case absentLectures: {
					if(!isDataVisible(row.getLectureBlockAndRollCall())) {
						return null;
					}
					if(row.getLectureBlockAndRollCall().isCompulsory()) {
						long value;
						if(isAuthorized(row.getLectureBlockAndRollCall())) {
							value = 0l;
						} else {
							value = positive(row.getLectureBlockAndRollCall().getLecturesAbsentNumber());
						}
						return value;
					}
					return null;
				}
				case authorizedAbsentLectures: {
					if(!isDataVisible(row.getLectureBlockAndRollCall())) {
						return null;
					}
					if(row.getLectureBlockAndRollCall().isCompulsory()) {
						long value;
						if(isAuthorized(row.getLectureBlockAndRollCall())) {
							value = positive(row.getLectureBlockAndRollCall().getLecturesAbsentNumber());
						} else {
							value = 0l;
						}
						return value;
					}
					return null;
				}
				case lectureBlockStatus: return row;
				case appealStatus: return row.getRollCall().getAppealStatus();
				default: return null;
			}
		}
		
		int propPos = col - TeacherRollCallController.USER_PROPS_OFFSET;
		return row.getIdentityProp(propPos);
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
	
	private boolean isDataVisible(LectureBlockAndRollCall row) {
		LectureBlockStatus status = row.getStatus();
		if(LectureBlockStatus.cancelled.equals(status)) {
			return false;
		}
		LectureRollCallStatus rollCallStatus = row.getRollCallStatus();
		return status == LectureBlockStatus.done
			&& (rollCallStatus == LectureRollCallStatus.closed || rollCallStatus == LectureRollCallStatus.autoclosed);
	}
	
	private int positive(int num) {
		return num < 0 ? 0 : num;
	}
	
	@Override
	public void setObjects(List<AppealRollCallRow> objects) {
		super.setObjects(objects);
		backups = objects;
	}
	
	public enum AppealCols implements FlexiSortableColumnDef {
		lectureBlockDate("table.header.date"),
		lectureBlockName("table.header.lecture.block"),
		coach("table.header.teachers"),
		plannedLectures("table.header.planned.lectures"),
		attendedLectures("table.header.attended.lectures"),
		absentLectures("table.header.absent.lectures"),
		unauthorizedAbsentLectures("table.header.unauthorized.absence"),
		authorizedAbsentLectures("table.header.authorized.absence"),
		lectureBlockStatus("table.header.status"),
		appealStatus("table.header.appeal.status");
		
		private final String i18nKey;
		
		private AppealCols(String i18nKey) {
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
