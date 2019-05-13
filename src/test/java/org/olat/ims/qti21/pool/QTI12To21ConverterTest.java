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
package org.olat.ims.qti21.pool;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.UUID;

import org.dom4j.Document;
import org.junit.Assert;
import org.junit.Test;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.xml.XMLParser;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;
import org.olat.ims.qti.editor.beecom.objects.QTIDocument;
import org.olat.ims.qti.editor.beecom.parser.ParserManager;
import org.olat.ims.qti21.model.xml.BadRessourceHelper;
import org.olat.ims.resources.IMSEntityResolver;

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.notification.Notification;
import uk.ac.ed.ph.jqtiplus.provision.BadResourceException;
import uk.ac.ed.ph.jqtiplus.reading.AssessmentObjectXmlLoader;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlReader;
import uk.ac.ed.ph.jqtiplus.utils.contentpackaging.ImsManifestException;
import uk.ac.ed.ph.jqtiplus.utils.contentpackaging.QtiContentPackageExtractor;
import uk.ac.ed.ph.jqtiplus.utils.contentpackaging.QtiContentPackageSummary;
import uk.ac.ed.ph.jqtiplus.validation.ItemValidationResult;
import uk.ac.ed.ph.jqtiplus.validation.TestValidationResult;
import uk.ac.ed.ph.jqtiplus.xmlutils.XmlResourceNotFoundException;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * 
 * Initial date: 19.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI12To21ConverterTest {
	
	private static final Logger log = Tracing.createLoggerFor(QTI12To21ConverterTest.class);
	
	@Test
	public void convert() throws URISyntaxException, IOException, XmlResourceNotFoundException, ImsManifestException {
		QTIDocument doc = loadDocument("qti12_4questiontypes.xml");
		
		File exportDir = new File(WebappHelper.getTmpDir(), "qti12to21" + UUID.randomUUID());
		exportDir.mkdirs();

		QTI12To21Converter converter = new QTI12To21Converter(exportDir, Locale.ENGLISH);
		converter.convert(null, doc, null);
		
		int validAssessmentItems = 0;
		boolean validAssessmentTest = false;
		QtiContentPackageSummary readableManifest = null;
		
		File[] generatedFiles = exportDir.listFiles();
		for(File generatedFile:generatedFiles) {
			String filename = generatedFile.getName();
			if(filename.equals("imsmanifest.xml")) {
				readableManifest = new QtiContentPackageExtractor(exportDir).parse();
			} else if(filename.startsWith("test")) {
				validAssessmentTest = validateAssessmentTest(generatedFile);
			} else if(filename.endsWith(".xml")) {
				boolean validItem = validateAssessmentItem(generatedFile);
				if(validItem) {
					validAssessmentItems++;
				}
			}
		}
		
		//delete tmp
        FileUtils.deleteDirsAndFiles(exportDir.toPath());
		
		Assert.assertTrue(validAssessmentTest);
		Assert.assertEquals(4, validAssessmentItems);
		Assert.assertEquals(1, readableManifest.getTestResources().size());
		Assert.assertEquals(4, readableManifest.getItemResources().size());
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
        if(e != null) {
        	StringBuilder err = new StringBuilder();
        	BadRessourceHelper.extractMessage(e, err);
        	log.error(err.toString());
        }
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
	
	private QTIDocument loadDocument(String filename) {
		try(InputStream in = QTI12To21ConverterTest.class.getResourceAsStream(filename)) {
			XMLParser xmlParser = new XMLParser(new IMSEntityResolver());
			Document doc = xmlParser.parse(in, true);
			ParserManager parser = new ParserManager();
			return (QTIDocument)parser.parse(doc);
		} catch (Exception e) {			
			log.error("Exception when parsing input QTI input stream for " + filename, e);
			return null;
		}
	}
}
