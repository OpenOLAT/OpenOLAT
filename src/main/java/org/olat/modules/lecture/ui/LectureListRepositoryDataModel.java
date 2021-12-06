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
	
	private final Locale locale;
	
	public LectureListRepositoryDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		List<LectureBlockRow> rows = new LectureListRepositorySortDelegate(orderBy, this, locale).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		LectureBlockRow block = getObject(row);
		return getValueAt(block, col);
	}

	@Override
	public Object getValueAt(LectureBlockRow row, int col) {
		switch(BlockCols.values()[col]) {
			case id: return row.getKey();
			case title: return  row.getLectureBlock().getTitle();
			case compulsory: return row.getLectureBlock().isCompulsory();
			case location: return row.getLectureBlock().getLocation();
			case date: return row.getLectureBlock().getStartDate();
			case startTime: return row.getLectureBlock().getStartDate();
			case endTime: return row.getLectureBlock().getEndDate();
			case status: return row.getLectureBlock();
			case teachers: return row.getTeachers();
			case tools: return row.getToolsLink();
			case assessmentMode: return row.isAssessmentMode();
			case dateChooser: return row.getDateChooser();
			case teacherChooser: return row.getTeacherChooserLink();
			case locationElement: return row.getLocationElement();
			case chosenTeachers: return transformIdentitiesToString(row.getTeachersList());
			default: return null;
		}
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
		locationElement("lecture.location");
		
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
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
