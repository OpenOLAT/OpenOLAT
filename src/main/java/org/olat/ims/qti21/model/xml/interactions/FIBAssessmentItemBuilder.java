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
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.appendTextEntryInteraction;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.createExtendedTextResponseDeclaration;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.createResponseProcessing;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.createTextEntryResponseDeclaration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.transform.stream.StreamResult;

import org.olat.core.gui.render.StringOutput;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.AssessmentItemFactory;
import org.olat.ims.qti21.model.xml.interactions.ChoiceAssessmentItemBuilder.ScoreEvaluation;

import uk.ac.ed.ph.jqtiplus.node.content.ItemBody;
import uk.ac.ed.ph.jqtiplus.node.content.basic.Block;
import uk.ac.ed.ph.jqtiplus.node.expression.general.BaseValue;
import uk.ac.ed.ph.jqtiplus.node.expression.general.MapResponse;
import uk.ac.ed.ph.jqtiplus.node.expression.general.Variable;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.And;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Match;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Sum;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.CorrectResponse;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.TextEntryInteraction;
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
import uk.ac.ed.ph.jqtiplus.node.shared.FieldValue;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.types.ComplexReferenceIdentifier;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.BaseType;
import uk.ac.ed.ph.jqtiplus.value.FloatValue;
import uk.ac.ed.ph.jqtiplus.value.SingleValue;
import uk.ac.ed.ph.jqtiplus.value.StringValue;

