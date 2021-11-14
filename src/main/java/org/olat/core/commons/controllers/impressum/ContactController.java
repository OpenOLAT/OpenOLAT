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
package org.olat.core.commons.controllers.impressum;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.modules.co.ContactFormController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <h3>Description:</h3> This controller shows a contact form and has a
 * user-configurable destination e-mail address which it reads from the file
 * <code>olatdata/system/configuration/contact.properties</code> 
 * 
 * Initial Date: Aug 10, 2009 <br>
 * 
 * @author twuersch, frentix GmbH, http://www.frentix.com
 */
public class ContactController extends BasicController implements GenericEventListener {

	private final VelocityContainer content;
	private ContactFormController contactForm;
	private String contactEmail = null;
	
	@Autowired
	private ImpressumModule impressumModule;

	/**
	 * Creates this controller.
	 * 
	 * @param ureq The user request.
	 * @param control The window control.
	 */
	public ContactController(UserRequest ureq, WindowControl control) {
		super(ureq, control);
		this.content = createVelocityContainer("contact");

		// Read the destination email from the impressModule
		contactEmail = impressumModule.getContactMail();

		// Initialize a few contact list management objects.
		ContactMessage contactMessage = new ContactMessage(ureq.getIdentity());
		ContactList contactList = new ContactList(translate("contact.to"));

		contactList.add(contactEmail);
		contactMessage.addEmailTo(contactList);

		// Show GUI
		contactForm = new ContactFormController(ureq, getWindowControl(), false, false, false, contactMessage);
		listenTo(contactForm);
		content.put("contactForm", contactForm.getInitialComponent());
		putInitialPanel(content);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
	// Do nothing.
	}

	/**
	 * @see org.olat.core.util.event.GenericEventListener#event(org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(Event event) {
	// nothing to do, the persisted properties used in this controller are
	// read-only, no GUI to modify the properties yet
	}
}
