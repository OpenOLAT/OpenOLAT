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
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.createMultipleChoiceCorrectResponseDeclaration;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.createResponseProcessing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.model.IdentifierGenerator;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.AssessmentItemFactory;

import uk.ac.ed.ph.jqtiplus.group.NodeGroupList;
import uk.ac.ed.ph.jqtiplus.node.content.ItemBody;
import uk.ac.ed.ph.jqtiplus.node.content.basic.Block;
import uk.ac.ed.ph.jqtiplus.node.expression.general.BaseValue;
import uk.ac.ed.ph.jqtiplus.node.expression.general.Correct;
import uk.ac.ed.ph.jqtiplus.node.expression.general.MapResponse;
import uk.ac.ed.ph.jqtiplus.node.expression.general.Variable;
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
import uk.ac.ed.ph.jqtiplus.value.MultipleValue;
import uk.ac.ed.ph.jqtiplus.value.SingleValue;
import uk.ac.ed.ph.jqtiplus.value.Value;

/**
 * 
 * Initial date: 08.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MultipleChoiceAssessmentItemBuilder extends SimpleChoiceAssessmentItemBuilder {
	
	private List<Identifier> correctAnswers;
	
	public MultipleChoiceAssessmentItemBuilder(String title, String defaultAnswer, QtiSerializer qtiSerializer) {
		super(createAssessmentItem(title, defaultAnswer), qtiSerializer);
	}
	
	public MultipleChoiceAssessmentItemBuilder(AssessmentItem assessmentItem, QtiSerializer qtiSerializer) {
		super(assessmentItem, qtiSerializer);
	}
	
	private static AssessmentItem createAssessmentItem(String title, String defaultAnswer) {
		AssessmentItem assessmentItem = AssessmentItemFactory.createAssessmentItem(QTI21QuestionType.mc, title);
		
		NodeGroupList nodeGroups = assessmentItem.getNodeGroups();

		Identifier responseDeclarationId = Identifier.assumedLegal("RESPONSE_1");
		Identifier correctResponseId = IdentifierGenerator.newAsIdentifier("mc");
		//define correct answer
		ResponseDeclaration responseDeclaration = createMultipleChoiceCorrectResponseDeclaration(assessmentItem, responseDeclarationId,
				Collections.singletonList(correctResponseId));
		nodeGroups.getResponseDeclarationGroup().getResponseDeclarations().add(responseDeclaration);
		
		//outcomes
		appendDefaultOutcomeDeclarations(assessmentItem, 1.0d);
		
		//the single choice interaction
		ItemBody itemBody = appendDefaultItemBody(assessmentItem);
		ChoiceInteraction choiceInteraction = appendChoiceInteraction(itemBody, responseDeclarationId, 0, true);
		
		appendSimpleChoice(choiceInteraction, defaultAnswer, correctResponseId);

		//response processing
		ResponseProcessing responseProcessing = createResponseProcessing(assessmentItem, responseDeclarationId);
		assessmentItem.getNodeGroups().getResponseProcessingGroup().setResponseProcessing(responseProcessing);
		
		return assessmentItem;
	}
	
	@Override
	public void extract() {
		super.extract();
		
		correctAnswers = new ArrayList<>(5);
		
		if(choiceInteraction != null) {
			ResponseDeclaration responseDeclaration = assessmentItem
					.getResponseDeclaration(choiceInteraction.getResponseIdentifier());
			if(responseDeclaration != null && responseDeclaration.getCorrectResponse() != null) {
				CorrectResponse correctResponse = responseDeclaration.getCorrectResponse();
				Value value = FieldValue.computeValue(Cardinality.MULTIPLE, correctResponse.getFieldValues());
				if(value instanceof MultipleValue) {
					MultipleValue multiValue = (MultipleValue)value;
					for(SingleValue sValue:multiValue.getAll()) {
						if(sValue instanceof IdentifierValue) {
							IdentifierValue identifierValue = (IdentifierValue)sValue;
							Identifier correctAnswer = identifierValue.identifierValue();
							correctAnswers.add(correctAnswer);
						}
					}
				}
			}
		}
	}
	
	@Override
	public QTI21QuestionType getQuestionType() {
		return QTI21QuestionType.mc;
	}

	@Override
	public boolean isCorrect(Choice choice) {
		return correctAnswers.contains(choice.getIdentifier());
	}
	
	public void setCorrectAnswers(List<Identifier> identifiers) {
		correctAnswers.clear();
		correctAnswers.addAll(identifiers);
	}
	
	public void addCorrectAnswer(Identifier identifier) {
		correctAnswers.add(identifier);
	}

	@Override
	public int getMaxPossibleCorrectAnswers() {
		return choices.size();
	}

	@Override
	public boolean scoreOfCorrectAnswerWarning() {
		boolean warning;
		if(getScoreEvaluationMode() == ScoreEvaluation.perAnswer) {
			boolean wrongAnswerHasPoint = false;
			boolean correctAnswerHasNotPoint = true;
			for(SimpleChoice choice:getChoices()) {
				Double score = getMapping(choice.getIdentifier());
				if(isCorrect(choice)) {
					correctAnswerHasNotPoint &= (score == null || score.doubleValue() < 0.000001);
				} else {
					wrongAnswerHasPoint |= (score != null && score.doubleValue() > 0.6);
				}	
			}
			warning = wrongAnswerHasPoint && correctAnswerHasNotPoint;
		} else {
			warning = false;
		}
		return warning;
	}

	@Override
	public void clearSimpleChoices() {
		if(correctAnswers != null) {
			correctAnswers.clear();
		}
		super.clearSimpleChoices();
	}

	@Override
	protected void buildResponseAndOutcomeDeclarations() {
		ResponseDeclaration responseDeclaration = AssessmentItemFactory
				.createMultipleChoiceCorrectResponseDeclaration(assessmentItem, responseIdentifier, correctAnswers);
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
				.createMultipleChoiceInteraction(assessmentItem, responseIdentifier, orientation, cssClass);
		singleChoiceInteraction.setShuffle(isShuffle());
		blocks.add(singleChoiceInteraction);
		List<SimpleChoice> choiceList = getChoices();
		singleChoiceInteraction.getSimpleChoices().addAll(choiceList);

		int finalMaxChoices = 0;
		if(maxChoices >= 0 && maxChoices <= choiceList.size()) {
			finalMaxChoices = maxChoices;
		}
		singleChoiceInteraction.setMaxChoices(finalMaxChoices);
		
		int finalMinChoices = 0;
		if(minChoices >= 0 && minChoices <= choiceList.size()) {
			finalMinChoices = minChoices;
		}
		singleChoiceInteraction.setMinChoices(finalMinChoices);
	}

	@Override
	protected void buildMainScoreRule(List<OutcomeDeclaration> outcomeDeclarations,  List<ResponseRule> responseRules) {
		ResponseCondition rule = new ResponseCondition(assessmentItem.getResponseProcessing());
		responseRules.add(0, rule);
		if(scoreEvaluation == ScoreEvaluation.perAnswer) {
			buildMainScoreRulePerAnswer(rule);
		} else {
			buildMainScoreRuleAllCorrectAnswers(rule);
		}
	}
	
	private void buildMainScoreRuleAllCorrectAnswers(ResponseCondition rule) {
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
						<variable identifier="MAXSCORE" />
					</sum>
				</setOutcomeValue>
				<setOutcomeValue identifier="FEEDBACKBASIC">
					<baseValue baseType="identifier">correct</baseValue>
				</setOutcomeValue>
			</responseIf>
			<responseElse>
				<setOutcomeValue identifier="FEEDBACKBASIC">
					<baseValue baseType="identifier">incorrect</baseValue>
				</setOutcomeValue>
			</responseElse>
		</responseCondition>
		 */
		//simple as build with / without feedback
		ensureFeedbackBasicOutcomeDeclaration();
	
		
		ResponseIf responseIf = new ResponseIf(rule);
		rule.setResponseIf(responseIf);
		
		{// match the correct answers
			Match match = new Match(responseIf);
			responseIf.getExpressions().add(match);
			
			Variable scoreVar = new Variable(match);
			ComplexReferenceIdentifier choiceResponseIdentifier
				= ComplexReferenceIdentifier.parseString(choiceInteraction.getResponseIdentifier().toString());
			scoreVar.setIdentifier(choiceResponseIdentifier);
			match.getExpressions().add(scoreVar);
			
			Correct correct = new Correct(match);
			correct.setIdentifier(choiceResponseIdentifier);
			match.getExpressions().add(correct);
		}
	
		{//outcome score
			SetOutcomeValue scoreOutcomeValue = new SetOutcomeValue(responseIf);
			scoreOutcomeValue.setIdentifier(QTI21Constants.SCORE_IDENTIFIER);
			responseIf.getResponseRules().add(scoreOutcomeValue);
			
			Sum sum = new Sum(scoreOutcomeValue);
			scoreOutcomeValue.getExpressions().add(sum);
			
			Variable scoreVar = new Variable(sum);
			scoreVar.setIdentifier(QTI21Constants.SCORE_CLX_IDENTIFIER);
			sum.getExpressions().add(scoreVar);
			
			Variable maxScoreVar = new Variable(sum);
			maxScoreVar.setIdentifier(QTI21Constants.MAXSCORE_CLX_IDENTIFIER);
			sum.getExpressions().add(maxScoreVar);
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
	
	private void buildMainScoreRulePerAnswer(ResponseCondition rule) {
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
					<baseValue baseType="identifier">correct</baseValue>
				</setOutcomeValue>
			</responseIf>
			<responseElse>
				<setOutcomeValue identifier="FEEDBACKBASIC">
					<baseValue baseType="identifier">incorrect</baseValue>
				</setOutcomeValue>
			</responseElse>
		</responseCondition>
		*/
		ResponseIf responseIf = new ResponseIf(rule);
		rule.setResponseIf(responseIf);
		
		{// match the correct answers
			Match match = new Match(responseIf);
			responseIf.getExpressions().add(match);
			
			Variable scoreVar = new Variable(match);
			ComplexReferenceIdentifier choiceResponseIdentifier
				= ComplexReferenceIdentifier.parseString(choiceInteraction.getResponseIdentifier().toString());
			scoreVar.setIdentifier(choiceResponseIdentifier);
			match.getExpressions().add(scoreVar);
			
			Correct correct = new Correct(match);
			correct.setIdentifier(choiceResponseIdentifier);
			match.getExpressions().add(correct);
		}
	
		{//outcome score
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