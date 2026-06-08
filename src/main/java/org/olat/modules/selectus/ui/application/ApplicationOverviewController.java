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
package org.olat.modules.selectus.ui.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.ExternalLink;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.rating.RatingEvent;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.resource.OresHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.ApplicationStatus;
import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.FeedbackService;
import org.olat.modules.selectus.MailService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SelectusReviewService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.manager.RatingClosedException;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.model.mail.EmailVariables;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.events.ApplicationChangeEvent;
import org.olat.modules.selectus.ui.events.FinalDecisionChangeEvent;
import org.olat.modules.selectus.ui.events.PositionApplicationEvent;
import org.olat.modules.selectus.ui.events.PushControllerEvent;
import org.olat.modules.selectus.ui.events.RatingChangedEvent;
import org.olat.modules.selectus.ui.events.SelectApplicationEvent;
import org.olat.modules.selectus.ui.feedback.appsfeedback.ApplicationFeedbacksController;
import org.olat.modules.selectus.ui.feedback.publicfeedback.ApplicationPublicFeedbacksController;
import org.olat.modules.selectus.ui.notifications.NotificationListController;
import org.olat.modules.selectus.ui.rating.CustomRatingComponent;
import org.olat.modules.selectus.ui.rating.RatingComparator;
import org.olat.modules.selectus.ui.rating.RatingsOverviewComponent;
import org.olat.modules.selectus.ui.rejection.CEmail_3_TemplateStep;
import org.olat.modules.selectus.ui.rejection.SendEmailRunnerCallback;
import org.olat.modules.selectus.ui.review.ApplicationReviewsController;
import org.olat.modules.selectus.ui.review.ReviewEditController;

