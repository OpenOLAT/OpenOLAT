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
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.RichTextConfiguration;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.RichTextConfigurationDelegate;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.interactions.HottextAssessmentItemBuilder;
import org.olat.ims.qti21.ui.editor.AssessmentTestEditorController;
import org.olat.ims.qti21.ui.editor.events.AssessmentItemEvent;

import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 16 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class HottextEditorController extends FormBasicController {
	
	private TextElement titleEl;
	private RichTextElement textEl;

	private final File itemFile;
	private final File rootDirectory;
	private final VFSContainer rootContainer;
	private final boolean restrictedEdit, readOnly;
	private final HottextAssessmentItemBuilder itemBuilder;
	
	public HottextEditorController(UserRequest ureq, WindowControl wControl, HottextAssessmentItemBuilder itemBuilder,
			File rootDirectory, VFSContainer rootContainer, File itemFile, boolean restrictedEdit, boolean readOnly) {
		super(ureq, wControl, LAYOUT_DEFAULT_2_10);
		setTranslator(Util.createPackageTranslator(AssessmentTestEditorController.class, getLocale()));
		this.itemFile = itemFile;
		this.itemBuilder = itemBuilder;
		this.rootDirectory = rootDirectory;
		this.rootContainer = rootContainer;
		this.readOnly = readOnly;
		this.restrictedEdit = restrictedEdit;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormContextHelp("Configure test questions");
	
		titleEl = uifactory.addTextElement("title", "form.imd.title", -1, itemBuilder.getTitle(), formLayout);
		titleEl.setElementCssClass("o_sel_assessment_item_title");
		titleEl.setMandatory(true);
		titleEl.setEnabled(!readOnly);
		
		String relativePath = rootDirectory.toPath().relativize(itemFile.toPath().getParent()).toString();
		VFSContainer itemContainer = (VFSContainer)rootContainer.resolve(relativePath);
		
		String question = itemBuilder.getQuestion();
		textEl = uifactory.addRichTextElementForQTI21("desc", "form.imd.descr", question, 16, -1, itemContainer,
				formLayout, ureq.getUserSession(),  getWindowControl());
		textEl.setElementCssClass("o_sel_assessment_item_hottext_text");
		textEl.addActionListener(FormEvent.ONCLICK);
		RichTextConfiguration richTextConfig = textEl.getEditorConfiguration();
		richTextConfig.enableQTITools(false, false, true);
		richTextConfig.setAdditionalConfiguration(new CorrectAnswersConfiguration());
		richTextConfig.setReadOnly(restrictedEdit || readOnly);
		
		// Submit Button
		FormLayoutContainer buttonsContainer = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsContainer.setElementCssClass("o_sel_hottext_save");
		buttonsContainer.setRootForm(mainForm);
		buttonsContainer.setVisible(!readOnly);
		formLayout.add(buttonsContainer);
		uifactory.addFormSubmitButton("submit", buttonsContainer);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(textEl == source) {
			String cmd = event.getCommand();
			if("hottext".equals(cmd)) {
				String identifier = ureq.getParameter("identifier");
				String correct = ureq.getParameter("correct");
				doHottext(identifier, correct);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doHottext(String identifier, String correct) {
		if(StringHelper.containsNonWhitespace(identifier) && ("true".equals(correct) || "false".equals(correct))) {
			Identifier hottextIdentifier = Identifier.parseString(identifier);
			if("true".equals(correct)) {
				itemBuilder.addCorrectAnswer(hottextIdentifier);
			} else {
				itemBuilder.removeCorrectAnswer(hottextIdentifier);
			}
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		String questionText = textEl.getRawValue();
		if(!StringHelper.containsNonWhitespace(questionText)) {
			textEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if(!questionText.contains("<hottext")) {
			textEl.setErrorKey("error.missing.hottext", null);
			allOk &= false;
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(readOnly) return;
		//title
		itemBuilder.setTitle(titleEl.getValue());
		
		if(!restrictedEdit) {
			//set the question with the text entries
			String questionText = textEl.getRawValue();
			itemBuilder.setQuestion(questionText);
		}
		fireEvent(ureq, new AssessmentItemEvent(AssessmentItemEvent.ASSESSMENT_ITEM_CHANGED, itemBuilder.getAssessmentItem(), QTI21QuestionType.hottext));
	}
	
	private class CorrectAnswersConfiguration implements RichTextConfigurationDelegate {
		@Override
		public void appendConfigToTinyJSArray_4(StringOutput out, Translator translator) {
			List<Identifier> correctAnswers = itemBuilder.getCorrectAnswers();
			out.append("correctHottexts: [");
			for(int i=correctAnswers.size(); i-->0; ) {
				out.append("'").append(correctAnswers.get(i).toString()).append("'");
				if(i > 0) {
					out.append(",");
				}
			}
			out.append("],");
		}
	}
}
