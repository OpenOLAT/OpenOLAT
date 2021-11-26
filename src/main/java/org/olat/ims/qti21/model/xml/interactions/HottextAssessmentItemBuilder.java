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
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.appendHottext;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.appendHottextInteraction;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.createHottextCorrectResponseDeclaration;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.createResponseProcessing;
import static org.olat.ims.qti21.model.xml.QtiNodesExtractor.extractIdentifiersFromCorrectResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.logging.Tracing;
import org.olat.core.util.filter.FilterFactory;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.model.IdentifierGenerator;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.AssessmentHtmlBuilder;
import org.olat.ims.qti21.model.xml.AssessmentItemFactory;
import org.olat.ims.qti21.model.xml.ResponseIdentifierForFeedback;
import org.olat.ims.qti21.model.xml.interactions.SimpleChoiceAssessmentItemBuilder.ScoreEvaluation;

import uk.ac.ed.ph.jqtiplus.node.content.ItemBody;
import uk.ac.ed.ph.jqtiplus.node.content.basic.Block;
import uk.ac.ed.ph.jqtiplus.node.content.basic.BlockStatic;
import uk.ac.ed.ph.jqtiplus.node.content.basic.TextRun;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.P;
import uk.ac.ed.ph.jqtiplus.node.expression.general.BaseValue;
import uk.ac.ed.ph.jqtiplus.node.expression.general.Correct;
import uk.ac.ed.ph.jqtiplus.node.expression.general.MapResponse;
import uk.ac.ed.ph.jqtiplus.node.expression.general.Variable;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.IsNull;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Match;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Sum;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.CorrectResponse;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.HottextInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.Choice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.content.Hottext;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.MapEntry;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.Mapping;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.ResponseDeclaration;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseCondition;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseElse;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseIf;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseProcessing;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseRule;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.SetOutcomeValue;
import uk.ac.ed.ph.jqtiplus.node.outcome.declaration.OutcomeDeclaration;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.types.ComplexReferenceIdentifier;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.utils.QueryUtils;
import uk.ac.ed.ph.jqtiplus.value.BaseType;
import uk.ac.ed.ph.jqtiplus.value.IdentifierValue;
import uk.ac.ed.ph.jqtiplus.value.SingleValue;

