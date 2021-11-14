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
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.AssessmentItemFactory;
import org.olat.ims.qti21.model.xml.interactions.SingleChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.ui.ResourcesMapper;
import org.olat.ims.qti21.ui.components.FlowFormItem;
import org.olat.ims.qti21.ui.editor.AssessmentTestEditorController;
import org.olat.ims.qti21.ui.editor.SyncAssessmentItem;
import org.olat.ims.qti21.ui.editor.events.AssessmentItemEvent;

import uk.ac.ed.ph.jqtiplus.node.content.basic.FlowStatic;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleChoice;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.Orientation;

/**
 * 
 * Initial date: 26.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SingleChoiceEditorController extends FormBasicController implements SyncAssessmentItem {

	private TextElement titleEl;
	private RichTextElement textEl;
	private SingleSelection shuffleEl;
	private SingleSelection orientationEl;
	private SingleSelection alignmentEl;
	private FormLayoutContainer answersCont;
	private final List<SimpleChoiceWrapper> choiceWrappers = new ArrayList<>();
	
	private int count = 0;
	private final File itemFile;
	private final VFSContainer itemContainer;
	private final boolean readOnly;
	private final boolean restrictedEdit;
	private final String mapperUri;
	private final SingleChoiceAssessmentItemBuilder itemBuilder;
	
	private static final String[] yesnoKeys = new String[]{ "y", "n"};
	private static final String[] alignmentKeys = new String[]{ "left", "right"};
	private static final String[] layoutKeys = new String[]{ Orientation.VERTICAL.name(), Orientation.HORIZONTAL.name() };

	public SingleChoiceEditorController(UserRequest ureq, WindowControl wControl,
			SingleChoiceAssessmentItemBuilder itemBuilder,
			File rootDirectory, VFSContainer rootContainer, File itemFile,
			boolean restrictedEdit, boolean readOnly) {
		super(ureq, wControl, "simple_choices_editor");
		setTranslator(Util.createPackageTranslator(AssessmentTestEditorController.class, getLocale()));
		this.itemBuilder = itemBuilder;
		this.readOnly = readOnly;
		this.itemFile = itemFile;
		this.restrictedEdit = restrictedEdit;

		mapperUri = registerCacheableMapper(null, "SingleChoiceEditorController::" + CodeHelper.getRAMUniqueID(),
				new ResourcesMapper(itemFile.toURI(), rootDirectory));
		String relativePath = rootDirectory.toPath().relativize(itemFile.toPath().getParent()).toString();
		itemContainer = (VFSContainer)rootContainer.resolve(relativePath);
		
		initForm(ureq);
		validateScoreOfCorrectAnswer();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer metadata = FormLayoutContainer.createDefaultFormLayout_2_10("metadata", getTranslator());
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
		textEl.setVisible(!readOnly);
		textEl.setEnabled(!readOnly);
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
		String[] layoutValues = new String[]{ translate("form.imd.layout.vertical"), translate("form.imd.layout.horizontal") };
		orientationEl = uifactory.addRadiosHorizontal("layout", "form.imd.layout", metadata, layoutKeys, layoutValues);
		orientationEl.setEnabled(!restrictedEdit && !readOnly);
		if (itemBuilder.getOrientation() == null || Orientation.VERTICAL.equals(itemBuilder.getOrientation())) {
			orientationEl.select(Orientation.VERTICAL.name(), true);
		} else {
			orientationEl.select(Orientation.HORIZONTAL.name(), true);
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
		String page = velocity_root + "/simple_choices.html";
		answersCont = FormLayoutContainer.createCustomFormLayout("answers", getTranslator(), page);
		answersCont.setRootForm(mainForm);
		formLayout.add(answersCont);
		formLayout.add("answers", answersCont);

		ChoiceInteraction interaction = itemBuilder.getChoiceInteraction();
		if(interaction != null) {
			List<SimpleChoice> choices = itemBuilder.getChoices();
			for(SimpleChoice choice:choices) {
				wrapAnswer(ureq, choice);
			}
		}
		answersCont.contextPut("choices", choiceWrappers);
		answersCont.contextPut("restrictedEdit", restrictedEdit || readOnly);
		recalculateUpDownLinks();

		// Submit Button
		FormLayoutContainer buttonsContainer = FormLayoutContainer.createDefaultFormLayout_2_10("buttons", getTranslator());
		buttonsContainer.setRootForm(mainForm);
		buttonsContainer.setElementCssClass("o_sel_choices_save");
		buttonsContainer.setVisible(!readOnly);
		formLayout.add(buttonsContainer);
		formLayout.add("buttons", buttonsContainer);
		if(!readOnly) {
			uifactory.addFormSubmitButton("submit", buttonsContainer);
		}
	}

	private void wrapAnswer(UserRequest ureq, SimpleChoice choice) {
		List<FlowStatic> choiceFlow = choice.getFlowStatics();
		String choiceContent =  itemBuilder.getHtmlHelper().flowStaticString(choiceFlow);
		String choiceId = "answer" + count++;
		RichTextElement choiceEl = uifactory.addRichTextElementForQTI21(choiceId, "form.imd.answer", choiceContent, 8, -1, itemContainer,
				answersCont, ureq.getUserSession(), getWindowControl());
		choiceEl.setEnabled(!readOnly);
		choiceEl.setVisible(!readOnly);
		choiceEl.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.oneLine);
		choiceEl.setUserObject(choice);
		answersCont.add("choiceId", choiceEl);
		
		String choiceRoId = "answerro" + count++;
		FlowFormItem choiceReadOnlyEl = new FlowFormItem(choiceRoId, itemFile);
		choiceReadOnlyEl.setFlowStatics(choiceFlow);
		choiceReadOnlyEl.setMapperUri(mapperUri);
		answersCont.add(choiceRoId, choiceReadOnlyEl);
		
		FormLink removeLink = uifactory.addFormLink("rm-".concat(choiceId), "rm", "", null, answersCont, Link.NONTRANSLATED);
		removeLink.setIconLeftCSS("o_icon o_icon-lg o_icon_delete");
		removeLink.setEnabled(!restrictedEdit && !readOnly);
		answersCont.add(removeLink);
		answersCont.add("rm-".concat(choiceId), removeLink);
		
		FormLink addLink = uifactory.addFormLink("add-".concat(choiceId), "add", "", null, answersCont, Link.NONTRANSLATED);
		addLink.setIconLeftCSS("o_icon o_icon-lg o_icon_add");
		addLink.setEnabled(!restrictedEdit && !readOnly);
		answersCont.add(addLink);
		answersCont.add("add-".concat(choiceId), addLink);
		
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
		
		choiceWrappers.add(new SimpleChoiceWrapper(choice, choiceEl, choiceReadOnlyEl, removeLink, addLink, upLink, downLink));
	}

	@Override
	public void sync(UserRequest ureq, AssessmentItemBuilder builder) {
		validateScoreOfCorrectAnswer();
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		titleEl.clearError();
		if(!StringHelper.containsNonWhitespace(titleEl.getValue())) {
			titleEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		answersCont.clearError();
		if(!restrictedEdit) {
			String correctAnswer = ureq.getParameter("correct");
			if(choiceWrappers.isEmpty()) {
				allOk &= false;
				answersCont.setErrorKey("error.atleast.one.answer", null);
			} else if(!StringHelper.containsNonWhitespace(correctAnswer)) {
				allOk &= false;
				answersCont.setErrorKey("error.need.correct.answer", null);
			}
		}
		validateScoreOfCorrectAnswer();// the validation is not mandatory, issue onnly a warning
		
		
		return allOk;
	}
	
	private void validateScoreOfCorrectAnswer() {
		try {
			Boolean warning = (Boolean)answersCont.contextGet("scoreWarning");
			Boolean newWarning = Boolean.valueOf(itemBuilder.scoreOfCorrectAnswerWarning());
			if(warning == null || !warning.equals(newWarning)) {
				answersCont.contextPut("scoreWarning", newWarning);
			}
		} catch (Exception e) {
			// not a critical feature, don't produce red screen
			logError("", e);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(readOnly) return;
		
		//title
		itemBuilder.setTitle(titleEl.getValue());
		//question
		String questionText = textEl.getRawValue();
		itemBuilder.setQuestion(questionText);
		
		Identifier correctAnswerIdentifier = null;
		if(!restrictedEdit) {
			//correct response
			String correctAnswer = ureq.getParameter("correct");
			correctAnswerIdentifier = Identifier.parseString(correctAnswer);
			itemBuilder.setCorrectAnswer(correctAnswerIdentifier);
			//shuffle
			itemBuilder.setShuffle(shuffleEl.isOneSelected() && shuffleEl.isSelected(0));
			//orientation
			itemBuilder.setOrientation(Orientation.valueOf(orientationEl.getSelectedKey()));
			//alignment
			if(alignmentEl.isOneSelected() && alignmentEl.isSelected(1)) {
				itemBuilder.addClass(QTI21Constants.CHOICE_ALIGN_RIGHT);
			} else {
				itemBuilder.removeClass(QTI21Constants.CHOICE_ALIGN_RIGHT);
			}
		}
		
		//replace simple choices
		List<SimpleChoice> choiceList = new ArrayList<>();
		for(SimpleChoiceWrapper choiceWrapper:choiceWrappers) {
			SimpleChoice choice = choiceWrapper.getSimpleChoice();
			//text
			String answer = choiceWrapper.getAnswer().getRawValue();
			itemBuilder.getHtmlHelper().appendHtml(choice, answer);
			choiceList.add(choice);
		}
		itemBuilder.setSimpleChoices(choiceList);
		validateScoreOfCorrectAnswer();

		fireEvent(ureq, new AssessmentItemEvent(AssessmentItemEvent.ASSESSMENT_ITEM_CHANGED, itemBuilder.getAssessmentItem(), QTI21QuestionType.sc));
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink) {
			FormLink button = (FormLink)source;
			String cmd = button.getCmd();
			if("rm".equals(cmd)) {
				updateCorrectAnswer(ureq);
				doRemoveSimpleChoice((SimpleChoiceWrapper)button.getUserObject());
			} else if("add".equals(cmd)) {
				updateCorrectAnswer(ureq);
				doAddSimpleChoice(ureq);
			} else if("up".equals(cmd)) {
				updateCorrectAnswer(ureq);
				doMoveSimpleChoiceUp((SimpleChoiceWrapper)button.getUserObject());
			} else if("down".equals(cmd)) {
				updateCorrectAnswer(ureq);
				doMoveSimpleChoiceDown((SimpleChoiceWrapper)button.getUserObject());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void updateCorrectAnswer(UserRequest ureq) {
		String correctAnswer = ureq.getParameter("correct");
		if(StringHelper.containsNonWhitespace(correctAnswer)) {
			Identifier correctAnswerIdentifier = Identifier.parseString(correctAnswer);
			itemBuilder.setCorrectAnswer(correctAnswerIdentifier);
		}
	}
	
	private void doAddSimpleChoice(UserRequest ureq) {
		ChoiceInteraction interaction = itemBuilder.getChoiceInteraction();
		SimpleChoice newChoice = AssessmentItemFactory.createSimpleChoice(interaction, translate("new.answer"), "sc");
		wrapAnswer(ureq, newChoice);
		recalculateUpDownLinks();
		flc.setDirty(true);
	}
	
	private void doRemoveSimpleChoice(SimpleChoiceWrapper choiceWrapper) {
		choiceWrappers.remove(choiceWrapper);
		recalculateUpDownLinks();
		flc.setDirty(true);
	}
	
	private void doMoveSimpleChoiceUp(SimpleChoiceWrapper choiceWrapper) {
		int index = choiceWrappers.indexOf(choiceWrapper) - 1;
		if(index >= 0 && index < choiceWrappers.size()) {
			choiceWrappers.remove(choiceWrapper);
			choiceWrappers.add(index, choiceWrapper);
		}
		recalculateUpDownLinks();
		flc.setDirty(true);
	}
	
	private void doMoveSimpleChoiceDown(SimpleChoiceWrapper choiceWrapper) {
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
		boolean canRemove = choiceWrappers.size() > 1;
		for(int i=0; i<numOfChoices; i++) {
			SimpleChoiceWrapper choiceWrapper = choiceWrappers.get(i);
			choiceWrapper.getUp().setEnabled(i != 0 && !restrictedEdit && !readOnly);
			choiceWrapper.getDown().setEnabled(i < (numOfChoices - 1) && !restrictedEdit && !readOnly);
			choiceWrapper.getRemove().setEnabled(canRemove && !restrictedEdit && !readOnly);
		}
	}

	public final class SimpleChoiceWrapper {
		
		private final SimpleChoice choice;
		private final RichTextElement answerEl;
		private final FlowFormItem answerReadOnlyEl;
		
		private final FormLink upLink;
		private final FormLink addLink;
		private final FormLink downLink;
		private final FormLink removeLink;
		
		private final Identifier choiceIdentifier;
		
		public SimpleChoiceWrapper(SimpleChoice choice, RichTextElement answerEl, FlowFormItem answerReadOnlyEl,
				FormLink removeLink, FormLink addLink, FormLink upLink, FormLink downLink) {
			this.choice = choice;
			this.choiceIdentifier = choice.getIdentifier();
			this.answerEl = answerEl;
			this.answerReadOnlyEl = answerReadOnlyEl;
			answerEl.setUserObject(this);
			this.removeLink = removeLink;
			removeLink.setUserObject(this);
			this.addLink = addLink;
			addLink.setUserObject(this);
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
			return itemBuilder.isCorrect(choice);
		}
		
		public SimpleChoice getSimpleChoice() {
			return choice;
		}
		
		public RichTextElement getAnswer() {
			return answerEl;
		}
		
		public FlowFormItem getAnswerReadOnly() {
			return answerReadOnlyEl;
		}

		public FormLink getRemove() {
			return removeLink;
		}

		public FormLink getAdd() {
			return addLink;
		}

		public FormLink getUp() {
			return upLink;
		}

		public FormLink getDown() {
			return downLink;
		}
	}
}
