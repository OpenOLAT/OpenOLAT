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
package org.olat.ims.qti21.questionimport;

import java.io.InputStream;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.StreamedMediaResource;
import org.olat.ims.qti21.QTI21Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TextInputController extends StepFormBasicController {

	private String validatedInp;
	private TextElement inputElement;
	
	private final AssessmentItemsPackage importedItems;
	private final ImportOptions options;
	
	@Autowired
	private QTI21Service qtiService;
	
	public TextInputController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form rootForm,
			AssessmentItemsPackage importedItems, ImportOptions options) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		this.importedItems = importedItems;
		this.options = options;
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("wizard.import.input.description");
		setFormContextHelp("manual_user/question_bank/Data_Management/#import");
		
		FormLayoutContainer textContainer = FormLayoutContainer.createCustomFormLayout("index", getTranslator(), velocity_root + "/example.html");
		formLayout.add(textContainer);
		String mapperURI = registerMapper(ureq, new ExampleMapper());
		textContainer.contextPut("mapperURI", mapperURI);
		
		inputElement = uifactory.addTextAreaElement("importform", "form.importdata", -1, 10, 100, false, false, "", formLayout);
		inputElement.setMandatory(true);
		inputElement.setNotEmptyCheck("form.legende.mandatory");
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		String inp = inputElement.getValue();
		if(validatedInp == null || !validatedInp.equals(inp)) {
			CSVToAssessmentItemConverter converter = new CSVToAssessmentItemConverter(options, getLocale(), qtiService.qtiSerializer());
			try {
				converter.parse(inputElement.getValue());
				List<AssessmentItemAndMetadata> items = converter.getItems();
				if(items == null || items.isEmpty()) {
					inputElement.setErrorKey("form.mandatory.hover", null);
					allOk &= false;
				}
			} catch (Exception e) {
				inputElement.setErrorKey("error.at.line", new String[] { Integer.toString(converter.getCurrentLine()) });
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		CSVToAssessmentItemConverter converter = new CSVToAssessmentItemConverter(options, getLocale(), qtiService.qtiSerializer());
		converter.parse(inputElement.getValue());
		importedItems.setItems(converter.getItems());
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	private static class ExampleMapper implements Mapper {
		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			InputStream in = TextInputController.class.getResourceAsStream("qti-import-metadata.xlsx");
			return new StreamedMediaResource(in, "ImportExample.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		}
	}
}