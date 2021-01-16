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
package org.olat.modules.taxonomy.ui;

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
 * Initial date: Jan 12, 2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class TaxonomyImportStep3 extends BasicStep {

	public TaxonomyImportStep3(UserRequest ureq) {
		super(ureq);
		setI18nTitleAndDescr("import.taxonomy.step.3.title", "import.taxonomy.step.3.desc");
		setNextStep(Step.NOSTEP);
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, false, true);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		return new TaxonomyImportStep3Controller(ureq, windowControl, form, stepsRunContext);
	}
	
	private class TaxonomyImportStep3Controller extends StepFormBasicController {

		private TaxonomyImportContext context;
		
		private MultipleSelectionElement updateTaxonomiesElement;
		
		public TaxonomyImportStep3Controller(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT_2_10, null);
			
			this.context = (TaxonomyImportContext) runContext.get(TaxonomyImportContext.CONTEXT_KEY);
			
			initForm(ureq);
		}

		@Override
		protected void doDispose() {
			// Nothing to dispose
		}
		
		@Override
		protected void formOK(UserRequest ureq) {
			// Set whether to update existing taxonomies
			context.setUpdatateExistingTaxonomies(updateTaxonomiesElement.isSelected(0));
			
			// Fire event to get to the next step
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			setFormDescription("import.taxonomy.step.3.desc");
			
			String[] keys = new String[] {"import.taxonomy.update.confirmation" + (context.getUpdatedTaxonomies() == 1 ? ".singular" : "")};
			String[] values = new String[] {translate(keys[0], String.valueOf(context.getUpdatedTaxonomies()))};
			
			updateTaxonomiesElement = uifactory.addCheckboxesHorizontal("import.taxonomy.update.confirmation.label", formLayout, keys, values);
			updateTaxonomiesElement.setEnabled(context.getUpdatedTaxonomies() > 0);
		}		
	}
}
