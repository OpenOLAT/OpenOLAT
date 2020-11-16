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
package org.olat.registration;

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Formatter;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.modules.co.ContactFormController;
import org.olat.user.UserModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The panel to request per E-mail the deletion of it's account.
 * 
 * 
 * Initial date: 4 févr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RequestAccountDeletionController extends FormBasicController {
	
	private FormLink requestButton;
	
	private CloseableModalController cmc;
	private ContactFormController contactCtrl;
	
	@Autowired
	private UserModule userModule;
	@Autowired
	private RegistrationManager registrationManager;
	
	public RequestAccountDeletionController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "request_delete");
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			Date confirmationDate = registrationManager.getDisclaimerConfirmationDate(getIdentity());
			if(confirmationDate != null) {
				String date = Formatter.getInstance(getLocale()).formatDate(confirmationDate);
				String time = Formatter.getInstance(getLocale()).formatTimeShort(confirmationDate);
				layoutCont.contextPut("title", translate("request.delete.account.title.date", new String[] { date, time }));
			}
			layoutCont.contextPut("text", translate("request.delete.account.text"));
		}
		
		requestButton = uifactory.addFormLink("request.delete.account", formLayout, Link.BUTTON);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(contactCtrl == source) {
			cmc.deactivate();
			cleanUp();
			if(event == Event.DONE_EVENT) {
				fireEvent(ureq, Event.DONE_EVENT);
				showInfo("request.delete.account.sent");
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(contactCtrl);
		removeAsListenerAndDispose(cmc);
		contactCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(requestButton == source) {
			doRequestDeletion(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doRequestDeletion(UserRequest ureq) {
		doOpenContactForm(ureq);
	}
	
	private void doOpenContactForm(UserRequest ureq) {
		if(contactCtrl != null) return;
		
		Identity identity = getIdentity();
		String[] args = new String[] {
			identity.getKey().toString(),											// 0
			identity.getName(),														// 1
			identity.getUser().getProperty(UserConstants.FIRSTNAME, getLocale()),	// 2
			identity.getUser().getProperty(UserConstants.LASTNAME, getLocale())		// 3
		};
		ContactMessage contactMessage = new ContactMessage(identity);
		contactMessage.setSubject(translate("request.delete.account.subject", args));
		contactMessage.setBodyText(translate("request.delete.account.body", args));
		
		String mailAddress = userModule.getMailToRequestAccountDeletion();
		ContactList contact = new ContactList(translate("request.delete.email.name"));
		contact.add(mailAddress);
		contactMessage.addEmailTo(contact);

		contactCtrl = new ContactFormController(ureq, getWindowControl(), true, false, false, contactMessage);
		listenTo(contactCtrl);
		
		String title = translate("request.delete.account");
		cmc = new CloseableModalController(getWindowControl(), "c", contactCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
}
