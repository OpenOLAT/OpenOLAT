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
package org.olat.course.assessment.ui.tool;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.ui.mode.AssessmentModeListController;
import org.olat.course.assessment.ui.mode.ModeStatusCellRenderer;
import org.olat.course.assessment.ui.mode.TimeCellRenderer;
import org.olat.course.assessment.ui.tool.AssessmentModeOverviewListTableModel.ModeCols;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Small list of the assessment planed today and in the future for the
 * coaches.
 * 
 * Initial date: 15 déc. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentModeOverviewListController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private AssessmentModeOverviewListTableModel model;
	
	private RepositoryEntry courseEntry;
	
	@Autowired
	private AssessmentModeManager asssessmentModeManager;
	
	public AssessmentModeOverviewListController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry) {
		super(ureq, wControl, "assessment_modes", Util.createPackageTranslator(AssessmentModeListController.class, ureq.getLocale()));
		this.courseEntry = courseEntry;
		initForm(ureq);
		loadModel();
	}
	
	public int getNumOfAssessmentModes() {
		return model == null ? 0 : model.getRowCount();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ModeCols.status, new ModeStatusCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ModeCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ModeCols.begin));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ModeCols.end));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ModeCols.leadTime, new TimeCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ModeCols.followupTime, new TimeCellRenderer(getTranslator())));

		model = new AssessmentModeOverviewListTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 10, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
	}
	
	private void loadModel() {
		Date today = CalendarUtils.removeTime(new Date());
		List<AssessmentMode> modes = asssessmentModeManager.getPlannedAssessmentMode(courseEntry, today);
		if(modes.size() > 10) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, 1);
			Date tomorrow = CalendarUtils.removeTime(cal.getTime());
			
			List<AssessmentMode> nextModes = new ArrayList<>(25);
			for(AssessmentMode mode:modes) {
				Date begin = mode.getBegin();
				if(tomorrow.after(begin) || nextModes.size() < 10) {
					nextModes.add(mode);
				}
			}
			modes = nextModes;	
		}
		model.setObjects(modes);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void doDispose() {
		//
	}
	

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
