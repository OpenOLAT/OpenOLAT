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
package org.olat.modules.portfolio.ui.wizard;

import org.apache.velocity.VelocityContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailTemplate;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.ui.PortfolioHomeController;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MemberMailController extends StepFormBasicController {
	
	private TextElement subjectEl;
	private TextElement bodyEl;
	private SelectionElement sendMailEl;
	
	private MailTemplate mailTemplate;
	
	@Autowired
	private UserManager userManager;

	public MemberMailController(UserRequest ureq, WindowControl wControl, Binder binder,
			Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
		setTranslator(Util.createPackageTranslator(PortfolioHomeController.class, getLocale(), getTranslator()));
		
		mailTemplate = (MailTemplate)runContext.get("maiTemplate");
		if(mailTemplate == null) {
			String sender = userManager.getUserDisplayName(getIdentity());
			String busLink = Settings.getServerContextPathURI() + "/url/HomeSite/0/PortfolioV2/0/SharedWithMe/0/Binder/" + binder.getKey();
			
			String[] args = new String[] {
				busLink,								// 0
				sender,									// 1
				getIdentity().getUser().getFirstName(),	// 2
				getIdentity().getUser().getLastName()	// 3
			};
			String subject = translate("invitation.mail.subject", args);
			String body = translate("invitation.mail.body", args);
			
			mailTemplate = new MailTemplate(subject, body, null) {
				@Override
				public void putVariablesInMailContext(VelocityContext vContext, Identity recipient) {
					//
				}
			};
		}

		initForm (ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_contact_form");
		sendMailEl = uifactory.addCheckboxesVertical("sendmail", "", formLayout, new String[]{"xx"}, new String[]{translate("mail.sendMail")}, 1);
		sendMailEl.setElementCssClass("o_pf_sel_send_mail");
		sendMailEl.select("xx", true);
		sendMailEl.addActionListener(FormEvent.ONCLICK);

		subjectEl = uifactory.addTextElement("subjectElem", "mail.subject", 128, mailTemplate.getSubjectTemplate(), formLayout);
		subjectEl.setDisplaySize(60);
		subjectEl.setMandatory(true);
	
		bodyEl = uifactory.addTextAreaElement("bodyElem", "mail.body", -1, 15, 60, true, false, mailTemplate.getBodyTemplate(), formLayout);
		bodyEl.setHelpUrlForManualPage("E-Mail");
		bodyEl.setMandatory(true);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		// mailTemplateForm.validateFormLogic(ureq);
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(sendMailEl == source) {
			boolean sm = sendMailEl.isSelected(0);
			subjectEl.setVisible(sm);
			bodyEl.setVisible(sm);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(sendMailEl.isSelected(0)) {
			mailTemplate.setSubjectTemplate(subjectEl.getValue());
			mailTemplate.setBodyTemplate(bodyEl.getValue());
			addToRunContext("mailTemplate", mailTemplate);
		} else {
			addToRunContext("mailTemplate", null);
		}
		fireEvent (ureq, StepsEvent.ACTIVATE_NEXT);
	}
}