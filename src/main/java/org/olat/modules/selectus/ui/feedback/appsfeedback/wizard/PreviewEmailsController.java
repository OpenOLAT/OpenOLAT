/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
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
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailerResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.manager.ApplicationMailTemplate;
import org.olat.modules.selectus.manager.MailerSender;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.SubjectAndBody;
import org.olat.modules.selectus.model.feedback.TransientApplicationFeedback;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.committee.wizard.CommitteeMember;
import org.olat.modules.selectus.ui.committee.wizard.CommitteeMemberStatus;

/**
 * 
 * Initial date: 29 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PreviewEmailsController extends StepFormBasicController {
	
	private FormLink nextButton;
	private FormLink previousButton;
	private SingleSelection mailListEl;
	private StaticTextElement bodyEl;
	private StaticTextElement subjectEl;

	private final FeedbackMembersContext feedbacksContext;
	
	@Autowired
	private RecruitingService recruitingService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	
	public PreviewEmailsController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext,
			Form form, FeedbackMembersContext feedbacksContext) {
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
		List<CommitteeMember> members = feedbacksContext.getMembers();
		for(CommitteeMember member:members) {
			if(member.getStatus() == CommitteeMemberStatus.ok) {
				String fullName = RecruitingHelper.formatFullName(member.getIdentity());
				appValues.add(SelectionValues.entry(member.getIdentifier(), fullName));
			}
		}
		mailListEl = uifactory.addDropdownSingleselect("select.mail", null, formLayout, appValues.keys(), appValues.values(), null);
		mailListEl.setDomReplacementWrapperRequired(false);
		mailListEl.addActionListener(FormEvent.ONCHANGE);

		FormLayoutContainer verticalCont = FormLayoutContainer.createVerticalFormLayout("mail", getTranslator());
		formLayout.add(verticalCont);
		subjectEl = uifactory.addStaticTextElement("preview.subject", "", verticalCont);
		bodyEl = uifactory.addStaticTextElement("preview.body", "", verticalCont);
		
		boolean navigation = appValues.size() > 1;
		previousButton.setVisible(navigation);
		nextButton.setVisible(navigation);
		mailListEl.setVisible(navigation);
		boolean hasContent = !appValues.isEmpty();
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
		selectCommitteeMember();
		
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
	
	private void selectCommitteeMember() {
		String memberKey = mailListEl.getSelectedKey();
		Position position = feedbacksContext.getPosition();
		CommitteeMember member = getCommitteeMember(memberKey);
		
		if(member != null) {
			ApplicationMailTemplate template = feedbacksContext.getMailTemplate();
			ApplicationsFeedbackConfiguration feedbackConfig = feedbacksContext.getConfiguration();
			Date deadline = feedbacksContext.getDeadline();
			List<ApplicationLight> mailApps = feedbacksContext.getSelectedApps();
			ApplicationLight mailApp = mailApps.size() == 1 ? mailApps.get(0) : null;
			List<ApplicationFeedback> feedbacks = new ArrayList<>();
			for(int i=0; i<mailApps.size(); i++) {
				feedbacks.add(new TransientApplicationFeedback(member.getIdentity(), null, feedbackConfig, deadline));
			}
			MailerSender sender = recruitingService.createMailSender();
			MailerResult mailerResult = new MailerResult();
			SubjectAndBody subjectBody = sender.createWithContext(mailApp, mailApps, null, member.getIdentity(), feedbacks, feedbackConfig, position, template, mailerResult);
			subjectEl.setValue(subjectBody.getSubject());
			String content = subjectBody.getBody();
			if(!StringHelper.isHtml(content)) {
				content = Formatter.escWithBR(content).toString();
			}
			bodyEl.setValue(content);
		}
	}

	private CommitteeMember getCommitteeMember(String memberId) {
		return feedbacksContext.getMembers().stream()
			.filter(mem -> mem.getIdentifier().equals(memberId))
			.findFirst().orElse(null);
	}
}
