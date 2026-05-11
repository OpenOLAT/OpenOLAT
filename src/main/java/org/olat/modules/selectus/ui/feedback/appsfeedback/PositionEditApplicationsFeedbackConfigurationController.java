/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.feedback.appsfeedback;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
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
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.selectus.FeedbackService;
import org.olat.modules.selectus.MailService;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.manager.ApplicationMailTemplate;
import org.olat.modules.selectus.manager.MailerSender;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.SubjectAndBody;
import org.olat.modules.selectus.model.mail.MailAttachment;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingMailTemplate;
import org.olat.modules.selectus.ui.position.PositionEditableController;
import org.olat.modules.selectus.ui.reference.ReferenceHelper;
import org.olat.modules.selectus.ui.rejection.MailVariablesController;
import org.olat.modules.selectus.ui.rejection.PreviewEmailController;
import org.olat.modules.selectus.ui.rejection.VariablesValidationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * 
 * Initial date: 21 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionEditApplicationsFeedbackConfigurationController extends FormBasicController implements PositionEditableController {

	private static final String[] enableKeys = new String[]{ "on" };
	private static final String[] monthKeys = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"};
	private String[] monthValues = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};

	private FormLink previewLink;
	private Link variablesButton;
	private MultipleSelectionElement enableFeedbackEl;
	private TextElement feedbackDeadlineDayElement;
	private SingleSelection feedbackDeadlineMonthElement;
	private TextElement feedbackDeadlineYearElement;
	private FormLayoutContainer feedbackDeadlineContainer;
	private RichTextElement feedbackMailTemplateEl;
	
	private Position position;
	private ApplicationsFeedbackConfiguration configuration;
	
	private CloseableModalController cmc;
	private PreviewEmailController mailPreviewCtrl;
	
	@Autowired
	private MailService mailService;
	@Autowired
	private FeedbackService feedbackService;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	
	public PositionEditApplicationsFeedbackConfigurationController(UserRequest ureq, WindowControl wControl,
			Position position, ApplicationsFeedbackConfiguration configuration) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.configuration = configuration;
		this.position = position;
		for(int i=monthKeys.length; i-->0; ) {
			monthValues[i] = translate("month.long." + i);
		}
		initForm(ureq);
		updateGUI();
	}
	
	public String getConfigurationName() {
		return configuration.getConfigurationName();
	}
	
	public boolean isEnabled() {
		return enableFeedbackEl.isAtLeastSelected(1);
	}

	@Override
	public Position getPosition() {
		return position;
	}

	@Override
	public void updatePosition(Position updatedPosition) {
		position = updatedPosition;
		
		if(configuration != null) {
			configuration = feedbackService.getApplicationsFeedbackConfiguration(configuration);
			if(StringHelper.containsNonWhitespace(configuration.getMailTemplate())) {
				feedbackMailTemplateEl.setValue(getBodyTemplate());
			}
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("edit.form_description.apps.feedback");
		formLayout.setElementCssClass("o_sel_edit_position_public_feedback_form");
		
		String[] enableValues = new String[]{ translate("enable") };
		
		enableFeedbackEl = uifactory.addCheckboxesHorizontal("edit.apps.feedback.enable", formLayout, enableKeys, enableValues);
		enableFeedbackEl.addActionListener(FormEvent.ONCHANGE);
		if(configuration.isEnabled()) {
			enableFeedbackEl.select(enableKeys[0], true);
		}
		
		// deadline container
		String feedbackDeadlineCont = velocity_root + "/edit_public_feedback.html";
		feedbackDeadlineContainer = FormLayoutContainer.createCustomFormLayout("public.feedback.deadline", getTranslator(), feedbackDeadlineCont);
		feedbackDeadlineContainer.setRootForm(mainForm);
		feedbackDeadlineContainer.setLabel("edit.public.feedback.deadline", null);
		feedbackDeadlineContainer.setMandatory(true);
		formLayout.add(feedbackDeadlineContainer);
		
		String feedbackDay = "";
		String feedbackMonth= "0";
		String feedbackYear = "";
		Date feedbackDeadline = configuration.getDeadline();
		if(feedbackDeadline != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(feedbackDeadline);
			feedbackDay = Integer.toString(cal.get(Calendar.DATE));
			feedbackMonth = Integer.toString(cal.get(Calendar.MONTH));
			feedbackYear = Integer.toString(cal.get(Calendar.YEAR));
		}
		
		feedbackDeadlineDayElement = uifactory.addTextElement("public.feedback.deadline.day", null, 2, feedbackDay, feedbackDeadlineContainer);
		feedbackDeadlineDayElement.setDomReplacementWrapperRequired(false);
		feedbackDeadlineDayElement.setDisplaySize(2);
		feedbackDeadlineDayElement.setMandatory(true);
		
		feedbackDeadlineMonthElement = uifactory.addDropdownSingleselect("public.feedback.deadline.month", null, feedbackDeadlineContainer, monthKeys, monthValues, null);
		feedbackDeadlineMonthElement.setDomReplacementWrapperRequired(false);
		feedbackDeadlineMonthElement.setMandatory(true);
		feedbackDeadlineMonthElement.select(feedbackMonth, true);
		
		feedbackDeadlineYearElement = uifactory.addTextElement("public.feedback.deadline.year", null, 4, feedbackYear, feedbackDeadlineContainer);
		feedbackDeadlineYearElement.setDomReplacementWrapperRequired(false);
		feedbackDeadlineYearElement.setDisplaySize(4);
		feedbackDeadlineYearElement.setMandatory(true);
		
		String bodyTemplate = getBodyTemplate();
		feedbackMailTemplateEl = uifactory.addRichTextElementForStringData("edit.template.referee", "reference.mail", bodyTemplate, 18, 60,
				false, null, null, formLayout, ureq.getUserSession(), getWindowControl());
		feedbackMailTemplateEl.getEditorConfiguration().setRelativeUrls(false);
		feedbackMailTemplateEl.getEditorConfiguration().setRemoveScriptHost(false);
		feedbackMailTemplateEl.getEditorConfiguration().setPathInStatusBar(false);
		
		String page = velocity_root + "/links.html";
		FormLayoutContainer variablesCont = FormLayoutContainer.createCustomFormLayout("links", getTranslator(), page);
		formLayout.add(variablesCont);

		variablesButton = LinkFactory.createLink("edit.template.variables", variablesCont.getFormItemComponent(), listener);
		variablesButton.setDomReplacementWrapperRequired(false);
		variablesButton.setIconLeftCSS("o_icon o_icon_help");
		variablesButton.setPopup(new LinkPopupSettings(800, 600, "Variables"));
		
		previewLink = uifactory.addFormLink("edit.template.preview", variablesCont, Link.LINK);
		previewLink.setDomReplacementWrapperRequired(false);
		previewLink.setIconLeftCSS("o_icon o_icon_preview");
		
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("submit", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
	
	private String getBodyTemplate() {
		String bodyTemplate = configuration.getMailTemplate();
		if(!StringHelper.containsNonWhitespace(bodyTemplate)) {
			bodyTemplate = FeedbackHelper.getDefaultTemplateBody(position, salutationGenerator, getLocale());
		}
		if(!StringHelper.isHtml(bodyTemplate)) {
			bodyTemplate = Formatter.stripTabsAndReturns(bodyTemplate).toString();
		}
		return bodyTemplate;
	}
	
	private void updateGUI() {
		boolean enabled = enableFeedbackEl.isAtLeastSelected(1);
		feedbackDeadlineContainer.setVisible(enabled);
		feedbackMailTemplateEl.setVisible(enabled);
		previewLink.setVisible(enabled);
		variablesButton.setVisible(enabled);
	}
	
	private Date getFeedbackDeadline() {
		String dayStr = feedbackDeadlineDayElement.getValue();
		String monthStr = feedbackDeadlineMonthElement.getSelectedKey();
		String yearStr = feedbackDeadlineYearElement.getValue();
		
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
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		feedbackDeadlineYearElement.clearError();
		if(getFeedbackDeadline() == null) {
			feedbackDeadlineYearElement.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else {
			allOk &= validateYearElement(feedbackDeadlineYearElement);
		}
		
		feedbackMailTemplateEl.clearError();
		if(StringHelper.containsNonWhitespace(feedbackMailTemplateEl.getValue())) {
			allOk &= checkTemplate(feedbackMailTemplateEl);
		}

		return allOk;
	}
	
	private boolean checkTemplate(TextElement element) {
		Application mailApp = ReferenceHelper.generateDummyApplication(position);
		List<Application> mailApps = Collections.singletonList(mailApp);
		VariablesValidationContext context = new VariablesValidationContext();

		Identity headOfCommittee = ReferenceHelper.generateDummyHeadOfCommittee();
		Identity secretary = ReferenceHelper.generateDummySecretary();
		String content = element.getValue();
		MailAttachment letter = null;
		if(configuration != null && StringHelper.containsNonWhitespace(configuration.getMailLetter())) {
			letter = mailService.toAttachment(configuration.getMailLetter(), mailApp, getLocale());
		}

		SubjectAndBody subjectAndBody = new SubjectAndBody("", content, letter);
		ApplicationMailTemplate template = new RecruitingMailTemplate(null, "feedback", "Feedback",
				subjectAndBody.getSubject(), subjectAndBody.getBody(), subjectAndBody.getLetter(),
				headOfCommittee, secretary, subjectAndBody, salutationGenerator, getTranslator());
		
		ApplicationFeedback feedback = FeedbackHelper.generateDummyFeedback(configuration, getFeedbackDeadline());
		List<ApplicationFeedback> feedbacks = Collections.singletonList(feedback);
		Identity member = FeedbackHelper.generateDummyMember();
		
		template.putVariablesInMailContext(context, mailApp, mailApps, null, member, feedbacks, configuration, position);
		
		MailerSender sender = recruitingService.createMailSender();
		boolean allOk = sender.checkTemplate(context, content);
		if(!context.getUnkownVariables().isEmpty()) {
			String i18nKey = context.getUnkownVariables().size() == 1 ? "error.template.unkown.variable" : "error.template.unkown.variables";
			element.setErrorKey(i18nKey, new String[] { context.stringuifiedUnkownVariables() });
			allOk &= false;
		}
		return allOk;
	}

	private boolean validateYearElement(TextElement textEl) {
		boolean ok = true;
		if(StringHelper.containsNonWhitespace(textEl.getValue())) {
			int currentYear = Calendar.getInstance().get(Calendar.YEAR) + 5; 
			try {
				int year = Integer.parseInt(textEl.getValue());
				if(year < 2010 || year > currentYear) {
					ok &= false;
					textEl.setErrorKey("deadline.error", new String[] { Integer.toString(currentYear) });
				}
			} catch (NumberFormatException e) {
				ok =false;
				textEl.setErrorKey("deadline.error", new String[] { Integer.toString(currentYear) });
			}
		}
		return ok;
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
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enableFeedbackEl == source) {
			updateGUI();
			markDirty();
		} else if(previewLink == source) {
			doOpenPreview(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		configuration.setEnabled(enableFeedbackEl.isAtLeastSelected(1));
		
		configuration.setDeadline(getFeedbackDeadline());
		configuration.setMailTemplate(feedbackMailTemplateEl.getValue());
		
		configuration = feedbackService.updateApplicationsFeedbackConfiguration(configuration);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void doOpenVariables(UserRequest ureq) {
		ControllerCreator ctrlCreator = (lureq, lwControl) -> {
			Controller mailVariablesCtrl = new MailVariablesController(lureq, lwControl,
					position, false, true, false, null, configuration, true, true, true);
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
		
		ApplicationFeedback feedback = FeedbackHelper.generateDummyFeedback(configuration, getFeedbackDeadline());
		List<ApplicationFeedback> feedbacks = Collections.singletonList(feedback);
		Application app = ReferenceHelper.generateDummyApplication(position);
		List<Application> apps = Collections.singletonList(app);
		Identity member = FeedbackHelper.generateDummyMember();
		
		String[] args = FeedbackHelper.generateMailArguments(headOfCommittee, position, app, configuration, member, salutationGenerator, getTranslator());
		String subject = translate("apps.feedback.mail.subject", args);
		String body = feedbackMailTemplateEl.getValue();
		MailAttachment letter = null;
		if(configuration != null && StringHelper.containsNonWhitespace(configuration.getMailLetter())) {
			letter = mailService.toAttachment(configuration.getMailLetter(), app, getLocale());
		}
		mailPreviewCtrl = new PreviewEmailController(ureq, getWindowControl(), subject, body, letter,
				position, member, apps, feedbacks, configuration, secretary, headOfCommittee, getTranslator()) ;
		listenTo(mailPreviewCtrl);
		
		String title = translate("edit.template.preview");
		cmc = new CloseableModalController(getWindowControl(), "c", mailPreviewCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
}
