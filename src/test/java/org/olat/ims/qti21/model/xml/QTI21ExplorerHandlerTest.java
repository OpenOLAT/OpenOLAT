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
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * 
 * Test if our explorer can guess the right editor and version of some packages.
 * 
 * Initial date: 2 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Parameterized.class)
public class QTI21ExplorerHandlerTest {
	
	@Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "resources/onyx/Auswahlaufgabe_1509468352.xml", "Onyx Editor", "3.6" },
                { "resources/onyx/Hotspotaufgabe_478898401.xml", "Onyx Editor", "3.6" },
                { "resources/onyx/Task_1597435347.xml", "Onyx Editor", "3.1.1" },
                { "resources/onyx/text-entry-b-3-8.xml", "Onyx Editor", "3.8.3" },
                { "resources/onyx/extended-text-3-7.xml", "Onyx Editor", "3.7.2" },
                { "resources/onyx/extended-text-b-3-7.xml", "Onyx Editor", "3.7.2" },
                { "resources/onyx/extended-text-c-3-7.xml", "Onyx Editor", "3.7.2" },
                { "resources/onyx/extended-text-e-3-7.xml", "Onyx Editor", "3.7.2" },
                { "resources/onyx/paragraphs-rec.xml", "Onyx Editor", "3.8.1" },
                { "resources/onyx/imsmanifest-5-1.xml", "ONYX Editor", "5.10.3" },
                { "resources/onyx/imsmanifest-test-5-11.xml", "ONYX Editor", "5.11.1a" },
                { "resources/openolat/essay3c2454b4c4dbd64347ea9df54cd.xml", "OpenOLAT", "11.3a" },
                { "resources/openolat/multiple-choice-score-all-11-2-3.xml", "OpenOLAT", "11.2.2" } 
        });
    }
    
    private String xmlFilename;
    private String editor;
    private String editorVersion;
    
    public QTI21ExplorerHandlerTest(String xmlFilename, String editor, String editorVersion) {
    	this.editor = editor;
    	this.editorVersion = editorVersion;
    	this.xmlFilename = xmlFilename;
    }
    
    @Test
    public void exploreAndGetInfos() throws Exception {
    	URL xmlUrl = QTI21ExplorerHandlerTest.class.getResource(xmlFilename);
		File xmlFile = new File(xmlUrl.toURI());
		try(InputStream in = Files.newInputStream(xmlFile.toPath())) {
			SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
			QTI21ExplorerHandler myHandler = new QTI21ExplorerHandler();
			saxParser.setProperty("http://xml.org/sax/properties/lexical-handler", myHandler);
			saxParser.parse(in, myHandler);
			
			QTI21Infos infos = myHandler.getInfos();
			Assert.assertNotNull(infos);
			Assert.assertEquals(editor, infos.getEditor());
			Assert.assertEquals(editorVersion, infos.getVersion());	
		} catch(Exception e1) {
			throw e1;
		}
    }
}
