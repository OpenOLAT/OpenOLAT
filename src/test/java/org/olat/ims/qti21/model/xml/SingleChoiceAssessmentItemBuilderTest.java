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
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;
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
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * 
 * Initial date: 20 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SingleChoiceAssessmentItemBuilderTest {
	
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
	
	private AssessmentItem loadAssessmentItem(URL itemUrl) throws URISyntaxException {
		QtiXmlReader qtiXmlReader = new QtiXmlReader(new JqtiExtensionManager());
		ResourceLocator fileResourceLocator = new PathResourceLocator(Paths.get(itemUrl.toURI()));
        AssessmentObjectXmlLoader assessmentObjectXmlLoader = new AssessmentObjectXmlLoader(qtiXmlReader, fileResourceLocator);
        ResolvedAssessmentItem item = assessmentObjectXmlLoader.loadAndResolveAssessmentItem(itemUrl.toURI());
		return item.getItemLookup().getRootNodeHolder().getRootNode();
	}

}
