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
package org.olat.core.util.openxml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Convert HTML code to OpenXML
 * 
 * 
 * Initial date: 05.09.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class HTMLToOpenXMLHandler extends DefaultHandler {

	private boolean latex = false;
	private StringBuilder textBuffer;
	
	private final OpenXMLDocument factory;
	
	private List<Node> content = new ArrayList<Node>();
	
	private Element currentParagraph;
	private Table currentTable;
	
	public HTMLToOpenXMLHandler(OpenXMLDocument document) {
		this.factory = document;
	}
	
	/**
	 * Flush the text if a new paragraph is created. Trailing text is flushed
	 * in the previous paragraph.
	 * @param create
	 * @return
	 */
	private Element getCurrentParagraph(boolean create) {
		if(create || currentParagraph == null) {
			//flush the text
			if(textBuffer != null) {
				flushText();
				addContent(currentParagraph);
			}
			currentParagraph = factory.createParagraphEl((Element)null);
		}
		return currentParagraph;
	}
	
	private void closeParagraph() {
		flushText();
		currentParagraph = addContent(currentParagraph);
		textBuffer = null;
		latex = false;
	}
	
	private Element addContent(Node element) {
		if(element == null) return null;
		
		if(currentTable != null) {
			currentTable.getCurrentCell().appendChild(element);
		} else {
			content.add(element);
		}
		return null;
	}
	
	private void flushText() {
		if(textBuffer == null) return;
		
		if(latex) {
			//begin a new paragraph
			if(currentParagraph != null) {
				currentParagraph = addContent(currentParagraph);
			}
			List<Node> nodes = factory.convertLaTeX(textBuffer.toString());
			for(Node node:nodes) {
				addContent(node);
			}
		} else {
			Element currentRun = getCurrentRun();
			String content = textBuffer.toString();
			if(content.length() > 0 && Character.isSpaceChar(content.charAt(0))) {
				currentRun.appendChild(factory.createPreserveSpaceEl());
			}
			currentRun.appendChild(factory.createTextEl(content));
			if(content.length() > 1 && Character.isSpaceChar(content.charAt(content.length() - 1))) {
				currentRun.appendChild(factory.createPreserveSpaceEl());
			}
		}
		latex = false;
		textBuffer = null;
	}
	
	/**
	 * Get or create a run on the current paragraph
	 * @return
	 */
	private Element getCurrentRun() {
		Element paragraphEl;
		if(currentParagraph == null) {
			paragraphEl = currentParagraph = factory.createParagraphEl((Element)null);
		} else {
			paragraphEl = currentParagraph;
		}
		Node lastChild = paragraphEl.getLastChild();
		if(lastChild != null && "w:r".equals(lastChild.getNodeName())) {
			return (Element)lastChild;
		}
		return (Element)paragraphEl.appendChild(factory.createRunEl(null));
	}

	private void setTextPreferences(String cssStyles) {
		if(cssStyles == null) {
			setTextPreferences(false, false, false, false);
		} else {
			boolean bold = cssStyles.contains("bold");
			boolean italic = cssStyles.contains("italic");
			boolean underline = cssStyles.contains("underline");
			boolean strike = cssStyles.contains("line-through");
			setTextPreferences(bold, italic, underline, strike);
		}
	}
	
	/**
	 * Create a new run with preferences
	 */
	private void setTextPreferences(boolean bold, boolean italic, boolean underline, boolean strike) {
		Element paragraphEl = getCurrentParagraph(false);
		
		Node runPrefs = null;
		Node run = paragraphEl.getLastChild();
		if(run != null && "w:r".equals(run.getNodeName())) {
			Node prefs = run.getLastChild();
			if("w:rPr".equals(prefs.getNodeName())){
				runPrefs = prefs;
			}
		}
		
		if(runPrefs == null) {
			run = paragraphEl.appendChild(factory.createRunEl(null));
			runPrefs = run.appendChild(factory.createRunPrefsEl());
		}
	
		if(!"w:rPr".equals(runPrefs.getNodeName())){
			runPrefs = run.appendChild(factory.createRunPrefsEl());
		}
		
		if(bold && !OpenXMLUtils.contains(runPrefs, "w:b")) {
			runPrefs.appendChild(runPrefs.getOwnerDocument().createElement("w:b"));
		}
		if(italic && !OpenXMLUtils.contains(runPrefs, "w:i")) {
			runPrefs.appendChild(runPrefs.getOwnerDocument().createElement("w:i"));
		}
		if(underline && !OpenXMLUtils.contains(runPrefs, "w:u")) {
			Element underlinePrefs = (Element)runPrefs.appendChild(runPrefs.getOwnerDocument().createElement("w:u"));
			underlinePrefs.setAttribute("val", "single");
		}
		if(strike && !OpenXMLUtils.contains(runPrefs, "w:strike")) {
			runPrefs.appendChild(runPrefs.getOwnerDocument().createElement("w:strike"));
		}
	}
	
	private void setImage(String path) {
		Element imgEl = factory.createImageEl(path);
		if(imgEl != null) {
			Element runEl = factory.createRunEl(Collections.singletonList(imgEl));
			Element paragrapheEl = getCurrentParagraph(false);
			paragrapheEl.appendChild(runEl);
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		String tag = localName.toLowerCase();
		if("p".equalsIgnoreCase(tag)) {
			getCurrentParagraph(true);
		} else if("span".equalsIgnoreCase(tag)) {
			flushText();

			String cl = attributes.getValue("class");
			if("math".equals(cl)) {
				latex = true;
			} else {
				String cssStyles = attributes.getValue("style");
				setTextPreferences(cssStyles);
			}
		} else if("em".equalsIgnoreCase(tag)) {
			flushText();
			setTextPreferences(false, true, false, false);
		} else if("strong".equalsIgnoreCase(tag)) {
			flushText();
			setTextPreferences(true, false, false, false);
		} else if("img".equals(tag)) {
			String path = attributes.getValue("src");
			setImage(path);
		} else if("table".equalsIgnoreCase(tag)) {
			closeParagraph();
			currentTable = new Table();
		} else if("tr".equals(tag)) {
			currentTable.addRowEl();	
		} else if("td".equals(tag) || "th".equals(tag)) {
			currentTable.addCellEl();
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		if(textBuffer == null) {
			textBuffer = new StringBuilder();
		}
		textBuffer.append(ch, start, length);
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) {
		String tag = localName.toLowerCase();
		if("p".equals(tag)) {
			closeParagraph();
		//flush text nodes to current paragraph
		} else if("span".equals(tag) || "em".equals(tag) || "strong".equals(tag)) {
			flushText();
		} else if("table".equals(tag)) {
			if(currentTable != null) {
				content.add(currentTable.getTableEl());
			}
		} else if("td".equals(tag) || "th".equals(tag)) {
			flushText();
			currentParagraph = addContent(currentParagraph);
		} else if("tr".equals(tag)) {
			textBuffer = null;
			latex = false;
			currentParagraph = null;
		}
	}
	
	@Override
	public void endDocument() throws SAXException {
		//clean up trailing text and pack it in a last paragraph
		closeParagraph();

		for(Node node:content) {
			factory.getCursor().appendChild(node);
		}
	}
	
	public class Table {
		private final Element tableEl;
		private final Element gridEl;
		private Node currentRowEl;
		private Element currentCellEl;
		
		public Table() {
			tableEl = factory.createTable();
			NodeList gridPrefs = tableEl.getElementsByTagName("w:tblGrid");
			gridEl = (Element)gridPrefs.item(0);
		}

		public Element getTableEl() {
			return tableEl;
		}

		public Element getGridEl() {
			return gridEl;
		}
		
		public Node addRowEl() {
			currentRowEl = tableEl.getOwnerDocument().createElement("w:tr");	
			return  tableEl.appendChild(currentRowEl);
		}
		
		public Node addCellEl() {
			currentCellEl = currentRowEl.getOwnerDocument().createElement("w:tc");
			return currentRowEl.appendChild(currentCellEl);
		}

		public Element getCurrentCell() {
			return currentCellEl;
		}

		public void setCurrentCell(Element currentCell) {
			this.currentCellEl = currentCell;
		}
	}
}