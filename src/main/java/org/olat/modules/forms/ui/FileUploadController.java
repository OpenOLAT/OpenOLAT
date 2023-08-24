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
package org.olat.modules.forms.ui;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FileElementEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.model.jpa.EvaluationFormResponses;
import org.olat.modules.forms.model.xml.FileUpload;
import org.olat.modules.forms.ui.model.EvaluationFormResponseController;
import org.olat.modules.forms.ui.model.Progress;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 02.02.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FileUploadController extends FormBasicController implements EvaluationFormResponseController {

	private static final Logger log = Tracing.createLoggerFor(FileUploadController.class);

	private FileElement fileEl;
	
	private FileUpload fileUpload;
	private EvaluationFormResponse response;
	private boolean validationEnabled = true;
	private boolean newFileUploaded = false;
	private boolean fileDeleted = false;
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	public FileUploadController(UserRequest ureq, WindowControl wControl, FileUpload fileUpload) {
		super(ureq, wControl, "file_upload");
		this.fileUpload = fileUpload;
		initForm(ureq);
	}
	
	public FileUploadController(UserRequest ureq, WindowControl wControl, FileUpload fileUpload, Form rootForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "file_upload", rootForm);
		this.fileUpload = fileUpload;
		initForm(ureq);
		updateUI(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String fileElId = "file_upload_" + CodeHelper.getRAMUniqueID();
		flc.contextPut("item", fileElId);
		fileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), fileElId, "", formLayout);
		fileEl.setButtonsEnabled(false);
		fileEl.setDeleteEnabled(true);
		fileEl.addActionListener(FormEvent.ONCHANGE);
	}
	
	public void updateUI(UserRequest ureq) {
		fileEl.setPreview(ureq.getUserSession(), MimeTypeSetFactory.hasPreview(fileUpload.getMimeTypeSetKey()));
	}
	
	public void updateReadOnlyUI(UserRequest ureq, EvaluationFormResponse response) {
		if (response != null) {
			String filename = response.getStringuifiedResponse();
			String filesize = null;
			String mapperUri = null;
			String iconCss = null;
			String thumbUri = null;
			VFSLeaf leaf = evaluationFormManager.loadResponseLeaf(response);
			if (leaf != null) {
				filename = leaf.getName();
				flc.contextPut("filename", filename);
				filesize = Formatter.formatBytes((leaf).getSize());
				flc.contextPut("filesize", filesize);
				mapperUri = registerCacheableMapper(ureq, "file-upload-" + CodeHelper.getRAMUniqueID() + "-" + leaf.getLastModified(), new VFSMediaMapper(leaf));
				flc.contextPut("mapperUri", mapperUri);
				iconCss = CSSHelper.createFiletypeIconCssClassFor(leaf.getName());
				flc.contextPut("iconCss", iconCss);
				
				VFSLeaf thumb = vfsRepositoryService.getThumbnail(leaf, 200, 200, false);
				if (thumb != null) {
					thumbUri = registerCacheableMapper(ureq, "file-upload-thumb" + CodeHelper.getRAMUniqueID() + "-" + leaf.getLastModified(), new VFSMediaMapper(thumb));
					flc.contextPut("thumbUri", thumbUri);
				}
			}	
		}
	}
	
	@Override
	public void setValidationEnabled(boolean enabled) {
		this.validationEnabled = enabled;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(event instanceof ChangePartEvent) {
			ChangePartEvent cpe = (ChangePartEvent)event;
			if(cpe.isElement(fileUpload)) {
				fileUpload = (FileUpload)cpe.getElement();
				updateUI(ureq);
			}
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		fileEl.clearError();
		if (!validationEnabled) return true;
		
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk = validateFile();
		
		return allOk;
	}

	private boolean validateFile() {
		boolean allOk = true;
		
		if (fileEl.isButtonsEnabled()) {
			File file = fileEl.isUploadSuccess() ? fileEl.getUploadFile(): fileEl.getInitialFile();
			if (file == null || !file.exists()) {
				if (fileUpload.isMandatory()) {
					fileEl.setErrorKey("form.legende.mandatory");
					allOk = false;
				}
			} else {
				if (fileUpload.getMaxUploadSizeKB() != null && file.length() > fileUpload.getMaxUploadSizeKB() * 1024l) {
					fileEl.setErrorKey("file.upload.error.limit.exeeded");
					allOk &= false;
				} else if (StringHelper.containsNonWhitespace(fileUpload.getMimeTypeSetKey())) {
					Set<String> mimeTypes = MimeTypeSetFactory.getMimeTypes(fileUpload.getMimeTypeSetKey());
					if (mimeTypes != null) {
						String fileName = fileEl.isUploadSuccess()? fileEl.getUploadFileName(): file.getName();
						String mimeType = WebappHelper.getMimeType(fileName);
						if (!mimeTypes.contains(mimeType)){
							fileEl.setErrorKey("file.upload.error.mime.type.wrong");
							allOk &= false;
						}
					}
				}
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == fileEl) {
			if (event instanceof FileElementEvent) {
				if (FileElementEvent.DELETE.equals(event.getCommand())) {
					fileEl.setInitialFile(null);
					if (fileEl.getUploadFile() != null) {
						fileEl.reset();
					}
					fileDeleted = true;
					flc.setDirty(true);
				}
			} else {
				newFileUploaded = true;
			}
		} 
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		flc.contextPut("readonly", Boolean.valueOf(readOnly));
	}

	@Override
	public boolean hasResponse() {
		return response != null && response.getFileResponse() != null;
	}

	@Override
	public void initResponse(UserRequest ureq, EvaluationFormSession session, EvaluationFormResponses responses) {
		response = responses.getResponse(session, fileUpload.getId());
		File responseFile = evaluationFormManager.loadResponseFile(response);
		if (responseFile != null) {
			fileEl.setInitialFile(responseFile);
		}
		fileEl.setButtonsEnabled(true);
		
		updateReadOnlyUI(ureq, response);
	}

	@Override
	public void saveResponse(UserRequest ureq, EvaluationFormSession session) {
		if (fileEl.isUploadSuccess()) {
			if (newFileUploaded) {
				File file = fileEl.getUploadFile();
				String filename = fileEl.getUploadFileName();
				try {
					if (response == null) {
						response = evaluationFormManager.createFileResponse(fileUpload.getId(), session,
								file, filename);
					} else {
						response = evaluationFormManager.updateFileResponse(response, file, filename);
					}
					newFileUploaded = false;
				} catch (IOException e) {
					log.warn("Cannot save file for an evaluation form response!", e);
					throw new RuntimeException(e);
				}
			}
		} else if (fileDeleted) {
			deleteResponse(session);
		}
		updateReadOnlyUI(ureq, response);
	}
	
	@Override
	public void deleteResponse(EvaluationFormSession session) {
		if (response != null) {
			evaluationFormManager.deleteResponse(response);
			response = null;
		}
	}

	@Override
	public Progress getProgress() {
		int current = hasResponse()? 1: 0;
		return Progress.of(current, 1);
	}

}
