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

import org.olat.core.configuration.PersistedProperties;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.modules.co.ContactFormController;

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
	private static String contactEmail = null;

	/**
	 * Creates this controller.
	 * 
	 * @param ureq The user request.
	 * @param control The window control.
	 */
	public ContactController(UserRequest ureq, WindowControl control) {
		super(ureq, control);
		this.content = createVelocityContainer("contact");

		// load configuration only once
		if (contactEmail == null) {
			// Read the destination e-mail address from the configuration file.
			PersistedProperties contactConfiguration = new PersistedProperties(this);
			contactConfiguration.init();
			contactEmail = contactConfiguration.getStringPropertyValue("contact.to.address", true);
			if (!StringHelper.containsNonWhitespace(contactEmail)) {
				// fallback to standard email
				contactEmail = WebappHelper.getMailConfig("mailSupport");
				if (!StringHelper.containsNonWhitespace(contactEmail)) {
					throw new OLATRuntimeException(
							"could not find valid contact email address, configure property 'contact.to.address' in olatdata/system/configuration/"
									+ this.getClass().getName() + ".properties", null);
				} else {
					logInfo("Initialize impressum email with standard support address::" + contactEmail
							+ " You can configure a specific impressum email in the property 'contact.to.address' in olatdata/system/configuration/"
							+ this.getClass().getName() + ".properties");
				}
			} else {
				logInfo("Initialize impressum email with address::" + contactEmail);
			}
		}

		// Initialize a few contact list management objects.
		ContactMessage contactMessage = new ContactMessage(ureq.getIdentity());
		ContactList contactList = new ContactList(translate("contact.to"));

		contactList.add(contactEmail);
		contactMessage.addEmailTo(contactList);

		// Show GUI
		contactForm = new ContactFormController(ureq, getWindowControl(), false, false, false, contactMessage, null);
		listenTo(contactForm);
		content.put("contactForm", contactForm.getInitialComponent());
		putInitialPanel(content);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
	// autodispose by basic controller
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
	// Do nothing.
	}

	/**
	 * @see org.olat.core.util.event.GenericEventListener#event(org.olat.core.gui.control.Event)
	 */
	public void event(Event event) {
	// nothing to do, the persisted properties used in this controller are
	// read-only, no GUI to modify the properties yet
	}
}
