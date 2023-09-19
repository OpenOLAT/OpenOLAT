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
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.nodes.gta.model.Solution;


/**
 *
 * Initial date: 24.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditSolutionController extends FormBasicController {

	private TextElement titleEl;
	private FileElement fileEl;

	private final boolean replaceFile;
	private final Solution solution;
	private final File solutionDir;
	private final VFSContainer solutionContainer;
	private final String filenameToReplace;

	public EditSolutionController(UserRequest ureq, WindowControl wControl,
			File solutionDir, VFSContainer solutionContainer) {
		this(ureq, wControl, new Solution(), solutionDir, solutionContainer, false);
	}

	public EditSolutionController(UserRequest ureq, WindowControl wControl, Solution solution,
			File solutionDir, VFSContainer solutionContainer) {
		this(ureq, wControl, solution, solutionDir, solutionContainer, true);
	}

	private EditSolutionController(UserRequest ureq, WindowControl wControl,
			Solution solution, File solutionDir, VFSContainer solutionContainer, boolean replaceFile) {
		super(ureq, wControl);
		this.replaceFile = replaceFile;
		this.solution = solution;
		this.filenameToReplace = solution != null ? solution.getFilename() : null;
		this.solutionDir = solutionDir;
		this.solutionContainer = solutionContainer;
		initForm(ureq);
	}

	public Solution getSolution() {
		return solution;
	}

	public String getFilenameToReplace() {
		return filenameToReplace;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_course_gta_upload_solution_form");
		
		fileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "file", "solution.file", formLayout);
		fileEl.setMandatory(true);
		fileEl.addActionListener(FormEvent.ONCHANGE);
		if(StringHelper.containsNonWhitespace(solution.getFilename())) {
			File currentFile = new File(solutionDir, solution.getFilename());
			if(currentFile.exists()) {
				fileEl.setInitialFile(currentFile);
			}
		}
		
		String title = solution.getTitle() == null ? "" : solution.getTitle();
		titleEl = uifactory.addTextElement("title", "solution.title", 128, title, formLayout);
		titleEl.setElementCssClass("o_sel_course_gta_upload_solution_title");
		titleEl.setMandatory(true);
		
		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonCont.setRootForm(mainForm);
		formLayout.add(buttonCont);
		uifactory.addFormSubmitButton("save", buttonCont);
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		titleEl.clearError();
		if(!StringHelper.containsNonWhitespace(titleEl.getValue())) {
			titleEl.setErrorKey("form.mandatory.hover");
			allOk &= false;
		}

		fileEl.clearError();
		if(fileEl.getInitialFile() == null && fileEl.getUploadFile() == null) {
			fileEl.setErrorKey("form.mandatory.hover");
			allOk &= false;
		} else if (fileEl.getUploadFile() != null && !FileUtils.validateFilename(fileEl.getUploadFileName())) {
			fileEl.setErrorKey("error.file.invalid");
			allOk = false;
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		solution.setTitle(titleEl.getValue());

		if(fileEl.getUploadFile() != null) {
			if(replaceFile && StringHelper.containsNonWhitespace(solution.getFilename())) {
				File currentFile = new File(solutionDir, solution.getFilename());
				if(currentFile.exists()) {
					FileUtils.deleteFile(currentFile);
				}
			}

			String filename = fileEl.getUploadFileName();
			if(!replaceFile) {
				File currentFile = new File(solutionDir, filename);
				if(currentFile.exists()) {
					filename = VFSManager.rename(solutionContainer, filename);
				}
			}

			solution.setFilename(filename);

			fileEl.moveUploadFileTo(solutionContainer);
		}

		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}