/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.chiefcontrollers.LanguageChangedEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.MailService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.manager.ApplicationMailTemplate;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.OrganisationUnit;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionMLHelper;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceRequestStatus;
import org.olat.modules.selectus.model.ReferenceSendMailType;
import org.olat.modules.selectus.model.ReferenceStatus;
import org.olat.modules.selectus.model.ReferenceType;
import org.olat.modules.selectus.model.SubjectAndBody;
import org.olat.modules.selectus.model.mail.MailAttachment;
import org.olat.modules.selectus.ui.app_wizard.AddApplicationInitialStep;
import org.olat.modules.selectus.ui.app_wizard.HiddenInstructionsStep;
import org.olat.modules.selectus.ui.app_wizard.Referee;
import org.olat.modules.selectus.ui.app_wizard.RefereeList;
import org.olat.modules.selectus.ui.app_wizard.WizardConstants;
import org.olat.modules.selectus.ui.components.DateCellRenderer;
import org.olat.modules.selectus.ui.events.SelectPositionEvent;
import org.olat.modules.selectus.ui.reference.ReferenceHelper;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  12 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ApplyToApplicationMainController extends BasicController implements Activateable2 {

	private static final String MAIN_VC_PAGE = Util.getPackageVelocityRoot(ApplyToApplicationMainController.class) + "/apply_main.html";
	
	private final VelocityContainer layoutMainVC;
	private ApplyStep start;
	private StepsMainRunController addApplicationWizard;
	private ApplicationAppliedController appliedController;
	private ClosedApplicationMessageController closeAppController;
	private ApplyToPositionListController positionListController;
	
	private final List<Position> openPositions;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private MailService mailService;
	@Autowired
	private AuditService auditService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	@Autowired
	private RecruitingService recruitingService;
	
	public ApplyToApplicationMainController(UserRequest ureq, WindowControl wControl, List<Position> positions) {
		super(ureq, wControl);
		this.openPositions = positions;
		layoutMainVC = new VelocityContainer("vc_main", MAIN_VC_PAGE, getTranslator(), this);
		setI18nArguments(null);
		reset(ureq);
		layoutMainVC.put("simpleContent", addApplicationWizard.getInitialComponent());
		putInitialPanel(layoutMainVC);
	}
	
	public ApplyToApplicationMainController(UserRequest ureq, WindowControl wControl, Position position) {
		super(ureq, wControl);
		this.openPositions = position == null ? List.of() : List.of(position);
		layoutMainVC = new VelocityContainer("vc_main", MAIN_VC_PAGE, getTranslator(), this);
		setI18nArguments(position);
		if(position == null) {
			layoutMainVC.contextPut("notFound", Boolean.TRUE);
		} else  {
			PositionStatus status = PositionStatus.valueOf(position.getStatus());
			if(status == PositionStatus.closed || status == PositionStatus.closedAndInScreening
					|| status == PositionStatus.closedAndNoRating) {
				layoutMainVC.contextPut("closed", Boolean.TRUE);
			} else if(status == PositionStatus.preparation || status == PositionStatus.reporting) {
				layoutMainVC.contextPut("notFound", Boolean.TRUE);
			}
			reset(ureq);
			layoutMainVC.put("simpleContent", addApplicationWizard.getInitialComponent());
		}
		putInitialPanel(layoutMainVC);
	}
	
	private void setI18nArguments(Position position) {
		OrganisationUnit organisationSettings = recruitingService.getOrganisationUnit(position);
		
		String[] i18nArguments = new String[] {
			recruitingModule.getOfficeMail(),
			recruitingModule.getStaffMail(position, organisationSettings)
		};
		layoutMainVC.contextPut("i18nArguments", i18nArguments);
		String listUrl = RecruitingHelper.getLinkToPositionList();
		layoutMainVC.contextPut("positionListUrl", listUrl);
	}
	
	public void reset(UserRequest ureq) {
		Application application = recruitingService.createTempApplication(null, false);
		update(ureq, application);
	}
	
	public void update(UserRequest ureq, Application application) {
		Position selectedPosition = null;
		if(start != null) {
			selectedPosition = start.getPreselectedPosition();
		}
		update(ureq, application, selectedPosition);
	}

	public void update(UserRequest ureq, Application application, Position selectedPosition) {
		//clean up
		removeAsListenerAndDispose(appliedController);
		removeAsListenerAndDispose(closeAppController);
		removeAsListenerAndDispose(addApplicationWizard);
		removeAsListenerAndDispose(positionListController);
		
		//new controllers
		if(selectedPosition == null) {
			start = new AddApplicationInitialStep(ureq, selectedPosition, application);
		} else {
			application.setPosition(selectedPosition);
			start = new HiddenInstructionsStep(ureq, selectedPosition, application);
		}
		WindowControl swControl = getWindowControl();
		if(selectedPosition != null) {
			swControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("Position", selectedPosition.getKey()), null);
		}
		addApplicationWizard = new StepsMainRunController(ureq, swControl, start, new FinishedCallback(),
				new CancelCallback(), translate("add_application"), "stepslayout_embedded", "o_sel", null);
		addApplicationWizard.setFinishTitle("submit");
		listenTo(addApplicationWizard);

		appliedController = new ApplicationAppliedController(ureq, getWindowControl());
		listenTo(appliedController);
		
		closeAppController = new ClosedApplicationMessageController(ureq, getWindowControl(), selectedPosition);
		listenTo(closeAppController);
		
		positionListController = new ApplyToPositionListController(ureq, getWindowControl(), openPositions);
		listenTo(positionListController);

		layoutMainVC.put("simpleContent", addApplicationWizard.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == addApplicationWizard) {
			if (event == Event.CANCELLED_EVENT) {
				showError("cancelled");
			} else if (event == Event.CHANGED_EVENT) {
				applied(ureq);
				removeAsListenerAndDispose(addApplicationWizard);
				addApplicationWizard = null;
				start = null;
			} else if (event == Event.DONE_EVENT){
				showError("failed");
			} else if(event instanceof LanguageChangedEvent) {
				LanguageChangedEvent lce = (LanguageChangedEvent)event;
				changeLanguage(ureq, lce);
			} else if(event instanceof SelectPositionEvent) {
				SelectPositionEvent spe = (SelectPositionEvent)event;
				changePosition(ureq, spe.getPosition());
				pushState(ureq);
			}
		} else if(positionListController == source) {
			if(event instanceof SelectPositionEvent) {
				SelectPositionEvent spe = (SelectPositionEvent)event;
				layoutMainVC.remove(positionListController.getInitialComponent());
				changePosition(ureq, spe.getPosition());
				pushState(ureq);
			} else if(event instanceof LanguageChangedEvent) {
				LanguageChangedEvent lce = (LanguageChangedEvent)event;
				changeLanguage(ureq, lce);
				layoutMainVC.put("positionList", positionListController.getInitialComponent());
			}
		}
		super.event(ureq, source, event);
	}
	
	private void pushState(UserRequest ureq) {
		if(addApplicationWizard != null) {
			//TODO push state browser history addApplicationWizard.pushState(); addApplicationWizard.pushState();
			addToHistory(ureq, OresHelper.createOLATResourceableType("Step"), null, addApplicationWizard.getWindowControlForDebug(), true);
		}
	}
	
	private void applied(UserRequest ureq) {
		Application app = (Application)addApplicationWizard.getRunContext().get(WizardConstants.APPLICATION);
		appliedController.setApplication(app);
		layoutMainVC.put("simpleContent", appliedController.getInitialComponent());
		
		try {
			//TODO selectus ureq.getUserSession().getSessionInfo().getSession().setMaxInactiveInterval(UserSessionManager.QUICK_OUT_INTERVAL);
		} catch (Exception e) {
			logError("", e);
		}
	}
	
	private void changePosition(UserRequest ureq, Position position) {
		Application application = start == null ? null : start.getApplication();
		if(application == null || application.isValid()) {
			application = recruitingService.createTempApplication(null, false);
		}
		if(position != null) {
			List<Locale> locales = recruitingModule.getPositionLocales(position);
			if(!locales.isEmpty() && !locales.contains(ureq.getLocale())) {
				changeLanguage(ureq, locales.get(0));
			}
			update(ureq, application, position);
		} else {
			update(ureq, application);
		}
		setI18nArguments(position);
	}
	
	private void changeLanguage(UserRequest ureq, LanguageChangedEvent lce) {
		changeLanguage(ureq, lce.getNewLocale());
	}

	private void changeLanguage(UserRequest ureq, Locale locale) {
		setLocale(locale, true);
		ureq.getUserSession().setLocale(locale);
		
		Application application = start.getApplication();
		if(application == null) {
			application = recruitingService.createTempApplication(null, false);
		}
		
		update(ureq, application);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty() || !activateWizard(ureq, entries)) {
			activateUri(ureq);
		}
	}
	
	private boolean activateWizard(UserRequest ureq, List<ContextEntry> entries) {
		if(!entries.isEmpty()) {
			String positionType = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if("Position".equalsIgnoreCase(positionType)) {
				Long positionKey = entries.get(0).getOLATResourceable().getResourceableId();
				if(start != null && start.getPreselectedPosition() != null) {
					if(start.getPreselectedPosition().getKey().equals(positionKey)) {
						int step = 0;
						if(entries.size() > 1 && "Step".equalsIgnoreCase(entries.get(1).getOLATResourceable().getResourceableTypeName())) {
							step = entries.get(1).getOLATResourceable().getResourceableId().intValue();
						}
						//TODO selectus addApplicationWizard.selectStep(step, false);
						addApplicationWizard.next(ureq);
						return true;
					} else {
						Position position = getPosition(positionKey);
						if(position != null) {
							activatePosition(ureq, position);
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	private Position getPosition(Long positionKey) {
		if(openPositions == null || openPositions.isEmpty()) return null;
		
		for(Position openPosition:openPositions) {
			if(openPosition.getKey().equals(positionKey)) {
				return openPosition;
			}
		}
		return null;
	}
	
	private void activateUri(UserRequest ureq) {
		String requestUri = ureq.getHttpReq().getRequestURI();
		String uriPrefix = ureq.getUriPrefix();
		if("/positiondetails/".equals(uriPrefix)) {
			activatePositionList(ureq);
			return;
		}
		
		if(uriPrefix.length() < requestUri.length()) {
			requestUri = requestUri.substring(uriPrefix.length());
		}
		
		if(requestUri.startsWith("/")) {
			requestUri = requestUri.substring(1, requestUri.length());
		}
		
		int slashFromRestUrlIndex = requestUri.indexOf('/');
		if(slashFromRestUrlIndex > 0 && slashFromRestUrlIndex + 1 < requestUri.length()) {
			requestUri = requestUri.substring(slashFromRestUrlIndex + 1, requestUri.length());
		}
		
		if(StringHelper.isLong(requestUri)) {
			try {
				Long positionKey = Long.parseLong(requestUri);
				Position position = recruitingService.getPosition(positionKey);
				if(position == null || !position.isValid()) {
					activatePositionList(ureq);
				} else {
					activatePosition(ureq, position);
					if(addApplicationWizard != null) {
						//TODO push state browser history addApplicationWizard.pushState();
					}
				}
			} catch (NumberFormatException e) {
				logWarn("Cannot parse position key: " + requestUri, e);
				activatePositionList(ureq);
			}
		} else {
			activatePositionList(ureq);
		}
	}
	
	private void activatePositionList(UserRequest ureq) {
		if(positionListController != null) {
			layoutMainVC.put("positionList", positionListController.getInitialComponent());
			addToHistory(ureq, positionListController);
		}
	}
	
	/**
	 * The method if the applicant can see the position or not.
	 * 
	 * @param ureq The user request
	 * @param position The position
	 */
	private void activatePosition(UserRequest ureq, Position position) {
		PositionStatus status = PositionStatus.valueOf(position.getStatus());
		if(status == PositionStatus.closed
				|| status == PositionStatus.closedAndInScreening
				|| status == PositionStatus.closedAndNoRating) {
			closeAppController.setPosition(position);
			layoutMainVC.put("simpleContent", closeAppController.getInitialComponent());
		} else if(status == PositionStatus.published || status == PositionStatus.publishedAndInScreening){
			changePosition(ureq, position);
			
			String url = RecruitingHelper.getLinkToPosition(position);
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.HOUR_OF_DAY, 24);
			String expire = new SimpleDateFormat("EEE, dd-MMM-yyyy hh:mm:ss z", Locale.ENGLISH).format(cal.getTime());
			ureq.getHttpResp().addHeader("Set-Cookie", "REST_URL=" + url + "; Expires=" + expire + "; Path=/");
			
			List<Locale> locales = recruitingModule.getPositionLocales(position);
			if(!locales.isEmpty() && !locales.contains(getLocale())) {
				changeLanguage(ureq, new LanguageChangedEvent(locales.get(0), ureq));
			}
		} else {
			layoutMainVC.contextPut("notFound",Boolean.TRUE);
			layoutMainVC.contextPut("closed",Boolean.FALSE);
		}
	}
	
	private void logSendMail(Position position, Application application, Reference reference) {
		ActionTarget target = null;
		String messageI18n = "";
		if(reference.getReferenceType() == ReferenceType.expert) {
			target = ActionTarget.expert;
			messageI18n = "audit.log.expert.send.email";
		} else if(reference.getReferenceType() == ReferenceType.recommendation) {
			target = ActionTarget.referee;
			messageI18n = "audit.log.referee.send.email";
		}
		String[] messageArgs = new String[] {
			salutationGenerator.getTitleFullname(reference, getLocale()),
			salutationGenerator.getTitleFullname(application, getLocale()),
			application.getId().toString()
		};
		auditService.auditRefereeLog(Action.sendMail, target, null, null, messageI18n, messageArgs, getTranslator(), position, application, reference, null);
	}
	
	public static final String[] generateVariablesArguments() {
		return new String[] {
				"$titleAndName",												// 0 (titleFullName)
				"$" + RecruitingMailTemplate.POSITION_TITLE,					// 1
				"$" + RecruitingMailTemplate.ORG_UNIT_MAIL,						// 2
				"$" + RecruitingMailTemplate.APPLICATION_DEAR_TITLE_NAME,		// 3
				"$" + RecruitingMailTemplate.ORG_UNIT_SIGNATURE,				// 4
				"$refereeDeadline",												// 5
				"$headFullname",												// 6
				"$" + RecruitingMailTemplate.HEAD_FIRST_NAME,					// 7
				"$" + RecruitingMailTemplate.HEAD_LAST_NAME,					// 8
				"$refereeDeadlineDe",											// 9
				"$" + RecruitingMailTemplate.POSITION_APPLICATION_DEADLINE,		// 10
				"$" + RecruitingMailTemplate.POSITION_APPLICATION_DEADLINE_DE,	// 11
				"$" + RecruitingMailTemplate.HEAD_MAIL,							// 12
				"$secretaryFullname",											// 13
				"$" + RecruitingMailTemplate.SECRETARY_FIRST_NAME,				// 14
				"$" + RecruitingMailTemplate.SECRETARY_LAST_NAME,				// 15
				"$" + RecruitingMailTemplate.SECRETARY_MAIL,					// 16
				"$" + RecruitingMailTemplate.SECRETARY_TITLE,					// 17
				"$secretaryTitleFullname",										// 18
				"$headTitleFullname",											// 19
				"$" + RecruitingMailTemplate.POSITION_MAIL,						// 20
				"$" + RecruitingMailTemplate.REFEREE_MIN_REQUIRED,				// 21
			};
	}
	
	public static String getDefaultMailTemplate(Locale locale, boolean refereeManagement) {
		Translator translator = Util.createPackageTranslator(ApplyToApplicationMainController.class, locale);
		String[] args = generateVariablesArguments();
		if(refereeManagement) {
			return translator.translate("apply_application.email.referee.mgmt", args);
		}
		return translator.translate("apply_application.email", args);
	}
	
	public static String getDefaultMailTemplateDuplicate(Locale locale) {
		Translator translator = Util.createPackageTranslator(ApplyToApplicationMainController.class, locale);
		String[] args = generateVariablesArguments();
		return translator.translate("apply_application.email.duplicate", args);
	}
	
	protected class FinishedCallback implements StepRunnerCallback {
		@Override
		public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
			final Application app = (Application)runContext.get(WizardConstants.APPLICATION);
			final RefereeList referees = (RefereeList)runContext.get(WizardConstants.REVIEWERS);
			final Position position = app.getPosition();
			final OrganisationUnit organisationSettings = recruitingService.getOrganisationUnit(position);
			final String posTitle = position.getMLTitle(getLocale());
			final String name = salutationGenerator.getTitleFullname(app, getLocale());
			final String dearTitleAndName = salutationGenerator.getSalutation(app, getLocale());
			final String staffMail = recruitingModule.getStaffMail(position, organisationSettings);
			final Identity headOfCommittee = recruitingService.getHeadOfCommittee(position);
			final Identity secretary = recruitingService.getSecretary(position);
			final String mailSignature;
			if(position.getOrganisation() != null) {
				//TODO selectus load unit
				mailSignature = position.getOrganisation().toString();
			} else {
				mailSignature = getTranslator().translate("email.signature");
			}
			String refereeDeadlineStr = "-";
			String refereeDeadlineDeStr = "-";
			Date refereeDeadline = position.getRefereeRecommandationDeadline();
			if(refereeDeadline != null) {
				refereeDeadlineStr = DateCellRenderer.format(refereeDeadline);
				refereeDeadlineDeStr = DateCellRenderer.format(refereeDeadline, Locale.GERMAN);
			}
			String numOfRecommendations = "";
			if(position.getMinReferees() != null && position.getMinReferees().longValue() >= 0) {
				numOfRecommendations = position.getMinReferees().toString();
			}
			
			Date applicationDeadline = position.getApplicationDeadline();
			String applicationDeadlineStr = "-";
			String applicationDeadlineDeStr = "-";
			if(applicationDeadline != null) {
				applicationDeadlineStr = DateCellRenderer.format(applicationDeadline);
				applicationDeadlineDeStr = DateCellRenderer.format(applicationDeadline, Locale.GERMAN);
			}
			
			String headFullname = "";
			String headFirstName = "";
			String headLastName = "";
			String headEmail = "";
			String headTitleWithFullname = "";
			if(headOfCommittee != null) {
				headFullname = userManager.getUserDisplayName(headOfCommittee);
				headFirstName = headOfCommittee.getUser().getProperty(UserConstants.FIRSTNAME, getLocale());
				headLastName = headOfCommittee.getUser().getProperty(UserConstants.LASTNAME, getLocale());
				headEmail = headOfCommittee.getUser().getProperty(UserConstants.EMAIL, getLocale());
				headTitleWithFullname = RecruitingHelper.formatFullNameWithTitle(headOfCommittee, getLocale());
			}
			
			String secretaryFullname = "";
			String secretaryFirstName = "";
			String secretaryLastName = "";
			String secretaryEmail = "";
			String secretaryTitle = "";
			String secretaryTitleWithFullname = "";
			if(secretary != null) {
				secretaryFullname = userManager.getUserDisplayName(secretary);
				secretaryFirstName = secretary.getUser().getProperty(UserConstants.FIRSTNAME, getLocale());
				secretaryLastName = secretary.getUser().getProperty(UserConstants.LASTNAME, getLocale());
				secretaryEmail = secretary.getUser().getProperty(UserConstants.EMAIL, getLocale());
				secretaryTitle = secretary.getUser().getProperty(UserConstants.TITLE, getLocale());
				if(secretaryTitle == null || secretaryTitle.equals("-")) {
					secretaryTitle = "";
				}
				secretaryTitleWithFullname = RecruitingHelper.formatFullNameWithTitle(secretary, getLocale());
			}
			
			final String[] bodyArgs = new String[] {
					name,						// 0 (titleFullName)
					posTitle,					// 1
					staffMail,					// 2
					dearTitleAndName,			// 3
					mailSignature,				// 4
					refereeDeadlineStr,			// 5
					headFullname,				// 6
					headFirstName,				// 7
					headLastName,				// 8
					refereeDeadlineDeStr,		// 9
					applicationDeadlineStr,		// 10
					applicationDeadlineDeStr,	// 11
					headEmail,					// 12
					secretaryFullname,			// 13
					secretaryFirstName,			// 14
					secretaryLastName,			// 15
					secretaryEmail,				// 16
					secretaryTitle,				// 17
					secretaryTitleWithFullname,	// 18
					headTitleWithFullname,		// 19
					staffMail,					// 20 (alias of 2 for the templates)
					numOfRecommendations		// 21
				};

			if(recruitingModule.isApplicationDuplicateEmailsAllowed(position) || recruitingService.checkUniqueApplication(app)) {
				boolean referenceApplicantManagement = recruitingModule.isReferenceApplicantManagement() && position.isApplicantRefereeManagementEnabled();
				if(referenceApplicantManagement) {
					List<Identity> identities = userManager.findIdentitiesByEmail(Collections.singletonList(app.getPerson().getMail()));
					if(identities.size() == 1) {
						app.setIdentity(identities.get(0));
					}
				}
				final Application savedApp = recruitingService.saveTempApplication(app, true);
				if(referees != null) {
					saveReferences(referees, savedApp, headOfCommittee);
				}
				
				String subject = getTranslator().translate("apply_application.email.subject", posTitle);
				
				String body;
				MailAttachment letter;
				if(referenceApplicantManagement) {
					body = PositionMLHelper.getApplicationConfirmationWithRefereeManagementMailTemplate(position, getLocale());
					if(!StringHelper.containsNonWhitespace(body)) {
						body = getTranslator().translate("apply_application.email.referee.mgmt", bodyArgs);
					}
					letter = mailService.toAttachment(position.getApplicationConfirmationWithRefereeManagementMailLetter(), savedApp, getLocale());
				} else {
					body = PositionMLHelper.getApplicationConfirmationMailTemplate(position, getLocale());
					if(!StringHelper.containsNonWhitespace(body)) {
						body = getTranslator().translate("apply_application.email", bodyArgs);
					}
					letter = mailService.toAttachment(position.getApplicationConfirmationMailLetter(), savedApp, getLocale());
				}

				ApplicationMailTemplate template = new RecruitingMailTemplate(null, null, null, subject, body, letter,
						headOfCommittee, secretary, new SubjectAndBody(subject, body, letter),
						salutationGenerator, getTranslator());
				recruitingService.sendToApplicant(savedApp, position, template, true);
				
				String after = auditService.toAuditXml(savedApp);
				String messageI18n = "audit.log.application.submitted";
				String[] messageArgs = new String[] { salutationGenerator.getTitleFullname(savedApp, getLocale()), savedApp.getId().toString() };
				auditService.auditApplicationLog(Action.add, ActionTarget.application, null, after, messageI18n, messageArgs, getTranslator(), position, savedApp, null);
			} else {
				String subject = translate("apply_application.email.duplicate.subject", new String[]{ posTitle });
				String body = PositionMLHelper.getApplicationDuplicateConfirmationMailTemplate(position, getLocale());
				if(!StringHelper.containsNonWhitespace(body)) {
					body = getTranslator().translate("apply_application.email.duplicate", bodyArgs);
				}
				MailAttachment letter = mailService.toAttachment(position.getApplicationConfirmationDuplicateMailLetter(), app, getLocale());
				ApplicationMailTemplate template = new RecruitingMailTemplate(null, null, null, subject, body, letter,
						headOfCommittee, secretary, new SubjectAndBody(subject, body, letter),
						salutationGenerator, getTranslator());
				recruitingService.sendToApplicant(app, position, template, true);
			}

			return StepsMainRunController.DONE_MODIFIED;
		}
		
		private void saveReferences(RefereeList referees, Application savedApp, Identity headOfCommittee) {
			Position position = savedApp.getPosition();
			Identity secretary = recruitingService.getSecretary(position);
			ReferenceSendMailType sendType = position.getRefereeRecommandationSendMailType();
			for(Referee reviewer:referees.getReferees()) {
				if(reviewer.isComplete()) {
					Reference reference = recruitingService.addReference(reviewer.getTitle(), reviewer.getFirstName(), reviewer.getLastName(), reviewer.getInstitution(), reviewer.getEmail(),
							null, ReferenceType.recommendation, ReferenceRequestStatus.notAnswered, null, savedApp, null);
					if(sendType == ReferenceSendMailType.auto) {
						Translator translator = Util.createPackageTranslator(ApplyToApplicationMainController.class, Locale.ENGLISH);
						ApplicationMailTemplate template = ReferenceHelper.referenceTemplate(headOfCommittee, secretary, position, savedApp, null, reference,
								salutationGenerator, translator);

						MailerResult result = new MailerResult();
						reference = recruitingService.sendRefereeMail(reference, savedApp, null, position,
								template, ReferenceStatus.sentAwaiting, false, result);
						if(result.getReturnCode() == MailerResult.OK) {
							logSendMail(position, savedApp, reference);
							logAudit("Send invitation to: " + reference.getEmail(), null);	
						} else {
							logError("Cannot send invitation to: " + reference.getEmail(), null);
						}
					} 
				}
			}
		}
	}
	
	protected class CancelCallback implements StepRunnerCallback {
		@Override
		public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
			reset(ureq);
			
			/*
			//TODO selectus
			StringBuilder sb = new StringBuilder();
			sb.append("if(window.opener != null) { window.close(); } ");
			wControl.getWindowBackOffice().sendCommandTo(new JSCommand(sb.toString()));
			*/
			return Step.NOSTEP;
		}
	}
	
	public interface ApplyStep extends Step {
		
		Application getApplication();

		Position getPreselectedPosition();
		
	}
}