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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailTemplate;

/**
 * This is a specialized form to send mail to...
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BGMailTemplateController extends FormBasicController {

	private TextElement subjectElem;
	private TextElement bodyElem;
	private SelectionElement sendMail;
	private SelectionElement ccSender;
	private SelectionElement defaultTemplate;
	private static final String NLS_CONTACT_SEND_CP_FROM = "contact.cp.from";
	
	private final boolean useCancel;
	private final boolean useSubmit;
	private MailTemplate template;
	private final boolean mandatoryEmail;
	private final boolean ccSenderAllowed;
	private final boolean customizingAvailable;
	
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
		
		subjectElem.clearError();
		bodyElem.clearError();
		if(defaultTemplate.isSelected(0)) {
			//
		} else if (mandatoryEmail || sendMail.isSelected(0)) {
			if(subjectElem != null && !StringHelper.containsNonWhitespace(subjectElem.getValue())) {
				subjectElem.setErrorKey("mailtemplateform.error.emptyfield", null);
				allOk &= false;
			}
			
			if(bodyElem != null && !StringHelper.containsNonWhitespace(bodyElem.getValue())) {
				bodyElem.setErrorKey("mailtemplateform.error.emptyfield", null);
				allOk &= false;
			}
		}
		return allOk;
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_contact_form");
		if(!mandatoryEmail) {
			sendMail = uifactory.addCheckboxesVertical("sendmail", "", formLayout, new String[]{"xx"}, new String[]{translate("mailtemplateform.sendMailSwitchElem")}, 1);
			sendMail.select("xx", true);
			sendMail.addActionListener(FormEvent.ONCLICK);
		}
		
		defaultTemplate = uifactory.addCheckboxesVertical("deftemplate", "", formLayout, new String[]{"xx"}, new String[]{translate("mailtemplateform.defaultTemplate")}, 1);
		if(!customizingAvailable && StringHelper.containsNonWhitespace(template.getSubjectTemplate())) {
			defaultTemplate.select("xx", true);
		}
		
		defaultTemplate.addActionListener(FormEvent.ONCLICK);
		defaultTemplate.setEnabled(customizingAvailable);

		if(customizingAvailable) {
			subjectElem = uifactory.addTextElement("subjectElem", "mailtemplateform.subject", 128, template.getSubjectTemplate(), formLayout);
			subjectElem.setDisplaySize(60);
			subjectElem.setMandatory(true);
		
			bodyElem = uifactory.addTextAreaElement("bodyElem", "mailtemplateform.body", -1, 15, 60, true, false, template.getBodyTemplate(), formLayout);
			bodyElem.setHelpText(translate("mailtemplateform.body.hover"));
			bodyElem.setHelpUrlForManualPage("E-Mail");
			bodyElem.setMandatory(true);
		}
		
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

		update();
	}
	
	private void update () {
		boolean sm = customizingAvailable && !defaultTemplate.isSelected(0);
		if(subjectElem != null) {
			subjectElem.setVisible(sm);
		}
		if(bodyElem != null) {
			bodyElem.setVisible(sm);
		}
		if(ccSender != null) {
			ccSender.setVisible(sm);
		}
	}
	
	@Override
	protected void formInnerEvent (UserRequest ureq, FormItem source, FormEvent event) {
		update();
	}
	
	/**
	 * @return true: mail switch is enabled; false: otherwise
	 */
	public boolean sendMailSwitchEnabled() {
		return mandatoryEmail || sendMail.isSelected(0);
	}
	
	public boolean isDefaultTemplate() {
		return defaultTemplate.isSelected(0);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
}