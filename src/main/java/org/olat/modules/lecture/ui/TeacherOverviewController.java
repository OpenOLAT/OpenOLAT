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

import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
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
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;
import org.olat.modules.lecture.ui.TeacherOverviewDataModel.TeachCols;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeacherOverviewController extends FormBasicController {
	
	private FormLink startButton;
	private FlexiTableElement tableEl;
	private TeacherOverviewDataModel tableModel;
	private final TooledStackedPanel toolbarPanel;
	
	private TeacherRollCallController rollCallCtrl;
	
	private final RepositoryEntry entry;
	private final RepositoryEntryLectureConfiguration entryConfig;
	
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	
	public TeacherOverviewController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel, RepositoryEntry entry) {
		super(ureq, wControl, "teacher_view");
		this.entry = entry;
		this.toolbarPanel = toolbarPanel;
		entryConfig = lectureService.getRepositoryEntryLectureConfiguration(entry);
		
		initForm(ureq);
		loadModel();
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		startButton = uifactory.addFormLink("start", formLayout, Link.BUTTON);
		startButton.setVisible(false);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeachCols.date, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeachCols.startTime, new TimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeachCols.endTime, new TimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeachCols.lectureBlock));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeachCols.status));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeachCols.details.i18nHeaderKey(), TeachCols.details.ordinal(), "details",
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("table.header.details"), "details"), null)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeachCols.export.i18nHeaderKey(), TeachCols.export.ordinal(), "export",
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("table.header.export"), "export"), null)));
		
		tableModel = new TeacherOverviewDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
	}
	
	private void loadModel() {
		List<LectureBlock> blocks = lectureService.getLectureBlocks(entry, getIdentity());
		tableModel.setObjects(blocks);
		tableEl.reset(true, true, true);

		//reset
		startButton.setVisible(false);
		startButton.setUserObject(null);
		
		// only show the start button if 
		if(ConfigurationHelper.isRollCallEnabled(entryConfig, lectureModule)) {
			for(LectureBlock block:blocks) {
				if(canStartRollCall(block)) {
					startButton.setVisible(true);
					startButton.setUserObject(block);
					startButton.setPrimary(true);
					flc.getFormItemComponent().contextPut("blockTitle", StringHelper.escapeHtml(block.getTitle()));
					break;
				}
			}
		}
	}
	
	private boolean canStartRollCall(LectureBlock block) {
		Date start = block.getStartDate();
		Date end = block.getEndDate();
		Date now = new Date();
		if(start.compareTo(now) <= 0 && end.compareTo(now) >= 0) {
			return true;
		}
		return false;
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if("details".equals(cmd)) {
					LectureBlock row = tableModel.getObject(se.getIndex());
					doSelectLectureBlock(ureq, row);
				}
			}
		} else if(source == startButton) {
			LectureBlock block = (LectureBlock)startButton.getUserObject();
			doStartRollCall(ureq, block);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doSelectLectureBlock(UserRequest ureq, LectureBlock block) {
		boolean editable = false;
		if(block.getStatus().equals(LectureBlockStatus.active)
				|| block.getStatus().equals(LectureBlockStatus.partiallydone)) {
			editable = true;
		}
		List<Identity> participants = lectureService.startLectureBlock(getIdentity(), block);
		rollCallCtrl = new TeacherRollCallController(ureq, getWindowControl(), block, participants, editable);
		listenTo(rollCallCtrl);
		toolbarPanel.pushController(block.getTitle(), rollCallCtrl);
	}
	
	//same as above???
	private void doStartRollCall(UserRequest ureq, LectureBlock block) {
		List<Identity> participants = lectureService.startLectureBlock(getIdentity(), block);
		rollCallCtrl = new TeacherRollCallController(ureq, getWindowControl(), block, participants, true);
		listenTo(rollCallCtrl);
		toolbarPanel.pushController(block.getTitle(), rollCallCtrl);
	}
}
