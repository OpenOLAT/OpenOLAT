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
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.appendInlineChoice;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.appendInlineChoiceInteraction;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.appendMapping;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.createInlineChoiceResponseDeclaration;
import static org.olat.ims.qti21.model.xml.AssessmentItemFactory.createResponseProcessing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.model.IdentifierGenerator;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.AssessmentItemFactory;
import org.olat.ims.qti21.model.xml.QtiNodesExtractor;
import org.olat.ims.qti21.model.xml.interactions.SimpleChoiceAssessmentItemBuilder.ScoreEvaluation;

import uk.ac.ed.ph.jqtiplus.attribute.ForeignAttribute;
import uk.ac.ed.ph.jqtiplus.node.content.ItemBody;
import uk.ac.ed.ph.jqtiplus.node.content.basic.Block;
import uk.ac.ed.ph.jqtiplus.node.content.basic.TextRun;
import uk.ac.ed.ph.jqtiplus.node.content.variable.TextOrVariable;
import uk.ac.ed.ph.jqtiplus.node.expression.general.BaseValue;
import uk.ac.ed.ph.jqtiplus.node.expression.general.Correct;
import uk.ac.ed.ph.jqtiplus.node.expression.general.MapResponse;
import uk.ac.ed.ph.jqtiplus.node.expression.general.Variable;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.And;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Match;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Sum;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.CorrectResponse;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.InlineChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.InlineChoice;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.MapEntry;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.Mapping;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.ResponseDeclaration;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseCondition;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseElse;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseIf;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseProcessing;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseRule;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.SetOutcomeValue;
import uk.ac.ed.ph.jqtiplus.node.item.template.declaration.TemplateDeclaration;
import uk.ac.ed.ph.jqtiplus.node.outcome.declaration.OutcomeDeclaration;
import uk.ac.ed.ph.jqtiplus.node.shared.FieldValue;
import uk.ac.ed.ph.jqtiplus.node.shared.declaration.DefaultValue;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.types.ComplexReferenceIdentifier;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.BaseType;
import uk.ac.ed.ph.jqtiplus.value.Cardinality;
import uk.ac.ed.ph.jqtiplus.value.IdentifierValue;
import uk.ac.ed.ph.jqtiplus.value.SingleValue;
import uk.ac.ed.ph.jqtiplus.value.StringValue;
import uk.ac.ed.ph.jqtiplus.value.Value;

