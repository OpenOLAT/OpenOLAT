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

import java.io.File;
import java.util.ArrayList;
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
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailLoggingAction;
import org.olat.core.util.mail.MailManager;
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
 * <b>Responsabilites: </b> <br>
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

	private Identity emailFrom;
	
	private ContactForm cntctForm;
	private DialogBoxController noUsersErrorCtr;
	private List<String> myButtons;
	private Object userObject;
	
	@Autowired
	private MailManager mailService;
	
	/**
	 * 
	 * @param ureq
	 * @param windowControl
	 * @param useDefaultTitle
	 * @param isCanceable
	 * @param isReadonly
	 * @param hasRecipientsEditable
	 * @param cmsg
	 */
	public ContactFormController(UserRequest ureq, WindowControl windowControl, boolean isCanceable, boolean isReadonly, boolean hasRecipientsEditable, ContactMessage cmsg) {
		super(ureq, windowControl);
		
		//init email form
		emailFrom = cmsg.getFrom();
		
		cntctForm = new ContactForm(ureq, windowControl, emailFrom, isReadonly,isCanceable,hasRecipientsEditable);
		listenTo(cntctForm);
		
		List<ContactList> recipList = cmsg.getEmailToContactLists();
		boolean hasAtLeastOneAddress = hasAtLeastOneAddress(recipList);
		cntctForm.setBody(cmsg.getBodyText());
		cntctForm.setSubject(cmsg.getSubject());
		
		//init display component
		init(ureq, hasAtLeastOneAddress, cmsg.getDisabledIdentities());
	}
	
	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}
	
	public void setContactFormTitle(String translatedTitle) {
		if(cntctForm != null) {
			cntctForm.setFormTranslatedTitle(translatedTitle);
		}
	}

	private boolean hasAtLeastOneAddress(List<ContactList> recipList) {
		boolean hasAtLeastOneAddress = false;
		if (recipList != null && recipList.size() > 0 ) {
			for (ContactList cl: recipList) {
				if (!hasAtLeastOneAddress && cl != null && cl.hasAddresses()) {
					hasAtLeastOneAddress = true;
				}
				if (cl.hasAddresses()) {
					cntctForm.addEmailTo(cl);
				}
			}
		}
		return hasAtLeastOneAddress;
	}
	
	public String getSubject() {
		if(cntctForm != null) {
			return cntctForm.getSubject();
		}
		return null;
	}
	
	public String getBody() {
		if(cntctForm != null) {
			return cntctForm.getBody();
		}
		return null;
	}

	/**
	 * @param useDefaultTitle
	 * @param hasAtLeastOneAddress
	 */
	private void init(UserRequest ureq, boolean hasAtLeastOneAddress, List<Identity> disabledIdentities) {
		if (hasAtLeastOneAddress) {
			putInitialPanel(cntctForm.getInitialComponent());	
		} else {
			Controller mCtr = MessageUIFactory.createInfoMessage(ureq, getWindowControl(), null, translate("error.msg.send.no.rcps"));
			listenTo(mCtr);// to be disposed as this controller gets disposed
			putInitialPanel(mCtr.getInitialComponent());
		}
		if(!hasAtLeastOneAddress | disabledIdentities.size() > 0){
			//show error that message can not be sent
			myButtons = new ArrayList<String>();
			myButtons.add(translate("back"));
			String title = "";
			String message = "";
			if(disabledIdentities.size() > 0) {
				title = MailHelper.getTitleForFailedUsersError(ureq.getLocale());
				message = MailHelper.getMessageForFailedUsersError(ureq.getLocale(), disabledIdentities);
			} else {
				title = translate("error.title.nousers");
				message = translate("error.msg.nousers");
			}
			noUsersErrorCtr = activateGenericDialog(ureq, title, message, myButtons, noUsersErrorCtr);
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
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
	
	private void doSend(UserRequest ureq) {

		MailerResult result;
		try {
			File[] attachments = cntctForm.getAttachments();
			MailContext context = new MailContextImpl(getWindowControl().getBusinessControl().getAsString());
			
			MailBundle bundle = new MailBundle();
			bundle.setContext(context);
			if (emailFrom == null) {
				// in case the user provides his own email in form						
				bundle.setFrom(cntctForm.getEmailFrom()); 
			} else {
				bundle.setFromId(emailFrom);						
			}
			bundle.setContactLists(cntctForm.getEmailToContactLists());
			bundle.setContent(cntctForm.getSubject(), cntctForm.getBody(), attachments);
			
			result = mailService.sendMessage(bundle);
			if(cntctForm.isTcpFrom()) {
				MailBundle ccBundle = new MailBundle();
				ccBundle.setContext(context);
				if (emailFrom == null) {
					// in case the user provides his own email in form
					ccBundle.setFrom(cntctForm.getEmailFrom()); 
					ccBundle.setTo(cntctForm.getEmailFrom()); 
				} else {
					ccBundle.setFromId(emailFrom); 
					ccBundle.setCc(emailFrom);							
				}
				ccBundle.setContent(cntctForm.getSubject(), cntctForm.getBody(), attachments);
				
				MailerResult ccResult = mailService.sendMessage(ccBundle);
				result.append(ccResult);
			}
			
			if(result != null) {
				if (result.isSuccessful()) {
					showInfo("msg.send.ok");
					// do logging
					ThreadLocalUserActivityLogger.log(MailLoggingAction.MAIL_SENT, getClass());
					fireEvent(ureq, Event.DONE_EVENT);
				} else {
					showError(ureq, result);
					fireEvent(ureq, Event.FAILED_EVENT);
				}
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
			warnings.append("<br>").append(warnings);
		}
		getWindowControl().setError(error.toString());
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	@Override
	protected void doDispose() {
		//
	}
}