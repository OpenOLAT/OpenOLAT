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

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.impl.HtmlFilter;
import org.olat.core.util.xml.XMLFactories;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;
import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.exception.QtiModelException;
import uk.ac.ed.ph.jqtiplus.node.AbstractNode;
import uk.ac.ed.ph.jqtiplus.node.LoadingContext;
import uk.ac.ed.ph.jqtiplus.node.QtiNode;
import uk.ac.ed.ph.jqtiplus.node.content.basic.Block;
import uk.ac.ed.ph.jqtiplus.node.content.basic.FlowStatic;
import uk.ac.ed.ph.jqtiplus.node.content.basic.InlineStatic;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.serialization.SaxFiringOptions;
import uk.ac.ed.ph.jqtiplus.xmlutils.SimpleDomBuilderHandler;
import uk.ac.ed.ph.jqtiplus.xmlutils.xslt.XsltSerializationOptions;

/**
 * Do the ugly job to convert the Tiny MCE HTML code to the object model
 * of QTI Works
 * 
 * Initial date: 10.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentHtmlBuilder {
	
	private static final Logger log = Tracing.createLoggerFor(AssessmentHtmlBuilder.class);
	private static final String SPACE = " ";
	
	private final QtiSerializer qtiSerializer;
	
	public AssessmentHtmlBuilder() {
		JqtiExtensionManager jqtiExtensionManager = new JqtiExtensionManager();
		qtiSerializer = new QtiSerializer(jqtiExtensionManager);
	}
	
	public AssessmentHtmlBuilder(QtiSerializer qtiSerializer) {
		this.qtiSerializer = qtiSerializer;
	}
	
	public boolean containsSomething(String html) {
		if(!StringHelper.containsNonWhitespace(html)) return false;

		try {
			// return always true? Neko send always an html tag -> content true
			HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALTER_INFOSET);
			ContentDetectionHandler contentHandler = new ContentDetectionHandler();
			parser.setContentHandler(contentHandler);
			parser.parse(new InputSource(new StringReader(html)));
			return contentHandler.isContentAvailable();
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
	}
	
	public String flowStaticString(List<? extends FlowStatic> statics) {
		try(StringOutput sb = new StringOutput()) {
			if(statics != null && !statics.isEmpty()) {
				for(FlowStatic flowStatic:statics) {
					serializeJqtiObject(flowStatic, sb);
				}
			}
			return cleanUpNamespaces(sb);
		} catch(IOException e) {
			log.error("", e);
			return null;
		}
	}
	
	public String blocksString(List<? extends Block> statics) {
		try(StringOutput sb = new StringOutput()) {
			if(statics != null && !statics.isEmpty()) {
				for(Block flowStatic:statics) {
					serializeJqtiObject(flowStatic, sb);
				}
			}
			
			return cleanUpNamespaces(sb);
		} catch(IOException e) {
			log.error("", e);
			return null;
		}
	}
	
	public String inlineStaticString(List<? extends InlineStatic> statics) {
		try(StringOutput sb = new StringOutput()) {
			if(statics != null && !statics.isEmpty()) {
				for(InlineStatic inlineStatic:statics) {
					serializeJqtiObject(inlineStatic, sb);
				}
			}
			return cleanUpNamespaces(sb);
		} catch(IOException e) {
			log.error("", e);
			return null;
		}
	}
	
	private void serializeJqtiObject(QtiNode node, StringOutput sb) {
		final XsltSerializationOptions xsltSerializationOptions = new XsltSerializationOptions();
        xsltSerializationOptions.setIndenting(false);
		qtiSerializer.serializeJqtiObject(node, new StreamResult(sb), new SaxFiringOptions(), xsltSerializationOptions);
	}
	
	private String cleanUpNamespaces(StringOutput sb) {
		String content = sb.toString();
		content = content.replace(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"", "");
		content = content.replace("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"", "");
		content = content.replace("\n   xmlns=\"http://www.imsglobal.org/xsd/imsqti_v2p1\"", "");
		content = content.replace(" xmlns=\"http://www.imsglobal.org/xsd/imsqti_v2p1\"", "");
		content = content.replace("xmlns=\"http://www.imsglobal.org/xsd/imsqti_v2p1\"", "");
		content = content.replace("\n   xsi:schemaLocation=\"http://www.imsglobal.org/xsd/imsqti_v2p1 http://www.imsglobal.org/xsd/imsqti_v2p1.xsd\"", "");
		content = content.replace(" xsi:schemaLocation=\"http://www.imsglobal.org/xsd/imsqti_v2p1 http://www.imsglobal.org/xsd/imsqti_v2p1.xsd\"", "");
		content = content.replace("xsi:schemaLocation=\"http://www.imsglobal.org/xsd/imsqti_v2p1 http://www.imsglobal.org/xsd/imsqti_v2p1.xsd\"", "");
		return content.trim();
	}
	
	public void appendHtml(AbstractNode parent, String htmlFragment) {
		appendHtml(parent, htmlFragment, false);
	}
	
	public void appendHtml(AbstractNode parent, String htmlFragment, boolean convertInputValue) {
		if(StringHelper.containsNonWhitespace(htmlFragment)) {
			htmlFragment = htmlFragment.trim();
			//tinymce bad habits
			if(StringHelper.isHtml(htmlFragment)) {
				if(htmlFragment.startsWith("<p>&nbsp;")) {
					htmlFragment = htmlFragment.replace("<p>&nbsp;", "<p>");
				}
			} else {
				htmlFragment = "<p>" + htmlFragment + "</p>";
			}
			//root element is an <html> element
			Document document = filter(htmlFragment, convertInputValue);
			if(document != null) {
				Element docElement = document.getDocumentElement();
				cleanUpNamespaces(docElement);
				// load the elements under the <html> root element
				parent.getNodeGroups().load(docElement, new HTMLLoadingContext());
			}
		}
	}
	
	private void cleanUpNamespaces(Element element) {
		Attr xsiattr = element.getAttributeNode("xmlns:xsi");
		if(xsiattr != null && "http://www.w3.org/2001/XMLSchema-instance".equals(xsiattr.getValue())) {
			element.removeAttribute("xmlns:xsi");
		}
		Attr attr = element.getAttributeNode("xmlns");
		if(attr != null && "http://www.imsglobal.org/xsd/imsqti_v2p1".equals(attr.getValue())) {
			element.removeAttribute("xmlns");
		}
		
		// escaped by the HTML parser -> always remove them
		Attr schemaLocationAttr = element.getAttributeNode("xsiU00003Aschemalocation");
		if(schemaLocationAttr != null) {
			element.removeAttribute("xsiU00003Aschemalocation");
		}
		Attr xsiattrEscaped = element.getAttributeNode("xmlnsU00003Axsi");
		if(xsiattrEscaped != null) {
			element.removeAttribute("xmlnsU00003Axsi");
		}

		for(Node child=element.getFirstChild(); child != null; child = child.getNextSibling()) {
			if(child instanceof Element) {
				cleanUpNamespaces((Element)child);
			}
		}
	}

	/**
	 * The method filters the content of textEntryInteraction (rename it correctly
	 * and remove TinyMCE attributes) and repackage the video from TinyMCE format
	 * to an object suited for QTI 2.1.
	 * 
	 * @param content The content to filter
	 * @return A document object with an HTML root element with the actual
	 * 		blocks under this root element.
	 */
	private Document filter(String content, boolean convertInputValue) {
		try {
			// Alter infoset because of special namespace on tag p
			HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALTER_INFOSET);
			DocumentBuilderFactory factory = XMLFactories.newDocumentBuilderFactory();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();
			HtmlToDomBuilderHandler contentHandler = new HtmlToDomBuilderHandler(document, convertInputValue);
			parser.setContentHandler(contentHandler);
			parser.parse(new InputSource(new StringReader(content)));
			return document;
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	/**
	 * Convert:<br>
	 * <ul>
	 * 		<li>textentryinteraction -> camel cased textEntryInteraction</li>
	 * </ul>
	 * 
	 * Initial date: 26.02.2016<br>
	 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
	 *
	 */
	private static class HtmlToDomBuilderHandler extends SimpleDomBuilderHandler {
		
		private boolean video = false;
		private StringBuilder scriptBuffer = new StringBuilder();
		
		private final boolean convertInputValue;
		
		public HtmlToDomBuilderHandler(Document document, boolean convertInputValue) {
			super(document);
			this.convertInputValue = convertInputValue;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			if(video || "head".equalsIgnoreCase(localName) || "body".equalsIgnoreCase(localName)) {
				return;
			}

			if("textentryinteraction".equalsIgnoreCase(localName)) {
				localName = qName = "textEntryInteraction";

				AttributesImpl attributesCleaned = new AttributesImpl("");
				for(int i=0; i<attributes.getLength(); i++) {
					String name = attributes.getLocalName(i);
					if(!"openolattype".equalsIgnoreCase(name)
							&& !"data-qti-solution".equalsIgnoreCase(name)
							&& !"data-qti-solution-empty".equalsIgnoreCase(name)) {
						String value = attributes.getValue(i);
						attributesCleaned.addAttribute(name, value);
					}
				}

				attributes = new AttributesDelegate(attributesCleaned);
				super.startElement(uri, localName, qName, attributes);
				super.endElement(uri, localName, qName);
				return;
			} else if(convertInputValue && "input".equalsIgnoreCase(localName)) {
				for(int i=0; i<attributes.getLength(); i++) {
					String name = attributes.getLocalName(i);
					if("value".equalsIgnoreCase(name)) {
						String val = attributes.getValue(i);
						if(val != null) {
							char[] valArray = val.toCharArray();
							localName = qName = "span";

							AttributesImpl attributesCleaned = new AttributesImpl("");
							attributesCleaned.addAttribute("class", "o_input_value o_input_value_wrapper");
							super.startElement(uri, localName, qName, attributesCleaned);
							super.characters(valArray, 0, valArray.length);
							super.endElement(uri, localName, qName);
						}
					}
				}
				return;
			} else if("span".equalsIgnoreCase(localName)) {
				String cssClass = attributes.getValue("class");
				if(cssClass != null && "olatFlashMovieViewer".equals(cssClass)) {
					video = true;
					return;
				}
			} else if("u".equalsIgnoreCase(localName)) {
				qName = "span";
				AttributesImpl underlineAttributes = new AttributesImpl("");
				underlineAttributes.addAttributes(attributes);
				underlineAttributes.addAttribute("style", "text-decoration: underline;");
				attributes = underlineAttributes;
			}
			super.startElement(uri, localName, qName, attributes);
		}

		@Override
		public void characters(char[] ch, int start, int length) {
			if(video) {
				scriptBuffer.append(new String(ch, start, length));
			} else {
				super.characters(ch, start, length);
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) {
			if("head".equalsIgnoreCase(localName) || "body".equalsIgnoreCase(localName)) {
				return;
			}
			if(video) {
				if("span".equalsIgnoreCase(localName)) {
					String content = scriptBuffer.toString();
					String startScript = "BPlayer.insertPlayer(";
					int start = content.indexOf(startScript);
					if(start >= 0) {
						int end = content.indexOf(')', start);
						String parameters = content.substring(start + startScript.length(), end);
						translateToObject(parameters);
					}
					scriptBuffer = new StringBuilder();
					video = false;
				}
				return;
			}
			if("textentryinteraction".equalsIgnoreCase(localName)
					|| (convertInputValue && "input".equalsIgnoreCase(localName))) {
				return;
			}
			super.endElement(uri, localName, qName);
		}
		
		private void translateToObject(String parameters) {
			String[] array = parameters.split(",");
			
			String data = array[0].replace("\"", "");
			String id = array[1].replace("\"", "");
			String width = array[2];
			String height = array[3];
			String type = array[6].replace("\"", "");
			String ooData = parameters.replace("\"", "'");

			AttributesImpl attributes = new AttributesImpl("");
			attributes.addAttribute("data", data);
			attributes.addAttribute("id", id);
			attributes.addAttribute("class", "olatFlashMovieViewer");
			attributes.addAttribute("width", width);
			attributes.addAttribute("height", height);
			attributes.addAttribute("type", type);
			attributes.addAttribute("data-oo-movie", ooData);

			super.startElement("", "object", "object", attributes);
			// ensure the tag is written <object> </object> and <object />
			super.characters(SPACE.toCharArray(), 0, SPACE.length());
			super.endElement("", "object", "object");
		}
	}
	
	private static class AttributeImpl {
		private String name;
		private String value;
		
		public AttributeImpl(String name, String value) {
			this.name = name;
			this.value = value;
		}
		
		public String getName() {
			return name;
		}
		
		public String getValue() {
			return value;
		}
	}
	
	/**
	 * This implementation is specifically coded for the SimpleDomBuilderHandler
	 * and only for this handler. It implements only the needed methods by this
	 * handler implementation.
	 * 
	 * Initial date: 07.09.2016<br>
	 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
	 *
	 */
	private static class AttributesImpl implements Attributes {
		
		private String attributesUri;
		private List<AttributeImpl> attributes = new ArrayList<>();
		
		public AttributesImpl(String uri) {
			this.attributesUri = uri;
		}
		
		public void addAttributes(Attributes attrs) {
			for(int i=0; i<attrs.getLength(); i++) {
				String name = attrs.getLocalName(i);
				String value = attrs.getValue(i);
				addAttribute(name, value);
			}
		}
		
		public void addAttribute(String name, String value) {
			attributes.add(new AttributeImpl(name, value));
		}

		@Override
		public int getLength() {
			return attributes.size();
		}

		@Override
		public String getURI(int index) {
			return attributesUri;
		}

		@Override
		public String getLocalName(int index) {
			return attributes.get(index).getName();
		}

		@Override
		public String getQName(int index) {
			return attributes.get(index).getName();
		}

		@Override
		public String getType(int index) {
			return null;
		}

		@Override
		public String getValue(int index) {
			return attributes.get(index).getValue();
		}

		@Override
		public int getIndex(String uri, String localName) {
			return 0;
		}

		@Override
		public int getIndex(String qName) {
			return 0;
		}

		@Override
		public String getType(String uri, String localName) {
			return null;
		}

		@Override
		public String getType(String qName) {
			return null;
		}

		@Override
		public String getValue(String uri, String localName) {
			return null;
		}

		@Override
		public String getValue(String qName) {
			return null;
		}
	}
	
	/**
	 * Convert:<br>
	 * <ul>
	 * 		<li>responseidentifier -> camel cased responseIdentifier</li>
	 * 		<li>and other attributes of textEntryInteraction</li>
	 * </ul>
	 * 
	 * Initial date: 26.02.2016<br>
	 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
	 *
	 */
	private static class AttributesDelegate implements Attributes {
		
		private final Attributes attributes;
		
		public AttributesDelegate(Attributes attributes) {
			this.attributes = attributes;
		}

		@Override
		public int getLength() {
			return attributes.getLength();
		}

		@Override
		public String getURI(int index) {
			return attributes.getURI(index);
		}

		@Override
		public String getLocalName(int index) {
			String localName = attributes.getLocalName(index);
			return translateAttributeName(localName);
		}

		@Override
		public String getQName(int index) {
			String qName = attributes.getQName(index);
			return translateAttributeName(qName);
		}
		
		private final String translateAttributeName(String attrName) {
			if(attrName != null) {
				switch(attrName) {
					case "responseidentifier": return "responseIdentifier";
					case "placeholdertext": return "placeholderText";
					case "expectedlength": return "expectedLength";
					case "patternmask": return "patternMask";
					default: return attrName;
				}
			}
			return attrName;
		}

		@Override
		public String getType(int index) {
			return attributes.getType(index);
		}

		@Override
		public String getValue(int index) {
			return attributes.getValue(index);
		}

		@Override
		public int getIndex(String uri, String localName) {
			return attributes.getIndex(uri, localName);
		}

		@Override
		public int getIndex(String qName) {
			return attributes.getIndex(qName.toLowerCase());
		}

		@Override
		public String getType(String uri, String localName) {
			return attributes.getType(uri, localName);
		}

		@Override
		public String getType(String qName) {
			return attributes.getType(qName);
		}

		@Override
		public String getValue(String uri, String localName) {
			return attributes.getValue(uri, localName);
		}

		@Override
		public String getValue(String qName) {
			return attributes.getValue(qName);
		}
	}
	
	private static final class HTMLLoadingContext implements LoadingContext {

		@Override
		public JqtiExtensionManager getJqtiExtensionManager() {
			return null;
		}

		@Override
		public void modelBuildingError(QtiModelException exception, Node badNode) {
			//
		}
	}
	
	private static class ContentDetectionHandler extends DefaultHandler {
		
		private boolean collect = false;
		private boolean content = false;
		
		public boolean isContentAvailable() {
			return content;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			String elem = localName.toLowerCase();
			if("script".equals(elem)) {
				collect = false;
			} else if(!HtmlFilter.blockTags.contains(localName)) {
				content = true;
			}
		}
		
		@Override
		public void characters(char[] chars, int offset, int length) {
			if(!content && collect && offset >= 0 && length > 0) {
				String text = new String(chars, offset, length);
				if(text.trim().length() > 0) {
					content = true;
				}
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) {
			String elem = localName.toLowerCase();
			if("script".equals(elem)) {
				collect = true;
			} else if(!HtmlFilter.blockTags.contains(localName)) {
				content = true;
			}
		}
	}
}
