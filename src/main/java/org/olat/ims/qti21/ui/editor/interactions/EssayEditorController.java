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
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.interactions.EssayAssessmentItemBuilder;
import org.olat.ims.qti21.ui.ResourcesMapper;
import org.olat.ims.qti21.ui.components.FlowFormItem;
import org.olat.ims.qti21.ui.editor.AssessmentTestEditorController;
import org.olat.ims.qti21.ui.editor.events.AssessmentItemEvent;

/**
 * 
 * Initial date: 08.01.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EssayEditorController extends FormBasicController {
	
	private TextElement titleEl;
	private TextElement lengthEl;
	private TextElement heightEl;
	private TextElement minWordsEl;
	private TextElement maxWordsEl;
	private TextElement placeholderEl;
	private RichTextElement textEl;
	private SingleSelection copyPasteEl;
	private SingleSelection textFormattingEl;

	private final File itemFile;
	private final File rootDirectory;
	private final VFSContainer rootContainer;
	
	private final boolean readOnly;
	private final boolean restrictedEdit;
	private final String mapperUri;
	private final EssayAssessmentItemBuilder itemBuilder;
	
	public EssayEditorController(UserRequest ureq, WindowControl wControl, EssayAssessmentItemBuilder itemBuilder,
			File rootDirectory, VFSContainer rootContainer, File itemFile, boolean restrictedEdit, boolean readOnly) {
		super(ureq, wControl, LAYOUT_DEFAULT_2_10);
		setTranslator(Util.createPackageTranslator(AssessmentTestEditorController.class, getLocale()));
		this.itemFile = itemFile;
		this.itemBuilder = itemBuilder;
		this.rootDirectory = rootDirectory;
		this.rootContainer = rootContainer;
		this.readOnly = readOnly;
		this.restrictedEdit = restrictedEdit;
		
		mapperUri = registerCacheableMapper(null, "EssayEditorController::" + CodeHelper.getRAMUniqueID(),
				new ResourcesMapper(itemFile.toURI(), rootDirectory));
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormContextHelp("manual_user/tests/Configure_test_questions/");
		
		titleEl = uifactory.addTextElement("title", "form.imd.title", -1, itemBuilder.getTitle(), formLayout);
		titleEl.setElementCssClass("o_sel_assessment_item_title");
		titleEl.setMandatory(true);
		titleEl.setEnabled(!readOnly);
		
		String relativePath = rootDirectory.toPath().relativize(itemFile.toPath().getParent()).toString();
		VFSContainer itemContainer = (VFSContainer)rootContainer.resolve(relativePath);

		String description = itemBuilder.getQuestion();
		textEl = uifactory.addRichTextElementForQTI21("desc", "form.imd.descr", description, 12, -1, itemContainer,
				formLayout, ureq.getUserSession(), getWindowControl());
		textEl.setElementCssClass("o_sel_assessment_item_question");
		textEl.setEnabled(!readOnly);
		textEl.setVisible(!readOnly);
		if(readOnly) {
			FlowFormItem textReadOnlyEl = new FlowFormItem("descro", itemFile);
			textReadOnlyEl.setLabel("form.imd.descr", null);
			textReadOnlyEl.setBlocks(itemBuilder.getQuestionBlocks());
			textReadOnlyEl.setMapperUri(mapperUri);
			formLayout.add(textReadOnlyEl);
		}
		
		//copy/paste
		SelectionValues modeKeys = new SelectionValues();
		modeKeys.add(SelectionValues.entry("standard", translate("essay.formating.standard")));
		modeKeys.add(SelectionValues.entry("rich", translate("essay.formating.rich")));
		textFormattingEl = uifactory.addRadiosHorizontal("essay.formating", "essay.formating", formLayout, modeKeys.keys(), modeKeys.values());
		textFormattingEl.setEnabled(!restrictedEdit && !readOnly);
		if(itemBuilder.isRichTextFormating()) {
			textFormattingEl.select("rich", true);
		} else {
			textFormattingEl.select("standard", true);
		}
		
		String placeholder = itemBuilder.getPlaceholder();
		placeholderEl = uifactory.addTextElement("placeholder", "fib.placeholder", 256, placeholder, formLayout);
		placeholderEl.setEnabled(!readOnly);
		
		//width (expectedLength), height (expectedLines)
		String expectedLength = getValue(itemBuilder.getExpectedLength());
		lengthEl = uifactory.addTextElement("cols", "essay.expectedLength", -1, expectedLength, formLayout);
		lengthEl.setEnabled(!restrictedEdit && !readOnly);
		lengthEl.setVisible(StringHelper.containsNonWhitespace(expectedLength));
		String expectedLines = getValue(itemBuilder.getExpectedLines());
		heightEl = uifactory.addTextElement("rows", "essay.rows", -1, expectedLines, formLayout);
		heightEl.setEnabled(!restrictedEdit && !readOnly);

		//words count min. max. (maxStrings)
		String minStrings = getValue(itemBuilder.getMinStrings());
		minWordsEl = uifactory.addTextElement("min.strings", "essay.min.strings", -1, minStrings, formLayout);
		minWordsEl.setEnabled(!restrictedEdit && !readOnly);
		String maxStrings = getValue(itemBuilder.getMaxStrings());
		maxWordsEl = uifactory.addTextElement("max.strings", "essay.max.strings", -1, maxStrings, formLayout);
		maxWordsEl.setEnabled(!restrictedEdit && !readOnly);
		
		//copy/paste
		SelectionValues keys = new SelectionValues();
		keys.add(SelectionValues.entry("yes", translate("yes")));
		keys.add(SelectionValues.entry("no", translate("no")));
		copyPasteEl = uifactory.addRadiosHorizontal("copy.paste", "essay.copy.paste", formLayout, keys.keys(), keys.values());
		copyPasteEl.setEnabled(!restrictedEdit && !readOnly);
		if(itemBuilder.isCopyPasteDisabled()) {
			copyPasteEl.select("no", true);
		} else {
			copyPasteEl.select("yes", true);
		}
		
		// Submit Button
		FormLayoutContainer buttonsContainer = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsContainer.setRootForm(mainForm);
		buttonsContainer.setElementCssClass("o_sel_lob_save");
		buttonsContainer.setVisible(!readOnly);
		formLayout.add(buttonsContainer);
		uifactory.addFormSubmitButton("submit", buttonsContainer);
	}
	
	private String getValue(Integer integer) {
		return integer == null ? "" : integer.toString();
	}
	
	public Integer getValue(TextElement integerEl) {
		String val = integerEl.getValue();
		Integer integer = null;
		if(isInteger(val)) {
			return Integer.parseInt(val);
		}
		return integer;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		titleEl.clearError();
		if(!StringHelper.containsNonWhitespace(titleEl.getValue())) {
			titleEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		copyPasteEl.clearError();
		if(!copyPasteEl.isOneSelected()) {
			copyPasteEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		textFormattingEl.clearError();
		if(!textFormattingEl.isOneSelected()) {
			textFormattingEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}

		allOk &= validateInteger(lengthEl);
		allOk &= validateInteger(heightEl);
		allOk &= validateInteger(minWordsEl);
		allOk &= validateInteger(maxWordsEl);
		return allOk;
	}
	
	private boolean validateInteger(TextElement integerEl) {
		boolean allOk = true;
		
		integerEl.clearError();
		if(StringHelper.containsNonWhitespace(integerEl.getValue())) {
			if(!StringHelper.isLong(integerEl.getValue())) {
				integerEl.setErrorKey("form.error.nointeger", null);
				allOk &= false;
			} else if(!isInteger(integerEl.getValue())) {
				integerEl.setErrorKey("error.integer.positive", null);
				allOk &= false;
			}
		}
		
		return allOk;
	}
	
	private boolean isInteger(String val) {
		if(StringHelper.isLong(val)) {
			try {
				int num = Integer.parseInt(val);
				return num >= 0;
			} catch(NumberFormatException e) {
				return false;
			}
		}
		return false;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(readOnly) return;
		//title
		itemBuilder.setTitle(titleEl.getValue());
		//question
		String questionText = textEl.getRawValue();
		itemBuilder.setQuestion(questionText);
		
		boolean copyPasteDisabled = copyPasteEl.isSelected(1);
		itemBuilder.setCopyPasteDisabled(copyPasteDisabled);
		
		boolean richText = "rich".equals(textFormattingEl.getSelectedKey());
		itemBuilder.setRichTextFormating(richText);
		
		itemBuilder.setPlaceholder(placeholderEl.getValue());
		if(!restrictedEdit) {
			itemBuilder.setExpectedLength(getValue(lengthEl));
			itemBuilder.setExpectedLines(getValue(heightEl));

			//min. max. words
			itemBuilder.setMinStrings(getValue(minWordsEl));
			itemBuilder.setMaxStrings(getValue(maxWordsEl));
		}
		
		fireEvent(ureq, new AssessmentItemEvent(AssessmentItemEvent.ASSESSMENT_ITEM_CHANGED, itemBuilder.getAssessmentItem(), QTI21QuestionType.essay));
	}
}