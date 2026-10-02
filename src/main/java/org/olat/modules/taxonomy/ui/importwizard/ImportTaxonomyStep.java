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
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.modules.taxonomy.manager.importexport.TaxonomyFileData;
import org.olat.modules.taxonomy.manager.importexport.TaxonomyImportExportService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Step 2 (new taxonomy only): reference and title of the new taxonomy.
 * 
 * Initial date: 30 Sept 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class ImportTaxonomyStep extends BasicStep {

	private final TaxonomyImportContext context;

	public ImportTaxonomyStep(UserRequest ureq, TaxonomyImportContext context) {
		super(ureq);
		this.context = context;
		setTranslator(TaxonomyImportWizard.getTranslator(ureq));
		setI18nTitleAndDescr("import.wizard.taxonomy.title", null);
		setNextStep(new ImportReviewStep(ureq, context, false));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		return new ImportTaxonomyController(ureq, wControl, form, runContext, context);
	}

	private static class ImportTaxonomyController extends StepFormBasicController {

		private TextElement identifierEl;
		private TextElement displayNameEl;

		private final TaxonomyImportContext context;

		@Autowired
		private TaxonomyImportExportService importExportService;

		public ImportTaxonomyController(UserRequest ureq, WindowControl wControl, Form rootForm,
				StepsRunContext runContext, TaxonomyImportContext context) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
			setTranslator(TaxonomyImportWizard.getTranslator(ureq));
			this.context = context;
			initForm(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			setFormDescription("import.wizard.taxonomy.desc");

			TaxonomyFileData data = context.getData();
			// suggest a free reference: two taxonomies must never share one
			String identifier = context.getIdentifier() != null
					? context.getIdentifier() : importExportService.getUniqueTaxonomyIdentifier(data.getIdentifier());
			String displayName = context.getDisplayName() != null ? context.getDisplayName() : data.getDisplayName();

			identifierEl = uifactory.addTextElement("taxonomy.identifier", "taxonomy.identifier",
					TaxonomyImportExportService.MAX_TAXONOMY_IDENTIFIER, identifier, formLayout);
			identifierEl.setMandatory(true);
			if(StringHelper.containsNonWhitespace(data.getIdentifier()) && !data.getIdentifier().equals(identifier)) {
				identifierEl.setExampleKey("import.wizard.taxonomy.identifier.changed", new String[] { data.getIdentifier() });
			}
			displayNameEl = uifactory.addTextElement("taxonomy.displayname", "taxonomy.title",
					TaxonomyImportExportService.MAX_TITLE, displayName, formLayout);
			displayNameEl.setMandatory(true);

			// the external ID is never edited in OpenOlat, as in the metadata of the taxonomy
			if(StringHelper.containsNonWhitespace(data.getExternalId())) {
				uifactory.addStaticTextElement("taxonomy.external.id", data.getExternalId(), formLayout);
			}
		}

		@Override
		protected boolean validateFormLogic(UserRequest ureq) {
			boolean allOk = super.validateFormLogic(ureq);
			if(validateMandatory(identifierEl, TaxonomyImportExportService.MAX_TAXONOMY_IDENTIFIER)
					&& importExportService.isTaxonomyIdentifierUsed(identifierEl.getValue().trim())) {
				identifierEl.setErrorKey("import.wizard.taxonomy.identifier.used");
				allOk &= false;
			} else if(identifierEl.hasError()) {
				allOk &= false;
			}
			allOk &= validateMandatory(displayNameEl, TaxonomyImportExportService.MAX_TITLE);

			return allOk;
		}

		private boolean validateMandatory(TextElement el, int maxLength) {
			el.clearError();
			if(!StringHelper.containsNonWhitespace(el.getValue())) {
				el.setErrorKey("form.legende.mandatory");
				return false;
			} else if(el.getValue().length() > maxLength) {
				el.setErrorKey("form.error.toolong", Integer.toString(maxLength));
				return false;
			}
			return true;
		}

		@Override
		protected void formNext(UserRequest ureq) {
			context.setIdentifier(identifierEl.getValue().trim());
			context.setDisplayName(displayNameEl.getValue());
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			//
		}
	}
}