/**
 * 
 * Initial date: 16.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FIBAssessmentItemBuilder extends AssessmentItemBuilder {

	private String question;
	private ScoreEvaluation scoreEvaluation;
	private Map<String,TextEntry> responseIdentifierToTextEntry;
	
	public FIBAssessmentItemBuilder(QtiSerializer qtiSerializer) {
		super(createAssessmentItem(), qtiSerializer);
	}
	
	public FIBAssessmentItemBuilder(AssessmentItem assessmentItem, QtiSerializer qtiSerializer) {
		super(assessmentItem, qtiSerializer);
	}
	
	private static AssessmentItem createAssessmentItem() {
		AssessmentItem assessmentItem = AssessmentItemFactory.createAssessmentItem(QTI21QuestionType.fib, "FIB");
		
		//define the response
		Identifier responseDeclarationId = Identifier.assumedLegal("RESPONSE_1");
		ResponseDeclaration responseDeclaration = createExtendedTextResponseDeclaration(assessmentItem, responseDeclarationId);
		assessmentItem.getNodeGroups().getResponseDeclarationGroup().getResponseDeclarations().add(responseDeclaration);
	
		//outcomes
		appendDefaultOutcomeDeclarations(assessmentItem, 1.0d);
		
		ItemBody itemBody = appendDefaultItemBody(assessmentItem);
		appendTextEntryInteraction(itemBody, responseDeclarationId);
		
		//response processing
		ResponseProcessing responseProcessing = createResponseProcessing(assessmentItem, responseDeclarationId);
		assessmentItem.getNodeGroups().getResponseProcessingGroup().setResponseProcessing(responseProcessing);
		return assessmentItem;
	}

	@Override
	protected void extract() {
		super.extract();
		extractTextEntryInteractions();
		extractTextEntrySettingsFromResponseDeclaration();
		if(scoreEvaluation == ScoreEvaluation.allCorrectAnswers) {
			extractCaseSensitivity();
		}
	}
	
	private void extractTextEntryInteractions() {
		StringOutput sb = new StringOutput();
		List<Block> blocks = assessmentItem.getItemBody().getBlocks();
		for(Block block:blocks) {
			qtiSerializer.serializeJqtiObject(block, new StreamResult(sb));
		}
		question = sb.toString();
		
		responseIdentifierToTextEntry = new HashMap<>();
		
		List<Interaction> interactions = assessmentItem.getItemBody().findInteractions();
		for(Interaction interaction:interactions) {
			if(interaction instanceof TextEntryInteraction && interaction.getResponseIdentifier() != null) {
				TextEntry entry = new TextEntry((TextEntryInteraction)interaction);
				responseIdentifierToTextEntry.put(interaction.getResponseIdentifier().toString(), entry);
			}
		}
	}
	
	private void extractTextEntrySettingsFromResponseDeclaration() {
		double mappedScore = 0.0d;
		int countAlternatives = 0;
		
		for(Map.Entry<String, TextEntry> textEntryEntry:responseIdentifierToTextEntry.entrySet()) {
			TextEntry textEntry = textEntryEntry.getValue();
			ResponseDeclaration responseDeclaration = assessmentItem.getResponseDeclaration(textEntry.getResponseIdentifier());
			if(responseDeclaration != null) {
				String solution = null;
				CorrectResponse correctResponse = responseDeclaration.getCorrectResponse();
				if(correctResponse != null && correctResponse.getFieldValues().size() > 0) {
					List<FieldValue> fValues = correctResponse.getFieldValues();
					SingleValue sValue = fValues.get(0).getSingleValue();
					if(sValue instanceof StringValue) {
						solution = ((StringValue)sValue).stringValue();
						textEntry.setSolution(solution);
					}
					
					if(correctResponse.getFieldValues().size() > 1) {
						List<TextEntryAlternative> alternatives = new ArrayList<>();
						for(int i=1; i<correctResponse.getFieldValues().size(); i++) {
							SingleValue aValue = fValues.get(i).getSingleValue();
							if(aValue instanceof StringValue) {
								TextEntryAlternative alternative = new TextEntryAlternative();
								alternative.setAlternative(((StringValue)aValue).stringValue());
								alternatives.add(alternative);
							}
						}
						textEntry.setAlternatives(alternatives);
					}
				}

				Mapping mapping = responseDeclaration.getMapping();
				if(mapping != null) {
					boolean caseSensitive = true;
					List<TextEntryAlternative> alternatives = new ArrayList<>();
					if(mapping != null) {
						List<MapEntry> mapEntries = mapping.getMapEntries();
						for(MapEntry mapEntry:mapEntries) {
							TextEntryAlternative alternative = new TextEntryAlternative();
							SingleValue sValue = mapEntry.getMapKey();
							if(sValue instanceof StringValue) {
								String alt = ((StringValue)sValue).stringValue();
								if(solution == null || !solution.equals(alt)) {
									alternative.setAlternative(alt);
									alternative.setScore(mapEntry.getMappedValue());
									alternatives.add(alternative);
								} else if(alt.equals(solution)) {
									textEntry.setScore(mapEntry.getMappedValue());
								}
								countAlternatives++;
								mappedScore += mapEntry.getMappedValue();
							}
							
							caseSensitive &= mapEntry.getCaseSensitive();
						}
					}
					textEntry.setCaseSensitive(caseSensitive);
					textEntry.setAlternatives(alternatives);
				}
			}
		}
		
		boolean hasMapping = Math.abs(mappedScore - (-1.0 * countAlternatives)) > 0.0001;
		scoreEvaluation = hasMapping ? ScoreEvaluation.perAnswer : ScoreEvaluation.allCorrectAnswers;
	}
	
	/**
	 * Case sensitivity is made of stringMatch instead of simple match
	 */
	private void extractCaseSensitivity() {
		
	}

	@Override
	public QTI21QuestionType getQuestionType() {
		return QTI21QuestionType.fib;
	}
	
	@Override
	public String getQuestion() {
		return question;
	}
	
	@Override
	public void setQuestion(String html) {
		this.question = html;
	}
	
	public ScoreEvaluation getScoreEvaluationMode() {
		return scoreEvaluation;
	}

	public void setScoreEvaluationMode(ScoreEvaluation scoreEvaluation) {
		this.scoreEvaluation = scoreEvaluation;
	}

	public TextEntry getTextEntry(String responseIdentifier) {
		return responseIdentifierToTextEntry.get(responseIdentifier);
	}
	
	public void clearTextEntries() {
		responseIdentifierToTextEntry.clear();
	}
	
	public List<TextEntry> getTextEntries() {
		return new ArrayList<>(responseIdentifierToTextEntry.values());
	}
	
	public String generateResponseIdentifier() {
		for(int i=1; i<9999; i++) {
			String responseIdentifier = "RESPONSE_" + i;
			if(!responseIdentifierToTextEntry.containsKey(responseIdentifier)) {
				return responseIdentifier;
			}
		}
		return null;
		
	}
	
	public TextEntry createTextEntry(String responseIdentifier) {
		TextEntry entry = new TextEntry(Identifier.parseString(responseIdentifier));
		responseIdentifierToTextEntry.put(responseIdentifier, entry);
		return entry;
	}

	@Override
	protected void buildResponseDeclaration() {
		List<ResponseDeclaration> responseDeclarations = assessmentItem.getResponseDeclarations();
		
		if(scoreEvaluation == ScoreEvaluation.perAnswer) {
			
			/*
			<responseDeclaration identifier="RESPONSE_1" cardinality="single" baseType="string">
				<correctResponse>
					<value>
						Gap
					</value>
				</correctResponse>
				<mapping defaultValue="0">
					<mapEntry mapKey="Gap" mappedValue="2" />
					<mapEntry mapKey="gap1" mappedValue="2" />
					<mapEntry mapKey="gap2" mappedValue="1" />
				</mapping>
			</responseDeclaration>
			*/

			for(Map.Entry<String, TextEntry> textEntryEntry:responseIdentifierToTextEntry.entrySet()) {
				TextEntry textEntry = textEntryEntry.getValue();
				if(textEntry.getSolution() != null) {
					ResponseDeclaration responseDeclaration = createTextEntryResponseDeclaration(assessmentItem,
							textEntry.getResponseIdentifier(), textEntry.getSolution(),
							textEntry.getScore(), textEntry.isCaseSensitive(),
							textEntry.getAlternatives());
					responseDeclarations.add(responseDeclaration);
				}
			}
		} else {
			for(Map.Entry<String, TextEntry> textEntryEntry:responseIdentifierToTextEntry.entrySet()) {
				TextEntry textEntry = textEntryEntry.getValue();
				if(textEntry.getSolution() != null) {
					ResponseDeclaration responseDeclaration = createTextEntryResponseDeclaration(assessmentItem,
							textEntry.getResponseIdentifier(), textEntry.getSolution(),
							-1.0, textEntry.isCaseSensitive(),
							textEntry.getAlternatives());
					responseDeclarations.add(responseDeclaration);
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

		//transfer text entry to the interactions
		List<Interaction> interactions = assessmentItem.getItemBody().findInteractions();
		for(Interaction interaction:interactions) {
			if(interaction instanceof TextEntryInteraction && interaction.getResponseIdentifier() != null) {
				TextEntryInteraction textEntryInteraction = (TextEntryInteraction)interaction;
				TextEntry entry = responseIdentifierToTextEntry.get(interaction.getResponseIdentifier().toString());
				textEntryInteraction.setPlaceholderText(entry.getPlaceholder());
				textEntryInteraction.setExpectedLength(entry.getExpectedLength());
			}
		}
	}

	@Override
	protected void buildScores(List<OutcomeDeclaration> outcomeDeclarations, List<ResponseRule> responseRules) {
		super.buildScores(outcomeDeclarations, responseRules);
	}

	@Override
	protected void buildMainScoreRule(List<OutcomeDeclaration> outcomeDeclarations, List<ResponseRule> responseRules) {
		ensureFeedbackBasicOutcomeDeclaration();
		if(scoreEvaluation == ScoreEvaluation.perAnswer) {
			buildMainScoreRulePerAnswer(outcomeDeclarations, responseRules);
		} else {
			buildMainScoreRuleAllCorrectAnswers(responseRules);
		}
	}
	
	private void buildMainScoreRuleAllCorrectAnswers(List<ResponseRule> responseRules) {
		/*
		<responseCondition>
			<responseIf>
				<or>
					<isNull>
						<variable identifier="RESPONSE_1" />
					</isNull>
				</or>
				<setOutcomeValue identifier="FEEDBACKBASIC">
					<baseValue baseType="identifier">
						incorrect
					</baseValue>
				</setOutcomeValue>
			</responseIf>
			<responseElseIf>
				<and>
					<match>
						<value>-1.0</value>
						<correct identifier="RESPONSE_1" />
					</match>
				</and>
				<setOutcomeValue identifier="SCORE">
					<sum>
						<variable identifier="SCORE" />
						<variable identifier="MAXSCORE" />
					</sum>
				</setOutcomeValue>
				<setOutcomeValue identifier="FEEDBACKBASIC">
					<baseValue baseType="identifier">
						incorrect
					</baseValue>
				</setOutcomeValue>
			</responseElseIf>
		</responseCondition>
		*/
		
		// add condition
		ResponseCondition rule = new ResponseCondition(assessmentItem.getResponseProcessing());
		responseRules.add(0, rule);
		/*
		{//missing responses
			ResponseIf responseIf = new ResponseIf(rule);
			rule.setResponseIf(responseIf);
			
			Or or = new Or(responseIf);
			responseIf.setExpression(or);
			
			for(Map.Entry<String, TextEntry> textEntryEntry:responseIdentifierToTextEntry.entrySet()) {
				TextEntry textEntry = textEntryEntry.getValue();
				IsNull isNull = new IsNull(or);
				or.getExpressions().add(isNull);
				
				Variable variable = new Variable(isNull);
				isNull.getExpressions().add(variable);
				variable.setIdentifier(ComplexReferenceIdentifier.parseString(textEntry.getResponseIdentifier().toString()));
			}
			
			{//outcome feedback basic
				SetOutcomeValue incorrectOutcomeValue = new SetOutcomeValue(responseIf);
				incorrectOutcomeValue.setIdentifier(QTI21Constants.FEEDBACKBASIC_IDENTIFIER);
				responseIf.getResponseRules().add(incorrectOutcomeValue);
				
				BaseValue incorrectValue = new BaseValue(incorrectOutcomeValue);
				incorrectValue.setBaseTypeAttrValue(BaseType.IDENTIFIER);
				incorrectValue.setSingleValue(QTI21Constants.INCORRECT_IDENTIFIER_VALUE);
				incorrectOutcomeValue.setExpression(incorrectValue);
			}
		}*/
		
		{// match all
			ResponseIf responseElseIf = new ResponseIf(rule);
			rule.setResponseIf(responseElseIf);
			//rule.getResponseElseIfs().add(responseElseIf);
			
			And and = new And(responseElseIf);
			responseElseIf.setExpression(and);
			
			for(Map.Entry<String, TextEntry> textEntryEntry:responseIdentifierToTextEntry.entrySet()) {
				TextEntry textEntry = textEntryEntry.getValue();
				Match match = new Match(and);
				and.getExpressions().add(match);
			
				BaseValue variable = new BaseValue(match);
				variable.setBaseTypeAttrValue(BaseType.FLOAT);
				variable.setSingleValue(new FloatValue(-1.0d));
				match.getExpressions().add(variable);
				
				MapResponse correct = new MapResponse(match);
				correct.setIdentifier(textEntry.getResponseIdentifier());
				match.getExpressions().add(correct);
			}
			
			{// outcome max score -> score
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
		}
		
		{// else feedback incorrect
			ResponseElse responseElse = new ResponseElse(rule);
			rule.setResponseElse(responseElse);
			
			{//outcome feedback
				SetOutcomeValue correctOutcomeValue = new SetOutcomeValue(responseElse);
				correctOutcomeValue.setIdentifier(QTI21Constants.FEEDBACKBASIC_IDENTIFIER);
				responseElse.getResponseRules().add(correctOutcomeValue);
				
				BaseValue correctValue = new BaseValue(correctOutcomeValue);
				correctValue.setBaseTypeAttrValue(BaseType.IDENTIFIER);
				correctValue.setSingleValue(QTI21Constants.INCORRECT_IDENTIFIER_VALUE);
				correctOutcomeValue.setExpression(correctValue);
			}
		}
	}

	private void buildMainScoreRulePerAnswer(List<OutcomeDeclaration> outcomeDeclarations, List<ResponseRule> responseRules) {
		/*
		<responseCondition>
			<responseIf>
				<not>
					<isNull>
						<variable identifier="RESPONSE_1" />
					</isNull>
				</not>
				<setOutcomeValue identifier="SCORE_RESPONSE_1">
					<mapResponse identifier="RESPONSE_1" />
				</setOutcomeValue>
				<setOutcomeValue identifier="FEEDBACKBASIC">
					<baseValue baseType="identifier">
						incorrect
					</baseValue>
				</setOutcomeValue>
			</responseIf>
		</responseCondition>
		*/

		int count = 0;
		
		for(Map.Entry<String, TextEntry> textEntryEntry:responseIdentifierToTextEntry.entrySet()) {
			TextEntry textEntry = textEntryEntry.getValue();
			String scoreIdentifier = "SCORE_" + textEntry.getResponseIdentifier().toString();

			{//outcome mapResonse
				SetOutcomeValue mapOutcomeValue = new SetOutcomeValue(assessmentItem.getResponseProcessing());
				responseRules.add(count++, mapOutcomeValue);
				mapOutcomeValue.setIdentifier(Identifier.parseString(scoreIdentifier));
				
				MapResponse mapResponse = new MapResponse(mapOutcomeValue);
				mapResponse.setIdentifier(textEntry.getResponseIdentifier());
				mapOutcomeValue.setExpression(mapResponse);
			}
		}
		
		/*
		for(Map.Entry<String, TextEntry> textEntryEntry:responseIdentifierToTextEntry.entrySet()) {
			TextEntry textEntry = textEntryEntry.getValue();
			String scoreIdentifier = "SCORE_" + textEntry.getResponseIdentifier().toString();
			
			// add outcome variables
			
			// add condition
			ResponseCondition rule = new ResponseCondition(assessmentItem.getResponseProcessing());
			responseRules.add(count++, rule);
			
			ResponseIf responseIf = new ResponseIf(rule);
			rule.setResponseIf(responseIf);
			
			Not not = new Not(responseIf);
			responseIf.setExpression(not);
			IsNull isNull = new IsNull(not);
			not.getExpressions().add(isNull);
			Variable variable = new Variable(isNull);
			isNull.getExpressions().add(variable);
			variable.setIdentifier(ComplexReferenceIdentifier.parseString(textEntry.getResponseIdentifier().toString()));
			
			{//outcome mapResonse
				SetOutcomeValue mapOutcomeValue = new SetOutcomeValue(responseIf);
				responseIf.getResponseRules().add(mapOutcomeValue);
				mapOutcomeValue.setIdentifier(Identifier.parseString(scoreIdentifier));
				
				MapResponse mapResponse = new MapResponse(mapOutcomeValue);
				mapResponse.setIdentifier(textEntry.getResponseIdentifier());
				mapOutcomeValue.setExpression(mapResponse);
			}
			
			{//outcome feedback basic
				SetOutcomeValue incorrectOutcomeValue = new SetOutcomeValue(responseIf);
				incorrectOutcomeValue.setIdentifier(QTI21Constants.FEEDBACKBASIC_IDENTIFIER);
				responseIf.getResponseRules().add(incorrectOutcomeValue);
				
				BaseValue incorrectValue = new BaseValue(incorrectOutcomeValue);
				incorrectValue.setBaseTypeAttrValue(BaseType.IDENTIFIER);
				incorrectValue.setSingleValue(QTI21Constants.EMPTY_IDENTIFIER_VALUE);
				incorrectOutcomeValue.setExpression(incorrectValue);
			}
		}*/

		/*
		<setOutcomeValue identifier="SCORE">
			<sum>
				<variable identifier="SCORE_RESPONSE_1" /><variable identifier="MINSCORE_RESPONSE_1" /><variable identifier="SCORE_RESPONSE_2" /><variable identifier="MINSCORE_RESPONSE_2" />
			</sum>
		</setOutcomeValue>
		*/
		{
			SetOutcomeValue scoreOutcome = new SetOutcomeValue(assessmentItem.getResponseProcessing());
			scoreOutcome.setIdentifier(QTI21Constants.SCORE_IDENTIFIER);
			responseRules.add(count++, scoreOutcome);
			
			Sum sum = new Sum(scoreOutcome);
			scoreOutcome.setExpression(sum);
			
			for(Map.Entry<String, TextEntry> textEntryEntry:responseIdentifierToTextEntry.entrySet()) {
				TextEntry textEntry = textEntryEntry.getValue();
				
				{//variable score
					Variable scoreVariable = new Variable(sum);
					sum.getExpressions().add(scoreVariable);
					String scoreIdentifier = "SCORE_" + textEntry.getResponseIdentifier().toString();
					scoreVariable.setIdentifier(ComplexReferenceIdentifier.parseString(scoreIdentifier));
					
					//create associated outcomeDeclaration
					OutcomeDeclaration modalOutcomeDeclaration = AssessmentItemFactory
							.createOutcomeDeclarationForScoreResponse(assessmentItem, scoreIdentifier);
					outcomeDeclarations.add(modalOutcomeDeclaration);
				}
				
				{//variable minscore
					Variable minScoreVariable = new Variable(sum);
					sum.getExpressions().add(minScoreVariable);
					String scoreIdentifier = "MINSCORE_" + textEntry.getResponseIdentifier().toString();
					minScoreVariable.setIdentifier(ComplexReferenceIdentifier.parseString(scoreIdentifier));
					
					//create associated outcomeDeclaration
					OutcomeDeclaration modalOutcomeDeclaration = AssessmentItemFactory
							.createOutcomeDeclarationForScoreResponse(assessmentItem, scoreIdentifier);
					outcomeDeclarations.add(modalOutcomeDeclaration);
				}
			}
		}
	}
	
	public class TextEntry {
		private Identifier responseIdentifier;
		
		private String placeholder;
		private Integer expectedLength;
		private boolean caseSensitive;
		
		private Double score;
		private String solution;
		private List<TextEntryAlternative> alternatives;
		
		public TextEntry(Identifier responseIdentifier) {
			this.responseIdentifier = responseIdentifier;
		}
		
		public TextEntry(TextEntryInteraction entry) {
			responseIdentifier = entry.getResponseIdentifier();
			placeholder = entry.getPlaceholderText();
			expectedLength = entry.getExpectedLength();
		}
		
		public Identifier getResponseIdentifier() {
			return responseIdentifier;
		}
		
		public String getPlaceholder() {
			return placeholder;
		}
		
		public void setPlaceholder(String placeholder) {
			this.placeholder = placeholder;
		}
		
		public Integer getExpectedLength() {
			return expectedLength;
		}
		
		public void setExpectedLength(Integer expectedLength) {
			this.expectedLength = expectedLength;
		}
		
		public boolean isCaseSensitive() {
			return caseSensitive;
		}

		public void setCaseSensitive(boolean caseSensitive) {
			this.caseSensitive = caseSensitive;
		}

		public Double getScore() {
			return score;
		}

		public void setScore(Double score) {
			this.score = score;
		}

		public String getSolution() {
			return solution;
		}

		public void setSolution(String solution) {
			this.solution = solution;
		}

		public List<TextEntryAlternative> getAlternatives() {
			return alternatives;
		}
		
		public String alternativesToString() {
			StringBuilder sb = new StringBuilder();
			if(alternatives != null) {
				for(TextEntryAlternative alternative:alternatives) {
					if(sb.length() > 0) sb.append(",");
					sb.append(alternative.getAlternative());
				}
			}
			return sb.toString();
		}

		public void setAlternatives(List<TextEntryAlternative> alternatives) {
			this.alternatives = alternatives;
		}
		
		public void addAlterantive(String alternative, double points) {
			if(alternatives == null) {
				alternatives = new ArrayList<>();
			}
			TextEntryAlternative alt = new TextEntryAlternative();
			alt.setAlternative(alternative);
			alt.setScore(points);
			alternatives.add(alt);
		}
		
		public void stringToAlternatives(String string) {
			if(alternatives == null) {
				alternatives = new ArrayList<>();
			}
			
			String[] alternativesArr = string.split(",");
			for(String alternative:alternativesArr) {
				boolean found = false;
				for(TextEntryAlternative textEntryAlternative:alternatives) {
					if(alternative.equals(textEntryAlternative.getAlternative())) {
						found = true;
					}
				}
				
				if(!found) {
					TextEntryAlternative newAlternative = new TextEntryAlternative();
					newAlternative.setAlternative(alternative);
					alternatives.add(newAlternative);
				}
			}
			
			for(Iterator<TextEntryAlternative> textEntryAlternativeIt=alternatives.iterator(); textEntryAlternativeIt.hasNext(); ) {
				TextEntryAlternative textEntryAlternative = textEntryAlternativeIt.next();
				
				boolean found = false;
				for(String alternative:alternativesArr) {
					if(alternative.equals(textEntryAlternative.getAlternative())) {
						found = true;
					}
				}

				if(!found) {
					textEntryAlternativeIt.remove();
				}
			}
		}
	}
	
	public class TextEntryAlternative {
		
		private String alternative;
		private double score;

		public String getAlternative() {
			return alternative;
		}

		public void setAlternative(String alternative) {
			this.alternative = alternative;
		}

		public double getScore() {
			return score;
		}

		public void setScore(double score) {
			this.score = score;
		}
	}
}
