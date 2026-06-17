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
package org.olat.modules.selectus.ui.feedback.appsfeedback.wizard;

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

import org.olat.modules.selectus.ui.committee.wizard.CommitteeMember;
import org.olat.modules.selectus.ui.committee.wizard.CommitteeMemberStatus;
import org.olat.modules.selectus.ui.committee.wizard.MembersController;
import org.olat.modules.selectus.ui.committee.wizard.MembersListController;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  12 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OverviewStepController extends StepFormBasicController {
	
	private static final String[] ROLES = new String[] { FeedbackMembersContext.FACULTY_MEMBER_PSEUDO_ROLE };

	private final List<CommitteeMember> membersToComplete;

	private MembersController memberController;
	private MembersListController memberListController;
	
	public OverviewStepController(UserRequest ureq, WindowControl wControl,
			FeedbackMembersContext feedbacksContext, StepsRunContext runContext, Form form) {
		super(ureq, wControl, form, runContext, LAYOUT_CUSTOM, "member_wrapper");
		membersToComplete = feedbacksContext.getMembers();
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(membersToComplete.size() == 1) {
			memberController = new MembersController(ureq, getWindowControl(), mainForm, membersToComplete.get(0), ROLES);
			listenTo(memberController);
			formLayout.add("member", memberController.getInitialFormItem());
		} else {
			memberListController = new MembersListController(ureq, getWindowControl(), mainForm, membersToComplete, ROLES);
			listenTo(memberListController);
			formLayout.add("member", memberListController.getInitialFormItem());
		}
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
		if(memberListController != null) {
			memberListController.event(ureq, source, event);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(memberController != null) {
			memberController.commitChanges();
			membersToComplete.get(0).setStatus(CommitteeMemberStatus.ok);
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