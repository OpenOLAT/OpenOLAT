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
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.olat.admin.SystemAdminMainController;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.folder.ui.FolderController;
import org.olat.core.commons.services.folder.ui.FolderUIFactory;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.ui.component.BytesCellRenderer;
import org.olat.core.commons.services.vfs.ui.version.VersionsDeletedFileDataModel.VersionsDeletedCols;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.confirmation.BulkDeleteConfirmationController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSItem;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial Date:  21 sept. 2009 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class VFSTrashController extends FormBasicController {
	
	private FormLink bulkDeletePermanentlyButton;
	
	private CloseableModalController cmc;
	private ConfirmationController deletePermanentlyConfirmationCtrl;
	
	private FlexiTableElement tableEl;
	private VersionsDeletedFileDataModel dataModel;
	
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private UserManager userManager;
	
	public VFSTrashController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		setTranslator(Util.createPackageTranslator(SystemAdminMainController.class, ureq.getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(FolderController.class, ureq.getLocale(), getTranslator()));
		initForm(ureq);
		loadModel();
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("version.deletedFiles");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, VersionsDeletedCols.id));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(VersionsDeletedCols.relativePath));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(VersionsDeletedCols.filename));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(VersionsDeletedCols.size, new BytesCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, VersionsDeletedCols.folder));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(VersionsDeletedCols.deletedDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(VersionsDeletedCols.deletedBy));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", translate("delete"), "delete"));
		
		dataModel = new VersionsDeletedFileDataModel(columnsModel, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "orphansList", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setEmptyTableSettings("version.noDeletedFiles", null, "o_icon_files");
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions(true);
		sortOptions.setDefaultOrderBy(new SortKey(VersionsDeletedCols.size.name(), false));
		tableEl.setSortSettings(sortOptions);
		tableEl.setAndLoadPersistedPreferences(ureq, "deleted-rev-file-list");
		
		FormLayoutContainer dummyCont = FormLayoutContainer.createBareBoneFormLayout("dummy", getTranslator());
		dummyCont.setRootForm(mainForm);
		bulkDeletePermanentlyButton = uifactory.addFormLink("delete.permanently", dummyCont, Link.BUTTON);
		bulkDeletePermanentlyButton.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
		tableEl.addBatchButton(bulkDeletePermanentlyButton);
	}
	
	private void loadModel() {
		List<VFSMetadata> metadataDeleted = vfsRepositoryService.getDeletedDateBeforeMetadatas(DateUtils.addDays(new Date(), 2));
		
		List<TrashRow> rows = new ArrayList<>(metadataDeleted.size());
		for (VFSMetadata vfsMetadata : metadataDeleted) {
			TrashRow row = new TrashRow(vfsMetadata);
			row.setDeletedDate(FolderUIFactory.getDeletedDate(vfsMetadata, null));
			row.setDeletedBy(FolderUIFactory.getDeletedBy(userManager, vfsMetadata, null));
			rows.add(row);
		}
		
		dataModel.setObjects(rows);
		tableEl.reset(false, true, true);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		 if (deletePermanentlyConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				if (deletePermanentlyConfirmationCtrl.getUserObject() instanceof VFSMetadata metadata) {
					doDeletePermanently(metadata);
					loadModel();
				} else {
					doBulkDeletePermanently();
					loadModel();
				}
			}
			cmc.deactivate();
			cleanup();
		} else if(source == cmc) {
			cleanup();
		}
		super.event(ureq, source, event);
	}
	
	
	
	private void cleanup() {
		removeAsListenerAndDispose(deletePermanentlyConfirmationCtrl);
		removeAsListenerAndDispose(cmc);
		deletePermanentlyConfirmationCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("delete".equals(se.getCommand())) {
					TrashRow row = dataModel.getObject(se.getIndex());
					doConfirmDeletePermanently(ureq, row.getMetadata());
				}
			}
		} else if (bulkDeletePermanentlyButton == source) {
			doBulkConfirmDeletePermanently(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doConfirmDeletePermanently(UserRequest ureq, VFSMetadata metadata) {
		if (guardModalController(deletePermanentlyConfirmationCtrl)) return;
		
		removeAsListenerAndDispose(deletePermanentlyConfirmationCtrl);
		
		String message = metadata.isDirectory()
				? translate("delete.permanently.confirmation.message.leaf", StringHelper.escapeHtml(metadata.getFilename()))
				: translate("delete.permanently.confirmation.message.container", StringHelper.escapeHtml(metadata.getFilename()));
		deletePermanentlyConfirmationCtrl = new ConfirmationController(ureq, getWindowControl(), message, null, translate("delete"), true);
		deletePermanentlyConfirmationCtrl.setUserObject(metadata);
		listenTo(deletePermanentlyConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), deletePermanentlyConfirmationCtrl.getInitialComponent(),
				true, translate("delete.permanently"), true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doDeletePermanently(VFSMetadata metadata) {
		VFSMetadata reloadedMetadata = vfsRepositoryService.getMetadata(metadata);
		if (reloadedMetadata == null || !reloadedMetadata.isDeleted()) {
			return;
		}
		
		VFSItem vfsItem = vfsRepositoryService.getItemFor(reloadedMetadata);
		if (vfsItem != null) {
			vfsItem.deleteSilently();
		}
	}
	
	private void doBulkConfirmDeletePermanently(UserRequest ureq) {
		if (guardModalController(deletePermanentlyConfirmationCtrl)) return;
		
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		if (selectedIndex == null || selectedIndex.isEmpty()) {
			showWarning("file.bulk.not.authorized");
			return;
		}
		
		List<String> filenames = selectedIndex.stream()
				.map(index-> dataModel.getObject(index.intValue()))
				.filter(Objects::nonNull)
				.map(TrashRow::getMetadata)
				.map(VFSMetadata::getFilename)
				.sorted()
				.toList();
		deletePermanentlyConfirmationCtrl = new BulkDeleteConfirmationController(ureq, getWindowControl(),
				translate("delete.permanently.confirmation.message", String.valueOf(filenames.size())),
				translate("delete.permanently.confirmation", new String[] { String.valueOf(filenames.size()) }),
				translate("delete"),
				translate("delete.permanently.confirmation.label"), filenames, null);
		listenTo(deletePermanentlyConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), deletePermanentlyConfirmationCtrl.getInitialComponent(),
				true, translate("delete.permanently"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doBulkDeletePermanently() {
		if (tableEl.getMultiSelectedIndex() == null) return;
		
		for (Integer index : tableEl.getMultiSelectedIndex()) {
			TrashRow trashRow = dataModel.getObject(index);
			if (trashRow != null) {
				doDeletePermanently(trashRow.getMetadata());
			}
		}
	}
	
}
