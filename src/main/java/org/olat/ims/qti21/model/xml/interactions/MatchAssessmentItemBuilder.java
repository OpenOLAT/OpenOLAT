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

import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.appendAssociationMatchResponseDeclaration;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.appendDefaultItemBody;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.appendDefaultOutcomeDeclarations;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.appendMatchInteraction;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.createMatchResponseDeclaration;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.createResponseProcessing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.stream.StreamResult;

import org.olat.core.gui.render.StringOutput;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.AssessmentItemFactory;
import org.olat.ims.qti21.model.xml.interactions.SimpleChoiceAssessmentItemBuilder.ScoreEvaluation;

import uk.ac.ed.ph.jqtiplus.group.NodeGroupList;
import uk.ac.ed.ph.jqtiplus.node.content.ItemBody;
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
import uk.ac.ed.ph.jqtiplus.node.item.interaction.MatchInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleAssociableChoice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleMatchSet;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.MapEntry;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.Mapping;
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
import uk.ac.ed.ph.jqtiplus.value.DirectedPairValue;
import uk.ac.ed.ph.jqtiplus.value.SingleValue;

/**
 * 
 * Initial date: 21 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MatchAssessmentItemBuilder extends AssessmentItemBuilder {

	private String question;
	private boolean shuffle;
	private boolean multipleChoice;
	private Identifier responseIdentifier;
	protected ScoreEvaluation scoreEvaluation;
	private MatchInteraction matchInteraction;
	private Map<Identifier, List<Identifier>> associations;
	private Map<DirectedPairValue, Double> scoreMapping;
	
	public MatchAssessmentItemBuilder(String title, QtiSerializer qtiSerializer) {
		super(createAssessmentItem(title), qtiSerializer);
	}
	
	public MatchAssessmentItemBuilder(AssessmentItem assessmentItem, QtiSerializer qtiSerializer) {
		super(assessmentItem, qtiSerializer);
	}
	
	private static AssessmentItem createAssessmentItem(String title) {
		AssessmentItem assessmentItem = AssessmentItemFactory.createAssessmentItem(QTI21QuestionType.match, title);
		
		NodeGroupList nodeGroups = assessmentItem.getNodeGroups();

		double maxScore = 1.0d;
		Identifier responseDeclarationId = Identifier.assumedLegal("RESPONSE_1");
		//define correct answer
		ResponseDeclaration responseDeclaration = createMatchResponseDeclaration(assessmentItem, responseDeclarationId, new HashMap<>(), maxScore);
		nodeGroups.getResponseDeclarationGroup().getResponseDeclarations().add(responseDeclaration);
		
		appendDefaultOutcomeDeclarations(assessmentItem, maxScore);

		//the single choice interaction
		ItemBody itemBody = appendDefaultItemBody(assessmentItem);
		appendMatchInteraction(itemBody, responseDeclarationId);
		Map<Identifier, List<Identifier>> associations = new HashMap<>();
		appendAssociationMatchResponseDeclaration(responseDeclaration, associations, 1.0);
		
		//response processing
		ResponseProcessing responseProcessing = createResponseProcessing(assessmentItem, responseDeclarationId);
		assessmentItem.getNodeGroups().getResponseProcessingGroup().setResponseProcessing(responseProcessing);
		
		return assessmentItem;
	}
	
	@Override
	public void extract() {
		super.extract();
		extractMatchInteraction();
		extractCorrectResponse();
		extractScoreEvaluationMode();
		extractSingleChoice();
		
		if(getMinScoreBuilder() == null) {
			setMinScore(0.0d);
		}
		if(getMaxScoreBuilder() == null) {
			setMaxScore(1.0d);
		}
	}
	
	/**
	 * Single choice is a special edge case where
	 * all source choices have matchMax=1 and all
	 * target choices have matchMax=0 
	 */
	private void extractSingleChoice() {
		List<SimpleAssociableChoice> sourceChoices = matchInteraction.getSimpleMatchSets().get(0).getSimpleAssociableChoices();
		List<SimpleAssociableChoice> targetChoices = matchInteraction.getSimpleMatchSets().get(1).getSimpleAssociableChoices();
		
		boolean singleChoice = true;
		for(SimpleAssociableChoice sourceChoice:sourceChoices) {
			if(sourceChoice.getMatchMax() != 1) {
				singleChoice &= false;
			}
		}
		
		for(SimpleAssociableChoice targetChoice:targetChoices) {
			if(targetChoice.getMatchMax() != 0) {
				singleChoice &= false;
			}
		}
		
		multipleChoice = !singleChoice;
	}
	
	private void extractCorrectResponse() {
		associations = new HashMap<>();

		if(matchInteraction != null) {
			ResponseDeclaration responseDeclaration = assessmentItem
					.getResponseDeclaration(matchInteraction.getResponseIdentifier());
			
			if(responseDeclaration != null && responseDeclaration.getCorrectResponse() != null) {
				CorrectResponse correctResponse = responseDeclaration.getCorrectResponse();
				List<FieldValue> values = correctResponse.getFieldValues();
				for(FieldValue value:values) {
					SingleValue sValue = value.getSingleValue();
					if(sValue instanceof DirectedPairValue) {
						DirectedPairValue dpValue = (DirectedPairValue)sValue;
						Identifier sourceId = dpValue.sourceValue();
						Identifier targetId = dpValue.destValue();
						List<Identifier> targetIds = associations.get(sourceId);
						if(targetIds == null) {
							targetIds = new ArrayList<>();
							associations.put(sourceId, targetIds);
						}
						targetIds.add(targetId);
					}
				}
			}
		}
	}
	
	private void extractMatchInteraction() {
		StringOutput sb = new StringOutput();
		List<Block> blocks = assessmentItem.getItemBody().getBlocks();
		for(Block block:blocks) {
			if(block instanceof MatchInteraction) {
				matchInteraction = (MatchInteraction)block;
				responseIdentifier = matchInteraction.getResponseIdentifier();
				shuffle = matchInteraction.getShuffle();
				break;
			} else {
				qtiSerializer.serializeJqtiObject(block, new StreamResult(sb));
			}
		}
		question = sb.toString();
	}
	
	private void extractScoreEvaluationMode() {
		boolean hasMapping = false;
		if(matchInteraction != null) {
			ResponseDeclaration responseDeclaration = assessmentItem
					.getResponseDeclaration(matchInteraction.getResponseIdentifier());
			if(responseDeclaration != null) {
				Mapping mapping = responseDeclaration.getMapping();
				
				hasMapping = (mapping != null && mapping.getMapEntries() != null && mapping.getMapEntries().size() > 0);
				if(hasMapping) {
					scoreMapping = new HashMap<>();
					for(MapEntry entry:mapping.getMapEntries()) {
						SingleValue sValue = entry.getMapKey();
						if(sValue instanceof DirectedPairValue) {
							Identifier sourceIdentifier = ((DirectedPairValue)sValue).sourceValue();
							Identifier destIdentifier = ((DirectedPairValue)sValue).destValue();
							scoreMapping.put(new DirectedPairValue(sourceIdentifier, destIdentifier), entry.getMappedValue());
						}
					}
				}
			}
		}
		scoreEvaluation = hasMapping ? ScoreEvaluation.perAnswer : ScoreEvaluation.allCorrectAnswers;
	}

	@Override
	public QTI21QuestionType getQuestionType() {
		return QTI21QuestionType.match;
	}
	
	public boolean isShuffle() {
		return shuffle;
	}
	
	public void setShuffle(boolean shuffle) {
		this.shuffle = shuffle;
	}

	public boolean isMultipleChoice() {
		return multipleChoice;
	}

	public void setMultipleChoice(boolean multipleChoice) {
		this.multipleChoice = multipleChoice;
	}

	@Override
	public String getQuestion() {
		return question;
	}

	@Override
	public void setQuestion(String question) {
		this.question = question;
	}
	
	public ScoreEvaluation getScoreEvaluationMode() {
		return scoreEvaluation;
	}

	public void setScoreEvaluationMode(ScoreEvaluation scoreEvaluation) {
		this.scoreEvaluation = scoreEvaluation;
	}
	
	public String getResponseIdentifier() {
		return responseIdentifier.toString();
	}

	public MatchInteraction getMatchInteraction() {
		return matchInteraction;
	}
	
	public SimpleMatchSet getSourceMatchSet() {
		return matchInteraction.getSimpleMatchSets().get(0);
	}
	
	public List<SimpleAssociableChoice> getSourceChoices() {
		return matchInteraction.getSimpleMatchSets().get(0).getSimpleAssociableChoices();
	}
	
	public SimpleMatchSet getTargetMatchSet() {
		return matchInteraction.getSimpleMatchSets().get(1);
	}
	
	public List<SimpleAssociableChoice> getTargetChoices() {
		return matchInteraction.getSimpleMatchSets().get(1).getSimpleAssociableChoices();
	}
	
	public boolean isCorrect(Identifier sourceChoiceId, Identifier targetChoiceId) {
		List<Identifier> targetIds = associations.get(sourceChoiceId);
		return targetIds != null && targetIds.contains(targetChoiceId);
	}
	
	public Double getScore(Identifier sourceChoiceId, Identifier targetChoiceId) {
		if(scoreMapping == null) return null;
		return scoreMapping.get(new DirectedPairValue(sourceChoiceId, targetChoiceId));
	}
	
	public void addScore(DirectedPairValue directedPair, Double score) {
		if(scoreMapping == null) {
			scoreMapping = new HashMap<>();
		}
		scoreMapping.put(new DirectedPairValue(directedPair.sourceValue(), directedPair.destValue()), score);
	}
	
	public void clearMapping() {
		if(scoreMapping != null) {
			scoreMapping.clear();
		}
	}
	
	public void clearAssociations() {
		associations.clear();
	}
	
	public void addAssociation(Identifier sourceChoiceId, Identifier targetChoiceId) {
		List<Identifier> targetIds = associations.get(sourceChoiceId);
		if(targetIds == null) {
			targetIds = new ArrayList<>();
			associations.put(sourceChoiceId, targetIds);
		}
		targetIds.add(targetChoiceId);
	}
	
	public void removeSimpleAssociableChoice(SimpleAssociableChoice associableChoice) {
		if(getSourceMatchSet().getSimpleAssociableChoices().contains(associableChoice)) {
			getSourceMatchSet().getSimpleAssociableChoices().remove(associableChoice);
			associations.remove(associableChoice.getIdentifier());
		} else if(getTargetMatchSet().getSimpleAssociableChoices().contains(associableChoice)) {
			getTargetMatchSet().getSimpleAssociableChoices().remove(associableChoice);
			for(Map.Entry<Identifier, List<Identifier>> association:associations.entrySet()) {
				List<Identifier> targetChoiceIdentifiers = association.getValue();
				if(targetChoiceIdentifiers != null && targetChoiceIdentifiers.size() > 0) {
					targetChoiceIdentifiers.remove(associableChoice.getIdentifier());
				}
			}
		}
	}

	@Override
	protected void buildItemBody() {
		//remove current blocks
		List<Block> blocks = assessmentItem.getItemBody().getBlocks();
		blocks.clear();

		//add question
		getHtmlHelper().appendHtml(assessmentItem.getItemBody(), question);
		
		int sourceMatchMax = multipleChoice ? 0 : 1;
		getSourceMatchSet().getSimpleAssociableChoices().forEach((choice) -> {
			choice.setMatchMin(0);
			choice.setMatchMax(sourceMatchMax);
		});
		
		getTargetMatchSet().getSimpleAssociableChoices().forEach((choice) -> {
			choice.setMatchMin(0);
			choice.setMatchMax(0);
		});
		
		matchInteraction.setMaxAssociations(0);
		matchInteraction.setShuffle(isShuffle());
		blocks.add(matchInteraction);
	}

	@Override
	protected void buildResponseAndOutcomeDeclarations() {
		//need min. and max. score
		double maxScore = getMaxScoreBuilder().getScore();
		
		//refresh correct response
		ResponseDeclaration responseDeclaration;
		if(assessmentItem.getResponseDeclaration(responseIdentifier) != null) {
			responseDeclaration = assessmentItem.getResponseDeclaration(responseIdentifier);
			appendAssociationMatchResponseDeclaration(responseDeclaration, associations, maxScore);
		} else {
			responseDeclaration =
					createMatchResponseDeclaration(assessmentItem, responseIdentifier, associations, maxScore);
			assessmentItem.getResponseDeclarations().add(responseDeclaration);
		}
		
		if(scoreEvaluation == ScoreEvaluation.perAnswer) {
			Map<DirectedPairValue,Double> pairMapping = new HashMap<>();
			if(scoreMapping != null && scoreMapping.size() > 0) {
				for(Map.Entry<DirectedPairValue, Double> entry:scoreMapping.entrySet()) {
					DirectedPairValue sdKey = entry.getKey();
					pairMapping.put(new DirectedPairValue(sdKey.sourceValue(), sdKey.destValue()), entry.getValue());
				}
			}
			AssessmentItemFactory.appendPairMapping(responseDeclaration, pairMapping);
		} else {
			//make sure there isn't any mapping
			responseDeclaration.setMapping(null);	
		}
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
	
	@Override
	protected void buildModalFeedbacksAndHints(List<OutcomeDeclaration> outcomeDeclarations, List<ResponseRule> responseRules) {
		if(correctFeedback != null || incorrectFeedback != null) {
			if(scoreEvaluation == ScoreEvaluation.perAnswer) {
				ResponseCondition responseCondition = AssessmentItemFactory.createModalFeedbackResponseConditionByScore(assessmentItem.getResponseProcessing());
				responseRules.add(responseCondition);
			}
		}

		super.buildModalFeedbacksAndHints(outcomeDeclarations, responseRules);
	}
	
	private void buildMainScoreRulePerAnswer(ResponseCondition rule) {
		/*
		<responseCondition>
			<responseIf>
				<not>
					<isNull>
						<variable identifier="RESPONSE_4953445" />
					</isNull>
				</not>
				<setOutcomeValue identifier="SCORE">
					<sum>
						<variable identifier="SCORE" /><mapResponse identifier="RESPONSE_4953445" />
					</sum>
				</setOutcomeValue>
				<setOutcomeValue identifier="FEEDBACKBASIC">
					<baseValue baseType="identifier">
						incorrect
					</baseValue>
				</setOutcomeValue>
			</responseIf>
		</responseCondition>
		<responseCondition>
			<responseIf>
				<and>
					<not>
						<match>
							<variable identifier="FEEDBACKBASIC" />
							<baseValue baseType="identifier">
								empty
							</baseValue>
						</match>
					</not>
					<equal toleranceMode="exact">
						<variable identifier="SCORE" /><variable identifier="MAXSCORE" />
					</equal>
				</and>
				<setOutcomeValue identifier="FEEDBACKBASIC">
					<baseValue baseType="identifier">
						correct
					</baseValue>
				</setOutcomeValue>
			</responseIf>
		</responseCondition>
		*/
		
		//if no response
		ResponseIf responseIf = new ResponseIf(rule);
		rule.setResponseIf(responseIf);
		
		Not not = new Not(responseIf);
		responseIf.getExpressions().add(not);

		IsNull isNull = new IsNull(not);
		not.getExpressions().add(isNull);
		
		Variable variable = new Variable(isNull);
		variable.setIdentifier(ComplexReferenceIdentifier.parseString(responseIdentifier.toString()));
		isNull.getExpressions().add(variable);
		
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
			mapResponse.setIdentifier(responseIdentifier);
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

	private void buildMainScoreRuleAllCorrectAnswers(ResponseCondition rule) {
		/*
		<responseCondition>
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
			<responseElseIf>
				<match>
					<variable identifier="RESPONSE_1" />
					<correct identifier="RESPONSE_1" />
				</match>
				<setOutcomeValue identifier="SCORE">
					<sum>
						<variable identifier="SCORE" /><
						variable identifier="MAXSCORE" />
					</sum>
				</setOutcomeValue>
				<setOutcomeValue identifier="FEEDBACKBASIC">
					<baseValue baseType="identifier">
						correct
					</baseValue>
				</setOutcomeValue>
			</responseElseIf>
			<responseElse>
				<setOutcomeValue identifier="FEEDBACKBASIC">
					<baseValue baseType="identifier">
						incorrect
					</baseValue>
				</setOutcomeValue>
			</responseElse>
		</responseCondition>
		*/
		
		//simple as build with / without feedback
		ensureFeedbackBasicOutcomeDeclaration();
		
		ResponseIf responseIf = new ResponseIf(rule);
		rule.setResponseIf(responseIf);
		
		{//if no response
			IsNull isNull = new IsNull(responseIf);
			responseIf.getExpressions().add(isNull);
			
			Variable variable = new Variable(isNull);
			variable.setIdentifier(ComplexReferenceIdentifier.parseString(responseIdentifier.toString()));
			isNull.getExpressions().add(variable);
			
			SetOutcomeValue incorrectOutcomeValue = new SetOutcomeValue(responseIf);
			incorrectOutcomeValue.setIdentifier(QTI21Constants.FEEDBACKBASIC_IDENTIFIER);
			responseIf.getResponseRules().add(incorrectOutcomeValue);
			
			BaseValue incorrectValue = new BaseValue(incorrectOutcomeValue);
			incorrectValue.setBaseTypeAttrValue(BaseType.IDENTIFIER);
			incorrectValue.setSingleValue(QTI21Constants.EMPTY_IDENTIFIER_VALUE);
			incorrectOutcomeValue.setExpression(incorrectValue);
		}
		
		ResponseElseIf responseElseIf = new ResponseElseIf(rule);
		rule.getResponseElseIfs().add(responseElseIf);
		
		{// match the correct answers
			Match match = new Match(responseElseIf);
			responseElseIf.getExpressions().add(match);
			
			Variable scoreVar = new Variable(match);
			ComplexReferenceIdentifier choiceResponseIdentifier
				= ComplexReferenceIdentifier.parseString(responseIdentifier.toString());
			scoreVar.setIdentifier(choiceResponseIdentifier);
			match.getExpressions().add(scoreVar);
			
			Correct correct = new Correct(match);
			correct.setIdentifier(choiceResponseIdentifier);
			match.getExpressions().add(correct);
		}
	
		{//outcome score
			SetOutcomeValue scoreOutcomeValue = new SetOutcomeValue(responseElseIf);
			scoreOutcomeValue.setIdentifier(QTI21Constants.SCORE_IDENTIFIER);
			responseElseIf.getResponseRules().add(scoreOutcomeValue);
			
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
			SetOutcomeValue correctOutcomeValue = new SetOutcomeValue(responseElseIf);
			correctOutcomeValue.setIdentifier(QTI21Constants.FEEDBACKBASIC_IDENTIFIER);
			responseElseIf.getResponseRules().add(correctOutcomeValue);
			
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
}
