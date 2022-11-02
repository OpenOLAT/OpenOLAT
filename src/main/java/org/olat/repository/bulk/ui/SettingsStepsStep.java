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
package org.olat.repository.bulk.ui;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.repository.RepositoryService;
import org.olat.repository.bulk.SettingsBulkEditables;
import org.olat.repository.bulk.model.SettingsContext;
import org.olat.repository.bulk.model.SettingsSteps;
import org.olat.repository.bulk.model.SettingsSteps.Step;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 Oct 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SettingsStepsStep extends BasicStep {

	private final SettingsContext context;
	private final SettingsSteps steps;
	private final SettingsBulkEditables editables;

	public SettingsStepsStep(UserRequest ureq, SettingsContext context, SettingsBulkEditables editables) {
		super(ureq);
		this.editables = editables;
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.context = context;
		this.steps = new SettingsSteps();
		
		setI18nTitleAndDescr("settings.bulk.steps.title", null);
		updateNextStep(ureq);
	}

	private void updateNextStep(UserRequest ureq) {
		setNextStep(RepositoryBulkUIFactory.getNextSettingsStep(ureq, steps, Step.steps));
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl,
			StepsRunContext stepsRunContext, Form form) {
		stepsRunContext.put(SettingsContext.DEFAULT_KEY, context);
		stepsRunContext.put(SettingsBulkEditables.DEFAULT_KEY, editables);
		return new SettingsStepsController(ureq, windowControl, form, stepsRunContext, steps);
	}
	
	class SettingsStepsController extends StepFormBasicController {
		
		private MultipleSelectionElement stepsEl;
		
		private final SettingsSteps steps;
		private final SettingsContext context;
		private final SettingsBulkEditables editables;
		
		@Autowired
		private CatalogV2Module catalogModule;

		public SettingsStepsController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext, SettingsSteps steps) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
			setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
			this.steps = steps;
			this.context = (SettingsContext)runContext.get(SettingsContext.DEFAULT_KEY);
			this.editables = (SettingsBulkEditables)runContext.get(SettingsBulkEditables.DEFAULT_KEY);
			runContext.put(SettingsBulkEditables.DEFAULT_KEY, editables);
			
			initForm(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			setFormTitle("settings.bulk.steps.title");
			setFormInfo("noTransOnlyParam",
					new String[] {RepositoryBulkUIFactory.getSettingsDescription(getTranslator(), context.getRepositoryEntries(), "settings.bulk.steps.desc")});
			
			SelectionValues stepsKV = new SelectionValues();
			if (editables.isEditable(Step.metadata)) {
				stepsKV.add(entry(SettingsSteps.Step.metadata.name(), translate("settings.bulk.metadata.title")));
			}
			if (editables.isEditable(Step.taxonomy)) {
				String taxonomyI18nKey = catalogModule.isEnabled()? "settings.bulk.taxonomy.title.catalog": "settings.bulk.taxonomy.title";
				stepsKV.add(entry(SettingsSteps.Step.taxonomy.name(), translate(taxonomyI18nKey)));
			}
			if (editables.isEditable(Step.organisation)) {
				stepsKV.add(entry(SettingsSteps.Step.organisation.name(), translate("settings.bulk.organisation.title")));
			}
			if (editables.isEditable(Step.authorRights)) {
				stepsKV.add(entry(SettingsSteps.Step.authorRights.name(), translate("settings.bulk.author.rights.title")));
			}
			if (editables.isEditable(Step.execution)) {
				stepsKV.add(entry(SettingsSteps.Step.execution.name(), translate("settings.bulk.execution.title")));
			}
			stepsEl = uifactory.addCheckboxesVertical("settings.bulk.steps.steps", formLayout, stepsKV.keys(), stepsKV.values(), 1);
			steps.stream().forEach(step -> stepsEl.select(step.name(), true));
		}
		
		@Override
		protected void formOK(UserRequest ureq) {
			steps.reset();
			stepsEl.getSelectedKeys().stream().map(SettingsSteps.Step::valueOf).forEach(steps::add);
			
			updateNextStep(ureq);
			fireEvent(ureq, StepsEvent.STEPS_CHANGED);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}
		
	}

}
