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
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.model.LectureBlockAndRollCall;
import org.olat.modules.lecture.ui.ParticipantLectureBlocksController.AppealCallback;

/**
 * 
 * Initial date: 29 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ParticipantLectureBlocksDataModel extends DefaultFlexiTableDataModel<LectureBlockAndRollCall>
implements SortableFlexiTableDataModel<LectureBlockAndRollCall> {
	
	private final Locale locale;
	private final AppealCallback appealCallback;
	private final boolean authorizedAbsenceEnabled;
	private final boolean absenceDefaultAuthorized;
	
	public ParticipantLectureBlocksDataModel(FlexiTableColumnModel columnModel, AppealCallback appealCallback,
			boolean authorizedAbsenceEnabled, boolean absenceDefaultAuthorized, Locale locale) {
		super(columnModel);
		this.locale = locale;
		this.appealCallback = appealCallback;
		this.authorizedAbsenceEnabled = authorizedAbsenceEnabled;
		this.absenceDefaultAuthorized = absenceDefaultAuthorized;
	}

	@Override
	public void sort(SortKey orderBy) {
		List<LectureBlockAndRollCall> rows = new ParticipantLectureBlocksSortDelegate(orderBy, this, locale).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		LectureBlockAndRollCall block = getObject(row);
		return getValueAt(block, col);
	}

	@Override
	public Object getValueAt(LectureBlockAndRollCall row, int col) {
		switch(ParticipantCols.values()[col]) {
			case compulsory: return row;
			case date: return row.getDate();
			case entry: return row.getEntryDisplayname();
			case lectureBlock: return row.getLectureBlockTitle();
			case coach: return row.getCoach();
			case plannedLectures: {
				if(LectureBlockStatus.cancelled.equals(row.getStatus())) {
					return null;
				}
				return row.isCompulsory() ? row.getPlannedLecturesNumber() : null;
			}
			case attendedLectures: {
				if(LectureBlockStatus.cancelled.equals(row.getStatus())) {
					return null;
				}
				if(row.isCompulsory()) {
					return row.getLecturesAttendedNumber() < 0 ? 0 : row.getLecturesAttendedNumber();
				}
				return null;
			}
			case absentLectures: {
				if(LectureBlockStatus.cancelled.equals(row.getStatus())) {
					return null;
				}
				if(row.isCompulsory()) {
					long value;
					if(isAuthorized(row)) {
						value = 0l;
					} else {
						value = positive(row.getLecturesAbsentNumber());
					}
					return value;
				}
				return null;
			}
			case authorizedAbsentLectures: {
				if(LectureBlockStatus.cancelled.equals(row.getStatus())) {
					return null;
				}
				if(row.isCompulsory()) {
					long value;
					if(isAuthorized(row)) {
						value = positive(row.getLecturesAbsentNumber());
					} else {
						value = 0l;
					}
					return value;
				}
				return null;
			}
			case appeal: {
				if(LectureBlockStatus.cancelled.equals(row.getStatus())) {
					return false;
				}
				return row.isCompulsory() && appealCallback.appealAllowed(row);
			}
			default: return null;
		}
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
	public DefaultFlexiTableDataModel<LectureBlockAndRollCall> createCopyWithEmptyList() {
		return new ParticipantLectureBlocksDataModel(getTableColumnModel(), appealCallback,
				authorizedAbsenceEnabled, absenceDefaultAuthorized, locale);
	}
	
	public enum ParticipantCols implements FlexiSortableColumnDef {
		compulsory("table.header.compulsory"),
		date("table.header.date"),
		entry("table.header.entry"),
		lectureBlock("table.header.lecture.block"),
		coach("table.header.teachers"),
		plannedLectures("table.header.planned.lectures"),
		attendedLectures("table.header.attended.lectures"),
		absentLectures("table.header.absent.lectures"),
		authorizedAbsentLectures("table.header.authorized.absence"),
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
