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

import org.olat.core.helpers.Settings;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.model.IdentifierGenerator;

import uk.ac.ed.ph.jqtiplus.group.NodeGroupList;
import uk.ac.ed.ph.jqtiplus.group.item.ItemBodyGroup;
import uk.ac.ed.ph.jqtiplus.group.item.interaction.PromptGroup;
import uk.ac.ed.ph.jqtiplus.group.item.interaction.choice.SimpleChoiceGroup;
import uk.ac.ed.ph.jqtiplus.group.item.response.declaration.ResponseDeclarationGroup;
import uk.ac.ed.ph.jqtiplus.group.item.response.processing.ResponseProcessingGroup;
import uk.ac.ed.ph.jqtiplus.group.outcome.declaration.OutcomeDeclarationGroup;
import uk.ac.ed.ph.jqtiplus.node.QtiNode;
import uk.ac.ed.ph.jqtiplus.node.content.ItemBody;
import uk.ac.ed.ph.jqtiplus.node.content.basic.TextRun;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.P;
import uk.ac.ed.ph.jqtiplus.node.expression.general.BaseValue;
import uk.ac.ed.ph.jqtiplus.node.expression.general.Correct;
import uk.ac.ed.ph.jqtiplus.node.expression.general.Variable;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.IsNull;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Match;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Sum;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.CorrectResponse;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleChoice;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.ResponseDeclaration;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseCondition;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseElse;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseElseIf;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseIf;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.ResponseProcessing;
import uk.ac.ed.ph.jqtiplus.node.item.response.processing.SetOutcomeValue;
import uk.ac.ed.ph.jqtiplus.node.outcome.declaration.OutcomeDeclaration;
import uk.ac.ed.ph.jqtiplus.node.shared.FieldValue;
import uk.ac.ed.ph.jqtiplus.node.shared.declaration.DefaultValue;
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
		assessmentItem.setIdentifier(IdentifierGenerator.newAsString());
		assessmentItem.setTitle("Single choice");
		assessmentItem.setToolName(QTI21Constants.TOOLNAME);
		assessmentItem.setToolVersion(Settings.getVersion());
		assessmentItem.setAdaptive(Boolean.FALSE);
		assessmentItem.setTimeDependent(Boolean.FALSE);
		
		NodeGroupList nodeGroups = assessmentItem.getNodeGroups();

		String responseDeclarationId = "RESPONSE_1";
		//define correct answer
		ResponseDeclarationGroup responseDeclarations = nodeGroups.getResponseDeclarationGroup();
		ResponseDeclaration responseDeclaration = new ResponseDeclaration(assessmentItem);
		responseDeclaration.setIdentifier(Identifier.parseString(responseDeclarationId));
		responseDeclaration.setCardinality(Cardinality.SINGLE);
		responseDeclaration.setBaseType(BaseType.IDENTIFIER);
		responseDeclarations.getResponseDeclarations().add(responseDeclaration);
		
		CorrectResponse correctResponse = new CorrectResponse(responseDeclaration);
		responseDeclaration.setCorrectResponse(correctResponse);
		
		Identifier correctResponseId = IdentifierGenerator.newAsIdentifier();
		FieldValue fieldValue = new FieldValue(correctResponse);
		IdentifierValue identifierValue = new IdentifierValue(correctResponseId);
		fieldValue.setSingleValue(identifierValue);
		correctResponse.getFieldValues().add(fieldValue);
		
		//outcomes
		OutcomeDeclarationGroup outcomeDeclarations = nodeGroups.getOutcomeDeclarationGroup();

		//outcome score
		OutcomeDeclaration scoreOutcomeDeclaration = createOutcomeDeclarationForScore(assessmentItem);
		outcomeDeclarations.getOutcomeDeclarations().add(scoreOutcomeDeclaration);

		// outcome max score
		OutcomeDeclaration maxScoreOutcomeDeclaration = createOutcomeDeclarationForMaxScore(assessmentItem);
		outcomeDeclarations.getOutcomeDeclarations().add(maxScoreOutcomeDeclaration);

		// outcome feedback
		OutcomeDeclaration feedbackOutcomeDeclaration = createOutcomeDeclarationForFeedback(assessmentItem);
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
		choiceInteraction.setResponseIdentifier(Identifier.parseString(responseDeclarationId));
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
	
	public static OutcomeDeclaration createOutcomeDeclarationForMaxScore(AssessmentItem assessmentItem) {
		OutcomeDeclaration maxScoreOutcomeDeclaration = new OutcomeDeclaration(assessmentItem);
		maxScoreOutcomeDeclaration.setIdentifier(QTI21Constants.MAXSCORE_IDENTIFIER);
		maxScoreOutcomeDeclaration.setCardinality(Cardinality.SINGLE);
		maxScoreOutcomeDeclaration.setBaseType(BaseType.FLOAT);
		
		DefaultValue maxScoreDefaultVal = new DefaultValue(maxScoreOutcomeDeclaration);
		maxScoreOutcomeDeclaration.setDefaultValue(maxScoreDefaultVal);
		
		FieldValue maxScoreDefaultFieldVal = new FieldValue(maxScoreDefaultVal, new FloatValue(1.0f));
		maxScoreDefaultVal.getFieldValues().add(maxScoreDefaultFieldVal);
		
		return maxScoreOutcomeDeclaration;
	}
	
	public static OutcomeDeclaration createOutcomeDeclarationForFeedback(AssessmentItem assessmentItem) {
		OutcomeDeclaration feedbackOutcomeDeclaration = new OutcomeDeclaration(assessmentItem);
		feedbackOutcomeDeclaration.setIdentifier(QTI21Constants.FEEDBACKBASIC_IDENTIFIER);
		feedbackOutcomeDeclaration.setCardinality(Cardinality.SINGLE);
		feedbackOutcomeDeclaration.setBaseType(BaseType.IDENTIFIER);
		
		DefaultValue feedbackDefaultVal = new DefaultValue(feedbackOutcomeDeclaration);
		feedbackOutcomeDeclaration.setDefaultValue(feedbackDefaultVal);
		
		FieldValue feedbackDefaultFieldVal = new FieldValue(feedbackDefaultVal, new IdentifierValue("empty"));
		feedbackDefaultVal.getFieldValues().add(feedbackDefaultFieldVal);
		
		return feedbackOutcomeDeclaration;
	}
	
	public static ResponseProcessing createResponseProcessing(AssessmentItem assessmentItem, String responseId) {
		ResponseProcessing responseProcessing = new ResponseProcessing(assessmentItem);

		ResponseCondition rule = new ResponseCondition(responseProcessing);
		
		//if no response
		ResponseIf responseIf = new ResponseIf(rule);
		rule.setResponseIf(responseIf);
		
		IsNull isNull = new IsNull(responseIf);
		responseIf.getExpressions().add(isNull);
		
		Variable variable = new Variable(isNull);
		variable.setIdentifier(ComplexReferenceIdentifier.parseString(responseId));
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
			responseVar.setIdentifier(ComplexReferenceIdentifier.parseString(responseId));
			match.getExpressions().add(responseVar);
			
			Correct correct = new Correct(match);
			correct.setIdentifier(ComplexReferenceIdentifier.parseString(responseId));
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
			correctFeedbackVal.setSingleValue(QTI21Constants.CORRECT);
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
			incorrectFeedbackVal.setSingleValue(QTI21Constants.INCORRECT);
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
