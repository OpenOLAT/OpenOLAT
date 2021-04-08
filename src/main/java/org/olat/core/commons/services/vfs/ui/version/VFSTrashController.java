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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.admin.SystemAdminMainController;
import org.olat.core.commons.modules.bc.commands.FolderCommand;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSMetadataRef;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.VFSRevision;
import org.olat.core.commons.services.vfs.VFSVersionModule;
import org.olat.core.commons.services.vfs.model.VFSRevisionRefImpl;
import org.olat.core.commons.services.vfs.ui.component.BytesCellRenderer;
import org.olat.core.commons.services.vfs.ui.version.VersionsDeletedFileDataModel.VersionsDeletedCols;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.progressbar.ProgressController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.core.util.async.ProgressDelegate;
import org.olat.core.util.vfs.VFSItem;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * This is a controller to configure the SimpleVersionConfig, the configuration
 * of the versioning system for briefcase.
 * 
 * <P>
 * Initial Date:  21 sept. 2009 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class VFSTrashController extends FormBasicController implements ProgressDelegate {
	
	private FormLink pruneLink;
	private FormLink cleanUpLink;
	private StaticTextElement orphanSizeEl;
	private StaticTextElement versionsSizeEl;
	
	private CloseableModalController cmc;
	private ProgressController progressCtrl;
	private DialogBoxController confirmPruneHistoryBox;
	private DialogBoxController confirmDeleteOrphansBox;
	
	private FormLink orphansDeleteButton;
	private FlexiTableElement orphansListTableEl;
	private VersionsDeletedFileDataModel versionsDeletedFileDataModel;
	
	private DialogBoxController dialogCtr;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private VFSVersionModule versionsModule;
	@Autowired
	private TaskExecutorManager taskExecutorManager;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	public VFSTrashController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		// use combined translator from system admin main
		setTranslator(Util.createPackageTranslator(SystemAdminMainController.class, ureq.getLocale(), getTranslator()));
		initForm(ureq);
		loadModel();
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer statsLayout = FormLayoutContainer.createDefaultFormLayout("orphansStats", getTranslator());
		String page = this.velocity_root + "/orphans.html";
		FormLayoutContainer tableLayout = FormLayoutContainer.createCustomFormLayout("orphansTable", getTranslator(), page);
		formLayout.add(statsLayout);
		formLayout.add(tableLayout);
		
		// Upper part
		statsLayout.setFormTitle(translate("version.maintenance.title"));
		
		versionsSizeEl = uifactory.addStaticTextElement("version.size", "version.size", "", statsLayout);
		orphanSizeEl = uifactory.addStaticTextElement("version.orphan.size", "version.orphan.size", "", statsLayout);
		
		FormLayoutContainer buttonsLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		statsLayout.add(buttonsLayout);
		
		cleanUpLink = uifactory.addFormLink("version.clean.up", buttonsLayout, Link.BUTTON);
		pruneLink = uifactory.addFormLink("version.prune.history", buttonsLayout, Link.BUTTON);
		cleanUpLink.setIconLeftCSS(CSSHelper.getIconCssClassFor(CSSHelper.CSS_CLASS_TRASHED));
		pruneLink.setIconLeftCSS(CSSHelper.getIconCssClassFor(CSSHelper.CSS_CLASS_REVISION));

		
		// Lower part
		tableLayout.setFormTitle(translate("version.deletedFiles"));
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, VersionsDeletedCols.id));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(VersionsDeletedCols.relativePath));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(VersionsDeletedCols.filename));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(VersionsDeletedCols.size, new BytesCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", translate("delete"), "delete"));
		
		versionsDeletedFileDataModel = new VersionsDeletedFileDataModel(columnsModel, getTranslator());
		orphansListTableEl = uifactory.addTableElement(getWindowControl(), "orphansList", versionsDeletedFileDataModel, 24, false, getTranslator(), tableLayout);
		orphansListTableEl.setEmptyTableSettings("version.noDeletedFiles", null, "o_icon_files");
		orphansListTableEl.setMultiSelect(true);
		orphansListTableEl.setSelectAllEnable(true);
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions(true);
		sortOptions.setDefaultOrderBy(new SortKey(VersionsDeletedCols.size.name(), false));
		orphansListTableEl.setSortSettings(sortOptions);
		orphansListTableEl.setAndLoadPersistedPreferences(ureq, "deleted-rev-file-list");
		
		orphansDeleteButton = uifactory.addFormLink("delete", tableLayout, Link.BUTTON);
		orphansDeleteButton.setIconLeftCSS(CSSHelper.getIconCssClassFor(CSSHelper.CSS_CLASS_TRASHED));
	}		

	@Override
	protected void doDispose() {
		//
	}
	
	private void loadModel() {
		long versionsSize = vfsRepositoryService.getRevisionsTotalSize();
		versionsSizeEl.setValue(Formatter.formatBytes(versionsSize));
		long versionsDeletedFiles = vfsRepositoryService.getRevisionsTotalSizeOfDeletedFiles();
		orphanSizeEl.setValue(Formatter.formatBytes(versionsDeletedFiles));
		
		List<VFSRevision> revisions = vfsRepositoryService.getRevisionsOfDeletedFiles();
		List<VersionsDeletedFileRow> rows = revisions.stream()
				.map(VersionsDeletedFileRow::new)
				.collect(Collectors.toList());
		versionsDeletedFileDataModel.setObjects(rows);
		orphansListTableEl.reset(true, true, true);
		orphansDeleteButton.setVisible(!rows.isEmpty());
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == confirmDeleteOrphansBox) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				doDeleteOrphans(ureq);
			}
		} else if(source == confirmPruneHistoryBox) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				doPruneHistory(ureq);
			}
		} else if(source == cmc) {
			cleanup();
		} else if (source == dialogCtr) {
			if (DialogBoxUIFactory.isYesEvent(event)) {	
				@SuppressWarnings("unchecked")
				List<VersionsDeletedFileRow> rowsToDelete =  (List<VersionsDeletedFileRow>)dialogCtr.getUserObject();
				doDelete(rowsToDelete);
				fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
				loadModel();
			}
		}
		super.event(ureq, source, event);
	}
	
	private void doDelete(List<VersionsDeletedFileRow> rowsToDelete) {
		for(VersionsDeletedFileRow row:rowsToDelete) {
			VFSRevision revision = vfsRepositoryService.getRevision(new VFSRevisionRefImpl(row.getRevisionKey()));
			if(revision != null) {
				doDelete(revision);
			}
		}
	}
	
	private void doDelete(VFSRevision revision) {
		VFSMetadata metadata = revision.getMetadata();
		vfsRepositoryService.deleteRevisions(getIdentity(), Collections.singletonList(revision));
		dbInstance.commit();
		
		if(metadata.isDeleted()) {
			List<VFSRevision> revisions = vfsRepositoryService.getRevisions(metadata);
			if(revisions.isEmpty()) {
				VFSItem item = vfsRepositoryService.getItemFor(metadata);
				if(item == null || !item.exists()) {
					vfsRepositoryService.deleteMetadata(metadata);
				}
			}
		}
	}
	
	private void cleanup() {
		removeAsListenerAndDispose(cmc);
		cmc = null;
		loadModel();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == cleanUpLink) {
			String text = translate("confirm.delete.orphans");
			confirmDeleteOrphansBox = activateYesNoDialog(ureq, null, text, confirmDeleteOrphansBox);
		} else if(source == pruneLink) {
			String text = translate("confirm.prune.history");
			confirmPruneHistoryBox = activateYesNoDialog(ureq, null, text, confirmPruneHistoryBox);
		} else if(orphansDeleteButton == source) {
			doConfirmDelete(ureq);
			loadModel();
		} else if(orphansListTableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("delete".equals(se.getCommand())) {
					VersionsDeletedFileRow row = versionsDeletedFileDataModel.getObject(se.getIndex());
					doConfirmDelete(ureq, Collections.singletonList(row));
					loadModel();
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doConfirmDelete(UserRequest ureq, List<VersionsDeletedFileRow> rows) {
		String msg = translate("version.del.confirm") + "<p>" + renderVersionsAsHtml(rows) + "</p>";
		dialogCtr = activateYesNoDialog(ureq, translate("version.del.header"), msg, dialogCtr);
		dialogCtr.setUserObject(rows);
	}
	
	private String renderVersionsAsHtml(List<VersionsDeletedFileRow> rows) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("<ul>");
		for (VersionsDeletedFileRow row:rows) {
			sb.append("<li>").append(row.getFilename()).append("</li>");
		}
		sb.append("</ul>");
		return sb.toString();
	}
	
	private void doConfirmDelete(UserRequest ureq) {
		Set<Integer> selectedIndexes = orphansListTableEl.getMultiSelectedIndex();
		List<VersionsDeletedFileRow> rowsToDelete = new ArrayList<>(selectedIndexes.size());
		for(Integer selectedIndex:selectedIndexes) {
			VersionsDeletedFileRow row = versionsDeletedFileDataModel.getObject(selectedIndex.intValue());
			if(row != null) {
				rowsToDelete.add(row);
			}
		}
		if(!rowsToDelete.isEmpty()) {
			doConfirmDelete(ureq, rowsToDelete);
		}
	}
	
	private void doDeleteOrphans(UserRequest ureq) {
		final List<VFSMetadataRef> deleted = vfsRepositoryService.getMetadataOfDeletedFiles();
		
		progressCtrl = new ProgressController(ureq, getWindowControl());
		progressCtrl.setMessage(translate("version.clean.up"));
		progressCtrl.setPercentagesEnabled(false);
		progressCtrl.setUnitLabel("%");
		progressCtrl.setActual(0.0f);
		progressCtrl.setMax(100.0f);
		listenTo(progressCtrl);
		
		taskExecutorManager.execute(() -> {
			waitASecond();
			deletedDeletedFilesRevisions(deleted);
		});

		synchronized(this) {
			if(progressCtrl != null) {
				String title = translate("version.clean.up");
				cmc = new CloseableModalController(getWindowControl(), null, progressCtrl.getInitialComponent(), true, title, false);
				cmc.activate();
				listenTo(cmc);
			}
		}
	}
	
	private void deletedDeletedFilesRevisions(List<VFSMetadataRef> toDeleteList) {
		try {
			int count = 0;
			for(VFSMetadataRef toDelete:toDeleteList) {
				VFSMetadata meta = vfsRepositoryService.getMetadata(toDelete);
				vfsRepositoryService.deleteMetadata(meta);
				dbInstance.commitAndCloseSession();
				setActual((++count / (float)toDeleteList.size()) * 100.0f);
			}	
		} catch (Exception e) {
			dbInstance.closeSession();
			logError("", e);
		}
		finished();
	}
	
	private void doPruneHistory(UserRequest ureq) {
		final int numOfVersions = getNumOfVersions();
		final List<VFSMetadataRef> metadata = vfsRepositoryService.getMetadataWithMoreRevisionsThan(numOfVersions);
		progressCtrl = new ProgressController(ureq, getWindowControl());
		progressCtrl.setMessage(translate("version.prune.history"));
		progressCtrl.setPercentagesEnabled(false);
		progressCtrl.setUnitLabel("%");
		progressCtrl.setMax(100.0f);
		progressCtrl.setActual(0.0f);
		listenTo(progressCtrl);

		taskExecutorManager.execute(() -> {
			waitASecond();
			pruneRevisions(metadata, numOfVersions); 
		});

		synchronized(this) {
			if(progressCtrl != null) {
				String title = translate("version.prune.history");
				cmc = new CloseableModalController(getWindowControl(), null, progressCtrl.getInitialComponent(),
						true, title, false);
				cmc.activate();
				listenTo(cmc);
			}
		}
	}
	
	private void pruneRevisions(final List<VFSMetadataRef> metadata, final int numOfVersions) {
		try {
			final Identity actingIdentity = getIdentity();
			int count = 0;
			for(VFSMetadataRef data:metadata) {
				List<VFSRevision> revs = vfsRepositoryService.getRevisions(data);
				Collections.sort(revs, new AscendingRevisionNrComparator());
				List<VFSRevision> toDelete = revs.subList(0, revs.size() - numOfVersions);
				vfsRepositoryService.deleteRevisions(actingIdentity, toDelete);
				dbInstance.commitAndCloseSession();
				setActual((++count / (float)metadata.size()) * 100.0f);
			}
		} catch (Exception e) {
			dbInstance.closeSession();
			logError("", e);
		}
		finished();
	}
	
	private final void waitASecond() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			logError("Can't wait", e);
		}
	}
	
	public int getNumOfVersions() {
		return versionsModule.getMaxNumberOfVersions();
	}
	
	public final void calculateOrphanSize() {
		long size = vfsRepositoryService.getRevisionsTotalSize();
		String sizeStr =Formatter.formatBytes(size);

		if(orphanSizeEl != null && !isDisposed()) {
			orphanSizeEl.setValue(sizeStr);
		}
	}
	
	@Override
	public void setMax(float max) {
		if(progressCtrl != null && !progressCtrl.isDisposed()) {
			progressCtrl.setMax(max);
		}
	}

	@Override
	public void setActual(float value) {
		if(progressCtrl != null && !progressCtrl.isDisposed()) {
			progressCtrl.setActual(value);
		}
	}

	@Override
	public void setInfo(String message) {
		if(progressCtrl != null && !progressCtrl.isDisposed()) {
			progressCtrl.setInfo(message);
		}
	}

	@Override
	public synchronized void finished() {
		if(cmc != null && !cmc.isDisposed()) {
			cmc.deactivate();
		}
		cleanup();
	}
	
	private static class AscendingRevisionNrComparator implements Comparator<VFSRevision> {
		@Override
		public int compare(VFSRevision o1, VFSRevision o2) {
			int n1 = o1.getRevisionNr();
			int n2 = o2.getRevisionNr();
			return Integer.compare(n1, n2);
		}
	}
}
