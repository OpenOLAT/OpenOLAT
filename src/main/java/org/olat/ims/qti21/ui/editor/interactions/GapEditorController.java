/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.ims.qti21.ui.editor.interactions;

import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.createInlineChoice;

import java.io.File;
import java.io.StringReader;
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
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
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
import org.olat.core.gui.control.winmgr.functions.FunctionCommand;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.ims.qti21.model.IdentifierGenerator;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.interactions.GapAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.GapAssessmentItemBuilder.AbstractEntry;
import org.olat.ims.qti21.model.xml.interactions.GapAssessmentItemBuilder.GlobalInlineChoice;
import org.olat.ims.qti21.model.xml.interactions.GapAssessmentItemBuilder.InlineChoiceInteractionEntry;
import org.olat.ims.qti21.model.xml.interactions.GapAssessmentItemBuilder.NumericalEntry;
import org.olat.ims.qti21.model.xml.interactions.GapAssessmentItemBuilder.TextEntry;
import org.olat.ims.qti21.model.xml.interactions.SimpleChoiceAssessmentItemBuilder.ScoreEvaluation;
import org.olat.ims.qti21.ui.editor.AssessmentTestEditorController;
import org.olat.ims.qti21.ui.editor.events.AssessmentItemEvent;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;
import uk.ac.ed.ph.jqtiplus.internal.util.StringUtilities;
import uk.ac.ed.ph.jqtiplus.node.content.basic.TextRun;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.InlineChoice;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 24.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GapEditorController extends FormBasicController {
	
	private TextElement titleEl;
	private RichTextElement textEl;
	private FormLink convertToGapMixButton;
	private FormToggle addGlobalChoiceToggle;
	private FormLayoutContainer convertToCont;
	private FormLayoutContainer globalChoicesCont;

	private CloseableModalController cmc;
	private FIBTextEntrySettingsController textEntrySettingsCtrl;
	private InlineChoiceInteractionSettingsController choicesSettingsCtrl;
	private FIBNumericalEntrySettingsController numericalEntrySettingsCtrl;

	private final File itemFile;
	private final File rootDirectory;
	private final VFSContainer rootContainer;
	
	private int counter = 0;
	private final boolean readOnly;
	private final boolean restrictedEdit;
	private final GapAssessmentItemBuilder itemBuilder;
	
	private boolean textEntry;
	private boolean inlineChoice;
	private boolean numericalEntry;
	private final QTI21QuestionType type;
	
	private List<GlobalInlineChoiceWrapper> globalChoicesWrappers = new ArrayList<>();
	private List<InlineChoiceInteractionWrapper> interactionWrappers = new ArrayList<>();
	
	public GapEditorController(UserRequest ureq, WindowControl wControl, GapAssessmentItemBuilder itemBuilder,
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
		
		for(InlineChoiceInteractionEntry entry:itemBuilder.getInlineChoiceInteractions()) {
			interactionWrappers.add(new InlineChoiceInteractionWrapper(entry));
		}
		
		type = itemBuilder.getQuestionType();
		textEntry = type != QTI21QuestionType.numerical && type != QTI21QuestionType.inlinechoice;
		numericalEntry = type != QTI21QuestionType.fib && type != QTI21QuestionType.inlinechoice;
		inlineChoice = type != QTI21QuestionType.fib && type != QTI21QuestionType.numerical;
		
		initForm(ureq);
		updateGlobalChoices();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormContextHelp("manual_user/learningresources/Configure_test_questions/");
		mainForm.setMultipartEnabled(true);
		
		initMixingWarning(formLayout);
		
		titleEl = uifactory.addTextElement("title", "form.imd.title", -1, itemBuilder.getTitle(), formLayout);
		titleEl.setElementCssClass("o_sel_assessment_item_title");
		titleEl.setMandatory(true);
		titleEl.setEnabled(!readOnly);
		
		String relativePath = rootDirectory.toPath().relativize(itemFile.toPath().getParent()).toString();
		VFSContainer itemContainer = (VFSContainer)rootContainer.resolve(relativePath);
		
		String question = itemBuilder.getQuestion();
		textEl = uifactory.addRichTextElementForQTI21("desc", "form.imd.descr", question, 16, -1, itemContainer,
				formLayout, ureq.getUserSession(),  getWindowControl());
		textEl.addActionListener(FormEvent.ONCLICK);
		textEl.setElementCssClass("o_sel_assessment_item_gap_text");
		RichTextConfiguration richTextConfig = textEl.getEditorConfiguration();
		richTextConfig.setReadOnly(restrictedEdit || readOnly);
		
		richTextConfig.enableQTITools(textEntry, numericalEntry, false, inlineChoice);
		richTextConfig.setAdditionalConfiguration(new MissingCorrectResponsesConfiguration());
		
		addGlobalChoiceToggle = uifactory.addToggleButton("enable.global.choice", "enable.global.choice", translate("on"), translate("off"), formLayout);
		addGlobalChoiceToggle.setVisible(inlineChoice);
		
		String globalPage = velocity_root + "/global_inline_choices.html";
		globalChoicesCont = uifactory.addCustomFormLayout("global_choices", null, globalPage, formLayout);
		globalChoicesCont.setElementCssClass("o_inlinechoice_globalchoices");
		for(GlobalInlineChoice globalChoice:itemBuilder.getGlobalInlineChoices()) {
			globalChoicesWrappers.add(forgeRow(globalChoice));
		}
		addGlobalChoiceToggle.toggle(inlineChoice);
		globalChoicesCont.setVisible(inlineChoice && globalChoicesWrappers.size() > 0);

		// Submit Button
		FormLayoutContainer buttonsContainer = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		buttonsContainer.setElementCssClass("o_sel_gap_save");
		buttonsContainer.setVisible(!readOnly);
		uifactory.addFormSubmitButton("submit", buttonsContainer);
	}
	
	private void initMixingWarning(FormItemContainer formLayout) {
		int numOfText = 0;
		int numOfNumerical = 0;
		for(AbstractEntry entry:itemBuilder.getTextEntries()) {
			if(entry instanceof TextEntry) {
				numOfText++;
			} else if(entry instanceof NumericalEntry) {
				numOfNumerical++;
			}
		}
		
		if(numOfText > 0 && numOfNumerical > 0 && type != QTI21QuestionType.gapmixed) {
			String page = velocity_root + "/convert_type.html";
			convertToCont = uifactory.addCustomFormLayout("convertcont", null, page, formLayout);
			convertToGapMixButton = uifactory.addFormLink("convert.to", "convert.to.gapmix", null, convertToCont, Link.BUTTON);
			convertToCont.contextPut("message", translate("convert.to.gapmix.explain"));
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(textEntrySettingsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				String solution = textEntrySettingsCtrl.getSolution();
				String responseIdentifier = textEntrySettingsCtrl.getResponseIdentifier().toString();
				feedbackToTextElement(responseIdentifier, "string", solution);
			} else if(event == Event.CANCELLED_EVENT) {
				cancelFeedbackToTextElement();
			}
			cmc.deactivate();
			cleanUp();
		} else if(numericalEntrySettingsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				Double val = numericalEntrySettingsCtrl.getSolution();
				String solution = val == null ? "" : Double.toString(val);
				String responseIdentifier = numericalEntrySettingsCtrl.getResponseIdentifier().toString();
				feedbackToTextElement(responseIdentifier, "float", solution);
			} else if(event == Event.CANCELLED_EVENT) {
				cancelFeedbackToTextElement();
			}
			cmc.deactivate();
			cleanUp();
		} else if(choicesSettingsCtrl == source) {
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
			getWindowControl().getWindowBackOffice().sendCommandTo(FunctionCommand.tinyMCEExec("qtiUpdateInlineChoice", jo));
		} catch (JSONException e) {
			logError("", e);
		}
	}
	
	private void feedbackToTextElement(String responseIdentifier, String gapType, String solution) {
		try {
			JSONObject jo = new JSONObject();
			jo.put("responseIdentifier", responseIdentifier);
			jo.put("data-qti-solution", solution);
			jo.put("data-qti-gap-type", gapType);
			getWindowControl().getWindowBackOffice().sendCommandTo(FunctionCommand.tinyMCEExec("qtiUpdateTextEntry", jo));
		} catch (JSONException e) {
			logError("", e);
		}
	}

	/**
	 * This helps TinyMCE to deselect the current tool.
	 */
	private void cancelFeedbackToInlineChoiceElement() {
		try {
			getWindowControl().getWindowBackOffice().sendCommandTo(FunctionCommand.tinyMCEExec("qtiCancelInlineChoice", null));
		} catch (JSONException e) {
			logError("", e);
		}
	}
	
	/**
	 * This helps TinyMCE to deselect the current tool.
	 */
	private void cancelFeedbackToTextElement() {
		try {
			getWindowControl().getWindowBackOffice().sendCommandTo(FunctionCommand.tinyMCEExec("qtiCancelTextEntry", null));
		} catch (JSONException e) {
			logError("", e);
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(numericalEntrySettingsCtrl);
		removeAsListenerAndDispose(textEntrySettingsCtrl);
		removeAsListenerAndDispose(choicesSettingsCtrl);
		removeAsListenerAndDispose(cmc);
		numericalEntrySettingsCtrl = null;
		textEntrySettingsCtrl = null;
		choicesSettingsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(textEl == source) {
			String cmd = event.getCommand();
			String qcmd = ureq.getParameter("qcmd");
			if("gapentry".equals(cmd)) {
				String responseIdentifier = ureq.getParameter("responseIdentifier");
				String selectedText = ureq.getParameter("selectedText");
				String type = ureq.getParameter("gapType");
				String newEntry = ureq.getParameter("newEntry");
				String emptySolution = ureq.getParameter("emptySolution");
				doCommitGlobalChoices();
				doGapEntry(ureq, responseIdentifier, selectedText, emptySolution, type, "true".equals(newEntry));
			} else if("copy-gapentry".equals(cmd)) {
				String responseIdentifier = ureq.getParameter("responseIdentifier");
				String selectedText = ureq.getParameter("selectedText");
				String type = ureq.getParameter("gapType");
				doCommitGlobalChoices();
				doCopyGapEntry(responseIdentifier, selectedText, type);
			} else if("inlinechoiceinteraction".equals(qcmd)) {
				String responseIdentifier = ureq.getParameter("responseIdentifier");
				String selectedText = ureq.getParameter("selectedText");
				String newEntry = ureq.getParameter("newEntry");
				String emptySelection = ureq.getParameter("emptySelection");
				doCommitGlobalChoices();
				doInlineChoiceInteraction(ureq, responseIdentifier, selectedText, "true".equals(emptySelection), "true".equals(newEntry));
			} else if("copy-inlinechoice".equals(qcmd)) {
				String responseIdentifier = ureq.getParameter("responseIdentifier");
				String sourceResponseIdentifier = ureq.getParameter("sourceResponseIdentifier");
				doCommitGlobalChoices();
				doCopyInlineChoice(responseIdentifier, sourceResponseIdentifier);
			}
		} else if(addGlobalChoiceToggle == source) {
			if(addGlobalChoiceToggle.isOn()) {
				doEnableGlobalChoices();
			}
		} else if(convertToGapMixButton == source) {
			doConvertToGapMixed(ureq);
		} else if(source instanceof FormLink link) {
			if("add".equals(link.getCmd()) && link.getUserObject() instanceof GlobalInlineChoiceWrapper choiceWrapper) {
				doCommitGlobalChoices();
				doAddGlobalChoice(choiceWrapper);
			} else if("delete".equals(link.getCmd()) && link.getUserObject() instanceof GlobalInlineChoiceWrapper choiceWrapper) {
				doCommitGlobalChoices();
				doRemoveGlobalChoice(choiceWrapper);
			}
		} else if(source instanceof TextElement el) {
			if(el.getName().startsWith("gic_") && el.getUserObject() instanceof GlobalInlineChoiceWrapper choiceWrapper) {
				choiceWrapper.setText();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		titleEl.clearError();
		if(!StringHelper.containsNonWhitespace(titleEl.getValue())) {
			titleEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}

		String questionText = textEl.getValue();
		QTI21QuestionType type = itemBuilder.getQuestionType();
		if(!StringHelper.containsNonWhitespace(questionText)) {
			textEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else if((type == QTI21QuestionType.fib || type == QTI21QuestionType.numerical)
				&& !questionText.contains("<textentryinteraction")) {
			textEl.setErrorKey("error.missing.gap");
			allOk &= false;
		} else if(type == QTI21QuestionType.inlinechoice && !questionText.contains("<inlinechoiceinteraction")) {
			textEl.setErrorKey("error.missing.inlinechoice");
			allOk &= false;
		} else if(type == QTI21QuestionType.gapmixed && !questionText.contains("<inlinechoiceinteraction")
				&& !questionText.contains("<textentryinteraction")) {
			textEl.setErrorKey("error.missing.gap");
			allOk &= false;
		} else if(!validateCorrectResponses()) {
			textEl.setErrorKey("error.missing.inlinechoice.missing.correct");
			allOk &= false;
		}

		return allOk;
	}
	
	private boolean validateCorrectResponses() {
		boolean allOk = true;
		
		String rawText = textEl.getValue();
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
		String rawText = textEl.getValue();
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
		
		Map<Identifier,InlineChoiceInteractionEntry> interactionEntries = itemBuilder.getInlineChoiceInteractions().stream()
				.collect(Collectors.toMap(InlineChoiceInteractionEntry::getResponseIdentifier, entry -> entry));
		for(InlineChoiceInteractionWrapper interactionWrapper: interactionWrappers) {
			InlineChoiceInteractionEntry entry = interactionEntries.get(interactionWrapper.getResponseIdentifier());
			if(entry == null) {
				entry = itemBuilder.createInlineChoiceEntry(interactionWrapper.getResponseIdentifier());
			}
			entry.setCorrectResponseId(interactionWrapper.getCorrectResponseId());
			entry.setShuffle(interactionWrapper.isShuffle());
			entry.getInlineChoices().clear();
			
			for(InlineChoice choice:interactionWrapper.getInlineChoices()) {
				entry.getInlineChoices().add(GapAssessmentItemBuilder.cloneInlineChoice(entry.getInteraction(), choice));
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
		String questionText = textEl.getValue();
		extractSolution(questionText);
		itemBuilder.setQuestion(questionText);

		// notify
		fireEvent(ureq, new AssessmentItemEvent(AssessmentItemEvent.ASSESSMENT_ITEM_CHANGED,
				itemBuilder.getAssessmentItem(), itemBuilder.getQuestionType()));

		itemBuilder.extractQuestions();
		itemBuilder.extractEntriesSettingsFromResponseDeclaration();
		
		String question = itemBuilder.getQuestion();
		textEl.setValue(question);
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
		//
	}
	
	private void doConvertToGapMixed(UserRequest ureq) {
		itemBuilder.changeQuestionType(QTI21QuestionType.gapmixed);
		convertToCont.setVisible(false);
		// notify
		fireEvent(ureq, new AssessmentItemEvent(AssessmentItemEvent.ASSESSMENT_ITEM_CHANGED,
				itemBuilder.getAssessmentItem(), QTI21QuestionType.gapmixed));
	}
	
	private void doCopyGapEntry(String responseIdentifier, String selectedText, String type) {
		AbstractEntry interaction = itemBuilder.getEntry(responseIdentifier);
		if(interaction == null) {
			createEntry(responseIdentifier, selectedText, type, true);
		}
	}

	private void doGapEntry(UserRequest ureq, String responseIdentifier, String selectedText, String emptySolution, String type, boolean newEntry) {
		if(textEntrySettingsCtrl != null || numericalEntrySettingsCtrl != null) return;
		
		boolean add = false;
		AbstractEntry interaction = itemBuilder.getEntry(responseIdentifier);
		if(interaction == null) {
			add = true;
			interaction = createEntry(responseIdentifier, selectedText, type, newEntry);
		} else if(StringHelper.containsNonWhitespace(selectedText)) {
			updateSolution(interaction, selectedText, emptySolution);
		}
		
		if(interaction instanceof TextEntry textEntry) {
			textEntrySettingsCtrl = new FIBTextEntrySettingsController(ureq, getWindowControl(), textEntry,
					restrictedEdit, readOnly);
			listenTo(textEntrySettingsCtrl);
			
			String title = translate(add ? "title.add.text.entry" : "title.edit.text.entry");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), textEntrySettingsCtrl.getInitialComponent(), true, title);
			cmc.activate();
			listenTo(cmc);
		} else if(interaction instanceof NumericalEntry numericalEntry) {
			numericalEntrySettingsCtrl = new FIBNumericalEntrySettingsController(ureq, getWindowControl(), numericalEntry,
					restrictedEdit, readOnly);
			listenTo(numericalEntrySettingsCtrl);

			String title = translate(add ? "title.add.numerical.entry" : "title.edit.numerical.entry");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), numericalEntrySettingsCtrl.getInitialComponent(), true, title);
			cmc.activate();
			listenTo(cmc);
		}
	}
	
	private AbstractEntry createEntry(String responseIdentifier, String selectedText, String type, boolean newEntry) {
		AbstractEntry interaction = null;
		if("string".equalsIgnoreCase(type)) {
			TextEntry textInteraction = itemBuilder.createTextEntry(responseIdentifier);
			if(StringHelper.containsNonWhitespace(selectedText)) {
				String[] alternatives = selectedText.split(",");
				for(String alternative:alternatives) {
					if(StringHelper.containsNonWhitespace(alternative)) {
						alternative = alternative.trim();
						if(textInteraction.getSolution() == null) {
							textInteraction.setSolution(alternative);
						} else {
							textInteraction.addAlternative(alternative, textInteraction.getScore());
						}
					}
				}
				if(alternatives.length > 0) {
					String solution = alternatives[0];
					if(newEntry && "gap".equals(solution)) {
						solution = "";
					}
					textInteraction.setSolution(solution);
				}
			}
			interaction = textInteraction;
		} else if("float".equalsIgnoreCase(type)) {
			NumericalEntry numericalInteraction = itemBuilder.createNumericalEntry(responseIdentifier);
			if(newEntry && "gap".equals(selectedText)) {
				//skip it, it's a placeholder
			} else if(StringHelper.containsNonWhitespace(selectedText)) {
				try {
					Double val = Double.parseDouble(selectedText.trim());
					numericalInteraction.setSolution(val);
				} catch (NumberFormatException e) {
					//
				}
			}
			interaction = numericalInteraction;
		}
		return interaction;
	}
	
	private void extractSolution(String content) {
		try {
			HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALTER_INFOSET);
			parser.setContentHandler(new SolutionExtractorHandler());
			parser.parse(new InputSource(new StringReader(content)));
		} catch (Exception e) {
			logError("", e);
		}
	}
	
	private void updateSolution(AbstractEntry entry, String solution, String solutionEmpty) {
		if(entry == null) {
			//problem
		} else if(entry instanceof TextEntry) {
			if("true".equals(solutionEmpty)) {
				((TextEntry)entry).setSolution("");
			} else {
				solution = itemBuilder.unescapeDataQtiSolution(solution);
				((TextEntry)entry).setSolution(solution);
			}
		} else if(entry instanceof NumericalEntry) {
			try {
				double val = Double.parseDouble(solution);
				((NumericalEntry)entry).setSolution(val);
			} catch (NumberFormatException e) {
				logError("", e);
			}
		}
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
			Identifier identifier = itemBuilder.generateGlobalChoiceIdentifier(globalInlineChoice.getIdentifier());
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
		addGlobalChoiceToggle.setVisible(inlineChoice);
		addGlobalChoiceToggle.toggle(!globalChoicesWrappers.isEmpty());
		globalChoicesCont.contextPut("wrappers", globalChoicesWrappers);
		globalChoicesCont.setVisible(inlineChoice && globalChoicesWrappers.size() > 0);
		flc.setDirty(true);
	}
	
	private GlobalInlineChoiceWrapper forgeRow(GlobalInlineChoice globalChoice) {
		String text = globalChoice.getText();
		String id = "gic_" + (counter++);
		TextElement choiceEl = uifactory.addTextElement(id, id, null, 255, text, globalChoicesCont);
		choiceEl.setDomReplacementWrapperRequired(false);
		choiceEl.addActionListener(FormEvent.ONCHANGE);
		choiceEl.setPlaceholderKey("global.choice.placeholder", null);
		
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
						Identifier choiceIdentifier = itemBuilder.generateGlobalChoiceIdentifier(globalChoice.getInlineChoiceIdentifier());
						newChoice = createInlineChoice(null, globalChoice.getText(), choiceIdentifier);
					}
				}
				
				if(newChoice == null) {
					Identifier choiceIdentifier = IdentifierGenerator.newAsIdentifier("inlinec");
					String text = GapAssessmentItemBuilder.getText(sourceInlineChoice);
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
		
		boolean add = false;
		InlineChoiceInteractionWrapper interactionWrapper;
		if(newEntry) {
			add = true;
			interactionWrapper = createInlineChoiceInteraction(responseIdentifier, selectedText);
		} else {
			interactionWrapper = getInteraction(responseIdentifier);
			if(interactionWrapper == null) {
				add = true;
				interactionWrapper = createInlineChoiceInteraction(responseIdentifier, selectedText);
			}
		}
		
		this.itemBuilder.getScoreEvaluationMode();
		
		choicesSettingsCtrl = new InlineChoiceInteractionSettingsController(ureq, getWindowControl(), interactionWrapper,
				restrictedEdit, readOnly);
		listenTo(choicesSettingsCtrl);
		
		String title = translate(add ? "title.add.dropdown.entry" : "title.edit.dropdown.entry");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), choicesSettingsCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private InlineChoiceInteractionWrapper createInlineChoiceInteraction(String responseIdentifier, String selectedText) {
		InlineChoiceInteractionEntry choiceBlock = new InlineChoiceInteractionEntry(Identifier.parseString(responseIdentifier));
		
		List<GlobalInlineChoiceWrapper> globalChoices = globalChoicesWrappers;
		if(!globalChoices.isEmpty()) {
			for(GlobalInlineChoiceWrapper globalChoice:globalChoices) {
				Identifier choiceIdentifier = itemBuilder.generateGlobalChoiceIdentifier(globalChoice.getInlineChoiceIdentifier());
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
					.map(choice -> GapAssessmentItemBuilder.cloneInlineChoice(interactionEntry.getInteraction(), choice))
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
					String text = StringHelper.escapeJson(GapAssessmentItemBuilder.getText(correctChoice));
					out.append("{ id:\"").append(responseIdentifier.toString()).append("\", value:\"").append(text).append("\"}");
				}
			}
			out.append("],");
		}
	}
	
	private class SolutionExtractorHandler extends DefaultHandler {
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			if("textentryinteraction".equals(localName)) {
				localName = qName = "textEntryInteraction";
				
				String solution = null;
				String solutionEmpty = null;
				String responseIdentifier = null;
				for(int i=0; i<attributes.getLength(); i++) {
					String name = attributes.getLocalName(i);
					if("data-qti-solution".equals(name)) {
						solution = attributes.getValue(i);
						if(solution != null) {
							solution = itemBuilder.unescapeDataQtiSolution(solution);
							solution = StringUtilities.trim(solution);
						}
					} else if("data-qti-solution-empty".equals(name)) {
						solutionEmpty = attributes.getValue(i);
					} else if("responseIdentifier".equalsIgnoreCase(name)) {
						responseIdentifier = attributes.getValue(i);
					}
				}
				
				if(StringHelper.containsNonWhitespace(responseIdentifier)
						&& (StringHelper.containsNonWhitespace(solution) || StringHelper.containsNonWhitespace(solutionEmpty))) {
					AbstractEntry entry = itemBuilder.getTextEntry(responseIdentifier);
					updateSolution(entry, solution, solutionEmpty);
				}
			}
		}
	}
}
