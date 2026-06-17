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
package org.olat.modules.selectus.ui.feedback.appsfeedback.wizard;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailerResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.manager.ApplicationMailTemplate;
import org.olat.modules.selectus.manager.MailerSender;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.SubjectAndBody;
import org.olat.modules.selectus.model.feedback.TransientApplicationFeedback;
import org.olat.modules.selectus.ui.RecruitingHelper;

/**
 * 
 * Initial date: 6 mai 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PreviewContactEmailsController extends StepFormBasicController {
	
	private FormLink nextButton;
	private FormLink previousButton;
	private SingleSelection mailListEl;
	private StaticTextElement bodyEl;
	private StaticTextElement subjectEl;

	private final ContactMembersContext feedbacksContext;
	
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	@Autowired
	private RecruitingService recruitingService;
	
	public PreviewContactEmailsController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext,
			Form form, ContactMembersContext feedbacksContext) {
		super(ureq, wControl, form, runContext, LAYOUT_CUSTOM, "mail_preview");
		this.feedbacksContext = feedbacksContext;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		previousButton = uifactory.addFormLink("previous.mail", formLayout, Link.BUTTON);
		previousButton.setIconLeftCSS("o_icon o_icon_previous");
		nextButton = uifactory.addFormLink("next.mail", formLayout, Link.BUTTON);
		nextButton.setIconRightCSS("o_icon o_icon_next");
		
		SelectionValues appValues = new SelectionValues();
		List<Identity> members = feedbacksContext.getSelectedMembers();
		for(Identity member:members) {
			String fullName = RecruitingHelper.formatFullName(member);
			appValues.add(SelectionValues.entry(member.getKey().toString(), fullName));
		}
		mailListEl = uifactory.addDropdownSingleselect("select.mail", null, formLayout, appValues.keys(), appValues.values(), null);
		mailListEl.setDomReplacementWrapperRequired(false);
		mailListEl.addActionListener(FormEvent.ONCHANGE);

		FormLayoutContainer verticalCont = FormLayoutContainer.createVerticalFormLayout("mail", getTranslator());
		formLayout.add(verticalCont);
		subjectEl = uifactory.addStaticTextElement("preview.subject", "", verticalCont);
		bodyEl = uifactory.addStaticTextElement("preview.body", "", verticalCont);
		
		boolean navigation = members.size() > 1;
		previousButton.setVisible(navigation);
		nextButton.setVisible(navigation);
		mailListEl.setVisible(navigation);
		boolean hasContent = !members.isEmpty();
		subjectEl.setVisible(hasContent);
		bodyEl.setVisible(hasContent);
		if(hasContent) {
			select(0);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(previousButton == source) {
			select(mailListEl.getSelected() - 1);
		} else if(nextButton == source) {
			select(mailListEl.getSelected() + 1);
		} else if(mailListEl == source) {
			select(mailListEl.getSelected());
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	private void select(int index) {
		if(index < 0) {
			index = 0;
		} else if(index >= mailListEl.getSize()) {
			index = mailListEl.getSize() - 1;
		}
		String nextKey = mailListEl.getKey(index);
		mailListEl.select(nextKey, true);
		selectMember();
		
		if(index <= 0) {
			previousButton.setEnabled(false);
			nextButton.setEnabled(true);
		} else if(index >= mailListEl.getSize() - 1) {
			previousButton.setEnabled(true);
			nextButton.setEnabled(false);
		} else {
			previousButton.setEnabled(true);
			nextButton.setEnabled(true);
		}
	}
	
	private void selectMember() {
		String appKey = mailListEl.getSelectedKey();
		Position position = feedbacksContext.getPosition();
		Identity member = feedbacksContext.getMember(appKey);
		
		if(member != null) {
			ApplicationsFeedbackConfiguration feedbackConfig = feedbacksContext.getConfiguration();
			List<Application> mailApps = feedbacksContext.getApplicationsOf(member);
			Application mailApp = mailApps.size() == 1 ? mailApps.get(0) : null;
			Date deadline = feedbacksContext.getDeadline();
			List<ApplicationFeedback> feedbacks = feedbacksContext.getApplicationsFeedbackOf(member);
			List<ApplicationFeedback> decoratedFeedbacks = new ArrayList<>();
			for(int i=0; i<feedbacks.size(); i++) {
				ApplicationFeedback feedback = feedbacks.get(i);
				decoratedFeedbacks.add(new TransientApplicationFeedback(member, feedback.getApplication(), feedbackConfig, deadline));
			}
			
			ApplicationMailTemplate template = feedbacksContext.getMailTemplate();
			MailerSender sender = recruitingService.createMailSender();
			MailerResult mailerResult = new MailerResult();
			SubjectAndBody subjectBody = sender.createWithContext(mailApp, mailApps, null,
					member, decoratedFeedbacks, feedbacksContext.getConfiguration(), position, template, mailerResult);
			subjectEl.setValue(subjectBody.getSubject());
			String content = subjectBody.getBody();
			if(!StringHelper.isHtml(content)) {
				content = Formatter.escWithBR(content).toString();
			}
			bodyEl.setValue(content);
		}
	}	
}
