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
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.ui.SubmitDocumentsController.SubmittedSolution;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryManager;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 27.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class DocumentUploadController extends FormBasicController {

	private FileElement fileEl;
	
	private final File fileToReplace;
	private final SubmittedSolution solution;
	private final VFSContainer documentsContainer;
	private final Task assignedTask;
	private final RepositoryEntry entry;

	@Autowired
	private UserManager userManager;
	@Autowired
	private RepositoryManager repositoryManager;

	public DocumentUploadController(UserRequest ureq, WindowControl wControl,
									VFSContainer documentsContainer, Task assignedTask, RepositoryEntry entry) {
		this(ureq, wControl, null, null, documentsContainer, assignedTask, entry);
	}

	public DocumentUploadController(UserRequest ureq, WindowControl wControl, SubmittedSolution solution, File fileToReplace,
			VFSContainer documentsContainer, Task assignedTask, RepositoryEntry entry) {
		super(ureq, wControl);
		this.solution = solution;
		this.fileToReplace = fileToReplace;
		this.documentsContainer = documentsContainer;
		this.assignedTask = assignedTask;
		this.entry = entry;
		initForm(ureq);
	}

	public SubmittedSolution getSolution() {
		return solution;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_course_gta_upload_form");

		RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(getIdentity(), ureq.getUserSession().getRoles(), entry);

		if (reSecurity.isEntryAdmin() || reSecurity.isCourseCoach() || reSecurity.isOwner()) {
			uifactory.addStaticTextElement("assessedParticipant", "participants", userManager.getUserDisplayName(assignedTask.getIdentity()), formLayout);
			uifactory.addStaticTextElement("taskStatus", "solution.task.step", translate("process." + assignedTask.getTaskStatus().name()), formLayout);
		}

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
			fileEl.setErrorKey("form.mandatory.hover");
			allOk &= false;
		} else if (fileEl.getUploadFile() != null && !FileUtils.validateFilename(fileEl.getUploadFileName())) {
			fileEl.setErrorKey("error.file.invalid");
			allOk &= false;
		} else if (fileEl.getUploadFile() != null && fileEl.getUploadFile().length() == 0) {
			fileEl.setErrorKey("error.file.empty");
			allOk &= false;
		} else if(fileToReplace == null && documentsContainer != null
				&& documentsContainer.resolve(fileEl.getUploadFileName()) != null) {
			fileEl.setErrorKey("error.file.exists", fileEl.getUploadFileName());
			allOk &= false;
		} else if(fileToReplace != null && !fileToReplace.getName().equals(fileEl.getUploadFileName())
				&& documentsContainer != null && documentsContainer.resolve(fileEl.getUploadFileName()) != null) {
			fileEl.setErrorKey("error.file.exists", fileEl.getUploadFileName());
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