/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.reference;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.ExternalLinkItem;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailerResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.manager.ApplicationMailTemplate;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceRequestStatus;
import org.olat.modules.selectus.model.ReferenceStatus;
import org.olat.modules.selectus.model.ReferenceToApplication;
import org.olat.modules.selectus.model.ReferenceType;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.mail.LetterMediaResource;

/**
 * 
 * Only there to customize the mail template.
 * 
 * Initial date: 19.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SendInvitationEmailController extends FormBasicController {
	
	private TextElement subjectEl;
	private RichTextElement bodyEl;
	
	private Reference reference;
	private final Position position;
	private final ApplicationMailTemplate template;

	@Autowired
	private AuditService auditService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;

	public SendInvitationEmailController(UserRequest ureq, WindowControl wControl,
			Position position, Reference reference, ApplicationMailTemplate template) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.position = position;
		this.template = template;
		this.reference = reference;
		initForm(ureq);
	}

	public Reference getReference() {
		return reference;
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
		
		if(recruitingModule.isMailLetterEnabled() && template.getLetterTemplate() != null
				&& StringHelper.containsNonWhitespace(template.getLetterTemplate().getContentToPdf())) {
			String mapperUri = registerCacheableMapper(ureq, null, new PreviewMapper());
			String url = mapperUri + "/preview.pdf?test=" + CodeHelper.getForeverUniqueID();
			ExternalLinkItem link = uifactory.addExternalLink("letter", url, "_blank", formLayout);
			link.setName(template.getLetterTemplate().getFilename());
			link.setIconLeftCSS("o_icon o_filetype_pdf");
		}
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("send", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		String body = bodyEl.getValue();
		bodyEl.clearError();
		if(!StringHelper.containsNonWhitespace(body)) {
			bodyEl.setErrorKey("form.legende.mandatory");
			allOk &= false; 
		} else if(!ReferenceHelper.validateLinks(bodyEl, reference)) {
			allOk &= false; 
		}
	
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String subjectTemplate = subjectEl.getValue();
		template.setSubjectTemplate(subjectTemplate);
		String bodyTemplate = bodyEl.getValue();
		template.setBodyTemplate(bodyTemplate);
		
		doSend();
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doSend() {
		MailerResult result = new MailerResult();
		reference = recruitingService.getReferenceById(reference.getKey());
		if(reference == null) {
			showWarning("warning.reference.deleted");
		} else {
			if(reference.getRequestStatus() != ReferenceRequestStatus.notAnswered) {
				reference.setRequestStatus(ReferenceRequestStatus.notAnswered);
				reference.setConsentByStaff(null);
				reference.setDateConsent(null);
				reference = recruitingService.updateReference(reference);
			}
			
			Application app = reference.getApplication();
			List<Application> appsList = this.recruitingService.getReferenceToApplications(reference)
					.stream().map(ReferenceToApplication::getApplication)
					.collect(Collectors.toList());
			reference = recruitingService.sendRefereeMail(reference, app, appsList, position, template, ReferenceStatus.sentAwaiting, false, result);
			if(result.getReturnCode() == MailerResult.OK) {
				showInfo("reference.mail.send.success");
				logSendMail(reference, app, appsList);
			} else if(result.getFailedAddresses().isEmpty()) {
				showError("rejection.mail.send.error", reference.getEmail());
			} else {
				String error = result.getFailedAddresses().size() == 1 ?"rejection.mail.send.invalid.address" : "rejection.mail.send.invalid.addresses";
				showError(error, new String[] { reference.getEmail(), result.failedAddressesToString() });
			}
		}
	}
	
	private void logSendMail(Reference ref, Application application, List<Application> applicationsList) {

		ActionTarget target = null;
		String messageI18n = "";
		if(ref.getReferenceType() == ReferenceType.expert) {
			target = ActionTarget.expert;
			messageI18n = "audit.log.expert.send.email";
		} else if(ref.getReferenceType() == ReferenceType.recommendation) {
			target = ActionTarget.referee;
			messageI18n = "audit.log.referee.send.email";
		} else if(ref.getReferenceType() == ReferenceType.comparativeAssessmentExpert) {
			target = ActionTarget.comparativeExpert;
			messageI18n = "audit.log.comparative.expert.send.email";
		}
		String[] messageArgs = new String[] {
			salutationGenerator.getTitleFullname(ref, getLocale()),
			salutationGenerator.getTitleFullname(application, applicationsList, getLocale()),
			RecruitingHelper.formatIDs(application, applicationsList)
		};
		auditService.auditRefereeLog(Action.sendMail, target, null, null, messageI18n, messageArgs, getTranslator(), position, application, ref, getIdentity());
	}
	
	private class PreviewMapper implements Mapper {
		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			String letter = template.getLetterTemplate().getContentToPdf();
			return new LetterMediaResource(letter);
		}
	}
}
