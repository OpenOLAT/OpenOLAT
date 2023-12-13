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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.services.doceditor.DocEditor;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorDisplayInfo;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.DownloadeableVFSMediaResource;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.ui.GroupAssessmentController.DocumentMapper;
import org.olat.course.nodes.gta.ui.GroupAssessmentController.DocumentWrapper;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.fileresource.DownloadeableMediaResource;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 May 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EditAssessmentDocumentController extends FormBasicController {
	
	private FileElement groupUploadDocsEl;
	private FormLayoutContainer docsLayoutCont;

	private final GTACourseNode gtaNode;
	private final AssessmentRow row;
	private final ICourse course;
	private final boolean readOnly;
	private final UserCourseEnvironment userCourseEnv;
	private final File assessmentDocsTmpDir;
	private final List<DocumentWrapper> documents = new ArrayList<>();

	private int counter = 0;
	private final Roles roles;

	private Controller docEditorCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private DocEditorService docEditorService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;

	public EditAssessmentDocumentController(UserRequest ureq, WindowControl wControl, OLATResourceable courseOres,
			GTACourseNode gtaNode, AssessmentRow row, boolean readOnly) {
		super(ureq, wControl, "edit_assessment_docs");
		this.gtaNode = gtaNode;
		this.row = row;
		this.readOnly = readOnly;
		roles = ureq.getUserSession().getRoles();
		course = CourseFactory.loadCourse(courseOres);
		userCourseEnv = row.getUserCourseEnvironment(course);
		assessmentDocsTmpDir = FileUtils.createTempDir("gtaassessmentdocs", null, null);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		List<VFSLeaf> currentAssessmentDocs = courseAssessmentService.getIndividualAssessmentVFSDocuments(gtaNode, userCourseEnv);
		String mapperUri = registerCacheableMapper(ureq, null, new DocumentMapper(documents));

		String page = velocity_root + "/individual_assessment_docs_large.html";
		docsLayoutCont = uifactory.addCustomFormLayout("docs", null, page, formLayout);
		formLayout.add("docs", docsLayoutCont);
		docsLayoutCont.contextPut("mapperUri", mapperUri);
		docsLayoutCont.contextPut("documents", documents);
		for (VFSLeaf assessmentDoc : currentAssessmentDocs) {
			DocumentWrapper wrapper = createDocumentWrapper(assessmentDoc, null);
			documents.add(wrapper);
		}
		
		if (!readOnly) {
			groupUploadDocsEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "upload", null, formLayout);
			groupUploadDocsEl.addActionListener(FormEvent.ONCHANGE);

			uifactory.addFormSubmitButton("save", formLayout);
			uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == docEditorCtrl) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(docEditorCtrl);
		docEditorCtrl = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(groupUploadDocsEl == source) {
			if(groupUploadDocsEl.getUploadFile() != null && StringHelper.containsNonWhitespace(groupUploadDocsEl.getUploadFileName())) {
				File newDocument = groupUploadDocsEl.moveUploadFileTo(assessmentDocsTmpDir);
				groupUploadDocsEl.reset();
				documents.add(createDocumentWrapper(null, newDocument));
				docsLayoutCont.setDirty(true);
			}
		} else if(source instanceof FormLink link && link.getUserObject() instanceof DocumentWrapper wrapper) {
			if("delete".equals(link.getCmd())) {
				doDeleteGroupAssessmentDoc(wrapper);
			} else if("download".equals(link.getCmd())) {
				MediaResource mediaResource = wrapper.getDocument() != null ?
						new DownloadeableVFSMediaResource(wrapper.getDocument())
						: new DownloadeableMediaResource(wrapper.getTempDocument());
				ureq.getDispatchResult().setResultingMediaResource(mediaResource);
			} else if("open".equals(link.getCmd())) {
				doOpenDocument(ureq, wrapper);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean hasDocuments = false;
		for(DocumentWrapper document:documents) {
			if(document.getTempDocument() != null) {
				File assessmentDoc = document.getTempDocument();
				courseAssessmentService.addIndividualAssessmentDocument(gtaNode, assessmentDoc,
						assessmentDoc.getName(), userCourseEnv, getIdentity());
				hasDocuments = true;
			} else if(document.getDocument() != null) {
				if(document.isMarkedAsDeleted()) {
					courseAssessmentService.removeIndividualAssessmentDocument(gtaNode, document.getDocument(),
							userCourseEnv, getIdentity());
				} else {
					hasDocuments = true;
				}
			}
		}

		if(hasDocuments) {
			row.getAssessmentDocsEditLink().setIconLeftCSS("o_icon o_icon_files");
		} else {
			row.getAssessmentDocsEditLink().setIconLeftCSS("o_icon o_filetype_file");
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void doDispose() {
		if (assessmentDocsTmpDir != null && assessmentDocsTmpDir.exists()) {
			FileUtils.deleteDirsAndFiles(assessmentDocsTmpDir, true, true);
		}
        super.doDispose();
	}
	
	private DocumentWrapper createDocumentWrapper(VFSLeaf document, File tempFile) {
		String initializedBy = null;
		Date creationDate = null;
		VFSMetadata metadata = null;
		if(tempFile != null) {
			creationDate = new Date();
			initializedBy = userManager.getUserDisplayName(getIdentity());
		} else if(document != null) {
			metadata = document.getMetaInfo();
			if(metadata != null) {
				creationDate = metadata.getCreationDate();
				Identity identity = metadata.getFileInitializedBy();
				if(identity != null) {
					initializedBy = userManager.getUserDisplayName(identity);
				}
			}
		}
		
		DocumentWrapper wrapper = new DocumentWrapper(document, tempFile, initializedBy, creationDate);
		
		DocEditorDisplayInfo editorInfo = docEditorService.getEditorInfo(getIdentity(), roles, document,
				metadata, true, DocEditorService.modesEditView(false));
		FormLink openButton;
		if(editorInfo != null && editorInfo.isEditorAvailable()) {
			openButton = uifactory.addFormLink("openfile_" + (++counter), "open", "open", null, docsLayoutCont, Link.LINK | Link.NONTRANSLATED);
			if (editorInfo.isNewWindow()) {
				openButton.setNewWindow(true, true, false);
			}
		} else {
			openButton = uifactory.addFormLink("download_alt_" + (++counter), "download", "download", null, docsLayoutCont, Link.LINK | Link.NONTRANSLATED);
		}
		openButton.getComponent().setCustomDisplayText(wrapper.getFilename());
		wrapper.setOpenButton(openButton);
		
		FormLink downloadButton = uifactory.addFormLink("download_doc_" + (++counter), "download", "", null, docsLayoutCont, Link.BUTTON_XSMALL | Link.NONTRANSLATED);
		downloadButton.setIconLeftCSS("o_icon o_icon-fw o_icon_download");
		downloadButton.setGhost(true);
		downloadButton.setEnabled(true);  
		downloadButton.setVisible(true);
		wrapper.setDownloadButton(downloadButton);
		
		if(!readOnly) {
			FormLink deleteButton = uifactory.addFormLink("delete_doc_" + (++counter), "delete", "", null, docsLayoutCont, Link.BUTTON_XSMALL | Link.NONTRANSLATED);
			deleteButton.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
			deleteButton.setGhost(true);
			deleteButton.setEnabled(true);  
			deleteButton.setVisible(true);
			wrapper.setDeleteButton(deleteButton);
		}
		return wrapper;
	}
	
	private void doOpenDocument(UserRequest ureq, DocumentWrapper wrapper) {
		DocEditorConfigs configs = DocEditorConfigs.builder()
				.withVersionControlled(false)
				.withMode(DocEditor.Mode.VIEW)
				.build(wrapper.getDocument());
		docEditorCtrl = docEditorService.openDocument(ureq, getWindowControl(), configs, DocEditorService.modesEditView(false)).getController();
		listenTo(docEditorCtrl);
	}
	
	private void doDeleteGroupAssessmentDoc(DocumentWrapper wrapper) {
		if(wrapper.getDocument() != null) {
			wrapper.setMarkedAsDeleted(true);
		} else if(wrapper.getTempDocument() != null) {
			try {
				Files.deleteIfExists(wrapper.getTempDocument().toPath());
			} catch (IOException e) {
				logError("", e);
			}
			documents.remove(wrapper);
		}
		docsLayoutCont.setDirty(true);
	}
}
