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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.course.nodes.gta.model.Solution;

/**
 * Initial date: 2022-09-08<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class AVSampleSolutionDetailsController extends FormBasicController {

	private TextElement titleEl;
	private TextElement fileNameEl;
	private final VFSContainer solutionContainer;
	private String fileName;
	private Solution solution;

	public AVSampleSolutionDetailsController(UserRequest ureq, WindowControl wControl, VFSContainer solutionContainer, String fileName) {
		super(ureq, wControl);
		this.solutionContainer = solutionContainer;
		this.fileName = fileName;
		initForm(ureq);
	}

	public Solution getSolution() {
		return solution;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_course_gta_upload_task_form");

		titleEl = uifactory.addTextElement("title", "task.title", 128, "", formLayout);
		titleEl.setMandatory(true);

		fileNameEl = uifactory.addTextElement("fileName", "task.file", 128, fileName, formLayout);
		fileNameEl.setMandatory(true);

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
		if (!StringHelper.containsNonWhitespace(titleEl.getValue())) {
			titleEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		}

		fileNameEl.clearError();
		if (!StringHelper.containsNonWhitespace(fileNameEl.getValue())) {
			fileNameEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		} else if (!FileUtils.validateFilename(fileNameEl.getValue())) {
			fileNameEl.setErrorKey("error.file.invalid", null);
			allOk &= false;
		} else {
			VFSItem item = solutionContainer.resolve(fileNameEl.getValue());
			if (item != null && item.exists()) {
				fileNameEl.setErrorKey("error.file.exists", new String[]{fileNameEl.getValue()});
				allOk &= false;
			}
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fileName = fileNameEl.getValue();
		solution = new Solution();
		solution.setTitle(titleEl.getValue());
		solution.setFilename(fileName);
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
