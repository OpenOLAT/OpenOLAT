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
package org.olat.group.ui.wizard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.Emailer;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.group.BusinessGroup;
import org.olat.modules.co.ContactForm;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BGEmailCompositionStepController extends StepFormBasicController   {
	
	private ContactForm contactForm;
	private final List<BusinessGroup> groups;
	private final BaseSecurity securityManager;
	
	public BGEmailCompositionStepController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, List<BusinessGroup> groups) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "wrapper");
		
		securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		this.groups = groups;

		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		List<SecurityGroup> secGroups = new ArrayList<SecurityGroup>();
		
		Boolean sendToTutorObj = (Boolean)getFromRunContext("tutors");
		boolean sendToTutors = sendToTutorObj == null ? false : sendToTutorObj.booleanValue();
		for(BusinessGroup group:groups) {
			if(group.getOwnerGroup() != null && sendToTutors) {
				secGroups.add(group.getOwnerGroup());
			}
		}
		
		Boolean sendToParticipantObj = (Boolean)getFromRunContext("participants");
		boolean sendToParticipants = sendToParticipantObj == null ? false : sendToParticipantObj.booleanValue();
		for(BusinessGroup group:groups) {
			if(group.getPartipiciantGroup() != null && sendToParticipants) {
				secGroups.add(group.getPartipiciantGroup());
			}
		}

		List<Identity> receveirs = securityManager.getIdentitiesOfSecurityGroups(secGroups);
		ContactList contacts = new ContactList("mails");
		contacts.addAllIdentites(receveirs);
		contactForm = new ContactForm(ureq, getWindowControl(), mainForm, getIdentity(), false, false, false, false);
		contactForm.addEmailTo(contacts);
		formLayout.add("wrapped", contactForm.getInitialFormItem());
	}

	@Override
	protected void doDispose() {
		contactForm.dispose();
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		return contactForm.validateFormLogic(ureq);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean success = false;
		try {
			Emailer emailer = new Emailer(getIdentity(), false);
			List<File> attachments = contactForm.getAttachments();	
			MailContext context = new MailContextImpl(getWindowControl().getBusinessControl().getAsString());
			success = emailer.sendEmail(context, contactForm.getEmailToContactLists(), contactForm.getSubject(), contactForm.getBody(), attachments);
			if(contactForm.isTcpFrom()) {
				success = emailer.sendEmailCC(context, contactForm.getEmailFrom(), contactForm.getSubject(), contactForm.getBody(), attachments);
			}
		} catch (AddressException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		
		if(success) {
			System.out.println("Youppi");
		}
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}
