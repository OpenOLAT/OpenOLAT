/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.mail;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.link.LinkPopupSettings;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;
import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.FeedbackService;
import org.olat.modules.selectus.MailService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.manager.CommitteeReminderSender;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.feedback.ApplicationsFeedbackConfigurationImpl;
import org.olat.modules.selectus.model.mail.MailAttachment;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.events.NewPositionSavedEvent;
import org.olat.modules.selectus.ui.feedback.appsfeedback.FeedbackHelper;
import org.olat.modules.selectus.ui.mail.PositionMailTemplateRow.Type;
import org.olat.modules.selectus.ui.reference.ReferenceHelper;
import org.olat.modules.selectus.ui.rejection.MailVariablesController;
import org.olat.modules.selectus.ui.rejection.PreviewEmailController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * The is the controller used to edit the mail templates to referees, experts,
 * feedback and reminder to committee.
 * 
 * Initial date: 24 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MailTemplateSingleLanguageEditController extends FormBasicController {

	private FormLink previewLink;
	private Link variablesButton;
	private RichTextElement bodyEl;
	
	private Position position;
	private Identity headOfCommittee;
	private final PositionMailTemplateRow templateRow;

	private CloseableModalController cmc;
	private PreviewEmailController mailPreviewCtrl;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private MailService mailService;
	@Autowired
	private AuditService auditService;
	@Autowired
	private FeedbackService feedbackService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;

	public MailTemplateSingleLanguageEditController(UserRequest ureq, WindowControl wControl,
			Position position, PositionMailTemplateRow templateRow) {
		super(ureq, wControl, LAYOUT_DEFAULT_2_10);
		setTranslator(Util.createPackageTranslator(PositionController.class, ureq.getLocale(), getTranslator()));
		this.position = position;
		this.templateRow = templateRow;
		initForm(ureq);
	}
	
	public Position getPosition() {
		return position;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String description = getBody();
		bodyEl = uifactory.addRichTextElementForStringData("body_", "edit.body", description, 20, 60,
				false, null, null, formLayout, ureq.getUserSession(), getWindowControl());
		bodyEl.getEditorConfiguration().setRelativeUrls(false);
		bodyEl.getEditorConfiguration().setRemoveScriptHost(false);
		bodyEl.getEditorConfiguration().setPathInStatusBar(false);
		bodyEl.setMandatory(true);
		bodyEl.setMaxLength(7000);
		bodyEl.setElementCssClass("o_sel_template_body");

		FormLayoutContainer variablesCont = FormLayoutContainer.createBareBoneFormLayout("links", getTranslator());
		formLayout.add(variablesCont);
		
		previewLink = uifactory.addFormLink("edit.template.preview_", "edit.template.preview", null, variablesCont, Link.LINK);
		previewLink.setDomReplacementWrapperRequired(false);
		previewLink.setIconLeftCSS("o_icon o_icon_preview");
		
		String page = velocity_root + "/variable_link.html";
		FormLayoutContainer subCont = uifactory.addCustomFormLayout("cusvar", null, page, variablesCont);
		subCont.setDomReplacementWrapperRequired(false);
		variablesButton = LinkFactory.createLink("edit.template.variables", subCont.getFormItemComponent(), listener);
		variablesButton.setDomReplacementWrapperRequired(false);
		variablesButton.setIconLeftCSS("o_icon o_icon_help");
		variablesButton.setPopup(new LinkPopupSettings(800, 600, "Variables"));
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		buttonsCont.setRootForm(mainForm);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	private String getBody() {
		String body = null;
		if(templateRow.getType() == Type.referee) {
			body = position.getRefereeRecommandationMailTemplate();
			if(!StringHelper.containsNonWhitespace(body)) {
				body = Util.createPackageTranslator(RecruitingHelper.class, getLocale())
						.translate("reference.recommendation.mail.body");
			}
		} else if(templateRow.getType() == Type.expert) {
			body = position.getExpertRecommandationMailTemplate();
			if(!StringHelper.containsNonWhitespace(body)) {
				body = Util.createPackageTranslator(RecruitingHelper.class, getLocale())
						.translate("reference.expert.mail.body");
			}
		} else if(templateRow.getType() == Type.comparativeExpert) {
			body = position.getComparativeAssessmentExpertMailTemplate();
			if(!StringHelper.containsNonWhitespace(body)) {
				body = Util.createPackageTranslator(RecruitingHelper.class, getLocale())
						.translate("reference.comparative.expert.mail.body");
			}
		} else if(templateRow.getType() == Type.committeeReminder) {
			body = position.getCommitteeReminderMailTemplate();
			if(!StringHelper.containsNonWhitespace(body)) {
				body = CommitteeReminderSender.getMailTemplateBody(getLocale());
			}
		} else if(templateRow.getType() == Type.feedback) {
			body = templateRow.getFeedbackConfiguration().getMailTemplate();
			if(!StringHelper.containsNonWhitespace(body)) {
				body = FeedbackHelper.getDefaultTemplateBody(position, salutationGenerator, getLocale());
			}
		} 
		return toHtml(body);
	}
	
	private String toHtml(String text) {
		if(StringHelper.isHtml(text)) {
			return text;
		}
		StringBuilder sb = Formatter.stripTabsAndReturns(text);
		return sb == null ? "" : sb.toString();
	}
	
	public Identity getHeadOfcommittee() {
		if(headOfCommittee == null) {
			headOfCommittee = recruitingService.getHeadOfCommittee(position);
		}
		return headOfCommittee;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= RecruitingHelper.validateRichTextElement(bodyEl, 32000, true, new OWASPAntiSamyXSSFilter());
		return allOk;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(variablesButton == source) {
			doOpenVariables(ureq);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(mailPreviewCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(mailPreviewCtrl);
		removeControllerListener(cmc);
		mailPreviewCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(variablesButton == source) {
			doOpenVariables(ureq);
		} else if(previewLink == source) {
			doOpenPreview(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		
		Event doneEvent = Event.DONE_EVENT;
		if(position.getKey() != null) {
			position = recruitingService.getPosition(position.getKey());
		} else {
			doneEvent = new NewPositionSavedEvent();
			position = recruitingService.savePosition(position);
		}
		
		String before = auditService.toAuditXml(position);
		
		String body = bodyEl.getValue();
		if(templateRow.getType() == Type.referee) {
			position.setRefereeRecommandationMailTemplate(body);
			position = recruitingService.savePosition(position);
		} else if(templateRow.getType() == Type.expert) {
			position.setExpertRecommandationMailTemplate(body);
			position = recruitingService.savePosition(position);
		} else if(templateRow.getType() == Type.comparativeExpert) {
			position.setComparativeAssessmentExpertMailTemplate(body);
			position = recruitingService.savePosition(position);
		} else if(templateRow.getType() == Type.committeeReminder) {
			position.setCommitteeReminderMailTemplate(body);
			position = recruitingService.savePosition(position);
		} else if(templateRow.getType() == Type.feedback) {
			templateRow.getFeedbackConfiguration().setMailTemplate(body);
			feedbackService.updateApplicationsFeedbackConfiguration(templateRow.getFeedbackConfiguration());
		} 
		
		getLogger().info(Tracing.M_AUDIT, "Update position: {}", position.toStringFull());
		
		String after = auditService.toAuditXml(position);
		if(!before.equals(after)) {
			String messageI18n = "audit.log.position.change.configuration";
			String[] messageArgs = new String[] { position.getMLTitle(recruitingModule.getPositionDefaultLocale()) };
			auditService.auditPositionLog(Action.changeConfiguration, ActionTarget.position, before, after,
					messageI18n, messageArgs, getTranslator(), position, getIdentity());
		}

		dbInstance.commit();
		fireEvent(ureq, doneEvent);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	public void doOpenVariables(UserRequest ureq) {
		Type templateType = templateRow.getType();

		final boolean withApplication = templateType != Type.committeeReminder && templateType != Type.feedback;
		final boolean withApplicationList = templateType == Type.feedback;
		final boolean withFacultyMembers = templateType == Type.committeeReminder || templateType == Type.feedback;
		final boolean withApplicantDashboardUrl = false;// No templates for the applicant itself
		
		final Reference dummyRef;
		if(templateType == Type.referee || templateType == Type.expert || templateType == Type.comparativeExpert) {
			dummyRef = ReferenceHelper.generateDummyReference();
		} else {
			dummyRef = null;
		}
		
		final ApplicationsFeedbackConfiguration feedbackConfiguration;
		if(templateType == Type.feedback) {
			feedbackConfiguration = new ApplicationsFeedbackConfigurationImpl();
			feedbackConfiguration.setEnabled(true);
		} else {
			feedbackConfiguration = null;
		}
		
		// referee expert
		ControllerCreator ctrlCreator = (lureq, lwControl) -> {
			Controller mailVariablesCtrl = new MailVariablesController(lureq, lwControl,
					position, withApplication, withApplicationList, withApplicantDashboardUrl,
					dummyRef, feedbackConfiguration, true, true, withFacultyMembers);
			LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(lureq, lwControl, mailVariablesCtrl);
			layoutCtr.addDisposableChildController(mailVariablesCtrl);
			return layoutCtr;
		};
		
		//wrap the content controller into a full header layout
		ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
		//open in new browser window
		PopupBrowserWindow pbw = getWindowControl().getWindowBackOffice().getWindowManager().createNewPopupBrowserWindowFor(ureq, layoutCtrlr);
		pbw.open(ureq);
	}
	
	private void doOpenPreview(UserRequest ureq) {
		Identity head = getHeadOfcommittee();
		if(head == null) {
			head = ReferenceHelper.generateDummyHeadOfCommittee();
		}
		Identity secretary = recruitingService.getSecretary(position);
		if(secretary == null) {
			secretary = ReferenceHelper.generateDummySecretary();
		}
		
		Reference reference = null;
		if(templateRow.getType() == Type.expert || templateRow.getType() == Type.referee
				|| templateRow.getType() == Type.comparativeExpert) {
			reference = ReferenceHelper.generateDummyReference();
		}
	
		Application app = ReferenceHelper.generateDummyApplication(position);
		ApplicationsFeedbackConfiguration feedbackConfig = templateRow.getFeedbackConfiguration();
		
		String subject = "";
		String body = bodyEl.getValue();
		
		String rawLetterConfiguration = null;
		if(templateRow.getType() == Type.referee) {
			rawLetterConfiguration = position.getRefereeRecommandationMailLetter();
		} else if(templateRow.getType() == Type.expert) {
			rawLetterConfiguration = position.getExpertRecommandationMailLetter();
		} else if(templateRow.getType() == Type.comparativeExpert) {
			rawLetterConfiguration = position.getComparativeAssessmentExpertMailLetter();
		} else if(templateRow.getType() == Type.committeeReminder) {
			rawLetterConfiguration = position.getCommitteeReminderMailLetter();
		} else if(templateRow.getType() == Type.feedback) {
			rawLetterConfiguration = feedbackConfig.getMailLetter();
		}

		MailAttachment letter = null;
		if(StringHelper.containsNonWhitespace(rawLetterConfiguration)) {
			letter = mailService.toAttachment(rawLetterConfiguration, app, getLocale());
		}

		Translator localeTranslator = Util.createPackageTranslator(MailTemplateSingleLanguageEditController.class, getLocale());
		mailPreviewCtrl = new PreviewEmailController(ureq, getWindowControl(), subject, body, letter,
				position, app, reference, feedbackConfig, secretary, head, localeTranslator) ;
		listenTo(mailPreviewCtrl);
		
		String title = translate("edit.template.preview");
		cmc = new CloseableModalController(getWindowControl(), "c", mailPreviewCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
}
