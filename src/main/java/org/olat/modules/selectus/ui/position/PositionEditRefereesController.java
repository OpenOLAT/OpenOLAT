/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.position;

import java.util.Calendar;
import java.util.Date;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.link.LinkPopupSettings;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.MailService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.manager.ApplicationMailTemplate;
import org.olat.modules.selectus.manager.MailerSender;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceSendMailType;
import org.olat.modules.selectus.model.SubjectAndBody;
import org.olat.modules.selectus.model.mail.MailAttachment;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.RecruitingMailTemplate;
import org.olat.modules.selectus.ui.events.NewPositionSavedEvent;
import org.olat.modules.selectus.ui.reference.ReferenceHelper;
import org.olat.modules.selectus.ui.rejection.MailVariablesController;
import org.olat.modules.selectus.ui.rejection.PreviewEmailController;
import org.olat.modules.selectus.ui.rejection.VariablesValidationContext;

/**
 * 
 * Initial date: 13.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionEditRefereesController extends FormBasicController implements PositionEditableController {
	
	private static final String REFEREE_DISABLED = "off";
	private static final String REFEREE_ENABLED = "on";
	private static final String REFEREE_APP_MANAGE = "manage";
	
	private static final String[] monthKeys = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"};
	private String[] monthValues = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};

	private FormLink previewLink;
	private Link variablesButton;
	private SingleSelection applicantRefereesEl;
	private DateChooser refereeDeadlineEl;
	private DateChooser applicantDeadlineEl;
	private TextElement minReferenceEl;
	private TextElement maxReferenceEl;
	private RichTextElement recommendationMailTemplateEl;
	private SingleSelection sendRefereeMailEl;
	
	private Position position;
	private final boolean readOnly;
	
	private CloseableModalController cmc;
	private PreviewEmailController mailPreviewCtrl;

	@Autowired
	private DB dbInstance;
	@Autowired
	private MailService mailService;
	@Autowired
	private AuditService auditService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	
	public PositionEditRefereesController(UserRequest ureq, WindowControl wControl, Position position, boolean readOnly) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.position = position;
		this.readOnly = readOnly;
		for(int i=monthKeys.length; i-->0; ) {
			monthValues[i] = translate("month.long." + i);
		}
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(recruitingModule.isReferenceApplicantManagement()) {
			setFormDescription("edit.form_description.referees.management");
		} else {
			setFormDescription("edit.form_description.referees");
		}
		formLayout.setElementCssClass("o_sel_edit_position_recommendation_form");
		
		SelectionValues enableValues = new SelectionValues();
		enableValues.add(new SelectionValue(REFEREE_DISABLED, translate("disable.referee"), translate("disable.referee.desc")));
		enableValues.add(new SelectionValue(REFEREE_ENABLED, translate("enable.referee"), translate("enable.referee.desc")));
		if(recruitingModule.isReferenceApplicantManagement()) {
			enableValues.add(new SelectionValue(REFEREE_APP_MANAGE, translate("enable.referee.manage"), translate("enable.referee.manage.desc")));
		}
		applicantRefereesEl = uifactory.addCardSingleSelectHorizontal("edit.applicant.referees", formLayout,
				enableValues.keys(), enableValues.values(), enableValues.descriptions(), enableValues.cssClasses());
		applicantRefereesEl.addActionListener(FormEvent.ONCHANGE);
		applicantRefereesEl.setEnabled(!readOnly);
		if(position.isApplicantRefereeManagementEnabled()) {
			// Fallback if referee management was disabled
			if(recruitingModule.isReferenceApplicantManagement()) {
				applicantRefereesEl.select(REFEREE_APP_MANAGE, true);
			} else {
				applicantRefereesEl.select(REFEREE_ENABLED, true);
			}
		} else if(position.isRefereeRecommendationEnabled()) {
			applicantRefereesEl.select(REFEREE_ENABLED, true);
		} else {
			applicantRefereesEl.select(REFEREE_DISABLED, true);
		}
		
		Date refDeadline = position.getRefereeRecommandationDeadline();
		refereeDeadlineEl = uifactory.addDateChooser("ref.deadline", "edit.reference.deadline", refDeadline, formLayout);
		refereeDeadlineEl.setMandatory(true);
		refereeDeadlineEl.setEnabled(!readOnly);
		
		Date appDeadline = position.getApplicantRefereeManagementDeadline();
		applicantDeadlineEl = uifactory.addDateChooser("ref.app.deadline", "edit.referee.applicant.deadline", appDeadline, formLayout);
		applicantDeadlineEl.setMandatory(true);
		applicantDeadlineEl.setEnabled(!readOnly);
		
		String minRefs = position.getMinReferees() == null ? null : position.getMinReferees().toString();
		minReferenceEl = uifactory.addTextElement("edit.min.references", "edit.min.references", 4, minRefs, formLayout);
		minReferenceEl.setMandatory(true);
		minReferenceEl.setEnabled(!readOnly);
		String maxRefs = position.getMaxReferees() == null ? null : position.getMaxReferees().toString();
		maxReferenceEl = uifactory.addTextElement("edit.max.references", "edit.max.references", 4, maxRefs, formLayout);
		maxReferenceEl.setMandatory(true);
		maxReferenceEl.setEnabled(!readOnly);
		
		String[] sendMailKeys = new String[] { ReferenceSendMailType.auto.name(), ReferenceSendMailType.staff.name() };
		String[] sendMailValues = new String[] { translate("edit.send.mail.referees.auto"), translate("edit.send.mail.referees.staff") };
		sendRefereeMailEl = uifactory.addDropdownSingleselect("edit.send.mail.referees", formLayout, sendMailKeys, sendMailValues, null);
		sendRefereeMailEl.setEnabled(!readOnly);
		boolean sendMailSelected = false;
		if(position.getRefereeRecommandationSendMailType() != null) {
			for(String sendMailKey:sendMailKeys) {
				if(position.getRefereeRecommandationSendMailType().name().equals(sendMailKey)) {
					sendRefereeMailEl.select(sendMailKey, true);
					sendMailSelected = true;
				}
			}
		}
		if(!sendMailSelected) {
			ReferenceSendMailType type = recruitingModule.getReferenceSendEmail();
			sendRefereeMailEl.select(type.name(), true);
		}

		String refereeTemplate = getRefereeTemplate();
		recommendationMailTemplateEl = uifactory.addRichTextElementForStringData("edit.template.referee", "reference.mail", refereeTemplate, 18, 60,
				false, null, null, formLayout, ureq.getUserSession(), getWindowControl());
		recommendationMailTemplateEl.getEditorConfiguration().setRelativeUrls(false);
		recommendationMailTemplateEl.getEditorConfiguration().setRemoveScriptHost(false);
		recommendationMailTemplateEl.getEditorConfiguration().setPathInStatusBar(false);
		recommendationMailTemplateEl.setEnabled(!readOnly);
		
		String page = velocity_root + "/links.html";
		FormLayoutContainer variablesCont = FormLayoutContainer.createCustomFormLayout("links", getTranslator(), page);
		formLayout.add(variablesCont);

		variablesButton = LinkFactory.createLink("edit.template.variables", variablesCont.getFormItemComponent(), listener);
		variablesButton.setDomReplacementWrapperRequired(false);
		variablesButton.setIconLeftCSS("o_icon o_icon_help");
		variablesButton.setPopup(new LinkPopupSettings(800, 600, "Variables"));
		variablesButton.setVisible(!readOnly);
		
		previewLink = uifactory.addFormLink("edit.template.preview", variablesCont, Link.LINK);
		previewLink.setDomReplacementWrapperRequired(false);
		previewLink.setIconLeftCSS("o_icon o_icon_preview");
		previewLink.setVisible(!readOnly);
		
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		buttonLayout.setVisible(!readOnly);
		uifactory.addFormSubmitButton("submit", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		
		updateGUI();
	}
	
	private void updateGUI() {
		boolean applicantEnabled = REFEREE_APP_MANAGE.equals(applicantRefereesEl.getSelectedKey());
		boolean referenceEnabled = applicantEnabled || REFEREE_ENABLED.equals(applicantRefereesEl.getSelectedKey());
		refereeDeadlineEl.setVisible(referenceEnabled);
		applicantDeadlineEl.setVisible(applicantEnabled);
		minReferenceEl.setVisible(referenceEnabled);
		maxReferenceEl.setVisible(referenceEnabled);
		sendRefereeMailEl.setVisible(referenceEnabled);
		recommendationMailTemplateEl.setVisible(referenceEnabled);
		previewLink.setVisible(referenceEnabled);
		variablesButton.setVisible(referenceEnabled);
		
		sendRefereeMailEl.setEnabled(!applicantEnabled);
		if(applicantEnabled) {
			sendRefereeMailEl.select(ReferenceSendMailType.auto.name(), true);
		}
	}
	
	@Override
	public Position getPosition() {
		return position;
	}
	
	@Override
	public void updatePosition(Position updatedPosition) {
		this.position = updatedPosition;
		String template = getRefereeTemplate();
		recommendationMailTemplateEl.setValue(template);
	}
	
	private String getRefereeTemplate() {
		String refereeTemplate = position.getRefereeRecommandationMailTemplate();
		if(!StringHelper.containsNonWhitespace(refereeTemplate)) {
			refereeTemplate = translate("reference.recommendation.mail.body");
		}
		return refereeTemplate;
	}
	
	private Long getLong(TextElement el) {
		Long lValue = null;
		if(el.isVisible()) {
			String val = el.getValue();
			if(StringHelper.isLong(val)) {
				try {
					lValue = Long.valueOf(val);
				} catch (NumberFormatException e) {
					logError("Cannot parse this number: " + val, e);
				}
			}
		}
		return lValue;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		minReferenceEl.clearError();
		maxReferenceEl.clearError();
		refereeDeadlineEl.clearError();
		applicantDeadlineEl.clearError();
		if(!applicantRefereesEl.isOneSelected()) {
			applicantRefereesEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else if(!REFEREE_DISABLED.equals(applicantRefereesEl.getSelectedKey())) {
			allOk &= validateInteger(minReferenceEl);
			allOk &= validateInteger(maxReferenceEl);
			if(allOk) {
				Long min = getLong(minReferenceEl);
				Long max = getLong(maxReferenceEl);
				if(min == null) {
					minReferenceEl.setErrorKey("form.legende.mandatory");
					allOk &= false;
				}
				if(max == null) {
					maxReferenceEl.setErrorKey("form.legende.mandatory");
					allOk &= false;
				} 
				if(min != null && max != null && min.compareTo(max) > 0) {
					maxReferenceEl.setErrorKey("error.min.bigger.max");
					allOk &= false;
				}
			}

			if(refereeDeadlineEl.getDate() == null) {
				refereeDeadlineEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			} else {
				allOk &= validateYearElement(refereeDeadlineEl);
			}
			
			if(REFEREE_APP_MANAGE.equals(applicantRefereesEl.getSelectedKey())) {
				if(applicantDeadlineEl.getDate() == null) {
					applicantDeadlineEl.setErrorKey("form.legende.mandatory");
					allOk &= false;
				} else {
					allOk &= validateYearElement(applicantDeadlineEl);
				}
			}
		}
		
		recommendationMailTemplateEl.clearError();
		if(StringHelper.containsNonWhitespace(recommendationMailTemplateEl.getValue())) {
			allOk &= checkTemplate(recommendationMailTemplateEl);
		}
		
		return allOk;
	}
	
	private boolean validateYearElement(DateChooser dateEl) {
		boolean ok = true;
		if(dateEl.getDate() != null) {
			int currentYear = Calendar.getInstance().get(Calendar.YEAR) + 5;
			try {
				Calendar cal = Calendar.getInstance();
				cal.setTime(dateEl.getDate());
				int year = cal.get(Calendar.YEAR);
				if(year < 2010 || year > currentYear) {
					ok &= false;
					dateEl.setErrorKey("deadline.error", new String[] { Integer.toString(currentYear) });
				}
			} catch (NumberFormatException e) {
				ok =false;
				dateEl.setErrorKey("deadline.error", new String[] { Integer.toString(currentYear) });
			}
		}
		return ok;
	}
	
	private boolean validateInteger(TextElement el) {
		boolean allOk = true;

		if(StringHelper.containsNonWhitespace(el.getValue())) {
			try {
				Long.valueOf(el.getValue());
			} catch (NumberFormatException e) {
				//to nothing
				el.setErrorKey("form.error.nointeger");
			}
		}
		
		return allOk;
	}
	
	private boolean checkTemplate(TextElement element) {
		Application mailApp = ReferenceHelper.generateDummyApplication(position);
		Reference mailReference = ReferenceHelper.generateDummyReference();
		VariablesValidationContext context = new VariablesValidationContext();

		Identity headOfCommittee = ReferenceHelper.generateDummyHeadOfCommittee();
		Identity secretary = ReferenceHelper.generateDummySecretary();
		String content = element.getValue();
		MailAttachment letter = mailService.toAttachment(position.getRefereeRecommandationMailLetter(), mailApp, getLocale());

		SubjectAndBody subjectAndBody = new SubjectAndBody("", content, letter);
		ApplicationMailTemplate template = new RecruitingMailTemplate(null, "expert", "Expert",
				"", content, letter, headOfCommittee, secretary,
				subjectAndBody, salutationGenerator, getTranslator());

		template.putVariablesInMailContext(context, mailApp, null, mailReference, null, null, null, position);
		
		MailerSender sender = recruitingService.createMailSender();
		boolean allOk = sender.checkTemplate(context, content);
		if(!context.getUnkownVariables().isEmpty()) {
			String i18nKey = context.getUnkownVariables().size() == 1 ? "error.template.unkown.variable" : "error.template.unkown.variables";
			element.setErrorKey(i18nKey, new String[] { context.stringuifiedUnkownVariables() });
			allOk &= false;
		}
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
		removeAsListenerAndDispose(cmc);
		mailPreviewCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Event doneEvent = Event.DONE_EVENT;
		if(position.getKey() != null) {
			position = recruitingService.getPosition(position.getKey());
		} else {
			doneEvent = new NewPositionSavedEvent();
		}
		
		String before = auditService.toAuditXml(position);

		boolean applicantCanManage = REFEREE_APP_MANAGE.equals(applicantRefereesEl.getSelectedKey());
		boolean referenceEnabled = applicantCanManage || REFEREE_ENABLED.equals(applicantRefereesEl.getSelectedKey());
		if(referenceEnabled != position.isRefereeRecommendationEnabled()) {
			logAudit("Referees " + (referenceEnabled ? "enabled" : "disabled") + " for position: " + position.toString(), null);
		}
		position.setRefereeRecommendationEnabled(referenceEnabled);
		position.setApplicantRefereeManagementEnabled(applicantCanManage);
		if(referenceEnabled) {
			position.setRefereeRecommandationDeadline(refereeDeadlineEl.getDate());
			position.setMinReferees(getLong(minReferenceEl));
			position.setMaxReferees(getLong(maxReferenceEl));
			position.setRefereeRecommandationMailTemplate(recommendationMailTemplateEl.getValue());
			if(sendRefereeMailEl.isOneSelected()) {
				position.setRefereeRecommandationSendMailType(ReferenceSendMailType.valueOf(sendRefereeMailEl.getSelectedKey()));
			} else {
				position.setRefereeRecommandationSendMailType(null);
			}
			if(applicantCanManage) {
				Date deadline = applicantDeadlineEl.getDate();
				deadline = RecruitingHelper.endOfDay(deadline);
				position.setApplicantRefereeManagementDeadline(deadline);
			} else {
				position.setApplicantRefereeManagementDeadline(null);
			}
		} else {
			position.setRefereeRecommandationDeadline(null);
			position.setApplicantRefereeManagementDeadline(null);
			position.setMinReferees(null);
			position.setMaxReferees(null);
			position.setRefereeRecommandationSendMailType(null);
			position.setRefereeRecommandationMailTemplate(null);
		}

		position = recruitingService.savePosition(position);
		dbInstance.commit();
		getLogger().info(Tracing.M_AUDIT, "Update referees / experts position: {}", position.toStringFull());
		
		String after = auditService.toAuditXml(position);
		if(!before.equals(after)) {
			String messageI18n = "audit.log.position.change.configuration";
			String[] messageArgs = new String[] { position.getMLTitle(recruitingModule.getPositionDefaultLocale()) };
			auditService.auditPositionLog(Action.changeConfiguration, ActionTarget.position, before, after,
					messageI18n, messageArgs, getTranslator(), position, getIdentity());
		}
		
		fireEvent(ureq, doneEvent);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(applicantRefereesEl == source) {
			updateGUI();
			markDirty();
		} else if(previewLink == source) {
			doOpenPreview(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doOpenVariables(UserRequest ureq) {
		ControllerCreator ctrlCreator = (lureq, lwControl) -> {
			Controller mailVariablesCtrl = new MailVariablesController(lureq, lwControl,
					position, true, false, false, ReferenceHelper.generateDummyReference(), null, true, true, false);
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
		Identity headOfCommittee = recruitingService.getHeadOfCommittee(position);
		if(headOfCommittee == null) {
			headOfCommittee = ReferenceHelper.generateDummyHeadOfCommittee();
		}
		Identity secretary = recruitingService.getSecretary(position);
		if(secretary == null) {
			secretary = ReferenceHelper.generateDummySecretary();
		}
		
		Reference reference = ReferenceHelper.generateDummyReference();
		Date deadline = refereeDeadlineEl.getDate();
		if(deadline != null) {
			reference.setSubmissionDeadline(deadline);
		}
		Application app = ReferenceHelper.generateDummyApplication(position);
		
		String[] args = ReferenceHelper.generateMailArguments(headOfCommittee, position, app, reference, salutationGenerator, getTranslator());
		String subject = translate("reference.recommendation.mail.subject", args);
		String body = recommendationMailTemplateEl.getValue();
		MailAttachment letter = mailService.toAttachment(position.getRefereeRecommandationMailLetter(), app, getLocale());
		mailPreviewCtrl = new PreviewEmailController(ureq, getWindowControl(), subject, body, letter,
				position, app, reference, null, secretary, headOfCommittee, getTranslator()) ;
		listenTo(mailPreviewCtrl);
		
		String title = translate("edit.template.preview");
		cmc = new CloseableModalController(getWindowControl(), "c", mailPreviewCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
}