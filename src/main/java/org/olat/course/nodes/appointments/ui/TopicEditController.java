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
package org.olat.course.nodes.appointments.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.appointments.AppointmentsSecurityCallback;
import org.olat.course.nodes.appointments.AppointmentsService;
import org.olat.course.nodes.appointments.Topic;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 Apr 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TopicEditController extends FormBasicController {
	
	private TextElement titleEl;
	private TextElement descriptionEl;
	
	private RepositoryEntry entry;
	private String subIdent;
	private AppointmentsSecurityCallback secCallback;
	private Topic topic;
	
	@Autowired
	private AppointmentsService appoinmentsService;

	public TopicEditController(UserRequest ureq, WindowControl wControl, AppointmentsSecurityCallback secCallback,
			RepositoryEntry entry, String subIdent) {
		super(ureq, wControl);
		this.secCallback = secCallback;
		this.entry = entry;
		this.subIdent = subIdent;

		initForm(ureq);
	}
	
	public TopicEditController(UserRequest ureq, WindowControl wControl, Topic topic,
			AppointmentsSecurityCallback secCallback) {
		super(ureq, wControl);
		this.secCallback = secCallback;
		this.topic = topic;
		
		initForm(ureq);
	}
	
	public Topic getTopic() {
		return topic;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String title = topic == null ? "" : topic.getTitle();
		titleEl = uifactory.addTextElement("topic.title", "topic.title", 128, title, formLayout);
		titleEl.setMandatory(true);
		if(!StringHelper.containsNonWhitespace(title)) {
			titleEl.setFocus(true);
		}
		
		String description = topic == null ? "" : topic.getDescription();
		descriptionEl = uifactory.addTextAreaElement("topic.description", "topic.description", 2000, 4, 72, false,
				false, description, formLayout);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		buttonsCont.setRootForm(mainForm);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		titleEl.clearError();
		if(!StringHelper.containsNonWhitespace(titleEl.getValue())) {
			titleEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSave();
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void doSave() {
		if (topic == null) {
			topic = appoinmentsService.createTopic(entry, subIdent);
			Identity organizer = secCallback.getDefaultOrganizer();
			if (organizer != null) {
				appoinmentsService.createOrganizer(topic, organizer);
			}
		}
		
		String title = titleEl.getValue();
		topic.setTitle(title);
		
		String description = descriptionEl.getValue();
		topic.setDescription(description);
		
		topic = appoinmentsService.updateTopic(topic);
	}

	@Override
	protected void doDispose() {
		//
	}

}
