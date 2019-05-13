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
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.model.xml.interactions.SimpleChoiceAssessmentItemBuilder.ScoreEvaluation;
import org.olat.ims.qti21.model.xml.interactions.SingleChoiceAssessmentItemBuilder;

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleChoice;
import uk.ac.ed.ph.jqtiplus.reading.AssessmentObjectXmlLoader;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlReader;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.running.ItemSessionController;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.types.ResponseData;
import uk.ac.ed.ph.jqtiplus.types.StringResponseData;
import uk.ac.ed.ph.jqtiplus.value.FloatValue;
import uk.ac.ed.ph.jqtiplus.value.Value;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * 
 * Initial date: 20 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SingleChoiceAssessmentItemBuilderTest {
	
	private static final Logger log = Tracing.createLoggerFor(SingleChoiceAssessmentItemBuilderTest.class);
	
	/**
	 * Check if a bare bone multiple choice created with our builder make a valid assessmentItem.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void readSingleChoice() throws IOException, URISyntaxException {
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
	
		URL itemUrl = AssessmentItemBuilderTest.class.getResource("resources/openolat/single-choice-feedbacks.xml");
		AssessmentItem assessmentItem = loadAssessmentItem(itemUrl);
		SingleChoiceAssessmentItemBuilder itemBuilder = new SingleChoiceAssessmentItemBuilder(assessmentItem, qtiSerializer);

		//basic check ChoiceInteraction
        AssessmentItem loadedItem = itemBuilder.getAssessmentItem();
        List<Interaction> interactions = loadedItem.getItemBody().findInteractions();
        Assert.assertNotNull(interactions);
        Assert.assertEquals(1, interactions.size());
        Assert.assertTrue(interactions.get(0) instanceof ChoiceInteraction);
        
        //correct feedback
        ModalFeedbackBuilder correctFeedback = itemBuilder.getCorrectFeedback();
        Assert.assertNotNull(correctFeedback);
        Assert.assertNotNull(correctFeedback.getText());
        Assert.assertTrue(correctFeedback.getText().contains("This is the correct answer"));
        
        //incorrect
        ModalFeedbackBuilder incorrectFeedback = itemBuilder.getIncorrectFeedback();
        Assert.assertNotNull(incorrectFeedback);
        Assert.assertNotNull(incorrectFeedback.getText());
        Assert.assertTrue(incorrectFeedback.getText().contains("This is the wrong answer"));
        
        //correct answer
        SimpleChoice correctChoice = itemBuilder.getChoice(Identifier.assumedLegal("id87d42b76-93d7-42fc-bdec-3e2419fa901d"));
        Assert.assertTrue(itemBuilder.isCorrect(correctChoice));
        
        //score per 
        Assert.assertEquals(ScoreEvaluation.allCorrectAnswers, itemBuilder.getScoreEvaluationMode());
	}
	
	@Test
	public void createSingleAssessmentItem_allCorrectAnswers() throws IOException {
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		SingleChoiceAssessmentItemBuilder itemBuilder = new SingleChoiceAssessmentItemBuilder("Single choice", "Single choice", qtiSerializer);
		itemBuilder.setQuestion("<p>Hello</p>");
		
		ChoiceInteraction interaction = itemBuilder.getChoiceInteraction();
		SimpleChoice choice1 = AssessmentItemFactory.createSimpleChoice(interaction, "One", "sc");
		SimpleChoice choice2 = AssessmentItemFactory.createSimpleChoice(interaction, "Two", "sc");
		SimpleChoice choice3 = AssessmentItemFactory.createSimpleChoice(interaction, "Three", "sc");

		List<SimpleChoice> choiceList = new ArrayList<>();
		choiceList.add(choice1);
		choiceList.add(choice2);
		choiceList.add(choice3);
		itemBuilder.setSimpleChoices(choiceList);
		itemBuilder.setCorrectAnswer(choice2.getIdentifier());
		itemBuilder.setMaxScore(3.0d);
		itemBuilder.setScoreEvaluationMode(ScoreEvaluation.allCorrectAnswers);
		itemBuilder.build();
		
		File itemFile = new File(WebappHelper.getTmpDir(), "scAssessmentItem" + UUID.randomUUID() + ".xml");
		try(FileOutputStream out = new FileOutputStream(itemFile)) {
			qtiSerializer.serializeJqtiObject(itemBuilder.getAssessmentItem(), out);
		} catch(Exception e) {
			log.error("", e);
		}
		
		{// correct answers
			Map<Identifier, ResponseData> responseMap = new HashMap<>();
			Identifier responseIdentifier = itemBuilder.getInteraction().getResponseIdentifier();
	        responseMap.put(responseIdentifier, new StringResponseData(choice2.getIdentifier().toString()));
			ItemSessionController itemSessionController = RunningItemHelper.run(itemFile, responseMap);
			Value score = itemSessionController.getItemSessionState().getOutcomeValue(QTI21Constants.SCORE_IDENTIFIER);
			Assert.assertEquals(new FloatValue(3.0d), score);
		}
		
		{// wrong answer
			Map<Identifier, ResponseData> responseMap = new HashMap<>();
			Identifier responseIdentifier = itemBuilder.getInteraction().getResponseIdentifier();
	        responseMap.put(responseIdentifier, new StringResponseData(choice3.getIdentifier().toString()));
			ItemSessionController itemSessionController = RunningItemHelper.run(itemFile, responseMap);
			Value score = itemSessionController.getItemSessionState().getOutcomeValue(QTI21Constants.SCORE_IDENTIFIER);
			Assert.assertEquals(new FloatValue(0.0d), score);
		}
		
		FileUtils.deleteDirsAndFiles(itemFile.toPath());
	}
	
	private AssessmentItem loadAssessmentItem(URL itemUrl) throws URISyntaxException {
		QtiXmlReader qtiXmlReader = new QtiXmlReader(new JqtiExtensionManager());
		ResourceLocator fileResourceLocator = new PathResourceLocator(Paths.get(itemUrl.toURI()));
        AssessmentObjectXmlLoader assessmentObjectXmlLoader = new AssessmentObjectXmlLoader(qtiXmlReader, fileResourceLocator);
        ResolvedAssessmentItem item = assessmentObjectXmlLoader.loadAndResolveAssessmentItem(itemUrl.toURI());
		return item.getItemLookup().getRootNodeHolder().getRootNode();
	}

}
