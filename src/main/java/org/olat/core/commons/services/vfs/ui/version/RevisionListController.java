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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityShort;
import org.olat.core.commons.modules.bc.commands.FolderCommand;
import org.olat.core.commons.modules.bc.commands.FolderCommandStatus;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.VFSRevision;
import org.olat.core.commons.services.vfs.ui.component.BytesCellRenderer;
import org.olat.core.commons.services.vfs.ui.media.VFSRevisionMediaResource;
import org.olat.core.commons.services.vfs.ui.version.RevisionListDataModel.RevisionCols;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DownloadLink;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RevisionListController extends FormBasicController {

	private int status = FolderCommandStatus.STATUS_SUCCESS;

	private DialogBoxController confirmDeleteBoxCtr;
	
	private FlexiTableElement tableEl;
	private RevisionListDataModel tableModel;
	
	private int counter = 0;
	private final boolean readOnly;
	private final VFSLeaf versionedFile;
	
	private final String title;
	private final String description;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;

	public RevisionListController(UserRequest ureq, WindowControl wControl, VFSItem versionedFile, boolean readOnly) {
		this(ureq, wControl, versionedFile, null, null, readOnly);
	}

	public RevisionListController(UserRequest ureq, WindowControl wControl, VFSItem versionedFile,
			String title, String description, boolean readOnly) {
		super(ureq, wControl, "revisions");
		
		this.readOnly = readOnly;
		this.title = title;
		this.description = description;
		this.versionedFile = (VFSLeaf)versionedFile;
		
		initForm(ureq);
		loadModel(this.versionedFile);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			if (StringHelper.containsNonWhitespace(title)) {
				layoutCont.contextPut("title", title);
			}
			if (StringHelper.containsNonWhitespace(description)) {
				layoutCont.contextPut("description", description);
			}
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RevisionCols.id));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RevisionCols.nr, new RevisionNrsCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RevisionCols.size, new BytesCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RevisionCols.author, new IdentityCellRenderer(userManager)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RevisionCols.revisionComment));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RevisionCols.date, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RevisionCols.download));
		if(!readOnly) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RevisionCols.restore, "restore",
					new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("version.restore"), "restore"), null)));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RevisionCols.delete, "delete",
					new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("delete"), "delete"), null)));
		}

		tableModel = new RevisionListDataModel(columnsModel, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 24, false, getTranslator(), formLayout);
		tableEl.setEmtpyTableMessageKey("version.noRevisions");
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions(true);
		sortOptions.setDefaultOrderBy(new SortKey(RevisionCols.nr.name(), false));
		tableEl.setSortSettings(sortOptions);
		tableEl.setAndLoadPersistedPreferences(ureq, "revisions-list");
		
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	@Override
	protected void doDispose() {
		//
	}

	private void loadModel(VFSLeaf versionedLeaf) {
		VFSMetadata metadata = vfsRepositoryService.getMetadataFor(versionedLeaf);
		List<VFSRevision> revisions = vfsRepositoryService.getRevisions(metadata);
		List<RevisionRow> rows = new ArrayList<>(revisions.size() + 2);
		VFSMediaResource resource = new VFSMediaResource(versionedFile);
		resource.setDownloadable(true);
		DownloadLink download = uifactory.addDownloadLink("download" + (counter++), translate("download"), null, resource, tableEl);
		rows.add(new RevisionRow(metadata, download));
		Collection<String> names = new HashSet<>();
		for(VFSRevision revision:revisions) {
			MediaResource revResource = new VFSRevisionMediaResource(metadata, revision);
			DownloadLink revDownload = uifactory.addDownloadLink("download" + (counter++), translate("download"), null, revResource, tableEl);
			rows.add(new RevisionRow(revision, revDownload));
		}
		
		Map<String, IdentityShort> mappedIdentities = new HashMap<>();
		for(IdentityShort identity :securityManager.findShortIdentitiesByName(names)) {
			mappedIdentities.put(identity.getName(), identity);
		}

		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	public int getStatus() {
		return status;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se  = (SelectionEvent)event;
				if("delete".equals(se.getCommand())) {
					RevisionRow row = tableModel.getObject(se.getIndex());
					doConfirmDelete(ureq, Collections.singletonList(row));
				} else if("restore".equals(se.getCommand())) {
					RevisionRow row = tableModel.getObject(se.getIndex());
					doRestore(ureq, row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == confirmDeleteBoxCtr) {
			doDeleteRevision(ureq, event);
		}
	}
	
	private void doRestore(UserRequest ureq, RevisionRow row) {
		if(row.getRevision() == null) {
			//restore current, do nothing
			status = FolderCommandStatus.STATUS_SUCCESS;
			fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
		} else {
			VFSRevision version = row.getRevision();
			String comment = translate("version.restore.comment", new String[]{ Integer.toString(version.getRevisionNr()) });
			
			boolean ok = vfsRepositoryService.restoreRevision(getIdentity(), version, comment);
			if(ok) {
				status = FolderCommandStatus.STATUS_SUCCESS;
				fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
			} else {
				status = FolderCommandStatus.STATUS_FAILED;
				showError("version.restore.failed");
				fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
			}
		}
	}
	
	private void doConfirmDelete(UserRequest ureq, List<RevisionRow> selectedRevisions) {
		String numOfVersionToDelete = Integer.toString(selectedRevisions.size());
		confirmDeleteBoxCtr = activateYesNoDialog(ureq, null, translate("version.confirmDelete",
				new String[] { numOfVersionToDelete }), confirmDeleteBoxCtr);
		confirmDeleteBoxCtr.setUserObject(selectedRevisions);
	}
	
	private void doDeleteRevision(UserRequest ureq, Event event) {
		if (DialogBoxUIFactory.isYesEvent(event)) {
			@SuppressWarnings("unchecked")
			List<RevisionRow> selectedVersions = (List<RevisionRow>) confirmDeleteBoxCtr.getUserObject();
			List<VFSRevision> revisions = new ArrayList<>(selectedVersions.size());
			for(RevisionRow selectedVersion:selectedVersions) {
				if(selectedVersion.getRevision() != null) {
					revisions.add(selectedVersion.getRevision());
				}
			}
			vfsRepositoryService.deleteRevisions(getIdentity(), revisions);
			status = FolderCommandStatus.STATUS_SUCCESS;
		} else {
			status = FolderCommandStatus.STATUS_CANCELED;
		}
		fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
	}
}