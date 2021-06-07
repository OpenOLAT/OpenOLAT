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
import java.util.Set;

import org.olat.basesecurity.Group;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.CSSIconFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TimeFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.id.Identity;
import org.olat.core.logging.activity.CoreLoggingResourceable;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockAuditLog;
import org.olat.modules.lecture.LectureBlockManagedFlag;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LectureBlockRow;
import org.olat.modules.lecture.model.LectureBlockWithTeachers;
import org.olat.modules.lecture.ui.LectureListRepositoryDataModel.BlockCols;
import org.olat.modules.lecture.ui.blockimport.BlocksImport_1_InputStep;
import org.olat.modules.lecture.ui.blockimport.ImportedLectureBlock;
import org.olat.modules.lecture.ui.blockimport.ImportedLectureBlocks;
import org.olat.modules.lecture.ui.component.LectureBlockStatusCellRenderer;
import org.olat.modules.lecture.ui.component.YesNoCellRenderer;
import org.olat.modules.lecture.ui.export.LectureBlockAuditLogExport;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureListRepositoryController extends FormBasicController {

	private FormLink addLectureButton;
	private FormLink deleteLecturesButton;
	private FormLink importLecturesButton;
	private FlexiTableElement tableEl;
	private LectureListRepositoryDataModel tableModel;
	
	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private StepsMainRunController importBlockWizard;
	private EditLectureBlockController editLectureCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private ConfirmDeleteLectureBlockController deleteLectureBlocksCtrl;

	private int counter = 0;
	private final RepositoryEntry entry;
	private final boolean lectureManagementManaged;
	private final LecturesSecurityCallback secCallback;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	
	public LectureListRepositoryController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, LecturesSecurityCallback secCallback) {
		super(ureq, wControl, "admin_repository_lectures");
		this.entry = entry;
		this.secCallback = secCallback;
		lectureManagementManaged = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.lecturemanagement);
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(!lectureManagementManaged && secCallback.canNewLectureBlock()) {
			addLectureButton = uifactory.addFormLink("add.lecture", formLayout, Link.BUTTON);
			addLectureButton.setIconLeftCSS("o_icon o_icon_add");
			addLectureButton.setElementCssClass("o_sel_repo_add_lecture");
			
			importLecturesButton = uifactory.addFormLink("import.lectures", formLayout, Link.BUTTON);
			importLecturesButton.setIconLeftCSS("o_icon o_icon_import");
			importLecturesButton.setElementCssClass("o_sel_repo_import_lectures");
			
			deleteLecturesButton = uifactory.addFormLink("delete", formLayout, Link.BUTTON);
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BlockCols.id));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.title));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.assessmentMode,
				new BooleanCellRenderer(new CSSIconFlexiCellRenderer("o_icon_assessment_mode"), null)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.compulsory, new YesNoCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.location));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.date, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.startTime, new TimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.endTime, new TimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.teachers));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.status, new LectureBlockStatusCellRenderer(getTranslator())));

		DefaultFlexiColumnModel editColumn = new DefaultFlexiColumnModel("table.header.edit", -1, "edit",
				new StaticFlexiCellRenderer("", "edit", "o_icon o_icon-lg o_icon_edit", translate("edit"), null));
		editColumn.setExportable(false);
		editColumn.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(editColumn);
			
		StickyActionColumnModel toolsColumn = new StickyActionColumnModel(BlockCols.tools);
		toolsColumn.setExportable(false);
		toolsColumn.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(toolsColumn);
		
		tableModel = new LectureListRepositoryDataModel(columnsModel, getLocale()); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setEmptyTableMessageKey("empty.table.lectures.blocks.admin");
		
		FlexiTableSortOptions options = new FlexiTableSortOptions();
		options.setDefaultOrderBy(new SortKey(BlockCols.date.name(), false));
		tableEl.setSortSettings(options);
		tableEl.setAndLoadPersistedPreferences(ureq, "repo-lecture-block-list-v2");
	}
	
	private void loadModel() {
		List<LectureBlockWithTeachers> blocks = lectureService.getLectureBlocksWithTeachers(entry);
		List<LectureBlockRow> rows = new ArrayList<>(blocks.size());
		for(LectureBlockWithTeachers block:blocks) {
			LectureBlock b = block.getLectureBlock();
			StringBuilder teachers = new StringBuilder();
			String separator = translate("user.fullname.separator");
			for(Identity teacher:block.getTeachers()) {
				if(teachers.length() > 0) teachers.append(" ").append(separator).append(" ");
				teachers.append(userManager.getUserDisplayName(teacher));
			}

			LectureBlockRow row = new LectureBlockRow(b, entry.getDisplayname(), entry.getExternalRef(),
					teachers.toString(), false, block.isAssessmentMode());
			rows.add(row);
			
			String linkName = "tools-" + counter++;
			FormLink toolsLink = uifactory.addFormLink(linkName, "", null, flc, Link.LINK | Link.NONTRANSLATED);
			toolsLink.setIconRightCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
			toolsLink.setUserObject(row);
			flc.add(linkName, toolsLink);
			row.setToolsLink(toolsLink);
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);

		if(deleteLecturesButton != null) {
			deleteLecturesButton.setVisible(!rows.isEmpty());
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addLectureButton == source) {
			doAddLectureBlock(ureq);
		} else if(deleteLecturesButton == source) {
			doConfirmBulkDelete(ureq);
		} else if(importLecturesButton == source) {
			doImportLecturesBlock(ureq);
		} else if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				LectureBlockRow row = tableModel.getObject(se.getIndex());
				if("edit".equals(cmd)) {
					doEditLectureBlock(ureq, row);
				}
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if(cmd != null && cmd.startsWith("tools-")) {
				LectureBlockRow row = (LectureBlockRow)link.getUserObject();
				doOpenTools(ureq, row, link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editLectureCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		} else if(toolsCalloutCtrl == source) {
			cleanUp();
		} else if(toolsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				if(toolsCalloutCtrl != null) {
					toolsCalloutCtrl.deactivate();
					cleanUp();
				}
			}
		} else if(importBlockWizard == source) {
			getWindowControl().pop();
			cleanUp();
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel();
			}
		} else if(deleteLectureBlocksCtrl == source) {
			if (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(deleteLectureBlocksCtrl);
		removeAsListenerAndDispose(editLectureCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		deleteLectureBlocksCtrl = null;
		toolsCalloutCtrl = null;
		editLectureCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doEditLectureBlock(UserRequest ureq, LectureBlockRow row) {
		if(guardModalController(editLectureCtrl)) return;
		
		LectureBlock block = lectureService.getLectureBlock(row);
		boolean readOnly = lectureManagementManaged || !secCallback.canNewLectureBlock();
		editLectureCtrl = new EditLectureBlockController(ureq, getWindowControl(), entry, block, readOnly);
		listenTo(editLectureCtrl);

		cmc = new CloseableModalController(getWindowControl(), "close", editLectureCtrl.getInitialComponent(), true, translate("add.lecture"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doAddLectureBlock(UserRequest ureq) {
		if(guardModalController(editLectureCtrl) || !secCallback.canNewLectureBlock()) return;
		
		editLectureCtrl = new EditLectureBlockController(ureq, getWindowControl(), entry);
		listenTo(editLectureCtrl);

		cmc = new CloseableModalController(getWindowControl(), "close", editLectureCtrl.getInitialComponent(), true, translate("add.lecture"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doImportLecturesBlock(UserRequest ureq) {
		removeAsListenerAndDispose(importBlockWizard);

		final ImportedLectureBlocks lectureBlocks = new ImportedLectureBlocks();
		Step start = new BlocksImport_1_InputStep(ureq, entry, lectureBlocks);
		StepRunnerCallback finish = (uureq, wControl, runContext) -> {
			doFinalizeImportedLectureBlocks(lectureBlocks);
			return StepsMainRunController.DONE_MODIFIED;
		};
		
		importBlockWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("tools.import.table"), "o_sel_lecture_table_import_wizard");
		listenTo(importBlockWizard);
		getWindowControl().pushAsModalDialog(importBlockWizard.getInitialComponent());
	}
	
	private void doFinalizeImportedLectureBlocks(ImportedLectureBlocks lectureBlocks) {
		List<ImportedLectureBlock> importedBlocks = lectureBlocks.getLectureBlocks();
		for(ImportedLectureBlock importedBlock:importedBlocks) {
			LectureBlock lectureBlock = importedBlock.getLectureBlock();
			boolean exists = lectureBlock.getKey() != null;

			List<Group> groups;
			if(importedBlock.getGroupMapping() != null && importedBlock.getGroupMapping().getGroup() != null) {
				groups = Collections.singletonList(importedBlock.getGroupMapping().getGroup());
			} else {
				groups = new ArrayList<>();
			}
			lectureBlock = lectureService.save(lectureBlock, groups);
			
			if(exists) {
				lectureService.adaptRollCalls(lectureBlock);
			}
			for(Identity teacher:importedBlock.getTeachers()) {
				lectureService.addTeacher(lectureBlock, teacher);
			}
		}
	}
	
	private void doCopy(LectureBlockRow row) {
		String newTitle = translate("lecture.block.copy", new String[]{ row.getLectureBlock().getTitle() });
		lectureService.copyLectureBlock(newTitle, row.getLectureBlock());
		loadModel();
		showInfo("lecture.block.copied");
	}
	
	private void doConfirmBulkDelete(UserRequest ureq) {
		Set<Integer> selections = tableEl.getMultiSelectedIndex();
		List<LectureBlock> blocks = new ArrayList<>();
		for(Integer selection:selections) {
			LectureBlockRow blockRow = tableModel.getObject(selection);
			if(!LectureBlockManagedFlag.isManaged(blockRow.getLectureBlock(), LectureBlockManagedFlag.delete)) {
				blocks.add(blockRow.getLectureBlock());
			}
		}
		
		if(blocks.isEmpty()) {
			showWarning("error.atleastone.lecture");
		} else {
			deleteLectureBlocksCtrl = new ConfirmDeleteLectureBlockController(ureq, getWindowControl(), blocks);
			listenTo(deleteLectureBlocksCtrl);
			
			String title = translate("delete.lectures.title");
			cmc = new CloseableModalController(getWindowControl(), "close", deleteLectureBlocksCtrl.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doConfirmDelete(UserRequest ureq, LectureBlockRow row) {
		List<LectureBlock> blocks = Collections.singletonList(row.getLectureBlock());
		deleteLectureBlocksCtrl = new ConfirmDeleteLectureBlockController(ureq, getWindowControl(), blocks);
		listenTo(deleteLectureBlocksCtrl);
		
		String title = translate("delete.lectures.title");
		cmc = new CloseableModalController(getWindowControl(), "close", deleteLectureBlocksCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doExportLog(UserRequest ureq, LectureBlockRow row) {
		LectureBlock lectureBlock = lectureService.getLectureBlock(row);
		List<LectureBlockAuditLog> auditLog = lectureService.getAuditLog(row);
		boolean authorizedAbsenceEnabled = lectureModule.isAuthorizedAbsenceEnabled();
		LectureBlockAuditLogExport export = new LectureBlockAuditLogExport(entry, lectureBlock, auditLog, authorizedAbsenceEnabled, getTranslator());
		ureq.getDispatchResult().setResultingMediaResource(export);
	}
	
	private void doReopen(LectureBlockRow row) {
		LectureBlock lectureBlock = lectureService.getLectureBlock(row);
		String before = lectureService.toAuditXml(lectureBlock);
		lectureBlock.setRollCallStatus(LectureRollCallStatus.reopen);
		if(lectureBlock.getStatus() == LectureBlockStatus.cancelled) {
			lectureBlock.setStatus(LectureBlockStatus.active);
		}
		
		lectureBlock = lectureService.save(lectureBlock, null);
		
		String after = lectureService.toAuditXml(lectureBlock);
		lectureService.auditLog(LectureBlockAuditLog.Action.reopenLectureBlock, before, after, null, lectureBlock, null, lectureBlock.getEntry(), null, getIdentity());
		ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LECTURE_BLOCK_ROLL_CALL_REOPENED, getClass(),
				CoreLoggingResourceable.wrap(lectureBlock, OlatResourceableType.lectureBlock, lectureBlock.getTitle()));
		
		loadModel();
	}
	
	private void doOpenTools(UserRequest ureq, LectureBlockRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);
	
		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}

	private class ToolsController extends BasicController {
		
		private Link deleteLink;
		private Link copyLink;
		private Link logLink;
		private Link reopenLink;
		
		private final LectureBlockRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, LectureBlockRow row) {
			super(ureq, wControl);
			this.row = row;
			
			VelocityContainer mainVC = createVelocityContainer("lectures_tools");
			
			LectureBlock lectureBlock = row.getLectureBlock();

			if(secCallback.canReopenLectureBlock() && (lectureBlock.getStatus() == LectureBlockStatus.cancelled
					|| lectureBlock.getRollCallStatus() == LectureRollCallStatus.closed
					|| lectureBlock.getRollCallStatus() == LectureRollCallStatus.autoclosed)) {
				reopenLink = LinkFactory.createLink("reopen.lecture.blocks", "reopen", getTranslator(), mainVC, this, Link.LINK);
				reopenLink.setIconLeftCSS("o_icon o_icon-fw o_icon_reopen");
			}
			
			if(secCallback.canNewLectureBlock()) {
				copyLink = LinkFactory.createLink("copy", "copy", getTranslator(), mainVC, this, Link.LINK);
				copyLink.setIconLeftCSS("o_icon o_icon-fw o_icon_copy");
				if(!LectureBlockManagedFlag.isManaged(row.getLectureBlock(), LectureBlockManagedFlag.delete)) {
					deleteLink = LinkFactory.createLink("delete", "delete", getTranslator(), mainVC, this, Link.LINK);
					deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
				}
			}
			logLink = LinkFactory.createLink("log", "log", getTranslator(), mainVC, this, Link.LINK);
			logLink.setIconLeftCSS("o_icon o_icon-fw o_icon_log"); 
			putInitialPanel(mainVC);
		}

		@Override
		protected void doDispose() {
			//
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(copyLink == source) {
				doCopy(row);
			} else if(deleteLink == source) {
				doConfirmDelete(ureq, row);
			} else if(logLink == source) {
				doExportLog(ureq, row);
			} else if(reopenLink == source) {
				doReopen(row);
			}
		}
	}
}