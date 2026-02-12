/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui.importwizard;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.StreamedMediaResource;

/**
 * 
 * Initial date: 6 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImportCurriculumsFileController extends StepFormBasicController {
	
	private FileElement importFileEl;
	
	private final ImportCurriculumsContext context;
	
	public ImportCurriculumsFileController(UserRequest ureq, WindowControl wControl, Form rootForm,
			ImportCurriculumsContext context, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "import_curriculums_file");
		this.context = context;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		importFileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "import.file", formLayout);
		importFileEl.setMandatory(true);
		importFileEl.setFormLayout("vertical");
		
		FormLayoutContainer textContainer = FormLayoutContainer.createCustomFormLayout("import.example", getTranslator(), velocity_root + "/example.html");
		textContainer.setLabel("import.example.label", null);
		formLayout.add(textContainer);
		String mapperURI = registerMapper(ureq, new ExampleMapper());
		textContainer.contextPut("mapperURI", mapperURI);
	}

	@Override
	protected void formNext(UserRequest ureq) {
		loadFile();
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		importFileEl.clearError();
		if(importFileEl.getUploadFile() == null) {
			importFileEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void loadFile() {
		File file = importFileEl.getUploadFile();
		if(file != null) {
			List<CurriculumImportedRow> rows = new ImportCurriculumsHelper(getTranslator()).loadFile(file);
			context.setImportedRows(rows);
		}
	}
	
	private static class ExampleMapper implements Mapper {
		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			InputStream in = ImportCurriculumsFileController.class.getResourceAsStream("products-example.xlsx");
			return new StreamedMediaResource(in, "ProductsExample.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		}
	}
}
