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
package org.olat.core.commons.services.webdav;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This is not a real Unit Test. I use it
 * to debug some issues with a Java client
 * on external servers.
 * 
 * 
 * Initial date: 06.05.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class WebDAVExternalTest {
	
	@Test
	public void test() 
	throws IOException, URISyntaxException {
		
		WebDAVConnection conn = new WebDAVConnection("http", "kivik.frentix.com", 443);
		conn.setCredentials("kanu", "kanu01");
		
		URI publicUri = conn.getBaseURI().path("webdav").path("coursefolders").path("Info").path("TP_learningmap").build();
		String response = conn.propfind(publicUri, 1);
		
		URI mapUri = conn.getBaseURI().path("webdav").path("coursefolders").path("Info").path("TP_learningmap").path("mapstyles.css").build();
		HttpGet getMap = conn.createGet(mapUri);
		
		HttpResponse mapResonse = conn.execute(getMap);
		String map = EntityUtils.toString(mapResonse.getEntity());
		System.out.println(map);

		IOUtils.closeQuietly(conn);
		parse(response);
	}

	private void parse(String response) {
		try {
			SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
			InputSource in = new InputSource(new StringReader(response));
			saxParser.parse(in, new WebDAVHandler());
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public static class WebDAVHandler extends DefaultHandler {
		private StringBuilder sb;

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
		throws SAXException {
			if("D:href".equals(qName)) {
				sb = new StringBuilder();
			}
		}

		@Override
		public void characters(char[] ch, int start, int length)
		throws SAXException {
			if(sb != null) {
				sb.append(ch, start, length);
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName)
		throws SAXException {
			if(sb != null) {
				System.out.println(sb.toString());
				sb = null;
			}
		}
	}
}