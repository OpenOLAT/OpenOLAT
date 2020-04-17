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

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;
import org.olat.ims.qti21.model.xml.interactions.OrderAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.SimpleChoiceAssessmentItemBuilder.ScoreEvaluation;
import org.olat.ims.qti21.model.xml.interactions.SingleChoiceAssessmentItemBuilder;

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleChoice;
import uk.ac.ed.ph.jqtiplus.reading.AssessmentObjectXmlLoader;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlReader;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * 
 * Initial date: 29 août 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OnyxToAssessmentItemBuilderTest {
	
	@Test
	public void extractSingleChoiceWithFeedbacks()  throws URISyntaxException {
		URL itemUrl = OnyxToAssessmentItemBuilderTest.class.getResource("resources/onyx/single-choice-1-with-feedbacks_5-11.xml");
		AssessmentItem assessmentItem = loadAssessmentItem(itemUrl);
		
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		SingleChoiceAssessmentItemBuilder itemBuilder = new SingleChoiceAssessmentItemBuilder(assessmentItem, qtiSerializer);
		
		//correct answer
		List<SimpleChoice> choices = itemBuilder.getChoices();
		Assert.assertNotNull(choices);
		SimpleChoice choice = choices.get(3);
		Assert.assertTrue(itemBuilder.isCorrect(choice));
		
		//scoring
		Assert.assertEquals(ScoreEvaluation.allCorrectAnswers, itemBuilder.getScoreEvaluationMode());
		ScoreBuilder maxScoreBuilder = itemBuilder.getMaxScoreBuilder();
		Assert.assertEquals(4.0d, maxScoreBuilder.getScore(), 0.00001d);
		
		// check standard feedback
		ModalFeedbackBuilder correctFeedback = itemBuilder.getCorrectFeedback();
		Assert.assertNotNull(correctFeedback);
		Assert.assertTrue(correctFeedback.isCorrectRule());
		Assert.assertEquals("<p>Richtig Text</p>", correctFeedback.getText());
		
		ModalFeedbackBuilder incorrectFeedback = itemBuilder.getIncorrectFeedback();
		Assert.assertNotNull(incorrectFeedback);
		Assert.assertTrue(incorrectFeedback.isIncorrectRule());
		Assert.assertEquals("<p>Falsch Text</p>", incorrectFeedback.getText());
	}
	
	@Test
	public void extractSingleChoiceWithExpertConditionFeedbacks() throws URISyntaxException {
		URL itemUrl = OnyxToAssessmentItemBuilderTest.class.getResource("resources/onyx/sc-expert-conditions-feedback.xml");
		AssessmentItem assessmentItem = loadAssessmentItem(itemUrl);
		
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		SingleChoiceAssessmentItemBuilder itemBuilder = new SingleChoiceAssessmentItemBuilder(assessmentItem, qtiSerializer);
		List<ModalFeedbackBuilder> feedbackBuilders = itemBuilder.getAdditionalFeedbackBuilders();
		Assert.assertEquals(1, feedbackBuilders.size());
	}
	
	/**
	 * Test a version 3.6 of the order interaction without feedback
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	public void extractOrder() throws URISyntaxException {
		URL itemUrl = OnyxToAssessmentItemBuilderTest.class.getResource("resources/onyx/order-3-6.xml");
		AssessmentItem assessmentItem = loadAssessmentItem(itemUrl);
		
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		OrderAssessmentItemBuilder itemBuilder = new OrderAssessmentItemBuilder(assessmentItem, qtiSerializer);
		Assert.assertEquals(Double.valueOf(1.0d), itemBuilder.getMaxScoreBuilder().getScore());
		Assert.assertEquals(4, itemBuilder.getChoices().size());
	}
	
	/**
	 * Test a version mordern of the order interaction with feedbacks
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	public void extractOrderWithFeedbacks() throws URISyntaxException {
		URL itemUrl = OnyxToAssessmentItemBuilderTest.class.getResource("resources/onyx/order-feedback.xml");
		AssessmentItem assessmentItem = loadAssessmentItem(itemUrl);
		
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		OrderAssessmentItemBuilder itemBuilder = new OrderAssessmentItemBuilder(assessmentItem, qtiSerializer);
		Assert.assertEquals(3, itemBuilder.getChoices().size());
		
		//scoring
		Assert.assertEquals(ScoreEvaluation.allCorrectAnswers, itemBuilder.getScoreEvaluationMode());
		ScoreBuilder maxScoreBuilder = itemBuilder.getMaxScoreBuilder();
		Assert.assertEquals(10.0d, maxScoreBuilder.getScore(), 0.00001d);
		
		// check standard feedback
		ModalFeedbackBuilder correctFeedback = itemBuilder.getCorrectFeedback();
		Assert.assertNotNull(correctFeedback);
		Assert.assertTrue(correctFeedback.isCorrectRule());
		Assert.assertEquals("<p>Gut gemacht!</p>", correctFeedback.getText());
		
		ModalFeedbackBuilder incorrectFeedback = itemBuilder.getIncorrectFeedback();
		Assert.assertNotNull(incorrectFeedback);
		Assert.assertTrue(incorrectFeedback.isIncorrectRule());
		Assert.assertEquals("<p>Bitte noch einmal ordnen!</p>", incorrectFeedback.getText());
	}
	
	private AssessmentItem loadAssessmentItem(URL itemUrl) throws URISyntaxException {
		QtiXmlReader qtiXmlReader = new QtiXmlReader(new JqtiExtensionManager());
		ResourceLocator fileResourceLocator = new PathResourceLocator(Paths.get(itemUrl.toURI()));
        AssessmentObjectXmlLoader assessmentObjectXmlLoader = new AssessmentObjectXmlLoader(qtiXmlReader, fileResourceLocator);
        ResolvedAssessmentItem item = assessmentObjectXmlLoader.loadAndResolveAssessmentItem(itemUrl.toURI());
		return item.getItemLookup().getRootNodeHolder().getRootNode();
	}

}
