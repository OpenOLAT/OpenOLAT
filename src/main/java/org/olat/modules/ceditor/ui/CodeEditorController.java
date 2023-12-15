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
package org.olat.modules.ceditor.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.ceditor.PageElementEditorController;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.model.CodeElement;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;

/**
 * Initial date: 2023-12-11<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CodeEditorController extends FormBasicController implements PageElementEditorController {

	private CodeElement code;
	private final PageElementStore<CodeElement> store;
	private TextAreaElement textAreaEl;


	public CodeEditorController(UserRequest ureq, WindowControl wControl, CodeElement code,
								PageElementStore<CodeElement> store) {
		super(ureq, wControl, "code_editor");
		this.code = code;
		this.store = store;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String content = code.getContent();

		textAreaEl = uifactory.addTextAreaElement("textArea", null, -1, 10, -1, false, true, content, formLayout);
		textAreaEl.setOriginalLineBreaks(true);
		textAreaEl.setStripedBackgroundEnabled(true);
		textAreaEl.setLineNumbersEnbaled(false);
		textAreaEl.setEnabled(true);
		textAreaEl.addActionListener(FormEvent.ONBLUR);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (textAreaEl == source) {
			doSave(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doUpdate() {
	}

	private void doSave(UserRequest ureq) {
		String content = textAreaEl.getValue();
		code.setContent(content);
		code = store.savePageElement(code);
		fireEvent(ureq, new ChangePartEvent(code));
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		//
	}
}
