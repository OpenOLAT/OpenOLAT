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
package org.olat.ims.qti21.repository.handlers;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;
import org.olat.ims.qti21.model.xml.BadRessourceHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.notification.Notification;
import uk.ac.ed.ph.jqtiplus.provision.BadResourceException;
import uk.ac.ed.ph.jqtiplus.reading.AssessmentObjectXmlLoader;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlReader;
import uk.ac.ed.ph.jqtiplus.utils.contentpackaging.ContentPackageResource;
import uk.ac.ed.ph.jqtiplus.utils.contentpackaging.ImsManifestException;
import uk.ac.ed.ph.jqtiplus.utils.contentpackaging.QtiContentPackageExtractor;
import uk.ac.ed.ph.jqtiplus.utils.contentpackaging.QtiContentPackageSummary;
import uk.ac.ed.ph.jqtiplus.validation.ItemValidationResult;
import uk.ac.ed.ph.jqtiplus.validation.TestValidationResult;
import uk.ac.ed.ph.jqtiplus.xmlutils.XmlResourceNotFoundException;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * 
 * Initial date: 04.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21AssessmentTestHandlerTest extends OlatTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(QTI21AssessmentTestHandlerTest.class);
	
	@Autowired
	private QTI21AssessmentTestHandler testHandler;

	/**
	 * The test creates a QTI 2.1 test with the QTI handler and check if all
	 * the elements are valid, assessment test, assessment item and
	 * manifest.
	 * 
	 * @throws IOException
	 * @throws XmlResourceNotFoundException
	 * @throws ImsManifestException
	 */
	@Test
	public void createAssessmentTest() throws IOException, XmlResourceNotFoundException, ImsManifestException {
		File tmpDir = new File(WebappHelper.getTmpDir(), "qti21fullpackage" + UUID.randomUUID());
		tmpDir.mkdirs();
		
		testHandler.createMinimalAssessmentTest("Generated test", tmpDir, Locale.ENGLISH);
		
		boolean foundImsManifest = false;
		boolean foundAssessmentTest = false;
		boolean foundAssessmentItem = false;
		
		boolean validAssessmentTest = false;
		boolean validAssessmentItem = false;
		boolean readableManifest = false;
		
		File[] generatedFiles = tmpDir.listFiles();
		for(File generatedFile:generatedFiles) {
			String filename = generatedFile.getName();
			if(filename.equals("imsmanifest.xml")) {
				foundImsManifest = true;
				readableManifest = readManifest(tmpDir);
			} else if(filename.startsWith("test")) {
				foundAssessmentTest = true;
				validAssessmentTest = validateAssessmentTest(generatedFile);
			} else if(filename.startsWith("sc")) {
				foundAssessmentItem = true;
				validAssessmentItem = validateAssessmentItem(generatedFile);
			}	
		}
		
		//delete tmp
        FileUtils.deleteDirsAndFiles(tmpDir.toPath());
        
        //checks
        Assert.assertTrue(foundImsManifest);
        Assert.assertTrue(foundAssessmentTest);
        Assert.assertTrue(foundAssessmentItem);
        
        Assert.assertTrue(validAssessmentTest);
        Assert.assertTrue(validAssessmentItem);
        
        Assert.assertTrue(readableManifest);
	}
	
	private boolean validateAssessmentTest(File assessmentTestFile) {
		QtiXmlReader qtiXmlReader = new QtiXmlReader(new JqtiExtensionManager());
		ResourceLocator fileResourceLocator = new PathResourceLocator(assessmentTestFile.toPath());
        AssessmentObjectXmlLoader assessmentObjectXmlLoader = new AssessmentObjectXmlLoader(qtiXmlReader, fileResourceLocator);
        TestValidationResult test = assessmentObjectXmlLoader.loadResolveAndValidateTest(assessmentTestFile.toURI());

        for(Notification notification: test.getModelValidationErrors()) {
        	log.error(notification.getQtiNode() + " : " + notification.getMessage());
        }
        
        BadResourceException e = test.getResolvedAssessmentTest().getTestLookup().getBadResourceException();
        StringBuilder err = new StringBuilder();
        BadRessourceHelper.extractMessage(e, err);
        log.error(err.toString());

        return test.getModelValidationErrors().isEmpty();
	}
	
	private boolean validateAssessmentItem(File assessmentItemFile) {
		QtiXmlReader qtiXmlReader = new QtiXmlReader(new JqtiExtensionManager());
		ResourceLocator fileResourceLocator = new PathResourceLocator(assessmentItemFile.toPath());
        AssessmentObjectXmlLoader assessmentObjectXmlLoader = new AssessmentObjectXmlLoader(qtiXmlReader, fileResourceLocator);
        ItemValidationResult itemResult = assessmentObjectXmlLoader.loadResolveAndValidateItem(assessmentItemFile.toURI());

        BadResourceException e = itemResult.getResolvedAssessmentItem().getItemLookup().getBadResourceException();
        if(e != null) {
			StringBuilder err = new StringBuilder();
			BadRessourceHelper.extractMessage(e, err);
			log.error(err.toString());
		}
        return itemResult.getModelValidationErrors().isEmpty();
	}
	
	private boolean readManifest(File tmpDir) throws ImsManifestException, XmlResourceNotFoundException {
		QtiContentPackageExtractor extractor = new QtiContentPackageExtractor(tmpDir);
        QtiContentPackageSummary summary = extractor.parse();
        List<ContentPackageResource> items = summary.getItemResources();
        List<ContentPackageResource> tests = summary.getTestResources();
        return items.size() == 1 && tests.size() == 1;
	}
}
