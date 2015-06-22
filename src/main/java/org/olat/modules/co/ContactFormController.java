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

import javax.mail.Address;
import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;

import org.olat.core.CoreSpringFactory;
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
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailLoggingAction;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;

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
 * TODO:pb:b refactor ContactFormController and ContactForm to extract a ContactMessageManager,
 * setSubject(..) setRecipients.. etc. should not be in the controller. Refactor to use ContactMessage!
 * @see org.olat.modules.co.ContactList
 * Initial Date: Jul 19, 2004
 * @author patrick
 */
public class ContactFormController extends BasicController {
	
	private static final OLog log = Tracing.createLoggerFor(ContactFormController.class);
	//
	private Identity emailFrom;
	
	private ContactForm cntctForm;
	private DialogBoxController noUsersErrorCtr;
	private ArrayList<String> myButtons;
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
		this.emailFrom = cmsg.getFrom();
		mailService = CoreSpringFactory.getImpl(MailManager.class);
		
		cntctForm = new ContactForm(ureq, windowControl, emailFrom, isReadonly,isCanceable,hasRecipientsEditable);
		listenTo(cntctForm);
		
		List<ContactList> recipList = cmsg.getEmailToContactLists();
		boolean hasAtLeastOneAddress = hasAtLeastOneAddress(recipList);
		cntctForm.setBody(cmsg.getBodyText());
		cntctForm.setSubject(cmsg.getSubject());
		
