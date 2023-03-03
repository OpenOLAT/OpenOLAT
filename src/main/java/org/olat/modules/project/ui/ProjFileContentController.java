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
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjFileContentController extends FormBasicController {
	
	private TextElement filenameEl;
	private TextElement titleEl;
	private TextAreaElement descriptionEl;
	
	private ProjProject project;
	private String initialFilename;
	
	@Autowired
	private ProjectService projectService;

	public ProjFileContentController(UserRequest ureq, WindowControl wControl, Form mainForm) {
		super(ureq, wControl, LAYOUT_VERTICAL, null, mainForm);
		setTranslator(Util.createPackageTranslator(CreateDocumentController.class, getLocale(), getTranslator()));
		
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// filename
		filenameEl = uifactory.addTextElement("file.filename", -1, null, formLayout);
		filenameEl.setMandatory(true);
		
		titleEl = uifactory.addTextElement("title", -1, null, formLayout);
		
		descriptionEl = uifactory.addTextAreaElement("file.description", "file.description", -1, 3, 1, true, false, null, formLayout);
	}
	
	public void setFilenameVisibility(boolean visible) {
		filenameEl.setVisible(visible);
	}
	
	public String getFilename() {
		return filenameEl.isVisible() && StringHelper.containsNonWhitespace(filenameEl.getValue()) ? filenameEl.getValue(): null;
	}
	
	public void setFilename(ProjProject project, String filename) {
		this.project = project;
		this.initialFilename = filename;
		filenameEl.setValue(filename);
	}
	
	public void updateUI(VFSMetadata vfsMetadata) {
		titleEl.setValue(vfsMetadata.getTitle());
		descriptionEl.setValue(vfsMetadata.getComment());
	}
	
	public String getTitle() {
		return StringHelper.containsNonWhitespace(titleEl.getValue())? titleEl.getValue(): null;
	}
	
	public String getDescription() {
		return StringHelper.containsNonWhitespace(descriptionEl.getValue())? descriptionEl.getValue(): null;
	}

	public void updateVfsMetdata(VFSMetadata vfsMetadata) {
		String title = StringHelper.containsNonWhitespace(titleEl.getValue())? titleEl.getValue(): null;
		vfsMetadata.setTitle(title);
		String description = StringHelper.containsNonWhitespace(descriptionEl.getValue())? descriptionEl.getValue(): null;
		vfsMetadata.setComment(description);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if (filenameEl.isVisible()) {
			filenameEl.clearError();
			if (!StringHelper.containsNonWhitespace(filenameEl.getValue())) {
				filenameEl.setErrorKey("form.mandatory.hover");
				allOk &= false;
			} else {
				// update in GUI so user sees how we optimized
				String filename = getCleanedFilename();
				filenameEl.setValue(filename);
				if (invalidFilename(filename)) {
					filenameEl.setErrorKey("create.doc.name.notvalid");
					allOk &= false;
				} else if (initialFilename != null && !initialFilename.equals(filename) && projectService.existsFile(project, filename)) {
					filenameEl.setErrorKey("create.doc.already.exists", new String[] { filename });
					allOk &= false;
				}
			}
		}
		
		return allOk;
	}

	private boolean invalidFilename(String docName) {
		return !FileUtils.validateFilename(docName);
	}
	
	private String getCleanedFilename() {
		String filename = filenameEl.getValue();
		String suffix = FileUtils.getFileSuffix(initialFilename);
		return filename.endsWith("." + suffix)
				? filename
				: filename + "." + suffix;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

}
