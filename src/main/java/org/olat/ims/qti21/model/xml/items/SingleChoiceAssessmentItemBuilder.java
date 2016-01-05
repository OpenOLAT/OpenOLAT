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
package org.olat.ims.qti21.model.xml.items;

import java.util.List;

import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.model.xml.AssessmentItemFactory;

import uk.ac.ed.ph.jqtiplus.node.content.basic.Block;
import uk.ac.ed.ph.jqtiplus.node.expression.general.BaseValue;
import uk.ac.ed.ph.jqtiplus.node.expression.general.Correct;
import uk.ac.ed.ph.jqtiplus.node.expression.general.MapResponse;
import uk.ac.ed.ph.jqtiplus.node.expression.general.Variable;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.IsNull;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Match;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Not;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Sum;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.CorrectResponse;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleChoice;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.ResponseDeclaration;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseCondition;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseElse;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseElseIf;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseIf;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseRule;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.SetOutcomeValue;
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
public class SingleChoiceAssessmentItemBuilder extends ChoiceAssessmentItemBuilder {
	
	private Identifier correctAnswer;
	
	public SingleChoiceAssessmentItemBuilder(AssessmentItem assessmentItem, QtiSerializer qtiSerializer) {
		super(assessmentItem, qtiSerializer);
	}
	
	@Override
	public void extract() {
		super.extract();
		
		List<ResponseDeclaration> responseDeclarations = assessmentItem.getResponseDeclarations();
		if(responseDeclarations.size() == 1) {
			CorrectResponse correctResponse = responseDeclarations.get(0).getCorrectResponse();
			if(correctResponse != null) {
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
	public boolean isCorrect(SimpleChoice choice) {
		return correctAnswer != null && correctAnswer.equals(choice.getIdentifier());
	}
	
	public void setCorrectAnswer(Identifier identifier) {
		correctAnswer = identifier;
	}

	@Override
	protected void buildResponseDeclaration() {
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
				.createSingleChoiceInteraction(assessmentItem, responseIdentifier);
		singleChoiceInteraction.setShuffle(isShuffle());
		blocks.add(singleChoiceInteraction);
		List<SimpleChoice> choiceList = getSimpleChoices();
		singleChoiceInteraction.getSimpleChoices().addAll(choiceList);
	}

	@Override
	protected void buildMainScoreRule(List<ResponseRule> responseRules) {
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
					<variable identifier="RESPONSE_1" /><correct identifier="RESPONSE_1" />
				</match>
				<setOutcomeValue identifier="SCORE">
					<sum>
						<variable identifier="SCORE" /><variable identifier="MAXSCORE" />
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
				<not>
					<isNull>
						<variable identifier="RESPONSE_1" />
					</isNull>
				</not>
				<setOutcomeValue identifier="SCORE">
					<sum>
						<variable identifier="SCORE" /><mapResponse identifier="RESPONSE_1" />
					</sum>
				</setOutcomeValue>
				<setOutcomeValue identifier="FEEDBACKBASIC">
					<baseValue baseType="identifier">
						incorrect
					</baseValue>
				</setOutcomeValue>
			</responseIf>
		</responseCondition>
		 */
		ResponseIf responseIf = new ResponseIf(rule);
		rule.setResponseIf(responseIf);
		
		Not not = new Not(responseIf);
		responseIf.getExpressions().add(not);
		
		IsNull isNull = new IsNull(not);
		not.getExpressions().add(isNull);
		
		Variable responseVar = new Variable(isNull);
		ComplexReferenceIdentifier choiceResponseIdentifier
			= ComplexReferenceIdentifier.parseString(choiceInteraction.getResponseIdentifier().toString());
		responseVar.setIdentifier(choiceResponseIdentifier);
		isNull.getExpressions().add(responseVar);
		
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
			SetOutcomeValue incorrectOutcomeValue = new SetOutcomeValue(responseIf);
			incorrectOutcomeValue.setIdentifier(QTI21Constants.FEEDBACKBASIC_IDENTIFIER);
			responseIf.getResponseRules().add(incorrectOutcomeValue);
			
			BaseValue incorrectValue = new BaseValue(incorrectOutcomeValue);
			incorrectValue.setBaseTypeAttrValue(BaseType.IDENTIFIER);
			incorrectValue.setSingleValue(QTI21Constants.INCORRECT_IDENTIFIER_VALUE);
			incorrectOutcomeValue.setExpression(incorrectValue);
		}
	}
}
