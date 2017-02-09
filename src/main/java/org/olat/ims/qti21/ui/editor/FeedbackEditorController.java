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
import org.olat.core.gui.components.form.flexible.impl.elements.richText.RichTextConfiguration;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.ModalFeedbackBuilder;
import org.olat.ims.qti21.ui.editor.events.AssessmentItemEvent;

/**
 * 
 * Initial date: 09.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FeedbackEditorController extends FormBasicController {
	
	private TextElement hintTitleEl;
	private RichTextElement hintTextEl;
	private TextElement feedbackCorrectSolutionTitleEl;
	private RichTextElement feedbackCorrectSolutionTextEl;
	private TextElement feedbackCorrectTitleEl, feedbackIncorrectTitleEl;
	private RichTextElement feedbackCorrectTextEl, feedbackIncorrectTextEl;
	
	private final boolean restrictedEdit;
	private final AssessmentItemBuilder itemBuilder;
	
	public FeedbackEditorController(UserRequest ureq, WindowControl wControl, AssessmentItemBuilder itemBuilder,
			boolean restrictedEdit) {
		super(ureq, wControl, LAYOUT_DEFAULT_2_10);
		this.itemBuilder = itemBuilder;
		this.restrictedEdit = restrictedEdit;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormContextHelp("Test editor QTI 2.1 in detail#details_testeditor_feedback");

		{//hint
			ModalFeedbackBuilder hint = itemBuilder.getHint();
			String hintTitle = hint == null ? "" : hint.getTitle();
			hintTitleEl = uifactory.addTextElement("hintTitle", "form.imd.hint.title", -1, hintTitle, formLayout);
			hintTitleEl.setUserObject(hint);
			hintTitleEl.setEnabled(!restrictedEdit);
			String hintText = hint == null ? "" : hint.getText();
			hintTextEl = uifactory.addRichTextElementForQTI21("hintText", "form.imd.hint.text", hintText, 8, -1, null,
					formLayout, ureq.getUserSession(), getWindowControl());
			hintTextEl.setEnabled(!restrictedEdit);
			RichTextConfiguration hintConfig = hintTextEl.getEditorConfiguration();
			hintConfig.setFileBrowserUploadRelPath("media");// set upload dir to the media dir
		}

		{//correct solution feedback
			ModalFeedbackBuilder correctSolutionFeedback = itemBuilder.getCorrectSolutionFeedback();
			String correctSolutionTitle = correctSolutionFeedback == null ? "" : correctSolutionFeedback.getTitle();
			feedbackCorrectSolutionTitleEl = uifactory.addTextElement("correctSolutionTitle", "form.imd.correct.solution.title", -1, correctSolutionTitle, formLayout);
			feedbackCorrectSolutionTitleEl.setUserObject(correctSolutionFeedback);
			feedbackCorrectSolutionTitleEl.setEnabled(!restrictedEdit);
			String correctSolutionText = correctSolutionFeedback == null ? "" : correctSolutionFeedback.getText();
			feedbackCorrectSolutionTextEl = uifactory.addRichTextElementForQTI21("correctSolutionText", "form.imd.correct.solution.text", correctSolutionText, 8, -1, null,
					formLayout, ureq.getUserSession(), getWindowControl());
			feedbackCorrectSolutionTextEl.setEnabled(!restrictedEdit);
			RichTextConfiguration richTextConfig2 = feedbackCorrectSolutionTextEl.getEditorConfiguration();
			richTextConfig2.setFileBrowserUploadRelPath("media");// set upload dir to the media dir
		}

		{//correct feedback
			ModalFeedbackBuilder correctFeedback = itemBuilder.getCorrectFeedback();
			String correctTitle = correctFeedback == null ? "" : correctFeedback.getTitle();
			feedbackCorrectTitleEl = uifactory.addTextElement("correctTitle", "form.imd.correct.title", -1, correctTitle, formLayout);
			feedbackCorrectTitleEl.setUserObject(correctFeedback);
			feedbackCorrectTitleEl.setEnabled(!restrictedEdit);
			String correctText = correctFeedback == null ? "" : correctFeedback.getText();
			feedbackCorrectTextEl = uifactory.addRichTextElementForQTI21("correctText", "form.imd.correct.text", correctText, 8, -1, null,
					formLayout, ureq.getUserSession(), getWindowControl());
			feedbackCorrectTextEl.setEnabled(!restrictedEdit);
			RichTextConfiguration richTextConfig = feedbackCorrectTextEl.getEditorConfiguration();
			richTextConfig.setFileBrowserUploadRelPath("media");// set upload dir to the media dir
		}

		//incorrect feedback
		{
			ModalFeedbackBuilder incorrectFeedback = itemBuilder.getIncorrectFeedback();
			String incorrectTitle = incorrectFeedback == null ? "" : incorrectFeedback.getTitle();
			feedbackIncorrectTitleEl = uifactory.addTextElement("incorrectTitle", "form.imd.incorrect.title", -1, incorrectTitle, formLayout);
			feedbackIncorrectTitleEl.setUserObject(incorrectFeedback);
			feedbackIncorrectTitleEl.setEnabled(!restrictedEdit);
			String incorrectText = incorrectFeedback == null ? "" : incorrectFeedback.getText();
			feedbackIncorrectTextEl = uifactory.addRichTextElementForQTI21("incorrectText", "form.imd.incorrect.text", incorrectText, 8, -1, null,
					formLayout, ureq.getUserSession(), getWindowControl());
			feedbackIncorrectTextEl.setEnabled(!restrictedEdit);
			RichTextConfiguration richTextConfig2 = feedbackIncorrectTextEl.getEditorConfiguration();
			richTextConfig2.setFileBrowserUploadRelPath("media");// set upload dir to the media dir
		}

	
		// Submit Button
		if(!restrictedEdit) {
			FormLayoutContainer buttonsContainer = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			buttonsContainer.setRootForm(mainForm);
			formLayout.add(buttonsContainer);
			uifactory.addFormSubmitButton("submit", buttonsContainer);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(restrictedEdit) return;
		
		String hintTitle = hintTitleEl.getValue();
		String hintText = hintTextEl.getValue();
		if(StringHelper.containsNonWhitespace(FilterFactory.getHtmlTagsFilter().filter(hintText))) {
			ModalFeedbackBuilder hintBuilder = itemBuilder.getHint();
			if(hintBuilder == null) {
				hintBuilder = itemBuilder.createHint();
			}
			hintBuilder.setTitle(hintTitle);
			hintBuilder.setText(hintText);
		} else {
			itemBuilder.removeHint();
		}
		
		String correctSolutionTitle = feedbackCorrectSolutionTitleEl.getValue();
		String correctSolutionText = feedbackCorrectSolutionTextEl.getValue();
		if(StringHelper.containsNonWhitespace(FilterFactory.getHtmlTagsFilter().filter(correctSolutionText))) {
			ModalFeedbackBuilder correctSolutionBuilder = itemBuilder.getCorrectSolutionFeedback();
			if(correctSolutionBuilder == null) {
				correctSolutionBuilder = itemBuilder.createCorrectSolutionFeedback();
			}
			correctSolutionBuilder.setTitle(correctSolutionTitle);
			correctSolutionBuilder.setText(correctSolutionText);
		} else {
			itemBuilder.removeCorrectSolutionFeedback();
		}
		
		String correctTitle = feedbackCorrectTitleEl.getValue();
		String correctText = feedbackCorrectTextEl.getValue();
		if(StringHelper.containsNonWhitespace(FilterFactory.getHtmlTagsFilter().filter(correctText))) {
			ModalFeedbackBuilder correctBuilder = itemBuilder.getCorrectFeedback();
			if(correctBuilder == null) {
				correctBuilder = itemBuilder.createCorrectFeedback();
			}
			correctBuilder.setTitle(correctTitle);
			correctBuilder.setText(correctText);
		} else {
			itemBuilder.removeCorrectFeedback();
		}

		String incorrectTitle = feedbackIncorrectTitleEl.getValue();
		String incorrectText = feedbackIncorrectTextEl.getValue();
		if(StringHelper.containsNonWhitespace(FilterFactory.getHtmlTagsFilter().filter(incorrectText))) {
			ModalFeedbackBuilder incorrectBuilder = itemBuilder.getIncorrectFeedback();
			if(incorrectBuilder == null) {
				incorrectBuilder = itemBuilder.createIncorrectFeedback();
			}
			incorrectBuilder.setTitle(incorrectTitle);
			incorrectBuilder.setText(incorrectText);
		} else {
			itemBuilder.removeIncorrectFeedback();
		}

		fireEvent(ureq, new AssessmentItemEvent(AssessmentItemEvent.ASSESSMENT_ITEM_CHANGED, itemBuilder.getAssessmentItem()));
	}
	
	@Override
	protected void doDispose() {
		//
	}
}
