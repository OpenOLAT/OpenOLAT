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
package org.olat.modules.project.ui;

import org.olat.core.commons.services.doceditor.ui.CreateDocumentController;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FileElementEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjFileUploadController extends FormBasicController {
	
	private FileElement fileEl;

	private ProjFileContentController fileEditCtrl;

	private final ProjProject project;
	private ProjFile file;
	
	@Autowired
	private ProjectService projectService;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;

	public ProjFileUploadController(UserRequest ureq, WindowControl wControl, ProjProject project) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		setTranslator(Util.createPackageTranslator(CreateDocumentController.class, getLocale(), getTranslator()));
		this.project = project;
		
		initForm(ureq);
	}
	
	public ProjFile getFile() {
		return file;
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		fileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "file.file", formLayout);
		fileEl.setMandatory(true, "form.mandatory.hover");
		fileEl.addActionListener(FormEvent.ONCHANGE);
		
		fileEditCtrl = new ProjFileContentController(ureq, getWindowControl(), mainForm, project, null);
		listenTo(fileEditCtrl);
		formLayout.add("file", fileEditCtrl.getInitialFormItem());
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("file.upload.button", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
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
				}
			} else {
				fileEditCtrl.setFilename(fileEl.getUploadFileName(), false);
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
		if (fileEl.getUploadFile() != null) {
			file = projectService.createFile(getIdentity(), project, fileEditCtrl.getFilename(), fileEl.getUploadInputStream(), true);
			if (file != null) {
				projectService.updateTags(getIdentity(), file.getArtefact(), fileEditCtrl.getTagDisplayValues());
				
				VFSMetadata vfsMetadata = file.getVfsMetadata();
				fileEditCtrl.updateVfsMetdata(vfsMetadata);
				vfsMetadata = vfsRepositoryService.updateMetadata(vfsMetadata);
			}
		} 
		
		fireEvent(ureq, FormEvent.DONE_EVENT);
	}

}
