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
import java.util.StringTokenizer;

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
import uk.ac.ed.ph.jqtiplus.node.expression.Expression;
import uk.ac.ed.ph.jqtiplus.node.expression.general.BaseValue;
import uk.ac.ed.ph.jqtiplus.node.expression.general.Correct;
import uk.ac.ed.ph.jqtiplus.node.expression.general.MapResponse;
import uk.ac.ed.ph.jqtiplus.node.expression.general.Variable;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.And;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Equal;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Gt;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Gte;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.IsNull;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Lt;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Lte;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Match;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Member;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Multiple;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Not;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Shape;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Sum;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.ToleranceMode;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.CorrectResponse;
import uk.ac.ed.ph.jqtiplus.node.item.ModalFeedback;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.DrawingInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ExtendedTextInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.HotspotInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.HottextInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.MatchInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.OrderInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.TextEntryInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.UploadInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleAssociableChoice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleChoice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleMatchSet;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.content.Hottext;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.graphic.HotspotChoice;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.MapEntry;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.Mapping;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.ResponseDeclaration;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseCondition;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseConditionChild;
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
import uk.ac.ed.ph.jqtiplus.utils.QueryUtils;
import uk.ac.ed.ph.jqtiplus.value.BaseType;
import uk.ac.ed.ph.jqtiplus.value.Cardinality;
import uk.ac.ed.ph.jqtiplus.value.DirectedPairValue;
import uk.ac.ed.ph.jqtiplus.value.FloatValue;
import uk.ac.ed.ph.jqtiplus.value.IdentifierValue;
import uk.ac.ed.ph.jqtiplus.value.IntegerValue;
import uk.ac.ed.ph.jqtiplus.value.Orientation;
import uk.ac.ed.ph.jqtiplus.value.SingleValue;
import uk.ac.ed.ph.jqtiplus.value.StringValue;