/**
 * 
 * Initial date: 16 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class HottextAssessmentItemBuilder extends ChoiceAssessmentItemBuilder implements ResponseIdentifierForFeedback {
	
	private static final Logger log = Tracing.createLoggerFor(HottextAssessmentItemBuilder.class);
	
	private String question;
	private Identifier responseIdentifier;
	private List<Identifier> correctAnswers;
	private HottextInteraction hottextInteraction;

	public HottextAssessmentItemBuilder(String title, String text, String hottext, QtiSerializer qtiSerializer) {
		super(createAssessmentItem(title, text, hottext), qtiSerializer);
	}
	
	public HottextAssessmentItemBuilder(AssessmentItem assessmentItem, QtiSerializer qtiSerializer) {
		super(assessmentItem, qtiSerializer);
	}
	
	private static AssessmentItem createAssessmentItem(String title, String text, String hottext) {
		AssessmentItem assessmentItem = AssessmentItemFactory.createAssessmentItem(QTI21QuestionType.hottext, title);
		
		//define correct answer
		Identifier responseDeclarationId = Identifier.assumedLegal("RESPONSE_1");
		Identifier correctResponseId = IdentifierGenerator.newAsIdentifier("ht");
		List<Identifier> correctResponseIds = new ArrayList<>();
		correctResponseIds.add(correctResponseId);
		ResponseDeclaration responseDeclaration = createHottextCorrectResponseDeclaration(assessmentItem, responseDeclarationId, correctResponseIds);
		assessmentItem.getNodeGroups().getResponseDeclarationGroup().getResponseDeclarations().add(responseDeclaration);
		
		//outcomes
		appendDefaultOutcomeDeclarations(assessmentItem, 1.0d);
		
		//the single choice interaction
		ItemBody itemBody = appendDefaultItemBody(assessmentItem);
		HottextInteraction hottextInteraction = appendHottextInteraction(itemBody, responseDeclarationId, 0);
		
		P p = new P(itemBody);
		p.getInlines().add(new TextRun(p, text));
		appendHottext(p, correctResponseId, hottext);
		hottextInteraction.getBlockStatics().add(p);

		//response processing
		ResponseProcessing responseProcessing = createResponseProcessing(assessmentItem, responseDeclarationId);
		assessmentItem.getNodeGroups().getResponseProcessingGroup().setResponseProcessing(responseProcessing);
		return assessmentItem;
	}
	
	@Override
	public void extract() {
		super.extract();
		extractHottextInteraction();
		extractScoreEvaluationMode();
		extractCorrectAnswers();
	}
	
	private void extractHottextInteraction() {
		try(StringOutput sb = new StringOutput()) {
			List<Block> blocks = assessmentItem.getItemBody().getBlocks();
			for(Block block:blocks) {
				if(block instanceof HottextInteraction) {
					hottextInteraction = (HottextInteraction)block;
					for(BlockStatic innerBlock: hottextInteraction.getBlockStatics()) {
						serializeJqtiObject(innerBlock, sb);
					}
					responseIdentifier = hottextInteraction.getResponseIdentifier();
					break;
				}
			}
			question = sb.toString();
		} catch(IOException e) {
			log.error("", e);
		}
	}
	
	private void extractScoreEvaluationMode() {
		scoreMapping = getMapping(assessmentItem, hottextInteraction);
		boolean hasMapping = scoreMapping != null && !scoreMapping.isEmpty();
		scoreEvaluation = hasMapping ? ScoreEvaluation.perAnswer : ScoreEvaluation.allCorrectAnswers;
	}
	
	public static Map<Identifier,Double> getMapping(AssessmentItem item, HottextInteraction interaction) {
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
	
	private void extractCorrectAnswers() {
		correctAnswers = new ArrayList<>(5);
		if(hottextInteraction != null) {
			ResponseDeclaration responseDeclaration = assessmentItem
					.getResponseDeclaration(hottextInteraction.getResponseIdentifier());
			if(responseDeclaration != null && responseDeclaration.getCorrectResponse() != null) {
				CorrectResponse correctResponse = responseDeclaration.getCorrectResponse();
				extractIdentifiersFromCorrectResponse(correctResponse, correctAnswers);
			}
		}
	}
	
	@Override
	public Identifier getResponseIdentifier() {
		return responseIdentifier;
	}
	
	@Override
	public List<Answer> getAnswers() {
		List<Hottext> hottexts = getChoices();
		List<Answer> answers = new ArrayList<>(hottexts.size());
		for(Hottext hottext:hottexts) {
			String answer = new AssessmentHtmlBuilder().inlineStaticString(hottext.getInlineStatics());
			answer = FilterFactory.getHtmlTagAndDescapingFilter().filter(answer);
			answers.add(new Answer(hottext.getIdentifier(), answer));
		}
		return answers;
	}

	@Override
	public QTI21QuestionType getQuestionType() {
		return QTI21QuestionType.hottext;
	}

	@Override
	public Interaction getInteraction() {
		return hottextInteraction;
	}

	@Override
	public boolean isCorrect(Choice choice) {
		return correctAnswers.contains(choice.getIdentifier());
	}
	
	@Override
	public boolean scoreOfCorrectAnswerWarning() {
		return false;
	}

	@Override
	public int getMaxPossibleCorrectAnswers() {
		return getChoices().size();
	}

	@Override
	public int getMaxChoices() {
		return hottextInteraction.getMaxChoices();
	}
	
	@Override
	public void setMaxChoices(int choices) {
		hottextInteraction.setMaxChoices(choices);
	}
	
	@Override
	public int getMinChoices() {
		return hottextInteraction.getMinChoices();
	}

	@Override
	public void setMinChoices(int choices) {
		hottextInteraction.setMinChoices(choices);
	}

	@Override
	public List<Hottext> getChoices() {
		return QueryUtils.search(Hottext.class, hottextInteraction.getBlockStatics());
	}

	@Override
	public String getQuestion() {
		return question;
	}

	@Override
	public void setQuestion(String question) {
		this.question = question;
	}
	
	public List<Identifier> getCorrectAnswers() {
		return correctAnswers;
	}
	
	public void addCorrectAnswer(Identifier identifier) {
		if(!correctAnswers.contains(identifier)) {
			correctAnswers.add(identifier);
		}
	}
	
	public void removeCorrectAnswer(Identifier identifier) {
		correctAnswers.remove(identifier);
	}

	@Override
	protected void buildItemBody() {
		//remove current blocks
		List<Block> blocks = assessmentItem.getItemBody().getBlocks();
		blocks.clear();

		//add question
		assessmentItem.getItemBody().getBlocks().add(hottextInteraction);
		getHtmlHelper().appendHtml(hottextInteraction, question);
		
		// filter deleted correct answers
		List<Hottext> hottexts = QueryUtils.search(Hottext.class, hottextInteraction.getBlockStatics());
		Set<Identifier> hottextIdentifiers = hottexts.stream()
				.map(hottext -> hottext.getIdentifier()).collect(Collectors.toSet());
		for(Iterator<Identifier> correctAnswerIt = correctAnswers.iterator(); correctAnswerIt.hasNext(); ) {
			if(!hottextIdentifiers.contains(correctAnswerIt.next())) {
				correctAnswerIt.remove();
			}
		}

		if(hottextInteraction.getMaxChoices() == 1) {
			if(correctAnswers.size() > 1) {
				hottextInteraction.setMaxChoices(0);
			}
		}
	}

	@Override
	protected void buildResponseAndOutcomeDeclarations() {
		ResponseDeclaration responseDeclaration = AssessmentItemFactory
				.createHottextCorrectResponseDeclaration(assessmentItem, responseIdentifier, correctAnswers);
		if(scoreEvaluation == ScoreEvaluation.perAnswer) {
			AssessmentItemFactory.appendMapping(responseDeclaration, scoreMapping);
		}
		assessmentItem.getResponseDeclarations().add(responseDeclaration);
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
			matchCorrectAnswers(responseIf);
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
		//simple as build with / without feedback
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
			matchCorrectAnswers(responseIf);
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
			mapResponse.setIdentifier(hottextInteraction.getResponseIdentifier());
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
			mapResponse.setIdentifier(hottextInteraction.getResponseIdentifier());
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
	
	/**
	 * Match the correct answer or, if there isn't not a single correct answer,
	 * match null.
	 * 
	 * @param responseIf
	 */
	private void matchCorrectAnswers(ResponseIf responseIf) {
		if(correctAnswers.isEmpty()) {
			IsNull isNull = new IsNull(responseIf);
			responseIf.getExpressions().add(isNull);
			
			Variable variable = new Variable(isNull);
			variable.setIdentifier(ComplexReferenceIdentifier.parseString(responseIdentifier.toString()));
			isNull.getExpressions().add(variable);
		} else {
			Match match = new Match(responseIf);
			responseIf.getExpressions().add(match);
			
			Variable scoreVar = new Variable(match);
			ComplexReferenceIdentifier choiceResponseIdentifier
				= ComplexReferenceIdentifier.parseString(hottextInteraction.getResponseIdentifier().toString());
			scoreVar.setIdentifier(choiceResponseIdentifier);
			match.getExpressions().add(scoreVar);
			
			Correct correct = new Correct(match);
			correct.setIdentifier(choiceResponseIdentifier);
			match.getExpressions().add(correct);
		}
	}
}
