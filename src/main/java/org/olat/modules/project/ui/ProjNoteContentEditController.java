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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement.TextAreaAutosaveEvent;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjNoteRef;
import org.olat.modules.project.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjNoteContentEditController extends FormBasicController {
	
	private TextElement titleEl;
	private TextAreaElement textEl;

	private final ProjNoteRef note;
	private final String lockEntryKey;
	private String title;
	private String text;
	
	@Autowired
	private ProjectService projectService;

	public ProjNoteContentEditController(UserRequest ureq, WindowControl wControl, Form mainForm, ProjNote note, String lockEntryKey) {
		super(ureq, wControl, LAYOUT_VERTICAL, null, mainForm);
		this.note = note;
		this.lockEntryKey = lockEntryKey;
		this.title = note.getTitle();
		this.text = note.getText();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		titleEl = uifactory.addTextElement("title", 80, title, formLayout);
		titleEl.addActionListener(FormEvent.ONCHANGE);
		
		textEl = uifactory.addTextAreaElement("note.text", "note.text", -1, 4, 1, true, false, text, formLayout);
		textEl.addActionListener(FormEvent.ONCHANGE);
		textEl.setAutosave(true);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (titleEl == source) {
			title = titleEl.getValue();
			doSave();
		} else if (textEl == source) {
			if (event instanceof TextAreaAutosaveEvent) {
				text = ((TextAreaAutosaveEvent)event).getText();
				doSave();
			} else {
				text = textEl.getValue();
				doSave();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	public void doSave() {
		projectService.updateNote(getIdentity(), note, lockEntryKey, title, text);
	}

}
