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

import org.junit.Assert;
import org.junit.Test;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;
import org.olat.ims.qti21.model.xml.interactions.SingleChoiceAssessmentItemBuilder;

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.reading.AssessmentObjectXmlLoader;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlReader;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * 
 * Initial date: 20.05.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentItemBuilderTest {
	
	private final JqtiExtensionManager jqtiExtensionManager = new JqtiExtensionManager();
	private final QtiXmlReader qtiXmlReader = new QtiXmlReader(jqtiExtensionManager);
	private final QtiSerializer qtiSerializer = new QtiSerializer(jqtiExtensionManager);
	
	@Test
	public void findFeedbacks()  throws URISyntaxException {
		URL itemUrl = AssessmentItemBuilderTest.class.getResource("assessment-item-single-choice-feedbacks.xml");
		AssessmentItem assessmentItem = loadAssessmentItem(itemUrl);
		SingleChoiceAssessmentItemBuilder itemBuilder = new SingleChoiceAssessmentItemBuilder(assessmentItem, qtiSerializer);
		
		ModalFeedbackBuilder correctFeedback = itemBuilder.getCorrectFeedback();
		Assert.assertNotNull(correctFeedback);
		Assert.assertTrue(correctFeedback.isCorrectRule());
		
		ModalFeedbackBuilder incorrectFeedback = itemBuilder.getIncorrectFeedback();
		Assert.assertNotNull(incorrectFeedback);
		Assert.assertTrue(incorrectFeedback.isIncorrectRule());
		
	}
	
	private AssessmentItem loadAssessmentItem(URL itemUrl) throws URISyntaxException {
		ResourceLocator fileResourceLocator = new PathResourceLocator(Paths.get(itemUrl.toURI()));
        AssessmentObjectXmlLoader assessmentObjectXmlLoader = new AssessmentObjectXmlLoader(qtiXmlReader, fileResourceLocator);
        ResolvedAssessmentItem item = assessmentObjectXmlLoader.loadAndResolveAssessmentItem(itemUrl.toURI());
		return item.getItemLookup().getRootNodeHolder().getRootNode();
	}

}