/**
 * 
 * Initial date: 27 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationOverviewController extends BasicController implements TooledController, GenericEventListener, Activateable2 {
	
	private UserRating myRating;
	private final String emitter;
	private List<Identity> committee;
	private List<Identity> reviewers;
	private final List<UserRating> appRatings = new ArrayList<>();
	private final Position position;
	private Application application;
	private final boolean singleApplicationView;
	private final OLATResourceable positionOres;
	
	private ApplicationController applicationController;
	private ApplicationReviewsController reviewsController;
	private ApplicationFeedbacksController memberFeedbacksController;
	private ApplicationPublicFeedbacksController publicFeedbacksController;
	private CustomRatingComponent ratingComponent;
	private RatingsOverviewComponent ratingsComponent;
	private StepsMainRunController sendMailWizardController;
	
	private CloseableModalController cmc;
	private ReviewEditController editReviewCtrl;
	private ApplicationEditController editApplicationCtrl;
	private CloseableModalController editApplicationDialogBox;

	private Link next;
	private Link editLink;
	private Link mailLink;
	private Link previous;
	private Link viewLogLink;
	private Link applications;
	private Link addReviewButton;

	private final int currentPosition;
	private final int numOfApplications;
	private final List<Long> sortedKeys;
	private final TooledStackedPanel stackPanel;
	
	private final RecruitingPositionSecurityCallback secCallback;

	@Autowired
	private MailService mailService;
	@Autowired
	private AuditService auditService;
	@Autowired
	private FeedbackService feedbackService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private SelectusReviewService reviewService;
	@Autowired
	private RecruitingService erFrontendManager;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;

	public ApplicationOverviewController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			Position position, Application application, int currentPosition, List<Long> sortedKeys,
			boolean singleApplicationView, RecruitingPositionSecurityCallback secCallback) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		
		this.position = position;
		this.application = application;
		this.currentPosition = currentPosition;
		this.numOfApplications = sortedKeys.size();
		this.sortedKeys = new ArrayList<>(sortedKeys);
		this.stackPanel = stackPanel;
		this.secCallback = secCallback;
		this.singleApplicationView = singleApplicationView;
		emitter = "app-" + CodeHelper.getRAMUniqueID();

		stackPanel.addListener(this);
		
		VelocityContainer mainVC = createVelocityContainer("application_overview");
		putInitialPanel(mainVC);

		loadModel();
		initLayout(mainVC, ureq);

		positionOres = OresHelper.clone(position);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, getIdentity(), positionOres);
	}
	
	private void initLayout(VelocityContainer mainVC, UserRequest ureq) {
		applicationController = new ApplicationController(ureq, getWindowControl(), position, application, secCallback);
		listenTo(applicationController);
		mainVC.put("application", applicationController.getInitialComponent());
		
		int count = 0;
		if(reviewService.isReviewEnabled(position)) {
			reviewsController = new ApplicationReviewsController(ureq, getWindowControl(),
					position, application, reviewers, secCallback);
			listenTo(reviewsController);
			count++;
		}
		
		if(feedbackService.isPublicFeedbackEnabled(position, application)) {
			publicFeedbacksController = new ApplicationPublicFeedbacksController(ureq, getWindowControl(), position, application, secCallback);
			listenTo(publicFeedbacksController);
			count++;
		}

		if(feedbackService.isApplicationFeedbackEnabled(position, application)) {
			memberFeedbacksController = new ApplicationFeedbacksController(ureq, this.getWindowControl(), position, application, secCallback);
			listenTo(memberFeedbacksController);
			count++;
		}
		
		if(count > 1) {
			TabbedPane tabPane = new TabbedPane("feedbacksAndReview", getLocale());
			tabPane.addListener(this);
			if(reviewsController != null) {
				tabPane.addTab(translate("reviews.title"), reviewsController);
			}
			if(memberFeedbacksController != null) {
				tabPane.addTab(translate("view.member.feedbacks.title"), memberFeedbacksController.getInitialComponent());
			}
			if(publicFeedbacksController != null) {
				tabPane.addTab(translate("public.feedback.title"), publicFeedbacksController);
			}
			mainVC.put("feedbacksAndReviews", tabPane);
		} else if(reviewsController != null) {
			mainVC.put("reviews", reviewsController.getInitialComponent());
		} else if(publicFeedbacksController != null) {
			mainVC.put("public.feedbacks", publicFeedbacksController.getInitialComponent());
		} else if(memberFeedbacksController != null) {
			mainVC.put("member.feedbacks", memberFeedbacksController.getInitialComponent());
		}
	}
	
	public Application getApplication() {
		return application;
	}
	
	public void loadModel() {
		myRating = erFrontendManager.getRating(application, getIdentity());
		committee = erFrontendManager.getCommittee(position, recruitingModule.getRolesAllowedToRate());
		reviewers = reviewService.getReviewers(position);
		List<UserRating> ratings = erFrontendManager.getRatings(application, committee);
		
		appRatings.clear();
		for(Identity member:committee) {
			UserRating memberRating = null;
			for(UserRating rating:ratings) {
				if(member.getKey().equals(rating.getCreator().getKey())) {
					memberRating = rating;
					break;
				}
			}
			appRatings.add(memberRating);
		}
		
		if(ratingsComponent != null) {
			Collections.sort(appRatings, new RatingComparator());
			ratingsComponent.setRatings(appRatings);
		}
	}

	@Override
	protected void doDispose() {
		if(stackPanel != null) {
			stackPanel.removeListener(this);
		}
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, positionOres);
	}

	@Override
	public void initTools() {
		if(!singleApplicationView) {
			previous = LinkFactory.createToolLink("previous", "previous", translate("previous"), this);
			stackPanel.addTool(previous);
			previous.setIconLeftCSS("o_icon o_icon-lg o_icon_previous_page");
	
			String pos = translate("application.currentPosition", new String[]{ Integer.toString((currentPosition + 1)), Integer.toString(numOfApplications) });
			String posTool = "<span class='o_text_icon'>" + pos + "</span>" + translate("application.applications");
			applications = LinkFactory.createToolLink("application.applications", posTool, this);
			stackPanel.addTool(applications);
			
			next = LinkFactory.createToolLink("next", translate("next"), this);
			stackPanel.addTool(next);
			next.setIconLeftCSS("o_icon o_icon-lg o_icon_next_page");
		}
		
		if(secCallback.canRate() && application.getApplicationStatus() == ApplicationStatus.active) {
			float r = (myRating == null || myRating.getRating() == null) ? 0.0f : myRating.getRating().floatValue();
			ratingComponent = new CustomRatingComponent("my-rating", r, recruitingModule.getMaxRating(), true, recruitingModule.isRatingAbstentionEnabled());
			ratingComponent.setDomReplacementWrapperRequired(false);
			ratingComponent.setTranslator(getTranslator());
			ratingComponent.setLevelLabel(0, "rating.0");
			ratingComponent.setLevelLabel(1, "rating.1");
			ratingComponent.setLevelLabel(2, "rating.2");
			ratingComponent.addListener(this);
			ratingComponent.setEnabled(isAllowedToRateByStatus());
			stackPanel.addTool(ratingComponent, Align.left);
		}
		
		if(reviewService.isReviewEnabled(position) && secCallback.canReview(application)) {
			String i18nKey = reviewsController != null && reviewsController.hasReviewed()
					? "edit.review" : "add.review";
			addReviewButton = LinkFactory.createButton(i18nKey, null, this);
			addReviewButton.setTranslator(getTranslator());
			addReviewButton.setElementCssClass("o_add_review_button");
			addReviewButton.setIconLeftCSS("o_icon o_icon-lg o_icon_reviews");
			stackPanel.addTool(addReviewButton, Align.left);
		}
		
		if(secCallback.canSeeCommitteeRatings()) {
			ratingsComponent = new RatingsOverviewComponent("committee-ratings");
			Collections.sort(appRatings, new RatingComparator());
			ratingsComponent.setRatings(appRatings);
			stackPanel.addTool(ratingsComponent, Align.left);
		}
		
		if(secCallback.canViewApplicationLog()) {
			viewLogLink = LinkFactory.createToolLink("view.application.log", translate("view.application.log"), this);
			viewLogLink.setIconLeftCSS("o_icon o_icon-lg o_filetype_log");
			stackPanel.addTool(viewLogLink, Align.right);
		}
		
		if(secCallback.canSendMailToApplicant()) {
			mailLink = LinkFactory.createToolLink("action.sendMail", translate("action.sendMail"), this);
			mailLink.setIconLeftCSS("o_icon o_icon-lg o_icon_mail");
			stackPanel.addTool(mailLink, Align.right);
		}
		
		if(!singleApplicationView && secCallback.canEditApplication()) {
			editLink = LinkFactory.createToolLink("edit", translate("edit"), this);
			editLink.setIconLeftCSS("o_icon o_icon-lg o_icon_edit");
			stackPanel.addTool(editLink, Align.right);
		}
		
		if(!singleApplicationView) {
			String path = "[Positions:0][Position:" + position.getKey() + "][Applications:" + application.getKey() + "]";
			String url = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(path);
			ExternalLink openInNewWindowButton = LinkFactory.createExternalLink("open.new.window", translate("open.new.window"), url);
			openInNewWindowButton.setIconLeftCSS("o_icon o_icon-lg o_icon_external_link");
			openInNewWindowButton.setName(translate("open.new.window"));
			stackPanel.addTool(openInNewWindowButton, Align.rightEdge);
		}
	}

	private boolean isAllowedToRateByStatus() {
		String statusStr = position.getStatus();
		if(!StringHelper.containsNonWhitespace(statusStr)) {
			return false;
		}
		if(application.getDecision() != null && application.getDecision() > 0) {
			return false;
		}
		PositionStatus status = PositionStatus.valueOf(statusStr);
		//not in screening -> can't rate
		return PositionStatus.publishedAndInScreening.equals(status) || PositionStatus.closedAndInScreening.equals(status);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (event instanceof PushControllerEvent) {
			fireEvent(ureq, event);
		} else if(source == editApplicationCtrl) {
			if (event == Event.CANCELLED_EVENT) {
				//do nothing
			} else if (event == Event.CHANGED_EVENT) {
				fireEvent(ureq, new ApplicationChangeEvent());
			}	else if (event == Event.DONE_EVENT){
				fireEvent(ureq, new ApplicationChangeEvent());
			}
			editApplicationDialogBox.deactivate();
			cleanUp();
		} else if(editReviewCtrl == source) {
			reviewsController.loadModel();
			updateAfterReviewChanged();
			cmc.deactivate();
			cleanUp();
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if(reviewsController == source) {
			if(event == Event.CHANGED_EVENT) {
				updateAfterReviewChanged();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if (source == editApplicationDialogBox || source == cmc) {
			cleanUp();
		} else if(sendMailWizardController == source) {
			if (event == Event.CANCELLED_EVENT) {
				getWindowControl().pop();
			} else if (event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				getWindowControl().pop();
				
				@SuppressWarnings("unchecked")
				Map<ApplicationLight, MailerResult> mailerResults = (Map<ApplicationLight, MailerResult>)sendMailWizardController.getRunContext().get("mailerResults");
				Boolean asyncMailer = (Boolean)sendMailWizardController.getRunContext().get("asyncMailer");
				analyseResults(asyncMailer, mailerResults);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			removeAsListenerAndDispose(sendMailWizardController);
			sendMailWizardController = null;
		} else if(applicationController == source) {
			if(event == Event.CHANGED_EVENT) {
				fireEvent(ureq, event);
			}
		} else if(source instanceof NotificationListController) {
			stackPanel.popUpToController(this);
			if(event instanceof SelectApplicationEvent) {
				SelectApplicationEvent sae = (SelectApplicationEvent)event;
				activate(ureq, sae.getActivation(), null);
			}
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(editReviewCtrl);
		removeAsListenerAndDispose(editApplicationCtrl);
		removeAsListenerAndDispose(editApplicationDialogBox);
		editApplicationDialogBox = null;
		editApplicationCtrl = null;
		editReviewCtrl = null;
		cmc = null;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(("Comment".equals(type) || "Review".equals(type)) && reviewsController != null) {
			reviewsController.activate(ureq, entries, state);
		}
	}

	@Override
	public void event(Event event) {
		if(FinalDecisionChangeEvent.FINAL_DECISION.equals(event.getCommand())
				&& event instanceof FinalDecisionChangeEvent) {
			FinalDecisionChangeEvent changeEvent = (FinalDecisionChangeEvent)event;
			if( changeEvent.getApplicationKey().equals(application.getKey())) {
				application = erFrontendManager.getApplicationByKey(application.getKey());
				if(ratingComponent != null) {
					ratingComponent.setEnabled(isAllowedToRateByStatus());
				}
				applicationController.updateApplication(application);
			}
		} else if(event instanceof RatingChangedEvent) {
			RatingChangedEvent changeEvent = (RatingChangedEvent)event;
			if(changeEvent.getDoerIdentityKey() != null
					&& changeEvent.getDoerIdentityKey().equals(getIdentity().getKey())
					&& changeEvent.getApplication().getKey().equals(application.getKey())
					&& !emitter.equals(changeEvent.getEmitter())
					&& ratingComponent != null && ratingComponent.isEnabled()) {
				
				UserRating rating = erFrontendManager.getRating(application, getIdentity());
				float currentRatingVal = rating == null || rating.getRating() == null ? 0.0f : rating.getRating().floatValue();
				ratingComponent.setCurrentRating(currentRatingVal);
			}
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == next) {
			doNext(ureq);
		} else if (source == previous) {
			doPrevious(ureq);
		} else if (source == applications) {
			fireEvent(ureq, PositionApplicationEvent.ALL);
		}  else if (source == ratingComponent) {
			doRate((RatingEvent)event);
			loadModel();
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if(source == editLink) {
			editApplication(ureq);
		} else if(viewLogLink == source) {
			doLog(ureq);
		} else if(source == addReviewButton) {
			doAddReview(ureq);
		} else if(source == mailLink) {
			doSendMail(ureq);
		}
	}
	
	private void doPrevious(UserRequest ureq) {
		Long appKey = null;
		if(currentPosition > 0 && currentPosition < sortedKeys.size()) {
			appKey = sortedKeys.get(currentPosition - 1);
		} else if (!sortedKeys.isEmpty()) {
			appKey = sortedKeys.get(sortedKeys.size() - 1);
		}
		fireEvent(ureq, new PositionApplicationEvent(PositionApplicationEvent.PREVIOUS, appKey, sortedKeys));
	}
	
	private void doNext(UserRequest ureq) {
		Long appKey = null;
		if(currentPosition >= 0 && (currentPosition + 1) < sortedKeys.size()) {
			appKey = sortedKeys.get(currentPosition + 1);
		} else if (!sortedKeys.isEmpty()) {
			appKey = sortedKeys.get(0);
		}
		fireEvent(ureq, new PositionApplicationEvent(PositionApplicationEvent.NEXT, appKey, sortedKeys));
	}
	
	private void doLog(UserRequest ureq) {
		NotificationListController notificationListCtrl = new NotificationListController(ureq, getWindowControl(), position, application);
		listenTo(notificationListCtrl);
		stackPanel.pushController(translate("view.application.log"), notificationListCtrl);
	}
	
	private void doRate(RatingEvent e) {
		try {
			boolean remove = false;
			Integer currentRating = null;
			if(myRating != null && myRating.getRating() != null) {
				currentRating = myRating.getRating();
				float selectedRating = e.getRating();
				float diff = currentRating.floatValue() - selectedRating;
				if(Math.abs(diff) <= 0.001) {
					remove = true;
				}
			}
			
			if(remove) {
				erFrontendManager.removeRating(application, getIdentity());
				ratingComponent.setCurrentRating(0.0f);
				myRating = null;
				
				//log
				String messageI18n = "audit.log.rating.remove";
				String beforeRating = currentRating == null ? "-" : Integer.toString(currentRating);
				String[] messageArgs = new String[] { translateRating(currentRating), "",
						salutationGenerator.getTitleFullname(application, getLocale()), application.getId().toString() };
				auditService.auditRatingLog(Action.remove, ActionTarget.rating, beforeRating, null,
						messageI18n, messageArgs, getTranslator(), position, application, null, getIdentity());
			} else {
				int newRating = Float.valueOf(e.getRating()).intValue();
				myRating = erFrontendManager.setRating(application, getIdentity(), newRating);
				
				//log
				if(currentRating == null) {
					String messageI18n = "audit.log.rating.add";
					String[] messageArgs = new String[] {
							"", translateRating(newRating),
							salutationGenerator.getTitleFullname(application, getLocale()), application.getId().toString() };
					auditService.auditRatingLog(Action.add, ActionTarget.rating, null, Integer.toString(newRating),
							messageI18n, messageArgs, getTranslator(), position, application, myRating, getIdentity());
				} else {
					String messageI18n = "audit.log.rating.update";
					String[] messageArgs = new String[] { translateRating(currentRating), translateRating(newRating),
							salutationGenerator.getTitleFullname(application, getLocale()), application.getId().toString() };
					auditService.auditRatingLog(Action.update, ActionTarget.rating, currentRating.toString(), Integer.toString(newRating),
							messageI18n, messageArgs, getTranslator(), position, application, myRating, getIdentity());
				}
			}
			
			RatingChangedEvent changedEvent = new RatingChangedEvent(application, getIdentity().getKey(), "app");
			CoordinatorManager.getInstance().getCoordinator().getEventBus()
					.fireEventToListenersOf(changedEvent, positionOres);
		} catch (RatingClosedException rce) {
			showWarning("rating.closed.error");
			ratingComponent.setEnabled(false);
			UserRating lastRating = rce.getLastRating();
			float r = (lastRating == null || lastRating.getRating() == null) ? 0.0f : lastRating.getRating().floatValue();
			ratingComponent.setCurrentRating(r);
		}
	}
	
	private String translateRating(Integer rating) {
		if(rating == null) {
			return "-";
		}
		if(rating.intValue() > 0 && rating.intValue() <= 3) {
			return translate("rating." + (rating - 1));
		}
		if(rating.intValue() == RecruitingService.ABSTENTION) {
			return translate("abstain.title");
		}
		return "";
	}

	private void editApplication(UserRequest ureq) {
		removeAsListenerAndDispose(editApplicationCtrl);
		removeAsListenerAndDispose(editApplicationDialogBox);

		Application reloadedApp = erFrontendManager.getApplicationToEdit(application);
		editApplicationCtrl = new ApplicationEditController(ureq, getWindowControl(), reloadedApp, position, secCallback);
		listenTo(editApplicationCtrl);
		
		String title = translate("edit_application");
		editApplicationDialogBox = new CloseableModalController(getWindowControl(), "c", editApplicationCtrl.getInitialComponent(), title);
		editApplicationDialogBox.activate();
		listenTo(editApplicationDialogBox);
	}
	
	private void updateAfterReviewChanged() {
		if (addReviewButton != null && reviewsController != null) {
			addReviewButton.setVisible(secCallback.canReview(application));
			String link = reviewsController.hasReviewed()
					? translate("edit.review") : translate("add.review");
					addReviewButton.setCustomDisplayText(link);			
		}
	}
	
	private void doAddReview(UserRequest ureq) {
		if(guardModalController(editReviewCtrl)) return;
		
		editReviewCtrl = new ReviewEditController(ureq, getWindowControl(), position, application);
		listenTo(editReviewCtrl);
		
		String title = translate("add.review");
		cmc = new CloseableModalController(getWindowControl(), "c", editReviewCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doSendMail(UserRequest ureq) {
		final EmailVariables emailVar = mailService.getEmailVariables(position, application, getLocale());
		emailVar.setShowAttachmentWarning(true);
		Step start = new CEmail_3_TemplateStep(ureq, emailVar, secCallback);
		startSendEmail(ureq, emailVar, start);
	}
	
	private void startSendEmail(UserRequest ureq, final EmailVariables emailVar, Step start) {
		StepRunnerCallback finish = new SendEmailRunnerCallback(getIdentity(), emailVar, secCallback, getTranslator());

		removeAsListenerAndDispose(sendMailWizardController);
		sendMailWizardController = new StepsMainRunController(ureq, getWindowControl(), start, finish, null, translate("rejection.wizard.title"), null);
		listenTo(sendMailWizardController);
		
		getWindowControl().pushAsModalDialog(sendMailWizardController.getInitialComponent());
	}
	
	private void analyseResults(Boolean asyncMailer, Map<ApplicationLight, MailerResult> mailerResults) {
		if(asyncMailer != null && asyncMailer.booleanValue()) {
			showInfo("rejection.mail.sent");
			return;
		}

		int countError = 0;
		for(MailerResult result:mailerResults.values()) {
			if(result.getReturnCode() != MailerResult.OK) {
				countError++;
			}
		}
		
		if(countError == 0) {
			showInfo("rejection.mail.send.success");
		} else {
			StringBuilder sb = new StringBuilder();
			MailerResult aggregated = new MailerResult();
			for(Map.Entry<ApplicationLight, MailerResult> entry:mailerResults.entrySet()) {
				MailerResult result = entry.getValue();
				if(result.getReturnCode() != MailerResult.OK) {
					ApplicationLight app = entry.getKey();
					String mail = app.getPerson().getMail();
					if(sb.length() > 0) sb.append(", ");
					sb.append(mail);
					aggregated.append(result);
				}
			}
			if(aggregated.getFailedAddresses().isEmpty()) {
				showError("rejection.mail.send.error", sb.toString());
			} else {
				String error = aggregated.getFailedAddresses().size() == 1 ?"rejection.mail.send.invalid.address" : "rejection.mail.send.invalid.addresses";
				showError(error, new String[] { sb.toString(), aggregated.failedAddressesToString() });
			}
		}
	}
	
	public static class PositionInfo {
		private final String planingsNumber;
		private final String title;
		
		public PositionInfo(String title) {
			this(null, title);
		}
		
		public PositionInfo(String planingsNumber, String title) {
			this.planingsNumber = planingsNumber;
			this.title = title;
		}

		public String getPlaningsNumber() {
			return planingsNumber;
		}

		public String getTitle() {
			return title;
		}
	}
}

