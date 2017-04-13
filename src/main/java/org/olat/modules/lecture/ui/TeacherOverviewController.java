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

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
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
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.ScreenMode.Mode;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;
import org.olat.modules.lecture.RollCallSecurityCallback;
import org.olat.modules.lecture.model.RollCallSecurityCallbackImpl;
import org.olat.modules.lecture.ui.TeacherOverviewDataModel.TeachCols;
import org.olat.modules.lecture.ui.component.LectureBlockStatusCellRenderer;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeacherOverviewController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private TeacherOverviewDataModel tableModel;
	private final TooledStackedPanel toolbarPanel;
	private FormLink startButton, startWizardButton;
	
	private TeacherRollCallController rollCallCtrl;
	private TeacherRollCallWizardController rollCallWizardCtrl;
	
	private final boolean admin;
	private final RepositoryEntry entry;
	private final RepositoryEntryLectureConfiguration entryConfig;
	
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	
	public TeacherOverviewController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			RepositoryEntry entry, boolean admin) {
		super(ureq, wControl, "teacher_view");
		this.entry = entry;
		this.admin = admin;
		this.toolbarPanel = toolbarPanel;
		entryConfig = lectureService.getRepositoryEntryLectureConfiguration(entry);
		
		initForm(ureq);
		loadModel();
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		startButton = uifactory.addFormLink("start", formLayout, Link.BUTTON);
		startButton.setVisible(false);
		
		startWizardButton = uifactory.addFormLink("start.wizard", formLayout, Link.BUTTON);
		startWizardButton.setVisible(false);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeachCols.date, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeachCols.startTime, new TimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeachCols.endTime, new TimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TeachCols.lectureBlock));
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
		//TODO absence tableEl.setAndLoadPersistedPreferences(ureq, "lecture-teacher-overview");
	}
	
	private void loadModel() {
		List<LectureBlock> blocks = lectureService.getLectureBlocks(entry, getIdentity());
		tableModel.setObjects(blocks);
		tableEl.reset(false, false, true);

		//reset
		startButton.setVisible(false);
		startButton.setUserObject(null);
		startWizardButton.setVisible(false);
		startWizardButton.setUserObject(null);
		
		// only show the start button if 
		if(ConfigurationHelper.isRollCallEnabled(entryConfig, lectureModule)) {
			for(LectureBlock block:blocks) {
				if(canStartRollCall(block)) {
					startButton.setVisible(true);
					startButton.setUserObject(block);
					startButton.setPrimary(true);
					
					startWizardButton.setVisible(true);
					startWizardButton.setUserObject(block);
					
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
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(rollCallCtrl == source) {
			if(event == Event.DONE_EVENT) {
				toolbarPanel.popController(rollCallCtrl);
				loadModel();
			}
		} else if(rollCallWizardCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
			}
			getWindowControl().pop();
			getWindowControl().getWindowBackOffice()
				.getChiefController().getScreenMode().setMode(Mode.standard);
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(rollCallWizardCtrl);
		rollCallWizardCtrl = null;
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
				} else if("export".equals(cmd)) {
					LectureBlock row = tableModel.getObject(se.getIndex());
					doExportLectureBlock(ureq, row);
				}
			}
		} else if(source == startButton) {
			LectureBlock block = (LectureBlock)startButton.getUserObject();
			doStartRollCall(ureq, block);
		} else if(source == startWizardButton) {
			LectureBlock block = (LectureBlock)startWizardButton.getUserObject();
			doStartWizardRollCall(ureq, block);
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
	
	//same as above???
	private void doStartRollCall(UserRequest ureq, LectureBlock block) {
		LectureBlock reloadedBlock = lectureService.getLectureBlock(block);
		List<Identity> participants = lectureService.startLectureBlock(getIdentity(), reloadedBlock);
		rollCallCtrl = new TeacherRollCallController(ureq, getWindowControl(), reloadedBlock, participants, getRollCallSecurityCallback(reloadedBlock));
		listenTo(rollCallCtrl);
		toolbarPanel.pushController(reloadedBlock.getTitle(), rollCallCtrl);
	}
	
	@SuppressWarnings("deprecation")
	private void doStartWizardRollCall(UserRequest ureq, LectureBlock block) {
		if(rollCallWizardCtrl != null) return;
		
		LectureBlock reloadedBlock = lectureService.getLectureBlock(block);
		List<Identity> participants = lectureService.startLectureBlock(getIdentity(), reloadedBlock);
		rollCallWizardCtrl = new TeacherRollCallWizardController(ureq, getWindowControl(), reloadedBlock, participants);
		listenTo(rollCallWizardCtrl);
		
		ChiefController cc = getWindowControl().getWindowBackOffice().getChiefController();
		cc.getScreenMode().setMode(Mode.full);
		getWindowControl().pushToMainArea(rollCallWizardCtrl.getInitialComponent());
	}
	
	private RollCallSecurityCallback getRollCallSecurityCallback(LectureBlock block) {
		return new RollCallSecurityCallbackImpl(admin, true, block, lectureModule);
	}
}
