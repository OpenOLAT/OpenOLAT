/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.reference;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.OrganisationUnit;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceRequestStatus;
import org.olat.modules.selectus.model.ReferenceStatus;
import org.olat.modules.selectus.model.ReferenceType;
import org.olat.modules.selectus.ui.PositionController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * 
 * Initial date: 19.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReferenceWarningController extends BasicController {
	
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	
	public ReferenceWarningController(UserRequest ureq, WindowControl wControl, Position position, Reference reference) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		
		VelocityContainer mainVC;
		if(reference == null) {
			mainVC = createVelocityContainer("recommendation_not_found");	
			mainVC.contextPut("officeMail", recruitingModule.getOfficeMail());
		} else if(reference.getReferenceStatus() == ReferenceStatus.deactivated) {
			mainVC = createVelocityContainer("recommendation_deactivated");
			String[] args = buildApplicantArgs(position, reference);
			String message = translate("submission.deactivated.reference.msg", args);
			if(reference.getReferenceType() == ReferenceType.expert) {
				message = translate("submission.deactivated.expert.msg", args);
			}
			mainVC.contextPut("message", message);
		} else if(reference.getRequestStatus() == ReferenceRequestStatus.declined) {
			mainVC = createVelocityContainer("submission_declined");
			String[] args = buildApplicantArgs(position, reference);
			String message = translate("review.description.reference.declined", args);
			if(reference.getReferenceType() == ReferenceType.expert) {
				message = translate("review.description.expert.declined", args);
			} else if(reference.getReferenceType() == ReferenceType.comparativeAssessmentExpert) {
				message = translate("review.description.comparative.expert.declined", args);
			}
			mainVC.contextPut("message", message);
		} else if(isPositionClosed(position)) {
			mainVC = createVelocityContainer("position_closed");
		} else if(reference.getReferenceStatus() == ReferenceStatus.late) {
			mainVC = createVelocityContainer("submission_deadline_expired");
		} else if(reference.getReferenceType() == ReferenceType.expert
				|| reference.getReferenceType() == ReferenceType.comparativeAssessmentExpert) {
			mainVC = createVelocityContainer("expert_already_submitted");
		} else {
			mainVC = createVelocityContainer("recommendation_already_submitted");
		}
		
		mainVC.contextPut("reference", reference);
		putInitialPanel(mainVC);
	}
	
	private boolean isPositionClosed(Position position) {
		return position == null
				|| position.getStatus() == null
				|| PositionStatus.valueOf(position.getStatus()) == PositionStatus.closed;
	}
	
	private String[] buildApplicantArgs(Position position, Reference reference) {
		Application application = null;
		List<Application> applications = null;
		if(reference.getReferenceType() == ReferenceType.comparativeAssessmentExpert) {
			applications = recruitingService.getReferenceToApplicationsList(reference);
		} else {
			application = reference.getApplication();
		}
		OrganisationUnit organisationSettings = recruitingService.getOrganisationUnit(position);

		String applicantFullName = StringHelper.escapeHtml(salutationGenerator.getTitleFullname(application, applications, getLocale()));
		String applicantLastName = StringHelper.escapeHtml(salutationGenerator.getTitleLastName(application, applications, getLocale()));
		String applicationTitleFirstLastName = salutationGenerator.getTitleFirstLastName(application, applications, getLocale());
		Identity headOfCommittee = recruitingService.getHeadOfCommittee(position);
		String headEmail = headOfCommittee == null ? "" : headOfCommittee.getUser().getProperty(UserConstants.EMAIL, getLocale());
		if(headEmail == null) {
			headEmail = "";
		}
		String consentDate = Formatter.getInstance(getLocale()).formatDateLong(reference.getDateConsent());
		String positionMail = recruitingModule.getStaffMail(position, organisationSettings);
		
		return new String[] {
			applicantFullName,						// 0
			position.getPositionTitle(getLocale()),	// 1
			applicantLastName,						// 2
			headEmail,								// 3
			applicationTitleFirstLastName,			// 4
			consentDate,							// 5
			positionMail							// 6
		};
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
