/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.gta.ui;

import static org.olat.course.nodes.gta.ui.GTAUIFactory.officeHtml;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.modules.singlepage.SinglePageController;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorDisplayInfo;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.doceditor.ui.DocEditorController;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSTranscodingService;
import org.olat.core.commons.services.vfs.manager.VFSTranscodingDoneEvent;
import org.olat.core.commons.services.video.ui.VideoAudioPlayerController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.avrecorder.AVVideoQuality;
import org.olat.core.gui.components.Component;
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
import org.olat.core.gui.util.CSSHelper;
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
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.ui.events.SubmitEvent;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.audiovideorecording.AVModule;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
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
	private CloseableCalloutWindowController ccwc;
	private AVConvertingMenuController avConvertingMenuCtrl;
	private VideoAudioPlayerController videoAudioPlayerController;
	private Controller docEditorCtrl;
	private ToolsController toolsCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;


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
		DefaultFlexiColumnModel downloadCol = new DefaultFlexiColumnModel(DocCols.download.i18nKey, DocCols.download.ordinal());
		columnsModel.addFlexiColumnModel(downloadCol);

		if (!readOnly) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DocCols.toolsLink.i18nKey, DocCols.toolsLink.ordinal()));
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
			FormLink openLink = null;
			FormLink documentLink = null;
			boolean inTranscoding = false;

			FormLink downloadLink;
			downloadLink = uifactory.addFormLink("view-" + CodeHelper.getRAMUniqueID(), "download", "table.header.download", null, flc, Link.LINK);

			VFSItem item = documentsContainer.resolve(filename);
			downloadLink.setUserObject(item);
			if(item instanceof VFSLeaf vfsLeaf && item.canMeta() == VFSConstants.YES) {
				VFSMetadata metaInfo = item.getMetaInfo();
				if(metaInfo != null) {
					createdBy = userManager.getUserDisplayName(metaInfo.getFileInitializedBy());
					inTranscoding = metaInfo.isInTranscoding();
				}

				DocEditorDisplayInfo editorInfo = docEditorService.getEditorInfo(getIdentity(), roles, vfsLeaf,
						metaInfo, true, DocEditorService.modesEditView(!readOnly));
				String iconFilename = "<i class=\"o_icon o_icon-fw " + CSSHelper.createFiletypeIconCssClassFor(filename) + "\"></i> " + filename;
				if (inTranscoding) {
					openLink = uifactory.addFormLink("transcoding_" + CodeHelper.getRAMUniqueID(), "transcoding", "av.converting", null, flc, Link.LINK);
					openLink.setUserObject(filename);
					documentLink = uifactory.addFormLink("transcoding_" + CodeHelper.getRAMUniqueID(), "transcoding", "av.converting", null, flc, Link.LINK);
					documentLink.setUserObject(filename);
				} else {
					if (editorInfo.isEditorAvailable()
							&&
							(embeddedEditor
							|| filename.endsWith(".drawio")
							|| filename.endsWith(".dwb")
							|| editorInfo.getMode().equals(Mode.VIEW))) {
						openLink = uifactory.addFormLink("open_" + CodeHelper.getRAMUniqueID(), "open", iconFilename, null, flc, Link.NONTRANSLATED);
						documentLink = uifactory.addFormLink("open_" + CodeHelper.getRAMUniqueID(), "open", iconFilename, null, flc, Link.NONTRANSLATED);
						if (editorInfo.isNewWindow()) {
							openLink.setNewWindow(true, true, false);
							documentLink.setNewWindow(true, true, false);
						}
					} else {
						openLink = uifactory.addFormLink("download_" + CodeHelper.getRAMUniqueID(), "download", iconFilename, null, flc, Link.NONTRANSLATED);
						documentLink = uifactory.addFormLink("download_" + CodeHelper.getRAMUniqueID(), "download", iconFilename, null, flc, Link.NONTRANSLATED);
					}
					openLink.setUserObject(vfsLeaf);
					documentLink.setUserObject(vfsLeaf);
				}
				openLink.setI18nKey(editorInfo.getModeButtonLabel(getTranslator()));
				openLink.setIconLeftCSS("o_icon o_icon-fw " + editorInfo.getModeIcon());
			}
			FormLink toolsLink = uifactory.addFormLink("tools_" + CodeHelper.getRAMUniqueID(), "tools", translate("table.header.action"), null, null, Link.NONTRANSLATED);
			docList.add(new SubmittedSolution(document, createdBy, downloadLink, openLink, documentLink, toolsLink, inTranscoding));
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
		if (event instanceof VFSTranscodingDoneEvent doneEvent) {
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
			if (event instanceof AVDoneEvent avDoneEvent) {
				updateModel(ureq);
				updateWarnings();
				String fileName = avDoneEvent.getRecording().getName();
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
		} else if(docEditorCtrl == source) {
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		} else if (ccwc == source) {
			cleanUp();
		} else if (avConvertingMenuCtrl == source) {
			if (event == AVConvertingMenuController.PLAY_MASTER_EVENT) {
				String fileName = (String) avConvertingMenuCtrl.getUserObject();
				ccwc.deactivate();
				cleanUp();
				doPlayMaster(ureq, fileName);
			}
		} else if (videoAudioPlayerController == source) {
			cmc.deactivate();
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
		removeAsListenerAndDispose(avConvertingMenuCtrl);
		removeAsListenerAndDispose(videoAudioPlayerController);
		removeAsListenerAndDispose(docEditorCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(ccwc);
		confirmDeleteCtrl = null;
		viewDocCtrl = null;
		copyDocCtrl = null;
		uploadCtrl = null;
		newDocCtrl = null;
		avSubmissionController = null;
		avConvertingMenuCtrl = null;
		videoAudioPlayerController = null;
		docEditorCtrl = null;
		toolsCtrl = null;
		toolsCalloutCtrl = null;
		cmc = null;
		ccwc = null;
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
		} else if(source instanceof FormLink link) {
			if("view".equals(link.getCmd())) {
				doView(ureq, (String)link.getUserObject());
			} else if ("open".equalsIgnoreCase(link.getCmd()) && link.getUserObject() instanceof VFSLeaf vfsLeaf) {
				doOpenMedia(ureq, vfsLeaf);
			} else if ("transcoding".equalsIgnoreCase(link.getCmd()) && link.getUserObject() instanceof String filename) {
				doOpenTranscoding(ureq, link, filename);
			} else if ("download".equalsIgnoreCase(link.getCmd())) {
				doDownload(ureq, (VFSLeaf) link.getUserObject());
			} else if ("tools".equalsIgnoreCase(link.getCmd())) {
				doOpenTools(ureq, link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doDownload(UserRequest ureq, VFSLeaf file) {
		VFSMediaResource vdr = new VFSMediaResource(file);
		vdr.setDownloadable(true);
		ureq.getDispatchResult().setResultingMediaResource(vdr);
	}

	private void doOpenTools(UserRequest ureq, FormLink link) {
		toolsCtrl = new ToolsController(ureq, getWindowControl(), (SubmittedSolution) link.getUserObject());
		listenTo(toolsCtrl);

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
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

	private void doPlayMaster(UserRequest ureq, String fileName) {
		VFSItem vfsItem = documentsContainer.resolve(fileName);
		if (vfsItem instanceof VFSLeaf vfsLeaf) {
			doOpenMedia(ureq, vfsLeaf);
		}
	}
	
	private void doOpenTranscoding(UserRequest ureq, FormLink link, String filename) {
		if (guardModalController(avConvertingMenuCtrl)) return;
		
		gtaManager.markNews(courseEnv, gtaNode);
		updateWarnings();
		checkDeadline(ureq);
		
		avConvertingMenuCtrl = new AVConvertingMenuController(ureq, getWindowControl(), filename);
		listenTo(avConvertingMenuCtrl);
		ccwc = new CloseableCalloutWindowController(ureq, getWindowControl(),
				avConvertingMenuCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(ccwc);
		ccwc.activate();
	}

	private void doOpenMedia(UserRequest ureq, VFSLeaf vfsLeaf) {
		fireEvent(ureq, new SubmitEvent(SubmitEvent.UPDATE, vfsLeaf.getName()));
		DocEditorConfigs configs = GTAUIFactory.getEditorConfig(documentsContainer, vfsLeaf, vfsLeaf.getName(), Mode.EDIT, null);
		docEditorCtrl = docEditorService.openDocument(ureq, getWindowControl(), configs, DocEditorService.modesEditView(!readOnly)).getController();
		listenTo(docEditorCtrl);
	}
	
	private void doReplaceDocument(UserRequest ureq, SubmittedSolution row) {
		replaceCtrl = new DocumentUploadController(ureq, getWindowControl(), row, row.getFile(), documentsContainer, assignedTask, courseEnv.getCourseGroupManager().getCourseEntry());
		listenTo(replaceCtrl);

		String title = translate("replace.document");
		cmc = new CloseableModalController(getWindowControl(), null, replaceCtrl.getInitialComponent(), true, title, true);
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
			uploadCtrl = new DocumentUploadController(ureq, getWindowControl(), documentsContainer, assignedTask, courseEnv.getCourseGroupManager().getCourseEntry());
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
		cmc = new CloseableModalController(getWindowControl(), translate("close"), avSubmissionController.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doRecordAudio(UserRequest ureq) {
		long recordingLengthLimit = 1000 * Long.parseLong(config.getStringValue(GTACourseNode.GTASK_MAX_AUDIO_DURATION, "600"));
		avSubmissionController = new AVSubmissionController(ureq, getWindowControl(), documentsContainer, true,
				recordingLengthLimit, null);
		listenTo(avSubmissionController);

		String title = translate("av.record.audio");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), avSubmissionController.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doCreateDocument(UserRequest ureq) {
		if(newDocCtrl != null) return;
		
		if(maxDocs > 0 && maxDocs <= model.getRowCount()) {
			showWarning("error.max.documents");
		} else {
			newDocCtrl = new NewDocumentController(ureq, getWindowControl(), documentsContainer,
					officeHtml(getIdentity(), ureq.getUserSession().getRoles(), getLocale()));
			listenTo(newDocCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"), newDocCtrl.getInitialComponent(),
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
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"), copyDocCtrl.getInitialComponent(),
					translate(copyDocButton.getI18nKey()));
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	public enum DocCols {
		document("document"),
		date("document.date"),
		createdBy("table.header.created.by"),
		download("table.header.download"),
		toolsLink("table.header.action");
		
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
		private final boolean inTranscoding;
		private final FormLink downloadLink;
		private final FormLink documentLink;
		private final FormLink toolsLink;
		private final FormLink openLink;

		public SubmittedSolution(File file, String createdBy, FormLink downloadLink, FormLink openLink,
								 FormLink documentLink, FormLink toolsLink, boolean inTranscoding) {
			this.file = file;
			this.createdBy = createdBy;
			this.downloadLink = downloadLink;
			this.openLink = openLink;
			this.documentLink = documentLink;
			this.toolsLink = toolsLink;
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

		public FormLink getOpenLink() {
			return openLink;
		}

		public FormLink getDocumentLink() {
			return documentLink;
		}

		public FormLink getToolsLink() {
			toolsLink.setUserObject(this);
			return toolsLink;
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
			switch (DocCols.values()[col]) {
				case document -> {
					return solution.getDocumentLink();
				}
				case date -> {
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(solution.getFile().lastModified());
					return cal.getTime();
				}
				case createdBy -> {
					return solution.getCreatedBy();
				}
				case download -> {
					return solution.getDownloadLink();
				}
				case toolsLink -> {
					return solution.getToolsLink();
				}
				default -> {
					return "ERROR";
				}
			}
		}
	}

	private class ToolsController extends BasicController {

		private final VelocityContainer mainVC;
		private final Link deleteLink;
		private final Link replaceLink;
		private Link openLink = null;
		private final Link downloadLink;
		private final SubmittedSolution submittedSolutionRow;

		public ToolsController(UserRequest ureq, WindowControl wControl, SubmittedSolution submittedSolutionRow) {
			super(ureq, wControl);
			this.submittedSolutionRow = submittedSolutionRow;

			mainVC = createVelocityContainer("submit_docs_tools");

			List<String> links = new ArrayList<>(2);

			if (submittedSolutionRow.getOpenLink().getI18nKey() != null
					&&
					(embeddedEditor
					|| submittedSolutionRow.getFile().getName().endsWith(".drawio")
					|| submittedSolutionRow.getFile().getName().endsWith(".dwb")
					|| submittedSolutionRow.getOpenLink().getI18nKey().equals("Open"))) {
				openLink = addLink(submittedSolutionRow.getOpenLink().getI18nKey(), submittedSolutionRow.getOpenLink().getComponent().getIconLeftCSS(), links);
				openLink.setNewWindow(submittedSolutionRow.getOpenLink().isNewWindow(), submittedSolutionRow.getOpenLink().isNewWindowAfterDispatchUrl());
			}
			downloadLink = addLink("download.file", "o_icon_download", links);
			downloadLink.setUserObject(submittedSolutionRow.getDownloadLink().getUserObject());
			replaceLink = addLink("table.header.replace.doc", "o_icon_redo", links);
			if (!externalEditor) {
				replaceLink.setVisible(false);
				if (openLink != null) {
					openLink.setVisible(false);
				}

			}
			deleteLink = addLink("delete", "o_icon_delete_item", links);

			mainVC.contextPut("links", links);

			putInitialPanel(mainVC);
		}

		private Link addLink(String name, String iconCss, List<String> links) {
			int presentation = Link.LINK;
			if (submittedSolutionRow.getOpenLink().getI18nKey() != null
					&& submittedSolutionRow.getOpenLink().getI18nKey().equals(name)) {
				presentation = Link.NONTRANSLATED;
			}
			Link link = LinkFactory.createLink(name, name, getTranslator(), mainVC, this, presentation);
			mainVC.put(name, link);
			links.add(name);
			link.setIconLeftCSS("o_icon o_icon-fw " + iconCss);
			return link;
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if (deleteLink == source) {
				close();
				doConfirmDelete(ureq, submittedSolutionRow);
			} else if (source == replaceLink) {
				close();
				doReplaceDocument(ureq, submittedSolutionRow);
			} else if (source == openLink) {
				close();
				if (submittedSolutionRow.getOpenLink().getCmd().equalsIgnoreCase("view")) {
					doView(ureq, submittedSolutionRow.getFile().getName());
				} else if (submittedSolutionRow.getOpenLink().getCmd().equalsIgnoreCase("open")) {
					doOpenMedia(ureq, (VFSLeaf) submittedSolutionRow.getOpenLink().getUserObject());
				} else if (submittedSolutionRow.getOpenLink().getCmd().equalsIgnoreCase("download")) {
					doDownload(ureq, (VFSLeaf) submittedSolutionRow.getDownloadLink().getUserObject());
				}
			} else if (source == downloadLink) {
				doDownload(ureq, (VFSLeaf) downloadLink.getUserObject());
			}
		}

		private void close() {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		}
	}

	@Override
	protected void doDispose() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, VFSTranscodingService.ores);
		super.doDispose();
	}
}