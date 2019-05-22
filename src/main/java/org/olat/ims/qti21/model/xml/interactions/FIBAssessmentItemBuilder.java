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
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.createNumericalEntryResponseDeclaration;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.createResponseProcessing;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.createTextEntryResponseDeclaration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;

import org.olat.core.gui.render.StringOutput;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.AssessmentItemFactory;
import org.olat.ims.qti21.model.xml.interactions.SimpleChoiceAssessmentItemBuilder.ScoreEvaluation;

import uk.ac.ed.ph.jqtiplus.exception.QtiAttributeException;
import uk.ac.ed.ph.jqtiplus.node.content.ItemBody;
import uk.ac.ed.ph.jqtiplus.node.content.basic.Block;
import uk.ac.ed.ph.jqtiplus.node.expression.Expression;
import uk.ac.ed.ph.jqtiplus.node.expression.general.BaseValue;
import uk.ac.ed.ph.jqtiplus.node.expression.general.Correct;
import uk.ac.ed.ph.jqtiplus.node.expression.general.MapResponse;
import uk.ac.ed.ph.jqtiplus.node.expression.general.Variable;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.And;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Equal;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Match;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Sum;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.ToleranceMode;
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
import uk.ac.ed.ph.jqtiplus.types.FloatOrVariableRef;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.BaseType;
import uk.ac.ed.ph.jqtiplus.value.Cardinality;
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
	
	private static final Logger log = Tracing.createLoggerFor(FIBAssessmentItemBuilder.class);

	private String question;
	private ScoreEvaluation scoreEvaluation;
	private Map<String, AbstractEntry> responseIdentifierToTextEntry;
	
	private QTI21QuestionType questionType = QTI21QuestionType.fib;
	
	public FIBAssessmentItemBuilder(String title, EntryType type, QtiSerializer qtiSerializer) {
		super(createAssessmentItem(title, type), qtiSerializer);
	}
	
	public FIBAssessmentItemBuilder(AssessmentItem assessmentItem, QtiSerializer qtiSerializer) {
		super(assessmentItem, qtiSerializer);
	}
	
	private static AssessmentItem createAssessmentItem(String title, EntryType type) {
		AssessmentItem assessmentItem = AssessmentItemFactory.createAssessmentItem(QTI21QuestionType.fib, title);
		
		//define the response
		Identifier responseDeclarationId = Identifier.assumedLegal("RESPONSE_1");
		if(type == EntryType.numerical) {
			ResponseDeclaration responseDeclaration = createNumericalEntryResponseDeclaration(assessmentItem, responseDeclarationId, 42);
			assessmentItem.getNodeGroups().getResponseDeclarationGroup().getResponseDeclarations().add(responseDeclaration);
		} else {
			ResponseDeclaration responseDeclaration = createTextEntryResponseDeclaration(assessmentItem, responseDeclarationId,
					"gap", Collections.emptyList());
			assessmentItem.getNodeGroups().getResponseDeclarationGroup().getResponseDeclarations().add(responseDeclaration);
		}
	
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
		extractQuestions();
		extractEntriesSettingsFromResponseDeclaration();
		extractQuestionType();
	}
	
	/**
	 * Use the extracted entries to calculate the type, fib or numerical.
	 */
	private void extractQuestionType() {
		int text = 0;
		int numerical = 0;
		for(Map.Entry<String, AbstractEntry> textEntryEntry:responseIdentifierToTextEntry.entrySet()) {
			AbstractEntry entry = textEntryEntry.getValue();
			if(entry instanceof TextEntry) {
				text++;
			} else if(entry instanceof NumericalEntry) {
				numerical++;
			}
		}
		
		if(text > 0 && numerical == 0) {
			questionType = QTI21QuestionType.fib;
		} else if(text == 0 && numerical > 0) {
			questionType = QTI21QuestionType.numerical;
		} else {
			questionType = QTI21QuestionType.fib;
		}
	}
	
	public String extractQuestions() {
		try(StringOutput sb = new StringOutput()) {
			List<Block> blocks = assessmentItem.getItemBody().getBlocks();
			for(Block block:blocks) {
				serializeJqtiObject(block, sb);
			}
			question = sb.toString();
		} catch(IOException e) {
			log.error("", e);
		}
		return question;
	}
	
	/**
	 * We loop around the textEntryInteraction, search the responseDeclaration. responseDeclaration
	 * of type string are gap text, of type float are numerical.
	 */
	public void extractEntriesSettingsFromResponseDeclaration() {
		DoubleAdder mappedScore = new DoubleAdder();
		AtomicInteger countAlternatives = new AtomicInteger(0);

		responseIdentifierToTextEntry = new HashMap<>();
		
		List<Interaction> interactions = assessmentItem.getItemBody().findInteractions();
		for(Interaction interaction:interactions) {
			if(interaction instanceof TextEntryInteraction && interaction.getResponseIdentifier() != null) {
				AbstractEntry entry = null;
				TextEntryInteraction textInteraction = (TextEntryInteraction)interaction;
				
				ResponseDeclaration responseDeclaration = assessmentItem.getResponseDeclaration(interaction.getResponseIdentifier());
				if(responseDeclaration != null) {
					if(responseDeclaration.hasBaseType(BaseType.STRING) && responseDeclaration.hasCardinality(Cardinality.SINGLE)) {
						TextEntry textEntry = new TextEntry(textInteraction);
						extractTextEntrySettingsFromResponseDeclaration(textEntry, responseDeclaration, countAlternatives, mappedScore);
						String marker = "responseIdentifier=\"" + interaction.getResponseIdentifier().toString() + "\"";
						question = question.replace(marker, marker + " openolatType=\"string\"");
						if(StringHelper.containsNonWhitespace(textEntry.getSolution())) {
							question = question.replace(marker, marker + " data-qti-solution=\"" + escapeForDataQtiSolution(textEntry.getSolution()) + "\"");
						}
						entry = textEntry;
						
					} else if(responseDeclaration.hasBaseType(BaseType.FLOAT) && responseDeclaration.hasCardinality(Cardinality.SINGLE)) {
						NumericalEntry numericalEntry = new NumericalEntry(textInteraction);
						entry = numericalEntry;
						extractNumericalEntrySettings(assessmentItem, numericalEntry, responseDeclaration, countAlternatives, mappedScore);
						
						String marker = "responseIdentifier=\"" + interaction.getResponseIdentifier().toString() + "\"";
						question = question.replace(marker, marker + " openolatType=\"float\"");
						if(numericalEntry.getSolution() != null) {
							question = question.replace(marker, marker + " data-qti-solution=\"" + Double.toString(numericalEntry.getSolution()) + "\"");
						}
					}
				}
				if(entry != null) {
					responseIdentifierToTextEntry.put(interaction.getResponseIdentifier().toString(), entry);
				}
			}
		}

		boolean hasMapping = Math.abs(mappedScore.doubleValue() - (-1.0 * countAlternatives.get())) > 0.0001;
		scoreEvaluation = hasMapping ? ScoreEvaluation.perAnswer : ScoreEvaluation.allCorrectAnswers;
	}
	
	public static void extractNumericalEntrySettings(AssessmentItem item, NumericalEntry numericalEntry, ResponseDeclaration responseDeclaration,
			AtomicInteger countAlternatives, DoubleAdder mappedScore) {
		
		Double solution = null;
		CorrectResponse correctResponse = responseDeclaration.getCorrectResponse();
		if(correctResponse != null && correctResponse.getFieldValues().size() > 0) {
			List<FieldValue> fValues = correctResponse.getFieldValues();
			SingleValue sValue = fValues.get(0).getSingleValue();
			if(sValue instanceof FloatValue) {
				solution = ((FloatValue)sValue).doubleValue();
				numericalEntry.setSolution(solution);
			}
		}
		
		//search the equal
		List<ResponseRule> responseRules = item.getResponseProcessing().getResponseRules();

		a_a:
		for(ResponseRule responseRule:responseRules) {
			if(responseRule instanceof ResponseCondition) {
				ResponseCondition condition = (ResponseCondition)responseRule;
				ResponseIf responseIf = condition.getResponseIf();
				if(responseIf != null && responseIf.getExpressions().size() > 0) {
					//first is an and/equal/
					Expression potentialEqualOrAnd = responseIf.getExpressions().get(0);
					if(potentialEqualOrAnd instanceof And) {
						And and = (And)potentialEqualOrAnd;
						for(Expression potentialEqual:and.getExpressions()) {
							if(potentialEqual instanceof Equal && potentialEqual.getExpressions().size() == 2 &&
									extractNumericalEntrySettings(numericalEntry, (Equal)potentialEqual)) {
								break a_a;
							}
						}
					} else if(potentialEqualOrAnd instanceof Equal) {
						if(extractNumericalEntrySettings(numericalEntry, (Equal)potentialEqualOrAnd)) {
							//find to score as outcome value
							if(responseIf.getResponseRules() != null && responseIf.getResponseRules().size() == 1
									&& responseIf.getResponseRules().get(0) instanceof SetOutcomeValue) {
								
								SetOutcomeValue outcomeValue = (SetOutcomeValue)responseIf.getResponseRules().get(0);
								if(outcomeValue.getExpressions() != null && outcomeValue.getExpressions().size() == 1
										&& outcomeValue.getExpressions().get(0) instanceof BaseValue) {
									
									BaseValue bValue = (BaseValue)outcomeValue.getExpressions().get(0);
									SingleValue sValue = bValue.getSingleValue();
									if(sValue instanceof FloatValue) {
										FloatValue fValue = (FloatValue)sValue;
										numericalEntry.setScore(fValue.doubleValue());
										mappedScore.add(fValue.doubleValue());
										countAlternatives.incrementAndGet();
									}
								}
							}
							break a_a;
						}
					}
				}
			}
		}
		
		//toleranceMode cannot be empty
		if(numericalEntry.getToleranceMode() == null) {
			numericalEntry.setToleranceMode(ToleranceMode.EXACT);
		}
	}
	
	private static boolean extractNumericalEntrySettings(NumericalEntry numericalEntry, Equal equal) {
		Expression variableOrCorrect = equal.getExpressions().get(0);
		Expression correctOrVariable = equal.getExpressions().get(1);
		
		Correct correct = null;
		if(variableOrCorrect instanceof Correct) {
			correct = (Correct)variableOrCorrect;
		} else if(correctOrVariable instanceof Correct) {
			correct = (Correct)correctOrVariable;
		}
		
		ComplexReferenceIdentifier reponseIdentifer = ComplexReferenceIdentifier
				.assumedLegal(numericalEntry.getResponseIdentifier().toString());
		
		if(correct != null && correct.getIdentifier().equals(reponseIdentifer)) {
			numericalEntry.setToleranceMode(equal.getToleranceMode());
			List<FloatOrVariableRef> tolerances = equal.getTolerances();
			if(tolerances != null && tolerances.size() == 2) {
				double lowerTolerance = tolerances.get(0).getConstantFloatValue().doubleValue();
				numericalEntry.setLowerTolerance(lowerTolerance);
				double upperTolerance = tolerances.get(1).getConstantFloatValue().doubleValue();
				numericalEntry.setUpperTolerance(upperTolerance);
			}
			return true;
		}
		return false;
	}
	
	/**
	 * All the needed informations are in the responseDeclaration, the list of alternatives
	 * is in the mapping with case sensitivity options and score. 
	 * 
	 * @param textEntry
	 * @param responseDeclaration
	 * @param countAlternatives
	 * @param mappedScore
	 */
	public static void extractTextEntrySettingsFromResponseDeclaration(TextEntry textEntry, ResponseDeclaration responseDeclaration,
			AtomicInteger countAlternatives, DoubleAdder mappedScore) {

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
						try {
							textEntry.setScore(mapEntry.getMappedValue());
						} catch (QtiAttributeException e) {
							log.error("", e);
						}
					}
					countAlternatives.incrementAndGet();
					mappedScore.add(mapEntry.getMappedValue());
				}
				
				caseSensitive &= mapEntry.getCaseSensitive();
			}

			textEntry.setCaseSensitive(caseSensitive);
			textEntry.setAlternatives(alternatives);
		}
	}
	
	public String escapeForDataQtiSolution(String solution) {
		return StringHelper.escapeHtml(solution).replace("/", "\u2215");
	}
	
	public String unescapeDataQtiSolution(String solution) {
		return StringHelper.unescapeHtml(solution).replace("\u2215", "/");
	}

	@Override
	public QTI21QuestionType getQuestionType() {
		return questionType;
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

	public AbstractEntry getEntry(String responseIdentifier) {
		return responseIdentifierToTextEntry.get(responseIdentifier);
	}
	
	public void clearTextEntries() {
		responseIdentifierToTextEntry.clear();
	}
	
	public List<AbstractEntry> getTextEntries() {
		return new ArrayList<>(responseIdentifierToTextEntry.values());
	}
	
	public List<AbstractEntry> getOrderedTextEntries() {
		List<Interaction> interactions = assessmentItem.getItemBody().findInteractions();
		
		List<AbstractEntry> entries = getTextEntries();
		List<AbstractEntry> orderedEntries = new ArrayList<>();
		for(Interaction interaction:interactions) {
			AbstractEntry entry = getTextEntry(interaction.getResponseIdentifier().toString());
			if(entry != null) {
				orderedEntries.add(entry);
				entries.remove(entry);
			}
		}
		
		if(entries.size() > 0) {
			orderedEntries.addAll(entries);//security
		}
		return orderedEntries;
	}
	
	public AbstractEntry getTextEntry(String responseIdentifier) {
		return responseIdentifierToTextEntry.get(responseIdentifier);
	}
	
	public boolean hasNumericalInputs() {
		for(Map.Entry<String, AbstractEntry> textEntryEntry:responseIdentifierToTextEntry.entrySet()) {
			if(textEntryEntry.getValue() instanceof NumericalEntry) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasTextEntry() {
		for(Map.Entry<String, AbstractEntry> textEntryEntry:responseIdentifierToTextEntry.entrySet()) {
			if(textEntryEntry.getValue() instanceof TextEntry) {
				return true;
			}
		}
		return false;
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
		entry.setScore(1.0d);
		responseIdentifierToTextEntry.put(responseIdentifier, entry);
		return entry;
	}
	
	public NumericalEntry createNumericalEntry(String responseIdentifier) {
		NumericalEntry entry = new NumericalEntry(Identifier.parseString(responseIdentifier));
		entry.setScore(1.0d);
		responseIdentifierToTextEntry.put(responseIdentifier, entry);
		return entry;
	}

	@Override
	protected void buildResponseAndOutcomeDeclarations() {
		List<ResponseDeclaration> responseDeclarations = assessmentItem.getResponseDeclarations();
		
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
		for(Map.Entry<String, AbstractEntry> textEntryEntry:responseIdentifierToTextEntry.entrySet()) {
			AbstractEntry entry = textEntryEntry.getValue();
			if(entry instanceof TextEntry) {
				TextEntry textEntry = (TextEntry)entry;
				if( textEntry.getSolution() != null) {
					Double score = -1.0d; 
					if(scoreEvaluation == ScoreEvaluation.perAnswer) {
						score = textEntry.getScore();
					}
					
					ResponseDeclaration responseDeclaration = createTextEntryResponseDeclaration(assessmentItem,
							textEntry.getResponseIdentifier(), textEntry.getSolution(),
							score, textEntry.isCaseSensitive(), textEntry.getAlternatives());
					responseDeclarations.add(responseDeclaration);
				}
			} else if(entry instanceof NumericalEntry) {
				NumericalEntry textEntry = (NumericalEntry)entry;
				if( textEntry.getSolution() != null) {
					ResponseDeclaration responseDeclaration = createNumericalEntryResponseDeclaration(assessmentItem,
							textEntry.getResponseIdentifier(), textEntry.getSolution());
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
		List<String> usedResponseIdentifiers = new ArrayList<>(interactions.size());
		for(Interaction interaction:interactions) {
			if(interaction instanceof TextEntryInteraction && interaction.getResponseIdentifier() != null) {
				TextEntryInteraction textEntryInteraction = (TextEntryInteraction)interaction;
				String responseIdentifier = interaction.getResponseIdentifier().toString();
				AbstractEntry entry = responseIdentifierToTextEntry.get(responseIdentifier);
				if(entry != null) {
					textEntryInteraction.setPlaceholderText(entry.getPlaceholder());
					textEntryInteraction.setExpectedLength(entry.getExpectedLength());
				}
				usedResponseIdentifiers.add(responseIdentifier);
			}
		}
		
		List<String> mappedResponseIdentifiers = new ArrayList<>(responseIdentifierToTextEntry.keySet());
		mappedResponseIdentifiers.removeAll(usedResponseIdentifiers);
		for(String mappedResponseIdentifier:mappedResponseIdentifiers) {
			responseIdentifierToTextEntry.remove(mappedResponseIdentifier);
		}
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
				<and>
					<match>
						<value>-1.0</value>
						<correct identifier="RESPONSE_1" />
					</match>
					<equal toleranceMode="relative" tolerance="0.1 0.1" includeLowerBound="true" includeUpperBound="true">
						<correct identifier="RESPONSE_2" />
						<variable identifier="RESPONSE_2" />
					</equal>
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
			</responseIf>
		</responseCondition>
		*/
		
		// add condition
		ResponseCondition rule = new ResponseCondition(assessmentItem.getResponseProcessing());
		responseRules.add(0, rule);

		{// match all
			ResponseIf responseElseIf = new ResponseIf(rule);
			rule.setResponseIf(responseElseIf);
			//rule.getResponseElseIfs().add(responseElseIf);
			
			And and = new And(responseElseIf);
			responseElseIf.setExpression(and);
			
			for(Map.Entry<String, AbstractEntry> textEntryEntry:responseIdentifierToTextEntry.entrySet()) {
				AbstractEntry abstractEntry = textEntryEntry.getValue();
	
				if(abstractEntry instanceof TextEntry) {
					Match match = new Match(and);
					and.getExpressions().add(match);
					
					TextEntry textEntry = (TextEntry)abstractEntry;
					BaseValue variable = new BaseValue(match);
					variable.setBaseTypeAttrValue(BaseType.FLOAT);
					variable.setSingleValue(new FloatValue(-1.0d));
					match.getExpressions().add(variable);
					
					MapResponse correct = new MapResponse(match);
					correct.setIdentifier(textEntry.getResponseIdentifier());
					match.getExpressions().add(correct);
					
				} else if(abstractEntry instanceof NumericalEntry) {
					NumericalEntry numericalEntry = (NumericalEntry)abstractEntry;
					Equal equal = new Equal(and);
					equal.setToleranceMode(numericalEntry.getToleranceMode());
					if(numericalEntry.getLowerTolerance() != null && numericalEntry.getUpperTolerance() != null) {
						List<FloatOrVariableRef> tolerances = new ArrayList<>();
						tolerances.add(new FloatOrVariableRef(numericalEntry.getLowerTolerance().doubleValue()));
						tolerances.add(new FloatOrVariableRef(numericalEntry.getUpperTolerance().doubleValue()));
						equal.setTolerances(tolerances);
					}
					equal.setIncludeLowerBound(Boolean.TRUE);
					equal.setIncludeUpperBound(Boolean.TRUE);
					and.getExpressions().add(equal);
					
					ComplexReferenceIdentifier responseIdentifier = ComplexReferenceIdentifier
							.assumedLegal(numericalEntry.getResponseIdentifier().toString());
					
					Correct correct = new Correct(equal);
					correct.setIdentifier(responseIdentifier);
					equal.getExpressions().add(correct);

					Variable variable = new Variable(equal);
					variable.setIdentifier(responseIdentifier);
					equal.getExpressions().add(variable);
				}
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

	private void buildMainScoreRulePerAnswer(List<OutcomeDeclaration> outcomeDeclarations, List<ResponseRule> responseRules) {
		/*
		<setOutcomeValue identifier="SCORE_RESPONSE_1">
			<mapResponse identifier="RESPONSE_1" />
		</setOutcomeValue>
		*/
		
		/*
		<responseCondition>
			<responseIf>
				<equal toleranceMode="absolute" tolerance="2.0 2.0" includeLowerBound="true" includeUpperBound="true">
					<variable identifier="RESPONSE_3"/>
					<correct identifier="RESPONSE_3"/>
				</equal>
				<setOutcomeValue identifier="SCORE_RESPONSE_3">
					<baseValue baseType="float">3.0</baseValue>
				</setOutcomeValue>
			</responseIf>
	    </responseCondition>
		 */

		int count = 0;
		
		for(Map.Entry<String, AbstractEntry> textEntryEntry:responseIdentifierToTextEntry.entrySet()) {
			AbstractEntry entry = textEntryEntry.getValue();
			String scoreIdentifier = "SCORE_" + entry.getResponseIdentifier().toString();

			if(entry instanceof TextEntry) {//outcome mapResonse
				SetOutcomeValue mapOutcomeValue = new SetOutcomeValue(assessmentItem.getResponseProcessing());
				responseRules.add(count++, mapOutcomeValue);
				mapOutcomeValue.setIdentifier(Identifier.parseString(scoreIdentifier));
				
				MapResponse mapResponse = new MapResponse(mapOutcomeValue);
				mapResponse.setIdentifier(entry.getResponseIdentifier());
				mapOutcomeValue.setExpression(mapResponse);
				
			} else if(entry instanceof NumericalEntry) {
				NumericalEntry numericalEntry = (NumericalEntry)entry;
				
				ResponseCondition rule = new ResponseCondition(assessmentItem.getResponseProcessing());
				responseRules.add(count++, rule);
				
				ResponseIf responseIf = new ResponseIf(rule);
				rule.setResponseIf(responseIf);
				
				Equal equal = new Equal(responseIf);
				equal.setToleranceMode(numericalEntry.getToleranceMode());
				if(numericalEntry.getLowerTolerance() != null && numericalEntry.getUpperTolerance() != null) {
					List<FloatOrVariableRef> tolerances = new ArrayList<>();
					tolerances.add(new FloatOrVariableRef(numericalEntry.getLowerTolerance().doubleValue()));
					tolerances.add(new FloatOrVariableRef(numericalEntry.getUpperTolerance().doubleValue()));
					equal.setTolerances(tolerances);
				}
				equal.setIncludeLowerBound(Boolean.TRUE);
				equal.setIncludeUpperBound(Boolean.TRUE);
				responseIf.getExpressions().add(equal);
				
				ComplexReferenceIdentifier responseIdentifier = ComplexReferenceIdentifier
						.assumedLegal(numericalEntry.getResponseIdentifier().toString());
				
				Correct correct = new Correct(equal);
				correct.setIdentifier(responseIdentifier);
				equal.getExpressions().add(correct);
				
				Variable variable = new Variable(equal);
				variable.setIdentifier(responseIdentifier);
				equal.getExpressions().add(variable);
				
				SetOutcomeValue mapOutcomeValue = new SetOutcomeValue(responseIf);
				responseIf.getResponseRules().add(mapOutcomeValue);
				mapOutcomeValue.setIdentifier(Identifier.parseString(scoreIdentifier));
				
				BaseValue correctValue = new BaseValue(mapOutcomeValue);
				correctValue.setBaseTypeAttrValue(BaseType.FLOAT);
				correctValue.setSingleValue(new FloatValue(entry.getScore()));
				mapOutcomeValue.setExpression(correctValue);
			}
		}
		

		/*
		<setOutcomeValue identifier="SCORE">
			<sum>
				<variable identifier="SCORE_RESPONSE_1" />
				<variable identifier="MINSCORE_RESPONSE_1" />
				<variable identifier="SCORE_RESPONSE_2" />
				<variable identifier="MINSCORE_RESPONSE_2" />
			</sum>
		</setOutcomeValue>
		*/
		{
			SetOutcomeValue scoreOutcome = new SetOutcomeValue(assessmentItem.getResponseProcessing());
			scoreOutcome.setIdentifier(QTI21Constants.SCORE_IDENTIFIER);
			responseRules.add(count++, scoreOutcome);
			
			Sum sum = new Sum(scoreOutcome);
			scoreOutcome.setExpression(sum);
			
			for(Map.Entry<String, AbstractEntry> textEntryEntry:responseIdentifierToTextEntry.entrySet()) {
				AbstractEntry textEntry = textEntryEntry.getValue();
				
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
		
		if(correctFeedback != null || incorrectFeedback != null) {
			SetOutcomeValue incorrectOutcomeValue = new SetOutcomeValue(assessmentItem.getResponseProcessing());
			incorrectOutcomeValue.setIdentifier(QTI21Constants.FEEDBACKBASIC_IDENTIFIER);
			
			BaseValue correctValue = new BaseValue(incorrectOutcomeValue);
			correctValue.setBaseTypeAttrValue(BaseType.IDENTIFIER);
			correctValue.setSingleValue(QTI21Constants.INCORRECT_IDENTIFIER_VALUE);
			incorrectOutcomeValue.setExpression(correctValue);
			
			responseRules.add(count++, incorrectOutcomeValue);
		}
	}
	
	public static abstract class AbstractEntry {
		
		private Identifier responseIdentifier;
		private String placeholder;
		private Integer expectedLength;

		private Double score;
		
		public AbstractEntry(Identifier responseIdentifier) {
			this.responseIdentifier = responseIdentifier;
		}
		
		public AbstractEntry(Identifier responseIdentifier, String placeholder, Integer expectedLength) {
			this.responseIdentifier = responseIdentifier;
			this.placeholder = placeholder;
			this.expectedLength = expectedLength;
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

		public Double getScore() {
			return score;
		}

		public void setScore(Double score) {
			this.score = score;
		}
		
		public abstract boolean match(String response);
	}
	
	public static class NumericalEntry extends AbstractEntry {
		
		private Double solution;
		
		private Double lowerTolerance;
		private Double upperTolerance;
		private ToleranceMode toleranceMode;
		
		public NumericalEntry(Identifier responseIdentifier) {
			super(responseIdentifier);
		}
		
		public NumericalEntry(TextEntryInteraction entry) {
			super(entry.getResponseIdentifier(), entry.getPlaceholderText(), entry.getExpectedLength());
		}

		public Double getSolution() {
			return solution;
		}

		public void setSolution(Double solution) {
			this.solution = solution;
		}

		public Double getLowerTolerance() {
			return lowerTolerance;
		}

		public void setLowerTolerance(Double lowerTolerance) {
			this.lowerTolerance = lowerTolerance;
		}

		public Double getUpperTolerance() {
			return upperTolerance;
		}

		public void setUpperTolerance(Double upperTolerance) {
			this.upperTolerance = upperTolerance;
		}

		public ToleranceMode getToleranceMode() {
			return toleranceMode;
		}

		public void setToleranceMode(ToleranceMode toleranceMode) {
			this.toleranceMode = toleranceMode;
		}

		@Override
		public boolean match(String response) {
			if(StringHelper.containsNonWhitespace(response)) {
				try {
					double firstNumber = Double.parseDouble(response);
					return match(firstNumber);
				} catch(NumberFormatException nfe) {
					if(response.indexOf(',') >= 0) {//allow , instead of .
	                    try {
							double firstNumber = Double.parseDouble(response.replace(',', '.'));
							return match(firstNumber);
						} catch (final NumberFormatException e1) {
							//format can happen
						} catch (Exception e) {
							log.error("", e);
						}
	            	}
				} catch (Exception e) {
					log.error("", e);
				}
			}
			return false;
		}
		
		private boolean match(double answer) {
			double lTolerance = lowerTolerance == null ? 0.0d : lowerTolerance.doubleValue();
			double uTolerance = upperTolerance == null ? 0.0d : upperTolerance.doubleValue();
			if(toleranceMode == ToleranceMode.ABSOLUTE && (lTolerance < 0.0d || uTolerance < 0.0d)) {
				return false;
			}
			return toleranceMode.isEqual(solution, answer,
					lTolerance, uTolerance,
					true, true);
		}
	}
	
	public static class TextEntry extends AbstractEntry {

		private boolean caseSensitive;
		
		private String solution;
		private List<TextEntryAlternative> alternatives;
		
		public TextEntry(Identifier responseIdentifier) {
			super(responseIdentifier);
		}
		
		public TextEntry(TextEntryInteraction entry) {
			super(entry.getResponseIdentifier(), entry.getPlaceholderText(), entry.getExpectedLength());
		}
		
		public boolean isCaseSensitive() {
			return caseSensitive;
		}

		public void setCaseSensitive(boolean caseSensitive) {
			this.caseSensitive = caseSensitive;
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

		public void setAlternatives(List<TextEntryAlternative> alternatives) {
			this.alternatives = alternatives;
		}
		
		public void addAlternative(String alternative, double points) {
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
		
		/**
		 * Quick method to find if a string match the correct responses of
		 * the text entry.
		 * 
		 * @param response
		 * @return
		 */
		@Override
		public boolean match(String response) {
			if(match(response, solution)) {
				return true;
			}
			
			for(TextEntryAlternative textEntryAlternative:alternatives) {
				if(match(response, textEntryAlternative.getAlternative())) {
					return true;
				}
			}
			return false;
		}

		private boolean match(String response, String alternative) {
			if(caseSensitive) {
				if(alternative.equals(response)
						|| (response != null && alternative.trim().equals(response.trim()))) {
					return true;
				}
			} else if(alternative.equalsIgnoreCase(response)
					|| (response != null && alternative.trim().equalsIgnoreCase(response.trim()))) {
				return true;
			}
			return false;
		}
	}
	
	public static class TextEntryAlternative {
		
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
	
	public enum EntryType {
		text,
		numerical
	}
}
