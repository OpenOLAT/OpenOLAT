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


import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.appendDefaultItemBody;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.appendDefaultOutcomeDeclarations;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.appendHotspotInteraction;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.createHotspotEntryResponseDeclarationSingle;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.createResponseProcessing;
import static org.olat.ims.qti21.model.xml.QtiNodesExtractor.extractIdentifiersFromCorrectResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.model.IdentifierGenerator;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.AssessmentItemFactory;
import org.olat.ims.qti21.model.xml.ResponseIdentifierForFeedback;
import org.olat.ims.qti21.model.xml.interactions.SimpleChoiceAssessmentItemBuilder.ScoreEvaluation;

import uk.ac.ed.ph.jqtiplus.node.content.ItemBody;
import uk.ac.ed.ph.jqtiplus.node.content.basic.Block;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.object.Object;
import uk.ac.ed.ph.jqtiplus.node.expression.general.BaseValue;
import uk.ac.ed.ph.jqtiplus.node.expression.general.Correct;
import uk.ac.ed.ph.jqtiplus.node.expression.general.MapResponse;
import uk.ac.ed.ph.jqtiplus.node.expression.general.Variable;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.IsNull;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Match;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Not;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Shape;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Sum;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.CorrectResponse;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.HotspotInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.graphic.HotspotChoice;
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
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.types.ComplexReferenceIdentifier;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.BaseType;
import uk.ac.ed.ph.jqtiplus.value.Cardinality;
import uk.ac.ed.ph.jqtiplus.value.IdentifierValue;
import uk.ac.ed.ph.jqtiplus.value.SingleValue;

