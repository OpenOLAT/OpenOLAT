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
package org.olat.core.commons.services.help;

import org.olat.admin.help.ui.HelpAdminController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.modules.co.ContactFormController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date: 27.04.2020 <br>
 * 
 * @author aboeckle, alexander.boeckle@frentix.com, frentix GmbH, http://www.frentix.com
 */
public class HelpSupportController extends BasicController implements GenericEventListener {

	private final VelocityContainer content;
	private ContactFormController contactForm;
	private String contactEmail = null;

	@Autowired
	private HelpModule helpModule;

	/**
	 * Creates this controller.
	 * 
	 * @param ureq The user request.
	 * @param control The window control.
	 */
	public HelpSupportController(UserRequest ureq, WindowControl control) {
		super(ureq, control);
		setTranslator(Util.createPackageTranslator(HelpAdminController.class, getLocale()));
		this.content = createVelocityContainer("contact");

		// Read the destination email from the helpModule
		contactEmail = helpModule.getSupportEmail();

		ContactMessage contactMessage = new ContactMessage(ureq.getIdentity());
		ContactList contactList = new ContactList(translate("contact.to"));

		contactList.add(contactEmail);
		contactMessage.addEmailTo(contactList);

		// Show GUI
		contactForm = new ContactFormController(ureq, getWindowControl(), true, false, false, contactMessage);
		listenTo(contactForm);
		content.put("contactForm", contactForm.getInitialComponent());
		putInitialPanel(content);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// Do nothing.
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == contactForm) {
			if (event.equals(Event.CANCELLED_EVENT)) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			} else if(event.equals(Event.DONE_EVENT)) {
				fireEvent(ureq, Event.DONE_EVENT);

			}
		}
		super.event(ureq, source, event);
	}

	@Override
	public void event(Event event) {
		// nothing to do, the persisted properties used in this controller are
		// read-only, no GUI to modify the properties yet
	}
}
