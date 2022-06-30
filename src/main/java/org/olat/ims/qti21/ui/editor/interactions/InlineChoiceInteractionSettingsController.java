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

import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.createInlineChoice;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.ims.qti21.model.IdentifierGenerator;
import org.olat.ims.qti21.model.xml.interactions.InlineChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.InlineChoiceAssessmentItemBuilder.InlineChoiceInteractionEntry;
import org.olat.ims.qti21.ui.editor.AssessmentTestEditorController;

import uk.ac.ed.ph.jqtiplus.node.content.basic.TextRun;
import uk.ac.ed.ph.jqtiplus.node.content.variable.TextOrVariable;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.InlineChoice;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 22 juin 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InlineChoiceInteractionSettingsController extends FormBasicController {
	
	private MultipleSelectionElement shuffleEl;
	private List<InlineChoiceWrapper> choiceWrappers = new ArrayList<>();
	
	private int counter = 0;
	private final boolean readOnly;
	private final boolean restrictedEdit;
	private final InlineChoiceInteractionEntry inlineChoiceBlock;
	
	public InlineChoiceInteractionSettingsController(UserRequest ureq, WindowControl wControl, InlineChoiceInteractionEntry choiceBlock,
			boolean restrictedEdit, boolean readOnly) {
		super(ureq, wControl, "inline_choices_settings", Util.createPackageTranslator(AssessmentTestEditorController.class, ureq.getLocale()));
		this.inlineChoiceBlock = choiceBlock;
		this.restrictedEdit = restrictedEdit;
		this.readOnly = readOnly;
		initForm(ureq);
		updateUI();
	}
	
	public Identifier getResponseIdentifier() {
		return inlineChoiceBlock.getResponseIdentifier();
	}
	
	public String getSolution() {
		Identifier correctResponseId = inlineChoiceBlock.getCorrectResponseId();
		for(InlineChoiceWrapper wrapper:choiceWrappers) {
			if(correctResponseId != null && correctResponseId.toString().equals(wrapper.getInlineChoiceIdentifier())) {
				return wrapper.getTextEl().getValue();
			}
		}
		return null;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues shuffleKeyValues = new SelectionValues();
		shuffleKeyValues.add(SelectionValues.entry("on", ""));
		shuffleEl = uifactory.addCheckboxesHorizontal("form.imd.shuffle", "form.imd.shuffle", formLayout,
				shuffleKeyValues.keys(), shuffleKeyValues.values());
		shuffleEl.select("on", inlineChoiceBlock.isShuffle());
		
		List<InlineChoice> choices = inlineChoiceBlock.getInlineChoices();
		for(InlineChoice choice:choices) {
			InlineChoiceWrapper wrapper = forgeInlineChoice(choice, formLayout);
			choiceWrappers.add(wrapper);
		}
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("correctAnswer", inlineChoiceBlock.getCorrectResponseId());
			layoutCont.contextPut("choices", choiceWrappers);
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	private InlineChoiceWrapper forgeInlineChoice(InlineChoice choice, FormItemContainer formLayout) {
		boolean editable = isEditable(choice);
		
		String id = "choice_" + (++counter);
		String text = InlineChoiceAssessmentItemBuilder.getText(choice);
		TextElement choiceEl = uifactory.addTextElement(id, null, 255, text, formLayout);
		choiceEl.setUserObject(choice);
		choiceEl.setEnabled(!restrictedEdit && !readOnly && !choice.getIdentifier().toString().startsWith("global-"));
		choiceEl.setDomReplacementWrapperRequired(false);
		choiceEl.setEnabled(editable);
		
		FormLink addButton = uifactory.addFormLink(id.concat("_add"), "add", "", null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
		addButton.setTitle(translate("add.global.choice"));
		addButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
		addButton.setVisible(!restrictedEdit && !readOnly);
		
		FormLink deleteButton = uifactory.addFormLink(id.concat("_del"), "delete", "", null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
		deleteButton.setTitle(translate("remove.global.choice"));
		deleteButton.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
		deleteButton.setVisible(editable);
		
		InlineChoiceWrapper wrapper = new InlineChoiceWrapper(choice, choiceEl, addButton, deleteButton);
		choiceEl.setUserObject(wrapper);
		addButton.setUserObject(wrapper);
		deleteButton.setUserObject(wrapper);
		return wrapper;
	}
	
	private void updateUI() {
		boolean canDelete = choiceWrappers.size() > 1;
		for(InlineChoiceWrapper choiceWrapper:choiceWrappers) {
			choiceWrapper.getDeleteButton().setVisible(canDelete && isEditable(choiceWrapper.getInlineChoice()));
		}
	}
	
	private boolean isEditable(InlineChoice choice) {
		return !restrictedEdit && !readOnly && !choice.getIdentifier().toString().startsWith("global-");
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		doCommitCorrectResponseID(ureq);
		
		if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("add".equals(link.getCmd()) && link.getUserObject() instanceof InlineChoiceWrapper) {
				doAddChoice((InlineChoiceWrapper)link.getUserObject());
				updateUI();
			} else if("delete".equals(link.getCmd()) && link.getUserObject() instanceof InlineChoiceWrapper) {
				doDeleteChoice((InlineChoiceWrapper)link.getUserObject());
				updateUI();
			}
		}
		
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doCommitCorrectResponseID(ureq);
		doCommitChoices();
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void doCommitCorrectResponseID(UserRequest ureq) {
		String correctResponseId = ureq.getParameter("correct");
		if(StringHelper.containsNonWhitespace(correctResponseId)) {
			inlineChoiceBlock.setCorrectResponseId(Identifier.parseString(correctResponseId));
		}
	}
	
	/**
	 * Copy the inline choice in the interaction wrapper of the item builder.
	 */
	private void doCommitChoices() {
		inlineChoiceBlock.getInlineChoices().clear();
		inlineChoiceBlock.setShuffle(shuffleEl.isAtLeastSelected(1));
		
		for(InlineChoiceWrapper choiceWrapper:choiceWrappers) {
			InlineChoice inlineChoice = choiceWrapper.getInlineChoice();
			List<TextOrVariable> texts = inlineChoice.getTextOrVariables();
			texts.clear();
			String text = choiceWrapper.getTextEl().getValue();
			text = text == null ? "" : text;
			texts.add(new TextRun(inlineChoice, text));
			inlineChoiceBlock.getInlineChoices().add(inlineChoice);
		}
	}
	
	private void doAddChoice(InlineChoiceWrapper wrapper) {
		Identifier responseId = IdentifierGenerator.newAsIdentifier("inlinec");
		InlineChoice newChoice = createInlineChoice(null, "", responseId);
		InlineChoiceWrapper choiceWrapper = forgeInlineChoice(newChoice, flc);
		int index = wrapper == null ? -1 : choiceWrappers.indexOf(wrapper) + 1;
		if(index >= 0 && index < choiceWrappers.size()) {
			choiceWrappers.add(index, choiceWrapper);
		} else {
			choiceWrappers.add(choiceWrapper);
		}
		flc.setDirty(true);
	}
	
	private void doDeleteChoice(InlineChoiceWrapper wrapper) {
		if(choiceWrappers.size() > 1) {
			choiceWrappers.remove(wrapper);
		} else if(choiceWrappers.size() == 1) {
			choiceWrappers.get(0).getTextEl().setErrorKey("error.atleast.one.choice", null);
		}
		flc.setDirty(true);
	}
	
	public class InlineChoiceWrapper {
		
		private InlineChoice inlineChoice;
		private TextElement textEl;
		private FormLink addButton;
		private FormLink deleteButton;
		
		public InlineChoiceWrapper(InlineChoice inlineChoice, TextElement textEl, FormLink addButton, FormLink deleteButton) {
			this.inlineChoice = inlineChoice;
			this.textEl = textEl;
			this.addButton = addButton;
			this.deleteButton = deleteButton;
		}
		
		public String getInlineChoiceIdentifier() {
			return inlineChoice.getIdentifier().toString();
		}
		
		public boolean isCorrect() {
			Identifier choiceIdentifier = inlineChoice.getIdentifier();
			return choiceIdentifier != null && choiceIdentifier.equals(inlineChoiceBlock.getCorrectResponseId());
		}

		public InlineChoice getInlineChoice() {
			return inlineChoice;
		}

		public void setInlineChoice(InlineChoice inlineChoice) {
			this.inlineChoice = inlineChoice;
		}

		public TextElement getTextEl() {
			return textEl;
		}

		public void setTextEl(TextElement textEl) {
			this.textEl = textEl;
		}

		public FormLink getAddButton() {
			return addButton;
		}

		public void setAddButton(FormLink addButton) {
			this.addButton = addButton;
		}

		public FormLink getDeleteButton() {
			return deleteButton;
		}

		public void setDeleteButton(FormLink deleteButton) {
			this.deleteButton = deleteButton;
		}
	}
}
