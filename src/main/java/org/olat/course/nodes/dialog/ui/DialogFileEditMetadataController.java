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
package org.olat.course.nodes.dialog.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.FileUtils;
import org.olat.course.nodes.dialog.DialogElement;
import org.olat.course.nodes.dialog.DialogElementsManager;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Controller for editing metadata of dialog files
 * <p>
 * Initial date: Jun 14, 2023
 *
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class DialogFileEditMetadataController extends FormBasicController {

	private final String subIdent;
	private final RepositoryEntry entry;
	private final DialogElement element;
	private TextElement fileNameEl;
	private TextElement authoredByEl;

	@Autowired
	DialogElementsManager dialogElementsManager;

	public DialogFileEditMetadataController(UserRequest ureq, WindowControl wControl, DialogElement element,
											String subIdent, RepositoryEntry entry) {
		super(ureq, wControl);
		this.element = element;
		this.subIdent = subIdent;
		this.entry = entry;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		fileNameEl = uifactory.addTextElement("filename", "dialog.metadata.filename", 256, null, formLayout);
		fileNameEl.setValue(element.getFilename());
		authoredByEl = uifactory.addTextElement("authoredby", "dialog.metadata.authored.by", 256, null, formLayout);
		authoredByEl.setValue(element.getAuthoredBy());
		authoredByEl.setMaxLength(256);

		// buttons
		FormLayoutContainer buttonLayoutCont = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		formLayout.add(buttonLayoutCont);
		uifactory.addFormSubmitButton("save", buttonLayoutCont);
		uifactory.addFormCancelButton("cancel", buttonLayoutCont, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean isInputValid = super.validateFormLogic(ureq);

		String filename = getFileName();

		if (!element.getFilename().equals(filename) && dialogElementsManager.hasDialogElementByFilename(filename, subIdent, entry)) {
			fileNameEl.setErrorKey("dialog.metadata.filename.error.duplicate");
			isInputValid = false;
		} else if (!FileUtils.validateFilename(filename)) {
			fileNameEl.setErrorKey("dialog.metadata.filename.error");
			isInputValid = false;
		}

		return isInputValid;
	}

	public String getFileName() {
		String filename;
		// build up full filename with extension if necessary
		if (!FileUtils.getFileSuffix(element.getFilename()).equals(FileUtils.getFileSuffix(fileNameEl.getValue()))) {
			filename = fileNameEl.getValue() + "." + FileUtils.getFileSuffix(element.getFilename());
		} else {
			filename = fileNameEl.getValue();
		}
		return filename;
	}

	public String getAuthoredBy() {
		return authoredByEl.getValue();
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
