/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.feedback.appsfeedback;

import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailerResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.FeedbackService;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.manager.ApplicationMailTemplate;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.ApplicationShort;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;

/**
 * 
 * Initial date: 5 mai 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SendFeedbackInvitationController extends FormBasicController {

	private TextElement subjectEl;
	private RichTextElement bodyEl;
	
	private final Position position;
	private ApplicationFeedback feedback;
	private final ApplicationMailTemplate template;
	
	@Autowired
	private AuditService auditService;
	@Autowired
	private FeedbackService feedbackService;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	
	public SendFeedbackInvitationController(UserRequest ureq, WindowControl wControl,
			Position position, ApplicationFeedback feedback, ApplicationMailTemplate template) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.feedback = feedback;
		this.template = template;
		this.position = position;
		initForm(ureq);
	}
	
	public ApplicationFeedback getFeedback() {
		return feedback;
	}
	
	public ApplicationMailTemplate getTemplate() {
		return template;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("reference.resend.description");
		
		subjectEl = uifactory.addTextElement("subjectElem", "mailtemplateform.subject", 128, template.getSubjectTemplate(), formLayout);
		subjectEl.setDisplaySize(60);
		subjectEl.setMandatory(true);

		bodyEl = uifactory.addRichTextElementForStringData("bodyElem", "mailtemplateform.body", template.getBodyTemplate(), 24, 60,
				false, null, null, formLayout, ureq.getUserSession(), getWindowControl());
		bodyEl.getEditorConfiguration().setRelativeUrls(false);
		bodyEl.getEditorConfiguration().setRemoveScriptHost(false);
		bodyEl.getEditorConfiguration().setPathInStatusBar(false);
		bodyEl.setMandatory(true);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("send", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String subjectTemplate = subjectEl.getValue();
		template.setSubjectTemplate(subjectTemplate);
		String bodyTemplate = bodyEl.getValue();
		template.setBodyTemplate(bodyTemplate);
		
		sendInvitation();
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void sendInvitation() {
		MailerResult result = new MailerResult();
		feedback = feedbackService.getApplicationFeedback(feedback);
		Identity member = feedback.getIdentity();
		String memberMail = FeedbackHelper.getEmail(member);
		
		List<ApplicationFeedback> feedbacks = Collections.singletonList(feedback);
		List<ApplicationShort> applications = Collections.singletonList(feedback.getApplication());
		feedbacks = recruitingService.sendFeedbackContactMail(member, feedbacks, null, feedback.getConfiguration(), applications,
				position, template, result);
		if(!feedbacks.isEmpty()) {
			feedback = feedbacks.get(0);
		}
		
		if(result.getReturnCode() == MailerResult.OK) {
			showInfo("reference.mail.send.success");
			logSendMail(member);
		} else if(result.getFailedAddresses().isEmpty()) {
			showError("apps.feedback.mail.send.error", memberMail);
		} else {
			String error = result.getFailedAddresses().size() == 1 ?"apps.feedback.mail.send.invalid.address" : "apps.feedback.mail.send.invalid.addresses";
			showError(error, new String[] { memberMail, result.failedAddressesToString() });
		}
	}
	
	private void logSendMail(Identity member) {
		Application application = feedback.getApplication();
		String messageI18n = "audit.log.member.feedback.send.email";

		String[] messageArgs = new String[] {
			RecruitingHelper.formatFullNameWithTitle(member, getLocale()),
			salutationGenerator.getTitleFullname(application, getLocale()),
			application.getId().toString()
		};
		auditService.auditFeedbackLog(Action.sendMail, null, null, messageI18n, messageArgs, getTranslator(), position, application, feedback, getIdentity());
	}
}
