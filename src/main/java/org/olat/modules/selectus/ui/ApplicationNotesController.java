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
package org.olat.modules.selectus.ui;

import static org.olat.modules.selectus.ui.RecruitingHelper.formatFullName;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Notes;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ApplicationNotesController extends FormBasicController {
	
	private FormLink editNotesLink;
	private StaticTextElement notesEl;

	private NotesController notesController;
	private CloseableModalController notesDialogBox;
	
	private Notes notes;
	private final Application application;
	@Autowired
	private RecruitingService erFrontendManager;
	
	public ApplicationNotesController(UserRequest ureq, WindowControl wControl, Application application, Form rootForm) {
		super(ureq, wControl, "app_notes");
		if(rootForm != null) {
			mainForm = rootForm;
			flc.setRootForm(rootForm);
			mainForm.addSubFormListener(this);
		}
		
		this.application = application;
		notes = erFrontendManager.getNotes(application.getKey(), getIdentity());
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer container = (FormLayoutContainer)formLayout;

			String fullName = formatFullName(getIdentity());
			String notesTitle = translate("notes.from", new String[]{fullName});
			container.contextPut("notesTitle", notesTitle);
			
			boolean hasNotes = notes != null && StringHelper.containsNonWhitespace(notes.getContent());
			
			String content;
			if(hasNotes) {
				content = notes.getContent();
				content = Formatter.escWithBR(content).toString();
			} else {
				content = translate("add.notes.comment");
			}
			notesEl = uifactory.addStaticTextElement("notes", null, content == null ? "" :  content, container);
			if(!hasNotes) {
				notesEl.setFocus(true);
			}
			container.add("notes", notesEl);
			container.contextPut("hasNotes", Boolean.valueOf(hasNotes));
			
			String buttonKey = notes != null && StringHelper.containsNonWhitespace(notes.getContent()) ? "edit.notes" : "create.notes";
			editNotesLink = uifactory.addFormLink("edit.notes", buttonKey, null, container, Link.BUTTON);
			container.add(editNotesLink);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == notesDialogBox) {
			removeAsListenerAndDispose(notesController);
			removeAsListenerAndDispose(notesDialogBox);
			notesController = null;
			notesDialogBox = null;	
		} else if (source == notesController) {
			//reload
			notesDialogBox.deactivate();
			removeAsListenerAndDispose(notesController);
			removeAsListenerAndDispose(notesDialogBox);
			notesController = null;
			notesDialogBox = null;
			updateNotes();
		} else {
			super.event(ureq, source, event);
		}
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == editNotesLink) {
			editNotes(ureq);
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void updateNotes() {
		notes = erFrontendManager.getNotes(application.getKey(), getIdentity());
		String content;
		String buttonKey;
		boolean hasNotes = notes != null && StringHelper.containsNonWhitespace(notes.getContent());
		if(hasNotes) {
			content = Formatter.escWithBR(notes.getContent()).toString();
			buttonKey = "edit.notes";
		} else {
			content = translate("add.notes.comment");
			buttonKey = "create.notes";
		}
		notesEl.setValue(content);

		flc.contextPut("hasNotes", Boolean.valueOf(hasNotes));
		editNotesLink.setI18nKey(buttonKey);
	}
	
	private void editNotes(UserRequest ureq) {
		removeAsListenerAndDispose(notesController);
		removeAsListenerAndDispose(notesDialogBox);
		notesController = new NotesController(ureq, getWindowControl(), application);
		listenTo(notesController);
		
		notesDialogBox = new CloseableModalController(getWindowControl(), "c", notesController.getInitialComponent(), translate("edit.notes"));
		notesDialogBox.activate();
		listenTo(notesDialogBox);		
	}
}