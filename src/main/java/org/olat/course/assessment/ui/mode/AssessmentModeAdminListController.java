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
package org.olat.course.assessment.ui.mode;

import java.util.Date;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.CorruptedCourseException;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentModeCoordinationService;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.model.SearchAssessmentModeParams;
import org.olat.course.assessment.ui.mode.AssessmentModeListModel.Cols;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 06.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentModeAdminListController extends FormBasicController {

	private TextElement nameEl;
	private DateChooser dateToEl;
	private DateChooser dateFromEl;
	private TextElement idAndRefsEl;
	private FormLink searchButton;
	private FlexiTableElement tableEl;
	private AssessmentModeListModel model;
	private SearchAssessmentModeParams params = new SearchAssessmentModeParams();
	
	@Autowired
	private AssessmentModeManager assessmentModeMgr;
	@Autowired
	private AssessmentModeCoordinationService assessmentModeCoordinationService;
	
	public AssessmentModeAdminListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "admin_search");
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//search form
		FormLayoutContainer searchLeftForm = FormLayoutContainer.createDefaultFormLayout("left_1", getTranslator());
		searchLeftForm.setRootForm(mainForm);
		formLayout.add("left_1", searchLeftForm);
		idAndRefsEl = uifactory.addTextElement("mode.id", "assessment.mode.id", 128, null, searchLeftForm);
		nameEl = uifactory.addTextElement("mode.name", "assessment.mode.name", 128, null, searchLeftForm);
		
		FormLayoutContainer searchRightForm = FormLayoutContainer.createDefaultFormLayout("right_1", getTranslator());
		searchRightForm.setRootForm(mainForm);
		formLayout.add("right_1", searchRightForm);
		dateFromEl = uifactory.addDateChooser("assessment.mode.date.from", null, searchRightForm);
		dateFromEl.addActionListener(FormEvent.ONCHANGE);
		dateToEl = uifactory.addDateChooser("assessment.mode.date.to", null, searchRightForm);
		
		//search button
		FormLayoutContainer searchButtons = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		searchButtons.setRootForm(mainForm);
		formLayout.add("button_layout", searchButtons);
		searchButton = uifactory.addFormLink("search", searchButtons, Link.BUTTON);
		searchButton.setPrimary(true);

		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.status, new ModeStatusCellRenderer(getTranslator())));
		FlexiCellRenderer renderer = new StaticFlexiCellRenderer("select", new TextFlexiCellRenderer());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.course, "select", renderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.externalId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.externalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.begin));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.end));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.leadTime, new TimeCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.followupTime, new TimeCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.target, new TargetAudienceCellRenderer(getTranslator())));
		
		model = new AssessmentModeListModel(columnsModel, getTranslator(), assessmentModeCoordinationService);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 20, false, getTranslator(), formLayout);
		tableEl.setMultiSelect(false);
		tableEl.setAndLoadPersistedPreferences(ureq, "assessment-mode-admin");
	}
	
	private void loadModel() {
		List<AssessmentMode> modes = assessmentModeMgr.findAssessmentMode(params);
		model.setObjects(modes);
		tableEl.reloadData();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(searchButton == source) {
			doSearch();
		} else if(dateFromEl == source) {
			if(dateToEl.getDate() == null) {
				dateToEl.setDate(dateFromEl.getDate());
			}
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				AssessmentMode row = model.getObject(se.getIndex());
				if("select".equals(cmd)) {
					launch(ureq, row.getRepositoryEntry());
				}
			}
			
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doSearch() {
		Date from = dateFromEl.getDate();
		if(from != null) {
			params.setDateFrom(CalendarUtils.startOfDay(from));
		}
		Date to = dateToEl.getDate();
		if(to != null) {
			params.setDateTo(CalendarUtils.endOfDay(to));
			
		}
		params.setIdAndRefs(idAndRefsEl.getValue());
		params.setName(nameEl.getValue());
		loadModel();
	}
	
	private void launch(UserRequest ureq, RepositoryEntry entry) {
		try {
			String businessPath = "[RepositoryEntry:" + entry.getKey() + "]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		} catch (CorruptedCourseException e) {
			logError("Course corrupted: " + entry.getKey() + " (" + entry.getOlatResource().getResourceableId() + ")", e);
			showError("cif.error.corrupted");
		}
	}
}