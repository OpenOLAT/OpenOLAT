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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
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
	
	private TextElement feedbackPassedTitleEl, feedbackFailedTitleEl;
	private RichTextElement feedbackPassedTextEl, feedbackFailedTextEl;

	private AssessmentTestBuilder testBuilder;
	
	public AssessmentTestFeedbackEditorController(UserRequest ureq, WindowControl wControl, AssessmentTestBuilder testBuilder) {
		super(ureq, wControl);
		this.testBuilder = testBuilder;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//correct feedback
		TestFeedbackBuilder passedFeedback = testBuilder.getPassedFeedback();
		String passedTitle = passedFeedback == null ? "" : passedFeedback.getTitle();
		feedbackPassedTitleEl = uifactory.addTextElement("correctTitle", "form.test.correct.title", -1, passedTitle, formLayout);
		feedbackPassedTitleEl.setUserObject(passedFeedback);
		String passedText = passedFeedback == null ? "" : passedFeedback.getText();
		feedbackPassedTextEl = uifactory.addRichTextElementForStringData("correctText", "form.test.correct.text", passedText, 8, -1, true, null, null,
				formLayout, ureq.getUserSession(), getWindowControl());
		feedbackPassedTextEl.getEditorConfiguration().setFileBrowserUploadRelPath("media");

		//incorrect feedback
		TestFeedbackBuilder failedFeedback = testBuilder.getFailedFeedback();
		String failedTitle = failedFeedback == null ? "" : failedFeedback.getTitle();
		feedbackFailedTitleEl = uifactory.addTextElement("incorrectTitle", "form.test.incorrect.title", -1, failedTitle, formLayout);
		feedbackFailedTitleEl.setUserObject(failedFeedback);
		String fialedText = failedFeedback == null ? "" : failedFeedback.getText();
		feedbackFailedTextEl = uifactory.addRichTextElementForStringData("incorrectText", "form.test.incorrect.text", fialedText, 8, -1, true, null, null,
				formLayout, ureq.getUserSession(), getWindowControl());
		feedbackFailedTextEl.getEditorConfiguration().setFileBrowserUploadRelPath("media");
	
		// Submit Button
		FormLayoutContainer buttonsContainer = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsContainer.setRootForm(mainForm);
		formLayout.add(buttonsContainer);
		uifactory.addFormSubmitButton("submit", buttonsContainer);
	}

	@Override
	protected void formOK(UserRequest ureq) {

		String passedTitle = feedbackPassedTitleEl.getValue();
		String passedText = feedbackPassedTextEl.getValue();
		if(StringHelper.containsNonWhitespace(FilterFactory.getHtmlTagsFilter().filter(passedText))) {
			TestFeedbackBuilder passedBuilder = testBuilder.getPassedFeedback();
			if(passedBuilder == null) {
				passedBuilder = testBuilder.createPassedFeedback();
			}
			passedBuilder.setTitle(passedTitle);
			passedBuilder.setText(passedText);
		}
		
		String failedTitle = feedbackFailedTitleEl.getValue();
		String failedText = feedbackFailedTextEl.getValue();
		if(StringHelper.containsNonWhitespace(FilterFactory.getHtmlTagsFilter().filter(failedText))) {
			TestFeedbackBuilder failedBuilder = testBuilder.getFailedFeedback();
			if(failedBuilder == null) {
				failedBuilder = testBuilder.createFailedFeedback();
			}
			failedBuilder.setTitle(failedTitle);
			failedBuilder.setText(failedText);
		}
		
		fireEvent(ureq, AssessmentTestEvent.ASSESSMENT_TEST_CHANGED_EVENT);
	}

	@Override
	protected void doDispose() {
		//
	}
}
