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
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.model.IdentifierGenerator;

import uk.ac.ed.ph.jqtiplus.group.NodeGroupList;
import uk.ac.ed.ph.jqtiplus.group.item.ItemBodyGroup;
import uk.ac.ed.ph.jqtiplus.group.item.interaction.PromptGroup;
import uk.ac.ed.ph.jqtiplus.group.item.interaction.choice.SimpleChoiceGroup;
import uk.ac.ed.ph.jqtiplus.group.item.response.processing.ResponseProcessingGroup;
import uk.ac.ed.ph.jqtiplus.group.outcome.declaration.OutcomeDeclarationGroup;
import uk.ac.ed.ph.jqtiplus.node.QtiNode;
import uk.ac.ed.ph.jqtiplus.node.content.ItemBody;
import uk.ac.ed.ph.jqtiplus.node.content.basic.TextRun;
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
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Sum;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.CorrectResponse;
import uk.ac.ed.ph.jqtiplus.node.item.ModalFeedback;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleChoice;
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
import uk.ac.ed.ph.jqtiplus.value.FloatValue;
import uk.ac.ed.ph.jqtiplus.value.IdentifierValue;

/**
 * 
 * Initial date: 03.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentItemFactory {
	
	public static AssessmentItem createSingleChoice() {
		
		AssessmentItem assessmentItem = new AssessmentItem();
		assessmentItem.setIdentifier(IdentifierGenerator.newAsString("item"));
		assessmentItem.setTitle("Single choice");
		assessmentItem.setToolName(QTI21Constants.TOOLNAME);
		assessmentItem.setToolVersion(Settings.getVersion());
		assessmentItem.setAdaptive(Boolean.FALSE);
		assessmentItem.setTimeDependent(Boolean.FALSE);
		
		NodeGroupList nodeGroups = assessmentItem.getNodeGroups();

		Identifier responseDeclarationId = Identifier.assumedLegal("RESPONSE_1");
		Identifier correctResponseId = IdentifierGenerator.newAsIdentifier("sc");
		//define correct answer
		ResponseDeclaration responseDeclaration = createSingleChoiceCorrectResponseDeclaration(assessmentItem, responseDeclarationId, correctResponseId);
		nodeGroups.getResponseDeclarationGroup().getResponseDeclarations().add(responseDeclaration);
		
		//outcomes
		OutcomeDeclarationGroup outcomeDeclarations = nodeGroups.getOutcomeDeclarationGroup();

		//outcome score
		OutcomeDeclaration scoreOutcomeDeclaration = createOutcomeDeclarationForScore(assessmentItem);
		outcomeDeclarations.getOutcomeDeclarations().add(scoreOutcomeDeclaration);

		// outcome max score
		OutcomeDeclaration maxScoreOutcomeDeclaration = createOutcomeDeclarationForMaxScore(assessmentItem, 1.0d);
		outcomeDeclarations.getOutcomeDeclarations().add(maxScoreOutcomeDeclaration);

		// outcome feedback
		OutcomeDeclaration feedbackOutcomeDeclaration = createOutcomeDeclarationForFeedbackBasic(assessmentItem);
		outcomeDeclarations.getOutcomeDeclarations().add(feedbackOutcomeDeclaration);
		
		//the single choice interaction
		ItemBodyGroup itemBodyGroup = nodeGroups.getItemBodyGroup();
		itemBodyGroup.setItemBody(new ItemBody(assessmentItem));
		
		ItemBody itemBody = itemBodyGroup.getItemBody();
		
		P question = getParagraph(itemBody, "");
		itemBodyGroup.getItemBody().getBlocks().add(question);
		
		ChoiceInteraction choiceInteraction = new ChoiceInteraction(itemBody);
		choiceInteraction.setMaxChoices(1);
		choiceInteraction.setShuffle(true);
		choiceInteraction.setResponseIdentifier(responseDeclarationId);
		itemBodyGroup.getItemBody().getBlocks().add(choiceInteraction);
		
		PromptGroup prompts = new PromptGroup(choiceInteraction);
		choiceInteraction.getNodeGroups().add(prompts);
		
		SimpleChoiceGroup singleChoices = new SimpleChoiceGroup(choiceInteraction);
		choiceInteraction.getNodeGroups().add(singleChoices);

		SimpleChoice firstChoice = new SimpleChoice(choiceInteraction);
		firstChoice.setIdentifier(correctResponseId);
		P firstChoiceText = getParagraph(firstChoice, "New answer");
		firstChoice.getFlowStatics().add(firstChoiceText);
		singleChoices.getSimpleChoices().add(firstChoice);
		

		//response processing
		ResponseProcessingGroup responsesProcessing = nodeGroups.getResponseProcessingGroup();
		ResponseProcessing responseProcessing = createResponseProcessing(assessmentItem, responseDeclarationId);
		responsesProcessing.setResponseProcessing(responseProcessing);
		
		return assessmentItem;
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
	
	private static void appendIdentifierValue(CorrectResponse correctResponse, Identifier correctResponseId) {
		FieldValue fieldValue = new FieldValue(correctResponse);
		IdentifierValue identifierValue = new IdentifierValue(correctResponseId);
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
