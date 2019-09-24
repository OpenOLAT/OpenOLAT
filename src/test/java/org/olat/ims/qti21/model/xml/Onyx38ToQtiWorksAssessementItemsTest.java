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
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.provision.BadResourceException;
import uk.ac.ed.ph.jqtiplus.reading.AssessmentObjectXmlLoader;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlReader;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.validation.ItemValidationResult;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * Only to play with
 *  
 *  
 * Initial date: 25.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Parameterized.class)
public class Onyx38ToQtiWorksAssessementItemsTest {
	
	private static final Logger log = Tracing.createLoggerFor(Onyx38ToQtiWorksAssessementItemsTest.class);
	
	@Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "Auswahlaufgabe_1509468352.xml" },
                { "Hotspotaufgabe_478898401.xml" },
                { "Task_1597435347.xml" },
                { "text-entry-b-3-8.xml" },
                { "extended-text-e-3-7.xml" },
                { "extended-text-b-3-7.xml" },
                { "extended-text-c-3-7.xml" },
                { "extended-text-3-7.xml" },
                { "text-entry-3-8.xml" },
                { "extended-text-d-3-7.xml" },
                { "paragraphs-rec.xml" }
        });
    }
    
    private String xmlFilename;
    
    public Onyx38ToQtiWorksAssessementItemsTest(String xmlFilename) {
    	this.xmlFilename = xmlFilename;
    }

	@Test
	public void fixItem()
	throws IOException, XMLStreamException, SAXException, ParserConfigurationException, URISyntaxException {	
		URL xmlUrl = Onyx38ToQtiWorksAssessementItemsTest.class.getResource("resources/onyx/" + xmlFilename);
		File xmlFile = new File(xmlUrl.toURI());
		File tmpDir = new File(WebappHelper.getTmpDir(), "onyx" + UUID.randomUUID());
		tmpDir.mkdirs();
		
		File outputFile = new File(tmpDir, "text.xml");
		try(InputStream in = Files.newInputStream(xmlFile.toPath());
				Writer out = Files.newBufferedWriter(outputFile.toPath(), Charset.forName("UTF-8"))) {
			XMLOutputFactory xof = XMLOutputFactory.newInstance();
	        XMLStreamWriter xtw = xof.createXMLStreamWriter(out);

			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			SAXParser saxParser = factory.newSAXParser();
			DefaultHandler2 myHandler = new Onyx38ToQtiWorksHandler(xtw);
			saxParser.setProperty("http://xml.org/sax/properties/lexical-handler", myHandler);
			saxParser.parse(in, myHandler);
		} catch(Exception e1) {
			log.error("", e1);
			throw e1;
		}

		QtiXmlReader qtiXmlReader = new QtiXmlReader(new JqtiExtensionManager());
		ResourceLocator fileResourceLocator = new PathResourceLocator(outputFile.toPath());
        AssessmentObjectXmlLoader assessmentObjectXmlLoader = new AssessmentObjectXmlLoader(qtiXmlReader, fileResourceLocator);
        ResolvedAssessmentItem resolvedAssessmentItem = assessmentObjectXmlLoader.loadAndResolveAssessmentItem(outputFile.toURI());
        Assert.assertNotNull(resolvedAssessmentItem);
        AssessmentItem assessmentItem = resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful();
        
        
        // validation is only 
        ItemValidationResult itemResult = assessmentObjectXmlLoader.loadResolveAndValidateItem(outputFile.toURI());
        BadResourceException e = itemResult.getResolvedAssessmentItem().getItemLookup().getBadResourceException();
		if(e != null) {
			StringBuilder err = new StringBuilder();
			BadRessourceHelper.extractMessage(e, err);
			log.error(err.toString());
		}
        FileUtils.deleteDirsAndFiles(tmpDir.toPath());
        
        Assert.assertNotNull(assessmentItem);
        Assert.assertFalse(BadRessourceHelper.hasFatalErrors(e));
	}
}
