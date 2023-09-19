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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.commons.controllers.accordion.AssistanceAccordionController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ComponentWrapperElement;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.ims.qti21.model.xml.ScoreBuilder;
import org.olat.ims.qti21.model.xml.interactions.InlineChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.InlineChoiceAssessmentItemBuilder.InlineChoiceInteractionEntry;
import org.olat.ims.qti21.model.xml.interactions.SimpleChoiceAssessmentItemBuilder.ScoreEvaluation;
import org.olat.ims.qti21.ui.editor.AssessmentTestEditorController;
import org.olat.ims.qti21.ui.editor.events.AssessmentItemEvent;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.InlineChoice;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 22 juin 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InlineChoiceScoreController extends AssessmentItemRefEditorController {
	
	private TextElement minScoreEl;
	private TextElement maxScoreEl;
	private FormLayoutContainer scoreCont;
	private SingleSelection assessmentModeEl;
	
	private int counter = 0;
	private final InlineChoiceAssessmentItemBuilder itemBuilder;
	private List<InlineChoiceInteractionWrapper> wrappers = new ArrayList<>();
	
	public InlineChoiceScoreController(UserRequest ureq, WindowControl wControl,
			InlineChoiceAssessmentItemBuilder itemBuilder, AssessmentItemRef itemRef, boolean restrictedEdit, boolean readOnly) {
		super(ureq, wControl, itemRef, restrictedEdit, readOnly);
		setTranslator(Util.createPackageTranslator(AssessmentTestEditorController.class, getLocale()));
		this.itemBuilder = itemBuilder;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormContextHelp("manual_user/learningresources/Configure_test_questions/#score");
		super.initForm(formLayout, listener, ureq);
		minScoreEl = uifactory.addTextElement("min.score", "min.score", 8, "0.0", formLayout);
		minScoreEl.setElementCssClass("o_sel_assessment_item_min_score");
		minScoreEl.setEnabled(false);
		
		ScoreBuilder maxScore = itemBuilder.getMaxScoreBuilder();
		String maxValue = maxScore == null || maxScore.getScore() == null ? "" : maxScore.getScore().toString();
		maxScoreEl = uifactory.addTextElement("max.score", "max.score", 8, maxValue, formLayout);
		maxScoreEl.setElementCssClass("o_sel_assessment_item_max_score");
		maxScoreEl.setEnabled(!restrictedEdit && !readOnly);
		
		SelectionValues modeValues = new SelectionValues();
		modeValues.add(SelectionValues.entry(ScoreEvaluation.allCorrectAnswers.name(), translate("form.score.assessment.all.correct")));
		modeValues.add(SelectionValues.entry(ScoreEvaluation.perAnswer.name(), translate("form.score.assessment.per.answer")));
		modeValues.add(SelectionValues.entry(ScoreEvaluation.negativePointSystem.name(), translate("form.score.assessment.nps")));
		assessmentModeEl = uifactory.addRadiosHorizontal("assessment.mode", "form.score.assessment.mode", formLayout,
				modeValues.keys(), modeValues.values());
		assessmentModeEl.addActionListener(FormEvent.ONCHANGE);
		assessmentModeEl.setEnabled(!restrictedEdit && !readOnly);
		if(itemBuilder.getScoreEvaluationMode() != null && modeValues.containsKey(itemBuilder.getScoreEvaluationMode().name())) {
			assessmentModeEl.select(itemBuilder.getScoreEvaluationMode().name(), true);
		} else {
			assessmentModeEl.select(ScoreEvaluation.allCorrectAnswers.name(), true);
		}
		
		AssistanceAccordionController assistanceCtrl = new AssistanceAccordionController(ureq, getWindowControl(), getTranslator(), "help");
		listenTo(assistanceCtrl);
		ComponentWrapperElement wrapperEl = new ComponentWrapperElement(assistanceCtrl.getInitialComponent());
		wrapperEl.setFormLayout("minimal");
		formLayout.add("assistance", wrapperEl);
		assistanceCtrl.addQuestionAnswer("form.score.assessment.all.correct", "form.score.assessment.all.correct.gap.details", new Component[0]);
        assistanceCtrl.addQuestionAnswer("form.score.assessment.per.answer", "form.score.assessment.per.answer.details", new Component[0]);
        assistanceCtrl.addQuestionAnswer("form.score.assessment.nps", "form.score.assessment.nps.gap.details", new Component[0]);
		
		String scorePage = velocity_root + "/inline_choices_score.html";
		scoreCont = FormLayoutContainer.createCustomFormLayout("scores", getTranslator(), scorePage);
		formLayout.add(scoreCont);
		scoreCont.setLabel(null, null);
		scoreCont.setVisible(ScoreEvaluation.perAnswer.name().equals(assessmentModeEl.getSelectedKey()));
		
		Map<Identifier,String> contextMap = extractContext(itemBuilder.getQuestion());
		for(InlineChoiceInteractionEntry inlineChoiceBlock:itemBuilder.getInteractions()) {
			wrappers.add(createChoiceWrapper(inlineChoiceBlock, contextMap.get(inlineChoiceBlock.getResponseIdentifier())));
		}
		scoreCont.contextPut("interactionWrappers", wrappers);
		scoreCont.setVisible(ScoreEvaluation.perAnswer.name().equals(assessmentModeEl.getSelectedKey()));

		// Submit Button
		FormLayoutContainer buttonsContainer = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsContainer.setRootForm(mainForm);
		buttonsContainer.setVisible(!readOnly);
		formLayout.add(buttonsContainer);
		uifactory.addFormSubmitButton("submit", buttonsContainer);
	}
	
	private InlineChoiceInteractionWrapper createChoiceWrapper(InlineChoiceInteractionEntry inlineChoiceInteractionEntry, String context) {
		List<InlineChoiceWrapper> choiceWrappers = new ArrayList<>();
		for(InlineChoice inlineChoice:inlineChoiceInteractionEntry.getInlineChoices()) {
			InlineChoiceWrapper choiceWrapper = createInlineChoiceWrapper(inlineChoice, inlineChoiceInteractionEntry);
			choiceWrappers.add(choiceWrapper);
		}
		return new InlineChoiceInteractionWrapper(context, inlineChoiceInteractionEntry, choiceWrappers);
	}
	
	private InlineChoiceWrapper createInlineChoiceWrapper(InlineChoice inlineChoice, 
			InlineChoiceInteractionEntry inlineChoiceInteractionEntry) {

		String points = "";
		Double score = inlineChoiceInteractionEntry.getScore(inlineChoice.getIdentifier());
		if(score != null) {
			points = score.toString();
		}
		
		String pointElId = "points_" + counter++;
		TextElement pointEl = uifactory.addTextElement(pointElId, null, 5, points, scoreCont);
		pointEl.setDomReplacementWrapperRequired(false);
		pointEl.setDisplaySize(5);
		pointEl.setEnabled(!restrictedEdit && !readOnly);
		scoreCont.add(pointElId, pointEl);
		
		Identifier correctResponseId = inlineChoiceInteractionEntry.getCorrectResponseId();
		boolean correct = correctResponseId != null && correctResponseId.equals(inlineChoice.getIdentifier());

		return new InlineChoiceWrapper(inlineChoice, pointEl, correct);
	}
	
	private Map<Identifier,String> extractContext(String html) {
		if(!StringHelper.containsNonWhitespace(html)) return Map.of();

		try {
			// return always true? Neko send always an html tag -> content true
			HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALTER_INFOSET);
			ContentDetectionHandler contentHandler = new ContentDetectionHandler();
			parser.setContentHandler(contentHandler);
			parser.parse(new InputSource(new StringReader(html)));
			return contentHandler.getContextMap();
		} catch (Exception e) {
			getLogger().error("", e);
			return Map.of();
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(assessmentModeEl.isOneSelected()) {
			updateScoresUI();
			onPerAnswerSelectionDefaultsScore();
			markDirty();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void updateScoresUI() {
		boolean perAnswer = ScoreEvaluation.perAnswer.name().equals(assessmentModeEl.getSelectedKey());
		scoreCont.setVisible(perAnswer);
	}
	
	/**
	 * Fill the scores with default values if not already set.
	 */
	private void onPerAnswerSelectionDefaultsScore() {
		boolean perAnswer = ScoreEvaluation.perAnswer.name().equals(assessmentModeEl.getSelectedKey());
		if(perAnswer) {
			for(InlineChoiceInteractionWrapper wrapper:wrappers) {
				InlineChoiceInteractionEntry interactionEntry = wrapper.getInlineChoiceInteractionEntry();
				Identifier correctResponseId = interactionEntry.getCorrectResponseId();
				
				List<InlineChoiceWrapper> choiceWrappers = wrapper.getInlineChoices();
				if(choiceWrappers != null) {
					for(InlineChoiceWrapper choiceWrapper:choiceWrappers) {
						if(!StringHelper.containsNonWhitespace(choiceWrapper.getPointsEl().getValue())) {
							if(correctResponseId != null && correctResponseId.equals(choiceWrapper.getIdentifier())) {
								choiceWrapper.getPointsEl().setValue("1");
							} else {
								choiceWrapper.getPointsEl().setValue("0");
							}
						}
					}
				}	
			}	
		}
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= validateDouble(maxScoreEl);

		if(assessmentModeEl.isOneSelected() && ScoreEvaluation.perAnswer.name().equals(assessmentModeEl.getSelectedKey())) {
			for(InlineChoiceInteractionWrapper interactionWrapper:wrappers) {
				for(InlineChoiceWrapper choiceWrapper:interactionWrapper.getInlineChoices()) {
					allOk &= validateDouble(choiceWrapper.getPointsEl());
				}
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(restrictedEdit || readOnly) return;
		
		super.formOK(ureq);
		String maxScoreValue = maxScoreEl.getValue();
		Double maxScore = Double.parseDouble(maxScoreValue);
		itemBuilder.setMaxScore(maxScore);
		itemBuilder.setMinScore(Double.valueOf(0.0d));
		
		if(assessmentModeEl.isOneSelected()) {
			String selectedKey = assessmentModeEl.getSelectedKey();
			if(ScoreEvaluation.perAnswer.name().equals(selectedKey)) {
				itemBuilder.setScoreEvaluationMode(ScoreEvaluation.perAnswer);
				for(InlineChoiceInteractionWrapper interactionWrapper:wrappers) {
					InlineChoiceInteractionEntry interactionEntry = interactionWrapper.getInlineChoiceInteractionEntry();
					for(InlineChoiceWrapper choiceWrapper:interactionWrapper.getInlineChoices()) {
						String pointsStr = choiceWrapper.getPointsEl().getValue();
						Double points = Double.valueOf(pointsStr);
						interactionEntry.putScore(choiceWrapper.getInlineChoice().getIdentifier(), points);
					}
				}
			} else {
				itemBuilder.setScoreEvaluationMode(ScoreEvaluation.valueOf(selectedKey));
			}
		} else {
			itemBuilder.setScoreEvaluationMode(ScoreEvaluation.allCorrectAnswers);
		}

		fireEvent(ureq, new AssessmentItemEvent(AssessmentItemEvent.ASSESSMENT_ITEM_CHANGED, itemBuilder.getAssessmentItem(), null));
	}
	
	public static class InlineChoiceWrapper {
		
		private final TextElement pointsEl;

		private boolean correct;
		private InlineChoice inlineChoice;
		
		public InlineChoiceWrapper(InlineChoice inlineChoice, TextElement scoreEl, boolean correct) {
			this.inlineChoice = inlineChoice;
			this.correct = correct;
			this.pointsEl = scoreEl;
		}
		
		public String getId() {
			return inlineChoice.getIdentifier().toString();
		}
		
		public Identifier getIdentifier() {
			return inlineChoice.getIdentifier();
		}
		
		public InlineChoice getInlineChoice() {
			return inlineChoice;
		}
		
		public void setInlineChoice(InlineChoice inlineChoice) {
			this.inlineChoice = inlineChoice;
		}
		
		public boolean isCorrect() {
			return correct;
		}
		
		public void setCorrect(boolean correct) {
			this.correct = correct;
		}
		
		public String getText() {
			return InlineChoiceAssessmentItemBuilder.getText(inlineChoice);
		}
		
		public TextElement getPointsEl() {
			return pointsEl;
		}
	}
	
	public static class InlineChoiceInteractionWrapper {
		
		private String context;
		private List<InlineChoiceWrapper> inlineChoices;
		private InlineChoiceInteractionEntry inlineChoiceInteractionEntry;
		
		public InlineChoiceInteractionWrapper(String context, InlineChoiceInteractionEntry inlineChoiceInteractionEntry,
				List<InlineChoiceWrapper> inlineChoices) {
			this.context = context;
			this.inlineChoices = inlineChoices;
			this.inlineChoiceInteractionEntry = inlineChoiceInteractionEntry;
		}
		
		public String getContext() {
			return context;
		}
		
		public void setContext(String context) {
			this.context = context;
		}
		
		public Identifier getResponseIdentifier() {
			return inlineChoiceInteractionEntry.getResponseIdentifier();
		}
		
		public List<InlineChoiceWrapper> getInlineChoices() {
			return inlineChoices;
		}
		
		public void addInlineChoiceWrapper(InlineChoiceWrapper wrapper) {
			inlineChoices.add(wrapper);
		}
		
		public InlineChoiceInteractionEntry getInlineChoiceInteractionEntry() {
			return inlineChoiceInteractionEntry;
		}
		
	}
	
	private static class ContentDetectionHandler extends DefaultHandler {
		
		private boolean collect = true;
		private StringBuilder context = new StringBuilder();
		private Map<Identifier,String> contextMap = new HashMap<>();
		
		public Map<Identifier,String> getContextMap() {
			return contextMap;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			String elem = localName.toLowerCase();
			if("inlinechoiceinteraction".equals(elem)) {
				String val = context.toString();
				String responseIdentifier = attributes.getValue("responseidentifier");
				if(StringHelper.containsNonWhitespace(val) && StringHelper.containsNonWhitespace(responseIdentifier)) {
					Identifier identifier = Identifier.assumedLegal(responseIdentifier);
					contextMap.put(identifier, val);
				}
				context = new StringBuilder();
				collect = false;
			} else if("script".equals(elem)) {
				collect = false;
			}
		}
		
		@Override
		public void characters(char[] chars, int offset, int length) {
			if(collect && offset >= 0 && length > 0) {
				String text = new String(chars, offset, length);
				if(text.trim().length() > 0) {
					context.append(" ").append(text);
				}
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			String elem = localName.toLowerCase();
			if("inlinechoiceinteraction".equals(elem) || "script".equals(elem)) {
				collect = true;
				
			}
		}
		
		
	}
}
