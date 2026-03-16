/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.committee.wizard;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.PositionRole;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  12 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MembersStepController extends StepFormBasicController {

	private final String[] availableRoles;
	private final List<CommitteeMember> membersToComplete;

	private MembersController memberController;
	private MembersListController memberListController;
	
	@Autowired
	private RecruitingModule recruitingModule;
	
	public MembersStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		super(ureq, wControl, form, runContext, LAYOUT_CUSTOM, "member_wrapper");
		Committee committee = (Committee)runContext.get(CommitteeWizard.COMMITTEE);
		membersToComplete = committee.getMembers();
		availableRoles = getAvailableRoles();
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(membersToComplete.size() == 1) {
			memberController = new MembersController(ureq, getWindowControl(), mainForm, membersToComplete.get(0), availableRoles);
			listenTo(memberController);
			formLayout.add("member", memberController.getInitialFormItem());
		} else {
			memberListController = new MembersListController(ureq, getWindowControl(), mainForm, membersToComplete, availableRoles);
			listenTo(memberListController);
			formLayout.add("member", memberListController.getInitialFormItem());
		}
	}
	
	private String[] getAvailableRoles() {
		if(recruitingModule.isRoleExOfficioEnabled()) {
			return PositionRole.roles();
		}
		return new String[]{ PositionRole.member.role(), PositionRole.head.role(), PositionRole.secretary.role() };
	}

	@Override
	protected void doDispose() {
		mainForm.removeSubFormListener(this);
		if(memberController != null) {
			mainForm.removeSubFormListener(memberController);
		}
		if(memberListController != null) {
			mainForm.removeSubFormListener(memberListController);
		}
	}
	
	@Override
	public void back() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		setFormWarning(null);
		if(memberController != null) {
			allOk &= memberController.validateFormLogic(ureq);
		} else if(memberListController != null) {
			allOk &= memberListController.validateFormLogic(ureq);
			if(!allOk) {
				setFormWarning("error.member.invalid");
			}
		}

		return allOk;
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(memberController == source) {
			if(event == Event.DONE_EVENT) {
				fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
			}
		}
		
		if(memberListController != null) {
			memberListController.event(ureq, source, event);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(memberController != null) {
			memberController.commitChanges();
		}
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(memberListController != null) {
			memberListController.formInnerEvent(ureq, source, event);
		}
		super.formInnerEvent(ureq, source, event);
	}
}