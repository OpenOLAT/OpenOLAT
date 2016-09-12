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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

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
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;
import org.xml.sax.SAXException;

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.provision.BadResourceException;
import uk.ac.ed.ph.jqtiplus.reading.AssessmentObjectXmlLoader;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlReader;
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
public class OnyxToQtiAssessementItemsTest {
	
	private static final OLog log = Tracing.createLoggerFor(OnyxToQtiAssessementItemsTest.class);
	
	@Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "Auswahlaufgabe_1509468352.xml" },
                { "Hotspotaufgabe_478898401.xml" },
                { "Task_1597435347.xml" }
        });
    }
    
    private String xmlFilename;
    
    public OnyxToQtiAssessementItemsTest(String xmlFilename) {
    	this.xmlFilename = xmlFilename;
    }

	@Test
	public void fixItem()
	throws IOException, XMLStreamException, SAXException, ParserConfigurationException, URISyntaxException {	
		URL xmlUrl = OnyxToQtiAssessementItemsTest.class.getResource(xmlFilename);
		File xmlFile = new File(xmlUrl.toURI());
		File tmpDir = new File(WebappHelper.getTmpDir(), "onyx" + UUID.randomUUID());
		tmpDir.mkdirs();

		File outFile = new File(tmpDir, "text.xml");
		OutputStream byteOut = new FileOutputStream(outFile);
		OutputStreamWriter out = new OutputStreamWriter(byteOut, "UTF8");
			// Parse the input
		XMLOutputFactory xof = XMLOutputFactory.newInstance();
		XMLStreamWriter xtw = xof.createXMLStreamWriter(out);

		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		OnyxToQtiWorksHandler myHandler = new OnyxToQtiWorksHandler(xtw);
		saxParser.setProperty("http://xml.org/sax/properties/lexical-handler", myHandler);
		saxParser.parse(xmlFile, myHandler);
		
		out.flush();
		byteOut.flush();

		QtiXmlReader qtiXmlReader = new QtiXmlReader(new JqtiExtensionManager());
		ResourceLocator fileResourceLocator = new PathResourceLocator(outFile.toPath());
        AssessmentObjectXmlLoader assessmentObjectXmlLoader = new AssessmentObjectXmlLoader(qtiXmlReader, fileResourceLocator);
        ItemValidationResult itemResult = assessmentObjectXmlLoader.loadResolveAndValidateItem(outFile.toURI());
       
        BadResourceException e = itemResult.getResolvedAssessmentItem().getItemLookup().getBadResourceException();
		if(e != null) {
			StringBuilder err = new StringBuilder();
			BadRessourceHelper.extractMessage(e, err);
			log.error(err.toString());
		}
        
        FileUtils.deleteDirsAndFiles(tmpDir.toPath());
        Assert.assertFalse(xmlFilename + " has errors", (itemResult.getModelValidationErrors().size() > 0));
	}
}
