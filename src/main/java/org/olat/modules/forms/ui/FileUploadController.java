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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FileElementEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.model.jpa.EvaluationFormResponses;
import org.olat.modules.forms.model.xml.FileUpload;
import org.olat.modules.forms.ui.model.EvaluationFormResponseController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 02.02.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FileUploadController extends FormBasicController implements EvaluationFormResponseController {

	private static final OLog log = Tracing.createLoggerFor(FileUploadController.class);

	private FileElement fileEl;
	
	private final FileUpload fileUpload;
	private EvaluationFormResponse response;
	private boolean newFileUploaded = false;
	private boolean fileDeleted = false;
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	
	public FileUploadController(UserRequest ureq, WindowControl wControl, FileUpload fileUpload) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.fileUpload = fileUpload;
		initForm(ureq);
	}
	
	public FileUploadController(UserRequest ureq, WindowControl wControl, FileUpload fileUpload, Form rootForm) {
		super(ureq, wControl, LAYOUT_VERTICAL, null, rootForm);
		this.fileUpload = fileUpload;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		fileEl = uifactory.addFileElement(getWindowControl(), "file_upload_" + CodeHelper.getRAMUniqueID(), "", formLayout);
		fileEl.setPreview(ureq.getUserSession(), true);
		fileEl.setButtonsEnabled(false);
		fileEl.setDeleteEnabled(true);
		fileEl.setMaxUploadSizeKB(fileUpload.getMaxUploadSizeKB(), "file.upload.error.limit.exeeded", null);
		Set<String> mimeTypes = MimeTypeSetFactory.getMimeTypes(fileUpload.getMimeTypeSetKey());
		fileEl.limitToMimeType(mimeTypes, "file.upload.error.mime.type.wrong", null);
		fileEl.setHelpTextKey("ul.select.fhelp", null);
		fileEl.addActionListener(FormEvent.ONCHANGE);
	}
	
	public void update() {
		fileEl.setMaxUploadSizeKB(fileUpload.getMaxUploadSizeKB(), null, null);
		Set<String> mimeTypes = MimeTypeSetFactory.getMimeTypes(fileUpload.getMimeTypeSetKey());
		fileEl.limitToMimeType(mimeTypes, null, null);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
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
		fileEl.setEnabled(!readOnly);
	}

	@Override
	public boolean hasResponse() {
		return response != null && response.getFileResponse() != null;
	}

	@Override
	public void initResponse(EvaluationFormSession session, EvaluationFormResponses responses) {
		response = responses.getResponse(session, fileUpload.getId());
		File responseFile = evaluationFormManager.loadResponseFile(response);
		if (responseFile != null) {
			fileEl.setInitialFile(responseFile);
		}
		fileEl.setButtonsEnabled(true);
	}

	@Override
	public void saveResponse(EvaluationFormSession session) {
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
		} else if (fileDeleted && response != null) {
			evaluationFormManager.deleteResponse(response);
			response = null;
		}
	}

}
