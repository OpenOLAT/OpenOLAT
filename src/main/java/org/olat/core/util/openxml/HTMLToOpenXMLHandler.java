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

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import org.olat.core.util.StringHelper;
import org.olat.core.util.openxml.OpenXMLDocument.Border;
import org.olat.core.util.openxml.OpenXMLDocument.Columns;
import org.olat.core.util.openxml.OpenXMLDocument.Indent;
import org.olat.core.util.openxml.OpenXMLDocument.ListParagraph;
import org.olat.core.util.openxml.OpenXMLDocument.PredefinedStyle;
import org.olat.core.util.openxml.OpenXMLDocument.Spacing;
import org.olat.core.util.openxml.OpenXMLDocument.Style;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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
	
	private static final Border QUOTE_BORDER = new Border(400, 24, "EEEEEE");

	private boolean latex = false;
	private StringBuilder textBuffer;
	private Spacing startSpacing;
	protected String relPath;
	
	private boolean appendToCursor = true;
	protected final OpenXMLDocument factory;
	
	protected List<Node> content = new ArrayList<>();
	protected Deque<StyleStatus> styleStack = new ArrayDeque<>();
	
	protected Deque<Table> tableStack = new ArrayDeque<>();
	protected Element currentParagraph;
	protected ListParagraph currentListParagraph;
	protected boolean pNeedNewParagraph = true;
	
	protected double maxWidthCm = OpenXMLConstants.PAGE_FULL_WIDTH_CM;
	
	public HTMLToOpenXMLHandler(OpenXMLDocument document) {
		this.factory = document;
	}

	/**
	 * @param document The OpenXML document
	 * @param paragraph The current paragraph
	 * @param appendToCursor If true, append automatically to the document
	 */
	public HTMLToOpenXMLHandler(OpenXMLDocument document, String relPath, Element paragraph, boolean appendToCursor) {
		this(document);
		this.currentParagraph = paragraph;
		this.appendToCursor = appendToCursor;
		this.relPath = relPath;
	}
	
	public HTMLToOpenXMLHandler(OpenXMLDocument document, Spacing spacing) {
		this(document);
		this.startSpacing = spacing;
	}
	
	public void setMaxWidthCm(double width) {
		this.maxWidthCm = width;
	}
	
	public void setInitialParagraph(Element paragraph) {
		this.currentParagraph = paragraph;
	}
	
	public List<Node> getContent() {
		return content;
	}
	
	/**
	 * Flush the text if a new paragraph is created. Trailing text is flushed
	 * in the previous paragraph.
	 * @param create
	 * @return
	 */
	protected Element getCurrentParagraph(boolean create) {
		if(create || currentParagraph == null) {
			//flush the text
			if(textBuffer != null) {
				flushText();
				addContent(currentParagraph);
			}
			currentParagraph = createParagraphWithCurrentStyling(startSpacing);
			startSpacing = null;//consumed
		}
		return currentParagraph;
	}
	
	protected Element appendParagraph(Spacing spacing) {
		//flush the text
		if(textBuffer != null) {
			flushText();
			addContent(currentParagraph);
		}
		currentParagraph = createParagraphWithCurrentStyling(spacing);
		return currentParagraph;
	}
	
	private Element createParagraphWithCurrentStyling(Spacing spacing) {
		Indent indent = getCurrentIndent();
		Border leftBorder = getCurrentLeftBorder();
		PredefinedStyle predefinedStyle = getCurrentPredefinedStyle();
		return factory.createParagraphEl(indent, leftBorder, spacing, predefinedStyle);
	}
	
	protected Element getCurrentListParagraph(boolean create) {
		if(create || currentParagraph == null) {
			//flush the text
			if(textBuffer != null) {
				flushText();
				addContent(currentParagraph);
			}
			if(currentListParagraph == null) {
				//nested list
				currentListParagraph = factory.createListParagraph();
			}
			currentParagraph = factory.createListParagraph(currentListParagraph);
		}
		return currentParagraph;
	}
	
	protected void closeParagraph() {
		flushText();
		currentParagraph = addContent(currentParagraph);
		textBuffer = null;
		latex = false;
	}
	
	protected Element addContent(Node element) {
		if(element == null) return null;
		
		Table currentTable = getCurrentTable();
		if(currentTable != null) {
			currentTable.getCurrentCell().appendChild(element);
		} else {
			content.add(element);
		}
		return null;
	}
	
	protected void trimTextBuffer() {
		if(textBuffer == null) return;
		
		String text = textBuffer.toString().trim();
		if(text.length() == 0) {
			textBuffer = null;
		} else {
			textBuffer = new StringBuilder(text);
		}
	}
	
	protected boolean hasTextToFlush() {
		return textBuffer != null && textBuffer.length() > 0;
	}
	
	protected void flushText() {
		if(textBuffer == null) return;
		
		if(latex) {
			flushLaTeX();
		} else {
			Element currentRun = getCurrentRun();
			String text = textBuffer.toString().replace("\n", "").replace("\r", "");
			if(text.length() > 0 && Character.isSpaceChar(text.charAt(0))) {
				currentRun.appendChild(factory.createPreserveSpaceEl());
			}
			currentRun.appendChild(factory.createTextEl(text));
			if(text.length() > 1 && Character.isSpaceChar(text.charAt(text.length() - 1))) {
				currentRun.appendChild(factory.createPreserveSpaceEl());
			}
		}
		latex = false;
		textBuffer = null;
	}
	
	private void flushLaTeX() {
		Element paragraphEl;
		if(currentParagraph == null) {
			paragraphEl = currentParagraph = createParagraphWithCurrentStyling(startSpacing);
			startSpacing = null;
		} else {
			paragraphEl = currentParagraph;
		}
		
		List<Node> nodes = factory.convertLaTeX(textBuffer.toString(), false);
		for(Node node:nodes) {
			paragraphEl.appendChild(node);
		}
	}
	
	/**
	 * Get or create a run on the current paragraph
	 * @return
	 */
	protected Element getCurrentRun() {
		Element paragraphEl;
		if(currentParagraph == null) {
			paragraphEl = currentParagraph = createParagraphWithCurrentStyling(startSpacing);
			startSpacing = null;
		} else {
			paragraphEl = currentParagraph;
		}
		Node lastChild = paragraphEl.getLastChild();
		if(lastChild != null && "w:r".equals(lastChild.getNodeName())) {
			return (Element)lastChild;
		}

		PredefinedStyle runStyle = getCurrentPredefinedStyle();
		return (Element)paragraphEl.appendChild(factory.createRunEl(null, runStyle));
	}

	protected Style[] setTextPreferences(String cssStyles) {
		if(cssStyles == null) {
			return setTextPreferences();
		} else {
			List<Style> styles = new ArrayList<>(4);
			if(cssStyles.contains("bold")) styles.add(Style.bold);
			if(cssStyles.contains("italic")) styles.add(Style.italic);
			if(cssStyles.contains("underline")) styles.add(Style.underline);
			if(cssStyles.contains("line-through")) styles.add(Style.strike);
			return setTextPreferences(styles.toArray(new Style[styles.size()]));
		}
	}
	
	/**
	 * Create a new run with preferences
	 */
	protected Style[] setTextPreferences(Style... styles) {
		Node runPrefs = getRunForTextPreferences();
		factory.createRunPrefsEl(runPrefs, styles);
		return styles;
	}
	
	protected Style[] unsetTextPreferences(Style... styles) {
		Node runPrefs = getRunForTextPreferences();
		factory.createRunReversePrefsEl(runPrefs, styles);
		return styles;
	}
	
	protected Node getRunForTextPreferences() {
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
			PredefinedStyle style = getCurrentPredefinedStyle();
			run = paragraphEl.appendChild(factory.createRunEl(null, style));
			runPrefs = run.appendChild(factory.createRunPrefsEl());
		}
	
		if(!"w:rPr".equals(runPrefs.getNodeName())){
			runPrefs = run.appendChild(factory.createRunPrefsEl());
		}
		return runPrefs;
	}
	
	public Style[] getCurrentStyle() {
		if(styleStack.isEmpty()) return null;
		return styleStack.getLast().getStyles();
	}
	
	public Indent getCurrentIndent() {
		if(styleStack.isEmpty()) return null;
		
		int indent = 0;
		for(StyleStatus style:styleStack) {
			if(style.isQuote()) {
				indent++;
			}
		}
		
		int emuIndent = 0;
		if(indent > 0) {
			emuIndent = 700;
		}
		if(indent > 1) {
			emuIndent += (indent - 1) * 100;
		}
		return emuIndent == 0 ? null : new Indent(emuIndent);
	}
	
	public Border getCurrentLeftBorder() {
		if(styleStack.isEmpty()) return null;
		
		int indent = 0;
		for(StyleStatus style:styleStack) {
			if(style.isQuote()) {
				indent++;
			}
		}
		
		String val;
		switch(indent) {
			case 1: val = "single"; break;
			case 2: val = "double"; break;
			default: val = "triple";
		}
		return indent == 0 ? null : new Border(QUOTE_BORDER, val);
	}
	
	public PredefinedStyle getCurrentPredefinedStyle() {
		if(styleStack.isEmpty()) return null;
		
		boolean quote = false;
		for(StyleStatus style:styleStack) {
			quote |= style.isQuote();
		}
		return quote ? PredefinedStyle.quote : null;
	}
	
	public Style[] popStyle(String tag) {
		StyleStatus status = styleStack.pollLast();
		if(status != null && status.getTag().equals(tag)) {
			return status.getStyles();
		}
		return null;
	}
	
	protected void setImage(String path) {
		Element imgEl = factory.createImageEl(path, maxWidthCm);
		if(imgEl != null) {
			PredefinedStyle style = getCurrentPredefinedStyle();
			Element runEl = factory.createRunEl(Collections.singletonList(imgEl), style);
			Element paragrapheEl = getCurrentParagraph(false);
			paragrapheEl.appendChild(runEl);
		}
	}
	
	protected void setImage(File file) {
		Element imgEl = factory.createImageEl(file, maxWidthCm);
		if(imgEl != null) {
			PredefinedStyle style = getCurrentPredefinedStyle();
			Element runEl = factory.createRunEl(Collections.singletonList(imgEl), style);
			Element paragrapheEl = getCurrentParagraph(false);
			paragrapheEl.appendChild(runEl);
		}
	}
	
	protected void startGraphic(File backgroundImage, List<OpenXMLGraphic> elements) {
		Element paragrapheEl = getCurrentParagraph(true);
		Element graphicEl = factory.createGraphicEl(backgroundImage, elements);
		Element runEl = factory.createRunEl();
		runEl.appendChild(graphicEl);
		paragrapheEl.appendChild(runEl);
		closeParagraph();
	}
	
	public Table getCurrentTable() {
		if(tableStack.isEmpty()) {
			return null;
		}
		return tableStack.getLast();
	}
	
	public Table startTable() {
		closeParagraph();
		tableStack.add(new Table(OpenXMLConstants.TABLE_FULL_FULL_WIDTH_PCT));
		return tableStack.getLast();
	}
	
	public Table startTable(Columns columns) {
		closeParagraph();
		tableStack.add(new Table(OpenXMLConstants.TABLE_FULL_FULL_WIDTH_PCT, columns));
		return tableStack.getLast();
	}
	
	public void startCurrentTableRow() {
		startCurrentTableRow(false);
	}
	
	public void startCurrentTableRow(boolean cantSplit) {
		getCurrentTable().addRowEl(cantSplit);
	}
	
	public Node addCell(int colSpan, int rowSpan) {
		return getCurrentTable().addCellEl(colSpan, rowSpan);
	}
	
	public Node addCell(Element cellEl) {
		return getCurrentTable().addCellEl(cellEl, 1);
	}
	
	public void closeCurrentTableRow() {
		Table currentTable = getCurrentTable();
		if(currentTable != null) {
			currentTable.closeRow();
		}
		textBuffer = null;
		latex = false;
		currentParagraph = null;
	}
	
	public void endTable() {
		if(!tableStack.isEmpty()) {
			Table currentTable = tableStack.removeLast();
			currentTable.closeTable();
			addContent(currentTable.getTableEl());
			if(!tableStack.isEmpty()) {
				Element emptyParagraph = factory.createParagraphEl();
				addContent(emptyParagraph);
			}
		}
		currentParagraph = null;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		String tag = localName.toLowerCase();
		if("p".equals(tag)) {
			getCurrentParagraph(pNeedNewParagraph);
		} else if("span".equalsIgnoreCase(tag)) {
			flushText();

			Style[] styles = null;
			String cl = attributes.getValue("class");
			if("math".equals(cl)) {
				latex = true;
			} else {
				String cssStyles = attributes.getValue("style");
				styles = setTextPreferences(cssStyles);
			}
			styleStack.add(new StyleStatus(tag, styles));
		} else if("br".equals(tag)) {
			closeParagraph();
		} else if("em".equals(tag)) {
			flushText();
			Style[] styles = setTextPreferences(Style.italic);
			styleStack.add(new StyleStatus(tag, styles));
		} else if("strong".equals(tag)) {
			flushText();
			Style[] styles = setTextPreferences(Style.bold);
			styleStack.add(new StyleStatus(tag, styles));
		} else if("img".equals(tag)) {
			String path = path(attributes.getValue("src"));
			setImage(path);
		} else if("table".equals(tag)) {
			startTable();
		} else if("tr".equals(tag)) {
			startCurrentTableRow(false);
		} else if("td".equals(tag) || "th".equals(tag)) {
			int colspan = OpenXMLUtils.getSpanAttribute("colspan", attributes);
			int rowspan = OpenXMLUtils.getSpanAttribute("rowspan", attributes);
			getCurrentTable().addCellEl(colspan, rowspan);
		} else if("ul".equals(tag) || "ol".equals(tag)) {
			currentListParagraph = factory.createListParagraph();
		} else if("li".equals(tag)) {
			getCurrentListParagraph(true);
		} else if("blockquote".equals(tag)) {
			Style[] styles = setTextPreferences(Style.italic);
			styleStack.add(new StyleStatus(tag, true, styles));
			appendParagraph(new Spacing(90, 0));
			pNeedNewParagraph = false;
		} else if("div".equals(tag)) {
			String cl = attributes.getValue("class");
			if(StringHelper.containsNonWhitespace(cl)) {
				if(cl.contains("o_quote_author")) {
					Style[] styles = setTextPreferences(Style.italic);
					styleStack.add(new StyleStatus(tag, true, styles));
					appendParagraph(new Spacing(120, 0));
					pNeedNewParagraph = false;
				} else {
					getCurrentParagraph(pNeedNewParagraph);
					styleStack.add(new StyleStatus(tag, new Style[0]));
				}
			} else {
				getCurrentParagraph(pNeedNewParagraph);
				styleStack.add(new StyleStatus(tag, new Style[0]));
			}
		}
	}
	
	public String path(String path) {
		if(relPath != null) {
			return relPath.concat(path);
		}
		return path;
	}
	
	public void appendText(String text) {
		if(textBuffer == null) {
			textBuffer = new StringBuilder();
		}
		textBuffer.append(text);
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
		} else if("span".equals(tag) ) {
			flushText();
			Style[] currentStyles = popStyle(tag);
			unsetTextPreferences(currentStyles);
		} else if("em".equalsIgnoreCase(tag)) {
			flushText();
			unsetTextPreferences(Style.italic);
			popStyle(tag);
		} else if("strong".equalsIgnoreCase(tag)) {
			flushText();
			unsetTextPreferences(Style.bold);
			popStyle(tag);
		}  else if("table".equals(tag)) {
			endTable();
		} else if("td".equals(tag) || "th".equals(tag)) {
			flushText();
			currentParagraph = addContent(currentParagraph);
		} else if("tr".equals(tag)) {
			closeCurrentTableRow();
		} else if("ul".equals(tag) || "ol".equals(tag)) {
			closeParagraph();
			currentListParagraph = null;
		} else if("li".equals(tag)) {
			closeParagraph();// close the paragraph but let the list
		} else if("blockquote".equals(tag)) {
			popStyle(tag);
		} else if("div".equals(tag)) {
			popStyle(tag);
			closeParagraph();
		}
	}
	
	@Override
	public void endDocument() throws SAXException {
		//clean up trailing text and pack it in a last paragraph
		closeParagraph();

		if(appendToCursor) {
			for(Node node:content) {
				factory.getCursor().appendChild(node);
			}
		}
	}
	
	public static class StyleStatus {
		private final String tag;
		private final Style[] styles;
		private final boolean quote;
		
		public StyleStatus(String tag, Style[] styles) {
			this(tag, false, styles);
		}
		
		public StyleStatus(String tag, boolean quote, Style[] styles) {
			this.tag = tag;
			this.quote = quote;
			this.styles = styles;
		}
		
		public String getTag() {
			return tag;
		}
		
		public boolean isQuote() {
			return quote;
		}
		
		public Style[] getStyles() {
			return styles;
		}
	}
	
	public class Table {
		private final Element tableEl;
		
		private int nextCol;
		private Node currentRowEl;
		private Element currentCellEl;
		
		private Columns columns;
		private Span[] rowSpans = new Span[128];
		
		public Table(int tableWidth) {
			tableEl = factory.createTable(tableWidth);
		}
		
		public Table(int tableWidth, Columns columns) {
			this.columns = columns;
			tableEl = factory.createTable(tableWidth, columns);
		}

		public Element getTableEl() {
			return tableEl;
		}
		
		public Columns getColumns() {
			return columns;
		}
		
		public void closeTable() {
			if(columns == null && nextCol > 0) {
				int width = (int)(9212.0d / nextCol);
				columns = Columns.sameWidthColumns(nextCol, width);
				factory.appendTableGrid(tableEl, columns);
			}
		}
		
		public Node addRowEl() {
			return addRowEl(false);
		}
		
		/**
		 * Add a new row in the table.
		 * @param cantSplit true to prevent the cells to be splitted across 2 pages
		 * @return The row element
		 */
		public Node addRowEl(boolean cantSplit) {
			for(int i=rowSpans.length; i-->0; ) {
				if(rowSpans[i] != null) {
					rowSpans[i].unDone();
				}
			}
			
			nextCol = 0;
			currentRowEl = factory.createTableRow(cantSplit);
			return tableEl.appendChild(currentRowEl);
		}
		
		public void closeRow() {
			closeCell(rowSpans.length-1);
		}
		
		/*
<w:tc>
	<w:tcPr>
    <w:gridSpan w:val="2" />
    <w:vMerge w:val="restart" />
		 */
		public Node addCellEl(int colSpan, int rowSpan) {
			nextCol += closeCell(nextCol);
			
			currentCellEl = currentRowEl.getOwnerDocument().createElement("w:tc");
			
			Node prefs = currentCellEl.appendChild(currentCellEl.getOwnerDocument().createElement("w:tcPr"));
			if(colSpan > 1) {
				Element gridSpan = (Element)prefs.appendChild(prefs.getOwnerDocument().createElement("w:gridSpan"));
				gridSpan.setAttribute("w:val", Integer.toString(colSpan));
			} else {
				/*
				<w:tcPr>
					<w:tcW w:w="0" w:type="auto" />
				</w:tcPr>
				*/
				Element wCPrefs = (Element)prefs.appendChild(prefs.getOwnerDocument().createElement("w:tcW"));
				wCPrefs.setAttribute("w:w", "0");
				wCPrefs.setAttribute("w:type", "auto");
			}
			
			if(rowSpan > 1) {
				Element vMerge = (Element)prefs.appendChild(prefs.getOwnerDocument().createElement("w:vMerge"));
				vMerge.setAttribute("w:val", "restart");
			}
			
			if(colSpan == 1 && rowSpan == 1) {
				rowSpans[nextCol] = Span.OneOnOne;
			} else {
				rowSpans[nextCol] = new Span(colSpan, rowSpan);
			}

			nextCol += (colSpan <= 1 ? 1 : colSpan);
			return currentRowEl.appendChild(currentCellEl);
		}
		
		public Element addCellEl(Element cellEl, int colSpan) {
			nextCol += closeCell(nextCol);
			currentCellEl = cellEl;
			nextCol += (colSpan <= 1 ? 1 : colSpan);
			return (Element)currentRowEl.appendChild(currentCellEl);
		}
		
		public int closeCell(int lastIndex) {
			for(int i=lastIndex+1; i-->0; ) {
				Span span = rowSpans[i];
				if(span != null) {
					if(span.getRowSpan() > 1 && !span.isDone()) {
						currentCellEl = (Element)currentRowEl.appendChild(currentRowEl.getOwnerDocument().createElement("w:tc"));
						Node prefs = currentCellEl.appendChild(currentCellEl.getOwnerDocument().createElement("w:tcPr"));

						if(span.getColSpan() > 1) {
							Element gridSpan = (Element)prefs.appendChild(prefs.getOwnerDocument().createElement("w:gridSpan"));
							gridSpan.setAttribute("w:val", Integer.toString(span.getColSpan()));
						}
						prefs.appendChild(prefs.getOwnerDocument().createElement("w:vMerge"));
						
						currentCellEl.appendChild(currentCellEl.getOwnerDocument().createElement("w:p"));
						span.decrementRowSpan();
						return span.getColSpan();
					} else {
						break;
					}
				}
			}
			return 0;
		}

		public Element getCurrentCell() {
			return currentCellEl;
		}
	}

	private static class Span {

		public static final Span OneOnOne = new Span(1,1);
		
		private int colspan;
		private int rowspan;
		private boolean done = true;
		
		private Span(int colspan, int rowspan) {
			this.colspan = colspan;
			this.rowspan = rowspan;
		}

		public int getColSpan() {
			return colspan;
		}
		
		public int getRowSpan() {
			return rowspan;
		}
		
		public void decrementRowSpan() {
			rowspan--;
		}
		
		public boolean isDone() {
			return done;
		}
		
		public void unDone() {
			done = false;
		}
	}
}