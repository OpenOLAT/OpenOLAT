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

import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.createSimpleAssociableChoice;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSFormItem;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.interactions.MatchAssessmentItemBuilder;
import org.olat.ims.qti21.ui.ResourcesMapper;
import org.olat.ims.qti21.ui.components.FlowFormItem;
import org.olat.ims.qti21.ui.editor.AssessmentTestEditorController;
import org.olat.ims.qti21.ui.editor.events.AssessmentItemEvent;

import uk.ac.ed.ph.jqtiplus.node.content.basic.FlowStatic;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.MatchInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleAssociableChoice;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 31 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TrueFalseEditorController extends FormBasicController {

	private static final String[] yesnoKeys = new String[]{ "y", "n"};
	private static final String[] layoutKeys = new String[] {
			QTI21Constants.CSS_MATCH_SOURCE_RIGHT, QTI21Constants.CSS_MATCH_SOURCE_LEFT
		};
	
	private TextElement titleEl;
	private FormLink addRowButton;
	private RichTextElement textEl;
	private SingleSelection shuffleEl;
	private SingleSelection layoutEl;
	private FormLayoutContainer answersCont;
	
	private int count = 0;
	private final File itemFile;
	private final VFSContainer itemContainer;
	
	private final boolean readOnly;
	private final boolean restrictedEdit;
	private final String mapperUri;
	private MatchAssessmentItemBuilder itemBuilder;
	private final List<SourceWrapper> sourceWrappers = new ArrayList<>();
	private final Map<String,List<String>> temporaryAssociations = new HashMap<>();
	
	/**
	 * 
	 * @param ureq The user request
	 * @param wControl The parent window control
	 * @param itemBuilder The assessment item builder for match (matrix or drag and drop)
	 * @param rootDirectory	The directory for images...
	 * @param rootContainer The directory for images...
	 * @param itemFile The assessment item file
	 * @param matrix
	 * @param restrictedEdit
	 */
	public TrueFalseEditorController(UserRequest ureq, WindowControl wControl, MatchAssessmentItemBuilder itemBuilder,
			File rootDirectory, VFSContainer rootContainer, File itemFile, boolean restrictedEdit, boolean readOnly) {
		super(ureq, wControl, "simple_choices_editor");
		setTranslator(Util.createPackageTranslator(AssessmentTestEditorController.class, getLocale()));
		this.itemBuilder = itemBuilder;
		this.readOnly = readOnly;
		this.itemFile = itemFile;
		this.restrictedEdit = restrictedEdit;
		
		mapperUri = registerCacheableMapper(null, "TrueFalseEditorController::" + CodeHelper.getRAMUniqueID(),
				new ResourcesMapper(itemFile.toURI(), rootDirectory));
		String relativePath = rootDirectory.toPath().relativize(itemFile.toPath().getParent()).toString();
		itemContainer = (VFSContainer)rootContainer.resolve(relativePath);
		
		initForm(ureq);
		recalculateDeleteButtons();
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
		
		String[] yesnoValues = new String[]{ translate("yes"), translate("no") };
		shuffleEl = uifactory.addRadiosHorizontal("shuffle", "form.imd.shuffle", metadata, yesnoKeys, yesnoValues);
		shuffleEl.setEnabled(!restrictedEdit && !readOnly);
		if (itemBuilder.isShuffle()) {
			shuffleEl.select("y", true);
		} else {
			shuffleEl.select("n", true);
		}
		
		String[] layoutValues = new String[]{
				// the label are reverted because the label is the one choices (align checkbox), but
				// it's a match interaction (align answers)
				translate("form.imd.layout.left"), translate("form.imd.layout.right")
			};
		layoutEl = uifactory.addRadiosHorizontal("layout", "form.imd.alignment", metadata, layoutKeys, layoutValues);
		layoutEl.setElementCssClass("o_sel_match_layout");
		layoutEl.setEnabled(!restrictedEdit && !readOnly);
		boolean found = false;
		for(String layoutKey:layoutKeys) {
			if(itemBuilder.hasMatchInteractionClass(layoutKey)) {
				layoutEl.select(layoutKey, true);
				found = true;
			}
		}
		if(!found) {
			layoutEl.select(QTI21Constants.CSS_MATCH_SOURCE_RIGHT, true);
		}

		//responses
		String page = velocity_root + "/match_truefalse.html";
		answersCont = FormLayoutContainer.createCustomFormLayout("answers", getTranslator(), page);
		answersCont.setRootForm(mainForm);
		answersCont.contextPut("showHeaders", (itemBuilder.getQuestionType() == QTI21QuestionType.matchdraganddrop));
		formLayout.add(answersCont);
		formLayout.add("answers", answersCont);

		MatchInteraction interaction = itemBuilder.getMatchInteraction();
		if(interaction != null) {
			List<SimpleAssociableChoice> sourceChoices = itemBuilder.getSourceChoices();
			for(SimpleAssociableChoice sourceChoice:sourceChoices) {
				wrapSource(ureq, sourceChoice, sourceWrappers);
			}
			List<TargetWrapper> targetChoices = itemBuilder.getTargetChoices()
					.stream().map(TargetWrapper::new).collect(Collectors.toList());
			answersCont.contextPut("targetChoices", targetChoices);
		}
		answersCont.contextPut("sourceChoices", sourceWrappers);
		answersCont.contextPut("restrictedEdit", restrictedEdit || readOnly);
		answersCont.contextPut("responseIdentifier", itemBuilder.getResponseIdentifier());
		int maxAssociations = itemBuilder.getMatchInteraction().getMaxAssociations();
		answersCont.contextPut("interactionMaxAssociations", maxAssociations);

		JSAndCSSFormItem js = new JSAndCSSFormItem("js", new String[] { "js/jquery/qti/jquery.match.js" });
		formLayout.add(js);
		
		if(!readOnly) {
			uifactory.addFormSubmitButton("submit", answersCont);
		}
		if(!restrictedEdit && !readOnly) {
			addRowButton = uifactory.addFormLink("add.match.row", answersCont, Link.BUTTON);
			addRowButton.setElementCssClass("o_sel_match_add_row");
			addRowButton.setIconLeftCSS("o_icon o_icon_add");
		}
	}
	
	private void wrapSource(UserRequest ureq, SimpleAssociableChoice choice, List<SourceWrapper> wrappers) {
		List<FlowStatic> choiceFlow = choice.getFlowStatics();
		String choiceContent =  itemBuilder.getHtmlHelper().flowStaticString(choiceFlow);
		String choiceId = "answer" + count++;
		RichTextElement choiceEl = uifactory.addRichTextElementVeryMinimalistic(choiceId, "form.imd.answer", choiceContent, 4, -1, false, itemContainer,
				answersCont, ureq.getUserSession(), getWindowControl());
		choiceEl.setUserObject(choice);
		choiceEl.setEnabled(!readOnly);
		choiceEl.setVisible(!readOnly);
		answersCont.add("choiceId", choiceEl);
		
		String choiceRoId = "answerro" + count++;
		FlowFormItem choiceReadOnlyEl = new FlowFormItem(choiceRoId, itemFile);
		choiceReadOnlyEl.setFlowStatics(choiceFlow);
		choiceReadOnlyEl.setMapperUri(mapperUri);
		answersCont.add(choiceRoId, choiceReadOnlyEl);
		
		FormLink deleteButton = uifactory.addFormLink("del_" + (count++), "delete", "delete", null, answersCont, Link.NONTRANSLATED);
		deleteButton.setIconLeftCSS("o_icon o_icon_delete_item");
		deleteButton.setVisible(!restrictedEdit && !readOnly);
		deleteButton.setI18nKey("");
		
		SourceWrapper wrapper = new SourceWrapper(choice, choiceEl, choiceReadOnlyEl, deleteButton);
		deleteButton.setUserObject(wrapper);
		wrappers.add(wrapper);
	}
	
	private void recalculateDeleteButtons() {
		boolean canDelete = sourceWrappers.size() > 1;
		for(SourceWrapper sourceWrapper:sourceWrappers) {
			sourceWrapper.getDeleteButton().setVisible(canDelete && !restrictedEdit && !readOnly);
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		commitTemporaryAssociations(ureq);
		if(sourceWrappers.isEmpty()) {
			answersCont.setErrorKey("error.atleast.one.answer", null);
			allOk &= false;
		} else {
			for(SourceWrapper sourceWrapper:sourceWrappers) {
				sourceWrapper.setErrorSingleChoice(false);
				List<String> answers = temporaryAssociations.get(sourceWrapper.getIdentifierString());
				if(answers == null || answers.size() != 1) {
					sourceWrapper.setErrorSingleChoice(true);
					allOk &= false;
				}
			}
		}
		
		if(layoutEl != null) {
			layoutEl.clearError();
			if(!layoutEl.isOneSelected()) {
				layoutEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
		}

		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addRowButton == source) {
			commitTemporaryAssociations(ureq);
			doAddSourceRow(ureq);
			recalculateDeleteButtons();
		} else if(source instanceof FormLink) {
			FormLink button = (FormLink)source;
			if("delete".equals(button.getCmd())) {
				commitTemporaryAssociations(ureq);
				SourceWrapper associationWrapper = (SourceWrapper)button.getUserObject();
				doDeleteAssociableChoice(associationWrapper);
				recalculateDeleteButtons();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(readOnly) return;
		//title
		itemBuilder.setTitle(titleEl.getValue());
		//question
		String questionText = textEl.getRawValue();
		itemBuilder.setQuestion(questionText);
		
		if(!restrictedEdit) {
			itemBuilder.setMultipleChoice(false);
			itemBuilder.setShuffle(shuffleEl.isOneSelected() && shuffleEl.isSelected(0));

			itemBuilder.removeMatchInteractionClass(layoutKeys);
			String cssClass = layoutEl.getSelectedKey();
			itemBuilder.addMatchInteractionClass(cssClass);
		}
		
		//update 
		for(SourceWrapper sourceWrapper:sourceWrappers) {
			SimpleAssociableChoice choice = sourceWrapper.getSimpleChoice();
			String answer = sourceWrapper.getText().getRawValue();
			itemBuilder.getHtmlHelper().appendHtml(choice, answer);
		}

		//correct answers
		commitAssociations(ureq);

		fireEvent(ureq, new AssessmentItemEvent(AssessmentItemEvent.ASSESSMENT_ITEM_CHANGED, itemBuilder.getAssessmentItem(), QTI21QuestionType.match));
	}
	
	private void commitTemporaryAssociations(UserRequest ureq) {
		temporaryAssociations.clear();
		
		for(SourceWrapper sourceWrapper:sourceWrappers) {
			String[] targetParamIds = ureq.getHttpReq().getParameterValues(sourceWrapper.getIdentifierString());
			if(targetParamIds != null && targetParamIds.length == 1) {
				String sourceId = sourceWrapper.getIdentifierString();
				String targetId = targetParamIds[0];
				
				List<String> targetIds = temporaryAssociations.computeIfAbsent(sourceId, s -> new ArrayList<>());
				targetIds.add(targetId);
				temporaryAssociations.put(sourceId, targetIds);
			}
		}
	}
	
	private void commitAssociations(UserRequest ureq) {
		if(restrictedEdit) return;
		
		temporaryAssociations.clear();
		itemBuilder.clearAssociations();
		for(SourceWrapper sourceWrapper:sourceWrappers) {
			String[] targetParamIds = ureq.getHttpReq().getParameterValues(sourceWrapper.getIdentifierString());
			if(targetParamIds != null && targetParamIds.length == 1) {
				Identifier sourceChoiceId = sourceWrapper.getIdentifier();
				Identifier targetChoiceId = Identifier.assumedLegal(targetParamIds[0]);
				itemBuilder.addAssociation(sourceChoiceId, targetChoiceId);
			}
		}
	}
	
	private void doAddSourceRow(UserRequest ureq) {
		SimpleAssociableChoice newChoice = createSimpleAssociableChoice("Text", itemBuilder.getSourceMatchSet());
		itemBuilder.getSourceMatchSet().getSimpleAssociableChoices().add(newChoice);
		wrapSource(ureq, newChoice, sourceWrappers);
		answersCont.setDirty(true);
	}
	
	private void doDeleteAssociableChoice(SourceWrapper associationWrapper) {
		if(sourceWrappers.remove(associationWrapper)) {
			itemBuilder.removeSimpleAssociableChoice(associationWrapper.getSimpleChoice());
		}
		answersCont.setDirty(true);
	}
	
	public class TargetWrapper {

		private final Identifier choiceIdentifier;
		private final String choiceText;
		
		public TargetWrapper(SimpleAssociableChoice choice) {
			choiceText = itemBuilder.getHtmlHelper().flowStaticString(choice.getFlowStatics());
			choiceIdentifier = choice.getIdentifier();
		}
		
		public Identifier getIdentifier() {
			return choiceIdentifier;
		}

		public String getIdentifierString() {
			return choiceIdentifier.toString();
		}

		public String getChoiceText() {
			return choiceText;
		}
	}

	public class SourceWrapper {
		
		private FormLink deleteButton;
		private final FlowFormItem choiceReadOnlyEl;
		private final RichTextElement choiceTextEl;
		private final Identifier choiceIdentifier;
		private final SimpleAssociableChoice choice;
		
		private boolean errorSingleChoice;
		
		public SourceWrapper(SimpleAssociableChoice choice, RichTextElement choiceTextEl, FlowFormItem choiceReadOnlyEl, FormLink deleteButton) {
			this.choice = choice;
			this.choiceTextEl = choiceTextEl;
			this.choiceReadOnlyEl = choiceReadOnlyEl;
			this.choiceIdentifier = choice.getIdentifier();
			this.deleteButton = deleteButton;
		}
		
		public Identifier getIdentifier() {
			return choiceIdentifier;
		}
		
		public String getIdentifierString() {
			return choiceIdentifier.toString();
		}
		
		public SimpleAssociableChoice getSimpleChoice() {
			return choice;
		}
		
		public FormLink getDeleteButton() {
			return deleteButton;
		}
		
		public RichTextElement getText() {
			return choiceTextEl;
		}
		
		public FlowFormItem getTextReadOnly() {
			return choiceReadOnlyEl;
		}
		
		public boolean isCorrect(Identifier targetChoiceId) {
			String sourceId = choice.getIdentifier().toString();
			String targetId = targetChoiceId.toString();
			if(temporaryAssociations.containsKey(sourceId)) {
				if(temporaryAssociations.get(sourceId).contains(targetId)) {
					return true;
				}
			}
			return itemBuilder.isCorrect(choiceIdentifier, targetChoiceId);
		}

		public boolean isErrorSingleChoice() {
			return errorSingleChoice;
		}

		public void setErrorSingleChoice(boolean errorSingleChoice) {
			this.errorSingleChoice = errorSingleChoice;
		}
	}
}
