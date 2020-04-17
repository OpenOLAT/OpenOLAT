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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.model.xml.interactions.OrderAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.SimpleChoiceAssessmentItemBuilder.ScoreEvaluation;

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleChoice;
import uk.ac.ed.ph.jqtiplus.running.ItemSessionController;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.types.ResponseData;
import uk.ac.ed.ph.jqtiplus.types.StringResponseData;
import uk.ac.ed.ph.jqtiplus.value.FloatValue;
import uk.ac.ed.ph.jqtiplus.value.Value;

/**
 * 
 * Initial date: 15 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrderAssessmentItemBuilderTest {
	
	private static final Logger log = Tracing.createLoggerFor(OrderAssessmentItemBuilderTest.class);
	
	/**
	 * A basic orderInteraction with three choices. If all answers
	 * are correct, the SCORE is 3.0.
	 * 
	 * @throws IOException
	 */
	@Test
	public void createOrderAssessmentItem_allCorrectAnswers() throws IOException {
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		OrderAssessmentItemBuilder itemBuilder = new OrderAssessmentItemBuilder("Order", "This is ordered", qtiSerializer);
		itemBuilder.setQuestion("<p>This is a question with a lot of order:</p>");
		
		SimpleChoice choice1 = AssessmentItemFactory.createSimpleChoice(itemBuilder.getOrderInteraction(), "One", "order");
		SimpleChoice choice2 = AssessmentItemFactory.createSimpleChoice(itemBuilder.getOrderInteraction(), "Two", "order");
		SimpleChoice choice3 = AssessmentItemFactory.createSimpleChoice(itemBuilder.getOrderInteraction(), "Three", "order");

		List<SimpleChoice> choices = new ArrayList<>();
		choices.add(choice1);
		choices.add(choice2);
		choices.add(choice3);
		itemBuilder.setSimpleChoices(choices);
		itemBuilder.setMaxScore(3.0d);
		itemBuilder.setScoreEvaluationMode(ScoreEvaluation.allCorrectAnswers);
		itemBuilder.build();
		
		File itemFile = new File(WebappHelper.getTmpDir(), "orderAssessmentItem" + UUID.randomUUID() + ".xml");
		try(FileOutputStream out = new FileOutputStream(itemFile)) {
			qtiSerializer.serializeJqtiObject(itemBuilder.getAssessmentItem(), out);
		} catch(Exception e) {
			log.error("", e);
		}
		
		{// correct answers
			Map<Identifier, ResponseData> responseMap = new HashMap<>();
			Identifier responseIdentifier = itemBuilder.getInteraction().getResponseIdentifier();
	        responseMap.put(responseIdentifier, new StringResponseData(choice1.getIdentifier().toString(),
	        		choice2.getIdentifier().toString(), choice3.getIdentifier().toString()));
			ItemSessionController itemSessionController = RunningItemHelper.run(itemFile, responseMap);
			Value score = itemSessionController.getItemSessionState().getOutcomeValue(QTI21Constants.SCORE_IDENTIFIER);
			Assert.assertEquals(new FloatValue(3.0d), score);
		}
		
		{// wrong answer
			Map<Identifier, ResponseData> responseMap = new HashMap<>();
			Identifier responseIdentifier = itemBuilder.getInteraction().getResponseIdentifier();
	        responseMap.put(responseIdentifier, new StringResponseData(choice3.getIdentifier().toString(),
	        		choice2.getIdentifier().toString(), choice1.getIdentifier().toString()));
			ItemSessionController itemSessionController = RunningItemHelper.run(itemFile, responseMap);
			Value score = itemSessionController.getItemSessionState().getOutcomeValue(QTI21Constants.SCORE_IDENTIFIER);
			Assert.assertEquals(new FloatValue(0.0d), score);
		}
		
		FileUtils.deleteDirsAndFiles(itemFile.toPath());
	}
}
