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
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.junit.Test;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.provision.BadResourceException;
import uk.ac.ed.ph.jqtiplus.reading.AssessmentObjectXmlLoader;
import uk.ac.ed.ph.jqtiplus.reading.QtiModelBuildingError;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlInterpretationException;
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
public class OnyxToQtiWorksTest {

	@Test
	public void fixItem() {
		File itemFile = new File("/Users/srosse/Desktop/QTI/Beispielaufgaben_Onyx_3_6/Auswahlaufgabe_1509468352.xml");
		File rubricFile = new File("/Users/srosse/Desktop/QTI/fxtest_Onyx_3_1_1/Fxtest_684549665.xml");
		File manifestFile = new File("/Users/srosse/Desktop/QTI/Beispielaufgaben_Onyx_3_6/imsmanifest.xml");
		File promptFile = new File("/Users/srosse/Desktop/QTI/fxtest_Onyx_3_1_1/Task_1597435347.xml");

		File xmlFile = promptFile;
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		File outFile = new File("/Users/srosse/Desktop/QTI/text.xml");
		if(outFile.exists()) {
			outFile.delete();
			outFile = new File("/Users/srosse/Desktop/QTI/text.xml");
		}
		
		try (OutputStream byteOut = new FileOutputStream(outFile);
				OutputStreamWriter out = new OutputStreamWriter(byteOut, "UTF8")) {
			// Parse the input
			XMLOutputFactory xof = XMLOutputFactory.newInstance();
	        XMLStreamWriter xtw = xof.createXMLStreamWriter(out);

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
	        System.out.println("Has errors: " + (itemResult.getModelValidationErrors().size() > 0));

	        BadResourceException e = itemResult.getResolvedAssessmentItem().getItemLookup().getBadResourceException();
	        if(e instanceof QtiXmlInterpretationException) {
	        	QtiXmlInterpretationException qe = (QtiXmlInterpretationException)e;
	        	for(QtiModelBuildingError error :qe.getQtiModelBuildingErrors()) {
	        		String localName = error.getElementLocalName();
	        		String msg = error.getException().getMessage();
	        		int lineNumber = error.getElementLocation().getLineNumber();
	        		System.out.println(lineNumber + " :: " + localName + " :: " + msg);
	        	}
	        }
	        //System.out.println("Actual validation result: " + ObjectDumper.dumpObject(itemResult, DumpMode.DEEP));
			
			
		} catch (Throwable t) {
			t.printStackTrace();
		}

	}
}
