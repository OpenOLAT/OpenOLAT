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
import java.util.List;
import java.util.Set;

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
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
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
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.activity.CoreLoggingResourceable;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockAuditLog;
import org.olat.modules.lecture.LectureBlockManagedFlag;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LectureBlockRow;
import org.olat.modules.lecture.model.LectureBlockWithTeachers;
import org.olat.modules.lecture.ui.LectureListRepositoryDataModel.BlockCols;
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

	private FormLink addLectureButton, deleteLecturesButton;
	private FlexiTableElement tableEl;
	private LectureListRepositoryDataModel tableModel;
	
	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private DialogBoxController deleteDialogCtrl;
	private DialogBoxController bulkDeleteDialogCtrl;
	private EditLectureBlockController editLectureCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;

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
			
			deleteLecturesButton = uifactory.addFormLink("delete", formLayout, Link.BUTTON);
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BlockCols.id));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.title));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.location));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.date, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.startTime, new TimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.endTime, new TimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BlockCols.teachers));

		DefaultFlexiColumnModel editColumn = new DefaultFlexiColumnModel("table.header.edit", -1, "edit",
				new StaticFlexiCellRenderer("", "edit", "o_icon o_icon-lg o_icon_edit", translate("edit"), null));
		editColumn.setExportable(false);
		editColumn.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(editColumn);
			
		DefaultFlexiColumnModel toolsColumn = new DefaultFlexiColumnModel(BlockCols.tools);
		toolsColumn.setExportable(false);
		toolsColumn.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(toolsColumn);
		
		tableModel = new LectureListRepositoryDataModel(columnsModel, getLocale()); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setEmtpyTableMessageKey("empty.table.lectures.blocks.admin");
		
		FlexiTableSortOptions options = new FlexiTableSortOptions();
		options.setDefaultOrderBy(new SortKey(BlockCols.date.name(), false));
		tableEl.setSortSettings(options);
		tableEl.setAndLoadPersistedPreferences(ureq, "repo-lecture-block-list");
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

			LectureBlockRow row = new LectureBlockRow(b, entry.getDisplayname(), entry.getExternalRef(), teachers.toString(), false);
			rows.add(row);
			
			String linkName = "tools-" + counter++;
			FormLink toolsLink = uifactory.addFormLink(linkName, "", null, flc, Link.LINK | Link.NONTRANSLATED);
			toolsLink.setIconRightCSS("o_icon o_icon_actions o_icon-lg");
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
		} else if(deleteDialogCtrl == source) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				LectureBlockRow row = (LectureBlockRow)deleteDialogCtrl.getUserObject();
				doDelete(row);
				loadModel();
				cleanUp();
			}
		} else if(bulkDeleteDialogCtrl == source) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				@SuppressWarnings("unchecked")
				List<LectureBlock> blocks = (List<LectureBlock>)bulkDeleteDialogCtrl.getUserObject();
				doDelete(blocks);
				loadModel();
				cleanUp();
			}
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(bulkDeleteDialogCtrl);
		removeAsListenerAndDispose(deleteDialogCtrl);
		removeAsListenerAndDispose(editLectureCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		bulkDeleteDialogCtrl = null;
		deleteDialogCtrl = null;
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
		if(editLectureCtrl != null) return;
		
		LectureBlock block = lectureService.getLectureBlock(row);
		boolean readOnly = lectureManagementManaged || !secCallback.canNewLectureBlock();
		editLectureCtrl = new EditLectureBlockController(ureq, getWindowControl(), entry, block, readOnly);
		listenTo(editLectureCtrl);

		cmc = new CloseableModalController(getWindowControl(), "close", editLectureCtrl.getInitialComponent(), true, translate("add.lecture"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doAddLectureBlock(UserRequest ureq) {
		if(editLectureCtrl != null || !secCallback.canNewLectureBlock()) return;
		
		editLectureCtrl = new EditLectureBlockController(ureq, getWindowControl(), entry);
		listenTo(editLectureCtrl);

		cmc = new CloseableModalController(getWindowControl(), "close", editLectureCtrl.getInitialComponent(), true, translate("add.lecture"));
		listenTo(cmc);
		cmc.activate();
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
			StringBuilder titles = new StringBuilder();
			for(LectureBlock block:blocks) {
				if(titles.length() > 0) titles.append(", ");
				titles.append(StringHelper.escapeHtml(block.getTitle()));
			}
			String text = translate("confirm.delete.lectures", new String[] { titles.toString() });
			bulkDeleteDialogCtrl = activateYesNoDialog(ureq, translate("delete.lectures.title"), text, bulkDeleteDialogCtrl);
			bulkDeleteDialogCtrl.setUserObject(blocks);
		}
	}
	
	private void doConfirmDelete(UserRequest ureq, LectureBlockRow row) {
		String text = translate("confirm.delete.lectures", new String[] { row.getLectureBlock().getTitle() });
		deleteDialogCtrl = activateYesNoDialog(ureq, translate("delete.lectures.title"), text, deleteDialogCtrl);
		deleteDialogCtrl.setUserObject(row);
	}
	
	private void doDelete(LectureBlockRow row) {
		if(LectureBlockManagedFlag.isManaged(row.getLectureBlock(), LectureBlockManagedFlag.delete)) return;
		
		LectureBlock lectureBlock = row.getLectureBlock();
		lectureService.deleteLectureBlock(lectureBlock);
		showInfo("lecture.deleted");
		logAudit("Lecture block deleted: " + lectureBlock, null);
		ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LECTURE_BLOCK_DELETED, getClass(),
				CoreLoggingResourceable.wrap(lectureBlock, OlatResourceableType.lectureBlock, lectureBlock.getTitle()));
		
	}
	
	private void doDelete(List<LectureBlock> blocks) {
		for(LectureBlock block:blocks) {
			lectureService.deleteLectureBlock(block);
			logAudit("Lecture block deleted: " + block, null);
			ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LECTURE_BLOCK_DELETED, getClass(),
					CoreLoggingResourceable.wrap(block, OlatResourceableType.lectureBlock, block.getTitle()));
		}
		showInfo("lecture.deleted");
	}
	
	private void doExportLog(UserRequest ureq, LectureBlockRow row) {
		LectureBlock lectureBlock = lectureService.getLectureBlock(row);
		List<LectureBlockAuditLog> auditLog = lectureService.getAuditLog(row);
		boolean authorizedAbsenceEnabled = lectureModule.isAuthorizedAbsenceEnabled();
		LectureBlockAuditLogExport export = new LectureBlockAuditLogExport(entry, lectureBlock, auditLog, authorizedAbsenceEnabled, getTranslator());
		ureq.getDispatchResult().setResultingMediaResource(export);
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
		
		private final LectureBlockRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, LectureBlockRow row) {
			super(ureq, wControl);
			this.row = row;
			
			VelocityContainer mainVC = createVelocityContainer("lectures_tools");
			
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
			}
		}
	}
}