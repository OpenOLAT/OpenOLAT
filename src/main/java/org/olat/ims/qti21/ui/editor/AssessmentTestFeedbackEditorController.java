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
package org.olat.ims.qti21.ui.editor;

import java.io.File;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.TextMode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.ims.qti21.model.xml.AssessmentTestBuilder;
import org.olat.ims.qti21.model.xml.TestFeedbackBuilder;
import org.olat.ims.qti21.ui.editor.events.AssessmentTestEvent;

/**
 * 
 * Initial date: 09.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentTestFeedbackEditorController extends FormBasicController {
	
	private FormSubmit saveButton;
	private TextElement feedbackPassedTitleEl, feedbackFailedTitleEl;
	private RichTextElement feedbackPassedTextEl, feedbackFailedTextEl;

	private final File testFile;
	private final File rootDirectory;
	private final VFSContainer rootContainer;
	
	private final boolean restrictedEdit;
	private final AssessmentTestBuilder testBuilder;
	
	public AssessmentTestFeedbackEditorController(UserRequest ureq, WindowControl wControl, AssessmentTestBuilder testBuilder,
			File rootDirectory, VFSContainer rootContainer, File testFile, boolean restrictedEdit) {
		super(ureq, wControl);
		this.testFile = testFile;
		this.rootDirectory = rootDirectory;
		this.rootContainer = rootContainer;
		this.testBuilder = testBuilder;
		this.restrictedEdit = restrictedEdit;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormContextHelp("Test editor QTI 2.1 in detail#details_testeditor_feedback");
		
		
		String relativePath = rootDirectory.toPath().relativize(testFile.toPath().getParent()).toString();
		VFSContainer itemContainer = (VFSContainer)rootContainer.resolve(relativePath);
		
		//correct feedback
		TestFeedbackBuilder passedFeedback = testBuilder.getPassedFeedback();
		String passedTitle = passedFeedback == null ? "" : passedFeedback.getTitle();
		feedbackPassedTitleEl = uifactory.addTextElement("correctTitle", "form.test.correct.title", -1, passedTitle, formLayout);
		feedbackPassedTitleEl.setUserObject(passedFeedback);
		feedbackPassedTitleEl.setEnabled(!restrictedEdit);
		String passedText = passedFeedback == null ? "" : passedFeedback.getText();
		feedbackPassedTextEl = uifactory.addRichTextElementForQTI21("correctText", "form.test.correct.text", passedText, 8, -1,
				itemContainer, formLayout, ureq.getUserSession(), getWindowControl());
		feedbackPassedTextEl.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.oneLine);
		feedbackPassedTextEl.setEnabled(!restrictedEdit);
		feedbackPassedTextEl.getEditorConfiguration().setFileBrowserUploadRelPath("media");

		//incorrect feedback
		TestFeedbackBuilder failedFeedback = testBuilder.getFailedFeedback();
		String failedTitle = failedFeedback == null ? "" : failedFeedback.getTitle();
		feedbackFailedTitleEl = uifactory.addTextElement("incorrectTitle", "form.test.incorrect.title", -1, failedTitle, formLayout);
		feedbackFailedTitleEl.setUserObject(failedFeedback);
		feedbackFailedTitleEl.setEnabled(!restrictedEdit);
		String fialedText = failedFeedback == null ? "" : failedFeedback.getText();
		feedbackFailedTextEl = uifactory.addRichTextElementForQTI21("incorrectText", "form.test.incorrect.text", fialedText, 8, -1,
				itemContainer, formLayout, ureq.getUserSession(), getWindowControl());
		feedbackFailedTextEl.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.oneLine);
		feedbackFailedTextEl.setEnabled(!restrictedEdit);
		feedbackFailedTextEl.getEditorConfiguration().setFileBrowserUploadRelPath("media");
	
		// Submit Button
		FormLayoutContainer buttonsContainer = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsContainer.setRootForm(mainForm);
		formLayout.add(buttonsContainer);
		saveButton = uifactory.addFormSubmitButton("submit", buttonsContainer);
		
		sync();//check the cut value
	}
	
	public void sync() {
		boolean hasCutValue = testBuilder.getCutValue() != null;
		saveButton.setVisible(hasCutValue);
		feedbackPassedTitleEl.setVisible(hasCutValue);
		feedbackPassedTextEl.setVisible(hasCutValue);
		feedbackFailedTitleEl.setVisible(hasCutValue);
		feedbackFailedTextEl.setVisible(hasCutValue);
		
		if(hasCutValue) {
			setFormWarning(null);
		} else {
			setFormWarning("warning.feedback.cutvalue");
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {

		String passedTitle = feedbackPassedTitleEl.getValue();
		String passedText = feedbackPassedTextEl.getRawValue();
		TestFeedbackBuilder passedBuilder = testBuilder.getPassedFeedback();
		if(StringHelper.containsNonWhitespace(FilterFactory.getHtmlTagsFilter().filter(passedText))) {
			if(passedBuilder == null) {
				passedBuilder = testBuilder.createPassedFeedback();
			}
			passedBuilder.setTitle(passedTitle);
			passedBuilder.setText(passedText);
		} else if(passedBuilder != null) {
			passedBuilder.setTitle(null);
			passedBuilder.setText(null);
		}
		
		String failedTitle = feedbackFailedTitleEl.getValue();
		String failedText = feedbackFailedTextEl.getRawValue();
		TestFeedbackBuilder failedBuilder = testBuilder.getFailedFeedback();
		if(StringHelper.containsNonWhitespace(FilterFactory.getHtmlTagsFilter().filter(failedText))) {
			if(failedBuilder == null) {
				failedBuilder = testBuilder.createFailedFeedback();
			}
			failedBuilder.setTitle(failedTitle);
			failedBuilder.setText(failedText);
		} else if(failedBuilder != null) {
			failedBuilder.setTitle(null);
			failedBuilder.setText(null);
		}
		
		fireEvent(ureq, AssessmentTestEvent.ASSESSMENT_TEST_CHANGED_EVENT);
	}
}
