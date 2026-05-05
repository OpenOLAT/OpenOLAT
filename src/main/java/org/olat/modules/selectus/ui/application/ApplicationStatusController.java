/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.application;

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.ApplicationStatus;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.components.DateCellRenderer;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  2 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ApplicationStatusController extends FormBasicController {
	
	private Application application;
	private SingleSelection decisionEl;
	private final RecruitingPositionSecurityCallback secCallback;
	
	@Autowired
	private RecruitingModule recruitingModule;

	public ApplicationStatusController(UserRequest ureq, WindowControl wControl, Application application,
			 RecruitingPositionSecurityCallback secCallback, Form rootForm) {
		super(ureq, wControl, "status", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		if(rootForm != null) {
			mainForm = rootForm;
			flc.setRootForm(rootForm);
			mainForm.addSubFormListener(this);
		}
		this.application = application;
		this.secCallback = secCallback;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		updateDecision(formLayout);
	}
	
	public void updateApplication(Application updatedApplication) {
		this.application = updatedApplication;
		updateDecision(flc);
	}
	
	private void updateDecision(FormItemContainer layoutCont) {
		if(application.isSubmittedByStaff()) {
			String text = translate("application.status.submittedByStaff.desc");
			layoutCont.contextPut("submittedByStaff", text);
		}
		
		if(application.getApplicationStatus() == ApplicationStatus.active
				&& (application.getWithdrawnDate() != null || application.getOnholdDate() != null || application.getRejectedDate() != null)) {
			initStatusDetails("application.status.active.explain", application.getCreationDate(), layoutCont);
		} else if(application.getApplicationStatus() == ApplicationStatus.onhold) {
			initStatusDetails("application.status.onhold.explain", application.getOnholdDate(), layoutCont);
		} else if(application.getApplicationStatus() == ApplicationStatus.rejected) {
			initStatusDetails("application.status.rejected.explain", application.getRejectedDate(), layoutCont);
		} else if(application.getApplicationStatus() == ApplicationStatus.withdrawn) {
			initStatusDetails("application.status.withdrawn.explain", application.getWithdrawnDate(), layoutCont);
		} else if(application.getApplicationStatus() == ApplicationStatus.noteligible) {
			initStatusDetails("application.status.noteligible.explain", application.getNotEligibleDate(), layoutCont);
		} else if(application.getApplicationStatus() == ApplicationStatus.granted) {
			initStatusDetails("application.status.granted.explain", application.getGrantedDate(), layoutCont);
		} else if(application.getApplicationStatus() == ApplicationStatus.hired) {
			initStatusDetails("application.status.hired.explain", application.getGrantedDate(), layoutCont);
		}
		
		if(secCallback.canEditCommitteeDecision()) {
			String[] decisionKeys = new String[]{ "0", "3", "2", "1" };
			String[] decisionValues = new String[] {
					translate("decision.0"), translate("decision.3"),
					translate("decision.2"), translate("decision.1")	
			};
			decisionEl = uifactory.addDropdownSingleselect("edit.decision", layoutCont, decisionKeys, decisionValues, null);
			decisionEl.setDomReplacementWrapperRequired(false);
			decisionEl.addActionListener(FormEvent.ONCHANGE);
			//TODO selectus decisionEl.setAriaLabel("edit.decision");
			
			String decision;
			if(application.getDecision() != null && application.getDecision().intValue() > 0) {
				decision = Integer.toString(application.getDecision().intValue());
			} else {
				decision = "0";
			}
			
			for(String decisionKey:decisionKeys) {
				if(decision.equals(decisionKey)) {
					decisionEl.select(decisionKey, true);
				}
			}
		} else {
			String decision;
			if(application.getDecision() != null && application.getDecision().intValue() > 0) {
				decision = translate("decision." + application.getDecision().intValue());
			} else {
				decision = translate("decision.0");
			}
			layoutCont.contextPut("decision", decision);
		}
		
		if(recruitingModule.isApplicationsCommitteeCommentEnabled() && secCallback.canViewCommitteeComment()) {
			String comment = application.getCommitteeComment();
			if(StringHelper.containsNonWhitespace(comment)) {
				comment = Formatter.escWithBR(comment).toString();
				layoutCont.contextPut("comment", comment);
				layoutCont.contextPut("hasComment", Boolean.TRUE);
			} else {
				layoutCont.contextPut("hasComment", Boolean.FALSE);
			}
		}
		
		if(recruitingModule.isApplicationsMemoEnabled()) {
			String memo = application.getMemo();
			if(StringHelper.containsNonWhitespace(memo)) {
				memo = Formatter.escWithBR(memo).toString();
				layoutCont.contextPut("memo", memo);
				layoutCont.contextPut("hasMemo", Boolean.TRUE);
			} else {
				layoutCont.contextPut("hasMemo", Boolean.FALSE);
			}
		}
	}
	
	private void initStatusDetails(String statusi18nKey, Date date, FormItemContainer layoutCont) {
		String formattedDate = DateCellRenderer.format(date);
		String text = translate(statusi18nKey, new String[]{ formattedDate });
		layoutCont.contextPut("status", text);
		if(secCallback.canEditApplicationStatus() && StringHelper.containsNonWhitespace(application.getStatusComment())) {
			StringBuilder comment = Formatter.escWithBR(application.getStatusComment());
			layoutCont.contextPut("statusComment", comment.toString());
		}
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == decisionEl) {
			//catch by the parent controller
		}
		super.formInnerEvent(ureq, source, event);
	}
}
