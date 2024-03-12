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
package org.olat.core.commons.services.export.ui;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.export.ArchiveType;
import org.olat.core.commons.services.export.ExportManager;
import org.olat.core.commons.services.export.ExportMetadata;
import org.olat.core.commons.services.export.model.ExportInfos;
import org.olat.core.commons.services.export.model.SearchExportMetadataParameters;
import org.olat.core.commons.services.export.ui.ExportsListDataModel.ExportsCols;
import org.olat.core.commons.services.taskexecutor.Task;
import org.olat.core.commons.services.taskexecutor.TaskEvent;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.commons.services.taskexecutor.TaskStatus;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelAlignment;
import org.olat.core.gui.components.progressbar.ProgressBarCallback;
import org.olat.core.gui.components.progressbar.ProgressBarItem;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 1 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ExportsListController extends FormBasicController implements FlexiTableComponentDelegate, GenericEventListener {
	
	private FormLink deleteMyButton;
	private FormLink deleteAllButton;
	private FlexiTableElement tableEl;
	private ExportsListDataModel tableModel;
	
	private final RepositoryEntry entry;
	private final String subIdent;
	private final boolean admin;
	
	private int count = 0;
	private final DateFormatSymbols symbols;
	private final ExportsListSettings options;
	protected final boolean isAdministrator;

	private CloseableModalController cmc;
	private ExportInfosController infosCtrl;
	protected DialogBoxController confirmCancelCtrl;
	private DialogBoxController confirmDeleteAllCtrl;
	private ConfirmDeleteExportController confirmDeleteCtrl;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private ExportManager exportManager;
	@Autowired
	private CoordinatorManager coordinatorManager;
	@Autowired
	private TaskExecutorManager taskExecutorManager;
	
	public ExportsListController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, String subIdent, boolean admin,
			ExportsListSettings options) {
		this(ureq, wControl, entry, subIdent, admin, options, "export_list");
	}

	public ExportsListController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, String subIdent, boolean admin,
			ExportsListSettings options, String pageName) {
		super(ureq, wControl, pageName, Util.createPackageTranslator(ExportsListController.class, ureq.getLocale()));
		this.admin = admin;
		this.entry = entry;
		this.options = options;
		this.subIdent = subIdent;
		symbols = new DateFormatSymbols(getLocale());
		isAdministrator = ureq.getUserSession().getRoles()
				.hasSomeRoles(OrganisationRoles.administrator);

		initForm(ureq);
		loadModel();
		
		coordinatorManager.getCoordinator().getEventBus()
			.registerFor(this, getIdentity(), TaskExecutorManager.TASK_EVENTS);
	}

	@Override
	protected void doDispose() {
		coordinatorManager.getCoordinator().getEventBus()
			.deregisterFor(this, TaskExecutorManager.TASK_EVENTS);
		super.doDispose();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			layoutCont.contextPut("withTitle", Boolean.valueOf(options.withTitle()));
		}
		
		deleteAllButton = uifactory.addFormLink("delete.all", formLayout, Link.BUTTON);
		deleteAllButton.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
		
		deleteMyButton = uifactory.addFormLink("delete.my", formLayout, Link.BUTTON);
		deleteMyButton.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
		deleteMyButton.setVisible(admin);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ExportsCols.title));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ExportsCols.creationDate));
		
		tableModel = new ExportsListDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "list", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setElementCssClass("o_sel_export_list");
		initTable(tableEl);
	}
	
	protected void initTable(FlexiTableElement tableElement) {
		VelocityContainer row = new VelocityContainer(null, "vc_row1", Util.getPackageVelocityRoot(ExportsListController.class) + "/row_1.html",
				getTranslator(), this);
		row.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
		tableElement.setRowRenderer(row, this);
	}
	
	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> cmps = null;
		if(rowObject instanceof ExportRow exportRow) {
			cmps = new ArrayList<>();
			if(exportRow.getCancelButton() != null) {
				cmps.add(exportRow.getCancelButton().getComponent());
			}
			if(exportRow.getDownloadButton() != null) {
				cmps.add(exportRow.getDownloadButton().getComponent());
			}
			if(exportRow.getDownloadLink() != null) {
				cmps.add(exportRow.getDownloadLink().getComponent());
			}
			if(exportRow.getInfosButton() != null) {
				cmps.add(exportRow.getInfosButton().getComponent());
			}
			if(exportRow.getDeleteButton() != null) {
				cmps.add(exportRow.getDeleteButton().getComponent());
			}
			if(exportRow.getProgressBar() != null) {
				cmps.add(exportRow.getProgressBar().getComponent());
			}
		}
		return cmps;
	}
	
	public SearchExportMetadataParameters getSearchParams() {
		SearchExportMetadataParameters params = new SearchExportMetadataParameters(entry, subIdent,
				List.of(ArchiveType.COMPLETE, ArchiveType.PARTIAL, ArchiveType.QTI21));
		if(!isAdministrator) {
			params.setOnlyAdministrators3(Boolean.FALSE);
		}
		return params;
	}

	public void loadModel() {
		List<ExportInfos> exports;
		SearchExportMetadataParameters params = getSearchParams();
		if(entry == null) {
			exports = exportManager.getResultsExport(params);
		} else {
			exports = exportManager.getResultsExport(entry, subIdent, params);
		}
		if(!admin) {
			exports = exports.stream()
					.filter(exp -> getIdentity().equals(exp.getCreator()))
					.collect(Collectors.toList());
		}
		
		List<ExportRow> rows = new ArrayList<>(exports.size());
		for(ExportInfos export:exports) {
			String creatorFullName = userManager.getUserDisplayName(export.getCreator());
			String type = getTranslatedType(export);
			ExportRow row = new ExportRow(export, type, creatorFullName);
			if(export.getTask() != null && (export.getTask().getStatus() == TaskStatus.newTask
					|| export.getTask().getStatus() == TaskStatus.inWork)) {
				forgeRunningExport(row);
			} else {
				forgeExport(row);
			}
			rows.add(row);
		}
		
		Collections.sort(rows, new ExportRowComparator());
		
		int currentMonth = -1;
		Calendar cal = Calendar.getInstance();
		for(ExportRow row:rows) {
			cal.setTime(row.getCreationDate());
			int month = cal.get(Calendar.MONTH);
			if(currentMonth != month) {
				row.setMonth(symbols.getMonths()[month]);
				currentMonth = month;
			}
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
		deleteAllButton.setVisible(!rows.isEmpty());
	}
	
	private String getTranslatedType(ExportInfos export) {
		ExportMetadata metadata  = export.getExportMetadata();
		if(metadata == null || metadata.getArchiveType() == null) {
			return null;
		}
		ArchiveType type = metadata.getArchiveType();
		return switch(type) {
			case COMPLETE -> translate("archive.complete");
			case PARTIAL -> translate("archive.partial");
			default -> "-";
		};
	}
	
	private void forgeRunningExport(ExportRow row) {
		String c = Integer.toString(++count);
		
		ProgressBarItem progressItem = uifactory.addProgressBar("progress-".concat(c), null, flc);	
		progressItem.setPercentagesEnabled(true);
		progressItem.setWidthInPercent(true);
		progressItem.setLabelAlignment(LabelAlignment.none);
		progressItem.setMax(1.0f);
		Task runningTask = row.getRunningTask();
		if(runningTask != null) {
			if(runningTask.getProgress() != null) {
				progressItem.setActual(runningTask.getProgress().floatValue());
			}
			progressItem.setProgressCallback(new TaskProgressBarCallback(runningTask));
		}
		row.setProgressBar(progressItem);

		FormLink cancelButton = uifactory.addFormLink("cancel-".concat(c), "cancel", "cancel", null, flc, Link.BUTTON);
		cancelButton.setGhost(true);
		cancelButton.setIconLeftCSS("o_icon o_icon-fw o_icon_cancel");
		row.setCancelButton(cancelButton);
		cancelButton.setUserObject(row);
	}
	
	private void forgeExport(ExportRow row) {
		String c = Integer.toString(++count);
		
		FormLink infosButton = uifactory.addFormLink("infos-".concat(c), "infos", "info", null, flc, Link.BUTTON);
		infosButton.setIconLeftCSS("o_icon o_icon-fw o_icon_description");
		infosButton.setGhost(true);
		row.setInfosButton(infosButton);
		infosButton.setUserObject(row);
		
		FormLink deleteButton = uifactory.addFormLink("delete-".concat(c), "delete", "delete", null, flc, Link.BUTTON);
		deleteButton.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
		deleteButton.setGhost(true);
		row.setDeleteButton(deleteButton);
		deleteButton.setUserObject(row);
		
		FormLink downloadButton = uifactory.addFormLink("download-".concat(c), "download", "download", null, flc, Link.BUTTON);
		downloadButton.setIconLeftCSS("o_icon o_icon-fw o_icon_download");
		downloadButton.setGhost(true);
		row.setDownloadButton(downloadButton);
		downloadButton.setUserObject(row);
		
		String title = StringHelper.escapeHtml(row.getTitle());
		FormLink downloadLink = uifactory.addFormLink("download-2-".concat(c), "download", title, null, flc, Link.NONTRANSLATED);
		row.setDownloadLink(downloadLink);
		downloadLink.setUserObject(row);
	}
	
	@Override
	public void event(Event event) {
		if(event instanceof TaskEvent te) {
			processTask(te.getTaskKey());
		}
	}
	
	private void processTask(Long taskKey) {
		try {
			List<ExportRow> rows = tableModel.getObjects();
			for(ExportRow row:rows) {
				ExportInfos export = row.getExport();
				if(export != null && export.getTask() != null && taskKey.equals(export.getTask().getKey())) {
					loadModel();
					tableEl.reset(false, false, true);
					break;
				}
			}
		} catch (Exception e) {
			logError("", e);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmDeleteCtrl == source) {
			loadModel();
			cmc.deactivate();
			cleanUp();
		} else if(confirmCancelCtrl == source) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				doCancel((ExportRow)confirmCancelCtrl.getUserObject());
			}
		} else if(confirmDeleteAllCtrl == source) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				doDeleteAll((Boolean)confirmDeleteAllCtrl.getUserObject());
			}
		} else if(infosCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	protected void cleanUp() {
		removeAsListenerAndDispose(confirmDeleteCtrl);
		removeAsListenerAndDispose(infosCtrl);
		removeAsListenerAndDispose(cmc);
		confirmDeleteCtrl = null;
		infosCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(deleteAllButton == source) {
			doConfirmDeleteAll(ureq, Boolean.TRUE);
		} else if(deleteMyButton == source) {
			doConfirmDeleteAll(ureq, Boolean.FALSE);
		}  else if(source instanceof FormLink link) {
			String cmd = link.getCmd();
			if("cancel".equals(cmd) && link.getUserObject() instanceof ExportRow exportRow) {
				doConfirmCancel(ureq, exportRow);
			} else if("infos".equals(cmd) && link.getUserObject() instanceof ExportRow exportRow) {
				doInfos(ureq, exportRow);
			} else if("delete".equals(cmd) && link.getUserObject() instanceof ExportRow exportRow) {
				doConfirmDelete(ureq, exportRow);
			} else if("download".equals(cmd) && link.getUserObject() instanceof ExportRow exportRow) {
				doDownload(ureq, exportRow);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	protected void doConfirmCancel(UserRequest ureq, ExportRow row) {
		String[] args = { StringHelper.escapeHtml(row.getTitle()) };
		String title = translate("confirm.cancel.title", args);
		String text = translate("confirm.cancel.text", args);		
		confirmCancelCtrl = activateYesNoDialog(ureq, title, text, confirmCancelCtrl);
		confirmCancelCtrl.setUserObject(row);
	}
	
	private void doCancel(ExportRow row) {
		exportManager.cancelExport(row.getExport(), entry, subIdent);
		loadModel();
		showInfo("info.export.cancelled");
	}
	
	private void doConfirmDelete(UserRequest ureq, ExportRow row) {
		confirmDeleteCtrl = new ConfirmDeleteExportController(ureq, getWindowControl(), row);
		listenTo(confirmDeleteCtrl);
		
		String title = translate("confirm.delete.title", row.getTitle());
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmDeleteCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmDeleteAll(UserRequest ureq, Boolean all) {
		int numOfRows = 0;
		List<ExportRow> rows = tableModel.getObjects();
		for(ExportRow row:rows) {
			if(all.booleanValue() || getIdentity().equals(row.getExport().getCreator())) {
				numOfRows++;
			}
		}
		
		String[] args = { Integer.toString(numOfRows) };
		String i18nTitle = numOfRows <= 1 ? "confirm.delete.all.title" : "confirm.delete.all.title.plural";
		String i18nText = numOfRows <= 1 ? "confirm.delete.all.text" : "confirm.delete.all.text.plural";
		String title = translate(i18nTitle, args);
		String text = translate(i18nText, args);		
		confirmDeleteAllCtrl = activateYesNoDialog(ureq, title, text, confirmDeleteAllCtrl);
		confirmDeleteAllCtrl.setUserObject(all);
	}
	
	private void doDeleteAll(Boolean all) {
		List<ExportRow> rows = tableModel.getObjects();
		for(ExportRow row:rows) {
			if(all.booleanValue() || getIdentity().equals(row.getExport().getCreator())) {
				exportManager.deleteExport(row.getExport());
			}
		}
		dbInstance.commitAndCloseSession();
		loadModel();
		tableEl.reset(true, true, true);
		showInfo("infos.export.delete.all");
	}
	
	protected void doInfos(UserRequest ureq, ExportRow row) {
		infosCtrl = new ExportInfosController(ureq, getWindowControl(), row);
		listenTo(infosCtrl);
		
		String title = translate("export.metadata", row.getTitle());
		cmc = new CloseableModalController(getWindowControl(), translate("close"), infosCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doDownload(UserRequest ureq, ExportRow row) {
		VFSLeaf archive = row.getArchive();
		MediaResource resource;
		if(archive == null) {
			resource = new NotFoundMediaResource();
		} else {
			resource = new VFSMediaResource(archive);
		}
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}
	
	private class TaskProgressBarCallback implements ProgressBarCallback {
		
		private final Task task;
		
		public TaskProgressBarCallback(Task task) {
			this.task = task;
		}

		@Override
		public float getMax() {
			return 1.0f;
		}

		@Override
		public float getActual() {
			Double progress = taskExecutorManager.getProgress(task);
			dbInstance.commitAndCloseSession();
			return progress == null ? 0.0f : progress.floatValue();
		}
	}
}
