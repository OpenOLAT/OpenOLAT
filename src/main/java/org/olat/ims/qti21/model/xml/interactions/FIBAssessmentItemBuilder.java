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

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.render.StringOutput;
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
import uk.ac.ed.ph.jqtiplus.node.expression.ExpressionParent;
import uk.ac.ed.ph.jqtiplus.node.expression.general.BaseValue;
import uk.ac.ed.ph.jqtiplus.node.expression.general.Correct;
import uk.ac.ed.ph.jqtiplus.node.expression.general.MapResponse;
import uk.ac.ed.ph.jqtiplus.node.expression.general.Variable;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.And;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Equal;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.IsNull;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Match;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Not;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Or;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.StringMatch;
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
	private boolean allowDuplicatedAnswers;
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
		extractAllowDuplicatesAnswers();
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
						question = question.replace(marker, marker + " data-qti-gap-type=\"string\"");
						if(StringHelper.containsNonWhitespace(textEntry.getSolution())) {
							question = question.replace(marker, marker + " data-qti-solution=\"" + escapeForDataQtiSolution(textEntry.getSolution()) + "\"");
						}
						entry = textEntry;
						
					} else if(responseDeclaration.hasBaseType(BaseType.FLOAT) && responseDeclaration.hasCardinality(Cardinality.SINGLE)) {
						NumericalEntry numericalEntry = new NumericalEntry(textInteraction);
						entry = numericalEntry;
						extractNumericalEntrySettings(assessmentItem, numericalEntry, responseDeclaration, countAlternatives, mappedScore);
						
						String marker = "responseIdentifier=\"" + interaction.getResponseIdentifier().toString() + "\"";
						question = question.replace(marker, marker + " data-qti-gap-type=\"float\"");
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
		if(correctResponse != null && !correctResponse.getFieldValues().isEmpty()) {
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
				if(responseIf != null && !responseIf.getExpressions().isEmpty()) {
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
		if(correctResponse != null && !correctResponse.getFieldValues().isEmpty()) {
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
	
	public void extractAllowDuplicatesAnswers() {
		allowDuplicatedAnswers = true;
		
		List<ResponseRule> responseRules = assessmentItem.getResponseProcessing().getResponseRules();
		for(ResponseRule responseRule:responseRules) {
			if(responseRule instanceof ResponseCondition) {
				ResponseCondition responseCondition = (ResponseCondition)responseRule;
				ResponseIf responseIf = responseCondition.getResponseIf();
				if(responseIf != null && !responseIf.getExpressions().isEmpty()) {
					if(responseIf.getExpressions().size() == 1 && responseIf.getExpressions().get(0) instanceof Not) {
						// check if the not is our check for duplication in case of score per answers
						if(isNotForDuplicatesAnswers((Not)responseIf.getExpressions().get(0))) {
							allowDuplicatedAnswers = false;
							break;
						}
					} else if(responseIf.getExpressions().get(0) instanceof And) {
						// check if the and contains the not for duplication if score is for all answers
						And and = (And)responseIf.getExpressions().get(0);
					
						int numOfNot = 0;
						int numOfMatch = 0;
						for(Expression expression:and.getExpressions()) {
							if(expression instanceof Match || expression instanceof Equal) {
								numOfMatch++;
							} else if(expression instanceof Not && isNotForDuplicatesAnswers((Not)expression)) {
								numOfNot++;
							}
						}
					
						if(numOfMatch > 0 && numOfNot > 0) {
							allowDuplicatedAnswers = false;
							break;
						}
					}
				}
			}
		}
	}
	
	private boolean isNotForDuplicatesAnswers(Not not) {	
		if(not.getChildren().size() == 1 && not.getChildren().get(0) instanceof Or) {
			Or or = (Or)not.getChildren().get(0);
			if(isStringMatchOrEquals(or)) {
				return true;
			} else {
				for(Expression expression:or.getExpressions()) {
					if(expression instanceof And && isStringMatchOrEquals(expression)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private boolean isStringMatchOrEquals(Expression parent) {
		for(Expression expression:parent.getExpressions()) {
			if(expression instanceof StringMatch || expression instanceof Equal) {
				List<Expression> variables = expression.getExpressions();
				if(variables.size() == 2
						&& variables.get(0) instanceof Variable
						&& variables.get(1) instanceof Variable) {
					Variable var1 = (Variable)variables.get(0);
					Variable var2 = (Variable)variables.get(1);
					String responseIdentifier1 = var1.getIdentifier().toString();
					String responseIdentifier2 = var2.getIdentifier().toString();
					return responseIdentifierToTextEntry.containsKey(responseIdentifier1)
							&& responseIdentifierToTextEntry.containsKey(responseIdentifier2);
				}
			}
		}
		return false;
	}

	@Override
	public QTI21QuestionType getQuestionType() {
		return questionType;
	}
	
	public void setQuestionType(QTI21QuestionType type) {
		if(type == QTI21QuestionType.numerical || type == QTI21QuestionType.fib) {
			this.questionType = type;
		}
	}
	
	@Override
	public String getQuestion() {
		return question;
	}
	
	@Override
	public void setQuestion(String html) {
		this.question = html;
	}
	
	public boolean isAllowDuplicatedAnswers() {
		return allowDuplicatedAnswers;
	}

	public void setAllowDuplicatedAnswers(boolean allowDuplicatedAnswers) {
		this.allowDuplicatedAnswers = allowDuplicatedAnswers;
	}

	public ScoreEvaluation getScoreEvaluationMode() {
		return scoreEvaluation;
	}

	public void setScoreEvaluationMode(ScoreEvaluation scoreEvaluation) {
		this.scoreEvaluation = scoreEvaluation;
	}
	
	/**
	 * This method only applies to score per answer.
	 * 
	 * @return true if some variant hasn't the same score as the main response.
	 */
	public boolean alternativesWithSpecificScore() {
		for(Map.Entry<String, AbstractEntry> entry:responseIdentifierToTextEntry.entrySet()) {
			AbstractEntry e = entry.getValue();
			if(e instanceof TextEntry) {
				TextEntry textEntry = (TextEntry)e;
				Double score = textEntry.getScore();
				if(textEntry.getAlternatives() != null && !textEntry.getAlternatives().isEmpty()) {
					for(TextEntryAlternative alternative:textEntry.getAlternatives()) {
						double altScore = alternative.getScore();
						if(score != null && score.doubleValue() != altScore) {
							return true;
						}
					}
				}
			}
		}
		return false;
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
		
		if(!entries.isEmpty()) {
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
					ResponseDeclaration	responseDeclaration = createTextEntryResponseDeclaration(assessmentItem,
								textEntry.getResponseIdentifier(), textEntry.getSolution(),
								score, textEntry.isCaseSensitive(), textEntry.getAlternatives(),
								scoreEvaluation == ScoreEvaluation.perAnswer);
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
			ResponseIf responseIf = new ResponseIf(rule);
			rule.setResponseIf(responseIf);
			
			And and = new And(responseIf);
			responseIf.setExpression(and);
			
			int numOfTextEntry = 0;
			
			for(Map.Entry<String, AbstractEntry> textEntryEntry:responseIdentifierToTextEntry.entrySet()) {
				AbstractEntry abstractEntry = textEntryEntry.getValue();
	
				if(abstractEntry instanceof TextEntry) {
					numOfTextEntry++;
					
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
					if(numericalEntry.getToleranceMode() == null) {
						equal.setToleranceMode(ToleranceMode.EXACT);
					} else {
					
						equal.setToleranceMode(numericalEntry.getToleranceMode());
					}
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
			
			if(!allowDuplicatedAnswers && numOfTextEntry > 1) {
				/*
				<not>
					<or>
						<stringMatch caseSensitive="false" substring="false">
							<variable identifier="RESPONSE_2"/>
							<variable identifier="RESPONSE_1"/>
						</stringMatch>
					</or>
				</not>
				<not>
					<or>
						<stringMatch caseSensitive="false" substring="false">
							<variable identifier="RESPONSE_3"/>
							<variable identifier="RESPONSE_2"/>
						</stringMatch>
						<stringMatch caseSensitive="false" substring="false">
							<variable identifier="RESPONSE_3"/>
							<variable identifier="RESPONSE_1"/>
						</stringMatch>
					</or>
				</not>
				 */

				List<String> responseIdentifiers = new ArrayList<>(responseIdentifierToTextEntry.keySet());
				int numOfResponseIdentifiers = responseIdentifiers.size();
				for(int i=1; i<numOfResponseIdentifiers; i++) {
					Not not = new Not(and);
					Or or = new Or(not);
					not.getExpressions().add(or);
					
					for(int j=i; j-->0; ) {
						match(responseIdentifiers.get(i), responseIdentifiers.get(j), or);
					}
					
					// in case of a mix numerical and text entries
					if(!or.getExpressions().isEmpty()) {
						and.getExpressions().add(not);
					}
				}
			}
			
			{// outcome max score -> score
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
	
	/**
	 * The idea (null && null) or (!null && !null && match).
	 * StringMatch considers only strings, but an empty response is not
	 * an empty string but a null object.<br>
	 * This block returns true if both responses are null or
	 * both responses are strings and equals.
	 * 
	 * @param responseIdentifier1 The first response identifier
	 * @param responseIdentifier2 The second response identifier
	 * @param parentOr The parent element
	 */
	private void match(String responseIdentifier1, String responseIdentifier2, ExpressionParent parentOr) {
		AbstractEntry entry1 = responseIdentifierToTextEntry.get(responseIdentifier1);
		AbstractEntry entry2 = responseIdentifierToTextEntry.get(responseIdentifier2);
		if(entry1 instanceof TextEntry && entry2 instanceof TextEntry
				&& shareSomeAlternatives((TextEntry)entry1, (TextEntry)entry2)) {
			
			{
				And and = new And(parentOr);
				parentOr.getExpressions().add(and);
				IsNull null1 = new IsNull(and);
				and.getExpressions().add(null1);
				Variable var1 = new Variable(null1);
				var1.setIdentifier(ComplexReferenceIdentifier.parseString(responseIdentifier1));
				null1.getExpressions().add(var1);
				
				IsNull null2 = new IsNull(and);
				and.getExpressions().add(null2);
				Variable var2 = new Variable(null2);
				var2.setIdentifier(ComplexReferenceIdentifier.parseString(responseIdentifier2));
				null2.getExpressions().add(var2);
			}
			
			{
				And and = new And(parentOr);
				parentOr.getExpressions().add(and);
				Not not1 = new Not(and);
				and.getExpressions().add(not1);
				IsNull null1 = new IsNull(not1);
				not1.getExpressions().add(null1);
				Variable var1 = new Variable(null1);
				var1.setIdentifier(ComplexReferenceIdentifier.parseString(responseIdentifier1));
				null1.getExpressions().add(var1);
				
				Not not2 = new Not(and);
				and.getExpressions().add(not2);
				IsNull null2 = new IsNull(not2);
				not2.getExpressions().add(null2);
				Variable var2 = new Variable(null2);
				var2.setIdentifier(ComplexReferenceIdentifier.parseString(responseIdentifier2));
				null2.getExpressions().add(var2);
				
				StringMatch match = stringMatch(responseIdentifier1, responseIdentifier2, and);
				and.getExpressions().add(match);
			}
		}
	}
	
	public boolean entriesSharesAlternatives() {
		List<String> responseIdentifiers = new ArrayList<>(responseIdentifierToTextEntry.keySet());
		int numOfResponseIdentifiers = responseIdentifiers.size();
		for(int i=1; i<numOfResponseIdentifiers; i++) {
			for(int j=i; j-->0; ) {
				String responseIdentifier1 = responseIdentifiers.get(i);
				String responseIdentifier2 = responseIdentifiers.get(j);
				AbstractEntry entry1 = responseIdentifierToTextEntry.get(responseIdentifier1);
				AbstractEntry entry2 = responseIdentifierToTextEntry.get(responseIdentifier2);
				if(entry1 instanceof TextEntry && entry2 instanceof TextEntry
						&& shareSomeAlternatives((TextEntry)entry1, (TextEntry)entry2)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean shareSomeAlternatives(TextEntry entry1, TextEntry entry2) {
		List<String> alternatives1 = alternativesToString(entry1);
		List<String> alternatives2 = alternativesToString(entry2);
		for(String alt1:alternatives1) {
			for(String alt2:alternatives2) {
				if(alt1.compareToIgnoreCase(alt2) == 0) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static List<String> alternativesToString(TextEntry entry) {
		List<String> alternatives = new ArrayList<>();
		if(entry.getSolution() != null) {
			alternatives.add(entry.getSolution());
		}
		if(entry.getAlternatives() != null) {
			for(TextEntryAlternative alternative:entry.getAlternatives()) {
				alternatives.add(alternative.getAlternative());
			}
		}
		return alternatives;
	}
	
	/*
	<stringMatch caseSensitive="false" substring="false">
		<variable identifier="RESPONSE_3"/>
		<variable identifier="RESPONSE_2"/>
	</stringMatch>
	 */
	private StringMatch stringMatch(String responseIdentifier1, String responseIdentifier2, ExpressionParent parent) {
		StringMatch stringMatch = new StringMatch(parent);
		stringMatch.setCaseSensitive(Boolean.FALSE);
		stringMatch.setSubString(Boolean.FALSE);
		
		Variable variable1 = new Variable(stringMatch);
		variable1.setIdentifier(ComplexReferenceIdentifier.parseString(responseIdentifier1));
		Variable variable2 = new Variable(stringMatch);
		variable2.setIdentifier(ComplexReferenceIdentifier.parseString(responseIdentifier2));
		
		stringMatch.getExpressions().add(variable1);
		stringMatch.getExpressions().add(variable2);
		
		return stringMatch;
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
		List<String> responseIdentifiers = new ArrayList<>(responseIdentifierToTextEntry.keySet());
		for(count = 0; count <responseIdentifiers.size(); count++) {
			String responseStringIdentifier = responseIdentifiers.get(count);
			AbstractEntry entry = responseIdentifierToTextEntry.get(responseStringIdentifier);
			String scoreIdentifier = "SCORE_" + entry.getResponseIdentifier().toString();
			buildScoreRulePerAnswer(count, entry, Identifier.parseString(scoreIdentifier), responseIdentifiers, responseRules);
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
	
	private void buildScoreRulePerAnswer(int count, AbstractEntry entry, Identifier scoreIdentifier,
			List<String> responseIdentifiers, List<ResponseRule> responseRules) {
		if(entry instanceof TextEntry) {
			buildScoreRulePerTextAnswer(count, (TextEntry)entry, scoreIdentifier, responseIdentifiers, responseRules);
		} else if(entry instanceof NumericalEntry) {
			buildScoreRulePerNumericalAnswer(count, (NumericalEntry)entry, scoreIdentifier, responseRules);
		}
	}
	
	/**
	 * Outcome map response.
	 * 
	 * @param count Current position of the rule
	 * @param entry The text entry
	 * @param scoreIdentifier The identifier of the score
	 * @param responseRules The list of response rules
	 */
	private void buildScoreRulePerTextAnswer(int count, TextEntry entry, Identifier scoreIdentifier, List<String> responseIdentifiers, List<ResponseRule> responseRules) {
		if(!allowDuplicatedAnswers && count > 0) {
			ResponseCondition rule = new ResponseCondition(assessmentItem.getResponseProcessing());

			ResponseIf responseIf = new ResponseIf(rule);
			rule.setResponseIf(responseIf);

			Not not = new Not(responseIf);
			Or or = new Or(not);
			not.getExpressions().add(or);
			
			for(int j=count; j-->0; ) {
				match(responseIdentifiers.get(count), responseIdentifiers.get(j), or);
			}
			
			// in case of a mix numerical and text entries
			if(or.getExpressions().isEmpty()) {
				SetOutcomeValue mapOutcomeValue = new SetOutcomeValue(assessmentItem.getResponseProcessing());
				responseRules.add(count, mapOutcomeValue);
				mapOutcomeValue.setIdentifier(scoreIdentifier);
				
				MapResponse mapResponse = new MapResponse(mapOutcomeValue);
				mapResponse.setIdentifier(entry.getResponseIdentifier());
				mapOutcomeValue.setExpression(mapResponse);
			} else {
				responseIf.getExpressions().add(not);
				responseRules.add(count, rule);
				
				SetOutcomeValue mapOutcomeValue = new SetOutcomeValue(responseIf);
				responseIf.getResponseRules().add(mapOutcomeValue);
				
				mapOutcomeValue.setIdentifier(scoreIdentifier);
				
				MapResponse mapResponse = new MapResponse(mapOutcomeValue);
				mapResponse.setIdentifier(entry.getResponseIdentifier());
				mapOutcomeValue.setExpression(mapResponse);
			}
		} else {
			SetOutcomeValue mapOutcomeValue = new SetOutcomeValue(assessmentItem.getResponseProcessing());
			responseRules.add(count, mapOutcomeValue);
			mapOutcomeValue.setIdentifier(scoreIdentifier);
			
			MapResponse mapResponse = new MapResponse(mapOutcomeValue);
			mapResponse.setIdentifier(entry.getResponseIdentifier());
			mapOutcomeValue.setExpression(mapResponse);
		}
	}

	private void buildScoreRulePerNumericalAnswer(int count, NumericalEntry numericalEntry, Identifier scoreIdentifier, List<ResponseRule> responseRules) {

		ResponseCondition rule = new ResponseCondition(assessmentItem.getResponseProcessing());
		responseRules.add(count, rule);
		
		ResponseIf responseIf = new ResponseIf(rule);
		rule.setResponseIf(responseIf);
		
		Equal equal = new Equal(responseIf);
		if(numericalEntry.getToleranceMode() == null) {
			equal.setToleranceMode(ToleranceMode.EXACT);
		} else {
			equal.setToleranceMode(numericalEntry.getToleranceMode());
		}
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
		mapOutcomeValue.setIdentifier(scoreIdentifier);
		
		BaseValue correctValue = new BaseValue(mapOutcomeValue);
		correctValue.setBaseTypeAttrValue(BaseType.FLOAT);
		correctValue.setSingleValue(new FloatValue(numericalEntry.getScore()));
		mapOutcomeValue.setExpression(correctValue);
	}
	
	public abstract static class AbstractEntry {
		
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
		
		public Double getAlternativeScore(String text) {
			Double altScore = null;
			if(alternatives != null) {
				for(TextEntryAlternative alternative:alternatives) {
					if(text != null && text.equals(alternative.getAlternative())) {
						altScore = Double.valueOf(alternative.getScore());
						break;
					}
				}
			}
			
			return altScore;
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
			if(alternatives != null && !alternatives.isEmpty()) {
				for(TextEntryAlternative textEntryAlternative:alternatives) {
					if(match(response, textEntryAlternative.getAlternative())) {
						return true;
					}
				}
			}
			return false;
		}

		private boolean match(String response, String alternative) {
			if(caseSensitive) {
				if((alternative != null && alternative.equals(response))
						|| (alternative != null && response != null && alternative.trim().equals(response.trim()))) {
					return true;
				}
			} else if((alternative != null && alternative.equalsIgnoreCase(response))
					|| (alternative != null && response != null && alternative.trim().equalsIgnoreCase(response.trim()))) {
				return true;
			}
			return false;
		}
	}
	
	public static class TextEntryAlternative {
		
		private String alternative;
		private double score;
		
		public TextEntryAlternative() {
			//
		}

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
