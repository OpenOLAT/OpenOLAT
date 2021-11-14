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
import java.util.List;

import org.olat.basesecurity.GroupRoles;
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
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.co.ContactForm;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BGEmailCompositionStepController extends StepFormBasicController   {
	
	private ContactForm contactForm;
	private final List<BusinessGroup> groups;
	
	@Autowired
	private MailManager mailService;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	public BGEmailCompositionStepController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, List<BusinessGroup> groups) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "wrapper");
		this.groups = groups;
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		StringBuilder groupNames = new StringBuilder();
		for(int i=0; i<5 && i<groups.size(); i++) {
			if(groupNames.length() > 0) groupNames.append(" - ");
			groupNames.append(groups.get(i).getName());
		}
		
		String contactsName;
		if(groups.size() > 5) {
			String otherGroups = Integer.toString(groups.size() - 5);
			contactsName = translate("email.other.groups", new String[] { groupNames.toString(), otherGroups });
		} else {
			contactsName = groupNames.toString();
		}
		ContactList contacts = new ContactList(contactsName);
		
		Boolean sendToTutorObj = (Boolean)getFromRunContext("tutors");
		boolean sendToTutors = sendToTutorObj == null ? false : sendToTutorObj.booleanValue();
		if(sendToTutors) {
			List<Identity> coaches = businessGroupService.getMembers(groups, GroupRoles.coach.name());
			contacts.addAllIdentites(coaches);
		}
		
		Boolean sendToParticipantObj = (Boolean)getFromRunContext("participants");
		boolean sendToParticipants = sendToParticipantObj == null ? false : sendToParticipantObj.booleanValue();
		if(sendToParticipants) {
			List<Identity> participants = businessGroupService.getMembers(groups, GroupRoles.participant.name());
			contacts.addAllIdentites(participants);
		}

		contactForm = new ContactForm(ureq, getWindowControl(), mainForm, getIdentity(), false, false, false, false);
		contactForm.addEmailTo(contacts);
		formLayout.add("wrapped", contactForm.getInitialFormItem());
	}

	@Override
	protected void doDispose() {
		contactForm.dispose();
        super.doDispose();
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
			File[] attachments = contactForm.getAttachments();	
			MailContext context = new MailContextImpl(getWindowControl().getBusinessControl().getAsString());
			MailBundle bundle = new MailBundle();
			bundle.setContext(context);
			bundle.setFromId(getIdentity());
			bundle.setContactLists(contactForm.getEmailToContactLists());
			bundle.setContent(contactForm.getSubject(), contactForm.getBody(), attachments);
			
			MailerResult result = mailService.sendMessage(bundle);
			success = result.isSuccessful();
			if(contactForm.isTcpFrom()) {
				
				MailBundle ccBundle = new MailBundle();
				ccBundle.setContext(context);
				ccBundle.setFromId(getIdentity());
				ccBundle.setCc(getIdentity());
				ccBundle.setContent(contactForm.getSubject(), contactForm.getBody(), attachments);
				
				MailerResult ccResult = mailService.sendMessage(ccBundle);
				success = ccResult.isSuccessful();
			}
		} catch (Exception e) {
			logError(null, e);
		}
		
		if(success) {
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}
	}
}
