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

import org.apache.commons.lang3.tuple.Pair;
import org.olat.core.commons.modules.singlepage.SinglePageController;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.doceditor.ui.DocEditorController;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSTranscodingService;
import org.olat.core.commons.services.vfs.manager.VFSTranscodingDoneEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.avrecorder.AVVideoQuality;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.id.Roles;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.io.SystemFileFilter;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.ui.component.ModeCellRenderer;
import org.olat.course.nodes.gta.ui.events.SubmitEvent;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.audiovideorecording.AVModule;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.olat.course.nodes.gta.ui.GTAUIFactory.getOpenMode;
import static org.olat.course.nodes.gta.ui.GTAUIFactory.htmlOffice;

/**
 * 
 * Initial date: 25.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class SubmitDocumentsController extends FormBasicController implements GenericEventListener {
	
	private DocumentTableModel model;
	private FlexiTableElement tableEl;
	private FormLink uploadDocButton;
	private FormLink createDocButton;
	private FormLink copyDocButton;
	private FormLink recordVideoButton;
	private FormLink recordAudioButton;

	private CloseableModalController cmc;
	private NewDocumentController newDocCtrl;
	private CopyDocumentController copyDocCtrl;
	private DocumentUploadController uploadCtrl;
	private DocumentUploadController replaceCtrl;
	private DialogBoxController confirmDeleteCtrl;
	private SinglePageController viewDocCtrl;
	private AVSubmissionController avSubmissionController;

	private final int minDocs;
	private final int maxDocs;
	private final String docI18nKey;
	protected Task assignedTask;
	private final File documentsDir;
	private final VFSContainer documentsContainer;
	private final VFSContainer copySourceContainer;
	private final String copyEnding;
	private final String copyI18nKey;
	protected final ModuleConfiguration config;
	protected final GTACourseNode gtaNode;
	protected final CourseEnvironment courseEnv;
	
	private boolean open = true;
	private final boolean readOnly;
	private final boolean externalEditor;
	private final boolean embeddedEditor;
	private final Date deadline;
	private Roles roles;

	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private DocEditorService docEditorService;
	@Autowired
	private AVModule avModule;
	@Autowired
	private VFSTranscodingService transcodingService;

	public SubmitDocumentsController(UserRequest ureq, WindowControl wControl, Task assignedTask, File documentsDir,
			VFSContainer documentsContainer, int minDocs, int maxDocs, GTACourseNode cNode, CourseEnvironment courseEnv,
			boolean readOnly, boolean externalEditor, boolean embeddedEditor, Date deadline, String docI18nKey,
			VFSContainer copySourceContainer, String copyEnding, String copyI18nKey) {
		super(ureq, wControl, "documents", Util.createPackageTranslator(DocEditorController.class, ureq.getLocale()));
		this.assignedTask = assignedTask;
		this.documentsDir = documentsDir;
		this.documentsContainer = documentsContainer;
		this.copySourceContainer = copySourceContainer;
		this.minDocs = minDocs;
		this.maxDocs = maxDocs;
		this.docI18nKey = docI18nKey;
		this.deadline = deadline;
		this.readOnly = readOnly;
		this.externalEditor = externalEditor;
		this.embeddedEditor = embeddedEditor;
		this.copyEnding = copyEnding;
		this.copyI18nKey = copyI18nKey;
		this.config = cNode.getModuleConfiguration();
		this.gtaNode = cNode;
		this.courseEnv = courseEnv;
		initForm(ureq);
		updateModel(ureq);

		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, null, VFSTranscodingService.ores);
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
	
	protected boolean isReadOnly() {
		return readOnly;
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(externalEditor) {
			uploadDocButton = uifactory.addFormLink("upload.document", formLayout, Link.BUTTON);
			uploadDocButton.setIconLeftCSS("o_icon o_icon_upload");
			uploadDocButton.setElementCssClass("o_sel_course_gta_submit_file");
			uploadDocButton.setVisible(!readOnly);
		}
		if(embeddedEditor) {
			createDocButton = uifactory.addFormLink("open.editor", formLayout, Link.BUTTON);
			createDocButton.setIconLeftCSS("o_icon o_icon_edit");
			createDocButton.setElementCssClass("o_sel_course_gta_create_doc");
			createDocButton.setI18nKey(docI18nKey + ".open.editor");
			createDocButton.setVisible(!readOnly);
			
			copyDocButton = uifactory.addFormLink("copy.document", formLayout, Link.BUTTON);
			copyDocButton.setIconLeftCSS("o_icon o_icon_copy");
			copyDocButton.setElementCssClass("o_sel_course_gta_copy_file");
			copyDocButton.setI18nKey(copyI18nKey);
			copyDocButton.setVisible(!readOnly && canCopy(ureq));
		}

		if (config.getBooleanSafe(GTACourseNode.GTASK_ALLOW_VIDEO_RECORDINGS)) {
			recordVideoButton = uifactory.addFormLink("av.record.video", formLayout, Link.BUTTON);
			recordVideoButton.setIconLeftCSS("o_icon o_icon_video_record");
			recordVideoButton.setElementCssClass("o_sel_course_gta_record_video");
			recordVideoButton.setVisible(!readOnly && avModule.isVideoRecordingEnabled());
		}

		if (config.getBooleanSafe(GTACourseNode.GTASK_ALLOW_AUDIO_RECORDINGS)) {
			recordAudioButton = uifactory.addFormLink("av.record.audio", formLayout, Link.BUTTON);
			recordAudioButton.setIconLeftCSS("o_icon o_icon_audio_record");
			recordAudioButton.setElementCssClass("o_sel_course_gta_record_audio");
			recordAudioButton.setVisible(!readOnly && avModule.isAudioRecordingEnabled());
		}

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(docI18nKey, DocCols.document.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DocCols.date.i18nKey(), DocCols.date.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DocCols.createdBy.i18nKey(), DocCols.createdBy.ordinal()));
		
		if (embeddedEditor) {
			String openI18n = "table.header.view";
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(openI18n, DocCols.mode.ordinal(), "open", new ModeCellRenderer("open", docEditorService)));
		}
		if(!readOnly) {
			if(externalEditor) {
				DefaultFlexiColumnModel replaceCol = new DefaultFlexiColumnModel("table.header.replace.doc", translate("table.header.metadata"), "replace");
				replaceCol.setIconHeader("o_icon o_icon_edit_metadata o_icon-lg");
				replaceCol.setHeaderTooltip("table.header.replace.doc");
				columnsModel.addFlexiColumnModel(replaceCol);
			}
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.delete", translate("table.header.delete"), "delete"));
		}
		
		model = new DocumentTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, getTranslator(), formLayout);
		formLayout.add("table", tableEl);
		// configure table to be as slim as possible
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setElementCssClass("o_table_no_margin");
	}
	
	private boolean canCopy(UserRequest ureq) {
		if (copySourceContainer == null) return false;
		
		// Copy is only allowed if the document can be edited with a document editor.
		// If you want change this, you can't open the copied document in a new window directly after copy.
		Collection<String> copySuffixes = GTAUIFactory.getCopySuffix(getIdentity(), ureq.getUserSession().getRoles());
		if (copySuffixes.isEmpty()) return false;
		
		for (VFSItem vfsItem : copySourceContainer.getItems()) {
			String suffix = FileUtils.getFileSuffix(vfsItem.getName()).toLowerCase();
			if (copySuffixes.contains(suffix) && vfsItem instanceof VFSLeaf) {
				return true;
			}
		}
		
		return false;
	}

	private void updateModel(UserRequest ureq) {
		if (ureq != null) {
			roles = ureq.getUserSession().getRoles();
		}
		File[] documents = documentsDir.listFiles(SystemFileFilter.FILES_ONLY);
		if(documents == null) {
			documents = new File[0];
		}
		List<SubmittedSolution> docList = new ArrayList<>(documents.length);
		for(File document:documents) {
			String filename = document.getName();
			String createdBy = null;
			Mode openMode = null;
			boolean inTranscoding = false;

			VFSItem item = documentsContainer.resolve(filename);
			if(item != null && item.canMeta() == VFSConstants.YES) {
				VFSMetadata metaInfo = item.getMetaInfo();
				if(metaInfo != null) {
					createdBy = userManager.getUserDisplayName(metaInfo.getFileInitializedBy());
					inTranscoding = metaInfo.isInTranscoding();
				}
			}
			
			FormItem download;
			if(filename.endsWith(".html")) {
				String iconFilename = "<i class=\"o_icon o_icon-fw o_filetype_file o_filetype_html\"></i> " + filename;
				download = uifactory.addFormLink("view-" + CodeHelper.getRAMUniqueID(), "view", iconFilename, null, flc, Link.LINK | Link.NONTRANSLATED);
				download.setUserObject(filename);
			} else {
				download = uifactory.addDownloadLink("view-" + CodeHelper.getRAMUniqueID(), filename, null, document, tableEl);
			}
			
			if(item instanceof VFSLeaf) {
				VFSLeaf vfsLeaf = (VFSLeaf)item;
				openMode = roles == null ? null : getOpenMode(getIdentity(), roles, vfsLeaf, readOnly);
			}
			docList.add(new SubmittedSolution(document, createdBy, download, openMode, inTranscoding));
		}
		model.setObjects(docList);
		tableEl.reset();
		updateWarnings();
		
		
		flc.contextPut("hasDocuments", Boolean.valueOf(hasUploadDocuments()));
	}
	
	private void updateWarnings() {
		if(minDocs > 0 && model.getRowCount() < minDocs) {
			String msg = translate("error.min.documents", new String[]{ Integer.toString(minDocs) });
			if(uploadDocButton != null) {
				uploadDocButton.setEnabled(true);
			}
			if(createDocButton != null) {
				createDocButton.setEnabled(true);
			}
			flc.contextPut("minDocsWarning", msg);
			flc.contextRemove("maxDocsWarning");
		} else if(maxDocs > 0 && model.getRowCount() >= maxDocs) {
			if(uploadDocButton != null) {
				uploadDocButton.setEnabled(false);
			}
			if(createDocButton != null) {
				createDocButton.setEnabled(false);
			}
			if(copyDocButton != null) {
				copyDocButton.setEnabled(false);
			}
			String msg = translate("error.max.documents", new String[]{ Integer.toString(maxDocs)});
			flc.contextPut("maxDocsWarning", msg);
			flc.contextRemove("minDocsWarning");
		} else {
			if(uploadDocButton != null) {
				uploadDocButton.setEnabled(true);
			}
			if(createDocButton != null) {
				createDocButton.setEnabled(true);
			}
			if(copyDocButton != null) {
				copyDocButton.setEnabled(true);
			}
			flc.contextRemove("maxDocsWarning");
			flc.contextRemove("minDocsWarning");
		}
	}

	@Override
	public void event(Event event) {
		if (event instanceof VFSTranscodingDoneEvent) {
			VFSTranscodingDoneEvent doneEvent = (VFSTranscodingDoneEvent) event;
			if (model.getObjects().stream().anyMatch(s -> doneEvent.getFileName().equals(s.getFile().getName()))) {
				updateModel(null);
			}
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(confirmDeleteCtrl == source) {
			if(DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				SubmittedSolution document = (SubmittedSolution)confirmDeleteCtrl.getUserObject();
				String filename = document.getFile().getName();
				doDelete(ureq, document);
				fireEvent(ureq, new SubmitEvent(SubmitEvent.DELETE, filename));
				gtaManager.markNews(courseEnv, gtaNode);
			}
			cleanUp();
			checkDeadline(ureq);
		} else if(uploadCtrl == source) {
			if (event == Event.DONE_EVENT) {
				String filename = uploadCtrl.getUploadedFilename();
				doUpload(ureq, uploadCtrl.getUploadedFile(), filename);
				fireEvent(ureq, new SubmitEvent(SubmitEvent.UPLOAD, filename));
				gtaManager.markNews(courseEnv, gtaNode);
			}
			cmc.deactivate();
			cleanUp();
			checkDeadline(ureq);
		} else if (avSubmissionController == source) {
			if (event instanceof AVDoneEvent) {
				updateModel(ureq);
				updateWarnings();
				String fileName = ((AVDoneEvent)event).getRecording().getName();
				fireEvent(ureq, new SubmitEvent(SubmitEvent.UPLOAD, fileName));
				gtaManager.markNews(courseEnv, gtaNode);
			}
			cmc.deactivate();
			cleanUp();
			checkDeadline(ureq);
		} else if(replaceCtrl == source) {
			if(event == Event.DONE_EVENT) {
				if (replaceCtrl.getUploadedFile() != null) {
					String filename = replaceCtrl.getUploadedFilename();
					doReplace(ureq, replaceCtrl.getSolution(), replaceCtrl.getUploadedFile(), filename);
					fireEvent(ureq, new SubmitEvent(SubmitEvent.UPDATE, filename));
					gtaManager.markNews(courseEnv, gtaNode);
				}
			}
			cmc.deactivate();
			cleanUp();
			checkDeadline(ureq);
		} else if(newDocCtrl == source) {
			String filename = newDocCtrl.getFilename();
			cmc.deactivate();
			cleanUp();
			if(event == Event.DONE_EVENT) {
				fireEvent(ureq, new SubmitEvent(SubmitEvent.CREATE, filename));
				gtaManager.markNews(courseEnv, gtaNode);
				updateModel(ureq);
				updateWarnings();
			} 
			checkDeadline(ureq);
		} else if(copyDocCtrl == source) {
			String filename = copyDocCtrl.getFilename();
			cmc.deactivate();
			cleanUp();
			if(event == Event.DONE_EVENT) {
				fireEvent(ureq, new SubmitEvent(SubmitEvent.CREATE, filename));
				gtaManager.markNews(courseEnv, gtaNode);
				updateModel(ureq);
				updateWarnings();
			} 
			checkDeadline(ureq);
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmDeleteCtrl);
		removeAsListenerAndDispose(viewDocCtrl);
		removeAsListenerAndDispose(copyDocCtrl);
		removeAsListenerAndDispose(uploadCtrl);
		removeAsListenerAndDispose(newDocCtrl);
		removeAsListenerAndDispose(avSubmissionController);
		removeAsListenerAndDispose(cmc);
		confirmDeleteCtrl = null;
		viewDocCtrl = null;
		copyDocCtrl = null;
		uploadCtrl = null;
		newDocCtrl = null;
		avSubmissionController = null;
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
				doCreateDocument(ureq);
			}
		} else if(copyDocButton == source) {
			if (checkOpen(ureq) && checkDeadline(ureq)) {
				doCopyDocument(ureq);
			}
		} else if (recordVideoButton == source) {
			if (checkOpen(ureq) && checkDeadline(ureq)) {
				doRecordVideo(ureq);
			}
		} else if (recordAudioButton == source) {
			if (checkOpen(ureq) && checkDeadline(ureq)) {
				doRecordAudio(ureq);
			}
		} else if(tableEl == source) {
			if(checkOpen(ureq) && checkDeadline(ureq) && event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				SubmittedSolution row = model.getObject(se.getIndex());
				if("delete".equals(se.getCommand())) {
					doConfirmDelete(ureq, row);
				} else if("open".equals(se.getCommand())) {
					String filename = row.getFile().getName();
					Mode mode = row.getMode();
					doOpen(ureq, filename, mode);
				} else if("replace".equals(se.getCommand())) {
					doReplaceDocument(ureq, row);
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
		if(guardModalController(viewDocCtrl)) return;
		
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

	private void doDelete(UserRequest ureq, SubmittedSolution solution) {
		File document = solution.getFile();
		VFSItem item = documentsContainer.resolve(document.getName());
		transcodingService.deleteMasterFile(item);
		FileUtils.deleteFile(document);
		updateModel(ureq);
		updateWarnings();
	}
	
	private void doOpen(UserRequest ureq, String filename, Mode mode) {
		gtaManager.markNews(courseEnv, gtaNode);
		updateWarnings();
		checkDeadline(ureq);
		VFSItem vfsItem = documentsContainer.resolve(filename);
		if(vfsItem == null || !(vfsItem instanceof VFSLeaf)) {
			showError("error.missing.file");
		} else {
			VFSLeaf vfsLeaf = (VFSLeaf)vfsItem;
			if (vfsLeaf.canMeta() == VFSConstants.YES && vfsLeaf.getMetaInfo() != null && vfsLeaf.getMetaInfo().isInTranscoding()) {
				return;
			}
			fireEvent(ureq, new SubmitEvent(SubmitEvent.UPDATE, vfsLeaf.getName()));
			DocEditorConfigs configs = GTAUIFactory.getEditorConfig(documentsContainer, vfsLeaf, filename, mode, null);
			String url = docEditorService.prepareDocumentUrl(ureq.getUserSession(), configs);
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(url));
		}
	}
	
	private void doReplaceDocument(UserRequest ureq, SubmittedSolution row) {
		replaceCtrl = new DocumentUploadController(ureq, getWindowControl(), row, row.getFile(), documentsContainer);
		listenTo(replaceCtrl);

		String title = translate("replace.document");
		cmc = new CloseableModalController(getWindowControl(), null, replaceCtrl.getInitialComponent(), true, title, false);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doReplace(UserRequest ureq, SubmittedSolution solution, File file, String filename) {
		File document = solution.getFile();
		FileUtils.deleteFile(document);
		doUpload(ureq, file, filename);
	}
	
	private void doUpload(UserRequest ureq, File file, String filename) {
		VFSItem target = documentsContainer.resolve(filename);
		
		if(target != null) {
			target.deleteSilently();
		}
		
		target = documentsContainer.createChildLeaf(filename);
		VFSManager.copyContent(file, (VFSLeaf)target, getIdentity());
		
		updateModel(ureq);
		updateWarnings();
	}
	
	private void doOpenDocumentUpload(UserRequest ureq) {
		if(guardModalController(uploadCtrl)) return;
		
		if(maxDocs > 0 && maxDocs <= model.getRowCount()) {
			showWarning("error.max.documents");
		} else {
			uploadCtrl = new DocumentUploadController(ureq, getWindowControl(), documentsContainer);
			listenTo(uploadCtrl);
	
			String title = translate("upload.document");
			cmc = new CloseableModalController(getWindowControl(), null, uploadCtrl.getInitialComponent(), true, title, false);
			listenTo(cmc);
			cmc.activate();
		}
	}

	private void doRecordVideo(UserRequest ureq) {
		long recordingLengthLimit = 1000 * Long.parseLong(config.getStringValue(GTACourseNode.GTASK_MAX_VIDEO_DURATION, "600"));
		avSubmissionController = new AVSubmissionController(ureq, getWindowControl(), documentsContainer, false,
				recordingLengthLimit, AVVideoQuality.valueOf(config.getStringValue(GTACourseNode.GTASK_VIDEO_QUALITY, AVVideoQuality.medium.name())));
		listenTo(avSubmissionController);

		String title = translate("av.record.video");
		cmc = new CloseableModalController(getWindowControl(), "close", avSubmissionController.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doRecordAudio(UserRequest ureq) {
		long recordingLengthLimit = 1000 * Long.parseLong(config.getStringValue(GTACourseNode.GTASK_MAX_AUDIO_DURATION, "600"));
		avSubmissionController = new AVSubmissionController(ureq, getWindowControl(), documentsContainer, true,
				recordingLengthLimit, null);
		listenTo(avSubmissionController);

		String title = translate("av.record.audio");
		cmc = new CloseableModalController(getWindowControl(), "close", avSubmissionController.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doCreateDocument(UserRequest ureq) {
		if(newDocCtrl != null) return;
		
		if(maxDocs > 0 && maxDocs <= model.getRowCount()) {
			showWarning("error.max.documents");
		} else {
			newDocCtrl = new NewDocumentController(ureq, getWindowControl(), documentsContainer,
					htmlOffice(getIdentity(), ureq.getUserSession().getRoles(), getLocale()));
			listenTo(newDocCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), "close", newDocCtrl.getInitialComponent(),
					translate(createDocButton.getI18nKey()));
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doCopyDocument(UserRequest ureq) {
		if(copyDocCtrl != null) return;
		
		if(maxDocs > 0 && maxDocs <= model.getRowCount()) {
			showWarning("error.max.documents");
		} else {
			copyDocCtrl = new CopyDocumentController(ureq, getWindowControl(), copySourceContainer, documentsContainer,
					copyEnding);
			listenTo(copyDocCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), "close", copyDocCtrl.getInitialComponent(),
					translate(copyDocButton.getI18nKey()));
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	public enum DocCols {
		document("document"),
		date("document.date"),
		createdBy("table.header.created.by"),
		mode("edit");
		
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
		private final String createdBy;
		private final FormItem downloadLink;
		private final Mode mode;
		private final boolean inTranscoding;
		
		public SubmittedSolution(File file, String createdBy, FormItem downloadLink, Mode mode, boolean inTranscoding) {
			this.file = file;
			this.createdBy = createdBy;
			this.downloadLink = downloadLink;
			this.mode = mode;
			this.inTranscoding = inTranscoding;
		}

		public File getFile() {
			return file;
		}

		public String getCreatedBy() {
			return createdBy;
		}

		public FormItem getDownloadLink() {
			return downloadLink;
		}

		public Mode getMode() {
			return mode;
		}

		public boolean isInTranscoding() {
			return inTranscoding;
		}
	}
	
	private static class DocumentTableModel extends DefaultFlexiTableDataModel<SubmittedSolution>  {
		
		public DocumentTableModel(FlexiTableColumnModel columnModel) {
			super(columnModel);
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
				case createdBy: return solution.getCreatedBy();
				case mode: return solution.isInTranscoding() ?
						GTAManager.BUSY_VALUE : Pair.of(solution.getMode(), solution.getFile().getName());
				default: return "ERROR";
			}
		}
	}

	@Override
	protected void doDispose() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, VFSTranscodingService.ores);
		super.doDispose();
	}
}