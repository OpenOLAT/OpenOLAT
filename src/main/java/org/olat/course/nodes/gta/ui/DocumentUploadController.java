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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.nodes.gta.ui.SubmitDocumentsController.SubmittedSolution;

/**
 *
 * Initial date: 27.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DocumentUploadController extends FormBasicController {

	private FileElement fileEl;
	
	private final File fileToReplace;
	private final SubmittedSolution solution;
	private final VFSContainer documentsContainer;

	public DocumentUploadController(UserRequest ureq, WindowControl wControl,
			VFSContainer documentsContainer) {
		this(ureq, wControl, null, null, documentsContainer);
	}

	public DocumentUploadController(UserRequest ureq, WindowControl wControl, SubmittedSolution solution, File fileToReplace,
			VFSContainer documentsContainer) {
		super(ureq, wControl);
		this.solution = solution;
		this.fileToReplace = fileToReplace;
		this.documentsContainer = documentsContainer;
		initForm(ureq);
	}

	public SubmittedSolution getSolution() {
		return solution;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_course_gta_upload_form");

		fileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "file", "solution.file", formLayout);
		fileEl.setMandatory(true);
		fileEl.addActionListener(FormEvent.ONCHANGE);
		if(fileToReplace != null) {
			fileEl.setInitialFile(fileToReplace);
		}

		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonCont.setRootForm(mainForm);
		formLayout.add(buttonCont);
		uifactory.addFormSubmitButton("save", buttonCont);
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
	}

	public String getUploadedFilename() {
		return fileEl.getUploadFileName();
	}

	public File getUploadedFile() {
		return fileEl.getUploadFile();
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		fileEl.clearError();
		if(fileEl.getInitialFile() == null && fileEl.getUploadFile() == null) {
			fileEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		} else if (fileEl.getUploadFile() != null && !FileUtils.validateFilename(fileEl.getUploadFileName())) {
			fileEl.setErrorKey("error.file.invalid", null);
			allOk &= false;
		} else if (fileEl.getUploadFile() != null && fileEl.getUploadFile().length() == 0) {
			fileEl.setErrorKey("error.file.empty", null);
			allOk &= false;
		} else if(fileToReplace == null && documentsContainer != null
				&& documentsContainer.resolve(fileEl.getUploadFileName()) != null) {
			fileEl.setErrorKey("error.file.exists", new String[]{ fileEl.getUploadFileName() });
			allOk &= false;
		} else if(fileToReplace != null && !fileToReplace.getName().equals(fileEl.getUploadFileName())
				&& documentsContainer != null && documentsContainer.resolve(fileEl.getUploadFileName()) != null) {
			fileEl.setErrorKey("error.file.exists", new String[]{ fileEl.getUploadFileName() });
			allOk &= false;
		}
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}