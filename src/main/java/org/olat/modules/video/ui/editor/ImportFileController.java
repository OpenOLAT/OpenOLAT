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
package org.olat.modules.video.ui.editor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.fileresource.types.VideoFileResource;
import org.olat.modules.video.VideoManager;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.resource.OLATResource;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-03-09<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ImportFileController extends FormBasicController {
	private FileElement uploadFileEl;
	private FormSubmit importButton;
	@Autowired
	private RepositoryHandlerFactory repositoryHandlerFactory;
	@Autowired
	private VideoManager videoManager;
	private final OLATResource videoResource;

	protected ImportFileController(UserRequest ureq, WindowControl wControl, OLATResource videoResource) {
		super(ureq, wControl);
		this.videoResource = videoResource;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("comment.add.import.file.description");

		uploadFileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "upload",
				"comment.add.import.file.file", formLayout);
		uploadFileEl.addActionListener(FormEvent.ONCHANGE);

		FormLayoutContainer buttonContainer = FormLayoutContainer.createButtonLayout("buttonContainer",
				getTranslator());
		formLayout.add("buttonContainer", buttonContainer);
		importButton = uifactory.addFormSubmitButton("comment.add.import.file", buttonContainer);
		importButton.setEnabled(false);
		uifactory.addFormCancelButton("cancel", buttonContainer, ureq, getWindowControl());
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (uploadFileEl == source) {
			doCheckUpload();
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doCheckUpload() {
		importButton.setEnabled(false);
		File uploadFile = uploadFileEl.getUploadFile();
		if (uploadFile == null) {
			uploadFileEl.reset();
		} else {
			String uploadFileName = uploadFileEl.getUploadFileName();
			for (String type : repositoryHandlerFactory.getSupportedTypes()) {
				RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(type);
				if (handler.getSupportedType().equals(VideoFileResource.TYPE_NAME)) {
					ResourceEvaluation evaluation = handler.acceptImport(uploadFile, uploadFileName);
					if (evaluation != null && evaluation.isValid()) {
						importButton.setEnabled(true);
						return;
					}
				}
			}
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		uploadFileEl.clearError();
		if (uploadFileEl.getInitialFile() == null && uploadFileEl.getUploadFile() == null) {
			uploadFileEl.setErrorKey("form.mandatory.hover");
			allOk = false;
		} else if (uploadFileEl.getUploadFile() != null) {
			String uploadFileName = uploadFileEl.getUploadFileName();
			VFSContainer targetContainer = videoManager.getCommentMediaContainer(videoResource);
			if (targetContainer.resolve(uploadFileName) != null) {
				uploadFileEl.setErrorKey("comment.add.error.file.exists", uploadFileName);
				allOk = false;
			}
		}

		return allOk;
	}

	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		VFSContainer targetContainer = videoManager.getCommentMediaContainer(videoResource);
		if (targetContainer instanceof LocalFolderImpl localFolder) {
			File uploadFile = uploadFileEl.getUploadFile();
			File targetFile = new File(localFolder.getBasefile(), uploadFileEl.getUploadFileName());
			try {
				Files.move(uploadFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				logError("", e);
			}
		}

		fireEvent(ureq, Event.DONE_EVENT);
	}

	public String getFileName() {
		return uploadFileEl.getUploadFileName();
	}
}
