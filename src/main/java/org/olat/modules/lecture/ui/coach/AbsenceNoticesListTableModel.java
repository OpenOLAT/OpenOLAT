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
package org.olat.modules.lecture.ui.coach;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.course.assessment.ui.tool.AssessmentToolConstants;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.LectureBlock;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 25 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AbsenceNoticesListTableModel extends DefaultFlexiTableDataModel<AbsenceNoticeRow>
implements SortableFlexiTableDataModel<AbsenceNoticeRow> {
	
	private static final NoticeCols[] COLS = NoticeCols.values();
	
	private final Locale locale;
	private final UserManager userManager;
	private final List<UserPropertyHandler> userPropertyHandlers;

	public AbsenceNoticesListTableModel(FlexiTableColumnModel columnModel, UserManager userManager,
			List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(columnModel);
		this.locale = locale;
		this.userManager = userManager;
		this.userPropertyHandlers = userPropertyHandlers;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<AbsenceNoticeRow> rows = new AbsenceNoticesListTableModelSortDelegate(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		AbsenceNoticeRow notice = getObject(row);
		return getValueAt(notice, col);
	}

	@Override
	public Object getValueAt(AbsenceNoticeRow row, int col) {
		if(col < AbsenceNoticesListController.USER_PROPS_OFFSET) {
			switch(COLS[col]) {
				case id: return row.getKey();
				case date: return row;
				case start: return row.getStartDate();
				case end: return row.getEndDate();
				case entry: return row.getEntriesLink();
				case lectureBlocks: return getLectureBlocks(row);
				case teachers: return getTeachers(row);
				case numOfLectures: return getNumOfLectures(row);
				case reason: return getAbsenceCategory(row);
				case type: return row.getTypeLink();
				case details: return row.getDetailsLink();
				case tools: return row.getToolsLink();
				default: return "ERROR";
			}
		}
		
		User user = row.getAbsentIdentity().getUser();
		int propPos = col - AssessmentToolConstants.USER_PROPS_OFFSET;
		return userPropertyHandlers.get(propPos).getUserProperty(user, locale);
	}
	
	private String getTeachers(AbsenceNoticeRow row) {
		StringBuilder sb = new StringBuilder(1024);
		for(Identity teacher:row.getTeachers()) {
			sb.append("<i class='o_icon o_icon_user'> </i> ")
			  .append(userManager.getUserDisplayName(teacher))
			  .append(" ");	
		}
		return sb.toString();
	}
	
	private String getAbsenceCategory(AbsenceNoticeRow row) {
		AbsenceNotice notice = row.getAbsenceNotice();
		if(notice.getAbsenceCategory() != null) {
			return notice.getAbsenceCategory().getTitle();
		}
		return null;
	}
	
	private int getNumOfLectures(AbsenceNoticeRow row) {
		int totalLectures = 0;
		for(LectureBlock lectureBlock:row.getLectureBlocks()) {
			totalLectures += lectureBlock.getCalculatedLecturesNumber();
		}
		return totalLectures;
	}

	
	private String getLectureBlocks(AbsenceNoticeRow row) {
		StringBuilder sb = new StringBuilder(1024);
		for(LectureBlock lectureBlock:row.getLectureBlocks()) {
			if(sb.length() > 0) sb.append(", ");
			sb.append(lectureBlock.getTitle());	
		}
		return sb.toString();
	}
	
	public enum NoticeCols implements FlexiSortableColumnDef {
		id("table.header.id"),
		date("table.header.date"),
		start("table.header.start.time"),
		end("table.header.end.time"),
		entry("table.header.entry"),
		lectureBlocks("table.header.lecture.block"),
		teachers("table.header.teachers"),
		numOfLectures("table.header.num.lecture.block"),
		reason("table.header.reason"),
		details("table.header.infos"),
		type("table.header.notice.type"),
		tools("table.header.tools");
		
		private final String i18nKey;
		
		private NoticeCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return this != details && this != tools;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}

}
