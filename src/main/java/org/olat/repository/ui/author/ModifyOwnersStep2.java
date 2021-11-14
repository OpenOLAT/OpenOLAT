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

import java.util.Collections;

import org.olat.admin.user.UserSearchFlexiController;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

/**
 * Initial date: Dec 21, 2020<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ModifyOwnersStep2 extends BasicStep {

	public ModifyOwnersStep2(UserRequest ureq) {
		super(ureq);
		
		setI18nTitleAndDescr("modify.owners.add", null);
		setNextStep(new ModifyOwnersStep3(ureq));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		return new AuthorListEditOwnersStep2Controller(ureq, windowControl, form, stepsRunContext);
	}
	
	private class AuthorListEditOwnersStep2Controller extends StepFormBasicController {
		
		private final UserSearchFlexiController searchController; 
		private boolean showOverviewTable;
		private ModifyOwnersContext context;

		public AuthorListEditOwnersStep2Controller(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);

			context = (ModifyOwnersContext) runContext.get(ModifyOwnersContext.CONTEXT_KEY);
			searchController = new UserSearchFlexiController(ureq, wControl, rootForm);
			
			listenTo(searchController);
			initForm (ureq);
		}

		@Override
		protected void event(UserRequest ureq, Controller source, Event event) {
			if(event instanceof SingleIdentityChosenEvent) {
				SingleIdentityChosenEvent e = (SingleIdentityChosenEvent)event;
				context.setOwnersToAdd(Collections.singletonList(e.getChosenIdentity()));
				fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
			} else if(event instanceof MultiIdentityChosenEvent) {
				MultiIdentityChosenEvent e = (MultiIdentityChosenEvent)event;
				context.setOwnersToAdd(e.getChosenIdentities());
				fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
			} else {
				super.event(ureq, source, event);
			}
		}

		@Override
		protected void formNext(UserRequest ureq) {
			context.setOwnersToAdd(searchController.getSelectedIdentities());
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			//do nothing
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			formLayout.add("search", searchController.getInitialFormItem());
			flc.contextPut("showTable", showOverviewTable);
		}
	}
}
