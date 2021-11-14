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
import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.TextMode;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.interactions.KPrimAssessmentItemBuilder;
import org.olat.ims.qti21.ui.ResourcesMapper;
import org.olat.ims.qti21.ui.components.FlowFormItem;
import org.olat.ims.qti21.ui.editor.AssessmentTestEditorController;
import org.olat.ims.qti21.ui.editor.events.AssessmentItemEvent;

import uk.ac.ed.ph.jqtiplus.node.content.basic.FlowStatic;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.MatchInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleAssociableChoice;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * KPrim is 8 simple choice, but 2 choices are paired together.
 * 
 * Initial date: 06.01.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class KPrimEditorController extends FormBasicController {
	
	private static final String[] yesnoKeys = new String[]{ "y", "n"};
	private static final String[] alignmentKeys = new String[]{ "left", "right"};
	
	private TextElement titleEl;
	private RichTextElement textEl;
	private SingleSelection shuffleEl, alignmentEl;
	private FormLayoutContainer answersCont;
	private final List<KprimWrapper> choiceWrappers = new ArrayList<>();

	private int count = 0;
	private final File itemFile;
	private final VFSContainer itemContainer;
	
	private final String mapperUri;
	private final boolean readOnly;
	private final boolean restrictedEdit;
	private final KPrimAssessmentItemBuilder itemBuilder;
	
	public KPrimEditorController(UserRequest ureq, WindowControl wControl, KPrimAssessmentItemBuilder itemBuilder,
			File rootDirectory, VFSContainer rootContainer, File itemFile, boolean restrictedEdit, boolean readOnly) {
		super(ureq, wControl, "simple_choices_editor");
		setTranslator(Util.createPackageTranslator(AssessmentTestEditorController.class, getLocale()));
		this.itemBuilder = itemBuilder;
		this.readOnly = readOnly;
		this.itemFile = itemFile;
		this.restrictedEdit = restrictedEdit;
		
		mapperUri = registerCacheableMapper(null, "KPrimEditorController::" + CodeHelper.getRAMUniqueID(),
				new ResourcesMapper(itemFile.toURI(), rootDirectory));
		String relativePath = rootDirectory.toPath().relativize(itemFile.toPath().getParent()).toString();
		itemContainer = (VFSContainer)rootContainer.resolve(relativePath);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer metadata = FormLayoutContainer.createDefaultFormLayout("metadata", getTranslator());
		metadata.setFormContextHelp("Configure test questions");
		metadata.setRootForm(mainForm);
		formLayout.add(metadata);
		formLayout.add("metadata", metadata);
		
		titleEl = uifactory.addTextElement("title", "form.imd.title", -1, itemBuilder.getTitle(), metadata);
		titleEl.setElementCssClass("o_sel_assessment_item_title");
		titleEl.setMandatory(true);
		titleEl.setEnabled(!readOnly);

		String description = itemBuilder.getQuestion();
		textEl = uifactory.addRichTextElementForQTI21("desc", "form.imd.descr", description, 8, -1, itemContainer,
				metadata, ureq.getUserSession(), getWindowControl());
		textEl.setEnabled(!readOnly);
		textEl.setVisible(!readOnly);
		if(readOnly) {
			FlowFormItem textReadOnlyEl = new FlowFormItem("descro", itemFile);
			textReadOnlyEl.setLabel("form.imd.descr", null);
			textReadOnlyEl.setBlocks(itemBuilder.getQuestionBlocks());
			textReadOnlyEl.setMapperUri(mapperUri);
			metadata.add(textReadOnlyEl);
		}
		
		//shuffle
		String[] yesnoValues = new String[]{ translate("yes"), translate("no") };
		shuffleEl = uifactory.addRadiosHorizontal("shuffle", "form.imd.shuffle", metadata, yesnoKeys, yesnoValues);
		shuffleEl.setEnabled(!restrictedEdit && !readOnly);
		if (itemBuilder.isShuffle()) {
			shuffleEl.select("y", true);
		} else {
			shuffleEl.select("n", true);
		}
		
		//layout
		String[] alignmentValues = new String[]{ translate("form.imd.alignment.left"), translate("form.imd.alignment.right") };
		alignmentEl = uifactory.addRadiosHorizontal("alignment", "form.imd.alignment", metadata, alignmentKeys, alignmentValues);
		alignmentEl.setEnabled(!restrictedEdit && !readOnly);
		if (itemBuilder.hasClassAttr(QTI21Constants.CHOICE_ALIGN_RIGHT)) {
			alignmentEl.select(alignmentKeys[1], true);
		} else {
			alignmentEl.select(alignmentKeys[0], true);
		}

		//responses
		String page = velocity_root + "/kprim_choices.html";
		answersCont = FormLayoutContainer.createCustomFormLayout("answers", getTranslator(), page);
		answersCont.setRootForm(mainForm);
		formLayout.add(answersCont);
		formLayout.add("answers", answersCont);

		MatchInteraction interaction = itemBuilder.getMatchInteraction();
		if(interaction != null) {
			List<SimpleAssociableChoice> choices = itemBuilder.getKprimChoices();
			for(SimpleAssociableChoice choice:choices) {
				wrapAnswer(ureq, choice);
			}
		}
		answersCont.contextPut("choices", choiceWrappers);
		answersCont.contextPut("restrictedEdit", restrictedEdit || readOnly);
		recalculateUpDownLinks();

		// Submit Button
		FormLayoutContainer buttonsContainer = FormLayoutContainer.createDefaultFormLayout("buttons", getTranslator());
		buttonsContainer.setElementCssClass("o_sel_choices_save");
		buttonsContainer.setRootForm(mainForm);
		buttonsContainer.setVisible(!readOnly);
		formLayout.add(buttonsContainer);
		formLayout.add("buttons", buttonsContainer);
		uifactory.addFormSubmitButton("submit", buttonsContainer);
	}
	
	private void wrapAnswer(UserRequest ureq, SimpleAssociableChoice choice) {
		List<FlowStatic> choiceFlow = choice.getFlowStatics();
		String choiceContent =  itemBuilder.getHtmlHelper().flowStaticString(choiceFlow);
		String choiceId = "answer" + count++;
		RichTextElement choiceEl = uifactory.addRichTextElementForQTI21(choiceId, "form.imd.answer", choiceContent, 8, -1, itemContainer,
				answersCont, ureq.getUserSession(), getWindowControl());
		choiceEl.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.oneLine);
		choiceEl.setUserObject(choice);
		choiceEl.setEnabled(!readOnly);
		choiceEl.setVisible(!readOnly);
		answersCont.add("choiceId", choiceEl);
		
		String choiceRoId = "answerro" + count++;
		FlowFormItem choiceReadOnlyEl = new FlowFormItem(choiceRoId, itemFile);
		choiceReadOnlyEl.setFlowStatics(choiceFlow);
		choiceReadOnlyEl.setMapperUri(mapperUri);
		answersCont.add(choiceRoId, choiceReadOnlyEl);
		
		FormLink upLink = uifactory.addFormLink("up-".concat(choiceId), "up", "", null, answersCont, Link.NONTRANSLATED);
		upLink.setIconLeftCSS("o_icon o_icon-lg o_icon_move_up");
		upLink.setEnabled(!restrictedEdit && !readOnly);
		answersCont.add(upLink);
		answersCont.add("up-".concat(choiceId), upLink);
		
		FormLink downLink = uifactory.addFormLink("down-".concat(choiceId), "down", "", null, answersCont, Link.NONTRANSLATED);
		downLink.setIconLeftCSS("o_icon o_icon-lg o_icon_move_down");
		downLink.setEnabled(!restrictedEdit && !readOnly);
		answersCont.add(downLink);
		answersCont.add("down-".concat(choiceId), downLink);
		
		choiceWrappers.add(new KprimWrapper(choice, choiceEl, choiceReadOnlyEl, upLink, downLink));
	}
		
	@Override
	protected void formOK(UserRequest ureq) {
		if(readOnly) return;
		//title
		itemBuilder.setTitle(titleEl.getValue());
		//question
		String questionText = textEl.getRawValue();
		itemBuilder.setQuestion(questionText);
		
		//shuffle
		if(!restrictedEdit) {
			itemBuilder.setShuffle(shuffleEl.isOneSelected() && shuffleEl.isSelected(0));
			//alignment
			if(alignmentEl.isOneSelected() && alignmentEl.isSelected(1)) {
				itemBuilder.addClass(QTI21Constants.CHOICE_ALIGN_RIGHT);
			} else {
				itemBuilder.removeClass(QTI21Constants.CHOICE_ALIGN_RIGHT);
			}
		}
		
		//update kprims
		for(KprimWrapper choiceWrapper:choiceWrappers) {
			SimpleAssociableChoice choice = choiceWrapper.getSimpleChoice();
			String answer = choiceWrapper.getAnswer().getRawValue();
			itemBuilder.getHtmlHelper().appendHtml(choice, answer);
		}
		
		//set associations
		if(!restrictedEdit) {
			List<SimpleAssociableChoice> choices = new ArrayList<>();
			for(KprimWrapper choiceWrapper:choiceWrappers) {
				SimpleAssociableChoice choice = choiceWrapper.getSimpleChoice();
				Identifier choiceIdentifier = choice.getIdentifier();
				String association = ureq.getHttpReq().getParameter(choiceIdentifier.toString());
				if("correct".equals(association)) {
					itemBuilder.setAssociation(choiceIdentifier, QTI21Constants.CORRECT_IDENTIFIER);
				} else if("wrong".equals(association)) {
					itemBuilder.setAssociation(choiceIdentifier, QTI21Constants.WRONG_IDENTIFIER);
				}
				choices.add(choice);
			}
			itemBuilder.setKprimChoices(choices);
		}

		fireEvent(ureq, new AssessmentItemEvent(AssessmentItemEvent.ASSESSMENT_ITEM_CHANGED, itemBuilder.getAssessmentItem(), QTI21QuestionType.kprim));
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink) {
			FormLink button = (FormLink)source;
			String cmd = button.getCmd();
			if("up".equals(cmd)) {
				updateMatch(ureq);
				doMoveSimpleChoiceUp((KprimWrapper)button.getUserObject());
			} else if("down".equals(cmd)) {
				updateMatch(ureq);
				doMoveSimpleChoiceDown((KprimWrapper)button.getUserObject());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void updateMatch(UserRequest ureq) {
		for(KprimWrapper choiceWrapper:choiceWrappers) {
			SimpleAssociableChoice choice = choiceWrapper.getSimpleChoice();
			Identifier choiceIdentifier = choice.getIdentifier();
			String association = ureq.getHttpReq().getParameter(choiceIdentifier.toString());
			if(StringHelper.containsNonWhitespace(association)) {
				if("correct".equals(association)) {
					itemBuilder.setAssociation(choiceIdentifier, QTI21Constants.CORRECT_IDENTIFIER);
				} else if("wrong".equals(association)) {
					itemBuilder.setAssociation(choiceIdentifier, QTI21Constants.WRONG_IDENTIFIER);
				}
			}
		}
	}

	private void doMoveSimpleChoiceUp(KprimWrapper choiceWrapper) {
		int index = choiceWrappers.indexOf(choiceWrapper) - 1;
		if(index >= 0 && index < choiceWrappers.size()) {
			choiceWrappers.remove(choiceWrapper);
			choiceWrappers.add(index, choiceWrapper);
		}
		recalculateUpDownLinks();
		flc.setDirty(true);
	}
	
	private void doMoveSimpleChoiceDown(KprimWrapper choiceWrapper) {
		int index = choiceWrappers.indexOf(choiceWrapper) + 1;
		if(index > 0 && index < choiceWrappers.size()) {
			choiceWrappers.remove(choiceWrapper);
			choiceWrappers.add(index, choiceWrapper);
		}
		recalculateUpDownLinks();
		flc.setDirty(true);
	}
	
	private void recalculateUpDownLinks() {
		int numOfChoices = choiceWrappers.size();
		for(int i=0; i<numOfChoices; i++) {
			KprimWrapper choiceWrapper = choiceWrappers.get(i);
			choiceWrapper.getUp().setEnabled(i != 0 && !restrictedEdit && !readOnly);
			choiceWrapper.getDown().setEnabled(i < (numOfChoices - 1) && !restrictedEdit && !readOnly);
		}
	}

	public final class KprimWrapper {
		
		private final SimpleAssociableChoice choice;
		private final RichTextElement answerEl;
		private final FlowFormItem answerReadOnlyEl;
		private final FormLink upLink, downLink;
		private final Identifier choiceIdentifier;
		
		public KprimWrapper(SimpleAssociableChoice choice, RichTextElement answerEl, FlowFormItem answerReadOnlyEl,
				FormLink upLink, FormLink downLink) {
			this.choice = choice;
			this.choiceIdentifier = choice.getIdentifier();
			this.answerEl = answerEl;
			this.answerReadOnlyEl = answerReadOnlyEl;
			answerEl.setUserObject(this);
			this.upLink = upLink;
			upLink.setUserObject(this);
			this.downLink = downLink;
			downLink.setUserObject(this);
		}
		
		public Identifier getIdentifier() {
			return choiceIdentifier;
		}
		
		public String getIdentifierString() {
			return choiceIdentifier.toString();
		}
		
		public boolean isCorrect() {
			return itemBuilder.isCorrect(choiceIdentifier);
		}
		
		public boolean isWrong() {
			return itemBuilder.isWrong(choiceIdentifier);
		}
		
		public SimpleAssociableChoice getSimpleChoice() {
			return choice;
		}
		
		public RichTextElement getAnswer() {
			return answerEl;
		}
		
		public FlowFormItem getAnswerReadOnly() {
			return answerReadOnlyEl;
		}

		public FormLink getUp() {
			return upLink;
		}

		public FormLink getDown() {
			return downLink;
		}
	}
}
