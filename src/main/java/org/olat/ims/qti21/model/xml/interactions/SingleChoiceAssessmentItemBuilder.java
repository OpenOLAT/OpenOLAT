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
package org.olat.ims.qti21.model.xml.interactions;

import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.appendChoiceInteraction;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.appendDefaultItemBody;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.appendDefaultOutcomeDeclarations;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.appendSimpleChoice;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.createResponseProcessing;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.createSingleChoiceCorrectResponseDeclaration;

import java.util.List;

import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.model.IdentifierGenerator;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.AssessmentItemFactory;

import uk.ac.ed.ph.jqtiplus.node.content.ItemBody;
import uk.ac.ed.ph.jqtiplus.node.content.basic.Block;
import uk.ac.ed.ph.jqtiplus.node.expression.general.BaseValue;
import uk.ac.ed.ph.jqtiplus.node.expression.general.Correct;
import uk.ac.ed.ph.jqtiplus.node.expression.general.MapResponse;
import uk.ac.ed.ph.jqtiplus.node.expression.general.Variable;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.IsNull;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Match;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Sum;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.CorrectResponse;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.Choice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleChoice;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.ResponseDeclaration;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseCondition;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseElse;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseElseIf;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseIf;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseProcessing;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseRule;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.SetOutcomeValue;
import uk.ac.ed.ph.jqtiplus.node.outcome.declaration.OutcomeDeclaration;
import uk.ac.ed.ph.jqtiplus.node.shared.FieldValue;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.types.ComplexReferenceIdentifier;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.BaseType;
import uk.ac.ed.ph.jqtiplus.value.Cardinality;
import uk.ac.ed.ph.jqtiplus.value.IdentifierValue;
import uk.ac.ed.ph.jqtiplus.value.Value;

