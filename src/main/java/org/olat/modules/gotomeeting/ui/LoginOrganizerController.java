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
package org.olat.modules.gotomeeting.ui;

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
import org.olat.modules.gotomeeting.GoToMeetingManager;
import org.olat.modules.gotomeeting.GoToOrganizer;
import org.olat.modules.gotomeeting.model.GoToError;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This controller use the replacement of the "login direct" in the new
 * OAuth v2 interface (but without the OAuth round trip to logmein webpages).
 * After login successfully, the controller will create or update the organizer
 * based on the account key and organizer key.
 * 
 * 
 * Initial date: 22.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LoginOrganizerController extends FormBasicController {
	
	private TextElement accountLabelEl;
	private TextElement usernameEl;
	private TextElement passwordEl;
	
	private final Identity owner;
	private final GoToOrganizer organizer;
	
	@Autowired
	private GoToMeetingManager meetingManager;
	
	public LoginOrganizerController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		organizer = null;
		owner = null;
		initForm(ureq);
	}
	
	public LoginOrganizerController(UserRequest ureq, WindowControl wControl, Identity owner) {
		super(ureq, wControl);
		organizer = null;
		this.owner = owner;
		initForm(ureq);
	}
	
	public LoginOrganizerController(UserRequest ureq, WindowControl wControl, GoToOrganizer organizer) {
		super(ureq, wControl);
		this.organizer = organizer;
		this.owner = null;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String label = organizer == null ? "" : organizer.getName();
		accountLabelEl = uifactory.addTextElement("organizer.label", "organizer.label", 128, label, formLayout);
		String username = organizer == null ? "" : organizer.getUsername();
		usernameEl = uifactory.addTextElement("organizer.username", "organizer.username", 128, username, formLayout);
		passwordEl = uifactory.addPasswordElement("organizer.password", "organizer.password", 128, "", formLayout);
		passwordEl.setAutocomplete("new-password");
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("ok", buttonLayout);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		usernameEl.clearError();
		if(!StringHelper.containsNonWhitespace(usernameEl.getValue())) {
			usernameEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		}
		
		passwordEl.clearError();
		if(!StringHelper.containsNonWhitespace(passwordEl.getValue())) {
			passwordEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String name = accountLabelEl.getValue();
		String username = usernameEl.getValue();
		String password = passwordEl.getValue();
		GoToError error = new GoToError();
		if(meetingManager.createOrUpdateOrganizer(name, username, password, owner, error)) {
			fireEvent(ureq, Event.DONE_EVENT);
		} else {
			showWarning("error.code." + error.getErrorCode());
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
