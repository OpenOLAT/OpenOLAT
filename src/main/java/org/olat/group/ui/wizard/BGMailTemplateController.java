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
* <p>
*/ 
package org.olat.group.ui.wizard;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailContent;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailTemplate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This is a specialized form to send mail to...
 * 
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class BGMailTemplateController extends FormBasicController {

	private static final String NLS_CONTACT_SEND_CP_FROM = "contact.cp.from";
	private static final String DEFAULT_MAIL_TEMPLATE = "default";

	private TextElement subjectElem;
	private RichTextElement bodyElem;
	private FormToggle sendMail;
	private SelectionElement ccSender;
	private SingleSelection mailContentSelection;
	private StaticTextElement defaultTemplateEl;
	
	private final boolean useCancel;
	private final boolean useSubmit;
	private final MailTemplate template;
	private final boolean mandatoryEmail;
	private final boolean ccSenderAllowed;
	private final boolean customizingAvailable;

	@Autowired
	private MailManager mailManager;
	
	/**
	 * Constructor for the mail notification form
	 * @param locale
	 * @param template Default values taken from this template
	 * @param useCancel 
	 * @param listeningController Controller that listens to form events
	 */
	public BGMailTemplateController(UserRequest ureq, WindowControl wControl, MailTemplate template,
			boolean ccSenderAllowed, boolean customizingAvailable, boolean useCancel, boolean mandatoryEmail) {
		super(ureq, wControl);
		this.template = template;
		this.useCancel = useCancel;
		this.useSubmit = true;
		this.mandatoryEmail = mandatoryEmail;
		this.ccSenderAllowed = ccSenderAllowed;
		this.customizingAvailable = customizingAvailable;
		initForm (ureq);
	}
	
	public BGMailTemplateController(UserRequest ureq, WindowControl wControl, MailTemplate template,
			boolean ccSenderAllowed, boolean customizingAvailable, boolean useCancel, boolean mandatoryEmail,
			Form rootForm) {
		super(ureq, wControl, LAYOUT_DEFAULT, null, rootForm);
		this.template = template;
		this.useCancel = useCancel;
		this.useSubmit = false;
		this.mandatoryEmail = mandatoryEmail;
		this.ccSenderAllowed = ccSenderAllowed;
		this.customizingAvailable = customizingAvailable;
		initForm (ureq);
	}

	/**
	 * Update the given templates with the values entered in the form
	 * @param mailTemplate 
	 */
	public void updateTemplateFromForm(MailTemplate mailTemplate) {
		if(subjectElem != null) {
			mailTemplate.setSubjectTemplate(subjectElem.getValue());
		}
		if(bodyElem != null) {
			mailTemplate.setBodyTemplate(bodyElem.getValue());
		}
		if(ccSender != null) {
			mailTemplate.setCpfrom(ccSender.isSelected(0));
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent (ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		if(subjectElem != null) {
			subjectElem.clearError();
		}
		if(bodyElem != null) {
			bodyElem.clearError();
		}
		if (mandatoryEmail || sendMail.isOn()) {
			if(subjectElem != null && !StringHelper.containsNonWhitespace(subjectElem.getValue())) {
				subjectElem.setErrorKey("mailtemplateform.error.emptyfield");
				allOk &= false;
			}
			
			if(bodyElem != null && !StringHelper.containsNonWhitespace(bodyElem.getValue())) {
				bodyElem.setErrorKey("mailtemplateform.error.emptyfield");
				allOk &= false;
			}
		}
		return allOk;
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_contact_form");
		// toggle sendMail or not
		if(!mandatoryEmail) {
			sendMail = uifactory.addToggleButton("sendmail", "mailtemplateform.sendMailSwitchElem", null, null, formLayout);
			sendMail.toggleOn();
			sendMail.addActionListener(FormEvent.ONCHANGE);
		}

		// selection if default or custom mail
		SelectionValues contentTypeSV = new SelectionValues();
		contentTypeSV.add(entry(DEFAULT_MAIL_TEMPLATE, translate("mailtemplateform.defaultTemplate")));
		if(customizingAvailable || !StringHelper.containsNonWhitespace(template.getSubjectTemplate())) {
			contentTypeSV.add(entry("custom", translate("mailtemplateform.customised")));
		}
		mailContentSelection = uifactory.addRadiosVertical("mailtemplateform.mail.content.type", formLayout, contentTypeSV.keys(), contentTypeSV.values());
		mailContentSelection.select(DEFAULT_MAIL_TEMPLATE, true);
		mailContentSelection.addActionListener(FormEvent.ONCHANGE);
		mailContentSelection.setEnabled(customizingAvailable);

		if(customizingAvailable) {
			subjectElem = uifactory.addTextElement("subjectElem", "mailtemplateform.subject", 128, template.getSubjectTemplate(), formLayout);
			subjectElem.setDisplaySize(60);
			subjectElem.setMandatory(true);
		
			String body = template.getBodyTemplate();
			if(body != null && !StringHelper.isHtml(body)) {
				body = Formatter.escWithBR(body).toString();
			}
			bodyElem = uifactory.addRichTextElementForStringDataMinimalistic("bodyElem", "mailtemplateform.body", body, 8, 60, formLayout, getWindowControl());
			bodyElem.setMandatory(true);
			MailHelper.setVariableNamesAsHelp(bodyElem, template, getLocale());
		}

		defaultTemplateEl = uifactory.addStaticTextElement("defaultTemplate", "mailtemplateform.defaultTemplate", null, formLayout);

		// default template evaluated
		MailContent mailContent = mailManager.evaluateTemplate(template);
		defaultTemplateEl.setValue("<strong>" + mailContent.getSubject()  +"</strong>" + "<br><br>" + mailContent.getBody());

		if(ccSenderAllowed) {
			ccSender = uifactory.addCheckboxesVertical("tcpfrom", "", formLayout, new String[]{"xx"}, new String[]{translate(NLS_CONTACT_SEND_CP_FROM)}, 1);
		}
		
		if(useSubmit || useCancel) {
			FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
			formLayout.add(buttonGroupLayout);
			if(useSubmit) {
				uifactory.addFormSubmitButton("continue", "mailtemplateform.continue", buttonGroupLayout);
			}
			if (useCancel) {
				uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
			}
		}

		updateVisibility();
	}
	
	private void updateVisibility() {
		boolean isSendMail = sendMail == null || sendMail.isOn();
		boolean sm = customizingAvailable && !mailContentSelection.isKeySelected(DEFAULT_MAIL_TEMPLATE);
		mailContentSelection.setVisible(isSendMail);
		defaultTemplateEl.setVisible(isSendMail && mailContentSelection.isKeySelected(DEFAULT_MAIL_TEMPLATE));
		if(subjectElem != null) {
			subjectElem.setVisible(sm && isSendMail);
		}
		if(bodyElem != null) {
			bodyElem.setVisible(sm && isSendMail);
		}
		if(ccSender != null) {
			ccSender.setVisible(sm && isSendMail);
		}
	}
	
	@Override
	protected void formInnerEvent (UserRequest ureq, FormItem source, FormEvent event) {
		updateVisibility();
	}
	
	/**
	 * @return true: mail switch is enabled; false: otherwise
	 */
	public boolean sendMailSwitchEnabled() {
		return mandatoryEmail || sendMail.isOn();
	}

	/**
	 * check if selected content is for default template or not
	 *
	 * @return true if default template is selected
	 */
	public boolean isMailContentDefault() {
		return mailContentSelection.isKeySelected(DEFAULT_MAIL_TEMPLATE);
	}
	
}