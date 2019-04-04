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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.modules.bc.commands.FolderCommand;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.VFSRevision;
import org.olat.core.commons.services.vfs.model.VFSRevisionRefImpl;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 avr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VersionsDeletedFileController extends FormBasicController {

	private FormLink deleteButton;
	private FlexiTableElement tableEl;
	private VersionsDeletedFileDataModel dataModel;
	
	private DialogBoxController dialogCtr;
	
	@Autowired
	private VFSRepositoryService vfsRepositoryService;

	public VersionsDeletedFileController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "deleted_files_revisions");
		initForm(ureq);
		loadModel();
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, VersionsDeletedCols.id));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(VersionsDeletedCols.relativePath));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(VersionsDeletedCols.filename));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(VersionsDeletedCols.size, new BytesCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", translate("delete"), "delete"));
		
		dataModel = new VersionsDeletedFileDataModel(columnsModel, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 24, false, getTranslator(), formLayout);
		tableEl.setEmtpyTableMessageKey("version.noDeletedFiles");
		tableEl.setMultiSelect(true);
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions(true);
		sortOptions.setDefaultOrderBy(new SortKey(VersionsDeletedCols.size.name(), false));
		tableEl.setSortSettings(sortOptions);
		tableEl.setAndLoadPersistedPreferences(ureq, "deleted-rev-file-list");
		
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		deleteButton = uifactory.addFormLink("delete", formLayout, Link.BUTTON);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private void loadModel() {
		List<VFSRevision> revisions = vfsRepositoryService.getRevisionsOfDeletedFiles();
		List<VersionsDeletedFileRow> rows = revisions.stream()
				.map(VersionsDeletedFileRow::new)
				.collect(Collectors.toList());
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
		deleteButton.setVisible(!rows.isEmpty());
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == dialogCtr) {
			if (DialogBoxUIFactory.isYesEvent(event)) {	
				@SuppressWarnings("unchecked")
				List<VersionsDeletedFileRow> rowsToDelete =  (List<VersionsDeletedFileRow>)dialogCtr.getUserObject();
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
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(deleteButton == source) {
			doConfirmDelete(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("delete".equals(se.getCommand())) {
					VersionsDeletedFileRow row = dataModel.getObject(se.getIndex());
					doConfirmDelete(ureq, Collections.singletonList(row));
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
	
	private void doConfirmDelete(UserRequest ureq) {
		Set<Integer> selectedIndexes = tableEl.getMultiSelectedIndex();
		List<VersionsDeletedFileRow> rowsToDelete = new ArrayList<>(selectedIndexes.size());
		for(Integer selectedIndex:selectedIndexes) {
			VersionsDeletedFileRow row = dataModel.getObject(selectedIndex.intValue());
			if(row != null) {
				rowsToDelete.add(row);
			}
		}
		if(!rowsToDelete.isEmpty()) {
			doConfirmDelete(ureq, rowsToDelete);
		}
	}
	
	private void doDelete(List<VersionsDeletedFileRow> rowsToDelete) {
		for(VersionsDeletedFileRow row:rowsToDelete) {
			VFSRevision revision = vfsRepositoryService.getRevision(new VFSRevisionRefImpl(row.getRevisionKey()));
			vfsRepositoryService.deleteRevisions(getIdentity(), Collections.singletonList(revision));
		}
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
}