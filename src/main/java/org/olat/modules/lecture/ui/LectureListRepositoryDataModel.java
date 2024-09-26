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
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.id.Identity;
import org.olat.modules.lecture.model.LectureBlockRow;

/**
 * 
 * Initial date: 24 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureListRepositoryDataModel extends DefaultFlexiTableDataModel<LectureBlockRow>
	implements SortableFlexiTableDataModel<LectureBlockRow> {
	
	private static final BlockCols[] COLS = BlockCols.values();
	
	private final Locale locale;
	
	public LectureListRepositoryDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<LectureBlockRow> rows = new LectureListRepositorySortDelegate(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		LectureBlockRow block = getObject(row);
		return getValueAt(block, col);
	}

	@Override
	public Object getValueAt(LectureBlockRow row, int col) {
		return switch(COLS[col]) {
			case id -> row.getKey();
			case externalId -> row.getLectureBlock().getExternalId();
			case externalRef -> row.getLectureBlock().getExternalRef();
			case title ->  row.getLectureBlock().getTitle();
			case compulsory -> row.getLectureBlock().isCompulsory();
			case location -> row.getLectureBlock().getLocation();
			case lecturesNumber -> row.getLectureBlock().getPlannedLecturesNumber();
			case curriculumElement -> row.getCurriculumElement();
			case entry -> row.getEntry();
			case date -> row.getLectureBlock().getStartDate();
			case startTime -> row.getLectureBlock().getStartDate();
			case endTime -> row.getLectureBlock().getEndDate();
			case status -> row.getLectureBlock();
			case teachers -> row.getTeachers();
			case numParticipants -> row.getNumOfParticipants();
			case tools -> row.getToolsLink();
			case assessmentMode -> row.isAssessmentMode();
			case dateChooser -> row.getDateChooser();
			case teacherChooser -> row.getTeacherChooserLink();
			case locationElement -> row.getLocationElement();
			case chosenTeachers -> transformIdentitiesToString(row.getTeachersList());
			default -> null;
		};
	}
	
	private String transformIdentitiesToString(List<Identity> identities) {
		if (identities == null || identities.isEmpty()) {
			return null;
		}
		
		List<String> names = new ArrayList<>();
		
		for (Identity identity : identities) {
			names.add(identity.getUser().getFirstName() + " " + identity.getUser().getLastName());
		}
		
		Collections.sort(names);
		
		return String.join(", ", names);
	}
	
	public enum BlockCols implements FlexiSortableColumnDef {
		id("table.header.id"),
		title("lecture.title"),
		location("lecture.location"),
		date("lecture.date"),
		startTime("table.header.start.time"),
		endTime("table.header.end.time"),
		teachers("table.header.teachers"),
		tools("table.header.actions"),
		status("table.header.status"),
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
		entry("table.header.entry");
		
		private final String i18nKey;
		
		private BlockCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return this != tools;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