/**
 * 
 * Initial date: 16.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class HotspotAssessmentItemBuilder extends AssessmentItemBuilder implements ResponseIdentifierForFeedback {
	
	private static final Logger log = Tracing.createLoggerFor(HotspotAssessmentItemBuilder.class);
	
	private String question;
	private Cardinality cardinality;
	private Identifier responseIdentifier;
	private List<Identifier> correctAnswers;
	protected ScoreEvaluation scoreEvaluation;
	private HotspotInteraction hotspotInteraction;
	protected Map<Identifier,Double> scoreMapping;
	
	public HotspotAssessmentItemBuilder(String title, QtiSerializer qtiSerializer) {
		super(createAssessmentItem(title), qtiSerializer);
	}
	
	public HotspotAssessmentItemBuilder(AssessmentItem assessmentItem, QtiSerializer qtiSerializer) {
		super(assessmentItem, qtiSerializer);
	}
	
	private static AssessmentItem createAssessmentItem(String title) {
		AssessmentItem assessmentItem = AssessmentItemFactory.createAssessmentItem(QTI21QuestionType.hotspot, title);
		
		//define the response
		Identifier responseDeclarationId = Identifier.assumedLegal("RESPONSE_1");
		Identifier correctResponseId = IdentifierGenerator.newNumberAsIdentifier("hc");
		
		ResponseDeclaration responseDeclaration = createHotspotEntryResponseDeclarationSingle(assessmentItem, responseDeclarationId, correctResponseId);
		assessmentItem.getNodeGroups().getResponseDeclarationGroup().getResponseDeclarations().add(responseDeclaration);
		
		//outcomes
		appendDefaultOutcomeDeclarations(assessmentItem, 1.0d);
				
		ItemBody itemBody = appendDefaultItemBody(assessmentItem);
		appendHotspotInteraction(itemBody, responseDeclarationId, correctResponseId);
		
		//response processing
		ResponseProcessing responseProcessing = createResponseProcessing(assessmentItem, responseDeclarationId);
		assessmentItem.getNodeGroups().getResponseProcessingGroup().setResponseProcessing(responseProcessing);
		
		return assessmentItem;
	}

	@Override
	protected void extract() {
		super.extract();
		extractHotspotInteraction();
		extractCorrectAnswers();
		extractScoreEvaluationMode();
	}
	
	private void extractHotspotInteraction() {
		try(StringOutput sb = new StringOutput()) {
			List<Block> blocks = assessmentItem.getItemBody().getBlocks();
			for(Block block:blocks) {
				if(block instanceof HotspotInteraction) {
					hotspotInteraction = (HotspotInteraction)block;
					responseIdentifier = hotspotInteraction.getResponseIdentifier();
					break;
				} else {
					serializeJqtiObject(block, sb);
				}
			}
			question = sb.toString();
		} catch(IOException e) {
			log.error("", e);
		}
	}
	
	private void extractCorrectAnswers() {
		correctAnswers = new ArrayList<>(5);

		if(hotspotInteraction != null) {
			ResponseDeclaration responseDeclaration = assessmentItem
					.getResponseDeclaration(hotspotInteraction.getResponseIdentifier());
			if(responseDeclaration != null) {
				if(responseDeclaration.getCorrectResponse() != null) {
					CorrectResponse correctResponse = responseDeclaration.getCorrectResponse();
					extractIdentifiersFromCorrectResponse(correctResponse, correctAnswers);
				}
				cardinality = responseDeclaration.getCardinality();
			}
		}
	}
	
	private void extractScoreEvaluationMode() {
		scoreMapping = getMapping(assessmentItem, hotspotInteraction);
		boolean hasMapping = scoreMapping != null && !scoreMapping.isEmpty();
		scoreEvaluation = hasMapping ? ScoreEvaluation.perAnswer : ScoreEvaluation.allCorrectAnswers;
	}
	
	public static Map<Identifier,Double> getMapping(AssessmentItem item, HotspotInteraction interaction) {
		Map<Identifier,Double> scoreMap = null;
		if(interaction != null) {
			ResponseDeclaration responseDeclaration = item
					.getResponseDeclaration(interaction.getResponseIdentifier());
			if(responseDeclaration != null) {
				Mapping mapping = responseDeclaration.getMapping();
				if(mapping != null && mapping.getMapEntries() != null && !mapping.getMapEntries().isEmpty()) {
					scoreMap = new HashMap<>();
					for(MapEntry entry:mapping.getMapEntries()) {
						SingleValue sValue = entry.getMapKey();
						if(sValue instanceof IdentifierValue) {
							Identifier identifier = ((IdentifierValue)sValue).identifierValue();
							scoreMap.put(identifier, entry.getMappedValue());
						}
					}
				}
			}
		}
		return scoreMap;	
	}
	
	@Override
	public Identifier getResponseIdentifier() {
		return responseIdentifier;
	}
	
	public boolean isSingleChoice() {
		return cardinality == Cardinality.SINGLE;
	}
	
	public void setCardinality(Cardinality cardinality) {
		this.cardinality = cardinality;
	}
	
	@Override
	public List<Answer> getAnswers() {
		List<HotspotChoice> hotspotChoices = getHotspotChoices();
		List<Answer> answers = new ArrayList<>(hotspotChoices.size());
		int count = 0;
		for(HotspotChoice choice:hotspotChoices) {
			answers.add(new Answer(choice.getIdentifier(), Integer.toString(++count)));
		}
		return answers;
	}

	public String getBackground() {
		Object graphichObject = hotspotInteraction.getObject();
		if(graphichObject != null) {
			return graphichObject.getData();
		}
		return null;
	}
	
	public void setBackground(String data, String mimeType, int height, int width) {
		Object graphichObject = hotspotInteraction.getObject();
		if(graphichObject == null) {
			graphichObject = new Object(hotspotInteraction);
			hotspotInteraction.setObject(graphichObject);
		}
		graphichObject.setData(data);
		graphichObject.setType(mimeType);
		if(height > 0) {
			graphichObject.setHeight(Integer.toString(height));
		} else {
			graphichObject.setHeight(null);
		}
		if(width > 0) {
			graphichObject.setWidth(Integer.toString(width));
		} else {
			graphichObject.setWidth(null);
		}
	}
	
	public boolean hasHotspotInteractionClass(String cssClass) {
		List<String> cssClassses = hotspotInteraction.getClassAttr();
		return cssClassses != null && cssClassses.contains(cssClass);
	}
	
	public void addHotspotInteractionClass(String cssClass) {
		if(!StringHelper.containsNonWhitespace(cssClass)) return;
		
		List<String> cssClassses = hotspotInteraction.getClassAttr();
		cssClassses = cssClassses == null ? new ArrayList<>() : new ArrayList<>(cssClassses);
		cssClassses.add(cssClass);
		hotspotInteraction.setClassAttr(cssClassses);
	}
	
	public void removeHotspotInteractionClass(String cssClass) {
		if(cssClass == null || hotspotInteraction.getClassAttr() == null) return;

		List<String> cssClassList = new ArrayList<>(hotspotInteraction.getClassAttr());
		for(Iterator<String> cssClassIt= cssClassList.iterator(); cssClassIt.hasNext(); ) {
			if(cssClass.equals(cssClassIt.next())) {
				cssClassIt.remove();
			}
		}
		hotspotInteraction.setClassAttr(cssClassList);
	}
	
	public boolean isCorrect(HotspotChoice choice) {
		if(correctAnswers == null) {
			correctAnswers = new ArrayList<>();
			return false;
		}
		return correctAnswers.contains(choice.getIdentifier());
	}
	
	public void clearCorrectAnswers() {
		if(correctAnswers != null) {
			correctAnswers.clear();
		}
	}
	
	public void setCorrect(HotspotChoice choice, boolean correct) {
		if(correctAnswers == null) {
			correctAnswers = new ArrayList<>();
		}
		if(correct) {
			if(!correctAnswers.contains(choice.getIdentifier())) {
				correctAnswers.add(choice.getIdentifier());
			}
		} else {
			correctAnswers.remove(choice.getIdentifier());
		}
	}
	
	public ScoreEvaluation getScoreEvaluationMode() {
		return scoreEvaluation;
	}

	public void setScoreEvaluationMode(ScoreEvaluation scoreEvaluation) {
		this.scoreEvaluation = scoreEvaluation;
	}
	
	public Double getMapping(Identifier identifier) {
		Double score = null;
		if(scoreMapping != null) {
			score = scoreMapping.get(identifier);
		}
		return score;
	}
	
	public void clearMapping() {
		if(scoreMapping != null) {
			scoreMapping.clear();
		}
	}
	
	public void setMapping(Identifier identifier, Double score) {
		if(scoreMapping == null) {
			scoreMapping = new HashMap<>();
		}
		scoreMapping.put(identifier, score);
	}

	@Override
	public QTI21QuestionType getQuestionType() {
		return QTI21QuestionType.hotspot;
	}
	
	/**
	 * @return A copy of the list of blocks which make the question.
	 * 		The list is a copy and modification will not be persisted.
	 */
	public List<Block> getQuestionBlocks() {
		List<Block> blocks = assessmentItem.getItemBody().getBlocks();
		List<Block> questionBlocks = new ArrayList<>(blocks.size());
		for(Block block:blocks) {
			if(block instanceof HotspotInteraction) {
				break;
			} else {
				questionBlocks.add(block);
			}
		}
		return questionBlocks;
	}

	@Override
	public String getQuestion() {
		return question;
	}

	@Override
	public void setQuestion(String question) {
		this.question = question;
	}
	
	public boolean isResponsive() {
		List<String> cssClasses = hotspotInteraction.getClassAttr();
		return cssClasses != null && cssClasses.size() > 0
				&& cssClasses.contains(QTI21Constants.CSS_INTERACTION_RESPONSIVE); 
	}
	
	public void setResponsive(boolean responsive) {
		List<String> cssClasses = hotspotInteraction.getClassAttr();
		if(cssClasses == null) {
			cssClasses = new ArrayList<>();
		}
		if(responsive) {
			if(!cssClasses.contains(QTI21Constants.CSS_INTERACTION_RESPONSIVE)) {
				cssClasses.add(QTI21Constants.CSS_INTERACTION_RESPONSIVE);
			}
		} else {
			cssClasses.remove(QTI21Constants.CSS_INTERACTION_RESPONSIVE);
		}
		hotspotInteraction.setClassAttr(cssClasses);
	}
	
	public HotspotChoice getHotspotChoice(String identifier) {
		List<HotspotChoice> choices = getHotspotChoices();
		for(HotspotChoice choice:choices) {
			if(choice.getIdentifier().toString().equals(identifier)) {
				return choice;
			}
		}
		return null;
	}
	
	public List<HotspotChoice> getHotspotChoices() {
		return hotspotInteraction.getHotspotChoices();
	}
	
	public HotspotChoice createHotspotChoice(Identifier identifier, Shape shape, String coords) {
		HotspotChoice choice = new HotspotChoice(hotspotInteraction);
		choice.setFixed(Boolean.FALSE);
		choice.setIdentifier(identifier);
		choice.setShape(shape);
		choice.setCoords(AssessmentItemFactory.coordsList(coords));
		hotspotInteraction.getHotspotChoices().add(choice);
		return choice;
	}
	
	public void deleteHotspotChoice(HotspotChoice choice) {
		hotspotInteraction.getHotspotChoices().remove(choice);
		correctAnswers.remove(choice.getIdentifier());
	}
	
	@Override
	protected void buildResponseAndOutcomeDeclarations() {
		ResponseDeclaration responseDeclaration = AssessmentItemFactory
				.createHotspotCorrectResponseDeclaration(assessmentItem, responseIdentifier, correctAnswers, cardinality);
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
		if(correctAnswers.size() == 1 && cardinality == Cardinality.SINGLE) {
			hotspotInteraction.setMaxChoices(1);
		} else {
			hotspotInteraction.setMaxChoices(0);
		}
		
		blocks.add(hotspotInteraction);
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

	private void buildMainScoreRuleAllCorrectAnswers(ResponseCondition rule) {
		/*
		<responseCondition>
			<responseIf>
				<isNull>
					<variable identifier="RESPONSE_1" />
				</isNull>
				<setOutcomeValue identifier="FEEDBACKBASIC">
					<baseValue baseType="identifier">empty</baseValue>
				</setOutcomeValue>
			</responseIf>
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
					<baseValue baseType="identifier">correct</baseValue>
				</setOutcomeValue>
			</responseElseIf>
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
				= ComplexReferenceIdentifier.parseString(hotspotInteraction.getResponseIdentifier().toString());
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

	private void buildMainScoreRulePerAnswer(ResponseCondition rule) {

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
						<variable identifier="SCORE" />
						<mapResponse identifier="RESPONSE_1" />
					</sum>
				</setOutcomeValue>
				<setOutcomeValue identifier="FEEDBACKBASIC">
					<baseValue baseType="identifier">incorrect</baseValue>
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
			mapResponse.setIdentifier(hotspotInteraction.getResponseIdentifier());
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