		//init display component
		init(ureq, hasAtLeastOneAddress, cmsg.getDisabledIdentities());
	}
	
	private boolean hasAtLeastOneAddress(List<ContactList> recipList) {
		boolean hasAtLeastOneAddress = false;
		if (recipList != null && recipList.size() > 0 ) {
			for (ContactList cl: recipList) {
				if (!hasAtLeastOneAddress && cl != null && cl.getEmailsAsStrings().size() > 0) {
					hasAtLeastOneAddress = true;
				}
				if (cl.getEmailsAsStrings().size() > 0) cntctForm.addEmailTo(cl);
			}
		}
		return hasAtLeastOneAddress;
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
		}
		else if (source == cntctForm) {
			if (event == Event.DONE_EVENT) {
				//
				boolean success = false;
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
					
					MailerResult result = mailService.sendMessage(bundle);
					success = result.isSuccessful();
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
						success = ccResult.isSuccessful();
					}
				} catch (Exception e) {
					//error in recipient email address(es)
					handleAddressException(success);
				}
				cntctForm.setDisplayOnly(true);
				if (success) {
					showInfo("msg.send.ok");
					// do logging
					ThreadLocalUserActivityLogger.log(MailLoggingAction.MAIL_SENT, getClass());
					fireEvent(ureq, Event.DONE_EVENT);
				} else {
					showInfo("error.msg.send.nok");
					fireEvent(ureq, Event.FAILED_EVENT);
				}
			} else if (event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
		}
	}

	/**
	 * handles events from Components <BR>
	 * creates an InfoMessage in the WindowController on error. <br>
	 * <b>Fires: </b>
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
	 * 
	 * @param ureq
	 * @param source
	 * @param event
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}

	/**
	 * @param success
	 */
	private void handleAddressException(boolean success) {
		StringBuilder errorMessage = new StringBuilder();
		if (success) {
			errorMessage.append(translate("error.msg.send.partially.nok"));
			errorMessage.append("<br />");
			errorMessage.append(translate("error.msg.send.invalid.rcps"));
		} else {
			errorMessage.append(translate("error.msg.send.nok"));
			errorMessage.append("<br />");
			errorMessage.append(translate("error.msg.send.553"));
		}
		this.getWindowControl().setError(errorMessage.toString());
	}

	/**
	 * handles the sendFailedException <p>generates an infoMessage
	 * 
	 * @param e
	 * @throws OLATRuntimeException
	 * return boolean true: handling was successful, exception can be ignored; 
	 * false: handling was not successful, refuse to proceed.
	 */
	public boolean handleSendFailedException(SendFailedException e) {
		//get wrapped excpetion
		MessagingException me = (MessagingException) e.getNextException();
		if (me instanceof AuthenticationFailedException) {
			// catch this one separately, this kind of exception has no message 
			// as the other below
			StringBuilder infoMessage = new StringBuilder();
			infoMessage.append(translate("error.msg.send.nok"));
			infoMessage.append("<br />");
			infoMessage.append(translate("error.msg.smtp.authentication.failed"));
			this.getWindowControl().setInfo(infoMessage.toString());			
			log.warn("Mail message could not be sent: ", e);
			// message could not be sent, however let user proceed with his action
			return true;
		}		
		String message = me.getMessage();
		if (message.startsWith("553")) {
			//javax.mail.MessagingException: 553 5.5.4 <invalid>... Domain name
			// required for sender address invalid@id.unizh.ch
			//javax.mail.MessagingException: 553 5.1.8 <invalid@invalid.>...
			// Domain of sender address invalid@invalid does not exist
			//...
			StringBuilder infoMessage = new StringBuilder();
			infoMessage.append(translate("error.msg.send.553"));
			showInfo(infoMessage.toString());

		} else if (message.startsWith("Invalid Addresses")) {
			//            javax.mail.SendFailedException: Sending failed;
			//              nested exception is:
			//                class javax.mail.SendFailedException: Invalid Addresses;
			//              nested exception is:
			//                class javax.mail.SendFailedException: 550 5.1.1 <dfgh>... User
			// unknownhandleSendFailedException
			StringBuilder infoMessage = new StringBuilder();
			infoMessage.append(translate("error.msg.send.nok"));
			infoMessage.append("<br />");
			infoMessage.append(translate("error.msg.send.invalid.rcps"));
			infoMessage.append(addressesArr2HtmlOList(e.getInvalidAddresses()));
			this.getWindowControl().setInfo(infoMessage.toString());
		} else if (message.startsWith("503 5.0.0")) {
			// message:503 5.0.0 Need RCPT (recipient) ,javax.mail.MessagingException
			StringBuilder infoMessage = new StringBuilder();
			infoMessage.append(translate("error.msg.send.nok"));
			infoMessage.append("<br />");
			infoMessage.append(translate("error.msg.send.no.rcps"));
			this.getWindowControl().setInfo(infoMessage.toString());
		} else if (message.startsWith("Unknown SMTP host")) {
			StringBuilder infoMessage = new StringBuilder();
			infoMessage.append(translate("error.msg.send.nok"));
			infoMessage.append("<br />");
			infoMessage.append(translate("error.msg.unknown.smtp", WebappHelper.getMailConfig("mailFrom")));
			this.getWindowControl().setInfo(infoMessage.toString());			
			log.warn("Mail message could not be sent: ", e);
			// message could not be sent, however let user proceed with his action
			return true;
		} else if (message.startsWith("Could not connect to SMTP host")){
			//could not connect to smtp host, no connection or connection timeout
			StringBuilder infoMessage = new StringBuilder();
			infoMessage.append(translate("error.msg.send.nok"));
			infoMessage.append("<br />");
			infoMessage.append(translate("error.msg.notconnectto.smtp", WebappHelper.getMailConfig("mailhost")));
			this.getWindowControl().setInfo(infoMessage.toString());			
			log.warn(null, e);
			// message could not be sent, however let user proceed with his action
			return true;
		}
		else {
			throw new OLATRuntimeException(ContactFormController.class, "" + cntctForm.getEmailTo(), e.getNextException());
		}
		// message could not be sent, return false
		return false;
	}

	/**
	 * converts an Address[] to an HTML ordered list
	 * 
	 * @param invalidAdr Address[] with invalid addresses
	 * @return StringBuilder
	 */
	private StringBuilder addressesArr2HtmlOList(Address[] invalidAdr) {
		StringBuilder iAddressesSB = new StringBuilder();
		if (invalidAdr != null && invalidAdr.length > 0) {
			iAddressesSB.append("<ol>");
			for (int i = 0; i < invalidAdr.length; i++) {
				iAddressesSB.append("<li>");
				iAddressesSB.append(invalidAdr[i].toString());
				iAddressesSB.append("</li>");
			}
			iAddressesSB.append("</ol>");
		}
		return iAddressesSB;
	}


	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		//
	}

}