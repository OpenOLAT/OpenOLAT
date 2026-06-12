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

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
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
import org.olat.course.assessment.AssessmentHelper;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.ScoreBuilder;
import org.olat.ims.qti21.model.xml.interactions.GapAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.GapAssessmentItemBuilder.AbstractEntry;
import org.olat.ims.qti21.model.xml.interactions.GapAssessmentItemBuilder.InlineChoiceInteractionEntry;
import org.olat.ims.qti21.model.xml.interactions.GapAssessmentItemBuilder.NumericalEntry;
import org.olat.ims.qti21.model.xml.interactions.GapAssessmentItemBuilder.TextEntry;
import org.olat.ims.qti21.model.xml.interactions.GapAssessmentItemBuilder.TextEntryAlternative;
import org.olat.ims.qti21.model.xml.interactions.SimpleChoiceAssessmentItemBuilder.ScoreEvaluation;
import org.olat.ims.qti21.ui.editor.AssessmentTestEditorController;
import org.olat.ims.qti21.ui.editor.SyncAssessmentItem;
import org.olat.ims.qti21.ui.editor.events.AssessmentItemEvent;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.ToleranceMode;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.InlineChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.TextEntryInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.InlineChoice;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 10 juin 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class GapScoreController extends AssessmentItemRefEditorController implements SyncAssessmentItem {

	private static final String[] yesnoKeys = new String[]{ "y", "n"};
	
	private static final String PER_ANSWER_AND_ALTERNAITVES = "perAnswerAndAlternatives";
	
	private TextElement minScoreEl;
	private TextElement maxScoreEl;
	private FormLayoutContainer scoreCont;
	private SingleSelection assessmentModeEl;
	private SingleSelection duplicateAllowedEl;

	private int counter = 0;
	private final QTI21QuestionType type;
	
	private GapAssessmentItemBuilder itemBuilder;
	private final List<InteractionBundle> wrappers = new ArrayList<>();
	
	public GapScoreController(UserRequest ureq, WindowControl wControl,
			GapAssessmentItemBuilder itemBuilder, AssessmentItemRef itemRef, boolean restrictedEdit, boolean readOnly) {
		super(ureq, wControl, itemRef, restrictedEdit, readOnly);
		setTranslator(Util.createPackageTranslator(AssessmentTestEditorController.class, getLocale()));
		this.itemBuilder = itemBuilder;
		
		type = itemBuilder.getQuestionType();
		
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
		String maxValue = maxScore == null ? "" : (maxScore.getScore() == null ? "" : maxScore.getScore().toString());
		maxScoreEl = uifactory.addTextElement("max.score", "max.score", 8, maxValue, formLayout);
		maxScoreEl.setElementCssClass("o_sel_assessment_item_max_score");
		maxScoreEl.setEnabled(!restrictedEdit && !readOnly);
		
		String[] yesnoValues = new String[]{ translate("yes"), translate("no") };
		duplicateAllowedEl = uifactory.addRadiosHorizontal("duplicate", "form.imd.duplicate.answers", formLayout, yesnoKeys, yesnoValues);
		duplicateAllowedEl.setElementCssClass("o_sel_assessment_item_gap_duplicate");
		duplicateAllowedEl.setEnabled(!restrictedEdit && !readOnly);
		duplicateAllowedEl.setVisible(hasSeveralTextEntryWithSharedAlternatives());
		duplicateAllowedEl.setHelpTextKey("form.imd.duplicate.answers.hint", null);
		if(itemBuilder.isAllowDuplicatedAnswers()) {
			duplicateAllowedEl.select(yesnoKeys[0], true);
		} else {
			duplicateAllowedEl.select(yesnoKeys[1], true);
		}
	
		SelectionValues modeValues = new SelectionValues();
		modeValues.add(SelectionValues.entry(ScoreEvaluation.allCorrectAnswers.name(), translate("form.score.assessment.all.correct")));
		modeValues.add(SelectionValues.entry(ScoreEvaluation.perAnswer.name(), translate("form.score.assessment.per.answer")));
		if(type != QTI21QuestionType.inlinechoice) {
			modeValues.add(SelectionValues.entry(PER_ANSWER_AND_ALTERNAITVES, translate("form.score.assessment.per.answer.and.alternatives")));
		}
		modeValues.add(SelectionValues.entry(ScoreEvaluation.negativePointSystem.name(), translate("form.score.assessment.nps")));
		assessmentModeEl = uifactory.addRadiosHorizontal("assessment.mode", "form.score.assessment.mode", formLayout,
				modeValues.keys(), modeValues.values());
		assessmentModeEl.addActionListener(FormEvent.ONCHANGE);
		assessmentModeEl.setEnabled(!restrictedEdit && !readOnly);
		if(itemBuilder.getScoreEvaluationMode() == ScoreEvaluation.perAnswer) {
			if(itemBuilder.alternativesWithSpecificScore()) {
				assessmentModeEl.select("perAnswerAndAlternatives", true);
			} else {
				assessmentModeEl.select(ScoreEvaluation.perAnswer.name(), true);
			}
		} else if(itemBuilder.getScoreEvaluationMode() != null
				&& modeValues.containsKey(itemBuilder.getScoreEvaluationMode().name())) {
			assessmentModeEl.select(itemBuilder.getScoreEvaluationMode().name(), true);
		} else {
			assessmentModeEl.select(ScoreEvaluation.allCorrectAnswers.name(), true);
		}
		
		String scorePage = velocity_root + "/gap_score.html";
		scoreCont = uifactory.addCustomFormLayout("scores", null, scorePage, formLayout);

		Map<Identifier,String> contextMap = extractContext(itemBuilder.getQuestion());
		for(AbstractEntry entry:itemBuilder.getOrderedTextEntries()) {
			wrappers.add(createTextEntryWrapper(entry, contextMap.get(entry.getResponseIdentifier())));
		}
		for(InlineChoiceInteractionEntry inlineChoiceBlock:itemBuilder.getInlineChoiceInteractions()) {
			wrappers.add(createChoiceWrapper(inlineChoiceBlock, contextMap.get(inlineChoiceBlock.getResponseIdentifier())));
		}
		scoreCont.contextPut("interactionWrappers", wrappers);
		scoreCont.setVisible(assessmentModeEl.isSelected(1) || assessmentModeEl.isSelected(2));
		scoreCont.contextPut("withAlternatives", Boolean.valueOf(assessmentModeEl.isSelected(2)));

		// Submit Button
		FormLayoutContainer buttonsContainer = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		buttonsContainer.setVisible(!readOnly);
		uifactory.addFormSubmitButton("submit", buttonsContainer);
		
		AssistanceAccordionController assistanceCtrl = new AssistanceAccordionController(ureq, getWindowControl(), getTranslator(), "help");
		assistanceCtrl.setCssClass("o_qti_assistance");
		listenTo(assistanceCtrl);
		ComponentWrapperElement wrapperEl = new ComponentWrapperElement(assistanceCtrl.getInitialComponent());
		wrapperEl.setFormLayout("minimal");
		formLayout.add("assistance", wrapperEl);
		assistanceCtrl.addQuestionAnswer("form.score.assessment.all.correct", "form.score.assessment.all.correct.gap.details", new Component[0]);
        assistanceCtrl.addQuestionAnswer("form.score.assessment.per.answer", "form.score.assessment.per.answer.details", new Component[0]);
        assistanceCtrl.addQuestionAnswer("form.score.assessment.nps", "form.score.assessment.nps.gap.details", new Component[0]);
	}
	
	private boolean hasSeveralTextEntryWithSharedAlternatives() {
		int count = 0;
		
		List<AbstractEntry> entries = itemBuilder.getOrderedTextEntries();
		for(AbstractEntry entry:entries) {
			if(entry instanceof TextEntry) {
				count++;
			}
		}

		return count > 1 && itemBuilder.entriesSharesAlternatives();
	}
	
	private Map<Identifier,String> extractContext(String html) {
		if(!StringHelper.containsNonWhitespace(html)) return Map.of();

		try {
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
	public void sync(UserRequest ureq, AssessmentItemBuilder assessmentItemBuilder) {
		if(itemBuilder == assessmentItemBuilder) {

			// create new entry
			List<Interaction> interactions = itemBuilder.getOrderedInteractions();
			Map<Identifier,String> contextMap = extractContext(itemBuilder.getQuestion());
			
			for(Interaction interaction:interactions) {
				Identifier responseIdentifier = interaction.getResponseIdentifier();
				InteractionBundle wrapper = getInteractionBundle(responseIdentifier);
				if(interaction instanceof TextEntryInteraction) {
					AbstractEntry entry = itemBuilder.getTextEntry(responseIdentifier.toString());
					if(wrapper == null) {
						wrappers.add(createTextEntryWrapper(entry, contextMap.get(responseIdentifier)));
					} else if(wrapper instanceof TextEntryInteractionBundle textEntryInteractionBundle) {
						textEntryInteractionBundle.setEntry(entry);
						if(entry instanceof TextEntry textEntry) {
							syncAlternatives(textEntryInteractionBundle, textEntry);
						} else if(entry instanceof NumericalEntry numericalEntry) {
							syncAlternatives(textEntryInteractionBundle, numericalEntry);
						}
					}
				} else if(interaction instanceof InlineChoiceInteraction) {
					InlineChoiceInteractionEntry entry = itemBuilder.getInlineChoiceInteractionEntry(responseIdentifier);
					if(wrapper == null) {
						wrappers.add(createChoiceWrapper(entry, contextMap.get(responseIdentifier)));
					} else if(wrapper instanceof InlineChoiceInteractionBundle inlineChoiceInteractionBundle) {
						syncChoices(inlineChoiceInteractionBundle, entry);
					}
				}
			}
			
			//remove removed entry
			for(Iterator<InteractionBundle> wrapperIt=wrappers.iterator(); wrapperIt.hasNext(); ) {
				Identifier responseIdentifier = wrapperIt.next().getResponseIdentifier();
				if(itemBuilder.getTextEntry(responseIdentifier.toString()) == null
						&& itemBuilder.getInlineChoiceInteractionEntry(responseIdentifier) == null) {
					wrapperIt.remove();
				}
			}
			
			//reorder the wrappers
			Map<Identifier,InteractionBundle> wrapperMap = new HashMap<>();
			for(InteractionBundle wrapper:wrappers) {
				wrapperMap.put(wrapper.getResponseIdentifier(), wrapper);
			}
			List<InteractionBundle> reorderedWrappers = new ArrayList<>();
			for(Interaction interaction:interactions) {
				Identifier responseIdentifier = interaction.getResponseIdentifier();
				if(responseIdentifier != null) {
					InteractionBundle wrapper = wrapperMap.get(responseIdentifier);
					if(wrapper != null) {
						reorderedWrappers.add(wrapper);
						wrapperMap.remove(responseIdentifier);
					}
				}
			}
			
			if(wrapperMap.size() > 0) {//paranoid security
				for(InteractionBundle wrapper:wrapperMap.values()) {
					if(!reorderedWrappers.contains(wrapper)) {
						reorderedWrappers.add(wrapper);
					}
				}
			}
			wrappers.clear();
			wrappers.addAll(reorderedWrappers);

			// duplicated only for text entry
			duplicateAllowedEl.setVisible(hasSeveralTextEntryWithSharedAlternatives());
		}
	}
	
	private void syncAlternatives(TextEntryInteractionBundle wrapper, NumericalEntry entry) {
		List<Alternative> variantsWrappers = wrapper.getAllVariants();
		for(Alternative alternativeWrapper:variantsWrappers) {
			if(alternativeWrapper instanceof TextEntryWrapper textEntryWrapper) {
				textEntryWrapper.setEntry(entry);
			}
		}
	}
	
	private void syncAlternatives(TextEntryInteractionBundle wrapper, TextEntry entry) {
		List<Alternative> variantsWrappers = wrapper.getAllVariants();
		List<TextEntryAlternative> alternatives = entry.getAlternatives();
		
		for(Iterator<Alternative> it=variantsWrappers.iterator(); it.hasNext(); ) {
			Alternative alternativeWrapper = it.next();
			if(alternativeWrapper instanceof TextEntryWrapper textEntryWrapper) {
				textEntryWrapper.setEntry(entry);
				continue;
			}
			
			boolean found = false;
			if(alternatives != null && !alternatives.isEmpty()) {
				for(TextEntryAlternative alternative:alternatives) {
					if(alternativeWrapper instanceof TextEntryAlternativeWrapper textEntryAlternativeWrapper
							&&  textEntryAlternativeWrapper.getAlternative() == alternative) {
						found = true;
					}
				}
			}
			
			if(!found) {
				it.remove();
			}
		}
		
		if(alternatives != null && !alternatives.isEmpty()) {
			for(TextEntryAlternative alternative:alternatives) {
				boolean found = false;
				for(Alternative alternativeWrapper:variantsWrappers) {
					if(alternativeWrapper instanceof TextEntryAlternativeWrapper textEntryAlternativeWrapper
							&& textEntryAlternativeWrapper.getAlternative() == alternative) {
						found = true;
					}
				}
				
				if(!found) {
					TextEntryAlternativeWrapper alternativeWrapper = createAlternativeWrapper(alternative);
					wrapper.addVariant(alternativeWrapper);
				}
			}
		}
	}
	
	private void syncChoices(InlineChoiceInteractionBundle wrapper, InlineChoiceInteractionEntry entry) {
		List<Alternative> variantsWrappers = wrapper.getAllVariants();
		List<InlineChoice> inlineChoices = entry.getInlineChoices();
		
		for(Iterator<Alternative> it=variantsWrappers.iterator(); it.hasNext(); ) {
			Alternative alternativeWrapper = it.next();

			boolean found = false;
			if(inlineChoices != null && !inlineChoices.isEmpty()) {
				for(InlineChoice inlineChoice:inlineChoices) {
					if(alternativeWrapper instanceof InlineChoiceWrapper inlineChoiceWrapper
							&&  inlineChoiceWrapper.getInlineChoice().getIdentifier().equals(inlineChoice.getIdentifier())) {
						inlineChoiceWrapper.setInlineChoice(inlineChoice);
						found = true;
					}
				}
			}
			
			if(!found) {
				it.remove();
			}
		}
		
		if(inlineChoices != null && !inlineChoices.isEmpty()) {
			for(InlineChoice inlineChoice:inlineChoices) {
				boolean found = false;
				for(Alternative alternativeWrapper:variantsWrappers) {
					if(alternativeWrapper instanceof InlineChoiceWrapper inlineChoiceWrapper
							&& inlineChoiceWrapper.getInlineChoice() == inlineChoice) {
						found = true;
					}
				}
				
				if(!found) {
					InlineChoiceWrapper inlineChoiceWrapper = createInlineChoiceWrapper(inlineChoice, entry);
					wrapper.addVariant(inlineChoiceWrapper);
				}
			}
		}
	}

	private InteractionBundle getInteractionBundle(Identifier responseIdentifier) {
		for(InteractionBundle bundle:wrappers) {
			if(responseIdentifier.equals(bundle.getResponseIdentifier())) {
				return bundle;
			}
		}
		return null;
	}
	
	private TextEntryInteractionBundle createTextEntryWrapper(AbstractEntry entry, String context) {
		String points = "";
		Double score = entry.getScore();
		if(score != null) {
			points = score.toString();
		}
		String pointElId = "points_" + counter++;
		TextElement pointEl = uifactory.addTextElement(pointElId, null, 5, points, scoreCont);
		pointEl.setDomReplacementWrapperRequired(false);
		pointEl.setDisplaySize(5);
		pointEl.setEnabled(!restrictedEdit && !readOnly);
		scoreCont.add(pointElId, pointEl);
		
		List<Alternative> alternativeWrappers = new ArrayList<>();
		TextEntryWrapper mainWrapper = new TextEntryWrapper(entry, pointEl);
		alternativeWrappers.add(mainWrapper);
		
		if(entry instanceof TextEntry textEntry) {
			if(textEntry.getAlternatives() != null) {
				for(TextEntryAlternative alternative:textEntry.getAlternatives()) {
					TextEntryAlternativeWrapper alternativeWrapper = createAlternativeWrapper(alternative);
					alternativeWrappers.add(alternativeWrapper);
				}
			}
		}

		return new TextEntryInteractionBundle(context, entry, alternativeWrappers);
	}
	
	private TextEntryAlternativeWrapper createAlternativeWrapper(TextEntryAlternative alternative) {
		String altPointElId = "points_" + counter++;
		String altScoreStr = Double.toString(alternative.getScore());
		TextElement altPointEl = uifactory.addTextElement(altPointElId, null, 5, altScoreStr, scoreCont);
		altPointEl.setDisplaySize(5);
		altPointEl.setEnabled(!restrictedEdit && !readOnly);
		scoreCont.add(altPointElId, altPointEl);
		return new TextEntryAlternativeWrapper(alternative, altPointEl);
	}
	
	private InlineChoiceInteractionBundle createChoiceWrapper(InlineChoiceInteractionEntry inlineChoiceInteractionEntry, String context) {
		List<InlineChoiceWrapper> choiceWrappers = new ArrayList<>();
		for(InlineChoice inlineChoice:inlineChoiceInteractionEntry.getInlineChoices()) {
			InlineChoiceWrapper choiceWrapper = createInlineChoiceWrapper(inlineChoice, inlineChoiceInteractionEntry);
			choiceWrappers.add(choiceWrapper);
		}
		return new InlineChoiceInteractionBundle(context, inlineChoiceInteractionEntry, choiceWrappers);
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
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= validateDouble(maxScoreEl);

		if(assessmentModeEl.isOneSelected() && (assessmentModeEl.isSelected(1) || assessmentModeEl.isSelected(2))) {
			boolean alternativeSpecificScore = assessmentModeEl.isSelected(2);
			for(InteractionBundle wrapper:wrappers) {
				List<Alternative> variants = wrapper.getVariants();
				int numOfVariants = variants.size();
				
				Double pointsForCorrect = null;
				for(int i=0; i<numOfVariants; i++) {
					Alternative variant = variants.get(i);
					if(i == 0 || alternativeSpecificScore || variant instanceof InlineChoiceWrapper) {
						allOk &= validateDouble(variant.getPointsEl());
					}
					
					if(variant.isCorrect()) {
						pointsForCorrect = variant.getPoints(); 
					}
				}
				
				if(pointsForCorrect != null) {
					for(int i=0; i<numOfVariants; i++) {
						Alternative variant = variants.get(i);
						if(!variant.isCorrect()) {
							validateAgainstCorrectVariant(variant, pointsForCorrect.doubleValue());
						}
					}
				}
			}
		}
		
		return allOk;
	}
	
	protected boolean validateAgainstCorrectVariant(Alternative variant, double pointsForCorrect) {
		boolean allOk = true;
		
		Double points = variant.getPoints();
		if(points != null && points.doubleValue() > pointsForCorrect) {
			variant.getPointsEl().setErrorKey("error.bigger.than.correct");
			allOk &= false;
		}
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(assessmentModeEl.isOneSelected()) {
			updateScoresUI();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void updateScoresUI() {
		boolean perAnswer = assessmentModeEl.isSelected(1);
		boolean perAnswerAndVariants = assessmentModeEl.isSelected(2);
		scoreCont.setVisible(perAnswer || perAnswerAndVariants);
		scoreCont.contextPut("withAlternatives", Boolean.valueOf(perAnswerAndVariants));
		if(perAnswer || perAnswerAndVariants) {
			for(InteractionBundle wrapper:wrappers) {
				if(wrapper instanceof TextEntryInteractionBundle textEntryInteractionBundle) {
					List<Alternative> variants = textEntryInteractionBundle.getAllVariants();
				
					Double points = null;
					if(variants.get(0) instanceof TextEntryWrapper entryWrapper
							&& entryWrapper.getEntry().getScore() != null
							&& entryWrapper.getEntry().getScore().doubleValue() == -1.0d) {
						entryWrapper.getEntry().setScore(1.0d);
						entryWrapper.getPointsEl().setValue("1.0");
						points = Double.valueOf(1.0d);
					}
					
					for(Alternative variant:variants) {
						if(variant instanceof TextEntryAlternativeWrapper alternativeWrapper) {
							TextEntryAlternative alternative = alternativeWrapper.getAlternative();
							if(StringHelper.containsNonWhitespace(alternativeWrapper.getPointsEl().getValue())) {
								if(points != null && (alternative.getScore() == 1.0d || alternative.getScore() == -1.0d)) {
									alternative.setScore(points.doubleValue());
									alternativeWrapper.getPointsEl().setValue(points.toString());
								}
							} else {
								if(alternative.getScore() >= 0.0) {
									alternativeWrapper.getPointsEl().setValue(Double.toString(alternative.getScore()));
								} else if(points != null && points.doubleValue() == -1.0d) {
									alternative.setScore(points.doubleValue());
									alternativeWrapper.getPointsEl().setValue(points.toString());
								}
							}
						}
					}
				} else if(wrapper instanceof InlineChoiceInteractionBundle inlineChoiceInteractionBundle) {
					InlineChoiceInteractionEntry interactionEntry = inlineChoiceInteractionBundle.getInlineChoiceInteractionEntry();
					Identifier correctResponseId = interactionEntry.getCorrectResponseId();
					
					List<InlineChoiceWrapper> choiceWrappers = inlineChoiceInteractionBundle.getInlineChoices();
					if(choiceWrappers != null) {
						for(InlineChoiceWrapper choiceWrapper:choiceWrappers) {
							if(!StringHelper.containsNonWhitespace(choiceWrapper.getPointsEl().getValue())) {
								if(correctResponseId != null && correctResponseId.equals(choiceWrapper.getIdentifier())) {
									choiceWrapper.getPointsEl().setValue("1.0");
								} else {
									choiceWrapper.getPointsEl().setValue("0.0");
								}
							}
						}
					}	
				}
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(restrictedEdit || readOnly) return;
		
		super.formOK(ureq);
		String maxScoreValue = maxScoreEl.getValue();
		Double maxScore = Double.parseDouble(maxScoreValue);
		itemBuilder.setMaxScore(maxScore);
		itemBuilder.setMinScore(Double.valueOf(0.0d));
		
		if(duplicateAllowedEl.isVisible()) {
			boolean allowDuplicates = duplicateAllowedEl.isOneSelected() && duplicateAllowedEl.isSelected(0);
			itemBuilder.setAllowDuplicatedAnswers(allowDuplicates);
		} else {
			itemBuilder.setAllowDuplicatedAnswers(true);
		}
		
		String selectedMode = assessmentModeEl.getSelectedKey();
		if(assessmentModeEl.isOneSelected() && (ScoreEvaluation.perAnswer.name().equals(selectedMode) || PER_ANSWER_AND_ALTERNAITVES.equals(selectedMode))) {
			itemBuilder.setScoreEvaluationMode(ScoreEvaluation.perAnswer);
			boolean alternativeSpecificScore = PER_ANSWER_AND_ALTERNAITVES.equals(selectedMode);
			
			for(InteractionBundle bundle:wrappers) {
				List<Alternative> variants = bundle.getAllVariants();
				if(bundle instanceof TextEntryInteractionBundle) {
					for(int i=0; i<variants.size(); i++) {
						Alternative variant = variants.get(i);
						
						String scoreStr = alternativeSpecificScore
								? variant.getPointsEl().getValue()
								: variants.get(0).getPointsEl().getValue();
						double score = Double.parseDouble(scoreStr);
						if(variant instanceof TextEntryWrapper entryWrapper) {
							entryWrapper.getEntry().setScore(score);
						} else if(variant instanceof TextEntryAlternativeWrapper alternativeWrapper) {
							alternativeWrapper.getAlternative().setScore(score);
						}
						
					}
				} else if(bundle instanceof InlineChoiceInteractionBundle interactionBundle) {
					InlineChoiceInteractionEntry interactionEntry = interactionBundle.getInlineChoiceInteractionEntry();
					for(InlineChoiceWrapper choiceWrapper:interactionBundle.getInlineChoices()) {
						String pointsStr = choiceWrapper.getPointsEl().getValue();
						Double points = Double.valueOf(pointsStr);
						interactionEntry.putScore(choiceWrapper.getInlineChoice().getIdentifier(), points);
					}
				}
			}
		} else {
			itemBuilder.setScoreEvaluationMode(ScoreEvaluation.valueOf(selectedMode));
		}

		fireEvent(ureq, new AssessmentItemEvent(AssessmentItemEvent.ASSESSMENT_ITEM_CHANGED, itemBuilder.getAssessmentItem(), null));
	}
	
	public interface InteractionBundle {
		
		String getContext();
		
		Identifier getResponseIdentifier();
		
		List<Alternative> getVariants();
		
		List<Alternative> getAllVariants();
		
	}
	
	public static abstract class Alternative {

		private boolean correct;
		private final TextElement pointsEl;
		
		public Alternative(TextElement pointsEl, boolean correct) {
			this.pointsEl = pointsEl;
			this.correct = correct;
		}
		
		public boolean isCorrect() {
			return correct;
		}
		
		public boolean isPositive() {
			if(isCorrect()) {
				return true;
			}
			
			String score = pointsEl.getValue();
			if(StringHelper.containsNonWhitespace(score)) {
				double val = Double.parseDouble(pointsEl.getValue());
				return val > 0.0d;
			}
			return false;
		}
		
		public void setCorrect(boolean correct) {
			this.correct = correct;
		}
		
		public abstract String getText();
		
		public TextElement getPointsEl() {
			return pointsEl;
		}
		
		public Double getPoints() {
			Double val = null;
			if(pointsEl != null && StringHelper.containsNonWhitespace(pointsEl.getValue())) {
				try {
					val = Double.valueOf(pointsEl.getValue());
				} catch (NumberFormatException e) {
					//
				}
			}
			return val;
		}
	}
	
	public class TextEntryInteractionBundle implements InteractionBundle {
		
		private String context;
		private AbstractEntry entry;
		private final List<Alternative> alternatives;
		
		public TextEntryInteractionBundle(String context, AbstractEntry entry, List<Alternative> alternatives) {
			this.entry = entry;
			this.context = context;
			this.alternatives = alternatives;
		}
		
		public AbstractEntry getEntry() {
			return entry;
		}
		
		public void setEntry(AbstractEntry entry) {
			this.entry = entry;
		}

		@Override
		public String getContext() {
			return (context == null ? "" : context)
					+ " <strong>&lt;"
					+ (entry instanceof NumericalEntry ? translate("form.numerical") : translate("form.fib"))
					+ "&gt;</strong>"; 
		}
		
		@Override
		public Identifier getResponseIdentifier() {
			return entry.getResponseIdentifier();
		}

		public List<Alternative> getAllVariants() {
			return alternatives;
		}

		@Override
		public List<Alternative> getVariants() {
			boolean perAnswerAndVariants = assessmentModeEl.isSelected(2);
			List<Alternative> list = perAnswerAndVariants
					? getAllVariants()
					: alternatives.stream()
						.filter(alt -> alt instanceof TextEntryWrapper)
						.toList();
			if(list.size() > 1) {
				list = new ArrayList<>(list);
				Collections.sort(list, new VariantComparator());
			}
			return list;
		}
		
		public void addVariant(Alternative variant) {
			alternatives.add(variant);
		}
		
		@Override
		public int hashCode() {
			return entry.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj instanceof TextEntryWrapper w) {
				return entry.getResponseIdentifier() != null
						&& entry.getResponseIdentifier().equals(w.entry.getResponseIdentifier());
			}
			return false;
		}
	}
	
	public class TextEntryWrapper extends Alternative {
		
		private AbstractEntry entry;
		
		public TextEntryWrapper(AbstractEntry entry, TextElement pointsEl) {
			super(pointsEl, true);
			this.entry = entry;
		}

		@Override
		public String getText() {
			String summary;
			if(entry instanceof TextEntry textEntry) {
				summary = textEntry.getSolution();
			} else if(entry instanceof NumericalEntry numericalEntry) {
				Double solution = numericalEntry.getSolution();
				summary = solution == null ? "???" : solution.toString();
				
				ToleranceMode mode =  numericalEntry.getToleranceMode();
				if(solution != null && numericalEntry.getLowerTolerance() != null && numericalEntry.getUpperTolerance() != null) {
					if(mode == ToleranceMode.ABSOLUTE) {
						BigDecimal solutionBig = BigDecimal.valueOf(solution);
			            BigDecimal lowerTolerance = BigDecimal.valueOf(numericalEntry.getLowerTolerance());
			            BigDecimal lower = solutionBig.subtract(lowerTolerance);
			            BigDecimal upperTolerance = BigDecimal.valueOf(numericalEntry.getUpperTolerance());
			            BigDecimal upper = solutionBig.add(upperTolerance);
						summary = translate("fib.tolerance.example", solution.toString(),
								AssessmentHelper.getRoundedScore(lower), AssessmentHelper.getRoundedScore(upper));
					} else if(mode == ToleranceMode.RELATIVE) {
						double tolerance1 = Math.abs(numericalEntry.getLowerTolerance());
			        	double tolerance2 = Math.abs(numericalEntry.getUpperTolerance());
			            double lower = solution.doubleValue() * (1 - tolerance1 / 100);
			            double upper = solution.doubleValue() * (1 + tolerance2 / 100);
			            
			            if(lower > upper) {
			            	double switchBounds = lower;
			            	lower = upper;
			            	upper = switchBounds;
			            }
			            
						summary = translate("fib.tolerance.example", solution.toString(),
								AssessmentHelper.getRoundedScore(Double.valueOf(lower)),
								AssessmentHelper.getRoundedScore(Double.valueOf(upper)));
					}
				}
			} else {
				summary = "???";
			}
			return summary;
		}
		
		public AbstractEntry getEntry() {
			return entry;
		}
		
		public void setEntry(AbstractEntry entry) {
			this.entry = entry;
		}
	}
	
	public class TextEntryAlternativeWrapper extends Alternative {
		
		private final TextEntryAlternative alternative;
		
		public TextEntryAlternativeWrapper(TextEntryAlternative alternative, TextElement pointsEl) {
			super(pointsEl, false);
			this.alternative = alternative;
		}

		@Override
		public String getText() {
			return alternative.getAlternative();
		}
		
		public TextEntryAlternative getAlternative() {
			return alternative;
		}
	}
	
	public class InlineChoiceWrapper extends Alternative {
	
		private InlineChoice inlineChoice;
		
		public InlineChoiceWrapper(InlineChoice inlineChoice, TextElement pointsEl, boolean correct) {
			super(pointsEl, correct);
			this.inlineChoice = inlineChoice;
		}
		
		public String getId() {
			return inlineChoice.getIdentifier().toString();
		}
		
		public Identifier getIdentifier() {
			return inlineChoice.getIdentifier();
		}

		@Override
		public String getText() {
			return GapAssessmentItemBuilder.getText(inlineChoice);
		}

		public InlineChoice getInlineChoice() {
			return inlineChoice;
		}
		
		public void setInlineChoice(InlineChoice inlineChoice) {
			this.inlineChoice = inlineChoice;
		}
	}
	
	public static final class VariantComparator implements Comparator<Alternative> {

		@Override
		public int compare(Alternative o1, Alternative o2) {
			boolean c1 = o1.isCorrect();
			boolean c2 = o2.isCorrect();
			
			int c = - Boolean.compare(c1, c2);
			if(c == 0) {
				double p1 = getScore(o1);
				double p2 = getScore(o2);
				c = - Double.compare(p1, p2);
			}
			
			if(c == 0) {
				String t1 = getText(o1);
				String t2 = getText(o2);
				c = - t1.compareTo(t2);
			}
			return c;
		}
		
		private String getText(Alternative o) {
			String text = o.getText();
			return text == null ? "" : text;
		}
		
		private double getScore(Alternative o) {
			Double val = o.getPoints();
			return val == null ? -1.0 : val.doubleValue();
		}
	}
	
	public class InlineChoiceInteractionBundle implements InteractionBundle {
		
		private String context;
		private List<InlineChoiceWrapper> inlineChoices;
		private InlineChoiceInteractionEntry inlineChoiceInteractionEntry;
		
		public InlineChoiceInteractionBundle(String context, InlineChoiceInteractionEntry inlineChoiceInteractionEntry,
				List<InlineChoiceWrapper> inlineChoices) {
			this.context = context;
			this.inlineChoices = inlineChoices;
			this.inlineChoiceInteractionEntry = inlineChoiceInteractionEntry;
		}
		
		@Override
		public String getContext() {
			return (context == null ? "" : context)
					+ " <strong>&lt;" + translate("form.inlinechoice") + "&gt;</strong>"; 
		}
		
		@Override
		public Identifier getResponseIdentifier() {
			return inlineChoiceInteractionEntry.getResponseIdentifier();
		}

		@Override
		public List<Alternative> getVariants() {
			List<Alternative> variants = getAllVariants();
			if(variants.size() > 1) {
				Collections.sort(variants, new VariantComparator());
			}
			return variants;
		}

		@Override
		public List<Alternative> getAllVariants() {
			return new ArrayList<>(inlineChoices);
		}
		
		public void addVariant(InlineChoiceWrapper variant) {
			inlineChoices.add(variant);
		}

		public void setContext(String context) {
			this.context = context;
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
		
		public void setInlineChoiceInteractionEntry(InlineChoiceInteractionEntry entry) {
			this.inlineChoiceInteractionEntry = entry;
		}
	}
	
	private static final class ContentDetectionHandler extends DefaultHandler {
		
		private boolean collect = true;
		private StringBuilder context = new StringBuilder();
		private final Map<Identifier,String> contextMap = new HashMap<>();
		
		public Map<Identifier,String> getContextMap() {
			return contextMap;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			String elem = localName.toLowerCase();
			if("inlinechoiceinteraction".equals(elem) || "textentryinteraction".equals(elem)) {
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
			if("inlinechoiceinteraction".equals(elem)
					|| "textentryinteraction".equals(elem)
					|| "script".equals(elem)) {
				collect = true;
			}
		}
	}
}
