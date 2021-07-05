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
package org.olat.repository.ui.author.copy.wizard.nodes;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.BasicStepCollection;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext.CopyType;
import org.olat.repository.ui.author.copy.wizard.CopyCourseSteps;
import org.olat.repository.ui.author.copy.wizard.CopyCourseStepsStep;

/**
 * Initial date: 14.05.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class WikiStep extends BasicStep {

	public static Step create(UserRequest ureq, BasicStepCollection stepCollection, CopyCourseSteps steps) {
		if (steps.isEditWikiSettings()) {
			return new WikiStep(ureq, stepCollection, steps);
		} else {
			return NodesOverviewStep.create(ureq, stepCollection, steps);
		}
	}
	
	public WikiStep(UserRequest ureq, BasicStepCollection stepCollection, CopyCourseSteps steps) {
		super(ureq);
		
		// Translator and title
		setTranslator(Util.createPackageTranslator(CopyCourseStepsStep.class, getLocale(), getTranslator()));
		setI18nTitleAndDescr("steps.wiki", null);
		
		// Check or create step collection
		if (stepCollection == null) {
			stepCollection = new BasicStepCollection();
			stepCollection.setTitle(getTranslator(), "steps.course.nodes.title");
		}
		setStepCollection(stepCollection);
		
		// Next step
		setNextStep(NodesOverviewStep.create(ureq, stepCollection, steps));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, !nextStep().equals(NOSTEP), nextStep().equals(NOSTEP));
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		return new WikiStepController(ureq, windowControl, form, stepsRunContext);
	}
	
	private class WikiStepController extends StepFormBasicController {

		private CopyCourseContext context;

		private SingleSelection wikiCopyModeEl;
		
		public WikiStepController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT_2_10, null);
	
			setTranslator(Util.createPackageTranslator(CopyCourseStepsStep.class, getLocale(), getTranslator()));
			
			context = (CopyCourseContext) runContext.get(CopyCourseContext.CONTEXT_KEY);
			
			initForm(ureq);
			loadModel();
		}

		@Override
		protected void doDispose() {
			// Nothing to dispose
			
		}
		
		@Override
		protected boolean validateFormLogic(UserRequest ureq) {
			boolean allOk = super.validateFormLogic(ureq);

			wikiCopyModeEl.clearError();

			if (!wikiCopyModeEl.isOneSelected()) {
				allOk &= false;
				wikiCopyModeEl.setErrorKey("error.select", null);
			}

			return allOk;
		}

		@Override
		protected void formOK(UserRequest ureq) {
			context.setCustomWikiCopyType(CopyType.valueOf(wikiCopyModeEl.getSelectedKey()));
			
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			setFormDescription("wikis.form.help");
			
			SelectionValue reference = new SelectionValue(CopyType.reference.name(), translate("wiki.reference"));
			SelectionValue createNew = new SelectionValue(CopyType.createNew.name(), translate("wiki.create.new"));
			SelectionValue ignore = new SelectionValue(CopyType.ignore.name(), translate("wiki.ignore"));
			
			SelectionValues wikiCopyModes = new SelectionValues(reference, createNew, ignore);
			
			wikiCopyModeEl = uifactory.addRadiosVertical("wiki.copy.mode", formLayout, wikiCopyModes.keys(), wikiCopyModes.values());
		}
		
		private void loadModel() {
			if (context.getCustomWikiCopyType() != null) {
				wikiCopyModeEl.select(context.getCustomWikiCopyType().name(), true);
			} else {
				wikiCopyModeEl.select(CopyType.reference.name(), true);
			}
		}
		
	}

}
