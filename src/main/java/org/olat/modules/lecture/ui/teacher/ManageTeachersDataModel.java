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
package org.olat.modules.lecture.ui.teacher;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;


/**
 * 
 * Initial date: 2 mai 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ManageTeachersDataModel extends DefaultFlexiTableDataModel<LectureBlockTeachersRow>
implements SortableFlexiTableDataModel<LectureBlockTeachersRow>, FilterableFlexiTableModel {
	
	private static final BlockTeachersCols[] COLS = BlockTeachersCols.values();
	
	private final Locale locale;
	private List<LectureBlockTeachersRow> backups;
	
	public ManageTeachersDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<LectureBlockTeachersRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if(filters != null && !filters.isEmpty() && filters.get(0) != null) {
			List<Long> teachersKeys = null;
			boolean noTeachers = false;
			
			FlexiTableFilter teachersFilter = FlexiTableFilter.getFilter(filters, ManageTeachersController.FILTER_TEACHERS);
			if(teachersFilter instanceof FlexiTableExtendedFilter extendedFilter) {
				List<String> filterValues = extendedFilter.getValues();
				if(filterValues != null && !filterValues.isEmpty()) {
					teachersKeys = filterValues.stream()
							.filter(StringHelper::isLong)
							.map(key -> Long.valueOf(key))
							.toList();
					if(filterValues.contains(ManageTeachersController.NO_TEACHER)) {
						noTeachers = true;
					}
				}
			}
			
			List<LectureBlockTeachersRow> filteredRows = new ArrayList<>(backups.size());
			for(LectureBlockTeachersRow row:backups) {
				boolean accept = acceptTeachers(row, teachersKeys, noTeachers);
				if(accept) {
					filteredRows.add(row);
				}
			}
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backups);
		}
	}
	
	private boolean acceptTeachers(LectureBlockTeachersRow row, List<Long> teachers, boolean noTeachers) {
		MultipleSelectionElement[] teachersEl = row.getTeachersEl();
		boolean hasTeachers = false;
		boolean hasTeacher = false;
		
		if(noTeachers) {
			if(teachersEl != null && teachersEl.length > 0) {
				for(int i=teachersEl.length; i-->0; ) {
					if(teachersEl[i].isAtLeastSelected(1)) {
						hasTeachers = true;
						break;
					}
				}
			}
		}
	
		if(teachers != null && !teachers.isEmpty()) {
			for(int i=teachersEl.length; i-->0; ) {
				if(teachersEl[i].isAtLeastSelected(1)
						&& teachersEl[i].getUserObject() instanceof Identity identity
						&& teachers.contains(identity.getKey())) {
					hasTeacher = true;
					break;
				}
			}
		}
		
		return (noTeachers && !hasTeachers)
				|| (teachers != null && !teachers.isEmpty() && hasTeacher)
				|| (!noTeachers && (teachers == null || teachers.isEmpty()));
	}

	@Override
	public Object getValueAt(int row, int col) {
		LectureBlockTeachersRow blockRow = getObject(row);
		return getValueAt(blockRow, col);
	}

	@Override
	public Object getValueAt(LectureBlockTeachersRow row, int col) {
		if(col >= 0 && col < ManageTeachersController.TEACHERS_OFFSET) {
			return switch(COLS[col]) {
				case id -> row.getKey();
				case title -> row.getTitle();
				case externalId -> row.getExternalId();
				case externalRef -> row.getExternalRef();
				case date -> row.getStartDate();
				case startTime -> row.getStartDate();
				case endTime -> row.getEndDate();
				case lecturesNumber -> row.getPlannedLecturesNumber();
				case curriculumElement -> row.getCurriculumElement();
				case status -> row.getLectureBlock();
				case entry -> row.getEntry();
				case location -> row.getLectureBlockRow();
				case numParticipants -> row.getNumOfParticipants();
				case compulsory -> row.isCompulsory();
				default -> "ERROR";
			};
		}
		
		int teacherCol = col - ManageTeachersController.TEACHERS_OFFSET;
		return row.getTeacherEl(teacherCol);
	}
	
	@Override
	public void setObjects(List<LectureBlockTeachersRow> objects) {
		backups = objects;
		super.setObjects(objects);
	}

	public enum BlockTeachersCols implements FlexiSortableColumnDef {
		id("table.header.id"),
		title("lecture.title"),
		location("lecture.location"),
		date("lecture.date"),
		startTime("table.header.start.time"),
		endTime("table.header.end.time"),
		teachers("table.header.teachers"),
		tools("action.more"),
		status("table.header.exec"),
		rollCallStatus("table.header.rollcall.status"),
		compulsory("table.header.compulsory.long"),
		assessmentMode("table.header.assessment.mode"),
		dateChooser("lecture.date"),
		chosenTeachers("table.header.teachers"),
		teacherChooser("table.header.teachers.edit"),
		locationElement("lecture.location"),
		externalId("table.header.external.id"),
		externalRef("table.header.external.ref"),
		lecturesNumber("table.header.num.lecture.block"),
		numParticipants("table.header.participants"),
		curriculumElement("table.header.curriculum.element"),
		onlineMeeting("table.header.online.meeting"),
		entry("table.header.entry");
		
		private final String i18nKey;
		
		private BlockTeachersCols(String i18nKey) {
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
