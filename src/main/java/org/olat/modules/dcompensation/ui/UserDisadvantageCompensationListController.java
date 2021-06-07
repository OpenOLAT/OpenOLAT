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
package org.olat.modules.dcompensation.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.dcompensation.DisadvantageCompensation;
import org.olat.modules.dcompensation.DisadvantageCompensationAuditLog;
import org.olat.modules.dcompensation.DisadvantageCompensationService;
import org.olat.modules.dcompensation.DisadvantageCompensationStatusEnum;
import org.olat.modules.dcompensation.ui.UserDisadvantageCompensationTableModel.CompensationCols;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 sept. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserDisadvantageCompensationListController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private FormLink batchDeleteButton;
	private FormLink addCompensationButton;
	private UserDisadvantageCompensationTableModel tableModel;
	
	private int counter = 0;
	private final boolean canModify;
	private final Identity disadvantegdIdentity;
	
	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private UserDisadvantageCompensationEditController editCtrl;
	private ConfirmDeleteDisadvantageCompensationController confirmDeleteCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private DisadvantageCompensationService disadvantageCompensationService;
	
	public UserDisadvantageCompensationListController(UserRequest ureq, WindowControl wControl,
			Identity disadvantegdIdentity, boolean canModify) {
		super(ureq, wControl, "user_compensation_list");
		this.canModify = canModify;
		this.disadvantegdIdentity = disadvantegdIdentity;
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormInfo("compensation.list.infos");
		
		addCompensationButton = uifactory.addFormLink("add.compensation", formLayout, Link.BUTTON);
		addCompensationButton.setIconLeftCSS("o_icon o_icon_disadvantage_compensation");
		addCompensationButton.setVisible(canModify);
		
		batchDeleteButton = uifactory.addFormLink("delete", formLayout, Link.BUTTON);
		batchDeleteButton.setVisible(canModify);

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CompensationCols.id));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CompensationCols.creationDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CompensationCols.creator));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CompensationCols.entryKey));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CompensationCols.entry, "select_course"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CompensationCols.externalRef, "select_course"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CompensationCols.courseElement, "select_course"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CompensationCols.courseElementIdent, "select_course"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CompensationCols.extraTime,
				new ExtraTimeCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CompensationCols.approvedBy));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CompensationCols.approvalDate,
				new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CompensationCols.status,
				new StatusCellRenderer(getTranslator())));
		
		StickyActionColumnModel toolsCol = new StickyActionColumnModel(CompensationCols.tools);
		toolsCol.setAlwaysVisible(true);
		toolsCol.setExportable(false);
		toolsCol.setIconHeader("o_icon o_icon-lg o_icon_actions o_icon-fws");
		columnsModel.addFlexiColumnModel(toolsCol);
		
		tableModel = new UserDisadvantageCompensationTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 24, false, getTranslator(), formLayout);
		
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setCustomizeColumns(true);
		tableEl.setExportEnabled(true);
		
		List<FlexiTableFilter> filters = new ArrayList<>();
		filters.add(new FlexiTableFilter(translate("status.active"), DisadvantageCompensationStatusEnum.active.name()));
		filters.add(new FlexiTableFilter(translate("status.deleted"), DisadvantageCompensationStatusEnum.deleted.name()));
		filters.add(FlexiTableFilter.SPACER);
		filters.add(new FlexiTableFilter(translate("show.all"), "all", true));
		tableEl.setFilters("status", filters, false);
		tableEl.setSelectedFilterKey(DisadvantageCompensationStatusEnum.active.name());
		tableEl.setAndLoadPersistedPreferences(ureq, "user-disadvantage-compensations-list-v3");
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private void loadModel() {
		List<DisadvantageCompensation> compensations = disadvantageCompensationService.getDisadvantageCompensations(disadvantegdIdentity);
		List<UserDisadvantageCompensationRow> rows = new ArrayList<>(compensations.size());
		for(DisadvantageCompensation compensation:compensations) {
			String creatorFullName = userManager.getUserDisplayName(compensation.getCreator());
			FormLink toolsLink = uifactory.addFormLink("tools_" + (++counter), "tools", "", null, null, Link.NONTRANSLATED);
			toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");

			UserDisadvantageCompensationRow row = new UserDisadvantageCompensationRow(compensation, creatorFullName, toolsLink);
			rows.add(row);
			toolsLink.setUserObject(row);
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editCtrl == source || confirmDeleteCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editCtrl);
		removeAsListenerAndDispose(cmc);
		editCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addCompensationButton == source) {
			doAddCompensation(ureq);
		} else if(batchDeleteButton == source) {
			doConfirmDelete(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("select_course".equals(se.getCommand())) {
					doOpenCourse(ureq, tableModel.getObject(se.getIndex()));
				}
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("tools".equals(link.getCmd()) && link.getUserObject() instanceof UserDisadvantageCompensationRow) {
				UserDisadvantageCompensationRow row = (UserDisadvantageCompensationRow)link.getUserObject();
				doOpenTools(ureq, row, link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doOpenCourse(UserRequest ureq, UserDisadvantageCompensationRow row) {
		String businessPath = "[RepositoryEntry:" + row.getEntry().getKey() + "][CourseNode:" + row.getCourseElementId() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private void doAddCompensation(UserRequest ureq) {
		if(guardModalController(editCtrl)) return;

		editCtrl = new UserDisadvantageCompensationEditController(ureq, getWindowControl(), disadvantegdIdentity);
		listenTo(editCtrl);
		
		String title = translate("add.compensation");
		cmc = new CloseableModalController(getWindowControl(), "close", editCtrl.getInitialComponent(), true, title, true);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doEditCompensation(UserRequest ureq, UserDisadvantageCompensationRow row) {
		if(guardModalController(editCtrl)) return;

		editCtrl = new UserDisadvantageCompensationEditController(ureq, getWindowControl(), row.getCompensation());
		listenTo(editCtrl);
		
		String title = translate("add.compensation");
		cmc = new CloseableModalController(getWindowControl(), "close", editCtrl.getInitialComponent(), true, title, true);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doConfirmDelete(UserRequest ureq) {
		List<DisadvantageCompensation> compensatioToDelete = tableEl.getMultiSelectedIndex().stream()
			.map(index -> tableModel.getObject(index.intValue()))
			.filter(Objects::nonNull)
			.map(UserDisadvantageCompensationRow::getCompensation)
			.collect(Collectors.toList());
		
		if(compensatioToDelete.isEmpty()) {
			showWarning("warning.atleast.one");
		} else {
			confirmDeleteCtrl = new ConfirmDeleteDisadvantageCompensationController(ureq, getWindowControl(), compensatioToDelete);
			listenTo(confirmDeleteCtrl);
			
			String title = translate("delete.compensation");
			cmc = new CloseableModalController(getWindowControl(), "close", confirmDeleteCtrl.getInitialComponent(), true, title, true);
			cmc.activate();
			listenTo(cmc);
		}
	}
	
	private void doConfirmDelete(UserRequest ureq, UserDisadvantageCompensationRow row) {
		DisadvantageCompensation compensation = row.getCompensation();
		List<DisadvantageCompensation> compensatioToDelete = Collections.singletonList(compensation);
		confirmDeleteCtrl = new ConfirmDeleteDisadvantageCompensationController(ureq, getWindowControl(), compensatioToDelete);
		listenTo(confirmDeleteCtrl);
		
		String title = translate("delete.compensation");
		cmc = new CloseableModalController(getWindowControl(), "close", confirmDeleteCtrl.getInitialComponent(), true, title, true);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doOpenTools(UserRequest ureq, UserDisadvantageCompensationRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private void doExportLog(UserRequest ureq, UserDisadvantageCompensationRow row) {
		List<DisadvantageCompensationAuditLog> auditLogs = disadvantageCompensationService.getAuditLogs(disadvantegdIdentity,
				row.getEntry(), row.getCourseElementId());
		
		String fullName = userManager.getUserDisplayName(disadvantegdIdentity);
		String name = StringHelper.transformDisplayNameToFileSystemName(fullName)
				+ "_" + StringHelper.transformDisplayNameToFileSystemName(row.getEntryDisplayName())
				+ "_" + Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()))
				+ ".xlsx";
		DisadvantageCompensationAuditLogExport export = new DisadvantageCompensationAuditLogExport(name, auditLogs, getTranslator());
		ureq.getDispatchResult().setResultingMediaResource(export);
	}
	
	private class ToolsController extends BasicController {
		
		private Link editLink;
		private Link deleteLink;
		private Link exportLogLink;
		private final VelocityContainer mainVC;

		private UserDisadvantageCompensationRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, UserDisadvantageCompensationRow row) {
			super(ureq, wControl);
			this.row = row;
			
			mainVC = createVelocityContainer("tools");
			
			List<String> links = new ArrayList<>(4);
			if(canModify) {
				editLink = addLink("edit", "o_icon_edit", links);
				deleteLink = addLink("delete", "o_icon_delete_item", links);
				links.add("-");
			}
			exportLogLink = addLink("export.log", "o_icon_log", links);

			mainVC.contextPut("links", links);
			putInitialPanel(mainVC);
		}
		
		private Link addLink(String name, String iconCss, List<String> links) {
			Link link = LinkFactory.createLink(name, name, getTranslator(), mainVC, this, Link.LINK);
			mainVC.put(name, link);
			links.add(name);
			link.setIconLeftCSS("o_icon " + iconCss);
			return link;
		}

		@Override
		protected void doDispose() {
			//
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if(editLink == source) {
				close();
				doEditCompensation(ureq, row);
			} else if(deleteLink == source) {
				close();
				doConfirmDelete(ureq, row);
			} else if(exportLogLink == source) {
				close();
				doExportLog(ureq, row);
			}
		}
		
		private void close() {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		}
	}
}
