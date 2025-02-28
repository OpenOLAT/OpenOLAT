/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui.member;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailContent;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.modules.curriculum.ui.CurriculumManagerController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CustomizeNotificationController extends FormBasicController {
	
	private TextElement subjectElem;
	private RichTextElement bodyElem;
	
	private final MailTemplate template;
	
	@Autowired
	private MailManager mailManager;
	
	public CustomizeNotificationController(UserRequest ureq, WindowControl wControl, MailTemplate template) {
		super(ureq, wControl, Util
				.createPackageTranslator(CurriculumManagerController.class, ureq.getLocale()));
		this.template = template;
		initForm(ureq);
	}
	
	public MailTemplate getMailTemplate() {
		template.setSubjectTemplate(subjectElem.getValue());
		template.setBodyTemplate(bodyElem.getValue());
		return template;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// default template evaluated
		MailContent mailContent = mailManager.evaluateTemplate(template);
		
		String subject = mailContent.getSubject();
		subjectElem = uifactory.addTextElement("subjectElem", "mailtemplateform.subject", 128, subject, formLayout);
		subjectElem.setDisplaySize(60);
		subjectElem.setMandatory(true);

		String body = mailContent.getBody();
		if(body != null && !StringHelper.isHtml(body)) {
			body = Formatter.escWithBR(body).toString();
		}
		bodyElem = uifactory.addRichTextElementForStringDataMinimalistic("bodyElem", "mailtemplateform.body", body, 8, 60, formLayout, getWindowControl());
		bodyElem.setMandatory(true);
		MailHelper.setVariableNamesAsHelp(bodyElem, template, getLocale());
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("send.notifications", "send.notifications", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		subjectElem.clearError();
		if(!StringHelper.containsNonWhitespace(subjectElem.getValue())) {
			subjectElem.setErrorKey("mailtemplateform.error.emptyfield");
			allOk &= false;
		}

		bodyElem.clearError();
		if(!StringHelper.containsNonWhitespace(bodyElem.getValue())) {
			bodyElem.setErrorKey("mailtemplateform.error.emptyfield");
			allOk &= false;
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
