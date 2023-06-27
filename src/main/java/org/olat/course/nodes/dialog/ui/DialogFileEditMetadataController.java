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
import org.olat.course.nodes.dialog.DialogElement;

/**
 * Controller for editing metadata of dialog files
 * <p>
 * Initial date: Jun 14, 2023
 *
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class DialogFileEditMetadataController extends FormBasicController {

	private final DialogElement element;
	private TextElement fileNameEl;
	private TextElement authoredByEl;

	public DialogFileEditMetadataController(UserRequest ureq, WindowControl wControl, DialogElement element) {
		super(ureq, wControl);
		this.element = element;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		fileNameEl = uifactory.addTextElement("filename", "dialog.metadata.filename", 256, null, formLayout);
		fileNameEl.setValue(element.getFilename().replaceAll("\\..*", ""));
		authoredByEl = uifactory.addTextElement("authoredby", "dialog.metadata.authored.by", 256, null, formLayout);
		authoredByEl.setValue(element.getAuthoredBy());

		// buttons
		FormLayoutContainer buttonLayoutCont = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		formLayout.add(buttonLayoutCont);
		uifactory.addFormSubmitButton("save", buttonLayoutCont);
		uifactory.addFormCancelButton("cancel", buttonLayoutCont, ureq, getWindowControl());
	}


	public String getFileName() {
		return fileNameEl.getValue();
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
