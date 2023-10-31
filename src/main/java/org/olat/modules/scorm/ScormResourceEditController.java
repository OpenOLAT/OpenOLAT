/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.scorm;

import java.io.File;

import org.olat.core.commons.modules.bc.FolderConfig;
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
import org.olat.core.util.FileUtils;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.fileresource.types.ScormCPFileResource;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.resource.references.ReferenceManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Oct 26, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ScormResourceEditController extends FormBasicController {

	private final RepositoryEntry entry;
	private FileElement uploadFileEl;
	private FormSubmit submit;
	@Autowired
	private RepositoryHandlerFactory repositoryHandlerFactory;
	@Autowired
	private ReferenceManager referenceManager;

	protected ScormResourceEditController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl);
		this.entry = entry;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("scorm.replace.desc");

		int numOfUsages = referenceManager.getRepositoryReferencesTo(entry.getOlatResource()).size();

		if (numOfUsages > 0) {
			setFormWarning("scorm.replace.already.used", new String[]{String.valueOf(numOfUsages)});
		}

		uploadFileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "upload", "scorm.replace.upload", formLayout);
		uploadFileEl.addActionListener(FormEvent.ONCHANGE);

		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonGroupLayout);
		submit = uifactory.addFormSubmitButton("submit", "tab.scorm.exchange", buttonGroupLayout);
		submit.setEnabled(false);
		uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == uploadFileEl) {
			RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(ScormCPFileResource.TYPE_NAME);
			ResourceEvaluation eval = handler.acceptImport(uploadFileEl.getUploadFile(), uploadFileEl.getUploadFileName());
			if (eval == null || !eval.isValid()) {
				uploadFileEl.setErrorKey("scorm.add.failed");
				submit.setEnabled(false);
			} else {
				uploadFileEl.clearError();
				submit.setEnabled(true);
			}
		}
	}

	private boolean doReplaceScormResource() {
		FileResourceManager frm = FileResourceManager.getInstance();
		File currentResource = frm.getFileResource(entry.getOlatResource());

		String typeName = entry.getOlatResource().getResourceableTypeName();
		if (typeName.equals(ScormCPFileResource.TYPE_NAME)) {
			if (currentResource.delete()) {
				FileUtils.copyFileToFile(uploadFileEl.getUploadFile(), currentResource, false);

				String repositoryHome = FolderConfig.getCanonicalRepositoryHome();
				String relUnzipDir = frm.getUnzippedDirRel(entry.getOlatResource());
				File unzipDir = new File(repositoryHome, relUnzipDir);
				if (unzipDir.exists()) {
					FileUtils.deleteDirsAndFiles(unzipDir, true, true);
				}
				frm.unzipFileResource(entry.getOlatResource());
			}
			return true;
		}
		return false;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (doReplaceScormResource()) {
			fireEvent(ureq, Event.DONE_EVENT);
		} else {
			uploadFileEl.setErrorKey("scorm.add.failed");
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
