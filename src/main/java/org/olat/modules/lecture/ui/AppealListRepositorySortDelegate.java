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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.model.LectureBlockAndRollCall;
import org.olat.modules.lecture.ui.AppealListRepositoryDataModel.AppealCols;

/**
 * 
 * Initial date: 14 juin 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AppealListRepositorySortDelegate extends SortableFlexiTableModelDelegate<AppealRollCallRow> {
	
	private static final AppealCols[] COLS = AppealCols.values();

	private final boolean authorizedAbsenceEnabled;
	private final boolean absenceDefaultAuthorized;

	public AppealListRepositorySortDelegate(SortKey orderBy, AppealListRepositoryDataModel tableModel,
			boolean authorizedAbsenceEnabled, boolean absenceDefaultAuthorized, Locale locale) {
		super(orderBy, tableModel, locale);
		this.authorizedAbsenceEnabled = authorizedAbsenceEnabled;
		this.absenceDefaultAuthorized = absenceDefaultAuthorized;
	}
	
	@Override
	protected void sort(List<AppealRollCallRow> rows) {
		int columnIndex = getColumnIndex();
		if(columnIndex < AppealListRepositoryController.USER_PROPS_OFFSET) {
			switch(COLS[columnIndex]) {
				case lectureBlockStatus: Collections.sort(rows, new StatusComparator()); break;
				default: super.sort(rows); break;
			}
		} else {
			super.sort(rows);
		}
	}
	
	private class StatusComparator implements Comparator<AppealRollCallRow> {
		@Override
		public int compare(AppealRollCallRow o1, AppealRollCallRow o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			
			LectureBlockAndRollCall r1 = o1.getLectureBlockAndRollCall();
			LectureBlockAndRollCall r2 = o2.getLectureBlockAndRollCall();
			if(r1 == null || r2 == null) {
				return compareNullObjects(r1, r2);
			}

			int s1 = stage(r1);
			int s2 = stage(r2);
			int c = Integer.compare(s1, s2);
			if(c == 0) {
				c = compareString(r1.getLectureBlockTitle(), r2.getLectureBlockTitle());
			}
			return c;
		}
		
		private int stage(LectureBlockAndRollCall rollCall) {
			int stage;
			if(rollCall.isRollCalled()) {
				LectureBlockStatus status = rollCall.getStatus();
				LectureRollCallStatus rollCallStatus = rollCall.getRollCallStatus();
				if(status == LectureBlockStatus.cancelled) {
					stage = 0;
				} else if(status == LectureBlockStatus.done
						&& (rollCallStatus == LectureRollCallStatus.closed || rollCallStatus == LectureRollCallStatus.autoclosed)) {
					
					if(rollCall.isCompulsory()) {
						int numOfLectures = rollCall.getPlannedLecturesNumber();
						if(rollCall.getLecturesAttendedNumber() >= numOfLectures) {
							stage = 3;
						} else if(authorizedAbsenceEnabled) {
							if(absenceDefaultAuthorized && rollCall.getLecturesAuthorizedAbsent() == null) {
								stage = 3;
							} else if(rollCall.getLecturesAuthorizedAbsent() != null && rollCall.getLecturesAuthorizedAbsent().booleanValue()) {
								stage = 11;
							} else {
								stage = 12;
							}
						} else {
							stage = 15;
						}
					} else {
						stage = 2;
					}
					
				} else {
					stage = 25;
				}
			} else if(!rollCall.isCompulsory()) {
				stage = 1;
			} else {
				stage = -1;
			}
			return stage;
		}
	}
}