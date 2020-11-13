/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.modules.co;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailLoggingAction;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <b>Fires Event: </b>
 * <UL>
 * <LI><b>Event.DONE_EVENT: </B> <BR>
 * email was sent successfully by the underlying Email subsystem</LI>
 * <LI><b>Event.FAILED_EVENT: </B> <BR>
 * email was not sent correct by the underlying Email subsystem <BR>
 * email may be partially sent correct, but some parts failed.</LI>
 * <LI><b>Event.CANCELLED_EVENT: </B> <BR>
 * user interaction, i.e. canceled message creation</LI>
 * </UL>
 * <p>
 * <b>Consumes Events from: </b>
 * <UL>
 * <LI>ContactForm:</LI>
 * <UL>
 * <LI>Form.EVENT_FORM_CANCELLED</LI>
 * <LI>Form.EVENT_VALIDATION_OK</LI>
 * </UL>
 * </UL>
 * <P>
 * <b>Main Purpose: </b> is to provide an easy interface for <i>contact message
 * creation and sending </i> from within different OLAT bulding blocks.
 * <P>
 * <b>Responsibilities: </b> <br>
 * <UL>
 * <LI>supplies a workflow for creating and sending contact messages</LI>
 * <LI>works with the ContactList encapsulating the e-mail addresses in a
 * mailing list.</LI>
 * <LI>contact messages with pre-initialized subject and/or body</LI>
 * </UL>
 * <P>
 * @see org.olat.modules.co.ContactList
 * Initial Date: Jul 19, 2004
 * @author patrick
 */
public class ContactFormController extends BasicController {

	private final ContactForm cntctForm;
	private DialogBoxController noUsersErrorCtr;

	private Object userObject;
	private Identity emailFrom;
	private List<MailTemplate> templates;
	
	@Autowired
	private MailManager mailService;
	
	public ContactFormController(UserRequest ureq, WindowControl windowControl, boolean isCanceable, boolean isReadonly, boolean hasRecipientsEditable, ContactMessage cmsg) {
		this(ureq, windowControl, isCanceable, isReadonly, hasRecipientsEditable, cmsg, Collections.emptyList());
	}
	
	/**
	 * 
	 * @param ureq The user request
	 * @param windowControl The window control
	 * @param isCanceable true if the user can cancel
	 * @param isReadonly true if the panel is read only and mail cannot be send
	 * @param hasRecipientsEditable true if the user can edit the recipients
	 * @param cmsg The message (mandatory)
	 * @param template A template filled with variables (optional)
	 */
	public ContactFormController(UserRequest ureq, WindowControl windowControl, boolean isCanceable, boolean isReadonly, boolean hasRecipientsEditable,
			ContactMessage cmsg, MailTemplate template) {
		this(ureq, windowControl, isCanceable, isReadonly, hasRecipientsEditable, cmsg,
				template == null ? null : Collections.singletonList(template));
	}
	
