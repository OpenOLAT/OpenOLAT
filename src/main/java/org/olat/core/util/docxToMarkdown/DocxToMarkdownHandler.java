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
package org.olat.core.util.docxToMarkdown;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.docxToMarkdown.DocxConversionMessage.Level;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX ContentHandler for {@code word/document.xml} — the core of the DOCX-to-Markdown
 * conversion pipeline.
 * <p>
 * The SAX parser used here is <em>not</em> namespace-aware; element and attribute
 * names are delivered with their OOXML prefixes (e.g. {@code w:p}, {@code w:val}).
 * Both prefixed and bare forms are handled for robustness.
 * <p>
 * Initial date: 2026-04-02<br>
 *
 * @author gnaegi, https://www.frentix.com, http://www.frentix.com
 */
class DocxToMarkdownHandler extends DefaultHandler {

	private static final Logger log = Tracing.createLoggerFor(DocxToMarkdownHandler.class);

	/** Maximum image size allowed for inline base64 embedding (10 MB). */
	private static final int MAX_IMAGE_BYTES = 10 * 1024 * 1024;

	// -----------------------------------------------------------------------
	// Dependencies
	// -----------------------------------------------------------------------

	private final Map<String, DocxRelTarget> relationships;
	private final Map<Integer, DocxNumberingDef> numbering;
	private final Map<String, String> styles;
	private final ZipFile zipFile;
	/** Directory to write extracted media files to (null = skip images). */
	private final File mediaDir;

	// -----------------------------------------------------------------------
	// Output
	// -----------------------------------------------------------------------

	/** Accumulates the final Markdown document. */
	private final StringBuilder markdown = new StringBuilder(4096);
	private final List<DocxConversionMessage> messages = new ArrayList<>();

	// -----------------------------------------------------------------------
	// Paragraph state
	// -----------------------------------------------------------------------

	private boolean inParagraph = false;
	private boolean inParagraphProperties = false;
	/**
	 * True once the paragraph prefix (heading hashes, list marker, blockquote {@code >})
	 * has been written to {@link #markdown} for the current paragraph.
	 */
	private boolean paragraphPrefixEmitted = false;
	private String currentStyleId = null;
	/** Whether the most recently completed top-level paragraph was a list item. */
	private boolean previousWasList = false;
	/** The numId of the most recently completed list item, for detecting list transitions. */
	private int previousListNumId = -1;

	// List context (set via w:pPr/w:numPr children)
	private int listNumId = -1;
	private int listLevel = 0;

	// -----------------------------------------------------------------------
	// Run state
	// -----------------------------------------------------------------------

	private boolean inRun = false;
	private boolean inRunProperties = false;
	/** Text accumulated within the current w:r run. */
	private final StringBuilder currentText = new StringBuilder();
	private boolean bold = false;
	private boolean italic = false;
	private boolean strikethrough = false;
	private boolean underline = false;
	private boolean superscript = false;
	private boolean subscript = false;
	private boolean highlight = false;

	// Pending formatted span: accumulates text across runs with identical formatting
	private final StringBuilder pendingText = new StringBuilder();
	private boolean pendingBold = false;
	private boolean pendingItalic = false;
	private boolean pendingStrikethrough = false;
	private boolean pendingUnderline = false;
	private boolean pendingSuperscript = false;
	private boolean pendingSubscript = false;
	private boolean pendingHighlight = false;
	private boolean hasPending = false;

	// -----------------------------------------------------------------------
	// Hyperlink state
	// -----------------------------------------------------------------------

	private boolean inHyperlink = false;
	private String hyperlinkUrl = null;
	/** Formatted text collected from all runs inside the current hyperlink. */
	private final StringBuilder hyperlinkText = new StringBuilder();

	// -----------------------------------------------------------------------
	// Table state
	// -----------------------------------------------------------------------

	/** Nesting depth: 0 = outside, 1 = top-level table, 2+ = nested. */
	private int tableNestingDepth = 0;
	private boolean inTableCell = false;
	/**
	 * Text accumulated for the current table cell (may span multiple w:p inside
	 * the cell — paragraphs are joined with a space).
	 */
	private final StringBuilder cellText = new StringBuilder();
	private boolean cellFirstParagraph = true;
	private boolean cellHasBold = false;
	private final List<String> currentRowCells = new ArrayList<>();
	private final List<Boolean> currentRowBold = new ArrayList<>();
	private boolean currentRowIsHeader = false;
	/** All rows collected for the current table: cells per row. */
	private final List<List<String>> tableRows = new ArrayList<>();
	/** Parallel to tableRows: whether each cell had bold formatting. */
	private final List<List<Boolean>> tableRowsBold = new ArrayList<>();
	/** Parallel to tableRows: whether the row was explicitly marked as header via w:tblHeader. */
	private final List<Boolean> tableRowsExplicitHeader = new ArrayList<>();

	// -----------------------------------------------------------------------
	// Math state
	// -----------------------------------------------------------------------

	private boolean inMath = false;
	private boolean inMathPara = false;
	/** Raw XML collected while inside a math element, passed to DocxMathConverter. */
	private final StringBuilder mathXml = new StringBuilder();

	// -----------------------------------------------------------------------
	// Track-changes state
	// -----------------------------------------------------------------------

	/** When true, all child elements and text are suppressed (w:del). */
	private boolean inDeletion = false;
	/** Element stack depth at which the w:del started (for correct nesting). */
	private int deletionEntryDepth = 0;

	// -----------------------------------------------------------------------
	// AlternateContent state (mc:AlternateContent / mc:Choice / mc:Fallback)
	// -----------------------------------------------------------------------

	private boolean inAlternateContent = false;
	private boolean inAlternateChoice = false;
	private boolean inAlternateFallback = false;
	private boolean alternateChoiceProducedContent = false;
	private int markdownLenAtAlternateStart = 0;

	// -----------------------------------------------------------------------
	// Image state
	// -----------------------------------------------------------------------

	/** Alt text from the most recently seen wp:docPr descr attribute. */
	private String imageAlt = null;
	/** Suppress text content inside drawing elements (wp:posOffset, etc.) */
	private int drawingDepth = 0;
	/** Width of the current drawing in pixels (from wp:extent cx, converted from EMU) */
	private int currentDrawingWidthPx = 0;
	private int currentDrawingHeightPx = 0;
	/** Pre-rendered SmartArt SVG filenames: diagramDrawing rId → SVG filename */
	private Map<String, String> smartArtSvgs = Collections.emptyMap();
	/** VML shape attributes for SVG conversion */
	private String vmlShapePath = null;
	private String vmlShapeStyle = null;
	private String vmlStrokeColor = null;
	private String vmlFillColor = null;
	private String vmlFilled = null;
	/** DrawingML shape collection for SVG conversion */
	private boolean inCustomGeom = false;
	private String presetGeomType = null;
	private final StringBuilder svgPathData = new StringBuilder();
	private int drawingmlPathW = 0;
	private int drawingmlPathH = 0;
	private int drawingmlCx = 0;
	private int drawingmlCy = 0;
	private String drawingmlFill = null;
	private String drawingmlStroke = null;
	private int drawingmlStrokeW = 0;
	/** Group shape state */
	private boolean inGroupShape = false;
	private int groupCx = 0;
	private int groupCy = 0;
	private final List<String> groupShapeSvgElements = new ArrayList<>();
	private int shapeOffX = 0;
	private int shapeOffY = 0;

	// -----------------------------------------------------------------------
	// Text box state
	// -----------------------------------------------------------------------

	private boolean inTextBox = false;
	private boolean hadTextBox = false;
	private int savedDrawingDepth = 0;
	private final StringBuilder textBoxContent = new StringBuilder();

	// -----------------------------------------------------------------------
	// Checkbox state (SDT)
	// -----------------------------------------------------------------------

	private boolean inSdt = false;
	private boolean sdtIsCheckbox = false;
	private boolean sdtChecked = false;
	private boolean suppressSdtContent = false;

	// -----------------------------------------------------------------------
	// Footnote / endnote state
	// -----------------------------------------------------------------------

	/** Footnote content map: footnoteId → text (parsed from word/footnotes.xml). */
	private Map<String, String> footnoteContents = Collections.emptyMap();
	private Map<String, String> endnoteContents = Collections.emptyMap();
	private final List<String> footnoteOrder = new ArrayList<>();
	private int footnoteCounter = 0;

