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
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

/**
 * Review of the level types or of the taxonomy levels of the file. The review
 * of the levels is the last step of the import as a new taxonomy, followed by
 * the update mode for an existing taxonomy.
 *
 * Initial date: 30 Sept 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class ImportReviewStep extends BasicStep {

	private final boolean levels;
	private final TaxonomyImportContext context;

	/**
	 * @param levels false for the review of the level types, true for the review of the levels
	 */
	public ImportReviewStep(UserRequest ureq, TaxonomyImportContext context, boolean levels) {
		super(ureq);
		this.context = context;
		this.levels = levels;
		setTranslator(TaxonomyImportWizard.getTranslator(ureq));
		setI18nTitleAndDescr(levels ? "import.wizard.levels.title" : "import.wizard.types.title", null);
		if(!levels) {
			setNextStep(new ImportReviewStep(ureq, context, true));
		} else if(!context.isNewTaxonomy()) {
			setNextStep(new ImportOptionsStep(ureq, context));
		}
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		boolean last = levels && context.isNewTaxonomy();
		return new PrevNextFinishConfig(true, !last, last);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		return new ImportReviewController(ureq, wControl, form, runContext, context, levels);
	}
}
