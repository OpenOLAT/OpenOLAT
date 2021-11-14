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
package org.olat.modules.lecture.ui.blockimport;

import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

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
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.ui.LectureListRepositoryController;
import org.olat.repository.RepositoryEntry;
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
	
	private final RepositoryEntry entry;
	private List<LectureBlock> currentBlocks;
	private ImportedLectureBlocks importedBlocks;
	
	@Autowired
	private LectureService lectureService;
	
	public TextInputController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry,
			ImportedLectureBlocks importedBlocks, StepsRunContext runContext, Form rootForm) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		setTranslator(Util.createPackageTranslator(LectureListRepositoryController.class, getLocale(), getTranslator()));
		this.entry = entry;
		this.importedBlocks = importedBlocks;
		currentBlocks = lectureService.getLectureBlocks(entry);
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("wizard.import.input.description");
		
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
			BlockConverter converter = new BlockConverter(entry, currentBlocks, getLocale());
			StringBuilder errors = new StringBuilder();
			try {
				converter.parse(inputElement.getValue());
				List<ImportedLectureBlock> blocks = converter.getImportedLectureBlocks();
				if(blocks == null || blocks.isEmpty()) {
					inputElement.setErrorKey("form.mandatory.hover", null);
					allOk &= false;
				} else {
					for(ImportedLectureBlock block:blocks) {
						validate(block, errors);
					}
				}
			} catch (Exception e) {
				logError("", e);
				inputElement.setErrorKey("error.at.line", new String[] { Integer.toString(converter.getCurrentLine()) });
				allOk &= false;
			}
			
			if(errors.length() > 0) {
				inputElement.setErrorKey("errors.at", new String[] { errors.toString() });
				allOk &= false;
			}
		}
		
		return allOk;
	}
	
	private void validate(ImportedLectureBlock importedBlock, StringBuilder errors) {
		LectureBlock lectureBlock = importedBlock.getLectureBlock();
		if(!StringHelper.containsNonWhitespace(lectureBlock.getTitle())) {
			errorFieldMandatory(errors, "lecture.title", importedBlock.getLine());
		}
		if(lectureBlock.getStartDate() == null) {
			errorFieldMandatory(errors, "lecture.start", importedBlock.getLine());
		}
		if(lectureBlock.getEndDate() == null) {
			errorFieldMandatory(errors, "lecture.end", importedBlock.getLine());
		}
	}

	private void errorFieldMandatory(StringBuilder errors, String filedI18nKey, int line) {
		String error = translate("error.mandatory.at.line", new String[] { translate(filedI18nKey), Integer.toString(line) });
		if(errors.length() > 0) errors.append("<br>");
		errors.append(error);
	}
		
	@Override
	protected void formOK(UserRequest ureq) {
		String inp = inputElement.getValue();
		BlockConverter converter = new BlockConverter(entry, currentBlocks, getLocale());
		converter.parse(inp);
		importedBlocks.setLectureBlocks(converter.getImportedLectureBlocks());
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	private static class ExampleMapper implements Mapper {
		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			InputStream in = TextInputController.class.getResourceAsStream("lecture-block-import-template.xlsx");
			return new StreamedMediaResource(in, "ImportLectureBlockExample.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		}
	}
}