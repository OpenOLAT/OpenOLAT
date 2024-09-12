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
package org.olat.gui.demo.guidemo;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.MarkdownElement;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

/**
 * Initial date: Jan 6, 2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class GuiDemoTextareaController extends FormBasicController {
	
	private TextAreaElement stripedBackgroundAndLineNumbersEl;
	private MarkdownElement markdownInputEl;
	private MarkdownElement markdownOutputEl;

	public GuiDemoTextareaController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("guidemo.textarea.title");
		
		stripedBackgroundAndLineNumbersEl = uifactory.addTextAreaElement("stripedAndLineNumbers", "guidemo.textarea.striped.line.numbers.label", -1, 10, -1, false, true, null, formLayout);
		stripedBackgroundAndLineNumbersEl.setOriginalLineBreaks(true);
		stripedBackgroundAndLineNumbersEl.setStripedBackgroundEnabled(true);
		stripedBackgroundAndLineNumbersEl.setLineNumbersEnbaled(true);
		stripedBackgroundAndLineNumbersEl.setEnabled(true);
		
		List<Integer> errors = new ArrayList<>();
		errors.add(4);
		errors.add(10);
		errors.add(60);
		errors.add(100);
		errors.add(1000);
		errors.add(1500);
		
		stripedBackgroundAndLineNumbersEl.setErrors(errors);

		markdownInputEl = uifactory.addMarkdownElement("markdownInput",
				"guidemo.textarea.markdown.input", "", formLayout);
		markdownInputEl.setPlaceholderKey("guidemo.textarea.markdown.input.placeholder", null);
		markdownInputEl.addActionListener(FormEvent.ONCHANGE);
		markdownInputEl.setAutosave(true);

		markdownOutputEl = uifactory.addMarkdownElement("markdownOutput",
				"guidemo.textarea.markdown.output", "", formLayout);
		markdownOutputEl.setEnabled(false);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == markdownInputEl) {
			if (event instanceof MarkdownElement.MarkdownAutosaveEvent markdownAutosaveEvent) {
				markdownOutputEl.setValue(markdownAutosaveEvent.getText());
			} else {
				markdownOutputEl.setValue(markdownInputEl.getValue());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

}
