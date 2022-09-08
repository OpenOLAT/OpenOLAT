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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.RichTextConfiguration;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.RichTextConfigurationDelegate;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.winmgr.Command;
import org.olat.core.gui.control.winmgr.JSCommand;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.ims.qti21.model.IdentifierGenerator;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.InlineChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.InlineChoiceAssessmentItemBuilder.GlobalInlineChoice;
import org.olat.ims.qti21.model.xml.interactions.InlineChoiceAssessmentItemBuilder.InlineChoiceInteractionEntry;
import org.olat.ims.qti21.model.xml.interactions.SimpleChoiceAssessmentItemBuilder.ScoreEvaluation;
import org.olat.ims.qti21.ui.editor.AssessmentTestEditorController;
import org.olat.ims.qti21.ui.editor.SyncAssessmentItem;
import org.olat.ims.qti21.ui.editor.events.AssessmentItemEvent;

import uk.ac.ed.ph.jqtiplus.node.content.basic.TextRun;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.InlineChoice;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 22 juin 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InlineChoiceEditorController extends FormBasicController implements SyncAssessmentItem {
	
	private TextElement titleEl;
	private RichTextElement textEl;
	private FormLink addGlobalChoiceButton;
	private FormLayoutContainer globalChoicesCont;
	
	private CloseableModalController cmc;
	private InlineChoiceInteractionSettingsController choicesSettingsCtrl;

	private int counter = 0;
	private final File itemFile;
	private final File rootDirectory;
	private final VFSContainer rootContainer;
	private final boolean readOnly;
	private final boolean restrictedEdit;
	private List<GlobalInlineChoiceWrapper> globalChoicesWrappers = new ArrayList<>();
	private List<InlineChoiceInteractionWrapper> interactionWrappers = new ArrayList<>();
	private final InlineChoiceAssessmentItemBuilder itemBuilder;
	
	public InlineChoiceEditorController(UserRequest ureq, WindowControl wControl,
			InlineChoiceAssessmentItemBuilder itemBuilder,
			File rootDirectory, VFSContainer rootContainer, File itemFile,
			boolean restrictedEdit, boolean readOnly) {
		super(ureq, wControl, LAYOUT_DEFAULT_2_10);
		setTranslator(Util.createPackageTranslator(AssessmentTestEditorController.class, getLocale()));
		this.itemFile = itemFile;
		this.itemBuilder = itemBuilder;
		this.rootDirectory = rootDirectory;
		this.rootContainer = rootContainer;
		this.readOnly = readOnly;
		this.restrictedEdit = restrictedEdit;

		for(InlineChoiceInteractionEntry entry:itemBuilder.getInteractions()) {
			interactionWrappers.add(new InlineChoiceInteractionWrapper(entry));
		}
		
		initForm(ureq);
		updateGlobalChoices();
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
		
		String question = itemBuilder.getQuestion();
		textEl = uifactory.addRichTextElementForQTI21("desc", "form.imd.descr", question, 8, -1, itemContainer,
				formLayout, ureq.getUserSession(),  getWindowControl());
		textEl.addActionListener(FormEvent.ONCLICK);
		textEl.setElementCssClass("o_sel_assessment_item_inlinechoice_text");
		RichTextConfiguration richTextConfig = textEl.getEditorConfiguration();
		richTextConfig.setReadOnly(restrictedEdit || readOnly);
		richTextConfig.enableQTITools(false, false, false, true);
		richTextConfig.setAdditionalConfiguration(new MissingCorrectResponsesConfiguration());
		
		String globalPage = velocity_root + "/global_inline_choices.html";
		globalChoicesCont = FormLayoutContainer.createCustomFormLayout("global_choices", getTranslator(), globalPage);
		formLayout.add(globalChoicesCont);
		globalChoicesCont.setLabel("form.imd.global.inline.choices", null);

		addGlobalChoiceButton = uifactory.addFormLink("add.global.choice", formLayout, Link.BUTTON);
		addGlobalChoiceButton.setVisible(true);
		
		for(GlobalInlineChoice globalChoice:itemBuilder.getGlobalInlineChoices()) {
			globalChoicesWrappers.add(forgeRow(globalChoice));
		}

		// Submit Button
		FormLayoutContainer buttonsContainer = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsContainer.setElementCssClass("o_sel_inlinechoice_save");
		buttonsContainer.setRootForm(mainForm);
		buttonsContainer.setVisible(!readOnly);
		formLayout.add(buttonsContainer);
		uifactory.addFormSubmitButton("submit", buttonsContainer);
	}

	@Override
	public void sync(UserRequest ureq, AssessmentItemBuilder itemBuilder) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(choicesSettingsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				String solution = choicesSettingsCtrl.getSolution();
				String responseIdentifier = choicesSettingsCtrl.getResponseIdentifier().toString();
				feedbackToInlineChoiceElement(responseIdentifier, solution, choicesSettingsCtrl.hasCorrectResponse());
			} else if(event == Event.CANCELLED_EVENT) {
				cancelFeedbackToInlineChoiceElement();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void feedbackToInlineChoiceElement(String responseIdentifier, String solution, boolean correctResponse) {
		try {
			JSONObject jo = new JSONObject();
			jo.put("responseIdentifier", responseIdentifier);
			jo.put("data-qti-solution", solution);
			jo.put("data-qti-correct-response", Boolean.toString(correctResponse));
			Command jsc = new JSCommand("try { tinymce.activeEditor.execCommand('qtiUpdateInlineChoice', false, " + jo.toString() + "); } catch(e){if(window.console) console.log(e) }");
			getWindowControl().getWindowBackOffice().sendCommandTo(jsc);
		} catch (JSONException e) {
			logError("", e);
		}
	}
	
	/**
	 * This helps TinyMCE to deselect the current tool.
	 */
	private void cancelFeedbackToInlineChoiceElement() {
		try {
			Command jsc = new JSCommand("try { tinymce.activeEditor.execCommand('qtiCancelInlineChoice'); } catch(e){if(window.console) console.log(e) }");
			getWindowControl().getWindowBackOffice().sendCommandTo(jsc);
		} catch (JSONException e) {
			logError("", e);
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(choicesSettingsCtrl);
		removeAsListenerAndDispose(cmc);
		choicesSettingsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(textEl == source) {
			String cmd = ureq.getParameter("qcmd");
			if("inlinechoiceinteraction".equals(cmd)) {
				String responseIdentifier = ureq.getParameter("responseIdentifier");
				String selectedText = ureq.getParameter("selectedText");
				String newEntry = ureq.getParameter("newEntry");
				String emptySelection = ureq.getParameter("emptySelection");
				doCommitGlobalChoices();
				doInlineChoiceInteraction(ureq, responseIdentifier, selectedText, "true".equals(emptySelection), "true".equals(newEntry));
			} else if("copy-inlinechoice".equals(cmd)) {
				String responseIdentifier = ureq.getParameter("responseIdentifier");
				String sourceResponseIdentifier = ureq.getParameter("sourceResponseIdentifier");
				doCommitGlobalChoices();
				doCopyInlineChoice(responseIdentifier, sourceResponseIdentifier);
			}
		} else if(addGlobalChoiceButton == source) {
			doEnableGlobalChoices();
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("add".equals(link.getCmd()) && link.getUserObject() instanceof GlobalInlineChoiceWrapper) {
				doCommitGlobalChoices();
				doAddGlobalChoice((GlobalInlineChoiceWrapper)link.getUserObject());
			} else if("delete".equals(link.getCmd()) && link.getUserObject() instanceof GlobalInlineChoiceWrapper) {
				doCommitGlobalChoices();
				doRemoveGlobalChoice((GlobalInlineChoiceWrapper)link.getUserObject());
			}
		} else if(source instanceof TextElement) {
			TextElement el = (TextElement)source;
			if(el.getName().startsWith("gic_") && el.getUserObject() instanceof GlobalInlineChoiceWrapper) {
				((GlobalInlineChoiceWrapper)el.getUserObject()).setText();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		String questionText = textEl.getRawValue();
		if(!StringHelper.containsNonWhitespace(questionText)) {
			textEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if(!questionText.contains("<inlinechoiceinteraction")) {
			textEl.setErrorKey("error.missing.inlinechoice", null);
			allOk &= false;
		} else if(!validateCorrectResponses()) {
			textEl.setErrorKey("error.missing.inlinechoice.missing.correct", null);
			allOk &= false;
		}

		return allOk;
	}
	
	private boolean validateCorrectResponses() {
		boolean allOk = true;
		
		String rawText = textEl.getRawValue();
		for(InlineChoiceInteractionWrapper interactionEntry:interactionWrappers) {
			Identifier correctResponseId = interactionEntry.getCorrectResponseId();
			if(rawText.contains(interactionEntry.getResponseIdentifier().toString())
					&& (correctResponseId == null || interactionEntry.getInlineChoice(correctResponseId) == null)) {
				allOk &= false;
			}
		}
		
		return allOk;
	}
	
	private List<String> getMissingCorrectResponses() {
		String rawText = textEl.getRawValue();
		List<String> responseIdentifiers = new ArrayList<>();
		for(InlineChoiceInteractionWrapper interactionEntry:interactionWrappers) {
			Identifier correctResponseId = interactionEntry.getCorrectResponseId();
			if(rawText.contains(interactionEntry.getResponseIdentifier().toString())
					&& (correctResponseId == null || interactionEntry.getInlineChoice(correctResponseId) == null)) {
				responseIdentifiers.add(interactionEntry.getResponseIdentifier().toString());
			}
		}
		return responseIdentifiers;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(readOnly) return;
		
		doCommitGlobalChoices();
		
		List<GlobalInlineChoice> globalChoices = globalChoicesWrappers.stream()
				.map(GlobalInlineChoiceWrapper::toInlineChoice)
				.collect(Collectors.toList());
		itemBuilder.setGlobalInlineChoices(globalChoices);
		
		boolean checkMissingScores = itemBuilder.getScoreEvaluationMode() == ScoreEvaluation.perAnswer;
		
		Map<Identifier,InlineChoiceInteractionEntry> interactionEntries = itemBuilder.getInteractions().stream()
				.collect(Collectors.toMap(InlineChoiceInteractionEntry::getResponseIdentifier, entry -> entry));
		for(InlineChoiceInteractionWrapper interactionWrapper: interactionWrappers) {
			InlineChoiceInteractionEntry entry = interactionEntries.get(interactionWrapper.getResponseIdentifier());
			if(entry == null) {
				entry = itemBuilder.createInteraction(interactionWrapper.getResponseIdentifier().toString());
			}
			entry.setCorrectResponseId(interactionWrapper.getCorrectResponseId());
			entry.setShuffle(interactionWrapper.isShuffle());
			entry.getInlineChoices().clear();
			
			for(InlineChoice choice:interactionWrapper.getInlineChoices()) {
				entry.getInlineChoices().add(InlineChoiceAssessmentItemBuilder.cloneInlineChoice(entry.getInteraction(), choice));
			}
			
			// Set scores for new inline choices
			if(checkMissingScores) {
				Identifier correctResponseId = entry.getCorrectResponseId();
				for(InlineChoice choice:interactionWrapper.getInlineChoices()) {
					Identifier choiceIdentifier = choice.getIdentifier();
					if(entry.getScore(choiceIdentifier) == null) {
						double score = correctResponseId != null && correctResponseId.equals(choiceIdentifier) ? 1.0d : 0.0d;
						entry.putScore(choice.getIdentifier(), Double.valueOf(score));
					}
				}
			}
		}
		
		//title
		itemBuilder.setTitle(titleEl.getValue());
		//set the question with the text entries
		String questionText = textEl.getRawValue();
		itemBuilder.setQuestion(questionText);

		fireEvent(ureq, new AssessmentItemEvent(AssessmentItemEvent.ASSESSMENT_ITEM_CHANGED, itemBuilder.getAssessmentItem(), QTI21QuestionType.inlinechoice));

		itemBuilder.extractQuestions();
		itemBuilder.extractInteractions();
		itemBuilder.extractInlineChoicesSettingsFromResponseDeclaration();
		
		String question = itemBuilder.getQuestion();
		textEl.setValue(question);
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
		//
	}
	
	private void doCommitGlobalChoices() {
		for(GlobalInlineChoiceWrapper globalChoicesWrapper:globalChoicesWrappers) {
			String globalText = globalChoicesWrapper.getChoiceEl().getValue();
			globalChoicesWrapper.setText();
			String globalIdentifier = globalChoicesWrapper.getInlineChoiceIdentifier().toString();
			
			if(StringHelper.containsNoneOfCoDouSemi(globalIdentifier) && globalIdentifier.length() > 16) {
				for(InlineChoiceInteractionWrapper interactionEntry:interactionWrappers) {
					List<InlineChoice> choices = interactionEntry.getInlineChoices();
					for(InlineChoice choice:choices) {
						String choiceIdentifier = choice.getIdentifier().toString();
						if(choiceIdentifier.startsWith(globalIdentifier)) {
							choice.getTextOrVariables().clear();
							choice.getTextOrVariables().add(new TextRun(choice, globalText));
						}
					}
				}	
			}
		}
	}
	
	private void doEnableGlobalChoices() {
		doAddGlobalChoice(null);
		updateGlobalChoices();
	}
	
	private void doAddGlobalChoice(GlobalInlineChoiceWrapper wrapper) {
		int index = wrapper == null ? -1 : globalChoicesWrappers.indexOf(wrapper) + 1;

		Identifier id = IdentifierGenerator.newAsIdentifier("global-1-");
		GlobalInlineChoice globalInlineChoice = new GlobalInlineChoice(id, "");
		GlobalInlineChoiceWrapper newWrapper = forgeRow(globalInlineChoice);
		if(index >= 0 && index < globalChoicesWrappers.size()) {
			globalChoicesWrappers.add(index, newWrapper);
		} else {
			globalChoicesWrappers.add(newWrapper);
		}
		
		for(InlineChoiceInteractionWrapper interactionWrapper: interactionWrappers) {
			Identifier identifier = itemBuilder.generateIdentifier(globalInlineChoice.getIdentifier());
			InlineChoice inlineChoice = new InlineChoice(interactionWrapper.getInteractionEntry().getInteraction());
			inlineChoice.setIdentifier(identifier);
			interactionWrapper.addInlineChoice(inlineChoice);
		}

		updateGlobalChoices();
	}
	
	private void doRemoveGlobalChoice(GlobalInlineChoiceWrapper wrapper) {
		globalChoicesWrappers.remove(wrapper);
		
		String globalIdentifier = wrapper.getInlineChoiceIdentifier().toString();
		for(InlineChoiceInteractionWrapper interactionWrapper: interactionWrappers) {
			List<InlineChoice> inlineChoices = new ArrayList<>(interactionWrapper.getInlineChoices());
			for(InlineChoice inlineChoice:inlineChoices) {
				if(inlineChoice.getIdentifier().toString().startsWith(globalIdentifier)) {
					interactionWrapper.removeInlineChoice(inlineChoice);
				}
			}
			
			if(interactionWrapper.getCorrectResponseId() != null && interactionWrapper.getCorrectResponseId().toString().startsWith(globalIdentifier)) {
				interactionWrapper.setCorrectResponseId(null);
			}
		}
		
		updateGlobalChoices();
	}
	
	private void updateGlobalChoices() {
		addGlobalChoiceButton.setVisible(globalChoicesWrappers.isEmpty());
		globalChoicesCont.contextPut("wrappers", globalChoicesWrappers);
		flc.setDirty(true);
	}
	
	private GlobalInlineChoiceWrapper forgeRow(GlobalInlineChoice globalChoice) {
		String text = globalChoice.getText();
		String id = "gic_" + (counter++);
		TextElement choiceEl = uifactory.addTextElement(id, id, null, 255, text, globalChoicesCont);
		choiceEl.setDomReplacementWrapperRequired(false);
		choiceEl.addActionListener(FormEvent.ONCHANGE);
		
		FormLink addButton = uifactory.addFormLink(id.concat("_add"), "add", "", null, globalChoicesCont, Link.BUTTON | Link.NONTRANSLATED);
		addButton.setTitle(translate("add.global.choice"));
		addButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
		
		FormLink deleteButton = uifactory.addFormLink(id.concat("_del"), "delete", "", null, globalChoicesCont, Link.BUTTON | Link.NONTRANSLATED);
		deleteButton.setTitle(translate("remove.global.choice"));
		deleteButton.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
		
		GlobalInlineChoiceWrapper wrapper = new GlobalInlineChoiceWrapper(globalChoice, choiceEl, addButton, deleteButton);
		choiceEl.setUserObject(wrapper);
		addButton.setUserObject(wrapper);
		deleteButton.setUserObject(wrapper);
		return wrapper;
	}
	
	private void doCopyInlineChoice(String responseIdentifier, String sourceResponseIdentifier) {
		InlineChoiceInteractionWrapper interaction = createInlineChoiceInteraction(responseIdentifier, null);
		
		InlineChoiceInteractionWrapper sourceInteraction = getInteraction(sourceResponseIdentifier);
		if(sourceInteraction != null) {
			Identifier sourceCorrectResponseIdentifier = sourceInteraction.getCorrectResponseId();
			List<InlineChoice> sourceInlineChoices = sourceInteraction.getInlineChoices();
			if(!sourceInlineChoices.isEmpty()) {
				interaction.removeAllInlineChoices();
			}
			
			for(InlineChoice sourceInlineChoice:sourceInlineChoices) {
				Identifier sourceIdentifier = sourceInlineChoice.getIdentifier();
				InlineChoice newChoice = null;
				if(sourceIdentifier.toString().startsWith("global-")) {
					GlobalInlineChoiceWrapper globalChoice = getGlobalInlineChoice(sourceIdentifier);
					if(globalChoice != null) {
						Identifier choiceIdentifier = itemBuilder.generateIdentifier(globalChoice.getInlineChoiceIdentifier());
						newChoice = createInlineChoice(null, globalChoice.getText(), choiceIdentifier);
					}
				}
				
				if(newChoice == null) {
					Identifier choiceIdentifier = IdentifierGenerator.newAsIdentifier("inlinec");
					String text = InlineChoiceAssessmentItemBuilder.getText(sourceInlineChoice);
					newChoice = createInlineChoice(null, text, choiceIdentifier);
				}
				
				interaction.getInlineChoices().add(newChoice);
				if(sourceCorrectResponseIdentifier != null && sourceCorrectResponseIdentifier.equals(sourceIdentifier)) {
					interaction.setCorrectResponseId(newChoice.getIdentifier());
				}
			}
		}
	}
	
	private InlineChoiceInteractionWrapper getInteraction(String responseIdentifier) {
		for(InlineChoiceInteractionWrapper interactionWrapper:interactionWrappers) {
			if(interactionWrapper.getResponseIdentifier().toString().equals(responseIdentifier)) {
				return interactionWrapper;
			}
		}
		return null;
	}
	
	private GlobalInlineChoiceWrapper getGlobalInlineChoice(Identifier inlineChoiceId) {
		String identifier = inlineChoiceId.toString();
		
		for(GlobalInlineChoiceWrapper globalChoiceWrapper:globalChoicesWrappers) {
			String globalId = globalChoiceWrapper.getInlineChoiceIdentifier().toString();	
			if(identifier.startsWith(globalId)) {
				return globalChoiceWrapper;
			}
		}
		return null;
	}
	
	private void doInlineChoiceInteraction(UserRequest ureq, String responseIdentifier, String selectedText,
			boolean emptySelection, boolean newEntry) {
		if(choicesSettingsCtrl != null) return;
		
		if(emptySelection) {
			selectedText = null;
		}
		
		InlineChoiceInteractionWrapper interactionWrapper;
		if(newEntry) {
			interactionWrapper = createInlineChoiceInteraction(responseIdentifier, selectedText);
		} else {
			interactionWrapper = getInteraction(responseIdentifier);
			if(interactionWrapper == null) {
				interactionWrapper = createInlineChoiceInteraction(responseIdentifier, selectedText);
			}
		}
		
		this.itemBuilder.getScoreEvaluationMode();
		
		choicesSettingsCtrl = new InlineChoiceInteractionSettingsController(ureq, getWindowControl(), interactionWrapper,
				restrictedEdit, readOnly);
		listenTo(choicesSettingsCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), choicesSettingsCtrl.getInitialComponent(), true, translate("title.add") );
		cmc.activate();
		listenTo(cmc);
	}
	
	private InlineChoiceInteractionWrapper createInlineChoiceInteraction(String responseIdentifier, String selectedText) {
		InlineChoiceInteractionEntry choiceBlock = new InlineChoiceInteractionEntry(Identifier.parseString(responseIdentifier));
		
		List<GlobalInlineChoiceWrapper> globalChoices = globalChoicesWrappers;
		if(!globalChoices.isEmpty()) {
			for(GlobalInlineChoiceWrapper globalChoice:globalChoices) {
				Identifier choiceIdentifier = itemBuilder.generateIdentifier(globalChoice.getInlineChoiceIdentifier());
				InlineChoice gChoice = createInlineChoice(null, globalChoice.getText(), choiceIdentifier);
				choiceBlock.getInlineChoices().add(gChoice);
			}
		}
		
		if(selectedText != null) {
			appendNewInlineChoice(choiceBlock, selectedText);
		} else if(globalChoices.isEmpty()) {
			appendNewInlineChoice(choiceBlock, "");
		}
		
		InlineChoiceInteractionWrapper interactionWrapper = new InlineChoiceInteractionWrapper(choiceBlock);
		interactionWrappers.add(interactionWrapper);
		return interactionWrapper;
	}
	
	private void appendNewInlineChoice(InlineChoiceInteractionEntry choiceBlock, String text) {
		Identifier responseId = IdentifierGenerator.newAsIdentifier("inlinec");
		InlineChoice newChoice = createInlineChoice(null, text, responseId);
		choiceBlock.getInlineChoices().add(newChoice);
		choiceBlock.setCorrectResponseId(responseId);
	}
	
	public static class InlineChoiceInteractionWrapper {
		
		private boolean shuffle;
		private Identifier correctResponseId;
		private final List<InlineChoice> inlineChoices;
		private final InlineChoiceInteractionEntry interactionEntry;
		
		public InlineChoiceInteractionWrapper(InlineChoiceInteractionEntry interactionEntry) {
			this.interactionEntry = interactionEntry;
			shuffle = interactionEntry.isShuffle();
			correctResponseId = interactionEntry.getCorrectResponseId();
			inlineChoices = interactionEntry.getInlineChoices().stream()
					.map(choice -> InlineChoiceAssessmentItemBuilder.cloneInlineChoice(interactionEntry.getInteraction(), choice))
					.collect(Collectors.toList());
		}
		
		public InlineChoiceInteractionEntry getInteractionEntry() {
			return interactionEntry;
		}
		
		public List<InlineChoice> getInlineChoices() {
			return inlineChoices;
		}
		
		public void addInlineChoice(InlineChoice inlineChoice) {
			inlineChoices.add(inlineChoice);
		}
		
		public void removeInlineChoice(InlineChoice inlineChoice) {
			inlineChoices.remove(inlineChoice);
		}
		
		public void removeAllInlineChoices() {
			inlineChoices.clear();
		}
		
		public InlineChoice getInlineChoice(Identifier identifier) {
			for(InlineChoice inlineChoice:inlineChoices) {
				if(inlineChoice.getIdentifier().equals(identifier)) {
					return inlineChoice;
				}
			}
			return null;
		}
		
		public boolean isShuffle() {
			return shuffle;
		}
		
		public void setShuffle(boolean shuffle) {
			this.shuffle = shuffle;
		}
		
		public Identifier getResponseIdentifier() {
			return interactionEntry.getResponseIdentifier();
		}
		
		public Identifier getCorrectResponseId() {
			return correctResponseId;
		}
		
		public void setCorrectResponseId(Identifier id) {
			this.correctResponseId = id;
		}
	}
	
	public static class GlobalInlineChoiceWrapper {
		
		private String text;
		private Identifier identifier;
		
		private final TextElement choiceEl;
		private final FormLink addButton;
		private final FormLink deleteButton;
		
		public GlobalInlineChoiceWrapper(GlobalInlineChoice inlineChoice, TextElement choiceEl, FormLink addButton, FormLink deleteButton) {
			identifier = inlineChoice.getIdentifier();
			text = inlineChoice.getText();
			this.choiceEl = choiceEl;
			this.addButton = addButton;
			this.deleteButton = deleteButton;
		}
		
		public Identifier getInlineChoiceIdentifier() {
			return identifier;
		}

		public GlobalInlineChoice toInlineChoice() {
			return new GlobalInlineChoice(identifier, getText());
		}
		
		public String getText() {
			return text;
		}
		
		public void setText() {
			text = choiceEl.getValue();
		}

		public TextElement getChoiceEl() {
			return choiceEl;
		}

		public FormLink getAddButton() {
			return addButton;
		}

		public FormLink getDeleteButton() {
			return deleteButton;
		}
	}
	
	private class MissingCorrectResponsesConfiguration implements RichTextConfigurationDelegate {
		@Override
		public void appendConfigToTinyJSArray_4(StringOutput out, Translator translator) {
			List<String> missingCorrectResponses = getMissingCorrectResponses();
			out.append("missingCorrectResponses: [");
			for(int i=missingCorrectResponses.size(); i-->0; ) {
				out.append("'").append(missingCorrectResponses.get(i)).append("'");
				if(i > 0) {
					out.append(",");
				}
			}
			out.append("],\n");
			out.append("correctResponses: [");
			boolean first = true;
			for(InlineChoiceInteractionWrapper interactionEntry:interactionWrappers) {
				Identifier responseIdentifier = interactionEntry.getResponseIdentifier();
				Identifier correctResponseId = interactionEntry.getCorrectResponseId();
				InlineChoice correctChoice = interactionEntry.getInlineChoice(correctResponseId);
				if(correctChoice != null) {
					if(first) {
						first = false;
					} else {
						out.append(",");
					}
					String text = InlineChoiceAssessmentItemBuilder.getText(correctChoice);
					out.append("{ id:\"").append(responseIdentifier.toString()).append("\", value:\"").append(text).append("\"}");
				}
			}
			out.append("],");
		}
	}
}
