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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TimeFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LectureReportRow;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.modules.lecture.ui.coach.LecturesReportTableModel.ReportCols;
import org.olat.modules.lecture.ui.component.IdentityListCellRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesReportController extends FormBasicController {
	
	private static final String[] statusKeys = new String[] {
			LectureRollCallStatus.open.name(), LectureRollCallStatus.closed.name(),
			LectureRollCallStatus.autoclosed.name(), LectureRollCallStatus.reopen.name()
		};
	
	private DateChooser endEl;
	private DateChooser startEl;
	private MultipleSelectionElement statusEl;
	
	private FlexiTableElement tableEl;
	private LecturesReportTableModel tableModel;
	
	@Autowired
	private LectureService lectureService;
	
	public LecturesReportController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "lectures_report", Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer searchCont = FormLayoutContainer.createDefaultFormLayout("searchCont", getTranslator());
		formLayout.add(searchCont);
		
		startEl = uifactory.addDateChooser("search.form.start", "search.form.start", null, searchCont);
		endEl = uifactory.addDateChooser("search.form.end", "search.form.end", null, searchCont);
		
		String[] statusValues = new String[] {
				translate("search.form.status.open"), translate("search.form.status.closed"),
				translate("search.form.status.autoclosed"), translate("search.form.status.reopen")
		};
		statusEl = uifactory.addCheckboxesHorizontal("search.form.status", "search.form.status", searchCont, statusKeys, statusValues);
		
		FormLayoutContainer searchButtonsCont = FormLayoutContainer.createButtonLayout("searchButtons", getTranslator());
		searchCont.add(searchButtonsCont);
		uifactory.addFormSubmitButton("search", "search", searchButtonsCont);

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		DefaultFlexiColumnModel keyCols = new DefaultFlexiColumnModel(false, ReportCols.key);
		keyCols.setExportable(false);
		columnsModel.addFlexiColumnModel(keyCols);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReportCols.date, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReportCols.start, new TimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReportCols.end, new TimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReportCols.externalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReportCols.lectureBlockTitle));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReportCols.owners, new IdentityListCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReportCols.teachers, new IdentityListCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReportCols.status));

		tableModel = new LecturesReportTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 50, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "lecturesblocks-admin-report");
	}

	@Override
	protected void formOK(UserRequest ureq) {
		loadModel();
	}
	
	private void loadModel() {
		Date from = startEl.getDate();
		Date to = endEl.getDate();
		if(to != null) {
			to = CalendarUtils.endOfDay(to);
		}
		Collection<String> selectedStatusKeys = statusEl.getSelectedKeys();
		List<LectureRollCallStatus> status = selectedStatusKeys.stream()
				.map(LectureRollCallStatus::valueOf).collect(Collectors.toList());
		List<LectureReportRow> rows = lectureService.getLectureBlocksReport(from, to, status);
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
}
