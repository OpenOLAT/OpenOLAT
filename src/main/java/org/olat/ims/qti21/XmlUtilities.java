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
package org.olat.ims.qti21;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

/**
 * 
 * Initial date: 10.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public final class XmlUtilities {
	
	private static final Logger log = Tracing.createLoggerFor(XmlUtilities.class);

	private static final SAXParserFactory parserFactory = SAXParserFactory.newInstance();
	static {
		try {
			parserFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			parserFactory.setNamespaceAware(true);
			parserFactory.setValidating(false);
		} catch (SAXNotRecognizedException | SAXNotSupportedException | ParserConfigurationException e) {
			log.error("", e);
		}
	}

	/**
	 * @return XMLReader namespace aware but not validating.
	 */
    public static final XMLReader createNsAwareSaxReader() {
        try {
            SAXParser parser = parserFactory.newSAXParser();
            parser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // Compliant
            parser.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""); // compliant
            return parser.getXMLReader();
        } catch (final Exception e) {
            throw new RuntimeException("Could not create NS-aware SAXParser with validating=false. Check deployment/runtime ClassPath", e);
        }
    }
}
