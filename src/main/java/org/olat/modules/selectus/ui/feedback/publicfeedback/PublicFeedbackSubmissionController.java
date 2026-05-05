/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.feedback.publicfeedback;

import java.util.Date;

import org.olat.admin.user.imp.TransientIdentity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.FeedbackService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PublicFeedback;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.components.DateCellRenderer;

/**
 * 
 * Initial date: 30 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PublicFeedbackSubmissionController extends FormBasicController {
	
	private TextElement feedbackEl;
	
	private PublicFeedback feedback;
	private final Position position;
	private final Application application;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AuditService auditService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private FeedbackService feedbackService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	@Autowired
	private RecruitingService recruitingService;
	
	public PublicFeedbackSubmissionController(UserRequest ureq, WindowControl wControl, Position position, Application application) {
		super(ureq, wControl, "public_feedback", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.position = position;
		this.application = application;
		
		Identity identity = ureq.getIdentity();
		String firstName = identity.getUser().getProperty(UserConstants.FIRSTNAME, getLocale());
		String lastName = identity.getUser().getProperty(UserConstants.LASTNAME, getLocale());
		String email = identity.getUser().getProperty(UserConstants.EMAIL, getLocale());
		feedback = feedbackService.getPublicFeedback(firstName, lastName, email, identity.getExternalId(), null, application);

		initForm(ureq);
	}
	
	public PublicFeedback getFeedback() {
		return feedback;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String[] args = new String[] {
			RecruitingHelper.formatFullName(feedback, false)
		};
		formLayout.contextPut("publicFeedbackTitle", translate("public.feedback.title.submission", args));
		
		String applicantFullName = StringHelper.escapeHtml(salutationGenerator.getTitleFullname(application, getLocale()));
		String applicantLastName = StringHelper.escapeHtml(salutationGenerator.getTitleLastName(application, getLocale()));
		String applicationTitleFirstLastName = salutationGenerator.getTitleFirstLastName(application, getLocale());
		Identity headOfCommittee = recruitingService.getHeadOfCommittee(position);
		String headEmail = headOfCommittee == null ? "" : headOfCommittee.getUser().getProperty(UserConstants.EMAIL, getLocale());
		if(headEmail == null) {
			headEmail = "";
		}
		
		String[] applicantArgs = new String[] {
			applicantFullName,						// 0
			position.getMLTitle(getLocale()),	// 1
			applicantLastName,						// 2
			headEmail,								// 3
			applicationTitleFirstLastName			// 4
		};

		String positionMsg = translate("public.feedback.title.text", applicantArgs);
		formLayout.contextPut("positionMsg", positionMsg);
		formLayout.contextPut("applicationTitleFirstLastName", applicationTitleFirstLastName);
		formLayout.contextPut("applicationTitleFirstLastNameEnabled", Boolean.valueOf(recruitingModule.isPublicFeedbackFormApplicationNameEnabled()));
		
		Date deadLine = application.getPublicFeedbackDeadline();
		if(deadLine == null) {
			deadLine = position.getPublicFeedbackDeadline();
		}
		formLayout.contextPut("deadLine", DateCellRenderer.format(deadLine, getLocale()));
		formLayout.contextPut("deadLineEnabled", Boolean.valueOf(recruitingModule.isPublicFeedbackFormDueDateEnabled()));
		
		
		String text = feedback == null ? null : feedback.getComment();
		feedbackEl = uifactory.addTextAreaElement("feedback", 8, 60, text, formLayout);

		uifactory.addFormSubmitButton("save", formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Action action;
		String before;
		String messageI18n;
		if(StringHelper.containsNonWhitespace(feedback.getComment())) {
			action = Action.update;
			before = auditService.toAuditXml(feedback);
			messageI18n = "audit.log.public.feedback.update";
		} else {
			action = Action.add;
			before = null;
			messageI18n = "audit.log.public.feedback.add";
		}
		
		feedback.setComment(feedbackEl.getValue());
		feedback = feedbackService.updatePublicFeedback(feedback);
		dbInstance.commit();
		
		String after = auditService.toAuditXml(feedback);
		String[] args = getLogArguments();
		Identity doer = getIdentity() instanceof TransientIdentity ? null : getIdentity();
		auditService.auditPublicFeedbackLog(action, before, after, messageI18n, args, getTranslator(), position, application, doer);
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private String[] getLogArguments() {
		String appName = salutationGenerator.getTitleFullname(application, getLocale());
		String appId = application.getId() == null ? "" : application.getId().toString();
		String reviewer = RecruitingHelper.formatFullName(feedback, true);
		return new String[] { appName, appId, reviewer };
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		feedbackEl.setValue("");
	}
}
