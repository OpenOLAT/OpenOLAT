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
package org.olat.modules.portfolio.ui;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.Invitation;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.portfolio.manager.InvitationDAO;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InvitationEditRightsController extends FormBasicController {
	
	private FormLink removeLink;
	private TextElement firstNameEl, lastNameEl, mailEl;
	
	private Binder binder;
	private Identity invitee;
	private Invitation invitation;
	
	@Autowired
	private MailManager mailManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private InvitationDAO invitationDao;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private PortfolioService portfolioService;
	
	public InvitationEditRightsController(UserRequest ureq, WindowControl wControl, Binder binder) {
		super(ureq, wControl);
		this.binder = binder;
		invitation = invitationDao.createInvitation();
		initForm(ureq);
	}
	
	public InvitationEditRightsController(UserRequest ureq, WindowControl wControl, Binder binder, Identity invitee) {
		super(ureq, wControl);
		this.binder = binder;
		this.invitee = invitee;
		invitation = invitationDao.findInvitation(binder.getBaseGroup(), invitee);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		firstNameEl = uifactory.addTextElement("firstName", "firstName", 64, invitation.getFirstName(), formLayout);
		firstNameEl.setMandatory(true);
		
		lastNameEl = uifactory.addTextElement("lastName", "lastName", 64, invitation.getLastName(), formLayout);
		lastNameEl.setMandatory(true);
		
		mailEl = uifactory.addTextElement("mail", "mail", 128, invitation.getMail(), formLayout);
		mailEl.setMandatory(true);
		mailEl.setNotEmptyCheck("map.share.empty.warn");
			
		if(StringHelper.containsNonWhitespace(invitation.getMail()) && MailHelper.isValidEmailAddress(invitation.getMail())) {
			SecurityGroup allUsers = securityManager.findSecurityGroupByName(Constants.GROUP_OLATUSERS);
			Identity currentIdentity = userManager.findIdentityByEmail(invitation.getMail());
			if(currentIdentity != null && securityManager.isIdentityInSecurityGroup(currentIdentity, allUsers)) {
				mailEl.setErrorKey("map.share.with.mail.error.olatUser", new String[]{ invitation.getMail() });
			}
		}
			
		String link = getInvitationLink();
		StaticTextElement linkEl = uifactory.addStaticTextElement("invitation.link" , link, formLayout);
		linkEl.setLabel("invitation.link", null);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		buttonsCont.setRootForm(mainForm);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		if(invitation.getKey() != null) {
			removeLink = uifactory.addFormLink("remove", buttonsCont, Link.BUTTON);
		}
		uifactory.addFormSubmitButton("save", buttonsCont);
	}
	
	private String getInvitationLink() {
		return Settings.getServerContextPathURI() + "/url/BinderInvitation/" + binder.getKey() + "?invitation=" + invitation.getToken();
	}
	
	@Override
	protected void doDispose() {
		// 
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
	
		if (mailEl != null) {
			String mail = mailEl.getValue();
			if (StringHelper.containsNonWhitespace(mail)) {
				if (MailHelper.isValidEmailAddress(mail)) {
					SecurityGroup allUsers = securityManager.findSecurityGroupByName(Constants.GROUP_OLATUSERS);
					Identity currentIdentity = userManager.findIdentityByEmail(mail);
					if (currentIdentity != null && securityManager.isIdentityInSecurityGroup(currentIdentity, allUsers)) {
						mailEl.setErrorKey("map.share.with.mail.error.olatUser", new String[] { mail });
						allOk &= false;
					}
				} else {
					mailEl.setErrorKey("error.mail.invalid", null);
					allOk &= false;
				}
			} else {
				mailEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
		}
		
		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		
		
		if(invitation.getKey() == null) {
			invitation.setFirstName(firstNameEl.getValue());
			invitation.setLastName(lastNameEl.getValue());
			invitation.setMail(mailEl.getValue());
			invitee = invitationDao.createIdentityAndPersistInvitation(invitation, binder.getBaseGroup(), getLocale());
			sendInvitation();
			fireEvent(ureq, Event.DONE_EVENT);
		} else {
			invitationDao.update(invitation, firstNameEl.getValue(), lastNameEl.getValue(), mailEl.getValue());
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(removeLink == source) {
			doRemoveInvitation();
			fireEvent(ureq, Event.DONE_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doRemoveInvitation() {
		portfolioService.removeAccessRights(binder, invitee);
		invitationDao.deleteInvitation(invitation);
	}

	private void sendInvitation() {
		String inviteeEmail = invitee.getUser().getProperty(UserConstants.EMAIL, getLocale());
		ContactList contactList = new ContactList(inviteeEmail);
		contactList.add(inviteeEmail);
		String busLink = getInvitationLink();

		boolean success = false;
		try {
			String first = getIdentity().getUser().getProperty(UserConstants.FIRSTNAME, null);
			String last = getIdentity().getUser().getProperty(UserConstants.LASTNAME, null);
			String sender = first + " " + last;
			String[] bodyArgs = new String[]{busLink, sender};

			MailContext context = new MailContextImpl(binder, null, getWindowControl().getBusinessControl().getAsString()); 
			MailBundle bundle = new MailBundle();
			bundle.setContext(context);
			bundle.setFrom(WebappHelper.getMailConfig("mailReplyTo"));
			bundle.setContactList(contactList);
			bundle.setContent(translate("invitation.mail.subject"), translate("invitation.mail.body", bodyArgs));

			MailerResult result = mailManager.sendExternMessage(bundle, null, true);
			success = result.isSuccessful();
		} catch (Exception e) {
			logError("Error on sending invitation mail to contactlist, invalid address.", e);
		}
		if (success) {
			showInfo("invitation.mail.success");
		}	else {
			showError("invitation.mail.failure");			
		}
	}
}
