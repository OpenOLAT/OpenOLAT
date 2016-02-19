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
import java.io.InputStream;
import java.net.URISyntaxException;

import org.dom4j.Document;
import org.junit.Test;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.xml.XMLParser;
import org.olat.ims.qti.editor.beecom.objects.QTIDocument;
import org.olat.ims.qti.editor.beecom.parser.ParserManager;
import org.olat.ims.resources.IMSEntityResolver;

/**
 * 
 * Initial date: 19.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI12To21ConverterTest {
	
	private static final OLog log = Tracing.createLoggerFor(QTI12To21ConverterTest.class);
	
	@Test
	public void convert() throws URISyntaxException {
		QTIDocument doc = loadDocument("qti12_4questiontypes.xml");
		File exportDir = new File("/HotCoffee/QTI/today/");
		exportDir.mkdirs();
		QTI12To21Converter converter = new QTI12To21Converter(exportDir);
		
		converter.convert(doc);
		
		
		
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
