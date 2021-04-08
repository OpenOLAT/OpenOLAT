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
package org.olat.core.commons.services.vfs.ui.version;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.core.commons.modules.bc.commands.FolderCommand;
import org.olat.core.commons.modules.bc.commands.FolderCommandStatus;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.VFSRevision;
import org.olat.core.commons.services.vfs.ui.component.BytesCellRenderer;
import org.olat.core.commons.services.vfs.ui.component.FileIconCellRenderer;
import org.olat.core.commons.services.vfs.ui.media.VFSRevisionMediaResource;
import org.olat.core.commons.services.vfs.ui.version.DeletedFileListDataModel.DeletedCols;
import org.olat.core.commons.services.vfs.ui.version.RevisionListDataModel.RevisionCols;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DownloadLink;
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
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DeletedFileListController extends FormBasicController {

	private int status = FolderCommandStatus.STATUS_SUCCESS;

	private FormLink deleteButton;
	private FormLink restoreButton;
	private FlexiTableElement tableEl;
	private DeletedFileListDataModel tableModel;

	private DialogBoxController dialogCtr;
	
	private int counter = 0;
	private final VFSContainer container;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	public DeletedFileListController(UserRequest ureq, WindowControl wControl, VFSContainer container) {
		super(ureq, wControl, "deleted_files");
		this.container = container;
		initForm(ureq);
		loadModel();
	}
	
	public int getStatus() {
		return status;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		VFSSecurityCallback secCallback = VFSManager.findInheritedSecurityCallback(container);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, DeletedCols.id));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DeletedCols.nr));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DeletedCols.css, new FileIconCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DeletedCols.filename));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DeletedCols.size, new BytesCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DeletedCols.deletedBy, new IdentityCellRenderer(userManager)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DeletedCols.date, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DeletedCols.download));
		if (secCallback != null && secCallback.canWrite()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DeletedCols.restore, "restore",
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("version.restore"), "restore"), null)));
		}
		
		tableModel = new DeletedFileListDataModel(columnsModel, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 24, false, getTranslator(), formLayout);
		tableEl.setEmptyTableSettings("version.noDeletedFiles", null, "o_icon_files");
		tableEl.setMultiSelect(true);
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions(true);
		sortOptions.setDefaultOrderBy(new SortKey(RevisionCols.nr.name(), false));
		tableEl.setSortSettings(sortOptions);
		tableEl.setAndLoadPersistedPreferences(ureq, "deleted-file-list-v2");

		if (secCallback != null) {
			if (secCallback.canDeleteRevisionsPermanently()) {
				deleteButton = uifactory.addFormLink("delete", formLayout, Link.BUTTON);
			}
			if (secCallback.canWrite()) {
				restoreButton = uifactory.addFormLink("version.restore", formLayout, Link.BUTTON);
			}
		}
		
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == dialogCtr) {
			if (DialogBoxUIFactory.isYesEvent(event)) {	
				@SuppressWarnings("unchecked")
				List<DeletedFileRow> rowsToDelete =  (List<DeletedFileRow>)dialogCtr.getUserObject();
				doDelete(rowsToDelete);
				fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		status = FolderCommandStatus.STATUS_CANCELED;
		fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(restoreButton == source) {
			doRestore(ureq);
		} else if(deleteButton == source) {
			doConfirmDelete(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("restore".equals(se.getCommand())) {
					DeletedFileRow row = tableModel.getObject(se.getIndex());
					doRestore(ureq, row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void loadModel() {
		VFSMetadata containerMetadata = vfsRepositoryService.getMetadataFor(container);
		List<VFSMetadata> metadataList = vfsRepositoryService.getChildren(containerMetadata);
		List<DeletedFileRow> rows = new ArrayList<>();
		Map<VFSMetadata,DeletedFileRow> metaToRows = new HashMap<>();
		for(VFSMetadata metadata:metadataList) {
			if(metadata.isDeleted() && !metadata.isDirectory()) {
				DeletedFileRow row = new DeletedFileRow(metadata);
				rows.add(row);
				metaToRows.put(metadata, row);
			}
		}

		List<VFSRevision> revisionList = vfsRepositoryService.getRevisions(new ArrayList<>(rows));
		for(VFSRevision revision:revisionList) {
			DeletedFileRow row = metaToRows.get(revision.getMetadata());
			if(row != null) {
				row.addRevision(revision);
			}
		}
		
		for(DeletedFileRow row:rows) {
			if(row.getLastRevision() != null) {
				MediaResource resource = new VFSRevisionMediaResource(row.getMetadata(), row.getLastRevision());
				DownloadLink download = uifactory.addDownloadLink("download" + (counter++), translate("download"), null, resource, tableEl);
				row.setDownloadLink(download);
			}
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void doDispose() {
	//
	}

	private void doConfirmDelete(UserRequest ureq) {
		List<DeletedFileRow> versionsToDelete = getSelectedRows();
		if (!versionsToDelete.isEmpty()) {
			String msg = translate("version.del.confirm") + "<p>" + renderVersionsAsHtml(versionsToDelete) + "</p>";
			// create dialog controller
			dialogCtr = activateYesNoDialog(ureq, translate("version.del.header"), msg, dialogCtr);
			dialogCtr.setUserObject(versionsToDelete);
		}
	}
	
	private String renderVersionsAsHtml(List<DeletedFileRow> rows) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("<ul>");
		for (DeletedFileRow row:rows) {
			sb.append("<li>").append(row.getFilename()).append("</li>");
		}
		sb.append("</ul>");
		return sb.toString();
	}
	
	private void doDelete(List<DeletedFileRow> rowsToDelete) {
		List<VFSRevision> revisionsToDelete = new ArrayList<>();
		for(DeletedFileRow row:rowsToDelete) {
			List<VFSRevision> rowRevisions = vfsRepositoryService.getRevisions(row.getMetadata());
			revisionsToDelete.addAll(rowRevisions);
		}
		vfsRepositoryService.deleteRevisions(getIdentity(), revisionsToDelete);
		for(DeletedFileRow rowToDelete:rowsToDelete) {
			List<VFSRevision> revisions = new ArrayList<>(rowToDelete.getRevisions());
			revisions.removeAll(revisionsToDelete);
			if(revisions.isEmpty()) {
				// double check if the real file is really deleted 
				VFSItem item = vfsRepositoryService.getItemFor(rowToDelete.getMetadata());
				if(item == null || !item.exists()) {
					VFSMetadata orphanMeta = vfsRepositoryService.getMetadata(rowToDelete);
					vfsRepositoryService.deleteMetadata(orphanMeta);
				}
			}
		}
		
		status = FolderCommandStatus.STATUS_SUCCESS;
	}
	
	private List<DeletedFileRow> getSelectedRows() {
		Set<Integer> selectedIndexes = tableEl.getMultiSelectedIndex();
		List<DeletedFileRow> selectedRows = new ArrayList<>(selectedIndexes.size());
		for(Integer selectedIndex:selectedIndexes) {
			DeletedFileRow row = tableModel.getObject(selectedIndex.intValue());
			if(row != null) {
				selectedRows.add(row);
			}
		}
		return selectedRows;
	}
	
	private void doRestore(UserRequest ureq, DeletedFileRow row) {
		if (row.getLastRevision() != null && vfsRepositoryService.restoreRevision(getIdentity(), row.getLastRevision(), "")) {
			status = FolderCommandStatus.STATUS_SUCCESS;
			fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
		} else {
			status = FolderCommandStatus.STATUS_FAILED;
			// ERROR
		}
	}

	private void doRestore(UserRequest ureq) {
		List<DeletedFileRow> selectedRows = getSelectedRows();
		boolean allOk = true;
		for (DeletedFileRow selectedRow : selectedRows) {
			if(selectedRow.getLastRevision() != null) {
				allOk &= vfsRepositoryService.restoreRevision(getIdentity(), selectedRow.getLastRevision(), "");
			}
		}
		if (allOk) {
			status = FolderCommandStatus.STATUS_SUCCESS;
			fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
		} else {
			status = FolderCommandStatus.STATUS_FAILED;
		}
	}
}
