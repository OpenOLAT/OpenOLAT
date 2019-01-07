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
package org.olat.course.nodes.gta.ui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.editor.htmleditor.HTMLEditorController;
import org.olat.core.commons.editor.htmleditor.WysiwygFactory;
import org.olat.core.commons.modules.singlepage.SinglePageController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.io.SystemFileFilter;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.meta.MetaInfo;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.ui.events.SubmitEvent;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class SubmitDocumentsController extends FormBasicController {
	
	private DocumentTableModel model;
	private FlexiTableElement tableEl;
	private FormLink uploadDocButton, createDocButton;

	private CloseableModalController cmc;
	private NewDocumentController newDocCtrl;
	private DocumentUploadController uploadCtrl, replaceCtrl;
	private DialogBoxController confirmDeleteCtrl;
	private SinglePageController viewDocCtrl;
	private HTMLEditorController newDocumentEditorCtrl, editDocumentEditorCtrl;
	
	private final int maxDocs;
	private final String docI18nKey;
	private final Task assignedTask;
	private final File documentsDir;
	private final VFSContainer documentsContainer;
	private final ModuleConfiguration config;
	private final GTACourseNode gtaNode;
	private final CourseEnvironment courseEnv;
	
	private boolean open = true;
	private final boolean readOnly;
	private final Date deadline;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private GTAManager gtaManager;
	
	public SubmitDocumentsController(UserRequest ureq, WindowControl wControl, Task assignedTask,
			File documentsDir, VFSContainer documentsContainer, int maxDocs, GTACourseNode cNode,
			CourseEnvironment courseEnv, boolean readOnly, Date deadline, String docI18nKey) {
		super(ureq, wControl, "documents");
		this.assignedTask = assignedTask;
		this.documentsDir = documentsDir;
		this.documentsContainer = documentsContainer;
		this.maxDocs = maxDocs;
		this.docI18nKey = docI18nKey;
		this.deadline = deadline;
		this.readOnly = readOnly;
		this.config = cNode.getModuleConfiguration();
		this.gtaNode = cNode;
		this.courseEnv = courseEnv;
		initForm(ureq);
		updateModel();
	}

	public Task getAssignedTask() {
		return assignedTask;
	}

	public boolean hasUploadDocuments() {
		return (model.getRowCount() > 0);
	}
	
	public void close() {
		open = false;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(config.getBooleanSafe(GTACourseNode.GTASK_EXTERNAL_EDITOR)) {
			uploadDocButton = uifactory.addFormLink("upload.document", formLayout, Link.BUTTON);
			uploadDocButton.setIconLeftCSS("o_icon o_icon_upload");
			uploadDocButton.setElementCssClass("o_sel_course_gta_submit_file");
			uploadDocButton.setVisible(!readOnly);
		}
		if(config.getBooleanSafe(GTACourseNode.GTASK_EMBBEDED_EDITOR)) {
			createDocButton = uifactory.addFormLink("open.editor", formLayout, Link.BUTTON);
			createDocButton.setIconLeftCSS("o_icon o_icon_edit");
			createDocButton.setElementCssClass("o_sel_course_gta_create_doc");
			createDocButton.setI18nKey(docI18nKey + ".open.editor");
			createDocButton.setVisible(!readOnly);
		}

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(docI18nKey, DocCols.document.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DocCols.date.i18nKey(), DocCols.date.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DocCols.uploadedBy.i18nKey(), DocCols.uploadedBy.ordinal()));
		if(!readOnly) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", DocCols.edit.ordinal(), "edit",
					new BooleanCellRenderer(
							new StaticFlexiCellRenderer(translate("edit"), "edit"),
							new StaticFlexiCellRenderer(translate("replace"), "edit"))));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", translate("delete"), "delete"));
		}
		
		model = new DocumentTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, getTranslator(), formLayout);
		formLayout.add("table", tableEl);
		// configure table to be as slim as possible
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setElementCssClass("o_table_no_margin");
	}
	
	private void updateModel() {
		File[] documents = documentsDir.listFiles(new SystemFileFilter(true, false));
		List<SubmittedSolution> docList = new ArrayList<>(documents.length);
		for(File document:documents) {
			String filename = document.getName();
			String uploadedBy = null;
			VFSItem item = documentsContainer.resolve(filename);
			if(item.canMeta() == VFSConstants.YES) {
				MetaInfo metaInfo = item.getMetaInfo();
				if(metaInfo != null && metaInfo.getAuthorIdentityKey() != null) {
					uploadedBy = userManager.getUserDisplayName(metaInfo.getAuthorIdentityKey());
				}
			}
			
			FormItem download;
			if(filename.endsWith(".html")) {
				download = uifactory.addFormLink("view-" + CodeHelper.getRAMUniqueID(), "view", filename, null, flc, Link.LINK | Link.NONTRANSLATED);
				download.setUserObject(filename);
			} else {
				download = uifactory.addDownloadLink("view-" + CodeHelper.getRAMUniqueID(), filename, null, document, tableEl);
			}
			docList.add(new SubmittedSolution(document, uploadedBy, download));
		}
		model.setObjects(docList);
		tableEl.reset();
		
		if(maxDocs > 0 && docList.size() >= maxDocs) {
			if(uploadDocButton != null) {
				uploadDocButton.setEnabled(false);
			}
			if(createDocButton != null) {
				createDocButton.setEnabled(false);
			}
			String msg = translate("error.max.documents", new String[]{ Integer.toString(maxDocs)});
			flc.contextPut("maxDocsWarning", msg);
		} else {
			if(uploadDocButton != null) {
				uploadDocButton.setEnabled(true);
			}
			if(createDocButton != null) {
				createDocButton.setEnabled(true);
			}
			flc.contextPut("maxDocsWarning", Boolean.FALSE);
		}
		
		flc.contextPut("hasDocuments", Boolean.valueOf(hasUploadDocuments()));
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(confirmDeleteCtrl == source) {
			if(DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				SubmittedSolution document = (SubmittedSolution)confirmDeleteCtrl.getUserObject();
				String filename = document.getFile().getName();
				doDelete(document);
				fireEvent(ureq, new SubmitEvent(SubmitEvent.DELETE, filename));
				gtaManager.markNews(courseEnv, gtaNode);
			}
			cleanUp();
			checkDeadline(ureq);
		} else if(uploadCtrl == source) {
			if(event == Event.DONE_EVENT) {
				String filename = uploadCtrl.getUploadedFilename();
				doUpload(ureq, uploadCtrl.getUploadedFile(), filename);
				fireEvent(ureq, new SubmitEvent(SubmitEvent.UPLOAD, filename));
				gtaManager.markNews(courseEnv, gtaNode);
			}
			cmc.deactivate();
			cleanUp();
			checkDeadline(ureq);
		} else if(replaceCtrl == source) {
			if(event == Event.DONE_EVENT) {
				String filename = replaceCtrl.getUploadedFilename();
				doReplace(ureq, replaceCtrl.getSolution(), replaceCtrl.getUploadedFile(), filename);
				fireEvent(ureq, new SubmitEvent(SubmitEvent.UPDATE, filename));
				gtaManager.markNews(courseEnv, gtaNode);
			}
			cmc.deactivate();
			cleanUp();
			checkDeadline(ureq);
		} else if(newDocCtrl == source) {
			String filename = newDocCtrl.getFilename();
			cmc.deactivate();
			cleanUp();
			
			if(event == Event.DONE_EVENT) {
				doCreateDocumentEditor(ureq, filename);
				updateModel();
			} 
			checkDeadline(ureq);
		} else if(newDocumentEditorCtrl == source) {
			if(event == Event.DONE_EVENT) {
				updateModel();
				fireEvent(ureq, new SubmitEvent(SubmitEvent.CREATE, newDocumentEditorCtrl.getFilename()));
				gtaManager.markNews(courseEnv, gtaNode);
			}
			cmc.deactivate();
			cleanUp();
			checkDeadline(ureq);
		} else if(editDocumentEditorCtrl == source) {
			if(event == Event.DONE_EVENT) {
				updateModel();
				fireEvent(ureq, new SubmitEvent(SubmitEvent.UPDATE, editDocumentEditorCtrl.getFilename()));
				gtaManager.markNews(courseEnv, gtaNode);
			}
			cmc.deactivate();
			cleanUp();
			checkDeadline(ureq);
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(newDocumentEditorCtrl);
		removeAsListenerAndDispose(confirmDeleteCtrl);
		removeAsListenerAndDispose(viewDocCtrl);
		removeAsListenerAndDispose(uploadCtrl);
		removeAsListenerAndDispose(newDocCtrl);
		removeAsListenerAndDispose(cmc);
		newDocumentEditorCtrl = null;
		confirmDeleteCtrl = null;
		viewDocCtrl = null;
		uploadCtrl = null;
		newDocCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(uploadDocButton == source) {
			if(checkOpen(ureq) && checkDeadline(ureq)) {
				doOpenDocumentUpload(ureq);
			}
		} else if(createDocButton == source) {
			if(checkOpen(ureq) && checkDeadline(ureq)) {
				doChooseFilename(ureq);
			}
		} else if(tableEl == source) {
			if(checkOpen(ureq) && checkDeadline(ureq) && event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				SubmittedSolution row = model.getObject(se.getIndex());
				if("delete".equals(se.getCommand())) {
					doConfirmDelete(ureq, row);
				} else if("edit".equals(se.getCommand())) {
					doEdit(ureq, row);
				}
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("view".equals(link.getCmd())) {
				doView(ureq, (String)link.getUserObject());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private boolean checkDeadline(UserRequest ureq) {
		if(deadline == null || deadline.after(new Date())) return true;
		showWarning("warning.tasks.submitted");
		fireEvent(ureq, Event.DONE_EVENT);
		return false;
	}
	
	private boolean checkOpen(UserRequest ureq) {
		if(open) return true;
		showWarning("warning.tasks.submitted");
		fireEvent(ureq, Event.DONE_EVENT);
		return false;
	}
	
	private void doView(UserRequest ureq, String filename) {
		if(viewDocCtrl != null) return;
		
		viewDocCtrl = new SinglePageController(ureq, getWindowControl(), documentsContainer, filename, false);
		listenTo(viewDocCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), viewDocCtrl.getInitialComponent(), true, filename);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmDelete(UserRequest ureq, SubmittedSolution solution) {
		String title = translate("confirm.delete.solution.title");
		String text = translate("confirm.delete.solution.description", new String[]{ solution.getFile().getName() });
		confirmDeleteCtrl = activateYesNoDialog(ureq, title, text, confirmDeleteCtrl);
		confirmDeleteCtrl.setUserObject(solution);
	}

	private void doDelete(SubmittedSolution solution) {
		File document = solution.getFile();
		if(document.exists()) {
			document.delete();
		}
		updateModel();
	}
	
	private void doEdit(UserRequest ureq, SubmittedSolution row) {
		if(row.getFile().getName().endsWith(".html")) {
			doEditDocumentEditor(ureq, row);
		} else {
			doReplaceDocument(ureq, row);
		}
	}
	
	private void doReplaceDocument(UserRequest ureq, SubmittedSolution row) {
		replaceCtrl = new DocumentUploadController(ureq, getWindowControl(), row, row.getFile());
		listenTo(replaceCtrl);

		String title = translate("replace.document");
		cmc = new CloseableModalController(getWindowControl(), null, replaceCtrl.getInitialComponent(), true, title, false);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doReplace(UserRequest ureq, SubmittedSolution solution, File file, String filename) {
		File document = solution.getFile();
		if(document.exists()) {
			document.delete();
		}
		doUpload(ureq, file, filename);
	}
	
	private void doUpload(UserRequest ureq, File file, String filename) {
		try {
			Path documentPath = documentsDir.toPath().resolve(filename);
			Files.move(file.toPath(), documentPath, StandardCopyOption.REPLACE_EXISTING);
			
			VFSItem downloadedFile = documentsContainer.resolve(filename);
			if(downloadedFile != null && downloadedFile.canMeta() == VFSConstants.YES) {
				MetaInfo  metadata = downloadedFile.getMetaInfo();
				metadata.setAuthor(ureq.getIdentity());
				metadata.write();
			}
		} catch (IOException e) {
			logError("", e);
			showError("");
		}
		updateModel();
	}
	
	private void doOpenDocumentUpload(UserRequest ureq) {
		if(uploadCtrl != null) return;
		
		if(maxDocs > 0 && maxDocs <= model.getRowCount()) {
			showWarning("error.max.documents");
		} else {
			uploadCtrl = new DocumentUploadController(ureq, getWindowControl());
			listenTo(uploadCtrl);
	
			String title = translate("upload.document");
			cmc = new CloseableModalController(getWindowControl(), null, uploadCtrl.getInitialComponent(), true, title, false);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doChooseFilename(UserRequest ureq) {
		if(newDocCtrl != null) return;
		
		if(maxDocs > 0 && maxDocs <= model.getRowCount()) {
			showWarning("error.max.documents");
		} else {
			newDocCtrl = new NewDocumentController(ureq, getWindowControl(), documentsContainer);
			listenTo(newDocCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), "close", newDocCtrl.getInitialComponent());
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doCreateDocumentEditor(UserRequest ureq, String documentName) {
		if(newDocumentEditorCtrl != null) return;
		
		if(maxDocs > 0 && maxDocs <= model.getRowCount()) {
			showWarning("error.max.documents");
		} else {
			VFSItem item = documentsContainer.resolve(documentName);
			if(item == null) {
				documentsContainer.createChildLeaf(documentName);
			} else {
				documentName = VFSManager.rename(documentsContainer, documentName);
				documentsContainer.createChildLeaf(documentName);
			}
			// add missing identity in meta info
			item = documentsContainer.resolve(documentName);
			if(item != null && item.canMeta() == VFSConstants.YES) {
				MetaInfo  metadata = item.getMetaInfo();
				metadata.setAuthor(ureq.getIdentity());
				metadata.write();
			}				
	
			newDocumentEditorCtrl = WysiwygFactory.createWysiwygController(ureq, getWindowControl(),
					documentsContainer, documentName, "media", true, true);
			newDocumentEditorCtrl.getRichTextConfiguration().disableMedia();
			newDocumentEditorCtrl.getRichTextConfiguration().setAllowCustomMediaFactory(false);
			newDocumentEditorCtrl.setNewFile(true);
			listenTo(newDocumentEditorCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), "close", newDocumentEditorCtrl.getInitialComponent());
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doEditDocumentEditor(UserRequest ureq, SubmittedSolution row) {
		String documentName = row.getFile().getName();

		editDocumentEditorCtrl = WysiwygFactory.createWysiwygController(ureq, getWindowControl(),
				documentsContainer, documentName, "media", true, true);
		editDocumentEditorCtrl.getRichTextConfiguration().disableMedia();
		editDocumentEditorCtrl.getRichTextConfiguration().setAllowCustomMediaFactory(false);
		listenTo(editDocumentEditorCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", editDocumentEditorCtrl.getInitialComponent());
		listenTo(cmc);
		cmc.activate();
	}
	
	public enum DocCols {
		document("document"),
		date("document.date"),
		uploadedBy("table.header.uploaded.by"),
		edit("edit");
		
		private final String i18nKey;
	
		private DocCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String i18nKey() {
			return i18nKey;
		}
	}
	
	public static class SubmittedSolution {
		
		private final File file;
		private final String uploadedBy;
		private final FormItem downloadLink;
		
		public SubmittedSolution(File file, String uploadedBy, FormItem downloadLink) {
			this.file = file;
			this.uploadedBy = uploadedBy;
			this.downloadLink = downloadLink;
		}

		public File getFile() {
			return file;
		}

		public String getUploadedBy() {
			return uploadedBy;
		}

		public FormItem getDownloadLink() {
			return downloadLink;
		}
	}
	
	private static class DocumentTableModel extends DefaultFlexiTableDataModel<SubmittedSolution>  {
		
		public DocumentTableModel(FlexiTableColumnModel columnModel) {
			super(columnModel);
		}

		@Override
		public DefaultFlexiTableDataModel<SubmittedSolution> createCopyWithEmptyList() {
			return new DocumentTableModel(getTableColumnModel());
		}

		@Override
		public Object getValueAt(int row, int col) {
			SubmittedSolution solution = getObject(row);
			switch(DocCols.values()[col]) {
				case document: return solution.getDownloadLink();
				case date: {
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(solution.getFile().lastModified());
					return cal.getTime();
				}
				case uploadedBy: return solution.getUploadedBy();
				case edit: return solution.getFile().getName().endsWith(".html");
				default: return "ERROR";
			}
		}
	}
}