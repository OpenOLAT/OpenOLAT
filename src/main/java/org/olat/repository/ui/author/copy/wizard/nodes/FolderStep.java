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
public class FolderStep extends BasicStep {

	public static Step create(UserRequest ureq, BasicStepCollection stepCollection, CopyCourseSteps steps) {
		if (steps.isEditFolderSettings()) {
			return new FolderStep(ureq, stepCollection, steps);
		} else {
			return WikiStep.create(ureq, stepCollection, steps);
		}
	}
	
	public FolderStep(UserRequest ureq, BasicStepCollection stepCollection, CopyCourseSteps steps) {
		super(ureq);
		
		// Translator and title
		setTranslator(Util.createPackageTranslator(CopyCourseStepsStep.class, getLocale(), getTranslator()));
		setI18nTitleAndDescr("steps.folder", null);
		
		// Check or create step collection
		if (stepCollection == null) {
			stepCollection = new BasicStepCollection();
			stepCollection.setTitle(getTranslator(), "steps.course.nodes.title");
		}
		setStepCollection(stepCollection);
		
		// Next step
		setNextStep(WikiStep.create(ureq, stepCollection, steps));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, !nextStep().equals(NOSTEP), nextStep().equals(NOSTEP));
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		return new FolderStepController(ureq, windowControl, form, stepsRunContext);
	}
	
	private class FolderStepController extends StepFormBasicController {

		private CopyCourseContext context;

		private SingleSelection folderCopyModeEl;
		
		public FolderStepController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
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

			folderCopyModeEl.clearError();

			if (!folderCopyModeEl.isOneSelected()) {
				allOk &= false;
				folderCopyModeEl.setErrorKey("error.select", null);
			}

			return allOk;
		}

		@Override
		protected void formOK(UserRequest ureq) {
			context.setCustomFolderCopyType(CopyType.valueOf(folderCopyModeEl.getSelectedKey()));
			
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			setFormDescription("folders.form.help");
			
			SelectionValue reference = new SelectionValue(CopyType.reference.name(), translate("folder.copy"));
			SelectionValue createNew = new SelectionValue(CopyType.createNew.name(), translate("folder.create.new"));
			SelectionValue ignore = new SelectionValue(CopyType.ignore.name(), translate("folder.ignore"));
			
			SelectionValues folderCopyModes = new SelectionValues(reference, createNew, ignore);
			
			folderCopyModeEl = uifactory.addRadiosVertical("folder.copy.mode", formLayout, folderCopyModes.keys(), folderCopyModes.values());
		}
		
		private void loadModel() {
			if (context.getCustomFolderCopyType() != null) {
				folderCopyModeEl.select(context.getCustomFolderCopyType().name(), true);
			} else {
				folderCopyModeEl.select(CopyType.reference.name(), true);
			}
		}
	}

}
