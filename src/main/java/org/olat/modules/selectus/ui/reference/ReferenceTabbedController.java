/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.reference;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.ExternalLink;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.DocumentEnum;
import org.olat.modules.selectus.DocumentOption;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.manager.ApplicationMailTemplate;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Attachment;
import org.olat.modules.selectus.model.OrganisationUnit;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceRequestStatus;
import org.olat.modules.selectus.model.ReferenceType;
import org.olat.modules.selectus.model.SubjectAndBody;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.RecruitingMailTemplate;
import org.olat.modules.selectus.ui.components.DateCellRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * 
 * Initial date: 16.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReferenceTabbedController extends BasicController {

	private final VelocityContainer mainVC;
	private Link acceptButton;
	private Link declineButton;
	private final Link submitLink;
	private final Link positionDetailsLink;
	private final Link applicationDetailsLink;
	
	private Controller applicationCtrl;
	private CloseableModalController cmc;
	private ReferenceSubmitController submitCtrl;
	private ReferencePositionController positionCtrl;
	private ConfirmDeclineConsentController confirmDeclineCtrl;
	
	private Reference reference;
	private final Position position;
	private final Application application;
	private final List<Application> applicationsList;
	private final OrganisationUnit organisationSettings;
	private final RecruitingPositionSecurityCallback secCallback;

	@Autowired
	private DB dbInstance;
	@Autowired
	private I18nManager i18nManager;
	@Autowired
	private AuditService auditService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	@Autowired
	private RecruitingService recruitingService;
	
	public ReferenceTabbedController(UserRequest ureq, WindowControl wControl, Position position, Reference reference,
			RecruitingPositionSecurityCallback secCallback) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		
		this.reference = reference;
		this.position = position;
		application = recruitingService.getApplication(reference.getApplication());
		applicationsList = recruitingService.getReferenceToApplicationsList(reference);
		organisationSettings = recruitingService.getOrganisationUnit(position);
		
		this.secCallback = secCallback;
		
		mainVC = createVelocityContainer("reference_reviewer_tabs");
		
		positionDetailsLink = LinkFactory.createLink("reference.position.details", mainVC, this);
		positionDetailsLink.setElementCssClass("o_sel_position_details");
		mainVC.put("positionDetails", positionDetailsLink);

		String appLinkName;
		boolean hasDocuments = hasDocuments();
		if(applicationsList != null && applicationsList.size() > 1) {
			appLinkName = hasDocuments ? "reference.application.and.docs.details.plural" : "reference.application.details.plural";
		} else {
			appLinkName = hasDocuments ? "reference.application.and.docs.details" : "reference.application.details";
		}

		applicationDetailsLink = LinkFactory.createLink("reference.application.details", appLinkName, mainVC, this);
		mainVC.put("applicationDetailsLink", applicationDetailsLink);

		applicationDetailsLink.setElementCssClass("o_sel_application_details");
		mainVC.put("applicationDetails", applicationDetailsLink);

		String submitI18nKey = "reference.submit.recommendation";
		if(reference.getReferenceType() == ReferenceType.expert) {
			submitI18nKey = "reference.submit.expert";
		} else if(reference.getReferenceType() == ReferenceType.comparativeAssessmentExpert) {
			submitI18nKey = "reference.submit.comparative.expert";
		}
		
		submitLink = LinkFactory.createLink("reference.submit", submitI18nKey, mainVC, this);
		submitLink.setElementCssClass("o_sel_submit_reference");
		mainVC.put("reference.submit", submitLink);
		String[] referencesFullNames = new String[]  {
				salutationGenerator.getFullname(reference, getLocale()),			// 0
				salutationGenerator.getTitleFullname(reference, getLocale()),		// 1
				salutationGenerator.getSalutation(reference, getLocale()),			// 2
				salutationGenerator.getTitleLastName(reference, getLocale()),		// 3
				salutationGenerator.getSalutation(reference, getLocale()),			// 4
				salutationGenerator.getTitleFirstLastName(reference, getLocale())	// 5
		};
		
		mainVC.contextPut("fullNames", referencesFullNames);
		String reviewTitle = translate("review.title", referencesFullNames);
		mainVC.contextPut("reviewTitle", reviewTitle);
		mainVC.contextPut("currentStep", "reference.position.details");

		String[] applicantArgs = buildApplicantArgs();
		initMessagesAndConsent(applicantArgs);
		initCountdown(applicantArgs);
		initTemplates();
		putInitialPanel(mainVC);
		doPosition(ureq);
	}
	
	private boolean hasDocuments() {
		Set<String> visibleDocs;
		if(reference.getReferenceType() == ReferenceType.expert) {
			visibleDocs = position.getExpertRecommendationDocuments();
		} else {
			visibleDocs = position.getRefereeRecommendationDocuments();
		}
		Set<String> available = position.getAvailableDocuments();
		Set<String> mandatory = position.getMandatoryDocuments();
		for(DocumentOption docOption:recruitingModule.getDocumentOptions()) {
			DocumentEnum doc = docOption.getDoc();
			if((visibleDocs.contains("all") || visibleDocs.contains(doc.name()))
					&& (available.contains(doc.name()) || mandatory.contains(doc.name()))) {
				if(application != null) {
					Attachment attachment = doc.path(application);
					if(attachment != null) {
						return true;
					}
				} else {
					for(Application app:applicationsList) {
						Attachment attachment = doc.path(app);
						if(attachment != null) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	private void initMessagesAndConsent(String[] applicantArgs) {
		String positionMsgI18nKey = "review.description.recommendation";
		if(reference.getReferenceType() == ReferenceType.expert) {
			positionMsgI18nKey = "review.description.expert";
		} else if(reference.getReferenceType() == ReferenceType.comparativeAssessmentExpert) {
			positionMsgI18nKey = "review.description.comparative.expert";
		}
		
		if(recruitingModule.isReferenceRefereeConsentEnabled()) {
			positionMsgI18nKey = "review.description.reference.consent";
			if(reference.getReferenceType() == ReferenceType.expert) {
				positionMsgI18nKey = "review.description.expert.consent";
			} else if(reference.getReferenceType() == ReferenceType.comparativeAssessmentExpert) {
				positionMsgI18nKey = "review.description.comparative.expert.consent";
			}
			initConsent(applicantArgs);
		}
		
		String positionMsg = translate(positionMsgI18nKey, applicantArgs);
		mainVC.contextPut("positionMsg", positionMsg);
	}
	
	private String[] buildApplicantArgs() {
		String applicantFullName = StringHelper.escapeHtml(salutationGenerator.getTitleFullname(application, applicationsList, getLocale()));
		String applicantLastName = StringHelper.escapeHtml(salutationGenerator.getTitleLastName(application, applicationsList, getLocale()));
		String applicationTitleFirstLastName = StringHelper.escapeHtml(salutationGenerator.getTitleFirstLastName(application, applicationsList, getLocale()));
		
		String applicationsHtmlFullName = listHtmlDecorate(getApplicationsListFullName());
		String applicationsHtmlLastName = listHtmlDecorate(getApplicationsListLastName());
		String applicationsHtmlTitleFirstLastName = listHtmlDecorate(getApplicationsListTitleFirstLastName());
		
		Identity headOfCommittee = recruitingService.getHeadOfCommittee(position);
		String headEmail = headOfCommittee == null ? "" : headOfCommittee.getUser().getProperty(UserConstants.EMAIL, getLocale());
		if(headEmail == null) {
			headEmail = "";
		}
		String consentDate = Formatter.getInstance(getLocale()).formatDateLong(reference.getDateConsent());
		String positionMail = recruitingModule.getStaffMail(position, organisationSettings);

		return new String[] {
			applicantFullName,						// 0
			position.getMLTitle(getLocale()),		// 1
			applicantLastName,						// 2
			headEmail,								// 3
			applicationTitleFirstLastName,			// 4
			consentDate,							// 5
			positionMail,							// 6
			applicationsHtmlFullName,				// 7
			applicationsHtmlLastName,				// 8
			applicationsHtmlTitleFirstLastName		// 9
		};
	}
	
	private List<String> getApplicationsListFullName() {
		if(applicationsList == null) return Collections.emptyList();
		return applicationsList.stream()
				.map(app -> salutationGenerator.getTitleFullname(app, getLocale()))
				.collect(Collectors.toList());
	}
	
	private List<String> getApplicationsListLastName() {
		if(applicationsList == null) return Collections.emptyList();
		return applicationsList.stream()
				.map(app -> salutationGenerator.getTitleLastName(app, getLocale()))
				.collect(Collectors.toList());
	}
	
	private List<String> getApplicationsListTitleFirstLastName() {
		if(applicationsList == null) return Collections.emptyList();
		return applicationsList.stream()
				.map(app -> salutationGenerator.getTitleFirstLastName(app, getLocale()))
				.collect(Collectors.toList());
	}
	
	private String listHtmlDecorate(List<String> values) {
		StringBuilder sb = new StringBuilder();
		if(values != null) {
			sb.append("<ul>");
			for(String value:values) {
				sb.append("<li>")
				  .append(StringHelper.escapeHtml(value))
				  .append("</li>");
			}
			sb.append("</ul>");
		}
		return sb.toString();
	}
	
	private void initConsent(String[] applicantArgs) {
		String consentMsgI18nKey = null;
		ReferenceRequestStatus requestStatus = reference.getRequestStatus();
		if(requestStatus == ReferenceRequestStatus.notAnswered) {
			consentMsgI18nKey = "review.description.reference.notAnswered";
			if(reference.getReferenceType() == ReferenceType.expert) {
				consentMsgI18nKey = "review.description.expert.notAnswered";
			} else if(reference.getReferenceType() == ReferenceType.comparativeAssessmentExpert) {
				consentMsgI18nKey = "review.description.comparative.expert.notAnswered";
			} else if(reference.getReferenceType() == ReferenceType.recommendation
					&& recruitingModule.isReferenceApplicantManagement() && position.isApplicantRefereeManagementEnabled()) {
				consentMsgI18nKey = "review.description.reference.with.mgmt.notAnswered";
			}
			
			acceptButton = LinkFactory.createLink("reference.accept.button", "reference.accept.button", getTranslator(), mainVC, this, Link.BUTTON);
			declineButton = LinkFactory.createLink("reference.decline.button", "reference.decline.button", getTranslator(), mainVC, this, Link.BUTTON);
		} else if(requestStatus == ReferenceRequestStatus.accepted) {
			consentMsgI18nKey = "review.description.reference.accepted";
			if(reference.getReferenceType() == ReferenceType.expert) {
				consentMsgI18nKey = "review.description.expert.accepted";
			} else if(reference.getReferenceType() == ReferenceType.comparativeAssessmentExpert) {
				consentMsgI18nKey = "review.description.comparative.expert.accepted";
			}
			
			if(acceptButton != null) {
				acceptButton.setVisible(false);
				declineButton.setVisible(false);
			}
		} else if(requestStatus == ReferenceRequestStatus.declined) {
			consentMsgI18nKey = "review.description.reference.declined";
			if(reference.getReferenceType() == ReferenceType.expert) {
				consentMsgI18nKey = "review.description.expert.declined";
			} else if(reference.getReferenceType() == ReferenceType.comparativeAssessmentExpert) {
				consentMsgI18nKey = "review.description.comparative.comparative.expert.declined";
			}
			
			if(acceptButton != null) {
				acceptButton.setVisible(false);
				declineButton.setVisible(false);
			}
		}
		
		if(consentMsgI18nKey != null) {
			String consentMsg = translate(consentMsgI18nKey, applicantArgs);
			mainVC.contextPut("consentMsg", consentMsg);
		}
	}
	
	private void initCountdown(String[] applicantArgs) {
		Date submissionDeadline = position.getRefereeRecommandationDeadline();
		String submissionMsgI18nKey = "review.submissiondate.recommendation.explain";
		if(reference.getReferenceType() == ReferenceType.expert) {
			submissionMsgI18nKey = "review.submissiondate.expert.explain";
			submissionDeadline = position.getExpertRecommandationDeadline();
		} else if(reference.getReferenceType() == ReferenceType.comparativeAssessmentExpert) {
			submissionMsgI18nKey = "review.submissiondate.comparative.expert.explain";
			submissionDeadline = position.getExpertRecommandationDeadline();
		}
		
		
		mainVC.contextPut("submissionDateMsg", translate(submissionMsgI18nKey, applicantArgs));
		if(reference.getSubmissionDeadline() != null) {
			submissionDeadline = reference.getSubmissionDeadline();
		}

		if(submissionDeadline != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(submissionDeadline);
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.SECOND, 59);
			cal.set(Calendar.MILLISECOND, 999);
			submissionDeadline = cal.getTime();

			String submissionDateStr = DateCellRenderer.format(submissionDeadline);
			mainVC.contextPut("submissionDate", submissionDateStr);
			
			String submissionDeadlineMsgI18nKey = "review.submissiondate.recommendation.deadline";
			if(reference.getReferenceType() == ReferenceType.expert) {
				submissionDeadlineMsgI18nKey = "review.submissiondate.expert.deadline";
			} else if(reference.getReferenceType() == ReferenceType.comparativeAssessmentExpert) {
				submissionDeadlineMsgI18nKey = "review.submissiondate.comparative.expert.deadline";
			}
	
			String countdown;
			long countDays = -1;
			Date now = new Date();

			if(now.after(submissionDeadline)) {
				countdown = "";
			} else {
				double daysConst = 24d * 60d * 60d * 1000d;
				long ratingDeadlineTime = submissionDeadline.getTime();
				countDays = Math.round((ratingDeadlineTime - now.getTime()) / daysConst);
				if(countDays > 1) {
					countdown = translate("review.countdown.day", new String[]{  Long.toString(countDays) });
				} else {
					double hoursConst = 60d * 60d * 1000d;
					long countHours = Math.round((ratingDeadlineTime - now.getTime()) / hoursConst);
					if(countHours > 1) {
						countdown = translate("review.countdown.hour", new String[]{  Long.toString(countHours) });
						
					} else {
						double minutesConst = 60d * 1000d;
						long countMinutes = Math.round((ratingDeadlineTime - now.getTime()) / minutesConst);
						countdown = translate("review.countdown.minute", new String[]{ Long.toString(countMinutes) });
					}
				}
			}
			
			String countDaysStr = countDays >= 0 ? Long.toString(countDays) : "";
			String[] submissionDeadlineArgs = new String[]{ submissionDateStr, countDaysStr };
			String submissionDeadlineMsg = translate(submissionDeadlineMsgI18nKey, submissionDeadlineArgs);
			mainVC.contextPut("submissionDeadlineMsg", submissionDeadlineMsg);
			mainVC.contextPut("countdown", countdown);
		}
	}
	
	private void initTemplates() {
		if(StringHelper.containsNonWhitespace(recruitingModule.getReferenceTemplate0())) {
			initTemplate("reference.template.0", recruitingModule.getReferenceTemplate0());
		}
		if(StringHelper.containsNonWhitespace(recruitingModule.getReferenceTemplate1())) {
			initTemplate("reference.template.1", recruitingModule.getReferenceTemplate1());
		}
		mainVC.contextPut("templateCssClass", "o_reference_template_" + reference.getReferenceType());
	}
	
	private void initTemplate(String name, String templateUrl) {
		ExternalLink helpLink = new ExternalLink(name);
		mainVC.put(name, helpLink);
		helpLink.setName(translate(name));
		helpLink.setTarget("_blank");
		helpLink.setUrl(Settings.getServerContextPathURI() + templateUrl);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(submitCtrl == source) {
			if(event == Event.DONE_EVENT) {
				notifyAfterSubmission(ureq);
			}
			fireEvent(ureq, event);
		} else if(positionCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doApplication(ureq);
				mainVC.contextPut("currentStep", "reference.application.details");
			}
		} else if(applicationCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doSubmit(ureq);
				mainVC.contextPut("currentStep", "reference.submit");
			}
		} else if(confirmDeclineCtrl == source) {
			String comment = confirmDeclineCtrl.getComment();
			cmc.deactivate();
			cleanUp();
			if(event == Event.DONE_EVENT) {
				doConsent(ureq, ReferenceRequestStatus.declined, comment);
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}	
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmDeclineCtrl);
		removeAsListenerAndDispose(cmc);
		confirmDeclineCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == positionDetailsLink) {
			doPosition(ureq);
			mainVC.contextPut("currentStep", "reference.position.details");
		} else if(source == applicationDetailsLink) {
			doApplication(ureq);
			mainVC.contextPut("currentStep", "reference.application.details");
		} else if(source == submitLink) {
			doSubmit(ureq);
			mainVC.contextPut("currentStep", "reference.submit");
		} else if(source == acceptButton) {
			doConsent(ureq, ReferenceRequestStatus.accepted, null);
		} else if(source == declineButton) {
			doConfirmDecline(ureq);
		}
	}
	
	private void doPosition(UserRequest ureq) {
		if(positionCtrl == null) {
			positionCtrl = new ReferencePositionController(ureq, getWindowControl(), position, application, applicationsList, secCallback);
			listenTo(positionCtrl);
		}
		mainVC.put("content", positionCtrl.getInitialComponent());
	}
	
	private void doApplication(UserRequest ureq) {	
		if(applicationCtrl == null) {
			if(reference.getReferenceType() == ReferenceType.comparativeAssessmentExpert) {
				if(applicationsList.size() == 1) {
					Application app = applicationsList.get(0);
					applicationCtrl = new ReferenceApplicationController(ureq, getWindowControl(), position, app, secCallback, false);
					listenTo(applicationCtrl);
				} else {
					applicationCtrl = new ReferenceApplicationsListWrapperController(ureq, getWindowControl(), position, applicationsList, secCallback);
					listenTo(applicationCtrl);
				}
			} else {
				applicationCtrl = new ReferenceApplicationController(ureq, getWindowControl(), position, application, secCallback, false);
				listenTo(applicationCtrl);
			}
		} else if(applicationCtrl instanceof Activateable2) {
			((Activateable2)applicationCtrl).activate(ureq, Collections.emptyList(), null);
		}
		mainVC.put("content", applicationCtrl.getInitialComponent());
	}
	
	private void doSubmit(UserRequest ureq) {
		if(submitCtrl == null) {
			List<Application> appsList = recruitingService.getReferenceToApplicationsList(reference);
			submitCtrl = new ReferenceSubmitController(ureq, getWindowControl(), position, application, appsList, reference);
			listenTo(submitCtrl);
		}
		mainVC.put("content", submitCtrl.getInitialComponent());
	}
	
	private void doConfirmDecline(UserRequest ureq) {
		confirmDeclineCtrl = new ConfirmDeclineConsentController(ureq, getWindowControl(), reference);
		listenTo(confirmDeclineCtrl);
		
		String title = translate("reference.confirm.decline.title");
		cmc = new CloseableModalController(getWindowControl(), "c", confirmDeclineCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConsent(UserRequest ureq, ReferenceRequestStatus status, String comment) {
		reference = recruitingService.getReferenceById(reference.getKey());
		reference.setRequestStatus(status);
		reference.setDateConsent(new Date());
		reference.setConsentByStaff(Boolean.FALSE);
		reference = recruitingService.updateReference(reference);
		dbInstance.commit();
		
		String[] logArgs = new String[] {
				salutationGenerator.getTitleFullname(reference, getLocale()),
				salutationGenerator.getTitleFullname(application, applicationsList, getLocale()),
				RecruitingHelper.formatIDs(application, applicationsList)
			};

		logConsent(status, logArgs);
		if(StringHelper.containsNonWhitespace(comment)) {
			recruitingService.addReferenceComment(reference, comment);
			logConsentComment(logArgs);
		}

		String[] applicantArgs = buildApplicantArgs();
		initConsent(applicantArgs);
		
		if(application != null && reference.getReferenceType() == ReferenceType.recommendation) {
			notifyApplicant(ureq, application);
		}
	}
	
	private void logConsent(ReferenceRequestStatus status, String[] logArgs) {
		boolean accept = (status == ReferenceRequestStatus.accepted);
		Action action = accept ? Action.accepted : Action.declined;
		
		ActionTarget target = null;
		String messageI18n = null;
		if(reference.getReferenceType() == ReferenceType.expert) {
			target = ActionTarget.expert;
			messageI18n = accept ? "audit.log.expert.accept.consent" : "audit.log.expert.decline.consent";
		} else if(reference.getReferenceType() == ReferenceType.recommendation) {
			target = ActionTarget.referee;
			messageI18n = accept ? "audit.log.referee.accept.consent" : "audit.log.referee.decline.consent";
		} else if(reference.getReferenceType() == ReferenceType.comparativeAssessmentExpert) {
			target = ActionTarget.comparativeExpert;
			messageI18n = accept ? "audit.log.comparative.expert.accept.consent" : "audit.log.comparative.expert.decline.consent";
		}
		
		auditService.auditRefereeLog(action, target, null, null, messageI18n, logArgs, getTranslator(), position, application, reference, null);
	}
	
	private void logConsentComment(String[] logArgs) {
		Action action = Action.comment;
		
		ActionTarget target = null;
		String messageI18n = null;
		if(reference.getReferenceType() == ReferenceType.expert) {
			target = ActionTarget.expert;
			messageI18n = "audit.log.expert.consent.comment";
		} else if(reference.getReferenceType() == ReferenceType.recommendation) {
			target = ActionTarget.referee;
			messageI18n = "audit.log.referee.consent.comment";
		} else if(reference.getReferenceType() == ReferenceType.comparativeAssessmentExpert) {
			target = ActionTarget.comparativeExpert;
			messageI18n = "audit.log.comparative.expert.consent.comment";
		}
		
		auditService.auditRefereeLog(action, target, null, null, messageI18n, logArgs, getTranslator(), position, application, reference, null);
	}
	
	private String[] getMessageArgs(Locale locale) {
		return new String[] {
				salutationGenerator.getTitleFullname(reference, locale),
				salutationGenerator.getTitleFullname(application, applicationsList, locale),
				RecruitingHelper.formatIDs(application, applicationsList),
				position.getMLTitle(locale)
			};
	}
	
	private void notifyAfterSubmission(UserRequest ureq) {
		if(application != null && reference.getReferenceType() == ReferenceType.recommendation) {
			notifyApplicant(ureq, application);
		}
		notifyReferee();
	}
	
	private void notifyReferee() {
		// Use default language
		Locale locale = i18nManager.getLocaleOrDefault(null);
		locale = recruitingModule.getPositionLocale(locale.getLanguage());
		
		final Identity headOfCommittee = recruitingService.getHeadOfCommittee(position);
		final Identity secretary = recruitingService.getSecretary(position);
		Translator appTranslator = Util.createPackageTranslator(PositionController.class, locale);

		String body;
		String subject;
		String[] messageArgs = getMessageArgs(locale);
		if(reference.getReferenceType() == ReferenceType.recommendation) {
			subject = position.getRefereeConfirmationSubmissionMailSubject();
			body = position.getRefereeConfirmationSubmissionMailTemplate();
		} else if(reference.getReferenceType() == ReferenceType.expert) {
			subject = position.getExpertConfirmationSubmissionMailSubject();
			body = position.getExpertConfirmationSubmissionMailTemplate();
		} else if(reference.getReferenceType() == ReferenceType.comparativeAssessmentExpert) {
			subject = position.getComparativeAssessmentExpertConfirmationSubmissionMailSubject();
			body = position.getComparativeAssessmentExpertConfirmationSubmissionMailTemplate();
		} else {
			return;
		}
		
		if(!RecruitingHelper.containsTemplate(subject)) {
			if(reference.getReferenceType() == ReferenceType.recommendation) {
				subject = appTranslator.translate("referee.submission.notification.subject", messageArgs);
			} else if(reference.getReferenceType() == ReferenceType.expert) {
				subject = appTranslator.translate("expert.submission.notification.subject", messageArgs);
			} else if(reference.getReferenceType() == ReferenceType.comparativeAssessmentExpert) {
				subject = appTranslator.translate("comparative.expert.submission.notification.subject", messageArgs);
			}
		}
		
		if(!RecruitingHelper.containsTemplate(body)) {
			if(reference.getReferenceType() == ReferenceType.recommendation) {
				body = appTranslator.translate("referee.submission.notification.body", messageArgs);
			} else if(reference.getReferenceType() == ReferenceType.expert) {
				body = appTranslator.translate("expert.submission.notification.body", messageArgs);
			} else if(reference.getReferenceType() == ReferenceType.comparativeAssessmentExpert) {
				body = appTranslator.translate("comparative.expert.submission.notification.body", messageArgs);
			}
		}

		ApplicationMailTemplate template = new RecruitingMailTemplate(null, null, null, subject, body, null,
				headOfCommittee, secretary, new SubjectAndBody(subject, body, null),
				salutationGenerator, appTranslator);
		recruitingService.sendToReference(reference, application, applicationsList, position, template);
	}
	
	private void notifyApplicant(UserRequest ureq, Application app) {
		if(!RecruitingHelper.isSendRefereeNotificationToApplicant(ureq, app, position)) {
			return;
		}
		
		Locale locale = i18nManager.getLocaleOrDefault(app.getLanguage());
		locale = recruitingModule.getPositionLocale(locale.getLanguage());
		
		String[] messageArgs = getMessageArgs(locale);
		final Identity headOfCommittee = recruitingService.getHeadOfCommittee(position);
		final Identity secretary = recruitingService.getSecretary(position);
		Translator appTranslator = Util.createPackageTranslator(PositionController.class, locale);
		
		String subject = appTranslator.translate("refereedashboard.notification.subject", messageArgs);
		String body = appTranslator.translate("refereedashboard.notification.body", messageArgs);
		ApplicationMailTemplate template = new RecruitingMailTemplate(null, null, null, subject, body, null,
				headOfCommittee, secretary, new SubjectAndBody(subject, body, null),
				salutationGenerator, appTranslator);
		recruitingService.sendToApplicant(app, position, template, false);
	}
}
