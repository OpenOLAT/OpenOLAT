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

import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TimeFlexiCellRenderer;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.RollCallSecurityCallback;
import org.olat.modules.lecture.model.LectureBlockRow;
import org.olat.modules.lecture.model.RollCallSecurityCallbackImpl;
import org.olat.modules.lecture.ui.TeacherOverviewDataModel.TeachCols;
import org.olat.modules.lecture.ui.component.LectureBlockStatusCellRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeacherLecturesTableController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private TeacherOverviewDataModel tableModel;
	private final TooledStackedPanel toolbarPanel;
	
	private TeacherRollCallController rollCallCtrl;
	
	private final boolean admin;
	private final String emptyI18nKey;
	
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	
	public TeacherLecturesTableController(UserRequest ureq, WindowControl wControl,
			TooledStackedPanel toolbarPanel, boolean admin, String emptyI18nKey) {
		super(ureq, wControl, "teacher_view_table");
		this.admin = admin;
		this.toolbarPanel = toolbarPanel;
		this.emptyI18nKey = emptyI18nKey;
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeachCols.date, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeachCols.startTime, new TimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeachCols.endTime, new TimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeachCols.lectureBlock));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeachCols.location));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeachCols.teachers));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeachCols.status, new LectureBlockStatusCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeachCols.details.i18nHeaderKey(), TeachCols.details.ordinal(), "details",
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("table.header.details"), "details"), null)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeachCols.export.i18nHeaderKey(), TeachCols.export.ordinal(), "export",
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("table.header.export"), "export"), null)));
		
		tableModel = new TeacherOverviewDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(TeachCols.date.name(), false));
		tableEl.setSortSettings(sortOptions);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setEmtpyTableMessageKey(emptyI18nKey);
		//TODO absence tableEl.setAndLoadPersistedPreferences(ureq, "lecture-teacher-overview");
	}
	
	protected void loadModel(List<LectureBlockRow> blocks) {
		tableModel.setObjects(blocks);
		tableEl.reset(true, true, true);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == rollCallCtrl) {
			if(event == Event.DONE_EVENT || event == Event.CANCELLED_EVENT || event == Event.CHANGED_EVENT) {
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if("details".equals(cmd)) {
					LectureBlockRow row = tableModel.getObject(se.getIndex());
					doSelectLectureBlock(ureq, row.getLectureBlock());
				} else if("export".equals(cmd)) {
					LectureBlockRow row = tableModel.getObject(se.getIndex());
					doExportLectureBlock(ureq, row.getLectureBlock());
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doExportLectureBlock(UserRequest ureq, LectureBlock row) {
		LectureBlockExport export = new LectureBlockExport(row, true, getTranslator());
		ureq.getDispatchResult().setResultingMediaResource(export);
	}
	
	private void doSelectLectureBlock(UserRequest ureq, LectureBlock block) {
		LectureBlock reloadedBlock = lectureService.getLectureBlock(block);
		List<Identity> participants = lectureService.startLectureBlock(getIdentity(), reloadedBlock);
		rollCallCtrl = new TeacherRollCallController(ureq, getWindowControl(), reloadedBlock, participants, getRollCallSecurityCallback(reloadedBlock));
		listenTo(rollCallCtrl);
		toolbarPanel.pushController(reloadedBlock.getTitle(), rollCallCtrl);
	}

	
	private RollCallSecurityCallback getRollCallSecurityCallback(LectureBlock block) {
		return new RollCallSecurityCallbackImpl(admin, true, block, lectureModule);
	}
}
