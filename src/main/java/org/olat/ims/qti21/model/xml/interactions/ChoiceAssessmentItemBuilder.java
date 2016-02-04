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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.stream.StreamResult;

import org.olat.core.gui.render.StringOutput;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;

import uk.ac.ed.ph.jqtiplus.node.content.basic.Block;
import uk.ac.ed.ph.jqtiplus.node.expression.general.BaseValue;
import uk.ac.ed.ph.jqtiplus.node.expression.general.Variable;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.And;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Equal;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Match;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Not;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.ToleranceMode;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleChoice;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.MapEntry;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.Mapping;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.ResponseDeclaration;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseCondition;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseIf;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseRule;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.SetOutcomeValue;
import uk.ac.ed.ph.jqtiplus.node.outcome.declaration.OutcomeDeclaration;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.BaseType;
import uk.ac.ed.ph.jqtiplus.value.IdentifierValue;
import uk.ac.ed.ph.jqtiplus.value.SingleValue;

/**
 * 
 * Initial date: 08.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class ChoiceAssessmentItemBuilder extends AssessmentItemBuilder {

	protected boolean shuffle;
	protected String question;
	protected List<SimpleChoice> choices;
	protected Identifier responseIdentifier;
	protected ScoreEvaluation scoreEvaluation;
	protected ChoiceInteraction choiceInteraction;
	protected Map<Identifier,Double> scoreMapping;
	
	public ChoiceAssessmentItemBuilder(AssessmentItem assessmentItem, QtiSerializer qtiSerializer) {
		super(assessmentItem, qtiSerializer);
	}
	
	@Override
	public void extract() {
		super.extract();
		extractChoiceInteraction();
		extractScoreEvaluationMode();
	}
	
	private void extractScoreEvaluationMode() {
		boolean hasMapping = false;
		if(choiceInteraction != null) {
			ResponseDeclaration responseDeclaration = assessmentItem
					.getResponseDeclaration(choiceInteraction.getResponseIdentifier());
			if(responseDeclaration != null) {
				Mapping mapping = responseDeclaration.getMapping();
				
				hasMapping = (mapping != null && mapping.getMapEntries() != null && mapping.getMapEntries().size() > 0);
				if(hasMapping) {
					scoreMapping = new HashMap<>();
					for(MapEntry entry:mapping.getMapEntries()) {
						SingleValue sValue = entry.getMapKey();
						if(sValue instanceof IdentifierValue) {
							Identifier identifier = ((IdentifierValue)sValue).identifierValue();
							scoreMapping.put(identifier, entry.getMappedValue());
						}
					}
				}
			}
		}
		scoreEvaluation = hasMapping ? ScoreEvaluation.perAnswer : ScoreEvaluation.allCorrectAnswers;
	}
	
	private void extractChoiceInteraction() {
		StringOutput sb = new StringOutput();
		List<Block> blocks = assessmentItem.getItemBody().getBlocks();
		for(Block block:blocks) {
			if(block instanceof ChoiceInteraction) {
				choiceInteraction = (ChoiceInteraction)block;
				responseIdentifier = choiceInteraction.getResponseIdentifier();
				shuffle = choiceInteraction.getShuffle();
				break;
			} else {
				qtiSerializer.serializeJqtiObject(block, new StreamResult(sb));
			}
		}
		question = sb.toString();
		
		choices = new ArrayList<>();
		if(choiceInteraction != null) {
			choices.addAll(choiceInteraction.getSimpleChoices());
		}
	}
	
	public ChoiceInteraction getChoiceInteraction() {
		return choiceInteraction;
	}
	
	public abstract boolean isCorrect(SimpleChoice choice);
	
	public boolean isShuffle() {
		return shuffle;
	}
	
	public void setShuffle(boolean shuffle) {
		this.shuffle = shuffle;
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
	
	/**
	 * Return the HTML block before the choice interaction as a string.
	 * 
	 * @return
	 */
	public String getQuestion() {
		return question;
	}
	
	public void setQuestion(String html) {
		this.question = html;
	}
	
	public List<SimpleChoice> getSimpleChoices() {
		return choices;
	}
	
	public void setSimpleChoices(List<SimpleChoice> choices) {
		this.choices = new ArrayList<>(choices);
	}

	@Override
	protected void buildModalFeedback(List<OutcomeDeclaration> outcomeDeclarations, List<ResponseRule> responseRules) {
		if(correctFeedback != null || incorrectFeedback != null) {
			/*
			<responseCondition>
				<responseIf>
					<and>
						<not>
							<match>
								<variable identifier="FEEDBACKBASIC" />
								<baseValue baseType="identifier">empty</baseValue>
							</match>
						</not>
						<equal toleranceMode="exact">
							<variable identifier="SCORE" />
							<variable identifier="MAXSCORE" />
						</equal>
					</and>
					<setOutcomeValue identifier="FEEDBACKBASIC">
						<baseValue baseType="identifier">correct</baseValue>
					</setOutcomeValue>
				</responseIf>
			</responseCondition>
			 */
			
			ResponseCondition responseCondition = new ResponseCondition(assessmentItem.getResponseProcessing());
			responseRules.add(responseCondition);
			
			ResponseIf responseIf = new ResponseIf(responseCondition);
			responseCondition.setResponseIf(responseIf);
			
			And and = new And(responseIf);
			responseIf.getExpressions().add(and);
			
			Not not = new Not(and);
			and.getExpressions().add(not);
			
			Match match = new Match(not);
			not.getExpressions().add(match);
			
			Variable feedbackbasicVar = new Variable(match);
			feedbackbasicVar.setIdentifier(QTI21Constants.FEEDBACKBASIC_CLX_IDENTIFIER);
			match.getExpressions().add(feedbackbasicVar);

			BaseValue emptyValue = new BaseValue(match);
			emptyValue.setBaseTypeAttrValue(BaseType.IDENTIFIER);
			emptyValue.setSingleValue(QTI21Constants.EMPTY_IDENTIFIER_VALUE);
			match.getExpressions().add(emptyValue);
	
			//SCORE == MAXSCORE
			Equal equal = new Equal(and);
			equal.setToleranceMode(ToleranceMode.EXACT);
			and.getExpressions().add(equal);
			
			Variable scoreVar = new Variable(equal);
			scoreVar.setIdentifier(QTI21Constants.SCORE_CLX_IDENTIFIER);
			equal.getExpressions().add(scoreVar);
			
			Variable maxScoreVar = new Variable(equal);
			maxScoreVar.setIdentifier(QTI21Constants.MAXSCORE_CLX_IDENTIFIER);
			equal.getExpressions().add(maxScoreVar);

			//outcome value
			SetOutcomeValue correctOutcomeValue = new SetOutcomeValue(responseIf);
			correctOutcomeValue.setIdentifier(QTI21Constants.FEEDBACKBASIC_IDENTIFIER);
			responseIf.getResponseRules().add(correctOutcomeValue);
			
			BaseValue correctValue = new BaseValue(correctOutcomeValue);
			correctValue.setBaseTypeAttrValue(BaseType.IDENTIFIER);
			correctValue.setSingleValue(QTI21Constants.CORRECT_IDENTIFIER_VALUE);
			correctOutcomeValue.setExpression(correctValue);
		}

		super.buildModalFeedback(outcomeDeclarations, responseRules);
	}

	public enum ScoreEvaluation {
		perAnswer,
		allCorrectAnswers
	}
}
