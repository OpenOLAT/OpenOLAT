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

import java.io.File;

/**
 * Initial date: 2022-09-08<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class AVSubmissionDetailsController extends FormBasicController {

	private TextElement fileNameEl;
	private final File documentsDir;
	private String fileName;

	public AVSubmissionDetailsController(UserRequest ureq, WindowControl wControl, File documentsDir, String fileName) {
		super(ureq, wControl);
		this.documentsDir = documentsDir;
		this.fileName = fileName;
		initForm(ureq);
	}

	public String getFileName() {
		return fileName;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_course_gta_av_submission_details_form");

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

		fileNameEl.clearError();
		if (!StringHelper.containsNonWhitespace(fileNameEl.getValue())) {
			fileNameEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		} else if (!FileUtils.validateFilename(fileNameEl.getValue())) {
			fileNameEl.setErrorKey("error.file.invalid", null);
			allOk &= false;
		} else {
			File target = new File(documentsDir, fileNameEl.getValue());
			if (target.exists()) {
				fileNameEl.setErrorKey("error.file.exists", new String[] { fileNameEl.getValue() });
				allOk &= false;
			}
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fileName = fileNameEl.getValue();
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
