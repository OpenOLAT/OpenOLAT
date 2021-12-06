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
package org.olat.modules.grading.ui;

import java.util.List;
import java.util.Locale;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.lecture.ui.TeacherRollCallController;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 23 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GradingAssignmentsTableModel extends DefaultFlexiTableDataModel<GradingAssignmentRow>
implements SortableFlexiTableDataModel<GradingAssignmentRow> {
	
	private static final GAssignmentsCol[] COLS = GAssignmentsCol.values();
	
	private final Locale locale;
	private final Translator translator;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final List<UserPropertyHandler> assessedUserPropertyHandlers;
	
	public GradingAssignmentsTableModel(FlexiTableColumnModel columnModel, List<UserPropertyHandler> userPropertyHandlers,
			List<UserPropertyHandler> assessedUserPropertyHandlers, Translator translator, Locale locale) {
		super(columnModel);
		this.locale = locale;
		this.translator = translator;
		this.userPropertyHandlers = userPropertyHandlers;
		this.assessedUserPropertyHandlers = assessedUserPropertyHandlers;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<GradingAssignmentRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		GradingAssignmentRow assignmentRow = getObject(row);
		return getValueAt(assignmentRow, col);
	}

	@Override
	public Object getValueAt(GradingAssignmentRow row, int col) {
		if(col >= 0 && col < COLS.length) {
			switch(COLS[col]) {
				case deadline: return row.getAssignmentStatus();
				case entry: return row.getEntryDisplayname();
				case entryExternalRef: return row.getEntryExternalRef();
				case taxonomy: return row.getTaxonomyLevels();
				case courseElement: return row.getCourseElementTitle();
				case correctionMinutes: return CalendarUtils.convertSecondsToMinutes(row.getRecordedSeconds());
				case correctionMetadataMinutes: return CalendarUtils.convertSecondsToMinutes(row.getRecordedMetadataSeconds());
				case assessmentDate: return row.getAssessmentDate();
				case assignmentDate: return row.getAssignmentDate();
				case doneDate: return row.getDoneDate();
				case score: return row.getScore();
				case passed: return row.getPassed();
				case grade: return row.canGrade();
				case tools: return row.getToolsLink();
				default: return "ERROR";
			}
		}
		
		if (col >= GradersListController.USER_PROPS_OFFSET && col < userPropertyHandlers.size() + GradersListController.USER_PROPS_OFFSET) {
			if(row.hasGrader()) {
				// get user property for this column
				UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(col - TeacherRollCallController.USER_PROPS_OFFSET);
				String value = userPropertyHandler.getUserProperty(row.getGrader().getUser(), locale);
				return (value == null ? "n/a" : value);
			}
			return translator.translate("assignment.status.unassigned");
		}
		if (col >= GradingAssignmentsListController.ASSESSED_PROPS_OFFSET && col < assessedUserPropertyHandlers.size() + GradingAssignmentsListController.ASSESSED_PROPS_OFFSET) {
			if(row.getAssessedIdentity() != null && row.isAssessedIdentityVisible()) {
				// get user property for this column
				UserPropertyHandler userPropertyHandler = assessedUserPropertyHandlers.get(col - GradingAssignmentsListController.ASSESSED_PROPS_OFFSET);
				String value = userPropertyHandler.getUserProperty(row.getAssessedIdentity().getUser(), locale);
				return (value == null ? "n/a" : value);
			}
			return "-";
		}
		return "ERROR";
	}
	
	public enum GAssignmentsCol implements FlexiSortableColumnDef {
		deadline("table.header.deadline"),
		entry("table.header.entry"),
		entryExternalRef("table.header.entry.external.ref"),
		taxonomy("table.header.taxonomy"),
		courseElement("table.header.course.element"),
		assessmentDate("table.header.assessment.date"),
		correctionMinutes("table.header.correction.minutes"),
		correctionMetadataMinutes("table.header.correction.meta.minutes"),
		assignmentDate("table.header.assignment.date"),
		doneDate("table.header.done.date"),
		score("table.header.score"),
		passed("table.header.passed"),
		grade("table.header.grade"),
		tools("table.header.tools");
		
		private final String i18nKey;
		
		private GAssignmentsCol(String i18nKey) {
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
