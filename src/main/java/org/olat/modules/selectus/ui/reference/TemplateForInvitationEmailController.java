/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.reference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkPopupSettings;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailerResult;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.manager.ApplicationMailTemplate;
import org.olat.modules.selectus.manager.MailerSender;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceType;
import org.olat.modules.selectus.model.SubjectAndBody;
import org.olat.modules.selectus.model.mail.InvitationVariables;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.rejection.MailVariablesController;
import org.olat.modules.selectus.ui.rejection.VariablesValidationContext;

/**
 * 
 * Initial date: 20.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TemplateForInvitationEmailController extends StepFormBasicController {

	private static final String[] monthKeys = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"};
	private String[] monthValues = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};

	private FormLink variablesButton;
	private TextElement expertSubjectEl;
	private RichTextElement expertBodyEl;
	
	private TextElement recommendationSubjectEl;
	private RichTextElement recommendationBodyEl;
	
	private TextElement comparativeExpertSubjectEl;
	private RichTextElement comparativeExpertBodyEl;
	
	private TextElement submissionDeadlineDayElement;
	private TextElement submissionDeadlineYearElement;
	private SingleSelection submissionDeadlineMonthElement;
	
	private Reference soloReference;
	private final InvitationVariables emailVar;
	private boolean withExpert;
	private boolean withRecommendation;
	private boolean withComparativeExpert;
	
	@Autowired
	private RecruitingService recruitingService;
	
	public TemplateForInvitationEmailController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext,
			Form form, InvitationVariables emailVar) {
		super(ureq, wControl, form, runContext, LAYOUT_DEFAULT, null);
		setTranslator(Util.createPackageTranslator(PositionController.class, getLocale(), getTranslator()));
		this.emailVar = emailVar;
		
		for(int i=monthKeys.length; i-->0; ) {
			monthValues[i] = translate("month.long." + i);
		}
		
		List<Reference> selectedApps = emailVar.getSelectedReferences();
		if(selectedApps.size() == 1) {
			soloReference = selectedApps.get(0);
		}
		for(Reference ref:selectedApps) {
			if(ref.getReferenceType() == ReferenceType.expert) {
				withExpert |= true;
			} else if(ref.getReferenceType() == ReferenceType.recommendation) {
				withRecommendation |= true;
			} else if(ref.getReferenceType() == ReferenceType.comparativeAssessmentExpert) {
				withComparativeExpert |= true;
			}
		}
		
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("wizard.invitation.template.description");
		formLayout.setElementCssClass("o_sel_invitation_mail_to_template");
		
		if(withRecommendation) {
			initFormReferees(formLayout, ureq);
		}
		
		if(withExpert) {
			initFormExperts(formLayout, ureq);
		}
		
		if(withComparativeExpert) {
			initFormComparativeExperts(formLayout, ureq);
		}
		
		if(withRecommendation || withExpert || withComparativeExpert) {
			initSubmissionDeadline(formLayout);
			
			variablesButton = uifactory.addFormLink("edit.template.variables", formLayout, Link.LINK);
			variablesButton.setIconLeftCSS("o_icon o_icon_help");
			variablesButton.setPopup(new LinkPopupSettings(800, 600, "Variables"));
		}
	}
	
	private void initSubmissionDeadline(FormItemContainer formLayout) {
		//submission deadline container
		String pageDeadline = velocity_root + "/edit_deadline.html";
		FormLayoutContainer submissionDeadlineContainer = FormLayoutContainer.createCustomFormLayout("expert.deadline", getTranslator(), pageDeadline);
		submissionDeadlineContainer.setRootForm(mainForm);
		submissionDeadlineContainer.setLabel("edit.expert.deadline", null);
		submissionDeadlineContainer.setMandatory(true);
		formLayout.add(submissionDeadlineContainer);
		
		String day = "";
		String month= "0";
		String year = "";
		Date submissionDeadline = getDefaultSubmissionDeadline();
		if(submissionDeadline != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(submissionDeadline);
			day = Integer.toString(cal.get(Calendar.DATE));
			month = Integer.toString(cal.get(Calendar.MONTH));
			year = Integer.toString(cal.get(Calendar.YEAR));
		}
		
		submissionDeadlineDayElement = uifactory.addTextElement("deadline.day", "", 2, day, submissionDeadlineContainer);
		submissionDeadlineDayElement.setDomReplacementWrapperRequired(false);
		submissionDeadlineDayElement.setDisplaySize(2);
		submissionDeadlineDayElement.setMandatory(true);
		
		submissionDeadlineMonthElement = uifactory.addDropdownSingleselect("deadline.month", "", submissionDeadlineContainer, monthKeys, monthValues, null);
		submissionDeadlineMonthElement.setDomReplacementWrapperRequired(false);
		submissionDeadlineMonthElement.setMandatory(true);
		submissionDeadlineMonthElement.select(month, true);
		
		submissionDeadlineYearElement = uifactory.addTextElement("deadline.year", "", 4, year, submissionDeadlineContainer);
		submissionDeadlineYearElement.setDomReplacementWrapperRequired(false);
		submissionDeadlineYearElement.setDisplaySize(4);
		submissionDeadlineYearElement.setMandatory(true);
	}
	
	private void initFormReferees(FormItemContainer formLayout, UserRequest ureq) {
		ApplicationMailTemplate recommendationTemplate = emailVar.getRecommendationTemplate();
		String recommendationSubject = recommendationTemplate.getSubjectTemplate();
		String recommendationBody = recommendationTemplate.getBodyTemplate();
		
		if(soloReference != null) {
			MailerResult mailerResult = new MailerResult(); 
			SubjectAndBody subjectAndBody2 = recruitingService.createMailSender()
					.createWithContext(soloReference.getApplication(), null, soloReference, null, null, null, emailVar.getPosition(), recommendationTemplate, mailerResult);
			if(mailerResult.isSuccessful()) {
				recommendationSubject = subjectAndBody2.getSubject();
				recommendationBody = subjectAndBody2.getBody();
			}
		}
	
		recommendationSubjectEl = uifactory.addTextElement("subjectRec", "mailtemplateform.subject.recommendation", 128, recommendationSubject, formLayout);
		recommendationSubjectEl.setDisplaySize(60);
		recommendationSubjectEl.setMandatory(true);
		recommendationBodyEl = uifactory.addRichTextElementForStringData("bodyRec", "mailtemplateform.body", recommendationBody, 16, 60,
				false, null, null, formLayout, ureq.getUserSession(), getWindowControl());
		recommendationBodyEl.getEditorConfiguration().setRelativeUrls(false);
		recommendationBodyEl.getEditorConfiguration().setRemoveScriptHost(false);
		recommendationBodyEl.getEditorConfiguration().setPathInStatusBar(false);
		recommendationBodyEl.setMandatory(true);
	}
	
	private void initFormExperts(FormItemContainer formLayout, UserRequest ureq) {
		ApplicationMailTemplate expertTemplate = emailVar.getExpertTemplate();
		String expertSubject = expertTemplate.getSubjectTemplate();
		String expertBody = expertTemplate.getBodyTemplate();
		
		if(soloReference != null) {
			MailerResult mailerResult = new MailerResult(); 
			SubjectAndBody subjectAndBody2 = recruitingService.createMailSender()
					.createWithContext(soloReference.getApplication(), null, soloReference, null, null, null, emailVar.getPosition(), expertTemplate, mailerResult);
			if(mailerResult.isSuccessful()) {
				expertSubject = subjectAndBody2.getSubject();
				expertBody = subjectAndBody2.getBody();
			}
		}
			
		expertSubjectEl = uifactory.addTextElement("subjectExp", "mailtemplateform.subject.expert", 128, expertSubject, formLayout);
		expertSubjectEl.setDisplaySize(60);
		expertSubjectEl.setMandatory(true);
		expertBodyEl = uifactory.addRichTextElementForStringData("bodyExp", "mailtemplateform.body", expertBody, 16, 60,
				false, null, null, formLayout, ureq.getUserSession(), getWindowControl());
		expertBodyEl.getEditorConfiguration().setRelativeUrls(false);
		expertBodyEl.getEditorConfiguration().setRemoveScriptHost(false);
		expertBodyEl.getEditorConfiguration().setPathInStatusBar(false);
		expertBodyEl.setMandatory(true);
	}
	
	private void initFormComparativeExperts(FormItemContainer formLayout, UserRequest ureq) {
		ApplicationMailTemplate comparativeExpertTemplate = emailVar.getComparativeExpertTemplate();
		String comparativeExpertSubject = comparativeExpertTemplate.getSubjectTemplate();
		String comparativeExpertBody = comparativeExpertTemplate.getBodyTemplate();
		
		if(soloReference != null) {
			MailerResult mailerResult = new MailerResult(); 
			SubjectAndBody subjectAndBody2 = recruitingService.createMailSender()
					.createWithContext(soloReference.getApplication(), null, soloReference, null, null, null, emailVar.getPosition(), comparativeExpertTemplate, mailerResult);
			if(mailerResult.isSuccessful()) {
				comparativeExpertSubject = subjectAndBody2.getSubject();
				comparativeExpertBody = subjectAndBody2.getBody();
			}
		}
			
		comparativeExpertSubjectEl = uifactory.addTextElement("subjectCompExp", "mailtemplateform.subject.comparative.expert", 128, comparativeExpertSubject, formLayout);
		comparativeExpertSubjectEl.setDisplaySize(60);
		comparativeExpertSubjectEl.setMandatory(true);
		comparativeExpertBodyEl = uifactory.addRichTextElementForStringData("bodyCompExp", "mailtemplateform.body", comparativeExpertBody, 16, 60,
				false, null, null, formLayout, ureq.getUserSession(), getWindowControl());
		comparativeExpertBodyEl.getEditorConfiguration().setRelativeUrls(false);
		comparativeExpertBodyEl.getEditorConfiguration().setRemoveScriptHost(false);
		comparativeExpertBodyEl.getEditorConfiguration().setPathInStatusBar(false);
		comparativeExpertBodyEl.setMandatory(true);
	}
	
	/**
	 * @return The latest date found in submissions deadline in the referee settings
	 * and the list of references to update
	 */
	private Date getDefaultSubmissionDeadline() {
		List<Date> submissionDates = new ArrayList<>();
		 if(withRecommendation && emailVar.getPosition().getRefereeRecommandationDeadline() != null) {
			submissionDates.add(emailVar.getPosition().getRefereeRecommandationDeadline());
		}
		 if(withExpert && emailVar.getPosition().getExpertRecommandationDeadline() != null) {
			submissionDates.add(emailVar.getPosition().getExpertRecommandationDeadline());
		} else if(withComparativeExpert && emailVar.getPosition().getComparativeAssessmentExpertDeadline() != null) {
			submissionDates.add(emailVar.getPosition().getComparativeAssessmentExpertDeadline());
		}
		Collections.sort(submissionDates);
		Date submissionDate = submissionDates.isEmpty() ? null : submissionDates.get(0);
		List<Reference> selectedRefs = emailVar.getSelectedReferences();
		if(selectedRefs != null) {
			for(Reference ref:selectedRefs) {
				Date deadline = ref.getSubmissionDeadline();
				if(submissionDate == null) {
					submissionDate = deadline;
				} else if(deadline != null && submissionDate.before(deadline)) {
					submissionDate = deadline;
				}
			}
		}

		return submissionDate;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		if(withExpert) {
			allOk &= checkElement(expertSubjectEl, ReferenceType.expert);
			allOk &= checkElement(expertBodyEl, ReferenceType.expert);
		}
		if(withRecommendation) {
			allOk &= checkElement(recommendationSubjectEl, ReferenceType.recommendation);
			allOk &= checkElement(recommendationBodyEl, ReferenceType.recommendation);
		}
		if(withComparativeExpert) {
			allOk &= checkElement(comparativeExpertSubjectEl, ReferenceType.comparativeAssessmentExpert);
			allOk &= checkElement(comparativeExpertBodyEl, ReferenceType.comparativeAssessmentExpert);
		}
		
		if(submissionDeadlineYearElement != null) {
			submissionDeadlineYearElement.clearError();
			if(getSubmissionDeadline() == null) {
				submissionDeadlineYearElement.setErrorKey("form.legende.mandatory");
				allOk &= false;
			} else {
				allOk &= validateYearElement(submissionDeadlineYearElement);
			}
		}
		return allOk;
	}
	
	private boolean validateYearElement(TextElement textEl) {
		boolean ok = true;
		if(StringHelper.containsNonWhitespace(textEl.getValue())) {
			int currentYear = Calendar.getInstance().get(Calendar.YEAR);
			int maxYear = currentYear + 5;
			try { 
				int year = Integer.parseInt(textEl.getValue());
				if(year < currentYear || year > maxYear) {
					ok &= false;
					textEl.setErrorKey("deadline.error", new String[] { Integer.toString(maxYear) });
				}
			} catch (NumberFormatException e) {
				ok = false;
				textEl.setErrorKey("deadline.error", new String[] { Integer.toString(maxYear) });
			}
		}
		return ok;
	}
	
	private boolean checkElement(TextElement element, ReferenceType referenceType) {
		boolean ok = true;
		if(element != null) {
			element.clearError();
			if(element.isVisible()) {
				String value = element.getValue();
				if(!StringHelper.containsNonWhitespace(value)) {
					element.setErrorKey("form.legende.mandatory");
					ok = false;
				} else if(!checkTemplate(element, referenceType)) {
					ok = false;
				}
			}
		}
		return ok;
	}
	
	private boolean checkTemplate(TextElement element, ReferenceType referenceType) {
		if(emailVar.getSelectedReferences().isEmpty()) {
			return true;
		}
		
		Position position = emailVar.getPosition();
		ApplicationMailTemplate template;
		if(referenceType == ReferenceType.expert) {
			template = emailVar.getExpertTemplate();	
		} else if(referenceType == ReferenceType.recommendation) {
			template = emailVar.getRecommendationTemplate();
		} else if(referenceType == ReferenceType.comparativeAssessmentExpert) {
			template = emailVar.getComparativeExpertTemplate();
		} else {
			return true;
		}

		Application mailApp = null;
		Set<Application> mailApps = new HashSet<>();
		Reference mailReference = null;
		for(Reference reference:emailVar.getSelectedReferences()) {
			if(reference.getReferenceType() == referenceType) {
				mailReference = reference;
				if(reference.getReferenceType() == ReferenceType.comparativeAssessmentExpert) {
					List<Application> apps = recruitingService.getReferenceToApplicationsList(reference);
					mailApps.addAll(apps);
				} else {
					mailApp = reference.getApplication();
				}
			}
		}
		
		VariablesValidationContext context = new VariablesValidationContext();
		template.putVariablesInMailContext(context, mailApp, new ArrayList<>(mailApps), mailReference, null, null, null, position);
		
		MailerSender sender = recruitingService.createMailSender();
		boolean allOk = sender.checkTemplate(context, element.getValue());
		if(!context.getUnkownVariables().isEmpty()) {
			String i18nKey = context.getUnkownVariables().size() == 1 ? "error.template.unkown.variable" : "error.template.unkown.variables";
			element.setErrorKey(i18nKey, new String[] { context.stringuifiedUnkownVariables() });
			allOk &= false;
		} else if(!ReferenceHelper.validateLinks(element, soloReference)) {
			allOk &= false;
		}
		return allOk;
	}
	
	private Date getSubmissionDeadline() {
		String dayStr = submissionDeadlineDayElement.getValue();
		String monthStr = submissionDeadlineMonthElement.getSelectedKey();
		String yearStr = submissionDeadlineYearElement.getValue();
		
		try {
			int day = Integer.parseInt(dayStr);
			int month = Integer.parseInt(monthStr);
			int year = Integer.parseInt(yearStr);
			return getDeadline(day, month, year, 0, 0);
		} catch (NumberFormatException e) {
			logDebug("Cannot parse date from: " + dayStr + "." + monthStr + "." + yearStr);
			return null;
		}
	}
	
	private Date getDeadline(int day, int month, int year, int hour, int minute) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DATE, day);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(variablesButton == source) {
			doOpenVariables(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(withExpert) {
			ApplicationMailTemplate expertTemplate = emailVar.getExpertTemplate();
			expertTemplate.setSubjectTemplate(expertSubjectEl.getValue());	
			expertTemplate.setBodyTemplate(expertBodyEl.getValue());
		}
		if(withRecommendation) {
			ApplicationMailTemplate recommendationTemplate = emailVar.getRecommendationTemplate();
			recommendationTemplate.setSubjectTemplate(recommendationSubjectEl.getValue());	
			recommendationTemplate.setBodyTemplate(recommendationBodyEl.getValue());
		}
		if(withComparativeExpert) {
			ApplicationMailTemplate comparativeExpertsTemplate = emailVar.getComparativeExpertTemplate();
			comparativeExpertsTemplate.setSubjectTemplate(comparativeExpertSubjectEl.getValue());	
			comparativeExpertsTemplate.setBodyTemplate(comparativeExpertBodyEl.getValue());
		}
		
		Date submissionDeadline = getSubmissionDeadline();
		emailVar.setSubmissionDeadline(submissionDeadline);
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	private void doOpenVariables(UserRequest ureq) {
		ControllerCreator ctrlCreator = (lureq, lwControl) -> {
			Position position = emailVar.getPosition();
			Controller mailVariablesCtrl = new MailVariablesController(lureq, lwControl,
					position, true, withComparativeExpert, false, ReferenceHelper.generateDummyReference(), null,
					true, true, false);
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
}