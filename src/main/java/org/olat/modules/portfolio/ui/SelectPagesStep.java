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
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

/**
 * Initial date: 26.02.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class SelectPagesStep extends BasicStep {

	private PortfolioImportEntriesContext context;
	private boolean binderSelected;
	
	public SelectPagesStep(UserRequest ureq, boolean binderSelected) {
		super(ureq);
	
		this.binderSelected = binderSelected;
		setI18nTitleAndDescr("select.entries.title", null);
		setNextStep(new SelectOrCreateSectionStep(ureq));
	}
	
	public SelectPagesStep(UserRequest ureq, PortfolioImportEntriesContext context, boolean binderSelected) {
		this(ureq, binderSelected);
		this.context = context;
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(!binderSelected, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		if (context != null) {
			stepsRunContext.put(PortfolioImportEntriesContext.CONTEXT_KEY, context);
		}
		
		return new SelectPagesStepController(ureq, windowControl, form, stepsRunContext);
	}
	
	private class SelectPagesStepController extends StepFormBasicController {

		private PortfolioImportEntriesContext context;
		private SelectPageListController selectPageController; 
		
		public SelectPagesStepController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
			
			context = (PortfolioImportEntriesContext) runContext.get(PortfolioImportEntriesContext.CONTEXT_KEY);
			
			selectPageController = new SelectPageListController(ureq, getWindowControl(), rootForm, context.getCurrentSection(), context.getBinderSecurityCallback(), true);
			listenTo(selectPageController);
			
			initForm(ureq);
		}

		@Override
		protected void formOK(UserRequest ureq) {			
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);	
			
			context.setSelectedEntries(selectPageController.getSelectedIndexes());
			context.setSelectedPortfolioEntries(selectPageController.getSelectedRows());
		}
		
		@Override
		protected boolean validateFormLogic(UserRequest ureq) {
			boolean allOk = super.validateFormLogic(ureq);
			
			allOk &= selectPageController.checkAndSetEmptySelectionError();
			
			return allOk;
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			setFormDescription("select.entries.desc");
			formLayout.add("selectEntries", selectPageController.getInitialFormItem());
			
			if (context.getSelectedEntries() != null) {
				selectPageController.setSelectedIndexex(context.getSelectedEntries());
			}
		}	
		
		@Override
		protected void event(UserRequest ureq, Controller source, Event event) {
			
			super.event(ureq, source, event);
		}
	}
}