	public ContactFormController(UserRequest ureq, WindowControl windowControl, boolean isCanceable, boolean isReadonly, boolean hasRecipientsEditable,
			ContactMessage cmsg, List<MailTemplate> templates) {
		super(ureq, windowControl);
		
		this.templates = templates;
		//init email form
		emailFrom = cmsg.getFrom();
		
		cntctForm = new ContactForm(ureq, windowControl, emailFrom, isReadonly, isCanceable, hasRecipientsEditable);
		listenTo(cntctForm);
		
		List<ContactList> recipList = cmsg.getEmailToContactLists();
		boolean hasAtLeastOneAddress = hasAtLeastOneAddress(recipList);
		
		if(templates != null && !templates.isEmpty()) {
			cntctForm.setTemplates(templates);
		}
		MailTemplate template = templates != null && templates.size() == 1 ? templates.get(0) : null;
		if(StringHelper.containsNonWhitespace(cmsg.getBodyText())) {
			cntctForm.setBody(cmsg.getBodyText());
		} else if(template != null && StringHelper.containsNonWhitespace(template.getBodyTemplate())) {
			cntctForm.setBody(template);
		}
		
		if(StringHelper.containsNonWhitespace(cmsg.getSubject())) {
			cntctForm.setSubject(cmsg.getSubject());
		} else if(template != null && StringHelper.containsNonWhitespace(template.getSubjectTemplate())) {
			cntctForm.setSubject(template.getSubjectTemplate());
		}
		
		//init display component
		init(ureq, hasAtLeastOneAddress, cmsg.getDisabledIdentities());
	}
	
	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}
	
	public String getAndRemoveTitle() {
		return cntctForm.getAndRemoveFormTitle();
	}
	
	public void setContactFormTitle(String translatedTitle) {
		cntctForm.setFormTranslatedTitle(translatedTitle);
	}
	
	public void setContactFormDescription(String translatedDescription) {
		cntctForm.setFormTranslatedDescription(translatedDescription);
	}

	private boolean hasAtLeastOneAddress(List<ContactList> recipList) {
		boolean hasAtLeastOneAddress = false;
		if (recipList != null && !recipList.isEmpty()) {
			for (ContactList cl: recipList) {
				if (!hasAtLeastOneAddress && cl != null && cl.hasAddresses()) {
					hasAtLeastOneAddress = true;
				}
				if (cl != null && cl.hasAddresses()) {
					cntctForm.addEmailTo(cl);
				}
			}
		}
		return hasAtLeastOneAddress;
	}
	
	public String getSubject() {
		return cntctForm.getSubject();
	}
	
	public String getBody() {
		return cntctForm.getBody();
	}
	
	public void setRecipientsLists(List<ContactList> recipientsLists) {
		cntctForm.setRecipientsLists(recipientsLists);
	}

	private void init(UserRequest ureq, boolean hasAtLeastOneAddress, List<Identity> disabledIdentities) {
		if (hasAtLeastOneAddress) {
			putInitialPanel(cntctForm.getInitialComponent());	
		} else {
			Controller mCtr = MessageUIFactory.createInfoMessage(ureq, getWindowControl(), null, translate("error.msg.send.no.rcps"));
			listenTo(mCtr);// to be disposed as this controller gets disposed
			putInitialPanel(mCtr.getInitialComponent());
		}
		if(!hasAtLeastOneAddress || !disabledIdentities.isEmpty()){
			//show error that message can not be sent
			List<String> myButtons = new ArrayList<>();
			myButtons.add(translate("back"));
			String title = "";
			String message = "";
			if(!disabledIdentities.isEmpty()) {
				title = MailHelper.getTitleForFailedUsersError(ureq.getLocale());
				message = MailHelper.getMessageForFailedUsersError(ureq.getLocale(), disabledIdentities);
			} else {
				title = translate("error.title.nousers");
				message = translate("error.msg.nousers");
			}
			noUsersErrorCtr = activateGenericDialog(ureq, title, message, myButtons, noUsersErrorCtr);
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == noUsersErrorCtr) {
			if(event.equals(Event.CANCELLED_EVENT)) {
				// user has clicked the close button in the top-right corner
				fireEvent(ureq, Event.CANCELLED_EVENT);
			} else {
				// user has clicked the cancel button
				int pos = DialogBoxUIFactory.getButtonPos(event);
				if (pos == 0){
					// cancel button has been pressed, fire event to parent
					fireEvent(ureq, Event.CANCELLED_EVENT);
				}
			}
		} else if (source == cntctForm) {
			if (event == Event.DONE_EVENT) {
				doSend(ureq);
			} else if (event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
		}
	}
	
	private MailBundle createBundle(MailerResult result) {
		MailContext context = new MailContextImpl(getWindowControl().getBusinessControl().getAsString());
		MailBundle bundle;
		if(templates == null || templates.isEmpty()) {
			bundle = new MailBundle(context);
			bundle.setContent(cntctForm.getSubject(), cntctForm.getBody(), cntctForm.getAttachments());
		} else {
			MailTemplate template;
			if(templates.size() == 1) {
				template = templates.get(0);
			} else {
				template = cntctForm.getTemplate();
			}
			template.setSubjectTemplate(cntctForm.getSubject());
			template.setBodyTemplate(cntctForm.getBody());
			template.setAttachments(cntctForm.getAttachments());
			bundle = mailService.makeMailBundle(context, null, template, null, null, result);
		}
		return bundle;
	}
	
	private void doSend(UserRequest ureq) {

		MailerResult result = new MailerResult();
		try {

			MailBundle bundle = createBundle(result);
			if (emailFrom == null) {
				// in case the user provides his own email in form
				bundle.setFrom(cntctForm.getEmailFrom()); 
			} else {
				bundle.setFromId(emailFrom);
			}
			bundle.setContactLists(cntctForm.getEmailToContactLists());
			
			MailerResult sendResult = mailService.sendMessage(bundle);
			result.append(sendResult);
			
			if(cntctForm.isTcpFrom()) {
				MailBundle ccBundle = createBundle(result);
				if (emailFrom == null) {
					// in case the user provides his own email in form
					ccBundle.setFrom(cntctForm.getEmailFrom()); 
					ccBundle.setTo(cntctForm.getEmailFrom()); 
				} else {
					ccBundle.setFromId(emailFrom); 
					ccBundle.setCc(emailFrom);							
				}
				
				MailerResult ccResult = mailService.sendMessage(ccBundle);
				result.append(ccResult);
			}

			if (result.isSuccessful()) {
				showInfo("msg.send.ok");
				// do logging
				ThreadLocalUserActivityLogger.log(MailLoggingAction.MAIL_SENT, getClass());
				fireEvent(ureq, Event.DONE_EVENT);
			} else {
				showError(ureq, result);
				fireEvent(ureq, Event.FAILED_EVENT);
			}
		} catch (Exception e) {
			logError("", e);
			showWarning("error.msg.send.nok");
		}
		cntctForm.setDisplayOnly(true);
	}
	
	private void showError(UserRequest ureq, MailerResult result) {
		StringBuilder errors = new StringBuilder(1024);
		StringBuilder warnings = new StringBuilder(1024);
		Roles roles = ureq.getUserSession().getRoles();
		boolean detailedErrorOutput = roles.isAdministrator() || roles.isSystemAdmin();
		MailHelper.appendErrorsAndWarnings(result, errors, warnings, detailedErrorOutput, getLocale());

		StringBuilder error = new StringBuilder(1024);
		error.append(translate("error.msg.send.nok"));
		if(errors.length() > 0) {
			error.append("<br>").append(errors);
		}
		if(warnings.length() > 0) {
			error.append("<br>").append(warnings);
		}
		getWindowControl().setError(error.toString());
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}
}