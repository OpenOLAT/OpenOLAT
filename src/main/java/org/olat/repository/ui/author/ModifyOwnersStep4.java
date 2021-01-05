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
package org.olat.repository.ui.author;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

/**
 * Initial date: Dec 21, 2020<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ModifyOwnersStep4 extends BasicStep {

	public ModifyOwnersStep4(UserRequest ureq) {
		super(ureq);
		
		setI18nTitleAndDescr("modify.owners.send.mail", null);
		setNextStep(Step.NOSTEP);
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, false, true);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		return new AuthorListEditOwnersStep4Controller(ureq, windowControl, form, stepsRunContext);
	}
	
	
	
	private class AuthorListEditOwnersStep4Controller extends StepFormBasicController {

		private final String[] keys = { "send" };
		
		private MultipleSelectionElement sendMailEl;
		
		private ModifyOwnersContext context;

		public AuthorListEditOwnersStep4Controller(UserRequest ureq, WindowControl wControl, Form form, StepsRunContext stepsRunContext) {
			super(ureq, wControl, form, stepsRunContext, LAYOUT_DEFAULT, null);
			
			context = (ModifyOwnersContext) stepsRunContext.get(ModifyOwnersContext.CONTEXT_KEY);
			
			initForm(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			setFormDescription("modify.owners.send.mail.description");
			formLayout.setElementCssClass("o_sel_users_import_contact");
			
			String[] values = new String[] { "" };
			sendMailEl = uifactory.addCheckboxesVertical("sendMail", "modify.owners.send.mail", formLayout, keys, values, 1);
			sendMailEl.select(keys[0], true);
		}
		
		@Override
		protected void doDispose() {
			//
		}

		@Override
		protected void formOK(UserRequest ureq) {
			context.setSendMail(sendMailEl.isSelected(0));
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}
	}
}
