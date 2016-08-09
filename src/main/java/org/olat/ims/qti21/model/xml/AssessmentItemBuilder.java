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

import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.P;
import uk.ac.ed.ph.jqtiplus.node.expression.general.BaseValue;
import uk.ac.ed.ph.jqtiplus.node.expression.general.Variable;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.ModalFeedback;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.EndAttemptInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.ResponseDeclaration;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseCondition;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseIf;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseProcessing;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseRule;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.SetOutcomeValue;
import uk.ac.ed.ph.jqtiplus.node.outcome.declaration.OutcomeDeclaration;
import uk.ac.ed.ph.jqtiplus.node.shared.declaration.DefaultValue;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.value.BaseType;
import uk.ac.ed.ph.jqtiplus.value.FloatValue;
import uk.ac.ed.ph.jqtiplus.value.IdentifierValue;
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
	protected final BadRessourceHelper builderHelper;
	
	private ScoreBuilder minScoreBuilder;
	private ScoreBuilder maxScoreBuilder;

	protected ModalFeedbackBuilder hint;
	protected ModalFeedbackBuilder emptyFeedback, answeredFeedback;
	protected ModalFeedbackBuilder correctFeedback;
	protected ModalFeedbackBuilder incorrectFeedback;
	private List<ModalFeedbackBuilder> additionalFeedbacks = new ArrayList<>();
	
	public AssessmentItemBuilder(AssessmentItem assessmentItem, QtiSerializer qtiSerializer) {
		this.assessmentItem = assessmentItem;
		this.qtiSerializer = qtiSerializer;
		builderHelper = new BadRessourceHelper();
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
			} else if(feedbackBuilder.isAnsweredRule()) {
				answeredFeedback = feedbackBuilder;
			} else if(feedbackBuilder.isHint()) {
				hint = feedbackBuilder;
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
	
	public ModalFeedbackBuilder getHint() {
		return hint;
	}
	
	public ModalFeedbackBuilder createHint() {
		hint = new ModalFeedbackBuilder(assessmentItem, null);
		return hint;
	}	
	
	public void removeHint() {
		hint = null;
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
	
	public ModalFeedbackBuilder getAnsweredFeedback() {
		return answeredFeedback;
	}
	
	public ModalFeedbackBuilder createAnsweredFeedback() {
		answeredFeedback = new ModalFeedbackBuilder(assessmentItem, null);
		return answeredFeedback;
	}
	
	public void removeAnsweredFeedback() {
		answeredFeedback = null;
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
	
	public BadRessourceHelper getHelper() {
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

		List<ModalFeedback> modalFeedbacks = assessmentItem.getModalFeedbacks();
		modalFeedbacks.clear();

		buildItemBody();
		buildResponseAndOutcomeDeclarations();
		buildModalFeedbacksAndHints(outcomeDeclarations, responseRules);
		buildMinMaxScores(outcomeDeclarations, responseRules);
		buildMainScoreRule(outcomeDeclarations, responseRules);
		buildHint(outcomeDeclarations, responseRules);
	}
	
	protected abstract void buildItemBody();
	
	protected abstract void buildResponseAndOutcomeDeclarations();
	
	protected void ensureFeedbackBasicOutcomeDeclaration() {
		OutcomeDeclaration feedbackBasicDeclaration = assessmentItem.getOutcomeDeclaration(QTI21Constants.FEEDBACKBASIC_IDENTIFIER);
		if(feedbackBasicDeclaration == null) {
			feedbackBasicDeclaration = AssessmentItemFactory
					.createOutcomeDeclarationForFeedbackBasic(assessmentItem);
			assessmentItem.getOutcomeDeclarations().add(feedbackBasicDeclaration);	
		}
	}
	
	protected abstract void buildMainScoreRule(List<OutcomeDeclaration> outcomeDeclarations, List<ResponseRule> responseRules);
	
	/**
	 * 
	 * @param outcomeDeclarations
	 * @param responseRules
	 */
	protected void buildHint(List<OutcomeDeclaration> outcomeDeclarations, List<ResponseRule> responseRules) {
		if(hint == null) return;

		//response declaration -> identifier=HINTREQUEST -> for the end attempt interaction
		ResponseDeclaration hintResponseDeclaration = AssessmentItemFactory
				.createHintRequestResponseDeclaration(assessmentItem);
		assessmentItem.getResponseDeclarations().add(hintResponseDeclaration);
		
		//outcome declaration -> identifier=HINTFEEDBACKMODAL -> for processing and feedback
		OutcomeDeclaration modalOutcomeDeclaration = AssessmentItemFactory
				.createOutcomeDeclarationForHint(assessmentItem);
		outcomeDeclarations.add(modalOutcomeDeclaration);
		
		//the body
		P paragraph = new P(assessmentItem.getItemBody());
		assessmentItem.getItemBody().getBlocks().add(paragraph);
		
		EndAttemptInteraction endAttemptInteraction = new EndAttemptInteraction(paragraph);
		endAttemptInteraction.setResponseIdentifier(QTI21Constants.HINT_REQUEST_IDENTIFIER);
		endAttemptInteraction.setTitle(hint.getTitle());
		
		paragraph.getInlines().add(endAttemptInteraction);
		
		//the feedback
		ModalFeedback emptyModalFeedback = AssessmentItemFactory
				.createModalFeedback(assessmentItem, QTI21Constants.HINT_FEEDBACKMODAL_IDENTIFIER, QTI21Constants.HINT_IDENTIFIER,
						hint.getTitle(), hint.getText());
		assessmentItem.getModalFeedbacks().add(emptyModalFeedback);

		//the response processing
		ResponseCondition rule = new ResponseCondition(assessmentItem.getResponseProcessing());
		responseRules.add(0, rule);
		
		ResponseIf responseIf = new ResponseIf(rule);
		rule.setResponseIf(responseIf);
		/*
		<responseIf>
			<variable identifier="HINTREQUEST"/>
			<setOutcomeValue identifier="FEEDBACK">
				<baseValue baseType="identifier">HINT</baseValue>
			</setOutcomeValue>
  		</responseIf>
		*/
		Variable variable = new Variable(responseIf);
		variable.setIdentifier(QTI21Constants.HINT_REQUEST_CLX_IDENTIFIER);
		responseIf.getExpressions().add(variable);
		
		SetOutcomeValue hintVar = new SetOutcomeValue(responseIf);
		hintVar.setIdentifier(QTI21Constants.HINT_FEEDBACKMODAL_IDENTIFIER);
		BaseValue hintVal = new BaseValue(hintVar);
		hintVal.setBaseTypeAttrValue(BaseType.IDENTIFIER);
		hintVal.setSingleValue(new IdentifierValue(QTI21Constants.HINT));
		hintVar.setExpression(hintVal);
		responseIf.getResponseRules().add(hintVar);
	}
	
	/**
	 * Add feedbackbasic and feedbackmodal outcomes
	 * @param outcomeDeclarations
	 * @param responseRules
	 */
	protected void buildModalFeedbacksAndHints(List<OutcomeDeclaration> outcomeDeclarations, List<ResponseRule> responseRules) {
		if(correctFeedback != null || incorrectFeedback != null || emptyFeedback != null || answeredFeedback != null
				|| additionalFeedbacks.size() > 0) {
			ensureFeedbackBasicOutcomeDeclaration();
			
			OutcomeDeclaration modalOutcomeDeclaration = AssessmentItemFactory
					.createOutcomeDeclarationForFeedbackModal(assessmentItem);
			outcomeDeclarations.add(modalOutcomeDeclaration);

			List<ModalFeedback> modalFeedbacks = assessmentItem.getModalFeedbacks();
			if(correctFeedback != null) {
				appendModalFeedback(correctFeedback, QTI21Constants.CORRECT, modalFeedbacks, responseRules);
			}
			if(incorrectFeedback != null) {
				appendModalFeedback(incorrectFeedback, QTI21Constants.INCORRECT, modalFeedbacks, responseRules);
			}
			if(emptyFeedback != null) {
				appendModalFeedback(emptyFeedback, QTI21Constants.EMPTY, modalFeedbacks, responseRules);
			}
			if(answeredFeedback != null) {
				appendModalFeedback(answeredFeedback, QTI21Constants.ANSWERED, modalFeedbacks, responseRules);
			}
		}
	}
	
	protected void appendModalFeedback(ModalFeedbackBuilder feedbackBuilder, String inCorrect,
			List<ModalFeedback> modalFeedbacks, List<ResponseRule> responseRules) {
		
		ModalFeedback emptyModalFeedback = AssessmentItemFactory
				.createModalFeedback(assessmentItem, feedbackBuilder.getIdentifier(),
						feedbackBuilder.getTitle(), feedbackBuilder.getText());
		modalFeedbacks.add(emptyModalFeedback);
		
		ResponseCondition feedbackCondition = AssessmentItemFactory
				.createModalFeedbackBasicRule(assessmentItem.getResponseProcessing(),
						feedbackBuilder.getIdentifier(), inCorrect, hint != null);
		responseRules.add(feedbackCondition);
	}
	
	/**
	 * Add outcome declaration for score, min. score and mx. score.
	 * and the response rules which ensure that the score is between
	 * the max. score and min. score.
	 * 
	 * @param outcomeDeclarations
	 * @param responseRules
	 */
	protected final void buildMinMaxScores(List<OutcomeDeclaration> outcomeDeclarations, List<ResponseRule> responseRules) {
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