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
package org.olat.modules.portfolio.ui;

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

/**
 * Initial date: 01.03.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class CreateNewBinderStep extends BasicStep {

	private PortfolioImportEntriesContext context; 
	
	public CreateNewBinderStep(UserRequest ureq, PortfolioImportEntriesContext context) {
		super(ureq);
		
		this.context = context; 
		
		setI18nTitleAndDescr("page.binders", null);
		setNextStep(new SelectPagesStep(ureq));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(false, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		stepsRunContext.put(PortfolioImportEntriesContext.CONTEXT_KEY, context);
		return new CreateNewBinderStepController(ureq, windowControl, form, stepsRunContext);
	}

	private class CreateNewBinderStepController extends StepFormBasicController {

		private PortfolioImportEntriesContext context; 
		
		private BinderMetadataEditController binderMetadataEditController;
		
		public CreateNewBinderStepController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
			
			context = (PortfolioImportEntriesContext) runContext.get(PortfolioImportEntriesContext.CONTEXT_KEY);
			binderMetadataEditController = new BinderMetadataEditController(ureq, wControl, rootForm, context);
			
			initForm(ureq);
		}

		@Override
		protected void doDispose() {
			// Nothing to dispose here
			
		}
		
		@Override
		public void dispose() {
			// Do nothing, because of RichTextElement
		}

		@Override
		protected void formOK(UserRequest ureq) {
			
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);				
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			formLayout.add("binderController", binderMetadataEditController.getInitialFormItem());			
		}
		
	}
}
