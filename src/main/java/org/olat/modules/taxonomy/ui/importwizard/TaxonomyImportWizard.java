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

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.manager.importexport.TaxonomyImportExportService;
import org.olat.modules.taxonomy.manager.importexport.TaxonomyImportOptions;
import org.olat.modules.taxonomy.manager.importexport.TaxonomyImportResult;
import org.olat.modules.taxonomy.matching.TaxonomyMatchingService;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;

/**
 * Wizard to import a taxonomy file (ZIP, XLSX, CSV) as a new
 * taxonomy or into an existing taxonomy.
 * 
 * Initial date: 30 Sept 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class TaxonomyImportWizard {

	private static final Logger log = Tracing.createLoggerFor(TaxonomyImportWizard.class);
	private static final String CONTEXT_KEY = "taxonomyImportContext";

	private TaxonomyImportWizard() {
		//
	}

	/**
	 * @param target The taxonomy to import into or null to create a new taxonomy
	 */
	public static StepsMainRunController create(UserRequest ureq, WindowControl wControl, Taxonomy target) {
		TaxonomyImportContext context = new TaxonomyImportContext(target);
		Translator translator = Util.createPackageTranslator(TaxonomyUIFactory.class, ureq.getLocale());
		String title = translator.translate(target == null ? "import.taxonomy.new" : "import.taxonomy.levels");

		Step start = new ImportFileStep(ureq, context);
		StepsMainRunController wizard = new StepsMainRunController(ureq, wControl, start, new FinishCallback(context, translator),
				new CancelCallback(context), title, "o_sel_taxonomy_import_wizard");
		wizard.getRunContext().put(CONTEXT_KEY, context);
		return wizard;
	}

	/**
	 * Delete the extracted media files of the wizard. Call it when the wizard is
	 * disposed without finish or cancel, e.g. on logout or session timeout.
	 */
	public static void cleanUp(StepsMainRunController wizard) {
		if(wizard != null && wizard.getRunContext().get(CONTEXT_KEY) instanceof TaxonomyImportContext context) {
			context.cleanUp();
		}
	}

	static Translator getTranslator(UserRequest ureq) {
		return Util.createPackageTranslator(TaxonomyUIFactory.class, ureq.getLocale());
	}

	private static class FinishCallback implements StepRunnerCallback {

		private final TaxonomyImportContext context;
		private final Translator translator;

		public FinishCallback(TaxonomyImportContext context, Translator translator) {
			this.context = context;
			this.translator = translator;
		}

		@Override
		public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
			TaxonomyImportExportService importExportService = CoreSpringFactory.getImpl(TaxonomyImportExportService.class);
			TaxonomyImportOptions options = new TaxonomyImportOptions(context.getIdentifier(), context.getDisplayName(),
					context.getMode(), ureq.getIdentity());
			try {
				TaxonomyImportResult result = importExportService.importData(context.getData(), context.getTarget(), options);
				CoreSpringFactory.getImpl(TaxonomyMatchingService.class).startIndexing();
				wControl.setInfo(translator.translate("import.wizard.done", Integer.toString(result.getCreatedTypes()),
						Integer.toString(result.getCreatedLevels()), Integer.toString(result.getUpdatedTypes()),
						Integer.toString(result.getUpdatedLevels())));
			} catch(Exception e) {
				log.error("Taxonomy import failed", e);
				wControl.setError(translator.translate("import.wizard.failed"));
			} finally {
				context.cleanUp();
			}
			return StepsMainRunController.DONE_MODIFIED;
		}
	}

	private static class CancelCallback implements StepRunnerCallback {

		private final TaxonomyImportContext context;

		public CancelCallback(TaxonomyImportContext context) {
			this.context = context;
		}

		@Override
		public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
			context.cleanUp();
			return Step.NOSTEP;
		}
	}
}
