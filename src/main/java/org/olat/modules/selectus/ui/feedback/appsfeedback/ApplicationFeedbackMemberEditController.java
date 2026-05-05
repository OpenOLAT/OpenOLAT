/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.feedback.appsfeedback;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.Preferences;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.resource.OresHelper;
import org.olat.registration.RegistrationManager;
import org.olat.registration.RegistrationModule;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.FeedbackService;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.RecruitingMainController;
import org.olat.modules.selectus.ui.committee.wizard.MembersController;

/**
 * 
 * Initial date: 24 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationFeedbackMemberEditController extends FormBasicController {

	private static final String formIdentifyer = MembersController.formIdentifyer;
	private static final String LOGINNAME = "loginname";
	
	private static final String[] monthKeys = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"};
	private String[] monthValues = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};

	private Map<String, String> formContext;
	private List<UserPropertyHandler> userPropertyHandlers;

	private TextElement emailTextElement;
	private TextElement feedbackDeadlineDayElement;
	private SingleSelection feedbackDeadlineMonthElement;
	private TextElement feedbackDeadlineYearElement;
	private FormLayoutContainer feedbackDeadlineContainer;
	private FormLink sendPasswordLink;

	private final Identity member;
	private final Position position;
	private ApplicationFeedback feedback;
	private final Application application;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager um;
	@Autowired
	private I18nModule i18nModule;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private AuditService auditService;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private FeedbackService feedbackService;
	@Autowired
	private RecruitingService erFrontendManager;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	@Autowired
	private RegistrationModule registrationModule;
	@Autowired
	private RegistrationManager registrationManager;
	
	public ApplicationFeedbackMemberEditController(UserRequest ureq, WindowControl wControl,
			Identity member, ApplicationFeedback feedback, Application application, Position position) {
		super(ureq, wControl, null, UserManager.getInstance().getPropertyHandlerTranslator(Util
				.createPackageTranslator(PositionController.class, ureq.getLocale())));
		this.member = member;
		this.position = position;
		this.feedback = feedback;
		this.application = application;
		formContext = new HashMap<>();
		for(int i=monthKeys.length; i-->0; ) {
			monthValues[i] = translate("month.long." + i);
		}

		initForm(ureq);
	}
	
	public Identity getMember() {
		return member;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("apps.feedback.edit.member.description");

		String username = member.getName();
		uifactory.addStaticTextElement(LOGINNAME, "username", username, formLayout);
		formContext.put("username", username);
		
		User user = member.getUser();

		sendPasswordLink = uifactory.addFormLink("tmp.password", "edit.committee.password.tmp", "edit.committee.password", formLayout, Link.LINK);
		sendPasswordLink.setHelpText(translate("edit.committee.password.hint"));
		sendPasswordLink.setIconLeftCSS("o_icon o_icon_external_link");

		userPropertyHandlers = um.getUserPropertyHandlersFor(formIdentifyer, true);
		// Add all available user fields to this form
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;
			userPropertyHandler.addFormItem(ureq.getLocale(), user, formIdentifyer, true, formLayout);
		}
		
		if(feedback != null) {
			initDeadline(formLayout);
		}
		
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
	
	private void initDeadline(FormItemContainer formLayout) {
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
		Date feedbackDeadline = feedback.getDeadline();
		if(feedbackDeadline == null) {
			feedbackDeadline = feedback.getConfiguration().getDeadline();
		}
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

		// validate special rules for each user property
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {			
			//we assume here that there are only textElements for the user properties
			FormItem formItem = flc.getFormComponent(userPropertyHandler.getName());
			formItem.clearError();
			if ( ! userPropertyHandler.isValid(member.getUser(), formItem, formContext) || formItem.hasError()) {
				allOk &= false;				
			}
		}
		// special test on email address: validate if email is already used
		if (emailTextElement != null) {	
			emailTextElement.clearError();
			
			String email = emailTextElement.getValue();
			// Check if email is not already taken
			List<Identity> exists = um.findIdentitiesByEmail(List.of(email));
			if (exists.size() > 1 || (exists.size() == 1 && !exists.get(0).equals(member))) {
				// Oups, email already taken, display error
				emailTextElement.setErrorKey("new.error.email.choosen");
				allOk &= false;
			}
		}
		
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
					textEl.setErrorKey("deadline.error", new String[] { Integer.toString(currentYear)} );
				}
			} catch (NumberFormatException e) {
				ok =false;
				textEl.setErrorKey("deadline.error", new String[] { Integer.toString(currentYear)});
			}
		}
		return ok;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(sendPasswordLink == source) {
			doSendMailWithToken(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		final Identity memberToModify = getMember();
		erFrontendManager.createOLATResource(position);
		
		saveFeedback(memberToModify);
		saveIdentity(memberToModify);

		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void saveFeedback(Identity memberToModify) {
		Date deadline = getFeedbackDeadline();
		if(feedback.getDeadline() == null || !DateUtils.isSameDay(deadline, feedback.getDeadline())) {
			Action action = Action.update;
			String before = auditService.toAuditXml(feedback);
			String messageI18n = "audit.log.public.feedback.update";

			feedback.setDeadline(deadline);
			feedback = feedbackService.updateApplicationFeedback(feedback);
			dbInstance.commit();
			
			String after = auditService.toAuditXml(feedback);
			String[] args = new String[] {
				salutationGenerator.getTitleFullname(application, getLocale()),
				application.getId() == null ? "" : application.getId().toString(),
				RecruitingHelper.formatFullName(memberToModify)
			};
			
			auditService.auditFeedbackLog(action, before, after, messageI18n, args, getTranslator(), position, application, feedback, getIdentity());
		}	
	}
	
	private void saveIdentity(Identity memberToModify) {
		// update the user profile data
		CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(
			OresHelper.createOLATResourceableInstance(Identity.class, memberToModify.getKey()), () -> {
					Identity identityToModify = securityManager.loadIdentityByKey(memberToModify.getKey());
					String before = auditService.toAuditXml(identityToModify);
					
					User user = identityToModify.getUser();
					// Now add data from user fields (firstName,lastName and email are mandatory)
					for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
						FormItem propertyItem = flc.getFormComponent(userPropertyHandler.getName());
						userPropertyHandler.updateUserFromFormItem(user, propertyItem);
					}

					if (!um.updateUserFromIdentity(identityToModify)) {
						// reload user data from db
						logError("Cannot update committee member", null);
					}
					
					String after = auditService.toAuditXml(identityToModify);
					if(!before.equals(after)) {
						String messageI18n = "audit.log.member.feedback.update.member";
						String[] messageArgs = new String[] {
								member.getKey().toString(),
								RecruitingHelper.formatFullNameWithTitle(member, getLocale())
							};
						auditService.auditFeedbackMemberLog(Action.update, before, after,
								messageI18n, messageArgs, getTranslator(), position, null, null, getIdentity());
					}
					
					CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new MultiUserEvent("changed"),
						OresHelper.createOLATResourceableInstance(Identity.class, identityToModify.getKey()));
			});
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doSendMailWithToken(UserRequest ureq) {
		Preferences prefs = member.getUser().getPreferences();
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(prefs.getLanguage());
		String emailAdress = member.getUser().getProperty(UserConstants.EMAIL, locale);
		if (StringHelper.containsNonWhitespace(emailAdress)) {
			String serverpath = Settings.getServerContextPathURI();
			Translator userTrans = Util.createPackageTranslator(RegistrationManager.class, locale);
			String toolName = Util.createPackageTranslator(RecruitingMainController.class, getLocale())
					.translate("topnav.home");
			
			String ip = ureq.getHttpReq().getRemoteAddr();
			registrationManager.createAndDeleteOldTemporaryKey(member.getKey(), emailAdress, ip,
					RegistrationManager.PW_CHANGE, registrationModule.getRESTValidityOfTemporaryKey());

			String subject = userTrans.translate("pwchange.subject", toolName);
			String body = userTrans.translate("pwchange.intro", member.getName(), toolName, serverpath)
					+ userTrans.translate("pwchange.body.send", serverpath, emailAdress, i18nModule.getLocaleKey(locale));
			sendToken(body, subject);
		} else {
			showWarning("");
		}
	}
	
	private void sendToken(String body, String subject) {
		MailBundle bundle = new MailBundle();
		bundle.setToId(member);
		bundle.setContent(subject, body);
		MailerResult result = mailManager.sendExternMessage(bundle, new MailerResult(), true);
		if(result.getReturnCode() == MailerResult.OK) {
			showInfo("email.sent");
		} else {
			showError("email.notsent");
		}
	}
}
