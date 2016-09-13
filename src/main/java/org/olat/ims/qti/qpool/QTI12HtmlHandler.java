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
package org.olat.ims.qti.qpool;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.util.StringHelper;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 * Initial date: 09.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI12HtmlHandler extends DefaultHandler {

	private static final String OPENOLAT_MOVIE_MARKER = "BPlayer.insertPlayer(";
	private final List<String> materialPath;
	
	private StringBuilder scriptBuffer;
	
	public QTI12HtmlHandler() {
		this.materialPath = new ArrayList<>();
	}
	
	public QTI12HtmlHandler(List<String> materialPath) {
		this.materialPath = materialPath;
	}
	
	public List<String> getMaterialPath() {
		return materialPath;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		String elem = localName.toLowerCase();
		if("img".equals(elem)) {
			String imgSrc = attributes.getValue("src");
			if(StringHelper.containsNonWhitespace(imgSrc)) {
				materialPath.add(imgSrc);
			}
		} else if("script".equals(elem)) {
			scriptBuffer = new StringBuilder();
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
	throws SAXException {
		if(scriptBuffer != null) {
			scriptBuffer.append(ch, start, length);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
	throws SAXException {
		String elem = localName.toLowerCase();
		if("script".equals(elem)) {
			String content = scriptBuffer == null ? "" : scriptBuffer.toString();
			processScriptContent(content);
			scriptBuffer = null;
		}
	}
	
	private void processScriptContent(String content) {
		int markerIndex = content.indexOf(OPENOLAT_MOVIE_MARKER);
		if(markerIndex >= 0) {
			int beginIndex = markerIndex + OPENOLAT_MOVIE_MARKER.length();
			char quote = content.charAt(beginIndex);
			int endIndex = content.indexOf(quote, beginIndex + 1);
			if(endIndex > beginIndex) {
				String media = content.substring(beginIndex + 1, endIndex);
				if(StringHelper.containsNonWhitespace(media)) {
					materialPath.add(media.trim());
				}
			}
		}
	}
}