/**
 * 
 * Initial date: 08.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SingleChoiceAssessmentItemBuilder extends SimpleChoiceAssessmentItemBuilder {
	
	private Identifier correctAnswer;
	
	public SingleChoiceAssessmentItemBuilder(String title, String defaultAnswer, QtiSerializer qtiSerializer) {
		super(createAssessmentItem(title, defaultAnswer), qtiSerializer);
	}
	
	public SingleChoiceAssessmentItemBuilder(AssessmentItem assessmentItem, QtiSerializer qtiSerializer) {
		super(assessmentItem, qtiSerializer);
	}
	
	private static AssessmentItem createAssessmentItem(String title, String defaultAnswer) {
		AssessmentItem assessmentItem = AssessmentItemFactory.createAssessmentItem(QTI21QuestionType.sc, title);

		//define correct answer
		Identifier responseDeclarationId = Identifier.assumedLegal("RESPONSE_1");
		Identifier correctResponseId = IdentifierGenerator.newAsIdentifier("sc");
		ResponseDeclaration responseDeclaration = createSingleChoiceCorrectResponseDeclaration(assessmentItem, responseDeclarationId, correctResponseId);
		assessmentItem.getNodeGroups().getResponseDeclarationGroup().getResponseDeclarations().add(responseDeclaration);
		
		//outcomes
		appendDefaultOutcomeDeclarations(assessmentItem, 1.0d);
		
		//the single choice interaction
		ItemBody itemBody = appendDefaultItemBody(assessmentItem);
		ChoiceInteraction choiceInteraction = appendChoiceInteraction(itemBody, responseDeclarationId, 1, true);
		
		appendSimpleChoice(choiceInteraction, defaultAnswer, correctResponseId);

		//response processing
		ResponseProcessing responseProcessing = createResponseProcessing(assessmentItem, responseDeclarationId);
		assessmentItem.getNodeGroups().getResponseProcessingGroup().setResponseProcessing(responseProcessing);
		return assessmentItem;
	}
	
	@Override
	public void extract() {
		super.extract();
		
		if(choiceInteraction != null) {
			ResponseDeclaration responseDeclaration = assessmentItem.getResponseDeclaration(choiceInteraction.getResponseIdentifier());
			if(responseDeclaration != null && responseDeclaration.getCorrectResponse() != null) {
				CorrectResponse correctResponse = responseDeclaration.getCorrectResponse();
				List<FieldValue> values = correctResponse.getFieldValues();
				Value value = FieldValue.computeValue(Cardinality.SINGLE, values);
				if(value instanceof IdentifierValue) {
					IdentifierValue identifierValue = (IdentifierValue)value;
					correctAnswer = identifierValue.identifierValue();
				}
			}
		}
	}
	
	@Override
	public QTI21QuestionType getQuestionType() {
		return QTI21QuestionType.sc;
	}

	@Override
	public boolean isCorrect(Choice choice) {
		return correctAnswer != null && correctAnswer.equals(choice.getIdentifier());
	}
	
	public void setCorrectAnswer(Identifier identifier) {
		correctAnswer = identifier;
	}
	
	@Override
	public int getMaxPossibleCorrectAnswers() {
		return 1;
	}
	
	@Override
	public void clearSimpleChoices() {
		correctAnswer = null;
		super.clearSimpleChoices();
	}
	
	@Override
	public boolean scoreOfCorrectAnswerWarning() {
		boolean warning = false;
		if(getScoreEvaluationMode() == ScoreEvaluation.perAnswer) {
			boolean wrongAnswerHasPoint = false;
			boolean correctAnswerHasNotPoint = false;
			
			for(SimpleChoice choiceWrapper:getChoices()) {
				Double score = getMapping(choiceWrapper.getIdentifier());
				if(isCorrect(choiceWrapper)) {
					correctAnswerHasNotPoint = (score == null || score.doubleValue() < 0.000001);
				} else {
					wrongAnswerHasPoint |= (score != null && score.doubleValue() > 0.6);
				}	
			}
			warning |= wrongAnswerHasPoint && correctAnswerHasNotPoint;
		}
		return warning;
	}

	@Override
	protected void buildResponseAndOutcomeDeclarations() {
		ResponseDeclaration responseDeclaration = AssessmentItemFactory
				.createSingleChoiceCorrectResponseDeclaration(assessmentItem, responseIdentifier, correctAnswer);
		if(scoreEvaluation == ScoreEvaluation.perAnswer) {
			AssessmentItemFactory.appendMapping(responseDeclaration, scoreMapping);
		}
		assessmentItem.getResponseDeclarations().add(responseDeclaration);
	}
	
	@Override
	protected void buildItemBody() {
		//remove current blocks
		List<Block> blocks = assessmentItem.getItemBody().getBlocks();
		blocks.clear();

		//add question
		getHtmlHelper().appendHtml(assessmentItem.getItemBody(), question);
		
		//add interaction
		ChoiceInteraction singleChoiceInteraction = AssessmentItemFactory
				.createSingleChoiceInteraction(assessmentItem, responseIdentifier, orientation, cssClass);
		singleChoiceInteraction.setShuffle(isShuffle());
		blocks.add(singleChoiceInteraction);
		List<SimpleChoice> choiceList = getChoices();
		singleChoiceInteraction.getSimpleChoices().addAll(choiceList);
	}

	@Override
	protected void buildMainScoreRule(List<OutcomeDeclaration> outcomeDeclarations, List<ResponseRule> responseRules) {
		ResponseCondition rule = new ResponseCondition(assessmentItem.getResponseProcessing());
		responseRules.add(0, rule);
		if(scoreEvaluation == ScoreEvaluation.perAnswer) {
			buildMainScoreRulePerAnswer(rule);
		} else {
			buildMainScoreRuleAllCorrectAnswers(rule);
		}
	}

	private void buildMainScoreRuleAllCorrectAnswers(ResponseCondition rule) {
		ensureFeedbackBasicOutcomeDeclaration();
		/*
			<responseIf>
				<isNull>
					<variable identifier="RESPONSE_1" />
				</isNull>
				<setOutcomeValue identifier="FEEDBACKBASIC">
					<baseValue baseType="identifier">
						empty
					</baseValue>
				</setOutcomeValue>
			</responseIf>
		*/
		
		//if no response
		ResponseIf responseIf = new ResponseIf(rule);
		rule.setResponseIf(responseIf);
		
		IsNull isNull = new IsNull(responseIf);
		responseIf.getExpressions().add(isNull);
		
		Variable variable = new Variable(isNull);
		variable.setIdentifier(ComplexReferenceIdentifier.parseString(responseIdentifier.toString()));
		isNull.getExpressions().add(variable);
		
		{
			SetOutcomeValue feedbackVar = new SetOutcomeValue(responseIf);
			feedbackVar.setIdentifier(QTI21Constants.FEEDBACKBASIC_IDENTIFIER);
			BaseValue feedbackVal = new BaseValue(feedbackVar);
			feedbackVal.setBaseTypeAttrValue(BaseType.IDENTIFIER);
			feedbackVal.setSingleValue(new IdentifierValue("empty"));
			feedbackVar.setExpression(feedbackVal);
			responseIf.getResponseRules().add(feedbackVar);
		}
		/*
			<responseElseIf>
				<match>
					<variable identifier="RESPONSE_1" />
					<correct identifier="RESPONSE_1" />
				</match>
				<setOutcomeValue identifier="SCORE">
					<sum>
						<variable identifier="SCORE" />
						<variable identifier="MAXSCORE" />
					</sum>
				</setOutcomeValue>
				<setOutcomeValue identifier="FEEDBACKBASIC">
					<baseValue baseType="identifier">
						correct
					</baseValue>
				</setOutcomeValue>
			</responseElseIf>
		*/
		
		//else if correct response
		ResponseElseIf responseElseIf = new ResponseElseIf(rule);
		rule.getResponseElseIfs().add(responseElseIf);
		
		//match 
		{
			Match match = new Match(responseElseIf);
			responseElseIf.getExpressions().add(match);
			
			Variable responseVar = new Variable(match);
			responseVar.setIdentifier(ComplexReferenceIdentifier.parseString(responseIdentifier.toString()));
			match.getExpressions().add(responseVar);
			
			Correct correct = new Correct(match);
			correct.setIdentifier(ComplexReferenceIdentifier.parseString(responseIdentifier.toString()));
			match.getExpressions().add(correct);
		}

		// outcome score
		{
			SetOutcomeValue scoreOutcomeVar = new SetOutcomeValue(responseIf);
			scoreOutcomeVar.setIdentifier(QTI21Constants.SCORE_IDENTIFIER);
			responseElseIf.getResponseRules().add(scoreOutcomeVar);
			
			Sum sum = new Sum(scoreOutcomeVar);
			scoreOutcomeVar.getExpressions().add(sum);
			
			Variable scoreVar = new Variable(sum);
			scoreVar.setIdentifier(QTI21Constants.SCORE_CLX_IDENTIFIER);
			sum.getExpressions().add(scoreVar);
			
			Variable maxScoreVar = new Variable(sum);
			maxScoreVar.setIdentifier(QTI21Constants.MAXSCORE_CLX_IDENTIFIER);
			sum.getExpressions().add(maxScoreVar);
		}
		
		// outcome feedback
		{
			SetOutcomeValue correctFeedbackVar = new SetOutcomeValue(responseIf);
			correctFeedbackVar.setIdentifier(QTI21Constants.FEEDBACKBASIC_IDENTIFIER);
			BaseValue correctFeedbackVal = new BaseValue(correctFeedbackVar);
			correctFeedbackVal.setBaseTypeAttrValue(BaseType.IDENTIFIER);
			correctFeedbackVal.setSingleValue(QTI21Constants.CORRECT_IDENTIFIER_VALUE);
			correctFeedbackVar.setExpression(correctFeedbackVal);
			responseElseIf.getResponseRules().add(correctFeedbackVar);
		}
		
		/*
			<responseElse>
				<setOutcomeValue identifier="FEEDBACKBASIC">
					<baseValue baseType="identifier">
						incorrect
					</baseValue>
				</setOutcomeValue>
			</responseElse>
		</responseCondition>
		*/
		
		ResponseElse responseElse = new ResponseElse(rule);
		rule.setResponseElse(responseElse);
		{// feedback incorrect
			SetOutcomeValue incorrectFeedbackVar = new SetOutcomeValue(responseIf);
			incorrectFeedbackVar.setIdentifier(QTI21Constants.FEEDBACKBASIC_IDENTIFIER);
			BaseValue incorrectFeedbackVal = new BaseValue(incorrectFeedbackVar);
			incorrectFeedbackVal.setBaseTypeAttrValue(BaseType.IDENTIFIER);
			incorrectFeedbackVal.setSingleValue(QTI21Constants.INCORRECT_IDENTIFIER_VALUE);
			incorrectFeedbackVar.setExpression(incorrectFeedbackVal);
			responseElse.getResponseRules().add(incorrectFeedbackVar);
		}
	}
	

	private void buildMainScoreRulePerAnswer(ResponseCondition rule) {
		ensureFeedbackBasicOutcomeDeclaration();
		/*
		<responseCondition>
			<responseIf>
				<match>
					<variable identifier="RESPONSE_1" />
					<correct identifier="RESPONSE_1" />
				</match>
				<setOutcomeValue identifier="SCORE">
					<sum>
						<variable identifier="SCORE" />
						<mapResponse identifier="RESPONSE_1" />
					</sum>
				</setOutcomeValue>
				<setOutcomeValue identifier="FEEDBACKBASIC">
					<baseValue baseType="identifier">
						correct
					</baseValue>
				</setOutcomeValue>
			</responseIf>
		</responseCondition>
		 */
		ResponseIf responseIf = new ResponseIf(rule);
		rule.setResponseIf(responseIf);
		
		{// match the correct answer
			Match match = new Match(responseIf);
			responseIf.getExpressions().add(match);
			
			Variable responseVar = new Variable(match);
			ComplexReferenceIdentifier choiceResponseIdentifier
				= ComplexReferenceIdentifier.parseString(choiceInteraction.getResponseIdentifier().toString());
			responseVar.setIdentifier(choiceResponseIdentifier);
			match.getExpressions().add(responseVar);
			
			Correct correct = new Correct(match);
			correct.setIdentifier(choiceResponseIdentifier);
			match.getExpressions().add(correct);
		}
		
		{// outcome score
			SetOutcomeValue scoreOutcome = new SetOutcomeValue(responseIf);
			scoreOutcome.setIdentifier(QTI21Constants.SCORE_IDENTIFIER);
			responseIf.getResponseRules().add(scoreOutcome);
			
			Sum sum = new Sum(scoreOutcome);
			scoreOutcome.getExpressions().add(sum);
			
			Variable scoreVar = new Variable(sum);
			scoreVar.setIdentifier(QTI21Constants.SCORE_CLX_IDENTIFIER);
			sum.getExpressions().add(scoreVar);
			
			MapResponse mapResponse = new MapResponse(sum);
			mapResponse.setIdentifier(choiceInteraction.getResponseIdentifier());
			sum.getExpressions().add(mapResponse);
		}
		
		{//outcome feedback
			SetOutcomeValue correctOutcomeValue = new SetOutcomeValue(responseIf);
			correctOutcomeValue.setIdentifier(QTI21Constants.FEEDBACKBASIC_IDENTIFIER);
			responseIf.getResponseRules().add(correctOutcomeValue);
			
			BaseValue correctValue = new BaseValue(correctOutcomeValue);
			correctValue.setBaseTypeAttrValue(BaseType.IDENTIFIER);
			correctValue.setSingleValue(QTI21Constants.CORRECT_IDENTIFIER_VALUE);
			correctOutcomeValue.setExpression(correctValue);
		}
		
		ResponseElse responseElse = new ResponseElse(rule);
		rule.setResponseElse(responseElse);
		
		{//outcome score
			SetOutcomeValue scoreOutcome = new SetOutcomeValue(responseElse);
			scoreOutcome.setIdentifier(QTI21Constants.SCORE_IDENTIFIER);
			responseElse.getResponseRules().add(scoreOutcome);
			
			Sum sum = new Sum(scoreOutcome);
			scoreOutcome.getExpressions().add(sum);
			
			Variable scoreVar = new Variable(sum);
			scoreVar.setIdentifier(QTI21Constants.SCORE_CLX_IDENTIFIER);
			sum.getExpressions().add(scoreVar);
			
			MapResponse mapResponse = new MapResponse(sum);
			mapResponse.setIdentifier(choiceInteraction.getResponseIdentifier());
			sum.getExpressions().add(mapResponse);
		}
		
		{// outcome feedback
			SetOutcomeValue incorrectOutcomeValue = new SetOutcomeValue(responseElse);
			incorrectOutcomeValue.setIdentifier(QTI21Constants.FEEDBACKBASIC_IDENTIFIER);
			responseElse.getResponseRules().add(incorrectOutcomeValue);
			
			BaseValue incorrectValue = new BaseValue(incorrectOutcomeValue);
			incorrectValue.setBaseTypeAttrValue(BaseType.IDENTIFIER);
			incorrectValue.setSingleValue(QTI21Constants.INCORRECT_IDENTIFIER_VALUE);
			incorrectOutcomeValue.setExpression(incorrectValue);
		}
	}
}
