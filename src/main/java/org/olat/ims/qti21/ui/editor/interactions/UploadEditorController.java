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
package org.olat.ims.qti21.ui.editor.interactions;

import java.io.File;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.interactions.UploadAssessmentItemBuilder;
import org.olat.ims.qti21.ui.editor.AssessmentTestEditorController;
import org.olat.ims.qti21.ui.editor.events.AssessmentItemEvent;

/**
 * 
 * Initial date: 08.01.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UploadEditorController extends FormBasicController {

	private TextElement titleEl;
	private RichTextElement textEl;

	private final File itemFile;
	private final File rootDirectory;
	private final VFSContainer rootContainer;
	
	private final UploadAssessmentItemBuilder itemBuilder;
	
	public UploadEditorController(UserRequest ureq, WindowControl wControl, UploadAssessmentItemBuilder itemBuilder,
			File rootDirectory, VFSContainer rootContainer, File itemFile) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(AssessmentTestEditorController.class, getLocale()));
		this.itemFile = itemFile;
		this.itemBuilder = itemBuilder;
		this.rootDirectory = rootDirectory;
		this.rootContainer = rootContainer;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormContextHelp("detail#details_testeditor_fragetypen_fileupload");
		
		titleEl = uifactory.addTextElement("title", "form.imd.title", -1, itemBuilder.getTitle(), formLayout);
		titleEl.setMandatory(true);
		
		String relativePath = rootDirectory.toPath().relativize(itemFile.toPath().getParent()).toString();
		VFSContainer itemContainer = (VFSContainer)rootContainer.resolve(relativePath);

		String description = itemBuilder.getQuestion();
		textEl = uifactory.addRichTextElementForQTI21("desc", "form.imd.descr", description, 16, -1, itemContainer,
				formLayout, ureq.getUserSession(), getWindowControl());

		// Submit Button
		FormLayoutContainer buttonsContainer = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsContainer.setRootForm(mainForm);
		formLayout.add(buttonsContainer);
		uifactory.addFormSubmitButton("submit", buttonsContainer);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;

		titleEl.clearError();
		if(!StringHelper.containsNonWhitespace(titleEl.getValue())) {
			titleEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}

		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//title
		itemBuilder.setTitle(titleEl.getValue());

		//question
		String questionText = textEl.getRawValue();
		itemBuilder.setQuestion(questionText);
		
		fireEvent(ureq, new AssessmentItemEvent(AssessmentItemEvent.ASSESSMENT_ITEM_CHANGED, itemBuilder.getAssessmentItem(), QTI21QuestionType.upload));
	}
}