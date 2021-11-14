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
package org.olat.modules.grading.ui.wizard;

import java.util.Collections;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.MailTemplate;
import org.olat.modules.co.ContactForm;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ImportGraderMailController extends StepFormBasicController {
	
	private MailTemplate mailTemplate;
	private final AbstractGraderContext assignGrader;
	
	private final ContactForm mailCtrl;

	public ImportGraderMailController(UserRequest ureq, WindowControl wControl, MailTemplate mailTemplate,
			ContactList contactList, AbstractGraderContext assignGrader, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "mail_template");
		this.mailTemplate = mailTemplate;
		this.assignGrader = assignGrader;
		mailCtrl = new ContactForm(ureq, getWindowControl(), rootForm, getIdentity(), false, false, false, false);
		mailCtrl.getAndRemoveFormTitle();
		mailCtrl.setOptional(true);
		mailCtrl.setSubject(mailTemplate.getSubjectTemplate());
		mailCtrl.setBody(mailTemplate);
		mailCtrl.setRecipientsLists(Collections.singletonList(contactList));
		initForm (ureq);
	}
	
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.add("template", mailCtrl.getInitialFormItem());
	}
	
	@Override
	protected void doDispose() {
		mailCtrl.cleanUpAttachments();
        super.doDispose();
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= mailCtrl.validateFormLogic(ureq);
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		assignGrader.setSendEmail(mailCtrl.isSend());
		if(mailCtrl.isSend()) {
			mailTemplate.setSubjectTemplate(mailCtrl.getSubject());
			mailTemplate.setBodyTemplate(mailCtrl.getBody());
			mailTemplate.setAttachments(mailCtrl.getAttachments());
			mailTemplate.setAttachmentsTmpDir(mailCtrl.getAttachmentsTempDir());
		}
		fireEvent (ureq, StepsEvent.ACTIVATE_NEXT);
	}
}