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
import org.olat.ims.qti21.model.xml.interactions.HottextAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.SimpleChoiceAssessmentItemBuilder.ScoreEvaluation;

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.EndAttemptInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.HottextInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.Choice;
import uk.ac.ed.ph.jqtiplus.reading.AssessmentObjectXmlLoader;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlReader;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.running.ItemSessionController;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.types.ResponseData;
import uk.ac.ed.ph.jqtiplus.types.StringResponseData;
import uk.ac.ed.ph.jqtiplus.value.FloatValue;
import uk.ac.ed.ph.jqtiplus.value.IdentifierValue;
import uk.ac.ed.ph.jqtiplus.value.Value;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * 
 * Initial date: 22 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class HottextAssessmentItemBuilderTest {
	
	private static final Logger log = Tracing.createLoggerFor(HottextAssessmentItemBuilderTest.class);
	
	/**
	 * A basic hottextInteraction with three choices. If all answers
	 * are correct, the SCORE is 3.0.
	 * 
	 * @throws IOException
	 */
	@Test
	public void createHottextAssessmentItem_allCorrectAnswers() throws IOException {
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		HottextAssessmentItemBuilder itemBuilder = new HottextAssessmentItemBuilder("Hot texts", "This is a hot ", "text", qtiSerializer);
		itemBuilder.setQuestion("<p>This is <hottext identifier=\"RESPONSE_HOT_1\">hot</hottext>, <hottext identifier=\"RESPONSE_HOT_2\">cold</hottext> or <hottext identifier=\"RESPONSE_HOT_3\">freezing</hottext></p>");
		itemBuilder.addCorrectAnswer(Identifier.parseString("RESPONSE_HOT_2"));
		itemBuilder.addCorrectAnswer(Identifier.parseString("RESPONSE_HOT_3"));
		itemBuilder.setMaxScore(3.0d);
		itemBuilder.setScoreEvaluationMode(ScoreEvaluation.allCorrectAnswers);
		itemBuilder.build();
		
		File itemFile = new File(WebappHelper.getTmpDir(), "hottextAssessmentItem" + UUID.randomUUID() + ".xml");
		try(FileOutputStream out = new FileOutputStream(itemFile)) {
			qtiSerializer.serializeJqtiObject(itemBuilder.getAssessmentItem(), out);
		} catch(Exception e) {
			log.error("", e);
		}
		
		{// correct answers
			Map<Identifier, ResponseData> responseMap = new HashMap<>();
			Identifier responseIdentifier = itemBuilder.getInteraction().getResponseIdentifier();
	        responseMap.put(responseIdentifier, new StringResponseData("RESPONSE_HOT_2", "RESPONSE_HOT_3"));
			ItemSessionController itemSessionController = RunningItemHelper.run(itemFile, responseMap);
			Value score = itemSessionController.getItemSessionState().getOutcomeValue(QTI21Constants.SCORE_IDENTIFIER);
			Assert.assertEquals(new FloatValue(3.0d), score);
		}
		
		{// wrong answer
			Map<Identifier, ResponseData> responseMap = new HashMap<>();
			Identifier responseIdentifier = itemBuilder.getInteraction().getResponseIdentifier();
	        responseMap.put(responseIdentifier, new StringResponseData("RESPONSE_HOT_1"));
			ItemSessionController itemSessionController = RunningItemHelper.run(itemFile, responseMap);
			Value score = itemSessionController.getItemSessionState().getOutcomeValue(QTI21Constants.SCORE_IDENTIFIER);
			Assert.assertEquals(new FloatValue(0.0d), score);
		}
		
		FileUtils.deleteDirsAndFiles(itemFile.toPath());
	}
	
	/**
	 * This is an hottextInteraction with 3 choices, one with 3.0 points and correct,
	 * one with 0.0 points but correct and one false with -1.0 points.
	 * 
	 * @throws IOException
	 */
	@Test
	public void createHottextAssessmentItem_perAnswer() throws IOException {
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		HottextAssessmentItemBuilder itemBuilder = new HottextAssessmentItemBuilder("Hot texts", "This is a hot ", "text", qtiSerializer);
		itemBuilder.setQuestion("<p>This is <hottext identifier=\"RESPONSE_HOT_1\">hot</hottext>, <hottext identifier=\"RESPONSE_HOT_2\">cold</hottext> or <hottext identifier=\"RESPONSE_HOT_3\">freezing</hottext></p>");
		
		itemBuilder.addCorrectAnswer(Identifier.parseString("RESPONSE_HOT_2"));
		itemBuilder.addCorrectAnswer(Identifier.parseString("RESPONSE_HOT_3"));
		
		itemBuilder.setMapping(Identifier.parseString("RESPONSE_HOT_1"), -1.0d);
		itemBuilder.setMapping(Identifier.parseString("RESPONSE_HOT_2"), 0.0d);
		itemBuilder.setMapping(Identifier.parseString("RESPONSE_HOT_3"), 3.0d);
		
		itemBuilder.setMaxScore(3.0d);
		itemBuilder.setScoreEvaluationMode(ScoreEvaluation.perAnswer);
		itemBuilder.build();
		
		File itemFile = new File(WebappHelper.getTmpDir(), "hottextAssessmentItem" + UUID.randomUUID() + ".xml");
		try(FileOutputStream out = new FileOutputStream(itemFile)) {
			qtiSerializer.serializeJqtiObject(itemBuilder.getAssessmentItem(), out);
		} catch(Exception e) {
			log.error("", e);
		}
		
		{// correct answers
			Map<Identifier, ResponseData> responseMap = new HashMap<>();
			Identifier responseIdentifier = itemBuilder.getInteraction().getResponseIdentifier();
	        responseMap.put(responseIdentifier, new StringResponseData("RESPONSE_HOT_2", "RESPONSE_HOT_3"));
			ItemSessionController itemSessionController = RunningItemHelper.run(itemFile, responseMap);
			Value score = itemSessionController.getItemSessionState().getOutcomeValue(QTI21Constants.SCORE_IDENTIFIER);
			Assert.assertEquals(new FloatValue(3.0d), score);
			Value feedbackBasic = itemSessionController.getItemSessionState().getOutcomeValue(QTI21Constants.FEEDBACKBASIC_IDENTIFIER);
			Assert.assertEquals(new IdentifierValue(QTI21Constants.CORRECT_IDENTIFIER), feedbackBasic);
		}

		{// max score but not all correct
			Map<Identifier, ResponseData> responseMap = new HashMap<>();
			Identifier responseIdentifier = itemBuilder.getInteraction().getResponseIdentifier();
	        responseMap.put(responseIdentifier, new StringResponseData("RESPONSE_HOT_3"));
			ItemSessionController itemSessionController = RunningItemHelper.run(itemFile, responseMap);
			Value score = itemSessionController.getItemSessionState().getOutcomeValue(QTI21Constants.SCORE_IDENTIFIER);
			Assert.assertEquals(new FloatValue(3.0d), score);
			Value feedbackBasic = itemSessionController.getItemSessionState().getOutcomeValue(QTI21Constants.FEEDBACKBASIC_IDENTIFIER);
			Assert.assertEquals(new IdentifierValue(QTI21Constants.INCORRECT_IDENTIFIER), feedbackBasic);
		}
		
		{// all wrong
			Map<Identifier, ResponseData> responseMap = new HashMap<>();
			Identifier responseIdentifier = itemBuilder.getInteraction().getResponseIdentifier();
	        responseMap.put(responseIdentifier, new StringResponseData("RESPONSE_HOT_1"));
			ItemSessionController itemSessionController = RunningItemHelper.run(itemFile, responseMap);
			Value score = itemSessionController.getItemSessionState().getOutcomeValue(QTI21Constants.SCORE_IDENTIFIER);
			Assert.assertEquals(new FloatValue(0.0d), score);
			Value feedbackBasic = itemSessionController.getItemSessionState().getOutcomeValue(QTI21Constants.FEEDBACKBASIC_IDENTIFIER);
			Assert.assertEquals(new IdentifierValue(QTI21Constants.INCORRECT_IDENTIFIER), feedbackBasic);
		}
		
		FileUtils.deleteDirsAndFiles(itemFile.toPath());
	}
	
	@Test
	public void readHottextAssessmentItem() throws URISyntaxException {
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		URL itemUrl = AssessmentItemBuilderTest.class.getResource("resources/openolat/hottext-per-answer-11-4-0.xml");
		AssessmentItem assessmentItem = loadAssessmentItem(itemUrl);
		HottextAssessmentItemBuilder itemBuilder = new HottextAssessmentItemBuilder(assessmentItem, qtiSerializer);
		
		//basic check ChoiceInteraction
        AssessmentItem loadedItem = itemBuilder.getAssessmentItem();
        List<Interaction> interactions = loadedItem.getItemBody().findInteractions();
        Assert.assertNotNull(interactions);
        Assert.assertEquals(1, interactions.size());
        Assert.assertTrue(interactions.get(0) instanceof HottextInteraction);
        
        //hot texts
        Choice correct1 = itemBuilder.getChoice(Identifier.assumedLegal("htf9250a96f24bc8873c9cc54dfbaaaa"));
        Assert.assertTrue(itemBuilder.isCorrect(correct1));
        Choice correct2 = itemBuilder.getChoice(Identifier.assumedLegal("RESPONSE_3"));
        Assert.assertTrue(itemBuilder.isCorrect(correct2));
        Choice wrong1 = itemBuilder.getChoice(Identifier.assumedLegal("RESPONSE_2"));
        Assert.assertFalse(itemBuilder.isCorrect(wrong1));
        Choice wrong2 = itemBuilder.getChoice(Identifier.assumedLegal("RESPONSE_4"));
        Assert.assertFalse(itemBuilder.isCorrect(wrong2));

        //score
        Double maxScore = itemBuilder.getMaxScoreBuilder().getScore();
        Assert.assertEquals(3.0, maxScore.doubleValue(), 0.0001);
        Double minScore = itemBuilder.getMinScoreBuilder().getScore();
        Assert.assertEquals(0.0, minScore.doubleValue(), 0.0001);

        //per answer
        Assert.assertEquals(ScoreEvaluation.perAnswer, itemBuilder.getScoreEvaluationMode());
	}
	
	@Test
	public void readHottextAssessmentItem_allCorrectAnswers() throws URISyntaxException {
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		URL itemUrl = AssessmentItemBuilderTest.class.getResource("resources/openolat/hottext-score-all-11-4-0.xml");
		AssessmentItem assessmentItem = loadAssessmentItem(itemUrl);
		HottextAssessmentItemBuilder itemBuilder = new HottextAssessmentItemBuilder(assessmentItem, qtiSerializer);
		
		//basic check ChoiceInteraction
        AssessmentItem loadedItem = itemBuilder.getAssessmentItem();
        List<Interaction> interactions = loadedItem.getItemBody().findInteractions();
        Assert.assertNotNull(interactions);
        Assert.assertEquals(2, interactions.size());
        Assert.assertTrue(interactions.get(0) instanceof HottextInteraction);
        Assert.assertTrue(interactions.get(1) instanceof EndAttemptInteraction);
        
        //hot texts
        Choice correct1 = itemBuilder.getChoice(Identifier.assumedLegal("htebdb40344641dba115e3c8c6ce3926"));
        Assert.assertTrue(itemBuilder.isCorrect(correct1));
        Choice correct2 = itemBuilder.getChoice(Identifier.assumedLegal("ht103ce53892dea97613005a5ce76be31e"));
        Assert.assertTrue(itemBuilder.isCorrect(correct2));
        Choice wrong1 = itemBuilder.getChoice(Identifier.assumedLegal("hte11a51c3e3d86a5f7293da19a1a8700e"));
        Assert.assertFalse(itemBuilder.isCorrect(wrong1));

        //score
        Double maxScore = itemBuilder.getMaxScoreBuilder().getScore();
        Assert.assertEquals(2.0, maxScore.doubleValue(), 0.0001);
        Double minScore = itemBuilder.getMinScoreBuilder().getScore();
        Assert.assertEquals(0.0, minScore.doubleValue(), 0.0001);

        //per answer
        Assert.assertEquals(ScoreEvaluation.allCorrectAnswers, itemBuilder.getScoreEvaluationMode());
        
        //correct feedback
        ModalFeedbackBuilder correctFeedback = itemBuilder.getCorrectFeedback();
        Assert.assertNotNull(correctFeedback);
        Assert.assertNotNull(correctFeedback.getText());
        Assert.assertTrue(correctFeedback.getText().contains("You check the right answers"));
        
        //incorrect
        ModalFeedbackBuilder incorrectFeedback = itemBuilder.getIncorrectFeedback();
        Assert.assertNotNull(incorrectFeedback);
        Assert.assertNotNull(incorrectFeedback.getText());
        Assert.assertTrue(incorrectFeedback.getText().contains("Some of your anwsers are not the correct one."));
 
        //correct solution feedback
        ModalFeedbackBuilder correctSolutionFeedback = itemBuilder.getCorrectSolutionFeedback();
        Assert.assertNotNull(correctSolutionFeedback);
        Assert.assertNotNull(correctSolutionFeedback.getText());
        Assert.assertTrue(correctSolutionFeedback.getText().contains("A little hint towards the correct solution"));

        //hint
        ModalFeedbackBuilder hint = itemBuilder.getHint();
        Assert.assertNotNull(hint);
        Assert.assertNotNull(hint.getText());
        Assert.assertTrue(hint.getText().contains("This is an endAttemptInteraction"));
	}

	private AssessmentItem loadAssessmentItem(URL itemUrl) throws URISyntaxException {
		QtiXmlReader qtiXmlReader = new QtiXmlReader(new JqtiExtensionManager());
		ResourceLocator fileResourceLocator = new PathResourceLocator(Paths.get(itemUrl.toURI()));
        AssessmentObjectXmlLoader assessmentObjectXmlLoader = new AssessmentObjectXmlLoader(qtiXmlReader, fileResourceLocator);
        ResolvedAssessmentItem item = assessmentObjectXmlLoader.loadAndResolveAssessmentItem(itemUrl.toURI());
		return item.getItemLookup().getRootNodeHolder().getRootNode();
	}
}
