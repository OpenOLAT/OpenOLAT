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

import java.io.File;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
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
import uk.ac.ed.ph.jqtiplus.node.item.ModalFeedback;
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
import uk.ac.ed.ph.jqtiplus.provision.BadResourceException;
import uk.ac.ed.ph.jqtiplus.reading.AssessmentObjectXmlLoader;
import uk.ac.ed.ph.jqtiplus.reading.QtiModelBuildingError;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlInterpretationException;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlReader;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.types.ComplexReferenceIdentifier;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.validation.ItemValidationResult;
import uk.ac.ed.ph.jqtiplus.value.BaseType;
import uk.ac.ed.ph.jqtiplus.value.Cardinality;
import uk.ac.ed.ph.jqtiplus.value.FloatValue;
import uk.ac.ed.ph.jqtiplus.value.IdentifierValue;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * 
 * Initial date: 04.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentItemPackageTest {
	
	private static final OLog log = Tracing.createLoggerFor(AssessmentItemPackageTest.class);
	
	@Test
	public void loadAssessmentItem() throws URISyntaxException {
		QtiXmlReader qtiXmlReader = new QtiXmlReader(new JqtiExtensionManager());
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		
		
		URL testUrl = AssessmentItemPackageTest.class.getResource("assessment-item-single-choice.xml");
		ResourceLocator fileResourceLocator = new PathResourceLocator(Paths.get(testUrl.toURI()));
        AssessmentObjectXmlLoader assessmentObjectXmlLoader = new AssessmentObjectXmlLoader(qtiXmlReader, fileResourceLocator);

        ResolvedAssessmentItem item = assessmentObjectXmlLoader.loadAndResolveAssessmentItem(testUrl.toURI());
		Assert.assertNotNull(item);
		
		AssessmentItem assessmentItem = item.getItemLookup().getRootNodeHolder().getRootNode();
		Assert.assertNotNull(assessmentItem);
		
		qtiSerializer.serializeJqtiObject(assessmentItem, System.out);
	}


	public AssessmentItem createAssessmentItem() {
		AssessmentItem assessmentItem = new AssessmentItem();
		assessmentItem.setIdentifier("id" + UUID.randomUUID());
		assessmentItem.setTitle("Physicists");
		assessmentItem.setAdaptive(Boolean.FALSE);
		assessmentItem.setTimeDependent(Boolean.FALSE);
		NodeGroupList nodeGroups = assessmentItem.getNodeGroups();
		
		Identifier deBroglieId = Identifier.parseString("id" + UUID.randomUUID().toString());
		Identifier maxPlanckId = Identifier.parseString("id" + UUID.randomUUID().toString());
		
	
		//define correct answer
		ResponseDeclarationGroup responseDeclarations = nodeGroups.getResponseDeclarationGroup();
		ResponseDeclaration responseDeclaration = new ResponseDeclaration(assessmentItem);
		responseDeclaration.setIdentifier(Identifier.parseString("RESPONSE_1"));
		responseDeclaration.setCardinality(Cardinality.SINGLE);
		responseDeclaration.setBaseType(BaseType.IDENTIFIER);
		responseDeclarations.getResponseDeclarations().add(responseDeclaration);
		
		CorrectResponse correctResponse = new CorrectResponse(responseDeclaration);
		responseDeclaration.setCorrectResponse(correctResponse);
		
		FieldValue fieldValue = new FieldValue(correctResponse);
		IdentifierValue identifierValue = new IdentifierValue(maxPlanckId);
		fieldValue.setSingleValue(identifierValue);
		correctResponse.getFieldValues().add(fieldValue);
		
		//outcomes
		OutcomeDeclarationGroup outcomeDeclarations = nodeGroups.getOutcomeDeclarationGroup();

		// outcome score
		OutcomeDeclaration scoreOutcomeDeclaration = new OutcomeDeclaration(assessmentItem);
		scoreOutcomeDeclaration.setIdentifier(Identifier.parseString("SCORE"));
		scoreOutcomeDeclaration.setCardinality(Cardinality.SINGLE);
		scoreOutcomeDeclaration.setBaseType(BaseType.FLOAT);
		outcomeDeclarations.getOutcomeDeclarations().add(scoreOutcomeDeclaration);
		
		DefaultValue scoreDefaultVal = new DefaultValue(scoreOutcomeDeclaration);
		scoreOutcomeDeclaration.setDefaultValue(scoreDefaultVal);
		
		FieldValue scoreDefaultFieldVal = new FieldValue(scoreDefaultVal, FloatValue.ZERO);
		scoreDefaultVal.getFieldValues().add(scoreDefaultFieldVal);

		// outcome max score
		OutcomeDeclaration maxScoreOutcomeDeclaration = new OutcomeDeclaration(assessmentItem);
		maxScoreOutcomeDeclaration.setIdentifier(Identifier.parseString("MAXSCORE"));
		maxScoreOutcomeDeclaration.setCardinality(Cardinality.SINGLE);
		maxScoreOutcomeDeclaration.setBaseType(BaseType.FLOAT);
		outcomeDeclarations.getOutcomeDeclarations().add(maxScoreOutcomeDeclaration);
		
		DefaultValue maxScoreDefaultVal = new DefaultValue(maxScoreOutcomeDeclaration);
		maxScoreOutcomeDeclaration.setDefaultValue(maxScoreDefaultVal);
		
		FieldValue maxScoreDefaultFieldVal = new FieldValue(maxScoreDefaultVal, new FloatValue(1.0f));
		maxScoreDefaultVal.getFieldValues().add(maxScoreDefaultFieldVal);
		
		// outcome feedback
		OutcomeDeclaration feedbackOutcomeDeclaration = new OutcomeDeclaration(assessmentItem);
		feedbackOutcomeDeclaration.setIdentifier(Identifier.parseString("FEEDBACKBASIC"));
		feedbackOutcomeDeclaration.setCardinality(Cardinality.SINGLE);
		feedbackOutcomeDeclaration.setBaseType(BaseType.IDENTIFIER);
		outcomeDeclarations.getOutcomeDeclarations().add(feedbackOutcomeDeclaration);
		
		DefaultValue feedbackDefaultVal = new DefaultValue(feedbackOutcomeDeclaration);
		feedbackOutcomeDeclaration.setDefaultValue(feedbackDefaultVal);
		
		FieldValue feedbackDefaultFieldVal = new FieldValue(feedbackDefaultVal, new IdentifierValue("empty"));
		feedbackDefaultVal.getFieldValues().add(feedbackDefaultFieldVal);
		
		
		//the interaction
		ItemBodyGroup itemBodyGroup = nodeGroups.getItemBodyGroup();
		itemBodyGroup.setItemBody(new ItemBody(assessmentItem));
		
		ItemBody itemBody = itemBodyGroup.getItemBody();
		
		P question = getParagraph(itemBody, "Wo is the greatest physicist of all time?");
		itemBodyGroup.getItemBody().getBlocks().add(question);
		
		ChoiceInteraction choiceInteraction = new ChoiceInteraction(itemBody);
		choiceInteraction.setMaxChoices(1);
		choiceInteraction.setShuffle(true);
		choiceInteraction.setResponseIdentifier(Identifier.parseString("RESPONSE_1"));
		itemBodyGroup.getItemBody().getBlocks().add(choiceInteraction);
		
		PromptGroup prompts = new PromptGroup(choiceInteraction);
		choiceInteraction.getNodeGroups().add(prompts);
		
		SimpleChoiceGroup singleChoices = new SimpleChoiceGroup(choiceInteraction);
		choiceInteraction.getNodeGroups().add(singleChoices);

		SimpleChoice firstChoice = new SimpleChoice(choiceInteraction);
		firstChoice.setIdentifier(deBroglieId);
		P firstChoiceText = getParagraph(firstChoice, "Louis de Broglie");
		firstChoice.getFlowStatics().add(firstChoiceText);
		singleChoices.getSimpleChoices().add(firstChoice);
		
		SimpleChoice secondChoice = new SimpleChoice(choiceInteraction);
		secondChoice.setIdentifier(maxPlanckId);
		P secondChoiceText = getParagraph(secondChoice, "Max Planck");
		secondChoice.getFlowStatics().add(secondChoiceText);
		singleChoices.getSimpleChoices().add(secondChoice);
		
		//response processing
		ResponseProcessingGroup responsesProcessing = nodeGroups.getResponseProcessingGroup();
		ResponseProcessing responseProcessing = new ResponseProcessing(assessmentItem);
		responsesProcessing.setResponseProcessing(responseProcessing);
		
		ResponseCondition rule = new ResponseCondition(responseProcessing);
		
		//if no response
		ResponseIf responseIf = new ResponseIf(rule);
		rule.setResponseIf(responseIf);
		
		IsNull isNull = new IsNull(responseIf);
		responseIf.getExpressions().add(isNull);
		
		Variable variable = new Variable(isNull);
		variable.setIdentifier(ComplexReferenceIdentifier.parseString("RESPONSE_1"));
		isNull.getExpressions().add(variable);
		
		{
			SetOutcomeValue feedbackVar = new SetOutcomeValue(responseIf);
			feedbackVar.setIdentifier(Identifier.parseString("FEEDBACKBASIC"));
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
			responseVar.setIdentifier(ComplexReferenceIdentifier.parseString("RESPONSE_1"));
			match.getExpressions().add(responseVar);
			
			Correct correct = new Correct(match);
			correct.setIdentifier(ComplexReferenceIdentifier.parseString("RESPONSE_1"));
			match.getExpressions().add(correct);
		}

		// outcome score
		{
			SetOutcomeValue scoreOutcomeVar = new SetOutcomeValue(responseIf);
			scoreOutcomeVar.setIdentifier(Identifier.parseString("SCORE"));
			responseElseIf.getResponseRules().add(scoreOutcomeVar);
			
			Sum sum = new Sum(scoreOutcomeVar);
			scoreOutcomeVar.getExpressions().add(sum);
			
			Variable scoreVar = new Variable(sum);
			scoreVar.setIdentifier(ComplexReferenceIdentifier.parseString("SCORE"));
			sum.getExpressions().add(scoreVar);
			
			Variable maxScoreVar = new Variable(sum);
			maxScoreVar.setIdentifier(ComplexReferenceIdentifier.parseString("MAXSCORE"));
			sum.getExpressions().add(maxScoreVar);
		}
		
		// outcome feedback
		{
			SetOutcomeValue correctFeedbackVar = new SetOutcomeValue(responseIf);
			correctFeedbackVar.setIdentifier(Identifier.parseString("FEEDBACKBASIC"));
			BaseValue correctFeedbackVal = new BaseValue(correctFeedbackVar);
			correctFeedbackVal.setBaseTypeAttrValue(BaseType.IDENTIFIER);
			correctFeedbackVal.setSingleValue(new IdentifierValue("correct"));
			correctFeedbackVar.setExpression(correctFeedbackVal);
			responseElseIf.getResponseRules().add(correctFeedbackVar);
		}
		
		
		// else failed
		ResponseElse responseElse = new ResponseElse(rule);
		rule.setResponseElse(responseElse);
		{// feedback incorrect
			SetOutcomeValue incorrectFeedbackVar = new SetOutcomeValue(responseIf);
			incorrectFeedbackVar.setIdentifier(Identifier.parseString("FEEDBACKBASIC"));
			BaseValue incorrectFeedbackVal = new BaseValue(incorrectFeedbackVar);
			incorrectFeedbackVal.setBaseTypeAttrValue(BaseType.IDENTIFIER);
			incorrectFeedbackVal.setSingleValue(new IdentifierValue("incorrect"));
			incorrectFeedbackVar.setExpression(incorrectFeedbackVal);
			responseElse.getResponseRules().add(incorrectFeedbackVar);
		}

		responseProcessing.getResponseRules().add(rule);
		
		ModalFeedback modalFeedback = AssessmentItemFactory
				.createModalFeedback(assessmentItem, Identifier.parseString("Feedback22"), "Hello", "<p>hello world</p>");
		
		assessmentItem.getModalFeedbacks().add(modalFeedback);
		
		return assessmentItem;
	}	
		

	@Test
	public void buildAssessmentItem() throws URISyntaxException {	

		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		
		AssessmentItem assessmentItem = createAssessmentItem();
		
		qtiSerializer.serializeJqtiObject(assessmentItem, System.out);
		System.out.println("\n-------------");
		
		File outputFile = new File("/HotCoffee/QTI/generated_item.xml");
		if(outputFile.exists()) {
			outputFile.delete();
			outputFile = new File("/HotCoffee/QTI/generated_item.xml");
		}
		try(FileOutputStream out = new FileOutputStream(outputFile)) {
			qtiSerializer.serializeJqtiObject(assessmentItem, out);	
		} catch(Exception e) {
			log.error("", e);
		}

		QtiXmlReader qtiXmlReader = new QtiXmlReader(new JqtiExtensionManager());
		ResourceLocator fileResourceLocator = new PathResourceLocator(outputFile.toPath());
        AssessmentObjectXmlLoader assessmentObjectXmlLoader = new AssessmentObjectXmlLoader(qtiXmlReader, fileResourceLocator);
        ItemValidationResult item = assessmentObjectXmlLoader.loadResolveAndValidateItem(outputFile.toURI());
        System.out.println("Has errors: " + (item.getModelValidationErrors().size() > 0));
        
        BadResourceException e = item.getResolvedAssessmentItem().getItemLookup().getBadResourceException();
        if(e instanceof QtiXmlInterpretationException) {
        	QtiXmlInterpretationException qe = (QtiXmlInterpretationException)e;
        	for(QtiModelBuildingError error :qe.getQtiModelBuildingErrors()) {
        		String localName = error.getElementLocalName();
        		String msg = error.getException().getMessage();
        		int lineNumber = error.getElementLocation().getLineNumber();
        		System.out.println(lineNumber + " :: " + localName + " :: " + msg);
        	}
        }
	}
	
	private P getParagraph(QtiNode parent, String content) {
		P paragraph = new P(parent);
		TextRun text = new TextRun(paragraph, content);
		paragraph.getInlines().add(text);
		return paragraph;
	}
	
	

}
