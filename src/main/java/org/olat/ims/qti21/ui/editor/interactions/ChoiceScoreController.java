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
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.FilterFactory;
import org.olat.ims.qti21.model.xml.AssessmentHtmlBuilder;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.ScoreBuilder;
import org.olat.ims.qti21.model.xml.interactions.ChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.SimpleChoiceAssessmentItemBuilder.ScoreEvaluation;
import org.olat.ims.qti21.ui.ResourcesMapper;
import org.olat.ims.qti21.ui.components.FlowFormItem;
import org.olat.ims.qti21.ui.editor.AssessmentTestEditorController;
import org.olat.ims.qti21.ui.editor.SyncAssessmentItem;
import org.olat.ims.qti21.ui.editor.events.AssessmentItemEvent;

import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.Choice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleChoice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.content.Hottext;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 08.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ChoiceScoreController extends AssessmentItemRefEditorController implements SyncAssessmentItem {
	
	private static final String[] modeKeys = new String[]{
			ScoreEvaluation.allCorrectAnswers.name(), ScoreEvaluation.perAnswer.name()
		};
	
	private TextElement minScoreEl;
	private TextElement maxScoreEl;
	private SingleSelection assessmentModeEl;
	private SingleSelection minChoicesEl;
	private SingleSelection maxChoicesEl;
	private FormLayoutContainer scoreCont;
	private final List<ChoiceWrapper> wrappers = new ArrayList<>();
	
	private int count = 0;
	private final File itemFileRef;
	private final String mapperUri;
	private final ChoiceAssessmentItemBuilder itemBuilder;
	
	private int counter = 0;
	
	public ChoiceScoreController(UserRequest ureq, WindowControl wControl,
			ChoiceAssessmentItemBuilder itemBuilder, AssessmentItemRef itemRef, File itemFileRef, File rootDirectory,
			boolean restrictedEdit, boolean readOnly) {
		super(ureq, wControl, itemRef, restrictedEdit, readOnly);
		setTranslator(Util.createPackageTranslator(AssessmentTestEditorController.class, getLocale()));
		this.itemBuilder = itemBuilder;
		this.itemFileRef = itemFileRef;
		
		URI assessmentObjectUri = itemFileRef.toURI();
		mapperUri = registerCacheableMapper(null, "ChoiceScoreController::" + CodeHelper.getRAMUniqueID(),
				new ResourcesMapper(assessmentObjectUri, rootDirectory));
		
		initForm(ureq);
		validateScoreOfCorrectAnswer();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormContextHelp("manual_user/tests/Configure_test_questions/#score");
		super.initForm(formLayout, listener, ureq);
		
		ScoreBuilder minScore = itemBuilder.getMinScoreBuilder();
		String minValue = minScore == null ? "" : (minScore.getScore() == null ? "" : minScore.getScore().toString());
		minScoreEl = uifactory.addTextElement("min.score", "min.score", 8, minValue, formLayout);
		minScoreEl.setElementCssClass("o_sel_assessment_item_min_score");
		minScoreEl.setEnabled(!restrictedEdit && !readOnly);
		
		ScoreBuilder maxScore = itemBuilder.getMaxScoreBuilder();
		String maxValue = maxScore == null ? "" : (maxScore.getScore() == null ? "" : maxScore.getScore().toString());
		maxScoreEl = uifactory.addTextElement("max.score", "max.score", 8, maxValue, formLayout);
		maxScoreEl.setElementCssClass("o_sel_assessment_item_max_score");
		maxScoreEl.setEnabled(!restrictedEdit && !readOnly);
		
		String[] choiceKeys = new String[0];
		String[] choiceValues = new String[0];
		maxChoicesEl = uifactory.addDropdownSingleselect("max.choices", formLayout, choiceKeys, choiceValues, null);
		maxChoicesEl.setEnabled(!restrictedEdit && !readOnly);
		minChoicesEl = uifactory.addDropdownSingleselect("min.choices", formLayout, choiceKeys, choiceValues, null);
		minChoicesEl.setEnabled(!restrictedEdit && !readOnly);
		updateMinMaxChoices();
		
		String[] modeValues = new String[]{
				translate("form.score.assessment.all.correct"),
				translate("form.score.assessment.per.answer")
		};
		assessmentModeEl = uifactory.addRadiosHorizontal("assessment.mode", "form.score.assessment.mode", formLayout, modeKeys, modeValues);
		assessmentModeEl.setEnabled(!restrictedEdit && !readOnly);
		assessmentModeEl.addActionListener(FormEvent.ONCHANGE);
		if(itemBuilder.getScoreEvaluationMode() == ScoreEvaluation.perAnswer) {
			assessmentModeEl.select(ScoreEvaluation.perAnswer.name(), true);
		} else {
			assessmentModeEl.select(ScoreEvaluation.allCorrectAnswers.name(), true);
		}
		
		String scorePage = velocity_root + "/choices_score.html";
		scoreCont = FormLayoutContainer.createCustomFormLayout("scores", getTranslator(), scorePage);
		formLayout.add(scoreCont);
		scoreCont.setLabel(null, null);
		
		for(Choice choice:itemBuilder.getChoices()) {
			ChoiceWrapper wrapper = createChoiceWrapper(choice);
			wrappers.add(wrapper);
		}
		scoreCont.contextPut("choices", wrappers);
		scoreCont.contextPut("restrictedEdit", restrictedEdit || readOnly);
		scoreCont.setVisible(assessmentModeEl.isSelected(1));

		// Submit Button
		FormLayoutContainer buttonsContainer = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsContainer.setRootForm(mainForm);
		buttonsContainer.setVisible(!readOnly);
		formLayout.add(buttonsContainer);
		uifactory.addFormSubmitButton("submit", buttonsContainer);
	}
	
	private void updateMinMaxChoices() {
		int maxPossibleChoices = itemBuilder.getMaxPossibleCorrectAnswers();
		String[] maxChoiceKeys = new String[maxPossibleChoices];
		String[] maxChoicesValues = new String[maxPossibleChoices];
		for(int i=0; i<maxPossibleChoices; i++) {
			maxChoiceKeys[i] = maxChoicesValues[i] = Integer.toString(i);
		}
		if(maxChoicesValues.length > 0) {
			maxChoicesValues[0] = translate("max.choices.unlimited");
		}
		maxChoicesEl.setKeysAndValues(maxChoiceKeys, maxChoicesValues, null);
		maxChoicesEl.setVisible(itemBuilder.getMaxPossibleCorrectAnswers() > 1);
		
		boolean found = false;
		String maxChoices = Integer.toString(itemBuilder.getMaxChoices());
		for(String choiceKey:maxChoiceKeys) {
			if(choiceKey.equals(maxChoices)) {
				maxChoicesEl.select(choiceKey, true);
				found = true;
			}
		}
		
		if(!found && maxChoiceKeys.length > 0) {
			maxChoicesEl.select(maxChoiceKeys[0], true);
		}
		
		String[] minChoiceKeys = new String[maxPossibleChoices];
		String[] minChoicesValues = new String[maxPossibleChoices];
		for(int i=0; i<maxPossibleChoices; i++) {
			minChoiceKeys[i] = minChoicesValues[i] = Integer.toString(i);
		}
		if(minChoicesValues.length > 0) {
			minChoicesValues[0] = translate("min.choices.unlimited");
		}
		minChoicesEl.setKeysAndValues(minChoiceKeys, minChoicesValues, null);
		minChoicesEl.setVisible(itemBuilder.getMaxPossibleCorrectAnswers() > 1);
		boolean minFound = false;
		String minChoices = Integer.toString(itemBuilder.getMinChoices());
		for(String choiceKey:minChoiceKeys) {
			if(choiceKey.equals(minChoices)) {
				minChoicesEl.select(choiceKey, true);
				minFound = true;
			}
		}
		
		if(!minFound && minChoiceKeys.length > 0) {
			minChoicesEl.select(minChoiceKeys[0], true);
		}
	}
	
	@Override
	public void sync(UserRequest ureq, AssessmentItemBuilder assessmentItemBuilder) {
		if(itemBuilder == assessmentItemBuilder) {
			for(Choice choice:itemBuilder.getChoices()) {
				ChoiceWrapper wrapper = getChoiceWrapper(choice);
				if(wrapper == null) {
					wrappers.add(createChoiceWrapper(choice));
				} else {
					wrapper.setChoice(choice);
				}
			}
			
			for(Iterator<ChoiceWrapper> wrapperIt=wrappers.iterator(); wrapperIt.hasNext(); ) {
				Identifier choiceIdentifier = wrapperIt.next().getChoice().getIdentifier();
				if(itemBuilder.getChoice(choiceIdentifier) == null) {
					wrapperIt.remove();
				}
			}
			
			updateMinMaxChoices();
		}
		validateScoreOfCorrectAnswer();
	}
	
	private ChoiceWrapper createChoiceWrapper(Choice choice) {
		String points = "";
		Double score = itemBuilder.getMapping(choice.getIdentifier());
		if(score != null) {
			points = score.toString();
		}
		String pointElId = "points_" + counter++;
		TextElement pointEl = uifactory.addTextElement(pointElId, null, 5, points, scoreCont);
		pointEl.setDisplaySize(5);
		pointEl.setEnabled(!restrictedEdit && !readOnly);
		scoreCont.add(pointElId, pointEl);
		return new ChoiceWrapper(choice, pointEl);
	}
	
	private ChoiceWrapper getChoiceWrapper(Choice choice) {
		for(ChoiceWrapper wrapper:wrappers) {
			if(wrapper.getChoice().getIdentifier().equals(choice.getIdentifier())) {
				return wrapper;
			}
		}
		return null;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= validateMinMaxScores(minScoreEl, maxScoreEl);

		if(assessmentModeEl.isOneSelected() && assessmentModeEl.isSelected(1)) {
			for(ChoiceWrapper wrapper:wrappers) {
				allOk &= validateDouble(wrapper.getPointsEl());
			}
		}

		validateScoreOfCorrectAnswer();// only a warning
		return allOk;
	}
	
	private void validateScoreOfCorrectAnswer() {
		try {
			Boolean warning = (Boolean)scoreCont.contextGet("scoreWarning");
			Boolean newWarning = Boolean.valueOf(itemBuilder.scoreOfCorrectAnswerWarning());
			if(warning == null || !warning.equals(newWarning)) {
				scoreCont.contextPut("scoreWarning", newWarning);
			}
		} catch (Exception e) {
			// not a critical feature, don't produce red screen
			logError("", e);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(assessmentModeEl.isOneSelected()) {
			scoreCont.setVisible(assessmentModeEl.isSelected(1));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(restrictedEdit || readOnly) return;
		
		super.formOK(ureq);
		String maxScoreValue = maxScoreEl.getValue();
		Double maxScore = Double.parseDouble(maxScoreValue);
		itemBuilder.setMaxScore(maxScore);
		String minScoreValue = minScoreEl.getValue();
		Double minScore = Double.parseDouble(minScoreValue);
		itemBuilder.setMinScore(minScore);
		if(maxChoicesEl != null && maxChoicesEl.isOneSelected()) {
			int maxChoices = Integer.parseInt(maxChoicesEl.getSelectedKey());
			itemBuilder.setMaxChoices(maxChoices);
		}
		if(minChoicesEl != null && minChoicesEl.isOneSelected()) {
			int minChoices = Integer.parseInt(minChoicesEl.getSelectedKey());
			itemBuilder.setMinChoices(minChoices);
		}
		
		if(assessmentModeEl.isOneSelected() && assessmentModeEl.isSelected(1)) {
			itemBuilder.setScoreEvaluationMode(ScoreEvaluation.perAnswer);
			itemBuilder.clearMapping();
			for(ChoiceWrapper wrapper:wrappers) {
				String pointsStr = wrapper.getPointsEl().getValue();
				Double points = Double.valueOf(pointsStr);
				itemBuilder.setMapping(wrapper.getChoice().getIdentifier(), points);
			}
		} else {
			itemBuilder.setScoreEvaluationMode(ScoreEvaluation.allCorrectAnswers);
			itemBuilder.clearMapping();
		}

		fireEvent(ureq, new AssessmentItemEvent(AssessmentItemEvent.ASSESSMENT_ITEM_CHANGED, itemBuilder.getAssessmentItem(), null));
		validateScoreOfCorrectAnswer();
	}
	
	public final class ChoiceWrapper {
		
		private String summary;
		private Choice choice;
		private final TextElement pointsEl;
		private FlowFormItem summaryEl;
		
		public ChoiceWrapper(Choice choice, TextElement pointsEl) {
			setChoice(choice);
			this.pointsEl = pointsEl;
			pointsEl.setUserObject(this);
		}
		
		public boolean isCorrect() {
			return itemBuilder.isCorrect(choice);
		}
		
		public String getSummary() {
			return summary;
		}
		
		public FlowFormItem getSummaryEl() {
			return summaryEl;
		}
		
		public TextElement getPointsEl() {
			return pointsEl;
		}
		
		public Choice getChoice() {
			return choice;
		}
		
		public void setChoice(Choice choice) {
			this.choice = choice;
			if(choice instanceof SimpleChoice) {
				SimpleChoice simpleChoice = (SimpleChoice)choice;
				String answer = new AssessmentHtmlBuilder().flowStaticString(simpleChoice.getFlowStatics());
				answer = FilterFactory.getHtmlTagAndDescapingFilter().filter(answer);
				answer = answer.trim();
				summary = Formatter.truncate(answer, 128);
				if(!StringHelper.containsNonWhitespace(summary)) {
					summaryEl = new FlowFormItem("summary" + count++, itemFileRef);
					summaryEl.setFlowStatics(simpleChoice.getFlowStatics());
					summaryEl.setMapperUri(mapperUri);
					scoreCont.add(summaryEl);
				}
			} else if(choice instanceof Hottext) {
				Hottext hottext = (Hottext)choice;
				String answer = new AssessmentHtmlBuilder().inlineStaticString(hottext.getInlineStatics());
				answer = FilterFactory.getHtmlTagAndDescapingFilter().filter(answer);
				answer = answer.trim();
				summary = Formatter.truncate(answer, 128);
				if(!StringHelper.containsNonWhitespace(summary)) {
					summaryEl = new FlowFormItem("summary" + count++, itemFileRef);
					summaryEl.setInlineStatics(hottext.getInlineStatics());
					summaryEl.setMapperUri(mapperUri);
					scoreCont.add(summaryEl);
				}
			} else {
				summary = "";
			}
		}
	}
}