	// -----------------------------------------------------------------------
	// Element nesting depth (for deletion tracking)
	// -----------------------------------------------------------------------

	private int elementDepth = 0;

	// -----------------------------------------------------------------------
	// One-shot warning flags
	// -----------------------------------------------------------------------

	private boolean commentWarningEmitted = false;

	// -----------------------------------------------------------------------
	// Constructor
	// -----------------------------------------------------------------------

	DocxToMarkdownHandler(
			Map<String, DocxRelTarget> relationships,
			Map<Integer, DocxNumberingDef> numbering,
			Map<String, String> styles,
			ZipFile zipFile) {
		this(relationships, numbering, styles, zipFile, null, Collections.emptyMap(), Collections.emptyMap());
	}

	DocxToMarkdownHandler(
			Map<String, DocxRelTarget> relationships,
			Map<Integer, DocxNumberingDef> numbering,
			Map<String, String> styles,
			ZipFile zipFile,
			File mediaDir,
			Map<String, String> footnoteContents,
			Map<String, String> endnoteContents) {
		this.relationships = relationships;
		this.numbering = numbering;
		this.styles = styles;
		this.zipFile = zipFile;
		this.mediaDir = mediaDir;
		this.footnoteContents = footnoteContents;
		this.endnoteContents = endnoteContents;
	}

	// -----------------------------------------------------------------------
	// Public API
	// -----------------------------------------------------------------------

	String getMarkdown() {
		String body = markdown.toString();
		// Append footnote definitions at the end
		if (!footnoteOrder.isEmpty()) {
			StringBuilder fn = new StringBuilder("\n");
			for (int i = 0; i < footnoteOrder.size(); i++) {
				fn.append("[^").append(i + 1).append("]: ").append(footnoteOrder.get(i)).append('\n');
			}
			body = body + fn;
		}
		return body;
	}

	List<DocxConversionMessage> getMessages() {
		return messages;
	}

	void setSmartArtSvgs(Map<String, String> smartArtSvgs) {
		this.smartArtSvgs = smartArtSvgs != null ? smartArtSvgs : Collections.emptyMap();
	}

