/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.taxonomy.ui.importwizard;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.modules.taxonomy.manager.importexport.TaxonomyImportMode;

/**
 * Last step of the import into an existing taxonomy: update the existing
 * level types and levels, add the new ones, or both.
 *
 * Initial date: 30 Sept 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class ImportOptionsStep extends BasicStep {

	private final TaxonomyImportContext context;

	public ImportOptionsStep(UserRequest ureq, TaxonomyImportContext context) {
		super(ureq);
		this.context = context;
		setTranslator(TaxonomyImportWizard.getTranslator(ureq));
		setI18nTitleAndDescr("import.wizard.options.title", null);
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, false, true);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		return new ImportOptionsController(ureq, wControl, form, runContext, context);
	}

	private static class ImportOptionsController extends StepFormBasicController {

		private SingleSelection modeEl;

		private final TaxonomyImportContext context;

		public ImportOptionsController(UserRequest ureq, WindowControl wControl, Form rootForm,
				StepsRunContext runContext, TaxonomyImportContext context) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
			setTranslator(TaxonomyImportWizard.getTranslator(ureq));
			this.context = context;
			initForm(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			setFormDescription("import.wizard.options.desc");

			SelectionValues modeValues = new SelectionValues();
			for(TaxonomyImportMode mode:TaxonomyImportMode.values()) {
				String i18nPrefix = "import.wizard.options.mode." + mode.name();
				modeValues.add(SelectionValues.entry(mode.name(), translate(i18nPrefix), translate(i18nPrefix + ".desc"),
						getIconCssClass(mode), null, true));
			}
			modeEl = uifactory.addCardSingleSelectHorizontal("import.wizard.options.label", "import.wizard.options.label",
					formLayout, modeValues);
			modeEl.select(context.getMode().name(), true);
		}

		private static String getIconCssClass(TaxonomyImportMode mode) {
			return switch(mode) {
				case updateExisting -> "o_icon o_icon_edit";
				case addNew -> "o_icon o_icon_add";
				case addNewAndUpdateExisting -> "o_icon o_icon_import";
			};
		}

		@Override
		protected void formFinish(UserRequest ureq) {
			context.setMode(TaxonomyImportMode.valueOf(modeEl.getSelectedKey()));
			fireEvent(ureq, StepsEvent.INFORM_FINISHED);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			//
		}
	}
}
