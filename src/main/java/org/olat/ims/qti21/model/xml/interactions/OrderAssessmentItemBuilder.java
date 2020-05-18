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
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.appendOrderInteraction;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.appendSimpleChoice;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.createOrderCorrectResponseDeclaration;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.createResponseProcessing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.logging.Tracing;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.model.IdentifierGenerator;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.AssessmentItemFactory;
import org.olat.ims.qti21.model.xml.interactions.SimpleChoiceAssessmentItemBuilder.ScoreEvaluation;

import uk.ac.ed.ph.jqtiplus.node.content.ItemBody;
import uk.ac.ed.ph.jqtiplus.node.content.basic.Block;
import uk.ac.ed.ph.jqtiplus.node.expression.general.BaseValue;
import uk.ac.ed.ph.jqtiplus.node.expression.general.Correct;
import uk.ac.ed.ph.jqtiplus.node.expression.general.Variable;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Match;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Sum;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.OrderInteraction;
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
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.types.ComplexReferenceIdentifier;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.BaseType;
import uk.ac.ed.ph.jqtiplus.value.Orientation;

/**
 * 
 * Initial date: 15 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrderAssessmentItemBuilder extends ChoiceAssessmentItemBuilder {
	
	private static final Logger log = Tracing.createLoggerFor(OrderAssessmentItemBuilder.class);

	protected String question;
	protected Orientation orientation;
	protected List<SimpleChoice> choices;
	protected Identifier responseIdentifier;
	private OrderInteraction orderInteraction;
	
	public OrderAssessmentItemBuilder(AssessmentItem assessmentItem, QtiSerializer qtiSerializer) {
		super(assessmentItem, qtiSerializer);
	}
	
	public OrderAssessmentItemBuilder(String title, String defaultAnswer, QtiSerializer qtiSerializer) {
		super(createAssessmentItem(title, defaultAnswer), qtiSerializer);
	}
	
	private static AssessmentItem createAssessmentItem(String title, String defaultAnswer) {
		AssessmentItem assessmentItem = AssessmentItemFactory.createAssessmentItem(QTI21QuestionType.order, title);

		//define correct answer
		Identifier responseDeclarationId = Identifier.assumedLegal("RESPONSE_1");
		Identifier correctResponseId = IdentifierGenerator.newAsIdentifier("order");
		ResponseDeclaration responseDeclaration = createOrderCorrectResponseDeclaration(assessmentItem, responseDeclarationId,
				Collections.singletonList(correctResponseId));
		assessmentItem.getNodeGroups().getResponseDeclarationGroup().getResponseDeclarations().add(responseDeclaration);
		
		//outcomes
		appendDefaultOutcomeDeclarations(assessmentItem, 1.0d);
		
		//the single choice interaction
		ItemBody itemBody = appendDefaultItemBody(assessmentItem);
		OrderInteraction orderInteraction = appendOrderInteraction(itemBody, responseDeclarationId, 1, true);
		
		appendSimpleChoice(orderInteraction, defaultAnswer, correctResponseId);

		//response processing
		ResponseProcessing responseProcessing = createResponseProcessing(assessmentItem, responseDeclarationId);
		assessmentItem.getNodeGroups().getResponseProcessingGroup().setResponseProcessing(responseProcessing);
		return assessmentItem;
	}
	
	@Override
	public void extract() {
		super.extract();
		extractOrderInteraction();
	}
	
	private void extractOrderInteraction() {
		try(StringOutput sb = new StringOutput()) {
			List<Block> blocks = assessmentItem.getItemBody().getBlocks();
			for(Block block:blocks) {
				if(block instanceof OrderInteraction) {
					orderInteraction = (OrderInteraction)block;
					responseIdentifier = orderInteraction.getResponseIdentifier();
					break;
				} else if(block != null) {
					serializeJqtiObject(block, sb);
				}
			}
			question = sb.toString();
		} catch(IOException e) {
			log.error("", e);
		}
		
		choices = new ArrayList<>();
		if(orderInteraction != null) {
			choices.addAll(orderInteraction.getSimpleChoices());
			orientation = orderInteraction.getOrientation();
			setScoreEvaluationMode(ScoreEvaluation.allCorrectAnswers);
		}
	}

	@Override
	public Interaction getInteraction() {
		return orderInteraction;
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
		return getChoices().size();
	}

	@Override
	public void setMaxChoices(int choices) {
		// do nothing
	}

	@Override
	public int getMinChoices() {
		return 0;
	}

	@Override
	public void setMinChoices(int choices) {
		// do nothing
	}

	public Orientation getOrientation() {
		return orientation;
	}

	public void setOrientation(Orientation orientation) {
		this.orientation = orientation;
	}

	@Override
	public boolean isCorrect(Choice choice) {
		return true;
	}
	
	public OrderInteraction getOrderInteraction() {
		return orderInteraction;
	}

	@Override
	public List<SimpleChoice> getChoices() {
		return choices;
	}
	
	public void setSimpleChoices(List<SimpleChoice> choices) {
		this.choices = new ArrayList<>(choices);
	}

	@Override
	public QTI21QuestionType getQuestionType() {
		return QTI21QuestionType.order;
	}

	@Override
	public String getQuestion() {
		return question;
	}

	@Override
	public void setQuestion(String question) {
		this.question = question;
	}
	
	/**
	 * @return A copy of the list of blocks which make the question.
	 * 		The list is a copy and modification will not be persisted.
	 */
	public List<Block> getQuestionBlocks() {
		List<Block> blocks = assessmentItem.getItemBody().getBlocks();
		List<Block> questionBlocks = new ArrayList<>(blocks.size());
		for(Block block:blocks) {
			if(block instanceof OrderInteraction) {
				break;
			} else if(block != null) {
				questionBlocks.add(block);
			}
		}
		return questionBlocks;
	}

	@Override
	protected void buildItemBody() {
		//remove current blocks
		List<Block> blocks = assessmentItem.getItemBody().getBlocks();
		blocks.clear();
		
		//add question
		getHtmlHelper().appendHtml(assessmentItem.getItemBody(), question);
				
		//add interaction
		OrderInteraction interaction = AssessmentItemFactory
					.createOrderInteraction(assessmentItem, responseIdentifier, orientation);
		interaction.setShuffle(true);
		blocks.add(interaction);
		List<SimpleChoice> choiceList = getChoices();
		interaction.getSimpleChoices().addAll(choiceList);
		
		int finalMaxChoices = choiceList.size();
		interaction.setMaxChoices(finalMaxChoices);
		
		int finalMinChoices = 0;
		interaction.setMinChoices(finalMinChoices);
	}

	@Override
	protected void buildResponseAndOutcomeDeclarations() {
		List<Identifier> correctOrderedAnswers = new ArrayList<>();
		for(SimpleChoice choice:this.getChoices()) {
			correctOrderedAnswers.add(choice.getIdentifier());
		}
		ResponseDeclaration responseDeclaration = AssessmentItemFactory
				.createOrderCorrectResponseDeclaration(assessmentItem, responseIdentifier, correctOrderedAnswers);
		assessmentItem.getResponseDeclarations().add(responseDeclaration);
	}

	@Override
	protected void buildMainScoreRule(List<OutcomeDeclaration> outcomeDeclarations, List<ResponseRule> responseRules) {
		ResponseCondition rule = new ResponseCondition(assessmentItem.getResponseProcessing());
		responseRules.add(0, rule);
		buildMainScoreRuleAllCorrectAnswers(rule);
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
			ComplexReferenceIdentifier orderResponseIdentifier
				= ComplexReferenceIdentifier.parseString(orderInteraction.getResponseIdentifier().toString());
			scoreVar.setIdentifier(orderResponseIdentifier);
			match.getExpressions().add(scoreVar);
			
			Correct correct = new Correct(match);
			correct.setIdentifier(orderResponseIdentifier);
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

}
