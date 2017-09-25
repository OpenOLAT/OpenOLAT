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

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.template.declaration.TemplateDeclaration;
import uk.ac.ed.ph.jqtiplus.reading.AssessmentObjectXmlLoader;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlReader;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.BaseType;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * Use some templates of QtiWorks sets.
 * 
 * Initial date: 5 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentItemCheckerTest {
	
	@Test
	public void checkAndCorrect_wrongType() throws URISyntaxException {
		URL itemUrl = OnyxToAssessmentItemBuilderTest.class.getResource("resources/onyx/set-correct-response-wrong-type-5-11.xml");
		AssessmentItem assessmentItem = loadAssessmentItem(itemUrl);
		boolean ok = AssessmentItemChecker.checkAndCorrect(assessmentItem);
		Assert.assertFalse(ok);

		TemplateDeclaration solutionDeclaration = assessmentItem.getTemplateDeclaration(Identifier.assumedLegal("solution"));
		Assert.assertEquals(BaseType.FLOAT, solutionDeclaration.getBaseType());
	}
	
	@Test
	public void checkAndCorrect_rightType() throws URISyntaxException {
		URL itemUrl = OnyxToAssessmentItemBuilderTest.class.getResource("resources/umpc/addition.xml");
		AssessmentItem assessmentItem = loadAssessmentItem(itemUrl);
		boolean ok = AssessmentItemChecker.checkAndCorrect(assessmentItem);
		Assert.assertTrue(ok);

		TemplateDeclaration solutionDeclaration = assessmentItem.getTemplateDeclaration(Identifier.assumedLegal("SOLUTION1"));
		Assert.assertEquals(BaseType.FLOAT, solutionDeclaration.getBaseType());
	}
	
	@Test
	public void checkAndCorrect_notTemplateDeclaration() throws URISyntaxException {
		URL itemUrl = OnyxToAssessmentItemBuilderTest.class.getResource("resources/ims/template_image.xml");
		AssessmentItem assessmentItem = loadAssessmentItem(itemUrl);
		boolean ok = AssessmentItemChecker.checkAndCorrect(assessmentItem);
		Assert.assertTrue(ok);

		TemplateDeclaration templateDeclaration = assessmentItem.getTemplateDeclaration(Identifier.assumedLegal("SOLUTION1"));
		Assert.assertNull(templateDeclaration);
	}
	
	@Test
	public void checkAndCorrect_hottextOpenOLAT() throws URISyntaxException {
		URL itemUrl = OnyxToAssessmentItemBuilderTest.class.getResource("resources/openolat/hottext-score-all-11-4-0.xml");
		AssessmentItem assessmentItem = loadAssessmentItem(itemUrl);
		boolean ok = AssessmentItemChecker.checkAndCorrect(assessmentItem);
		Assert.assertTrue(ok);

		TemplateDeclaration templateDeclaration = assessmentItem.getTemplateDeclaration(Identifier.assumedLegal("SOLUTION1"));
		Assert.assertNull(templateDeclaration);
	}
	
	private AssessmentItem loadAssessmentItem(URL itemUrl) throws URISyntaxException {
		QtiXmlReader qtiXmlReader = new QtiXmlReader(new JqtiExtensionManager());
		ResourceLocator fileResourceLocator = new PathResourceLocator(Paths.get(itemUrl.toURI()));
        AssessmentObjectXmlLoader assessmentObjectXmlLoader = new AssessmentObjectXmlLoader(qtiXmlReader, fileResourceLocator);
        ResolvedAssessmentItem item = assessmentObjectXmlLoader.loadAndResolveAssessmentItem(itemUrl.toURI());
		return item.getItemLookup().getRootNodeHolder().getRootNode();
	}

}