	// -----------------------------------------------------------------------
	// SAX: startElement
	// -----------------------------------------------------------------------

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attrs)
			throws SAXException {

		elementDepth++;

		// ---- Math XML collection -----------------------------------------
		if (inMath) {
			appendMathStartTag(qName, attrs);
			return;
		}

		// ---- Track changes: deletion -------------------------------------
		if ("w:del".equals(qName)) {
			inDeletion = true;
			deletionEntryDepth = elementDepth;
			return;
		}
		if (inDeletion) {
			return;
		}

		// ---- AlternateContent -------------------------------------------
		if ("mc:AlternateContent".equals(qName)) {
			inAlternateContent = true;
			inAlternateChoice = false;
			inAlternateFallback = false;
			alternateChoiceProducedContent = false;
			markdownLenAtAlternateStart = markdown.length();
			return;
		}
		if (inAlternateContent) {
			if ("mc:Choice".equals(qName)) {
				inAlternateChoice = true;
				inAlternateFallback = false;
				return;
			}
			if ("mc:Fallback".equals(qName)) {
				// Check if mc:Choice produced any output
				inAlternateChoice = false;
				inAlternateFallback = true;
				alternateChoiceProducedContent = markdown.length() > markdownLenAtAlternateStart
						|| (inTextBox && textBoxContent.length() > 0);
				return;
			}
			// Suppress mc:Fallback content if mc:Choice already produced output
			if (inAlternateFallback && alternateChoiceProducedContent) {
				return;
			}
			// Otherwise: fall through to normal processing
		}

		switch (qName) {

			// ----------------------------------------------------------------
			// Track changes: insertion — accept; process children normally
			// ----------------------------------------------------------------
			case "w:ins":
				break;

			// ----------------------------------------------------------------
			// Paragraph
			// ----------------------------------------------------------------
			case "w:p":
				onStartParagraph();
				break;

			case "w:pPr":
				inParagraphProperties = true;
				break;

			case "w:pStyle":
				if (inParagraphProperties) {
					currentStyleId = attrVal(attrs, "w:val", "val");
				}
				break;

			case "w:numId":
				if (inParagraphProperties) {
					String val = attrVal(attrs, "w:val", "val");
					if (val != null) {
						try {
							listNumId = Integer.parseInt(val);
						} catch (NumberFormatException e) {
							log.warn("Unparseable w:numId val: {}", val);
						}
					}
				}
				break;

			case "w:ilvl":
				if (inParagraphProperties) {
					String val = attrVal(attrs, "w:val", "val");
					if (val != null) {
						try {
							listLevel = Integer.parseInt(val);
						} catch (NumberFormatException e) {
							log.warn("Unparseable w:ilvl val: {}", val);
						}
					}
				}
				break;

			// ----------------------------------------------------------------
			// Run
			// ----------------------------------------------------------------
			case "w:r":
				onStartRun();
				break;

			case "w:rPr":
				inRunProperties = true;
				break;

			case "w:rStyle": {
				if (inRunProperties) {
					String styleId = attrVal(attrs, "w:val", "val");
					applyCharacterStyle(styleId);
				}
				break;
			}

			case "w:b": {
				if (inRunProperties) {
					String val = attrVal(attrs, "w:val", "val");
					bold = isTrueOrAbsent(val);
				}
				break;
			}

			case "w:i": {
				if (inRunProperties) {
					String val = attrVal(attrs, "w:val", "val");
					italic = isTrueOrAbsent(val);
				}
				break;
			}

			case "w:strike": {
				if (inRunProperties) {
					String val = attrVal(attrs, "w:val", "val");
					strikethrough = isTrueOrAbsent(val);
				}
				break;
			}

			case "w:u": {
				if (inRunProperties) {
					String val = attrVal(attrs, "w:val", "val");
					underline = val != null && !"none".equals(val);
				}
				break;
			}

			case "w:highlight": {
				if (inRunProperties) {
					String val = attrVal(attrs, "w:val", "val");
					highlight = val != null && !"none".equals(val);
				}
				break;
			}

			case "w:vertAlign": {
				if (inRunProperties) {
					String val = attrVal(attrs, "w:val", "val");
					superscript = "superscript".equals(val);
					subscript = "subscript".equals(val);
				}
				break;
			}

			// w:t — text content arrives via characters()
			case "w:t":
				break;

			// ----------------------------------------------------------------
			// Line break
			// ----------------------------------------------------------------
			case "w:br": {
				flushCurrentRun();
				flushPendingSpan();
				String brType = attrVal(attrs, "w:type", "type");
				if ("page".equals(brType)) {
					emitToParagraph("\n---\n");
				} else {
					emitToParagraph("  \n");
				}
				break;
			}

			// ----------------------------------------------------------------
			// Tab
			// ----------------------------------------------------------------
			case "w:tab":
				if (inRun && !inRunProperties) {
					flushCurrentRun();
					flushPendingSpan();
					emitToParagraph("    ");
				}
				break;

			// ----------------------------------------------------------------
			// Hyperlink
			// ----------------------------------------------------------------
			case "w:hyperlink":
				onStartHyperlink(attrs);
				break;

			// ----------------------------------------------------------------
			// Table
			// ----------------------------------------------------------------
			case "w:tbl":
				onStartTable();
				break;

			case "w:tr":
				onStartTableRow();
				break;

			case "w:tblHeader":
				// Word explicitly marks this row as a table header
				if (tableNestingDepth == 1) {
					currentRowIsHeader = true;
				}
				break;

			case "w:tc":
				onStartTableCell();
				break;

			// ----------------------------------------------------------------
			// Drawing / Image
			// ----------------------------------------------------------------
			case "w:drawing":
				drawingDepth++;
				currentDrawingWidthPx = 0;
				currentDrawingHeightPx = 0;
				break;

			case "wp:extent": {
				String cx = attrs.getValue("cx");
				String cy = attrs.getValue("cy");
				if (cx != null && currentDrawingWidthPx == 0) {
					try {
						currentDrawingWidthPx = Math.max(1, Integer.parseInt(cx) / 9525);
					} catch (NumberFormatException e) { /* ignore */ }
				}
				if (cy != null && currentDrawingHeightPx == 0) {
					try {
						currentDrawingHeightPx = Math.max(1, Integer.parseInt(cy) / 9525);
					} catch (NumberFormatException e) { /* ignore */ }
				}
				break;
			}

			case "w:pict":
				drawingDepth++;
				break;

			case "wp:docPr": {
				String descr = attrs.getValue("descr");
				if (descr != null) {
					imageAlt = descr;
				}
				break;
			}

			case "a:blip": {
				// r:embed is the primary attribute; r:link is a fallback
				String rId = attrVal(attrs, "r:embed", "r:link");
				if (rId != null) {
					processImage(rId);
				}
				break;
			}

			case "a:videoFile": {
				// Video file reference — extract to media dir and emit as link
				String rId = attrVal(attrs, "r:link", "r:embed");
				if (rId != null) {
					processVideo(rId);
				}
				break;
			}

			case "v:imagedata": {
				// VML image (legacy/fallback) — extract via r:id
				String rId = attrVal(attrs, "r:id", "id");
				if (rId != null) {
					processImage(rId);
				}
				break;
			}

			case "dgm:relIds": {
				// SmartArt diagram — look up pre-rendered SVG by diagramData rel ID (r:dm)
				String dmRelId = attrs.getValue("r:dm");
				if (dmRelId != null) {
					String svgFile = smartArtSvgs.get(dmRelId);
					if (svgFile != null) {
						emitAsBlock(buildImageMarkdown("", "media/" + svgFile));
					}
				}
				break;
			}

			case "v:shape": {
				// VML vector shape — capture attributes for SVG conversion
				String path = attrs.getValue("path");
				if (path != null && !path.isEmpty()) {
					vmlShapePath = path;
					vmlShapeStyle = attrs.getValue("style");
					vmlStrokeColor = attrs.getValue("strokecolor");
					vmlFillColor = attrs.getValue("fillcolor");
					vmlFilled = attrs.getValue("filled");
				}
				break;
			}

			// ----------------------------------------------------------------
			// DrawingML custom geometry (wps:wsp shapes)
			// ----------------------------------------------------------------
			case "wpg:wgp":
				inGroupShape = true;
				groupShapeSvgElements.clear();
				break;

			case "a:off": {
				if (drawingDepth > 0) {
					String x = attrs.getValue("x");
					String y = attrs.getValue("y");
					if (x != null && y != null) {
						try {
							shapeOffX = Integer.parseInt(x);
							shapeOffY = Integer.parseInt(y);
						} catch (NumberFormatException e) { /* ignore */ }
					}
				}
				break;
			}

			case "a:custGeom":
				inCustomGeom = true;
				svgPathData.setLength(0);
				break;

			case "a:prstGeom": {
				String prst = attrs.getValue("prst");
				if (prst != null) {
					presetGeomType = prst;
				}
				break;
			}

			case "a:path": {
				if (inCustomGeom) {
					String w = attrs.getValue("w");
					String h = attrs.getValue("h");
					if (w != null) try { drawingmlPathW = Integer.parseInt(w); } catch (NumberFormatException e) { /* ignore */ }
					if (h != null) try { drawingmlPathH = Integer.parseInt(h); } catch (NumberFormatException e) { /* ignore */ }
				}
				break;
			}

			case "a:moveTo":
				if (inCustomGeom) svgPathData.append("M");
				break;

			case "a:cubicBezTo":
				if (inCustomGeom) svgPathData.append("C");
				break;

			case "a:lnTo":
				if (inCustomGeom) svgPathData.append("L");
				break;

			case "a:close":
				if (inCustomGeom) svgPathData.append("Z ");
				break;

			case "a:pt": {
				if (inCustomGeom) {
					String x = attrs.getValue("x");
					String y = attrs.getValue("y");
					if (x != null && y != null) {
						svgPathData.append(x).append(' ').append(y).append(' ');
					}
				}
				break;
			}

			case "a:ext": {
				// Capture shape dimensions (EMU) from a:xfrm/a:ext
				if (drawingDepth > 0) {
					String cx = attrs.getValue("cx");
					String cy = attrs.getValue("cy");
					if (cx != null && cy != null) {
						try {
							int cxVal = Integer.parseInt(cx);
							int cyVal = Integer.parseInt(cy);
							drawingmlCx = cxVal;
							drawingmlCy = cyVal;
							// First a:ext in a group sets the group dimensions
							if (inGroupShape && groupCx == 0) {
								groupCx = cxVal;
								groupCy = cyVal;
							}
						} catch (NumberFormatException e) { /* ignore */ }
					}
				}
				break;
			}

			case "a:srgbClr": {
				// Capture fill/stroke color
				if (drawingDepth > 0) {
					String val = attrs.getValue("val");
					if (val != null) {
						// This is approximate — ideally we'd track whether we're inside a:solidFill or a:ln
						if (drawingmlFill == null) {
							drawingmlFill = "#" + val;
						} else if (drawingmlStroke == null) {
							drawingmlStroke = "#" + val;
						}
					}
				}
				break;
			}

			case "a:ln": {
				if (drawingDepth > 0) {
					String w = attrs.getValue("w");
					if (w != null) try { drawingmlStrokeW = Integer.parseInt(w); } catch (NumberFormatException e) { /* ignore */ }
				}
				break;
			}

			// ----------------------------------------------------------------
			// Math
			// ----------------------------------------------------------------
			case "m:oMathPara":
				flushCurrentRun();
				flushPendingSpan();
				inMathPara = true;
				inMath = true;
				mathXml.setLength(0);
				mathXml.append("<m:oMathPara xmlns:m=\"http://schemas.openxmlformats.org/officeDocument/2006/math\""
					+ " xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">");
				break;

			case "m:oMath":
				if (!inMathPara) {
					flushCurrentRun();
					flushPendingSpan();
					inMath = true;
					mathXml.setLength(0);
					mathXml.append("<m:oMath xmlns:m=\"http://schemas.openxmlformats.org/officeDocument/2006/math\""
						+ " xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">");
				}
				break;

			// ----------------------------------------------------------------
			// Footnotes / endnotes
			// ----------------------------------------------------------------
			case "w:footnoteReference": {
				String fnId = attrVal(attrs, "w:id", "id");
				if (fnId != null && !"0".equals(fnId) && !"-1".equals(fnId)) {
					String content = footnoteContents.get(fnId);
					if (content != null && !content.isBlank()) {
						flushCurrentRun();
						flushPendingSpan();
						footnoteCounter++;
						footnoteOrder.add(content.trim());
						emitToParagraph("[^" + footnoteCounter + "]");
					}
				}
				break;
			}
			case "w:endnoteReference": {
				String enId = attrVal(attrs, "w:id", "id");
				if (enId != null && !"0".equals(enId) && !"-1".equals(enId)) {
					String content = endnoteContents.get(enId);
					if (content != null && !content.isBlank()) {
						flushCurrentRun();
						flushPendingSpan();
						footnoteCounter++;
						footnoteOrder.add(content.trim());
						emitToParagraph("[^" + footnoteCounter + "]");
					}
				}
				break;
			}

			// ----------------------------------------------------------------
			// Structured document tags (checkboxes)
			// ----------------------------------------------------------------
			case "w:sdt":
				inSdt = true;
				sdtIsCheckbox = false;
				sdtChecked = false;
				break;

			case "w14:checkbox":
			case "w:checkBox":
				sdtIsCheckbox = true;
				break;

			case "w14:checked":
			case "w:checked": {
				String val = attrVal(attrs, "w14:val", "w:val");
				if (val == null) val = attrVal(attrs, "val", "val");
				sdtChecked = "1".equals(val) || "true".equals(val);
				break;
			}

			case "w:sdtContent":
				if (inSdt && sdtIsCheckbox) {
					flushCurrentRun();
					flushPendingSpan();
					emitToParagraph(sdtChecked ? "[x] " : "[ ] ");
					suppressSdtContent = true;
				}
				break;

			// ----------------------------------------------------------------
			// Text boxes
			// ----------------------------------------------------------------
			case "w:txbxContent":
				flushCurrentRun();
				flushPendingSpan();
				inTextBox = true;
				hadTextBox = true;
				savedDrawingDepth = drawingDepth;
				drawingDepth = 0; // allow text inside text box
				textBoxContent.setLength(0);
				break;

			// ----------------------------------------------------------------
			// Comments
			// ----------------------------------------------------------------
			case "w:commentRangeStart":
				if (!commentWarningEmitted) {
					messages.add(new DocxConversionMessage(Level.WARNING, "docx.convert.warn.comments"));
					commentWarningEmitted = true;
				}
				break;

			default:
				break;
		}
	}

	// -----------------------------------------------------------------------
	// SAX: endElement
	// -----------------------------------------------------------------------

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {

		// ---- Math XML collection -----------------------------------------
		if (inMath) {
			boolean closingMathRoot =
					("m:oMath".equals(qName) && !inMathPara) ||
					("m:oMathPara".equals(qName) && inMathPara);

			if (closingMathRoot) {
				mathXml.append("</").append(qName).append('>');
				emitMath();
			} else {
				mathXml.append("</").append(qName).append('>');
			}
			elementDepth--;
			return;
		}

		// ---- Track changes: deletion ------------------------------------
		if (inDeletion) {
			if (elementDepth == deletionEntryDepth) {
				// We are closing the w:del element itself
				inDeletion = false;
				deletionEntryDepth = 0;
			}
			elementDepth--;
			return;
		}

		// ---- AlternateContent ------------------------------------------
		if (inAlternateContent) {
			if ("mc:AlternateContent".equals(qName)) {
				inAlternateContent = false;
				inAlternateChoice = false;
				inAlternateFallback = false;
				elementDepth--;
				return;
			}
			if ("mc:Choice".equals(qName) || "mc:Fallback".equals(qName)) {
				elementDepth--;
				return;
			}
			// Suppress mc:Fallback content if mc:Choice already produced output
			if (inAlternateFallback && alternateChoiceProducedContent) {
				elementDepth--;
				return;
			}
		}

		switch (qName) {

			case "w:pPr":
				inParagraphProperties = false;
				break;

			case "w:rPr":
				inRunProperties = false;
				break;

			case "w:p":
				onEndParagraph();
				break;

			case "w:r":
				onEndRun();
				break;

			case "w:hyperlink":
				onEndHyperlink();
				break;

			case "w:tbl":
				onEndTable();
				break;

			case "w:tr":
				onEndTableRow();
				break;

			case "w:tc":
				onEndTableCell();
				break;

			case "w:drawing":
			case "w:pict":
				if (drawingDepth > 0) drawingDepth--;
				break;

			case "a:custGeom":
				if (inCustomGeom && svgPathData.length() > 0 && mediaDir != null) {
					emitDrawingMLShape();
				}
				inCustomGeom = false;
				break;

			case "wps:wsp":
				// Emit preset/custom geometry shapes as SVG
				if (!hadTextBox && drawingmlCx > 0 && drawingmlCy > 0) {
					if (inGroupShape) {
						// Collect shape for group SVG
						String svgEl = buildShapeSvgElement();
						if (svgEl != null) {
							groupShapeSvgElements.add(svgEl);
						}
					} else if (presetGeomType != null && mediaDir != null) {
						emitPresetShape();
					}
				}
				hadTextBox = false;
				presetGeomType = null;
				drawingmlFill = null;
				drawingmlStroke = null;
				drawingmlStrokeW = 0;
				drawingmlCx = 0;
				drawingmlCy = 0;
				shapeOffX = 0;
				shapeOffY = 0;
				break;

			case "wpg:wgp":
				if (inGroupShape && !groupShapeSvgElements.isEmpty() && mediaDir != null) {
					emitGroupShape();
				}
				inGroupShape = false;
				groupShapeSvgElements.clear();
				groupCx = 0;
				groupCy = 0;
				break;

			case "v:shape":
				if (vmlShapePath != null && mediaDir != null) {
					String svg = VmlToSvgConverter.convert(
						vmlShapePath, vmlShapeStyle, vmlStrokeColor, vmlFillColor, vmlFilled);
					if (svg != null) {
						// Write SVG to media directory
						String svgFilename = "shape_" + System.nanoTime() + ".svg";
						try {
							File svgFile = new File(mediaDir, svgFilename);
							Files.writeString(svgFile.toPath(), svg);
							emitAsBlock(buildImageMarkdown("", "media/" + svgFilename));
						} catch (Exception e) {
							log.debug("Failed to write SVG: {}", e.getMessage());
						}
					}
					vmlShapePath = null;
				}
				break;

			case "w:sdt":
				inSdt = false;
				sdtIsCheckbox = false;
				suppressSdtContent = false;
				break;

			case "w:txbxContent":
				if (inTextBox) {
					inTextBox = false;
					drawingDepth = savedDrawingDepth;
					String tbContent = textBoxContent.toString().trim();
					textBoxContent.setLength(0);
					if (!tbContent.isEmpty()) {
						// Emit as note admonition
						markdown.append("\n> [!NOTE]\n");
						for (String line : tbContent.split("\n")) {
							markdown.append("> ").append(line).append('\n');
						}
						markdown.append('\n');
					}
				}
				break;

			default:
				break;
		}

		elementDepth--;
	}

	// -----------------------------------------------------------------------
	// SAX: characters
	// -----------------------------------------------------------------------

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (inMath) {
			mathXml.append(escapeXmlText(new String(ch, start, length)));
			return;
		}
		if (inDeletion) {
			return;
		}
		if (inAlternateFallback && alternateChoiceProducedContent) {
			return;
		}
		if (suppressSdtContent) {
			return;
		}
		if (drawingDepth > 0) {
			return; // suppress text from drawing metadata (wp:posOffset, etc.)
		}
		if (inRun) {
			currentText.append(stripControlChars(new String(ch, start, length)));
		}
	}

	// -----------------------------------------------------------------------
	// Paragraph logic
	// -----------------------------------------------------------------------

	private void onStartParagraph() {
		inParagraph = true;
		paragraphPrefixEmitted = false;
		currentStyleId = null;
		listNumId = -1;
		listLevel = 0;
		imageAlt = null;
	}

	private void onEndParagraph() {
		if (!inParagraph) return;

		// Flush any open run and pending formatting span
		flushCurrentRun();
		flushPendingSpan();

		inParagraph = false;

		if (inTextBox) {
			textBoxContent.append('\n');
			return;
		}

		if (inTableCell) {
			// Multi-paragraph cells: join with space
			if (!cellFirstParagraph && cellText.length() > 0) {
				cellText.append(' ');
			}
			cellFirstParagraph = false;
			return;
		}

		boolean isList = listNumId > 0;

		// If emitAsBlock already closed this paragraph (paragraphPrefixEmitted == false
		// after content was emitted), don't add extra newlines
		if (!paragraphPrefixEmitted && markdown.length() > 0
				&& markdown.charAt(markdown.length() - 1) == '\n') {
			// Paragraph was already properly terminated by emitAsBlock
			if (isList) {
				previousWasList = true;
				previousListNumId = listNumId;
			} else {
				previousWasList = false;
			}
			return;
		}

		if (isList) {
			markdown.append('\n');
			previousWasList = true;
			previousListNumId = listNumId;
		} else {
			// Two newlines = blank line between paragraphs (standard Markdown)
			markdown.append("\n\n");
			previousWasList = false;
		}
	}

	/**
	 * Emits content into the current paragraph, writing the paragraph prefix
	 * (heading markers, list markers, blockquote {@code >}) on first emission.
	 */
	private void emitToParagraph(String text) {
		if (text.isEmpty()) return;

		if (inTextBox) {
			textBoxContent.append(text);
			return;
		}

		if (inTableCell) {
			cellText.append(text);
			return;
		}

		if (!paragraphPrefixEmitted) {
			paragraphPrefixEmitted = true;
			String prefix = buildParagraphPrefix();
			markdown.append(prefix);
		}
		markdown.append(text);
	}

	/**
	 * Builds the prefix string for the current paragraph style/list context.
	 * Returns an empty string for regular body paragraphs.
	 */
	private String buildParagraphPrefix() {
		// List item
		if (listNumId > 0) {
			String prefix = "";
			// Insert blank line between different lists or before first list item after non-list
			if (previousWasList && previousListNumId != listNumId) {
				prefix = "\n"; // blank line between different lists
			}
			String indent = "    ".repeat(Math.max(0, listLevel));
			DocxNumberingDef def = numbering.get(listNumId);
			boolean ordered = (def != null) && def.isOrdered(listLevel);
			return prefix + indent + (ordered ? "1. " : "- ");
		}

		// Blank line after list block when transitioning to non-list paragraph
		String prefix = "";
		if (previousWasList) {
			prefix = "\n";
			previousWasList = false;
		}

		// Heading
		int headingLevel = resolveHeadingLevel(currentStyleId);
		if (headingLevel > 0) {
			return prefix + "#".repeat(headingLevel) + " ";
		}

		// Intense blockquote
		if (isIntenseQuoteStyle(currentStyleId)) {
			return prefix + "> [!IMPORTANT]\n> ";
		}

		// Regular blockquote
		if (isQuoteStyle(currentStyleId)) {
			return prefix + "> ";
		}

		return prefix;
	}

	// -----------------------------------------------------------------------
	// Style resolution helpers
	// -----------------------------------------------------------------------

	/**
	 * Applies formatting flags based on a character style (w:rStyle).
	 * Emphasis → italic, IntenseEmphasis → bold+italic, Strong → bold.
	 */
	private void applyCharacterStyle(String styleId) {
		if (styleId == null) return;
		String resolved = styles.getOrDefault(styleId, styleId);
		switch (resolved) {
			case "Emphasis", "SubtleEmphasis":
				italic = true;
				break;
			case "IntenseEmphasis", "BookTitle":
				bold = true;
				italic = true;
				break;
			case "Strong":
				bold = true;
				break;
			default:
				break;
		}
	}

	/** Returns the heading level (1–6) for the given style ID, or 0 if not a heading. */
	private int resolveHeadingLevel(String styleId) {
		if (styleId == null) return 0;
		String resolved = styles.getOrDefault(styleId, styleId);
		return switch (resolved) {
			case "Heading1", "Title" -> 1;
			case "Heading2", "Subtitle" -> 2;
			case "Heading3" -> 3;
			case "Heading4" -> 4;
			case "Heading5" -> 5;
			case "Heading6" -> 6;
			default -> {
				if (resolved.startsWith("Heading")) {
					try {
						int lvl = Integer.parseInt(resolved.substring(7));
						yield Math.min(Math.max(lvl, 1), 6);
					} catch (NumberFormatException e) {
						yield 0;
					}
				}
				yield 0;
			}
		};
	}

	private boolean isQuoteStyle(String styleId) {
		if (styleId == null) return false;
		return "Quote".equals(styles.getOrDefault(styleId, styleId));
	}

	private boolean isIntenseQuoteStyle(String styleId) {
		if (styleId == null) return false;
		return "IntenseQuote".equals(styles.getOrDefault(styleId, styleId));
	}

	// -----------------------------------------------------------------------
	// Run logic
	// -----------------------------------------------------------------------

	private void onStartRun() {
		inRun = true;
		currentText.setLength(0);
		bold = false;
		italic = false;
		strikethrough = false;
		underline = false;
		superscript = false;
		subscript = false;
		highlight = false;
	}

	private void onEndRun() {
		flushCurrentRun();
		inRun = false;
	}

	/**
	 * Moves the current run's text into the pending span. If the formatting
	 * differs from the pending span, the pending span is flushed first.
	 * This merges consecutive runs with identical formatting into a single
	 * markdown formatting block (e.g., two bold runs → one **...** span).
	 */
	private void flushCurrentRun() {
		String raw = currentText.toString();
		currentText.setLength(0);
		if (raw.isEmpty()) return;

		// If formatting changed from pending span, flush the pending span first
		if (hasPending && (bold != pendingBold || italic != pendingItalic
				|| strikethrough != pendingStrikethrough || underline != pendingUnderline
				|| superscript != pendingSuperscript || subscript != pendingSubscript
				|| highlight != pendingHighlight)) {
			flushPendingSpan();
		}

		// Accumulate into pending span
		pendingText.append(escapeMarkdown(raw));
		// Track bold for table header detection
		if (inTableCell && bold) {
			cellHasBold = true;
		}
		pendingBold = bold;
		pendingItalic = italic;
		pendingStrikethrough = strikethrough;
		pendingUnderline = underline;
		pendingSuperscript = superscript;
		pendingSubscript = subscript;
		pendingHighlight = highlight;
		hasPending = true;
	}

	/**
	 * Emits the pending formatted span to the output, wrapping with
	 * the appropriate markdown formatting characters.
	 */
	private void flushPendingSpan() {
		if (!hasPending || pendingText.length() == 0) {
			hasPending = false;
			pendingText.setLength(0);
			return;
		}

		String escaped = pendingText.toString();
		pendingText.setLength(0);
		hasPending = false;

		boolean hasFormatting = pendingBold || pendingItalic || pendingStrikethrough
				|| pendingUnderline || pendingSuperscript || pendingSubscript || pendingHighlight;

		// Move leading/trailing whitespace outside formatting markers so that
		// markdown renderers don't break (e.g. ~~text ~~ won't render).
		String leading = "";
		String trailing = "";
		if (hasFormatting) {
			int start = 0;
			while (start < escaped.length() && escaped.charAt(start) == ' ') start++;
			int end = escaped.length();
			while (end > start && escaped.charAt(end - 1) == ' ') end--;
			if (start > 0 || end < escaped.length()) {
				leading = escaped.substring(0, start);
				trailing = escaped.substring(end);
				escaped = escaped.substring(start, end);
				if (escaped.isEmpty()) {
					// Only whitespace — emit without formatting
					emitOrBuffer(leading + trailing);
					return;
				}
			}
		}

		String formatted;
		if (pendingBold && pendingItalic && pendingStrikethrough) {
			formatted = "~~***" + escaped + "***~~";
		} else if (pendingBold && pendingItalic) {
			formatted = "***" + escaped + "***";
		} else if (pendingBold && pendingStrikethrough) {
			formatted = "~~**" + escaped + "**~~";
		} else if (pendingItalic && pendingStrikethrough) {
			formatted = "~~*" + escaped + "*~~";
		} else if (pendingBold) {
			formatted = "**" + escaped + "**";
		} else if (pendingItalic) {
			formatted = "*" + escaped + "*";
		} else if (pendingStrikethrough) {
			formatted = "~~" + escaped + "~~";
		} else {
			formatted = escaped;
		}

		// Underline wraps around everything else
		if (pendingUnderline) {
			formatted = "<span style=\"text-decoration:underline\">" + formatted + "</span>";
		}
		// Highlight as <mark>
		if (pendingHighlight) {
			formatted = "<mark>" + formatted + "</mark>";
		}
		// Superscript / subscript as HTML
		if (pendingSuperscript) {
			formatted = "<sup>" + formatted + "</sup>";
		}
		if (pendingSubscript) {
			formatted = "<sub>" + formatted + "</sub>";
		}

		emitOrBuffer(leading + formatted + trailing);
	}

	private void emitOrBuffer(String text) {
		if (inHyperlink) {
			hyperlinkText.append(text);
		} else {
			emitToParagraph(text);
		}
	}

	/**
	 * Builds markdown image syntax with optional dimensions using the CommonMark
	 * image-attributes extension: ![alt](url){width=280 height=186}
	 */
	private String buildImageMarkdown(String alt, String url) {
		if (currentDrawingWidthPx > 0) {
			StringBuilder attrs = new StringBuilder("{width=");
			attrs.append(currentDrawingWidthPx);
			if (currentDrawingHeightPx > 0) {
				attrs.append(" height=").append(currentDrawingHeightPx);
			}
			attrs.append('}');
			return "![" + alt + "](" + url + ")" + attrs;
		}
		return "![" + alt + "](" + url + ")";
	}

	/**
	 * Emits content as its own standalone markdown block, separate from the
	 * current paragraph. Used for images, shapes, and videos which must be
	 * in their own paragraph to be recognized by the markdown importer.
	 */
	/**
	 * Emits media content (images, shapes, videos) as its own markdown paragraph.
	 * Ensures text before AND after the media starts a separate paragraph so the
	 * markdown importer recognizes the image as standalone.
	 */
	private void emitAsBlock(String content) {
		if (inTextBox) {
			textBoxContent.append(content).append('\n');
			return;
		}
		if (inTableCell) {
			cellText.append(content);
			return;
		}
		// Flush any pending formatted text
		flushCurrentRun();
		flushPendingSpan();

		if (paragraphPrefixEmitted) {
			// Paragraph already has text — close it first
			markdown.append("\n\n");
		}

		// Emit media with the proper prefix for the block context
		String prefix = buildContinuationPrefix();
		markdown.append(prefix).append(content).append("\n\n");

		// Reset so that any following text in the same w:p starts a fresh paragraph
		paragraphPrefixEmitted = false;
	}

	/**
	 * Returns the prefix for continuation lines within the current block context.
	 * Unlike buildParagraphPrefix() which emits the full prefix (list marker, admonition header),
	 * this returns only the indentation/continuation marker needed for subsequent lines.
	 */
	private String buildContinuationPrefix() {
		// Blockquote continuation
		if (isQuoteStyle(currentStyleId) || isIntenseQuoteStyle(currentStyleId)) {
			return "> ";
		}
		// List item continuation: indent to content level (marker width + spaces)
		if (listNumId > 0) {
			String indent = "    ".repeat(Math.max(0, listLevel));
			return indent + "  "; // 2 extra spaces to align under list content
		}
		return "";
	}

	// -----------------------------------------------------------------------
	// Hyperlink logic
	// -----------------------------------------------------------------------

	private void onStartHyperlink(Attributes attrs) {
		// Flush pending text before entering hyperlink context
		flushPendingSpan();
		inHyperlink = true;
		hyperlinkText.setLength(0);
		hyperlinkUrl = null;

		// External hyperlink: r:id references a relationship
		String rId = attrVal(attrs, "r:id", "id");
		if (rId != null) {
			DocxRelTarget rel = relationships.get(rId);
			if (rel != null) {
				hyperlinkUrl = rel.target();
			}
		} else {
			// Internal anchor
			String anchor = attrVal(attrs, "w:anchor", "anchor");
			if (anchor != null) {
				hyperlinkUrl = "#" + anchor;
			}
		}
	}

	private void onEndHyperlink() {
		// Flush any dangling run text and pending span first
		flushCurrentRun();
		flushPendingSpan();

		String text = hyperlinkText.toString();
		String url = (hyperlinkUrl != null) ? hyperlinkUrl : "";

		if (text.isEmpty()) {
			text = url;
		}

		String linkMarkdown = url.isEmpty() ? text : "[" + text + "](" + url + ")";
		inHyperlink = false;
		hyperlinkUrl = null;
		hyperlinkText.setLength(0);

		emitToParagraph(linkMarkdown);
	}

	// -----------------------------------------------------------------------
	// Table logic
	// -----------------------------------------------------------------------

	private void onStartTable() {
		tableNestingDepth++;
		if (tableNestingDepth > 1) {
			messages.add(new DocxConversionMessage(Level.WARNING, "docx.convert.warn.table.nested"));
		} else {
			tableRows.clear();
			tableRowsBold.clear();
			tableRowsExplicitHeader.clear();
		}
	}

	private void onEndTable() {
		if (tableNestingDepth == 1) {
			emitTable();
		}
		tableNestingDepth--;
	}

	private void onStartTableRow() {
		if (tableNestingDepth == 1) {
			currentRowCells.clear();
			currentRowBold.clear();
			currentRowIsHeader = false;
		}
	}

	private void onEndTableRow() {
		if (tableNestingDepth != 1) return;
		tableRows.add(new ArrayList<>(currentRowCells));
		tableRowsBold.add(new ArrayList<>(currentRowBold));
		tableRowsExplicitHeader.add(currentRowIsHeader);
	}

	private void onStartTableCell() {
		if (tableNestingDepth == 1) {
			inTableCell = true;
			cellText.setLength(0);
			cellFirstParagraph = true;
			cellHasBold = false;
		}
	}

	private void onEndTableCell() {
		if (tableNestingDepth != 1) return;

		// Flush any open run and pending span into the cell
		flushCurrentRun();
		flushPendingSpan();

		inTableCell = false;
		String content = cellText.toString()
				.replace("|", "\\|")
				.trim();
		currentRowCells.add(content);
		currentRowBold.add(cellHasBold && !content.isEmpty());
		cellText.setLength(0);
	}

	/**
	 * Emits the collected table with header detection (priority order):
	 * 1. Explicit w:tblHeader on the first row(s) → those rows are the header
	 * 2. Fuzzy: ALL non-empty cells in the first row are bold → first row is header
	 * 3. Otherwise → no header row; an empty header row is emitted for GFM compliance
	 */
	private void emitTable() {
		if (tableRows.isEmpty()) return;

		int numCols = 0;
		for (List<String> row : tableRows) {
			numCols = Math.max(numCols, row.size());
		}
		if (numCols == 0) return;

		// Priority 1: Explicit header rows (w:tblHeader)
		int explicitHeaderCount = 0;
		for (int r = 0; r < tableRowsExplicitHeader.size(); r++) {
			if (tableRowsExplicitHeader.get(r)) {
				explicitHeaderCount = r + 1; // consecutive header rows from top
			} else {
				break;
			}
		}

		if (explicitHeaderCount > 0) {
			// Emit explicit header rows (GFM only supports 1, so use the last header row as GFM header)
			// For multi-row headers: emit earlier rows as regular rows, last header row + separator
			for (int r = 0; r < explicitHeaderCount; r++) {
				emitTableRow(tableRows.get(r), numCols);
			}
			emitSeparator(numCols);
			for (int r = explicitHeaderCount; r < tableRows.size(); r++) {
				emitTableRow(tableRows.get(r), numCols);
			}
		} else {
			// Priority 2: Fuzzy detection — first row all bold
			boolean firstRowAllBold = isAllBold(tableRowsBold.get(0), tableRows.get(0));

			if (firstRowAllBold) {
				// First row is the header
				emitTableRow(tableRows.get(0), numCols);
				emitSeparator(numCols);
				for (int r = 1; r < tableRows.size(); r++) {
					emitTableRow(tableRows.get(r), numCols);
				}
			} else {
				// No header detected — GFM requires a header row.
				// Use the first row as header since there's no alternative in GFM.
				emitTableRow(tableRows.get(0), numCols);
				emitSeparator(numCols);
				for (int r = 1; r < tableRows.size(); r++) {
					emitTableRow(tableRows.get(r), numCols);
				}
			}
		}

		markdown.append('\n');
	}

	private void emitTableRow(List<String> cells, int numCols) {
		StringBuilder row = new StringBuilder("|");
		for (int c = 0; c < numCols; c++) {
			String cell = c < cells.size() ? cells.get(c) : "";
			row.append(' ').append(cell).append(" |");
		}
		markdown.append(row).append('\n');
	}

	private void emitSeparator(int numCols) {
		StringBuilder sep = new StringBuilder("|");
		for (int c = 0; c < numCols; c++) {
			sep.append("---|");
		}
		markdown.append(sep).append('\n');
	}

	/** Returns true if all non-empty cells in the row have bold formatting. */
	private static boolean isAllBold(List<Boolean> boldFlags, List<String> cells) {
		boolean hasNonEmpty = false;
		for (int i = 0; i < cells.size(); i++) {
			if (!cells.get(i).isEmpty()) {
				hasNonEmpty = true;
				if (i >= boldFlags.size() || !boldFlags.get(i)) {
					return false;
				}
			}
		}
		return hasNonEmpty;
	}

	// -----------------------------------------------------------------------
	// Math logic
	// -----------------------------------------------------------------------

	private void appendMathStartTag(String qName, Attributes attrs) {
		mathXml.append('<').append(qName);
		for (int i = 0; i < attrs.getLength(); i++) {
			mathXml.append(' ')
					.append(attrs.getQName(i))
					.append("=\"")
					.append(escapeXmlAttr(attrs.getValue(i)))
					.append('"');
		}
		mathXml.append('>');
	}

	private void emitMath() {
		String xml = mathXml.toString();
		mathXml.setLength(0);

		log.debug("Math XML collected ({} chars): {}",
				xml.length(), xml.length() > 500 ? xml.substring(0, 500) + "..." : xml);

		// Strategy 1: Try per-formula conversion via minimal DOCX wrapper + fmath
		String latex = DocxMathConverter.convertToLatex(xml);

		// Strategy 2: Fallback to plain text extraction from math XML
		if (latex == null || latex.isBlank()) {
			latex = DocxMathConverter.extractMathText(xml);
		}

		if (latex == null || latex.isBlank()) {
			log.warn("Math conversion returned null/empty for XML of length {}", xml.length());
			messages.add(new DocxConversionMessage(Level.WARNING, "docx.convert.warn.math.failed"));
		} else {
			// Always use display math ($$...$$) because the markdown importer
			// only supports display math blocks, not inline $...$ math.
			emitToParagraph("\n\n$$\n" + latex + "\n$$\n\n");
		}

		inMath = false;
		inMathPara = false;
	}

	// -----------------------------------------------------------------------
	// DrawingML shape → SVG
	// -----------------------------------------------------------------------

	private void emitDrawingMLShape() {
		String pathData = svgPathData.toString().strip();
		svgPathData.setLength(0);
		if (pathData.isEmpty()) return;

		// Use the path's own coordinate system as viewBox
		int viewW = drawingmlPathW > 0 ? drawingmlPathW : (drawingmlCx > 0 ? drawingmlCx : 1000);
		int viewH = drawingmlPathH > 0 ? drawingmlPathH : (drawingmlCy > 0 ? drawingmlCy : 1000);

		// Convert EMU to pixels (1 EMU = 1/914400 inch, ~96 DPI)
		int pxW = drawingmlCx > 0 ? Math.max(20, drawingmlCx / 9525) : 100;
		int pxH = drawingmlCy > 0 ? Math.max(20, drawingmlCy / 9525) : 100;

		String fill = drawingmlFill != null ? drawingmlFill : "none";
		String stroke = drawingmlStroke != null ? drawingmlStroke : "#000000";
		double strokeW = drawingmlStrokeW > 0 ? Math.max(0.5, drawingmlStrokeW / 12700.0) : 1.0;

		String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"" + pxW
			+ "\" height=\"" + pxH + "\" viewBox=\"0 0 " + viewW + " " + viewH + "\">"
			+ "<path d=\"" + pathData + "\" fill=\"" + fill + "\" stroke=\"" + stroke
			+ "\" stroke-width=\"" + String.format("%.1f", strokeW * viewW / pxW) + "\"/></svg>";

		String svgFilename = "shape_" + System.nanoTime() + ".svg";
		try {
			File svgFile = new File(mediaDir, svgFilename);
			Files.writeString(svgFile.toPath(), svg);
			emitAsBlock(buildImageMarkdown("", "media/" + svgFilename));
		} catch (Exception e) {
			log.debug("Failed to write SVG: {}", e.getMessage());
		}
	}

	// -----------------------------------------------------------------------
	// Group shape → SVG
	// -----------------------------------------------------------------------

	/** Build an SVG element string for the current shape (for use inside a group SVG). */
	private String buildShapeSvgElement() {
		int w = drawingmlCx > 0 ? Math.max(1, drawingmlCx / 9525) : 50;
		int h = drawingmlCy > 0 ? Math.max(1, drawingmlCy / 9525) : 50;
		String fill = drawingmlFill != null ? drawingmlFill : "#4472C4";
		String stroke = drawingmlStroke != null ? drawingmlStroke : "#2F528F";
		double sw = drawingmlStrokeW > 0 ? Math.max(0.5, drawingmlStrokeW / 12700.0) : 1.0;
		// Offset in EMU → px
		int ox = shapeOffX / 9525;
		int oy = shapeOffY / 9525;
		String attrs = " fill=\"" + fill + "\" stroke=\"" + stroke + "\" stroke-width=\"" + String.format("%.1f", sw) + "\"";

		if (presetGeomType != null) {
			return switch (presetGeomType) {
				case "ellipse" -> "<ellipse cx=\"" + (ox + w/2) + "\" cy=\"" + (oy + h/2)
					+ "\" rx=\"" + (w/2) + "\" ry=\"" + (h/2) + "\"" + attrs + "/>";
				case "rect", "rectangle" -> "<rect x=\"" + ox + "\" y=\"" + oy
					+ "\" width=\"" + w + "\" height=\"" + h + "\"" + attrs + "/>";
				case "roundRect" -> "<rect x=\"" + ox + "\" y=\"" + oy
					+ "\" width=\"" + w + "\" height=\"" + h
					+ "\" rx=\"" + Math.min(w, h)/10 + "\"" + attrs + "/>";
				default -> null;
			};
		}
		return null;
	}

	/** Emit all collected group shapes as a single SVG. */
	private void emitGroupShape() {
		int pxW = groupCx > 0 ? Math.max(20, groupCx / 9525) : 200;
		int pxH = groupCy > 0 ? Math.max(20, groupCy / 9525) : 200;

		StringBuilder svg = new StringBuilder();
		svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"")
			.append(pxW).append("\" height=\"").append(pxH).append("\">");
		for (String el : groupShapeSvgElements) {
			svg.append(el);
		}
		svg.append("</svg>");

		String filename = "shape_" + System.nanoTime() + ".svg";
		try {
			File svgFile = new File(mediaDir, filename);
			Files.writeString(svgFile.toPath(), svg.toString());
			emitAsBlock(buildImageMarkdown("", "media/" + filename));
		} catch (Exception e) {
			log.debug("Failed to write group SVG: {}", e.getMessage());
		}
	}

	// -----------------------------------------------------------------------
	// Preset shape → SVG
	// -----------------------------------------------------------------------

	private void emitPresetShape() {
		int pxW = Math.max(20, drawingmlCx / 9525);
		int pxH = Math.max(20, drawingmlCy / 9525);
		String fill = drawingmlFill != null ? drawingmlFill : "#4472C4";
		String stroke = drawingmlStroke != null ? drawingmlStroke : "#2F528F";
		double strokeW = drawingmlStrokeW > 0 ? Math.max(0.5, drawingmlStrokeW / 12700.0) : 1.0;

		String shapeContent = switch (presetGeomType) {
			case "ellipse" -> "<ellipse cx=\"" + (pxW/2) + "\" cy=\"" + (pxH/2)
				+ "\" rx=\"" + (pxW/2 - 1) + "\" ry=\"" + (pxH/2 - 1) + "\"";
			case "rect", "rectangle" -> "<rect x=\"1\" y=\"1\" width=\"" + (pxW-2) + "\" height=\"" + (pxH-2) + "\"";
			case "roundRect" -> "<rect x=\"1\" y=\"1\" width=\"" + (pxW-2) + "\" height=\"" + (pxH-2)
				+ "\" rx=\"" + Math.min(pxW, pxH)/10 + "\"";
			case "triangle", "rtTriangle" -> "<polygon points=\"" + (pxW/2) + ",1 1," + (pxH-1) + " " + (pxW-1) + "," + (pxH-1) + "\"";
			case "diamond" -> "<polygon points=\"" + (pxW/2) + ",1 " + (pxW-1) + "," + (pxH/2) + " " + (pxW/2) + "," + (pxH-1) + " 1," + (pxH/2) + "\"";
			case "line" -> "<line x1=\"0\" y1=\"0\" x2=\"" + pxW + "\" y2=\"" + pxH + "\"";
			case "star5" -> buildStarPoints(pxW, pxH, 5);
			case "hexagon" -> buildRegularPolygon(pxW, pxH, 6);
			case "octagon" -> buildRegularPolygon(pxW, pxH, 8);
			case "arrow", "rightArrow" -> "<polygon points=\"0," + (pxH/3) + " " + (pxW*2/3) + "," + (pxH/3)
				+ " " + (pxW*2/3) + ",0 " + pxW + "," + (pxH/2) + " " + (pxW*2/3) + "," + pxH
				+ " " + (pxW*2/3) + "," + (pxH*2/3) + " 0," + (pxH*2/3) + "\"";
			default -> null;
		};

		if (shapeContent == null) {
			log.debug("Unsupported preset geometry: {}", presetGeomType);
			return;
		}

		String svgElement = shapeContent + " fill=\"" + fill + "\" stroke=\"" + stroke
			+ "\" stroke-width=\"" + String.format("%.1f", strokeW) + "\"/>";

		String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"" + pxW
			+ "\" height=\"" + pxH + "\">" + svgElement + "</svg>";

		String filename = "shape_" + System.nanoTime() + ".svg";
		try {
			File svgFile = new File(mediaDir, filename);
			Files.writeString(svgFile.toPath(), svg);
			emitAsBlock(buildImageMarkdown("", "media/" + filename));
		} catch (Exception e) {
			log.debug("Failed to write preset shape SVG: {}", e.getMessage());
		}
	}

	private String buildRegularPolygon(int w, int h, int sides) {
		StringBuilder pts = new StringBuilder("<polygon points=\"");
		double cx = w / 2.0, cy = h / 2.0;
		double r = Math.min(cx, cy) - 1;
		for (int i = 0; i < sides; i++) {
			double angle = 2 * Math.PI * i / sides - Math.PI / 2;
			int x = (int) Math.round(cx + r * Math.cos(angle));
			int y = (int) Math.round(cy + r * Math.sin(angle));
			if (i > 0) pts.append(' ');
			pts.append(x).append(',').append(y);
		}
		pts.append('"');
		return pts.toString();
	}

	private String buildStarPoints(int w, int h, int points) {
		StringBuilder pts = new StringBuilder("<polygon points=\"");
		double cx = w / 2.0, cy = h / 2.0;
		double outerR = Math.min(cx, cy) - 1;
		double innerR = outerR * 0.4;
		for (int i = 0; i < points * 2; i++) {
			double angle = Math.PI * i / points - Math.PI / 2;
			double r = (i % 2 == 0) ? outerR : innerR;
			int x = (int) Math.round(cx + r * Math.cos(angle));
			int y = (int) Math.round(cy + r * Math.sin(angle));
			if (i > 0) pts.append(' ');
			pts.append(x).append(',').append(y);
		}
		pts.append('"');
		return pts.toString();
	}

	// -----------------------------------------------------------------------
	// Video logic
	// -----------------------------------------------------------------------

	private void processVideo(String rId) {
		DocxRelTarget rel = relationships.get(rId);
		if (rel == null) {
			log.debug("No relationship found for video rId: {}", rId);
			return;
		}

		String target = rel.target();
		String entryPath = target.startsWith("word/") ? target : "word/" + target;
		String filename = target.contains("/")
				? target.substring(target.lastIndexOf('/') + 1)
				: target;

		if (mediaDir != null) {
			ZipEntry entry = zipFile.getEntry(entryPath);
			if (entry == null) {
				log.debug("Video entry not found in ZIP: {}", entryPath);
				return;
			}
			try {
				// Stream directly to file to avoid loading large videos into memory
				File videoFile = new File(mediaDir, filename);
				try (InputStream is = zipFile.getInputStream(entry);
						FileOutputStream fos = new FileOutputStream(videoFile)) {
					is.transferTo(fos);
				}
				// Emit as a markdown link (not an image — videos are linked, not embedded)
				emitAsBlock("[Video: " + escapeMarkdown(filename) + "](media/" + filename + ") ");
			} catch (IOException e) {
				log.debug("Failed to extract video '{}': {}", filename, e.getMessage());
			}
		}
	}

	// -----------------------------------------------------------------------
	// Image logic
	// -----------------------------------------------------------------------

	private void processImage(String rId) {
		DocxRelTarget rel = relationships.get(rId);
		if (rel == null) {
			log.debug("No relationship found for image rId: {}", rId);
			return;
		}

		String target = rel.target(); // e.g. "media/image1.png"
		// The relationship target is relative to word/; ZipFile entry is word/<target>
		String entryPath = "word/" + target;
		String filename = target.contains("/")
				? target.substring(target.lastIndexOf('/') + 1)
				: target;
		String ext = fileExtension(filename).toLowerCase();

		String mimeType = switch (ext) {
			case "png" -> "image/png";
			case "jpg", "jpeg" -> "image/jpeg";
			case "gif" -> "image/gif";
			case "svg" -> "image/svg+xml";
			case "tiff", "tif" -> "image/tiff";
			case "emf" -> "image/emf";
			case "wmf" -> "image/wmf";
			case "bmp" -> "image/bmp";
			default -> null;
		};

		if (mimeType == null) {
			log.debug("Unsupported image extension '{}' — skipping image '{}'", ext, filename);
			return;
		}

		ZipEntry entry = zipFile.getEntry(entryPath);
		if (entry == null) {
			messages.add(new DocxConversionMessage(Level.WARNING,
					"docx.convert.warn.image.missing", new String[]{filename}));
			return;
		}

		long entrySize = entry.getSize();
		if (entrySize > MAX_IMAGE_BYTES || entrySize < 0) {
			// entrySize == -1 means unknown; still skip if stream read exceeds limit
			if (entrySize > MAX_IMAGE_BYTES) {
				messages.add(new DocxConversionMessage(Level.WARNING,
						"docx.convert.warn.image.skipped", new String[]{filename}));
				return;
			}
		}

		byte[] imageBytes;
		try (InputStream is = zipFile.getInputStream(entry)) {
			imageBytes = is.readAllBytes();
		} catch (IOException e) {
			log.warn("Could not read image entry '{}': {}", entryPath, e.getMessage());
			messages.add(new DocxConversionMessage(Level.WARNING,
					"docx.convert.warn.image.missing", new String[]{filename}));
			return;
		}

		if (imageBytes.length > MAX_IMAGE_BYTES) {
			messages.add(new DocxConversionMessage(Level.WARNING,
					"docx.convert.warn.image.skipped", new String[]{filename}));
			return;
		}

		String alt = (imageAlt != null) ? escapeMarkdown(imageAlt) : "";

		if (mediaDir != null) {
			// Write image to media directory, reference relatively
			try {
				File imageFile = new File(mediaDir, filename);
				log.debug("Writing image: {} ({} bytes)", filename, imageBytes.length);
				try (FileOutputStream fos = new FileOutputStream(imageFile)) {
					fos.write(imageBytes);
				}
				String relativePath = "media/" + filename;
				emitAsBlock(buildImageMarkdown(alt, relativePath));
			} catch (IOException e) {
				log.warn("Failed to write image '{}': {}", filename, e.getMessage());
				messages.add(new DocxConversionMessage(Level.WARNING,
						"docx.convert.warn.image.missing", new String[]{filename}));
			}
		} else {
			// Fallback: inline base64 (for tests or when no media dir is provided)
			String base64 = Base64.getEncoder().encodeToString(imageBytes);
			emitAsBlock(buildImageMarkdown(alt, "data:" + mimeType + ";base64," + base64));
		}
		imageAlt = null;
	}

	// -----------------------------------------------------------------------
	// Text utilities
	// -----------------------------------------------------------------------

	/**
	 * Strips control characters U+0000–U+001F from {@code s}, except
	 * TAB (U+0009), LF (U+000A), and CR (U+000D).
	 */
	private static String stripControlChars(String s) {
		if (s == null) return "";
		int len = s.length();
		StringBuilder sb = null; // lazy init — avoid allocation when nothing to strip
		for (int i = 0; i < len; i++) {
			char c = s.charAt(i);
			boolean strip = c < 0x20 && c != '\t' && c != '\n' && c != '\r';
			if (strip) {
				if (sb == null) {
					sb = new StringBuilder(len);
					sb.append(s, 0, i);
				}
			} else if (sb != null) {
				sb.append(c);
			}
		}
		return (sb != null) ? sb.toString() : s;
	}

	/**
	 * Escapes characters that have special meaning in Markdown when they appear
	 * in user-supplied text content (not in generated Markdown syntax).
	 */
	private static String escapeMarkdown(String text) {
		if (text == null) return "";
		StringBuilder sb = new StringBuilder(text.length() + 8);
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			switch (c) {
				case '\\', '*', '_', '~', '`', '[', ']', '(', ')', '#', '>', '+', '-', '!', '|':
					sb.append('\\').append(c);
					break;
				default:
					sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * Escapes characters special in XML attribute values.
	 */
	private static String escapeXmlAttr(String s) {
		if (s == null) return "";
		return s.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;");
	}

	/**
	 * Escapes characters special in XML text nodes.
	 */
	private static String escapeXmlText(String s) {
		if (s == null) return "";
		return s.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;");
	}

	/**
	 * Returns the file extension of {@code filename} (without the leading dot),
	 * or an empty string if there is none.
	 */
	private static String fileExtension(String filename) {
		int dot = filename.lastIndexOf('.');
		return (dot >= 0 && dot < filename.length() - 1) ? filename.substring(dot + 1) : "";
	}

	// -----------------------------------------------------------------------
	// Attribute helper
	// -----------------------------------------------------------------------

	/**
	 * Returns the value of the first attribute whose qualified name matches
	 * {@code primary} or, if absent, {@code fallback}. Returns {@code null} if
	 * neither is present.
	 */
	private static String attrVal(Attributes attrs, String primary, String fallback) {
		String val = attrs.getValue(primary);
		if (val == null && fallback != null) {
			val = attrs.getValue(fallback);
		}
		return val;
	}

	/**
	 * Returns {@code true} when the attribute value means "enabled":
	 * {@code null} (attribute absent), {@code "true"} (case-insensitive), or {@code "1"}.
	 */
	private static boolean isTrueOrAbsent(String val) {
		return val == null || "true".equalsIgnoreCase(val) || "1".equals(val);
	}
}
