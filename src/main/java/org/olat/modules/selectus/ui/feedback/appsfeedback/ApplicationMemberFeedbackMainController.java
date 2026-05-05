/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.feedback.appsfeedback;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;

/**
 * 
 * Initial date: 30 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationMemberFeedbackMainController extends BasicController {
	
	private static final String DETAILS_STEP = "application.details";
	private static final String SUBMIT_STEP = "feedback.submit";
	
	private final Link submitLink;
	private final Link applicationDetailsLink;
	private final VelocityContainer mainVC;
	
	private final Position position;
	private final Application application;
	private ApplicationFeedback feedback;
	private final RecruitingPositionSecurityCallback secCallback;
	
	private ApplicationMemberFeedbackDetailsController detailsCtrl;
	private ApplicationMemberFeedbackSubmissionController submitCtrl;
	
	@Autowired
	private RecruitingService recruitingService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	
	public ApplicationMemberFeedbackMainController(UserRequest ureq, WindowControl wControl, ApplicationFeedback feedback,
			RecruitingPositionSecurityCallback secCallback) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		
		this.feedback = feedback;
		this.secCallback = secCallback;
		application = feedback.getApplication();
		position = application.getPosition();
		
		mainVC = createVelocityContainer("feedback_tabs");
		
		String appLinkName = "apps.feedback.application.details";
		applicationDetailsLink = LinkFactory.createLink("apps.feedback.application.details", appLinkName, mainVC, this);
		applicationDetailsLink.setElementCssClass("o_sel_application_details");
		mainVC.put("applicationDetailsLink", applicationDetailsLink);

		String submitLinkName = "apps.feedback.submit";
		submitLink = LinkFactory.createLink(submitLinkName, submitLinkName, mainVC, this);
		submitLink.setElementCssClass("o_sel_submit_reference");
		mainVC.put("reference.submit", submitLink);

		String[] fullNames = new String[] {
			RecruitingHelper.formatFullName(getIdentity()),
			RecruitingHelper.formatFullNameWithTitle(getIdentity(), getLocale())
		};
		String feedbackTitle = translate("apps.feedback.submiter.title", fullNames);
		mainVC.contextPut("feedbackTitle", feedbackTitle);
		mainVC.contextPut("currentStep", DETAILS_STEP);
		
		String[] applicationMsgArgs = applicationMessageArgs();
		String applicationMsg = translate("apps.feedback.submiter.text", applicationMsgArgs);
		mainVC.contextPut("applicationMsg", applicationMsg);
		
		putInitialPanel(mainVC);
		doDetails(ureq);
	}
	
	private String[] applicationMessageArgs() {
		String applicantFullName = StringHelper.escapeHtml(salutationGenerator.getTitleFullname(application, getLocale()));
		String applicantLastName = StringHelper.escapeHtml(salutationGenerator.getTitleLastName(application, getLocale()));
		String applicationTitleFirstLastName = salutationGenerator.getTitleFirstLastName(application, getLocale());
		Identity headOfCommittee = recruitingService.getHeadOfCommittee(position);
		String headEmail = headOfCommittee == null ? "" : headOfCommittee.getUser().getProperty(UserConstants.EMAIL, getLocale());
		if(headEmail == null) {
			headEmail = "";
		}
		
		return new String[] {
			applicantFullName,						// 0
			position.getPositionTitle(getLocale()),	// 1
			applicantLastName,						// 2
			headEmail,								// 3
			applicationTitleFirstLastName			// 4
		};
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(detailsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doSubmit(ureq);
				mainVC.contextPut("currentStep", SUBMIT_STEP);
			}
		} else if(submitCtrl == source) {
			if(event == Event.DONE_EVENT) {
				feedback = submitCtrl.getFeedback();
				fireEvent(ureq, event);
			} else if(event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(applicationDetailsLink == source) {
			doDetails(ureq);
			mainVC.contextPut("currentStep", DETAILS_STEP);
		} else if(submitLink == source) {
			doSubmit(ureq);
			mainVC.contextPut("currentStep", SUBMIT_STEP);
		}
	}
	
	private void doDetails(UserRequest ureq) {
		if(detailsCtrl == null) {
			detailsCtrl = new ApplicationMemberFeedbackDetailsController(ureq, getWindowControl(),
					position, application, secCallback, false);
			listenTo(detailsCtrl);
		}
		mainVC.put("content", detailsCtrl.getInitialComponent());
	}
	
	private void doSubmit(UserRequest ureq) {
		if(submitCtrl == null) {
			submitCtrl = new ApplicationMemberFeedbackSubmissionController(ureq, getWindowControl(),
					position, application, feedback);
			listenTo(submitCtrl);
		}
		mainVC.put("content", submitCtrl.getInitialComponent());
	}

}
