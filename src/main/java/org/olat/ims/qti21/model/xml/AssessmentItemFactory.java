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
package org.olat.ims.qti21.model.xml;

import static org.olat.ims.qti21.QTI21Constants.MAXSCORE_CLX_IDENTIFIER;
import static org.olat.ims.qti21.QTI21Constants.MINSCORE_CLX_IDENTIFIER;
import static org.olat.ims.qti21.QTI21Constants.SCORE_CLX_IDENTIFIER;
import static org.olat.ims.qti21.QTI21Constants.SCORE_IDENTIFIER;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.olat.core.helpers.Settings;
import org.olat.core.util.StringHelper;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.model.IdentifierGenerator;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.TextEntryAlternative;

import uk.ac.ed.ph.jqtiplus.group.NodeGroupList;
import uk.ac.ed.ph.jqtiplus.group.item.ItemBodyGroup;
import uk.ac.ed.ph.jqtiplus.group.item.interaction.PromptGroup;
import uk.ac.ed.ph.jqtiplus.group.item.interaction.choice.SimpleChoiceGroup;
import uk.ac.ed.ph.jqtiplus.group.outcome.declaration.OutcomeDeclarationGroup;
import uk.ac.ed.ph.jqtiplus.node.QtiNode;
import uk.ac.ed.ph.jqtiplus.node.content.ItemBody;
import uk.ac.ed.ph.jqtiplus.node.content.basic.TextRun;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.object.Object;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.P;
import uk.ac.ed.ph.jqtiplus.node.expression.general.BaseValue;
import uk.ac.ed.ph.jqtiplus.node.expression.general.Correct;
import uk.ac.ed.ph.jqtiplus.node.expression.general.Variable;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.And;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Gt;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.IsNull;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Lt;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Match;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Multiple;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Shape;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Sum;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.CorrectResponse;
import uk.ac.ed.ph.jqtiplus.node.item.ModalFeedback;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ExtendedTextInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.HotspotInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.MatchInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.TextEntryInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleAssociableChoice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleChoice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleMatchSet;
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
import uk.ac.ed.ph.jqtiplus.node.shared.FieldValue;
import uk.ac.ed.ph.jqtiplus.node.shared.declaration.DefaultValue;
import uk.ac.ed.ph.jqtiplus.node.test.View;
import uk.ac.ed.ph.jqtiplus.node.test.VisibilityMode;
import uk.ac.ed.ph.jqtiplus.types.ComplexReferenceIdentifier;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.BaseType;
import uk.ac.ed.ph.jqtiplus.value.Cardinality;
import uk.ac.ed.ph.jqtiplus.value.DirectedPairValue;
import uk.ac.ed.ph.jqtiplus.value.FloatValue;
import uk.ac.ed.ph.jqtiplus.value.IdentifierValue;
import uk.ac.ed.ph.jqtiplus.value.StringValue;

