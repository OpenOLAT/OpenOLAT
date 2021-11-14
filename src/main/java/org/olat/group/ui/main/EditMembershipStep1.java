/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.group.ui.main;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.course.member.wizard.MembersContext;

/**
 * Initial date: Nov 13, 2020<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class EditMembershipStep1 extends BasicStep {

	private MembersContext membersContext;
	private List<Identity> members;
	
	public EditMembershipStep1(UserRequest ureq, List<Identity> members, MembersContext membersContext) {
		super(ureq);
		
		this.members = members;
		this.membersContext = membersContext;
		
		setI18nTitleAndDescr("edit.member", null);
        setNextStep(new EditMembershipStep2(ureq, membersContext));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext stepsRunContext, Form form) {
		 return new EditMembershipStep1Controller(ureq, wControl, membersContext, form, stepsRunContext);
	}
	
	
	private class EditMembershipStep1Controller extends StepFormBasicController {
		
		private EditMembershipController editMembershipController;
		
		public EditMembershipStep1Controller(UserRequest ureq, WindowControl wControl, MembersContext membersContext, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_BAREBONE, null);
			
			editMembershipController = new EditMembershipController(ureq, getWindowControl(), members, membersContext, rootForm);
			listenTo(editMembershipController);
			
			initForm(ureq);
		}
		
		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			formLayout.add(editMembershipController.getInitialFormItem());
		}
		
		@Override
		protected void formOK(UserRequest ureq) {
			MemberPermissionChangeEvent changeEvent = new MemberPermissionChangeEvent(null);
			editMembershipController.collectRepoChanges(changeEvent);
			editMembershipController.collectGroupChanges(changeEvent);
			editMembershipController.collectCurriculumElementChanges(changeEvent);
			
			addToRunContext("membershipChanges", changeEvent);
			addToRunContext("members", members);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}
		
		@Override
		protected boolean validateFormLogic(UserRequest ureq) {
			// Nothing to validate at the moment
			
			return super.validateFormLogic(ureq);
		}
	}
}
