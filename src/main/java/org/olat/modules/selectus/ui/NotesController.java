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

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.ApplicationShort;
import org.olat.modules.selectus.model.Notes;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class NotesController extends FormBasicController {
	
	private TextElement notesElement;
	
	private Notes notes;
	private final String fullApplicatantName;
	private final String projectTitle;
	private final Long applicationKey;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RecruitingService recruitingService;
	
	public NotesController(UserRequest ureq, WindowControl wControl, Application application) {
		super(ureq, wControl, "edit_notes");

		this.applicationKey = application.getKey();
		this.fullApplicatantName = formatFullName(application, getTranslator());
		this.projectTitle = getPojectTitle(application);
		notes = recruitingService.getNotes(application.getKey(), getIdentity());
		
		initForm(ureq);
	}
	
	public NotesController(UserRequest ureq, WindowControl wControl, ApplicationLight application) {
		super(ureq, wControl, "edit_notes");

		this.applicationKey = application.getKey();
		this.fullApplicatantName = formatFullName(application, getTranslator());
		this.projectTitle = getPojectTitle(application);
		notes = recruitingService.getNotes(application.getKey(), getIdentity());
		
		initForm(ureq);
	}
	
	public Notes getNotes() {
		return notes;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String notesTitle = null;
		if (projectTitle == null) {
			notesTitle = translate("notes.forApplication", new String[]{ fullApplicatantName});				
		} else {
			notesTitle = translate("notes.forApplicationAndProject", new String[]{ fullApplicatantName, projectTitle});								
		}
		formLayout.contextPut("notesTitle", notesTitle);
		
		final FormLayoutContainer notesLayout = FormLayoutContainer.createVerticalFormLayout("notes_layout", getTranslator());
		formLayout.add(notesLayout);
		
		String content = notes == null ? null : notes.getContent();
		notesElement = uifactory.addTextAreaElement("notes", 12, 12, content, notesLayout);
		notesElement.setMaxLength(4000);
		notesElement.setLabel(null, null);

		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("submit", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
	
	/**
	 * Helper to extract a project title or NULL from the application
	 * @param application
	 * @return
	 */
	private String getPojectTitle(ApplicationShort application) {
		if (application.getProject() != null && StringHelper.containsNonWhitespace(application.getProject().getTitle())) {
			return application.getProject().getTitle();
		} else {
			return null;
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		String value = notesElement.getValue();
		notesElement.clearError();
		if(value != null && value.getBytes().length > 4000) {
			notesElement.setErrorKey("input.toolong", new String[]{ "4000" });
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String content = notesElement.getValue();
		if(notes == null) {
			notes = recruitingService.createNotes(applicationKey, getIdentity(), content);
		} else {
			notes = recruitingService.updateNotes(applicationKey, getIdentity(), content);
		}
		dbInstance.commit();
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}