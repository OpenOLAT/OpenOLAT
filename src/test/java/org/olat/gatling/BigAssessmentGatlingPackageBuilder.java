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
package org.olat.gatling;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.logging.Tracing;
import org.olat.ims.qti21.model.IdentifierGenerator;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.AssessmentItemFactory;
import org.olat.ims.qti21.model.xml.AssessmentTestFactory;
import org.olat.ims.qti21.model.xml.ManifestBuilder;

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.node.content.variable.RubricBlock;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentSection;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.test.ItemSessionControl;
import uk.ac.ed.ph.jqtiplus.node.test.Ordering;
import uk.ac.ed.ph.jqtiplus.node.test.Selection;
import uk.ac.ed.ph.jqtiplus.node.test.TestPart;
import uk.ac.ed.ph.jqtiplus.node.test.View;
import uk.ac.ed.ph.jqtiplus.reading.AssessmentObjectXmlLoader;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlReader;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.testutils.UnitTestHelper;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.FileResourceLocator;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * 
 * Initial date: 04.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigAssessmentGatlingPackageBuilder {
	
	private static final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HHmmss");
	private static final Logger log = Tracing.createLoggerFor(BigAssessmentGatlingPackageBuilder.class);
	private static final QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
	
	private int numOfSections = 15;
	private int numOfQuestions = 500;
	
	@Test
	public void createAssessmentTest() throws URISyntaxException {
		String date = format.format(new Date());
		File directory = new File("/HotCoffee/qti/" + date + "/");
		directory.mkdirs();
		ManifestBuilder manifest = ManifestBuilder.createAssessmentTestBuilder();
		System.out.println(directory);
        

		//test
        File testFile = new File(directory, IdentifierGenerator.newAssessmentTestFilename());
		AssessmentTest assessmentTest = AssessmentTestFactory.createAssessmentTest("Big test " + date, "Section");
		manifest.appendAssessmentTest(testFile.getName());

		TestPart part = assessmentTest.getTestParts().get(0);
		part.getAssessmentSections().clear();

		// section
		for(int i=0; i<numOfSections; i++) {
			AssessmentSection section = new AssessmentSection(part);
			section.setFixed(Boolean.TRUE);
			section.setVisible(Boolean.TRUE);
			section.setTitle((i+1) + ". Section");
			section.setIdentifier(IdentifierGenerator.newAsIdentifier("sec"));
			part.getAssessmentSections().add(section);
			
			Ordering ordering = new Ordering(section);
			ordering.setShuffle(true);
			section.setOrdering(ordering);
			
			Selection selection = new Selection(section);
			selection.setSelect(4);
			section.setSelection(selection);
			
			ItemSessionControl itemSessionControl = new ItemSessionControl(section);
			itemSessionControl.setAllowSkipping(Boolean.TRUE);
			itemSessionControl.setAllowComment(Boolean.FALSE);
			itemSessionControl.setShowFeedback(Boolean.FALSE);
			section.setItemSessionControl(itemSessionControl);
			
			RubricBlock rubrickBlock = new RubricBlock(section);
			rubrickBlock.setViews(Collections.singletonList(View.CANDIDATE));
			section.getRubricBlocks().add(rubrickBlock);
			
			for(int j=0; j<numOfQuestions; j++) {
				//single choice
				String itemId = IdentifierGenerator.newAsString(QTI21QuestionType.sc.getPrefix());
				File itemFile = new File(directory, itemId + ".xml");
				AssessmentItem assessmentItem = AssessmentItemFactory.createSingleChoice("Single choice", "New answer");
				assessmentItem.setTitle((i+1) + "." + (j+1) + ". Question SC");
				
				AssessmentTestFactory.appendAssessmentItem(section, itemFile.getName());
				manifest.appendAssessmentItem(itemFile.getName());	
				
				try(FileOutputStream out = new FileOutputStream(itemFile)) {
					qtiSerializer.serializeJqtiObject(assessmentItem, out);	
				} catch(Exception e) {
					log.error("", e);
				}
			}
		}
		
		try(FileOutputStream out = new FileOutputStream(testFile)) {
			qtiSerializer.serializeJqtiObject(assessmentTest, out);	
		} catch(Exception e) {
			log.error("", e);
		}
		
		manifest.write(new File(directory, "imsmanifest.xml"));
	}
	

	@Test
	public void openBigTest_twice() {
		final long time = openBigTest_sub();
        System.out.println("Takes (ms): " + (time / 1000000));

		try {
			System.gc();
			System.gc();

			Thread.sleep(120000);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

	public long openBigTest_sub() {
		final File resourceFile = new File("/HotCoffee/QTI/20160219-180424/testfa908329-ab44-4821-a20d-ca634b6afb06.xml");
		final QtiXmlReader qtiXmlReader = UnitTestHelper.createUnitTestQtiXmlReader();
		final ResourceLocator fileResourceLocator = new FileResourceLocator();

		final long start = System.nanoTime();
        final AssessmentObjectXmlLoader assessmentObjectXmlLoader = new AssessmentObjectXmlLoader(qtiXmlReader, fileResourceLocator);
        final ResolvedAssessmentTest resolvedTest = assessmentObjectXmlLoader.loadAndResolveAssessmentTest(resourceFile.toURI());
        Assert.assertNotNull(resolvedTest);
        final AssessmentTest test = resolvedTest.getTestLookup().extractIfSuccessful();
        Assert.assertNotNull(test);
        final long time = (System.nanoTime() - start);

        final AssessmentItemRef itemRef = resolvedTest.getAssessmentItemRefs().get(0);
        final ResolvedAssessmentItem resolvedItem = resolvedTest.getResolvedAssessmentItem(itemRef);
        Assert.assertNotNull(resolvedItem);
        final AssessmentItem item = resolvedItem.getRootNodeLookup().extractIfSuccessful();
        Assert.assertNotNull(item);
        Assert.assertEquals(1, item.getItemBody().findInteractions().size());
        return time;
	}
}