/**
 * 
 * Initial date: 22 juin 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InlineChoiceAssessmentItemBuilder extends AssessmentItemBuilder {

	private static final Logger log = Tracing.createLoggerFor(InlineChoiceAssessmentItemBuilder.class);
	
	private String question;
	private ScoreEvaluation scoreEvaluation;
	
	private List<GlobalInlineChoice> globalInlineChoices;
	private List<InlineChoiceInteractionEntry> inlineChoiceInteractions;
	
	public InlineChoiceAssessmentItemBuilder(String title, QtiSerializer qtiSerializer) {
		super(createAssessmentItem(title), qtiSerializer);
	}
	
	public InlineChoiceAssessmentItemBuilder(AssessmentItem assessmentItem, QtiSerializer qtiSerializer) {
		super(assessmentItem, qtiSerializer);
	}
	
	private static AssessmentItem createAssessmentItem(String title) {
		AssessmentItem assessmentItem = AssessmentItemFactory.createAssessmentItem(QTI21QuestionType.inlinechoice, title);
		
		//define the response
		Identifier responseDeclarationId = Identifier.assumedLegal("RESPONSE_1");
		Identifier correctResponseId = IdentifierGenerator.newAsIdentifier("inline");
		ResponseDeclaration responseDeclaration = createInlineChoiceResponseDeclaration(assessmentItem, responseDeclarationId,
				correctResponseId);
		assessmentItem.getNodeGroups().getResponseDeclarationGroup().getResponseDeclarations().add(responseDeclaration);

		//outcomes
		appendDefaultOutcomeDeclarations(assessmentItem, 1.0d);
		
		ItemBody itemBody = appendDefaultItemBody(assessmentItem);
		InlineChoiceInteraction interaction = appendInlineChoiceInteraction(itemBody, responseDeclarationId);
		appendInlineChoice(interaction, "Gap", correctResponseId);
		
		//response processing
		ResponseProcessing responseProcessing = createResponseProcessing(assessmentItem, responseDeclarationId);
		assessmentItem.getNodeGroups().getResponseProcessingGroup().setResponseProcessing(responseProcessing);
		return assessmentItem;
	}
	
	@Override
	protected void extract() {
		super.extract();
		extractTemplateDeclarationsForGlobal();
		extractQuestions();
		extractInteractions();
		extractInlineChoicesSettingsFromResponseDeclaration();
	}
	
	public void extractTemplateDeclarationsForGlobal() {
		globalInlineChoices = new ArrayList<>();
		
		List<TemplateDeclaration> templateDeclarations = assessmentItem.getTemplateDeclarations();
		for(TemplateDeclaration templateDeclaration:templateDeclarations) {
			String identifier = templateDeclaration.getIdentifier().toString();
			if(identifier.startsWith("global-")) {
				List<FieldValue> fValues = templateDeclaration.getDefaultValue().getFieldValues();
				for(FieldValue fValue:fValues) {
					String id = QtiNodesExtractor.extractId(fValue);
					SingleValue sValue = fValue.getSingleValue();
					if(sValue instanceof StringValue) {
						String val = ((StringValue)sValue).stringValue();
						globalInlineChoices.add(new GlobalInlineChoice(Identifier.assumedLegal(id), val));
					}
				}
			}
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
	
	public void extractInteractions() {
		inlineChoiceInteractions = new ArrayList<>();
		
		List<Interaction> interactions = assessmentItem.getItemBody().findInteractions();
		if(interactions != null) {
			for(Interaction interaction:interactions) {
				if(interaction instanceof InlineChoiceInteraction) {
					inlineChoiceInteractions.add(new InlineChoiceInteractionEntry((InlineChoiceInteraction)interaction));
				}
			}
		}
	}
	

	public void extractInlineChoicesSettingsFromResponseDeclaration() {
		boolean hasMapping = false;
		List<Interaction> interactions = assessmentItem.getItemBody().findInteractions();
		for(Interaction interaction:interactions) {
			if(interaction instanceof InlineChoiceInteraction && interaction.getResponseIdentifier() != null) {
				InlineChoiceInteraction inlineChoiceInteraction = (InlineChoiceInteraction)interaction;
				
				ResponseDeclaration responseDeclaration = assessmentItem.getResponseDeclaration(interaction.getResponseIdentifier());
				if(responseDeclaration != null && responseDeclaration.hasBaseType(BaseType.IDENTIFIER) && responseDeclaration.hasCardinality(Cardinality.SINGLE)) {
					InlineChoiceInteractionEntry inlineChoiceBlock = getInteraction(inlineChoiceInteraction.getResponseIdentifier().toString());
					extractInlineChoicesInteractionSettingsFromResponseDeclaration(responseDeclaration, inlineChoiceBlock);
					String marker = "responseIdentifier=\"" + interaction.getResponseIdentifier().toString() + "\"";
					if(inlineChoiceBlock.getCorrectResponseId() != null && inlineChoiceBlock.getSolution() != null) {
						String solution = inlineChoiceBlock.getSolution();
						question = question.replace(marker, marker + " data-qti-solution=\"" + escapeForDataQtiSolution(solution) + "\"");
					}
					
					hasMapping |= inlineChoiceBlock.hasScores();
				}
			}
		}
		
		scoreEvaluation = hasMapping ? ScoreEvaluation.perAnswer : ScoreEvaluation.allCorrectAnswers;
	}
	
	public static void extractInlineChoicesInteractionSettingsFromResponseDeclaration(ResponseDeclaration responseDeclaration,
			InlineChoiceInteractionEntry inlineChoiceBlock) {
		CorrectResponse correctResponse = responseDeclaration.getCorrectResponse();
		if(correctResponse != null) {
			Value value = FieldValue.computeValue(Cardinality.SINGLE, correctResponse.getFieldValues());
			if(value instanceof IdentifierValue) {
				IdentifierValue identifierValue = (IdentifierValue)value;
				inlineChoiceBlock.setCorrectResponseId(identifierValue.identifierValue());
			}
		}
		
		Mapping mapping = responseDeclaration.getMapping();
		if(mapping != null) {
			List<MapEntry> mapEntries = mapping.getMapEntries();
			for(MapEntry mapEntry:mapEntries) {
				SingleValue iValue = mapEntry.getMapKey();
				if(iValue instanceof IdentifierValue) {
					Identifier identifier = ((IdentifierValue)iValue).identifierValue();
					inlineChoiceBlock.putScore(identifier, mapEntry.getMappedValue());
				}	
			}
		}
	}
	
	public Identifier generateResponseIdentifier() {
		return IdentifierGenerator.newAsIdentifier("inline");
	}
	
	public Identifier generateIdentifier(GlobalInlineChoice globalChoice) {
		String choiceIdentifier = globalChoice.getIdentifier().toString() + "-" + CodeHelper.getRAMUniqueID();
		return Identifier.assumedLegal(choiceIdentifier);
	}
	
	public List<InlineChoiceInteractionEntry> getInteractions() {
		return new ArrayList<>(inlineChoiceInteractions);
	}
	
	public InlineChoiceInteractionEntry getInteraction(String responseIdentifier) {
		Identifier responseIdent = Identifier.assumedLegal(responseIdentifier);
		for(InlineChoiceInteractionEntry interaction:inlineChoiceInteractions) {
			if(responseIdent.equals(interaction.getResponseIdentifier())) {
				return interaction;
			}
		}
		return null;
	}
	
	public InlineChoiceInteractionEntry createInteraction(String responseIdentifier) {
		Identifier responseIdent = Identifier.parseString(responseIdentifier);
		InlineChoiceInteractionEntry choiceBlock = new InlineChoiceInteractionEntry(responseIdent);
		inlineChoiceInteractions.add(choiceBlock);
		return choiceBlock;
	}
	
	public void removeInteraction(InlineChoiceInteractionEntry interaction) {
		inlineChoiceInteractions.remove(interaction);
	}
	
	public List<GlobalInlineChoice> getGlobalInlineChoices() {
		return globalInlineChoices;
	}
	
	public GlobalInlineChoice getGlobalInlineChoice(Identifier inlineChoiceId) {
		String identifier = inlineChoiceId.toString();
		
		for(GlobalInlineChoice globalInlineChoice:globalInlineChoices) {
			String globalId = globalInlineChoice.getIdentifier().toString();	
			if(identifier.startsWith(globalId)) {
				return globalInlineChoice;
			}
		}
		return null;
	}
	
	public GlobalInlineChoice getGlobalInlineChoiceByText(String text) {
		if(text == null) return null;

		for(GlobalInlineChoice globalInlineChoice:globalInlineChoices) {
			String globalText = globalInlineChoice.getText();
			if(text.equals(globalText)) {
				return globalInlineChoice;
			}
		}
		return null;
	}
	
	/**
	 * @param index The position in the list of global inline choices (not assessment relevant)
	 * @return A new, empty inline choice
	 */
	public GlobalInlineChoice addGlobalInlineChoice(int index) {
		Identifier id = IdentifierGenerator.newAsIdentifier("global-1-");
		GlobalInlineChoice globalInlineChoice = new GlobalInlineChoice(id, "");
		if(index < 0 || index >= globalInlineChoices.size()) {
			globalInlineChoices.add(globalInlineChoice);
		} else {
			globalInlineChoices.add(index, globalInlineChoice);
		}
		
		for(InlineChoiceInteractionEntry interactionEntry:inlineChoiceInteractions) {
			InlineChoice newChoice = new InlineChoice(interactionEntry.getInteraction());
			Identifier choiceIdentifier = generateIdentifier(globalInlineChoice);
			newChoice.setIdentifier(choiceIdentifier);
			newChoice.getTextOrVariables().add(new TextRun(newChoice, globalInlineChoice.getText()));
			interactionEntry.getInlineChoices().add(newChoice);
		}
		
		return globalInlineChoice;
	}
	
	public void removeGlobalInlineChoice(GlobalInlineChoice globalChoice) {
		String globalIdentifier = globalChoice.getIdentifier().toString();
		globalInlineChoices.remove(globalChoice);
		
		for(InlineChoiceInteractionEntry interactionEntry:inlineChoiceInteractions) {
			List<InlineChoice> choices = interactionEntry.getInlineChoices();
			for(Iterator<InlineChoice> choiceIt=choices.iterator(); choiceIt.hasNext(); ) {
				InlineChoice choice = choiceIt.next();
				if(choice.getIdentifier().toString().startsWith(globalIdentifier)) {
					choiceIt.remove();
				}
			}
		}
	}

	public ScoreEvaluation getScoreEvaluationMode() {
		return scoreEvaluation;
	}

	public void setScoreEvaluationMode(ScoreEvaluation scoreEvaluation) {
		this.scoreEvaluation = scoreEvaluation;
	}

	@Override
	public QTI21QuestionType getQuestionType() {
		return QTI21QuestionType.inlinechoice;
	}

	@Override
	public String getQuestion() {
		return question;
	}

	@Override
	public void setQuestion(String html) {
		this.question = html;
	}

	@Override
	protected void buildItemBody() {
		//remove current blocks
		List<Block> blocks = assessmentItem.getItemBody().getBlocks();
		blocks.clear();

		//add question
		getHtmlHelper().appendHtml(assessmentItem.getItemBody(), question);
		
		List<Interaction> interactions = assessmentItem.getItemBody().findInteractions();
		for(Interaction interaction:interactions) {
			if(interaction instanceof InlineChoiceInteraction) {
				buildInlineChoiceInteraction((InlineChoiceInteraction)interaction);
			}
		}
	}
	
	private void buildInlineChoiceInteraction(InlineChoiceInteraction inlineChoiceInteraction) {
		Identifier responseIdentifier = inlineChoiceInteraction.getResponseIdentifier();
		InlineChoiceInteractionEntry inlineChoiceInteractionEntry = inlineChoiceInteractions.stream()
			.filter(block -> responseIdentifier.equals(block.getResponseIdentifier()))
			.findFirst().orElse(null);
		if(inlineChoiceInteractionEntry != null) {
			inlineChoiceInteraction.setShuffle(inlineChoiceInteractionEntry.isShuffle());
			inlineChoiceInteraction.getInlineChoices().clear();
			Set<GlobalInlineChoice> usedGlobalInlineChoices = new HashSet<>();
			
			List<InlineChoice> inlineChoices = inlineChoiceInteractionEntry.getInlineChoices();
			for(InlineChoice inlineChoice:inlineChoices) {
				Identifier choiceIdentifier = inlineChoice.getIdentifier();
				InlineChoice copy = new InlineChoice(inlineChoiceInteraction);
				copy.setIdentifier(choiceIdentifier);
				
				String text;
				GlobalInlineChoice globalInlineChoice = getGlobalInlineChoice(inlineChoice.getIdentifier());
				if(globalInlineChoice != null) {
					text = globalInlineChoice.getText();
					usedGlobalInlineChoices.add(globalInlineChoice);
				} else {
					text = getText(inlineChoice);
				}
				copy.getTextOrVariables().add(new TextRun(copy, text));
				inlineChoiceInteraction.getInlineChoices().add(copy);
			}
			
			List<GlobalInlineChoice> missingGlobalInlineChoices = new ArrayList<>(globalInlineChoices);
			missingGlobalInlineChoices.removeAll(usedGlobalInlineChoices);
			
			for(GlobalInlineChoice missingGlobalInlineChoice:missingGlobalInlineChoices ) {
				InlineChoice copy = new InlineChoice(inlineChoiceInteraction);
				Identifier choiceIdentifier = generateIdentifier(missingGlobalInlineChoice);
				copy.setIdentifier(choiceIdentifier);
				copy.getTextOrVariables().add(new TextRun(copy, missingGlobalInlineChoice.getText()));
				inlineChoiceInteraction.getInlineChoices().add(copy);
			}
		}
	}

	@Override
	protected void buildResponseAndOutcomeDeclarations() {
		List<ResponseDeclaration> responseDeclarations = assessmentItem.getResponseDeclarations();
		responseDeclarations.clear();
		
		/*
		<responseDeclaration identifier="RESPONSE_1" cardinality="single" baseType="identifier">
			<correctResponse>
				<value>Inline</value>
			</correctResponse>
			<mapping defaultValue="0">
				<mapEntry mapKey="inline" mappedValue="2" />
				<mapEntry mapKey="inline1" mappedValue="2" />
				<mapEntry mapKey="inline2" mappedValue="1" />
			</mapping>
		</responseDeclaration>
		*/
		for(InlineChoiceInteractionEntry inlineChoiceInteractionEntry:inlineChoiceInteractions) {
			if(inlineChoiceInteractionEntry.getResponseIdentifier() != null && inlineChoiceInteractionEntry.getCorrectResponseId() != null) {
				ResponseDeclaration responseDeclaration = createInlineChoiceResponseDeclaration(assessmentItem,
						inlineChoiceInteractionEntry.getResponseIdentifier(), inlineChoiceInteractionEntry.getCorrectResponseId());
				responseDeclarations.add(responseDeclaration);
				
				if(scoreEvaluation == ScoreEvaluation.perAnswer) {
					Map<Identifier,Double> scoreMap = inlineChoiceInteractionEntry.getScores();
					appendMapping(responseDeclaration, scoreMap);
				}
			}
		}
		
		List<TemplateDeclaration> templateDeclarations = assessmentItem.getTemplateDeclarations();
		templateDeclarations.clear();
		if(!globalInlineChoices.isEmpty()) {
			buildTemplateDeclaration("global-inline-choices-1", globalInlineChoices, templateDeclarations);
		}
	}
	
	private void buildTemplateDeclaration(String identifier, List<GlobalInlineChoice> choices, List<TemplateDeclaration> templateDeclarations) {
		TemplateDeclaration templateDeclaration = new TemplateDeclaration(assessmentItem);
		templateDeclaration.setIdentifier(Identifier.assumedLegal(identifier));
		templateDeclaration.setCardinality(Cardinality.MULTIPLE);
		templateDeclaration.setBaseType(BaseType.STRING);
		templateDeclarations.add(templateDeclaration);
		
		DefaultValue defaultValue = new DefaultValue(templateDeclaration);
		templateDeclaration.setDefaultValue(defaultValue);
		
		List<FieldValue> fValues = defaultValue.getFieldValues();
		for(GlobalInlineChoice choice:choices) {
			FieldValue fieldVal = new FieldValue(defaultValue, new StringValue(choice.getText()));
			ForeignAttribute idAttr = new ForeignAttribute(fieldVal, "id", "");
			idAttr.setValue(choice.getIdentifier().toString());
			fieldVal.getAttributes().add(idAttr);
			fValues.add(fieldVal);
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
	
	@Override
	protected void buildModalFeedbacksAndHints(List<OutcomeDeclaration> outcomeDeclarations, List<ResponseRule> responseRules) {
		if((correctFeedback != null || incorrectFeedback != null) && scoreEvaluation == ScoreEvaluation.perAnswer) {
			ResponseCondition responseCondition = AssessmentItemFactory.createModalFeedbackResponseConditionByScore(assessmentItem.getResponseProcessing());
			responseRules.add(responseCondition);
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
		List<InlineChoiceInteractionEntry> entries = new ArrayList<>(inlineChoiceInteractions);
		for(count = 0; count <entries.size(); count++) {
			InlineChoiceInteractionEntry inlineChoiceInteractionEntry = entries.get(count);
			String scoreIdentifier = "SCORE_" + inlineChoiceInteractionEntry.getResponseIdentifier().toString();
			buildScoreRulePerAnswer(count, inlineChoiceInteractionEntry, Identifier.parseString(scoreIdentifier), responseRules);
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
			
			for(InlineChoiceInteractionEntry inlineChoiceInteractionEntry:entries) {
				
				{//variable score
					Variable scoreVariable = new Variable(sum);
					sum.getExpressions().add(scoreVariable);
					String scoreIdentifier = "SCORE_" + inlineChoiceInteractionEntry.getResponseIdentifier().toString();
					scoreVariable.setIdentifier(ComplexReferenceIdentifier.parseString(scoreIdentifier));
					
					//create associated outcomeDeclaration
					OutcomeDeclaration modalOutcomeDeclaration = AssessmentItemFactory
							.createOutcomeDeclarationForScoreResponse(assessmentItem, scoreIdentifier);
					outcomeDeclarations.add(modalOutcomeDeclaration);
				}
				
				{//variable minscore
					Variable minScoreVariable = new Variable(sum);
					sum.getExpressions().add(minScoreVariable);
					String scoreIdentifier = "MINSCORE_" + inlineChoiceInteractionEntry.getResponseIdentifier().toString();
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
	
	/**
	 * Outcome map response.
	 * 
	 * @param count Current position of the rule
	 * @param entry The text entry
	 * @param scoreIdentifier The identifier of the score
	 * @param responseRules The list of response rules
	 */
	private void buildScoreRulePerAnswer(int count, InlineChoiceInteractionEntry entry, Identifier scoreIdentifier,
			 List<ResponseRule> responseRules) {
		SetOutcomeValue mapOutcomeValue = new SetOutcomeValue(assessmentItem.getResponseProcessing());
		responseRules.add(count, mapOutcomeValue);
		mapOutcomeValue.setIdentifier(scoreIdentifier);
		
		MapResponse mapResponse = new MapResponse(mapOutcomeValue);
		mapResponse.setIdentifier(entry.getResponseIdentifier());
		mapOutcomeValue.setExpression(mapResponse);
	}
	
	private void buildMainScoreRuleAllCorrectAnswers(List<ResponseRule> responseRules) {
		/*
		<responseCondition>
			<responseIf>
				<and>
					<match>
						<variable identifier="RESPONSE_1" />
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
			
			for(InlineChoiceInteractionEntry inlineChoiceEntry:inlineChoiceInteractions) {
				ComplexReferenceIdentifier responseIdentifier = ComplexReferenceIdentifier
						.assumedLegal(inlineChoiceEntry.getResponseIdentifier().toString());
				
				Match match = new Match(and);
				and.getExpressions().add(match);
					
				Variable variable = new Variable(match);
				variable.setIdentifier(responseIdentifier);
				match.getExpressions().add(variable);
					
				Correct correct = new Correct(match);
				correct.setIdentifier(responseIdentifier);
				match.getExpressions().add(correct);	
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
	
	@Override
	public void postImportProcessing() {
		super.postImportProcessing();
		postImportGlobalChoiceProcessing();
	}

	private void postImportGlobalChoiceProcessing() {
		List<InlineChoiceInteractionEntry> interactions = getInteractions();
		if(interactions.size() <= 1) return;
		
		List<String> referenceSet = null;
		for(InlineChoiceInteractionEntry interaction:interactions) {
			List<InlineChoice> inlineChoices = interaction.getInlineChoices();
			List<String> choiceSet = inlineChoices.stream()
					.map(InlineChoiceAssessmentItemBuilder::getText)
					.collect(Collectors.toList());
			
			if(referenceSet == null) {
				referenceSet = choiceSet;
			} else if(!referenceSet.containsAll(choiceSet) || !choiceSet.containsAll(referenceSet)) {
				return;
			}	
		}
		
		if(referenceSet != null) {
			// Make reference set global
			for(int i=0; i<referenceSet.size(); i++) {
				GlobalInlineChoice gChoice = addGlobalInlineChoice(i);
				gChoice.setText(referenceSet.get(i));
			}
			
			// Mutate the choice identifier and correct response identifier with global ones
			for(InlineChoiceInteractionEntry interaction:interactions) {
				Identifier correctResponse = interaction.getCorrectResponseId();
				List<InlineChoice> inlineChoices = interaction.getInlineChoices();
				for(InlineChoice inlineChoice:inlineChoices) {
					final String text = getText(inlineChoice);
					final Identifier identifier = inlineChoice.getIdentifier();
					final GlobalInlineChoice gChoice = getGlobalInlineChoiceByText(text);
					if(gChoice != null) {
						Identifier newIdentifier = generateIdentifier(gChoice);
						if(correctResponse != null && correctResponse.equals(identifier)) {
							interaction.setCorrectResponseId(newIdentifier);
						}
						inlineChoice.setIdentifier(newIdentifier);
					}
				}
			}
		}
	}
	
	public static String getText(InlineChoice inlineChoice) {
		StringBuilder textSolution = new StringBuilder();

		List<TextOrVariable> values = inlineChoice.getTextOrVariables();
		for(TextOrVariable value:values) {
			if(value instanceof TextRun) {
				String text = ((TextRun)value).getTextContent();
				if(StringHelper.containsNonWhitespace(text)) {
					textSolution.append(text);
				}
			}
		}

		return textSolution.toString();
	}
	
	public static class GlobalInlineChoice {
		
		private final Identifier identifier;
		private String text;
		
		public GlobalInlineChoice(Identifier identifier, String text) {
			this.identifier = identifier;
			this.text = text;
		}

		public Identifier getIdentifier() {
			return identifier;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		@Override
		public int hashCode() {
			return identifier.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if(obj instanceof GlobalInlineChoice) {
				GlobalInlineChoice other = (GlobalInlineChoice) obj;
				return Objects.equals(identifier, other.identifier);
			}
			return false;
		}
	}

	public static class InlineChoiceInteractionEntry {
		
		private final InlineChoiceInteraction interaction;
		private final Identifier responseIdentifier;
		private final List<InlineChoice> inlineChoices;
		private final Map<Identifier,Double> scores = new HashMap<>();
		
		private Identifier correctResponseId;
		private boolean shuffle = true;
		
		public InlineChoiceInteractionEntry(InlineChoiceInteraction interaction) {
			this.interaction = interaction;
			responseIdentifier = interaction.getResponseIdentifier();
			inlineChoices = new ArrayList<>(interaction.getInlineChoices());
			shuffle = interaction.getShuffle();
		}
		
		public InlineChoiceInteractionEntry(Identifier responseIdentifier) {
			interaction = null;
			inlineChoices = new ArrayList<>();
			this.responseIdentifier = responseIdentifier;
		}
		
		public Identifier getResponseIdentifier() {
			return interaction == null ? responseIdentifier : interaction.getResponseIdentifier();
		}
		
		public Double getScore(Identifier identifier) {
			return scores.get(identifier);
		}
		
		public void putScore(Identifier identifier, Double score) {
			if(score != null) {
				scores.put(identifier, score);
			} else {
				scores.remove(identifier);
			}	
		}
		
		public Map<Identifier,Double> getScores() {
			Map<Identifier,Double> scoresMap = new HashMap<>();
			for(InlineChoice inlineChoice:inlineChoices) {
				Double score = scores.get(inlineChoice.getIdentifier());
				if(score == null) {
					score = Double.valueOf(0.0d);
				}
				scoresMap.put(inlineChoice.getIdentifier(), score);
			}
			return scoresMap;
		}
		
		public boolean hasScores() {
			return !scores.isEmpty();
		}
		
		public InlineChoiceInteraction getInteraction() {
			return interaction;
		}
		
		public List<InlineChoice> getInlineChoices() {
			return inlineChoices;
		}
		
		public InlineChoice getInlineChoice(Identifier identifier) {
			return inlineChoices.stream()
					.filter(choice -> identifier.equals(choice.getIdentifier()))
					.findFirst().orElse(null);
		}

		public boolean isShuffle() {
			return shuffle;
		}

		public void setShuffle(boolean shuffle) {
			this.shuffle = shuffle;
		}

		public Identifier getCorrectResponseId() {
			return correctResponseId;
		}

		public void setCorrectResponseId(Identifier correctResponseId) {
			this.correctResponseId = correctResponseId;
		}
		
		public String getSolution() {
			String textSolution = null;
			if(correctResponseId != null && interaction != null && interaction.getInlineChoices() != null) {
				for(InlineChoice inlineChoice:interaction.getInlineChoices()) {
					if(correctResponseId.equals(inlineChoice.getIdentifier())) {
						textSolution = getText(inlineChoice);
					}
				}
			}
			return textSolution;
		}

		@Override
		public int hashCode() {
			return Objects.hash(responseIdentifier);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj instanceof InlineChoiceInteractionEntry) {
				InlineChoiceInteractionEntry other = (InlineChoiceInteractionEntry) obj;
				return responseIdentifier != null && Objects.equals(responseIdentifier, other.responseIdentifier);
			}
			return false;
		}
	}
}