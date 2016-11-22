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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.ims.qti21.model.xml.AssessmentHtmlBuilder;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.ScoreBuilder;
import org.olat.ims.qti21.model.xml.interactions.MatchAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.SimpleChoiceAssessmentItemBuilder.ScoreEvaluation;
import org.olat.ims.qti21.ui.editor.AssessmentTestEditorController;
import org.olat.ims.qti21.ui.editor.SyncAssessmentItem;
import org.olat.ims.qti21.ui.editor.events.AssessmentItemEvent;

import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleAssociableChoice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleMatchSet;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.DirectedPairValue;

/**
 * 
 * Initial date: 22 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MatchScoreController extends AssessmentItemRefEditorController implements SyncAssessmentItem {
	
	private static final String[] modeKeys = new String[]{
			ScoreEvaluation.allCorrectAnswers.name(), ScoreEvaluation.perAnswer.name()
		};
	
	private TextElement minScoreEl;
	private TextElement maxScoreEl;
	private SingleSelection assessmentModeEl;
	private FormLayoutContainer scoreCont;
	
	private MatchAssessmentItemBuilder itemBuilder;
	
	private List<MatchWrapper> sourceWrappers = new ArrayList<>();
	private List<MatchWrapper> targetWrappers = new ArrayList<>();
	private Map<DirectedPairValue, MatchScoreWrapper> scoreWrappers = new HashMap<>();
	
	public MatchScoreController(UserRequest ureq, WindowControl wControl, MatchAssessmentItemBuilder itemBuilder,
			AssessmentItemRef itemRef, boolean restrictedEdit) {
		super(ureq, wControl, itemRef, restrictedEdit);
		setTranslator(Util.createPackageTranslator(AssessmentTestEditorController.class, getLocale()));
		this.itemBuilder = itemBuilder;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		super.initForm(formLayout, listener, ureq);
		setFormContextHelp("Test editor QTI 2.1 in detail#details_testeditor_score");
		
		minScoreEl = uifactory.addTextElement("min.score", "min.score", 8, "0.0", formLayout);
		minScoreEl.setEnabled(false);
		minScoreEl.setEnabled(!restrictedEdit);
		
		ScoreBuilder maxScore = itemBuilder.getMaxScoreBuilder();
		String maxValue = maxScore == null ? "" : (maxScore.getScore() == null ? "" : maxScore.getScore().toString());
		maxScoreEl = uifactory.addTextElement("max.score", "max.score", 8, maxValue, formLayout);
		maxScoreEl.setEnabled(!restrictedEdit);
		
		String[] modeValues = new String[]{
				translate("form.score.assessment.all.correct"),
				translate("form.score.assessment.per.answer")
		};
		assessmentModeEl = uifactory.addRadiosHorizontal("assessment.mode", "form.score.assessment.mode", formLayout, modeKeys, modeValues);
		assessmentModeEl.addActionListener(FormEvent.ONCHANGE);
		assessmentModeEl.setEnabled(!restrictedEdit);
		if(itemBuilder.getScoreEvaluationMode() == ScoreEvaluation.perAnswer) {
			assessmentModeEl.select(ScoreEvaluation.perAnswer.name(), true);
		} else {
			assessmentModeEl.select(ScoreEvaluation.allCorrectAnswers.name(), true);
		}
		
		String scorePage = velocity_root + "/match_score.html";
		scoreCont = FormLayoutContainer.createCustomFormLayout("scores", getTranslator(), scorePage);
		formLayout.add(scoreCont);
		scoreCont.setLabel(null, null);
		scoreCont.setVisible(assessmentModeEl.isSelected(1));
		
		for(SimpleAssociableChoice choice:itemBuilder.getSourceMatchSet().getSimpleAssociableChoices()) {
			sourceWrappers.add(createMatchWrapper(choice));
		}
		scoreCont.contextPut("sourceChoices", sourceWrappers);
		for(SimpleAssociableChoice choice:itemBuilder.getTargetMatchSet().getSimpleAssociableChoices()) {
			targetWrappers.add(createMatchWrapper(choice));
		}
		scoreCont.contextPut("targetChoices", targetWrappers);
		forgeScoreElements();

		// Submit Button
		FormLayoutContainer buttonsContainer = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsContainer.setRootForm(mainForm);
		formLayout.add(buttonsContainer);
		uifactory.addFormSubmitButton("submit", buttonsContainer);
	}
	
	@Override
	public void sync(UserRequest ureq, AssessmentItemBuilder assessmentItemBuilder) {
		if(itemBuilder == assessmentItemBuilder) {
			sourceWrappers = sync(itemBuilder.getSourceMatchSet(), sourceWrappers);
			targetWrappers = sync(itemBuilder.getTargetMatchSet(), targetWrappers);
			scoreCont.contextPut("sourceChoices", sourceWrappers);
			scoreCont.contextPut("targetChoices", targetWrappers);
			forgeScoreElements();
		}
	}
	
	private List<MatchWrapper> sync(SimpleMatchSet matchSet, List<MatchWrapper> wrappers) {
		Map<Identifier,MatchWrapper> currentMapping = wrappers.stream()
				.collect(Collectors.toMap(w -> w.getChoiceIdentifier(), w -> w));

		List<MatchWrapper> newWrappers = new ArrayList<>();
		List<SimpleAssociableChoice> choices = matchSet.getSimpleAssociableChoices();
		for(SimpleAssociableChoice choice:choices) {
			if(currentMapping.containsKey(choice.getIdentifier())) {
				newWrappers.add(currentMapping.get(choice.getIdentifier()));
			} else {
				newWrappers.add(createMatchWrapper(choice));
			}
		}
		
		return newWrappers;
	}
	
	private void forgeScoreElements() {
		for(MatchWrapper sourceWrapper:sourceWrappers) {
			for(MatchWrapper targetWrapper:targetWrappers) {
				forgeScoreElement(sourceWrapper, targetWrapper);
			}
		}
	}
	
	private void forgeScoreElement(MatchWrapper sourceWrapper, MatchWrapper targetWrapper) {
		Identifier sourceIdentifier = sourceWrapper.getChoiceIdentifier();
		Identifier targetIdentifier = targetWrapper.getChoiceIdentifier();
		DirectedPairValue dKey = new DirectedPairValue(sourceIdentifier, targetIdentifier);
		if(!scoreWrappers.containsKey(dKey)) {
			String key = sourceIdentifier.toString() + "-" + targetIdentifier.toString();
			TextElement textEl = uifactory.addTextElement(key, null, 4, "", scoreCont);
			MatchScoreWrapper scoreWrapper = new MatchScoreWrapper(sourceIdentifier, targetIdentifier, textEl);
			textEl.setDomReplacementWrapperRequired(false);
			textEl.setDisplaySize(4);
			textEl.setUserObject(scoreWrapper);
			textEl.setEnabled(!restrictedEdit);
			
			Double score = itemBuilder.getScore(sourceIdentifier, targetIdentifier);
			if(score == null) {
				textEl.setValue("0.0");
			} else {
				textEl.setValue(AssessmentHelper.getRoundedScore(score));
			}
			
			scoreWrappers.put(dKey, scoreWrapper);
			scoreCont.contextPut(key, scoreWrapper);
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		allOk &= validateDouble(maxScoreEl);

		if(assessmentModeEl.isOneSelected() && assessmentModeEl.isSelected(1)) {
			/*for(HotspotChoiceWrapper wrapper:wrappers) {
				allOk &= validateDouble(wrapper.getPointsEl());
			}*/
		}
		
		return allOk & super.validateFormLogic(ureq);
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
		super.formOK(ureq);
		String maxScoreValue = maxScoreEl.getValue();
		Double maxScore = Double.parseDouble(maxScoreValue);
		itemBuilder.setMaxScore(maxScore);
		itemBuilder.setMinScore(new Double(0d));
		
		if(assessmentModeEl.isOneSelected() && assessmentModeEl.isSelected(1)) {
			itemBuilder.setScoreEvaluationMode(ScoreEvaluation.perAnswer);
			itemBuilder.clearMapping();
			
			for(Map.Entry<DirectedPairValue, MatchScoreWrapper> entry:scoreWrappers.entrySet()) {
				DirectedPairValue directedPair = entry.getKey();
				MatchScoreWrapper scoreWrapper = entry.getValue();
				
				String val = scoreWrapper.getScoreEl().getValue();
				double score = Double.parseDouble(val);
				itemBuilder.addScore(directedPair, score);
			}
		} else {
			itemBuilder.setScoreEvaluationMode(ScoreEvaluation.allCorrectAnswers);
			itemBuilder.clearMapping();
		}

		fireEvent(ureq, new AssessmentItemEvent(AssessmentItemEvent.ASSESSMENT_ITEM_CHANGED, itemBuilder.getAssessmentItem(), null));
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private MatchWrapper createMatchWrapper(SimpleAssociableChoice choice) {
		return new MatchWrapper(choice.getIdentifier(), choice);
	}
	
	public static class MatchWrapper {

		private final Identifier choiceIdentifier;
		private final SimpleAssociableChoice choice;
		private String summary;
		
		public MatchWrapper(Identifier choiceIdentifier, SimpleAssociableChoice choice) {
			this.choiceIdentifier = choiceIdentifier;
			this.choice = choice;
			if(choice != null) {
				summary = new AssessmentHtmlBuilder().flowStaticString(choice.getFlowStatics());
			} else {
				summary = "";
			}
		}
		
		public String getSummary() {
			return summary;
		}

		public Identifier getChoiceIdentifier() {
			return choiceIdentifier;
		}

		public SimpleAssociableChoice getChoice() {
			return choice;
		}
	}
	
	public class MatchScoreWrapper {
		
		private final Identifier sourceIdentifier;
		private final Identifier targetIdentifier;
		private final TextElement scoreEl;
		
		public MatchScoreWrapper(Identifier sourceIdentifier, Identifier targetIdentifier, TextElement scoreEl) {
			this.scoreEl = scoreEl;
			this.sourceIdentifier = sourceIdentifier;
			this.targetIdentifier = targetIdentifier;
		}

		public Identifier getSourceIdentifier() {
			return sourceIdentifier;
		}

		public Identifier getTargetIdentifier() {
			return targetIdentifier;
		}

		public TextElement getScoreEl() {
			return scoreEl;
		}
		
		public boolean isCorrect() {
			return itemBuilder.isCorrect(sourceIdentifier, targetIdentifier);
		}
	}
}