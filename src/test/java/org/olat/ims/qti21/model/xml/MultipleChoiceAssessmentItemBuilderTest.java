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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.model.xml.interactions.MultipleChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.SimpleChoiceAssessmentItemBuilder.ScoreEvaluation;

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.EndAttemptInteraction;
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
import uk.ac.ed.ph.jqtiplus.value.IdentifierValue;
import uk.ac.ed.ph.jqtiplus.value.Value;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * 
 * Initial date: 20 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MultipleChoiceAssessmentItemBuilderTest {
	
	/**
	 * Check if a bare bone multiple choice created with our builder make a valid assessmentItem.
	 * It has 2 correct choices, two wrong, correct and wrong feedbacks. The max score is 1.0
	 * but the 2 choices has each a score of 1.0.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void readMultipleChoice_perAnswer_1110() throws IOException, URISyntaxException {
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
	
		URL itemUrl = AssessmentItemBuilderTest.class.getResource("resources/openolat/multiple-choice-per-answer-11-1-0.xml");
		AssessmentItem assessmentItem = loadAssessmentItem(itemUrl);
		MultipleChoiceAssessmentItemBuilder itemBuilder = new MultipleChoiceAssessmentItemBuilder(assessmentItem, qtiSerializer);

		//basic check ChoiceInteraction
        AssessmentItem loadedItem = itemBuilder.getAssessmentItem();
        List<Interaction> interactions = loadedItem.getItemBody().findInteractions();
        Assert.assertNotNull(interactions);
        Assert.assertEquals(1, interactions.size());
        Assert.assertTrue(interactions.get(0) instanceof ChoiceInteraction);
        
        //choices
        SimpleChoice correct1 = itemBuilder.getChoice(Identifier.assumedLegal("mc80677ac3f4449ebc689cf60c230a3d"));
        Assert.assertTrue(itemBuilder.isCorrect(correct1));
        SimpleChoice correct2 = itemBuilder.getChoice(Identifier.assumedLegal("mc7e5667abeb415fa05a8c7d8fd3d6bb"));
        Assert.assertTrue(itemBuilder.isCorrect(correct2));
        SimpleChoice wrong1 = itemBuilder.getChoice(Identifier.assumedLegal("mcaacc51e0ca4027b3adb3107cda4e30"));
        Assert.assertFalse(itemBuilder.isCorrect(wrong1));
        SimpleChoice wrong2 = itemBuilder.getChoice(Identifier.assumedLegal("mc1b7b8257e2419b880936ea11bff1f1"));
        Assert.assertFalse(itemBuilder.isCorrect(wrong2));
        
        //score
        Double maxScore = itemBuilder.getMaxScoreBuilder().getScore();
        Assert.assertEquals(1.0, maxScore.doubleValue(), 0.0001);
        Double minScore = itemBuilder.getMinScoreBuilder().getScore();
        Assert.assertEquals(0.0, minScore.doubleValue(), 0.0001);
        
        //correct feedback
        ModalFeedbackBuilder correctFeedback = itemBuilder.getCorrectFeedback();
        Assert.assertNotNull(correctFeedback);
        Assert.assertNotNull(correctFeedback.getText());
        Assert.assertTrue(correctFeedback.getText().contains("All answers correct"));
        
        //incorrect
        ModalFeedbackBuilder incorrectFeedback = itemBuilder.getIncorrectFeedback();
        Assert.assertNotNull(incorrectFeedback);
        Assert.assertNotNull(incorrectFeedback.getText());
        Assert.assertTrue(incorrectFeedback.getText().contains("Some choices are wrong"));
        
        //per answer
        Assert.assertEquals(ScoreEvaluation.perAnswer, itemBuilder.getScoreEvaluationMode());
	}
	
	@Test
	public void readMultipleChoice_perAnswer_1123() throws IOException, URISyntaxException {
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
	
		URL itemUrl = AssessmentItemBuilderTest.class.getResource("resources/openolat/multiple-choice-per-answer-11-2-3.xml");
		AssessmentItem assessmentItem = loadAssessmentItem(itemUrl);
		MultipleChoiceAssessmentItemBuilder itemBuilder = new MultipleChoiceAssessmentItemBuilder(assessmentItem, qtiSerializer);

		//basic check ChoiceInteraction
        AssessmentItem loadedItem = itemBuilder.getAssessmentItem();
        List<Interaction> interactions = loadedItem.getItemBody().findInteractions();
        Assert.assertNotNull(interactions);
        Assert.assertEquals(1, interactions.size());
        Assert.assertTrue(interactions.get(0) instanceof ChoiceInteraction);
        
        //choices
        SimpleChoice correct1 = itemBuilder.getChoice(Identifier.assumedLegal("mc80677ac3f4449ebc689cf60c230a3d"));
        Assert.assertTrue(itemBuilder.isCorrect(correct1));
        SimpleChoice correct2 = itemBuilder.getChoice(Identifier.assumedLegal("mc7e5667abeb415fa05a8c7d8fd3d6bb"));
        Assert.assertTrue(itemBuilder.isCorrect(correct2));
        SimpleChoice wrong1 = itemBuilder.getChoice(Identifier.assumedLegal("mcaacc51e0ca4027b3adb3107cda4e30"));
        Assert.assertFalse(itemBuilder.isCorrect(wrong1));
        SimpleChoice wrong2 = itemBuilder.getChoice(Identifier.assumedLegal("mc1b7b8257e2419b880936ea11bff1f1"));
        Assert.assertFalse(itemBuilder.isCorrect(wrong2));
        
        //score
        Double maxScore = itemBuilder.getMaxScoreBuilder().getScore();
        Assert.assertEquals(1.0, maxScore.doubleValue(), 0.0001);
        Double minScore = itemBuilder.getMinScoreBuilder().getScore();
        Assert.assertEquals(0.0, minScore.doubleValue(), 0.0001);
        
        //correct feedback
        ModalFeedbackBuilder correctFeedback = itemBuilder.getCorrectFeedback();
        Assert.assertNotNull(correctFeedback);
        Assert.assertNotNull(correctFeedback.getText());
        Assert.assertTrue(correctFeedback.getText().contains("All answers correct"));
        
        //incorrect
        ModalFeedbackBuilder incorrectFeedback = itemBuilder.getIncorrectFeedback();
        Assert.assertNotNull(incorrectFeedback);
        Assert.assertNotNull(incorrectFeedback.getText());
        Assert.assertTrue(incorrectFeedback.getText().contains("Some choices are wrong"));
        
        //per answer
        Assert.assertEquals(ScoreEvaluation.perAnswer, itemBuilder.getScoreEvaluationMode());
	}
	@Test
	public void readMultipleChoice_allAnswers_1110() throws IOException, URISyntaxException {
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
	
		URL itemUrl = AssessmentItemBuilderTest.class.getResource("resources/openolat/multiple-choice-score-all-11-1-0.xml");
		AssessmentItem assessmentItem = loadAssessmentItem(itemUrl);
		MultipleChoiceAssessmentItemBuilder itemBuilder = new MultipleChoiceAssessmentItemBuilder(assessmentItem, qtiSerializer);

		//basic check ChoiceInteraction
        AssessmentItem loadedItem = itemBuilder.getAssessmentItem();
        List<Interaction> interactions = loadedItem.getItemBody().findInteractions();
        Assert.assertNotNull(interactions);
        Assert.assertEquals(2, interactions.size());
        Assert.assertTrue(interactions.get(0) instanceof ChoiceInteraction);
        Assert.assertTrue(interactions.get(1) instanceof EndAttemptInteraction);
        
        //choices
        SimpleChoice correct1 = itemBuilder.getChoice(Identifier.assumedLegal("mc1959a495d449f9af65b38695d3aff1"));
        Assert.assertTrue(itemBuilder.isCorrect(correct1));
        SimpleChoice correct2 = itemBuilder.getChoice(Identifier.assumedLegal("mca856e7adb54d3f9af06ecf9c00da69"));
        Assert.assertTrue(itemBuilder.isCorrect(correct2));
        SimpleChoice wrong1 = itemBuilder.getChoice(Identifier.assumedLegal("mcd39be64a6b4f20a2372cba44340e59"));
        Assert.assertFalse(itemBuilder.isCorrect(wrong1));
        SimpleChoice wrong2 = itemBuilder.getChoice(Identifier.assumedLegal("mc18648f96a84d479817cb5e81165c80"));
        Assert.assertFalse(itemBuilder.isCorrect(wrong2));
        
        //score
        Double maxScore = itemBuilder.getMaxScoreBuilder().getScore();
        Assert.assertEquals(2.0, maxScore.doubleValue(), 0.0001);
        Double minScore = itemBuilder.getMinScoreBuilder().getScore();
        Assert.assertEquals(0.0, minScore.doubleValue(), 0.0001);
        
        //correct feedback
        ModalFeedbackBuilder correctFeedback = itemBuilder.getCorrectFeedback();
        Assert.assertNotNull(correctFeedback);
        Assert.assertNotNull(correctFeedback.getText());
        Assert.assertTrue(correctFeedback.getText().contains("All answers are correct"));
        
        //incorrect
        ModalFeedbackBuilder incorrectFeedback = itemBuilder.getIncorrectFeedback();
        Assert.assertNotNull(incorrectFeedback);
        Assert.assertNotNull(incorrectFeedback.getText());
        Assert.assertTrue(incorrectFeedback.getText().contains("You missed something"));
        
        //hint
        ModalFeedbackBuilder hint = itemBuilder.getHint();
        Assert.assertNotNull(hint);
        Assert.assertNotNull(hint.getText());
        Assert.assertTrue(hint.getText().contains("This is the correct solution"));
        
        //per answer
        Assert.assertEquals(ScoreEvaluation.allCorrectAnswers, itemBuilder.getScoreEvaluationMode());
	}
	
	@Test
	public void readMultipleChoice_allAnswers_1123() throws IOException, URISyntaxException {
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
	
		URL itemUrl = AssessmentItemBuilderTest.class.getResource("resources/openolat/multiple-choice-score-all-11-2-3.xml");
		AssessmentItem assessmentItem = loadAssessmentItem(itemUrl);
		MultipleChoiceAssessmentItemBuilder itemBuilder = new MultipleChoiceAssessmentItemBuilder(assessmentItem, qtiSerializer);

		//basic check ChoiceInteraction
        AssessmentItem loadedItem = itemBuilder.getAssessmentItem();
        List<Interaction> interactions = loadedItem.getItemBody().findInteractions();
        Assert.assertNotNull(interactions);
        Assert.assertEquals(2, interactions.size());
        Assert.assertTrue(interactions.get(0) instanceof ChoiceInteraction);
        Assert.assertTrue(interactions.get(1) instanceof EndAttemptInteraction);
        
        //choices
        SimpleChoice correct1 = itemBuilder.getChoice(Identifier.assumedLegal("mc1959a495d449f9af65b38695d3aff1"));
        Assert.assertTrue(itemBuilder.isCorrect(correct1));
        SimpleChoice correct2 = itemBuilder.getChoice(Identifier.assumedLegal("mca856e7adb54d3f9af06ecf9c00da69"));
        Assert.assertTrue(itemBuilder.isCorrect(correct2));
        SimpleChoice wrong1 = itemBuilder.getChoice(Identifier.assumedLegal("mcd39be64a6b4f20a2372cba44340e59"));
        Assert.assertFalse(itemBuilder.isCorrect(wrong1));
        SimpleChoice wrong2 = itemBuilder.getChoice(Identifier.assumedLegal("mc18648f96a84d479817cb5e81165c80"));
        Assert.assertFalse(itemBuilder.isCorrect(wrong2));
        
        //score
        Double maxScore = itemBuilder.getMaxScoreBuilder().getScore();
        Assert.assertEquals(2.0, maxScore.doubleValue(), 0.0001);
        Double minScore = itemBuilder.getMinScoreBuilder().getScore();
        Assert.assertEquals(0.0, minScore.doubleValue(), 0.0001);
        
        //correct feedback
        ModalFeedbackBuilder correctFeedback = itemBuilder.getCorrectFeedback();
        Assert.assertNotNull(correctFeedback);
        Assert.assertNotNull(correctFeedback.getText());
        Assert.assertTrue(correctFeedback.getText().contains("All answers are correct"));
        
        //incorrect
        ModalFeedbackBuilder incorrectFeedback = itemBuilder.getIncorrectFeedback();
        Assert.assertNotNull(incorrectFeedback);
        Assert.assertNotNull(incorrectFeedback.getText());
        Assert.assertTrue(incorrectFeedback.getText().contains("You missed something"));
        
        //hint
        ModalFeedbackBuilder hint = itemBuilder.getHint();
        Assert.assertNotNull(hint);
        Assert.assertNotNull(hint.getText());
        Assert.assertTrue(hint.getText().contains("This is the correct solution"));
        
        //per answer
        Assert.assertEquals(ScoreEvaluation.allCorrectAnswers, itemBuilder.getScoreEvaluationMode());
	}
	
	/**
	 * The test run the assessmentItem and check the outcome variables SCORE and FEEDBACKBASIC.
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	public void runMultipleChoice_allAnswers_1123() throws URISyntaxException {
        URI inputUri = URI.create("classpath:/org/olat/ims/qti21/model/xml/resources/openolat/multiple-choice-score-all-11-2-3.xml");
		
        //check all correct answers
        {
	        Map<Identifier, ResponseData> responseMap = new HashMap<>();
	        responseMap.put(Identifier.parseString("RESPONSE_1"), new StringResponseData("mc1959a495d449f9af65b38695d3aff1", "mca856e7adb54d3f9af06ecf9c00da69"));
			ItemSessionController itemSessionController = RunningItemHelper.run(inputUri, responseMap);
			Value score = itemSessionController.getItemSessionState().getOutcomeValue(QTI21Constants.SCORE_IDENTIFIER);
			Assert.assertEquals(new FloatValue(2.0d), score);
			Value feedbackBasic = itemSessionController.getItemSessionState().getOutcomeValue(QTI21Constants.FEEDBACKBASIC_IDENTIFIER);
			Assert.assertEquals(new IdentifierValue(QTI21Constants.CORRECT_IDENTIFIER), feedbackBasic);
        }
		
		//check one correct answers
        {
			Map<Identifier, ResponseData> responseMap = new HashMap<>();
	        responseMap.put(Identifier.parseString("RESPONSE_1"), new StringResponseData("mc1959a495d449f9af65b38695d3aff1"));
			ItemSessionController itemSessionController = RunningItemHelper.run(inputUri, responseMap);
			Value score = itemSessionController.getItemSessionState().getOutcomeValue(QTI21Constants.SCORE_IDENTIFIER);
			Assert.assertEquals(new FloatValue(0.0d), score);
			Value feedbackBasic = itemSessionController.getItemSessionState().getOutcomeValue(QTI21Constants.FEEDBACKBASIC_IDENTIFIER);
			Assert.assertEquals(new IdentifierValue(QTI21Constants.INCORRECT_IDENTIFIER), feedbackBasic);
        }
	}
	
	private AssessmentItem loadAssessmentItem(URL itemUrl) throws URISyntaxException {
		QtiXmlReader qtiXmlReader = new QtiXmlReader(new JqtiExtensionManager());
		ResourceLocator fileResourceLocator = new PathResourceLocator(Paths.get(itemUrl.toURI()));
        AssessmentObjectXmlLoader assessmentObjectXmlLoader = new AssessmentObjectXmlLoader(qtiXmlReader, fileResourceLocator);
        ResolvedAssessmentItem item = assessmentObjectXmlLoader.loadAndResolveAssessmentItem(itemUrl.toURI());
		return item.getItemLookup().getRootNodeHolder().getRootNode();
	}
}