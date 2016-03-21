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
package org.olat.ims.qti21.model.xml;

import static org.olat.ims.qti21.QTI21Constants.MAXSCORE_IDENTIFIER;
import static org.olat.ims.qti21.QTI21Constants.MINSCORE_IDENTIFIER;

import java.util.ArrayList;
import java.util.List;

import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.model.QTI21QuestionType;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.ModalFeedback;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.ResponseDeclaration;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseCondition;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseProcessing;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseRule;
import uk.ac.ed.ph.jqtiplus.node.outcome.declaration.OutcomeDeclaration;
import uk.ac.ed.ph.jqtiplus.node.shared.declaration.DefaultValue;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.value.FloatValue;
import uk.ac.ed.ph.jqtiplus.value.Value;

/**
 * 
 * 
 * Initial date: 08.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AssessmentItemBuilder {

	protected final AssessmentItem assessmentItem;
	protected final QtiSerializer qtiSerializer;
	protected final AssessmentHtmlBuilder htmlHelper;
	protected final AssessmentBuilderHelper builderHelper;
	
	private ScoreBuilder minScoreBuilder;
	private ScoreBuilder maxScoreBuilder;

	protected ModalFeedbackBuilder emptyFeedback;
	protected ModalFeedbackBuilder correctFeedback;
	protected ModalFeedbackBuilder incorrectFeedback;
	private List<ModalFeedbackBuilder> additionalFeedbacks = new ArrayList<>();
	
	public AssessmentItemBuilder(AssessmentItem assessmentItem, QtiSerializer qtiSerializer) {
		this.assessmentItem = assessmentItem;
		this.qtiSerializer = qtiSerializer;
		builderHelper = new AssessmentBuilderHelper();
		htmlHelper = new AssessmentHtmlBuilder(qtiSerializer);
		extract();
	}
	
	public AssessmentItem getAssessmentItem() {
		return assessmentItem;
	}
	
	public abstract QTI21QuestionType getQuestionType();
	
	protected void extract() {
		extractMinScore();
		extractMaxScore();
		extractModalFeedbacks();
	}
	
	private void extractMinScore() {
		OutcomeDeclaration outcomeDeclaration = assessmentItem.getOutcomeDeclaration(MINSCORE_IDENTIFIER);
		if(outcomeDeclaration != null) {
			DefaultValue defaultValue = outcomeDeclaration.getDefaultValue();
			if(defaultValue != null) {
				Value minScoreValue = defaultValue.evaluate();
				if(minScoreValue instanceof FloatValue) {
					Double minScore = new Double(((FloatValue)minScoreValue).doubleValue());
					minScoreBuilder = new ScoreBuilder(minScore, outcomeDeclaration);
				}
			}
		}
	}
	
	private void extractMaxScore() {
		OutcomeDeclaration outcomeDeclaration = assessmentItem.getOutcomeDeclaration(MAXSCORE_IDENTIFIER);
		if(outcomeDeclaration != null) {
			DefaultValue defaultValue = outcomeDeclaration.getDefaultValue();
			if(defaultValue != null) {
				Value maxScoreValue = defaultValue.evaluate();
				if(maxScoreValue instanceof FloatValue) {
					Double maxScore = new Double(((FloatValue)maxScoreValue).doubleValue());
					maxScoreBuilder = new ScoreBuilder(maxScore, outcomeDeclaration);
				}
			}
		}
	}
	
	private void extractModalFeedbacks() {
		List<ModalFeedback> feedbacks = assessmentItem.getModalFeedbacks();
		for(ModalFeedback feedback:feedbacks) {
			ModalFeedbackBuilder feedbackBuilder = new ModalFeedbackBuilder(assessmentItem, feedback);
			if(feedbackBuilder.isCorrectRule()) {
				correctFeedback = feedbackBuilder;
			} else if(feedbackBuilder.isIncorrectRule()) {
				incorrectFeedback = feedbackBuilder;
			} else if(feedbackBuilder.isEmptyRule()) {
				emptyFeedback = feedbackBuilder;
			} else {
				additionalFeedbacks.add(feedbackBuilder);
			}
		}
	}
	
	public String getTitle() {
		return assessmentItem.getTitle();
	}
	
	public void setTitle(String title) {
		assessmentItem.setTitle(title);
	}
	
	public abstract String getQuestion();
	
	public abstract void setQuestion(String question);
	
	public ScoreBuilder getMinScoreBuilder() {
		return minScoreBuilder;
	}
	
	public void setMinScore(Double minScore) {
		if(minScoreBuilder == null) {
			minScoreBuilder = new ScoreBuilder(minScore, null);
		} else {
			minScoreBuilder.setScore(minScore);
		}
	}

	public ScoreBuilder getMaxScoreBuilder() {
		return maxScoreBuilder;
	}
	
	public void setMaxScore(Double maxScore) {
		if(maxScoreBuilder == null) {
			maxScoreBuilder = new ScoreBuilder(maxScore, null);
		} else {
			maxScoreBuilder.setScore(maxScore);
		}
	}
	
	public ModalFeedbackBuilder getCorrectFeedback() {
		return correctFeedback;
	}
	
	public ModalFeedbackBuilder createCorrectFeedback() {
		correctFeedback = new ModalFeedbackBuilder(assessmentItem, null);
		return correctFeedback;
	}
	
	public void removeCorrectFeedback() {
		correctFeedback = null;
	}
	
	public ModalFeedbackBuilder getEmptyFeedback() {
		return emptyFeedback;
	}
	
	public ModalFeedbackBuilder createEmptyFeedback() {
		emptyFeedback = new ModalFeedbackBuilder(assessmentItem, null);
		return emptyFeedback;
	}
	public void removeEmptyFeedback() {
		emptyFeedback = null;
	}
	
	public ModalFeedbackBuilder getIncorrectFeedback() {
		return incorrectFeedback;
	}
	
	public ModalFeedbackBuilder createIncorrectFeedback() {
		incorrectFeedback = new ModalFeedbackBuilder(assessmentItem, null);
		return incorrectFeedback;
	}
	
	public void removeIncorrectFeedback() {
		incorrectFeedback = null;
	}
	
	public AssessmentBuilderHelper getHelper() {
		return builderHelper;
	}
	
	public AssessmentHtmlBuilder getHtmlHelper() {
		return htmlHelper;
	}
	
	public List<String> getInteractionNames() {
		List<Interaction> interactions = assessmentItem.getItemBody().findInteractions();
		List<String> interactionNames = new ArrayList<>(interactions.size());
		for(Interaction interaction:interactions) {
			String interactionName = interaction.getQtiClassName();
			interactionNames.add(interactionName);
		}
		
		return interactionNames;
	}

	public final void build() {
		List<OutcomeDeclaration> outcomeDeclarations = assessmentItem.getOutcomeDeclarations();
		outcomeDeclarations.clear();
		
		ResponseProcessing responseProcessing = assessmentItem.getResponseProcessing();
		List<ResponseRule> responseRules = responseProcessing.getResponseRules();
		responseRules.clear();
		
		List<ResponseDeclaration> responseDeclarations = assessmentItem.getResponseDeclarations();
		responseDeclarations.clear();

		buildItemBody();
		buildResponseDeclaration();
		buildModalFeedback(outcomeDeclarations, responseRules);
		buildScores(outcomeDeclarations, responseRules);
		buildMainScoreRule(outcomeDeclarations, responseRules);
	}
	
	protected void buildResponseDeclaration() {
		//
	}
	
	protected void buildItemBody() {
		//
	}
	
	protected abstract void buildMainScoreRule(List<OutcomeDeclaration> outcomeDeclarations, List<ResponseRule> responseRules);
	
	protected void buildModalFeedback(List<OutcomeDeclaration> outcomeDeclarations, List<ResponseRule> responseRules) {
		//add feedbackbasic and feedbackmodal outcomes
		if(correctFeedback != null || incorrectFeedback != null || emptyFeedback != null
				|| additionalFeedbacks.size() > 0) {
			ensureFeedbackBasicOutcomeDeclaration();
			
			OutcomeDeclaration modalOutcomeDeclaration = AssessmentItemFactory
					.createOutcomeDeclarationForFeedbackModal(assessmentItem);
			outcomeDeclarations.add(modalOutcomeDeclaration);
		}

		//add modal
		List<ModalFeedback> modalFeedbacks = assessmentItem.getModalFeedbacks();
		modalFeedbacks.clear();
		
		if(correctFeedback != null) {
			ModalFeedback correctModalFeedback = AssessmentItemFactory
					.createModalFeedback(assessmentItem, correctFeedback.getIdentifier(),
							correctFeedback.getTitle(), correctFeedback.getText());
			modalFeedbacks.add(correctModalFeedback);
			
			ResponseCondition feedbackCondition = AssessmentItemFactory
					.createModalFeedbackBasicRule(assessmentItem.getResponseProcessing(), correctFeedback.getIdentifier(), QTI21Constants.CORRECT);
			responseRules.add(feedbackCondition);
		}
		
		if(emptyFeedback != null) {
			ModalFeedback emptyModalFeedback = AssessmentItemFactory
					.createModalFeedback(assessmentItem, emptyFeedback.getIdentifier(),
							emptyFeedback.getTitle(), emptyFeedback.getText());
			modalFeedbacks.add(emptyModalFeedback);
			
			ResponseCondition feedbackCondition = AssessmentItemFactory
					.createModalFeedbackBasicRule(assessmentItem.getResponseProcessing(), emptyFeedback.getIdentifier(), QTI21Constants.EMPTY);
			responseRules.add(feedbackCondition);
		}
		
		if(incorrectFeedback != null) {
			ModalFeedback incorrectModalFeedback = AssessmentItemFactory
					.createModalFeedback(assessmentItem, incorrectFeedback.getIdentifier(),
							incorrectFeedback.getTitle(), incorrectFeedback.getText());
			modalFeedbacks.add(incorrectModalFeedback);
			
			ResponseCondition feedbackCondition = AssessmentItemFactory
					.createModalFeedbackBasicRule(assessmentItem.getResponseProcessing(), incorrectFeedback.getIdentifier(), QTI21Constants.INCORRECT);
			responseRules.add(feedbackCondition);
		}
	}
	
	protected void ensureFeedbackBasicOutcomeDeclaration() {
		OutcomeDeclaration feedbackBasicDeclaration = assessmentItem.getOutcomeDeclaration(QTI21Constants.FEEDBACKBASIC_IDENTIFIER);
		if(feedbackBasicDeclaration == null) {
			feedbackBasicDeclaration = AssessmentItemFactory
					.createOutcomeDeclarationForFeedbackBasic(assessmentItem);
			assessmentItem.getOutcomeDeclarations().add(feedbackBasicDeclaration);	
		}
	}
	
	/**
	 * Add outcome declaration for score, min. score and mx. score.
	 * and the response rules which ensure that the score is between
	 * the max. score and min. score.
	 * 
	 * @param outcomeDeclarations
	 * @param responseRules
	 */
	protected void buildScores(List<OutcomeDeclaration> outcomeDeclarations, List<ResponseRule> responseRules) {
		if((getMinScoreBuilder() != null && getMinScoreBuilder().getScore() != null)
				|| (getMaxScoreBuilder() != null && getMaxScoreBuilder().getScore() != null)) {
			
			OutcomeDeclaration outcomeDeclaration = AssessmentItemFactory
					.createOutcomeDeclarationForScore(assessmentItem);
			outcomeDeclarations.add(outcomeDeclaration);	
		}
		
		if(getMinScoreBuilder() != null && getMinScoreBuilder().getScore() != null) {
			OutcomeDeclaration outcomeDeclaration = AssessmentItemFactory
					.createOutcomeDeclarationForMinScore(assessmentItem, getMinScoreBuilder().getScore().doubleValue());
			outcomeDeclarations.add(outcomeDeclaration);
			
			//ensure that the score is not smaller than min. score value
			ResponseRule minScoreBoundResponseRule = AssessmentItemFactory
					.createMinScoreBoundLimitRule(assessmentItem.getResponseProcessing());
			responseRules.add(minScoreBoundResponseRule);
		}
		
		if(getMaxScoreBuilder() != null && getMaxScoreBuilder().getScore() != null) {
			OutcomeDeclaration outcomeDeclaration = AssessmentItemFactory
					.createOutcomeDeclarationForMaxScore(assessmentItem, getMaxScoreBuilder().getScore().doubleValue());
			outcomeDeclarations.add(outcomeDeclaration);
			
			//ensure that the score is not bigger than the max. score value
			ResponseRule maxScoreBoundResponseRule = AssessmentItemFactory
					.createMaxScoreBoundLimitRule(assessmentItem.getResponseProcessing());
			responseRules.add(maxScoreBoundResponseRule);
		}
	}
}