/**
 * 
 * Initial date: 03.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentItemFactory {
	
	public static AssessmentItem createSingleChoice(String title, String defaultAnswer) {
		AssessmentItem assessmentItem = createAssessmentItem(QTI21QuestionType.sc, title);

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
		appendSimpleChoice(choiceInteraction, defaultAnswer, correctResponseId);

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
	
	/**
	 * Append the itemBody with an empty paragraph.
	 * 
	 * @param assessmentItem
	 * @return
	 */
	public static ItemBody appendDefaultItemBody(AssessmentItem assessmentItem) {
		NodeGroupList nodeGroups = assessmentItem.getNodeGroups();
		//the single choice interaction
		ItemBodyGroup itemBodyGroup = nodeGroups.getItemBodyGroup();
		ItemBody itemBody = new ItemBody(assessmentItem);
		itemBodyGroup.setItemBody(itemBody);
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
	<setOutcomeValue identifier="FEEDBACKBASIC">
		<baseValue baseType="identifier">
			correct
		</baseValue>
	</setOutcomeValue>
	*/
	public static void appendSetOutcomeFeedbackCorrect(ResponseConditionChild responseCondition) {
		SetOutcomeValue correctOutcomeValue = new SetOutcomeValue(responseCondition);
		correctOutcomeValue.setIdentifier(QTI21Constants.FEEDBACKBASIC_IDENTIFIER);
		responseCondition.getResponseRules().add(correctOutcomeValue);
		
		BaseValue correctValue = new BaseValue(correctOutcomeValue);
		correctValue.setBaseTypeAttrValue(BaseType.IDENTIFIER);
		correctValue.setSingleValue(QTI21Constants.CORRECT_IDENTIFIER_VALUE);
		correctOutcomeValue.setExpression(correctValue);
	}
	
	/*
	<setOutcomeValue identifier="FEEDBACKBASIC">
		<baseValue baseType="identifier">incorrect</baseValue>
	</setOutcomeValue>
	*/
	public static void appendSetOutcomeFeedbackIncorrect(ResponseConditionChild responseCondition) {
		SetOutcomeValue incorrectOutcomeValue = new SetOutcomeValue(responseCondition);
		incorrectOutcomeValue.setIdentifier(QTI21Constants.FEEDBACKBASIC_IDENTIFIER);
		responseCondition.getResponseRules().add(incorrectOutcomeValue);
		
		BaseValue incorrectValue = new BaseValue(incorrectOutcomeValue);
		incorrectValue.setBaseTypeAttrValue(BaseType.IDENTIFIER);
		incorrectValue.setSingleValue(QTI21Constants.INCORRECT_IDENTIFIER_VALUE);
		incorrectOutcomeValue.setExpression(incorrectValue);
	}
	
	/*
    <setOutcomeValue identifier="SCORE">
      <sum>
        <variable identifier="SCORE"/>
        <mapResponse identifier="RESPONSE_1"/>
      </sum>
    </setOutcomeValue>
	*/
	public static void appendSetOutcomeScoreMapResponse(ResponseConditionChild responseCondition, Identifier responseIdentifier) {
		SetOutcomeValue scoreOutcome = new SetOutcomeValue(responseCondition);
		scoreOutcome.setIdentifier(QTI21Constants.SCORE_IDENTIFIER);
		responseCondition.getResponseRules().add(scoreOutcome);
		
		Sum sum = new Sum(scoreOutcome);
		scoreOutcome.getExpressions().add(sum);
		
		Variable scoreVar = new Variable(sum);
		scoreVar.setIdentifier(QTI21Constants.SCORE_CLX_IDENTIFIER);
		sum.getExpressions().add(scoreVar);
		
		MapResponse mapResponse = new MapResponse(sum);
		mapResponse.setIdentifier(responseIdentifier);
		sum.getExpressions().add(mapResponse);
	}
	
	/*
	<setOutcomeValue identifier="SCORE">
	    <sum>
	      <variable identifier="SCORE"/>
	      <variable identifier="MAXSCORE"/>
	    </sum>
	  </setOutcomeValue>
	*/
	public static void appendSetOutcomeScoreMaxScore(ResponseConditionChild responseCondition) {
		SetOutcomeValue scoreOutcomeValue = new SetOutcomeValue(responseCondition);
		scoreOutcomeValue.setIdentifier(QTI21Constants.SCORE_IDENTIFIER);
		responseCondition.getResponseRules().add(scoreOutcomeValue);
		
		Sum sum = new Sum(scoreOutcomeValue);
		scoreOutcomeValue.getExpressions().add(sum);
		
		Variable scoreVar = new Variable(sum);
		scoreVar.setIdentifier(QTI21Constants.SCORE_CLX_IDENTIFIER);
		sum.getExpressions().add(scoreVar);
		
		Variable maxScoreVar = new Variable(sum);
		maxScoreVar.setIdentifier(QTI21Constants.MAXSCORE_CLX_IDENTIFIER);
		sum.getExpressions().add(maxScoreVar);
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
		coords.add(Integer.valueOf(55));
		coords.add(Integer.valueOf(77));
		coords.add(Integer.valueOf(16));
		choice.setCoords(coords);
		hotspotInteraction.getHotspotChoices().add(choice);
		
		return hotspotInteraction;
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
	
	public static ResponseDeclaration createHotspotCorrectResponseDeclaration(AssessmentItem assessmentItem, Identifier declarationId,
			List<Identifier> correctResponseIds, Cardinality cardinality) {
		ResponseDeclaration responseDeclaration = new ResponseDeclaration(assessmentItem);
		responseDeclaration.setIdentifier(declarationId);
		if(cardinality != null && (cardinality == Cardinality.SINGLE || cardinality == Cardinality.MULTIPLE)) {
			responseDeclaration.setCardinality(cardinality);
		} else if(correctResponseIds == null || correctResponseIds.isEmpty() || correctResponseIds.size() > 1) {
			responseDeclaration.setCardinality(Cardinality.MULTIPLE);
		} else {
			responseDeclaration.setCardinality(Cardinality.SINGLE);
		}
		responseDeclaration.setBaseType(BaseType.IDENTIFIER);

		CorrectResponse correctResponse = new CorrectResponse(responseDeclaration);
		responseDeclaration.setCorrectResponse(correctResponse);
		
		for(Identifier correctResponseId:correctResponseIds) {
			appendIdentifierValue(correctResponse, correctResponseId);
		}
		return responseDeclaration;
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
			List<TextEntryAlternative> alternatives, boolean useScoreOfAlternatives) {
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
			mapEntry.setCaseSensitive(Boolean.valueOf(caseSensitive));
			mapping.getMapEntries().add(mapEntry);
		}
		
		//map alternatives
		if(alternatives != null && !alternatives.isEmpty()) {
			for(TextEntryAlternative alternative:alternatives) {
				if(StringHelper.containsNonWhitespace(alternative.getAlternative())) {
					MapEntry mapEntry = new MapEntry(mapping);
					mapEntry.setMapKey(new StringValue(alternative.getAlternative()));
					if(useScoreOfAlternatives) {
						mapEntry.setMappedValue(alternative.getScore());
					} else {
						mapEntry.setMappedValue(score);
					}
					mapEntry.setCaseSensitive(Boolean.valueOf(caseSensitive));
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
	
	public static UploadInteraction appendUploadInteraction(ItemBody itemBody, Identifier responseDeclarationId) {
		UploadInteraction textInteraction = new UploadInteraction(itemBody);
		textInteraction.setResponseIdentifier(responseDeclarationId);
		itemBody.getBlocks().add(textInteraction);
		return textInteraction;
	}
	
	public static ResponseDeclaration createUploadResponseDeclaration(AssessmentItem assessmentItem, Identifier declarationId) {
		ResponseDeclaration responseDeclaration = new ResponseDeclaration(assessmentItem);
		responseDeclaration.setIdentifier(declarationId);
		responseDeclaration.setCardinality(Cardinality.SINGLE);
		responseDeclaration.setBaseType(BaseType.FILE);
		return responseDeclaration;
	}
	
	public static DrawingInteraction appendDrawingInteraction(ItemBody itemBody, Identifier responseDeclarationId) {
		DrawingInteraction textInteraction = new DrawingInteraction(itemBody);
		textInteraction.setResponseIdentifier(responseDeclarationId);
		itemBody.getBlocks().add(textInteraction);
		return textInteraction;
	}
	
	public static ResponseDeclaration createDrawingResponseDeclaration(AssessmentItem assessmentItem, Identifier declarationId) {
		ResponseDeclaration responseDeclaration = new ResponseDeclaration(assessmentItem);
		responseDeclaration.setIdentifier(declarationId);
		responseDeclaration.setCardinality(Cardinality.SINGLE);
		responseDeclaration.setBaseType(BaseType.FILE);
		return responseDeclaration;
	}
	
	public static MatchInteraction appendMatchInteractionForKPrim(ItemBody itemBody, Identifier responseDeclarationId, String defaultAnswer) {
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
			correctChoice.setMatchMin(1);
			correctChoice.setIdentifier(IdentifierGenerator.newNumberAsIdentifier(classic[i]));
			P question = getParagraph(correctChoice, defaultAnswer + " " + classic[i]);
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
	
	public static MatchInteraction appendMatchInteraction(ItemBody itemBody, Identifier responseDeclarationId) {
		MatchInteraction matchInteraction = new MatchInteraction(itemBody);
		matchInteraction.setResponseIdentifier(responseDeclarationId);
		matchInteraction.setMaxAssociations(0);
		matchInteraction.setShuffle(false);
		itemBody.getBlocks().add(matchInteraction);
		
		PromptGroup prompts = new PromptGroup(matchInteraction);
		matchInteraction.getNodeGroups().add(prompts);
		
		SimpleMatchSet sourceMatchSet = new SimpleMatchSet(matchInteraction);
		matchInteraction.getSimpleMatchSets().add(sourceMatchSet);
		
		String[] sources = new String[]{ "A", "B" };
		for(int i=0; i<sources.length; i++) {
			SimpleAssociableChoice sourceChoice = new SimpleAssociableChoice(sourceMatchSet);
			sourceChoice.setMatchMax(0);
			sourceChoice.setMatchMin(0);
			sourceChoice.setIdentifier(IdentifierGenerator.newNumberAsIdentifier(sources[i]));
			P question = getParagraph(sourceChoice, sources[i]);
			sourceChoice.getFlowStatics().add(question);
			sourceMatchSet.getSimpleAssociableChoices().add(sourceChoice);
		}
		
		SimpleMatchSet targetMatchSet = new SimpleMatchSet(matchInteraction);
		matchInteraction.getSimpleMatchSets().add(targetMatchSet);
		
		String[] targets = new String[]{ "M", "N" };
		for(int i=0; i<targets.length; i++) {
			SimpleAssociableChoice targetChoice = new SimpleAssociableChoice(sourceMatchSet);
			targetChoice.setMatchMax(0);
			targetChoice.setMatchMin(0);
			targetChoice.setIdentifier(IdentifierGenerator.newNumberAsIdentifier(targets[i]));
			P question = getParagraph(targetChoice, targets[i]);
			targetChoice.getFlowStatics().add(question);
			targetMatchSet.getSimpleAssociableChoices().add(targetChoice);
		}
		
		return matchInteraction;
	}
	
	public static MatchInteraction appendMatchInteractionTrueFalse(ItemBody itemBody,
			String unanswered, String right, String wrong, Identifier responseDeclarationId) {
		MatchInteraction matchInteraction = new MatchInteraction(itemBody);
		matchInteraction.setResponseIdentifier(responseDeclarationId);
		matchInteraction.setMaxAssociations(4);
		matchInteraction.setShuffle(false);
		itemBody.getBlocks().add(matchInteraction);
		
		PromptGroup prompts = new PromptGroup(matchInteraction);
		matchInteraction.getNodeGroups().add(prompts);
		
		SimpleMatchSet sourceMatchSet = new SimpleMatchSet(matchInteraction);
		matchInteraction.getSimpleMatchSets().add(sourceMatchSet);
		
		String[] sources = new String[]{ "A", "B" };
		for(int i=0; i<sources.length; i++) {
			appendSimpleAssociableChoice(sourceMatchSet, sources[i], sources[i], 1, 1);
		}
		
		SimpleMatchSet targetMatchSet = new SimpleMatchSet(matchInteraction);
		matchInteraction.getSimpleMatchSets().add(targetMatchSet);
		appendSimpleAssociableChoice(targetMatchSet, "unanswered", unanswered, 0, 0);
		appendSimpleAssociableChoice(targetMatchSet, "right", right, 0, 0);
		appendSimpleAssociableChoice(targetMatchSet, "wrong", wrong, 0, 0);
		
		return matchInteraction;
	}
	
	public static void appendSimpleAssociableChoice(SimpleMatchSet matchSet, String identifierPrefix, String value, int matchMax, int matchMin) {
		SimpleAssociableChoice choice = new SimpleAssociableChoice(matchSet);
		choice.setMatchMax(matchMax);
		choice.setMatchMin(matchMin);
		choice.setIdentifier(IdentifierGenerator.newNumberAsIdentifier(identifierPrefix));
		P question = getParagraph(choice, value);
		choice.getFlowStatics().add(question);
		matchSet.getSimpleAssociableChoices().add(choice);
	}
	
	public static SimpleAssociableChoice createSimpleAssociableChoice(String text, SimpleMatchSet matchSet) {
		SimpleAssociableChoice targetChoice = new SimpleAssociableChoice(matchSet);
		targetChoice.setMatchMax(0);
		targetChoice.setMatchMin(0);
		targetChoice.setIdentifier(IdentifierGenerator.newNumberAsIdentifier("sa"));
		P question = getParagraph(targetChoice, text);
		targetChoice.getFlowStatics().add(question);
		return targetChoice;
	}
	
	/**
	 * Add the response declaration with correct answers (but without score mapping)
	 * 
	 * @param assessmentItem
	 * @param declarationId
	 * @param associations
	 * @return
	 */
	public static ResponseDeclaration createMatchResponseDeclaration(AssessmentItem assessmentItem, Identifier declarationId,
			Map<Identifier, List<Identifier>> associations) {
		ResponseDeclaration responseDeclaration = new ResponseDeclaration(assessmentItem);
		responseDeclaration.setIdentifier(declarationId);
		responseDeclaration.setCardinality(Cardinality.MULTIPLE);
		responseDeclaration.setBaseType(BaseType.DIRECTED_PAIR);
		return appendAssociationMatchResponseDeclaration(responseDeclaration, associations);
	}
	
	public static ResponseDeclaration appendAssociationMatchResponseDeclaration(ResponseDeclaration responseDeclaration,
			Map<Identifier, List<Identifier>> associations) {
		responseDeclaration.setCardinality(Cardinality.MULTIPLE);
		responseDeclaration.setBaseType(BaseType.DIRECTED_PAIR);

		//correct response
		CorrectResponse correctResponse = new CorrectResponse(responseDeclaration);
		responseDeclaration.setCorrectResponse(correctResponse);
		for(Map.Entry<Identifier,List<Identifier>> association:associations.entrySet()) {
			Identifier sourceChoiceId = association.getKey();
			List<Identifier> targetChoiceIds = association.getValue();
			
			for(Identifier targetChoiceId:targetChoiceIds) {
				DirectedPairValue dpValue = new DirectedPairValue(sourceChoiceId, targetChoiceId);
				FieldValue fValue = new FieldValue(correctResponse, dpValue);
				correctResponse.getFieldValues().add(fValue);
			}
		}
		
		return responseDeclaration;
	}
	
	public static OrderInteraction createOrderInteraction(AssessmentItem assessmentItem, Identifier responseDeclarationId, Orientation orientation) {
		OrderInteraction orderInteraction = new OrderInteraction(assessmentItem.getItemBody());
		orderInteraction.setMaxChoices(0);
		orderInteraction.setMinChoices(0);
		orderInteraction.setShuffle(true);
		if(orientation != null) {
			orderInteraction.setOrientation(orientation);
		}
		orderInteraction.setResponseIdentifier(responseDeclarationId);
		
		PromptGroup prompts = new PromptGroup(orderInteraction);
		orderInteraction.getNodeGroups().add(prompts);
		
		SimpleChoiceGroup singleChoices = new SimpleChoiceGroup(orderInteraction);
		orderInteraction.getNodeGroups().add(singleChoices);
		return orderInteraction;
	}
	
	public static OrderInteraction appendOrderInteraction(ItemBody itemBody, Identifier responseDeclarationId, int maxChoices, boolean shuffle) {
		OrderInteraction orderInteraction = new OrderInteraction(itemBody);
		orderInteraction.setMaxChoices(maxChoices);
		orderInteraction.setMinChoices(0);
		orderInteraction.setShuffle(shuffle);
		orderInteraction.setResponseIdentifier(responseDeclarationId);
		itemBody.getBlocks().add(orderInteraction);
		
		PromptGroup prompts = new PromptGroup(orderInteraction);
		orderInteraction.getNodeGroups().add(prompts);
		
		SimpleChoiceGroup singleChoices = new SimpleChoiceGroup(orderInteraction);
		orderInteraction.getNodeGroups().add(singleChoices);
		return orderInteraction;
	}
	
	public static ResponseDeclaration createOrderCorrectResponseDeclaration(AssessmentItem assessmentItem, Identifier declarationId, List<Identifier> correctResponseIds) {
		ResponseDeclaration responseDeclaration = new ResponseDeclaration(assessmentItem);
		responseDeclaration.setIdentifier(declarationId);
		responseDeclaration.setCardinality(Cardinality.ORDERED);
		responseDeclaration.setBaseType(BaseType.IDENTIFIER);

		CorrectResponse correctResponse = new CorrectResponse(responseDeclaration);
		responseDeclaration.setCorrectResponse(correctResponse);
		
		for(Identifier correctResponseId:correctResponseIds) {
			appendIdentifierValue(correctResponse, correctResponseId);
		}
		return responseDeclaration;
	}
	
	public static SimpleChoice createSimpleChoice(OrderInteraction orderInteraction, String text, String prefix) {
		SimpleChoice newChoice = new SimpleChoice(orderInteraction);
		newChoice.setIdentifier(IdentifierGenerator.newAsIdentifier(prefix));
		P firstChoiceText = AssessmentItemFactory.getParagraph(newChoice, text);
		newChoice.getFlowStatics().add(firstChoiceText);
		return newChoice;
	}
	
	public static SimpleChoice appendSimpleChoice(OrderInteraction orderInteraction, String text, Identifier identifier) {
		SimpleChoice newChoice = new SimpleChoice(orderInteraction);
		newChoice.setIdentifier(identifier);
		P firstChoiceText = AssessmentItemFactory.getParagraph(newChoice, text);
		newChoice.getFlowStatics().add(firstChoiceText);
		orderInteraction.getNodeGroups().getSimpleChoiceGroup().getSimpleChoices().add(newChoice);
		return newChoice;
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
	
	public static HottextInteraction appendHottextInteraction(ItemBody itemBody, Identifier responseDeclarationId, int maxChoices) {
		HottextInteraction hottextInteraction = new HottextInteraction(itemBody);
		hottextInteraction.setMaxChoices(maxChoices);
		hottextInteraction.setResponseIdentifier(responseDeclarationId);
		itemBody.getBlocks().add(hottextInteraction);
		
		PromptGroup prompts = new PromptGroup(hottextInteraction);
		hottextInteraction.getNodeGroups().add(prompts);

		return hottextInteraction;
	}
	
	public static Hottext appendHottext(P parent, Identifier responseId, String text) {
		Hottext hottext = new Hottext(parent);
		hottext.setIdentifier(responseId);
		hottext.getInlineStatics().add(new TextRun(hottext, text));
		parent.getInlines().add(hottext);
		return hottext;
	}
	
	public static ResponseDeclaration createHottextCorrectResponseDeclaration(AssessmentItem assessmentItem, Identifier declarationId, List<Identifier> correctResponseIds) {
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
	
	//<responseDeclaration identifier="HINTREQUEST" cardinality="single" baseType="boolean"/>
	public static ResponseDeclaration createHintRequestResponseDeclaration(AssessmentItem assessmentItem) {
		ResponseDeclaration responseDeclaration = new ResponseDeclaration(assessmentItem);
		responseDeclaration.setIdentifier(QTI21Constants.HINT_REQUEST_IDENTIFIER);
		responseDeclaration.setCardinality(Cardinality.SINGLE);
		responseDeclaration.setBaseType(BaseType.BOOLEAN);
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
	
	public static SimpleChoice appendSimpleChoice(ChoiceInteraction choiceInteraction, String text, Identifier identifier) {
		SimpleChoice newChoice = new SimpleChoice(choiceInteraction);
		newChoice.setIdentifier(identifier);
		P firstChoiceText = AssessmentItemFactory.getParagraph(newChoice, text);
		newChoice.getFlowStatics().add(firstChoiceText);
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
	
	public static Mapping appendPairMapping(ResponseDeclaration responseDeclaration, Map<DirectedPairValue,Double> map) {
		Mapping mapping = new Mapping(responseDeclaration);
		mapping.setDefaultValue(0d);
		responseDeclaration.setMapping(mapping);
		for(Map.Entry<DirectedPairValue, Double> entry:map.entrySet()) {
			MapEntry mapEntry = new MapEntry(mapping);
			mapEntry.setMapKey(entry.getKey());
			mapEntry.setMappedValue(entry.getValue());
			mapping.getMapEntries().add(mapEntry);
		}
		return mapping;
	}
	
	
	/**
	 * Create an outcome declaration with SCORE as identifier, single and float
	 * and 0 as default value.
	 * 
	 * @param assessmentItem
	 * @return
	 */
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
			<value>0</value>
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
		
		FieldValue feedbackDefaultFieldVal = new FieldValue(feedbackDefaultVal, new IdentifierValue("none"));
		feedbackDefaultVal.getFieldValues().add(feedbackDefaultFieldVal);
		
		List<View> views = new ArrayList<>();
		views.add(View.TEST_CONSTRUCTOR);
		feedbackOutcomeDeclaration.setViews(views);
		
		return feedbackOutcomeDeclaration;
	}
	
	public static OutcomeDeclaration createOutcomeDeclarationForHint(AssessmentItem assessmentItem) {
		OutcomeDeclaration feedbackOutcomeDeclaration = new OutcomeDeclaration(assessmentItem);
		feedbackOutcomeDeclaration.setIdentifier(QTI21Constants.HINT_FEEDBACKMODAL_IDENTIFIER);
		feedbackOutcomeDeclaration.setCardinality(Cardinality.SINGLE);
		feedbackOutcomeDeclaration.setBaseType(BaseType.IDENTIFIER);

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
	
	public static OutcomeDeclaration createOutcomeDeclarationForCorrectSolutionFeedbackModal(AssessmentItem assessmentItem) {
		OutcomeDeclaration feedbackOutcomeDeclaration = new OutcomeDeclaration(assessmentItem);
		feedbackOutcomeDeclaration.setIdentifier(QTI21Constants.CORRECT_SOLUTION_IDENTIFIER);
		feedbackOutcomeDeclaration.setCardinality(Cardinality.SINGLE);
		feedbackOutcomeDeclaration.setBaseType(BaseType.IDENTIFIER);
		
		List<View> views = new ArrayList<>();
		views.add(View.TEST_CONSTRUCTOR);
		feedbackOutcomeDeclaration.setViews(views);

		return feedbackOutcomeDeclaration;
	}
	
	public static ChoiceInteraction createSingleChoiceInteraction(AssessmentItem assessmentItem, Identifier responseDeclarationId,
			Orientation orientation, List<String> classAtrr) {
		ChoiceInteraction choiceInteraction = new ChoiceInteraction(assessmentItem.getItemBody());
		choiceInteraction.setMaxChoices(1);
		choiceInteraction.setShuffle(true);
		if(orientation != null) {
			choiceInteraction.setOrientation(orientation);
		}
		choiceInteraction.setResponseIdentifier(responseDeclarationId);
		if(classAtrr != null && classAtrr.size() > 0) {
			choiceInteraction.setClassAttr(classAtrr);
		}
		
		PromptGroup prompts = new PromptGroup(choiceInteraction);
		choiceInteraction.getNodeGroups().add(prompts);
		
		SimpleChoiceGroup singleChoices = new SimpleChoiceGroup(choiceInteraction);
		choiceInteraction.getNodeGroups().add(singleChoices);
		return choiceInteraction;
	}
	
	public static ChoiceInteraction createMultipleChoiceInteraction(AssessmentItem assessmentItem, Identifier responseDeclarationId,
			Orientation orientation, List<String> classAtrr) {
		ChoiceInteraction choiceInteraction = new ChoiceInteraction(assessmentItem.getItemBody());
		choiceInteraction.setMaxChoices(0);
		choiceInteraction.setShuffle(true);
		if(orientation != null) {
			choiceInteraction.setOrientation(orientation);
		}
		if(classAtrr != null && !classAtrr.isEmpty()) {
			choiceInteraction.setClassAttr(classAtrr);
		}
		choiceInteraction.setResponseIdentifier(responseDeclarationId);
		
		PromptGroup prompts = new PromptGroup(choiceInteraction);
		choiceInteraction.getNodeGroups().add(prompts);
		
		SimpleChoiceGroup singleChoices = new SimpleChoiceGroup(choiceInteraction);
		choiceInteraction.getNodeGroups().add(singleChoices);
		return choiceInteraction;
	}
	
	public static void ensureFeedbackBasicOutcomeDeclaration(AssessmentItem assessmentItem) {
		OutcomeDeclaration feedbackBasicDeclaration = assessmentItem.getOutcomeDeclaration(QTI21Constants.FEEDBACKBASIC_IDENTIFIER);
		if(feedbackBasicDeclaration == null) {
			feedbackBasicDeclaration = createOutcomeDeclarationForFeedbackBasic(assessmentItem);
			assessmentItem.getOutcomeDeclarations().add(feedbackBasicDeclaration);	
		}
	}

	public static ModalFeedback createModalFeedback(AssessmentItem assessmentItem, Identifier identifier, String title, String text) {
		return createModalFeedback(assessmentItem, QTI21Constants.FEEDBACKMODAL_IDENTIFIER, identifier, title, text);
	}
	
	public static ModalFeedback createModalFeedback(AssessmentItem assessmentItem, Identifier outcomeIdentifier, Identifier identifier, 
			String title, String text) {
		/*
		<modalFeedback identifier="Feedback1041659806" outcomeIdentifier="FEEDBACKMODAL" showHide="show" title="Wrong answer">
			<p>Feedback answer</p>
		</modalFeedback>
		*/
		
		ModalFeedback modalFeedback = new ModalFeedback(assessmentItem);
		modalFeedback.setIdentifier(identifier);
		modalFeedback.setOutcomeIdentifier(outcomeIdentifier);
		modalFeedback.setVisibilityMode(VisibilityMode.parseVisibilityMode("show"));
		modalFeedback.getAttributes().getStringAttribute(ModalFeedback.ATTR_TITLE_NAME).setValue(title);
		
		new AssessmentHtmlBuilder().appendHtml(modalFeedback, text, true);
		return modalFeedback;
	}
	
	/**
	 * the additional feedback have only responseIf
	 * 
	 * 
	 * @param item
	 * @param feedback
	 * @return
	 */
	public static boolean matchAdditionalFeedback(AssessmentItem item, ModalFeedback feedback) {
		List<ResponseRule> responseRules = item.getResponseProcessing().getResponseRules();
		for(ResponseRule responseRule:responseRules) {
			if(responseRule instanceof ResponseCondition) {
				ResponseCondition responseCondition = (ResponseCondition)responseRule;
				if(responseCondition.getResponseIf() == null || responseCondition.getResponseElse() != null
						|| (responseCondition.getResponseElseIfs() != null && !responseCondition.getResponseElseIfs().isEmpty())) {
					continue;
				}
				
				ResponseIf responseIf = responseCondition.getResponseIf();
				List<ResponseRule> ifResponseRules = responseIf.getResponseRules();
				if(ifResponseRules == null || ifResponseRules.size() != 1 || !(ifResponseRules.get(0) instanceof SetOutcomeValue)) {
					continue;
				}
				
				SetOutcomeValue setOutcomeValue = (SetOutcomeValue)responseIf.getResponseRules().get(0);
				if(!findBaseValueInExpression(setOutcomeValue.getExpression(), feedback.getIdentifier())) {
					continue;
				}
				
				List<Expression> expressions = responseIf.getExpressions();
				if(expressions == null || expressions.size() != 1 || !(expressions.get(0) instanceof And)) {
					continue;
				}
				
				List<Variable> variables = QueryUtils.search(Variable.class, expressions.get(0));
				if(variables != null && variables.size() == 1) {
					Variable bValue = variables.get(0);
					ComplexReferenceIdentifier identifier = bValue.getIdentifier();
					if(identifier.equals(QTI21Constants.SCORE_CLX_IDENTIFIER)
							|| identifier.equals(QTI21Constants.NUM_ATTEMPTS_CLX_IDENTIFIER)) {
						return true;
					}
					if(identifier.equals(QTI21Constants.CORRECT_CLX_IDENTIFIER)
							|| identifier.equals(QTI21Constants.INCORRECT_CLX_IDENTIFIER)
							|| identifier.equals(QTI21Constants.EMPTY_CLX_IDENTIFIER)) {
						return false;
					}
					String identifierToString = identifier.toString();
					if(identifierToString.contains("RESPONSE_")) {
						return true;
					}
				}	
			}
		}

		return false;
	}
	
	public static ResponseCondition createModalFeedbackRuleWithConditions(ResponseProcessing responseProcessing,
			Identifier feedbackIdentifier, Identifier responseIdentifier, Cardinality cardinality, List<ModalFeedbackCondition> conditions) {
		ResponseCondition rule = new ResponseCondition(responseProcessing);
		
		/*
		<responseCondition>
			<responseIf>
				<and>
					<equal toleranceMode="exact">
						<variable identifier="SCORE" />
						<baseValue baseType="float">
							4
						</baseValue>
					</equal>
				</and>
				<setOutcomeValue identifier="FEEDBACKMODAL">
					<multiple>
						<variable identifier="FEEDBACKMODAL" />
						<baseValue baseType="identifier">
							Feedback2074019497
						</baseValue>
					</multiple>
				</setOutcomeValue>
			</responseIf>
		</responseCondition>
		*/
		
		ResponseIf responseIf = new ResponseIf(rule);
		rule.setResponseIf(responseIf);
		
		{//rule
			And and = new And(responseIf);
			responseIf.getExpressions().add(and);
			for(ModalFeedbackCondition condition:conditions) {
				appendModalFeedbackCondition(condition, responseIdentifier, cardinality, and);
			}
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
	
	private static void appendModalFeedbackCondition(ModalFeedbackCondition condition, Identifier responseIdentifier, Cardinality cardinality, And and) {
		ModalFeedbackCondition.Variable var = condition.getVariable();
		ModalFeedbackCondition.Operator operator = condition.getOperator();
		String value = condition.getValue();
		
		if(var == ModalFeedbackCondition.Variable.response) {
			if(cardinality == Cardinality.MULTIPLE) {
				if(operator == ModalFeedbackCondition.Operator.equals) {
					Member member = new Member(and);
					and.getExpressions().add(member);
					appendVariableBaseValue(var, value, responseIdentifier, member, true);
				} else if(operator == ModalFeedbackCondition.Operator.notEquals) {
					Not not = new Not(and);
					and.getExpressions().add(not);
					
					Member member = new Member(not);
					not.getExpressions().add(member);
					appendVariableBaseValue(var, value, responseIdentifier, member, true);
				}
			} else {
				if(operator == ModalFeedbackCondition.Operator.equals) {
					Match match = new Match(and);
					and.getExpressions().add(match);
					appendVariableBaseValue(var, value, responseIdentifier, match, false);
				} else if(operator == ModalFeedbackCondition.Operator.notEquals) {
					Not not = new Not(and);
					and.getExpressions().add(not);
					
					Match match = new Match(not);
					not.getExpressions().add(match);
					appendVariableBaseValue(var, value, responseIdentifier, match, false);
				}
			}
		} else {
			switch(operator) {
				case bigger: {
					Gt gt = new Gt(and);
					and.getExpressions().add(gt);
					appendVariableBaseValue(var, value, responseIdentifier, gt, false);
					break;
				}
				case biggerEquals: {
					Gte gte = new Gte(and);
					and.getExpressions().add(gte);
					appendVariableBaseValue(var, value, responseIdentifier, gte, false);
					break;
				}
				case equals: {
					Equal equal = new Equal(and);
					equal.setToleranceMode(ToleranceMode.EXACT);
					and.getExpressions().add(equal);
					appendVariableBaseValue(var, value, responseIdentifier, equal, false);
					break;
				}
				case notEquals: {
					Not not = new Not(and);
					and.getExpressions().add(not);
					Equal equal = new Equal(not);
					equal.setToleranceMode(ToleranceMode.EXACT);
					not.getExpressions().add(equal);
					appendVariableBaseValue(var, value, responseIdentifier, equal, false);
					break;
				}
				case smaller: {
					Lt lt = new Lt(and);
					and.getExpressions().add(lt);
					appendVariableBaseValue(var, value, responseIdentifier, lt, false);
					break;
				}
				case smallerEquals: {
					Lte lte = new Lte(and);
					and.getExpressions().add(lte);
					appendVariableBaseValue(var, value, responseIdentifier, lte, false);
					break;
				}
			}
		}
	}
	
	/*
		<variable identifier="SCORE" />
		<baseValue baseType="float">4</baseValue>
	 */
	/**
	 * 
	 * @param var
	 * @param value
	 * @param responseIdentifier
	 * @param parentExpression
	 * @param reverse if true, reverse the order, first baseValue and then variable
	 */
	private static void appendVariableBaseValue(ModalFeedbackCondition.Variable var, String value,
			Identifier responseIdentifier, Expression parentExpression, boolean reverse) {

		Variable variable = new Variable(parentExpression);
		BaseValue bValue = new BaseValue(parentExpression);
		if(reverse) {
			parentExpression.getExpressions().add(bValue);
			parentExpression.getExpressions().add(variable);
		} else {
			parentExpression.getExpressions().add(variable);
			parentExpression.getExpressions().add(bValue);
		}

		switch(var) {
			case score:
				bValue.setBaseTypeAttrValue(BaseType.FLOAT);
				bValue.setSingleValue(new FloatValue(Double.parseDouble(value)));
				variable.setIdentifier(QTI21Constants.SCORE_CLX_IDENTIFIER);
				break;
			case attempts:
				bValue.setBaseTypeAttrValue(BaseType.INTEGER);
				bValue.setSingleValue(new IntegerValue(Integer.parseInt(value)));
				variable.setIdentifier(QTI21Constants.NUM_ATTEMPTS_CLX_IDENTIFIER);
				break;	
			case response:
				bValue.setBaseTypeAttrValue(BaseType.IDENTIFIER);
				bValue.setSingleValue(new IdentifierValue(Identifier.parseString(value)));
				variable.setIdentifier(ComplexReferenceIdentifier.parseString(responseIdentifier.toString()));
				break;
		}
	}
	
	public static ResponseCondition createModalFeedbackBasicRule(ResponseProcessing responseProcessing,
			Identifier feedbackIdentifier, String inCorrect, boolean hint) {
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
			
			//not match the HINT
			if(hint) {
				IsNull isNull = new IsNull(and);
				and.getExpressions().add(isNull);
				
				Variable hintVar = new Variable(isNull);
				hintVar.setIdentifier(QTI21Constants.HINT_FEEDBACKMODAL_CLX_IDENTIFIER);
				isNull.getExpressions().add(hintVar);
			}
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
	
	
	/**
	 * Generate the special case for "correct solution" feedback which is almost the same as
	 * incorrect feedback.
	 * 
	 * @param responseProcessing
	 * @param feedbackIdentifier
	 * @return
	 */
	public static ResponseCondition createCorrectSolutionModalFeedbackBasicRule(ResponseProcessing responseProcessing,
			Identifier correctSolutionFeedbackIdentifier, Identifier incorrectFeedbackIdentifier, boolean hint) {
		ResponseCondition rule = new ResponseCondition(responseProcessing);
		/*
		<responseIf>
			<and>
				<match>
					<baseValue baseType="identifier">incorrect</baseValue>
					<variable identifier="FEEDBACKBASIC" />
				</match>
			</and>
			<setOutcomeValue identifier="FEEDBACKMODAL">
				<multiple>
					<variable identifier="FEEDBACKMODAL" />
					<baseValue baseType="identifier">Feedback261171147</baseValue>
				</multiple>
			</setOutcomeValue>
			<setOutcomeValue identifier="SOLUTIONMODAL">
				<baseValue baseType="identifier">Feedback261171147</baseValue>
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
			feedbackVal.setSingleValue(new IdentifierValue(QTI21Constants.INCORRECT));
			match.getExpressions().add(feedbackVal);
			
			Variable variable = new Variable(match);
			variable.setIdentifier(ComplexReferenceIdentifier.parseString(QTI21Constants.FEEDBACKBASIC));
			match.getExpressions().add(variable);
			
			//not match the HINT
			if(hint) {
				IsNull isNull = new IsNull(and);
				and.getExpressions().add(isNull);
				
				Variable hintVar = new Variable(isNull);
				hintVar.setIdentifier(QTI21Constants.HINT_FEEDBACKMODAL_CLX_IDENTIFIER);
				isNull.getExpressions().add(hintVar);
			}
		}
		
		if(incorrectFeedbackIdentifier != null) {//outcome incorrect
			SetOutcomeValue feedbackVar = new SetOutcomeValue(responseIf);
			feedbackVar.setIdentifier(QTI21Constants.FEEDBACKMODAL_IDENTIFIER);
			
			Multiple multiple = new Multiple(feedbackVar);
			feedbackVar.setExpression(multiple);
			
			Variable variable = new Variable(multiple);
			variable.setIdentifier(ComplexReferenceIdentifier.parseString(QTI21Constants.FEEDBACKMODAL));
			multiple.getExpressions().add(variable);
			
			BaseValue feedbackVal = new BaseValue(feedbackVar);
			feedbackVal.setBaseTypeAttrValue(BaseType.IDENTIFIER);
			feedbackVal.setSingleValue(new IdentifierValue(incorrectFeedbackIdentifier));
			multiple.getExpressions().add(feedbackVal);
			
			responseIf.getResponseRules().add(feedbackVar);
		}

		if(correctSolutionFeedbackIdentifier != null) {//outcome correct solution
			SetOutcomeValue feedbackVar = new SetOutcomeValue(responseIf);
			feedbackVar.setIdentifier(QTI21Constants.CORRECT_SOLUTION_IDENTIFIER);

			BaseValue feedbackVal = new BaseValue(feedbackVar);
			feedbackVal.setBaseTypeAttrValue(BaseType.IDENTIFIER);
			feedbackVal.setSingleValue(new IdentifierValue(correctSolutionFeedbackIdentifier));
			feedbackVar.getExpressions().add(feedbackVal);
			
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
	
	/**
	 * This generate a response rule which compare the max score and the score
	 * to set the feedback as "correct".
	 * 
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
	public static ResponseCondition createModalFeedbackResponseConditionByScore(ResponseProcessing responseProcessing) {
		ResponseCondition responseCondition = new ResponseCondition(responseProcessing);

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

		//SCORE >= MAXSCORE ( > is for security and special case where the max score is smalle than the sum of correct answers)
		Gte greaterOrEqual = new Gte(and);
		and.getExpressions().add(greaterOrEqual);
		
		Variable scoreVar = new Variable(greaterOrEqual);
		scoreVar.setIdentifier(QTI21Constants.SCORE_CLX_IDENTIFIER);
		greaterOrEqual.getExpressions().add(scoreVar);
		
		Variable maxScoreVar = new Variable(greaterOrEqual);
		maxScoreVar.setIdentifier(QTI21Constants.MAXSCORE_CLX_IDENTIFIER);
		greaterOrEqual.getExpressions().add(maxScoreVar);

		//outcome value
		SetOutcomeValue correctOutcomeValue = new SetOutcomeValue(responseIf);
		correctOutcomeValue.setIdentifier(QTI21Constants.FEEDBACKBASIC_IDENTIFIER);
		responseIf.getResponseRules().add(correctOutcomeValue);
		
		BaseValue correctValue = new BaseValue(correctOutcomeValue);
		correctValue.setBaseTypeAttrValue(BaseType.IDENTIFIER);
		correctValue.setSingleValue(QTI21Constants.CORRECT_IDENTIFIER_VALUE);
		correctOutcomeValue.setExpression(correctValue);
		
		return responseCondition;
	}

	public static P getParagraph(QtiNode parent, String content) {
		P paragraph = new P(parent);
		TextRun text = new TextRun(paragraph, content);
		paragraph.getInlines().add(text);
		return paragraph;
	}
	
	public static String coordsString(List<Integer> coords) {
		StringBuilder sb = new StringBuilder();
		if(coords != null && !coords.isEmpty()) {
			for(Integer coord:coords) {
				if(sb.length() > 0) sb.append(",");
				sb.append(coord.intValue());
			}
		}
		return sb.toString();
	}
	
	public static List<Integer> coordsList(String coords) {
		List<Integer> list = new ArrayList<>();
		if(StringHelper.containsNonWhitespace(coords)) {
			for(StringTokenizer tokenizer = new StringTokenizer(coords, ","); tokenizer.hasMoreElements(); ) {
				String coord = tokenizer.nextToken();
				list.add(Integer.valueOf(Math.round(Float.parseFloat(coord))));
			}
		}
		return list;
	}
	
	public static boolean findBaseValueInExpression(Expression expression, Identifier feedbackIdentifier) {
		if(expression instanceof BaseValue) {
			BaseValue bValue = (BaseValue)expression;
			SingleValue sValue = bValue.getSingleValue();
			if(sValue instanceof IdentifierValue) {
				IdentifierValue iValue = (IdentifierValue)sValue;
				if(feedbackIdentifier.equals(iValue.identifierValue())) {
					return true;
				}
			}
		} else {
			List<Expression> childExpressions = expression.getExpressions();
			for(Expression childExpression:childExpressions) {
				if(findBaseValueInExpression(childExpression, feedbackIdentifier)) {
					return true;
				}
			}
		}
		return false;
	}
	

}
