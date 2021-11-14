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
package org.olat.modules.quality.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.modules.co.ContactFormController;
import org.olat.modules.quality.QualityModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25.10.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SuggestionController extends BasicController {
	
	private ContactFormController contactFormCtrl;

	@Autowired
	private QualityModule qualityModule;

	public SuggestionController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		ContactMessage contactMessage = new ContactMessage(ureq.getIdentity());

		ContactList contactList = new ContactList(translate("suggstion.recipients"));
		List<String> emailAddresses = qualityModule.getSuggestionEmailAddresses();
		for (String emailAddress : emailAddresses) {
			contactList.add(emailAddress);
		}
		contactMessage.addEmailTo(contactList);
		
		String emailSubject = qualityModule.getSuggestionEmailSubject();
		contactMessage.setSubject(emailSubject);
		
		String emailBody = qualityModule.getSuggestionEmailBody();
		contactMessage.setBodyText(emailBody);

		contactFormCtrl = new ContactFormController(ureq, getWindowControl(), false, false, false, contactMessage);
		contactFormCtrl.setContactFormTitle(translate("suggestion.title"));
		contactFormCtrl.setContactFormDescription(translate("suggestion.description"));
		listenTo(contactFormCtrl);
		putInitialPanel(contactFormCtrl.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
