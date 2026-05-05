/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.feedback.appsfeedback.wizard;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.manager.ApplicationMailTemplate;
import org.olat.modules.selectus.manager.MailerSender;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.feedback.appsfeedback.FeedbackHelper;
import org.olat.modules.selectus.ui.rejection.MailVariablesController;
import org.olat.modules.selectus.ui.rejection.VariablesValidationContext;

/**
 * 
 * Initial date: 6 mai 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TemplateForContactEmailController extends StepFormBasicController {
	
	private static final String[] monthKeys = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"};
	private String[] monthValues = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
	
	private FormLink variablesButton;
	private TextElement subjectEl;
	private RichTextElement bodyEl;
	private TextElement feedbackDeadlineDayElement;
	private SingleSelection feedbackDeadlineMonthElement;
	private TextElement feedbackDeadlineYearElement;

	private final ContactMembersContext feedbacksContext;

	@Autowired
	private RecruitingService erFrontendManager;
	
	public TemplateForContactEmailController(UserRequest ureq, WindowControl wControl, ContactMembersContext feedbacksContext,
			StepsRunContext runContext, Form form) {
		super(ureq, wControl, form, runContext, LAYOUT_DEFAULT, null);
		setTranslator(Util.createPackageTranslator(PositionController.class, getLocale(), getTranslator()));
		this.feedbacksContext = feedbacksContext;
		for(int i=monthKeys.length; i-->0; ) {
			monthValues[i] = translate("month.long." + i);
		}
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("wizard.template.mail.description");
		formLayout.setElementCssClass("o_sel_mail_to_template");
	
		ApplicationMailTemplate template = feedbacksContext.getMailTemplate();
		String subject = template.getSubjectTemplate();
		subjectEl = uifactory.addTextElement("subjectElem", "mailtemplateform.subject", 256, subject, formLayout);
		subjectEl.setDisplaySize(60);
		subjectEl.setMandatory(true);
		
		String htmlBody = toHtml(template.getBodyTemplate());
		bodyEl = uifactory.addRichTextElementForStringData("bodyElem", "mailtemplateform.body", htmlBody, 20, 60,
				false, null, null, formLayout, ureq.getUserSession(), getWindowControl());
		bodyEl.getEditorConfiguration().setRelativeUrls(false);
		bodyEl.getEditorConfiguration().setRemoveScriptHost(false);
		bodyEl.getEditorConfiguration().setPathInStatusBar(false);
		bodyEl.setMandatory(true);
		bodyEl.setMaxLength(7000);
		
		variablesButton = uifactory.addFormLink("edit.template.variables", formLayout, Link.LINK);
		variablesButton.setIconLeftCSS("o_icon o_icon_help");
		variablesButton.setPopup(new LinkPopupSettings(800, 600, "Variables"));
		
		// deadline container
		String feedbackDeadlineCont = velocity_root + "/edit_deadline.html";
		FormLayoutContainer feedbackDeadlineContainer = FormLayoutContainer.createCustomFormLayout("public.feedback.deadline", getTranslator(), feedbackDeadlineCont);
		feedbackDeadlineContainer.setRootForm(mainForm);
		feedbackDeadlineContainer.setLabel("edit.public.feedback.deadline", null);
		feedbackDeadlineContainer.setMandatory(true);
		formLayout.add(feedbackDeadlineContainer);
		
		String feedbackDay = "";
		String feedbackMonth= "0";
		String feedbackYear = "";
		Date feedbackDeadline = feedbacksContext.getDeadline();
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
	}
	
	private String toHtml(String text) {
		if(StringHelper.isHtml(text)) {
			return text;
		}
		StringBuilder sb = Formatter.stripTabsAndReturns(text);
		return sb == null ? "" : sb.toString();
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

		allOk &= checkSubjectSize(subjectEl, subjectEl.getMaxLength());
		allOk &= checkElement(bodyEl);
		
		feedbackDeadlineYearElement.clearError();
		if(getFeedbackDeadline() == null) {
			feedbackDeadlineYearElement.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else {
			allOk &= validateYearElement(feedbackDeadlineYearElement);
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
	
	private boolean checkSubjectSize(TextElement element, int size) {
		boolean ok = true;
		
		String value = element.getValue();
		if(StringHelper.containsNonWhitespace(value) && value.length() >= size ) {
			element.setErrorKey("error.subject.max.length", new String[] { Integer.toString(size) });
			ok &= false;
		}
		
		return ok;
	}
	
	private boolean checkElement(TextElement element) {
		boolean ok = true;
		String value = element.getValue();
		if(!StringHelper.containsNonWhitespace(value)) {
			element.setErrorKey("form.legende.mandatory");
			ok &= false;
		} else if(!checkTemplate(element)) {
			ok &= false;
		}
		return ok;
	}
	
	private boolean checkTemplate(TextElement element) {
		List<Application> mailApps = feedbacksContext.getApplications();
		if(mailApps == null || mailApps.isEmpty()) {
			return true;
		}
		
		Position position = feedbacksContext.getPosition();
		Application mailApp = mailApps.size() == 1 ? mailApps.get(0) : null;
		ApplicationMailTemplate template = feedbacksContext.getMailTemplate();
		ApplicationsFeedbackConfiguration feedbackConfig = feedbacksContext.getConfiguration();
		ApplicationFeedback dummyFeedback = FeedbackHelper.generateDummyFeedback(feedbackConfig, null);
		List<ApplicationFeedback> dummyFeedbacks = Collections.singletonList(dummyFeedback);
		Identity member = FeedbackHelper.generateDummyMember();

		VariablesValidationContext context = new VariablesValidationContext();
		template.putVariablesInMailContext(context, mailApp, mailApps, null, member, dummyFeedbacks, feedbackConfig, position);
		
		MailerSender sender = erFrontendManager.createMailSender();
		boolean allOk = sender.checkTemplate(context, element.getValue());
		if(!context.getUnkownVariables().isEmpty()) {
			String i18nKey = context.getUnkownVariables().size() == 1 ? "error.template.unkown.variable" : "error.template.unkown.variables";
			element.setErrorKey(i18nKey, new String[] { context.stringuifiedUnkownVariables() });
			allOk &= false;
		}
		return allOk;
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
		ApplicationMailTemplate template = feedbacksContext.getMailTemplate();
		template.setSubjectTemplate(subjectEl.getValue());
		template.setBodyTemplate(bodyEl.getValue());

		Date deadline = getFeedbackDeadline();
		feedbacksContext.setDeadline(deadline);
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	private void doOpenVariables(UserRequest ureq) {
		ControllerCreator ctrlCreator = (lureq, lwControl) -> {
			List<Application> mailApps = feedbacksContext.getApplications();
			boolean app = mailApps.size() == 1;
			boolean appList = !mailApps.isEmpty();
			Controller mailVariablesCtrl = new MailVariablesController(lureq, lwControl,
					feedbacksContext.getPosition(), app, appList, false, null, feedbacksContext.getConfiguration(),
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