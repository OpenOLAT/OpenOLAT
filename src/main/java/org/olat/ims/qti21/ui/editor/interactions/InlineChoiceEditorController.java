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
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.winmgr.Command;
import org.olat.core.gui.control.winmgr.JSCommand;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.ims.qti21.model.IdentifierGenerator;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.InlineChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.InlineChoiceAssessmentItemBuilder.GlobalInlineChoice;
import org.olat.ims.qti21.model.xml.interactions.InlineChoiceAssessmentItemBuilder.InlineChoiceInteractionEntry;
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
		
		
		String globalPage = velocity_root + "/global_inline_choices.html";
		globalChoicesCont = FormLayoutContainer.createCustomFormLayout("global_choices", getTranslator(), globalPage);
		formLayout.add(globalChoicesCont);
		globalChoicesCont.setLabel("form.imd.global.inline.choices", null);
		globalChoicesCont.setVisible(false);

		addGlobalChoiceButton = uifactory.addFormLink("add.global.choices", formLayout, Link.BUTTON);
		addGlobalChoiceButton.setVisible(true);
		
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
				feedbackToInlineChoiceElement(responseIdentifier, solution);
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
	
	private void feedbackToInlineChoiceElement(String responseIdentifier, String solution) {
		try {
			JSONObject jo = new JSONObject();
			jo.put("responseIdentifier", responseIdentifier);
			jo.put("data-qti-solution", solution);
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
				GlobalInlineChoiceWrapper gicw = (GlobalInlineChoiceWrapper)el.getUserObject();
				gicw.getInlineChoice().setText(el.getValue());
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
		
		for(InlineChoiceInteractionEntry interactionEntry:itemBuilder.getInteractions()) {
			Identifier correctResponseId = interactionEntry.getCorrectResponseId();
			if(correctResponseId == null || interactionEntry.getInlineChoice(correctResponseId) == null) {
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(readOnly) return;
		
		doCommitGlobalChoices();
		
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
			globalChoicesWrapper.getInlineChoice().setText(globalText);
			String globalIdentifier = globalChoicesWrapper.getInlineChoiceIdentifier().toString();
			
			if(StringHelper.containsNoneOfCoDouSemi(globalIdentifier) && globalIdentifier.length() > 16) {
				for(InlineChoiceInteractionEntry interactionEntry:itemBuilder.getInteractions()) {
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
		itemBuilder.addGlobalInlineChoice(-1);
		updateGlobalChoices();
		flc.setDirty(true);
	}
	
	private void doAddGlobalChoice(GlobalInlineChoiceWrapper wrapper) {
		int index = wrapper == null ? -1 : globalChoicesWrappers.indexOf(wrapper) + 1;
		itemBuilder.addGlobalInlineChoice(index);
		updateGlobalChoices();
	}
	
	private void doRemoveGlobalChoice(GlobalInlineChoiceWrapper wrapper) {
		itemBuilder.removeGlobalInlineChoice(wrapper.getInlineChoice());
		updateGlobalChoices();
	}
	
	private void updateGlobalChoices() {
		Map<Identifier,GlobalInlineChoiceWrapper> wrapperMap = globalChoicesWrappers.stream()
				.collect(Collectors.toMap(GlobalInlineChoiceWrapper::getInlineChoiceIdentifier, wrapper -> wrapper, (u, v) -> u));

		List<GlobalInlineChoice> globalChoices = itemBuilder.getGlobalInlineChoices();
		List<GlobalInlineChoiceWrapper> wrappers = new ArrayList<>();
		for(GlobalInlineChoice globalChoice:globalChoices) {
			Identifier identifier = globalChoice.getIdentifier();
			GlobalInlineChoiceWrapper wrapper = wrapperMap.get(identifier);
			if(wrapper == null) {
				wrapper = forgeRow(globalChoice);
			} else {
				wrapper.getChoiceEl().setValue(globalChoice.getText());
			}
			wrappers.add(wrapper);
		}

		addGlobalChoiceButton.setVisible(globalChoices.isEmpty());
		globalChoicesCont.setVisible(!globalChoices.isEmpty());
		globalChoicesCont.contextPut("wrappers", wrappers);
		globalChoicesWrappers = wrappers;
		
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
		InlineChoiceInteractionEntry interaction = createInlineChoiceBlock(responseIdentifier, null);
		
		InlineChoiceInteractionEntry sourceInteraction = itemBuilder.getInteraction(sourceResponseIdentifier);
		if(sourceInteraction != null) {
			Identifier sourceCorrectResponseIdentifier = sourceInteraction.getCorrectResponseId();
			List<InlineChoice> sourceInlineChoices = sourceInteraction.getInlineChoices();
			
			for(InlineChoice sourceInlineChoice:sourceInlineChoices) {
				Identifier sourceIdentifier = sourceInlineChoice.getIdentifier();
				InlineChoice newChoice = null;
				if(sourceIdentifier.toString().startsWith("global-")) {
					GlobalInlineChoice globalChoice = itemBuilder.getGlobalInlineChoice(sourceIdentifier);
					if(globalChoice != null) {
						Identifier choiceIdentifier = itemBuilder.generateIdentifier(globalChoice);
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
	
	private void doInlineChoiceInteraction(UserRequest ureq, String responseIdentifier, String selectedText,
			boolean emptySelection, boolean newEntry) {
		if(choicesSettingsCtrl != null) return;
		
		if(emptySelection) {
			selectedText = null;
		}
		
		InlineChoiceInteractionEntry interaction;
		if(newEntry) {
			interaction = createInlineChoiceBlock(responseIdentifier, selectedText);
		} else {
			interaction = itemBuilder.getInteraction(responseIdentifier);
			if(interaction == null) {
				interaction = createInlineChoiceBlock(responseIdentifier, selectedText);
			}
		}
		
		choicesSettingsCtrl = new InlineChoiceInteractionSettingsController(ureq, getWindowControl(), interaction,
				restrictedEdit, readOnly);
		listenTo(choicesSettingsCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), choicesSettingsCtrl.getInitialComponent(), true, translate("title.add") );
		cmc.activate();
		listenTo(cmc);
	}
	
	private InlineChoiceInteractionEntry createInlineChoiceBlock(String responseIdentifier, String selectedText) {
		InlineChoiceInteractionEntry choiceBlock = itemBuilder.createInteraction(responseIdentifier);
		
		List<GlobalInlineChoice> globalChoices = itemBuilder.getGlobalInlineChoices();
		if(!globalChoices.isEmpty()) {
			for(GlobalInlineChoice globalChoice:globalChoices) {
				Identifier choiceIdentifier = itemBuilder.generateIdentifier(globalChoice);
				InlineChoice gChoice = createInlineChoice(null, globalChoice.getText(), choiceIdentifier);
				choiceBlock.getInlineChoices().add(gChoice);
			}
		}
		
		if(selectedText != null) {
			appendNewInlineChoice(choiceBlock, selectedText);
		} else if(globalChoices.isEmpty()) {
			appendNewInlineChoice(choiceBlock, "");
		}
		return choiceBlock;
	}
	
	private void appendNewInlineChoice(InlineChoiceInteractionEntry choiceBlock, String text) {
		Identifier responseId = IdentifierGenerator.newAsIdentifier("inlinec");
		InlineChoice newChoice = createInlineChoice(null, text, responseId);
		choiceBlock.getInlineChoices().add(newChoice);
		choiceBlock.setCorrectResponseId(responseId);
	}
	
	public class GlobalInlineChoiceWrapper {
		
		private final GlobalInlineChoice inlineChoice;
		private final TextElement choiceEl;
		private final FormLink addButton;
		private final FormLink deleteButton;
		
		public GlobalInlineChoiceWrapper(GlobalInlineChoice inlineChoice, TextElement choiceEl, FormLink addButton, FormLink deleteButton) {
			this.inlineChoice = inlineChoice;
			this.choiceEl = choiceEl;
			this.addButton = addButton;
			this.deleteButton = deleteButton;
		}
		
		public Identifier getInlineChoiceIdentifier() {
			return inlineChoice.getIdentifier();
		}

		public GlobalInlineChoice getInlineChoice() {
			return inlineChoice;
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
}
