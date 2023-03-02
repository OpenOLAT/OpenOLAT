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
package org.olat.modules.coach.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.CoachingAssignmentStatistics;
import org.olat.course.assessment.IndentedNodeRenderer;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.coach.ui.CourseCoachAssignmentsTableModel.CAssignmentsCol;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 févr. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseCoachAssignmentsController extends FormBasicController {
	
	private static final String CMD_OPEN_ASSIGN = "assign";
	private static final String CMD_COURSE_ELEMENT = "course-element";
	private static final String CMD_OPEN_ASSIGNMENTS = "open-assignments";

	private FlexiTableElement tableEl;
	private CourseCoachAssignmentsTableModel tableModel;
	
	@Autowired
	private AssessmentToolManager assessmentToolManager;
	
	public CourseCoachAssignmentsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "course_coach_assignments");
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CAssignmentsCol.entryId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CAssignmentsCol.entry));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CAssignmentsCol.entryExternalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CAssignmentsCol.entryExternalId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CAssignmentsCol.courseElement, CMD_COURSE_ELEMENT, new IndentedNodeRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CAssignmentsCol.openAssignments, CMD_OPEN_ASSIGNMENTS));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("assign", translate("assign"), CMD_OPEN_ASSIGN));
		
		tableModel = new CourseCoachAssignmentsTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, "orders-coach-assignments-v1");
	}
	
	private void loadModel() {
		List<CoachingAssignmentStatistics> statistics = assessmentToolManager.getCoachingAssignmentStatistics(getIdentity(), AssessmentEntryStatus.inReview);
		List<CourseCoachAssignmentRow> rows = new ArrayList<>(statistics.size());
		for(CoachingAssignmentStatistics stats:statistics) {
			if(stats.getNumOfAssignments() > 0l) {
				rows.add(new CourseCoachAssignmentRow(stats.getRepositoryEntry(), stats.getCourseElement(), stats.getNumOfAssignments()));
			}
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				if(CMD_COURSE_ELEMENT.equals(cmd)) {
					doOpenCourseElement(ureq, tableModel.getObject(se.getIndex()));
				} else if(CMD_OPEN_ASSIGNMENTS.equals(cmd)) {
					doOpenCourseElement(ureq, tableModel.getObject(se.getIndex()));
				} else if(CMD_OPEN_ASSIGN.equals(cmd)) {
					doOpenAssignments(ureq, tableModel.getObject(se.getIndex()));
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpenCourseElement(UserRequest ureq, CourseCoachAssignmentRow row) {
		String businessPath = "[RepositoryEntry:" + row.getCourseEntry().getKey() +"][CourseNode:" + row.getSubIdent() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private void doOpenAssignments(UserRequest ureq, CourseCoachAssignmentRow row) {
		String businessPath = "[RepositoryEntry:" + row.getCourseEntry().getKey() +"][CourseNode:" + row.getSubIdent() + "][Coach:0][Assignments:0]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
}
