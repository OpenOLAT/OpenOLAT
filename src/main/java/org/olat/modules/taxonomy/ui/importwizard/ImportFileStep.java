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

import java.io.File;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.output.CloseShieldOutputStream;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.modules.taxonomy.manager.importexport.FileLevel;
import org.olat.modules.taxonomy.manager.importexport.FileLevelType;
import org.olat.modules.taxonomy.manager.importexport.FileMessage;
import org.olat.modules.taxonomy.manager.importexport.TaxonomyFileData;
import org.olat.modules.taxonomy.manager.importexport.TaxonomyImportExportService;
import org.olat.modules.taxonomy.manager.importexport.TaxonomyFileWriter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Step 1: upload and read the file.
 * 
 * Initial date: 30 Sept 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class ImportFileStep extends BasicStep {
	
	private final TaxonomyImportContext context;
	
	public ImportFileStep(UserRequest ureq, TaxonomyImportContext context) {
		super(ureq);
		this.context = context;
		setTranslator(TaxonomyImportWizard.getTranslator(ureq));
		setI18nTitleAndDescr("import.wizard.file.title", null);
		if(context.isNewTaxonomy()) {
			setNextStep(new ImportTaxonomyStep(ureq, context));
		} else {
			setNextStep(new ImportReviewStep(ureq, context, false));
		}
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(false, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		return new ImportFileController(ureq, wControl, form, runContext, context);
	}
	
	private static class ImportFileController extends StepFormBasicController {
		
		private FileElement fileEl;
		private FormLink exampleLink;
		
		private File validatedFile;
		private final TaxonomyImportContext context;
		
		@Autowired
		private TaxonomyImportExportService importExportService;
		
		public ImportFileController(UserRequest ureq, WindowControl wControl, Form rootForm,
				StepsRunContext runContext, TaxonomyImportContext context) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
			setTranslator(TaxonomyImportWizard.getTranslator(ureq));
			this.context = context;
			initForm(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			setFormDescription("import.wizard.file.desc");
			
			exampleLink = uifactory.addFormLink("import.wizard.file.example", formLayout, Link.LINK);
			exampleLink.setIconLeftCSS("o_icon o_filetype_xls o_icon-lg");
			exampleLink.setLabel("import.wizard.file.example.label", null);
			
			fileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "import.wizard.file.label", formLayout);
			fileEl.setMandatory(true);
			fileEl.addActionListener(FormEvent.ONCHANGE);
		}

		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if(exampleLink == source) {
				doDownloadExample(ureq);
			} else if(fileEl == source) {
				validatedFile = null;
				fileEl.clearError();
			}
			super.formInnerEvent(ureq, source, event);
		}

		@Override
		protected boolean validateFormLogic(UserRequest ureq) {
			boolean allOk = super.validateFormLogic(ureq);
			
			fileEl.clearError();
			File file = fileEl.getUploadFile();
			if(file == null) {
				fileEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			} else if(!Objects.equals(validatedFile, file)) {
				TaxonomyFileData data = importExportService.read(file, fileEl.getUploadFileName());
				if(!data.hasErrors()) {
					importExportService.validate(data, context.getTarget());
				}
				context.setData(data);
				
				String error = getFileError(data);
				if(error != null) {
					fileEl.setErrorKey("noTransOnlyParam", error);
					allOk &= false;
				} else {
					validatedFile = file;
				}
			}
			return allOk;
		}
		
		private String getFileError(TaxonomyFileData data) {
			for(FileMessage message:data.getMessages()) {
				if(message.isError()) {
					return translate(message.i18nKey(), message.args());
				}
			}
			List<FileLevel> levels = data.getLevels();
			List<FileLevelType> types = data.getLevelTypes();
			if(levels.isEmpty() && types.isEmpty()) {
				return translate("import.wizard.nothing");
			}
			return null;
		}

		@Override
		protected void formNext(UserRequest ureq) {
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			//
		}
		
		private void doDownloadExample(UserRequest ureq) {
			TaxonomyFileData example = importExportService.createExampleData();
			TaxonomyFileWriter writer = new TaxonomyFileWriter(getTranslator());
			ureq.getDispatchResult().setResultingMediaResource(new OpenXMLWorkbookResource("taxonomy_example.xlsx") {
				@Override
				protected void generate(OutputStream out) {
					writer.writeWorkbook(example, CloseShieldOutputStream.wrap(out));
				}
			});
		}
	}
}
