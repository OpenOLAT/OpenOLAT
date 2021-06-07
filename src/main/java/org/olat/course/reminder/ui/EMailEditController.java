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
package org.olat.course.reminder.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailHelper;
import org.olat.modules.reminder.Reminder;
import org.olat.modules.reminder.manager.CourseReminderTemplate;
import org.olat.modules.reminder.ui.ReminderAdminController;

/**
 * 
 * Initial date: 31 May 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EMailEditController extends StepFormBasicController {

	private TextElement subjectEl;
	private RichTextElement emailEl;
	
	private final Reminder reminder;

	public EMailEditController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
		setTranslator(Util.createPackageTranslator(ReminderAdminController.class, getLocale(), getTranslator()));
		reminder = (Reminder)runContext.get(RulesEditStep.CONTEXT_KEY);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//email subject
		String subject = reminder.getEmailSubject();
		subjectEl = uifactory.addTextElement("reminder.subject", "reminder.subject", 128, subject, formLayout);
		subjectEl.setMandatory(true);
		subjectEl.setElementCssClass("o_sel_course_reminder_subject");
		
		String emailContent = reminder.getEmailBody();
		if(!StringHelper.containsNonWhitespace(emailContent)) {
			emailContent = translate("reminder.def.body");
		}
		emailEl = uifactory.addRichTextElementForStringDataMinimalistic("email.content", "email.content", emailContent, 10, 60, formLayout, getWindowControl());
		emailEl.setMandatory(true);
		MailHelper.setVariableNamesAsHelp(emailEl, CourseReminderTemplate.variableNames(), getLocale());
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		subjectEl.clearError();
		if(!StringHelper.containsNonWhitespace(subjectEl.getValue())) {
			subjectEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		}		

		emailEl.clearError();
		if(!StringHelper.containsNonWhitespace(emailEl.getValue())) {
			emailEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String emailSubject = subjectEl.getValue();
		reminder.setEmailSubject(emailSubject);
		
		String emailBody = emailEl.getValue();
		reminder.setEmailBody(emailBody);
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void doDispose() {
		//
	}

}
