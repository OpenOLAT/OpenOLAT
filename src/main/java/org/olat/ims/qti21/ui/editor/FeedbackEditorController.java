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
	
	private TextElement feedbackCorrectTitleEl, feedbackIncorrectTitleEl, feedbackEmptyTitleEl;
	private RichTextElement feedbackCorrectTextEl, feedbackIncorrectTextEl, feedbackEmptyTextEl;

	private final boolean restrictedEdit;
	private final boolean empty, correct, incorrect;
	private final AssessmentItemBuilder itemBuilder;
	
	public FeedbackEditorController(UserRequest ureq, WindowControl wControl, AssessmentItemBuilder itemBuilder,
			boolean empty, boolean correct, boolean incorrect, boolean restrictedEdit) {
		super(ureq, wControl);
		this.empty = empty;
		this.correct = correct;
		this.incorrect = incorrect;
		this.itemBuilder = itemBuilder;
		this.restrictedEdit = restrictedEdit;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		ModalFeedbackBuilder hint = itemBuilder.getHint();
		String hintTitle = hint == null ? "" : hint.getTitle();
		hintTitleEl = uifactory.addTextElement("hintTitle", "form.imd.hint.title", -1, hintTitle, formLayout);
		hintTitleEl.setUserObject(hint);
		hintTitleEl.setEnabled(!restrictedEdit);
		String hintText = hint == null ? "" : hint.getText();
		hintTextEl = uifactory.addRichTextElementForStringDataCompact("hintText", "form.imd.hint.text", hintText, 8, -1, null,
				formLayout, ureq.getUserSession(), getWindowControl());
		hintTextEl.setEnabled(!restrictedEdit);
		RichTextConfiguration hintConfig = hintTextEl.getEditorConfiguration();
		hintConfig.setFileBrowserUploadRelPath("media");// set upload dir to the media dir
		
		//correct feedback
		if(correct) {
			ModalFeedbackBuilder correctFeedback = itemBuilder.getCorrectFeedback();
			String correctTitle = correctFeedback == null ? "" : correctFeedback.getTitle();
			feedbackCorrectTitleEl = uifactory.addTextElement("correctTitle", "form.imd.correct.title", -1, correctTitle, formLayout);
			feedbackCorrectTitleEl.setUserObject(correctFeedback);
			feedbackCorrectTitleEl.setEnabled(!restrictedEdit);
			String correctText = correctFeedback == null ? "" : correctFeedback.getText();
			feedbackCorrectTextEl = uifactory.addRichTextElementForStringDataCompact("correctText", "form.imd.correct.text", correctText, 8, -1, null,
					formLayout, ureq.getUserSession(), getWindowControl());
			feedbackCorrectTextEl.setEnabled(!restrictedEdit);
			RichTextConfiguration richTextConfig = feedbackCorrectTextEl.getEditorConfiguration();
			richTextConfig.setFileBrowserUploadRelPath("media");// set upload dir to the media dir
		}
		
		if(empty) {
			ModalFeedbackBuilder emptyFeedback = itemBuilder.getEmptyFeedback();
			String emptyTitle = emptyFeedback == null ? "" : emptyFeedback.getTitle();
			feedbackEmptyTitleEl = uifactory.addTextElement("emptyTitle", "form.imd.empty.title", -1, emptyTitle, formLayout);
			feedbackEmptyTitleEl.setUserObject(emptyFeedback);
			feedbackEmptyTitleEl.setEnabled(!restrictedEdit);
			String emptyText = emptyFeedback == null ? "" : emptyFeedback.getText();
			feedbackEmptyTextEl = uifactory.addRichTextElementForStringDataCompact("emptyText", "form.imd.empty.text", emptyText, 8, -1, null,
					formLayout, ureq.getUserSession(), getWindowControl());
			feedbackEmptyTextEl.setEnabled(!restrictedEdit);
			RichTextConfiguration richTextConfig = feedbackEmptyTextEl.getEditorConfiguration();
			richTextConfig.setFileBrowserUploadRelPath("media");// set upload dir to the media dir
		}

		//incorrect feedback
		if(incorrect) {
			ModalFeedbackBuilder incorrectFeedback = itemBuilder.getIncorrectFeedback();
			String incorrectTitle = incorrectFeedback == null ? "" : incorrectFeedback.getTitle();
			feedbackIncorrectTitleEl = uifactory.addTextElement("incorrectTitle", "form.imd.incorrect.title", -1, incorrectTitle, formLayout);
			feedbackIncorrectTitleEl.setUserObject(incorrectFeedback);
			feedbackIncorrectTitleEl.setEnabled(!restrictedEdit);
			String incorrectText = incorrectFeedback == null ? "" : incorrectFeedback.getText();
			feedbackIncorrectTextEl = uifactory.addRichTextElementForStringDataCompact("incorrectText", "form.imd.incorrect.text", incorrectText, 8, -1, null,
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
		
		if(correct) {
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
		} else {
			itemBuilder.removeCorrectFeedback();
		}
		
		if(empty) {
			String emptyTitle = feedbackEmptyTitleEl.getValue();
			String emptyText = feedbackEmptyTextEl.getValue();
			if(StringHelper.containsNonWhitespace(FilterFactory.getHtmlTagsFilter().filter(emptyText))) {
				ModalFeedbackBuilder emptyBuilder = itemBuilder.getEmptyFeedback();
				if(emptyBuilder == null) {
					emptyBuilder = itemBuilder.createEmptyFeedback();
				}
				emptyBuilder.setTitle(emptyTitle);
				emptyBuilder.setText(emptyText);
			} else {
				itemBuilder.removeEmptyFeedback();
			}
		} else {
			itemBuilder.removeEmptyFeedback();	
		}
		
		if(incorrect) {
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
