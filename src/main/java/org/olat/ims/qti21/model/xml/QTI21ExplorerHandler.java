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

import java.util.Scanner;

import org.olat.core.util.StringHelper;
import org.olat.ims.qti21.model.xml.QTI21Infos.InputType;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

/**
 * The handler search the version and the editor in a comment
 * at the beginning of the file, in the VCARD of imsmanifest,
 * it will react to some non-standard features like mapTolResponse or
 * some HTML code erros like <p> in <p>.
 * 
 * 
 * Initial date: 1 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21ExplorerHandler extends DefaultHandler2 {
	
	private static final String VERSION_MARKER = "Version ";
	private static final String PRODID_MARKER = "PRODID:";
	
	private StringBuilder collector;
	private int pLevel = -1;
	private final QTI21Infos infos = new QTI21Infos();
	
	public QTI21Infos getInfos() {
		return infos;
	}

	@Override
	public void comment(char[] ch, int start, int length)
	throws SAXException {
		String comment = new String(ch, start, length);
		if(comment.contains("Onyx Editor")) {
			infos.setEditor("Onyx Editor");
			int versionIndex = comment.indexOf(VERSION_MARKER);
			if(versionIndex > 0) {
				int offset = VERSION_MARKER.length();
				infos.setVersion(comment.substring(versionIndex + offset, comment.indexOf(' ', versionIndex + offset)));
			}
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if("assessmentItem".equals(qName)) {
			infos.setType(InputType.assessmentItem);
			toolAttributes(attributes);
		} else if("assessmentTest".equals(qName)) {
			infos.setType(InputType.assessmentTest);
			toolAttributes(attributes);
		} else if("toolName".equals(qName)) {
			collector = new StringBuilder();
		} else if("toolVersion".equals(qName)) {
			collector = new StringBuilder();
		} else if("entity".equals(qName)) {
			collector = new StringBuilder();
		} else if("mapTolResponse".equals(qName)) {
			if(!StringHelper.containsNonWhitespace(infos.getEditor())) {
				infos.setEditor("Onyx Editor");
				infos.setVersion("3.8.1");
			}
		} else if("p".equals(qName)) {
			pLevel++;
			if(pLevel == 1 && !StringHelper.containsNonWhitespace(infos.getEditor())) {
				infos.setEditor("Onyx Editor");
				infos.setVersion("3.8.1");
			}
		}
	}
	
	private void toolAttributes(Attributes attributes) {
		if(attributes.getIndex("toolVersion") >= 0) {
			infos.setVersion(attributes.getValue("toolVersion"));
		}
		if(attributes.getIndex("toolName") >= 0) {
			infos.setEditor(attributes.getValue("toolName"));
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if(collector != null) {
			collector.append(ch, start, length);
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if("toolName".equals(qName)) {
			infos.setEditor(collector.toString().trim());
			collector = null;
		} else if("toolVersion".equals(qName)) {
			infos.setVersion(collector.toString().trim());
			collector = null;
		} else if("entity".equals(qName)) {
			String entity = collector.toString();
			if(infos.getEditor() == null && StringHelper.containsNonWhitespace(entity)) {
				final Scanner s = new Scanner(entity);
				while(s.hasNextLine()) {
				    final String line = s.nextLine();
				    if(line.startsWith(PRODID_MARKER)) {
				    		int index = line.indexOf(':') + 1;
				    		int nextIndex = line.lastIndexOf(' ');
				    		if(index > 0 && nextIndex > 0) {
				    			String editor = line.substring(index, nextIndex);
				    			String version = line.substring(nextIndex + 1);
				    			infos.setEditor(editor);
				    			infos.setVersion(version);
				    		}
				    }
				}
				s.close();
			}
			
			collector = null;
		} else if("p".equals(qName)) {
			pLevel--;
		}
	}
}