/**
 * 
 * Initial date: 03.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentItemFactory {
	
	public static AssessmentItem createSingleChoice() {
		AssessmentItem assessmentItem = createAssessmentItem(QTI21QuestionType.sc, "Single choice");

		//define correct answer
		Identifier responseDeclarationId = Identifier.assumedLegal("RESPONSE_1");
		Identifier correctResponseId = IdentifierGenerator.newAsIdentifier("sc");
		ResponseDeclaration responseDeclaration = createSingleChoiceCorrectResponseDeclaration(assessmentItem, responseDeclarationId, correctResponseId);
		assessmentItem.getNodeGroups().getResponseDeclarationGroup().getResponseDeclarations().add(responseDeclaration);
		
		//outcomes
		appendDefaultOutcomeDeclarations(assessmentItem, 1.0d);
		
		//the single choice interaction
		ItemBody itemBody = appendDefaultItemBody(assessmentItem);
		ChoiceInteraction choiceInteraction = appendChoiceInteraction(itemBody, responseDeclarationId, 1, true);
		appendSimpleChoice(choiceInteraction, "New answer", "sc");

		//response processing
		ResponseProcessing responseProcessing = createResponseProcessing(assessmentItem, responseDeclarationId);
		assessmentItem.getNodeGroups().getResponseProcessingGroup().setResponseProcessing(responseProcessing);
		return assessmentItem;
	}
	
	public static AssessmentItem createAssessmentItem(QTI21QuestionType type, String defaultTitle) {
		AssessmentItem assessmentItem = new AssessmentItem();
		if(type != null) {
			assessmentItem.setIdentifier(IdentifierGenerator.newAsString(type.getPrefix()));
		} else {
			assessmentItem.setIdentifier(IdentifierGenerator.newAsString("item"));
		}
		assessmentItem.setTitle(defaultTitle);
		assessmentItem.setToolName(QTI21Constants.TOOLNAME);
		assessmentItem.setToolVersion(Settings.getVersion());
		assessmentItem.setAdaptive(Boolean.FALSE);
		assessmentItem.setTimeDependent(Boolean.FALSE);
		return assessmentItem;
	}
	
	public static ItemBody appendDefaultItemBody(AssessmentItem assessmentItem) {
		NodeGroupList nodeGroups = assessmentItem.getNodeGroups();
		//the single choice interaction
		ItemBodyGroup itemBodyGroup = nodeGroups.getItemBodyGroup();
		ItemBody itemBody = new ItemBody(assessmentItem);
		itemBodyGroup.setItemBody(itemBody);

		P question = getParagraph(itemBody, "");
		itemBody.getBlocks().add(question);
		return itemBody;
	}
	
	/**
	 * Append the default outcome declaration for score, max score and feedback basic.
	 * 
	 * @param assessmentItem
	 */
	public static void appendDefaultOutcomeDeclarations(AssessmentItem assessmentItem, double maxScore) {
		NodeGroupList nodeGroups = assessmentItem.getNodeGroups();
		//outcomes
		OutcomeDeclarationGroup outcomeDeclarations = nodeGroups.getOutcomeDeclarationGroup();
		//outcome score
		OutcomeDeclaration scoreOutcomeDeclaration = createOutcomeDeclarationForScore(assessmentItem);
		outcomeDeclarations.getOutcomeDeclarations().add(scoreOutcomeDeclaration);
		// outcome max score
		OutcomeDeclaration maxScoreOutcomeDeclaration = createOutcomeDeclarationForMaxScore(assessmentItem, maxScore);
		outcomeDeclarations.getOutcomeDeclarations().add(maxScoreOutcomeDeclaration);
		// outcome min score
		OutcomeDeclaration minScoreOutcomeDeclaration = createOutcomeDeclarationForMinScore(assessmentItem, 0.0d);
		outcomeDeclarations.getOutcomeDeclarations().add(minScoreOutcomeDeclaration);
		// outcome feedback
		OutcomeDeclaration feedbackOutcomeDeclaration = createOutcomeDeclarationForFeedbackBasic(assessmentItem);
		outcomeDeclarations.getOutcomeDeclarations().add(feedbackOutcomeDeclaration);
	}
	
	/*
	<responseDeclaration identifier="RESPONSE" cardinality="single" baseType="identifier">
		<correctResponse>
			<value>Choice0</value>
		</correctResponse>
	</responseDeclaration>
	*/
	public static ResponseDeclaration createHotspotEntryResponseDeclarationSingle(AssessmentItem assessmentItem,
			Identifier responseIdentifier, Identifier correctAnswerIdentifier) {
		ResponseDeclaration responseDeclaration = new ResponseDeclaration(assessmentItem);
		responseDeclaration.setIdentifier(responseIdentifier);
		responseDeclaration.setCardinality(Cardinality.SINGLE);
		responseDeclaration.setBaseType(BaseType.IDENTIFIER);
		
		//correct response
		CorrectResponse correctResponse = new CorrectResponse(responseDeclaration);
		responseDeclaration.setCorrectResponse(correctResponse);
		appendIdentifierValue(correctResponse, correctAnswerIdentifier);
		return responseDeclaration;
	}
	
	public static HotspotInteraction appendHotspotInteraction(ItemBody itemBody, Identifier responseDeclarationId, Identifier correctResponseId) {
		HotspotInteraction hotspotInteraction = new HotspotInteraction(itemBody);
		hotspotInteraction.setResponseIdentifier(responseDeclarationId);
		hotspotInteraction.setMaxChoices(1);
		itemBody.getBlocks().add(hotspotInteraction);
		
		Object graphicObject = new Object(hotspotInteraction);
		graphicObject.setType("image/png");
		graphicObject.setWidth("400");
		graphicObject.setHeight("320");
		hotspotInteraction.setObject(graphicObject);
		
		HotspotChoice choice = new HotspotChoice(hotspotInteraction);
		choice.setIdentifier(correctResponseId);
		choice.setFixed(Boolean.FALSE);
		choice.setShape(Shape.CIRCLE);
		List<Integer> coords = new ArrayList<>();
		coords.add(new Integer(77));
		coords.add(new Integer(115));
		coords.add(new Integer(8));
		choice.setCoords(coords);
		hotspotInteraction.getHotspotChoices().add(choice);
		
		return hotspotInteraction;
	}
	
	public static TextEntryInteraction appendTextEntryInteraction(ItemBody itemBody, Identifier responseDeclarationId) {
		P paragraph = new P(itemBody);
		TextRun text = new TextRun(paragraph, "New text");
		paragraph.getInlines().add(text);
		TextEntryInteraction textInteraction = new TextEntryInteraction(paragraph);
		textInteraction.setResponseIdentifier(responseDeclarationId);
		paragraph.getInlines().add(textInteraction);
		itemBody.getBlocks().add(paragraph);
		return textInteraction;
	}
	
	public static ResponseDeclaration createNumericalEntryResponseDeclaration(AssessmentItem assessmentItem,
			Identifier declarationId, double response) {
		ResponseDeclaration responseDeclaration = new ResponseDeclaration(assessmentItem);
		responseDeclaration.setIdentifier(declarationId);
		responseDeclaration.setCardinality(Cardinality.SINGLE);
		responseDeclaration.setBaseType(BaseType.FLOAT);
		
		//correct response
		CorrectResponse correctResponse = new CorrectResponse(responseDeclaration);
		responseDeclaration.setCorrectResponse(correctResponse);
		appendFloatValue(correctResponse, response);
		return responseDeclaration;
	}
	
	public static ResponseDeclaration createTextEntryResponseDeclaration(AssessmentItem assessmentItem,
			Identifier declarationId, String response, Double score, boolean caseSensitive,
			List<TextEntryAlternative> alternatives) {
		ResponseDeclaration responseDeclaration = new ResponseDeclaration(assessmentItem);
		responseDeclaration.setIdentifier(declarationId);
		responseDeclaration.setCardinality(Cardinality.SINGLE);
		responseDeclaration.setBaseType(BaseType.STRING);
		
		//correct response
		CorrectResponse correctResponse = new CorrectResponse(responseDeclaration);
		responseDeclaration.setCorrectResponse(correctResponse);
		appendStringValue(correctResponse, response);

		// mapping
		Mapping mapping = new Mapping(responseDeclaration);
		mapping.setDefaultValue(0.0d);
		responseDeclaration.setMapping(mapping);

		{//map correct response
			MapEntry mapEntry = new MapEntry(mapping);
			mapEntry.setMapKey(new StringValue(response));
			mapEntry.setMappedValue(score);
			mapEntry.setCaseSensitive(new Boolean(caseSensitive));
			mapping.getMapEntries().add(mapEntry);
		}
		
		//map alternatives
		if(alternatives != null && alternatives.size() > 0) {
			for(TextEntryAlternative alternative:alternatives) {
				if(StringHelper.containsNonWhitespace(alternative.getAlternative())) {
					MapEntry mapEntry = new MapEntry(mapping);
					mapEntry.setMapKey(new StringValue(alternative.getAlternative()));
					mapEntry.setMappedValue(score);
					mapEntry.setCaseSensitive(new Boolean(caseSensitive));
					mapping.getMapEntries().add(mapEntry);
				}
			}
		}
		
		return responseDeclaration;
	}
	
	/**
	 * For the all answers get the point
	 * @param assessmentItem
	 * @param declarationId
	 * @param response
	 * @param alternatives
	 * @return
	 */
	public static ResponseDeclaration createTextEntryResponseDeclaration(AssessmentItem assessmentItem,
			Identifier declarationId, String response, List<TextEntryAlternative> alternatives) {
		ResponseDeclaration responseDeclaration = new ResponseDeclaration(assessmentItem);
		responseDeclaration.setIdentifier(declarationId);
		responseDeclaration.setCardinality(Cardinality.SINGLE);
		responseDeclaration.setBaseType(BaseType.STRING);
		
		//correct response
		CorrectResponse correctResponse = new CorrectResponse(responseDeclaration);
		responseDeclaration.setCorrectResponse(correctResponse);
		appendStringValue(correctResponse, response);
		if(alternatives != null) {
			for(TextEntryAlternative alternative:alternatives) {
				appendStringValue(correctResponse, alternative.getAlternative());
			}
		}
		return responseDeclaration;
	}
	
	public static ExtendedTextInteraction appendExtendedTextInteraction(ItemBody itemBody, Identifier responseDeclarationId) {
		ExtendedTextInteraction textInteraction = new ExtendedTextInteraction(itemBody);
		textInteraction.setResponseIdentifier(responseDeclarationId);
		itemBody.getBlocks().add(textInteraction);
		return textInteraction;
	}
	
	public static ResponseDeclaration createExtendedTextResponseDeclaration(AssessmentItem assessmentItem, Identifier declarationId) {
		ResponseDeclaration responseDeclaration = new ResponseDeclaration(assessmentItem);
		responseDeclaration.setIdentifier(declarationId);
		responseDeclaration.setCardinality(Cardinality.SINGLE);
		responseDeclaration.setBaseType(BaseType.STRING);
		return responseDeclaration;
	}
	
	public static MatchInteraction appendMatchInteractionForKPrim(ItemBody itemBody, Identifier responseDeclarationId) {
		MatchInteraction matchInteraction = new MatchInteraction(itemBody);
		matchInteraction.setResponseIdentifier(responseDeclarationId);
		matchInteraction.setMaxAssociations(4);
		matchInteraction.setShuffle(false);
		itemBody.getBlocks().add(matchInteraction);
		
		PromptGroup prompts = new PromptGroup(matchInteraction);
		matchInteraction.getNodeGroups().add(prompts);
		
		SimpleMatchSet questionMatchSet = new SimpleMatchSet(matchInteraction);
		matchInteraction.getSimpleMatchSets().add(questionMatchSet);
		
		String[] classic = new String[]{ "a", "b", "c", "d" };
		for(int i=0; i<4; i++) {
			SimpleAssociableChoice correctChoice = new SimpleAssociableChoice(questionMatchSet);
			correctChoice.setMatchMax(1);
			correctChoice.setIdentifier(IdentifierGenerator.newNumberAsIdentifier(classic[i]));
			P question = getParagraph(correctChoice, "New answer " + classic[i]);
			correctChoice.getFlowStatics().add(question);
			questionMatchSet.getSimpleAssociableChoices().add(correctChoice);
		}
		
		SimpleMatchSet correctWrongMatchSet = new SimpleMatchSet(matchInteraction);
		matchInteraction.getSimpleMatchSets().add(correctWrongMatchSet);
		
		SimpleAssociableChoice correctChoice = new SimpleAssociableChoice(correctWrongMatchSet);
		correctChoice.setMatchMax(4);
		correctChoice.setFixed(Boolean.TRUE);
		correctChoice.setIdentifier(QTI21Constants.CORRECT_IDENTIFIER);
		correctChoice.getFlowStatics().add(new TextRun(correctChoice, "+"));
		correctWrongMatchSet.getSimpleAssociableChoices().add(correctChoice);

		SimpleAssociableChoice wrongChoice = new SimpleAssociableChoice(correctWrongMatchSet);
		wrongChoice.setMatchMax(4);
		wrongChoice.setFixed(Boolean.TRUE);
		wrongChoice.setIdentifier(QTI21Constants.WRONG_IDENTIFIER);
		wrongChoice.getFlowStatics().add(new TextRun(correctChoice, "-"));
		correctWrongMatchSet.getSimpleAssociableChoices().add(wrongChoice);
		
		return matchInteraction;
	}
	
	public static ResponseDeclaration createKPrimResponseDeclaration(AssessmentItem assessmentItem, Identifier declarationId,
			Map<Identifier,Identifier> associations, double maxScore) {
		ResponseDeclaration responseDeclaration = new ResponseDeclaration(assessmentItem);
		responseDeclaration.setIdentifier(declarationId);
		responseDeclaration.setCardinality(Cardinality.MULTIPLE);
		responseDeclaration.setBaseType(BaseType.DIRECTED_PAIR);
		return appendAssociationKPrimResponseDeclaration(responseDeclaration, associations, maxScore);
	}
	
	public static ResponseDeclaration appendAssociationKPrimResponseDeclaration(ResponseDeclaration responseDeclaration,
			Map<Identifier,Identifier> associations, double maxScore) {
		responseDeclaration.setCardinality(Cardinality.MULTIPLE);
		responseDeclaration.setBaseType(BaseType.DIRECTED_PAIR);

		//correct response
		CorrectResponse correctResponse = new CorrectResponse(responseDeclaration);
		responseDeclaration.setCorrectResponse(correctResponse);
		for(Map.Entry<Identifier,Identifier> association:associations.entrySet()) {
			Identifier choiceId = association.getKey();
			Identifier correctwrongId = association.getValue();

			DirectedPairValue dpValue = new DirectedPairValue(choiceId, correctwrongId);
			FieldValue fValue = new FieldValue(correctResponse, dpValue);
			correctResponse.getFieldValues().add(fValue);
		}
		
		double mappedValue = maxScore;
		if(associations.size() > 0) {
			mappedValue = maxScore / associations.size();
		}
		
		// mapping
		Mapping mapping = new Mapping(responseDeclaration);
		mapping.setDefaultValue(-mappedValue);
		responseDeclaration.setMapping(mapping);
		for(Map.Entry<Identifier,Identifier> association:associations.entrySet()) {
			Identifier choiceId = association.getKey();
			Identifier correctwrongId = association.getValue();
			
			MapEntry mapEntry = new MapEntry(mapping);
			mapEntry.setMapKey(new DirectedPairValue(choiceId, correctwrongId));
			mapEntry.setMappedValue(mappedValue);
			mapping.getMapEntries().add(mapEntry);
		}
		
		return responseDeclaration;
	}
	
	public static ChoiceInteraction appendChoiceInteraction(ItemBody itemBody, Identifier responseDeclarationId, int maxChoices, boolean shuffle) {
		ChoiceInteraction choiceInteraction = new ChoiceInteraction(itemBody);
		choiceInteraction.setMaxChoices(maxChoices);
		choiceInteraction.setShuffle(shuffle);
		choiceInteraction.setResponseIdentifier(responseDeclarationId);
		itemBody.getBlocks().add(choiceInteraction);
		
		PromptGroup prompts = new PromptGroup(choiceInteraction);
		choiceInteraction.getNodeGroups().add(prompts);
		
		SimpleChoiceGroup singleChoices = new SimpleChoiceGroup(choiceInteraction);
		choiceInteraction.getNodeGroups().add(singleChoices);
		return choiceInteraction;
	}
	
	public static ResponseDeclaration createSingleChoiceCorrectResponseDeclaration(AssessmentItem assessmentItem, Identifier declarationId, Identifier correctResponseId) {
		ResponseDeclaration responseDeclaration = new ResponseDeclaration(assessmentItem);
		responseDeclaration.setIdentifier(declarationId);
		responseDeclaration.setCardinality(Cardinality.SINGLE);
		responseDeclaration.setBaseType(BaseType.IDENTIFIER);

		CorrectResponse correctResponse = new CorrectResponse(responseDeclaration);
		responseDeclaration.setCorrectResponse(correctResponse);
		
		appendIdentifierValue(correctResponse, correctResponseId);
		return responseDeclaration;
	}
	
	public static ResponseDeclaration createMultipleChoiceCorrectResponseDeclaration(AssessmentItem assessmentItem, Identifier declarationId, List<Identifier> correctResponseIds) {
		ResponseDeclaration responseDeclaration = new ResponseDeclaration(assessmentItem);
		responseDeclaration.setIdentifier(declarationId);
		responseDeclaration.setCardinality(Cardinality.MULTIPLE);
		responseDeclaration.setBaseType(BaseType.IDENTIFIER);

		CorrectResponse correctResponse = new CorrectResponse(responseDeclaration);
		responseDeclaration.setCorrectResponse(correctResponse);
		
		for(Identifier correctResponseId:correctResponseIds) {
			appendIdentifierValue(correctResponse, correctResponseId);
		}
		return responseDeclaration;
	}
	
	public static SimpleChoice createSimpleChoice(ChoiceInteraction choiceInteraction, String text, String prefix) {
		SimpleChoice newChoice = new SimpleChoice(choiceInteraction);
		newChoice.setIdentifier(IdentifierGenerator.newAsIdentifier(prefix));
		P firstChoiceText = AssessmentItemFactory.getParagraph(newChoice, text);
		newChoice.getFlowStatics().add(firstChoiceText);
		return newChoice;
	}
	
	public static SimpleChoice appendSimpleChoice(ChoiceInteraction choiceInteraction, String text, String prefix) {
		SimpleChoice newChoice = createSimpleChoice(choiceInteraction, text, prefix);
		choiceInteraction.getNodeGroups().getSimpleChoiceGroup().getSimpleChoices().add(newChoice);
		return newChoice;
	}
	
	private static void appendIdentifierValue(CorrectResponse correctResponse, Identifier correctResponseId) {
		FieldValue fieldValue = new FieldValue(correctResponse);
		IdentifierValue identifierValue = new IdentifierValue(correctResponseId);
		fieldValue.setSingleValue(identifierValue);
		correctResponse.getFieldValues().add(fieldValue);
	}
	
	private static void appendStringValue(CorrectResponse correctResponse, String response) {
		FieldValue fieldValue = new FieldValue(correctResponse);
		StringValue identifierValue = new StringValue(response);
		fieldValue.setSingleValue(identifierValue);
		correctResponse.getFieldValues().add(fieldValue);
	}
	
	private static void appendFloatValue(CorrectResponse correctResponse, double response) {
		FieldValue fieldValue = new FieldValue(correctResponse);
		FloatValue identifierValue = new FloatValue(response);
		fieldValue.setSingleValue(identifierValue);
		correctResponse.getFieldValues().add(fieldValue);
	}
	
	/*
	<mapping defaultValue="0">
		<mapEntry mapKey="idd072fa37-f4c3-4532-a2fb-4458fa23e919" mappedValue="2.0" />
		<mapEntry mapKey="ide18af420-393e-43dc-b194-7af94663b576" mappedValue="-0.5" />
		<mapEntry mapKey="id72eb2dda-4053-45ba-a9f8-cc101f3e3987" mappedValue="2.0" />
	</mapping>
	 */
	public static Mapping appendMapping(ResponseDeclaration responseDeclaration, Map<Identifier,Double> map) {
		Mapping mapping = new Mapping(responseDeclaration);
		mapping.setDefaultValue(0d);
		responseDeclaration.setMapping(mapping);
		for(Map.Entry<Identifier, Double> entry:map.entrySet()) {
			MapEntry mapEntry = new MapEntry(mapping);
			mapEntry.setMapKey(new IdentifierValue(entry.getKey()));
			mapEntry.setMappedValue(entry.getValue());
			mapping.getMapEntries().add(mapEntry);
		}
		return mapping;
	}
	
	public static OutcomeDeclaration createOutcomeDeclarationForScore(AssessmentItem assessmentItem) {
		OutcomeDeclaration scoreOutcomeDeclaration = new OutcomeDeclaration(assessmentItem);
		scoreOutcomeDeclaration.setIdentifier(QTI21Constants.SCORE_IDENTIFIER);
		scoreOutcomeDeclaration.setCardinality(Cardinality.SINGLE);
		scoreOutcomeDeclaration.setBaseType(BaseType.FLOAT);

		DefaultValue scoreDefaultVal = new DefaultValue(scoreOutcomeDeclaration);
		scoreOutcomeDeclaration.setDefaultValue(scoreDefaultVal);
		
		FieldValue scoreDefaultFieldVal = new FieldValue(scoreDefaultVal, FloatValue.ZERO);
		scoreDefaultVal.getFieldValues().add(scoreDefaultFieldVal);
		
		return scoreOutcomeDeclaration;
	}
	/*
	<outcomeDeclaration identifier="SCORE_RESPONSE_2" cardinality="single" baseType="float" view="testConstructor">
		<defaultValue>
			<value>
				0
			</value>
		</defaultValue>
	</outcomeDeclaration>
	*/
	public static OutcomeDeclaration createOutcomeDeclarationForScoreResponse(AssessmentItem assessmentItem, String scoreIdentifier) {
		OutcomeDeclaration scoreOutcomeDeclaration = new OutcomeDeclaration(assessmentItem);
		scoreOutcomeDeclaration.setIdentifier(Identifier.parseString(scoreIdentifier));
		scoreOutcomeDeclaration.setCardinality(Cardinality.SINGLE);
		scoreOutcomeDeclaration.setBaseType(BaseType.FLOAT);

		DefaultValue scoreDefaultVal = new DefaultValue(scoreOutcomeDeclaration);
		scoreOutcomeDeclaration.setDefaultValue(scoreDefaultVal);
		
		FieldValue scoreDefaultFieldVal = new FieldValue(scoreDefaultVal, FloatValue.ZERO);
		scoreDefaultVal.getFieldValues().add(scoreDefaultFieldVal);
		
		return scoreOutcomeDeclaration;
	}
	
	/**
	 * Rule which ensure that the final score is not above the max. score value.
	 */
	public static ResponseRule createMaxScoreBoundLimitRule(ResponseProcessing responseProcessing) {
		/*
		<responseCondition>
			<responseIf>
				<gt>
					<variable identifier="SCORE" /><variable identifier="MAXSCORE" />
				</gt>
				<setOutcomeValue identifier="SCORE">
					<variable identifier="MAXSCORE" />
				</setOutcomeValue>
			</responseIf>
		</responseCondition>
		*/
		ResponseCondition rule = new ResponseCondition(responseProcessing);
		ResponseIf responseIf = new ResponseIf(rule);
		rule.setResponseIf(responseIf);
		
		Gt gt = new Gt(responseIf);
		responseIf.setExpression(gt);
		
		Variable scoreVar = new Variable(gt);
		scoreVar.setIdentifier(SCORE_CLX_IDENTIFIER);
		gt.getExpressions().add(scoreVar);
		
		Variable maxScoreVar = new Variable(gt);
		maxScoreVar.setIdentifier(MAXSCORE_CLX_IDENTIFIER);
		gt.getExpressions().add(maxScoreVar);
		
		SetOutcomeValue setOutcomeValue = new SetOutcomeValue(responseIf);
		setOutcomeValue.setIdentifier(SCORE_IDENTIFIER);
		
		Variable maxScoreOutcomeVar = new Variable(setOutcomeValue);
		maxScoreOutcomeVar.setIdentifier(MAXSCORE_CLX_IDENTIFIER);
		setOutcomeValue.setExpression(maxScoreOutcomeVar);
		responseIf.getResponseRules().add(setOutcomeValue);
		
		return rule;
	}
	
	public static OutcomeDeclaration createOutcomeDeclarationForMaxScore(AssessmentItem assessmentItem, double maxScore) {
		OutcomeDeclaration maxScoreOutcomeDeclaration = new OutcomeDeclaration(assessmentItem);
		maxScoreOutcomeDeclaration.setIdentifier(QTI21Constants.MAXSCORE_IDENTIFIER);
		maxScoreOutcomeDeclaration.setCardinality(Cardinality.SINGLE);
		maxScoreOutcomeDeclaration.setBaseType(BaseType.FLOAT);
		
		DefaultValue maxScoreDefaultVal = new DefaultValue(maxScoreOutcomeDeclaration);
		maxScoreOutcomeDeclaration.setDefaultValue(maxScoreDefaultVal);
		
		FieldValue maxScoreDefaultFieldVal = new FieldValue(maxScoreDefaultVal, new FloatValue(maxScore));
		maxScoreDefaultVal.getFieldValues().add(maxScoreDefaultFieldVal);
		
		return maxScoreOutcomeDeclaration;
	}
	
	/**
	 * Rule which ensure that the final score is not under the min. score value.
	 */
	public static ResponseRule createMinScoreBoundLimitRule(ResponseProcessing responseProcessing) {
		/*
		<responseCondition>
			<responseIf>
				<lt>
					<variable identifier="SCORE" /><variable identifier="MINSCORE" />
				</lt>
				<setOutcomeValue identifier="SCORE">
					<variable identifier="MINSCORE" />
				</setOutcomeValue>
			</responseIf>
		</responseCondition>
		*/
		ResponseCondition rule = new ResponseCondition(responseProcessing);
		ResponseIf responseIf = new ResponseIf(rule);
		rule.setResponseIf(responseIf);
		
		Lt lt = new Lt(responseIf);
		responseIf.setExpression(lt);
		
		Variable scoreVar = new Variable(lt);
		scoreVar.setIdentifier(SCORE_CLX_IDENTIFIER);
		lt.getExpressions().add(scoreVar);
		
		Variable minScoreVar = new Variable(lt);
		minScoreVar.setIdentifier(MINSCORE_CLX_IDENTIFIER);
		lt.getExpressions().add(minScoreVar);
		
		SetOutcomeValue setOutcomeValue = new SetOutcomeValue(responseIf);
		setOutcomeValue.setIdentifier(SCORE_IDENTIFIER);
		
		Variable minScoreOutcomeVar = new Variable(setOutcomeValue);
		minScoreOutcomeVar.setIdentifier(MINSCORE_CLX_IDENTIFIER);
		setOutcomeValue.setExpression(minScoreOutcomeVar);
		responseIf.getResponseRules().add(setOutcomeValue);

		return rule;
	}
	
	public static OutcomeDeclaration createOutcomeDeclarationForMinScore(AssessmentItem assessmentItem, double minScore) {
		OutcomeDeclaration maxScoreOutcomeDeclaration = new OutcomeDeclaration(assessmentItem);
		maxScoreOutcomeDeclaration.setIdentifier(QTI21Constants.MINSCORE_IDENTIFIER);
		maxScoreOutcomeDeclaration.setCardinality(Cardinality.SINGLE);
		maxScoreOutcomeDeclaration.setBaseType(BaseType.FLOAT);
		
		List<View> views = new ArrayList<>();
		views.add(View.TEST_CONSTRUCTOR);
		maxScoreOutcomeDeclaration.setViews(views);

		DefaultValue maxScoreDefaultVal = new DefaultValue(maxScoreOutcomeDeclaration);
		maxScoreOutcomeDeclaration.setDefaultValue(maxScoreDefaultVal);
		
		FieldValue maxScoreDefaultFieldVal = new FieldValue(maxScoreDefaultVal, new FloatValue(minScore));
		maxScoreDefaultVal.getFieldValues().add(maxScoreDefaultFieldVal);
		
		return maxScoreOutcomeDeclaration;
	}
	
	public static OutcomeDeclaration createOutcomeDeclarationForFeedbackBasic(AssessmentItem assessmentItem) {
		OutcomeDeclaration feedbackOutcomeDeclaration = new OutcomeDeclaration(assessmentItem);
		feedbackOutcomeDeclaration.setIdentifier(QTI21Constants.FEEDBACKBASIC_IDENTIFIER);
		feedbackOutcomeDeclaration.setCardinality(Cardinality.SINGLE);
		feedbackOutcomeDeclaration.setBaseType(BaseType.IDENTIFIER);
		
		DefaultValue feedbackDefaultVal = new DefaultValue(feedbackOutcomeDeclaration);
		feedbackOutcomeDeclaration.setDefaultValue(feedbackDefaultVal);
		
		FieldValue feedbackDefaultFieldVal = new FieldValue(feedbackDefaultVal, new IdentifierValue("empty"));
		feedbackDefaultVal.getFieldValues().add(feedbackDefaultFieldVal);
		
		List<View> views = new ArrayList<>();
		views.add(View.TEST_CONSTRUCTOR);
		feedbackOutcomeDeclaration.setViews(views);
		
		return feedbackOutcomeDeclaration;
	}
	
	public static OutcomeDeclaration createOutcomeDeclarationForFeedbackModal(AssessmentItem assessmentItem) {
		OutcomeDeclaration feedbackOutcomeDeclaration = new OutcomeDeclaration(assessmentItem);
		feedbackOutcomeDeclaration.setIdentifier(QTI21Constants.FEEDBACKMODAL_IDENTIFIER);
		feedbackOutcomeDeclaration.setCardinality(Cardinality.MULTIPLE);
		feedbackOutcomeDeclaration.setBaseType(BaseType.IDENTIFIER);
		
		List<View> views = new ArrayList<>();
		views.add(View.TEST_CONSTRUCTOR);
		feedbackOutcomeDeclaration.setViews(views);

		return feedbackOutcomeDeclaration;
	}
	
	public static ChoiceInteraction createSingleChoiceInteraction(AssessmentItem assessmentItem, Identifier responseDeclarationId) {
		ChoiceInteraction choiceInteraction = new ChoiceInteraction(assessmentItem.getItemBody());
		choiceInteraction.setMaxChoices(1);
		choiceInteraction.setShuffle(true);
		choiceInteraction.setResponseIdentifier(responseDeclarationId);
		
		PromptGroup prompts = new PromptGroup(choiceInteraction);
		choiceInteraction.getNodeGroups().add(prompts);
		
		SimpleChoiceGroup singleChoices = new SimpleChoiceGroup(choiceInteraction);
		choiceInteraction.getNodeGroups().add(singleChoices);
		return choiceInteraction;
	}
	
	public static ChoiceInteraction createMultipleChoiceInteraction(AssessmentItem assessmentItem, Identifier responseDeclarationId) {
		ChoiceInteraction choiceInteraction = new ChoiceInteraction(assessmentItem.getItemBody());
		choiceInteraction.setMaxChoices(0);
		choiceInteraction.setShuffle(true);
		choiceInteraction.setResponseIdentifier(responseDeclarationId);
		
		PromptGroup prompts = new PromptGroup(choiceInteraction);
		choiceInteraction.getNodeGroups().add(prompts);
		
		SimpleChoiceGroup singleChoices = new SimpleChoiceGroup(choiceInteraction);
		choiceInteraction.getNodeGroups().add(singleChoices);
		return choiceInteraction;
	}

	public static ModalFeedback createModalFeedback(AssessmentItem assessmentItem, Identifier identifier, String title, String text) {
		/*
		<modalFeedback identifier="Feedback1041659806" outcomeIdentifier="FEEDBACKMODAL" showHide="show" title="Wrong answer">
			<p>Feedback answer</p>
		</modalFeedback>
		*/
		
		ModalFeedback modalFeedback = new ModalFeedback(assessmentItem);
		modalFeedback.setIdentifier(identifier);
		modalFeedback.setOutcomeIdentifier(QTI21Constants.FEEDBACKMODAL_IDENTIFIER);
		modalFeedback.setVisibilityMode(VisibilityMode.parseVisibilityMode("show"));
		modalFeedback.getAttributes().getStringAttribute(ModalFeedback.ATTR_TITLE_NAME).setValue(title);
		
		new AssessmentHtmlBuilder().appendHtml(modalFeedback, text);
		
		/*List<Block> blocks = new AssessmentHTMLBuilder().parseHtml(text);
		for(Block block:blocks) {
			if(block instanceof FlowStatic) {
				modalFeedback.getFlowStatics().add((FlowStatic)block);
			}
		}*/

		return modalFeedback;
	}
	
	public static ResponseCondition createModalFeedbackBasicRule(ResponseProcessing responseProcessing, Identifier feedbackIdentifier, String inCorrect) {
		ResponseCondition rule = new ResponseCondition(responseProcessing);
		/*
		<responseIf>
			<and>
				<match>
					<baseValue baseType="identifier">correct</baseValue>
					<variable identifier="FEEDBACKBASIC" />
				</match>
			</and>
			<setOutcomeValue identifier="FEEDBACKMODAL">
				<multiple>
					<variable identifier="FEEDBACKMODAL" />
					<baseValue baseType="identifier">Feedback261171147</baseValue>
				</multiple>
			</setOutcomeValue>
		</responseIf>
		*/
		
		ResponseIf responseIf = new ResponseIf(rule);
		rule.setResponseIf(responseIf);
		
		{//rule
			And and = new And(responseIf);
			responseIf.getExpressions().add(and);
			
			Match match = new Match(and);
			and.getExpressions().add(match);
			
			BaseValue feedbackVal = new BaseValue(match);
			feedbackVal.setBaseTypeAttrValue(BaseType.IDENTIFIER);
			feedbackVal.setSingleValue(new IdentifierValue(inCorrect));
			match.getExpressions().add(feedbackVal);
			
			Variable variable = new Variable(match);
			variable.setIdentifier(ComplexReferenceIdentifier.parseString(QTI21Constants.FEEDBACKBASIC));
			match.getExpressions().add(variable);
		}

		{//outcome
			SetOutcomeValue feedbackVar = new SetOutcomeValue(responseIf);
			feedbackVar.setIdentifier(QTI21Constants.FEEDBACKMODAL_IDENTIFIER);
			
			Multiple multiple = new Multiple(feedbackVar);
			feedbackVar.setExpression(multiple);
			
			Variable variable = new Variable(multiple);
			variable.setIdentifier(ComplexReferenceIdentifier.parseString(QTI21Constants.FEEDBACKMODAL));
			multiple.getExpressions().add(variable);
			
			BaseValue feedbackVal = new BaseValue(feedbackVar);
			feedbackVal.setBaseTypeAttrValue(BaseType.IDENTIFIER);
			feedbackVal.setSingleValue(new IdentifierValue(feedbackIdentifier));
			multiple.getExpressions().add(feedbackVal);
			
			responseIf.getResponseRules().add(feedbackVar);
		}
		
		return rule;
	}
	
	public static ResponseProcessing createResponseProcessing(AssessmentItem assessmentItem, Identifier responseId) {
		ResponseProcessing responseProcessing = new ResponseProcessing(assessmentItem);

		ResponseCondition rule = new ResponseCondition(responseProcessing);
		
		//if no response
		ResponseIf responseIf = new ResponseIf(rule);
		rule.setResponseIf(responseIf);
		
		IsNull isNull = new IsNull(responseIf);
		responseIf.getExpressions().add(isNull);
		
		Variable variable = new Variable(isNull);
		variable.setIdentifier(ComplexReferenceIdentifier.parseString(responseId.toString()));
		isNull.getExpressions().add(variable);
		
		{
			SetOutcomeValue feedbackVar = new SetOutcomeValue(responseIf);
			feedbackVar.setIdentifier(QTI21Constants.FEEDBACKBASIC_IDENTIFIER);
			BaseValue feedbackVal = new BaseValue(feedbackVar);
			feedbackVal.setBaseTypeAttrValue(BaseType.IDENTIFIER);
			feedbackVal.setSingleValue(new IdentifierValue("empty"));
			feedbackVar.setExpression(feedbackVal);
			responseIf.getResponseRules().add(feedbackVar);
		}
		
		//else if correct response
		ResponseElseIf responseElseIf = new ResponseElseIf(rule);
		rule.getResponseElseIfs().add(responseElseIf);
		
		//match 
		{
			Match match = new Match(responseElseIf);
			responseElseIf.getExpressions().add(match);
			
			Variable responseVar = new Variable(match);
			responseVar.setIdentifier(ComplexReferenceIdentifier.parseString(responseId.toString()));
			match.getExpressions().add(responseVar);
			
			Correct correct = new Correct(match);
			correct.setIdentifier(ComplexReferenceIdentifier.parseString(responseId.toString()));
			match.getExpressions().add(correct);
		}

		// outcome score
		{
			SetOutcomeValue scoreOutcomeVar = new SetOutcomeValue(responseIf);
			scoreOutcomeVar.setIdentifier(QTI21Constants.SCORE_IDENTIFIER);
			responseElseIf.getResponseRules().add(scoreOutcomeVar);
			
			Sum sum = new Sum(scoreOutcomeVar);
			scoreOutcomeVar.getExpressions().add(sum);
			
			Variable scoreVar = new Variable(sum);
			scoreVar.setIdentifier(QTI21Constants.SCORE_CLX_IDENTIFIER);
			sum.getExpressions().add(scoreVar);
			
			Variable maxScoreVar = new Variable(sum);
			maxScoreVar.setIdentifier(QTI21Constants.MAXSCORE_CLX_IDENTIFIER);
			sum.getExpressions().add(maxScoreVar);
		}
		
		// outcome feedback
		{
			SetOutcomeValue correctFeedbackVar = new SetOutcomeValue(responseIf);
			correctFeedbackVar.setIdentifier(QTI21Constants.FEEDBACKBASIC_IDENTIFIER);
			BaseValue correctFeedbackVal = new BaseValue(correctFeedbackVar);
			correctFeedbackVal.setBaseTypeAttrValue(BaseType.IDENTIFIER);
			correctFeedbackVal.setSingleValue(QTI21Constants.CORRECT_IDENTIFIER_VALUE);
			correctFeedbackVar.setExpression(correctFeedbackVal);
			responseElseIf.getResponseRules().add(correctFeedbackVar);
		}

		// else failed
		ResponseElse responseElse = new ResponseElse(rule);
		rule.setResponseElse(responseElse);
		{// feedback incorrect
			SetOutcomeValue incorrectFeedbackVar = new SetOutcomeValue(responseIf);
			incorrectFeedbackVar.setIdentifier(QTI21Constants.FEEDBACKBASIC_IDENTIFIER);
			BaseValue incorrectFeedbackVal = new BaseValue(incorrectFeedbackVar);
			incorrectFeedbackVal.setBaseTypeAttrValue(BaseType.IDENTIFIER);
			incorrectFeedbackVal.setSingleValue(QTI21Constants.INCORRECT_IDENTIFIER_VALUE);
			incorrectFeedbackVar.setExpression(incorrectFeedbackVal);
			responseElse.getResponseRules().add(incorrectFeedbackVar);
		}

		responseProcessing.getResponseRules().add(rule);
		return responseProcessing;
	}

	
	public static P getParagraph(QtiNode parent, String content) {
		P paragraph = new P(parent);
		TextRun text = new TextRun(paragraph, content);
		paragraph.getInlines().add(text);
		return paragraph;
	}
}
