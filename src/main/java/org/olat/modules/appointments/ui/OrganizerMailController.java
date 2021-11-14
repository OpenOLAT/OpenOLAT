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
package org.olat.modules.appointments.ui;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.modules.appointments.Organizer;
import org.olat.modules.appointments.Topic;
import org.olat.modules.co.ContactFormController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 15 May 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OrganizerMailController extends BasicController {
	
	private ContactFormController contactFormCtrl;
	
	@Autowired
	private BaseSecurityManager securityManager;

	public OrganizerMailController(UserRequest ureq, WindowControl wControl, Topic topic,
			Collection<Organizer> organizers) {
		super(ureq, wControl);
		ContactMessage contactMessage = new ContactMessage(ureq.getIdentity());

		ContactList contactList = new ContactList(translate("email.organizer.recipients"));
		List<Long> organizerKeys = organizers.stream()
				.map(organizer -> organizer.getIdentity().getKey())
				.collect(Collectors.toList());
		List<Identity> identities = securityManager.loadIdentityByKeys(organizerKeys);
		contactList.addAllIdentites(identities);
		contactMessage.addEmailTo(contactList);
		
		contactMessage.setSubject(translate("email.organizer.subject", new String[] {topic.getTitle()} ));

		contactFormCtrl = new ContactFormController(ureq, getWindowControl(), true, false, false, contactMessage);
		contactFormCtrl.setContactFormTitle(null);
		listenTo(contactFormCtrl);
		putInitialPanel(contactFormCtrl.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == contactFormCtrl) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

}
